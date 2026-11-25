package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SlowMotionVideo
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.SpeakingEvaluator
import com.example.audio.SpeechCoachEngine
import com.example.data.curriculum.ExtensionsCatalog
import com.example.data.local.JournalEntryEntity
import com.example.model.JournalCorrectionResult
import com.example.model.LeoMood
import com.example.model.SpeechInputLocale
import com.example.model.TranslateLearnResult
import com.example.model.UserProfileState
import com.example.ui.components.BigMicButton
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.LiveWaveformBars
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.bi
import com.example.ui.components.malayalamStyle
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.SoftYellowHighlight

@Composable
fun MalayalamToEnglishScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    speechEngine: SpeechCoachEngine,
    translateResult: TranslateLearnResult?,
    translateLoading: Boolean,
    journalCorrection: JournalCorrectionResult?,
    journalLoading: Boolean,
    savedJournals: List<JournalEntryEntity>,
    onTranslateMalayalam: (String) -> Unit,
    onPolishJournal: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lang = profile.language

    var tabMode by remember { mutableIntStateOf(0) } // 0=ML->EN, 1=Reverse, 2=Journal
    var mlInput by remember { mutableStateOf("") }
    var repeatScoreText by remember { mutableStateOf<String?>(null) }

    var reverseIndex by remember { mutableIntStateOf(0) }
    var reverseFeedback by remember { mutableStateOf<String?>(null) }

    var journalText by remember { mutableStateOf("") }

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()
    val rmsLevel by speechEngine.rmsLevel.collectAsStateWithLifecycle()

    var pendingMicAction by remember { mutableStateOf<() -> Unit>({}) }
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pendingMicAction()
    }

    fun launchWithMic(locale: SpeechInputLocale, onSpoken: (String) -> Unit) {
        val action = { speechEngine.startListening(locale, onSpoken) }
        val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (hasMic) action() else {
            pendingMicAction = action
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bi("Malayalam -> English Lab", "മലയാളത്തിൽ നിന്ന് ഇംഗ്ലീഷ്", lang),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = bi("Translate, Reverse Mode & Daily Journal", "വിവർത്തനം, റിവേഴ്സ്, ഡെയ്‌ലി ജേർണൽ", lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepGreenPrimary
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                0 to bi("Say in ML", "മലയാളത്തിൽ പറയൂ", lang),
                1 to bi("Reverse", "റിവേഴ്സ്", lang),
                2 to bi("Journal", "ജേർണൽ", lang)
            ).forEach { (idx, title) ->
                FilterChip(
                    selected = tabMode == idx,
                    onClick = { tabMode = idx },
                    label = { Text(title, style = MaterialTheme.typography.labelMedium) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (tabMode) {
                0 -> {
                    item {
                        LeoSpeechGuide(
                            mood = LeoMood.HAPPY,
                            message = bi(
                                "Speak or type anything in Malayalam! I will give you natural Formal & Casual English so you can repeat it.",
                                "മലയാളത്തിൽ എന്തും പറയൂ അല്ലെങ്കിൽ ടൈപ്പ് ചെയ്യൂ! ഔപചാരികവും സാധാരണവുമായ ഇംഗ്ലീഷ് ഞാൻ തരാം.",
                                lang
                            )
                        )
                    }
                    item {
                        SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                OutlinedTextField(
                                    value = mlInput,
                                    onValueChange = { mlInput = it },
                                    label = { Text(bi("Speak (ml-IN) or type Malayalam", "മലയാളത്തിൽ ടൈപ്പ് ചെയ്യൂ", lang)) },
                                    placeholder = { Text("ഉദാ: നാളെ മീറ്റിംഗ് ഉണ്ടോ?") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                LiveWaveformBars(isListening = isListening, rmsLevel = rmsLevel)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            launchWithMic(SpeechInputLocale.MALAYALAM_IN) { spoken ->
                                                mlInput = spoken
                                                onTranslateMalayalam(spoken)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp), tint = DeepGreenPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(bi("Speak ML", "മലയാളം പറയൂ", lang))
                                    }
                                    Button(
                                        onClick = { if (mlInput.isNotBlank()) onTranslateMalayalam(mlInput) },
                                        enabled = mlInput.isNotBlank() && !translateLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (translateLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                        } else {
                                            Text(bi("Get English", "ഇംഗ്ലീഷ് കാണൂ", lang))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    translateResult?.let { res ->
                        item {
                            SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Formal English:", style = MaterialTheme.typography.labelMedium, color = DeepGreenPrimary)
                                    Text(res.formalEnglish, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    if (res.manglishFormal.isNotBlank()) {
                                        Text(res.manglishFormal, style = malayalamStyle(MaterialTheme.typography.bodySmall), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Casual English:", style = MaterialTheme.typography.labelMedium, color = DeepGreenPrimary)
                                    Text(res.casualEnglish, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    if (res.manglishCasual.isNotBlank()) {
                                        Text(res.manglishCasual, style = malayalamStyle(MaterialTheme.typography.bodySmall), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text(res.contextNote, style = malayalamStyle(MaterialTheme.typography.bodySmall), modifier = Modifier.padding(10.dp))
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = { speechEngine.speak(res.casualEnglish) }, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(bi("Play", "കേൾക്കൂ", lang))
                                        }
                                        OutlinedButton(onClick = { speechEngine.speak(res.casualEnglish, slow = true) }, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(bi("Slow", "പതുക്കെ", lang))
                                        }
                                        Button(
                                            onClick = {
                                                launchWithMic(SpeechInputLocale.ENGLISH_IN) { spokenEn ->
                                                    val ev = SpeakingEvaluator.evaluateSentenceMatch(spokenEn, res.casualEnglish, profile.leoPersonality)
                                                    repeatScoreText = "\"$spokenEn\" -> ${ev.scorePercent}% (${ev.stars}/5★)"
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(bi("Repeat", "പറയൂ", lang))
                                        }
                                    }
                                    repeatScoreText?.let { scoreMsg ->
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(scoreMsg, style = MaterialTheme.typography.labelLarge, color = DeepGreenPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val promptPair = ExtensionsCatalog.reverseMalayalamPrompts[reverseIndex % ExtensionsCatalog.reverseMalayalamPrompts.size]
                    item {
                        LeoSpeechGuide(
                            mood = if (isListening) LeoMood.WAITING else LeoMood.HAPPY,
                            message = bi(
                                "Reverse Mode: Listen to my Malayalam sentence, then speak the English version aloud!",
                                "റിവേഴ്സ് മോഡ്: ഞാൻ പറയുന്ന മലയാളം വാക്യം കേട്ട് ഇംഗ്ലീഷിൽ പറയൂ!",
                                lang
                            )
                        )
                    }
                    item {
                        SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = promptPair.first,
                                    style = malayalamStyle(MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(onClick = { speechEngine.speakMalayalam(promptPair.first) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(bi("Hear Leo in Malayalam", "മലയാളത്തിൽ കേൾക്കാം", lang))
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                BigMicButton(
                                    isListening = isListening,
                                    onClick = {
                                        if (isListening) speechEngine.stopListening()
                                        else launchWithMic(SpeechInputLocale.ENGLISH_IN) { spokenEn ->
                                            val ev = SpeakingEvaluator.evaluateSentenceMatch(spokenEn, promptPair.second, profile.leoPersonality)
                                            reverseFeedback = "You said: \"$spokenEn\"\nExpected: \"${promptPair.second}\"\nScore: ${ev.scorePercent}% (${ev.stars}/5★)"
                                        }
                                    }
                                )
                                reverseFeedback?.let { fb ->
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(color = DeepGreenContainerLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                                        Text(fb, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        reverseIndex += 1
                                        reverseFeedback = null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(bi("Next Malayalam Prompt", "അടുത്ത വാക്യം", lang))
                                }
                            }
                        }
                    }
                }

                else -> {
                    item {
                        LeoSpeechGuide(
                            mood = LeoMood.THINKING,
                            message = bi(
                                "\"Think in English\" Daily Journal: speak 3–5 sentences about your day! I will polish it naturally without grammar rules.",
                                "ഇന്നത്തെ ദിവസത്തെക്കുറിച്ച് 3–5 വാക്യം ഇംഗ്ലീഷിൽ പറയൂ! വ്യാകരണ നിയമങ്ങളില്ലാതെ കൂടുതൽ സ്വാഭാവികമായി എങ്ങനെ പറയാമെന്ന് ഞാൻ കാണിക്കാം.",
                                lang
                            )
                        )
                    }
                    item {
                        SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                OutlinedTextField(
                                    value = journalText,
                                    onValueChange = { journalText = it },
                                    label = { Text(bi("Speak or type 3-5 sentences about today", "ഇന്നത്തെ ദിവസത്തെക്കുറിച്ച് പറയൂ", lang)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            launchWithMic(SpeechInputLocale.ENGLISH_IN) { spoken ->
                                                journalText = if (journalText.isBlank()) spoken else "$journalText. $spoken"
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.Mic, contentDescription = null, tint = DeepGreenPrimary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(bi("Add Voice", "സംസാരിക്കൂ", lang))
                                    }
                                    Button(
                                        onClick = { if (journalText.isNotBlank()) onPolishJournal(journalText) },
                                        enabled = journalText.isNotBlank() && !journalLoading && isOnline,
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(bi("Polish", "മെച്ചപ്പെടുത്തൂ", lang))
                                    }
                                }
                                if (!isOnline) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(bi("Needs internet to polish journal", "മെച്ചപ്പെടുത്താൻ ഇന്റർനെറ്റ് വേണം", lang), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    journalCorrection?.let { jc ->
                        item {
                            SpeakUpCard(modifier = Modifier.fillMaxWidth(), containerColor = DeepGreenContainerLight) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(bi("Natural Spoken Version", "സ്വാഭാവിക ഇംഗ്ലീഷ്", lang), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepGreenPrimary)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(jc.naturalVersion, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    jc.keyImprovements.forEach { tip ->
                                        Text("✨ $tip", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(onClick = { speechEngine.speak(jc.naturalVersion) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(bi("Listen & Shadow", "കേട്ട് ആവർത്തിക്കാം", lang))
                                    }
                                }
                            }
                        }
                    }

                    if (savedJournals.isNotEmpty()) {
                        item {
                            Text(bi("Past Journal Entries", "മുൻ ജേർണലുകൾ", lang), style = MaterialTheme.typography.titleSmall)
                        }
                        items(savedJournals.take(5)) { entry ->
                            SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(entry.dateKey, style = MaterialTheme.typography.labelSmall, color = DeepGreenPrimary)
                                    Text(entry.naturalVersion, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
