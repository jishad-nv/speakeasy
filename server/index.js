import 'dotenv/config';
import express from 'express';
import cors from 'cors';
import rateLimit from 'express-rate-limit';
import cron from 'node-cron';
import webpush from 'web-push';
import Database from 'better-sqlite3';
import { GoogleGenAI, Type } from '@google/genai';

const app = express();
const PORT = process.env.PORT || 8080;
const DAILY_AI_CAP = Number(process.env.DAILY_AI_CAP || 60);
const MAX_MEDIA_MB = Number(process.env.MAX_MEDIA_MB || 10);
const MAX_MEDIA_BYTES = MAX_MEDIA_MB * 1024 * 1024;

app.use(cors({ origin: process.env.ALLOWED_ORIGIN || '*' }));
app.use(express.json({ limit: '14mb' }));

// Rate limiting for AI and push routes
const apiLimiter = rateLimit({
  windowMs: 60 * 1000,
  max: 45,
  standardHeaders: true,
  legacyHeaders: false,
  message: { error: 'Too many requests. Please wait a moment and try again.' }
});
app.use('/api/', apiLimiter);

// Gemini SDK (server-side only; GEMINI_API_KEY is never exposed to clients)
const geminiKey = process.env.GEMINI_API_KEY;
const ai = geminiKey ? new GoogleGenAI({ apiKey: geminiKey }) : null;
const MODEL_ID = 'gemini-2.5-flash';

// Web Push VAPID configuration
if (process.env.VAPID_PUBLIC_KEY && process.env.VAPID_PRIVATE_KEY) {
  webpush.setVapidDetails(
    process.env.VAPID_SUBJECT || 'mailto:coach@speakup.app',
    process.env.VAPID_PUBLIC_KEY,
    process.env.VAPID_PRIVATE_KEY
  );
}

// SQLite storage for profiles, mistakes, IELTS history, saved words, daily AI caps, analytics, and push subscriptions
const db = new Database('speakup.db');
db.exec(`
  CREATE TABLE IF NOT EXISTS users (
    user_id TEXT PRIMARY KEY,
    profile_json TEXT NOT NULL,
    updated_at INTEGER NOT NULL
  );
  CREATE TABLE IF NOT EXISTS mistakes (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    category TEXT NOT NULL,
    wrong_pattern TEXT NOT NULL,
    correct_form TEXT NOT NULL,
    malayalam_note TEXT NOT NULL,
    occurrences INTEGER DEFAULT 1,
    fixed_streak INTEGER DEFAULT 0,
    is_fixed INTEGER DEFAULT 0,
    updated_at INTEGER NOT NULL
  );
  CREATE TABLE IF NOT EXISTS ielts_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    skill TEXT NOT NULL,
    band_score REAL NOT NULL,
    details_json TEXT NOT NULL,
    created_at INTEGER NOT NULL
  );
  CREATE TABLE IF NOT EXISTS saved_words (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    word TEXT NOT NULL,
    malayalam_meaning TEXT NOT NULL,
    source_type TEXT DEFAULT 'general',
    created_at INTEGER NOT NULL,
    UNIQUE(user_id, word)
  );
  CREATE TABLE IF NOT EXISTS ai_usage_daily (
    user_id TEXT NOT NULL,
    day_key TEXT NOT NULL,
    calls_count INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, day_key)
  );
  CREATE TABLE IF NOT EXISTS push_subscriptions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    endpoint TEXT UNIQUE NOT NULL,
    subscription_json TEXT NOT NULL,
    timezone TEXT NOT NULL,
    reminder_times_json TEXT NOT NULL,
    sent_today_count INTEGER DEFAULT 0,
    last_sent_date TEXT DEFAULT ''
  );
  CREATE TABLE IF NOT EXISTS analytics_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    event_name TEXT NOT NULL,
    payload_json TEXT,
    created_at INTEGER NOT NULL
  );
`);

