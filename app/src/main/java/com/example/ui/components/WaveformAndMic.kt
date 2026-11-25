package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.ui.theme.EmeraldContainerLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldenSun
import com.example.ui.theme.TangerineLight
import com.example.ui.theme.TangerinePrimary
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun OfflineStatusBanner(
    isOnline: Boolean,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isOnline) {
        EmeraldSuccess.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = if (isOnline) {
        EmeraldSuccess
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Filled.CloudDone else Icons.Filled.CloudOff,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isOnline) {
                    bi("Online • All lessons & AI Coach ready", "ഓൺലൈൻ • പാഠങ്ങളും എ.ഐ കോച്ചും തയ്യാർ", language)
                } else {
                    bi("Offline • Daily tasks work offline (AI needs internet)", "ഓഫ്‌ലൈൻ • പാഠങ്ങൾ തുറക്കാം (എ.ഐക്ക് ഇന്റർനെറ്റ് വേണം)", language)
                },
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
fun LiveWaveformBars(
    isListening: Boolean,
    rmsLevel: Float,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "wave")
    val phase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )
    val activeAmp by animateFloatAsState(
        targetValue = if (isListening) rmsLevel.coerceIn(0.22f, 1f) else 0.1f,
        animationSpec = spring(),
        label = "amp"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .semantics { contentDescription = if (isListening) "Listening audio waveform" else "Audio waveform idle" }
    ) {
        val barCount = 24
        val totalWidth = size.width
        val gap = totalWidth * 0.015f
        val barWidth = (totalWidth - gap * (barCount - 1)) / barCount

        for (i in 0 until barCount) {
            val wave = if (isListening) {
                (0.45f + 0.55f * ((sin(phase + i * 0.55f) + 1f) / 2f)) * activeAmp
            } else {
                0.14f
            }
            val barH = (size.height * wave).coerceAtLeast(6.dp.toPx())
            val x = i * (barWidth + gap)
            val y = (size.height - barH) / 2f
            drawRoundRect(
                color = if (i % 2 == 0) primaryColor else secondaryColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun BigMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "practice_mic_button"
) {
    val infinite = rememberInfiniteTransition(label = "mic_pulse")
    val ringScale by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )
    val buttonScale by animateFloatAsState(
        targetValue = if (isListening) 1.07f else 1f,
        animationSpec = spring(),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .size(110.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(ringScale)
                    .clip(CircleShape)
                    .background(com.example.ui.theme.DeepGreenPrimary.copy(alpha = 0.24f))
            )
        }

        Box(
            modifier = Modifier
                .size(84.dp)
                .scale(buttonScale)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = if (isListening) {
                            listOf(Color(0xFFD9383A), Color(0xFFFF6B4A))
                        } else {
                            listOf(com.example.ui.theme.DeepGreenPrimary, com.example.ui.theme.DeepGreenLight)
                        }
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isListening) "Stop listening" else "Tap to speak",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
fun StarScoreRow(
    stars: Int,
    scorePercent: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= stars) GoldenSun else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Surface(
            color = EmeraldContainerLight,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "$scorePercent%",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = EmeraldSuccess,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private data class ConfettiPiece(
    val xFraction: Float,
    val speed: Float,
    val sizeDp: Float,
    val color: Color,
    val swayOffset: Float
)

@Composable
fun ConfettiOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        val pieces = remember {
            val palette = listOf(
                Color(0xFFF26419),
                Color(0xFF0E7C7B),
                Color(0xFFF6AE2D),
                Color(0xFF1B9E5A),
                Color(0xFF38BDF8),
                Color(0xFFFF5A79)
            )
            List(42) { idx ->
                ConfettiPiece(
                    xFraction = Random(idx * 17).nextFloat(),
                    speed = 0.6f + Random(idx * 31).nextFloat() * 0.9f,
                    sizeDp = 7f + Random(idx * 7).nextFloat() * 7f,
                    color = palette[idx % palette.size],
                    swayOffset = Random(idx * 13).nextFloat() * 6.28f
                )
            }
        }

        val infinite = rememberInfiniteTransition(label = "confetti")
        val progress by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "confetti_progress"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            pieces.forEach { p ->
                val y = ((progress * p.speed) % 1f) * size.height
                val x = p.xFraction * size.width + sin(progress * 10f + p.swayOffset) * 28f
                drawRoundRect(
                    color = p.color,
                    topLeft = Offset(x, y),
                    size = Size(p.sizeDp.dp.toPx(), (p.sizeDp * 0.6f).dp.toPx()),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
    }
}
