package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.IeltsAttemptEntity
import com.example.data.local.LearnedWordEntity
import com.example.data.local.PracticeSessionEntity
import com.example.data.local.SoundProgressEntity
import com.example.data.local.SrsItemEntity
import com.example.model.BadgeItem
import com.example.model.EnglishLevel
import com.example.model.LeoMood
import com.example.model.UserProfileState
import com.example.model.WeeklySummaryReport
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.bi
import com.example.ui.theme.DeepGreenContainerLight
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FreezeIce
import com.example.ui.theme.GoldenSun
import com.example.ui.theme.MalabarTeal
import com.example.ui.theme.StreakFlame
import com.example.ui.theme.TangerinePrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(
    profile: UserProfileState,
    sessions: List<PracticeSessionEntity>,
    learnedWords: List<LearnedWordEntity>,
    srsItems: List<SrsItemEntity>,
    soundHistory: List<SoundProgressEntity>,
    ieltsAttempts: List<IeltsAttemptEntity>,
    analyticsCount: Int,
    badges: List<BadgeItem>,
    weeklySummary: WeeklySummaryReport?,
    weeklySummaryLoading: Boolean,
    onGenerateWeeklySummary: () -> Unit,
    onActivateFreeze: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = profile.language

    val weeklyDays = remember(sessions, profile.todayMinutesPracticed) {
        val fmtKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val fmtLabel = SimpleDateFormat("EEE", Locale.US)
        val todayCal = Calendar.getInstance()
        val list = mutableListOf<Pair<String, Int>>()
        for (offset in 6 downTo 0) {
            val cal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
            val key = fmtKey.format(cal.time)
            val label = fmtLabel.format(cal.time)
            val mins = sessions.filter { it.dateKey == key }.sumOf { it.minutesSpent }
            val finalMins = if (offset == 0) maxOf(mins, profile.todayMinutesPracticed) else mins
            list.add(label to finalMins)
        }
        list
    }

    val activeDateKeys = remember(sessions, profile.completedTaskIdsToday) {
        val keys = sessions.map { it.dateKey }.toMutableSet()
        if (profile.completedTaskIdsToday.isNotEmpty()) {
            keys.add(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time))
        }
        keys
    }

    val nextLevel = EnglishLevel.entries.getOrNull(profile.currentLevel.index + 1)
    val levelProgress = if (nextLevel != null) {
        val span = (nextLevel.xpRequired - profile.currentLevel.xpRequired).coerceAtLeast(1)
        ((profile.xp - profile.currentLevel.xpRequired).toFloat() / span).coerceIn(0f, 1f)
    } else 1f

    val masteredSrsCount = remember(srsItems) { srsItems.count { it.repetitions >= 2 } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bi("Your Speaking Progress", "നിങ്ങളുടെ പുരോഗതി", lang),
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = DeepGreenPrimary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LeoSpeechGuide(
                mood = if (profile.streakDays >= 3) LeoMood.CHEERING else LeoMood.HAPPY,
                message = bi(
                    "You've spoken ${weeklyDays.sumOf { it.second }} minutes this week & learned ${learnedWords.size} words!",
                    "ഈ ആഴ്ച ${weeklyDays.sumOf { it.second }} മിനിറ്റ് സംസാരിക്കുകയും ${learnedWords.size} വാക്കുകൾ പഠിക്കുകയും ചെയ്തു!",
                    lang
                )
            )
        }

        // Accent & IELTS snapshot card
        item {
            SpeakUpCard(containerColor = DeepGreenContainerLight, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = DeepGreenPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = bi("Accent & IELTS Tracker", "ഉച്ചാരണവും IELTS പുരോഗതിയും", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DeepGreenPrimary
                        )
                    }
                    val avgSoundScore = if (soundHistory.isNotEmpty()) soundHistory.map { it.score }.average().toInt() else 0
                    Text(
                        text = bi(
                            "Pronunciation drills: ${soundHistory.size} logged (avg $avgSoundScore%) • IELTS attempts: ${ieltsAttempts.size}",
                            "ഉച്ചാരണ പരിശീലനങ്ങൾ: ${soundHistory.size} (ശരാശരി $avgSoundScore%) • IELTS പരീക്ഷകൾ: ${ieltsAttempts.size}",
                            lang
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Est. IELTS Band: ${String.format(Locale.US, "%.1f", profile.ieltsCurrentBand)} / Target ${String.format(Locale.US, "%.1f", profile.ieltsTargetBand)} • AI estimated, not an official score",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF523600)
                    )
                }
            }
        }

        // AI Weekly Summary Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MalabarTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = bi("AI Weekly Summary", "പ്രതിവാര എ.ഐ റിപ്പോർട്ട്", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        if (weeklySummaryLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (weeklySummary != null) {
                        Text(
                            text = weeklySummary.headline,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = weeklySummary.summary, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🎯 ${weeklySummary.nextWeekFocus}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    } else {
                        Text(
                            text = bi(
                                "Get Leo's personalized weekly insight on your speaking minutes, new words, and score trend.",
                                "നിങ്ങളുടെ സംസാര മിനിറ്റുകളും പുരോഗതിയും അടിസ്ഥാനമാക്കി ലിയോയുടെ റിപ്പോർട്ട് കാണുക.",
                                lang
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    OutlinedButton(
                        onClick = onGenerateWeeklySummary,
                        enabled = !weeklySummaryLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (weeklySummary == null) bi("Generate AI Weekly Summary", "എ.ഐ റിപ്പോർട്ട് തയ്യാറാക്കുക", lang)
                            else bi("Refresh Summary", "റിപ്പോർട്ട് പുതുക്കുക", lang)
                        )
                    }
                }
            }
        }

        // Streak Calendar + Streak Freeze Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = StreakFlame, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = bi("${profile.streakDays}-Day Streak", "${profile.streakDays} ദിവസത്തെ സ്ട്രീക്ക്", lang),
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = bi("Last 14 days activity", "കഴിഞ്ഞ 14 ദിവസങ്ങൾ", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val last14Days = remember(activeDateKeys) {
                        val fmtKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val fmtDayNum = SimpleDateFormat("d", Locale.US)
                        val calNow = Calendar.getInstance()
                        (13 downTo 0).map { offset ->
                            val cal = (calNow.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -offset) }
                            val key = fmtKey.format(cal.time)
                            Triple(fmtDayNum.format(cal.time), activeDateKeys.contains(key), offset == 0)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last14Days.take(7).forEach { (dayNum, active, isToday) ->
                            CalendarDayBubble(dayNum = dayNum, active = active, isToday = isToday)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        last14Days.drop(7).forEach { (dayNum, active, isToday) ->
                            CalendarDayBubble(dayNum = dayNum, active = active, isToday = isToday)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        color = FreezeIce.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Filled.AcUnit, contentDescription = null, tint = FreezeIce, modifier = Modifier.size(22.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = bi(
                                            "Streak Freeze (${profile.streakFreezes} left)",
                                            "സ്ട്രീക്ക് ഫ്രീസ് (${profile.streakFreezes} ബാക്കി)",
                                            lang
                                        ),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = if (profile.streakFreezeActive) {
                                            bi("Protected for today!", "ഇന്ന് സുരക്ഷിതമാണ്!", lang)
                                        } else {
                                            bi("Protects your streak if you miss a day", "ഒരു ദിവസം മുടങ്ങിയാലും സ്ട്രീക്ക് നഷ്ടപ്പെടില്ല", lang)
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (!profile.streakFreezeActive && profile.streakFreezes > 0) {
                                Button(
                                    onClick = onActivateFreeze,
                                    colors = ButtonDefaults.buttonColors(containerColor = FreezeIce)
                                ) {
                                    Text(bi("Use", "ഉപയോഗിക്കുക", lang))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Weekly Speaking Minutes Chart
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = bi("Weekly Speaking Minutes", "ആഴ്ചയിലെ സംസാര മിനിറ്റുകൾ", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val maxVal = maxOf(profile.dailyMinutesGoal, weeklyDays.maxOfOrNull { it.second } ?: 10, 10)
                    val barColor = TangerinePrimary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val count = weeklyDays.size
                        val slotW = size.width / count
                        val barW = slotW * 0.48f
                        weeklyDays.forEachIndexed { idx, (_, mins) ->
                            val x = idx * slotW + (slotW - barW) / 2f
                            drawRoundRect(
                                color = trackColor,
                                topLeft = Offset(x, 0f),
                                size = Size(barW, size.height),
                                cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                            )
                            val fillRatio = (mins.toFloat() / maxVal).coerceIn(0f, 1f)
                            val fillH = (size.height * fillRatio).coerceAtLeast(if (mins > 0) 10.dp.toPx() else 0f)
                            if (fillH > 0f) {
                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(x, size.height - fillH),
                                    size = Size(barW, fillH),
                                    cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        weeklyDays.forEach { (day, mins) ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${mins}m",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Level & SM-2 Spaced Repetition Progress
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = bi("Level & SM-2 Memory Progress", "ലെവലും SM-2 ഓർമ്മ പുരോഗതിയും", lang),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = bi(profile.currentLevel.titleEn, profile.currentLevel.titleMl, lang),
                            style = MaterialTheme.typography.titleLarge,
                            color = MalabarTeal
                        )
                        Text(
                            text = "${profile.xp} XP",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = bi(
                            "SM-2 tracked: ${srsItems.size} items ($masteredSrsCount mastered) • $analyticsCount events logged",
                            "SM-2 വാക്കുകൾ: ${srsItems.size} ($masteredSrsCount ഉറപ്പിച്ചു) • $analyticsCount ഇവന്റുകൾ",
                            lang
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Words Learned
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = MalabarTeal)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bi("Words Learned (${learnedWords.size})", "പഠിച്ച വാക്കുകൾ (${learnedWords.size})", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (learnedWords.isEmpty()) {
                        Text(
                            text = bi(
                                "Complete 'Word of the Day' to collect spoken words here!",
                                "ഇന്നത്തെ 5 വാക്കുകൾ പൂർത്തിയാക്കുമ്പോൾ വാക്കുകൾ ഇവിടെ കാണാം!",
                                lang
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            learnedWords.take(15).forEach { w ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "${w.word} • ${w.malayalamMeaning}",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Badges Grid
        item {
            Text(
                text = bi("Badges", "ബാഡ്ജുകൾ", lang),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                badges.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { badge ->
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                border = if (badge.unlocked) BorderStroke(1.5.dp, GoldenSun) else null,
                                colors = CardDefaults.cardColors(
                                    containerColor = if (badge.unlocked) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = if (badge.unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = if (badge.unlocked) GoldenSun else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = bi(badge.titleEn, badge.titleMl, lang),
                                        style = MaterialTheme.typography.titleSmall,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = bi(badge.descEn, badge.descMl, lang),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun CalendarDayBubble(
    dayNum: String,
    active: Boolean,
    isToday: Boolean
) {
    Surface(
        shape = CircleShape,
        color = when {
            active -> EmeraldSuccess
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        border = if (isToday) BorderStroke(2.dp, TangerinePrimary) else null,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (active) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(dayNum, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
