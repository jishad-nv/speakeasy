package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.example.model.AppLanguage
import com.example.model.SupportLevel
import com.example.ui.theme.MalayalamFontFamily

fun bi(en: String, ml: String, language: AppLanguage): String =
    if (language == AppLanguage.MALAYALAM) ml else en

fun adaptiveInstruction(
    supportLevel: SupportLevel,
    fullMl: String,
    mixed: String,
    simpleEn: String
): String = when (supportLevel) {
    SupportLevel.FULL_MALAYALAM -> fullMl
    SupportLevel.MIXED -> mixed
    SupportLevel.ENGLISH_ONLY -> simpleEn
}

@Composable
fun malayalamStyle(base: TextStyle = MaterialTheme.typography.bodyMedium): TextStyle =
    base.copy(fontFamily = MalayalamFontFamily)

@Composable
fun SpeakUpCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
            content = content
        )
    }
}
