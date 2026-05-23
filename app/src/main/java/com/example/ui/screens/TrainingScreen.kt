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
    dueChapters: List<Chapter>,
    allChapters: List<Chapter>,
    isTimerActive: Boolean,
    timerSecondsRemaining: Int,
    timerTotalMinutes: Int,
    timerProgress: Float,
    activeChapter: Chapter?,
    isFocusMode: Boolean,
    currentStreak: Int,
    totalHours: Double,
    totalXp: Int,
    onStartTraining: (Chapter, durationMinutes: Int, focus: Boolean) -> Unit,
    onPauseOrAbandon: () -> Unit,
    onCompleteRevision: (Long) -> Unit,
    onSkipRevision: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isTimerActive && activeChapter != null) {
        // FOCUS mode timer takes over the universe for absolute mental concentration!
        FocusTimerPanel(
            activeChapter = activeChapter,
            secondsRemaining = timerSecondsRemaining,
            totalMinutes = timerTotalMinutes,
            progress = timerProgress,
            isFocusMode = isFocusMode,
            onAbandon = onPauseOrAbandon
        )
    } else {
        // Standard training HUD home screen
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

            // Daily Study Missions section
            item {
                Text(
                    text = "► DAILY OBJECTIVES (DUE REVISIONS)",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (dueChapters.isEmpty()) {
                item {
                    ShonenPanel(
                        borderColor = ShonenBorder,
                        borderWidth = 1.dp,
                        backgroundColor = ShonenSurface
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "⚔️ ALL REVISIONS CLEARED!",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your daily memory chambers are fully optimized. Create a new arc or conduct customized sparring sessions below!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ShonenTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                }
            } else {
                items(dueChapters, key = { it.id }) { chapter ->
                    MissionCard(
                        chapter = chapter,
                        onTrain = { duration, focus -> onStartTraining(chapter, duration, focus) },
                        onDone = { onCompleteRevision(chapter.id) },
                        onSkip = { onSkipRevision(chapter.id) }
                    )
                }
            }

            // Sparring Ring (Quick Training for any of your general chapters)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "► SPARRING CHAMBER (ANY ARC)",
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "NO ARCS DISCOVERED YET",
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
                    
                    // Show select sparring partner dropdown
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
                            // Sparring Launcher configuration
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

        Spacer(modifier = Modifier.height(16.dp))

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
fun MissionCard(
    chapter: Chapter,
    onTrain: (Int, Boolean) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLauncher by remember { mutableStateOf(false) }

    ShonenPanel(
        borderColor = if (chapter.isBoss) NeonRed else ShonenBorder,
        borderWidth = if (chapter.isBoss) 1.5.dp else 1.dp,
        backgroundColor = ShonenSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chapter.isBoss) {
                        Text(
                            text = "⚠ BOSS ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonRed,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    } else {
                        Text(
                            text = "⚡ MISSION",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                    Text(
                        text = chapter.subjectName,
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ShonenTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions: Start Training directly opens launcher config, Done / Skip directly scheduled
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
                        .weight(1.5f)
                        .height(38.dp)
                        .testTag("start_training_${chapter.id}")
                ) {
                    Text(
                        text = if (chapter.isBoss) "SLAY BOSS" else "START TRAINING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
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
                        .height(38.dp)
                        .testTag("done_${chapter.id}")
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
                        containerColor = ShonenSurface,
                        contentColor = ShonenTextSecondary
                    ),
                    border = BorderStroke(1.dp, ShonenBorder),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("skip_${chapter.id}")
                ) {
                    Text(
                        text = "SKIP",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else {
            // Expanded launcher setting
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
fun FocusTimerPanel(
    activeChapter: Chapter,
    secondsRemaining: Int,
    totalMinutes: Int,
    progress: Float,
    isFocusMode: Boolean,
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
        // High Contrast Focus Frame Header
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

        Spacer(modifier = Modifier.height(24.dp))

        // Topic and status labels
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
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Giant HUD timer circular readout or minimalist box
        Box(
            modifier = Modifier
                .size(240.dp)
                .border(2.dp, ShonenBorder, RoundedCornerShape(120.dp))
                .padding(12.dp)
        ) {
            // Futuristic outer circle overlay
            CircularProgressIndicator(
                progress = { progress },
                strokeWidth = 6.dp,
                color = if (activeChapter.isBoss) NeonRed else NeonYellow,
                trackColor = Color(0xFF111111),
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayLarge,
                    color = ShonenTextPrimary,
                    fontSize = 48.sp,
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

        Spacer(modifier = Modifier.height(40.dp))

        // Tactical tips block
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

        Spacer(modifier = Modifier.height(40.dp))

        // Exit / Stop Button
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
                .height(44.dp)
                .testTag("abandon_timer_button")
        ) {
            Text(
                text = if (isFocusMode) "ELEVATE RETREAT (-XP)" else "ABANDON TRAINING",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
