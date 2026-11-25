package com.example.audio

import com.example.model.EnglishLevel
import com.example.model.LeoPersonality
import com.example.model.PlacementQuestion
import com.example.model.SpeakingEvaluation
import kotlin.math.max
import kotlin.math.min

object SpeakingEvaluator {

    private fun normalizeWords(text: String): List<String> =
        text.lowercase()
            .replace(Regex("[^a-z0-9' ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

    private fun levenshteinSimilarity(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        val maxLen = max(a.length, b.length)
        return 1.0 - dp[a.length][b.length].toDouble() / maxLen
    }

    fun evaluateSentenceMatch(
        spoken: String,
        target: String,
        personality: LeoPersonality
    ): SpeakingEvaluation {
        val spokenWords = normalizeWords(spoken)
        val targetWords = normalizeWords(target)
        if (spokenWords.isEmpty()) {
            return SpeakingEvaluation(
                stars = 1,
                scorePercent = 20,
                spokenText = spoken,
                targetText = target,
                matchedWords = emptyList(),
                missedWords = targetWords,
                feedbackEn = "Speak a little louder so Leo can catch every word!",
                feedbackMl = "ലിയോയ്ക്ക് വ്യക്തമായി കേൾക്കാൻ കുറച്ചുകൂടി ഉറക്കെ പറയൂ!"
            )
        }

        val matched = mutableListOf<String>()
        val missed = mutableListOf<String>()
        var totalSim = 0.0

        for (tw in targetWords) {
            val bestSim = spokenWords.maxOfOrNull { sw -> levenshteinSimilarity(sw, tw) } ?: 0.0
            totalSim += bestSim
            if (bestSim >= 0.72) {
                matched.add(tw)
            } else {
                missed.add(tw)
            }
        }

        val baseRatio = if (targetWords.isEmpty()) 1.0 else totalSim / targetWords.size
        val scorePercent = (baseRatio * 100).toInt().coerceIn(25, 100)
        val stars = when {
            scorePercent >= 90 -> 5
            scorePercent >= 76 -> 4
            scorePercent >= 58 -> 3
            scorePercent >= 40 -> 2
            else -> 1
        }

        val (feedbackEn, feedbackMl) = when {
            stars >= 5 -> when (personality) {
                LeoPersonality.FRIENDLY -> "Fantastic clarity! You sounded natural and confident." to "അടിപൊളി! വളരെ വ്യക്തവും സ്വാഭാവികവുമായിരുന്നു."
                LeoPersonality.STRICT_COACH -> "Spot on. Crisp delivery and zero hesitation." to "കൃത്യമായ ഉച്ചാരണം. മടികൂടാതെ നന്നായി പറഞ്ഞു."
                LeoPersonality.FUNNY -> "Roar! Even a British tea kettle would applaud that!" to "സൂപ്പർ! ലിയോയുടെ ചെവികൾ സന്തോഷം കൊണ്ട് തുള്ളുന്നു!"
            }
            stars >= 3 -> when (personality) {
                LeoPersonality.FRIENDLY -> "Good try! Listen once more and emphasize: ${missed.take(3).joinToString(", ")}." to "നല്ല ശ്രമം! വിട്ടുപോയ വാക്കുകൾ ഒന്ന് കൂടി കേട്ട് പറയൂ."
                LeoPersonality.STRICT_COACH -> "Decent, but tighten these words: ${missed.take(3).joinToString(", ")}." to "കുഴപ്പമില്ല, ഈ വാക്കുകൾ കൂടുതൽ വ്യക്തമാക്കൂ."
                LeoPersonality.FUNNY -> "Almost nailed it! A tiny tongue stretch on ${missed.take(2).joinToString(", ")}." to "ഏകദേശം ശരിയായി! ആ രണ്ട് വാക്കുകൾ കൂടി ഒന്ന് നോക്കൂ."
            }
            else -> "No worries—tap slow speed (0.7x), listen closely, and try once more!" to "സാരമില്ല! സ്ലോ സ്പീഡ് കേട്ട് ഒരിക്കൽ കൂടി ശ്രമിക്കൂ."
        }

        return SpeakingEvaluation(
            stars = stars,
            scorePercent = scorePercent,
            spokenText = spoken,
            targetText = target,
            matchedWords = matched,
            missedWords = missed,
            feedbackEn = feedbackEn,
            feedbackMl = feedbackMl,
            isOfflineScore = true
        )
    }

    fun evaluateOpenResponse(
        spoken: String,
        sampleTarget: String,
        expectedKeywords: List<String>,
        minWords: Int,
        personality: LeoPersonality
    ): SpeakingEvaluation {
        val spokenWords = normalizeWords(spoken)
        val matchedKeywords = expectedKeywords.filter { kw ->
            spokenWords.any { sw -> levenshteinSimilarity(sw, kw.lowercase()) >= 0.75 }
        }
        val lengthRatio = (spokenWords.size.toDouble() / max(1, minWords)).coerceIn(0.0, 1.0)
        val keywordRatio = if (expectedKeywords.isEmpty()) 0.7 else {
            (matchedKeywords.size.toDouble() / min(3, expectedKeywords.size)).coerceIn(0.0, 1.0)
        }
        val combined = ((lengthRatio * 0.65 + keywordRatio * 0.35) * 100).toInt().coerceIn(30, 100)
        val stars = when {
            combined >= 88 -> 5
            combined >= 74 -> 4
            combined >= 58 -> 3
            combined >= 42 -> 2
            else -> 1
        }
        val (feedbackEn, feedbackMl) = if (stars >= 4) {
            when (personality) {
                LeoPersonality.FRIENDLY -> "Great flow! You answered smoothly in natural English." to "നല്ല ഒഴുക്ക്! വളരെ സ്വാഭാവികമായി ഉത്തരം പറഞ്ഞു."
                LeoPersonality.STRICT_COACH -> "Strong, clear response. Keep pushing your sentence length." to "ശക്തമായ ഉത്തരം. അടുത്തതിലേക്ക് നീങ്ങാം."
                LeoPersonality.FUNNY -> "Look at you chatting like a pro! Leo is impressed!" to "കൊള്ളാമല്ലോ! പ്രൊഫഷണലിനെപ്പോലെ സംസാരിച്ചു!"
            }
        } else {
            "Good start! Try adding 2-3 more words like the natural example below." to "നല്ല തുടക്കം! താഴെ കാണുന്നതുപോലെ 2-3 വാക്കുകൾ കൂടി ചേർത്ത് പറയൂ."
        }

        return SpeakingEvaluation(
            stars = stars,
            scorePercent = combined,
            spokenText = spoken,
            targetText = sampleTarget,
            matchedWords = matchedKeywords,
            missedWords = emptyList(),
            feedbackEn = feedbackEn,
            feedbackMl = feedbackMl,
            isOfflineScore = true
        )
    }

    fun calculatePlacementLevel(answers: Map<Int, String>, questions: List<PlacementQuestion>): EnglishLevel {
        var totalScore = 0
        for (q in questions) {
            val response = answers[q.id].orEmpty()
            val words = normalizeWords(response)
            if (words.isEmpty()) continue
            val hits = q.expectedKeywords.count { kw ->
                words.any { sw -> levenshteinSimilarity(sw, kw) >= 0.75 }
            }
            val lengthScore = when {
                words.size >= 10 -> 3
                words.size >= 5 -> 2
                words.size >= 2 -> 1
                else -> 0
            }
            val hitScore = min(2, hits)
            totalScore += (lengthScore + hitScore) * q.difficultyWeight
        }

        return when {
            totalScore >= 58 -> EnglishLevel.MASTER
            totalScore >= 45 -> EnglishLevel.ADVANCED
            totalScore >= 32 -> EnglishLevel.UPPER_INTERMEDIATE
            totalScore >= 20 -> EnglishLevel.INTERMEDIATE
            totalScore >= 10 -> EnglishLevel.ELEMENTARY
            else -> EnglishLevel.BEGINNER
        }
    }
}
