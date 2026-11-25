package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.DifficultyTier
import com.example.model.EnglishLevel
import com.example.model.LearningGoal
import com.example.model.LeoPersonality
import com.example.model.SupportLevel
import com.example.model.ThemeMode
import com.example.model.UserProfileState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val Context.speakUpDataStore: DataStore<Preferences> by preferencesDataStore(name = "speakup_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val GOAL = stringPreferencesKey("goal")
        val DAILY_MINUTES_GOAL = intPreferencesKey("daily_minutes_goal")
        val REMINDER_TIMES = stringPreferencesKey("reminder_times")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val PERSONALITY = stringPreferencesKey("leo_personality")
        val LANGUAGE = stringPreferencesKey("language")
        val SUPPORT_LEVEL = stringPreferencesKey("support_level")
        val SUPPORT_OVERRIDDEN = booleanPreferencesKey("support_overridden")
        val SOURCE_LANG_CODE = stringPreferencesKey("source_lang_code")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LEVEL_INDEX = intPreferencesKey("level_index")
        val DIFFICULTY_TIER = stringPreferencesKey("difficulty_tier")
        val XP = intPreferencesKey("xp")
        val STREAK_DAYS = intPreferencesKey("streak_days")
        val STREAK_FREEZES = intPreferencesKey("streak_freezes")
        val STREAK_FREEZE_ACTIVE = booleanPreferencesKey("streak_freeze_active")
        val COMPLETED_TASKS_TODAY = stringPreferencesKey("completed_tasks_today")
        val LAST_PRACTICE_DATE = stringPreferencesKey("last_practice_date")
        val TODAY_MINUTES_PRACTICED = intPreferencesKey("today_minutes_practiced")
        val OFFLINE_DOWNLOADED = booleanPreferencesKey("offline_downloaded")
        val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        val LAST_SYNC_TS = longPreferencesKey("last_sync_ts")
        val IELTS_TARGET_BAND = doublePreferencesKey("ielts_target_band")
        val IELTS_CURRENT_BAND = doublePreferencesKey("ielts_current_band")
        val IELTS_EXAM_DATE = stringPreferencesKey("ielts_exam_date")
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun defaultExamDateKey(): String {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun computeDaysUntilExam(examDateKey: String): Int {
        if (examDateKey.isBlank()) return 30
        return runCatching {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val exam = fmt.parse(examDateKey) ?: return 30
            val today = fmt.parse(todayKey()) ?: return 30
            TimeUnit.MILLISECONDS.toDays(exam.time - today.time).toInt().coerceAtLeast(0)
        }.getOrDefault(30)
    }

    private fun autoSupportForLevel(level: EnglishLevel): SupportLevel = when {
        level.index <= EnglishLevel.BEGINNER.index -> SupportLevel.FULL_MALAYALAM
        level.index <= EnglishLevel.INTERMEDIATE.index -> SupportLevel.MIXED
        else -> SupportLevel.ENGLISH_ONLY
    }

    private fun computeMissedDays(lastDateStr: String): Int {
        if (lastDateStr.isBlank()) return 0
        return runCatching {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val last = fmt.parse(lastDateStr) ?: return 0
            val today = fmt.parse(todayKey()) ?: return 0
            val diffDays = TimeUnit.MILLISECONDS.toDays(today.time - last.time).toInt()
            (diffDays - 1).coerceAtLeast(0)
        }.getOrDefault(0)
    }

    val userProfileFlow: Flow<UserProfileState> = context.speakUpDataStore.data.map { prefs ->
        val today = todayKey()
        val storedDate = prefs[Keys.LAST_PRACTICE_DATE] ?: ""
        val isSameDay = storedDate == today

        val completedToday = if (isSameDay) {
            prefs[Keys.COMPLETED_TASKS_TODAY]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet()
        } else {
            emptySet()
        }

        val reminders = prefs[Keys.REMINDER_TIMES]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: listOf("08:30", "20:00")

        val level = EnglishLevel.fromIndex(prefs[Keys.LEVEL_INDEX] ?: 0)
        val support = runCatching {
            SupportLevel.valueOf(prefs[Keys.SUPPORT_LEVEL] ?: autoSupportForLevel(level).name)
        }.getOrDefault(autoSupportForLevel(level))

        val examDate = prefs[Keys.IELTS_EXAM_DATE]?.takeIf { it.isNotBlank() } ?: defaultExamDateKey()

        val state = UserProfileState(
            onboardingCompleted = prefs[Keys.ONBOARDING_DONE] ?: false,
            goal = runCatching { LearningGoal.valueOf(prefs[Keys.GOAL] ?: "DAILY_LIFE") }
                .getOrDefault(LearningGoal.DAILY_LIFE),
            dailyMinutesGoal = prefs[Keys.DAILY_MINUTES_GOAL] ?: 10,
            reminderTimes = reminders,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            leoPersonality = runCatching { LeoPersonality.valueOf(prefs[Keys.PERSONALITY] ?: "FRIENDLY") }
                .getOrDefault(LeoPersonality.FRIENDLY),
            language = runCatching { AppLanguage.valueOf(prefs[Keys.LANGUAGE] ?: "ENGLISH") }
                .getOrDefault(AppLanguage.ENGLISH),
            supportLevel = support,
            supportOverriddenByUser = prefs[Keys.SUPPORT_OVERRIDDEN] ?: false,
            sourceLanguageCode = prefs[Keys.SOURCE_LANG_CODE] ?: "ml",
            themeMode = runCatching { ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: "SYSTEM") }
                .getOrDefault(ThemeMode.SYSTEM),
            currentLevel = level,
            difficultyTier = runCatching { DifficultyTier.valueOf(prefs[Keys.DIFFICULTY_TIER] ?: "STANDARD") }
                .getOrDefault(DifficultyTier.STANDARD),
            xp = prefs[Keys.XP] ?: 0,
            streakDays = prefs[Keys.STREAK_DAYS] ?: 1,
            streakFreezes = prefs[Keys.STREAK_FREEZES] ?: 1,
            streakFreezeActive = prefs[Keys.STREAK_FREEZE_ACTIVE] ?: false,
            missedDays = computeMissedDays(storedDate),
            completedTaskIdsToday = completedToday,
            todayMinutesPracticed = if (isSameDay) (prefs[Keys.TODAY_MINUTES_PRACTICED] ?: 0) else 0,
            offlineDownloaded = prefs[Keys.OFFLINE_DOWNLOADED] ?: false,
            soundEffectsEnabled = prefs[Keys.SOUND_EFFECTS] ?: true,
            lastSyncTimestamp = prefs[Keys.LAST_SYNC_TS] ?: 0L,
            ieltsTargetBand = prefs[Keys.IELTS_TARGET_BAND] ?: 7.0,
            ieltsCurrentBand = prefs[Keys.IELTS_CURRENT_BAND] ?: 5.5,
            ieltsExamDateKey = examDate
        )
        mirrorReminderSnapshot(state)
        state
    }

    private fun mirrorReminderSnapshot(state: UserProfileState) {
        val sp = context.getSharedPreferences("speakup_reminder_meta", Context.MODE_PRIVATE)
        sp.edit()
            .putString("today_key", todayKey())
            .putInt("completed_count", state.completedTaskIdsToday.size)
            .putInt("streak_days", state.streakDays)
            .putInt("missed_days", state.missedDays)
            .putInt("ielts_days_left", computeDaysUntilExam(state.ieltsExamDateKey))
            .apply()
    }

    fun updateMistakeNotificationCount(count: Int) {
        val sp = context.getSharedPreferences("speakup_reminder_meta", Context.MODE_PRIVATE)
        sp.edit().putInt("unfixed_mistakes_count", count).apply()
    }

    suspend fun completeOnboarding(
        goal: LearningGoal,
        dailyMinutes: Int,
        reminders: List<String>,
        notificationsEnabled: Boolean,
        assignedLevel: EnglishLevel
    ) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_DONE] = true
            prefs[Keys.GOAL] = goal.name
            prefs[Keys.DAILY_MINUTES_GOAL] = dailyMinutes
            prefs[Keys.REMINDER_TIMES] = reminders.take(3).joinToString(",")
            prefs[Keys.NOTIFICATIONS_ENABLED] = notificationsEnabled
            prefs[Keys.LEVEL_INDEX] = assignedLevel.index
            if (prefs[Keys.SUPPORT_OVERRIDDEN] != true) {
                prefs[Keys.SUPPORT_LEVEL] = autoSupportForLevel(assignedLevel).name
            }
            prefs[Keys.XP] = maxOf(prefs[Keys.XP] ?: 0, assignedLevel.xpRequired)
            prefs[Keys.STREAK_DAYS] = maxOf(prefs[Keys.STREAK_DAYS] ?: 1, 1)
            prefs[Keys.STREAK_FREEZES] = prefs[Keys.STREAK_FREEZES] ?: 1
        }
    }

    suspend fun recordTaskCompletion(taskId: String, minutes: Int, xpEarned: Int): EnglishLevel? {
        var leveledUpTo: EnglishLevel? = null
        val today = todayKey()
        context.speakUpDataStore.edit { prefs ->
            val prevDate = prefs[Keys.LAST_PRACTICE_DATE] ?: ""
            val existingTasks = if (prevDate == today) {
                prefs[Keys.COMPLETED_TASKS_TODAY]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet()
                    ?: mutableSetOf()
            } else {
                mutableSetOf()
            }
            existingTasks.add(taskId)
            prefs[Keys.COMPLETED_TASKS_TODAY] = existingTasks.joinToString(",")
            prefs[Keys.LAST_PRACTICE_DATE] = today

            val prevMinutes = if (prevDate == today) (prefs[Keys.TODAY_MINUTES_PRACTICED] ?: 0) else 0
            prefs[Keys.TODAY_MINUTES_PRACTICED] = prevMinutes + minutes

            if (prevDate != today && prevDate.isNotBlank()) {
                val missed = computeMissedDays(prevDate)
                if (missed == 0 || prefs[Keys.STREAK_FREEZE_ACTIVE] == true) {
                    prefs[Keys.STREAK_DAYS] = (prefs[Keys.STREAK_DAYS] ?: 1) + 1
                } else {
                    prefs[Keys.STREAK_DAYS] = 1
                }
                prefs[Keys.STREAK_FREEZE_ACTIVE] = false
            }

            val oldLevelIndex = prefs[Keys.LEVEL_INDEX] ?: 0
            val newXp = (prefs[Keys.XP] ?: 0) + xpEarned
            prefs[Keys.XP] = newXp

            val xpLevel = EnglishLevel.forXp(newXp)
            if (xpLevel.index > oldLevelIndex) {
                prefs[Keys.LEVEL_INDEX] = xpLevel.index
                if (prefs[Keys.SUPPORT_OVERRIDDEN] != true) {
                    prefs[Keys.SUPPORT_LEVEL] = autoSupportForLevel(xpLevel).name
                }
                leveledUpTo = xpLevel
            }
        }
        return leveledUpTo
    }

    suspend fun setDifficultyAndLevel(tier: DifficultyTier, promotedLevel: EnglishLevel?) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.DIFFICULTY_TIER] = tier.name
            if (promotedLevel != null) {
                prefs[Keys.LEVEL_INDEX] = promotedLevel.index
                if (prefs[Keys.SUPPORT_OVERRIDDEN] != true) {
                    prefs[Keys.SUPPORT_LEVEL] = autoSupportForLevel(promotedLevel).name
                }
                if ((prefs[Keys.XP] ?: 0) < promotedLevel.xpRequired) {
                    prefs[Keys.XP] = promotedLevel.xpRequired
                }
            }
        }
    }

    suspend fun setSupportLevel(support: SupportLevel, userOverrode: Boolean = true) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.SUPPORT_LEVEL] = support.name
            prefs[Keys.SUPPORT_OVERRIDDEN] = userOverrode
        }
    }

    suspend fun setSourceLanguageCode(code: String) {
        context.speakUpDataStore.edit { prefs -> prefs[Keys.SOURCE_LANG_CODE] = code }
    }

    suspend fun updateIeltsConfig(targetBand: Double, examDateKey: String) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.IELTS_TARGET_BAND] = targetBand.coerceIn(5.5, 8.5)
            prefs[Keys.IELTS_EXAM_DATE] = examDateKey
        }
    }

    suspend fun updateIeltsEstimatedBand(band: Double) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.IELTS_CURRENT_BAND] = band.coerceIn(0.0, 9.0)
        }
    }

    suspend fun markSyncedNow() {
        context.speakUpDataStore.edit { it[Keys.LAST_SYNC_TS] = System.currentTimeMillis() }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.speakUpDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun toggleLanguage() {
        context.speakUpDataStore.edit { prefs ->
            val current = prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.name
            prefs[Keys.LANGUAGE] = if (current == AppLanguage.ENGLISH.name) {
                AppLanguage.MALAYALAM.name
            } else {
                AppLanguage.ENGLISH.name
            }
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.speakUpDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setPersonality(personality: LeoPersonality) {
        context.speakUpDataStore.edit { it[Keys.PERSONALITY] = personality.name }
    }

    suspend fun setDailyMinutesGoal(minutes: Int) {
        context.speakUpDataStore.edit { it[Keys.DAILY_MINUTES_GOAL] = minutes }
    }

    suspend fun setGoal(goal: LearningGoal) {
        context.speakUpDataStore.edit { it[Keys.GOAL] = goal.name }
    }

    suspend fun setReminders(times: List<String>, enabled: Boolean) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.REMINDER_TIMES] = times.take(3).joinToString(",")
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun activateStreakFreeze() {
        context.speakUpDataStore.edit { prefs ->
            val freezes = prefs[Keys.STREAK_FREEZES] ?: 0
            if (freezes > 0 && prefs[Keys.STREAK_FREEZE_ACTIVE] != true) {
                prefs[Keys.STREAK_FREEZES] = freezes - 1
                prefs[Keys.STREAK_FREEZE_ACTIVE] = true
            }
        }
    }

    suspend fun setOfflineDownloaded(downloaded: Boolean) {
        context.speakUpDataStore.edit { it[Keys.OFFLINE_DOWNLOADED] = downloaded }
    }

    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.speakUpDataStore.edit { it[Keys.SOUND_EFFECTS] = enabled }
    }

    suspend fun setCurrentLevel(level: EnglishLevel) {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.LEVEL_INDEX] = level.index
            if (prefs[Keys.SUPPORT_OVERRIDDEN] != true) {
                prefs[Keys.SUPPORT_LEVEL] = autoSupportForLevel(level).name
            }
            if ((prefs[Keys.XP] ?: 0) < level.xpRequired) {
                prefs[Keys.XP] = level.xpRequired
            }
        }
    }

    suspend fun resetPlacementForRetake() {
        context.speakUpDataStore.edit { it[Keys.ONBOARDING_DONE] = false }
    }

    suspend fun resetAllProgress() {
        context.speakUpDataStore.edit { prefs ->
            prefs[Keys.XP] = 0
            prefs[Keys.LEVEL_INDEX] = 0
            prefs[Keys.DIFFICULTY_TIER] = DifficultyTier.STANDARD.name
            prefs[Keys.SUPPORT_LEVEL] = SupportLevel.FULL_MALAYALAM.name
            prefs[Keys.SUPPORT_OVERRIDDEN] = false
            prefs[Keys.STREAK_DAYS] = 1
            prefs[Keys.STREAK_FREEZES] = 1
            prefs[Keys.STREAK_FREEZE_ACTIVE] = false
            prefs[Keys.COMPLETED_TASKS_TODAY] = ""
            prefs[Keys.TODAY_MINUTES_PRACTICED] = 0
            prefs[Keys.LAST_PRACTICE_DATE] = ""
            prefs[Keys.LAST_SYNC_TS] = 0L
        }
    }
}