function sanitizeText(input, maxLen = 2000) {
  return String(input || '')
    .replace(/[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g, ' ')
    .trim()
    .slice(0, maxLen);
}

function checkAndIncrementAiCap(userId = 'default') {
  const safeUser = sanitizeText(userId, 64) || 'default';
  const dayKey = new Date().toISOString().slice(0, 10);
  const row = db.prepare(`SELECT calls_count FROM ai_usage_daily WHERE user_id = ? AND day_key = ?`).get(safeUser, dayKey);
  if (row && row.calls_count >= DAILY_AI_CAP) {
    return false;
  }
  db.prepare(`
    INSERT INTO ai_usage_daily (user_id, day_key, calls_count)
    VALUES (?, ?, 1)
    ON CONFLICT(user_id, day_key) DO UPDATE SET calls_count = calls_count + 1
  `).run(safeUser, dayKey);
  return true;
}

function validateBase64Media(base64Data) {
  if (!base64Data || typeof base64Data !== 'string') return { ok: false, reason: 'Missing base64 media data' };
  const stripped = base64Data.includes(',') ? base64Data.split(',')[1] : base64Data;
  const approxBytes = Math.ceil((stripped.length * 3) / 4);
  if (approxBytes > MAX_MEDIA_BYTES) {
    return { ok: false, reason: `Media exceeds ${MAX_MEDIA_MB}MB limit` };
  }
  return { ok: true, base64: stripped };
}

async function generateStructuredJson(contents, responseSchema) {
  if (!ai) {
    throw new Error('GEMINI_API_KEY is not configured on the server.');
  }
  const response = await ai.models.generateContent({
    model: MODEL_ID,
    contents,
    config: {
      responseMimeType: 'application/json',
      responseSchema
    }
  });
  return JSON.parse(response.text);
}

// 1. POST /api/placement
app.post('/api/placement', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const answers = Array.isArray(req.body.answers) ? req.body.answers.map((a) => sanitizeText(a, 400)) : [];
  try {
    if (!checkAndIncrementAiCap(userId)) throw new Error('Daily AI cap reached');
    const schema = {
      type: Type.OBJECT,
      properties: {
        level: {
          type: Type.STRING,
          enum: ['BEGINNER', 'ELEMENTARY', 'INTERMEDIATE', 'UPPER_INTERMEDIATE', 'ADVANCED', 'MASTER']
        },
        score: { type: Type.INTEGER },
        encouragement: { type: Type.STRING }
      },
      required: ['level', 'score', 'encouragement']
    };
    const prompt = `Evaluate these 5 spoken English placement responses from a Malayalam speaker (no grammar jargon). Assign one of BEGINNER, ELEMENTARY, INTERMEDIATE, UPPER_INTERMEDIATE, ADVANCED, MASTER.\nAnswers: ${JSON.stringify(answers)}`;
    const result = await generateStructuredJson(prompt, schema);
    res.json({ ...result, offlineFallback: false });
  } catch (err) {
    const wordCount = answers.map((a) => a.split(/\s+/).filter(Boolean).length).reduce((s, n) => s + n, 0);
    const level = wordCount >= 45 ? 'ADVANCED' : wordCount >= 30 ? 'UPPER_INTERMEDIATE' : wordCount >= 18 ? 'INTERMEDIATE' : wordCount >= 8 ? 'ELEMENTARY' : 'BEGINNER';
    res.json({
      level,
      score: Math.min(100, wordCount * 2),
      encouragement: 'Great start! Leo is ready to guide your daily speaking.',
      offlineFallback: true
    });
  }
});

// 2. POST /api/chat
app.post('/api/chat', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const message = sanitizeText(req.body.message, 600);
  const { topic = 'Daily Life', level = 'BEGINNER', personality = 'FRIENDLY', correctMe = true, askMalayalam = false, history = [] } = req.body;
  if (!message) return res.status(400).json({ error: 'message is required' });

  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI practice cap reached. Offline tasks still work!' });
    const schema = {
      type: Type.OBJECT,
      properties: {
        reply: { type: Type.STRING },
        correctedSentence: { type: Type.STRING, nullable: true },
        malayalamHelp: { type: Type.STRING, nullable: true }
      },
      required: ['reply']
    };
    const prompt = `You are Leo (${sanitizeText(personality, 32)} coach) speaking with a Malayalam speaker at level ${sanitizeText(level, 32)} on topic "${sanitizeText(topic, 80)}".
Rules:
1. Reply in 1-3 short spoken sentences matching ${level} vocabulary.
2. Always end with a simple follow-up question.
3. No grammar rules! If correctMe=${Boolean(correctMe)} and the learner made a mistake, put a natural corrected rephrase in correctedSentence; else null.
4. Provide malayalamHelp ONLY if askMalayalam=${Boolean(askMalayalam)}; otherwise null.
History: ${JSON.stringify(history.slice(-6))}
User: ${message}`;
    const result = await generateStructuredJson(prompt, schema);
    res.json(result);
  } catch (err) {
    res.status(503).json({
      error: 'Leo needs internet to chat',
      offlineFallback: true,
      reply: 'That sounds great! Tell me a little more about what happened next?'
    });
  }
});

