package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SpeakingEvaluator
import com.example.audio.SpeechCoachEngine
import com.example.data.curriculum.CurriculumRepository
import com.example.model.AppLanguage
import com.example.model.LearningGoal
import com.example.model.LeoMood
import com.example.notifications.SpeakUpNotificationHelper
import com.example.ui.components.BigMicButton
import com.example.ui.components.LeoMascotAvatar
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.LiveWaveformBars
import com.example.ui.components.bi
import com.example.ui.theme.EmeraldSuccess
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    language: AppLanguage,
    speechEngine: SpeechCoachEngine,
    onToggleLanguage: () -> Unit,
    onFinishOnboarding: (
        goal: LearningGoal,
        dailyMinutes: Int,
        reminders: List<String>,
        notificationsEnabled: Boolean,
        placementAnswers: Map<Int, String>
    ) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var selectedGoal by remember { mutableStateOf(LearningGoal.DAILY_LIFE) }
    var selectedMinutes by remember { mutableIntStateOf(10) }
    val reminderTimes = remember { mutableStateListOf("08:30", "20:00") }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var notificationPermissionGranted by remember {
        mutableStateOf(SpeakUpNotificationHelper.hasNotificationPermission(context))
    }

    var placementIndex by remember { mutableIntStateOf(0) }
    val placementAnswers = remember { mutableStateMapOf<Int, String>() }
    var manualReplyInput by remember { mutableStateOf("") }
    var showPlacementResult by remember { mutableStateOf(false) }

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()
    val rmsLevel by speechEngine.rmsLevel.collectAsStateWithLifecycle()
    val transcript by speechEngine.transcript.collectAsStateWithLifecycle()
    val speechError by speechEngine.speechError.collectAsStateWithLifecycle()

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
        notificationsEnabled = granted
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val q = CurriculumRepository.placementQuestions[placementIndex]
            speechEngine.startListening { spoken ->
                placementAnswers[q.id] = spoken
                manualReplyInput = spoken
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .widthIn(max = 600.dp)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar: back + progress + EN/ML toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 0 && !showPlacementResult) {
                    IconButton(onClick = { step -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                LinearProgressIndicator(
                    progress = { (step + 1) / 5f },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .height(8.dp),
                )

                FilterChip(
                    selected = language == AppLanguage.MALAYALAM,
                    onClick = onToggleLanguage,
                    label = { Text(if (language == AppLanguage.MALAYALAM) "മലയാളം" else "EN / ML") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = "Switch language",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier.testTag("onboarding_lang_toggle")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(targetState = Pair(step, showPlacementResult), label = "onboarding_step") { (currentStep, resultShown) ->
                    when {
                        resultShown -> {
                            val assigned = remember(placementAnswers.toMap()) {
                                SpeakingEvaluator.calculatePlacementLevel(
                                    placementAnswers.toMap(),
                                    CurriculumRepository.placementQuestions
                                )
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                                LeoMascotAvatar(mood = LeoMood.CHEERING, size = 136.dp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = bi("Your Starting Level!", "നിങ്ങളുടെ തുടക്ക നില!", language),
                                    style = MaterialTheme.typography.headlineMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    shape = RoundedCornerShape(24.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(22.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = assigned.titleEn,
                                            style = MaterialTheme.typography.displaySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = assigned.titleMl,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = bi(assigned.taglineEn, assigned.taglineMl, language),
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = bi(
                                        "No grammar books! Just ${selectedMinutes} minutes of real speaking every day.",
                                        "വ്യാകരണ പുസ്തകങ്ങളില്ല! ദിവസവും ${selectedMinutes} മിനിറ്റ് സംസാരിച്ച് പഠിക്കാം.",
                                        language
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        currentStep == 0 -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(14.dp))
                                LeoMascotAvatar(mood = LeoMood.HAPPY, size = 148.dp)
                                Spacer(modifier = Modifier.height(18.dp))
                                Text(
                                    text = bi(
                                        "Hi! I'm Leo, your English Speaking Coach",
                                        "ഹായ്! ഞാൻ ലിയോ, നിങ്ങളുടെ ഇംഗ്ലീഷ് സ്പീക്കിംഗ് കോച്ച്",
                                        language
                                    ),
                                    style = MaterialTheme.typography.displaySmall,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = bi(
                                        "Zero grammar rules. You learn only by listening, speaking out loud, and practicing real-life conversations.",
                                        "വ്യാകരണ നിയമങ്ങളില്ല! കേട്ടും, ഉറക്കെ സംസാരിച്ചും, യഥാർത്ഥ സന്ദർഭങ്ങളിലൂടെയും ഇംഗ്ലീഷ് പഠിക്കാം.",
                                        language
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = bi("Under 2-minute setup:", "2 മിനിറ്റിൽ തുടങ്ങാം:", language),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = bi(
                                                "• Pick your speaking goal & daily minutes\n• Set friendly reminder times\n• Answer 5 quick spoken questions with Leo",
                                                "• ലക്ഷ്യവും ദിവസേനയുള്ള സമയവും തിരഞ്ഞെടുക്കുക\n• ഓർമ്മപ്പെടുത്തൽ സമയം വെക്കുക\n• ലിയോയോട് 5 ചെറിയ ചോദ്യങ്ങൾക്ക് സംസാരിച്ച് ഉത്തരം പറയുക",
                                                language
                                            ),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        currentStep == 1 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LeoSpeechGuide(
                                    mood = LeoMood.HAPPY,
                                    message = bi(
                                        "Why do you want to speak better English?",
                                        "നിങ്ങൾ എന്തിനാണ് ഇംഗ്ലീഷ് സംസാരം മെച്ചപ്പെടുത്താൻ ആഗ്രഹിക്കുന്നത്?",
                                        language
                                    )
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                LearningGoal.entries.forEach { goal ->
                                    val selected = selectedGoal == goal
                                    GoalOptionCard(
                                        title = bi(goal.titleEn, goal.titleMl, language),
                                        subtitle = bi(goal.subtitleEn, goal.subtitleMl, language),
                                        icon = goalIcon(goal),
                                        selected = selected,
                                        onClick = { selectedGoal = goal }
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                            }
                        }

                        currentStep == 2 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LeoSpeechGuide(
                                    mood = LeoMood.THINKING,
                                    message = bi(
                                        "How many minutes can you speak each day?",
                                        "ദിവസവും എത്ര മിനിറ്റ് സംസാരിക്കാൻ കഴിയും?",
                                        language
                                    )
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                val options = listOf(
                                    5 to bi("Quick Warm-up • 5 min/day", "ചെറിയ തുടക്കം • 5 മിനിറ്റ്", language),
                                    10 to bi("Recommended • 10 min/day", "ശുപാർശ ചെയ്തത് • 10 മിനിറ്റ്", language),
                                    15 to bi("Steady Growth • 15 min/day", "വേഗത്തിലുള്ള വളർച്ച • 15 മിനിറ്റ്", language),
                                    20 to bi("Fast Fluency • 20 min/day", "അതിവേഗ പരിശീലനം • 20 മിനിറ്റ്", language)
                                )
                                options.forEach { (mins, label) ->
                                    val selected = selectedMinutes == mins
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp)
                                            .clickable { selectedMinutes = mins },
                                        shape = RoundedCornerShape(18.dp),
                                        border = BorderStroke(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        ),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(18.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Filled.Schedule,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(label, style = MaterialTheme.typography.titleMedium)
                                            }
                                            if (selected) {
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        currentStep == 3 -> {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LeoSpeechGuide(
                                    mood = LeoMood.WAITING,
                                    message = bi(
                                        "Can I nudge you up to 3 times a day so you never break your streak?",
                                        "നിങ്ങളുടെ തുടർച്ച നഷ്ടപ്പെടാതിരിക്കാൻ ദിവസം 3 തവണ വരെ ഞാൻ ഓർമ്മിപ്പിക്കട്ടെ?",
                                        language
                                    ),
                                    subMessage = bi(
                                        "Leo only sends gentle, encouraging practice nudges.",
                                        "സ്നേഹത്തോടെയുള്ള ചെറിയ ഓർമ്മപ്പെടുത്തലുകൾ മാത്രമേ അയക്കൂ.",
                                        language
                                    )
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Filled.NotificationsActive,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = bi("Daily Reminders", "ദിവസേനയുള്ള ഓർമ്മപ്പെടുത്തൽ", language),
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                            Switch(
                                                checked = notificationsEnabled,
                                                onCheckedChange = { wantEnable ->
                                                    if (wantEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                                                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    } else {
                                                        notificationsEnabled = wantEnable
                                                    }
                                                }
                                            )
                                        }

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    bi(
                                                        "Allow Notification Permission",
                                                        "നോട്ടിഫിക്കേഷൻ അനുമതി നൽകുക",
                                                        language
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = bi(
                                                "Reminder Times (${reminderTimes.size}/3)",
                                                "ഓർമ്മപ്പെടുത്തൽ സമയം (${reminderTimes.size}/3)",
                                                language
                                            ),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            reminderTimes.forEachIndexed { idx, time ->
                                                FilterChip(
                                                    selected = true,
                                                    onClick = { reminderTimes.removeAt(idx) },
                                                    label = { Text(time) },
                                                    trailingIcon = {
                                                        Icon(
                                                            Icons.Filled.Close,
                                                            contentDescription = "Remove reminder",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                )
                                            }
                                            if (reminderTimes.size < 3) {
                                                OutlinedButton(
                                                    onClick = {
                                                        TimePickerDialog(
                                                            context,
                                                            { _, hour, minute ->
                                                                val formatted = String.format(Locale.US, "%02d:%02d", hour, minute)
                                                                if (!reminderTimes.contains(formatted)) {
                                                                    reminderTimes.add(formatted)
                                                                }
                                                            },
                                                            18,
                                                            0,
                                                            true
                                                        ).show()
                                                    }
                                                ) {
                                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(bi("Add Time", "സമയം ചേർക്കുക", language))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        currentStep == 4 -> {
                            val questions = CurriculumRepository.placementQuestions
                            val q = questions[placementIndex]
                            val currentAnswer = placementAnswers[q.id].orEmpty()

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = bi(
                                        "Spoken Check • Question ${placementIndex + 1} of ${questions.size}",
                                        "സംസാര പരിശോധന • ചോദ്യം ${placementIndex + 1} / ${questions.size}",
                                        language
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LeoSpeechGuide(
                                    mood = if (isListening) LeoMood.WAITING else LeoMood.HAPPY,
                                    message = q.leoQuestion,
                                    subMessage = q.malayalamHint
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { speechEngine.speak(q.leoQuestion) }
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(bi("Hear Leo's Question", "ചോദ്യം കേൾക്കാം", language))
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                LiveWaveformBars(isListening = isListening, rmsLevel = rmsLevel)
                                Spacer(modifier = Modifier.height(10.dp))

                                BigMicButton(
                                    isListening = isListening,
                                    testTag = "placement_mic_button",
                                    onClick = {
                                        if (isListening) {
                                            speechEngine.stopListening()
                                        } else {
                                            val hasMic = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasMic) {
                                                speechEngine.startListening { spoken ->
                                                    placementAnswers[q.id] = spoken
                                                    manualReplyInput = spoken
                                                }
                                            } else {
                                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    }
                                )

                                val displayTranscript = when {
                                    transcript.isNotBlank() -> transcript
                                    currentAnswer.isNotBlank() -> currentAnswer
                                    else -> ""
                                }
                                if (displayTranscript.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "\"$displayTranscript\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (!speechError.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = speechError.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = manualReplyInput,
                                    onValueChange = {
                                        manualReplyInput = it
                                        placementAnswers[q.id] = it
                                    },
                                    label = {
                                        Text(bi("Or type your answer if mic is quiet", "മൈക്ക് ഉപയോഗിക്കാൻ കഴിയില്ലെങ്കിൽ ടൈപ്പ് ചെയ്യാം", language))
                                    },
                                    placeholder = { Text(q.sampleResponse) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = false,
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = {
                                        if (placementIndex < questions.lastIndex) {
                                            placementIndex += 1
                                            manualReplyInput = placementAnswers[questions[placementIndex].id].orEmpty()
                                            speechEngine.clearTranscript()
                                        } else {
                                            showPlacementResult = true
                                        }
                                    }
                                ) {
                                    Text(bi("Skip this question", "ഈ ചോദ്യം ഒഴിവാക്കുക", language))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary bottom CTA
            Button(
                onClick = {
                    when {
                        showPlacementResult -> {
                            onFinishOnboarding(
                                selectedGoal,
                                selectedMinutes,
                                reminderTimes.toList(),
                                notificationsEnabled,
                                placementAnswers.toMap()
                            )
                        }
                        step < 4 -> {
                            step += 1
                            if (step == 4) {
                                speechEngine.speak(CurriculumRepository.placementQuestions[0].leoQuestion)
                            }
                        }
                        else -> {
                            val q = CurriculumRepository.placementQuestions[placementIndex]
                            if (manualReplyInput.isNotBlank() && placementAnswers[q.id].isNullOrBlank()) {
                                placementAnswers[q.id] = manualReplyInput
                            }
                            if (placementIndex < CurriculumRepository.placementQuestions.lastIndex) {
                                placementIndex += 1
                                val nextQ = CurriculumRepository.placementQuestions[placementIndex]
                                manualReplyInput = placementAnswers[nextQ.id].orEmpty()
                                speechEngine.clearTranscript()
                                speechEngine.speak(nextQ.leoQuestion)
                            } else {
                                showPlacementResult = true
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_next_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                val ctaLabel = when {
                    showPlacementResult -> bi("Start My SpeakUp Journey!", "എന്റെ പരിശീലനം തുടങ്ങാം!", language)
                    step == 0 -> bi("Let's Start!", "തുടങ്ങാം!", language)
                    step == 4 && placementIndex < CurriculumRepository.placementQuestions.lastIndex ->
                        bi("Next Question", "അടുത്ത ചോദ്യം", language)
                    step == 4 -> bi("See My Level", "എന്റെ ലെവൽ കാണുക", language)
                    else -> bi("Continue", "തുടരുക", language)
                }
                Text(ctaLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun GoalOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
            }
        }
    }
}

private fun goalIcon(goal: LearningGoal): ImageVector = when (goal) {
    LearningGoal.DAILY_LIFE -> Icons.Filled.ShoppingBag
    LearningGoal.JOB_INTERVIEW -> Icons.Filled.Work
    LearningGoal.TRAVEL -> Icons.Filled.Flight
    LearningGoal.CONFIDENCE -> Icons.Filled.RecordVoiceOver
}
