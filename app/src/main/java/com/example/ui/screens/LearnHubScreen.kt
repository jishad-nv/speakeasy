package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.LeoMood
import com.example.model.SupportLevel
import com.example.model.UserProfileState
import com.example.ui.components.LeoSpeechGuide
import com.example.ui.components.SpeakUpCard
import com.example.ui.components.adaptiveInstruction
import com.example.ui.components.bi
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.LeoOrangeAccent
import com.example.ui.theme.SoftYellowHighlight

enum class LearnDestination {
    HUB,
    ABSOLUTE_ZERO,
    MALAYALAM_TO_ENGLISH,
    ACCENT_COACH,
    MISTAKE_NOTEBOOK,
    CAMERA_LEARN,
    SIMULATORS,
    LEVEL_PATH
}

@Composable
fun LearnHubScreen(
    profile: UserProfileState,
    unfixedMistakesCount: Int,
    savedWordsCount: Int,
    onNavigate: (LearnDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = profile.language
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = bi("Learn & Practice Labs", "പഠന കേന്ദ്രം", lang),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LeoSpeechGuide(
                mood = LeoMood.HAPPY,
                message = adaptiveInstruction(
                    supportLevel = profile.supportLevel,
                    fullMl = "ഇംഗ്ലീഷ് പൂജ്യത്തിൽ നിന്ന് തുടങ്ങാനും ഉച്ചാരണം ശരിയാക്കാനും താഴെയുള്ള ടൂളുകൾ ഉപയോഗിക്കൂ!",
                    mixed = "Start from Absolute Zero phrases, fix Malayali accent sounds, or scan signs with Camera Learn!",
                    simpleEn = "Choose a speaking lab below—practice accent sounds, real-life scenes, or review mistakes."
                ),
                subMessage = bi(
                    "Support: ${profile.supportLevel.titleEn}",
                    "സഹായ നില: ${profile.supportLevel.titleMl}",
                    lang
                )
            )
        }

        item {
            LearnFeatureCard(
                title = bi("Level 0: Absolute Zero (300 Phrases)", "ലെവൽ 0: തുടക്കം (300 വാക്യങ്ങൾ)", lang),
                subtitle = bi(
                    "Malayalam meanings + Manglish pronunciation script + slow play",
                    "മലയാളം അർത്ഥവും ഉച്ചാരണവും പതുക്കെ കേൾക്കാനും",
                    lang
                ),
                icon = Icons.AutoMirrored.Filled.MenuBook,
                tint = DeepGreenPrimary,
                badgeText = "300",
                testTag = "learn_card_zero",
                onClick = { onNavigate(LearnDestination.ABSOLUTE_ZERO) }
            )
        }

        item {
            LearnFeatureCard(
                title = bi("Say in Malayalam -> Learn in English", "മലയാളത്തിൽ പറയൂ, ഇംഗ്ലീഷിൽ പഠിക്കൂ", lang),
                subtitle = bi(
                    "Formal & casual translations, reverse mode & daily speaking journal",
                    "ഔപചാരിക/സാധാരണ ഇംഗ്ലീഷ്, റിവേഴ്സ് മോഡ്, ഡെയ്‌ലി ജേർണൽ",
                    lang
                ),
                icon = Icons.Filled.Translate,
                tint = LeoOrangeAccent,
                badgeText = "ml → en",
                testTag = "learn_card_translate",
                onClick = { onNavigate(LearnDestination.MALAYALAM_TO_ENGLISH) }
            )
        }

        item {
            LearnFeatureCard(
                title = bi("Malayali-Accent Pronunciation Coach", "ഉച്ചാരണ പരിശീലകൻ (Audio AI)", lang),
                subtitle = bi(
                    "TH/DH, V/W, F/P, Z & vowel drills + mouth diagrams + before/after",
                    "വായയുടെ ചിത്രം, ജോഡി വാക്കുകൾ, മുൻപും ശേഷവും താരതമ്യം",
                    lang
                ),
                icon = Icons.Filled.RecordVoiceOver,
                tint = DeepGreenPrimary,
                badgeText = "Audio",
                testTag = "learn_card_accent",
                onClick = { onNavigate(LearnDestination.ACCENT_COACH) }
            )
        }

        item {
            LearnFeatureCard(
                title = bi("Personal Mistake Notebook", "തെറ്റുകളുടെ നോട്ട്ബുക്ക് (AI Memory)", lang),
                subtitle = bi(
                    "Leo remembers recurring mistakes and helps you fix them",
                    "നിങ്ങളുടെ പ്രധാന തെറ്റുകൾ തിരുത്തി ഉറപ്പിക്കാൻ",
                    lang
                ),
                icon = Icons.Filled.BookmarkAdded,
                tint = Color(0xFFD9534F),
                badgeText = if (unfixedMistakesCount > 0) "$unfixedMistakesCount active" else "Clean!",
                testTag = "learn_card_mistakes",
                onClick = { onNavigate(LearnDestination.MISTAKE_NOTEBOOK) }
            )
        }

        item {
            LearnFeatureCard(
                title = bi("Camera Learn (Photo -> Words)", "ക്യാമറ പഠനം (ഫോട്ടോ -> വാക്കുകൾ)", lang),
                subtitle = bi(
                    "Snap menus, signs, bills to learn Malayalam meanings & save to SM-2 ($savedWordsCount saved)",
                    "ബോർഡുകളോ മെനുവോ ഫോട്ടോ എടുത്ത് വാക്കുകൾ പഠിക്കൂ",
                    lang
                ),
                icon = Icons.Filled.CameraAlt,
                tint = DeepGreenPrimary,
                badgeText = "Vision",
                testTag = "learn_card_camera",
                onClick = { onNavigate(LearnDestination.CAMERA_LEARN) }
            )
        }

        item {
            LearnFeatureCard(
                title = bi("Real-Life Simulators (10 Scenes)", "യഥാർത്ഥ ജീവിത സന്ദർഭങ്ങൾ (10)", lang),
                subtitle = bi(
                    "Phone, interview, doctor, airport & more with Surprise Me & Pressure timer",
                    "വ്യത്യസ്ത ശബ്ദങ്ങളും ടൈമറും സർപ്രൈസ് ട്വിസ്റ്റുകളും",
                    lang
                ),
                icon = Icons.Filled.TheaterComedy,
                tint = LeoOrangeAccent,
                badgeText = "10 Voices",
                testTag = "learn_card_simulators",
                onClick = { onNavigate(LearnDestination.SIMULATORS) }
            )
        }

        item {
            LearnFeatureCard(
                title = bi("6-Level Winding Speaking Path", "6 ലെവൽ പഠന പാത", lang),
                subtitle = bi(
                    "View milestones from Beginner to Master (${profile.xp} XP)",
                    "തുടക്കക്കാരൻ മുതൽ മാസ്റ്റർ വരെയുള്ള പാത",
                    lang
                ),
                icon = Icons.Filled.Map,
                tint = Color(0xFF0E7C7B),
                badgeText = profile.currentLevel.titleEn,
                testTag = "learn_card_path",
                onClick = { onNavigate(LearnDestination.LEVEL_PATH) }
            )
        }
    }
}

@Composable
private fun LearnFeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    badgeText: String,
    testTag: String,
    onClick: () -> Unit
) {
    SpeakUpCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tint.copy(alpha = 0.14f),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(11.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        color = SoftYellowHighlight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF523600),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
