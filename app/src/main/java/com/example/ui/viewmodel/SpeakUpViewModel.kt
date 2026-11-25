package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.LeoAiRepository
import com.example.ai.SimulatorTurnResponse
import com.example.ai.SpeakUpFeatureAiRepository
import com.example.audio.AudioClipRecorder
import com.example.audio.CelebrationSoundPlayer
import com.example.audio.SpeakingEvaluator
import com.example.audio.SpeechCoachEngine
import com.example.connectivity.NetworkMonitor
import com.example.data.curriculum.AbsoluteZeroCatalog
import com.example.data.curriculum.CurriculumRepository
import com.example.data.curriculum.ExtensionsCatalog
import com.example.data.local.AnalyticsEventEntity
import com.example.data.local.IeltsAttemptEntity
import com.example.data.local.IeltsStarterPackEntity
import com.example.data.local.JournalEntryEntity
import com.example.data.local.LearnedWordEntity
import com.example.data.local.MistakeEntryEntity
import com.example.data.local.OfflinePackEntity
import com.example.data.local.PendingSyncEntity
import com.example.data.local.PracticeSessionEntity
import com.example.data.local.PreferencesRepository
import com.example.data.local.SoundProgressEntity
import com.example.data.local.SpeakUpDatabase
import com.example.data.local.SrsItemEntity
import com.example.data.local.ZeroPhraseOfflineEntity
import com.example.learning.LearningEngine
import com.example.model.AccentSoundTarget
import com.example.model.AppLanguage
import com.example.model.AudioPronunciationAnalysis
import com.example.model.BadgeItem
import com.example.model.CameraExtractResult
import com.example.model.CameraWordItem
import com.example.model.ChatMessage
import com.example.model.DailyPlanState
import com.example.model.DailyTaskType
import com.example.model.EnglishLevel
import com.example.model.IeltsSpeakingEval
import com.example.model.IeltsStudyPlan
import com.example.model.IeltsVocabItem
import com.example.model.IeltsWritingEval
import com.example.model.JournalCorrectionResult
import com.example.model.LearningGoal
import com.example.model.LeoPersonality
import com.example.model.RealLifeScenario
import com.example.model.SimulatorReportCard
import com.example.model.SourceLanguageRegistry
import com.example.model.SpeakingEvaluation
import com.example.model.SupportLevel
import com.example.model.ThemeMode
import com.example.model.TranslateLearnResult
import com.example.model.UserProfileState
import com.example.model.WeeklySummaryReport
import com.example.model.WordDrill
import com.example.notifications.SpeakUpNotificationHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SpeakUpViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private val dao = SpeakUpDatabase.getInstance(application).speakUpDao()
    private val networkMonitor = NetworkMonitor(application)
    private val leoAi = LeoAiRepository()
    private val featureAi = SpeakUpFeatureAiRepository()

    val speechEngine = SpeechCoachEngine(application)
    val audioRecorder = AudioClipRecorder(application)

    val userProfile: StateFlow<UserProfileState> = prefsRepo.userProfileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfileState()
        )

    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val practiceSessions: StateFlow<List<PracticeSessionEntity>> = dao.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val learnedWords: StateFlow<List<LearnedWordEntity>> = dao.observeLearnedWords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlinePacks: StateFlow<List<OfflinePackEntity>> = dao.observeOfflinePacks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offlineDrillsCount: StateFlow<Int> = dao.observeOfflineDrillsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val srsItems: StateFlow<List<SrsItemEntity>> = dao.observeAllSrsItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSyncQueue: StateFlow<List<PendingSyncEntity>> = dao.observePendingSync()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analyticsEvents: StateFlow<List<AnalyticsEventEntity>> = dao.observeAnalyticsEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mistakes: StateFlow<List<MistakeEntryEntity>> = dao.observeMistakes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val soundProgress: StateFlow<List<SoundProgressEntity>> = dao.observeSoundProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ieltsAttempts: StateFlow<List<IeltsAttemptEntity>> = dao.observeIeltsAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val journalEntries: StateFlow<List<JournalEntryEntity>> = dao.observeJournalEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val zeroPhrasesCount: StateFlow<Int> = dao.observeZeroPhrasesCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val ieltsStarterCount: StateFlow<Int> = dao.observeIeltsStarterCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _levelUpEvent = MutableStateFlow<EnglishLevel?>(null)
    val levelUpEvent: StateFlow<EnglishLevel?> = _levelUpEvent.asStateFlow()

    private val _adaptiveBanner = MutableStateFlow<String?>(null)
    val adaptiveBanner: StateFlow<String?> = _adaptiveBanner.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _dailyPlan = MutableStateFlow(DailyPlanState())
    val dailyPlan: StateFlow<DailyPlanState> = _dailyPlan.asStateFlow()

    private val _dailyPlanLoading = MutableStateFlow(false)
    val dailyPlanLoading: StateFlow<Boolean> = _dailyPlanLoading.asStateFlow()

    private val _weeklySummary = MutableStateFlow<WeeklySummaryReport?>(null)
    val weeklySummary: StateFlow<WeeklySummaryReport?> = _weeklySummary.asStateFlow()

    private val _weeklySummaryLoading = MutableStateFlow(false)
    val weeklySummaryLoading: StateFlow<Boolean> = _weeklySummaryLoading.asStateFlow()

    // Pronunciation Coach state
    private val _pronunciationAnalysis = MutableStateFlow<AudioPronunciationAnalysis?>(null)
    val pronunciationAnalysis: StateFlow<AudioPronunciationAnalysis?> = _pronunciationAnalysis.asStateFlow()

    private val _pronunciationLoading = MutableStateFlow(false)
    val pronunciationLoading: StateFlow<Boolean> = _pronunciationLoading.asStateFlow()

    // Translate & Learn + Journal state
    private val _translateResult = MutableStateFlow<TranslateLearnResult?>(null)
    val translateResult: StateFlow<TranslateLearnResult?> = _translateResult.asStateFlow()

    private val _translateLoading = MutableStateFlow(false)
    val translateLoading: StateFlow<Boolean> = _translateLoading.asStateFlow()

    private val _journalCorrection = MutableStateFlow<JournalCorrectionResult?>(null)
    val journalCorrection: StateFlow<JournalCorrectionResult?> = _journalCorrection.asStateFlow()

    private val _journalLoading = MutableStateFlow(false)
    val journalLoading: StateFlow<Boolean> = _journalLoading.asStateFlow()

    // Camera Learn state
    private val _cameraExtract = MutableStateFlow<CameraExtractResult?>(null)
    val cameraExtract: StateFlow<CameraExtractResult?> = _cameraExtract.asStateFlow()

    private val _cameraLoading = MutableStateFlow(false)
    val cameraLoading: StateFlow<Boolean> = _cameraLoading.asStateFlow()

    private val _cameraError = MutableStateFlow<String?>(null)
    val cameraError: StateFlow<String?> = _cameraError.asStateFlow()

    // Real-Life Simulators state
    private val _simulatorTurnLoading = MutableStateFlow(false)
    val simulatorTurnLoading: StateFlow<Boolean> = _simulatorTurnLoading.asStateFlow()

    private val _simulatorReportLoading = MutableStateFlow(false)
    val simulatorReportLoading: StateFlow<Boolean> = _simulatorReportLoading.asStateFlow()

    private val _simulatorLastTurn = MutableStateFlow<SimulatorTurnResponse?>(null)
    val simulatorLastTurn: StateFlow<SimulatorTurnResponse?> = _simulatorLastTurn.asStateFlow()

    private val _simulatorReport = MutableStateFlow<SimulatorReportCard?>(null)
    val simulatorReport: StateFlow<SimulatorReportCard?> = _simulatorReport.asStateFlow()

    // IELTS state
    private val _ieltsPlan = MutableStateFlow<IeltsStudyPlan?>(null)
    val ieltsPlan: StateFlow<IeltsStudyPlan?> = _ieltsPlan.asStateFlow()

    private val _ieltsPlanLoading = MutableStateFlow(false)
    val ieltsPlanLoading: StateFlow<Boolean> = _ieltsPlanLoading.asStateFlow()

    private val _ieltsSpeakingEval = MutableStateFlow<IeltsSpeakingEval?>(null)
    val ieltsSpeakingEval: StateFlow<IeltsSpeakingEval?> = _ieltsSpeakingEval.asStateFlow()

    private val _ieltsSpeakingLoading = MutableStateFlow(false)
    val ieltsSpeakingLoading: StateFlow<Boolean> = _ieltsSpeakingLoading.asStateFlow()

    private val _ieltsWritingEval = MutableStateFlow<IeltsWritingEval?>(null)
    val ieltsWritingEval: StateFlow<IeltsWritingEval?> = _ieltsWritingEval.asStateFlow()

    private val _ieltsWritingLoading = MutableStateFlow(false)
    val ieltsWritingLoading: StateFlow<Boolean> = _ieltsWritingLoading.asStateFlow()

    // Talk with Leo AI Chat state
    private val _chatTopic = MutableStateFlow("Daily Coffee & Small Talk")
    val chatTopic: StateFlow<String> = _chatTopic.asStateFlow()

    private val _correctMeEnabled = MutableStateFlow(true)
    val correctMeEnabled: StateFlow<Boolean> = _correctMeEnabled.asStateFlow()

    private val _askMalayalamEnabled = MutableStateFlow(false)
    val askMalayalamEnabled: StateFlow<Boolean> = _askMalayalamEnabled.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                id = "welcome_leo",
                isFromLeo = true,
                text = "Hey there! I'm Leo. Tap the mic and tell me how your day is going?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            val count = dao.observeOfflineDrillsCount().first()
            val zeroCount = dao.observeZeroPhrasesCount().first()
            val ieltsCount = dao.observeIeltsStarterCount().first()
            if (count == 0 || zeroCount == 0 || ieltsCount == 0) {
                seedBundled30DayOfflinePack()
            }
        }
        viewModelScope.launch {
            mistakes.collect { list ->
                prefsRepo.updateMistakeNotificationCount(list.count { !it.isFixed })
            }
        }
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    flushPendingSyncQueue()
                }
            }
        }
    }

    private suspend fun seedBundled30DayOfflinePack() {
        val drills = CurriculumRepository.generate30DayOfflineDrills()
        dao.insertOfflineDayDrills(drills)
        val packs = CurriculumRepository.allLevels.map { c ->
            OfflinePackEntity(
                levelIndex = c.level.index,
                levelTitle = c.level.titleEn,
                drillsCount = drills.count { it.levelIndex == c.level.index },
                daysBundled = 30
            )
        }
        dao.insertOfflinePacks(packs)

        // Seed 300 Level 0 Absolute Zero phrases offline
        val zeroEntities = AbsoluteZeroCatalog.all300Phrases.map {
            ZeroPhraseOfflineEntity(
                id = it.id,
                categoryId = it.category.id,
                englishText = it.englishText,
                malayalamMeaning = it.malayalamMeaning,
                manglishScript = it.manglishScript
            )
        }
        dao.insertZeroPhrases(zeroEntities)

        // Seed IELTS Starter Packs (Listening + Reading + Speaking + Writing + Vocab)
        val ieltsPacks = buildList {
            ExtensionsCatalog.ieltsListeningSections.forEach { sec ->
                add(
                    IeltsStarterPackEntity(
                        id = "ielts_listen_${sec.sectionNumber}",
                        skillType = "listening",
                        title = sec.title,
                        payloadJson = sec.audioScript
                    )
                )
            }
            ExtensionsCatalog.ieltsReadingPassages.forEach { pass ->
                add(
                    IeltsStarterPackEntity(
                        id = "ielts_read_${pass.id}",
                        skillType = "reading",
                        title = pass.title,
                        payloadJson = pass.body
                    )
                )
            }
            add(
                IeltsStarterPackEntity(
                    id = "ielts_vocab_awl",
                    skillType = "vocab",
                    title = "AWL + Topic Starter Deck (${ExtensionsCatalog.ieltsVocabulary.size} words)",
                    payloadJson = ExtensionsCatalog.ieltsVocabulary.joinToString(",") { it.word }
                )
            )
        }
        dao.insertIeltsStarterPacks(ieltsPacks)
        prefsRepo.setOfflineDownloaded(true)
    }

    private suspend fun flushPendingSyncQueue() {
        val pending = dao.getPendingSyncOnce()
        if (pending.isNotEmpty() && isOnline.value) {
            dao.clearSyncQueue()
            dao.markAllMistakesSynced()
            prefsRepo.markSyncedNow()
            logAnalytics("offline_queue_synced", "count=${pending.size}")
        }
    }

    fun logAnalytics(eventName: String, metadata: String = "") {
        viewModelScope.launch {
            dao.insertAnalyticsEvent(
                AnalyticsEventEntity(
                    eventName = eventName,
                    metadata = metadata
                )
            )
        }
    }

    fun completeOnboarding(
        goal: LearningGoal,
        dailyMinutes: Int,
        reminders: List<String>,
        notificationsEnabled: Boolean,
        placementAnswers: Map<Int, String>
    ) {
        viewModelScope.launch {
            val localFallback = SpeakingEvaluator.calculatePlacementLevel(
                answers = placementAnswers,
                questions = CurriculumRepository.placementQuestions
            )
            val assignedLevel = if (isOnline.value && placementAnswers.values.any { it.isNotBlank() }) {
                leoAi.evaluatePlacementWithAi(placementAnswers).getOrNull()?.level ?: localFallback
            } else {
                localFallback
            }

            prefsRepo.completeOnboarding(
                goal = goal,
                dailyMinutes = dailyMinutes,
                reminders = reminders,
                notificationsEnabled = notificationsEnabled,
                assignedLevel = assignedLevel
            )
            SpeakUpNotificationHelper.scheduleDailyReminders(
                context = getApplication(),
                times = reminders,
                enabled = notificationsEnabled
            )
            logAnalytics("onboarding_completed", "level=${assignedLevel.name},goal=${goal.name}")
            CelebrationSoundPlayer.playLevelUpFanfare(userProfile.value.soundEffectsEnabled)
            refreshDailyPlan()
        }
    }

    fun refreshDailyPlan() {
        if (_dailyPlanLoading.value) return
        viewModelScope.launch {
            _dailyPlanLoading.value = true
            val profile = userProfile.value
            val dueSrs = dao.getDueSrsItems(System.currentTimeMillis())
            val topMistakes = dao.getTopUnfixedMistakes()
            val weakWords = (dueSrs.map { it.term } + topMistakes.map { it.correctForm.take(28) }).distinct().take(5)

            if (isOnline.value) {
                val aiPlan = leoAi.generateDailyPlanWithAi(
                    level = profile.currentLevel,
                    goal = profile.goal,
                    weakWords = weakWords,
                    dailyMinutes = profile.dailyMinutesGoal
                )
                aiPlan.onSuccess { plan ->
                    _dailyPlan.value = plan
                }.onFailure {
                    _dailyPlan.value = DailyPlanState(
                        focusNote = "Speak ${profile.dailyMinutesGoal} min today • ${profile.goal.titleEn}",
                        weakWordsInFocus = weakWords,
                        isAiGenerated = false
                    )
                }
            } else {
                _dailyPlan.value = DailyPlanState(
                    focusNote = "Offline 30-day pack active • ${profile.goal.titleEn}",
                    weakWordsInFocus = weakWords,
                    isAiGenerated = false
                )
            }
            _dailyPlanLoading.value = false
        }
    }

    suspend fun evaluateSpeaking(
        spokenText: String,
        targetSentence: String,
        expectedKeywords: List<String>,
        minWords: Int,
        isExactMatchDrill: Boolean,
        itemId: String,
        malayalamHint: String
    ): SpeakingEvaluation {
        val profile = userProfile.value
        val localEval = if (isExactMatchDrill) {
            SpeakingEvaluator.evaluateSentenceMatch(spokenText, targetSentence, profile.leoPersonality)
        } else {
            SpeakingEvaluator.evaluateOpenResponse(
                spoken = spokenText,
                sampleTarget = targetSentence,
                expectedKeywords = expectedKeywords,
                minWords = minWords,
                personality = profile.leoPersonality
            )
        }

        val finalEval = if (isOnline.value && isExactMatchDrill && spokenText.isNotBlank()) {
            val aiResult = leoAi.evaluateFeedbackWithAi(targetSentence, spokenText).getOrNull()
            if (aiResult != null) {
                val stars = when {
                    aiResult.score >= 90 -> 5
                    aiResult.score >= 76 -> 4
                    aiResult.score >= 58 -> 3
                    aiResult.score >= 40 -> 2
                    else -> 1
                }
                localEval.copy(
                    stars = stars,
                    scorePercent = aiResult.score,
                    missedWords = aiResult.mispronouncedWords,
                    feedbackEn = aiResult.tip,
                    isOfflineScore = false
                )
            } else {
                localEval
            }
        } else {
            localEval
        }

        // Update SM-2 spaced repetition item
        updateSrsItem(
            itemId = itemId,
            term = targetSentence.take(40),
            malayalamHint = malayalamHint,
            sampleSentence = targetSentence,
            itemType = if (isExactMatchDrill) "sentence" else "word",
            levelIndex = profile.currentLevel.index,
            scorePercent = finalEval.scorePercent
        )

        // Log recurring mistakes into Mistake Notebook when words are missed or score < 72%
        if (finalEval.missedWords.isNotEmpty() || finalEval.scorePercent < 72) {
            recordMistakeInternal(
                category = if (finalEval.missedWords.isNotEmpty()) "pronunciation" else "sentence_pattern",
                wrongPattern = spokenText.ifBlank { finalEval.missedWords.joinToString(", ") }.take(60),
                correctForm = targetSentence,
                malayalamNote = malayalamHint.ifBlank { finalEval.feedbackMl }
            )
        }

        return finalEval
    }

    private suspend fun updateSrsItem(
        itemId: String,
        term: String,
        malayalamHint: String,
        sampleSentence: String,
        itemType: String,
        levelIndex: Int,
        scorePercent: Int
    ) {
        val existing = dao.getSrsItem(itemId)
        val sm2 = LearningEngine.computeSm2Next(
            previousRepetitions = existing?.repetitions ?: 0,
            previousIntervalDays = existing?.intervalDays ?: 1,
            previousEaseFactor = existing?.easeFactor ?: 2.5,
            scorePercent = scorePercent
        )
        dao.upsertSrsItem(
            SrsItemEntity(
                itemId = itemId,
                term = existing?.term ?: term,
                malayalamHint = malayalamHint,
                sampleSentence = sampleSentence,
                itemType = itemType,
                levelIndex = levelIndex,
                repetitions = sm2.repetitions,
                intervalDays = sm2.intervalDays,
                easeFactor = sm2.easeFactor,
                nextReviewAt = sm2.nextReviewAt,
                lastScore = scorePercent
            )
        )
    }

    fun recordTaskCompletion(
        taskType: DailyTaskType,
        level: EnglishLevel,
        stars: Int,
        scorePercent: Int = stars * 20,
        isOfflineScore: Boolean = !isOnline.value,
        minutesSpent: Int = 2
    ) {
        viewModelScope.launch {
            val bonusXp = taskType.xpReward + (stars * 3)
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val dayLabel = SimpleDateFormat("EEE", Locale.US).format(Date())
            dao.insertSession(
                PracticeSessionEntity(
                    dateKey = dateKey,
                    dayLabel = dayLabel,
                    taskId = taskType.id,
                    levelIndex = level.index,
                    minutesSpent = minutesSpent,
                    stars = stars,
                    scorePercent = scorePercent,
                    xpEarned = bonusXp,
                    isOfflineScore = isOfflineScore
                )
            )

            // Queue progress for sync if offline
            if (!isOnline.value) {
                dao.enqueueSync(
                    PendingSyncEntity(
                        actionType = "task_completed",
                        payloadJson = """{"taskId":"${taskType.id}","stars":$stars,"score":$scorePercent,"xp":$bonusXp}"""
                    )
                )
            } else {
                prefsRepo.markSyncedNow()
            }

            val leveledUpTo = prefsRepo.recordTaskCompletion(
                taskId = taskType.id,
                minutes = minutesSpent,
                xpEarned = bonusXp
            )

            // Adaptive difficulty evaluation across recent scores
            val recentScores = dao.getRecentScores()
            val profile = userProfile.value
            val adaptive = LearningEngine.evaluateAdaptiveShift(
                recentScores = recentScores,
                currentLevel = profile.currentLevel,
                currentTier = profile.difficultyTier
            )
            if (adaptive.newTier != profile.difficultyTier || adaptive.promotedLevel != null) {
                prefsRepo.setDifficultyAndLevel(adaptive.newTier, adaptive.promotedLevel)
                _adaptiveBanner.value = if (profile.language == AppLanguage.MALAYALAM) {
                    adaptive.reasonMl
                } else {
                    adaptive.reasonEn
                }
            }

            logAnalytics("task_completed", "task=${taskType.id},score=$scorePercent,offline=$isOfflineScore")
            logAnalytics("streak_updated", "streak=${userProfile.value.streakDays}")

            val finalLevelUp = leveledUpTo ?: adaptive.promotedLevel
            if (finalLevelUp != null) {
                logAnalytics("level_up", "level=${finalLevelUp.name}")
                _levelUpEvent.value = finalLevelUp
                CelebrationSoundPlayer.playLevelUpFanfare(userProfile.value.soundEffectsEnabled)
            } else {
                CelebrationSoundPlayer.playSuccessChime(userProfile.value.soundEffectsEnabled)
            }
        }
    }

    fun recordLearnedWord(drill: WordDrill, level: EnglishLevel, scorePercent: Int = 85) {
        viewModelScope.launch {
            dao.insertWord(
                LearnedWordEntity(
                    word = drill.word,
                    malayalamMeaning = drill.malayalamMeaning,
                    sampleSentence = drill.sampleSentence,
                    levelIndex = level.index
                )
            )
            updateSrsItem(
                itemId = "word_${drill.word.lowercase()}",
                term = drill.word,
                malayalamHint = drill.malayalamMeaning,
                sampleSentence = drill.sampleSentence,
                itemType = "word",
                levelIndex = level.index,
                scorePercent = scorePercent
            )
        }
    }

    fun generateWeeklySummary() {
        if (_weeklySummaryLoading.value) return
        viewModelScope.launch {
            _weeklySummaryLoading.value = true
            val sessions = practiceSessions.value
            val totalMins = maxOf(sessions.sumOf { it.minutesSpent }, userProfile.value.todayMinutesPracticed)
            val wordsCount = learnedWords.value.size
            val avgScore = if (sessions.isNotEmpty()) sessions.map { it.scorePercent }.average().toInt() else 80
            val streak = userProfile.value.streakDays

            if (isOnline.value) {
                val res = leoAi.generateWeeklySummaryWithAi(totalMins, wordsCount, avgScore, streak)
                res.onSuccess { report ->
                    _weeklySummary.value = report
                }.onFailure {
                    _weeklySummary.value = WeeklySummaryReport(
                        headline = "$totalMins Minutes Spoken This Week",
                        summary = "You practiced $wordsCount words with an average score of $avgScore% and kept a $streak-day streak.",
                        nextWeekFocus = "Keep speaking 10 minutes daily to unlock your next milestone.",
                        isAiGenerated = false
                    )
                }
            } else {
                _weeklySummary.value = WeeklySummaryReport(
                    headline = "$totalMins Minutes Spoken (Offline Summary)",
                    summary = "You practiced $wordsCount words with an average score of $avgScore% and a $streak-day streak.",
                    nextWeekFocus = "Connect to internet anytime for Leo's AI coaching insights.",
                    isAiGenerated = false
                )
            }
            _weeklySummaryLoading.value = false
        }
    }

    fun buildExportProgressIntent(): Intent {
        val profile = userProfile.value
        val root = JSONObject().apply {
            put("app", "SpeakUp")
            put("exportedAt", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date()))
            put("level", profile.currentLevel.name)
            put("difficultyTier", profile.difficultyTier.name)
            put("xp", profile.xp)
            put("streakDays", profile.streakDays)
            put("goal", profile.goal.name)
            put("dailyMinutesGoal", profile.dailyMinutesGoal)
            put("sessionsCompleted", practiceSessions.value.size)
            put("wordsLearned", JSONArray(learnedWords.value.map { it.word }))
            put("srsItemsCount", srsItems.value.size)
            put("analyticsEventsCount", analyticsEvents.value.size)
        }
        logAnalytics("export_progress", "sessions=${practiceSessions.value.size}")
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "SpeakUp Progress Export")
            putExtra(Intent.EXTRA_TEXT, root.toString(2))
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            prefsRepo.resetAllProgress()
            dao.clearSessions()
            dao.clearWords()
            dao.clearSrs()
            dao.clearSyncQueue()
            dao.clearAnalytics()
            dao.clearMistakes()
            dao.clearSoundProgress()
            dao.clearIeltsAttempts()
            dao.clearJournal()
            _weeklySummary.value = null
            _adaptiveBanner.value = null
            _pronunciationAnalysis.value = null
            _translateResult.value = null
            _journalCorrection.value = null
            _cameraExtract.value = null
            _ieltsPlan.value = null
            _ieltsSpeakingEval.value = null
            _ieltsWritingEval.value = null
            logAnalytics("progress_reset", "")
        }
    }

    fun dismissLevelUp() {
        _levelUpEvent.value = null
    }

    fun dismissAdaptiveBanner() {
        _adaptiveBanner.value = null
    }

    fun triggerLevelCelebrationPreview(level: EnglishLevel) {
        _levelUpEvent.value = level
        CelebrationSoundPlayer.playLevelUpFanfare(userProfile.value.soundEffectsEnabled)
    }

    fun toggleLanguage() {
        viewModelScope.launch { prefsRepo.toggleLanguage() }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { prefsRepo.setLanguage(language) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { prefsRepo.setThemeMode(mode) }
    }

    fun setPersonality(personality: LeoPersonality) {
        viewModelScope.launch { prefsRepo.setPersonality(personality) }
    }

    fun setDailyGoal(minutes: Int) {
        viewModelScope.launch { prefsRepo.setDailyMinutesGoal(minutes) }
    }

    fun setLearningGoal(goal: LearningGoal) {
        viewModelScope.launch { prefsRepo.setGoal(goal) }
    }

    fun updateReminders(times: List<String>, enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setReminders(times, enabled)
            SpeakUpNotificationHelper.scheduleDailyReminders(getApplication(), times, enabled)
        }
    }

    fun sendTestNotification() {
        SpeakUpNotificationHelper.showSmartScheduledReminder(getApplication(), isManualPreview = true)
    }

    fun activateStreakFreeze() {
        viewModelScope.launch { prefsRepo.activateStreakFreeze() }
    }

    fun setSoundEffects(enabled: Boolean) {
        viewModelScope.launch { prefsRepo.setSoundEffectsEnabled(enabled) }
    }

    fun selectActiveLevel(level: EnglishLevel) {
        viewModelScope.launch { prefsRepo.setCurrentLevel(level) }
    }

    fun retakePlacementTest() {
        viewModelScope.launch { prefsRepo.resetPlacementForRetake() }
    }

    fun downloadOfflinePacks() {
        if (_downloadProgress.value != null) return
        viewModelScope.launch {
            _downloadProgress.value = 0.15f
            seedBundled30DayOfflinePack()
            _downloadProgress.value = 0.7f
            delay(250)
            _downloadProgress.value = 1f
            delay(180)
            _downloadProgress.value = null
            CelebrationSoundPlayer.playSuccessChime(userProfile.value.soundEffectsEnabled)
        }
    }

    // Support Level & Source Language Config
    fun setSupportLevel(support: SupportLevel) {
        viewModelScope.launch { prefsRepo.setSupportLevel(support, userOverrode = true) }
    }

    fun setSourceLanguageCode(code: String) {
        viewModelScope.launch { prefsRepo.setSourceLanguageCode(code) }
    }

    fun computeDaysUntilExam(): Int =
        prefsRepo.computeDaysUntilExam(userProfile.value.ieltsExamDateKey)

    // Mistake Notebook helpers
    private suspend fun recordMistakeInternal(
        category: String,
        wrongPattern: String,
        correctForm: String,
        malayalamNote: String
    ) {
        val cleanCorrect = correctForm.trim()
        if (cleanCorrect.isBlank()) return
        val id = "${category}_${cleanCorrect.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "_").take(36)}"
        val existing = dao.getMistakeById(id)
        val entry = if (existing != null) {
            existing.copy(
                wrongPattern = wrongPattern.ifBlank { existing.wrongPattern },
                occurrences = existing.occurrences + 1,
                fixedStreak = 0,
                isFixed = false,
                lastSeenAt = System.currentTimeMillis(),
                synced = false
            )
        } else {
            MistakeEntryEntity(
                id = id,
                category = category,
                wrongPattern = wrongPattern.ifBlank { "Needs review" },
                correctForm = cleanCorrect,
                malayalamNote = malayalamNote,
                synced = isOnline.value
            )
        }
        dao.upsertMistake(entry)
        if (!isOnline.value) {
            dao.enqueueSync(PendingSyncEntity(actionType = "mistake_upsert", payloadJson = """{"id":"$id"}"""))
        }
    }

    fun practiceMistakeResult(mistakeId: String, scorePercent: Int) {
        viewModelScope.launch {
            val existing = dao.getMistakeById(mistakeId) ?: return@launch
            val passed = scorePercent >= 80
            val newStreak = if (passed) existing.fixedStreak + 1 else 0
            val fixed = newStreak >= 2
            dao.upsertMistake(
                existing.copy(
                    fixedStreak = newStreak,
                    isFixed = fixed,
                    lastSeenAt = System.currentTimeMillis(),
                    synced = isOnline.value
                )
            )
            if (fixed) {
                CelebrationSoundPlayer.playLevelUpFanfare(userProfile.value.soundEffectsEnabled)
            } else if (passed) {
                CelebrationSoundPlayer.playSuccessChime(userProfile.value.soundEffectsEnabled)
            }
        }
    }

    // Malayali-Accent Pronunciation Coach (Real Audio -> Gemini + Weekly Sound Progress)
    fun analyzeAccentAudio(
        audioBytes: ByteArray,
        targetSentence: String,
        soundTarget: AccentSoundTarget
    ) {
        if (_pronunciationLoading.value) return
        viewModelScope.launch {
            _pronunciationLoading.value = true
            val profile = userProfile.value
            val sourceLang = SourceLanguageRegistry.byCode(profile.sourceLanguageCode)
            val result = if (isOnline.value && audioBytes.isNotEmpty()) {
                featureAi.analyzePronunciationAudio(
                    audioBytes = audioBytes,
                    targetSentence = targetSentence,
                    supportLevel = profile.supportLevel,
                    sourceLang = sourceLang
                ).getOrNull()
            } else null

            val finalAnalysis = result ?: AudioPronunciationAnalysis(
                overallScore = 76,
                wordScores = targetSentence.split(" ").take(6).map {
                    com.example.model.WordPronunciationScore(it.trim(',', '.', '?'), 76, "Listen to slow model audio")
                },
                soundErrors = listOf(
                    com.example.model.SoundErrorDetail(
                        soundTargetId = soundTarget.id,
                        issue = "Offline check: focus on ${soundTarget.labelEn}",
                        correctionTip = soundTarget.mouthGuideMl
                    )
                ),
                tip = soundTarget.mouthGuideMl,
                transcriptHeard = targetSentence,
                isOfflineFallback = true
            )
            _pronunciationAnalysis.value = finalAnalysis

            val weekKey = SimpleDateFormat("yyyy-'W'ww", Locale.US).format(Date())
            dao.insertSoundProgress(
                SoundProgressEntity(
                    soundTargetId = soundTarget.id,
                    score = finalAnalysis.overallScore,
                    weekKey = weekKey
                )
            )
            if (finalAnalysis.overallScore < 80) {
                recordMistakeInternal(
                    category = "pronunciation",
                    wrongPattern = soundTarget.labelEn,
                    correctForm = targetSentence,
                    malayalamNote = finalAnalysis.tip
                )
            }
            _pronunciationLoading.value = false
        }
    }

    // Say in Malayalam -> Learn in English + Daily Journal
    fun translateMalayalamToEnglish(malayalamText: String) {
        val cleaned = malayalamText.trim()
        if (cleaned.isBlank() || _translateLoading.value) return
        viewModelScope.launch {
            _translateLoading.value = true
            val profile = userProfile.value
            val sourceLang = SourceLanguageRegistry.byCode(profile.sourceLanguageCode)
            val res = if (isOnline.value) {
                featureAi.translateAndLearn(cleaned, profile.currentLevel, sourceLang).getOrNull()
            } else null

            _translateResult.value = res ?: TranslateLearnResult(
                malayalamInput = cleaned,
                formalEnglish = "Could you please help me with this?",
                casualEnglish = "Can you help me with this?",
                manglishFormal = "കുഡ് യു പ്ലീസ് ഹെൽപ് മീ വിത്ത് ദിസ്?",
                manglishCasual = "കാൻ യു ഹെൽപ് മീ വിത്ത് ദിസ്?",
                contextNote = "ഇന്റർനെറ്റ് ലഭിക്കുമ്പോൾ നിങ്ങളുടെ കൃത്യമായ വാക്യത്തിന് AI വിവർത്തനം ലഭിക്കും.",
                isAiGenerated = false
            )
            _translateLoading.value = false
        }
    }

    fun polishDailyJournal(spokenEnglish: String) {
        val cleaned = spokenEnglish.trim()
        if (cleaned.isBlank() || _journalLoading.value) return
        viewModelScope.launch {
            _journalLoading.value = true
            val profile = userProfile.value
            val res = if (isOnline.value) {
                featureAi.polishDailyJournal(cleaned, profile.supportLevel).getOrNull()
            } else null

            val finalRes = res ?: JournalCorrectionResult(
                spokenOriginal = cleaned,
                naturalVersion = cleaned,
                keyImprovements = listOf("Connect online to get Leo's natural speaking polish."),
                encouragement = "Awesome habit speaking your thoughts in English every day!",
                isAiGenerated = false
            )
            _journalCorrection.value = finalRes
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            dao.insertJournalEntry(
                JournalEntryEntity(
                    spokenOriginal = cleaned,
                    naturalVersion = finalRes.naturalVersion,
                    dateKey = dateKey
                )
            )
            _journalLoading.value = false
        }
    }

    // Camera Learn
    fun analyzeCameraBitmap(bitmap: Bitmap) {
        if (_cameraLoading.value) return
        if (!isOnline.value) {
            _cameraError.value = "Camera Learn needs internet for Vision extraction. Saved words remain available offline!"
            return
        }
        viewModelScope.launch {
            _cameraLoading.value = true
            _cameraError.value = null
            val sourceLang = SourceLanguageRegistry.byCode(userProfile.value.sourceLanguageCode)
            featureAi.extractWordsFromPhoto(bitmap, sourceLang)
                .onSuccess { _cameraExtract.value = it }
                .onFailure { _cameraError.value = it.localizedMessage ?: "Couldn't read text from photo." }
            _cameraLoading.value = false
        }
    }

    fun saveCameraWordToSrs(item: CameraWordItem) {
        viewModelScope.launch {
            dao.insertWord(
                LearnedWordEntity(
                    word = item.englishText,
                    malayalamMeaning = item.malayalamMeaning,
                    sampleSentence = item.exampleSentence,
                    levelIndex = userProfile.value.currentLevel.index
                )
            )
            updateSrsItem(
                itemId = "cam_${item.englishText.lowercase(Locale.US).take(28)}",
                term = item.englishText,
                malayalamHint = item.malayalamMeaning,
                sampleSentence = item.exampleSentence,
                itemType = "camera_word",
                levelIndex = userProfile.value.currentLevel.index,
                scorePercent = 70
            )
        }
    }

    // Real-Life Simulators
    fun sendSimulatorTurn(
        scenario: RealLifeScenario,
        userMessage: String,
        difficulty: Int,
        surpriseMe: Boolean,
        historyPairs: List<Pair<Boolean, String>>
    ) {
        if (_simulatorTurnLoading.value) return
        viewModelScope.launch {
            _simulatorTurnLoading.value = true
            val res = if (isOnline.value) {
                featureAi.generateSimulatorTurn(scenario, userMessage, difficulty, surpriseMe, historyPairs).getOrNull()
            } else null

            _simulatorLastTurn.value = res ?: SimulatorTurnResponse(
                characterReply = if (surpriseMe) scenario.surpriseTwist else "Thank you! Could you tell me one more detail about that?",
                malayalamHint = "കൂടുതൽ വിവരങ്ങൾ ചെറിയ വാക്യത്തിൽ പറയൂ.",
                newUsefulPhrase = "Could you please confirm that?"
            )
            _simulatorTurnLoading.value = false
        }
    }

    fun finishSimulatorForReport(scenario: RealLifeScenario, dialogue: List<Pair<Boolean, String>>) {
        if (_simulatorReportLoading.value) return
        viewModelScope.launch {
            _simulatorReportLoading.value = true
            val res = if (isOnline.value) {
                featureAi.generateSimulatorReport(scenario, dialogue).getOrNull()
            } else null

            _simulatorReport.value = res ?: SimulatorReportCard(
                fluencyScore = 80,
                confidenceScore = 82,
                newWordsLearned = listOf("polite request", "confirm", "schedule"),
                improvementPoints = listOf(
                    "Pause briefly before key nouns for clarity.",
                    "Keep polite openings like 'Excuse me' or 'Could you'.",
                    "Answer the follow-up question in 2 complete sentences."
                )
            )
            _simulatorReportLoading.value = false
        }
    }

    fun saveSimulatorWords(words: List<String>) {
        viewModelScope.launch {
            words.forEach { word ->
                updateSrsItem(
                    itemId = "sim_${word.lowercase(Locale.US).take(24)}",
                    term = word,
                    malayalamHint = "From real-life roleplay",
                    sampleSentence = "I practiced using '$word' in roleplay.",
                    itemType = "word",
                    levelIndex = userProfile.value.currentLevel.index,
                    scorePercent = 75
                )
            }
        }
    }

    fun clearSimulatorState() {
        _simulatorLastTurn.value = null
        _simulatorReport.value = null
    }

    // IELTS Preparation Module
    fun saveIeltsSetup(targetBand: Double, examDateKey: String) {
        viewModelScope.launch {
            prefsRepo.updateIeltsConfig(targetBand, examDateKey)
        }
    }

    fun generateIeltsPlan(currentBand: Double, targetBand: Double, daysLeft: Int) {
        if (_ieltsPlanLoading.value) return
        viewModelScope.launch {
            _ieltsPlanLoading.value = true
            val res = if (isOnline.value) {
                featureAi.generateIeltsStudyPlan(currentBand, targetBand, daysLeft, userProfile.value.supportLevel).getOrNull()
            } else null

            _ieltsPlan.value = res ?: com.example.model.IeltsStudyPlan(
                estimatedCurrentBand = currentBand,
                targetBand = targetBand,
                weeklyFocus = listOf(
                    "Daily Part 2 2-minute cue card fluency drills",
                    "Eliminate V/W and TH/DH pronunciation slips",
                    "Timed Reading T/F/NG + Writing Task 2 structure"
                ),
                dailyChecklist = listOf(
                    "10 min IELTS Speaking Part 1/2",
                    "1 Listening section + Malayalam review",
                    "5 AWL vocabulary sentences via SM-2"
                ),
                malayalamMotivation = "ദിവസവും 20 മിനിറ്റ് പരിശീലിച്ചാൽ $daysLeft ദിവസത്തിനുള്ളിൽ മികച്ച ബാൻഡ് നേടാം!",
                isAiGenerated = false
            )
            _ieltsPlanLoading.value = false
        }
    }

    fun evaluateIeltsSpeaking(part: Int, question: String, transcript: String, durationSeconds: Int) {
        if (_ieltsSpeakingLoading.value) return
        viewModelScope.launch {
            _ieltsSpeakingLoading.value = true
            val res = if (isOnline.value) {
                featureAi.evaluateIeltsSpeaking(part, question, transcript, durationSeconds).getOrNull()
            } else null

            val words = transcript.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val finalEval = res ?: IeltsSpeakingEval(
                bandScore = if (words >= 35) 6.5 else 5.5,
                breakdown = com.example.model.IeltsCriteriaBreakdown(6.0, 6.0, 6.0, 6.5),
                fillerWordCount = Regex("\\b(um|uh|like|actually)\\b", RegexOption.IGNORE_CASE).findAll(transcript).count(),
                wordsPerMinute = ((words * 60) / durationSeconds.coerceAtLeast(10)).coerceIn(60, 180),
                feedbackSummary = "Clear message! Extend your ideas with one specific real-life example.",
                modelImprovedAnswer = "Personally, I find that daily spoken practice builds lasting confidence because it trains quick thinking.",
                weakestFocus = "Idea expansion"
            )
            _ieltsSpeakingEval.value = finalEval
            recordIeltsSkillAttempt("speaking", finalEval.bandScore, finalEval.weakestFocus, finalEval.feedbackSummary, false)
            _ieltsSpeakingLoading.value = false
        }
    }

    fun evaluateIeltsWriting(taskNumber: Int, promptText: String, essayText: String, handwritingPhoto: Bitmap?) {
        if (_ieltsWritingLoading.value) return
        viewModelScope.launch {
            _ieltsWritingLoading.value = true
            val res = if (isOnline.value) {
                featureAi.evaluateIeltsWriting(taskNumber, promptText, essayText, handwritingPhoto).getOrNull()
            } else null

            val words = essayText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val finalEval = res ?: IeltsWritingEval(
                bandScore = if (words >= 150) 6.5 else 5.5,
                breakdown = com.example.model.IeltsCriteriaBreakdown(6.0, 6.0, 6.0, 6.5),
                corrections = listOf("Connect online for detailed sentence-by-sentence writing evaluation."),
                rewrittenParagraph = "Furthermore, dedicating daily time to practical communication equips learners with measurable workplace confidence.",
                usefulVocabulary = listOf("consequently", "measurable", "proficiency", "perspective")
            )
            _ieltsWritingEval.value = finalEval
            recordIeltsSkillAttempt("writing", finalEval.bandScore, "Writing Task $taskNumber", "Words: $words", false)
            _ieltsWritingLoading.value = false
        }
    }

    fun recordIeltsSkillAttempt(
        skill: String,
        band: Double,
        weakestType: String,
        summary: String,
        isMock: Boolean
    ) {
        viewModelScope.launch {
            dao.insertIeltsAttempt(
                IeltsAttemptEntity(
                    skill = skill,
                    bandScore = band,
                    weakestQuestionType = weakestType,
                    detailsSummary = summary,
                    isMockTest = isMock
                )
            )
            prefsRepo.updateIeltsEstimatedBand(band)
        }
    }

    fun saveIeltsVocabToSrs(item: IeltsVocabItem) {
        viewModelScope.launch {
            dao.insertWord(
                LearnedWordEntity(
                    word = item.word,
                    malayalamMeaning = item.malayalamMeaning,
                    sampleSentence = item.academicExample,
                    levelIndex = userProfile.value.currentLevel.index
                )
            )
            updateSrsItem(
                itemId = "ielts_${item.word.lowercase(Locale.US)}",
                term = item.word,
                malayalamHint = item.malayalamMeaning,
                sampleSentence = item.academicExample,
                itemType = "ielts_vocab",
                levelIndex = userProfile.value.currentLevel.index,
                scorePercent = 70
            )
        }
    }

    // Talk with Leo actions
    fun selectChatTopic(topic: String) {
        _chatTopic.value = topic
        _chatError.value = null
    }

    fun toggleCorrectMe() {
        _correctMeEnabled.update { !it }
    }

    fun toggleAskMalayalam() {
        _askMalayalamEnabled.update { !it }
    }

    fun sendUserChatMessage(text: String, speakReply: Boolean = true) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _chatLoading.value) return
        if (!isOnline.value) {
            _chatError.value = if (userProfile.value.language == AppLanguage.MALAYALAM) {
                "സംസാരിക്കാൻ ലിയോയ്ക്ക് ഇന്റർനെറ്റ് ആവശ്യമാണ്."
            } else {
                "Leo needs internet to chat. Daily tasks still work offline!"
            }
            return
        }

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromLeo = false,
            text = trimmed
        )
        _chatMessages.update { it + userMsg }
        _chatError.value = null
        _chatLoading.value = true

        viewModelScope.launch {
            val historyPairs = _chatMessages.value.map { it.isFromLeo to it.text }
            val profile = userProfile.value
            val result = leoAi.chatWithLeo(
                userMessage = trimmed,
                topic = _chatTopic.value,
                level = profile.currentLevel,
                personality = profile.leoPersonality,
                correctMe = _correctMeEnabled.value,
                askMalayalam = _askMalayalamEnabled.value,
                historyPairs = historyPairs
            )
            _chatLoading.value = false
            result.onSuccess { structured ->
                val leoMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromLeo = true,
                    text = structured.reply,
                    correctionText = structured.gentleCorrection,
                    malayalamHelp = structured.malayalamTip
                )
                _chatMessages.update { it + leoMsg }
                if (!structured.gentleCorrection.isNullOrBlank()) {
                    recordMistakeInternal(
                        category = "sentence_pattern",
                        wrongPattern = trimmed.take(60),
                        correctForm = structured.gentleCorrection,
                        malayalamNote = structured.malayalamTip.orEmpty()
                    )
                }
                if (speakReply) {
                    speechEngine.speak(structured.reply)
                }
            }.onFailure { err ->
                _chatError.value = err.message ?: "Leo needs internet to chat."
            }
        }
    }

    fun requestMalayalamHelpForMessage(messageId: String) {
        val msg = _chatMessages.value.firstOrNull { it.id == messageId } ?: return
        if (!msg.malayalamHelp.isNullOrBlank()) return
        if (!isOnline.value) {
            _chatError.value = "Leo needs internet to chat."
            return
        }

        viewModelScope.launch {
            try {
                leoAi.streamMalayalamHelp(msg.text).collect { partialMl ->
                    _chatMessages.update { list ->
                        list.map { item ->
                            if (item.id == messageId) item.copy(malayalamHelp = partialMl) else item
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _chatError.value = e.localizedMessage ?: "Couldn't load Malayalam help."
            }
        }
    }

    fun computeBadges(profile: UserProfileState, sessionsCount: Int, wordsCount: Int): List<BadgeItem> {
        return listOf(
            BadgeItem(
                id = "first_voice",
                titleEn = "First Roar",
                titleMl = "ആദ്യ ശബ്ദം",
                descEn = "Completed your first speaking task",
                descMl = "ആദ്യ സംസാര പരിശീലനം പൂർത്തിയാക്കി",
                unlocked = sessionsCount >= 1 || profile.xp > 0
            ),
            BadgeItem(
                id = "word_collector",
                titleEn = "Word Builder",
                titleMl = "വാക്ക് ശേഖരം",
                descEn = "Spoke 5+ new words in sentences",
                descMl = "5 പുതിയ വാക്കുകൾ വാക്യത്തിൽ ഉപയോഗിച്ചു",
                unlocked = wordsCount >= 5
            ),
            BadgeItem(
                id = "daily_champ",
                titleEn = "Daily 5-Star",
                titleMl = "ദിവസ ചാമ്പ്യൻ",
                descEn = "Finished all 5 daily tasks in one day",
                descMl = "ഒരു ദിവസത്തെ 5 ടാസ്ക്കുകളും പൂർത്തിയാക്കി",
                unlocked = profile.completedTaskIdsToday.size >= 5
            ),
            BadgeItem(
                id = "streak_keeper",
                titleEn = "Flame Keeper",
                titleMl = "തുടർക്കഥ",
                descEn = "Reached a 3-day speaking streak",
                descMl = "3 ദിവസത്തെ തുടർച്ചയായ പരിശീലനം",
                unlocked = profile.streakDays >= 3
            ),
            BadgeItem(
                id = "rising_speaker",
                titleEn = "Level Climber",
                titleMl = "ഉയർച്ച",
                descEn = "Reached Elementary level or higher",
                descMl = "എലമെന്ററി ലെവൽ എത്തി",
                unlocked = profile.currentLevel.index >= EnglishLevel.ELEMENTARY.index
            ),
            BadgeItem(
                id = "offline_ready",
                titleEn = "30-Day Offline",
                titleMl = "ഓഫ്‌ലൈൻ വീരൻ",
                descEn = "Bundled 30-day speaking pack ready",
                descMl = "30 ദിവസത്തെ ഓഫ്‌ലൈൻ പാഠങ്ങൾ തയ്യാർ",
                unlocked = profile.offlineDownloaded
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechEngine.release()
        audioRecorder.release()
        networkMonitor.unregister()
    }
}
