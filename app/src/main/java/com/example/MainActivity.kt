package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.configureAppCheck
import com.example.model.DailyTaskType
import com.example.model.EnglishLevel
import com.example.model.LeoMood
import com.example.notifications.SpeakUpNotificationHelper
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.LeoMascotAvatar
import com.example.ui.components.bi
import com.example.ui.screens.AbsoluteZeroScreen
import com.example.ui.screens.CameraLearnScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IeltsPrepScreen
import com.example.ui.screens.LearnDestination
import com.example.ui.screens.LearnHubScreen
import com.example.ui.screens.LevelPathScreen
import com.example.ui.screens.MalayalamToEnglishScreen
import com.example.ui.screens.MistakeNotebookScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.PronunciationCoachScreen
import com.example.ui.screens.RealLifeSimulatorScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TalkWithLeoScreen
import com.example.ui.theme.SpeakUpTheme
import com.example.ui.viewmodel.SpeakUpViewModel

private const val ROUTE_HOME = "home"
private const val ROUTE_LEARN = "learn"
private const val ROUTE_TALK = "talk"
private const val ROUTE_IELTS = "ielts"
private const val ROUTE_PROGRESS = "progress"
private const val ROUTE_SETTINGS = "settings"

private data class BottomTab(
    val route: String,
    val labelEn: String,
    val labelMl: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomTabs = listOf(
    BottomTab(ROUTE_HOME, "Home", "ഹോം", Icons.Filled.Home, Icons.Outlined.Home),
    BottomTab(ROUTE_LEARN, "Learn", "പഠനം", Icons.Filled.School, Icons.Outlined.School),
    BottomTab(ROUTE_TALK, "Talk", "സംസാരം", Icons.Filled.Forum, Icons.Outlined.Forum),
    BottomTab(ROUTE_IELTS, "IELTS", "IELTS", Icons.Filled.WorkspacePremium, Icons.Outlined.WorkspacePremium),
    BottomTab(ROUTE_PROGRESS, "Progress", "പുരോഗതി", Icons.Filled.BarChart, Icons.Outlined.BarChart)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before rendering UI or creating ViewModels.
        configureAppCheck(this, intent)

        val launchStartToday = intent?.getBooleanExtra(
            SpeakUpNotificationHelper.EXTRA_START_TODAY_PRACTICE,
            false
        ) == true

        enableEdgeToEdge()
        setContent {
            val viewModel: SpeakUpViewModel = viewModel()
            val profile by viewModel.userProfile.collectAsStateWithLifecycle()

            SpeakUpTheme(themeMode = profile.themeMode) {
                SpeakUpRoot(
                    viewModel = viewModel,
                    launchStartToday = launchStartToday
                )
            }
        }
    }
}

