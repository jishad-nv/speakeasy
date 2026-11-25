package com.example.data.curriculum

import com.example.model.AccentSoundTarget
import com.example.model.IeltsListeningSection
import com.example.model.IeltsQuestion
import com.example.model.IeltsReadingPassage
import com.example.model.IeltsVocabItem
import com.example.model.MinimalPairDrill
import com.example.model.RealLifeScenario

object ExtensionsCatalog {

    val minimalPairDrills: List<MinimalPairDrill> = listOf(
        MinimalPairDrill(
            id = "mp_th_1",
            target = AccentSoundTarget.TH_DH,
            wordA = "think",
            wordB = "sink",
            sentencePrompt = "I think this is the third thing we discussed.",
            malayalamTip = "'തിങ്ക്' എന്ന് പറയരുത്—നാവിന്റെ അഗ്രം പല്ലിനിടയിൽ വെച്ച് മൃദുവായി വായു വിടുക."
        ),
        MinimalPairDrill(
            id = "mp_vw_1",
            target = AccentSoundTarget.V_W,
            wordA = "vest",
            wordB = "west",
            sentencePrompt = "We visited the very warm village in the west.",
            malayalamTip = "Very-ക്ക് താഴത്തെ ചുണ്ട് പല്ലിൽ തൊടുക; West-ന് ചുണ്ട് 'ഉ' പോലെ ഉരുട്ടുക."
        ),
        MinimalPairDrill(
            id = "mp_vw_2",
            target = AccentSoundTarget.V_W,
            wordA = "very",
            wordB = "berry",
            sentencePrompt = "This berry juice is very fresh.",
            malayalamTip = "'വെരി' പറയുമ്പോൾ 'ബെറി' ആകാതിരിക്കാൻ ചുണ്ടുകൾ രണ്ടും അമർത്താതെ കീഴ്ചുണ്ട് പല്ലിൽ തൊടുക."
        ),
        MinimalPairDrill(
            id = "mp_fp_1",
            target = AccentSoundTarget.F_P,
            wordA = "fan",
            wordB = "pan",
            sentencePrompt = "Fix the fast fan before frying in the pan.",
            malayalamTip = "Fan-ന് 'ഫ' പോലെ പൊട്ടിക്കാതെ മുകളിലെ പല്ല് കീഴ്ചുണ്ടിൽ വെച്ച് കാറ്റ് ഊതുക."
        ),
        MinimalPairDrill(
            id = "mp_z_1",
            target = AccentSoundTarget.Z_SOUND,
            wordA = "zoo",
            wordB = "sue",
            sentencePrompt = "Zebra zones at the zoo are easy to visit.",
            malayalamTip = "'സൂ' അല്ല—തൊണ്ടയിൽ കമ്പനത്തോടെ 'സ്സൂ' (buzz) ശബ്ദം ഉണ്ടാക്കുക."
        ),
        MinimalPairDrill(
            id = "mp_vowel_1",
            target = AccentSoundTarget.VOWEL_LENGTH,
            wordA = "ship",
            wordB = "sheep",
            sentencePrompt = "The big ship carried fifty white sheep.",
            malayalamTip = "Ship വേഗത്തിൽ ചെറുതായി പറയുക; Sheep നീട്ടി 'ഷീപ്പ്' എന്ന് പറയുക."
        ),
        MinimalPairDrill(
            id = "mp_stress_1",
            target = AccentSoundTarget.WORD_STRESS,
            wordA = "PREsent (gift)",
            wordB = "preSENT (give)",
            sentencePrompt = "She wants to deVELop her PHOtography project.",
            malayalamTip = "വലിയക്ഷരത്തിലുള്ള ഭാഗം (VEL / PHO) കൂടുതൽ ശക്തിയിലും ഉയരത്തിലും പറയുക."
        ),
        MinimalPairDrill(
            id = "mp_rhythm_1",
            target = AccentSoundTarget.INTONATION_RHYTHM,
            wordA = "Could you help?",
            wordB = "Yes, I can.",
            sentencePrompt = "Excuse me, could you tell me where the station is?",
            malayalamTip = "ചോദ്യത്തിന്റെ അവസാനം സ്വരം ഉയർത്തുക, ഉത്തരത്തിൽ താഴ്ത്തുക."
        )
    )