// 3. POST /api/feedback
app.post('/api/feedback', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const targetSentence = sanitizeText(req.body.targetSentence, 500);
  const transcript = sanitizeText(req.body.transcript, 500);
  try {
    if (!checkAndIncrementAiCap(userId)) throw new Error('Daily AI cap reached');
    const schema = {
      type: Type.OBJECT,
      properties: {
        score: { type: Type.INTEGER },
        mispronouncedWords: { type: Type.ARRAY, items: { type: Type.STRING } },
        tip: { type: Type.STRING }
      },
      required: ['score', 'mispronouncedWords', 'tip']
    };
    const prompt = `Compare target English sentence "${targetSentence}" with spoken transcript "${transcript}". Return score 0-100, list of mispronounced/missed words, and ONE short friendly speaking tip (no grammar jargon).`;
    const result = await generateStructuredJson(prompt, schema);
    res.json({ ...result, offlineScore: false });
  } catch (err) {
    const targetWords = targetSentence.toLowerCase().replace(/[^a-z0-9 ]/g, '').split(/\s+/).filter(Boolean);
    const spokenSet = new Set(transcript.toLowerCase().replace(/[^a-z0-9 ]/g, '').split(/\s+/).filter(Boolean));
    const missed = targetWords.filter((w) => !spokenSet.has(w));
    const score = targetWords.length ? Math.round(((targetWords.length - missed.length) / targetWords.length) * 100) : 70;
    res.json({
      score,
      mispronouncedWords: missed,
      tip: missed.length ? `Speak these clearly: ${missed.slice(0, 3).join(', ')}` : 'Clear pacing and strong pronunciation!',
      offlineScore: true
    });
  }
});

// 4. POST /api/daily-plan
app.post('/api/daily-plan', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const { level = 'BEGINNER', goal = 'DAILY_LIFE', weakWords = [], dailyMinutes = 10 } = req.body;
  try {
    if (!checkAndIncrementAiCap(userId)) throw new Error('Daily AI cap reached');
    const schema = {
      type: Type.OBJECT,
      properties: {
        focusNote: { type: Type.STRING },
        tasks: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              type: { type: Type.STRING },
              title: { type: Type.STRING },
              prompt: { type: Type.STRING },
              malayalamHint: { type: Type.STRING }
            },
            required: ['type', 'title', 'prompt', 'malayalamHint']
          }
        }
      },
      required: ['focusNote', 'tasks']
    };
    const prompt = `Create today's 5 speaking tasks (SHADOW_SPEAKING, WORD_OF_THE_DAY, SITUATION_ROLEPLAY, LISTEN_AND_ANSWER, CHALLENGE) for a Malayalam speaker. Level: ${sanitizeText(level, 32)}, Goal: ${sanitizeText(goal, 32)}, Time: ${Number(dailyMinutes) || 10} min, Weak words to review: ${JSON.stringify(weakWords.slice(0, 8))}. No grammar lessons.`;
    const result = await generateStructuredJson(prompt, schema);
    res.json(result);
  } catch (err) {
    res.json({
      focusNote: 'Offline daily plan ready!',
      tasks: [
        { type: 'SHADOW_SPEAKING', title: 'Shadow Speaking', prompt: 'Could you help me with this, please?', malayalamHint: 'ഇതിൽ എന്നെ ഒന്ന് സഹായിക്കാമോ?' },
        { type: 'WORD_OF_THE_DAY', title: 'Word of the Day', prompt: weakWords[0] || 'Confident', malayalamHint: 'ആത്മവിശ്വാസമുള്ള' },
        { type: 'SITUATION_ROLEPLAY', title: 'Situation Roleplay', prompt: 'Ordering at a cafe', malayalamHint: 'കഫേയിൽ ഓർഡർ ചെയ്യുക' },
        { type: 'LISTEN_AND_ANSWER', title: 'Listen & Answer', prompt: 'What are your plans for this evening?', malayalamHint: 'ഇന്ന് വൈകുന്നേരം എന്താണ് പ്ലാൻ?' },
        { type: 'CHALLENGE', title: 'Daily Challenge', prompt: 'Describe your favorite place in 30 seconds.', malayalamHint: 'ഇഷ്ടപ്പെട്ട സ്ഥലത്തെക്കുറിച്ച് 30 സെക്കൻഡ് പറയൂ.' }
      ]
    });
  }
});

