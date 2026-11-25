package com.example.model

enum class SupportLevel(
    val titleEn: String,
    val titleMl: String,
    val descEn: String,
    val descMl: String
) {
    FULL_MALAYALAM(
        titleEn = "Full Malayalam",
        titleMl = "പൂർണ്ണ മലയാളം സഹായം",
        descEn = "All instructions, tips & meanings in Malayalam",
        descMl = "എല്ലാ നിർദ്ദേശങ്ങളും അർത്ഥങ്ങളും മലയാളത്തിൽ"
    ),
    MIXED(
        titleEn = "Mixed (ML + EN)",
        titleMl = "മിക്സ്ഡ് (മലയാളം + ഇംഗ്ലീഷ്)",
        descEn = "Simple English with Malayalam safety net",
        descMl = "ലളിത ഇംഗ്ലീഷും ആവശ്യമുള്ളപ്പോൾ മലയാളവും"
    ),
    ENGLISH_ONLY(
        titleEn = "English Only",
        titleMl = "ഇംഗ്ലീഷ് മാത്രം",
        descEn = "Full English immersion for fast thinking",
        descMl = "വേഗത്തിൽ ചിന്തിക്കാൻ പൂർണ്ണ ഇംഗ്ലീഷ്"
    )
}

enum class SpeechInputLocale(val bcp47: String, val labelEn: String, val labelMl: String) {
    ENGLISH_IN("en-IN", "English (IN)", "ഇംഗ്ലീഷ് (IN)"),
    ENGLISH_US("en-US", "English (US)", "ഇംഗ്ലീഷ് (US)"),
    MALAYALAM_IN("ml-IN", "Malayalam", "മലയാളം")
}

enum class ZeroPhraseCategory(
    val id: String,
    val titleEn: String,
    val titleMl: String
) {
    GREETINGS("greetings", "Greetings", "ആശംസകൾ"),
    NUMBERS("numbers", "Numbers & Time", "അക്കങ്ങളും സമയവും"),
    FAMILY("family", "Family & People", "കുടുംബവും ആളുകളും"),
    FOOD("food", "Food & Shopping", "ഭക്ഷണവും കടയും"),
    DAILY_ROUTINE("routine", "Daily Routine", "നിത്യജീവിതം")
}

data class ZeroSurvivalPhrase(
    val id: String,
    val category: ZeroPhraseCategory,
    val englishText: String,
    val malayalamMeaning: String,
    val manglishScript: String,
    val numberTag: Int
)

enum class AccentSoundTarget(
    val id: String,
    val labelEn: String,
    val labelMl: String,
    val pairExample: String,
    val mouthGuideEn: String,
    val mouthGuideMl: String
) {
    TH_DH(
        id = "th_dh",
        labelEn = "TH / DH Sounds",
        labelMl = "TH / DH ശബ്ദങ്ങൾ",
        pairExample = "think vs sink • this vs dis",
        mouthGuideEn = "Gently place the tip of your tongue between your upper & lower front teeth and blow air.",
        mouthGuideMl = "നാവിന്റെ അഗ്രം മുകളിലത്തെയും താഴത്തെയും പല്ലുകൾക്കിടയിൽ മെല്ലെ വെച്ച് വായു പുറത്തുവിടുക (ത/ദ അല്ല)."
    ),
    V_W(
        id = "v_w",
        labelEn = "V vs W",
        labelMl = "V / W വ്യത്യാസം",
        pairExample = "very vs berry • vest vs west",
        mouthGuideEn = "For V, touch bottom lip to top teeth. For W, round both lips like saying 'oo' with no teeth contact.",
        mouthGuideMl = "V പറയുമ്പോൾ താഴത്തെ ചുണ്ട് മുകളിലെ പല്ലിൽ തൊടണം; W-ന് ചുണ്ടുകൾ 'ഉ' പോലെ ഉരുട്ടണം."
    ),
    F_P(
        id = "f_p",
        labelEn = "F vs P",
        labelMl = "F / P ശബ്ദങ്ങൾ",
        pairExample = "fan vs pan • fine vs pine",
        mouthGuideEn = "For F, top teeth lightly rest on lower lip and push soft air continuously (not a 'p' pop).",
        mouthGuideMl = "F-ന് മുകളിലെ പല്ല് താഴത്തെ ചുണ്ടിൽ വെച്ച് പതുക്കെ കാറ്റ് ഊതുക ('ഫ' പോലെ പൊട്ടിക്കരുത്)."
    ),
    Z_SOUND(
        id = "z_sound",
        labelEn = "Z Buzz Sound",
        labelMl = "Z ശബ്ദം",
        pairExample = "zoo vs sue • zip vs sip",
        mouthGuideEn = "Keep tongue near the top tooth ridge like 's', but vibrate your throat like a buzzing bee!",
        mouthGuideMl = "'സ' പറയുന്ന പോലെ നാവ് വെച്ച് തൊണ്ടയിൽ വണ്ട് മൂളുന്ന പോലെ ശബ്ദം കമ്പനം ചെയ്യിക്കുക."
    ),
    VOWEL_LENGTH(
        id = "short_long_vowels",
        labelEn = "Short vs Long Vowels",
        labelMl = "ഹ്രസ്വ-ദീർഘ സ്വരങ്ങൾ",
        pairExample = "ship vs sheep • full vs fool",
        mouthGuideEn = "Stretch 'ee' / 'oo' smoothly for long vowels; keep 'i' / 'u' crisp and short.",
        mouthGuideMl = "'sheep' പറയുമ്പോൾ നീട്ടി പറയുക, 'ship' ചെറുതും വേഗത്തിലും പറയുക."
    ),
    WORD_STRESS(
        id = "word_stress",
        labelEn = "Word Stress",
        labelMl = "വാക്കിലെ ഊന്നൽ",
        pairExample = "de-VEL-op • PHO-to-graph",
        mouthGuideEn = "Make the stressed syllable slightly louder, longer, and higher in pitch—never flat.",
        mouthGuideMl = "വാക്കിലെ പ്രധാന ഭാഗം അല്പം ഉറക്കെയും നീട്ടിയും പറയുക."
    ),
    INTONATION_RHYTHM(
        id = "intonation_rhythm",
        labelEn = "Sentence Rhythm",
        labelMl = "വാക്യത്തിന്റെ താളം",
        pairExample = "Could YOU help ME please? ↗",
        mouthGuideEn = "Group words naturally and raise pitch slightly at the end of yes/no questions.",
        mouthGuideMl = "വാക്കുകൾ മുറിച്ചു പറയാതെ താളത്തിൽ ചേർത്ത് പറയുക; ചോദ്യങ്ങളിൽ അവസാനം സ്വരം ഉയർത്തുക."
    )
}

