package com.example.ai

import android.util.Log
import com.example.model.DailyPlanItem
import com.example.model.DailyPlanState
import com.example.model.DailyTaskType
import com.example.model.EnglishLevel
import com.example.model.LearningGoal
import com.example.model.LeoPersonality
import com.example.model.WeeklySummaryReport
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "LeoAiRepository"

data class LeoStructuredReply(
    val reply: String,
    val gentleCorrection: String?,
    val malayalamTip: String?
)

data class AiPlacementResult(
    val level: EnglishLevel,
    val score: Int,
    val encouragement: String
)

data class AiFeedbackResult(
    val score: Int,
    val mispronouncedWords: List<String>,
    val tip: String
)

class LeoAiRepository {

    private val chatReplySchema = Schema.obj(
        properties = mapOf(
            "reply" to Schema.string(description = "Leo's reply in 1-3 short spoken sentences, ending with a question"),
            "gentleCorrection" to Schema.string(
                description = "Natural corrected sentence repeating what the user meant if correctMe is true and there was a mistake; null otherwise",
                nullable = true
            ),
            "malayalamTip" to Schema.string(
                description = "Short Malayalam help ONLY if askMalayalam is true; null otherwise",
                nullable = true
            )
        ),
        optionalProperties = listOf("gentleCorrection", "malayalamTip")
    )

    private val placementSchema = Schema.obj(
        properties = mapOf(
            "level" to Schema.enumeration(
                values = EnglishLevel.entries.map { it.name },
                description = "Assigned English level from the 6 levels"
            ),
            "score" to Schema.integer(description = "Overall spoken fluency score from 0 to 100"),
            "encouragement" to Schema.string(description = "Short warm encouragement sentence from Leo")
        )
    )

    private val feedbackSchema = Schema.obj(
        properties = mapOf(
            "score" to Schema.integer(description = "Pronunciation and accuracy score from 0 to 100"),
            "mispronouncedWords" to Schema.array(
                items = Schema.string(),
                description = "Words missed or likely mispronounced"
            ),
            "tip" to Schema.string(description = "One short actionable speaking tip (no grammar jargon)")
        )
    )

