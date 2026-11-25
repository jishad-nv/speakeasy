package com.example.data.curriculum

import com.example.R
import com.example.model.ChallengeDrill
import com.example.model.ChallengeKind
import com.example.model.EnglishLevel
import com.example.model.LevelCurriculum
import com.example.model.ListenAnswerDrill
import com.example.model.PlacementQuestion
import com.example.model.RoleplayScene
import com.example.model.RoleplayTurn
import com.example.model.ShadowDrill
import com.example.model.WordDrill

object CurriculumRepository {

    val placementQuestions: List<PlacementQuestion> = listOf(
        PlacementQuestion(
            id = 1,
            leoQuestion = "Hello! What is your name and where are you from?",
            malayalamHint = "നിങ്ങളുടെ പേരും സ്ഥലവും ഇംഗ്ലീഷിൽ പറയൂ (ഉദാ: My name is Arun, I am from Kochi).",
            sampleResponse = "My name is Arun and I am from Kochi",
            expectedKeywords = listOf("name", "am", "from"),
            difficultyWeight = 1
        ),
        PlacementQuestion(
            id = 2,
            leoQuestion = "What did you eat for breakfast or lunch today?",
            malayalamHint = "ഇന്ന് രാവിലെ അല്ലെങ്കിൽ ഉച്ചയ്ക്ക് കഴിച്ച ഭക്ഷണം പറയൂ.",
            sampleResponse = "I ate dosa and coffee for breakfast today",
            expectedKeywords = listOf("ate", "had", "eat", "breakfast", "lunch", "coffee", "tea", "rice"),
            difficultyWeight = 2
        ),
        PlacementQuestion(
            id = 3,
            leoQuestion = "Tell me about your favorite movie or hobby and why you like it.",
            malayalamHint = "നിങ്ങൾക്ക് ഇഷ്ടമുള്ള ഒരു സിനിമ അല്ലെങ്കിൽ ഹോബി എന്തിനാണ് ഇഷ്ടമെന്ന് പറയൂ.",
            sampleResponse = "I like watching cricket because it is very exciting with friends",
            expectedKeywords = listOf("like", "love", "because", "favorite", "enjoy"),
            difficultyWeight = 3
        ),
        PlacementQuestion(
            id = 4,
            leoQuestion = "If you missed your bus or train before an important meeting, what would you do?",
            malayalamHint = "പ്രധാനപ്പെട്ട മീറ്റിംഗിന് മുമ്പ് ബസ് മിസ്സായാൽ എന്തുചെയ്യുമെന്ന് പറയൂ.",
            sampleResponse = "I would call my manager immediately and book a cab to reach on time",
            expectedKeywords = listOf("would", "call", "inform", "book", "take", "apologize", "reach"),
            difficultyWeight = 4
        ),
        PlacementQuestion(
            id = 5,
            leoQuestion = "Do you think remote work helps people stay productive? Share your view in two sentences.",
            malayalamHint = "വീട്ടിലിരുന്ന് ജോലി ചെയ്യുന്നത് നല്ലതാണോ എന്ന് രണ്ട് വാക്യത്തിൽ പറയൂ.",
            sampleResponse = "Remote work saves travel time and helps focus, although team communication needs extra effort",
            expectedKeywords = listOf("think", "believe", "time", "work", "because", "however", "although", "balance"),
            difficultyWeight = 5
        )
    )

