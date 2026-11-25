package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.AudioPronunciationAnalysis
import com.example.model.CameraExtractResult
import com.example.model.CameraWordItem
import com.example.model.EnglishLevel
import com.example.model.IeltsCriteriaBreakdown
import com.example.model.IeltsSpeakingEval
import com.example.model.IeltsStudyPlan
import com.example.model.IeltsWritingEval
import com.example.model.JournalCorrectionResult
import com.example.model.RealLifeScenario
import com.example.model.SimulatorReportCard
import com.example.model.SoundErrorDetail
import com.example.model.SourceLanguageConfig
import com.example.model.SupportLevel
import com.example.model.TranslateLearnResult
import com.example.model.WordPronunciationScore
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "SpeakUpFeatureAi"

data class SimulatorTurnResponse(
    val characterReply: String,
    val malayalamHint: String,
    val newUsefulPhrase: String
)

class SpeakUpFeatureAiRepository {

    private val pronunciationSchema = Schema.obj(
        properties = mapOf(
            "overallScore" to Schema.integer("Overall pronunciation accuracy 0-100"),
            "transcriptHeard" to Schema.string("What you actually heard in the audio"),
            "wordScores" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "word" to Schema.string(),
                        "score" to Schema.integer(),
                        "note" to Schema.string()
                    )
                )
            ),
            "soundErrors" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "soundTargetId" to Schema.enumeration(
                            values = listOf(
                                "th_dh", "v_w", "f_p", "z_sound",
                                "short_long_vowels", "word_stress", "intonation_rhythm"
                            )
                        ),
                        "issue" to Schema.string(),
                        "correctionTip" to Schema.string()
                    )
                )
            ),
            "tip" to Schema.string("One practical coaching tip in Malayalam or simple English")
        )
    )

    private val translateLearnSchema = Schema.obj(
        properties = mapOf(
            "formalEnglish" to Schema.string(),
            "casualEnglish" to Schema.string(),
            "manglishFormal" to Schema.string("Formal English written phonetically in Malayalam script"),
            "manglishCasual" to Schema.string("Casual English written phonetically in Malayalam script"),
            "contextNote" to Schema.string("Short friendly usage note in Malayalam")
        )
    )

    private val journalSchema = Schema.obj(
        properties = mapOf(
            "naturalVersion" to Schema.string("More natural spoken English version of the user's journal"),
            "keyImprovements" to Schema.array(items = Schema.string(), description = "2-3 natural rephrased expressions, zero grammar rules"),
            "encouragement" to Schema.string("Warm encouragement from Leo")
        )
    )

    private val cameraSchema = Schema.obj(
        properties = mapOf(
            "sceneSummaryMl" to Schema.string("Short Malayalam description of what is in the photo"),
            "items" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "englishText" to Schema.string(),
                        "malayalamMeaning" to Schema.string(),
                        "manglishPronunciation" to Schema.string("Pronunciation written in Malayalam script"),
                        "exampleSentence" to Schema.string()
                    )
                )
            )
        )
    )

    private val simulatorTurnSchema = Schema.obj(
        properties = mapOf(
            "characterReply" to Schema.string("Character's 1-2 sentence in-character spoken reply ending with a question"),
            "malayalamHint" to Schema.string("Short Malayalam reply idea"),
            "newUsefulPhrase" to Schema.string("One practical English phrase from this situation")
        )
    )

    private val simulatorReportSchema = Schema.obj(
        properties = mapOf(
            "fluencyScore" to Schema.integer(),
            "confidenceScore" to Schema.integer(),
            "newWordsLearned" to Schema.array(items = Schema.string()),
            "improvementPoints" to Schema.array(items = Schema.string())
        )
    )

    private val ieltsSpeakingSchema = Schema.obj(
        properties = mapOf(
            "bandScore" to Schema.double(),
            "fluency" to Schema.double(),
            "lexical" to Schema.double(),
            "grammar" to Schema.double(),
            "pronunciation" to Schema.double(),
            "fillerWordCount" to Schema.integer(),
            "wordsPerMinute" to Schema.integer(),
            "feedbackSummary" to Schema.string(),
            "modelImprovedAnswer" to Schema.string(),
            "weakestFocus" to Schema.string()
        )
    )

    private val ieltsWritingSchema = Schema.obj(
        properties = mapOf(
            "bandScore" to Schema.double(),
            "taskResponse" to Schema.double(),
            "lexical" to Schema.double(),
            "grammar" to Schema.double(),
            "coherence" to Schema.double(),
            "corrections" to Schema.array(items = Schema.string()),
            "rewrittenParagraph" to Schema.string(),
            "usefulVocabulary" to Schema.array(items = Schema.string())
        )
    )

    private val ieltsPlanSchema = Schema.obj(
        properties = mapOf(
            "weeklyFocus" to Schema.array(items = Schema.string()),
            "dailyChecklist" to Schema.array(items = Schema.string()),
            "malayalamMotivation" to Schema.string()
        )
    )

    private fun structuredModel(schema: Schema) = Firebase.ai.generativeModel(
        modelName = "gemini-3.8-flash",
        generationConfig = generationConfig {
            responseMimeType = "application/json"
            responseSchema = schema
        }
    )

    private val pronunciationModel by lazy { structuredModel(pronunciationSchema) }
    private val translateModel by lazy { structuredModel(translateLearnSchema) }
    private val journalModel by lazy { structuredModel(journalSchema) }
    private val cameraModel by lazy { structuredModel(cameraSchema) }
    private val sceneTurnModel by lazy { structuredModel(simulatorTurnSchema) }
    private val sceneReportModel by lazy { structuredModel(simulatorReportSchema) }
    private val ieltsSpeakingModel by lazy { structuredModel(ieltsSpeakingSchema) }
    private val ieltsWritingModel by lazy { structuredModel(ieltsWritingSchema) }
    private val ieltsPlanModel by lazy { structuredModel(ieltsPlanSchema) }

    suspend fun analyzePronunciationAudio(
        audioBytes: ByteArray,
        mimeType: String = "audio/mp4",
        targetSentence: String,
        supportLevel: SupportLevel,
        sourceLang: SourceLanguageConfig
    ): Result<AudioPronunciationAnalysis> {
        return try {
            val langInstruction = when (supportLevel) {
                SupportLevel.FULL_MALAYALAM -> "Write the practical tip in simple spoken ${sourceLang.displayName}."
                SupportLevel.MIXED -> "Write the practical tip in simple English with a short ${sourceLang.displayName} cue."
                SupportLevel.ENGLISH_ONLY -> "Write the practical tip in clear, simple English."
            }
            val prompt = """
                Analyze this ${sourceLang.aiTutorContextName}'s spoken English audio recording against the target sentence:
                "$targetSentence"
                Listen closely for common Malayali English pronunciation patterns: th/dh dental vs retroflex stops, v vs w, f vs p, z buzz vs s, short vs long vowels (ship/sheep), word stress, and sentence rhythm.
                $langInstruction No grammar jargon.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                pronunciationModel.generateContent(
                    content {
                        inlineData(audioBytes, mimeType)
                        text(prompt)
                    }
                )
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty audio evaluation"))
            val wordArr = json.optJSONArray("wordScores")
            val wordScores = buildList {
                if (wordArr != null) {
                    for (i in 0 until wordArr.length()) {
                        val o = wordArr.optJSONObject(i) ?: continue
                        add(WordPronunciationScore(o.optString("word"), o.optInt("score", 75), o.optString("note")))
                    }
                }
            }
            val errArr = json.optJSONArray("soundErrors")
            val soundErrors = buildList {
                if (errArr != null) {
                    for (i in 0 until errArr.length()) {
                        val o = errArr.optJSONObject(i) ?: continue
                        add(SoundErrorDetail(o.optString("soundTargetId", "th_dh"), o.optString("issue"), o.optString("correctionTip")))
                    }
                }
            }
            Result.success(
                AudioPronunciationAnalysis(
                    overallScore = json.optInt("overallScore", 75).coerceIn(0, 100),
                    wordScores = wordScores,
                    soundErrors = soundErrors,
                    tip = json.optString("tip", "Speak slowly and round your lips for W sounds."),
                    transcriptHeard = json.optString("transcriptHeard", targetSentence),
                    isOfflineFallback = false
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Audio pronunciation analysis failed", e)
            Result.failure(e)
        }
    }

    suspend fun translateAndLearn(
        malayalamInput: String,
        level: EnglishLevel,
        sourceLang: SourceLanguageConfig
    ): Result<TranslateLearnResult> {
        return try {
            val prompt = """
                The learner (${sourceLang.aiTutorContextName}, English level: ${level.titleEn}) wants to say this in natural spoken English:
                "$malayalamInput"
                Provide:
                1. formalEnglish (polite / workplace / elder-friendly)
                2. casualEnglish (natural everyday friend talk)
                3. manglishFormal & manglishCasual (English words written phonetically in ${sourceLang.displayName} script, e.g. "ഹൗ ആർ യു")
                4. contextNote (short friendly usage tip in ${sourceLang.displayName}, zero grammar rules).
            """.trimIndent()

            val response = withTimeout(45_000L) { translateModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty translate response"))
            Result.success(
                TranslateLearnResult(
                    malayalamInput = malayalamInput,
                    formalEnglish = json.getString("formalEnglish"),
                    casualEnglish = json.getString("casualEnglish"),
                    manglishFormal = json.optString("manglishFormal"),
                    manglishCasual = json.optString("manglishCasual"),
                    contextNote = json.optString("contextNote"),
                    isAiGenerated = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Translate & learn failed", e)
            Result.failure(e)
        }
    }

    suspend fun polishDailyJournal(
        spokenEnglish: String,
        supportLevel: SupportLevel
    ): Result<JournalCorrectionResult> {
        return try {
            val prompt = """
                A Malayalam speaker spoke these daily journal sentences in English:
                "$spokenEnglish"
                Rewrite them into a warm, natural spoken English version without explaining ANY grammar rules.
                In keyImprovements, list 2-3 short "Instead of X, say Y" phrases.
                Support level: ${supportLevel.name}.
            """.trimIndent()

            val response = withTimeout(45_000L) { journalModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty journal response"))
            val arr = json.optJSONArray("keyImprovements")
            val improvements = buildList {
                if (arr != null) for (i in 0 until arr.length()) add(arr.optString(i))
            }.filter { it.isNotBlank() }
            Result.success(
                JournalCorrectionResult(
                    spokenOriginal = spokenEnglish,
                    naturalVersion = json.getString("naturalVersion"),
                    keyImprovements = improvements,
                    encouragement = json.optString("encouragement", "Great job thinking in English today!"),
                    isAiGenerated = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Journal polish failed", e)
            Result.failure(e)
        }
    }

    suspend fun extractWordsFromPhoto(
        bitmap: Bitmap,
        sourceLang: SourceLanguageConfig
    ): Result<CameraExtractResult> {
        return try {
            val prompt = """
                Read the English text, signs, labels, menu items, or objects visible in this photo.
                Extract up to 8 practical English words or short phrases that a ${sourceLang.aiTutorContextName} sees here.
                For each item return:
                - englishText
                - malayalamMeaning (${sourceLang.displayName} meaning)
                - manglishPronunciation (spoken English written in ${sourceLang.displayName} script)
                - exampleSentence (short everyday spoken sentence).
                Also give sceneSummaryMl in 1 short ${sourceLang.displayName} sentence.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                cameraModel.generateContent(
                    content {
                        image(bitmap)
                        text(prompt)
                    }
                )
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty camera response"))
            val arr = json.optJSONArray("items")
            val items = buildList {
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val o = arr.optJSONObject(i) ?: continue
                        add(
                            CameraWordItem(
                                englishText = o.optString("englishText"),
                                malayalamMeaning = o.optString("malayalamMeaning"),
                                manglishPronunciation = o.optString("manglishPronunciation"),
                                exampleSentence = o.optString("exampleSentence")
                            )
                        )
                    }
                }
            }.filter { it.englishText.isNotBlank() }

            Result.success(
                CameraExtractResult(
                    sceneSummaryMl = json.optString("sceneSummaryMl", "ചിത്രത്തിലെ ഇംഗ്ലീഷ് വാക്കുകൾ"),
                    items = items
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Camera Vision extract failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateSimulatorTurn(
        scenario: RealLifeScenario,
        userMessage: String,
        difficulty: Int,
        surpriseMe: Boolean,
        historyPairs: List<Pair<Boolean, String>>
    ): Result<SimulatorTurnResponse> {
        return try {
            val twistInstruction = if (surpriseMe) {
                "Introduce this unexpected twist naturally right now: \"${scenario.surpriseTwist}\""
            } else {
                "Stay naturally in character."
            }
            val historyText = historyPairs.takeLast(6).joinToString("\n") { (isChar, msg) ->
                if (isChar) "${scenario.characterName}: $msg" else "Learner: $msg"
            }
            val prompt = """
                Roleplay as ${scenario.characterName} (${scenario.characterRole}) in "${scenario.titleEn}".
                Difficulty level: $difficulty/5. $twistInstruction
                Reply in 1-2 short spoken sentences matching the character and always end with a question.
                Recent dialogue:
                $historyText
                Learner: $userMessage
            """.trimIndent()

            val response = withTimeout(45_000L) { sceneTurnModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty scene turn"))
            Result.success(
                SimulatorTurnResponse(
                    characterReply = json.getString("characterReply"),
                    malayalamHint = json.optString("malayalamHint"),
                    newUsefulPhrase = json.optString("newUsefulPhrase")
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Simulator turn failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateSimulatorReport(
        scenario: RealLifeScenario,
        dialogue: List<Pair<Boolean, String>>
    ): Result<SimulatorReportCard> {
        return try {
            val prompt = """
                Score this ${scenario.titleEn} roleplay performance from a Malayalam speaker:
                ${dialogue.joinToString("\n") { (isChar, txt) -> if (isChar) "Character: $txt" else "Learner: $txt" }}
                Return fluencyScore (0-100), confidenceScore (0-100), 3 newWordsLearned, and exactly 3 practical improvementPoints (zero grammar rules).
            """.trimIndent()
            val response = withTimeout(45_000L) { sceneReportModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty report"))
            val words = json.optJSONArray("newWordsLearned")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            val pts = json.optJSONArray("improvementPoints")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            Result.success(
                SimulatorReportCard(
                    fluencyScore = json.optInt("fluencyScore", 78).coerceIn(0, 100),
                    confidenceScore = json.optInt("confidenceScore", 80).coerceIn(0, 100),
                    newWordsLearned = words.filter { it.isNotBlank() },
                    improvementPoints = pts.filter { it.isNotBlank() }.take(3)
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Simulator report failed", e)
            Result.failure(e)
        }
    }

    suspend fun evaluateIeltsSpeaking(
        part: Int,
        question: String,
        transcript: String,
        durationSeconds: Int
    ): Result<IeltsSpeakingEval> {
        return try {
            val words = transcript.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val wpm = if (durationSeconds > 0) ((words * 60) / durationSeconds).coerceIn(40, 220) else 110
            val prompt = """
                You are an IELTS Speaking Examiner evaluating Part $part.
                Prompt: "$question"
                Candidate transcript (${words} words in ${durationSeconds}s, ~$wpm WPM):
                "$transcript"
                Return official IELTS-style 0.0-9.0 band score (in 0.5 increments), 4 criterion scores (fluency, lexical, grammar, pronunciation), fillerWordCount, wordsPerMinute, concise coaching feedbackSummary, weakestFocus, and a Band 8.0 modelImprovedAnswer.
            """.trimIndent()
            val response = withTimeout(60_000L) { ieltsSpeakingModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty IELTS speaking eval"))
            Result.success(
                IeltsSpeakingEval(
                    bandScore = json.optDouble("bandScore", 6.0),
                    breakdown = IeltsCriteriaBreakdown(
                        fluencyOrTask = json.optDouble("fluency", 6.0),
                        lexical = json.optDouble("lexical", 6.0),
                        grammar = json.optDouble("grammar", 6.0),
                        pronunciationOrCoherence = json.optDouble("pronunciation", 6.0)
                    ),
                    fillerWordCount = json.optInt("fillerWordCount", 0),
                    wordsPerMinute = json.optInt("wordsPerMinute", wpm),
                    feedbackSummary = json.optString("feedbackSummary"),
                    modelImprovedAnswer = json.optString("modelImprovedAnswer"),
                    weakestFocus = json.optString("weakestFocus", "Lexical variety")
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "IELTS speaking eval failed", e)
            Result.failure(e)
        }
    }

    suspend fun evaluateIeltsWriting(
        taskNumber: Int,
        promptText: String,
        essayText: String,
        handwritingPhoto: Bitmap? = null
    ): Result<IeltsWritingEval> {
        return try {
            val prompt = """
                Evaluate this IELTS Writing Task $taskNumber response.
                Prompt: "$promptText"
                Typed text (if any): "$essayText"
                If an image is attached, transcribe the handwritten essay first and evaluate it.
                Return estimated Band (0.0-9.0), breakdown (taskResponse, lexical, grammar, coherence), 3 specific corrections, a Band 8 rewrittenParagraph, and 4 usefulVocabulary items.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                if (handwritingPhoto != null) {
                    ieltsWritingModel.generateContent(content { image(handwritingPhoto); text(prompt) })
                } else {
                    ieltsWritingModel.generateContent(prompt)
                }
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty IELTS writing eval"))
            val corr = json.optJSONArray("corrections")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            val vocab = json.optJSONArray("usefulVocabulary")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            Result.success(
                IeltsWritingEval(
                    bandScore = json.optDouble("bandScore", 6.0),
                    breakdown = IeltsCriteriaBreakdown(
                        fluencyOrTask = json.optDouble("taskResponse", 6.0),
                        lexical = json.optDouble("lexical", 6.0),
                        grammar = json.optDouble("grammar", 6.0),
                        pronunciationOrCoherence = json.optDouble("coherence", 6.0)
                    ),
                    corrections = corr.filter { it.isNotBlank() },
                    rewrittenParagraph = json.optString("rewrittenParagraph"),
                    usefulVocabulary = vocab.filter { it.isNotBlank() }
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "IELTS writing eval failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateIeltsStudyPlan(
        currentBand: Double,
        targetBand: Double,
        daysLeft: Int,
        supportLevel: SupportLevel
    ): Result<IeltsStudyPlan> {
        return try {
            val prompt = """
                Create a personalized IELTS study plan for a Malayalam speaker.
                Current estimated band: $currentBand, Target band: $targetBand, Exam in $daysLeft days.
                Support preference: ${supportLevel.name}.
                Return weeklyFocus (3-4 bullet milestones), dailyChecklist (4 short daily actions), and a warm malayalamMotivation sentence.
            """.trimIndent()
            val response = withTimeout(45_000L) { ieltsPlanModel.generateContent(prompt) }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty IELTS plan"))
            val weekly = json.optJSONArray("weeklyFocus")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            val daily = json.optJSONArray("dailyChecklist")?.let { a -> (0 until a.length()).map { a.optString(it) } }.orEmpty()
            Result.success(
                IeltsStudyPlan(
                    estimatedCurrentBand = currentBand,
                    targetBand = targetBand,
                    weeklyFocus = weekly.filter { it.isNotBlank() },
                    dailyChecklist = daily.filter { it.isNotBlank() },
                    malayalamMotivation = json.optString("malayalamMotivation", "ദിവസവും 20 മിനിറ്റ് പരിശീലിച്ചാൽ ലക്ഷ്യത്തിലെത്താം!"),
                    isAiGenerated = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "IELTS plan failed", e)
            Result.failure(e)
        }
    }
}
