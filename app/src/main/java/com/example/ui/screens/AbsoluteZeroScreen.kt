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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SlowMotionVideo
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
import com.example.data.curriculum.AbsoluteZeroCatalog
import com.example.model.LeoMood
import com.example.model.SpeechInputLocale
import com.example.model.UserProfileState
import com.example.model.ZeroPhraseCategory
import com.example.model.ZeroSurvivalPhrase
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.bi
import com.example.ui.components.malayalamStyle
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.SoftYellowHighlight

@Composable
fun AbsoluteZeroScreen(
    profile: UserProfileState,
    speechEngine: SpeechCoachEngine,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val lang = profile.language

    var selectedCategory by remember { mutableStateOf(ZeroPhraseCategory.GREETINGS) }
    var searchQuery by remember { mutableStateOf("") }
    var activePhrase by remember { mutableStateOf<ZeroSurvivalPhrase?>(null) }
    var quickScoreFeedback by remember { mutableStateOf<String?>(null) }

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val phrase = activePhrase
        if (granted && phrase != null) {
            speechEngine.startListening(SpeechInputLocale.ENGLISH_IN) { spoken ->
                val eval = SpeakingEvaluator.evaluateSentenceMatch(spoken, phrase.englishText, profile.leoPersonality)
                quickScoreFeedback = "\"$spoken\" -> ${eval.scorePercent}% (${eval.stars}/5★)"
            }
        }
    }

    val phrases = remember(selectedCategory, searchQuery) {
        val base = if (searchQuery.isBlank()) {
            AbsoluteZeroCatalog.byCategory(selectedCategory)
        } else {
            AbsoluteZeroCatalog.all300Phrases.filter {
                it.englishText.contains(searchQuery, ignoreCase = true) ||
                    it.malayalamMeaning.contains(searchQuery, ignoreCase = true) ||
                    it.manglishScript.contains(searchQuery, ignoreCase = true)
            }
        }
        base
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
                    text = bi("Level 0: Absolute Zero", "ലെവൽ 0: ആദ്യ ചുവടുകൾ", lang),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = bi("300 Survival Phrases • Offline Ready", "300 നിത്യോപയോഗ വാക്യങ്ങൾ • ഓഫ്‌ലൈൻ", lang),
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
                LeoSpeechGuide(
                    mood = if (isListening) LeoMood.WAITING else LeoMood.HAPPY,
                    message = bi(
                        "Know zero English? No problem! Read the Malayalam script pronunciation, listen slowly, and say it aloud!",
                        "ഇംഗ്ലീഷ് ഒട്ടും അറിയില്ലേ? വിഷമിക്കേണ്ട! മലയാളത്തിൽ എഴുതിയ ഉച്ചാരണം നോക്കി പതുക്കെ കേട്ട് കൂടെ പറയൂ!",
                        lang
                    )
                )
            }

            if (!quickScoreFeedback.isNullOrBlank()) {
                item {
                    Surface(
                        color = DeepGreenContainerLight,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎤 $quickScoreFeedback",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = DeepGreenPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    placeholder = { Text(bi("Search 300 phrases...", "300 വാക്യങ്ങളിൽ തിരയുക...", lang)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ZeroPhraseCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat && searchQuery.isBlank(),
                            onClick = {
                                searchQuery = ""
                                selectedCategory = cat
                            },
                            label = {
                                Text(bi(cat.titleEn, cat.titleMl, lang), style = MaterialTheme.typography.labelMedium)
                            }
                        )
                    }
                }
            }

            items(phrases, key = { it.id }) { phrase ->
                SpeakUpCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "#${phrase.numberTag} • ${phrase.englishText}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = phrase.malayalamMeaning,
                            style = malayalamStyle(MaterialTheme.typography.bodyMedium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = SoftYellowHighlight,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "🗣️ ${phrase.manglishScript}",
                                style = malayalamStyle(MaterialTheme.typography.labelLarge),
                                color = androidx.compose.ui.graphics.Color(0xFF422C00),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { speechEngine.speak(phrase.englishText, slow = false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Play", "കേൾക്കൂ", lang))
                            }
                            OutlinedButton(
                                onClick = { speechEngine.speak(phrase.englishText, slow = true) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.SlowMotionVideo, contentDescription = "Slow play", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Slow", "പതുക്കെ", lang))
                            }
                            OutlinedButton(
                                onClick = {
                                    activePhrase = phrase
                                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        speechEngine.startListening(SpeechInputLocale.ENGLISH_IN) { spoken ->
                                            val eval = SpeakingEvaluator.evaluateSentenceMatch(spoken, phrase.englishText, profile.leoPersonality)
                                            quickScoreFeedback = "\"$spoken\" -> ${eval.scorePercent}% (${eval.stars}/5★)"
                                        }
                                    } else {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = "Say it", modifier = Modifier.size(16.dp), tint = DeepGreenPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Say", "പറയൂ", lang))
                            }
                        }
                    }
                }
            }
        }
    }
}