// 5. POST /api/weekly-summary
app.post('/api/weekly-summary', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const { speakingMinutes = 0, newWordsCount = 0, averageScore = 0, streakDays = 1 } = req.body;
  try {
    if (!checkAndIncrementAiCap(userId)) throw new Error('Daily AI cap reached');
    const schema = {
      type: Type.OBJECT,
      properties: {
        headline: { type: Type.STRING },
        summary: { type: Type.STRING },
        nextWeekFocus: { type: Type.STRING }
      },
      required: ['headline', 'summary', 'nextWeekFocus']
    };
    const prompt = `Write a warm 2-sentence AI weekly speaking summary for a Malayalam speaker: ${Number(speakingMinutes)} speaking minutes, ${Number(newWordsCount)} new words, ${Number(averageScore)}% average score, ${Number(streakDays)}-day streak.`;
    const result = await generateStructuredJson(prompt, schema);
    res.json(result);
  } catch (err) {
    res.json({
      headline: `${speakingMinutes} Minutes Spoken This Week!`,
      summary: `You practiced ${newWordsCount} words with an average score of ${averageScore}% and kept a ${streakDays}-day streak.`,
      nextWeekFocus: 'Try one extra roleplay turn each day to boost natural flow.'
    });
  }
});

// 6. POST /api/pronunciation (Audio input -> Gemini pronunciation coach)
app.post('/api/pronunciation', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const targetSentence = sanitizeText(req.body.targetSentence, 400);
  const mimeType = sanitizeText(req.body.mimeType || 'audio/mp4', 40);
  const supportLevel = sanitizeText(req.body.supportLevel || 'MIXED', 24);
  const media = validateBase64Media(req.body.audioBase64);

  if (!targetSentence) return res.status(400).json({ error: 'targetSentence is required' });
  if (!media.ok) return res.status(400).json({ error: media.reason });

  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI cap reached' });
    const schema = {
      type: Type.OBJECT,
      properties: {
        overallScore: { type: Type.INTEGER },
        wordScores: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              word: { type: Type.STRING },
              score: { type: Type.INTEGER },
              note: { type: Type.STRING }
            }
          }
        },
        soundErrors: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              soundTargetId: { type: Type.STRING },
              issue: { type: Type.STRING },
              correctionTip: { type: Type.STRING }
            }
          }
        },
        tip: { type: Type.STRING }
      },
      required: ['overallScore', 'wordScores', 'soundErrors', 'tip']
    };
    const contents = [
      { inlineData: { data: media.base64, mimeType } },
      {
        text: `Analyze this Malayalam speaker's spoken audio against "${targetSentence}". Focus on th/dh, v vs w, f vs p, z buzz, short/long vowels, word stress, and rhythm. Support level: ${supportLevel}. Give practical coaching tip without grammar rules.`
      }
    ];
    const result = await generateStructuredJson(contents, schema);
    res.json({ ...result, isOfflineFallback: false });
  } catch (err) {
    res.json({
      overallScore: 76,
      wordScores: targetSentence.split(/\s+/).slice(0, 6).map((w) => ({ word: w, score: 76, note: 'Practice slowly' })),
      soundErrors: [{ soundTargetId: 'th_dh', issue: 'Offline audio analysis unavailable', correctionTip: 'Listen to slow playback and compare mouth position.' }],
      tip: 'പതുക്കെ കേട്ട് വീണ്ടും പറഞ്ഞ് നോക്കൂ.',
      isOfflineFallback: true
    });
  }
});

// 7. POST /api/translate-learn ("Say in Malayalam, Learn in English")
app.post('/api/translate-learn', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const malayalamText = sanitizeText(req.body.text, 500);
  const level = sanitizeText(req.body.level || 'BEGINNER', 32);
  const sourceLanguage = sanitizeText(req.body.sourceLanguage || 'Malayalam', 32);
  if (!malayalamText) return res.status(400).json({ error: 'text is required' });

  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI cap reached' });
    const schema = {
      type: Type.OBJECT,
      properties: {
        formalEnglish: { type: Type.STRING },
        casualEnglish: { type: Type.STRING },
        manglishFormal: { type: Type.STRING },
        manglishCasual: { type: Type.STRING },
        contextNote: { type: Type.STRING }
      },
      required: ['formalEnglish', 'casualEnglish', 'manglishFormal', 'manglishCasual', 'contextNote']
    };
    const prompt = `A ${sourceLanguage} speaker (level ${level}) wants to express: "${malayalamText}". Return formalEnglish, casualEnglish, phonetic pronunciation in ${sourceLanguage} script (manglishFormal, manglishCasual), and one brief ${sourceLanguage} usage contextNote.`;
    const result = await generateStructuredJson(prompt, schema);
    res.json({ ...result, isAiGenerated: true });
  } catch (err) {
    res.status(503).json({ error: 'Translation unavailable offline', isAiGenerated: false });
  }
});

