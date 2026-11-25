package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SpeechCoachEngine
import com.example.data.curriculum.ExtensionsCatalog
import com.example.data.local.IeltsAttemptEntity
import com.example.model.IeltsSpeakingEval
import com.example.model.IeltsStudyPlan
import com.example.model.IeltsVocabItem
import com.example.model.IeltsWritingEval
import com.example.model.LeoMood
import com.example.model.SpeechInputLocale
import com.example.model.UserProfileState
import com.example.ui.components.BigMicButton
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.LiveWaveformBars
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.bi
import com.example.ui.components.malayalamStyle
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.LeoOrangeAccent
import com.example.ui.theme.SoftYellowHighlight
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private enum class IeltsSectionTab(val labelEn: String, val labelMl: String) {
    PLAN("Setup & Plan", "പ്ലാൻ"),
    SPEAKING("Speaking", "സ്പീക്കിംഗ്"),
    WRITING("Writing", "റൈറ്റിംഗ്"),
    LISTENING("Listening", "ലിസണിംഗ്"),
    READING("Reading", "റീഡിംഗ്"),
    VOCAB_MOCK("Vocab & Mock", "പദസമ്പത്ത് / മോക്ക്")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IeltsPrepScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    daysUntilExam: Int,
    speechEngine: SpeechCoachEngine,
    studyPlan: IeltsStudyPlan?,
    studyPlanLoading: Boolean,
    speakingEval: IeltsSpeakingEval?,
    speakingEvalLoading: Boolean,
    writingEval: IeltsWritingEval?,
    writingEvalLoading: Boolean,
    attemptsHistory: List<IeltsAttemptEntity>,
    onSaveIeltsSetup: (targetBand: Double, examDateKey: String) -> Unit,
    onGenerateStudyPlan: (currentBand: Double, targetBand: Double, daysLeft: Int) -> Unit,
    onEvaluateSpeaking: (part: Int, question: String, transcript: String, durationSeconds: Int) -> Unit,
    onEvaluateWriting: (taskNumber: Int, prompt: String, essay: String, photo: Bitmap?) -> Unit,
    onRecordSkillAttempt: (skill: String, band: Double, weakestType: String, summary: String, isMock: Boolean) -> Unit,
    onSaveIeltsVocabToSrs: (IeltsVocabItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = profile.language
    var activeTab by remember { mutableStateOf(IeltsSectionTab.PLAN) }

    // Setup state
    var targetBand by remember(profile.ieltsTargetBand) { mutableDoubleStateOf(profile.ieltsTargetBand) }
    var examDaysInput by remember(daysUntilExam) { mutableIntStateOf(daysUntilExam.coerceIn(7, 180)) }

    // Diagnostic check
    var diagAnswer by remember { mutableStateOf("") }
    var diagDone by remember { mutableStateOf(false) }

    // Speaking state
    var speakingPart by remember { mutableIntStateOf(1) }
    var speakingPromptIdx by remember { mutableIntStateOf(0) }
    var speakingAnswer by remember { mutableStateOf("") }
    var part2PrepSeconds by remember { mutableIntStateOf(0) }
    var part2TalkSeconds by remember { mutableIntStateOf(0) }
    var speakingElapsed by remember { mutableIntStateOf(30) }

    // Writing state
    var writingTaskNum by remember { mutableIntStateOf(2) }
    var essayText by remember { mutableStateOf("") }
    var writingSecondsLeft by remember { mutableIntStateOf(2400) }
    var writingTimerRunning by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Listening & Reading answers
    val listeningSelections = remember { mutableStateMapOf<String, String>() }
    var listeningSubmitted by remember { mutableStateOf(false) }
    val readingSelections = remember { mutableStateMapOf<String, String>() }
    var readingSubmitted by remember { mutableStateOf(false) }
    var readingTimerSecs by remember { mutableIntStateOf(900) }
    var readingTimerRunning by remember { mutableStateOf(false) }
    var selectedVocabTopic by remember { mutableStateOf("All") }
    var mockModeActive by remember { mutableStateOf(false) }

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()
    val rmsLevel by speechEngine.rmsLevel.collectAsStateWithLifecycle()

    LaunchedEffect(part2PrepSeconds) {
        if (part2PrepSeconds > 0) {
            delay(1000L)
            part2PrepSeconds -= 1
            if (part2PrepSeconds == 0) {
                part2TalkSeconds = 120
            }
        }
    }

    LaunchedEffect(part2TalkSeconds) {
        if (part2TalkSeconds > 0) {
            delay(1000L)
            part2TalkSeconds -= 1
        }
    }

    LaunchedEffect(writingTimerRunning, writingSecondsLeft) {
        if (writingTimerRunning && writingSecondsLeft > 0) {
            delay(1000L)
            writingSecondsLeft -= 1
        }
    }

    LaunchedEffect(readingTimerRunning, readingTimerSecs) {
        if (readingTimerRunning && readingTimerSecs > 0) {
            delay(1000L)
            readingTimerSecs -= 1
        }
    }

    fun decodeScaledBitmap(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { s ->
            val raw = BitmapFactory.decodeStream(s) ?: return null
            val scale = minOf(1f, 1280f / maxOf(raw.width, raw.height))
            if (scale < 1f) Bitmap.createScaledBitmap(raw, (raw.width * scale).toInt(), (raw.height * scale).toInt(), true) else raw
        }
    }.getOrNull()

    fun createPhotoUri(): Uri {
        val dir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
        val file = File(dir, "ielts_essay_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val handwritingPrompts = ExtensionsCatalog.ieltsWritingPrompts[writingTaskNum].orEmpty()
    val currentWritingPrompt = handwritingPrompts.firstOrNull()?.second.orEmpty()

    val essayCameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = tempPhotoUri
        if (ok && uri != null) {
            decodeScaledBitmap(uri)?.let { bmp ->
                onEvaluateWriting(writingTaskNum, currentWritingPrompt, essayText, bmp)
            }
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createPhotoUri()
            tempPhotoUri = uri
            essayCameraLauncher.launch(uri)
        }
    }

    val essayGalleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            decodeScaledBitmap(uri)?.let { bmp ->
                onEvaluateWriting(writingTaskNum, currentWritingPrompt, essayText, bmp)
            }
        }
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechEngine.startListening(SpeechInputLocale.ENGLISH_IN) { spoken ->
                speakingAnswer = if (speakingAnswer.isBlank()) spoken else "$speakingAnswer $spoken"
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Countdown + Disclaimer banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = bi("IELTS Mastery Lab", "IELTS പരിശീലന കേന്ദ്രം", lang),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = bi(
                            "Target Band ${String.format(Locale.US, "%.1f", targetBand)} • Exam in $examDaysInput days",
                            "ലക്ഷ്യം: ബാൻഡ് ${String.format(Locale.US, "%.1f", targetBand)} • പരീക്ഷയ്ക്ക് $examDaysInput ദിവസം",
                            lang
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = DeepGreenPrimary
                    )
                }
                Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(10.dp)) {
                    Text(
                        text = "Est. ${String.format(Locale.US, "%.1f", profile.ieltsCurrentBand)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFF523600),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            AiEstimateDisclaimerBanner()
        }

        // Sub-navigation tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(IeltsSectionTab.entries) { tab ->
                    FilterChip(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        label = { Text(bi(tab.labelEn, tab.labelMl, lang)) },
                        modifier = Modifier.testTag("ielts_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }

        when (activeTab) {
            IeltsSectionTab.PLAN -> {
                item {
                    LeoSpeechGuide(
                        mood = LeoMood.HAPPY,
                        message = bi(
                            "Set your target band (5.5–8.0) and exam date. Take the quick 1-question spoken diagnostic to build your AI study plan!",
                            "നിങ്ങളുടെ ലക്ഷ്യ ബാൻഡും (5.5–8.0) പരീക്ഷാ തീയതിയും നൽകുക. AI പഠന പ്ലാൻ തയ്യാറാക്കാം!",
                            lang
                        )
                    )
                }

                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(bi("Target IELTS Band", "ലക്ഷ്യ ബാൻഡ്", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(5.5, 6.0, 6.5, 7.0, 7.5, 8.0).forEach { band ->
                                    FilterChip(
                                        selected = targetBand == band,
                                        onClick = { targetBand = band },
                                        label = { Text(String.format(Locale.US, "%.1f", band)) }
                                    )
                                }
                            }
                            Text(bi("Days until exam", "പരീക്ഷയ്ക്ക് ഇനി എത്ര ദിവസം", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(14, 30, 60, 90).forEach { d ->
                                    FilterChip(
                                        selected = examDaysInput == d,
                                        onClick = { examDaysInput = d },
                                        label = { Text("${d}d") }
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, examDaysInput) }
                                    val key = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
                                    onSaveIeltsSetup(targetBand, key)
                                    onGenerateStudyPlan(profile.ieltsCurrentBand, targetBand, examDaysInput)
                                },
                                enabled = !studyPlanLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (studyPlanLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(bi("Save Setup & Generate AI Study Plan", "സേവ് ചെയ്ത് AI പ്ലാൻ ഉണ്ടാക്കുക", lang))
                            }
                        }
                    }
                }

                // Quick Diagnostic Check
                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(bi("Quick Diagnostic Check", "ലെവൽ പരിശോധന", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(
                                text = "Describe in 2–3 sentences why you want to improve your English or take the IELTS test:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            OutlinedTextField(
                                value = diagAnswer,
                                onValueChange = { diagAnswer = it },
                                placeholder = { Text("Write or speak 2–3 sentences...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                            Button(
                                onClick = {
                                    val words = diagAnswer.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                                    val estBand = when {
                                        words >= 35 -> 6.5
                                        words >= 20 -> 6.0
                                        words >= 10 -> 5.5
                                        else -> 5.0
                                    }
                                    diagDone = true
                                    onRecordSkillAttempt("diagnostic", estBand, "Diagnostic Check", "Words: $words", false)
                                },
                                enabled = diagAnswer.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = LeoOrangeAccent)
                            ) {
                                Text(bi("Check Estimated Band", "ബാൻഡ് പരിശോധിക്കുക", lang))
                            }
                            if (diagDone) {
                                Text(
                                    text = "Updated estimated band: ${String.format(Locale.US, "%.1f", profile.ieltsCurrentBand)} • AI estimated, not an official score.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepGreenPrimary
                                )
                            }
                        }
                    }
                }

                if (studyPlan != null) {
                    item {
                        SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = DeepGreenPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(bi("Your Personalized IELTS Plan", "നിങ്ങളുടെ IELTS പഠന പ്ലാൻ", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Text(studyPlan.malayalamMotivation, style = malayalamStyle(MaterialTheme.typography.bodyMedium), color = DeepGreenPrimary)
                                Text(bi("Weekly Focus", "ആഴ്ചയിലെ ലക്ഷ്യങ്ങൾ", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                studyPlan.weeklyFocus.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                                Text(bi("Daily Checklist", "ദിവസേന ചെയ്യേണ്ടവ", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                studyPlan.dailyChecklist.forEach { Text("✓ $it", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }

            IeltsSectionTab.SPEAKING -> {
                val partQuestions = ExtensionsCatalog.ieltsSpeakingParts[speakingPart].orEmpty()
                val activePrompt = partQuestions.getOrElse(speakingPromptIdx % partQuestions.size.coerceAtLeast(1)) { "" }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1 to "Part 1 Interview", 2 to "Part 2 Cue Card", 3 to "Part 3 Discussion").forEach { (part, label) ->
                            FilterChip(
                                selected = speakingPart == part,
                                onClick = {
                                    speakingPart = part
                                    speakingPromptIdx = 0
                                    speakingElapsed = if (part == 2) 120 else 35
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Examiner Question", style = MaterialTheme.typography.labelLarge, color = DeepGreenPrimary)
                                OutlinedButton(
                                    onClick = {
                                        speechEngine.speakWithVoice(activePrompt, pitch = 0.94f, rate = 0.95f, localeTag = "en-GB")
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Examiner Voice")
                                }
                            }
                            Text(activePrompt, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))

                            if (speakingPart == 2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { part2PrepSeconds = 60; part2TalkSeconds = 0 }) {
                                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (part2PrepSeconds > 0) "Prep: ${part2PrepSeconds}s" else "Start 1-Min Prep")
                                    }
                                    OutlinedButton(onClick = { part2PrepSeconds = 0; part2TalkSeconds = 120 }) {
                                        Text(if (part2TalkSeconds > 0) "Talk: ${part2TalkSeconds}s" else "Start 2-Min Talk")
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { speakingPromptIdx += 1 }) {
                                    Text(bi("Next Question", "അടുത്ത ചോദ്യം", lang))
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isListening) LiveWaveformBars(isListening = true, rmsLevel = rmsLevel)
                        BigMicButton(
                            isListening = isListening,
                            testTag = "ielts_speaking_mic",
                            onClick = {
                                if (isListening) speechEngine.stopListening()
                                else {
                                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        speechEngine.startListening(SpeechInputLocale.ENGLISH_IN) { spoken ->
                                            speakingAnswer = if (speakingAnswer.isBlank()) spoken else "$speakingAnswer $spoken"
                                        }
                                    } else {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )
                        OutlinedTextField(
                            value = speakingAnswer,
                            onValueChange = { speakingAnswer = it },
                            label = { Text(bi("Your spoken / edited answer", "നിങ്ങളുടെ ഉത്തരം", lang)) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Button(
                            onClick = { onEvaluateSpeaking(speakingPart, activePrompt, speakingAnswer, speakingElapsed) },
                            enabled = speakingAnswer.isNotBlank() && !speakingEvalLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (speakingEvalLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            else Text(bi("Score IELTS Speaking", "ബാൻഡ് സ്കോർ പരിശോധിക്കുക", lang))
                        }
                    }
                }

                if (speakingEval != null) {
                    item {
                        SpeakUpCard(containerColor = DeepGreenContainerLight, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                BandScoreHeader(
                                    title = "Estimated IELTS Speaking Band",
                                    band = speakingEval.bandScore
                                )
                                Text(
                                    text = "Fluency: ${speakingEval.breakdown.fluencyOrTask} • Lexical: ${speakingEval.breakdown.lexical} • Grammar: ${speakingEval.breakdown.grammar} • Pronunciation: ${speakingEval.breakdown.pronunciationOrCoherence}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "Pace: ${speakingEval.wordsPerMinute} WPM • Filler words: ${speakingEval.fillerWordCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepGreenPrimary
                                )
                                Text(speakingEval.feedbackSummary, style = MaterialTheme.typography.bodyMedium)
                                Surface(color = Color.White, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Band 8 Improved Model Answer", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DeepGreenPrimary)
                                        Text(speakingEval.modelImprovedAnswer, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            IeltsSectionTab.WRITING -> {
                val wordCount = essayText.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1 to "Task 1", 2 to "Task 2").forEach { (num, label) ->
                                FilterChip(
                                    selected = writingTaskNum == num,
                                    onClick = {
                                        writingTaskNum = num
                                        writingSecondsLeft = if (num == 1) 1200 else 2400
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                        OutlinedButton(onClick = { writingTimerRunning = !writingTimerRunning }) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${writingSecondsLeft / 60}:${String.format(Locale.US, "%02d", writingSecondsLeft % 60)}")
                        }
                    }
                }

                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = handwritingPrompts.firstOrNull()?.first ?: "Writing Task",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = DeepGreenPrimary
                            )
                            Text(currentWritingPrompt, style = MaterialTheme.typography.bodyMedium)
                            OutlinedTextField(
                                value = essayText,
                                onValueChange = { essayText = it },
                                label = { Text("Type essay ($wordCount words)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                        if (hasCam) {
                                            val uri = createPhotoUri()
                                            tempPhotoUri = uri
                                            essayCameraLauncher.launch(uri)
                                        } else {
                                            cameraPermLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Photo Paper", maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        essayGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Upload Image", maxLines = 1)
                                }
                            }
                            Button(
                                onClick = { onEvaluateWriting(writingTaskNum, currentWritingPrompt, essayText, null) },
                                enabled = essayText.isNotBlank() && !writingEvalLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (writingEvalLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                else Text(bi("Evaluate Writing Task", "എഴുത്ത് വിലയിരുത്തുക", lang))
                            }
                        }
                    }
                }

                if (writingEval != null) {
                    item {
                        SpeakUpCard(containerColor = DeepGreenContainerLight, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                BandScoreHeader(title = "Estimated IELTS Writing Band", band = writingEval.bandScore)
                                Text(
                                    text = "Task: ${writingEval.breakdown.fluencyOrTask} • Lexical: ${writingEval.breakdown.lexical} • Grammar: ${writingEval.breakdown.grammar} • Coherence: ${writingEval.breakdown.pronunciationOrCoherence}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text("Key Corrections", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                writingEval.corrections.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                                Surface(color = Color.White, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Band 8 Rewritten Paragraph", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DeepGreenPrimary)
                                        Text(writingEval.rewrittenParagraph, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    writingEval.usefulVocabulary.forEach { v ->
                                        Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(8.dp)) {
                                            Text(v, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            IeltsSectionTab.LISTENING -> {
                item {
                    LeoSpeechGuide(
                        mood = LeoMood.WAITING,
                        message = bi(
                            "4 Listening sections work completely offline! Listen carefully and answer the questions below.",
                            "ഈ 4 ലിസണിംഗ് ഭാഗങ്ങളും ഇന്റർനെറ്റ് ഇല്ലാതെയും പ്രവർത്തിക്കും!",
                            lang
                        )
                    )
                }

                items(ExtensionsCatalog.ieltsListeningSections, key = { it.sectionNumber }) { sec ->
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(sec.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(sec.contextLabel, style = MaterialTheme.typography.labelSmall, color = DeepGreenPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { speechEngine.speak(sec.audioScript, slow = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Audio")
                                }
                                OutlinedButton(onClick = { speechEngine.speak(sec.audioScript, slow = true) }) {
                                    Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("0.7x")
                                }
                                OutlinedButton(onClick = { speechEngine.stopSpeaking() }) {
                                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }

                            sec.questions.forEach { q ->
                                Text("${q.questionType}: ${q.prompt}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    q.options.forEach { opt ->
                                        FilterChip(
                                            selected = listeningSelections[q.id] == opt,
                                            onClick = { listeningSelections[q.id] = opt },
                                            label = { Text(opt) }
                                        )
                                    }
                                }
                                if (listeningSubmitted) {
                                    val isRight = listeningSelections[q.id] == q.correctAnswer
                                    Text(
                                        text = (if (isRight) "✅ Correct! " else "❌ Answer: ${q.correctAnswer}. ") +
                                            bi(q.explanationEn, q.explanationMl, lang),
                                        style = malayalamStyle(MaterialTheme.typography.bodySmall),
                                        color = if (isRight) DeepGreenPrimary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    val totalQ = ExtensionsCatalog.ieltsListeningSections.sumOf { it.questions.size }.coerceAtLeast(1)
                    val correctQ = ExtensionsCatalog.ieltsListeningSections.flatMap { it.questions }
                        .count { listeningSelections[it.id] == it.correctAnswer }
                    val estBand = (4.5 + (correctQ.toDouble() / totalQ) * 4.0)

                    Button(
                        onClick = {
                            listeningSubmitted = true
                            val wrongType = ExtensionsCatalog.ieltsListeningSections.flatMap { it.questions }
                                .firstOrNull { listeningSelections[it.id] != it.correctAnswer }?.questionType ?: "None"
                            onRecordSkillAttempt("listening", estBand, wrongType, "Score $correctQ/$totalQ", mockModeActive)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(bi("Check Listening Answers ($correctQ/$totalQ)", "ഉത്തരങ്ങൾ പരിശോധിക്കുക", lang))
                    }
                    if (listeningSubmitted) {
                        Spacer(modifier = Modifier.height(6.dp))
                        BandScoreHeader(title = "Estimated Listening Band ($correctQ/$totalQ)", band = estBand)
                    }
                }
            }

            IeltsSectionTab.READING -> {
                val passage = ExtensionsCatalog.ieltsReadingPassages.first()
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(passage.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { readingTimerRunning = !readingTimerRunning }) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${readingTimerSecs / 60}:${String.format(Locale.US, "%02d", readingTimerSecs % 60)}")
                        }
                    }
                }

                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(passage.body, style = MaterialTheme.typography.bodyMedium)
                            Text(bi("Tap highlighted words for Malayalam meaning:", "വാക്കുകളിൽ തൊട്ട് മലയാളം അർത്ഥം കാണൂ:", lang), style = MaterialTheme.typography.labelLarge, color = DeepGreenPrimary)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                passage.highlightedVocab.forEach { (word, meaningMl) ->
                                    Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(8.dp)) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("$word = $meaningMl", style = malayalamStyle(MaterialTheme.typography.labelSmall))
                                            IconButton(onClick = { speechEngine.speak(word) }, modifier = Modifier.size(22.dp)) {
                                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                items(passage.questions, key = { it.id }) { q ->
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${q.questionType}: ${q.prompt}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                q.options.forEach { opt ->
                                    FilterChip(
                                        selected = readingSelections[q.id] == opt,
                                        onClick = { readingSelections[q.id] = opt },
                                        label = { Text(opt) }
                                    )
                                }
                            }
                            if (readingSubmitted) {
                                val ok = readingSelections[q.id] == q.correctAnswer
                                Text(
                                    text = (if (ok) "✅ Correct! " else "❌ Correct: ${q.correctAnswer}. ") + bi(q.explanationEn, q.explanationMl, lang),
                                    style = malayalamStyle(MaterialTheme.typography.bodySmall),
                                    color = if (ok) DeepGreenPrimary else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                item {
                    val totalR = passage.questions.size.coerceAtLeast(1)
                    val correctR = passage.questions.count { readingSelections[it.id] == it.correctAnswer }
                    val estBandR = 4.5 + (correctR.toDouble() / totalR) * 4.0
                    Button(
                        onClick = {
                            readingSubmitted = true
                            val wrongType = passage.questions.firstOrNull { readingSelections[it.id] != it.correctAnswer }?.questionType ?: "None"
                            onRecordSkillAttempt("reading", estBandR, wrongType, "Score $correctR/$totalR", mockModeActive)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(bi("Submit Reading ($correctR/$totalR)", "റീഡിംഗ് പരിശോധിക്കുക", lang))
                    }
                    if (readingSubmitted) {
                        Spacer(modifier = Modifier.height(6.dp))
                        BandScoreHeader(title = "Estimated Reading Band ($correctR/$totalR)", band = estBandR)
                    }
                }
            }

            IeltsSectionTab.VOCAB_MOCK -> {
                // Band Trend + Weakest Question Types + Mock Mode + Topic Vocab
                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(bi("Band Progress Trend", "ബാൻഡ് പുരോഗതി", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                OutlinedButton(onClick = { mockModeActive = !mockModeActive }) {
                                    Text(if (mockModeActive) "Mock Mode: ON" else "Start Mock Mode")
                                }
                            }
                            val trendBands = attemptsHistory.take(8).reversed().map { it.bandScore }.ifEmpty { listOf(profile.ieltsCurrentBand) }
                            BandTrendSparkline(bands = trendBands)
                            val weakest = attemptsHistory.firstOrNull { it.weakestQuestionType != "None" }?.weakestQuestionType ?: "Keep practicing all types!"
                            Text(
                                text = "Weakest Question Focus: $weakest • AI estimated, not an official score",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    val topics = listOf("All", "AWL", "Environment", "Education", "Technology", "Health")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(topics) { topic ->
                            FilterChip(
                                selected = selectedVocabTopic == topic,
                                onClick = { selectedVocabTopic = topic },
                                label = { Text(topic) }
                            )
                        }
                    }
                }

                val filteredVocab = ExtensionsCatalog.ieltsVocabulary.filter {
                    selectedVocabTopic == "All" || it.topic.equals(selectedVocabTopic, ignoreCase = true)
                }
                items(filteredVocab, key = { it.word }) { item ->
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.word, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(6.dp)) {
                                        Text(item.topic, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text(item.malayalamMeaning, style = malayalamStyle(MaterialTheme.typography.bodySmall), color = DeepGreenPrimary)
                                Text(item.academicExample, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { speechEngine.speak(item.academicExample) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen")
                            }
                            IconButton(onClick = { onSaveIeltsVocabToSrs(item) }) {
                                Icon(Icons.Filled.BookmarkAdded, contentDescription = "Save to SM-2", tint = DeepGreenPrimary)
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun AiEstimateDisclaimerBanner() {
    Surface(
        color = SoftYellowHighlight,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Color(0xFF7A4B00), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AI estimated, not an official score",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF523600)
            )
        }
    }
}

@Composable
private fun BandScoreHeader(title: String, band: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Surface(color = DeepGreenPrimary, shape = RoundedCornerShape(10.dp)) {
                Text(
                    text = "Band ${String.format(Locale.US, "%.1f", band)}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        Text(
            text = "AI estimated, not an official score",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF6B4E00)
        )
    }
}

@Composable
private fun BandTrendSparkline(bands: List<Double>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        val path = Path()
        val minBand = 4.0f
        val maxBand = 9.0f
        bands.forEachIndexed { idx, b ->
            val x = if (bands.size <= 1) size.width / 2f else (idx.toFloat() / (bands.size - 1)) * (size.width - 24f) + 12f
            val ratio = ((b.toFloat() - minBand) / (maxBand - minBand)).coerceIn(0f, 1f)
            val y = size.height - 14f - ratio * (size.height - 28f)
            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color = LeoOrangeAccent, radius = 6.dp.toPx(), center = Offset(x, y))
        }
        if (bands.size > 1) {
            drawPath(
                path = path,
                color = DeepGreenPrimary,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
