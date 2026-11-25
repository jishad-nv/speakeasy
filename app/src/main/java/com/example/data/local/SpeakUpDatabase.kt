package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "practice_sessions")
data class PracticeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateKey: String,
    val dayLabel: String,
    val taskId: String,
    val levelIndex: Int,
    val minutesSpent: Int,
    val stars: Int,
    val scorePercent: Int = stars * 20,
    val xpEarned: Int,
    val isOfflineScore: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "learned_words")
data class LearnedWordEntity(
    @PrimaryKey val word: String,
    val malayalamMeaning: String,
    val sampleSentence: String,
    val levelIndex: Int,
    val learnedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_packs")
data class OfflinePackEntity(
    @PrimaryKey val levelIndex: Int,
    val levelTitle: String,
    val drillsCount: Int,
    val daysBundled: Int = 30,
    val downloadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_day_drills")
data class OfflineDayDrillEntity(
    @PrimaryKey val id: String,
    val levelIndex: Int,
    val dayNumber: Int,
    val taskTypeId: String,
    val promptText: String,
    val malayalamHint: String,
    val audioText: String
)

@Entity(tableName = "srs_items")
data class SrsItemEntity(
    @PrimaryKey val itemId: String,
    val term: String,
    val malayalamHint: String,
    val sampleSentence: String,
    val itemType: String, // "word", "sentence", "camera_word", "ielts_vocab"
    val levelIndex: Int,
    val repetitions: Int = 0,
    val intervalDays: Int = 1,
    val easeFactor: Double = 2.5,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastScore: Int = 0
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val actionType: String,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventName: String,
    val metadata: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mistake_entries")
data class MistakeEntryEntity(
    @PrimaryKey val id: String,
    val category: String, // "pronunciation", "word_choice", "sentence_pattern"
    val wrongPattern: String,
    val correctForm: String,
    val malayalamNote: String,
    val occurrences: Int = 1,
    val fixedStreak: Int = 0,
    val isFixed: Boolean = false,
    val lastSeenAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(tableName = "sound_progress")
data class SoundProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val soundTargetId: String,
    val score: Int,
    val weekKey: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ielts_attempts")
data class IeltsAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val skill: String, // "speaking", "writing", "listening", "reading", "mock"
    val bandScore: Double,
    val weakestQuestionType: String,
    val detailsSummary: String,
    val isMockTest: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val spokenOriginal: String,
    val naturalVersion: String,
    val dateKey: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "zero_phrases_offline")
data class ZeroPhraseOfflineEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val englishText: String,
    val malayalamMeaning: String,
    val manglishScript: String
)

@Entity(tableName = "ielts_starter_packs")
data class IeltsStarterPackEntity(
    @PrimaryKey val id: String,
    val skillType: String,
    val title: String,
    val payloadJson: String
)

@Dao
interface SpeakUpDao {
    @Query("SELECT * FROM practice_sessions ORDER BY timestamp DESC")
    fun observeAllSessions(): Flow<List<PracticeSessionEntity>>

    @Query("SELECT scorePercent FROM practice_sessions ORDER BY timestamp DESC LIMIT 5")
    suspend fun getRecentScores(): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PracticeSessionEntity)

    @Query("SELECT * FROM learned_words ORDER BY learnedAt DESC")
    fun observeLearnedWords(): Flow<List<LearnedWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: LearnedWordEntity)

    @Query("SELECT * FROM offline_packs ORDER BY levelIndex ASC")
    fun observeOfflinePacks(): Flow<List<OfflinePackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflinePacks(packs: List<OfflinePackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOfflineDayDrills(drills: List<OfflineDayDrillEntity>)

    @Query("SELECT COUNT(*) FROM offline_day_drills")
    fun observeOfflineDrillsCount(): Flow<Int>

    @Query("SELECT * FROM srs_items ORDER BY nextReviewAt ASC")
    fun observeAllSrsItems(): Flow<List<SrsItemEntity>>

    @Query("SELECT * FROM srs_items WHERE nextReviewAt <= :nowMs ORDER BY lastScore ASC LIMIT 5")
    suspend fun getDueSrsItems(nowMs: Long): List<SrsItemEntity>

    @Query("SELECT * FROM srs_items WHERE itemId = :itemId LIMIT 1")
    suspend fun getSrsItem(itemId: String): SrsItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSrsItem(item: SrsItemEntity)

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    fun observePendingSync(): Flow<List<PendingSyncEntity>>

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    suspend fun getPendingSyncOnce(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSync(item: PendingSyncEntity)

    @Query("DELETE FROM pending_sync")
    suspend fun clearSyncQueue()

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC")
    fun observeAnalyticsEvents(): Flow<List<AnalyticsEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvent(event: AnalyticsEventEntity)

    // Mistake Notebook
    @Query("SELECT * FROM mistake_entries ORDER BY isFixed ASC, occurrences DESC, lastSeenAt DESC")
    fun observeMistakes(): Flow<List<MistakeEntryEntity>>

    @Query("SELECT * FROM mistake_entries WHERE isFixed = 0 ORDER BY occurrences DESC LIMIT 5")
    suspend fun getTopUnfixedMistakes(): List<MistakeEntryEntity>

    @Query("SELECT * FROM mistake_entries WHERE id = :id LIMIT 1")
    suspend fun getMistakeById(id: String): MistakeEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMistake(entry: MistakeEntryEntity)

    @Query("UPDATE mistake_entries SET synced = 1")
    suspend fun markAllMistakesSynced()

    // Sound Progress
    @Query("SELECT * FROM sound_progress ORDER BY timestamp DESC")
    fun observeSoundProgress(): Flow<List<SoundProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSoundProgress(item: SoundProgressEntity)

    // IELTS Attempts & Starter Packs
    @Query("SELECT * FROM ielts_attempts ORDER BY timestamp DESC")
    fun observeIeltsAttempts(): Flow<List<IeltsAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIeltsAttempt(attempt: IeltsAttemptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZeroPhrases(phrases: List<ZeroPhraseOfflineEntity>)

    @Query("SELECT COUNT(*) FROM zero_phrases_offline")
    fun observeZeroPhrasesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIeltsStarterPacks(packs: List<IeltsStarterPackEntity>)

    @Query("SELECT COUNT(*) FROM ielts_starter_packs")
    fun observeIeltsStarterCount(): Flow<Int>

    // Journal
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun observeJournalEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity)

    // Clear all user progress
    @Query("DELETE FROM practice_sessions")
    suspend fun clearSessions()

    @Query("DELETE FROM learned_words")
    suspend fun clearWords()

    @Query("DELETE FROM srs_items")
    suspend fun clearSrs()

    @Query("DELETE FROM analytics_events")
    suspend fun clearAnalytics()

    @Query("DELETE FROM mistake_entries")
    suspend fun clearMistakes()

    @Query("DELETE FROM sound_progress")
    suspend fun clearSoundProgress()

    @Query("DELETE FROM ielts_attempts")
    suspend fun clearIeltsAttempts()

    @Query("DELETE FROM journal_entries")
    suspend fun clearJournal()
}

@Database(
    entities = [
        PracticeSessionEntity::class,
        LearnedWordEntity::class,
        OfflinePackEntity::class,
        OfflineDayDrillEntity::class,
        SrsItemEntity::class,
        PendingSyncEntity::class,
        AnalyticsEventEntity::class,
        MistakeEntryEntity::class,
        SoundProgressEntity::class,
        IeltsAttemptEntity::class,
        JournalEntryEntity::class,
        ZeroPhraseOfflineEntity::class,
        IeltsStarterPackEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class SpeakUpDatabase : RoomDatabase() {
    abstract fun speakUpDao(): SpeakUpDao

    companion object {
        @Volatile
        private var INSTANCE: SpeakUpDatabase? = null

        fun getInstance(context: Context): SpeakUpDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SpeakUpDatabase::class.java,
                    "speakup_offline.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