// 8. GET & POST /api/mistakes (Personal Mistake Notebook sync)
app.get('/api/mistakes', (req, res) => {
  const userId = sanitizeText(req.query.userId || 'default', 64);
  const rows = db.prepare(`SELECT * FROM mistakes WHERE user_id = ? ORDER BY is_fixed ASC, occurrences DESC, updated_at DESC LIMIT 50`).all(userId);
  res.json({ mistakes: rows });
});

app.post('/api/mistakes', (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const items = Array.isArray(req.body.mistakes) ? req.body.mistakes : [];
  const stmt = db.prepare(`
    INSERT INTO mistakes (id, user_id, category, wrong_pattern, correct_form, malayalam_note, occurrences, fixed_streak, is_fixed, updated_at)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    ON CONFLICT(id) DO UPDATE SET
      occurrences = MAX(occurrences, excluded.occurrences),
      fixed_streak = excluded.fixed_streak,
      is_fixed = excluded.is_fixed,
      updated_at = excluded.updated_at
  `);
  const now = Date.now();
  for (const m of items.slice(0, 50)) {
    stmt.run(
      sanitizeText(m.id, 64),
      userId,
      sanitizeText(m.category || 'sentence_pattern', 32),
      sanitizeText(m.wrongPattern, 160),
      sanitizeText(m.correctForm, 200),
      sanitizeText(m.malayalamNote, 240),
      Number(m.occurrences || 1),
      Number(m.fixedStreak || 0),
      m.isFixed ? 1 : 0,
      now
    );
  }
  res.json({ synced: true, count: items.length });
});

// 9. POST /api/scene (Real-Life Simulator turn or final report)
app.post('/api/scene', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const { mode = 'turn', scenarioTitle = 'Phone Call', characterName = 'Priya', userMessage = '', difficulty = 2, surpriseTwist = '', history = [] } = req.body;
  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI cap reached' });
    if (mode === 'report') {
      const schema = {
        type: Type.OBJECT,
        properties: {
          fluencyScore: { type: Type.INTEGER },
          confidenceScore: { type: Type.INTEGER },
          newWordsLearned: { type: Type.ARRAY, items: { type: Type.STRING } },
          improvementPoints: { type: Type.ARRAY, items: { type: Type.STRING } }
        },
        required: ['fluencyScore', 'confidenceScore', 'newWordsLearned', 'improvementPoints']
      };
      const prompt = `Score this ${sanitizeText(scenarioTitle, 64)} roleplay performance: ${JSON.stringify(history.slice(-12))}. Return fluencyScore, confidenceScore, newWordsLearned (3 items), and 3 improvementPoints (no grammar rules).`;
      const result = await generateStructuredJson(prompt, schema);
      return res.json(result);
    }

    const schema = {
      type: Type.OBJECT,
      properties: {
        characterReply: { type: Type.STRING },
        malayalamHint: { type: Type.STRING },
        newUsefulPhrase: { type: Type.STRING }
      },
      required: ['characterReply', 'malayalamHint', 'newUsefulPhrase']
    };
    const prompt = `Roleplay as ${sanitizeText(characterName, 40)} in ${sanitizeText(scenarioTitle, 60)}. Difficulty ${Number(difficulty) || 2}/5. ${surpriseTwist ? `Include this twist: ${sanitizeText(surpriseTwist, 200)}` : ''} Reply in 1-2 short spoken sentences ending with a question. History: ${JSON.stringify(history.slice(-6))}. Learner: ${sanitizeText(userMessage, 400)}`;
    const result = await generateStructuredJson(prompt, schema);
    res.json(result);
  } catch (err) {
    res.json({
      characterReply: 'Could you say that once more slowly so I can confirm it for you?',
      malayalamHint: 'ഒരിക്കൽ കൂടി വ്യക്തമായി പറയൂ.',
      newUsefulPhrase: 'Could you please confirm that?'
    });
  }
});

