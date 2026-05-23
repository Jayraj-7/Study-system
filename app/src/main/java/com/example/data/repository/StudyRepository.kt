package com.example.data.repository

import com.example.data.dao.StudyDao
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.data.model.StudySession
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class StudyRepository(private val studyDao: StudyDao) {

    val allSubjects: Flow<List<Subject>> = studyDao.getAllSubjectsFlow()
    val allChapters: Flow<List<Chapter>> = studyDao.getAllChaptersFlow()
    val allSessions: Flow<List<StudySession>> = studyDao.getAllStudySessionsFlow()
    val bossChapters: Flow<List<Chapter>> = studyDao.getBossChaptersFlow()

    fun getChaptersBySubject(subjectId: Long): Flow<List<Chapter>> {
        return studyDao.getChaptersBySubjectFlow(subjectId)
    }

    fun getDueChapters(time: Long): Flow<List<Chapter>> {
        return studyDao.getDueChaptersFlow(time)
    }

    suspend fun getSubjectById(id: Long): Subject? = studyDao.getSubjectById(id)
    suspend fun getChapterById(id: Long): Chapter? = studyDao.getChapterById(id)

    suspend fun insertSubject(subject: Subject): Long = studyDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = studyDao.updateSubject(subject)
    suspend fun deleteSubject(id: Long) {
        studyDao.deleteChaptersBySubjectId(id)
        studyDao.deleteSubjectById(id)
    }

    suspend fun insertChapter(chapter: Chapter): Long = studyDao.insertChapter(chapter)
    suspend fun updateChapter(chapter: Chapter) = studyDao.updateChapter(chapter)
    suspend fun deleteChapter(id: Long) = studyDao.deleteChapterById(id)

    // --- GAME ENGINE AND LOGIC ENGINE ---

    /**
     * Completes a study session, grants XP, handles leveling up, and logs the session.
     * Returns a Pair indicating: (New Level reached, Leveled up boolean)
     */
    suspend fun recordStudySession(
        subjectId: Long,
        chapterId: Long,
        durationMinutes: Int,
        xpGained: Int,
        sessionType: String,
        rating: String = "MEDIUM"
    ): LevelUpResult {
        // 1. Log the study session
        val chapter = studyDao.getChapterById(chapterId) ?: return LevelUpResult(false, 1, 1)
        val session = StudySession(
            subjectId = subjectId,
            subjectName = chapter.subjectName,
            chapterId = chapterId,
            chapterTitle = chapter.title,
            durationMinutes = durationMinutes,
            xpGained = xpGained,
            sessionType = sessionType,
            outcome = "VICTORY"
        )
        studyDao.insertStudySession(session)

        // 2. Add XP to subject and calculate level-ups
        val subject = studyDao.getSubjectById(subjectId) ?: return LevelUpResult(false, 1, 1)
        var newLevel = subject.level
        var newXp = subject.xp + xpGained
        val newTotalHours = subject.totalHours + (durationMinutes / 60.0)

        var leveledUp = false
        // RPG Mechanism: level threshold is Level * 100
        while (newXp >= newLevel * 100) {
            newXp -= newLevel * 100
            newLevel++
            leveledUp = true
        }

        val updatedSubject = subject.copy(
            level = newLevel,
            xp = newXp,
            totalHours = newTotalHours
        )
        studyDao.updateSubject(updatedSubject)

        // 3. For Spaced Repetion: completing a TRAINING/REVISION on this chapter advances its intervals
        val currentStreak = if (rating == "HARD") {
            0
        } else {
            chapter.consecutiveDoneCount + 1
        }
        val daysToAdd = getAdaptiveSpacedRepetitionDays(currentStreak, rating)
        val nextDue = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(daysToAdd.toLong())

        // If it was a boss, defeating it clears boss status!
        val updatedChapter = chapter.copy(
            consecutiveDoneCount = currentStreak,
            nextRevisionTime = nextDue,
            missedCount = if (rating == "HARD") chapter.missedCount + 1 else 0,
            isBoss = false, // Boss Defeated!
            lastRating = rating
        )
        studyDao.updateChapter(updatedChapter)

        return LevelUpResult(leveledUp, subject.level, newLevel)
    }

    /**
     * Skips or misses a revision session.
     * Mark as boss if missed, zero the streak, set due date to tomorrow.
     */
    suspend fun skipRevision(chapterId: Long) {
        val chapter = studyDao.getChapterById(chapterId) ?: return
        val newMissedCount = chapter.missedCount + 1
        
        // Zero streak, make due tomorrow, make a BOSS if missed >= 1
        val nextDue = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        val updatedChapter = chapter.copy(
            consecutiveDoneCount = 0, // Reset progression streak
            nextRevisionTime = nextDue,
            missedCount = newMissedCount,
            isBoss = true, // Marking as Boss Enemy!
            lastRating = "HARD"
        )
        studyDao.updateChapter(updatedChapter)

        // Force log a skipped session
        val session = StudySession(
            subjectId = chapter.subjectId,
            subjectName = chapter.subjectName,
            chapterId = chapterId,
            chapterTitle = chapter.title,
            durationMinutes = 0,
            xpGained = 0,
            sessionType = "SKIPPED_REVISION",
            outcome = "FLED"
        )
        studyDao.insertStudySession(session)
    }

    /**
     * Determines the interval in days based on consecutive successful reviews and latest performance rating.
     */
    private fun getAdaptiveSpacedRepetitionDays(streak: Int, rating: String): Int {
        return when (rating) {
            "EASY" -> {
                when (streak) {
                    0 -> 2
                    1 -> 4
                    2 -> 8
                    3 -> 15
                    else -> 45
                }
            }
            "MEDIUM" -> {
                when (streak) {
                    0 -> 1
                    1 -> 3
                    2 -> 7
                    3 -> 14
                    else -> 30
                }
            }
            "HARD" -> 1
            else -> 1
        }
    }
}

data class LevelUpResult(
    val leveledUp: Boolean,
    val oldLevel: Int,
    val newLevel: Int
)
