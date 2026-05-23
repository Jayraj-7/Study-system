package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.ui.components.HUDMetricTag
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.ShonenPanel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun TrainingScreen(
    subjects: List<Subject>,
    primaryTask: Chapter?,
    secondaryTasks: List<Chapter>,
    allChapters: List<Chapter>,
    isTimerActive: Boolean,
    isTimerPaused: Boolean,
    pauseCount: Int,
    isTimerFinishedAndAwaitingFeedback: Boolean,
    timerSecondsRemaining: Int,
    timerTotalMinutes: Int,
    timerProgress: Float,
    activeChapter: Chapter?,
    isFocusMode: Boolean,
    currentStreak: Int,
    totalHours: Double,
    totalXp: Int,
    onStartTraining: (Chapter, durationMinutes: Int, focus: Boolean) -> Unit,
    onTogglePause: () -> Unit,
    onDoneEarly: () -> Unit,
    onSubmitFeedback: (String) -> Unit,
    onPauseOrAbandon: () -> Unit,
    onCompleteRevision: (Long) -> Unit,
    onSkipRevision: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTimerActive && activeChapter != null) {
        // High Concentration Focus Zone overlay (Full takeover)
        FocusModeCockpit(
            activeChapter = activeChapter,
            isTimerPaused = isTimerPaused,
            pauseCount = pauseCount,
            isTimerFinishedAndAwaitingFeedback = isTimerFinishedAndAwaitingFeedback,
            secondsRemaining = timerSecondsRemaining,
            totalMinutes = timerTotalMinutes,
            progress = timerProgress,
            isFocusMode = isFocusMode,
            onTogglePause = onTogglePause,
            onDoneEarly = onDoneEarly,
            onSubmitFeedback = onSubmitFeedback,
            onAbandon = onPauseOrAbandon
        )
    } else {
        // Standard Mission Control center layout (Dashboard)
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // Stats HUD Summary
            item {
                HUDStatsHeader(
                    currentStreak = currentStreak,
                    totalHours = totalHours,
                    totalXp = totalXp
                )
            }

            // Weekly intelligence report
            item {
                WeeklyPerformanceReport(
                    currentStreak = currentStreak,
                    totalHours = totalHours,
                    subjects = subjects,
                    allChapters = allChapters
                )
            }

            // PRIMARY study mission banner
            item {
                Text(
                    text = "► TODAY'S PRIMARY OBJECTIVE",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (primaryTask != null) {
                item {
                    PrimaryMissionCard(
                        chapter = primaryTask,
                        onTrain = { duration, focus -> onStartTraining(primaryTask, duration, focus) },
                        onDone = { onCompleteRevision(primaryTask.id) },
                        onSkip = { onSkipRevision(primaryTask.id) }
                    )
                }
            } else {
                item {
                    ShonenPanel(
                        borderColor = NeonGreen,
                        borderWidth = 1.dp,
                        backgroundColor = ShonenSurface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚔️ HERO IS LEVELED - DIRECTIVES CLEARED!",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "All core training tracks are up to date! Continue sparring in the custom chamber below.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShonenTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            }

            // SECONDARY missions list
            item {
                Text(
                    text = "► SECONDARY STUDY MISSIONS",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            val filteredSecondary = secondaryTasks.take(5)
            if (filteredSecondary.isEmpty()) {
                item {
                    ShonenPanel(
                        borderColor = ShonenBorder,
                        backgroundColor = ShonenSurfaceVariant
                    ) {
                        Text(
                            text = "No secondary missions pending. All arcs balanced!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShonenTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            } else {
                items(filteredSecondary, key = { "sec_${it.id}" }) { secondary ->
                    SecondaryMissionCard(
                        chapter = secondary,
                        onTrain = { duration, focus -> onStartTraining(secondary, duration, focus) },
                        onDone = { onCompleteRevision(secondary.id) },
                        onSkip = { onSkipRevision(secondary.id) }
                    )
                }
            }

            // Sparring Ring (Quick Training for any of your general chapters)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "► CUSTOM SPARRING CHAMBER",
                    style = MaterialTheme.typography.titleMedium,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (allChapters.isEmpty()) {
                item {
                    ShonenPanel(
                        borderColor = ShonenBorder,
                        backgroundColor = ShonenSurface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NO CHAPTERS DISCOVERED YET",
                                style = MaterialTheme.typography.titleMedium,
                                color = ShonenTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Navigate to the SKILLS tab and create a chapter arc to begin your first training mission!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShonenTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    var expanded by remember { mutableStateOf(false) }
                    var selectedChapter by remember { mutableStateOf<Chapter?>(null) }

                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, ShonenBorder, CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
                                .background(ShonenSurfaceVariant)
                                .clickable { expanded = !expanded }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = selectedChapter?.let { "[${it.subjectName}] ${it.title}" } ?: "CHOOSE AN ARC TO COMMENCE SPARRING...",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (selectedChapter != null) NeonCyan else ShonenTextSecondary,
                                fontSize = 14.sp
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(ShonenSurface)
                                .border(1.dp, ShonenBorder)
                        ) {
                            allChapters.forEach { chapter ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "[${chapter.subjectName}] ${chapter.title}",
                                            color = ShonenTextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        selectedChapter = chapter
                                        expanded = false
                                    },
                                    modifier = Modifier.background(ShonenSurface)
                                )
                            }
                        }

                        selectedChapter?.let { chapter ->
                            Spacer(modifier = Modifier.height(12.dp))
                            MissionConfigureLauncher(
                                chapter = chapter,
                                onLaunch = { duration, focus ->
                                    onStartTraining(chapter, duration, focus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HUDStatsHeader(
    currentStreak: Int,
    totalHours: Double,
    totalXp: Int,
    modifier: Modifier = Modifier
) {
    ShonenPanel(
        borderColor = NeonCyan,
        borderWidth = 1.5.dp,
        backgroundColor = ShonenSurface,
        cutCornerSize = 12.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TACTICAL RADAR STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShonenTextSecondary,
                    fontSize = 10.sp
                )
                Text(
                    text = "EXAM SOLDIER HUD",
                    style = MaterialTheme.typography.titleLarge,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Black
                )
            }
            HUDMetricTag(
                label = "STREAK",
                value = "$currentStreak DAYS",
                color = NeonYellow
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BATTLE MERITS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShonenTextSecondary
                )
                Text(
                    text = "$totalXp XP",
                    style = MaterialTheme.typography.displayMedium,
                    color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TRAINING TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShonenTextSecondary
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f HRS", totalHours),
                    style = MaterialTheme.typography.displayMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )
            }
        }
    }
}

@Composable
fun WeeklyPerformanceReport(
    currentStreak: Int,
    totalHours: Double,
    subjects: List<Subject>,
    allChapters: List<Chapter>,
    modifier: Modifier = Modifier
) {
    // Determine dynamically weak categories
    val weakChapters = allChapters.filter { it.getWeaknessCategory() == "WEAK" }
    val improvedSubjects = subjects.filter { it.level > 1 || it.xp > 0 }
    val consistencyScore = (currentStreak * 12).coerceIn(0, 100)

    ShonenPanel(
        borderColor = ShonenBorderBright,
        borderWidth = 1.dp,
        backgroundColor = ShonenSurfaceVariant,
        cutCornerSize = 8.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "📊 TACTICAL WEEKLY INTELLIGENCE REPORT",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Consistency level bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Consistency score:",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$consistencyScore%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonYellow,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            NeonProgressBar(
                progress = consistencyScore.toFloat() / 100f,
                color = NeonYellow,
                modifier = Modifier.fillMaxWidth(),
                height = 6.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left quadrant: dynamic Weak Areas
                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "🚨 WEAK CHANNELS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (weakChapters.isEmpty()) {
                        Text(
                            text = "⚔️ FLANK SECURE",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    } else {
                        weakChapters.take(2).forEach { chapter ->
                            Text(
                                text = "• [${chapter.subjectName}] ${chapter.title}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShonenTextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Right quadrant: Subjects upgraded
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📈 UPGRADED SECTORS",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (improvedSubjects.isEmpty()) {
                        Text(
                            text = "None yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShonenTextSecondary,
                            fontSize = 11.sp
                        )
                    } else {
                        improvedSubjects.take(2).forEach { subject ->
                            Text(
                                text = "▲ ${subject.name} (Lv.${subject.level})",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShonenTextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryMissionCard(
    chapter: Chapter,
    onTrain: (Int, Boolean) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLauncher by remember { mutableStateOf(false) }

    ShonenPanel(
        borderColor = if (chapter.isBoss) NeonRed else NeonGreen,
        borderWidth = 2.dp,
        backgroundColor = ShonenSurface,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(if (chapter.isBoss) NeonRed else NeonGreen, RoundedCornerShape(2.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (chapter.isBoss) "BOSS ARCH" else "PRIMARY MISSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Arc: ${chapter.subjectName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Medium
                    )
                }

                val weakness = chapter.getWeaknessCategory()
                Text(
                    text = "Weakness: $weakness",
                    color = if (weakness == "WEAK") NeonRed else if (weakness == "STRONG") NeonGreen else NeonYellow,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleLarge,
                color = ShonenTextPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!showLauncher) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showLauncher = true },
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (chapter.isBoss) NeonRed else NeonGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .height(44.dp)
                            .testTag("start_primary_training_${chapter.id}")
                    ) {
                        Text(
                            text = if (chapter.isBoss) "⚔️ ENGAGE BOSS RAID" else "⚡ COMMENCE MISSION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onDone,
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShonenSurfaceVariant,
                            contentColor = NeonCyan
                        ),
                        border = BorderStroke(1.dp, ShonenBorderBright),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("primary_done_${chapter.id}")
                    ) {
                        Text(
                            text = "DONE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onSkip,
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShonenSurfaceVariant,
                            contentColor = ShonenTextSecondary
                        ),
                        border = BorderStroke(1.dp, ShonenBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("primary_skip_${chapter.id}")
                    ) {
                        Text(
                            text = "SKIP",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                MissionConfigureLauncher(
                    chapter = chapter,
                    onLaunch = { duration, focus ->
                        onTrain(duration, focus)
                        showLauncher = false
                    },
                    onCancel = { showLauncher = false }
                )
            }
        }
    }
}

@Composable
fun SecondaryMissionCard(
    chapter: Chapter,
    onTrain: (Int, Boolean) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLauncher by remember { mutableStateOf(false) }

    ShonenPanel(
        borderColor = if (chapter.isBoss) NeonRed else ShonenBorder,
        borderWidth = 1.dp,
        backgroundColor = ShonenSurfaceVariant,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "SUB-DIRECTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShonenTextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "[${chapter.subjectName}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }

                val weakness = chapter.getWeaknessCategory()
                Text(
                    text = weakness,
                    color = if (weakness == "WEAK") NeonRed else if (weakness == "STRONG") NeonGreen else NeonYellow,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chapter.title,
                style = MaterialTheme.typography.titleMedium,
                color = ShonenTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (!showLauncher) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showLauncher = true },
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (chapter.isBoss) NeonRed else ShonenSurface,
                            contentColor = if (chapter.isBoss) Color.Black else NeonCyan
                        ),
                        border = if (chapter.isBoss) null else BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(34.dp)
                            .testTag("start_secondary_training_${chapter.id}")
                    ) {
                        Text(
                            text = if (chapter.isBoss) "SLAY BOSS" else "TRAIN SECTOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = onDone,
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShonenSurface,
                            contentColor = NeonCyan
                        ),
                        border = BorderStroke(1.dp, ShonenBorderBright),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("sec_done_${chapter.id}")
                    ) {
                        Text(
                            text = "DONE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }

                    Button(
                        onClick = onSkip,
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ShonenSurface,
                            contentColor = ShonenTextSecondary
                        ),
                        border = BorderStroke(1.dp, ShonenBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "SKIP",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            } else {
                MissionConfigureLauncher(
                    chapter = chapter,
                    onLaunch = { duration, focus ->
                        onTrain(duration, focus)
                        showLauncher = false
                    },
                    onCancel = { showLauncher = false }
                )
            }
        }
    }
}

@Composable
fun MissionConfigureLauncher(
    chapter: Chapter,
    onLaunch: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null
) {
    var selectedDuration by remember { mutableStateOf(25) }
    var useFocusShield by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .border(1.dp, ShonenBorder, CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
            .background(Color(0xFF090909))
            .padding(12.dp)
    ) {
        Text(
            text = "CHAMBER CALIBRATION",
            style = MaterialTheme.typography.labelSmall,
            color = ShonenTextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Duration selector
        Text(
            text = "CHOOSE MISSION LENGTH:",
            style = MaterialTheme.typography.labelSmall,
            color = ShonenTextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                Triple(25, "SKIRMISH", "25M"),
                Triple(30, "CAMPAIGN", "30M"),
                Triple(40, "BOSS RAID", "40M")
            ).forEach { (minutes, label, shortLabel) ->
                val isSelected = selectedDuration == minutes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isSelected) NeonCyan else ShonenBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .background(
                            if (isSelected) NeonCyan.copy(alpha = 0.12f) else ShonenSurface
                        )
                        .clickable { selectedDuration = minutes }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) NeonCyan else ShonenTextSecondary,
                            fontSize = 8.sp
                        )
                        Text(
                            text = shortLabel,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isSelected) ShonenTextPrimary else ShonenTextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Focus toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = "ENGAGE FOCUS SHIELD",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (useFocusShield) NeonYellow else ShonenTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Text(
                    text = "Locks app interaction & shuts navigation tabs completely until session is finished or fled (+15% Bonus XP).",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShonenTextSecondary,
                    fontSize = 9.sp,
                    lineHeight = 12.sp
                )
            }
            Switch(
                checked = useFocusShield,
                onCheckedChange = { useFocusShield = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = NeonYellow,
                    checkedTrackColor = NeonYellow.copy(alpha = 0.4f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Black
                ),
                modifier = Modifier.testTag("focus_shield_switch")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fire buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (onCancel != null) {
                Button(
                    onClick = onCancel,
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShonenSurface,
                        contentColor = ShonenTextPrimary
                    ),
                    border = BorderStroke(1.dp, ShonenBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ABORT",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Button(
                onClick = { onLaunch(selectedDuration, useFocusShield) },
                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (chapter.isBoss) NeonRed else NeonCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(2f)
                    .testTag("launch_training_button")
            ) {
                Text(
                    text = "ENTER TRAINING chamber ✈",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun FocusModeCockpit(
    activeChapter: Chapter,
    isTimerPaused: Boolean,
    pauseCount: Int,
    isTimerFinishedAndAwaitingFeedback: Boolean,
    secondsRemaining: Int,
    totalMinutes: Int,
    progress: Float,
    isFocusMode: Boolean,
    onTogglePause: () -> Unit,
    onDoneEarly: () -> Unit,
    onSubmitFeedback: (String) -> Unit,
    onAbandon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShonenBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isTimerFinishedAndAwaitingFeedback) {
            // CONFIDENCE DIFFICULTY FEEDBACK WINDOW
            Box(
                modifier = Modifier
                    .border(1.5.dp, NeonYellow, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                    .background(Color(0xFF0C0C0C))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✨ SESSION COMPLETE & EXPEDITION TRIUMPHED!",
                        style = MaterialTheme.typography.titleLarge,
                        color = NeonYellow,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Assess your command of this active study channel. Your rating updates the spaced-repetition logic adaptively.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShonenTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "HOW SECURE WAS YOUR MEMORY SPAR?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShonenTextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // EASY
                    Button(
                        onClick = { onSubmitFeedback("EASY") },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = NeonGreen
                        ),
                        border = BorderStroke(1.5.dp, NeonGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rate_easy")
                    ) {
                        Text(text = "🟢 EASY / SECURED (Leaps Spaced Interval)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // MEDIUM
                    Button(
                        onClick = { onSubmitFeedback("MEDIUM") },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = NeonCyan
                        ),
                        border = BorderStroke(1.5.dp, NeonCyan),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rate_medium")
                    ) {
                        Text(text = "🟡 MEDIUM / SECURE (Standard Schedule)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // HARD
                    Button(
                        onClick = { onSubmitFeedback("HARD") },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = NeonRed
                        ),
                        border = BorderStroke(1.5.dp, NeonRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("rate_hard")
                    ) {
                        Text(text = "🔴 HARD / STRETCHED (Repeat Tomorrow)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // STANDARD DYNAMIC COUNTDOWN TIMER
            Box(
                modifier = Modifier
                    .border(1.dp, NeonYellow, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
                    .background(NeonYellow.copy(alpha = 0.05f))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(
                    text = if (activeChapter.isBoss) "⚔ ACTIVE BOSS BATTLE SCREEN" else "⚡ EXTREME FOCUS CHAMBER ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (activeChapter.isBoss) NeonRed else NeonYellow,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = activeChapter.subjectName,
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = activeChapter.title,
                style = MaterialTheme.typography.displayMedium,
                color = ShonenTextPrimary,
                textAlign = TextAlign.Center,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(240.dp)
                    .border(2.dp, ShonenBorder, RoundedCornerShape(120.dp))
                    .padding(12.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    strokeWidth = 6.dp,
                    color = if (isTimerPaused) ShonenTextSecondary else if (activeChapter.isBoss) NeonRed else NeonYellow,
                    trackColor = Color(0xFF111111),
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isTimerPaused) "PAUSED" else timeFormatted,
                        style = MaterialTheme.typography.displayLarge,
                        color = if (isTimerPaused) ShonenTextSecondary else ShonenTextPrimary,
                        fontSize = 44.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "COMBUSTION TIMER",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShonenTextSecondary,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Substantial dynamic bottom action layout (Pause, Done Early)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onTogglePause,
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = NeonYellow
                    ),
                    border = BorderStroke(1.dp, NeonYellow),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(46.dp)
                        .testTag("toggle_pause_timer")
                ) {
                    val labelText = if (isTimerPaused) "RESUME" else "PAUSE (FREEZES: $pauseCount)"
                    Text(text = labelText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDoneEarly,
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("complete_timer_early")
                ) {
                    Text(text = "DONE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ShonenPanel(
                borderColor = ShonenBorder,
                backgroundColor = ShonenSurface,
                cutCornerSize = 6.dp,
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Text(
                    text = if (isFocusMode) {
                        "🛡️ FOCUS SHIELD LOCKED: Navigate out of this app and study metrics are compromised. Stay in the zone!"
                    } else {
                        "⚡ SPARRING SPUR: Avoid checking social feeds. Focus on critical synthesis and active recall queries."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFocusMode) NeonYellow else ShonenTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onAbandon,
                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = NeonRed
                ),
                border = BorderStroke(1.dp, NeonRed),
                modifier = Modifier
                    .width(200.dp)
                    .height(40.dp)
                    .testTag("abandon_timer_button")
            ) {
                Text(
                    text = "ABANDON TRAINING",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