// 10. POST /api/camera (Gemini Vision vocabulary extraction)
app.post('/api/camera', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const mimeType = sanitizeText(req.body.mimeType || 'image/jpeg', 40);
  const media = validateBase64Media(req.body.imageBase64);
  if (!media.ok) return res.status(400).json({ error: media.reason });

  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI cap reached' });
    const schema = {
      type: Type.OBJECT,
      properties: {
        sceneSummaryMl: { type: Type.STRING },
        items: {
          type: Type.ARRAY,
          items: {
            type: Type.OBJECT,
            properties: {
              englishText: { type: Type.STRING },
              malayalamMeaning: { type: Type.STRING },
              manglishPronunciation: { type: Type.STRING },
              exampleSentence: { type: Type.STRING }
            }
          }
        }
      },
      required: ['sceneSummaryMl', 'items']
    };
    const contents = [
      { inlineData: { data: media.base64, mimeType } },
      { text: 'Extract up to 8 practical English words/phrases from this photo with Malayalam meanings, Manglish pronunciation in Malayalam script, and an example spoken sentence.' }
    ];
    const result = await generateStructuredJson(contents, schema);
    res.json(result);
  } catch (err) {
    res.status(503).json({ error: 'Camera extraction unavailable offline.' });
  }
});

// 11. IELTS Endpoints (/api/ielts/speaking, /api/ielts/writing, /api/ielts/plan)
app.post('/api/ielts/speaking', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const part = Number(req.body.part || 1);
  const question = sanitizeText(req.body.question, 400);
  const transcript = sanitizeText(req.body.transcript, 2500);
  const durationSeconds = Number(req.body.durationSeconds || 45);
  if (!transcript) return res.status(400).json({ error: 'transcript is required' });

  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI cap reached' });
    const schema = {
      type: Type.OBJECT,
      properties: {
        bandScore: { type: Type.NUMBER },
        fluency: { type: Type.NUMBER },
        lexical: { type: Type.NUMBER },
        grammar: { type: Type.NUMBER },
        pronunciation: { type: Type.NUMBER },
        fillerWordCount: { type: Type.INTEGER },
        wordsPerMinute: { type: Type.INTEGER },
        feedbackSummary: { type: Type.STRING },
        modelImprovedAnswer: { type: Type.STRING },
        weakestFocus: { type: Type.STRING }
      },
      required: ['bandScore', 'fluency', 'lexical', 'grammar', 'pronunciation', 'fillerWordCount', 'wordsPerMinute', 'feedbackSummary', 'modelImprovedAnswer', 'weakestFocus']
    };
    const prompt = `IELTS Speaking Part ${part}. Prompt: "${question}". Candidate response (${durationSeconds}s): "${transcript}". Return bandScore (0-9 in 0.5 steps), 4 criteria, fillerWordCount, WPM, feedbackSummary, weakestFocus, and Band 8 modelImprovedAnswer.`;
    const result = await generateStructuredJson(prompt, schema);
    db.prepare(`INSERT INTO ielts_history (user_id, skill, band_score, details_json, created_at) VALUES (?, 'speaking', ?, ?, ?)`)
      .run(userId, result.bandScore, JSON.stringify(result), Date.now());
    res.json({ ...result, disclaimer: 'AI estimated, not an official score' });
  } catch (err) {
    res.status(503).json({ error: 'IELTS Speaking scoring unavailable offline', disclaimer: 'AI estimated, not an official score' });
  }
});

app.post('/api/ielts/writing', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const taskNumber = Number(req.body.taskNumber || 2);
  const promptText = sanitizeText(req.body.promptText, 600);
  const essayText = sanitizeText(req.body.essayText, 5000);
  const imageBase64 = req.body.imageBase64;
  const mimeType = sanitizeText(req.body.mimeType || 'image/jpeg', 40);

  if (!essayText && !imageBase64) return res.status(400).json({ error: 'essayText or handwriting imageBase64 required' });

  try {
    if (!checkAndIncrementAiCap(userId)) return res.status(429).json({ error: 'Daily AI cap reached' });
    const schema = {
      type: Type.OBJECT,
      properties: {
        bandScore: { type: Type.NUMBER },
        taskResponse: { type: Type.NUMBER },
        lexical: { type: Type.NUMBER },
        grammar: { type: Type.NUMBER },
        coherence: { type: Type.NUMBER },
        corrections: { type: Type.ARRAY, items: { type: Type.STRING } },
        rewrittenParagraph: { type: Type.STRING },
        usefulVocabulary: { type: Type.ARRAY, items: { type: Type.STRING } }
      },
      required: ['bandScore', 'taskResponse', 'lexical', 'grammar', 'coherence', 'corrections', 'rewrittenParagraph', 'usefulVocabulary']
    };
    const parts = [];
    if (imageBase64) {
      const media = validateBase64Media(imageBase64);
      if (!media.ok) return res.status(400).json({ error: media.reason });
      parts.push({ inlineData: { data: media.base64, mimeType } });
    }
    parts.push({
      text: `Evaluate this IELTS Writing Task ${taskNumber}. Prompt: "${promptText}". Typed text: "${essayText}". Return estimated bandScore, 4 criteria, 3 corrections, Band 8 rewrittenParagraph, and 4 usefulVocabulary.`
    });
    const result = await generateStructuredJson(parts, schema);
    db.prepare(`INSERT INTO ielts_history (user_id, skill, band_score, details_json, created_at) VALUES (?, 'writing', ?, ?, ?)`)
      .run(userId, result.bandScore, JSON.stringify(result), Date.now());
    res.json({ ...result, disclaimer: 'AI estimated, not an official score' });
  } catch (err) {
    res.status(503).json({ error: 'IELTS Writing evaluation unavailable offline', disclaimer: 'AI estimated, not an official score' });
  }
});

