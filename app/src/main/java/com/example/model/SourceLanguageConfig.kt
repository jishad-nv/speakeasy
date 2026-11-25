package com.example.model

/**
 * Config-driven source-language abstraction so SpeakUp can support
 * additional native languages (e.g., Hindi, Tamil) by adding/selecting a config entry.
 */
data class SourceLanguageConfig(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val speechLocale: String,
    val ttsLocaleTag: String,
    val transliterationLabel: String,
    val aiTutorContextName: String
)

object SourceLanguageRegistry {
    val MALAYALAM = SourceLanguageConfig(
        code = "ml",
        displayName = "Malayalam",
        nativeName = "മലയാളം",
        speechLocale = "ml-IN",
        ttsLocaleTag = "ml-IN",
        transliterationLabel = "Manglish (മലയാളം ലിപി)",
        aiTutorContextName = "Malayalam speaker"
    )

    val HINDI = SourceLanguageConfig(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिन्दी",
        speechLocale = "hi-IN",
        ttsLocaleTag = "hi-IN",
        transliterationLabel = "Hinglish (देवनागरी)",
        aiTutorContextName = "Hindi speaker"
    )

    val TAMIL = SourceLanguageConfig(
        code = "ta",
        displayName = "Tamil",
        nativeName = "தமிழ்",
        speechLocale = "ta-IN",
        ttsLocaleTag = "ta-IN",
        transliterationLabel = "Tanglish (தமிழ்)",
        aiTutorContextName = "Tamil speaker"
    )

    val allConfigs = listOf(MALAYALAM, HINDI, TAMIL)

    fun byCode(code: String): SourceLanguageConfig =
        allConfigs.firstOrNull { it.code == code } ?: MALAYALAM
}