    val allLevels: List<LevelCurriculum> = listOf(
        // 1. BEGINNER
        LevelCurriculum(
            level = EnglishLevel.BEGINNER,
            shadowDrills = listOf(
                ShadowDrill(
                    id = "b_s1",
                    sentence = "Good morning, how are you doing today?",
                    malayalamHint = "സുപ്രഭാതം, നിങ്ങൾക്ക് ഇന്ന് സുഖമാണോ?",
                    speakingTipEn = "Connect 'how are' smoothly like 'how-are'.",
                    speakingTipMl = "'How are' ഒരുമിച്ച് മൃദുവായി പറയൂ."
                ),
                ShadowDrill(
                    id = "b_s2",
                    sentence = "Could you please give me a glass of water?",
                    malayalamHint = "ദയവായി എനിക്ക് ഒരു ഗ്ലാസ് വെള്ളം തരാമോ?",
                    speakingTipEn = "Say 'glass of water' clearly without rushing.",
                    speakingTipMl = "'Glass of water' വ്യക്തമായി സാവധാനം പറയൂ."
                ),
                ShadowDrill(
                    id = "b_s3",
                    sentence = "I am learning to speak English every day.",
                    malayalamHint = "ഞാൻ എല്ലാ ദിവസവും ഇംഗ്ലീഷ് സംസാരിക്കാൻ പഠിക്കുകയാണ്.",
                    speakingTipEn = "Smile slightly as you say 'every day'.",
                    speakingTipMl = "ആത്മവിശ്വാസത്തോടെ 'every day' പറയൂ."
                )
            ),
            wordDrills = listOf(
                WordDrill("Polite", "puh-LYT", "വിനയമുള്ള", "He is always polite to customers."),
                WordDrill("Fresh", "fresh", "പുതിയ / ഫ്രഷ് ആയ", "I want to buy some fresh bananas."),
                WordDrill("Change", "cheynj", "ബാക്കി പണം / മാറ്റം", "Please keep the small change."),
                WordDrill("Morning", "MOR-ning", "രാവിലെ", "I go for a walk every morning."),
                WordDrill("Help", "help", "സഹായം", "Can you help me find the bus stop?")
            ),
            roleplayScene = RoleplayScene(
                id = "b_rp",
                placeTitleEn = "At the Fruit Shop",
                placeTitleMl = "പഴക്കടയിൽ",
                roleLabelEn = "Shopkeeper Leo",
                turns = listOf(
                    RoleplayTurn(
                        leoLine = "Hello! Welcome to my fruit shop. What would you like today?",
                        leoLineMl = "ഹലോ! എന്റെ പഴക്കടയിലേക്ക് സ്വാഗതം. ഇന്ന് എന്താണ് വേണ്ടത്?",
                        suggestedReply = "Hello, I would like one kilo of bananas please.",
                        hintMl = "ഒരു കിലോ പഴം വേണമെന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Sure! These yellow bananas are fresh. Anything else?",
                        leoLineMl = "തീർച്ചയായും! ഈ പഴങ്ങൾ നല്ല ഫ്രഷ് ആണ്. വേറെ എന്തെങ്കിലും?",
                        suggestedReply = "No thank you, how much is the total?",
                        hintMl = "വേണ്ട നന്ദി, ആകെ എത്ര രൂപയായി എന്ന് ചോദിക്കൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "That will be sixty rupees. Here is your bag!",
                        leoLineMl = "അറുപത് രൂപയാണ്. ഇതാ നിങ്ങളുടെ ബാഗ്!",
                        suggestedReply = "Here is the money. Thank you very much!",
                        hintMl = "ഇതാ പണം, വളരെ നന്ദി എന്ന് പറയൂ."
                    )
                )
            ),
            listenAnswerDrills = listOf(
                ListenAnswerDrill(
                    id = "b_la1",
                    audioQuestion = "Excuse me, what time do you usually wake up?",
                    malayalamHint = "നിങ്ങൾ സാധാരണ എത്ര മണിക്കാണ് ഉണരുന്നത് എന്ന് പറയൂ.",
                    sampleAnswer = "I usually wake up at six thirty in the morning.",
                    expectedKeywords = listOf("wake", "up", "at", "morning", "six", "seven", "eight", "five")
                ),
                ListenAnswerDrill(
                    id = "b_la2",
                    audioQuestion = "What is your favorite food to eat on Sunday?",
                    malayalamHint = "ഞായറാഴ്ച കഴിക്കാൻ ഏറ്റവും ഇഷ്ടമുള്ള ഭക്ഷണം ഏതാണ്?",
                    sampleAnswer = "My favorite food is biryani and chicken curry.",
                    expectedKeywords = listOf("favorite", "food", "like", "eat", "love")
                )
            ),
            challengeDrill = ChallengeDrill(
                id = "b_ch1",
                kind = ChallengeKind.DESCRIBE_PICTURE,
                promptEn = "Look at this Kerala market! Speak 3 short sentences about what you see.",
                promptMl = "ഈ മാർക്കറ്റ് ചിത്രം നോക്കി കാണുന്ന കാര്യങ്ങൾ 3 ചെറിയ വാക്യങ്ങളിൽ പറയൂ.",
                imageRes = R.drawable.img_challenge_market,
                minWordsExpected = 6
            )
        ),

        // 2. ELEMENTARY
        LevelCurriculum(
            level = EnglishLevel.ELEMENTARY,
            shadowDrills = listOf(
                ShadowDrill(
                    id = "e_s1",
                    sentence = "I have been waiting for the doctor since ten o'clock.",
                    malayalamHint = "പത്തു മണി മുതൽ ഞാൻ ഡോക്ടറെ കാത്തിരിക്കുകയാണ്.",
                    speakingTipEn = "Stress 'waiting' and 'ten o'clock'.",
                    speakingTipMl = "'Waiting', 'ten o'clock' എന്നീ വാക്കുകളിൽ ഊന്നൽ നൽകൂ."
                ),
                ShadowDrill(
                    id = "e_s2",
                    sentence = "Could you tell me which platform the Kochi train leaves from?",
                    malayalamHint = "കൊച്ചി ട്രെയിൻ ഏത് പ്ലാറ്റ്‌ഫോമിൽ നിന്നാണ് പോകുന്നതെന്ന് പറയാമോ?",
                    speakingTipEn = "Keep your tone rising politely at the end.",
                    speakingTipMl = "അവസാനം ചോദ്യഭാവത്തിൽ വിനയത്തോടെ പറയൂ."
                ),
                ShadowDrill(
                    id = "e_s3",
                    sentence = "Let me check my schedule and call you back soon.",
                    malayalamHint = "ഞാൻ എന്റെ സമയം നോക്കിയിട്ട് ഉടനെ തിരിച്ചു വിളിക്കാം.",
                    speakingTipEn = "Link 'call you' cleanly.",
                    speakingTipMl = "'Call you back' തടസ്സമില്ലാതെ പറയൂ."
                )
            ),
            wordDrills = listOf(
                WordDrill("Appointment", "uh-POYNT-munt", "അപ്പോയിന്റ്മെന്റ് / കൂടിക്കാഴ്ച", "I booked a doctor appointment for five PM."),
                WordDrill("Symptom", "SIMP-tum", "രോഗലക്ഷണം", "Fever and cough are my main symptoms."),
                WordDrill("Prescription", "pri-SKRIP-shun", "മരുന്ന് കുറിപ്പടി", "Take this prescription to the pharmacy."),
                WordDrill("Available", "uh-VAY-luh-bul", "ലഭ്യമായ", "Is the manager available right now?"),
                WordDrill("Confirm", "kun-FURM", "ഉറപ്പാക്കുക", "Please confirm my seat before Friday.")
            ),
            roleplayScene = RoleplayScene(
                id = "e_rp",
                placeTitleEn = "At the Hospital Clinic",
                placeTitleMl = "ആശുപത്രിയിൽ",
                roleLabelEn = "Nurse Leo",
                turns = listOf(
                    RoleplayTurn(
                        leoLine = "Good afternoon! How can I help you at the clinic today?",
                        leoLineMl = "ഗുഡ് ആഫ്റ്റർനൂൺ! ഇന്ന് ക്ലിനിക്കിൽ എന്ത് സഹായമാണ് വേണ്ടത്?",
                        suggestedReply = "Good afternoon, I have a fever and sore throat since yesterday.",
                        hintMl = "ഇന്നലെ മുതൽ പനിയും തൊണ്ടവേദനയും ഉണ്ടെന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "I see. Do you have an appointment with Dr. Nair?",
                        leoLineMl = "മനസ്സിലായി. ഡോ. നായരെ കാണാൻ അപ്പോയിന്റ്മെന്റ് എടുത്തിട്ടുണ്ടോ?",
                        suggestedReply = "No, I didn't book online. Can I see the doctor today?",
                        hintMl = "ഇല്ല, ഇന്ന് ഡോക്ടറെ കാണാൻ പറ്റുമോ എന്ന് ചോദിക്കൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Yes, please wait in Room 3. The doctor will call you in ten minutes.",
                        leoLineMl = "പറ്റും, റൂം 3-ൽ ഇരിക്കൂ. പത്ത് മിനിറ്റിൽ വിളിക്കും.",
                        suggestedReply = "Thank you, I will wait near Room 3.",
                        hintMl = "നന്ദി, ഞാൻ അവിടെ കാത്തിരിക്കാം എന്ന് പറയൂ."
                    )
                )
            ),
            listenAnswerDrills = listOf(
                ListenAnswerDrill(
                    id = "e_la1",
                    audioQuestion = "Where did you go last weekend and who went with you?",
                    malayalamHint = "കഴിഞ്ഞ വാരാന്ത്യത്തിൽ എവിടെയാണ് പോയത്, ആരൊക്കെ കൂടെയുണ്ടായിരുന്നു?",
                    sampleAnswer = "Last weekend I went to the beach with my family.",
                    expectedKeywords = listOf("went", "weekend", "with", "family", "friends", "home")
                ),
                ListenAnswerDrill(
                    id = "e_la2",
                    audioQuestion = "How do you usually travel to your college or office?",
                    malayalamHint = "നിങ്ങൾ സാധാരണ ഓഫീസിലേക്കോ കോളേജിലേക്കോ എങ്ങനെയാണ് പോകുന്നത്?",
                    sampleAnswer = "I usually take the morning bus or ride my scooter.",
                    expectedKeywords = listOf("bus", "train", "bike", "car", "walk", "metro", "take", "go")
                )
            ),
            challengeDrill = ChallengeDrill(
                id = "e_ch1",
                kind = ChallengeKind.TONGUE_TWISTER,
                promptEn = "Say this tongue twister clearly twice without stopping!",
                promptMl = "ഈ വാക്ക് തടസ്സമില്ലാതെ രണ്ട് തവണ വ്യക്തമായി പറയൂ!",
                targetText = "Fresh fried fish, fresh fried fish, fish fresh fried.",
                minWordsExpected = 5
            )
        ),

        // 3. INTERMEDIATE
        LevelCurriculum(
            level = EnglishLevel.INTERMEDIATE,
            shadowDrills = listOf(
                ShadowDrill(
                    id = "i_s1",
                    sentence = "I really appreciate your help with the client presentation yesterday.",
                    malayalamHint = "ഇന്നലെ ക്ലയന്റ് പ്രസന്റേഷനിൽ സഹായിച്ചതിന് വളരെ നന്ദി.",
                    speakingTipEn = "Pronounce 'appreciate' with four clear beats: uh-PREE-shee-ayt.",
                    speakingTipMl = "'Appreciate' എന്നതിലെ 'shee' ശബ്ദം വ്യക്തമാക്കൂ."
                ),
                ShadowDrill(
                    id = "i_s2",
                    sentence = "Could we reschedule our afternoon call to tomorrow morning instead?",
                    malayalamHint = "ഇന്ന് ഉച്ചയ്ക്കുള്ള കോൾ നാളെ രാവിലത്തേക്ക് മാറ്റാമോ?",
                    speakingTipEn = "Pause slightly after 'call' for natural rhythm.",
                    speakingTipMl = "'Call' എന്നതിന് ശേഷം ചെറിയൊരു ഇടവേള കൊടുക്കൂ."
                ),
                ShadowDrill(
                    id = "i_s3",
                    sentence = "The cafe was crowded, so we decided to grab a coffee to go.",
                    malayalamHint = "കഫേയിൽ തിരക്കായതുകൊണ്ട് ഞങ്ങൾ കോഫി പാഴ്സൽ വാങ്ങാൻ തീരുമാനിച്ചു.",
                    speakingTipEn = "Say 'decided to' smoothly.",
                    speakingTipMl = "'Decided to' ഒഴുക്കോടെ പറയൂ."
                )
            ),
            wordDrills = listOf(
                WordDrill("Reschedule", "ree-SKEJ-ool", "സമയം മാറ്റിവെക്കുക", "Let us reschedule the meeting for Monday."),
                WordDrill("Deadline", "DED-lyn", "അവസാന തീയതി", "We finished the project before the deadline."),
                WordDrill("Smoothly", "SMOOTH-lee", "തടസ്സമില്ലാതെ", "Everything went smoothly during the event."),
                WordDrill("Recommend", "rek-uh-MEND", "ശുപാർശ ചെയ്യുക", "What drink do you recommend here?"),
                WordDrill("Feedback", "FEED-bak", "അഭിപ്രായം", "Thanks for sharing your honest feedback.")
            ),
            roleplayScene = RoleplayScene(
                id = "i_rp",
                placeTitleEn = "At the Office Desk",
                placeTitleMl = "ഓഫീസിൽ",
                roleLabelEn = "Team Lead Leo",
                turns = listOf(
                    RoleplayTurn(
                        leoLine = "Hey! Have you had a chance to review the new sales report yet?",
                        leoLineMl = "ഹേയ്! പുതിയ സെയിൽസ് റിപ്പോർട്ട് നോക്കാൻ സമയം കിട്ടിയോ?",
                        suggestedReply = "Yes, I reviewed it this morning and noticed two small updates we should make.",
                        hintMl = "അതെ, രാവിലെ നോക്കി, രണ്ട് ചെറിയ മാറ്റങ്ങൾ വരുത്താനുണ്ട് എന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Oh good catch! Can you fix those numbers before the 3 PM client call?",
                        leoLineMl = "നല്ല കാര്യം! മൂന്ന് മണിക്ക് മുമ്പ് അത് ശരിയാക്കാമോ?",
                        suggestedReply = "Definitely, I will update the slides and send them to you by two o'clock.",
                        hintMl = "തീർച്ചയായും, രണ്ട് മണിക്ക് മുമ്പ് അപ്ഡേറ്റ് ചെയ്ത് അയക്കാം എന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Awesome teamwork. Let me know if you need any data from finance.",
                        leoLineMl = "കൊള്ളാം. ഫിനാൻസിൽ നിന്ന് വിവരങ്ങൾ വേണമെങ്കിൽ പറയണേ.",
                        suggestedReply = "Thanks, I have everything I need right now. I'll keep you posted!",
                        hintMl = "നന്ദി, ഇപ്പോൾ എല്ലാം കയ്യിലുണ്ട്, വിവരം അറിയിക്കാം എന്ന് പറയൂ."
                    )
                )
            ),
            listenAnswerDrills = listOf(
                ListenAnswerDrill(
                    id = "i_la1",
                    audioQuestion = "If a friend visits Kerala for two days, which place would you take them first?",
                    malayalamHint = "ഒരു സുഹൃത്ത് രണ്ട് ദിവസത്തേക്ക് കേരളത്തിൽ വന്നാൽ ആദ്യം എവിടെ കൊണ്ടുപോകും?",
                    sampleAnswer = "I would take them to Munnar or Alleppey backwaters because the nature is peaceful.",
                    expectedKeywords = listOf("take", "would", "because", "visit", "place", "beautiful", "enjoy")
                ),
                ListenAnswerDrill(
                    id = "i_la2",
                    audioQuestion = "What is one daily habit that helps you stay calm during a busy week?",
                    malayalamHint = "തിരക്കുള്ള ആഴ്ചയിൽ മനസ്സ് ശാന്തമാക്കാൻ സഹായിക്കുന്ന ഒരു ശീലം എന്താണ്?",
                    sampleAnswer = "Listening to music and walking outside in the evening helps me relax.",
                    expectedKeywords = listOf("helps", "relax", "calm", "music", "walk", "sleep", "tea", "read")
                )
            ),
            challengeDrill = ChallengeDrill(
                id = "i_ch1",
                kind = ChallengeKind.DESCRIBE_PICTURE,
                promptEn = "Describe what is happening inside The Sunny Bean cafe in 30 seconds!",
                promptMl = "ഈ കഫേ ചിത്രത്തിൽ കാണുന്ന കാര്യങ്ങൾ 30 സെക്കൻഡിനുള്ളിൽ വിവരിക്കൂ!",
                imageRes = R.drawable.img_challenge_cafe,
                minWordsExpected = 10
            )
        ),

        // 4. UPPER-INTERMEDIATE
        LevelCurriculum(
            level = EnglishLevel.UPPER_INTERMEDIATE,
            shadowDrills = listOf(
                ShadowDrill(
                    id = "u_s1",
                    sentence = "Although the project timeline was tight, our team delivered great quality.",
                    malayalamHint = "സമയം കുറവായിരുന്നെങ്കിലും ഞങ്ങളുടെ ടീം മികച്ച നിലവാരത്തിൽ ചെയ്തു.",
                    speakingTipEn = "Contrast 'tight' and 'great quality' with clear voice energy.",
                    speakingTipMl = "'Tight', 'great quality' എന്നിവയിൽ ശബ്ദത്തിന് വ്യക്തത നൽകൂ."
                ),
                ShadowDrill(
                    id = "u_s2",
                    sentence = "I thrive in collaborative environments where everyone shares ideas openly.",
                    malayalamHint = "എല്ലാവരും തുറന്ന് ആശയങ്ങൾ പങ്കുവെക്കുന്ന ടീമിൽ പ്രവർത്തിക്കാൻ എനിക്ക് ഇഷ്ടമാണ്.",
                    speakingTipEn = "Say 'collaborative' smoothly: kuh-LAB-uh-ray-tiv.",
                    speakingTipMl = "'Collaborative' എന്ന വാക്ക് താളത്തിൽ പറയൂ."
                ),
                ShadowDrill(
                    id = "u_s3",
                    sentence = "From my experience, breaking big tasks into small milestones prevents delays.",
                    malayalamHint = "വലിയ ജോലികൾ ചെറിയ ഘട്ടങ്ങളായി തിരിച്ചാൽ വൈകുന്നത് ഒഴിവാക്കാം.",
                    speakingTipEn = "Pause briefly after 'From my experience'.",
                    speakingTipMl = "'From my experience' ശേഷം ചെറുതായി നിർത്തൂ."
                )
            ),
            wordDrills = listOf(
                WordDrill("Collaborate", "kuh-LAB-uh-rayt", "ഒരുമിച്ച് പ്രവർത്തിക്കുക", "We collaborate closely with the design team."),
                WordDrill("Prioritize", "pry-OR-ih-tyz", "മുൻഗണന നൽകുക", "I prioritize urgent customer issues first."),
                WordDrill("Challenge", "CHAL-inj", "വെല്ലുവിളി", "Handling tight deadlines was a big challenge."),
                WordDrill("Initiative", "ih-NISH-uh-tiv", "മുൻകൈ എടുക്കൽ", "She took the initiative to train new hires."),
                WordDrill("Outcome", "OWT-kum", "ഫലം", "The outcome exceeded our expectations.")
            ),
            roleplayScene = RoleplayScene(
                id = "u_rp",
                placeTitleEn = "Job Interview Room",
                placeTitleMl = "ജോലി അഭിമുഖം",
                roleLabelEn = "Interviewer Leo",
                turns = listOf(
                    RoleplayTurn(
                        leoLine = "Thanks for coming in! Tell me briefly about your biggest strength at work.",
                        leoLineMl = "വന്നതിന് നന്ദി! ജോലിയിലെ നിങ്ങളുടെ ഏറ്റവും വലിയ കരുത്ത് എന്താണെന്ന് പറയൂ.",
                        suggestedReply = "My biggest strength is staying calm under pressure and solving problems quickly.",
                        hintMl = "സമ്മർദ്ദത്തിലും ശാന്തമായി പ്രശ്നങ്ങൾ പരിഹരിക്കാൻ കഴിയുമെന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Great. Can you share a time you handled a disagreement in a team?",
                        leoLineMl = "നല്ലത്. ടീമിൽ അഭിപ്രായവ്യത്യാസം ഉണ്ടായപ്പോൾ എങ്ങനെയാണ് കൈകാര്യം ചെയ്തത്?",
                        suggestedReply = "I listened to both viewpoints carefully and suggested a compromise that met our deadline.",
                        hintMl = "എല്ലാവരുടെയും അഭിപ്രായം കേട്ട് ഒരുമിച്ച് പരിഹാരം കണ്ടു എന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Impressive approach. Do you have any questions for us about the role?",
                        leoLineMl = "മികച്ച സമീപനം. ഈ ജോലിയെക്കുറിച്ച് എന്തെങ്കിലും ചോദിക്കാനുണ്ടോ?",
                        suggestedReply = "Yes, what does success look like in the first ninety days for this position?",
                        hintMl = "ആദ്യ 90 ദിവസത്തിൽ ഈ റോളിൽ നിന്ന് എന്താണ് പ്രതീക്ഷിക്കുന്നതെന്ന് ചോദിക്കൂ."
                    )
                )
            ),
            listenAnswerDrills = listOf(
                ListenAnswerDrill(
                    id = "u_la1",
                    audioQuestion = "What is a skill you taught yourself recently, and how did you practice it?",
                    malayalamHint = "ഈയടുത്ത് സ്വയം പഠിച്ചെടുത്ത ഒരു കഴിവ് എന്താണ്, എങ്ങനെ പരിശീലിച്ചു?",
                    sampleAnswer = "I taught myself video editing by watching tutorials and practicing every evening.",
                    expectedKeywords = listOf("learned", "taught", "practiced", "because", "skill", "watching", "every")
                ),
                ListenAnswerDrill(
                    id = "u_la2",
                    audioQuestion = "Why do you think good communication matters more than working alone?",
                    malayalamHint = "ഒറ്റയ്ക്ക് ജോലി ചെയ്യുന്നതിനേക്കാൾ ആശയവിനിമയം പ്രധാനമാകുന്നത് എന്തുകൊണ്ട്?",
                    sampleAnswer = "Good communication prevents misunderstandings and helps teams move faster together.",
                    expectedKeywords = listOf("communication", "team", "helps", "faster", "understand", "mistakes", "together")
                )
            ),
            challengeDrill = ChallengeDrill(
                id = "u_ch1",
                kind = ChallengeKind.TOPIC_TALK_30S,
                promptEn = "Speak for 30 seconds: Describe your dream career and why it excites you.",
                promptMl = "30 സെക്കൻഡ് സംസാരിക്കൂ: നിങ്ങളുടെ സ്വപ്ന ജോലിയെക്കുറിച്ചും അതിന്റെ കാരണവും പറയൂ.",
                minWordsExpected = 12
            )
        ),

        // 5. ADVANCED
        LevelCurriculum(
            level = EnglishLevel.ADVANCED,
            shadowDrills = listOf(
                ShadowDrill(
                    id = "a_s1",
                    sentence = "While AI automates repetitive tasks, human empathy remains completely irreplaceable.",
                    malayalamHint = "എ.ഐ ആവർത്തന ജോലികൾ ചെയ്യുമ്പോഴും മനുഷ്യന്റെ സഹാനുഭൂതിക്ക് പകരമില്ല.",
                    speakingTipEn = "Give weight to 'irreplaceable' with steady pacing.",
                    speakingTipMl = "'Irreplaceable' എന്ന വാക്ക് വ്യക്തമായ താളത്തിൽ പറയൂ."
                ),
                ShadowDrill(
                    id = "a_s2",
                    sentence = "Let us step back and evaluate the long-term impact before committing resources.",
                    malayalamHint = "വിഭവങ്ങൾ ചെലവഴിക്കുന്നതിന് മുമ്പ് ദീർഘകാല ഫലം വിലയിരുത്താം.",
                    speakingTipEn = "Emphasize 'long-term impact'.",
                    speakingTipMl = "'Long-term impact' ഊന്നിപ്പറയൂ."
                ),
                ShadowDrill(
                    id = "a_s3",
                    sentence = "To put it simply, clarity always beats complexity when presenting to stakeholders.",
                    malayalamHint = "ചുരുക്കിപ്പറഞ്ഞാൽ, അവതരണങ്ങളിൽ സങ്കീർണ്ണതയേക്കാൾ വ്യക്തതയാണ് പ്രധാനം.",
                    speakingTipEn = "Use a crisp pause after 'To put it simply'.",
                    speakingTipMl = "'To put it simply' കഴിഞ്ഞു ചെറുതായി നിർത്തൂ."
                )
            ),
            wordDrills = listOf(
                WordDrill("Perspective", "per-SPEK-tiv", "കാഴ്ചപ്പാട്", "Hearing her perspective changed my mind."),
                WordDrill("Consensus", "kun-SEN-sus", "സമവായം", "We reached a consensus after the debate."),
                WordDrill("Sustainable", "suh-STAY-nuh-bul", "സുസ്ഥിരമായ", "We need a sustainable growth strategy."),
                WordDrill("Articulate", "ar-TIK-yuh-lit", "വ്യക്തമായി അവതരിപ്പിക്കുക", "He can articulate complex ideas simply."),
                WordDrill("Trade-off", "TRAYD-off", "വിട്ടുവീഴ്ച / താരതമ്യ തിരഞ്ഞെടുപ്പ്", "Speed versus cost is a classic trade-off.")
            ),
            roleplayScene = RoleplayScene(
                id = "a_rp",
                placeTitleEn = "International Airport Lounge",
                placeTitleMl = "അന്താരാഷ്ട്ര വിമാനത്താവളം",
                roleLabelEn = "Airline Agent Leo",
                turns = listOf(
                    RoleplayTurn(
                        leoLine = "Good evening. Unfortunately, your connecting flight has been delayed by three hours.",
                        leoLineMl = "ഗുഡ് ഈവനിംഗ്. നിർഭാഗ്യവശാൽ നിങ്ങളുടെ കണക്റ്റിംഗ് ഫ്ലൈറ്റ് മൂന്ന് മണിക്കൂർ വൈകി.",
                        suggestedReply = "Oh, that is concerning because I have an early meeting. Are there any earlier alternatives?",
                        hintMl = "രാവിലെ മീറ്റിംഗ് ഉള്ളതിനാൽ നേരത്തെ വേറെ ഫ്ലൈറ്റ് ഉണ്ടോ എന്ന് ചോദിക്കൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "I can put you on a partner airline departing in forty minutes, though it has a short layover.",
                        leoLineMl = "40 മിനിറ്റിൽ പോകുന്ന മറ്റൊരു ഫ്ലൈറ്റിൽ മാറ്റാം, പക്ഷെ ചെറിയ ലേഓവർ ഉണ്ട്.",
                        suggestedReply = "That sounds much better, provided my checked luggage transfers automatically.",
                        hintMl = "ബാഗേജ് നേരിട്ട് എത്തുമെങ്കിൽ അതാണ് നല്ലതെന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Confirmed—your bag is tagged straight through and here is your lounge pass.",
                        leoLineMl = "ബാഗ് നേരിട്ട് എത്തും, ഇതാ ലോഞ്ച് പാസ്.",
                        suggestedReply = "I truly appreciate your prompt help. Have a wonderful evening!",
                        hintMl = "വേഗത്തിലുള്ള സഹായത്തിന് നന്ദി പറഞ്ഞ് യാത്ര പറയൂ."
                    )
                )
            ),
            listenAnswerDrills = listOf(
                ListenAnswerDrill(
                    id = "a_la1",
                    audioQuestion = "Should cities invest more in public transport than wider highways? Why?",
                    malayalamHint = "നഗരങ്ങൾ കൂടുതൽ നിക്ഷേപിക്കേണ്ടത് പൊതുഗതാഗതത്തിലാണോ അതോ വലിയ റോഡുകളിലാണോ?",
                    sampleAnswer = "Investing in public transport reduces traffic congestion and cuts pollution more sustainably.",
                    expectedKeywords = listOf("public", "transport", "traffic", "pollution", "because", "cities", "better")
                ),
                ListenAnswerDrill(
                    id = "a_la2",
                    audioQuestion = "How can someone stay persuasive during a disagreement without sounding aggressive?",
                    malayalamHint = "അഭിപ്രായവ്യത്യാസത്തിൽ ദേഷ്യപ്പെടാതെ എങ്ങനെ മറ്റുള്ളവരെ ബോധ്യപ്പെടുത്താം?",
                    sampleAnswer = "By acknowledging the other person's concern first and backing up points with calm examples.",
                    expectedKeywords = listOf("listen", "calm", "respect", "example", "acknowledge", "evidence", "tone")
                )
            ),
            challengeDrill = ChallengeDrill(
                id = "a_ch1",
                kind = ChallengeKind.DESCRIBE_PICTURE,
                promptEn = "Describe the mood, people, and details in this airport terminal like a travel reporter!",
                promptMl = "ഒരു ട്രാവൽ റിപ്പോർട്ടറെപ്പോലെ ഈ എയർപോർട്ട് ദൃശ്യം വിവരിക്കൂ!",
                imageRes = R.drawable.img_challenge_airport,
                minWordsExpected = 14
            )
        ),

        // 6. MASTER
        LevelCurriculum(
            level = EnglishLevel.MASTER,
            shadowDrills = listOf(
                ShadowDrill(
                    id = "m_s1",
                    sentence = "Mastering fluency isn't about sounding foreign; it's about expressing yourself effortlessly.",
                    malayalamHint = "ഒഴുക്കോടെ സംസാരിക്കുകയെന്നാൽ അനുകരിക്കലല്ല, സ്വയം അനായാസം പ്രകടിപ്പിക്കലാണ്.",
                    speakingTipEn = "Deliver both halves with balanced rhythm and warmth.",
                    speakingTipMl = "രണ്ട് ഭാഗങ്ങളും സ്വാഭാവികമായ താളത്തിൽ പറയൂ."
                ),
                ShadowDrill(
                    id = "m_s2",
                    sentence = "Despite initial skepticism, the pilot program demonstrated measurable value within weeks.",
                    malayalamHint = "തുടക്കത്തിലെ സംശയങ്ങൾക്കിടയിലും പൈലറ്റ് പ്രോഗ്രാം ആഴ്ചകൾക്കുള്ളിൽ ഫലം കണ്ടു.",
                    speakingTipEn = "Articulate 'demonstrated measurable value' clearly.",
                    speakingTipMl = "'Measurable value' വ്യക്തമായി ഉച്ചരിക്കൂ."
                ),
                ShadowDrill(
                    id = "m_s3",
                    sentence = "True leadership shows up when ambiguity is high and the path forward isn't obvious.",
                    malayalamHint = "വ്യക്തതയില്ലാത്ത പ്രതിസന്ധി ഘട്ടങ്ങളിലാണ് യഥാർത്ഥ നേതൃത്വം തെളിയുന്നത്.",
                    speakingTipEn = "Pause for half a beat after 'leadership'.",
                    speakingTipMl = "'Leadership' കഴിഞ്ഞു ചെറിയൊരു ഇടവേള എടുക്കൂ."
                )
            ),
            wordDrills = listOf(
                WordDrill("Nuance", "NOO-ahns", "സൂക്ഷ്മമായ വ്യത്യാസം", "Great speakers notice subtle nuances in tone."),
                WordDrill("Ambiguity", "am-bih-GYOO-ih-tee", "അവ്യക്തത", "Leaders stay calm even amidst ambiguity."),
                WordDrill("Compelling", "kum-PEL-ing", "ആകർഷകവും ശക്തവുമായ", "She told a compelling story in two minutes."),
                WordDrill("Resilience", "rih-ZIL-yuns", "പ്രതിസന്ധി അതിജീവനശേഷി", "Adaptability and resilience drive growth."),
                WordDrill("Eloquent", "EL-uh-kwunt", "വാഗ്‌വിലാസമുള്ള", "His closing remarks were brief yet eloquent.")
            ),
            roleplayScene = RoleplayScene(
                id = "m_rp",
                placeTitleEn = "Executive Product Pitch",
                placeTitleMl = "എക്സിക്യൂട്ടീവ് പിച്ച് മീറ്റിംഗ്",
                roleLabelEn = "VP Leo",
                turns = listOf(
                    RoleplayTurn(
                        leoLine = "We only have five minutes. Why should we fund your community learning idea?",
                        leoLineMl = "നമുക്ക് അഞ്ച് മിനിറ്റേ ഉള്ളൂ. നിങ്ങളുടെ ആശയത്തിന് എന്തിനാണ് ഫണ്ട് നൽകേണ്ടത്?",
                        suggestedReply = "Because learners quit passive apps, whereas daily voice practice triples retention.",
                        hintMl = "വോയ്‌സ് പരിശീലനം പഠനത്തിനുള്ള താല്പര്യം മൂന്നിരട്ടിയാക്കുന്നു എന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "Bold claim. What happens if users feel shy speaking out loud?",
                        leoLineMl = "ശരി, പക്ഷെ ഉറക്കെ സംസാരിക്കാൻ ആളുകൾക്ക് മടി തോന്നിയാലോ?",
                        suggestedReply = "We start with sixty-second low-stakes prompts and friendly feedback so confidence builds naturally.",
                        hintMl = "ചെറിയ ലളിതമായ പരിശീലനത്തിലൂടെ ആത്മവിശ്വാസം വളർത്തുമെന്ന് പറയൂ."
                    ),
                    RoleplayTurn(
                        leoLine = "That's a sharp insight. Send me a one-page rollout plan by Thursday.",
                        leoLineMl = "മികച്ച ചിന്ത. വ്യാഴാഴ്ചയ്ക്ക് മുമ്പ് പ്ലാൻ അയക്കൂ.",
                        suggestedReply = "You'll have it tomorrow morning along with pilot metrics. Thank you!",
                        hintMl = "നാളെ രാവിലെ തന്നെ വിവരങ്ങൾ അയക്കാം എന്ന് പറഞ്ഞ് നന്ദി അറിയിക്കൂ."
                    )
                )
            ),
            listenAnswerDrills = listOf(
                ListenAnswerDrill(
                    id = "m_la1",
                    audioQuestion = "What separates someone who knows English grammar from someone who speaks confidently?",
                    malayalamHint = "വ്യാകരണം അറിയുന്നയാളെയും ആത്മവിശ്വാസത്തോടെ സംസാരിക്കുന്നയാളെയും വേർതിരിക്കുന്നത് എന്താണ്?",
                    sampleAnswer = "Confidence comes from thinking aloud without fear of mistakes rather than memorizing rules.",
                    expectedKeywords = listOf("confidence", "practice", "mistakes", "fear", "speaking", "natural", "rules")
                ),
                ListenAnswerDrill(
                    id = "m_la2",
                    audioQuestion = "Describe one technological change in Kerala over the last decade that amazed you.",
                    malayalamHint = "കഴിഞ്ഞ പത്ത് വർഷത്തിനിടെ കേരളത്തിൽ കണ്ട അത്ഭുതപ്പെടുത്തിയ ഒരു സാങ്കേതിക മാറ്റം പറയൂ.",
                    sampleAnswer = "Digital payments everywhere from small tea stalls to buses transformed everyday convenience.",
                    expectedKeywords = listOf("digital", "upi", "payment", "internet", "mobile", "changed", "easier", "kerala")
                )
            ),
            challengeDrill = ChallengeDrill(
                id = "m_ch1",
                kind = ChallengeKind.TONGUE_TWISTER,
                promptEn = "Master Speed Challenge: Say this clearly in one smooth breath!",
                promptMl = "മാസ്റ്റർ ചലഞ്ച്: ഒറ്റ ശ്വാസത്തിൽ തെറ്റാതെ പറയൂ!",
                targetText = "Unique New York, unique New York, you know you need unique New York.",
                minWordsExpected = 8
            )
        )
    )