    val reverseMalayalamPrompts: List<Pair<String, String>> = listOf(
        "എനിക്ക് ഒരു കപ്പ് ചൂട് ചായ തരാമോ?" to "Could you give me a cup of hot tea?",
        "റെയിൽവേ സ്റ്റേഷൻ ഇവിടെ നിന്ന് എത്ര ദൂരെയാണ്?" to "How far is the railway station from here?",
        "ക്ഷമിക്കണം, എനിക്ക് അല്പം താമസിച്ചുപോയി." to "I am sorry, I am a bit late.",
        "നാളെ രാവിലെ പത്തു മണിക്ക് നമുക്ക് കാണാം." to "Let us meet tomorrow at ten in the morning.",
        "ഇതിന്റെ വില അല്പം കുറയ്ക്കാൻ പറ്റുമോ?" to "Can you reduce the price a little bit?"
    )

    val realLifeScenarios: List<RealLifeScenario> = listOf(
        RealLifeScenario(
            id = "phone_call",
            titleEn = "Phone Call",
            titleMl = "ഫോൺ കോൾ",
            characterName = "Priya",
            characterRole = "Clinic Receptionist",
            openingLine = "Good morning, City Care Clinic, Priya speaking. How may I help you today?",
            openingLineMl = "ഗുഡ് മോർണിംഗ്, സിറ്റി കെയർ ക്ലിനിക്, പ്രിയയാണ്. എന്ത് സഹായമാണ് വേണ്ടത്?",
            voicePitch = 1.12f,
            voiceRate = 0.96f,
            surpriseTwist = "Wait—our computer system just froze! Can you spell your full name slowly and confirm your phone number?"
        ),
        RealLifeScenario(
            id = "job_interview",
            titleEn = "Job Interview",
            titleMl = "ജോലി അഭിമുഖം",
            characterName = "Mr. Menon",
            characterRole = "Hiring Manager",
            openingLine = "Welcome! Thanks for coming in today. Could you tell me a little about yourself?",
            openingLineMl = "സ്വാഗതം! നിങ്ങളെക്കുറിച്ച് ചുരുക്കി ഒന്ന് പറയാമോ?",
            voicePitch = 0.88f,
            voiceRate = 0.95f,
            surpriseTwist = "Interesting! Imagine a teammate disagrees strongly with your idea ten minutes before a deadline—how would you respond?"
        ),
        RealLifeScenario(
            id = "doctor_visit",
            titleEn = "Doctor Visit",
            titleMl = "ഡോക്ടറെ കാണൽ",
            characterName = "Dr. Nair",
            characterRole = "Family Physician",
            openingLine = "Hello, please have a seat. What brings you in to see me today?",
            openingLineMl = "ഹലോ, ഇരിക്കൂ. ഇന്ന് എന്താണ് അസുഖം?",
            voicePitch = 0.92f,
            voiceRate = 0.92f,
            surpriseTwist = "I see. Also, your blood pressure is slightly up—have you been drinking enough water or skipping meals?"
        ),
        RealLifeScenario(
            id = "airport",
            titleEn = "Airport Counter",
            titleMl = "വിമാനത്താവളം",
            characterName = "Officer Alex",
            characterRole = "Check-in & Immigration",
            openingLine = "Good evening! May I see your passport and boarding pass, please?",
            openingLineMl = "ഗുഡ് ഈവനിംഗ്! പാസ്പോർട്ടും ബോർഡിംഗ് പാസും കാണിക്കാമോ?",
            voicePitch = 0.95f,
            voiceRate = 1.0f,
            surpriseTwist = "Your suitcase is 3 kilograms over the free limit! Will you remove an item or pay for extra baggage?"
        ),
        RealLifeScenario(
            id = "hotel",
            titleEn = "Hotel Check-in",
            titleMl = "ഹോട്ടൽ ചെക്ക്-ഇൻ",
            characterName = "Maya",
            characterRole = "Front Desk Concierge",
            openingLine = "Welcome to Ocean Breeze Hotel! Do you have a reservation under your name?",
            openingLineMl = "ഓഷ്യൻ ബ്രീസ് ഹോട്ടലിലേക്ക് സ്വാഗതം! ബുക്കിംഗ് ഉണ്ടോ?",
            voicePitch = 1.08f,
            voiceRate = 0.96f,
            surpriseTwist = "Oh, the sea-view room is still being cleaned! Would you like a free upgrade to the garden suite or wait 20 minutes?"
        ),
        RealLifeScenario(
            id = "restaurant",
            titleEn = "Restaurant",
            titleMl = "ഹോട്ടലിൽ ഓർഡർ",
            characterName = "Rahul",
            characterRole = "Head Server",
            openingLine = "Welcome! Here is our menu. Would you like to start with something to drink?",
            openingLineMl = "സ്വാഗതം! ഇതാ മെനു. കുടിക്കാൻ എന്തെങ്കിലും ആദ്യം വേണോ?",
            voicePitch = 1.0f,
            voiceRate = 0.98f,
            surpriseTwist = "Oh no, we just sold out of the chicken special! Would you prefer grilled fish or our chef's masala pasta?"
        ),
        RealLifeScenario(
            id = "bank",
            titleEn = "Bank Counter",
            titleMl = "ബാങ്ക് കൗണ്ടർ",
            characterName = "Divya",
            characterRole = "Bank Executive",
            openingLine = "Good morning! How can I assist you with your bank account today?",
            openingLineMl = "ഗുഡ് മോർണിംഗ്! അക്കൗണ്ടുമായി ബന്ധപ്പെട്ട് എന്ത് സഹായമാണ് വേണ്ടത്?",
            voicePitch = 1.1f,
            voiceRate = 0.94f,
            surpriseTwist = "To unlock international transfers, we need a quick security check—what was your last transaction or branch name?"
        ),
        RealLifeScenario(
            id = "shopping",
            titleEn = "Shopping",
            titleMl = "ഷോപ്പിംഗ്",
            characterName = "Kuttan",
            characterRole = "Store Owner",
            openingLine = "Welcome to my store! What are you looking to buy today?",
            openingLineMl = "കടയിലേക്ക് സ്വാഗതം! ഇന്ന് എന്താണ് വാങ്ങാൻ നോക്കുന്നത്?",
            voicePitch = 0.96f,
            voiceRate = 0.94f,
            surpriseTwist = "Actually, this jacket has one last piece left—if you buy two shirts today, I can offer 20% off!"
        ),
        RealLifeScenario(
            id = "office_meeting",
            titleEn = "Office Meeting",
            titleMl = "ഓഫീസ് മീറ്റിംഗ്",
            characterName = "Sarah",
            characterRole = "Team Lead",
            openingLine = "Hi everyone, let us start our quick stand-up! What progress did you make yesterday?",
            openingLineMl = "ഹായ്, മീറ്റിംഗ് തുടങ്ങാം! ഇന്നലെ എന്ത് ജോലിയാണ് ചെയ്തത്?",
            voicePitch = 1.06f,
            voiceRate = 1.02f,
            surpriseTwist = "Wait—the client moved the demo up to 4 PM today! What part do you need help finishing?"
        ),
        RealLifeScenario(
            id = "making_friends",
            titleEn = "Making Friends",
            titleMl = "സുഹൃത്തുക്കളെ ഉണ്ടാക്കൽ",
            characterName = "Arjun",
            characterRole = "Friendly Neighbor",
            openingLine = "Hey! I think you just moved into flat 4B, right? I am Arjun!",
            openingLineMl = "ഹേയ്! നിങ്ങൾ 4B ഫ്ലാറ്റിലേക്ക് പുതിയതായി മാറിയതാണോ? ഞാൻ അർജുൻ!",
            voicePitch = 0.98f,
            voiceRate = 0.98f,
            surpriseTwist = "Oh nice! A few of us are heading to play badminton in 15 minutes—want to join us?"
        )
    )

