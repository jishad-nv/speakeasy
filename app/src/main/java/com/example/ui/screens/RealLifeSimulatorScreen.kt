package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.SimulatorTurnResponse
import com.example.audio.SpeechCoachEngine
import com.example.data.curriculum.ExtensionsCatalog
import com.example.model.LeoMood
import com.example.model.RealLifeScenario
import com.example.model.SimulatorReportCard
import com.example.model.SpeechInputLocale
import com.example.model.SupportLevel
import com.example.model.UserProfileState
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.LiveWaveformBars
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.adaptiveInstruction
import com.example.ui.components.bi
import com.example.ui.components.malayalamStyle
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.LeoOrangeAccent
import com.example.ui.theme.SoftYellowHighlight
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class SceneDialogueLine(
    val isCharacter: Boolean,
    val text: String,
    val malayalamHint: String = "",
    val usefulPhrase: String = ""
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RealLifeSimulatorScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    speechEngine: SpeechCoachEngine,
    turnLoading: Boolean,
    reportLoading: Boolean,
    reportCard: SimulatorReportCard?,
    lastTurnResponse: SimulatorTurnResponse?,
    onSendSceneTurn: (
        scenario: RealLifeScenario,
        userText: String,
        difficulty: Int,
        surpriseMe: Boolean,
        history: List<Pair<Boolean, String>>
    ) -> Unit,
    onFinishSceneForReport: (scenario: RealLifeScenario, dialogue: List<Pair<Boolean, String>>) -> Unit,
    onSaveLearnedWords: (List<String>) -> Unit,
    onClearSceneState: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lang = profile.language

    var selectedScenario by remember { mutableStateOf<RealLifeScenario?>(null) }
    var difficultySlider by remember { mutableFloatStateOf(2f) }
    var surpriseMe by remember { mutableStateOf(false) }
    var pressureMode by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(20) }
    var inputText by remember { mutableStateOf("") }
    val dialogue = remember { mutableStateListOf<SceneDialogueLine>() }
    var savedNewWords by remember { mutableStateOf(false) }

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()
    val rmsLevel by speechEngine.rmsLevel.collectAsStateWithLifecycle()
    val activeLocale by speechEngine.activeLocale.collectAsStateWithLifecycle()

    LaunchedEffect(lastTurnResponse) {
        val resp = lastTurnResponse ?: return@LaunchedEffect
        val scenario = selectedScenario ?: return@LaunchedEffect
        if (dialogue.lastOrNull()?.text != resp.characterReply) {
            dialogue.add(
                SceneDialogueLine(
                    isCharacter = true,
                    text = resp.characterReply,
                    malayalamHint = resp.malayalamHint,
                    usefulPhrase = resp.newUsefulPhrase
                )
            )
            speechEngine.speakWithVoice(
                text = resp.characterReply,
                pitch = scenario.voicePitch,
                rate = scenario.voiceRate,
                localeTag = "en-US"
            )
            secondsLeft = (26 - difficultySlider.roundToInt() * 3).coerceIn(10, 25)
        }
    }

    LaunchedEffect(selectedScenario, pressureMode, dialogue.size, reportCard) {
        if (selectedScenario != null && pressureMode && reportCard == null) {
            while (secondsLeft > 0) {
                delay(1000L)
                secondsLeft -= 1
            }
        }
    }

    fun submitTurn(text: String) {
        val sc = selectedScenario ?: return
        val cleaned = text.trim()
        if (cleaned.isBlank() || turnLoading) return
        dialogue.add(SceneDialogueLine(isCharacter = false, text = cleaned))
        val pairs = dialogue.map { it.isCharacter to it.text }
        val triggerTwist = surpriseMe && dialogue.count { !it.isCharacter } == 2
        onSendSceneTurn(sc, cleaned, difficultySlider.roundToInt(), triggerTwist, pairs)
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechEngine.startListening { spoken -> submitTurn(spoken) }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (selectedScenario != null) {
                            selectedScenario = null
                            dialogue.clear()
                            onClearSceneState()
                        } else {
                            onBack()
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column {
                    Text(
                        text = selectedScenario?.let { bi(it.titleEn, it.titleMl, lang) }
                            ?: bi("Real-Life Simulators", "യഥാർത്ഥ ജീവിത സന്ദർഭങ്ങൾ", lang),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (selectedScenario != null) {
                        Text(
                            text = "${selectedScenario!!.characterName} (${selectedScenario!!.characterRole})",
                            style = MaterialTheme.typography.labelMedium,
                            color = DeepGreenPrimary
                        )
                    }
                }
            }

            if (selectedScenario != null && reportCard == null) {
                Button(
                    onClick = {
                        onFinishSceneForReport(
                            selectedScenario!!,
                            dialogue.map { it.isCharacter to it.text }
                        )
                    },
                    enabled = !reportLoading && dialogue.any { !it.isCharacter },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("finish_scene_button")
                ) {
                    Text(bi("Finish", "റിപ്പോർട്ട്", lang))
                }
            }
        }

        val activeScene = selectedScenario
        if (activeScene == null) {
            // Scenario picker + Difficulty + Surprise Me + Pressure Mode controls
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LeoSpeechGuide(
                        mood = LeoMood.HAPPY,
                        message = adaptiveInstruction(
                            supportLevel = profile.supportLevel,
                            fullMl = "10 യഥാർത്ഥ ജീവിത സന്ദർഭങ്ങളിൽ വ്യത്യസ്ത ശബ്ദങ്ങളുമായി സംസാരിച്ചു പരിശീലിക്കൂ!",
                            mixed = "Pick any of the 10 scenes below. Each partner speaks with a unique voice!",
                            simpleEn = "Choose a scenario, set difficulty (1–5), and practice speaking naturally."
                        )
                    )
                }

                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = bi("Difficulty: ${difficultySlider.roundToInt()}/5", "ബുദ്ധിമുട്ട്: ${difficultySlider.roundToInt()}/5", lang),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = when (difficultySlider.roundToInt()) {
                                        1 -> bi("Gentle & Slow", "ലളിതം", lang)
                                        2 -> bi("Everyday", "സാധാരണം", lang)
                                        3 -> bi("Natural Speed", "സ്വാഭാവികം", lang)
                                        4 -> bi("Fast Follow-ups", "വേഗം", lang)
                                        else -> bi("Challenging", "കടുപ്പം", lang)
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = DeepGreenPrimary
                                )
                            }
                            Slider(
                                value = difficultySlider,
                                onValueChange = { difficultySlider = it },
                                valueRange = 1f..5f,
                                steps = 3
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.Shuffle, contentDescription = null, tint = LeoOrangeAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(bi("Surprise Me Mode", "സർപ്രൈസ് ട്വിസ്റ്റ്", lang), style = MaterialTheme.typography.titleSmall)
                                        Text(bi("Unexpected twist mid-conversation", "ഇടയ്ക്ക് അപ്രതീക്ഷിത ചോദ്യം വരും", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(checked = surpriseMe, onCheckedChange = { surpriseMe = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Filled.Timer, contentDescription = null, tint = DeepGreenPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(bi("Pressure Timer Mode", "ടൈമർ സമ്മർദ്ദം", lang), style = MaterialTheme.typography.titleSmall)
                                        Text(bi("Countdown timer per reply", "വേഗത്തിൽ ഉത്തരം പറയാൻ ടൈമർ", lang), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Switch(checked = pressureMode, onCheckedChange = { pressureMode = it })
                            }
                        }
                    }
                }

                items(ExtensionsCatalog.realLifeScenarios, key = { it.id }) { sc ->
                    SpeakUpCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scene_card_${sc.id}"),
                        onClick = {
                            onClearSceneState()
                            savedNewWords = false
                            selectedScenario = sc
                            dialogue.clear()
                            dialogue.add(
                                SceneDialogueLine(
                                    isCharacter = true,
                                    text = sc.openingLine,
                                    malayalamHint = sc.openingLineMl
                                )
                            )
                            secondsLeft = 20
                            speechEngine.speakWithVoice(
                                text = sc.openingLine,
                                pitch = sc.voicePitch,
                                rate = sc.voiceRate,
                                localeTag = "en-US"
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bi(sc.titleEn, sc.titleMl, lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${sc.characterName} • ${sc.characterRole}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepGreenPrimary
                                )
                                if (profile.supportLevel != SupportLevel.ENGLISH_ONLY) {
                                    Text(
                                        text = sc.openingLineMl,
                                        style = malayalamStyle(MaterialTheme.typography.bodySmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = bi("Start", "തുടങ്ങാം", lang),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF523600),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(14.dp)) }
            }
        } else if (reportCard != null) {
            // Post-scene Report Card
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    LeoSpeechGuide(
                        mood = LeoMood.CHEERING,
                        message = bi(
                            "Scene Complete with ${activeScene.characterName}! Here is your report card.",
                            "${activeScene.characterName}-നോടൊപ്പമുള്ള സംസാരം പൂർത്തിയായി! നിങ്ങളുടെ റിപ്പോർട്ട് ഇതാ.",
                            lang
                        )
                    )
                }

                item {
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${reportCard.fluencyScore}%",
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = DeepGreenPrimary
                                    )
                                    Text(bi("Fluency", "ഒഴുക്ക്", lang), style = MaterialTheme.typography.labelMedium)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${reportCard.confidenceScore}%",
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = LeoOrangeAccent
                                    )
                                    Text(bi("Confidence", "ആത്മവിശ്വാസം", lang), style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Text(bi("New Words Learned", "പഠിച്ച പുതിയ വാക്കുകൾ", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                reportCard.newWordsLearned.forEach { w ->
                                    Surface(color = DeepGreenContainerLight, shape = RoundedCornerShape(10.dp)) {
                                        Text(w, style = MaterialTheme.typography.labelMedium, color = DeepGreenPrimary, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                    }
                                }
                            }

                            if (!savedNewWords && reportCard.newWordsLearned.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        onSaveLearnedWords(reportCard.newWordsLearned)
                                        savedNewWords = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(bi("Save New Words to My Review Deck", "വാക്കുകൾ SM-2 ലിസ്റ്റിലേക്ക് സേവ് ചെയ്യൂ", lang))
                                }
                            }

                            Text(bi("3 Improvement Points", "മെച്ചപ്പെടുത്താനുള്ള 3 കാര്യങ്ങൾ", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            reportCard.improvementPoints.forEachIndexed { idx, tip ->
                                Text("${idx + 1}. $tip", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            selectedScenario = null
                            dialogue.clear()
                            onClearSceneState()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary)
                    ) {
                        Text(bi("Try Another Scene", "മറ്റൊരു സന്ദർഭം തിരഞ്ഞെടുക്കൂ", lang))
                    }
                }
            }
        } else {
            // Active conversation view
            AnimatedVisibility(visible = pressureMode) {
                Surface(
                    color = if (secondsLeft <= 5) MaterialTheme.colorScheme.errorContainer else SoftYellowHighlight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (secondsLeft > 0) bi("Pressure Timer: ${secondsLeft}s left", "സമയം: ${secondsLeft} സെക്കൻഡ്", lang)
                            else bi("Time's up—speak quickly anyway!", "സമയം കഴിഞ്ഞു—വേഗം ഉത്തരം പറയൂ!", lang),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dialogue) { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (line.isCharacter) Arrangement.Start else Arrangement.End
                    ) {
                        SpeakUpCard(
                            containerColor = if (line.isCharacter) MaterialTheme.colorScheme.surface else DeepGreenPrimary,
                            modifier = Modifier.fillMaxWidth(0.86f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (line.isCharacter) {
                                    Text(
                                        text = activeScene.characterName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = LeoOrangeAccent
                                    )
                                }
                                Text(
                                    text = line.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (line.isCharacter) MaterialTheme.colorScheme.onSurface else Color.White
                                )
                                if (line.isCharacter && line.malayalamHint.isNotBlank() && profile.supportLevel != SupportLevel.ENGLISH_ONLY) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = line.malayalamHint,
                                        style = malayalamStyle(MaterialTheme.typography.bodySmall),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (line.usefulPhrase.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(8.dp)) {
                                        Text(
                                            text = "💡 ${line.usefulPhrase}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFF523600),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                if (line.isCharacter) {
                                    IconButton(
                                        onClick = {
                                            speechEngine.speakWithVoice(
                                                text = line.text,
                                                pitch = activeScene.voicePitch,
                                                rate = activeScene.voiceRate
                                            )
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Replay character voice", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (turnLoading || reportLoading) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (reportLoading) bi("Scoring your performance...", "റിപ്പോർട്ട് തയ്യാറാക്കുന്നു...", lang)
                                else "${activeScene.characterName} is replying...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Bottom Speech Locale Switch + Mic + Text Input
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpeechInputLocale.entries.forEach { loc ->
                            FilterChip(
                                selected = activeLocale == loc,
                                onClick = { speechEngine.setInputLocale(loc) },
                                label = { Text(loc.labelEn, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    if (isListening) {
                        LiveWaveformBars(isListening = true, rmsLevel = rmsLevel)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isListening) {
                                    speechEngine.stopListening()
                                } else {
                                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        speechEngine.startListening { spoken -> submitTurn(spoken) }
                                    } else {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isListening) MaterialTheme.colorScheme.error else DeepGreenPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Icon(if (isListening) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = "Speak")
                        }

                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    if (isOnline) bi("Speak or type reply...", "മറുപടി പറയുകയോ ടൈപ്പ് ചെയ്യുകയോ ചെയ്യൂ...", lang)
                                    else bi("Offline starter mode...", "ഓഫ്‌ലൈൻ മറുപടി...", lang)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            shape = RoundedCornerShape(16.dp)
                        )

                        IconButton(
                            onClick = {
                                val t = inputText
                                inputText = ""
                                submitTurn(t)
                            },
                            enabled = inputText.isNotBlank() && !turnLoading
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = DeepGreenPrimary)
                        }
                    }
                }
            }
        }
    }
}
