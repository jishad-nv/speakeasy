package com.example.model

import androidx.annotation.DrawableRes

enum class LeoMood {
    HAPPY,
    CHEERING,
    SLEEPY,
    THINKING,
    WAITING
}

enum class LeoPersonality(
    val titleEn: String,
    val titleMl: String,
    val subtitleEn: String,
    val subtitleMl: String
) {
    FRIENDLY(
        titleEn = "Friendly",
        titleMl = "സൗഹൃദപരം",
        subtitleEn = "Warm, patient & cheering every step",
        subtitleMl = "സ്നേഹത്തോടെ പ്രോത്സാഹിപ്പിക്കുന്നു"
    ),
    STRICT_COACH(
        titleEn = "Strict Coach",
        titleMl = "കർക്കശ കോച്ച്",
        subtitleEn = "Direct, crisp & high standards",
        subtitleMl = "വ്യക്തവും കൃത്യവുമായ പരിശീലനം"
    ),
    FUNNY(
        titleEn = "Funny",
        titleMl = "തമാശക്കാരൻ",
        subtitleEn = "Playful jokes & light energy",
        subtitleMl = "തമാശകളും രസകരമായ സംസാരവും"
    )
}

enum class DifficultyTier(val labelEn: String, val labelMl: String) {
    FOUNDATION("Gentle Pace", "ലളിത വേഗത"),
    STANDARD("Balanced", "സമതുലിത"),
    CHALLENGE("Stretch Mode", "ഉയർന്ന വേഗത")
}

enum class AppLanguage(val label: String, val nativeLabel: String) {
    ENGLISH("English", "EN"),
    MALAYALAM("Malayalam", "മലയാളം")
}

enum class ThemeMode(val labelEn: String, val labelMl: String) {
    SYSTEM("System", "സിസ്റ്റം"),
    LIGHT("Light", "ലൈറ്റ്"),
    DARK("Dark", "ഡാർക്ക്")
}

enum class LearningGoal(
    val titleEn: String,
    val titleMl: String,
    val subtitleEn: String,
    val subtitleMl: String
) {
    DAILY_LIFE("Daily Life", "നിത്യജീവിതം", "Shopping, neighbors & everyday talk", "കടകൾ, യാത്ര, ദൈനംദിന സംസാരം"),
    JOB_INTERVIEW("Job Interview", "ജോലി അഭിമുഖം", "Confident answers & office English", "അഭിമുഖവും ഓഫീസ് സംസാരവും"),
    TRAVEL("Travel", "യാത്ര", "Airports, hotels & asking directions", "വിമാനത്താവളം, ഹോട്ടൽ, വഴി ചോദിക്കൽ"),
    CONFIDENCE("Confidence", "ആത്മവിശ്വാസം", "Speak without hesitation or fear", "പേടിയില്ലാതെ ഇംഗ്ലീഷ് സംസാരിക്കാൻ")
}

enum class EnglishLevel(
    val index: Int,
    val titleEn: String,
    val titleMl: String,
    val xpRequired: Int,
    val taglineEn: String,
    val taglineMl: String
) {
    BEGINNER(0, "Beginner", "തുടക്കക്കാരൻ", 0, "Simple greetings & daily words", "ലളിതമായ വാക്കുകളും ആശംസകളും"),
    ELEMENTARY(1, "Elementary", "പ്രാഥമികം", 150, "Short everyday sentences", "ചെറിയ ദൈനംദിന വാക്യങ്ങൾ"),
    INTERMEDIATE(2, "Intermediate", "മധ്യനില", 350, "Smooth conversations & stories", "ഒഴുക്കോടെയുള്ള സംസാരം"),
    UPPER_INTERMEDIATE(3, "Upper-Intermediate", "ഉയർന്ന മധ്യനില", 600, "Workplaces, meetings & opinions", "ഓഫീസ്, മീറ്റിംഗുകൾ, അഭിപ്രായങ്ങൾ"),
    ADVANCED(4, "Advanced", "മികച്ച നില", 900, "Fluent persuasion & nuance", "ആത്മവിശ്വാസത്തോടെയുള്ള പ്രഭാഷണം"),
    MASTER(5, "Master", "മാസ്റ്റർ", 1300, "Natural, effortless English", "സ്വാഭാവികവും പ്രാവീണ്യമുള്ളതും");

    companion object {
        fun fromIndex(index: Int): EnglishLevel =
            entries.firstOrNull { it.index == index } ?: BEGINNER

        fun forXp(xp: Int): EnglishLevel =
            entries.lastOrNull { xp >= it.xpRequired } ?: BEGINNER
    }
}