    private val dailyPlanSchema = Schema.obj(
        properties = mapOf(
            "focusNote" to Schema.string(description = "One short motivational daily focus sentence"),
            "tasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "taskId" to Schema.enumeration(
                            values = DailyTaskType.entries.map { it.id },
                            description = "Matching daily task id"
                        ),
                        "prompt" to Schema.string(description = "Personalized speaking prompt for today"),
                        "malayalamHint" to Schema.string(description = "Short Malayalam hint")
                    )
                ),
                description = "5 personalized speaking tasks for today"
            )
        )
    )

    private val weeklySummarySchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(description = "Short upbeat weekly headline"),
            "summary" to Schema.string(description = "2-sentence summary highlighting speaking minutes, words, and progress"),
            "nextWeekFocus" to Schema.string(description = "1 short suggestion for next week")
        )
    )

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = chatReplySchema
            }
        )
    }

    private val placementModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = placementSchema
            }
        )
    }

    private val feedbackModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = feedbackSchema
            }
        )
    }

    private val dailyPlanModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = dailyPlanSchema
            }
        )
    }

    private val weeklySummaryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = weeklySummarySchema
            }
        )
    }

    private val streamingModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private fun personalityPrompt(personality: LeoPersonality): String = when (personality) {
        LeoPersonality.FRIENDLY -> "You are Leo, a warm, encouraging lion cub English speaking coach for Malayalam speakers."
        LeoPersonality.STRICT_COACH -> "You are Leo, a crisp, disciplined, direct English speaking coach who pushes for clear pronunciation and strong phrasing."
        LeoPersonality.FUNNY -> "You are Leo, a witty, playful lion cub English coach who sprinkles gentle humor while keeping practice fun."
    }

    suspend fun evaluatePlacementWithAi(answers: Map<Int, String>): Result<AiPlacementResult> {
        return try {
            val formatted = answers.entries.sortedBy { it.key }.joinToString("\n") { (id, ans) ->
                "Q$id: ${ans.ifBlank { "(no response)" }}"
            }
            val prompt = """
                Evaluate these 5 spoken placement answers from a Malayalam speaker learning spoken English.
                Assign one level from: BEGINNER, ELEMENTARY, INTERMEDIATE, UPPER_INTERMEDIATE, ADVANCED, MASTER.
                No grammar rule explanations—only encouraging coaching feedback.
                Answers:
                $formatted
            """.trimIndent()

            val response = withTimeout(45_000L) {
                placementModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty placement response"))
            val levelName = json.getString("level")
            val level = runCatching { EnglishLevel.valueOf(levelName) }.getOrDefault(EnglishLevel.BEGINNER)
            val score = json.optInt("score", 50).coerceIn(0, 100)
            val encouragement = json.optString("encouragement", "Awesome! Let's start speaking every day.")
            Result.success(AiPlacementResult(level, score, encouragement))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI placement failed", e)
            Result.failure(e)
        }
    }

    suspend fun chatWithLeo(
        userMessage: String,
        topic: String,
        level: EnglishLevel,
        personality: LeoPersonality,
        correctMe: Boolean,
        askMalayalam: Boolean,
        historyPairs: List<Pair<Boolean, String>>
    ): Result<LeoStructuredReply> {
        return try {
            val historyText = historyPairs.takeLast(6).joinToString("\n") { (isLeo, msg) ->
                if (isLeo) "Leo: $msg" else "Learner: $msg"
            }

            val prompt = """
                ${personalityPrompt(personality)}
                Learner's current English level: ${level.titleEn} (strictly match vocabulary to this level).
                Conversation topic: $topic.
                CRITICAL RULES:
                1. Reply in 1-3 short spoken sentences max.
                2. ALWAYS end your reply with a simple question to keep the user speaking.
                3. NEVER explain grammar rules or grammar terms.
                4. If correctMe=$correctMe and the learner made a mistake, naturally provide ONLY the corrected English sentence in gentleCorrection (e.g. "I went to the market yesterday."). Otherwise set gentleCorrection to null.
                5. Provide malayalamTip ONLY if askMalayalam=$askMalayalam; otherwise set malayalamTip to null.

                Recent conversation:
                $historyText
                Learner: $userMessage
            """.trimIndent()

            val response = withTimeout(60_000L) {
                chatModel.generateContent(prompt)
            }
            val raw = response.text ?: throw IllegalStateException("Leo returned an empty response.")
            val json = JSONObject(raw)
            val reply = json.getString("reply")
            val correction = if (json.isNull("gentleCorrection")) null else json.optString("gentleCorrection").takeIf { it.isNotBlank() }
            val mlTip = if (!askMalayalam || json.isNull("malayalamTip")) null else json.optString("malayalamTip").takeIf { it.isNotBlank() }

            Result.success(LeoStructuredReply(reply, correction, mlTip))
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Leo chat timed out", e)
            Result.failure(Exception("Leo took too long to reply (timeout). Please check your connection and try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Leo chat failed", e)
            Result.failure(Exception(e.localizedMessage ?: "Couldn't reach Leo right now."))
        }
    }

    suspend fun evaluateFeedbackWithAi(
        targetSentence: String,
        userTranscript: String
    ): Result<AiFeedbackResult> {
        return try {
            val prompt = """
                Compare target English sentence: "$targetSentence"
                with learner's spoken transcript: "$userTranscript".
                Return score (0-100), list of mispronouncedWords (words missed or unclear), and ONE short friendly speaking tip (no grammar jargon).
            """.trimIndent()

            val response = withTimeout(35_000L) {
                feedbackModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty feedback response"))
            val score = json.optInt("score", 75).coerceIn(0, 100)
            val arr = json.optJSONArray("mispronouncedWords")
            val missed = if (arr != null) {
                (0 until arr.length()).mapNotNull { arr.optString(it).takeIf { s -> s.isNotBlank() } }
            } else emptyList()
            val tip = json.optString("tip", "Speak smoothly and pause naturally between phrases.")
            Result.success(AiFeedbackResult(score, missed, tip))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI feedback failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateDailyPlanWithAi(
        level: EnglishLevel,
        goal: LearningGoal,
        weakWords: List<String>,
        dailyMinutes: Int
    ): Result<DailyPlanState> {
        return try {
            val prompt = """
                Create today's 5 personalized English speaking tasks (one for each taskId: shadow, words, roleplay, listen_answer, challenge) for a Malayalam speaker.
                Level: ${level.titleEn}
                Goal: ${goal.titleEn}
                Daily time available: $dailyMinutes minutes
                Weak words due for review: ${if (weakWords.isEmpty()) "none" else weakWords.joinToString(", ")}
                Keep prompts short, spoken, practical, and zero grammar rules.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                dailyPlanModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty daily plan"))
            val focusNote = json.optString("focusNote", "Let's practice ${goal.titleEn} speaking today!")
            val arr = json.optJSONArray("tasks")
            val items = mutableListOf<DailyPlanItem>()
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val taskId = obj.optString("taskId")
                    val taskType = DailyTaskType.entries.firstOrNull { it.id == taskId } ?: continue
                    items.add(
                        DailyPlanItem(
                            taskType = taskType,
                            customPrompt = obj.optString("prompt"),
                            malayalamHint = obj.optString("malayalamHint")
                        )
                    )
                }
            }
            Result.success(
                DailyPlanState(
                    focusNote = focusNote,
                    weakWordsInFocus = weakWords,
                    items = items,
                    isAiGenerated = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI daily plan failed", e)
            Result.failure(e)
        }
    }

    suspend fun generateWeeklySummaryWithAi(
        speakingMinutes: Int,
        newWordsCount: Int,
        avgScore: Int,
        streakDays: Int
    ): Result<WeeklySummaryReport> {
        return try {
            val prompt = """
                Write an encouraging weekly speaking progress summary for a Malayalam speaker learning English:
                - Weekly speaking minutes: $speakingMinutes
                - Words practiced: $newWordsCount
                - Average pronunciation/fluency score: $avgScore%
                - Current streak: $streakDays days
                No grammar jargon; focus on speaking confidence.
            """.trimIndent()

            val response = withTimeout(45_000L) {
                weeklySummaryModel.generateContent(prompt)
            }
            val json = JSONObject(response.text ?: throw IllegalStateException("Empty weekly summary"))
            Result.success(
                WeeklySummaryReport(
                    headline = json.getString("headline"),
                    summary = json.getString("summary"),
                    nextWeekFocus = json.getString("nextWeekFocus"),
                    isAiGenerated = true
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI weekly summary failed", e)
            Result.failure(e)
        }
    }

    fun streamMalayalamHelp(englishText: String): Flow<String> = flow {
        try {
            val prompt = "Translate this English coaching message into simple, warm spoken Malayalam (1-2 short sentences, no grammar terms): \"$englishText\""
            val accumulated = StringBuilder()
            withTimeout(60_000L) {
                streamingModel.generateContentStream(content { text(prompt) }).collect { chunk ->
                    accumulated.append(chunk.text.orEmpty())
                    emit(accumulated.toString())
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Malayalam help streaming failed", e)
            throw e
        }
    }
}