    fun getCurriculum(level: EnglishLevel): LevelCurriculum =
        allLevels.firstOrNull { it.level == level } ?: allLevels.first()

    fun generate30DayOfflineDrills(): List<com.example.data.local.OfflineDayDrillEntity> {
        val result = mutableListOf<com.example.data.local.OfflineDayDrillEntity>()
        val extraTopics = listOf(
            "Ordering breakfast at a local restaurant",
            "Asking for directions to the nearest metro station",
            "Introducing yourself to a new neighbor",
            "Explaining a delay politely to your manager",
            "Shopping for fresh vegetables and asking prices",
            "Talking about your weekend plans with a friend"
        )
        allLevels.forEach { curr ->
            for (day in 1..30) {
                val shadow = curr.shadowDrills[(day - 1) % curr.shadowDrills.size]
                val word = curr.wordDrills[(day - 1) % curr.wordDrills.size]
                val turn = curr.roleplayScene.turns[(day - 1) % curr.roleplayScene.turns.size]
                val listen = curr.listenAnswerDrills[(day - 1) % curr.listenAnswerDrills.size]
                val ch = curr.challengeDrill
                val topicVariation = extraTopics[(day - 1) % extraTopics.size]

                result.add(
                    com.example.data.local.OfflineDayDrillEntity(
                        id = "${curr.level.index}_d${day}_shadow",
                        levelIndex = curr.level.index,
                        dayNumber = day,
                        taskTypeId = "shadow",
                        promptText = shadow.sentence,
                        malayalamHint = shadow.malayalamHint,
                        audioText = shadow.sentence
                    )
                )
                result.add(
                    com.example.data.local.OfflineDayDrillEntity(
                        id = "${curr.level.index}_d${day}_words",
                        levelIndex = curr.level.index,
                        dayNumber = day,
                        taskTypeId = "words",
                        promptText = "${word.word}: ${word.sampleSentence}",
                        malayalamHint = word.malayalamMeaning,
                        audioText = "${word.word}. ${word.sampleSentence}"
                    )
                )
                result.add(
                    com.example.data.local.OfflineDayDrillEntity(
                        id = "${curr.level.index}_d${day}_roleplay",
                        levelIndex = curr.level.index,
                        dayNumber = day,
                        taskTypeId = "roleplay",
                        promptText = "${turn.leoLine} ($topicVariation)",
                        malayalamHint = turn.hintMl,
                        audioText = turn.leoLine
                    )
                )
                result.add(
                    com.example.data.local.OfflineDayDrillEntity(
                        id = "${curr.level.index}_d${day}_listen",
                        levelIndex = curr.level.index,
                        dayNumber = day,
                        taskTypeId = "listen_answer",
                        promptText = listen.audioQuestion,
                        malayalamHint = listen.malayalamHint,
                        audioText = listen.audioQuestion
                    )
                )
                result.add(
                    com.example.data.local.OfflineDayDrillEntity(
                        id = "${curr.level.index}_d${day}_challenge",
                        levelIndex = curr.level.index,
                        dayNumber = day,
                        taskTypeId = "challenge",
                        promptText = ch.targetText ?: "${ch.promptEn} ($topicVariation)",
                        malayalamHint = ch.promptMl,
                        audioText = ch.targetText ?: ch.promptEn
                    )
                )
            }
        }
        return result
    }
}
