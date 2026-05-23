package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.DatabaseProvider
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.data.model.StudySession
import com.example.data.repository.StudyRepository
import com.example.data.repository.LevelUpResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class ActiveTab {
    TRAINING,  // Home: daily missions, timer
    GRID_SKILLS, // Subject level system & Chapter management
    BATTLE_LOG, // Study history & streaks
    BOSSES      // Underachieved / overdue revisions
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    // --- UI VIEW STATE ---
    var activeTab by mutableStateOf(ActiveTab.TRAINING)

    // --- DATABASE STATS FLOWS ---
    val subjectsState: StateFlow<List<Subject>>
    val chaptersState: StateFlow<List<Chapter>>
    val sessionsState: StateFlow<List<StudySession>>
    val bossChaptersState: StateFlow<List<Chapter>>
    val dueChaptersState: StateFlow<List<Chapter>>

    // --- ANALYTICS STATE (computed from sessions) ---
    var currentStreak by mutableStateOf(0)
        private set
    var totalStudyHours by mutableStateOf(0.0)
        private set
    var totalXpGained by mutableStateOf(0)
        private set

    // --- TIMER / ACTIVE TRAINING STATE ---
    var isTimerActive by mutableStateOf(false)
        private set
    var timerSecondsRemaining by mutableStateOf(0)
        private set
    var timerTotalDurationMinutes by mutableStateOf(25)
        private set
    val timerProgress: Float
        get() = if (timerTotalDurationMinutes > 0) {
            timerSecondsRemaining.toFloat() / (timerTotalDurationMinutes * 60)
        } else {
            0f
        }
    var activeChapter by mutableStateOf<Chapter?>(null)
        private set
    var isFocusMode by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    // --- GAME ENGINE EVENTS ---
    private val _levelUpEvent = MutableStateFlow<LevelUpResult?>(null)
    val levelUpEvent: StateFlow<LevelUpResult?> = _levelUpEvent.asStateFlow()

    private val _perfectVictoryEvent = MutableStateFlow<Boolean>(false)
    val perfectVictoryEvent: StateFlow<Boolean> = _perfectVictoryEvent.asStateFlow()

    init {
        val db = DatabaseProvider.getDatabase(application)
        repository = StudyRepository(db.studyDao())

        subjectsState = repository.allSubjects
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        chaptersState = repository.allChapters
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        sessionsState = repository.allSessions
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        bossChaptersState = repository.bossChapters
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        dueChaptersState = repository.getDueChapters(System.currentTimeMillis())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Compute streak and totals when sessions update
        viewModelScope.launch {
            sessionsState.collect { sessions ->
                calculateStats(sessions)
            }
        }

        // Seeding database if newly installed
        viewModelScope.launch {
            delay(500)
            if (repository.allSubjects.first().isEmpty()) {
                seedDatabase()
            }
        }
    }

    // --- DATABASE INTERACTION / MUTATION ---

    fun selectTab(tab: ActiveTab) {
        if (isTimerActive && isFocusMode) {
            // Cannot escape focus mode!
            return
        }
        activeTab = tab
    }

    fun addNewSubject(name: String, colorHex: String) {
        viewModelScope.launch {
            val subject = Subject(name = name.uppercase(), colorHex = colorHex)
            repository.insertSubject(subject)
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    fun addNewChapter(subjectId: Long, subjectName: String, title: String, isBoss: Boolean = false) {
        viewModelScope.launch {
            val chapter = Chapter(
                subjectId = subjectId,
                subjectName = subjectName,
                title = title,
                isBoss = isBoss,
                // Automatically scheduled for immediately
                nextRevisionTime = System.currentTimeMillis()
            )
            repository.insertChapter(chapter)
        }
    }

    fun deleteChapter(id: Long) {
        viewModelScope.launch {
            repository.deleteChapter(id)
        }
    }

    fun setChapterIsBoss(chapterId: Long, isBoss: Boolean) {
        viewModelScope.launch {
            val chapter = repository.getChapterById(chapterId)
            if (chapter != null) {
                repository.updateChapter(chapter.copy(isBoss = isBoss))
            }
        }
    }

    // --- SPACED REPETITION CONTROLS ---

    fun completeRevisionOnly(chapterId: Long) {
        viewModelScope.launch {
            val chapter = repository.getChapterById(chapterId) ?: return@launch
            // Standard flash revision (takes, say, 5 mins in background, gets 10 XP)
            val result = repository.recordStudySession(
                subjectId = chapter.subjectId,
                chapterId = chapterId,
                durationMinutes = 5,
                xpGained = 15,
                sessionType = "REVISION"
            )
            if (result.leveledUp) {
                _levelUpEvent.value = result
            }
        }
    }

    fun skipRevision(chapterId: Long) {
        viewModelScope.launch {
            repository.skipRevision(chapterId)
        }
    }

    // --- LIVE FOCUS TRAINING TIMER ENGINE ---

    fun startTraining(chapter: Chapter, durationMinutes: Int, focus: Boolean) {
        if (isTimerActive) return
        activeChapter = chapter
        timerTotalDurationMinutes = durationMinutes
        timerSecondsRemaining = durationMinutes * 60
        isFocusMode = focus
        isTimerActive = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timerSecondsRemaining > 0) {
                delay(1000)
                timerSecondsRemaining--
            }
            // SUCCESS! Full focus session completed!
            completeActiveTraining(completedFully = true)
        }
    }

    fun pauseOrAbandonTraining() {
        if (!isTimerActive) return
        timerJob?.cancel()
        
        // Calculate partially completed time
        val elapsedSeconds = (timerTotalDurationMinutes * 60) - timerSecondsRemaining
        val elapsedMinutes = elapsedSeconds / 60

        if (elapsedMinutes >= 1) {
            // Give them partial XP for showing up
            completeActiveTraining(completedFully = false, durationMin = elapsedMinutes)
        } else {
            // Left without even 1 minute
            resetTimer()
        }
    }

    private fun completeActiveTraining(completedFully: Boolean, durationMin: Int = timerTotalDurationMinutes) {
        val chapter = activeChapter ?: return
        
        // XP calculation: 1 XP per minute + 15 bonus XP if completed fully!
        val baseXp = durationMin
        val bonus = if (completedFully) 20 else 0
        val finalXp = baseXp + bonus

        viewModelScope.launch {
            val result = repository.recordStudySession(
                subjectId = chapter.subjectId,
                chapterId = chapter.id,
                durationMinutes = durationMin,
                xpGained = finalXp,
                sessionType = if (chapter.isBoss) "BOSS_BATTLE" else "TRAINING"
            )

            if (completedFully) {
                _perfectVictoryEvent.value = true
            }

            if (result.leveledUp) {
                _levelUpEvent.value = result
            }

            resetTimer()
        }
    }

    fun clearLevelUpEvent() {
        _levelUpEvent.value = null
    }

    fun clearPerfectVictoryEvent() {
        _perfectVictoryEvent.value = false
    }

    private fun resetTimer() {
        timerJob?.cancel()
        isTimerActive = false
        timerSecondsRemaining = 0
        activeChapter = null
        isFocusMode = false
    }

    // --- GAME STATS ENGINE ---

    private fun calculateStats(sessions: List<StudySession>) {
        if (sessions.isEmpty()) {
            currentStreak = 0
            totalStudyHours = 0.0
            totalXpGained = 0
            return
        }

        // Total XP
        totalXpGained = sessions.sumOf { it.xpGained }

        // Total Hours
        val totalMinutes = sessions.sumOf { it.durationMinutes }
        totalStudyHours = totalMinutes / 60.0

        // Streak
        currentStreak = computeStreak(sessions)
    }

    private fun computeStreak(sessions: List<StudySession>): Int {
        val victorySessions = sessions.filter { it.outcome == "VICTORY" && it.durationMinutes > 0 }
        if (victorySessions.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dates = victorySessions.map {
            dateFormat.format(Date(it.timestamp))
        }.distinct().sortedDescending() // Newest first

        if (dates.isEmpty()) return 0

        val todayStr = dateFormat.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(cal.time)

        val latestDate = dates[0]
        if (latestDate != todayStr && latestDate != yesterdayStr) {
            return 0 // Streak reset if no study session today or yesterday
        }

        var streak = 1
        var currentMills = dateFormat.parse(latestDate)?.time ?: return streak

        for (i in 1 until dates.size) {
            val nextMills = dateFormat.parse(dates[i])?.time ?: break
            val diff = (currentMills - nextMills) / (24 * 60 * 60 * 1000)
            if (diff == 1L) {
                streak++
                currentMills = nextMills
            } else if (diff > 1L) {
                break
            }
        }
        return streak
    }

    // --- DATABASE SEEDING FOR NEW ADVENTURERS ---

    private suspend fun seedDatabase() {
        // Create 4 standard anime subjects (skills)
        val mathId = repository.insertSubject(Subject(name = "MATH (ALGEBRA/CALCULUS)", colorHex = "#00FF66")) // Neon Green
        val scienceId = repository.insertSubject(Subject(name = "SCIENCE (PHYSICS/CHEM)", colorHex = "#00E5FF")) // Cyber Cyan
        val historyId = repository.insertSubject(Subject(name = "HISTORY (WORLD WARS)", colorHex = "#FF9100")) // Neon Orange
        val englishId = repository.insertSubject(Subject(name = "LANGUAGE & LITERATURE", colorHex = "#E040FB")) // Neon Magenta

        // Math chapters (arcs)
        repository.insertChapter(Chapter(
            subjectId = mathId,
            subjectName = "MATH (ALGEBRA/CALCULUS)",
            title = "Arc 1: Derivative Basics",
            nextRevisionTime = System.currentTimeMillis() - 1000 // due already!
        ))
        repository.insertChapter(Chapter(
            subjectId = mathId,
            subjectName = "MATH (ALGEBRA/CALCULUS)",
            title = "Arc 2: Integrals & Area",
            nextRevisionTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1)
        ))

        // Science chapters
        repository.insertChapter(Chapter(
            subjectId = scienceId,
            subjectName = "SCIENCE (PHYSICS/CHEM)",
            title = "Arc 1: Newton's Laws of Motion",
            nextRevisionTime = System.currentTimeMillis() - 1000 // due already!
        ))
        repository.insertChapter(Chapter(
            subjectId = scienceId,
            subjectName = "SCIENCE (PHYSICS/CHEM)",
            title = "Arc 2: Chemical Bonds & Orbitals",
            nextRevisionTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(2)
        ))

        // History chapters
        repository.insertChapter(Chapter(
            subjectId = historyId,
            subjectName = "HISTORY (WORLD WARS)",
            title = "Arc 1: Causes of WWI",
            nextRevisionTime = System.currentTimeMillis() - 1000 // due
        ))

        // Seed a historic session so they see a streak of 1 right away to feel legendary!
        val historicalXp = 35
        val historicalDuration = 30
        repository.recordStudySession(
            subjectId = mathId,
            chapterId = 1, // first seeded chapter
            durationMinutes = historicalDuration,
            xpGained = historicalXp,
            sessionType = "TRAINING"
        )
    }
}
