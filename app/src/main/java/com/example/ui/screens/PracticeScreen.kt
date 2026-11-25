package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SpeechCoachEngine
import com.example.data.curriculum.CurriculumRepository
import com.example.model.ChallengeKind
import com.example.model.DailyTaskType
import com.example.model.EnglishLevel
import com.example.model.LeoMood
import com.example.model.SpeakingEvaluation
import com.example.model.UserProfileState
import com.example.model.WordDrill
import com.example.ui.components.BigMicButton
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.LeoMascotAvatar
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.LiveWaveformBars
import com.example.ui.components.StarScoreRow
import com.example.ui.components.bi
import com.example.ui.theme.EmeraldContainerLight
import com.example.ui.theme.EmeraldSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeScreen(
    taskType: DailyTaskType,
    level: EnglishLevel,
    profile: UserProfileState,
    speechEngine: SpeechCoachEngine,
    onEvaluateSpeaking: suspend (
        spokenText: String,
        targetSentence: String,
        expectedKeywords: List<String>,
        minWords: Int,
        isExactMatchDrill: Boolean,
        itemId: String,
        malayalamHint: String
    ) -> SpeakingEvaluation,
    onWordSpoken: (WordDrill, Int) -> Unit,
    onTaskCompleted: (stars: Int, scorePercent: Int, isOfflineScore: Boolean) -> Unit,
    onDropOff: (stepIndex: Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lang = profile.language
    val curriculum = remember(level) { CurriculumRepository.getCurriculum(level) }

    val totalSteps = when (taskType) {
        DailyTaskType.SHADOW_SPEAKING -> curriculum.shadowDrills.size
        DailyTaskType.WORD_OF_THE_DAY -> curriculum.wordDrills.size
        DailyTaskType.SITUATION_ROLEPLAY -> curriculum.roleplayScene.turns.size
        DailyTaskType.LISTEN_AND_ANSWER -> curriculum.listenAnswerDrills.size
        DailyTaskType.CHALLENGE -> 1
    }

    var stepIndex by remember { mutableIntStateOf(0) }
    var evaluation by remember { mutableStateOf<SpeakingEvaluation?>(null) }
    var evaluatingAi by remember { mutableStateOf(false) }
    var showMalayalamHint by remember { mutableStateOf(true) }
    var showTextFallback by remember { mutableStateOf(false) }
    var manualText by remember { mutableStateOf("") }
    var accumulatedStars by remember { mutableIntStateOf(0) }
    var accumulatedScore by remember { mutableIntStateOf(0) }
    var anyOfflineScore by remember { mutableStateOf(false) }
    var taskDone by remember { mutableStateOf(false) }
    var topicTalkSecondsLeft by remember { mutableIntStateOf(30) }
    var timerRunning by remember { mutableStateOf(false) }

    fun handleExit() {
        speechEngine.stopListening()
        speechEngine.stopSpeaking()
        if (!taskDone) {
            onDropOff(stepIndex)
        }
        onBack()
    }

    BackHandler { handleExit() }

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()
    val rmsLevel by speechEngine.rmsLevel.collectAsStateWithLifecycle()
    val transcript by speechEngine.transcript.collectAsStateWithLifecycle()
    val speechError by speechEngine.speechError.collectAsStateWithLifecycle()

    LaunchedEffect(timerRunning) {
        while (timerRunning && topicTalkSecondsLeft > 0) {
            delay(1000L)
            topicTalkSecondsLeft -= 1
        }
        if (topicTalkSecondsLeft == 0) {
            timerRunning = false
            speechEngine.stopListening()
        }
    }

    fun evaluateResponse(spokenText: String) {
        scope.launch {
            evaluatingAi = true
            val eval = when (taskType) {
                DailyTaskType.SHADOW_SPEAKING -> {
                    val drill = curriculum.shadowDrills[stepIndex]
                    onEvaluateSpeaking(
                        spokenText,
                        drill.sentence,
                        emptyList(),
                        3,
                        true,
                        drill.id,
                        drill.malayalamHint
                    )
                }
                DailyTaskType.WORD_OF_THE_DAY -> {
                    val wordDrill = curriculum.wordDrills[stepIndex]
                    val res = onEvaluateSpeaking(
                        spokenText,
                        wordDrill.sampleSentence,
                        listOf(wordDrill.word),
                        4,
                        false,
                        "word_${wordDrill.word.lowercase()}",
                        wordDrill.malayalamMeaning
                    )
                    onWordSpoken(wordDrill, res.scorePercent)
                    res
                }
                DailyTaskType.SITUATION_ROLEPLAY -> {
                    val turn = curriculum.roleplayScene.turns[stepIndex]
                    onEvaluateSpeaking(
                        spokenText,
                        turn.suggestedReply,
                        turn.suggestedReply.split(" ").filter { it.length > 3 }.take(4),
                        4,
                        false,
                        "${curriculum.roleplayScene.id}_$stepIndex",
                        turn.hintMl
                    )
                }
                DailyTaskType.LISTEN_AND_ANSWER -> {
                    val drill = curriculum.listenAnswerDrills[stepIndex]
                    onEvaluateSpeaking(
                        spokenText,
                        drill.sampleAnswer,
                        drill.expectedKeywords,
                        5,
                        false,
                        drill.id,
                        drill.malayalamHint
                    )
                }
                DailyTaskType.CHALLENGE -> {
                    val ch = curriculum.challengeDrill
                    val isExact = ch.kind == ChallengeKind.TONGUE_TWISTER && ch.targetText != null
                    onEvaluateSpeaking(
                        spokenText,
                        ch.targetText ?: "I see vibrant colors, people interacting warmly, and interesting details all around.",
                        listOf("see", "there", "people", "wearing", "is", "are", "because"),
                        ch.minWordsExpected,
                        isExact,
                        ch.id,
                        ch.promptMl
                    )
                }
            }
            if (eval.isOfflineScore) anyOfflineScore = true
            evaluation = eval
            evaluatingAi = false
        }
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (taskType == DailyTaskType.CHALLENGE && curriculum.challengeDrill.kind == ChallengeKind.TOPIC_TALK_30S) {
                timerRunning = true
            }
            speechEngine.startListening { spoken -> evaluateResponse(spoken) }
        }
    }

    LaunchedEffect(taskType, stepIndex) {
        speechEngine.clearTranscript()
        evaluation = null
        manualText = ""
        when (taskType) {
            DailyTaskType.SHADOW_SPEAKING -> speechEngine.speak(curriculum.shadowDrills[stepIndex].sentence)
            DailyTaskType.SITUATION_ROLEPLAY -> speechEngine.speak(curriculum.roleplayScene.turns[stepIndex].leoLine)
            DailyTaskType.LISTEN_AND_ANSWER -> speechEngine.speak(curriculum.listenAnswerDrills[stepIndex].audioQuestion)
            DailyTaskType.WORD_OF_THE_DAY -> speechEngine.speak(curriculum.wordDrills[stepIndex].word)
            DailyTaskType.CHALLENGE -> {
                val ch = curriculum.challengeDrill
                speechEngine.speak(ch.targetText ?: ch.promptEn)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { handleExit() },
                        modifier = Modifier.testTag("practice_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text(
                            text = bi(taskType.titleEn, taskType.titleMl, lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${level.titleEn} • ${stepIndex + 1}/$totalSteps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { showMalayalamHint = !showMalayalamHint }) {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = "Toggle Malayalam hints",
                            tint = if (showMalayalamHint) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { (stepIndex + 1).toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (taskDone) {
                    val avgStars = (accumulatedStars.toFloat() / totalSteps).toInt().coerceIn(3, 5)
                    val avgScore = (accumulatedScore.toFloat() / totalSteps).toInt().coerceIn(50, 100)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LeoMascotAvatar(mood = LeoMood.CHEERING, size = 148.dp)
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = bi("Task Complete!", "ടാസ്ക് പൂർത്തിയായി!", lang),
                            style = MaterialTheme.typography.displaySmall,
                            color = EmeraldSuccess
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        StarScoreRow(stars = avgStars, scorePercent = avgScore)
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "+${taskType.xpReward + avgStars * 3} XP",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("practice_finish_home_button"),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(bi("Back to Home", "ഹോമിലേക്ക് മടങ്ങുക", lang))
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        when (taskType) {
                            DailyTaskType.SHADOW_SPEAKING -> {
                                val drill = curriculum.shadowDrills[stepIndex]
                                LeoSpeechGuide(
                                    mood = if (evaluation != null) LeoMood.CHEERING else LeoMood.HAPPY,
                                    message = bi("Listen carefully and repeat out loud!", "ശ്രദ്ധിച്ചു കേട്ട് ഉറക്കെ ആവർത്തിക്കൂ!", lang),
                                    subMessage = if (showMalayalamHint) drill.speakingTipMl else drill.speakingTipEn
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                PromptAudioCard(
                                    primaryText = drill.sentence,
                                    malayalamText = if (showMalayalamHint) drill.malayalamHint else null,
                                    onPlayNormal = { speechEngine.speak(drill.sentence, slow = false) },
                                    onPlaySlow = { speechEngine.speak(drill.sentence, slow = true) },
                                    language = lang
                                )
                            }

                            DailyTaskType.WORD_OF_THE_DAY -> {
                                val word = curriculum.wordDrills[stepIndex]
                                LeoSpeechGuide(
                                    mood = LeoMood.HAPPY,
                                    message = bi(
                                        "Say a sentence using '${word.word}'!",
                                        "'${word.word}' എന്ന വാക്ക് ചേർത്ത് ഒരു ഇംഗ്ലീഷ് വാക്യം പറയൂ!",
                                        lang
                                    )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    word.word,
                                                    style = MaterialTheme.typography.displaySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    "/${word.pronunciationHint}/",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                            IconButton(onClick = { speechEngine.speak("${word.word}. ${word.sampleSentence}") }) {
                                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear word")
                                            }
                                        }
                                        if (showMalayalamHint) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = word.malayalamMeaning,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = bi("Example: \"${word.sampleSentence}\"", "ഉദാഹരണം: \"${word.sampleSentence}\"", lang),
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            DailyTaskType.SITUATION_ROLEPLAY -> {
                                val scene = curriculum.roleplayScene
                                val turn = scene.turns[stepIndex]
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${bi(scene.placeTitleEn, scene.placeTitleMl, lang)} • ${scene.roleLabelEn}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                LeoSpeechGuide(
                                    mood = LeoMood.WAITING,
                                    message = turn.leoLine,
                                    subMessage = if (showMalayalamHint) turn.leoLineMl else null
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                PromptAudioCard(
                                    primaryText = bi("Suggested reply: \"${turn.suggestedReply}\"", "ഇങ്ങനെ പറയാം: \"${turn.suggestedReply}\"", lang),
                                    malayalamText = if (showMalayalamHint) turn.hintMl else null,
                                    onPlayNormal = { speechEngine.speak(turn.leoLine, slow = false) },
                                    onPlaySlow = { speechEngine.speak(turn.leoLine, slow = true) },
                                    language = lang
                                )
                            }

                            DailyTaskType.LISTEN_AND_ANSWER -> {
                                val drill = curriculum.listenAnswerDrills[stepIndex]
                                LeoSpeechGuide(
                                    mood = LeoMood.THINKING,
                                    message = bi("Listen to Leo's question and reply naturally!", "ലിയോയുടെ ചോദ്യം കേട്ട് സ്വാഭാവികമായി ഉത്തരം പറയൂ!", lang),
                                    subMessage = if (showMalayalamHint) drill.malayalamHint else null
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                PromptAudioCard(
                                    primaryText = drill.audioQuestion,
                                    malayalamText = if (showMalayalamHint) bi("Natural sample: ${drill.sampleAnswer}", "ഉദാഹരണം: ${drill.sampleAnswer}", lang) else null,
                                    onPlayNormal = { speechEngine.speak(drill.audioQuestion, slow = false) },
                                    onPlaySlow = { speechEngine.speak(drill.audioQuestion, slow = true) },
                                    language = lang
                                )
                            }

                            DailyTaskType.CHALLENGE -> {
                                val ch = curriculum.challengeDrill
                                LeoSpeechGuide(
                                    mood = LeoMood.CHEERING,
                                    message = bi(ch.promptEn, ch.promptMl, lang),
                                    subMessage = ch.kind.titleEn
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                if (ch.imageRes != null) {
                                    Image(
                                        painter = painterResource(id = ch.imageRes),
                                        contentDescription = "Picture to describe",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                if (ch.targetText != null) {
                                    PromptAudioCard(
                                        primaryText = ch.targetText,
                                        malayalamText = if (showMalayalamHint) ch.promptMl else null,
                                        onPlayNormal = { speechEngine.speak(ch.targetText, slow = false) },
                                        onPlaySlow = { speechEngine.speak(ch.targetText, slow = true) },
                                        language = lang
                                    )
                                }
                                if (ch.kind == ChallengeKind.TOPIC_TALK_30S) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Timer, contentDescription = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${topicTalkSecondsLeft}s",
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LiveWaveformBars(isListening = isListening, rmsLevel = rmsLevel)
                        Spacer(modifier = Modifier.height(8.dp))
                        BigMicButton(
                            isListening = isListening,
                            onClick = {
                                if (isListening) {
                                    speechEngine.stopListening()
                                    timerRunning = false
                                } else {
                                    val hasMic = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        if (taskType == DailyTaskType.CHALLENGE && curriculum.challengeDrill.kind == ChallengeKind.TOPIC_TALK_30S) {
                                            topicTalkSecondsLeft = 30
                                            timerRunning = true
                                        }
                                        speechEngine.startListening { spoken ->
                                            timerRunning = false
                                            evaluateResponse(spoken)
                                        }
                                    } else {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                        )

                        if (evaluatingAi) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(bi("Checking pronunciation...", "ഉച്ചാരണം പരിശോധിക്കുന്നു...", lang), style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        val liveText = transcript.ifBlank { evaluation?.spokenText.orEmpty() }
                        if (liveText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = bi("You said: \"$liveText\"", "നിങ്ങൾ പറഞ്ഞത്: \"$liveText\"", lang),
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

                        AnimatedVisibility(visible = evaluation != null) {
                            evaluation?.let { eval ->
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = EmeraldContainerLight.copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            StarScoreRow(stars = eval.stars, scorePercent = eval.scorePercent)
                                            if (eval.isOfflineScore) {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Filled.CloudOff, contentDescription = null, modifier = Modifier.size(13.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = bi("Offline score", "ഓഫ്‌ലൈൻ സ്കോർ", lang),
                                                            style = MaterialTheme.typography.labelSmall
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = bi(eval.feedbackEn, eval.feedbackMl, lang),
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                        )
                                        if (eval.missedWords.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = bi("Try saying smoothly:", "ഒരിക്കൽ കൂടി വ്യക്തമായി പറയാം:", lang),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = "\"${eval.targetText}\"",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                eval.missedWords.take(4).forEach { w ->
                                                    AssistChip(
                                                        onClick = { speechEngine.speak(w, slow = true) },
                                                        label = { Text(w) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { showTextFallback = !showTextFallback }) {
                            Icon(Icons.Filled.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(bi("Type response instead", "ടൈപ്പ് ചെയ്ത് ഉത്തരം നൽകാം", lang))
                        }
                        AnimatedVisibility(visible = showTextFallback) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = manualText,
                                    onValueChange = { manualText = it },
                                    placeholder = { Text(bi("Speak or type here...", "ഇവിടെ ടൈപ്പ് ചെയ്യൂ...", lang)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        if (manualText.isNotBlank()) {
                                            evaluateResponse(manualText)
                                        }
                                    },
                                    modifier = Modifier.testTag("practice_submit_text_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Check typed sentence")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val earnedStars = evaluation?.stars ?: 4
                            val earnedScore = evaluation?.scorePercent ?: (earnedStars * 20)
                            accumulatedStars += earnedStars
                            accumulatedScore += earnedScore
                            if (stepIndex < totalSteps - 1) {
                                stepIndex += 1
                            } else {
                                val avgStars = (accumulatedStars.toFloat() / totalSteps).toInt().coerceIn(3, 5)
                                val avgScore = (accumulatedScore.toFloat() / totalSteps).toInt().coerceIn(50, 100)
                                taskDone = true
                                onTaskCompleted(avgStars, avgScore, anyOfflineScore)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("practice_next_step_button"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        val label = if (stepIndex < totalSteps - 1) {
                            bi("Next Drill", "അടുത്തത്", lang)
                        } else {
                            bi("Complete Task", "ടാസ്ക് പൂർത്തിയാക്കാം", lang)
                        }
                        Text(label, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        ConfettiOverlay(
            visible = taskDone,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PromptAudioCard(
    primaryText: String,
    malayalamText: String?,
    onPlayNormal: () -> Unit,
    onPlaySlow: () -> Unit,
    language: com.example.model.AppLanguage
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            if (!malayalamText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = malayalamText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPlayNormal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(bi("Replay", "കേൾക്കാം", language))
                }
                OutlinedButton(
                    onClick = onPlaySlow,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(bi("Slow 0.7x", "സാവധാനം", language))
                }
            }
        }
    }
}