app.post('/api/ielts/plan', async (req, res) => {
  const userId = sanitizeText(req.body.userId || 'default', 64);
  const currentBand = Number(req.body.currentBand || 5.5);
  const targetBand = Number(req.body.targetBand || 7.0);
  const daysLeft = Number(req.body.daysLeft || 30);
  try {
    if (!checkAndIncrementAiCap(userId)) throw new Error('Daily AI cap reached');
    const schema = {
      type: Type.OBJECT,
      properties: {
        weeklyFocus: { type: Type.ARRAY, items: { type: Type.STRING } },
        dailyChecklist: { type: Type.ARRAY, items: { type: Type.STRING } },
        malayalamMotivation: { type: Type.STRING }
      },
      required: ['weeklyFocus', 'dailyChecklist', 'malayalamMotivation']
    };
    const prompt = `Build a concise IELTS study plan for a Malayalam speaker. Current estimated band: ${currentBand}, Target: ${targetBand}, Exam in ${daysLeft} days.`;
    const result = await generateStructuredJson(prompt, schema);
    res.json({ ...result, disclaimer: 'AI estimated, not an official score' });
  } catch (err) {
    res.json({
      weeklyFocus: ['Part 2 2-min cue card fluency', 'Academic Task 2 structure', 'Listening section note completion'],
      dailyChecklist: ['10 min IELTS Speaking', '1 Listening or Reading section', '5 AWL words'],
      malayalamMotivation: `ദിവസവും പരിശീലിച്ചാൽ ${daysLeft} ദിവസത്തിനുള്ളിൽ ലക്ഷ്യത്തിലെത്താം!`,
      disclaimer: 'AI estimated, not an official score'
    });
  }
});

// Sync user state, saved words & analytics
app.post('/api/sync', (req, res) => {
  const { userId, profile, events = [], savedWords = [] } = req.body;
  if (!userId || !profile) return res.status(400).json({ error: 'userId and profile required' });
  const safeUser = sanitizeText(userId, 64);
  const now = Date.now();
  db.prepare(`INSERT INTO users (user_id, profile_json, updated_at) VALUES (?, ?, ?)
              ON CONFLICT(user_id) DO UPDATE SET profile_json = excluded.profile_json, updated_at = excluded.updated_at`)
    .run(safeUser, JSON.stringify(profile), now);

  const insertEvent = db.prepare(`INSERT INTO analytics_events (user_id, event_name, payload_json, created_at) VALUES (?, ?, ?, ?)`);
  for (const ev of events.slice(0, 100)) {
    insertEvent.run(safeUser, sanitizeText(ev.name || 'unknown', 64), JSON.stringify(ev.payload || {}), ev.timestamp || now);
  }

  const insertWord = db.prepare(`INSERT OR IGNORE INTO saved_words (user_id, word, malayalam_meaning, source_type, created_at) VALUES (?, ?, ?, ?, ?)`);
  for (const w of savedWords.slice(0, 100)) {
    insertWord.run(safeUser, sanitizeText(w.word, 80), sanitizeText(w.malayalamMeaning, 140), sanitizeText(w.sourceType || 'general', 32), now);
  }
  res.json({ synced: true, timestamp: now });
});