@Composable
private fun SpeakUpRoot(
    viewModel: SpeakUpViewModel,
    launchStartToday: Boolean
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val sessions by viewModel.practiceSessions.collectAsStateWithLifecycle()
    val learnedWords by viewModel.learnedWords.collectAsStateWithLifecycle()
    val offlinePacks by viewModel.offlinePacks.collectAsStateWithLifecycle()
    val offlineDrillsCount by viewModel.offlineDrillsCount.collectAsStateWithLifecycle()
    val zeroPhrasesCount by viewModel.zeroPhrasesCount.collectAsStateWithLifecycle()
    val ieltsStarterCount by viewModel.ieltsStarterCount.collectAsStateWithLifecycle()
    val srsItems by viewModel.srsItems.collectAsStateWithLifecycle()
    val pendingSyncQueue by viewModel.pendingSyncQueue.collectAsStateWithLifecycle()
    val analyticsEvents by viewModel.analyticsEvents.collectAsStateWithLifecycle()
    val mistakes by viewModel.mistakes.collectAsStateWithLifecycle()
    val soundProgress by viewModel.soundProgress.collectAsStateWithLifecycle()
    val ieltsAttempts by viewModel.ieltsAttempts.collectAsStateWithLifecycle()
    val journalEntries by viewModel.journalEntries.collectAsStateWithLifecycle()

    val levelUpEvent by viewModel.levelUpEvent.collectAsStateWithLifecycle()
    val adaptiveBanner by viewModel.adaptiveBanner.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val dailyPlan by viewModel.dailyPlan.collectAsStateWithLifecycle()
    val dailyPlanLoading by viewModel.dailyPlanLoading.collectAsStateWithLifecycle()
    val weeklySummary by viewModel.weeklySummary.collectAsStateWithLifecycle()
    val weeklySummaryLoading by viewModel.weeklySummaryLoading.collectAsStateWithLifecycle()

    val pronunciationAnalysis by viewModel.pronunciationAnalysis.collectAsStateWithLifecycle()
    val pronunciationLoading by viewModel.pronunciationLoading.collectAsStateWithLifecycle()
    val translateResult by viewModel.translateResult.collectAsStateWithLifecycle()
    val translateLoading by viewModel.translateLoading.collectAsStateWithLifecycle()
    val journalCorrection by viewModel.journalCorrection.collectAsStateWithLifecycle()
    val journalLoading by viewModel.journalLoading.collectAsStateWithLifecycle()
    val cameraExtract by viewModel.cameraExtract.collectAsStateWithLifecycle()
    val cameraLoading by viewModel.cameraLoading.collectAsStateWithLifecycle()
    val cameraError by viewModel.cameraError.collectAsStateWithLifecycle()
    val simTurnLoading by viewModel.simulatorTurnLoading.collectAsStateWithLifecycle()
    val simReportLoading by viewModel.simulatorReportLoading.collectAsStateWithLifecycle()
    val simLastTurn by viewModel.simulatorLastTurn.collectAsStateWithLifecycle()
    val simReport by viewModel.simulatorReport.collectAsStateWithLifecycle()

    val ieltsPlan by viewModel.ieltsPlan.collectAsStateWithLifecycle()
    val ieltsPlanLoading by viewModel.ieltsPlanLoading.collectAsStateWithLifecycle()
    val ieltsSpeakingEval by viewModel.ieltsSpeakingEval.collectAsStateWithLifecycle()
    val ieltsSpeakingLoading by viewModel.ieltsSpeakingLoading.collectAsStateWithLifecycle()
    val ieltsWritingEval by viewModel.ieltsWritingEval.collectAsStateWithLifecycle()
    val ieltsWritingLoading by viewModel.ieltsWritingLoading.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatTopic by viewModel.chatTopic.collectAsStateWithLifecycle()
    val correctMeEnabled by viewModel.correctMeEnabled.collectAsStateWithLifecycle()
    val askMalayalamEnabled by viewModel.askMalayalamEnabled.collectAsStateWithLifecycle()
    val chatLoading by viewModel.chatLoading.collectAsStateWithLifecycle()
    val chatError by viewModel.chatError.collectAsStateWithLifecycle()

    var activePractice by remember { mutableStateOf<Pair<DailyTaskType, EnglishLevel>?>(null) }
    var learnSubDestination by remember { mutableStateOf(LearnDestination.HUB) }
    var consumedDeepLink by remember { mutableStateOf(false) }

    LaunchedEffect(launchStartToday, profile.onboardingCompleted) {
        if (launchStartToday && profile.onboardingCompleted && !consumedDeepLink) {
            consumedDeepLink = true
            val nextTask = DailyTaskType.entries.firstOrNull { !profile.completedTaskIdsToday.contains(it.id) }
                ?: DailyTaskType.SHADOW_SPEAKING
            activePractice = nextTask to profile.currentLevel
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME
    val lang = profile.language

    if (!profile.onboardingCompleted) {
        OnboardingScreen(
            language = lang,
            speechEngine = viewModel.speechEngine,
            onToggleLanguage = { viewModel.toggleLanguage() },
            onFinishOnboarding = { goal, mins, reminders, enabled, answers ->
                viewModel.completeOnboarding(goal, mins, reminders, enabled, answers)
            }
        )
        return
    }

    activePractice?.let { (taskType, level) ->
        PracticeScreen(
            taskType = taskType,
            level = level,
            profile = profile,
            speechEngine = viewModel.speechEngine,
            onEvaluateSpeaking = { spoken, target, keywords, minWords, isExact, itemId, mlHint ->
                viewModel.evaluateSpeaking(
                    spokenText = spoken,
                    targetSentence = target,
                    expectedKeywords = keywords,
                    minWords = minWords,
                    isExactMatchDrill = isExact,
                    itemId = itemId,
                    malayalamHint = mlHint
                )
            },
            onWordSpoken = { wordDrill, score ->
                viewModel.recordLearnedWord(wordDrill, level, score)
            },
            onTaskCompleted = { stars, scorePercent, isOffline ->
                viewModel.recordTaskCompletion(taskType, level, stars, scorePercent, isOffline)
            },
            onDropOff = { stepIdx ->
                viewModel.logAnalytics("drop_off", "task=${taskType.id},step=$stepIdx")
            },
            onBack = { activePractice = null }
        )
        return
    }

    fun navigateTopTab(route: String) {
        navController.navigate(route) {
            popUpTo(ROUTE_HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val unfixedMistakesCount = remember(mistakes) { mistakes.count { !it.isFixed } }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars
            ) {
                bottomTabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (tab.route == ROUTE_LEARN) {
                                learnSubDestination = LearnDestination.HUB
                            }
                            navigateTopTab(tab.route)
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.labelEn
                            )
                        },
                        label = {
                            Text(
                                text = bi(tab.labelEn, tab.labelMl, lang),
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("nav_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            NavHost(
                navController = navController,
                startDestination = ROUTE_HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                composable(ROUTE_HOME) {
                    val nowMs = System.currentTimeMillis()
                    val dueSrsCount = remember(srsItems) {
                        srsItems.count { it.nextReviewAt <= nowMs }
                    }
                    HomeScreen(
                        profile = profile,
                        isOnline = isOnline,
                        dailyPlan = dailyPlan,
                        dailyPlanLoading = dailyPlanLoading,
                        pendingSyncCount = pendingSyncQueue.size,
                        dueSrsCount = dueSrsCount,
                        unfixedMistakesCount = unfixedMistakesCount,
                        adaptiveBanner = adaptiveBanner,
                        onDismissAdaptiveBanner = { viewModel.dismissAdaptiveBanner() },
                        onRefreshDailyPlan = { viewModel.refreshDailyPlan() },
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onStartPractice = { task ->
                            viewModel.logAnalytics("session_started", "task=${task.id}")
                            activePractice = task to profile.currentLevel
                        },
                        onOpenPath = {
                            learnSubDestination = LearnDestination.LEVEL_PATH
                            navigateTopTab(ROUTE_LEARN)
                        },
                        onOpenIelts = { navigateTopTab(ROUTE_IELTS) },
                        onOpenMistakes = {
                            learnSubDestination = LearnDestination.MISTAKE_NOTEBOOK
                            navigateTopTab(ROUTE_LEARN)
                        },
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                    )
                }

                composable(ROUTE_LEARN) {
                    when (learnSubDestination) {
                        LearnDestination.HUB -> LearnHubScreen(
                            profile = profile,
                            unfixedMistakesCount = unfixedMistakesCount,
                            savedWordsCount = learnedWords.size,
                            onNavigate = { learnSubDestination = it }
                        )

                        LearnDestination.ABSOLUTE_ZERO -> AbsoluteZeroScreen(
                            profile = profile,
                            speechEngine = viewModel.speechEngine,
                            onBack = { learnSubDestination = LearnDestination.HUB }
                        )

                        LearnDestination.MALAYALAM_TO_ENGLISH -> MalayalamToEnglishScreen(
                            profile = profile,
                            isOnline = isOnline,
                            speechEngine = viewModel.speechEngine,
                            translateResult = translateResult,
                            translateLoading = translateLoading,
                            journalCorrection = journalCorrection,
                            journalLoading = journalLoading,
                            savedJournals = journalEntries,
                            onTranslateMalayalam = { viewModel.translateMalayalamToEnglish(it) },
                            onPolishJournal = { viewModel.polishDailyJournal(it) },
                            onBack = { learnSubDestination = LearnDestination.HUB }
                        )

                        LearnDestination.ACCENT_COACH -> PronunciationCoachScreen(
                            profile = profile,
                            isOnline = isOnline,
                            speechEngine = viewModel.speechEngine,
                            audioRecorder = viewModel.audioRecorder,
                            analysisResult = pronunciationAnalysis,
                            analysisLoading = pronunciationLoading,
                            soundHistory = soundProgress,
                            onAnalyzeAudio = { bytes, sentence, target ->
                                viewModel.analyzeAccentAudio(bytes, sentence, target)
                            },
                            onBack = { learnSubDestination = LearnDestination.HUB }
                        )

                        LearnDestination.MISTAKE_NOTEBOOK -> MistakeNotebookScreen(
                            profile = profile,
                            mistakes = mistakes,
                            speechEngine = viewModel.speechEngine,
                            onPracticeMistakeResult = { id, score ->
                                viewModel.practiceMistakeResult(id, score)
                            },
                            onBack = { learnSubDestination = LearnDestination.HUB }
                        )

                        LearnDestination.CAMERA_LEARN -> CameraLearnScreen(
                            profile = profile,
                            isOnline = isOnline,
                            speechEngine = viewModel.speechEngine,
                            extractResult = cameraExtract,
                            loading = cameraLoading,
                            errorMessage = cameraError,
                            onAnalyzeBitmap = { viewModel.analyzeCameraBitmap(it) },
                            onSaveWordToSrs = { viewModel.saveCameraWordToSrs(it) },
                            onBack = { learnSubDestination = LearnDestination.HUB }
                        )

                        LearnDestination.SIMULATORS -> RealLifeSimulatorScreen(
                            profile = profile,
                            isOnline = isOnline,
                            speechEngine = viewModel.speechEngine,
                            turnLoading = simTurnLoading,
                            reportLoading = simReportLoading,
                            reportCard = simReport,
                            lastTurnResponse = simLastTurn,
                            onSendSceneTurn = { scenario, text, diff, surprise, history ->
                                viewModel.sendSimulatorTurn(scenario, text, diff, surprise, history)
                            },
                            onFinishSceneForReport = { scenario, dialogue ->
                                viewModel.finishSimulatorForReport(scenario, dialogue)
                            },
                            onSaveLearnedWords = { viewModel.saveSimulatorWords(it) },
                            onClearSceneState = { viewModel.clearSimulatorState() },
                            onBack = { learnSubDestination = LearnDestination.HUB }
                        )

                        LearnDestination.LEVEL_PATH -> LevelPathScreen(
                            profile = profile,
                            onSelectLevel = { level -> viewModel.selectActiveLevel(level) },
                            onPracticeLevelTask = { level, task ->
                                activePractice = task to level
                            },
                            onPreviewCelebration = { level ->
                                viewModel.triggerLevelCelebrationPreview(level)
                            }
                        )
                    }
                }

                composable(ROUTE_TALK) {
                    TalkWithLeoScreen(
                        profile = profile,
                        isOnline = isOnline,
                        messages = chatMessages,
                        selectedTopic = chatTopic,
                        correctMeEnabled = correctMeEnabled,
                        askMalayalamEnabled = askMalayalamEnabled,
                        isLoading = chatLoading,
                        errorMessage = chatError,
                        speechEngine = viewModel.speechEngine,
                        onSelectTopic = { viewModel.selectChatTopic(it) },
                        onToggleCorrectMe = { viewModel.toggleCorrectMe() },
                        onToggleAskMalayalam = { viewModel.toggleAskMalayalam() },
                        onSendMessage = { viewModel.sendUserChatMessage(it) },
                        onRequestMalayalamHelp = { viewModel.requestMalayalamHelpForMessage(it) }
                    )
                }

                composable(ROUTE_IELTS) {
                    IeltsPrepScreen(
                        profile = profile,
                        isOnline = isOnline,
                        daysUntilExam = viewModel.computeDaysUntilExam(),
                        speechEngine = viewModel.speechEngine,
                        studyPlan = ieltsPlan,
                        studyPlanLoading = ieltsPlanLoading,
                        speakingEval = ieltsSpeakingEval,
                        speakingEvalLoading = ieltsSpeakingLoading,
                        writingEval = ieltsWritingEval,
                        writingEvalLoading = ieltsWritingLoading,
                        attemptsHistory = ieltsAttempts,
                        onSaveIeltsSetup = { band, dateKey -> viewModel.saveIeltsSetup(band, dateKey) },
                        onGenerateStudyPlan = { cur, target, days -> viewModel.generateIeltsPlan(cur, target, days) },
                        onEvaluateSpeaking = { part, q, trans, dur -> viewModel.evaluateIeltsSpeaking(part, q, trans, dur) },
                        onEvaluateWriting = { task, prompt, essay, bmp -> viewModel.evaluateIeltsWriting(task, prompt, essay, bmp) },
                        onRecordSkillAttempt = { skill, band, weak, sum, mock ->
                            viewModel.recordIeltsSkillAttempt(skill, band, weak, sum, mock)
                        },
                        onSaveIeltsVocabToSrs = { viewModel.saveIeltsVocabToSrs(it) }
                    )
                }

                composable(ROUTE_PROGRESS) {
                    val badges = remember(profile, sessions.size, learnedWords.size) {
                        viewModel.computeBadges(profile, sessions.size, learnedWords.size)
                    }
                    ProgressScreen(
                        profile = profile,
                        sessions = sessions,
                        learnedWords = learnedWords,
                        srsItems = srsItems,
                        soundHistory = soundProgress,
                        ieltsAttempts = ieltsAttempts,
                        analyticsCount = analyticsEvents.size,
                        badges = badges,
                        weeklySummary = weeklySummary,
                        weeklySummaryLoading = weeklySummaryLoading,
                        onGenerateWeeklySummary = { viewModel.generateWeeklySummary() },
                        onActivateFreeze = { viewModel.activateStreakFreeze() },
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                    )
                }

                composable(ROUTE_SETTINGS) {
                    SettingsScreen(
                        profile = profile,
                        offlinePacksCount = offlinePacks.size,
                        offlineDrillsCount = offlineDrillsCount,
                        zeroPhrasesCount = zeroPhrasesCount,
                        ieltsStarterCount = ieltsStarterCount,
                        downloadProgress = downloadProgress,
                        onSetLanguage = { viewModel.setLanguage(it) },
                        onSetSupportLevel = { viewModel.setSupportLevel(it) },
                        onSetSourceLanguageCode = { viewModel.setSourceLanguageCode(it) },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onSetPersonality = { viewModel.setPersonality(it) },
                        onSetDailyGoal = { viewModel.setDailyGoal(it) },
                        onSetLearningGoal = { viewModel.setLearningGoal(it) },
                        onUpdateReminders = { times, enabled -> viewModel.updateReminders(times, enabled) },
                        onSendTestNotification = { viewModel.sendTestNotification() },
                        onDownloadOffline = { viewModel.downloadOfflinePacks() },
                        onSetSoundEffects = { viewModel.setSoundEffects(it) },
                        onRetakePlacement = { viewModel.retakePlacementTest() },
                        onExportProgress = {
                            val shareIntent = viewModel.buildExportProgressIntent()
                            context.startActivity(Intent.createChooser(shareIntent, "Export SpeakUp Progress"))
                        },
                        onResetProgress = { viewModel.resetAllProgress() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    levelUpEvent?.let { newLevel ->
        Box(modifier = Modifier.fillMaxSize()) {
            ConfettiOverlay(visible = true, modifier = Modifier.fillMaxSize())
            AlertDialog(
                onDismissRequest = { viewModel.dismissLevelUp() },
                shape = RoundedCornerShape(28.dp),
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LeoMascotAvatar(mood = LeoMood.CHEERING, size = 110.dp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = bi("Level Up!", "പുതിയ ലെവൽ!", lang),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = bi(newLevel.titleEn, newLevel.titleMl, lang),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bi(newLevel.taglineEn, newLevel.taglineMl, lang),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.dismissLevelUp() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(bi("Keep Speaking!", "തുടരാം!", lang))
                    }
                }
            )
        }
    }
}
