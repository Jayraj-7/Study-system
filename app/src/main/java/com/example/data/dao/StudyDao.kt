package com.example.data.dao

import androidx.room.*
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.data.model.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- SUBJECTS (SKILLS) ---
    @Query("SELECT * FROM subjects ORDER BY level DESC, xp DESC")
    fun getAllSubjectsFlow(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects")
    suspend fun getAllSubjects(): List<Subject>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): Subject?

    @Query("SELECT * FROM subjects WHERE name = :name LIMIT 1")
    suspend fun getSubjectByName(name: String): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubjectById(id: Long)

    // --- CHAPTERS (ARCS) ---
    @Query("SELECT * FROM chapters ORDER BY title ASC")
    fun getAllChaptersFlow(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE subjectId = :subjectId ORDER BY title ASC")
    fun getChaptersBySubjectFlow(subjectId: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE nextRevisionTime <= :time ORDER BY nextRevisionTime ASC")
    fun getDueChaptersFlow(time: Long): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE isBoss = 1 ORDER BY nextRevisionTime ASC")
    fun getBossChaptersFlow(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter): Long

    @Update
    suspend fun updateChapter(chapter: Chapter)

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: Long)

    @Query("DELETE FROM chapters WHERE subjectId = :subjectId")
    suspend fun deleteChaptersBySubjectId(subjectId: Long)

    // --- STUDY SESSIONS (BATTLE HISTORY) ---
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessionsFlow(): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession): Long

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteStudySessionById(id: Long)
}
