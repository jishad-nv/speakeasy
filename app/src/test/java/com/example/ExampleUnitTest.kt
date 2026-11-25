package com.example

import com.example.audio.SpeakingEvaluator
import com.example.data.curriculum.CurriculumRepository
import com.example.learning.LearningEngine
import com.example.model.DifficultyTier
import com.example.model.EnglishLevel
import com.example.model.LeoPersonality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun curriculumCoversAllSixLevelsWithFiveDailyDrills() {
        assertEquals(6, CurriculumRepository.allLevels.size)
        CurriculumRepository.allLevels.forEach { curriculum ->
            assertTrue(curriculum.shadowDrills.isNotEmpty())
            assertEquals(5, curriculum.wordDrills.size)
            assertTrue(curriculum.roleplayScene.turns.isNotEmpty())
            assertTrue(curriculum.listenAnswerDrills.isNotEmpty())
        }
    }

    @Test
    fun offlineDrillPackGeneratesThirtyDaysAcrossAllSixLevels() {
        val drills = CurriculumRepository.generate30DayOfflineDrills()
        assertEquals(6 * 30 * 5, drills.size)
        assertEquals(6, drills.map { it.levelIndex }.distinct().size)
        assertEquals(30, drills.map { it.dayNumber }.distinct().size)
    }

    @Test
    fun sm2SpacedRepetitionProgressesAndResetsOnLowScore() {
        val step1 = LearningEngine.computeSm2Next(
            previousRepetitions = 0,
            previousIntervalDays = 1,
            previousEaseFactor = 2.5,
            scorePercent = 90,
            nowMs = 1_000L
        )
        assertEquals(1, step1.repetitions)
        assertEquals(1, step1.intervalDays)

        val step2 = LearningEngine.computeSm2Next(
            previousRepetitions = step1.repetitions,
            previousIntervalDays = step1.intervalDays,
            previousEaseFactor = step1.easeFactor,
            scorePercent = 95,
            nowMs = 2_000L
        )
        assertEquals(2, step2.repetitions)
        assertEquals(6, step2.intervalDays)

        val step3 = LearningEngine.computeSm2Next(
            previousRepetitions = step2.repetitions,
            previousIntervalDays = step2.intervalDays,
            previousEaseFactor = step2.easeFactor,
            scorePercent = 88,
            nowMs = 3_000L
        )
        assertEquals(3, step3.repetitions)
        assertTrue(step3.intervalDays > 6)

        val reset = LearningEngine.computeSm2Next(
            previousRepetitions = step3.repetitions,
            previousIntervalDays = step3.intervalDays,
            previousEaseFactor = step3.easeFactor,
            scorePercent = 45,
            nowMs = 4_000L
        )
        assertEquals(0, reset.repetitions)
        assertEquals(1, reset.intervalDays)
    }

    @Test
    fun adaptiveDifficultyPromotesOnHighScoresAndLowersOnStruggle() {
        val stepUp = LearningEngine.evaluateAdaptiveShift(
            recentScores = listOf(88, 91, 86),
            currentLevel = EnglishLevel.BEGINNER,
            currentTier = DifficultyTier.STANDARD
        )
        assertEquals(DifficultyTier.CHALLENGE, stepUp.newTier)
        assertNull(stepUp.promotedLevel)

        val promoteLevel = LearningEngine.evaluateAdaptiveShift(
            recentScores = listOf(90, 92, 87),
            currentLevel = EnglishLevel.BEGINNER,
            currentTier = DifficultyTier.CHALLENGE
        )
        assertEquals(EnglishLevel.ELEMENTARY, promoteLevel.promotedLevel)
        assertEquals(DifficultyTier.STANDARD, promoteLevel.newTier)

        val stepDown = LearningEngine.evaluateAdaptiveShift(
            recentScores = listOf(40, 50, 48),
            currentLevel = EnglishLevel.ELEMENTARY,
            currentTier = DifficultyTier.STANDARD
        )
        assertEquals(DifficultyTier.FOUNDATION, stepDown.newTier)
        assertNull(stepDown.promotedLevel)
    }

    @Test
    fun evaluateSentenceMatchGivesFiveStarsOnExactMatch() {
        val result = SpeakingEvaluator.evaluateSentenceMatch(
            spoken = "Good morning how are you doing today",
            target = "Good morning, how are you doing today?",
            personality = LeoPersonality.FRIENDLY
        )
        assertEquals(5, result.stars)
        assertTrue(result.missedWords.isEmpty())
    }

    @Test
    fun placementAssignsBeginnerWhenEmptyAndHigherWhenFluent() {
        val beginner = SpeakingEvaluator.calculatePlacementLevel(
            answers = emptyMap(),
            questions = CurriculumRepository.placementQuestions
        )
        assertEquals(EnglishLevel.BEGINNER, beginner)

        val fluent = SpeakingEvaluator.calculatePlacementLevel(
            answers = CurriculumRepository.placementQuestions.associate { it.id to it.sampleResponse },
            questions = CurriculumRepository.placementQuestions
        )
        assertTrue(fluent.index >= EnglishLevel.UPPER_INTERMEDIATE.index)
    }

    @Test
    fun absoluteZeroCatalogHasExactly300SurvivalPhrasesWithManglish() {
        val phrases = com.example.data.curriculum.AbsoluteZeroCatalog.all300Phrases
        assertEquals(300, phrases.size)
        assertEquals(5, phrases.map { it.category }.distinct().size)
        assertTrue(phrases.all { it.englishText.isNotBlank() && it.malayalamMeaning.isNotBlank() && it.manglishScript.isNotBlank() })
    }

    @Test
    fun extensionsCatalogCoversAllScenariosAndIeltsSections() {
        assertEquals(10, com.example.data.curriculum.ExtensionsCatalog.realLifeScenarios.size)
        assertEquals(4, com.example.data.curriculum.ExtensionsCatalog.ieltsListeningSections.size)
        assertTrue(com.example.data.curriculum.ExtensionsCatalog.minimalPairDrills.size >= 8)
        assertTrue(com.example.data.curriculum.ExtensionsCatalog.ieltsVocabulary.size >= 12)
    }

    @Test
    fun sourceLanguageRegistrySupportsConfigDrivenSwitching() {
        val ml = com.example.model.SourceLanguageRegistry.byCode("ml")
        val hi = com.example.model.SourceLanguageRegistry.byCode("hi")
        val ta = com.example.model.SourceLanguageRegistry.byCode("ta")
        assertEquals("ml-IN", ml.speechLocale)
        assertEquals("hi-IN", hi.speechLocale)
        assertEquals("ta-IN", ta.speechLocale)
    }
}
