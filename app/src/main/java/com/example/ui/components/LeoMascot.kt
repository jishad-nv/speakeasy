package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.LeoMood
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LeoMascotAvatar(
    mood: LeoMood,
    modifier: Modifier = Modifier,
    size: Dp = 84.dp
) {
    val infinite = rememberInfiniteTransition(label = "leo_idle")
    val bobOffset by infinite.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (mood == LeoMood.CHEERING) 450 else 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )
    val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = "Leo the lion mascot feeling ${mood.name.lowercase()}" }
    ) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f + bobOffset * (h / 100f)

        val maneColor = Color(0xFFF26419)
        val maneHighlight = Color(0xFFFF8C4B)
        val faceColor = Color(0xFFFFCA4B)
        val cheekColor = Color(0xFFFF8A80)
        val muzzleColor = Color(0xFFFFECC2)
        val headphoneBand = Color(0xFF2B3445)
        val headphoneCup = Color(0xFF0E7C7B)
        val outlineColor = Color(0xFF2B2118)

        // Scalloped lion mane
        val maneRadius = w * 0.36f
        val petalCount = 12
        for (i in 0 until petalCount) {
            val angle = (i * 2 * Math.PI / petalCount).toFloat()
            val px = cx + cos(angle) * (maneRadius * 0.72f)
            val py = cy + sin(angle) * (maneRadius * 0.72f)
            drawCircle(
                color = if (i % 2 == 0) maneColor else maneHighlight,
                radius = w * 0.145f,
                center = Offset(px, py)
            )
        }
        drawCircle(
            color = maneColor,
            radius = maneRadius * 0.88f,
            center = Offset(cx, cy)
        )

        // Ears
        val earR = w * 0.095f
        drawCircle(faceColor, radius = earR, center = Offset(cx - w * 0.21f, cy - h * 0.21f))
        drawCircle(maneHighlight, radius = earR * 0.55f, center = Offset(cx - w * 0.21f, cy - h * 0.21f))
        drawCircle(faceColor, radius = earR, center = Offset(cx + w * 0.21f, cy - h * 0.21f))
        drawCircle(maneHighlight, radius = earR * 0.55f, center = Offset(cx + w * 0.21f, cy - h * 0.21f))

        // Headphone headband
        drawArc(
            color = headphoneBand,
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.35f, cy - h * 0.36f),
            size = Size(w * 0.70f, h * 0.68f),
            style = Stroke(width = w * 0.055f, cap = StrokeCap.Round)
        )

        // Face circle
        val faceR = w * 0.265f
        drawCircle(
            color = faceColor,
            radius = faceR,
            center = Offset(cx, cy + h * 0.02f)
        )

        // Headphone earcups
        drawRoundRect(
            color = headphoneCup,
            topLeft = Offset(cx - w * 0.365f, cy - h * 0.09f),
            size = Size(w * 0.10f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        drawRoundRect(
            color = headphoneCup,
            topLeft = Offset(cx + w * 0.265f, cy - h * 0.09f),
            size = Size(w * 0.10f, h * 0.22f),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )

        // Mic boom from right earcup
        drawLine(
            color = headphoneBand,
            start = Offset(cx + w * 0.31f, cy + h * 0.10f),
            end = Offset(cx + w * 0.14f, cy + h * 0.23f),
            strokeWidth = w * 0.028f,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = headphoneCup,
            radius = w * 0.036f,
            center = Offset(cx + w * 0.12f, cy + h * 0.23f)
        )

        // Muzzle & cheeks
        drawOval(
            color = muzzleColor,
            topLeft = Offset(cx - w * 0.12f, cy + h * 0.04f),
            size = Size(w * 0.24f, h * 0.16f)
        )
        drawCircle(
            color = cheekColor.copy(alpha = 0.55f),
            radius = w * 0.045f,
            center = Offset(cx - w * 0.15f, cy + h * 0.07f)
        )
        drawCircle(
            color = cheekColor.copy(alpha = 0.55f),
            radius = w * 0.045f,
            center = Offset(cx + w * 0.15f, cy + h * 0.07f)
        )

        // Eyes by mood
        val eyeY = cy - h * 0.02f
        val leftEyeX = cx - w * 0.095f
        val rightEyeX = cx + w * 0.095f
        val eyeR = w * 0.036f

        when (mood) {
            LeoMood.HAPPY, LeoMood.WAITING -> {
                drawCircle(outlineColor, radius = eyeR, center = Offset(leftEyeX, eyeY))
                drawCircle(Color.White, radius = eyeR * 0.38f, center = Offset(leftEyeX - eyeR * 0.25f, eyeY - eyeR * 0.25f))
                drawCircle(outlineColor, radius = eyeR, center = Offset(rightEyeX, eyeY))
                drawCircle(Color.White, radius = eyeR * 0.38f, center = Offset(rightEyeX - eyeR * 0.25f, eyeY - eyeR * 0.25f))
            }
            LeoMood.CHEERING -> {
                // Joyful crescent eyes
                drawArc(
                    color = outlineColor,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(leftEyeX - eyeR, eyeY - eyeR * 0.5f),
                    size = Size(eyeR * 2f, eyeR * 1.4f),
                    style = Stroke(width = w * 0.026f, cap = StrokeCap.Round)
                )
                drawArc(
                    color = outlineColor,
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(rightEyeX - eyeR, eyeY - eyeR * 0.5f),
                    size = Size(eyeR * 2f, eyeR * 1.4f),
                    style = Stroke(width = w * 0.026f, cap = StrokeCap.Round)
                )
                // Sparkles around Leo
                val sparkColor = Color(0xFFF6AE2D)
                drawCircle(sparkColor, radius = w * 0.025f * (0.7f + 0.4f * pulse), center = Offset(w * 0.12f, h * 0.18f))
                drawCircle(sparkColor, radius = w * 0.028f * (1.1f - 0.3f * pulse), center = Offset(w * 0.88f, h * 0.20f))
            }
            LeoMood.SLEEPY -> {
                drawLine(
                    color = outlineColor,
                    start = Offset(leftEyeX - eyeR, eyeY),
                    end = Offset(leftEyeX + eyeR, eyeY + h * 0.01f),
                    strokeWidth = w * 0.025f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = outlineColor,
                    start = Offset(rightEyeX - eyeR, eyeY + h * 0.01f),
                    end = Offset(rightEyeX + eyeR, eyeY),
                    strokeWidth = w * 0.025f,
                    cap = StrokeCap.Round
                )
            }
            LeoMood.THINKING -> {
                // Left eye normal, right eye slightly squinted looking up
                drawCircle(outlineColor, radius = eyeR * 0.9f, center = Offset(leftEyeX, eyeY - h * 0.015f))
                drawCircle(Color.White, radius = eyeR * 0.32f, center = Offset(leftEyeX, eyeY - h * 0.025f))
                drawCircle(outlineColor, radius = eyeR * 0.78f, center = Offset(rightEyeX, eyeY - h * 0.025f))
                // Thinking dots top-right
                val dotColor = Color(0xFF0E7C7B)
                drawCircle(dotColor.copy(alpha = 0.5f + 0.4f * pulse), radius = w * 0.024f, center = Offset(w * 0.82f, h * 0.17f))
                drawCircle(dotColor.copy(alpha = 0.9f - 0.4f * pulse), radius = w * 0.032f, center = Offset(w * 0.89f, h * 0.10f))
            }
        }

        // Nose triangle
        val nosePath = Path().apply {
            moveTo(cx - w * 0.038f, cy + h * 0.055f)
            lineTo(cx + w * 0.038f, cy + h * 0.055f)
            lineTo(cx, cy + h * 0.095f)
            close()
        }
        drawPath(nosePath, color = outlineColor)

        // Mouth by mood
        when (mood) {
            LeoMood.HAPPY, LeoMood.CHEERING -> {
                drawArc(
                    color = outlineColor,
                    startAngle = 15f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.075f, cy + h * 0.08f),
                    size = Size(w * 0.15f, h * 0.09f),
                    style = Stroke(width = w * 0.025f, cap = StrokeCap.Round)
                )
            }
            LeoMood.SLEEPY -> {
                drawCircle(
                    color = outlineColor,
                    radius = w * 0.022f,
                    center = Offset(cx, cy + h * 0.13f)
                )
            }
            LeoMood.THINKING -> {
                drawLine(
                    color = outlineColor,
                    start = Offset(cx - w * 0.05f, cy + h * 0.13f),
                    end = Offset(cx + w * 0.05f, cy + h * 0.115f),
                    strokeWidth = w * 0.024f,
                    cap = StrokeCap.Round
                )
            }
            LeoMood.WAITING -> {
                drawArc(
                    color = outlineColor,
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(cx - w * 0.055f, cy + h * 0.09f),
                    size = Size(w * 0.11f, h * 0.06f),
                    style = Stroke(width = w * 0.023f, cap = StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun LeoSpeechGuide(
    mood: LeoMood,
    message: String,
    subMessage: String? = null,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 74.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        LeoMascotAvatar(mood = mood, size = avatarSize)
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomEnd = 20.dp,
                bottomStart = 6.dp
            ),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .weight(1f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 6.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subMessage.isNullOrBlank()) {
                    Text(
                        text = subMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