// POST /api/push/subscribe
app.post('/api/push/subscribe', (req, res) => {
  const { userId = 'default', subscription, timezone = 'Asia/Kolkata', reminderTimes = ['08:30', '20:00'] } = req.body;
  if (!subscription?.endpoint) return res.status(400).json({ error: 'Valid push subscription required' });

  db.prepare(`
    INSERT INTO push_subscriptions (user_id, endpoint, subscription_json, timezone, reminder_times_json)
    VALUES (?, ?, ?, ?, ?)
    ON CONFLICT(endpoint) DO UPDATE SET
      user_id = excluded.user_id,
      subscription_json = excluded.subscription_json,
      timezone = excluded.timezone,
      reminder_times_json = excluded.reminder_times_json
  `).run(sanitizeText(userId, 64), subscription.endpoint, JSON.stringify(subscription), timezone, JSON.stringify(reminderTimes.slice(0, 3)));

  res.json({ subscribed: true });
});

// Minute-by-minute context-aware Web Push scheduler (max 3/day, skips when tasks done)
cron.schedule('* * * * *', async () => {
  if (!process.env.VAPID_PUBLIC_KEY || !process.env.VAPID_PRIVATE_KEY) return;
  const rows = db.prepare(`SELECT p.*, u.profile_json FROM push_subscriptions p LEFT JOIN users u ON p.user_id = u.user_id`).all();

  const rotatingMessages = [
    'ഇന്ന് IELTS Speaking practice ചെയ്യാം 🎤',
    'Time to speak! Just 5 minutes with Leo today.',
    'ഇന്നലെ തെറ്റിയ വാക്ക് ഒന്നുകൂടി പറഞ്ഞു നോക്കാം!'
  ];

  for (const row of rows) {
    try {
      const nowInTz = new Date(new Date().toLocaleString('en-US', { timeZone: row.timezone || 'Asia/Kolkata' }));
      const hhmm = `${String(nowInTz.getHours()).padStart(2, '0')}:${String(nowInTz.getMinutes()).padStart(2, '0')}`;
      const todayStr = nowInTz.toISOString().slice(0, 10);
      const times = JSON.parse(row.reminder_times_json || '[]');
      if (!times.includes(hhmm)) continue;

      const sentCount = row.last_sent_date === todayStr ? row.sent_today_count : 0;
      if (sentCount >= 3) continue;

      const profile = row.profile_json ? JSON.parse(row.profile_json) : {};
      if ((profile.completedTasksToday || []).length >= 5) continue;

      const unfixedMistake = db.prepare(`SELECT COUNT(*) AS cnt FROM mistakes WHERE user_id = ? AND is_fixed = 0`).get(row.user_id);
      const unfixedCount = Number(unfixedMistake?.cnt || 0);
      const ieltsDaysLeft = Number(profile.ieltsDaysLeft ?? -1);
      const missedDays = Number(profile.missedDays || 0);
      const streak = Number(profile.streakDays || 1);

      let body = rotatingMessages[sentCount % rotatingMessages.length];
      if (ieltsDaysLeft >= 0 && ieltsDaysLeft <= 30 && sentCount === 0) {
        body = `Your exam is in ${ieltsDaysLeft} days — ഇന്ന് IELTS Speaking practice ചെയ്യാം!`;
      } else if (unfixedCount > 0 && sentCount === 1) {
        body = 'ഇന്നലെ തെറ്റിയ വാക്ക് ഒന്നുകൂടി പറഞ്ഞു നോക്കാം!';
      } else if (missedDays >= 2) {
        body = 'No rush—just one tiny English sentence with Leo today keeps you moving.';
      } else if (streak >= 3 && sentCount === 2) {
        body = `Your ${streak}-day streak is in danger, come save it!`;
      }

      const payload = JSON.stringify({
        title: 'Leo is waiting!',
        body,
        icon: '/icons/leo-192.png',
        actions: [
          { action: 'start_now', title: 'Start now' },
          { action: 'snooze_30', title: 'Remind me in 30 min' }
        ],
        url: '/?action=start_today'
      });

      await webpush.sendNotification(JSON.parse(row.subscription_json), payload);
      db.prepare(`UPDATE push_subscriptions SET sent_today_count = ?, last_sent_date = ? WHERE id = ?`)
        .run(sentCount + 1, todayStr, row.id);
    } catch (err) {
      if (err?.statusCode === 404 || err?.statusCode === 410) {
        db.prepare(`DELETE FROM push_subscriptions WHERE id = ?`).run(row.id);
      }
    }
  }
});

app.get('/health', (_req, res) => res.json({ status: 'ok', dailyAiCap: DAILY_AI_CAP, maxMediaMb: MAX_MEDIA_MB }));

app.listen(PORT, () => {
  console.log(`SpeakUp backend listening on port ${PORT}`);
});