data class MinimalPairDrill(
    val id: String,
    val target: AccentSoundTarget,
    val wordA: String,
    val wordB: String,
    val sentencePrompt: String,
    val malayalamTip: String
)

data class WordPronunciationScore(
    val word: String,
    val score: Int,
    val note: String
)

data class SoundErrorDetail(
    val soundTargetId: String,
    val issue: String,
    val correctionTip: String
)

data class AudioPronunciationAnalysis(
    val overallScore: Int,
    val wordScores: List<WordPronunciationScore>,
    val soundErrors: List<SoundErrorDetail>,
    val tip: String,
    val transcriptHeard: String,
    val isOfflineFallback: Boolean = false
)

data class TranslateLearnResult(
    val malayalamInput: String,
    val formalEnglish: String,
    val casualEnglish: String,
    val manglishFormal: String,
    val manglishCasual: String,
    val contextNote: String,
    val isAiGenerated: Boolean = true
)

data class JournalCorrectionResult(
    val spokenOriginal: String,
    val naturalVersion: String,
    val keyImprovements: List<String>,
    val encouragement: String,
    val isAiGenerated: Boolean = true
)

data class RealLifeScenario(
    val id: String,
    val titleEn: String,
    val titleMl: String,
    val characterName: String,
    val characterRole: String,
    val openingLine: String,
    val openingLineMl: String,
    val voicePitch: Float,
    val voiceRate: Float,
    val surpriseTwist: String
)

data class SimulatorReportCard(
    val fluencyScore: Int,
    val confidenceScore: Int,
    val newWordsLearned: List<String>,
    val improvementPoints: List<String>
)

data class CameraWordItem(
    val englishText: String,
    val malayalamMeaning: String,
    val manglishPronunciation: String,
    val exampleSentence: String
)

data class CameraExtractResult(
    val sceneSummaryMl: String,
    val items: List<CameraWordItem>
)

// IELTS Module Models
enum class IeltsSkill(val id: String, val titleEn: String, val titleMl: String) {
    SPEAKING("speaking", "Speaking", "സ്പീക്കിംഗ്"),
    WRITING("writing", "Writing", "റൈറ്റിംഗ്"),
    LISTENING("listening", "Listening", "ലിസണിംഗ്"),
    READING("reading", "Reading", "റീഡിംഗ്")
}

data class IeltsCriteriaBreakdown(
    val fluencyOrTask: Double,
    val lexical: Double,
    val grammar: Double,
    val pronunciationOrCoherence: Double
)

data class IeltsSpeakingEval(
    val bandScore: Double,
    val breakdown: IeltsCriteriaBreakdown,
    val fillerWordCount: Int,
    val wordsPerMinute: Int,
    val feedbackSummary: String,
    val modelImprovedAnswer: String,
    val weakestFocus: String
)

data class IeltsWritingEval(
    val bandScore: Double,
    val breakdown: IeltsCriteriaBreakdown,
    val corrections: List<String>,
    val rewrittenParagraph: String,
    val usefulVocabulary: List<String>
)

data class IeltsQuestion(
    val id: String,
    val questionType: String, // "T/F/NG", "Matching Headings", "Fill Blank", "MCQ"
    val prompt: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanationEn: String,
    val explanationMl: String
)

data class IeltsReadingPassage(
    val id: String,
    val title: String,
    val body: String,
    val highlightedVocab: Map<String, String>, // English word -> Malayalam meaning
    val questions: List<IeltsQuestion>
)

data class IeltsListeningSection(
    val sectionNumber: Int,
    val title: String,
    val contextLabel: String,
    val audioScript: String,
    val questions: List<IeltsQuestion>
)

data class IeltsVocabItem(
    val word: String,
    val topic: String, // "AWL", "Environment", "Education", "Technology", "Health"
    val malayalamMeaning: String,
    val academicExample: String
)

data class IeltsStudyPlan(
    val estimatedCurrentBand: Double,
    val targetBand: Double,
    val weeklyFocus: List<String>,
    val dailyChecklist: List<String>,
    val malayalamMotivation: String,
    val isAiGenerated: Boolean = true
)
