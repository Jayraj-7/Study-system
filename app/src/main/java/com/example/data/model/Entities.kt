package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val level: Int = 1,
    val xp: Int = 0, // Cumulative current level XP
    val totalHours: Double = 0.0,
    val colorHex: String = "#00FF66" // Hex representation for theme accents
) {
    // RPG mechanism: level-up threshold scales with level
    fun xpNeededForNextLevel(): Int {
        return level * 100
    }
}

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val title: String,
    val consecutiveDoneCount: Int = 0, // Spaced repetition interval step
    val nextRevisionTime: Long = System.currentTimeMillis(), // Spaced repetition due date (Timestamp)
    val isBoss: Boolean = false, // Underachieved / overdue / manually flagged weak chapter -> BOSS ENEMY!
    val missedCount: Int = 0, // Number of times skipped or overdue
    val status: String = "ACTIVE", // ACTIVE, MASTERED
    val lastRating: String = "MEDIUM" // EASY, MEDIUM, HARD
) {
    fun getWeaknessCategory(): String {
        return when {
            isBoss || lastRating == "HARD" || missedCount > 1 -> "WEAK"
            consecutiveDoneCount >= 3 && lastRating == "EASY" && missedCount == 0 -> "STRONG"
            else -> "MEDIUM"
        }
    }
}

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val chapterId: Long,
    val chapterTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val xpGained: Int,
    val sessionType: String = "TRAINING", // TRAINING, REVISION, BOSS_BATTLE
    val outcome: String = "VICTORY" // VICTORY, ESCAPED
)