enum class DailyTaskType(
    val id: String,
    val titleEn: String,
    val titleMl: String,
    val durationLabel: String,
    val xpReward: Int
) {
    SHADOW_SPEAKING("shadow", "Shadow Speaking", "കേട്ട് ആവർത്തിക്കാം", "2 min", 25),
    WORD_OF_THE_DAY("words", "Word of the Day", "ഇന്നത്തെ 5 വാക്കുകൾ", "3 min", 30),
    SITUATION_ROLEPLAY("roleplay", "Situation Roleplay", "സന്ദർഭ സംഭാഷണം", "3 min", 35),
    LISTEN_AND_ANSWER("listen_answer", "Listen & Answer", "കേട്ട് ഉത്തരം പറയാം", "2 min", 25),
    CHALLENGE("challenge", "Daily Challenge", "ഇന്നത്തെ ചലഞ്ച്", "2 min", 40)
}

enum class ChallengeKind(val titleEn: String, val titleMl: String) {
    TONGUE_TWISTER("Tongue Twister", "നാവുളുക്കാതെ പറയാം"),
    TOPIC_TALK_30S("30-Second Talk", "30 സെക്കൻഡ് സംസാരം"),
    DESCRIBE_PICTURE("Describe the Picture", "ചിത്രം വിവരിക്കാം")
}

data class ShadowDrill(
    val id: String,
    val sentence: String,
    val malayalamHint: String,
    val speakingTipEn: String,
    val speakingTipMl: String
)

data class WordDrill(
    val word: String,
    val pronunciationHint: String,
    val malayalamMeaning: String,
    val sampleSentence: String
)

data class RoleplayTurn(
    val leoLine: String,
    val leoLineMl: String,
    val suggestedReply: String,
    val hintMl: String
)

data class RoleplayScene(
    val id: String,
    val placeTitleEn: String,
    val placeTitleMl: String,
    val roleLabelEn: String,
    val turns: List<RoleplayTurn>
)

data class ListenAnswerDrill(
    val id: String,
    val audioQuestion: String,
    val malayalamHint: String,
    val sampleAnswer: String,
    val expectedKeywords: List<String>
)

data class ChallengeDrill(
    val id: String,
    val kind: ChallengeKind,
    val promptEn: String,
    val promptMl: String,
    val targetText: String? = null,
    @DrawableRes val imageRes: Int? = null,
    val minWordsExpected: Int = 5
)

data class LevelCurriculum(
    val level: EnglishLevel,
    val shadowDrills: List<ShadowDrill>,
    val wordDrills: List<WordDrill>,
    val roleplayScene: RoleplayScene,
    val listenAnswerDrills: List<ListenAnswerDrill>,
    val challengeDrill: ChallengeDrill
)

data class PlacementQuestion(
    val id: Int,
    val leoQuestion: String,
    val malayalamHint: String,
    val sampleResponse: String,
    val expectedKeywords: List<String>,
    val difficultyWeight: Int
)

data class SpeakingEvaluation(
    val stars: Int,
    val scorePercent: Int,
    val spokenText: String,
    val targetText: String,
    val matchedWords: List<String>,
    val missedWords: List<String>,
    val feedbackEn: String,
    val feedbackMl: String,
    val isOfflineScore: Boolean = false
)

data class DailyPlanItem(
    val taskType: DailyTaskType,
    val customPrompt: String,
    val malayalamHint: String
)

data class DailyPlanState(
    val focusNote: String = "",
    val weakWordsInFocus: List<String> = emptyList(),
    val items: List<DailyPlanItem> = emptyList(),
    val isAiGenerated: Boolean = false
)

data class WeeklySummaryReport(
    val headline: String,
    val summary: String,
    val nextWeekFocus: String,
    val isAiGenerated: Boolean
)

data class BadgeItem(
    val id: String,
    val titleEn: String,
    val titleMl: String,
    val descEn: String,
    val descMl: String,
    val unlocked: Boolean
)

data class ChatMessage(
    val id: String,
    val isFromLeo: Boolean,
    val text: String,
    val correctionText: String? = null,
    val malayalamHelp: String? = null,
    val isStreaming: Boolean = false
)

data class UserProfileState(
    val onboardingCompleted: Boolean = false,
    val goal: LearningGoal = LearningGoal.DAILY_LIFE,
    val dailyMinutesGoal: Int = 10,
    val reminderTimes: List<String> = listOf("08:30", "20:00"),
    val notificationsEnabled: Boolean = true,
    val leoPersonality: LeoPersonality = LeoPersonality.FRIENDLY,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val supportLevel: SupportLevel = SupportLevel.FULL_MALAYALAM,
    val supportOverriddenByUser: Boolean = false,
    val sourceLanguageCode: String = "ml",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val currentLevel: EnglishLevel = EnglishLevel.BEGINNER,
    val difficultyTier: DifficultyTier = DifficultyTier.STANDARD,
    val xp: Int = 0,
    val streakDays: Int = 1,
    val streakFreezes: Int = 1,
    val streakFreezeActive: Boolean = false,
    val missedDays: Int = 0,
    val completedTaskIdsToday: Set<String> = emptySet(),
    val todayMinutesPracticed: Int = 0,
    val offlineDownloaded: Boolean = false,
    val soundEffectsEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val ieltsTargetBand: Double = 7.0,
    val ieltsCurrentBand: Double = 5.5,
    val ieltsExamDateKey: String = ""
)