    val ieltsSpeakingParts: Map<Int, List<String>> = mapOf(
        1 to listOf(
            "Where is your hometown, and what do you like most about it?",
            "Do you prefer working or studying in the morning or at night?",
            "What kind of food do people enjoy in your area?"
        ),
        2 to listOf(
            "Describe a skill you learned outside school or college.\nYou should say:\n• What the skill was\n• Who helped you learn it\n• How long it took\nAnd explain why it is useful to you now."
        ),
        3 to listOf(
            "Why do you think practical communication skills matter more than memorizing rules?",
            "How has technology changed the way adults learn new languages?"
        )
    )

    val ieltsWritingPrompts: Map<Int, List<Pair<String, String>>> = mapOf(
        1 to listOf(
            "Task 1 Letter (General)" to "You recently stayed at a hotel and accidentally left your passport charger and jacket in the room. Write a letter to the hotel manager describing the items, where you left them, and how they can return them to you. (Aim for 150+ words)",
            "Task 1 Chart (Academic)" to "A survey shows daily mobile learning minutes in Kerala rose from 15 mins in 2020 to 48 mins in 2025, while print dictionary use fell from 40% to 12%. Summarize the main trends and make comparisons. (Aim for 150+ words)"
        ),
        2 to listOf(
            "Task 2 Essay" to "Some people think schools should focus mainly on technology and coding, while others believe communication and spoken confidence are equally important. Discuss both views and give your opinion. (Aim for 250+ words)"
        )
    )

