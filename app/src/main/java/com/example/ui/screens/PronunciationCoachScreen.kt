package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio.AudioClipRecorder
import com.example.audio.SpeechCoachEngine
import com.example.data.curriculum.ExtensionsCatalog
import com.example.data.local.SoundProgressEntity
import com.example.model.AccentSoundTarget
import com.example.model.AudioPronunciationAnalysis
import com.example.model.LeoMood
import com.example.model.UserProfileState
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.MouthPositionDiagram
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.bi
import com.example.ui.components.malayalamStyle
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.LeoOrangeAccent
import com.example.ui.theme.SoftYellowHighlight

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PronunciationCoachScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    speechEngine: SpeechCoachEngine,
    audioRecorder: AudioClipRecorder,
    analysisResult: AudioPronunciationAnalysis?,
    analysisLoading: Boolean,
    soundHistory: List<SoundProgressEntity>,
    onAnalyzeAudio: (bytes: ByteArray, targetSentence: String, soundTarget: AccentSoundTarget) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lang = profile.language

    var selectedTarget by remember { mutableStateOf(AccentSoundTarget.TH_DH) }
    var activeSlot by remember { mutableStateOf("before") }

    val isRecording by audioRecorder.isRecording.collectAsStateWithLifecycle()
    val hasBefore by audioRecorder.hasBeforeClip.collectAsStateWithLifecycle()
    val hasAfter by audioRecorder.hasAfterClip.collectAsStateWithLifecycle()
    val playingSlot by audioRecorder.playingSlot.collectAsStateWithLifecycle()

    val drillsForTarget = remember(selectedTarget) {
        ExtensionsCatalog.minimalPairDrills.filter { it.target == selectedTarget }
            .ifEmpty { ExtensionsCatalog.minimalPairDrills.take(1) }
    }
    var selectedDrillIndex by remember(selectedTarget) { mutableStateOf(0) }
    val activeDrill = drillsForTarget[selectedDrillIndex.coerceIn(0, drillsForTarget.lastIndex)]

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            audioRecorder.startRecording(activeSlot)
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
                    text = bi("Malayali-Accent Coach", "ഉച്ചാരണ പരിശീലനം", lang),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = bi("Audio AI + Mouth Guide + Before/After", "ശബ്ദ പരിശോധനയും വായയുടെ ചിത്രവും", lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepGreenPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LeoSpeechGuide(
                    mood = if (isRecording) LeoMood.WAITING else LeoMood.HAPPY,
                    message = bi(
                        "Record your voice directly—Gemini listens to your real audio to spot TH/DH, V/W, F/P, Z, and vowel length!",
                        "നിങ്ങളുടെ ശബ്ദം റെക്കോർഡ് ചെയ്യൂ—TH/DH, V/W, F/P, Z ശബ്ദങ്ങൾ AI കേട്ട് കൃത്യമായ നിർദ്ദേശം നൽകും!",
                        lang
                    )
                )
            }

            // Sound Target Selector
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(AccentSoundTarget.entries) { t ->
                        FilterChip(
                            selected = selectedTarget == t,
                            onClick = { selectedTarget = t },
                            label = { Text(bi(t.labelEn, t.labelMl, lang), style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }

            // Mouth / Tongue SVG Diagram + Guide
            item {
                SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = bi(selectedTarget.labelEn, selectedTarget.labelMl, lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepGreenPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        MouthPositionDiagram(target = selectedTarget)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedTarget.mouthGuideEn,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedTarget.mouthGuideMl,
                            style = malayalamStyle(MaterialTheme.typography.bodySmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Minimal-Pair Drill Card
            item {
                SpeakUpCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = SoftYellowHighlight.copy(alpha = 0.45f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = bi("Minimal-Pair Drill", "ജോഡി വാക്ക് പരിശീലനം", lang),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color(0xFF6B4700)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { speechEngine.speak(activeDrill.wordA, slow = true) },
                                label = { Text("🔊 ${activeDrill.wordA}") }
                            )
                            AssistChip(
                                onClick = { speechEngine.speak(activeDrill.wordB, slow = true) },
                                label = { Text("🔊 ${activeDrill.wordB}") }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${activeDrill.sentencePrompt}\"",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeDrill.malayalamTip,
                            style = malayalamStyle(MaterialTheme.typography.bodySmall),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { speechEngine.speak(activeDrill.sentencePrompt, slow = false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Hear", "കേൾക്കൂ", lang))
                            }
                            OutlinedButton(
                                onClick = { speechEngine.speak(activeDrill.sentencePrompt, slow = true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Slow", "പതുക്കെ", lang))
                            }
                        }
                    }
                }
            }

            // Before vs After Recording Comparison + Gemini Audio Analysis
            item {
                SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = bi("Before vs After Audio Lab", "മുൻപും ശേഷവും ശബ്ദ താരതമ്യം", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (!isOnline) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bi(
                                    "Offline: recordings save locally. Connect online for deep Gemini Audio phone-by-phone analysis.",
                                    "ഓഫ്‌ലൈൻ: റെക്കോർഡിംഗ് കേൾക്കാം. ആഴത്തിലുള്ള AI ശബ്ദ പരിശോധനയ്ക്ക് ഇന്റർനെറ്റ് വേണം.",
                                    lang
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = LeoOrangeAccent
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("before" to bi("1. Before", "1. ആദ്യം", lang), "after" to bi("2. After", "2. മെച്ചപ്പെട്ടത്", lang)).forEach { (slot, label) ->
                                val recordingThis = isRecording && activeSlot == slot
                                Button(
                                    onClick = {
                                        if (recordingThis) {
                                            val bytes = audioRecorder.stopRecording(slot)
                                            if (bytes != null) {
                                                onAnalyzeAudio(bytes, activeDrill.sentencePrompt, selectedTarget)
                                            }
                                        } else {
                                            activeSlot = slot
                                            val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                            if (hasMic) {
                                                audioRecorder.startRecording(slot)
                                            } else {
                                                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (recordingThis) MaterialTheme.colorScheme.error else DeepGreenPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (recordingThis) Icons.Filled.Stop else Icons.Filled.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (recordingThis) bi("Stop", "നിർത്തൂ", lang) else label)
                                }
                            }
                        }

                        if (hasBefore || hasAfter) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { audioRecorder.playSlot("before") },
                                    enabled = hasBefore,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (playingSlot == "before") "Playing..." else bi("Play Before", "ആദ്യ ശബ്ദം", lang))
                                }
                                OutlinedButton(
                                    onClick = { audioRecorder.playSlot("after") },
                                    enabled = hasAfter,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (playingSlot == "after") "Playing..." else bi("Play After", "പുതിയ ശബ്ദം", lang))
                                }
                            }
                        }

                        if (analysisLoading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(bi("Gemini is listening to your audio...", "ജെമിനി നിങ്ങളുടെ ശബ്ദം പരിശോധിക്കുന്നു...", lang))
                            }
                        }

                        analysisResult?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                color = DeepGreenContainerLight,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = bi("Audio Accuracy: ${res.overallScore}%", "ശബ്ദ കൃത്യത: ${res.overallScore}%", lang),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = DeepGreenPrimary
                                        )
                                        if (res.isOfflineFallback) {
                                            Text("Offline fallback", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = res.tip, style = malayalamStyle(MaterialTheme.typography.bodyMedium))

                                    if (res.wordScores.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            res.wordScores.forEach { ws ->
                                                val badgeColor = if (ws.score >= 80) DeepGreenPrimary else LeoOrangeAccent
                                                Surface(
                                                    color = badgeColor.copy(alpha = 0.14f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "${ws.word} (${ws.score}%)",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = badgeColor,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    res.soundErrors.forEach { err ->
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "• ${err.issue}: ${err.correctionTip}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Weekly Sound Progress Overview
            item {
                SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = DeepGreenPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bi("Weekly Per-Sound Tracking", "ആഴ്ചയിലെ ശബ്ദ പുരോഗതി", lang),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        val perTargetAvg = AccentSoundTarget.entries.map { t ->
                            val items = soundHistory.filter { it.soundTargetId == t.id }
                            val avg = if (items.isEmpty()) 0 else items.map { it.score }.average().toInt()
                            t to avg
                        }
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(84.dp)
                        ) {
                            val slotW = size.width / perTargetAvg.size
                            val barW = slotW * 0.55f
                            perTargetAvg.forEachIndexed { idx, (_, score) ->
                                val x = idx * slotW + (slotW - barW) / 2f
                                drawRoundRect(
                                    color = Color(0xFFEDE4D3),
                                    topLeft = Offset(x, 0f),
                                    size = Size(barW, size.height),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                                val fillH = (size.height * (score / 100f)).coerceAtLeast(if (score > 0) 6.dp.toPx() else 0f)
                                if (fillH > 0f) {
                                    drawRoundRect(
                                        color = if (score >= 80) DeepGreenPrimary else LeoOrangeAccent,
                                        topLeft = Offset(x, size.height - fillH),
                                        size = Size(barW, fillH),
                                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            perTargetAvg.forEach { (t, avg) ->
                                Text(
                                    text = "${t.name.take(3)}\n${if (avg > 0) "$avg%" else "-"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
