package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DailyTaskType
import com.example.model.EnglishLevel
import com.example.model.LeoMood
import com.example.model.UserProfileState
import com.example.ui.components.LeoMascotAvatar
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.bi
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldenSun
import com.example.ui.theme.TangerinePrimary

@Composable
fun LevelPathScreen(
    profile: UserProfileState,
    onSelectLevel: (EnglishLevel) -> Unit,
    onPracticeLevelTask: (EnglishLevel, DailyTaskType) -> Unit,
    onPreviewCelebration: (EnglishLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = profile.language
    val levels = EnglishLevel.entries
    val nextLevel = levels.getOrNull(profile.currentLevel.index + 1)
    val xpProgress = if (nextLevel != null) {
        val span = (nextLevel.xpRequired - profile.currentLevel.xpRequired).coerceAtLeast(1)
        ((profile.xp - profile.currentLevel.xpRequired).toFloat() / span).coerceIn(0f, 1f)
    } else 1f

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = bi("Your Speaking Path", "നിങ്ങളുടെ പഠന പാത", lang),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LeoSpeechGuide(
                mood = LeoMood.HAPPY,
                message = bi(
                    "6 levels from Beginner to Master. Speak daily to unlock every milestone!",
                    "തുടക്കക്കാരൻ മുതൽ മാസ്റ്റർ വരെ 6 ലെവലുകൾ. എല്ലാ ദിവസവും സംസാരിച്ച് മുന്നേറൂ!",
                    lang
                )
            )
        }

        // XP Bar Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = bi(profile.currentLevel.titleEn, profile.currentLevel.titleMl, lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Bolt, contentDescription = null, tint = GoldenSun, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (nextLevel != null) "${profile.xp} / ${nextLevel.xpRequired} XP" else "${profile.xp} XP (MAX)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                    if (nextLevel != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = bi(
                                "${(nextLevel.xpRequired - profile.xp).coerceAtLeast(0)} XP to unlock ${nextLevel.titleEn}",
                                "${nextLevel.titleMl} തുറക്കാൻ ${(nextLevel.xpRequired - profile.xp).coerceAtLeast(0)} XP കൂടി വേണം",
                                lang
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Winding Path Nodes
        itemsIndexed(levels, key = { _, lvl -> lvl.index }) { idx, lvl ->
            val unlocked = profile.xp >= lvl.xpRequired || lvl.index <= profile.currentLevel.index
            val isCurrent = lvl == profile.currentLevel
            val completed = lvl.index < profile.currentLevel.index

            // Alternating winding horizontal offset
            val horizontalOffset = when (idx % 4) {
                0 -> (-36).dp
                1 -> 28.dp
                2 -> 42.dp
                else -> (-24).dp
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (idx > 0) {
                    val pathColor = if (unlocked) TangerinePrimary else MaterialTheme.colorScheme.outline
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    ) {
                        drawLine(
                            color = pathColor,
                            start = Offset(size.width / 2f, 0f),
                            end = Offset(size.width / 2f, size.height),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = horizontalOffset),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.84f)
                            .clickable {
                                if (unlocked) {
                                    onSelectLevel(lvl)
                                }
                            }
                            .testTag("level_node_${lvl.name.lowercase()}"),
                        shape = RoundedCornerShape(22.dp),
                        border = when {
                            isCurrent -> BorderStroke(2.5.dp, TangerinePrimary)
                            completed -> BorderStroke(1.5.dp, EmeraldSuccess.copy(alpha = 0.6f))
                            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                unlocked -> MaterialTheme.colorScheme.surface
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    completed -> EmeraldSuccess
                                    isCurrent -> TangerinePrimary
                                    unlocked -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    when {
                                        completed -> Icon(Icons.Filled.Check, contentDescription = "Completed", tint = Color.White)
                                        !unlocked -> Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.White)
                                        else -> Text(
                                            text = "${lvl.index + 1}",
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bi(lvl.titleEn, lvl.titleMl, lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = bi(lvl.taglineEn, lvl.taglineMl, lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (unlocked) {
                                        bi("Unlocked • ${lvl.xpRequired}+ XP", "തുറന്നു • ${lvl.xpRequired}+ XP", lang)
                                    } else {
                                        bi("Unlocks at ${lvl.xpRequired} XP", "${lvl.xpRequired} XP-യിൽ തുറക്കും", lang)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (unlocked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isCurrent) {
                                LeoMascotAvatar(mood = LeoMood.CHEERING, size = 48.dp)
                            } else if (unlocked) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = "Practice level",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (isCurrent) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onPracticeLevelTask(lvl, DailyTaskType.SHADOW_SPEAKING) }
                        ) {
                            Text(bi("Practice ${lvl.titleEn}", "${lvl.titleMl} പരിശീലിക്കാം", lang))
                        }
                    }
                }
            }
        }

        // Preview Level-Up Celebration button so user can test level-up animation anytime
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val target = levels.getOrNull(profile.currentLevel.index + 1) ?: EnglishLevel.MASTER
                        onPreviewCelebration(target)
                    }
            ) {
                Text(
                    text = bi("Preview Level-Up Celebration", "ലെവൽ-അപ്പ് ആഘോഷം കാണുക", lang),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