    val ieltsListeningSections: List<IeltsListeningSection> = listOf(
        IeltsListeningSection(
            sectionNumber = 1,
            title = "Section 1: Community Library Registration",
            contextLabel = "Everyday Conversation • Form Completion",
            audioScript = "Receptionist: Welcome to Cochin Public Library. Let me register your membership. Your full name is Meera Krishnan, spelled K-R-I-S-H-N-A-N. Annual fee is four hundred and fifty rupees. The reading room opens at eight thirty in the morning and closes at seven in the evening.",
            questions = listOf(
                IeltsQuestion(
                    id = "l1_q1",
                    questionType = "Fill Blank",
                    prompt = "Annual membership fee in rupees:",
                    options = listOf("350", "450", "550", "400"),
                    correctAnswer = "450",
                    explanationEn = "The receptionist clearly says 'four hundred and fifty rupees'.",
                    explanationMl = "വാർഷിക ഫീസ് 450 രൂപയാണെന്ന് റിസപ്ഷനിസ്റ്റ് വ്യക്തമായി പറയുന്നു."
                ),
                IeltsQuestion(
                    id = "l1_q2",
                    questionType = "MCQ",
                    prompt = "When does the reading room open?",
                    options = listOf("8:00 AM", "8:30 AM", "9:00 AM"),
                    correctAnswer = "8:30 AM",
                    explanationEn = "She states it opens at eight thirty in the morning.",
                    explanationMl = "രാവിലെ 8:30-ന് തുറക്കും എന്ന് പറയുന്നു."
                )
            )
        ),
        IeltsListeningSection(
            sectionNumber = 2,
            title = "Section 2: Botanical Garden Audio Tour",
            contextLabel = "Monologue • Map & Facilities",
            audioScript = "Welcome visitors to Hilltop Botanical Park. On your left near the main gate is the Orchid House, famous for monsoon blooms. Please avoid bringing plastic bottles inside the greenhouse. Free drinking water fountains are located behind the Bamboo Cafe.",
            questions = listOf(
                IeltsQuestion(
                    id = "l2_q1",
                    questionType = "MCQ",
                    prompt = "Where are the free water fountains located?",
                    options = listOf("Inside Orchid House", "At the main gate", "Behind the Bamboo Cafe"),
                    correctAnswer = "Behind the Bamboo Cafe",
                    explanationEn = "Water fountains are located behind the Bamboo Cafe.",
                    explanationMl = "ബാംബൂ കഫേയുടെ പിന്നിലാണ് കുടിവെള്ളം ഉള്ളത്."
                )
            )
        ),
        IeltsListeningSection(
            sectionNumber = 3,
            title = "Section 3: Student Research Project",
            contextLabel = "Academic Discussion",
            audioScript = "Tutor: Arun, your survey on solar streetlights has strong data, especially from fifty rural households. However, you should shorten the introduction and add two local interview quotes before Friday's seminar.",
            questions = listOf(
                IeltsQuestion(
                    id = "l3_q1",
                    questionType = "MCQ",
                    prompt = "What did the tutor advise Arun to shorten?",
                    options = listOf("The survey data", "The introduction", "The conclusion"),
                    correctAnswer = "The introduction",
                    explanationEn = "The tutor asks Arun to shorten the introduction.",
                    explanationMl = "ആമുഖം (introduction) ചുരുക്കാൻ അധ്യാപകൻ നിർദ്ദേശിച്ചു."
                )
            )
        ),
        IeltsListeningSection(
            sectionNumber = 4,
            title = "Section 4: Urban Micro-Forests Lecture",
            contextLabel = "Academic Lecture • Note Completion",
            audioScript = "Today we examine urban micro-forests. Planting dense native trees on small city plots cools nearby streets by up to three degrees Celsius within four years. Because native roots hold rainwater deep underground, city flooding drops noticeably.",
            questions = listOf(
                IeltsQuestion(
                    id = "l4_q1",
                    questionType = "Fill Blank",
                    prompt = "Micro-forests cool nearby streets by up to ___ degrees Celsius.",
                    options = listOf("2", "3", "5", "6"),
                    correctAnswer = "3",
                    explanationEn = "The lecturer specifies 'three degrees Celsius'.",
                    explanationMl = "മൂന്ന് ഡിഗ്രി സെൽഷ്യസ് വരെ തണുപ്പ് നൽകുന്നു എന്ന് പറയുന്നു."
                )
            )
        )
    )

