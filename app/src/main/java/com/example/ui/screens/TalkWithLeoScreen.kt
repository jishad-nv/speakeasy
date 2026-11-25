package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.audio.SpeechCoachEngine
import com.example.model.ChatMessage
import com.example.model.LeoMood
import com.example.model.SpeechInputLocale
import com.example.model.SupportLevel
import com.example.model.UserProfileState
import com.example.ui.components.LeoMascotAvatar
import com.example.ui.components.LiveWaveformBars
import com.example.ui.components.bi
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.EmeraldContainerLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TangerinePrimary

@Composable
fun TalkWithLeoScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    messages: List<ChatMessage>,
    selectedTopic: String,
    correctMeEnabled: Boolean,
    askMalayalamEnabled: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    speechEngine: SpeechCoachEngine,
    onSelectTopic: (String) -> Unit,
    onToggleCorrectMe: () -> Unit,
    onToggleAskMalayalam: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRequestMalayalamHelp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = profile.language
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val isListening by speechEngine.isListening.collectAsStateWithLifecycle()
    val rmsLevel by speechEngine.rmsLevel.collectAsStateWithLifecycle()
    val transcript by speechEngine.transcript.collectAsStateWithLifecycle()
    val activeLocale by speechEngine.activeLocale.collectAsStateWithLifecycle()

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechEngine.startListening { spoken ->
                onSendMessage(spoken)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val topics = listOf(
        "Daily Coffee & Small Talk",
        "Job Interview Practice",
        "At the Doctor",
        "Travel & Airport",
        "Free Conversation"
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Header + Toggles
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LeoMascotAvatar(
                            mood = when {
                                isLoading -> LeoMood.THINKING
                                isListening -> LeoMood.WAITING
                                else -> LeoMood.HAPPY
                            },
                            size = 46.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = bi("Talk with Leo", "ലിയോയോട് സംസാരിക്കാം", lang),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = bi(
                                    "${profile.leoPersonality.titleEn} • ${profile.currentLevel.titleEn}",
                                    "${profile.leoPersonality.titleMl} • ${profile.currentLevel.titleMl}",
                                    lang
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Spellcheck,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = bi("Correct me", "തിരുത്തുക", lang),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = correctMeEnabled,
                            onCheckedChange = { onToggleCorrectMe() },
                            modifier = Modifier.testTag("correct_me_toggle")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = askMalayalamEnabled,
                            onClick = onToggleAskMalayalam,
                            label = { Text("മലയാളം സഹായം", style = MaterialTheme.typography.labelMedium) },
                            leadingIcon = {
                                Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }
                    items(topics) { topic ->
                        FilterChip(
                            selected = selectedTopic == topic,
                            onClick = { onSelectTopic(topic) },
                            label = { Text(topic, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }
            }
        }

        // Offline "Leo needs internet to chat" banner
        AnimatedVisibility(visible = !isOnline) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bi(
                            "Leo needs internet to chat — daily lessons and offline drills still work!",
                            "ചാറ്റ് ചെയ്യാൻ ലിയോയ്ക്ക് ഇന്റർനെറ്റ് വേണം — ദൈനംദിന പാഠങ്ങൾ ഓഫ്‌ലൈനായി ഉപയോഗിക്കാം!",
                            lang
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubbleItem(
                    message = msg,
                    language = lang,
                    onSpeak = { speechEngine.speak(msg.text) },
                    onMalayalamHelp = { onRequestMalayalamHelp(msg.id) }
                )
            }

            if (isLoading) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = bi("Leo is thinking...", "ലിയോ ചിന്തിക്കുന്നു...", lang),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!errorMessage.isNullOrBlank()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // Bottom Voice + Text Input Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                AnimatedVisibility(visible = isListening) {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        LiveWaveformBars(isListening = true, rmsLevel = rmsLevel)
                        if (transcript.isNotBlank()) {
                            Text(
                                text = transcript,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    SpeechInputLocale.entries.forEach { loc ->
                        FilterChip(
                            selected = activeLocale == loc,
                            onClick = { speechEngine.setInputLocale(loc) },
                            label = { Text(loc.labelEn, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("talk_locale_${loc.name.lowercase()}")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (!isOnline) {
                                onSendMessage("Leo needs internet to chat")
                                return@FloatingActionButton
                            }
                            if (isListening) {
                                speechEngine.stopListening()
                            } else {
                                val hasMic = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasMic) {
                                    speechEngine.startListening { spoken ->
                                        onSendMessage(spoken)
                                    }
                                } else {
                                    micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        containerColor = if (isListening) MaterialTheme.colorScheme.error else DeepGreenPrimary,
                        contentColor = Color.White,
                        modifier = Modifier
                            .size(50.dp)
                            .testTag("chat_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = "Speak to Leo"
                        )
                    }

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text(
                                when {
                                    !isOnline -> bi("Leo needs internet to chat", "ചാറ്റ് ചെയ്യാൻ ഇന്റർനെറ്റ് വേണം", lang)
                                    activeLocale == SpeechInputLocale.MALAYALAM_IN -> "മലയാളത്തിൽ പറയുകയോ ടൈപ്പ് ചെയ്യുകയോ ചെയ്യൂ..."
                                    profile.supportLevel == SupportLevel.FULL_MALAYALAM -> "മലയാളത്തിലോ ഇംഗ്ലീഷിലോ പറയൂ..."
                                    else -> bi("Speak or type in English...", "ഇംഗ്ലീഷിൽ പറയുകയോ ടൈപ്പ് ചെയ്യുകയോ ചെയ്യൂ...", lang)
                                }
                            )
                        },
                        enabled = isOnline && !isLoading,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        shape = RoundedCornerShape(18.dp)
                    )

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val toSend = textInput
                                textInput = ""
                                onSendMessage(toSend)
                            }
                        },
                        enabled = isOnline && !isLoading && textInput.isNotBlank(),
                        modifier = Modifier.testTag("chat_send_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = if (isOnline && textInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: ChatMessage,
    language: com.example.model.AppLanguage,
    onSpeak: () -> Unit,
    onMalayalamHelp: () -> Unit
) {
    val isLeo = message.isFromLeo
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isLeo) Arrangement.Start else Arrangement.End
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isLeo) 4.dp else 18.dp,
                bottomEnd = if (isLeo) 18.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isLeo) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isLeo) MaterialTheme.colorScheme.onSurface else Color.White
                )

                if (!message.correctionText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = EmeraldContainerLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✨ ${message.correctionText}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (!message.malayalamHelp.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = message.malayalamHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                if (isLeo) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onSpeak) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(bi("Listen", "കേൾക്കാം", language), style = MaterialTheme.typography.labelMedium)
                        }
                        if (message.malayalamHelp.isNullOrBlank()) {
                            TextButton(onClick = onMalayalamHelp) {
                                Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("മലയാളം സഹായം", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
