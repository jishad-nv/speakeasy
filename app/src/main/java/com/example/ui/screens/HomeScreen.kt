package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.DailyPlanState
import com.example.model.DailyTaskType
import com.example.model.LeoMood
import com.example.model.LeoPersonality
import com.example.model.SupportLevel
import com.example.model.UserProfileState
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.OfflineStatusBanner
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.adaptiveInstruction
import com.example.ui.components.bi
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FreezeIce
import com.example.ui.theme.GoldenSun
import com.example.ui.theme.MalabarTeal
import com.example.ui.theme.SoftYellowHighlight
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.TangerinePrimary
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    profile: UserProfileState,
    isOnline: Boolean,
    dailyPlan: DailyPlanState,
    dailyPlanLoading: Boolean,
    pendingSyncCount: Int,
    dueSrsCount: Int,
    unfixedMistakesCount: Int,
    adaptiveBanner: String?,
    onDismissAdaptiveBanner: () -> Unit,
    onRefreshDailyPlan: () -> Unit,
    onToggleLanguage: () -> Unit,
    onStartPractice: (DailyTaskType) -> Unit,
    onOpenPath: () -> Unit,
    onOpenIelts: () -> Unit,
    onOpenMistakes: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = profile.language
    val completedCount = profile.completedTaskIdsToday.size
    val totalTasks = DailyTaskType.entries.size
    val nextTask = DailyTaskType.entries.firstOrNull { !profile.completedTaskIdsToday.contains(it.id) }
        ?: DailyTaskType.SHADOW_SPEAKING

    val greetingMessage = when {
        completedCount >= totalTasks -> when (profile.leoPersonality) {
            LeoPersonality.FRIENDLY -> bi("All 5 tasks done today! You're unstoppable!", "ഇന്നത്തെ 5 ടാസ്ക്കുകളും പൂർത്തിയാക്കി! മിടുക്കൻ!", lang)
            LeoPersonality.STRICT_COACH -> bi("5 out of 5 complete. Consistency builds mastery.", "5-ൽ 5 പൂർത്തിയായി. ഈ തുടർച്ച നിലനിർത്തുക.", lang)
            LeoPersonality.FUNNY -> bi("5/5! Leo is doing a happy victory dance right now!", "5/5! ലിയോ സന്തോഷം കൊണ്ട് തുള്ളിച്ചാടുന്നു!", lang)
        }
        profile.missedDays >= 2 -> adaptiveInstruction(
            profile.supportLevel,
            fullMl = "തിരികെ സ്വാഗതം! സമ്മർദ്ദമില്ലാതെ 2 മിനിറ്റ് സംസാരിച്ച് നമുക്ക് വീണ്ടും തുടങ്ങാം.",
            mixed = "Welcome back! No stress—just 2 minutes of speaking gets your voice flowing.",
            simpleEn = "Welcome back! One quick 2-minute drill gets your voice flowing again."
        )
        completedCount == 0 -> adaptiveInstruction(
            profile.supportLevel,
            fullMl = "ഹായ്! ഇന്ന് ${profile.dailyMinutesGoal} മിനിറ്റ് പതുക്കെ ഇംഗ്ലീഷ് സംസാരിക്കാൻ തുടങ്ങാം!",
            mixed = "Ready to warm up? Let's speak English for ${profile.dailyMinutesGoal} mins today!",
            simpleEn = "Ready to speak? Start your ${profile.dailyMinutesGoal}-minute daily flow now!"
        )
        else -> bi(
            "Awesome! $completedCount of $totalTasks done today. Keep rolling!",
            "കൊള്ളാം! ഇന്ന് $completedCount / $totalTasks കഴിഞ്ഞു. തുടരാം!",
            lang
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header row: Brand + Level chip + EN/ML toggle + Settings
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SpeakUp",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = DeepGreenPrimary
                    )
                    Text(
                        text = "${bi(profile.currentLevel.titleEn, profile.currentLevel.titleMl, lang)} • ${profile.supportLevel.titleEn}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.clickable(onClick = onOpenPath)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = lang == AppLanguage.MALAYALAM,
                        onClick = onToggleLanguage,
                        label = { Text(if (lang == AppLanguage.MALAYALAM) "മലയാളം" else "EN / ML") },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Translate,
                                contentDescription = "Language toggle",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("home_language_toggle")
                    )
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.testTag("home_settings_button")
                    ) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = DeepGreenPrimary
                        )
                    }
                }
            }
        }

        // Offline / Online Banner + Pending Sync Badge
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OfflineStatusBanner(isOnline = isOnline, language = lang)
                if (pendingSyncCount > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.CloudSync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bi(
                                    "$pendingSyncCount offline session(s) queued — will auto-sync online",
                                    "$pendingSyncCount ഓഫ്‌ലൈൻ പരിശീലനങ്ങൾ സിങ്ക് ചെയ്യാൻ കാത്തിരിക്കുന്നു",
                                    lang
                                ),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Adaptive Difficulty Notification Banner
        if (!adaptiveBanner.isNullOrBlank()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDismissAdaptiveBanner() }
                ) {
                    Text(
                        text = "⚡ $adaptiveBanner",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        // Leo Greeting
        item {
            LeoSpeechGuide(
                mood = if (completedCount >= totalTasks) LeoMood.CHEERING else LeoMood.HAPPY,
                message = greetingMessage,
                subMessage = bi(
                    "Goal: ${profile.goal.titleEn} • ${profile.dailyMinutesGoal} min/day",
                    "ലക്ഷ്യം: ${profile.goal.titleMl} • ${profile.dailyMinutesGoal} മിനിറ്റ്/ദിവസം",
                    lang
                )
            )
        }

        // Mistake Notebook proactive nudge
        if (unfixedMistakesCount > 0) {
            item {
                SpeakUpCard(
                    containerColor = SoftYellowHighlight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_mistake_nudge"),
                    onClick = onOpenMistakes
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.BookmarkAdded, contentDescription = null, tint = Color(0xFF7A4B00))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bi(
                                    "You made this mistake earlier ($unfixedMistakesCount active) — let's try again!",
                                    "കഴിഞ്ഞ ആഴ്ച തെറ്റിയ $unfixedMistakesCount ഭാഗങ്ങൾ ഒന്നുകൂടി ശരിയാക്കി നോക്കാം!",
                                    lang
                                ),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF523600)
                            )
                            Text(
                                text = bi("Tap to open your Mistake Notebook drills", "മിസ്റ്റേക്ക് നോട്ട്ബുക്ക് തുറക്കൂ", lang),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF7A4B00)
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF7A4B00))
                    }
                }
            }
        }

        // Progress Ring + Streak Flame + XP Dashboard Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val targetFraction = (completedCount.toFloat() / totalTasks).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetFraction,
                        animationSpec = tween(650),
                        label = "ring"
                    )
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    val activeColor = if (completedCount >= totalTasks) EmeraldSuccess else TangerinePrimary

                    Box(
                        modifier = Modifier.size(92.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = trackColor,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = activeColor,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedProgress,
                                useCenter = false,
                                style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$completedCount/$totalTasks",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = bi("Today", "ഇന്ന്", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatPill(
                                icon = Icons.Filled.LocalFireDepartment,
                                iconTint = StreakFlame,
                                value = "${profile.streakDays}d",
                                label = bi("Streak", "സ്ട്രീക്ക്", lang),
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                icon = Icons.Filled.Bolt,
                                iconTint = GoldenSun,
                                value = "${profile.xp}",
                                label = "XP",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = bi(
                                    "${profile.todayMinutesPracticed}/${profile.dailyMinutesGoal} min • $dueSrsCount SRS due",
                                    "${profile.todayMinutesPracticed}/${profile.dailyMinutesGoal} മിനിറ്റ് • $dueSrsCount റിവ്യൂ",
                                    lang
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (profile.streakFreezeActive) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.AcUnit,
                                        contentDescription = "Streak freeze active",
                                        tint = FreezeIce,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = bi("Freeze", "ഫ്രീസ്", lang),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = FreezeIce
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Personalized Daily Plan Card (AI + SM-2 Weak Words)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MalabarTeal, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (dailyPlan.isAiGenerated) {
                                    bi("AI Smart Daily Plan", "എ.ഐ സ്മാർട്ട് പ്ലാൻ", lang)
                                } else {
                                    bi("Today's Adaptive Plan", "ഇന്നത്തെ പഠന പ്ലാൻ", lang)
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (dailyPlanLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = onRefreshDailyPlan, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Filled.Refresh, contentDescription = "Refresh daily plan", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (dailyPlan.focusNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dailyPlan.focusNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    if (dailyPlan.weakWordsInFocus.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            dailyPlan.weakWordsInFocus.take(3).forEach { w ->
                                AssistChip(
                                    onClick = { onStartPractice(DailyTaskType.WORD_OF_THE_DAY) },
                                    label = { Text("SM-2: $w", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Big Start Today's Practice Hero CTA (Deep Green primary action button)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(DeepGreenPrimary, DeepGreenLight)
                        )
                    )
                    .clickable { onStartPractice(nextTask) }
                    .padding(18.dp)
                    .testTag("start_todays_practice_button")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (completedCount >= totalTasks) {
                                bi("Practice Again", "വീണ്ടും പരിശീലിക്കാം", lang)
                            } else {
                                bi("Start Today's Practice", "ഇന്നത്തെ പരിശീലനം തുടങ്ങാം", lang)
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = bi(
                                "Up next: ${nextTask.titleEn} (${nextTask.durationLabel})",
                                "അടുത്തത്: ${nextTask.titleMl} (${nextTask.durationLabel})",
                                lang
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Start practice",
                            tint = TangerinePrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Daily Tasks Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bi("Today's 5 Speaking Tasks", "ഇന്നത്തെ 5 സംസാര ടാസ്ക്കുകൾ", lang),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "$completedCount / $totalTasks",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 5 Daily Tasks
        items(DailyTaskType.entries, key = { it.id }) { task ->
            val done = profile.completedTaskIdsToday.contains(task.id)
            val customPlanItem = dailyPlan.items.firstOrNull { it.taskType == task }
            DailyTaskCard(
                task = task,
                subtitleOverride = customPlanItem?.customPrompt?.takeIf { it.isNotBlank() },
                completed = done,
                language = lang,
                onClick = { onStartPractice(task) }
            )
        }

        // Separate IELTS Preparation Section on Home
        item {
            Spacer(modifier = Modifier.height(4.dp))
            SpeakUpCard(
                containerColor = DeepGreenContainerLight,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_ielts_section_card"),
                onClick = onOpenIelts
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DeepGreenPrimary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Filled.WorkspacePremium,
                            contentDescription = "IELTS Prep",
                            tint = Color.White,
                            modifier = Modifier.padding(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = bi("IELTS Preparation Track", "IELTS പരിശീലന വിഭാഗം", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepGreenPrimary
                        )
                        Text(
                            text = bi(
                                "Target Band ${String.format(Locale.US, "%.1f", profile.ieltsTargetBand)} • Est. ${String.format(Locale.US, "%.1f", profile.ieltsCurrentBand)} (AI estimated, not an official score)",
                                "ലക്ഷ്യം ബാൻഡ് ${String.format(Locale.US, "%.1f", profile.ieltsTargetBand)} • Speaking, Writing, Listening, Reading",
                                lang
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = DeepGreenPrimary)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatPill(
    icon: ImageVector,
    iconTint: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DailyTaskCard(
    task: DailyTaskType,
    subtitleOverride: String?,
    completed: Boolean,
    language: AppLanguage,
    onClick: () -> Unit
) {
    val (icon, tint) = when (task) {
        DailyTaskType.SHADOW_SPEAKING -> Icons.Filled.GraphicEq to TangerinePrimary
        DailyTaskType.WORD_OF_THE_DAY -> Icons.Filled.School to MalabarTeal
        DailyTaskType.SITUATION_ROLEPLAY -> Icons.Filled.Forum to Color(0xFF7B4AE2)
        DailyTaskType.LISTEN_AND_ANSWER -> Icons.Filled.Headphones to Color(0xFF0284C7)
        DailyTaskType.CHALLENGE -> Icons.Filled.AutoAwesome to GoldenSun
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("daily_task_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        border = if (completed) BorderStroke(1.5.dp, EmeraldSuccess.copy(alpha = 0.55f)) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (completed) EmeraldSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (completed) 0.dp else 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = tint.copy(alpha = 0.14f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bi(task.titleEn, task.titleMl, language),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = subtitleOverride ?: "${task.durationLabel} • +${task.xpReward} XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (completed) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Completed",
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open task",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