    val ieltsReadingPassages: List<IeltsReadingPassage> = listOf(
        IeltsReadingPassage(
            id = "read_1",
            title = "Backwaters & Sustainable Solar Boats",
            body = "Paragraph A: Across Kerala's backwaters, electric and solar ferries are quietly replacing noisy diesel boats. Passengers enjoy peaceful journeys without fuel smoke.\n\n" +
                "Paragraph B: Although solar panels cost more at installation, operators save nearly seventy percent on daily fuel expenses within two years. Local mechanics are also trained to maintain battery packs safely.\n\n" +
                "Paragraph C: Researchers note that fish populations near ferry docks have started recovering because underwater engine vibration has dropped.",
            highlightedVocab = mapOf(
                "sustainable" to "ദീർഘകാലം നിലനിൽക്കുന്ന",
                "installation" to "സ്ഥാപിക്കൽ",
                "expenses" to "ചെലവുകൾ",
                "vibration" to "കമ്പനം / വിറയൽ"
            ),
            questions = listOf(
                IeltsQuestion(
                    id = "r1_q1",
                    questionType = "T/F/NG",
                    prompt = "Solar ferries produce louder engine noise than diesel boats.",
                    options = listOf("True", "False", "Not Given"),
                    correctAnswer = "False",
                    explanationEn = "Paragraph A says solar ferries replace noisy diesel boats and offer peaceful journeys.",
                    explanationMl = "സോളാർ ബോട്ടുകൾ ശബ്ദമില്ലാത്ത സമാധാനപരമായ യാത്രയാണ് നൽകുന്നത്, അതിനാൽ പ്രസ്താവന തെറ്റാണ് (False)."
                ),
                IeltsQuestion(
                    id = "r1_q2",
                    questionType = "Matching Headings",
                    prompt = "Best heading for Paragraph B:",
                    options = listOf("Rising noise levels", "Long-term fuel savings", "Tourism ticket prices"),
                    correctAnswer = "Long-term fuel savings",
                    explanationEn = "Paragraph B focuses on saving 70% on daily fuel expenses.",
                    explanationMl = "രണ്ടാം ഖണ്ഡിക ഇന്ധനച്ചെലവ് 70% കുറയുന്നതിനെക്കുറിച്ചാണ്."
                ),
                IeltsQuestion(
                    id = "r1_q3",
                    questionType = "Fill Blank",
                    prompt = "Operators save nearly ___ percent on daily fuel expenses.",
                    options = listOf("thirty", "fifty", "seventy"),
                    correctAnswer = "seventy",
                    explanationEn = "Paragraph B explicitly mentions 'seventy percent'.",
                    explanationMl = "എഴുപത് ശതമാനം (seventy) ഇന്ധനച്ചെലവ് ലാഭിക്കാം."
                )
            )
        )
    )

    val ieltsVocabulary: List<IeltsVocabItem> = listOf(
        IeltsVocabItem("analyze", "AWL", "വിശകലനം ചെയ്യുക", "Let us analyze the chart before speaking."),
        IeltsVocabItem("significant", "AWL", "പ്രധാനപ്പെട്ട / ശ്രദ്ധേയമായ", "There was a significant rise in solar energy."),
        IeltsVocabItem("contribute", "AWL", "സംഭാവന നൽകുക", "Small daily habits contribute to fluency."),
        IeltsVocabItem("mitigate", "Environment", "ലഘൂകരിക്കുക / കുറയ്ക്കുക", "Planting trees helps mitigate urban heat."),
        IeltsVocabItem("biodiversity", "Environment", "ജൈവവൈവിധ്യം", "Coastal wetlands protect rich biodiversity."),
        IeltsVocabItem("curriculum", "Education", "പാഠ്യപദ്ധതി", "Schools should add speaking practice to the curriculum."),
        IeltsVocabItem("literacy", "Education", "സാക്ഷരത", "Digital literacy opens global opportunities."),
        IeltsVocabItem("automation", "Technology", "ഓട്ടോമേഷൻ / യന്ത്രവൽക്കരണം", "Automation handles repetitive factory work."),
        IeltsVocabItem("innovation", "Technology", "പുതുമ / നവീനത", "Clean-tech innovation is growing rapidly."),
        IeltsVocabItem("preventive", "Health", "പ്രതിരോധപരമായ", "Daily walking is great preventive healthcare."),
        IeltsVocabItem("well-being", "Health", "ആരോഗ്യവും സന്തോഷവും", "Enough sleep improves mental well-being."),
        IeltsVocabItem("resilient", "AWL", "പ്രതിസന്ധികളെ അതിജീവിക്കുന്ന", "A resilient learner keeps practicing after mistakes.")
    )
}
