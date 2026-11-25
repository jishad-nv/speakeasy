package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.audio.SpeakingEvaluator
import com.example.audio.SpeechCoachEngine
import com.example.data.local.MistakeEntryEntity
import com.example.model.CameraExtractResult
import com.example.model.CameraWordItem
import com.example.model.LeoMood
import com.example.model.SpeechInputLocale
import com.example.model.UserProfileState
import com.example.ui.components.ConfettiOverlay
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.bi
import com.example.ui.components.malayalamStyle
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.LeoOrangeAccent
import com.example.ui.theme.SoftYellowHighlight
import java.io.File

@Composable
fun MistakeNotebookScreen(
    profile: UserProfileState,
    mistakes: List<MistakeEntryEntity>,
    speechEngine: SpeechCoachEngine,
    onPracticeMistakeResult: (mistakeId: String, scorePercent: Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lang = profile.language

    var activeMistake by remember { mutableStateOf<MistakeEntryEntity?>(null) }
    var celebrateFixed by remember { mutableStateOf(false) }
    var lastAttemptMsg by remember { mutableStateOf<String?>(null) }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val m = activeMistake
        if (granted && m != null) {
            speechEngine.startListening(SpeechInputLocale.ENGLISH_IN) { spoken ->
                val eval = SpeakingEvaluator.evaluateSentenceMatch(spoken, m.correctForm, profile.leoPersonality)
                onPracticeMistakeResult(m.id, eval.scorePercent)
                if (eval.scorePercent >= 80 && m.fixedStreak >= 1) {
                    celebrateFixed = true
                }
                lastAttemptMsg = "\"$spoken\" -> ${eval.scorePercent}%"
            }
        }
    }

    val unfixed = remember(mistakes) { mistakes.filter { !it.isFixed } }
    val fixed = remember(mistakes) { mistakes.filter { it.isFixed } }

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
                Text(bi("Personal Mistake Notebook", "തെറ്റുകളുടെ നോട്ട്ബുക്ക്", lang), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = bi("${unfixed.size} active • ${fixed.size} mastered (works offline)", "${unfixed.size} സജീവം • ${fixed.size} ശരിയാക്കി", lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepGreenPrimary
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                val topWeak = unfixed.firstOrNull()
                LeoSpeechGuide(
                    mood = if (celebrateFixed) LeoMood.CHEERING else LeoMood.HAPPY,
                    message = if (topWeak != null) {
                        bi(
                            "You missed \"${topWeak.correctForm}\" earlier—let's try it again! Speak twice cleanly to mark it fixed.",
                            "മുൻപ് തെറ്റിയ \"${topWeak.correctForm}\" നമുക്ക് ഒരിക്കൽ കൂടി പറയാം!",
                            lang
                        )
                    } else {
                        bi(
                            "No active mistakes right now! Speak in daily tasks or chats and I will remember patterns for you.",
                            "ഇപ്പോൾ സജീവമായ തെറ്റുകളൊന്നുമില്ല! പതിവായി സംസാരിക്കൂ.",
                            lang
                        )
                    }
                )
            }

            if (!lastAttemptMsg.isNullOrBlank()) {
                item {
                    Surface(color = DeepGreenContainerLight, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("🎤 $lastAttemptMsg", style = MaterialTheme.typography.bodyMedium, color = DeepGreenPrimary, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            items(mistakes, key = { it.id }) { m ->
                SpeakUpCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (m.isFixed) DeepGreenContainerLight.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(8.dp)) {
                                Text(
                                    text = m.category.replace("_", " ").uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (m.isFixed) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = DeepGreenPrimary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(bi("Fixed!", "ശരിയാക്കി!", lang), style = MaterialTheme.typography.labelSmall, color = DeepGreenPrimary)
                                }
                            } else {
                                Text("Seen ${m.occurrences}x • Streak ${m.fixedStreak}/2", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (m.wrongPattern.isNotBlank()) {
                            Text(
                                text = "✗ ${m.wrongPattern}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "✓ ${m.correctForm}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepGreenPrimary
                        )
                        if (m.malayalamNote.isNotBlank()) {
                            Text(m.malayalamNote, style = malayalamStyle(MaterialTheme.typography.bodySmall), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { speechEngine.speak(m.correctForm, slow = true) }, modifier = Modifier.weight(1f)) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Hear", "കേൾക്കൂ", lang))
                            }
                            Button(
                                onClick = {
                                    activeMistake = m
                                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        speechEngine.startListening(SpeechInputLocale.ENGLISH_IN) { spoken ->
                                            val eval = SpeakingEvaluator.evaluateSentenceMatch(spoken, m.correctForm, profile.leoPersonality)
                                            onPracticeMistakeResult(m.id, eval.scorePercent)
                                            if (eval.scorePercent >= 80 && m.fixedStreak >= 1) celebrateFixed = true
                                            lastAttemptMsg = "\"$spoken\" -> ${eval.scorePercent}%"
                                        }
                                    } else {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Try Fix", "പരിഹരിക്കൂ", lang))
                            }
                        }
                    }
                }
            }
        }
    }

    ConfettiOverlay(visible = celebrateFixed, modifier = Modifier.fillMaxSize())
}

@Composable
fun CameraLearnScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    speechEngine: SpeechCoachEngine,
    extractResult: CameraExtractResult?,
    loading: Boolean,
    errorMessage: String?,
    onAnalyzeBitmap: (Bitmap) -> Unit,
    onSaveWordToSrs: (CameraWordItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lang = profile.language
    var savedItems by remember { mutableStateOf<Set<String>>(emptySet()) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    fun createTempPhotoUri(): Uri {
        val dir = File(context.cacheDir, "camera_photos").apply { mkdirs() }
        val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun decodeScaledBitmap(uri: Uri): Bitmap? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val raw = BitmapFactory.decodeStream(stream) ?: return null
            val maxDim = 1280f
            val scale = minOf(1f, maxDim / maxOf(raw.width, raw.height))
            if (scale < 1f) {
                Bitmap.createScaledBitmap(raw, (raw.width * scale).toInt(), (raw.height * scale).toInt(), true)
            } else raw
        }
    }.getOrNull()

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        val uri = tempCameraUri
        if (ok && uri != null) {
            decodeScaledBitmap(uri)?.let(onAnalyzeBitmap)
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempPhotoUri()
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            decodeScaledBitmap(uri)?.let(onAnalyzeBitmap)
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
                Text(bi("Camera Learn", "ക്യാമറ പഠനം", lang), style = MaterialTheme.typography.titleLarge)
                Text(
                    text = bi("Snap any sign, menu or label -> Malayalam + SM-2", "ബോർഡോ മെനുവോ ഫോട്ടോ എടുക്കൂ", lang),
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
                    mood = LeoMood.HAPPY,
                    message = bi(
                        "Point your camera at any English sign, bill, menu, or bottle! I'll explain every useful word in Malayalam.",
                        "ഏതെങ്കിലും ഇംഗ്ലീഷ് ബോർഡോ ബില്ലോ മെനുവോ ഫോട്ടോ എടുക്കൂ! മലയാളത്തിൽ അർത്ഥം പറഞ്ഞുതരാം.",
                        lang
                    )
                )
            }

            if (!isOnline) {
                item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = bi(
                                    "Camera Learn needs internet to read photos. Your saved words & 30-day offline pack still work!",
                                    "ഫോട്ടോ വായിക്കാൻ ഇന്റർനെറ്റ് ആവശ്യമാണ്. സേവ് ചെയ്ത വാക്കുകൾ ഓഫ്‌ലൈനായി പഠിക്കാം!",
                                    lang
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val hasCam = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasCam) {
                                val uri = createTempPhotoUri()
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        enabled = isOnline && !loading,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreenPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(bi("Take Photo", "ക്യാമറ", lang))
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = isOnline && !loading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(bi("Pick Photo", "ഗാലറി", lang))
                    }
                }
            }

            if (loading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(bi("Reading English text & translating to Malayalam...", "ചിത്രത്തിലെ വാക്കുകൾ വായിക്കുന്നു...", lang))
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(errorMessage, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            extractResult?.let { res ->
                item {
                    Surface(color = SoftYellowHighlight, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(res.sceneSummaryMl, style = malayalamStyle(MaterialTheme.typography.titleSmall), modifier = Modifier.padding(12.dp))
                    }
                }
                items(res.items) { word ->
                    val alreadySaved = savedItems.contains(word.englishText)
                    SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(word.englishText, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(word.malayalamMeaning, style = malayalamStyle(MaterialTheme.typography.bodyMedium), color = DeepGreenPrimary)
                            Text("🗣️ ${word.manglishPronunciation}", style = malayalamStyle(MaterialTheme.typography.labelMedium))
                            if (word.exampleSentence.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("\"${word.exampleSentence}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { speechEngine.speak(word.englishText, slow = false) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                OutlinedButton(onClick = { speechEngine.speak(word.englishText, slow = true) }) {
                                    Icon(Icons.Filled.SlowMotionVideo, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Button(
                                    onClick = {
                                        onSaveWordToSrs(word)
                                        savedItems = savedItems + word.englishText
                                    },
                                    enabled = !alreadySaved,
                                    colors = ButtonDefaults.buttonColors(containerColor = if (alreadySaved) Color.Gray else DeepGreenPrimary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.BookmarkAdded, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (alreadySaved) bi("Saved (SM-2)", "സേവ് ചെയ്തു", lang) else bi("Save to my words", "സേവ് ചെയ്യുക", lang))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
