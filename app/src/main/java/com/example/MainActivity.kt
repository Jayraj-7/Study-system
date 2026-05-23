package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HUDTabButton
import com.example.ui.components.ShonenPanel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveTab
import com.example.ui.viewmodel.StudyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge full content viewports
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                val studyViewModel: StudyViewModel = viewModel()
                
                ShonenAppContent(viewModel = studyViewModel)
            }
        }
    }
}

@Composable
fun ShonenAppContent(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    // --- DATABASE REACTIVE STATE FLOWS ---
    val subjects by viewModel.subjectsState.collectAsStateWithLifecycle()
    val chapters by viewModel.chaptersState.collectAsStateWithLifecycle()
    val sessions by viewModel.sessionsState.collectAsStateWithLifecycle()
    val bossChapters by viewModel.bossChaptersState.collectAsStateWithLifecycle()
    val dueChapters by viewModel.dueChaptersState.collectAsStateWithLifecycle()

    // --- GAME EVENT FLOWS ---
    val levelUpEvent by viewModel.levelUpEvent.collectAsStateWithLifecycle()
    val perfectVictoryEvent by viewModel.perfectVictoryEvent.collectAsStateWithLifecycle()

    // --- MAIN SCREEN SCAFFOLD ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ShonenBackground)
    ) {
        Scaffold(
            topBar = {
                // Tactical minimalist heading HUD label
                if (!viewModel.isTimerActive || !viewModel.isFocusMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SHONEN STUDY SYSTEM",
                                style = MaterialTheme.typography.titleMedium,
                                color = ShonenTextPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "CORE // HUD.v1.0",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(ShonenBorder)
                        )
                    }
                }
            },
            bottomBar = {
                // Clutter-free bottom HUD switcher slots
                // Locked if in active extreme focus training (No distraction escape route!)
                if (!viewModel.isTimerActive || !viewModel.isFocusMode) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // Notch/system bar safety
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(ShonenBorder)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ShonenBackground)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            HUDTabButton(
                                text = "Training",
                                isSelected = viewModel.activeTab == ActiveTab.TRAINING,
                                onClick = { viewModel.selectTab(ActiveTab.TRAINING) },
                                testTag = "tab_training",
                                accentColor = NeonGreen,
                                modifier = Modifier.weight(1f)
                            )
                            HUDTabButton(
                                text = "Skills",
                                isSelected = viewModel.activeTab == ActiveTab.GRID_SKILLS,
                                onClick = { viewModel.selectTab(ActiveTab.GRID_SKILLS) },
                                testTag = "tab_skills",
                                accentColor = NeonCyan,
                                modifier = Modifier.weight(1f)
                            )
                            HUDTabButton(
                                text = "Battle History",
                                isSelected = viewModel.activeTab == ActiveTab.BATTLE_LOG,
                                onClick = { viewModel.selectTab(ActiveTab.BATTLE_LOG) },
                                testTag = "tab_history",
                                accentColor = NeonYellow,
                                modifier = Modifier.weight(1f)
                            )
                            HUDTabButton(
                                text = "Bosses",
                                isSelected = viewModel.activeTab == ActiveTab.BOSSES,
                                onClick = { viewModel.selectTab(ActiveTab.BOSSES) },
                                testTag = "tab_bosses",
                                accentColor = NeonRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            containerColor = ShonenBackground,
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            // Layout padding adjusts dynamically
            val contentModifier = Modifier
                .fillMaxSize()
                .padding(
                    top = if (viewModel.isTimerActive && viewModel.isFocusMode) 0.dp else innerPadding.calculateTopPadding(),
                    bottom = if (viewModel.isTimerActive && viewModel.isFocusMode) 0.dp else innerPadding.calculateBottomPadding()
                )

            // active panels selector
            when (viewModel.activeTab) {
                ActiveTab.TRAINING -> {
                    val primaryTask by viewModel.primaryTaskState.collectAsStateWithLifecycle()
                    val secondaryTasks by viewModel.secondaryTasksState.collectAsStateWithLifecycle()

                    TrainingScreen(
                        subjects = subjects,
                        primaryTask = primaryTask,
                        secondaryTasks = secondaryTasks,
                        allChapters = chapters,
                        isTimerActive = viewModel.isTimerActive,
                        isTimerPaused = viewModel.isTimerPaused,
                        pauseCount = viewModel.pauseCount,
                        isTimerFinishedAndAwaitingFeedback = viewModel.isTimerFinishedAndAwaitingFeedback,
                        timerSecondsRemaining = viewModel.timerSecondsRemaining,
                        timerTotalMinutes = viewModel.timerTotalDurationMinutes,
                        timerProgress = viewModel.timerProgress,
                        activeChapter = viewModel.activeChapter,
                        isFocusMode = viewModel.isFocusMode,
                        currentStreak = viewModel.currentStreak,
                        totalHours = viewModel.totalStudyHours,
                        totalXp = viewModel.totalXpGained,
                        onStartTraining = { chapter, duration, focus ->
                            viewModel.startTraining(chapter, duration, focus)
                        },
                        onTogglePause = {
                            viewModel.togglePauseTimer()
                        },
                        onDoneEarly = {
                            viewModel.triggerDoneEarly()
                        },
                        onSubmitFeedback = { rating ->
                            viewModel.submitFocusSessionFeedback(rating)
                        },
                        onPauseOrAbandon = {
                            viewModel.pauseOrAbandonTraining()
                        },
                        onCompleteRevision = { chapterId ->
                            viewModel.completeRevisionOnly(chapterId)
                        },
                        onSkipRevision = { chapterId ->
                            viewModel.skipRevision(chapterId)
                        },
                        modifier = contentModifier
                    )
                }
                ActiveTab.GRID_SKILLS -> {
                    SkillsScreen(
                        subjects = subjects,
                        chapters = chapters,
                        onAddSubject = { name, colorHex ->
                            viewModel.addNewSubject(name, colorHex)
                        },
                        onDeleteSubject = { id ->
                            viewModel.deleteSubject(id)
                        },
                        onAddChapter = { subjectId, subjectName, title, isBoss ->
                            viewModel.addNewChapter(subjectId, subjectName, title, isBoss)
                        },
                        onDeleteChapter = { id ->
                            viewModel.deleteChapter(id)
                        },
                        onSetChapterBoss = { id, isBoss ->
                            viewModel.setChapterIsBoss(id, isBoss)
                        },
                        modifier = contentModifier
                    )
                }
                ActiveTab.BATTLE_LOG -> {
                    BattleLogScreen(
                        sessions = sessions,
                        currentStreak = viewModel.currentStreak,
                        totalHours = viewModel.totalStudyHours,
                        totalXp = viewModel.totalXpGained,
                        modifier = contentModifier
                    )
                }
                ActiveTab.BOSSES -> {
                    BossesScreen(
                        bossChapters = bossChapters,
                        allChapters = chapters,
                        onLaunchBossRaid = { chapter, duration, focus ->
                            // Enforce training launch
                            viewModel.startTraining(chapter, duration, focus)
                            viewModel.selectTab(ActiveTab.TRAINING)
                        },
                        onPurifyBoss = { id ->
                            viewModel.setChapterIsBoss(id, false)
                        },
                        onMarkAsBoss = { id ->
                            viewModel.setChapterIsBoss(id, true)
                        },
                        modifier = contentModifier
                    )
                }
            }
        }

        // --- RPG LEVEL UP CONGRATULATORY MODAL (LIMIT BREAK!) ---
        levelUpEvent?.let { result ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { /* Block taps */ }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ShonenPanel(
                    borderColor = NeonYellow,
                    borderWidth = 2.dp,
                    backgroundColor = ShonenSurface,
                    cutCornerSize = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .testTag("level_up_dialog")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚡ LIMIT BREAK ⚡",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonYellow,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SKILL LEVEL UP!",
                            style = MaterialTheme.typography.displayMedium,
                            color = ShonenTextPrimary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Level numbers HUD visual
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LVL ${result.oldLevel}",
                                style = MaterialTheme.typography.displayMedium,
                                color = ShonenTextSecondary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " ➔ ",
                                style = MaterialTheme.typography.displayMedium,
                                color = NeonGreen,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "LVL ${result.newLevel}",
                                style = MaterialTheme.typography.displayMedium,
                                color = NeonGreen,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "⚔️ CONGRATULATIONS STUDY REAPER! ⚔️\nYour cerebral pathways have restructured. Tactical capacity increased (+100 IQ). Procrastination defense buffed by +5%!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShonenTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.clearLevelUpEvent() },
                            shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonYellow,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .testTag("close_level_up_dialog")
                        ) {
                            Text(
                                text = "RECEIVE REWARDS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // --- PERFECT FOCUS VICTORY MODAL ---
        if (perfectVictoryEvent) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { /* Block taps */ }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                ShonenPanel(
                    borderColor = NeonGreen,
                    borderWidth = 2.dp,
                    backgroundColor = ShonenSurface,
                    cutCornerSize = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .testTag("victory_dialog")
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆 COMBAT DEBRIEF 🏆",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PERFECT FOCUS VICTORY!",
                            style = MaterialTheme.typography.displayMedium,
                            color = ShonenTextPrimary,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "⭐ SUCCESS RATE: 100% ⭐",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonYellow,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Discipline +1. Concentration +1.\nYou completed the entire focus segment without distractions.\n\nReward: Duration XP + Bonus Focus XP (+20 XP ACCORDED)!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShonenTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { viewModel.clearPerfectVictoryEvent() },
                            shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(48.dp)
                                .testTag("close_victory_dialog")
                        ) {
                            Text(
                                text = "COMMENCE RECOVERY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
