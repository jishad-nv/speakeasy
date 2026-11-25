package com.example.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.LearningGoal
import com.example.model.LeoMood
import com.example.model.LeoPersonality
import com.example.model.SourceLanguageRegistry
import com.example.model.SupportLevel
import com.example.model.ThemeMode
import com.example.model.UserProfileState
import com.example.notifications.SpeakUpNotificationHelper
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.bi
import com.example.ui.theme.EmeraldSuccess
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    profile: UserProfileState,
    offlinePacksCount: Int,
    offlineDrillsCount: Int,
    zeroPhrasesCount: Int,
    ieltsStarterCount: Int,
    downloadProgress: Float?,
    onSetLanguage: (AppLanguage) -> Unit,
    onSetSupportLevel: (SupportLevel) -> Unit,
    onSetSourceLanguageCode: (String) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetPersonality: (LeoPersonality) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onSetLearningGoal: (LearningGoal) -> Unit,
    onUpdateReminders: (List<String>, Boolean) -> Unit,
    onSendTestNotification: () -> Unit,
    onDownloadOffline: () -> Unit,
    onSetSoundEffects: (Boolean) -> Unit,
    onRetakePlacement: () -> Unit,
    onExportProgress: () -> Unit,
    onResetProgress: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lang = profile.language
    var showResetConfirm by remember { mutableStateOf(false) }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onUpdateReminders(profile.reminderTimes, granted)
        if (granted) onSendTestNotification()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(
                    text = bi("Settings", "ക്രമീകരണങ്ങൾ", lang),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LeoSpeechGuide(
                mood = LeoMood.HAPPY,
                message = bi(
                    "Customize Leo's Malayalam support level, reminders, 30-day offline packs, and progress data!",
                    "മലയാളം സഹായ നിലവാരവും സമയവും 30-ദിവസ ഓഫ്‌ലൈൻ പാഠങ്ങളും ഇവിടെ ക്രമീകരിക്കാം!",
                    lang
                )
            )
        }

        // Language & Dark Mode
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(bi("App Language", "ആപ്പ് ഭാഷ", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppLanguage.entries.forEach { option ->
                            FilterChip(
                                selected = profile.language == option,
                                onClick = { onSetLanguage(option) },
                                label = { Text("${option.label} (${option.nativeLabel})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = bi("Malayalam Teaching Support Level", "പഠന സഹായ നിലവാരം", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SupportLevel.entries.forEach { sup ->
                            FilterChip(
                                selected = profile.supportLevel == sup,
                                onClick = { onSetSupportLevel(sup) },
                                label = { Text(bi(sup.titleEn, sup.titleMl, lang)) },
                                modifier = Modifier.testTag("support_level_${sup.name.lowercase()}")
                            )
                        }
                    }
                    Text(
                        text = bi(profile.supportLevel.descEn, profile.supportLevel.descMl, lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = bi("Tutor Language Config (Extensible)", "മാതൃഭാഷ കോൺഫിഗ്", lang),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SourceLanguageRegistry.allConfigs.forEach { cfg ->
                            FilterChip(
                                selected = profile.sourceLanguageCode == cfg.code,
                                onClick = { onSetSourceLanguageCode(cfg.code) },
                                label = { Text("${cfg.nativeName} (${cfg.displayName})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(bi("Appearance", "തീം", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = profile.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(bi(mode.labelEn, mode.labelMl, lang)) },
                                modifier = Modifier.testTag("theme_${mode.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }

        // Leo's Personality
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(bi("Leo's Personality", "ലിയോയുടെ സ്വഭാവം", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LeoPersonality.entries.forEach { p ->
                            FilterChip(
                                selected = profile.leoPersonality == p,
                                onClick = { onSetPersonality(p) },
                                label = { Text(bi(p.titleEn, p.titleMl, lang)) },
                                modifier = Modifier.testTag("personality_${p.name.lowercase()}")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = bi(profile.leoPersonality.subtitleEn, profile.leoPersonality.subtitleMl, lang),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Daily Goal & Learning Focus
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(bi("Daily Speaking Goal", "ദിവസേനയുള്ള ലക്ഷ്യം", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(5, 10, 15, 20).forEach { mins ->
                            FilterChip(
                                selected = profile.dailyMinutesGoal == mins,
                                onClick = { onSetDailyGoal(mins) },
                                label = { Text("${mins}m") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(bi("Focus Area", "പരിശീലന മേഖല", lang), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LearningGoal.entries.forEach { goal ->
                            FilterChip(
                                selected = profile.goal == goal,
                                onClick = { onSetLearningGoal(goal) },
                                label = { Text(bi(goal.titleEn, goal.titleMl, lang)) }
                            )
                        }
                    }
                }
            }
        }

        // Reminders & Notifications
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(bi("Smart Reminders (Max 3/day)", "സ്മാർട്ട് ഓർമ്മപ്പെടുത്തൽ", lang), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Switch(
                            checked = profile.notificationsEnabled,
                            onCheckedChange = { enable ->
                                if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                    !SpeakUpNotificationHelper.hasNotificationPermission(context)
                                ) {
                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    onUpdateReminders(profile.reminderTimes, enable)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bi("Times (${profile.reminderTimes.size}/3)", "സമയങ്ങൾ (${profile.reminderTimes.size}/3)", lang),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        profile.reminderTimes.forEach { time ->
                            FilterChip(
                                selected = true,
                                onClick = {
                                    val updated = profile.reminderTimes.filterNot { it == time }
                                    onUpdateReminders(updated, profile.notificationsEnabled)
                                },
                                label = { Text(time) },
                                trailingIcon = {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                        if (profile.reminderTimes.size < 3) {
                            OutlinedButton(
                                onClick = {
                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            val formatted = String.format(Locale.US, "%02d:%02d", hour, minute)
                                            if (!profile.reminderTimes.contains(formatted)) {
                                                onUpdateReminders(profile.reminderTimes + formatted, profile.notificationsEnabled)
                                            }
                                        },
                                        19,
                                        0,
                                        true
                                    ).show()
                                }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(bi("Add", "ചേർക്കുക", lang))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                !SpeakUpNotificationHelper.hasNotificationPermission(context)
                            ) {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onSendTestNotification()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(bi("Preview Leo Notification (with Actions)", "ലിയോ നോട്ടിഫിക്കേഷൻ കാണുക", lang))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(bi("Celebration Sound Effects", "ആഘോഷ ശബ്ദങ്ങൾ", lang), style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = profile.soundEffectsEnabled,
                            onCheckedChange = onSetSoundEffects
                        )
                    }
                }
            }
        }

        // Offline 30-Day Pack + Export / Reset Progress + Retake Placement
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (profile.offlineDownloaded) Icons.Filled.CheckCircle else Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = if (profile.offlineDownloaded) EmeraldSuccess else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bi("30-Day Offline Lessons Pack", "30-ദിവസ ഓഫ്‌ലൈൻ പാഠങ്ങൾ", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (profile.offlineDownloaded) {
                                    bi(
                                        "$offlinePacksCount levels • $offlineDrillsCount drills • $zeroPhrasesCount Zero phrases • $ieltsStarterCount IELTS packs offline!",
                                        "$offlinePacksCount ലെവലുകൾ • $offlineDrillsCount പാഠങ്ങൾ • $zeroPhrasesCount തുടക്ക വാക്യങ്ങൾ • $ieltsStarterCount IELTS പാക്കുകൾ തയ്യാർ!",
                                        lang
                                    )
                                } else {
                                    bi("Save 30 days of tasks + 300 Zero phrases + IELTS starter packs.", "30 ദിവസത്തെ പാഠങ്ങളും IELTS പാക്കുകളും സേവ് ചെയ്യുക.", lang)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (downloadProgress != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDownloadOffline,
                        enabled = downloadProgress == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("download_offline_button")
                    ) {
                        Text(
                            if (profile.offlineDownloaded) {
                                bi("Refresh 30-Day Offline Pack", "30-ദിവസ ഓഫ്‌ലൈൻ പാക്ക് പുതുക്കുക", lang)
                            } else {
                                bi("Download 30-Day Offline Pack", "30-ദിവസ ഓഫ്‌ലൈൻ പാക്ക് ഡൗൺലോഡ് ചെയ്യുക", lang)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onRetakePlacement,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(bi("Retake 5-Question Spoken Test", "സംസാര പരീക്ഷ വീണ്ടും എഴുതുക", lang))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExportProgress,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_progress_button")
                        ) {
                            Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(bi("Export", "എക്സ്പോർട്ട്", lang))
                        }
                        OutlinedButton(
                            onClick = { showResetConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reset_progress_button")
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(bi("Reset", "റീസെറ്റ്", lang))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(bi("Reset All Progress?", "പുരോഗതി മായ്ക്കണോ?", lang)) },
            text = {
                Text(
                    bi(
                        "This clears your XP, streak, learned words, and SM-2 review history on this device.",
                        "നിങ്ങളുടെ XP, സ്ട്രീക്ക്, പഠിച്ച വാക്കുകൾ എന്നിവ ഈ ഫോണിൽ നിന്ന് മായ്ക്കും.",
                        lang
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirm = false
                        onResetProgress()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(bi("Reset", "മായ്ക്കുക", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text(bi("Cancel", "വേണ്ട", lang))
                }
            }
        )
    }
}
