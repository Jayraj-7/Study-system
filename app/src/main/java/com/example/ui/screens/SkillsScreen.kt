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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.ui.components.NeonProgressBar
import com.example.ui.components.ShonenPanel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SkillsScreen(
    subjects: List<Subject>,
    chapters: List<Chapter>,
    onAddSubject: (String, String) -> Unit,
    onDeleteSubject: (Long) -> Unit,
    onAddChapter: (Long, String, String, Boolean) -> Unit,
    onDeleteChapter: (Long) -> Unit,
    onSetChapterBoss: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Dialog visibility states Since we want to be clean and fast
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var selectedSubjectForChapter by remember { mutableStateOf<Subject?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Skill Panel Controller Header
        item {
            ShonenPanel(
                borderColor = ShonenBorder,
                backgroundColor = ShonenSurfaceVariant,
                cutCornerSize = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "► SUBJECT PROGRESSIONS",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Treat school subjects as combat disciplines. Level up nodes to 100!",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShonenTextSecondary
                        )
                    }
                    Button(
                        onClick = { showAddSubjectDialog = true },
                        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = NeonCyan
                        ),
                        border = BorderStroke(1.dp, NeonCyan),
                        modifier = Modifier.testTag("add_subject_button")
                    ) {
                        Text(
                            text = "+ SKILL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (subjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "[NO PASSIVE SKILLS CONFIGURED YET. ENGAGE THE COVENANT TO LEARN]",
                        color = ShonenTextSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else {
            items(subjects, key = { it.id }) { subject ->
                val subjectColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorHex)) }.getOrDefault(NeonGreen)
                val subjectChapters = chapters.filter { it.subjectId == subject.id }

                SubjectSkillCard(
                    subject = subject,
                    color = subjectColor,
                    chapters = subjectChapters,
                    onDeleteSubject = { onDeleteSubject(subject.id) },
                    onAddChapterClick = { selectedSubjectForChapter = subject },
                    onDeleteChapter = onDeleteChapter,
                    onSetChapterBoss = onSetChapterBoss
                )
            }
        }
    }

    // --- CREATE NEW SUBJECT DIALOG ---
    if (showAddSubjectDialog) {
        var subjectName by remember { mutableStateOf("") }
        val colorsPalette = listOf("#00FF66", "#00E5FF", "#FF9100", "#E040FB", "#FF0055", "#FFEA00")
        var selectedColorHex by remember { mutableStateOf("#00FF66") }

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = {
                Text(
                    text = "ACQUIRE NEW DISCIPLINE Node",
                    style = MaterialTheme.typography.titleLarge,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Initialize an official school subject as a character skill node:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShonenTextSecondary
                    )
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject Name (e.g. AP PHYSICS)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ShonenBorder,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = ShonenTextSecondary,
                            focusedTextColor = ShonenTextPrimary,
                            unfocusedTextColor = ShonenTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("subject_name_input"),
                        singleLine = true
                    )

                    // Color block select
                    Text(
                        text = "HUD GLOW CALIBRATION:",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShonenTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorsPalette.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            val isSelected = selectedColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) ShonenTextPrimary else Color.Transparent,
                                        shape = RoundedCornerShape(17.dp)
                                    )
                                    .clickable { selectedColorHex = hex }
                                    .testTag("color_$hex")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (subjectName.isNotBlank()) {
                            onAddSubject(subjectName, selectedColorHex)
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    modifier = Modifier.testTag("confirm_add_subject")
                ) {
                    Text("LEARN SKILL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("ABORT", color = ShonenTextSecondary)
                }
            },
            containerColor = ShonenSurface,
            tonalElevation = 6.dp
        )
    }

    // --- CREATE NEW CHAPTER (ARC) DIALOG ---
    if (selectedSubjectForChapter != null) {
        val subject = selectedSubjectForChapter!!
        var chapterTitle by remember { mutableStateOf("") }
        var isImmediateBoss by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { selectedSubjectForChapter = null },
            title = {
                Text(
                    text = "CHRONICLE NEW ARC (CHAPTER)",
                    style = MaterialTheme.typography.titleLarge,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Adding custom chapter arc to ${subject.name}. Revisions are auto-scheduled in 1d, 3d, 7d, 14d, 30d upon completions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ShonenTextSecondary
                    )
                    OutlinedTextField(
                        value = chapterTitle,
                        onValueChange = { chapterTitle = it },
                        label = { Text("Chapter Title (e.g. Thermodynamics Basics)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = ShonenBorder,
                            focusedLabelColor = NeonCyan,
                            unfocusedLabelColor = ShonenTextSecondary,
                            focusedTextColor = ShonenTextPrimary,
                            unfocusedTextColor = ShonenTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("chapter_title_input"),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = "INITIALIZE AS BOSS ENEMY",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isImmediateBoss) NeonRed else ShonenTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mark immediately as a critical weakness / Boss room target.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ShonenTextSecondary,
                                fontSize = 9.sp
                            )
                        }
                        Switch(
                            checked = isImmediateBoss,
                            onCheckedChange = { isImmediateBoss = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonRed,
                                checkedTrackColor = NeonRed.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("is_boss_switch")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (chapterTitle.isNotBlank()) {
                            onAddChapter(subject.id, subject.name, chapterTitle, isImmediateBoss)
                            selectedSubjectForChapter = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    modifier = Modifier.testTag("confirm_add_chapter")
                ) {
                    Text("PUBLISH ARC")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedSubjectForChapter = null }) {
                    Text("ABORT", color = ShonenTextSecondary)
                }
            },
            containerColor = ShonenSurface
        )
    }
}

