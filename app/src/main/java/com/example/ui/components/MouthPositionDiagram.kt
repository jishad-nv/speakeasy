package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.model.AccentSoundTarget
import com.example.ui.theme.DeepGreenPrimary
import com.example.ui.theme.LeoOrangeAccent

@Composable
fun MouthPositionDiagram(
    target: AccentSoundTarget,
    modifier: Modifier = Modifier
) {
    val bgTint = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val palateColor = Color(0xFF8D5B4C)
    val teethColor = Color(0xFFFFFDF9)
    val lipColor = Color(0xFFE07A68)
    val tongueColor = Color(0xFFD94F6D)
    val airflowColor = DeepGreenPrimary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .semantics { contentDescription = "Mouth and tongue diagram for ${target.labelEn}" }
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f

        // Background card area
        drawRoundRect(
            color = bgTint,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx(), 16.dp.toPx())
        )

        // Upper palate arc (sagittal side view facing left)
        val palate = Path().apply {
            moveTo(cx - w * 0.22f, h * 0.36f)
            quadraticTo(cx, h * 0.14f, cx + w * 0.25f, h * 0.30f)
        }
        drawPath(palate, color = palateColor, style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round))

        // Upper & lower teeth
        drawRect(
            color = teethColor,
            topLeft = Offset(cx - w * 0.21f, h * 0.36f),
            size = Size(w * 0.045f, h * 0.12f)
        )
        drawRect(
            color = teethColor,
            topLeft = Offset(cx - w * 0.21f, h * 0.58f),
            size = Size(w * 0.045f, h * 0.11f)
        )

        // Lips (vary slightly for V vs W vs F)
        val upperLipX = cx - w * 0.27f
        val lowerLipX = when (target) {
            AccentSoundTarget.V_W, AccentSoundTarget.F_P -> cx - w * 0.22f // lower lip tucked toward upper teeth
            else -> cx - w * 0.27f
        }
        drawCircle(color = lipColor, radius = 9.dp.toPx(), center = Offset(upperLipX, h * 0.39f))
        drawCircle(color = lipColor, radius = 9.dp.toPx(), center = Offset(lowerLipX, h * 0.62f))

        // Tongue position based on target sound
        val tonguePath = Path().apply {
            when (target) {
                AccentSoundTarget.TH_DH -> {
                    // Tongue tip between upper & lower front teeth
                    moveTo(cx - w * 0.25f, h * 0.51f)
                    quadraticTo(cx - w * 0.05f, h * 0.45f, cx + w * 0.22f, h * 0.72f)
                }
                AccentSoundTarget.Z_SOUND -> {
                    // Tongue tip close to upper alveolar ridge behind teeth
                    moveTo(cx - w * 0.14f, h * 0.39f)
                    quadraticTo(cx + w * 0.02f, h * 0.48f, cx + w * 0.22f, h * 0.74f)
                }
                AccentSoundTarget.VOWEL_LENGTH -> {
                    // Raised front/high vowel arch
                    moveTo(cx - w * 0.16f, h * 0.56f)
                    quadraticTo(cx - w * 0.02f, h * 0.34f, cx + w * 0.22f, h * 0.70f)
                }
                else -> {
                    // Neutral relaxed tongue
                    moveTo(cx - w * 0.17f, h * 0.60f)
                    quadraticTo(cx, h * 0.52f, cx + w * 0.22f, h * 0.73f)
                }
            }
        }
        drawPath(tonguePath, color = tongueColor, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))

        // Airflow / vibration / rhythm visual cue
        val airY = h * 0.49f
        drawLine(
            color = airflowColor,
            start = Offset(cx - w * 0.12f, airY),
            end = Offset(cx - w * 0.39f, airY),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
        )

        if (target == AccentSoundTarget.Z_SOUND) {
            // Throat buzz indicator
            drawCircle(
                color = LeoOrangeAccent,
                radius = 8.dp.toPx(),
                center = Offset(cx + w * 0.22f, h * 0.80f),
                style = Stroke(width = 3.dp.toPx())
            )
        } else if (target == AccentSoundTarget.WORD_STRESS || target == AccentSoundTarget.INTONATION_RHYTHM) {
            // Pitch/stress contour wave on right side
            val contour = Path().apply {
                moveTo(cx + w * 0.05f, h * 0.36f)
                quadraticTo(cx + w * 0.16f, h * 0.16f, cx + w * 0.28f, h * 0.24f)
            }
            drawPath(contour, color = LeoOrangeAccent, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}
