package com.example.learning

import com.example.model.DifficultyTier
import com.example.model.EnglishLevel
import kotlin.math.max
import kotlin.math.roundToInt

data class Sm2UpdateResult(
    val repetitions: Int,
    val intervalDays: Int,
    val easeFactor: Double,
    val nextReviewAt: Long
)

data class AdaptiveOutcome(
    val newTier: DifficultyTier,
    val promotedLevel: EnglishLevel?,
    val reasonEn: String,
    val reasonMl: String
)

object LearningEngine {

    private const val DAY_MS = 24L * 60L * 60L * 1000L

    /**
     * Standard SM-2 spaced repetition algorithm.
     * Maps speaking scorePercent (0..100) to SM-2 quality (0..5).
     */
    fun scoreToQuality(scorePercent: Int): Int = when {
        scorePercent >= 92 -> 5
        scorePercent >= 80 -> 4
        scorePercent >= 66 -> 3
        scorePercent >= 50 -> 2
        scorePercent >= 30 -> 1
        else -> 0
    }

    fun computeSm2Next(
        previousRepetitions: Int,
        previousIntervalDays: Int,
        previousEaseFactor: Double,
        scorePercent: Int,
        nowMs: Long = System.currentTimeMillis()
    ): Sm2UpdateResult {
        val quality = scoreToQuality(scorePercent)
        var repetitions = previousRepetitions
        var interval = previousIntervalDays
        var ef = previousEaseFactor

        if (quality < 3) {
            repetitions = 0
            interval = 1
        } else {
            interval = when (repetitions) {
                0 -> 1
                1 -> 6
                else -> (interval * ef).roundToInt().coerceAtLeast(1)
            }
            repetitions += 1
        }

        ef = ef + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        ef = max(1.3, ef)

        return Sm2UpdateResult(
            repetitions = repetitions,
            intervalDays = interval,
            easeFactor = ef,
            nextReviewAt = nowMs + interval * DAY_MS
        )
    }

    /**
     * Adaptive difficulty:
     * - Consistent recent scores > 85 raise difficulty tier or promote level.
     * - Repeated low scores (< 55) gently lower difficulty tier.
     */
    fun evaluateAdaptiveShift(
        recentScores: List<Int>,
        currentLevel: EnglishLevel,
        currentTier: DifficultyTier
    ): AdaptiveOutcome {
        if (recentScores.size < 3) {
            return AdaptiveOutcome(
                newTier = currentTier,
                promotedLevel = null,
                reasonEn = "Steady pace",
                reasonMl = "സാധാരണ വേഗത"
            )
        }

        val lastThree = recentScores.take(3)
        val allHigh = lastThree.all { it >= 85 }
        val allLow = lastThree.all { it < 55 }

        if (allHigh) {
            return when (currentTier) {
                DifficultyTier.FOUNDATION -> AdaptiveOutcome(
                    newTier = DifficultyTier.STANDARD,
                    promotedLevel = null,
                    reasonEn = "Great consistency! Moving to balanced speed.",
                    reasonMl = "മികച്ച പ്രകടനം! സാധാരണ വേഗതയിലേക്ക് മാറുന്നു."
                )
                DifficultyTier.STANDARD -> AdaptiveOutcome(
                    newTier = DifficultyTier.CHALLENGE,
                    promotedLevel = null,
                    reasonEn = "Scores above 85%! Stepping up to Stretch Mode.",
                    reasonMl = "85%-ൽ കൂടുതൽ! ഉയർന്ന പരിശീലനത്തിലേക്ക് കടക്കാം."
                )
                DifficultyTier.CHALLENGE -> {
                    val nextLvl = EnglishLevel.entries.getOrNull(currentLevel.index + 1)
                    AdaptiveOutcome(
                        newTier = if (nextLvl != null) DifficultyTier.STANDARD else DifficultyTier.CHALLENGE,
                        promotedLevel = nextLvl,
                        reasonEn = if (nextLvl != null) "Mastered this tier! Promoted to ${nextLvl.titleEn}." else "Master fluency maintained!",
                        reasonMl = if (nextLvl != null) "ഈ ഘട്ടം വിജയിച്ചു! ${nextLvl.titleMl} ലെവലിലേക്ക് ഉയർന്നു." else "മാസ്റ്റർ നിലവാരം!"
                    )
                }
            }
        }

        if (allLow) {
            val lowered = when (currentTier) {
                DifficultyTier.CHALLENGE -> DifficultyTier.STANDARD
                DifficultyTier.STANDARD -> DifficultyTier.FOUNDATION
                DifficultyTier.FOUNDATION -> DifficultyTier.FOUNDATION
            }
            return AdaptiveOutcome(
                newTier = lowered,
                promotedLevel = null,
                reasonEn = "Switching to gentler sentences so you build confidence smoothly.",
                reasonMl = "ആത്മവിശ്വാസം കൂട്ടാൻ ലളിതമായ വാക്യങ്ങളിലേക്ക് മാറ്റുന്നു."
            )
        }

        return AdaptiveOutcome(
            newTier = currentTier,
            promotedLevel = null,
            reasonEn = "Balanced pace",
            reasonMl = "സമതുലിതമായ വേഗത"
        )
    }
}