@Composable
fun SubjectSkillCard(
    subject: Subject,
    color: Color,
    chapters: List<Chapter>,
    onDeleteSubject: () -> Unit,
    onAddChapterClick: () -> Unit,
    onDeleteChapter: (Long) -> Unit,
    onSetChapterBoss: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    // Collapse chapters to minimize visual noise
    var isExpanded by remember { mutableStateOf(false) }

    ShonenPanel(
        borderColor = color.copy(alpha = 0.7f),
        backgroundColor = ShonenSurface,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "LEVEL UNIT Node",
                    style = MaterialTheme.typography.labelSmall,
                    color = ShonenTextSecondary,
                    fontSize = 9.sp
                )
            }
            // Level circle
            Box(
                modifier = Modifier
                    .border(1.dp, color, RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.05f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LVL ${subject.level}",
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // XP bar
        val currentLevelXpNeeded = subject.xpNeededForNextLevel()
        val xpProgress = if (currentLevelXpNeeded > 0) subject.xp.toFloat() / currentLevelXpNeeded else 0f
        
        NeonProgressBar(
            progress = xpProgress,
            color = color,
            label = "XP GATHERED",
            sublabelText = "${subject.xp} / $currentLevelXpNeeded XP"
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        // Studied hours
        Text(
            text = String.format(Locale.getDefault(), "Combat training time logged: %.2f hours", subject.totalHours),
            style = MaterialTheme.typography.bodySmall,
            color = ShonenTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Footer Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Show Chapters" Collapse Trigger
            Text(
                text = if (isExpanded) "▼ RETRACT STORY ARCS (${chapters.size})" else "▶ REVEAL STORY ARCS (${chapters.size})",
                color = NeonCyan,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { isExpanded = !isExpanded }
                    .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAddChapterClick,
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShonenSurfaceVariant,
                        contentColor = ShonenTextPrimary
                    ),
                    border = BorderStroke(1.dp, ShonenBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("+ ARC", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }

                Button(
                    onClick = onDeleteSubject,
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShonenSurfaceVariant,
                        contentColor = NeonRed.copy(alpha = 0.8f)
                    ),
                    border = BorderStroke(1.dp, ShonenBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("delete_subject_${subject.id}")
                ) {
                    Text("DELETE NODE", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
            }
        }

        // Expanded chapter list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .border(0.5.dp, ShonenBorder, CutCornerShape(bottomEnd = 6.dp))
                    .background(Color(0xFF070707))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (chapters.isEmpty()) {
                    Text(
                        text = "NO COVENANT ARCS ESTABLISHED.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ShonenTextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    chapters.forEach { chapter ->
                        ChapterListRow(
                            chapter = chapter,
                            onDelete = { onDeleteChapter(chapter.id) },
                            onToggleBoss = { onSetChapterBoss(chapter.id, !chapter.isBoss) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChapterListRow(
    chapter: Chapter,
    onDelete: () -> Unit,
    onToggleBoss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val nextRevStr = sdf.format(Date(chapter.nextRevisionTime))
    val isOverdue = chapter.nextRevisionTime <= System.currentTimeMillis()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, ShonenBorder, RoundedCornerShape(2.dp))
            .background(ShonenSurface)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = ShonenTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (chapter.isBoss) {
                    Text(
                        text = " [BOSS]",
                        color = NeonRed,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                text = if (isOverdue) "Next Revision Due Today" else "Next Duel: $nextRevStr (Streak: x${chapter.consecutiveDoneCount})",
                style = MaterialTheme.typography.bodySmall,
                color = if (isOverdue) NeonCyan else ShonenTextSecondary,
                fontSize = 11.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Toggle Boss weakness status
            IconButton(
                onClick = onToggleBoss,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("toggle_boss_${chapter.id}")
            ) {
                Icon(
                    imageVector = if (chapter.isBoss) Icons.Filled.Warning else Icons.Outlined.Warning,
                    contentDescription = "Toggle Boss Weakness",
                    tint = if (chapter.isBoss) NeonRed else ShonenTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Trash Chapter
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("delete_chapter_${chapter.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete Chapter",
                    tint = ShonenTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
