package com.example.ui.screens

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Subject
import com.example.data.model.Chapter
import com.example.ui.components.ShonenPanel
import com.example.ui.theme.*

@Composable
fun BossesScreen(
    bossChapters: List<Chapter>,
    allChapters: List<Chapter>,
    onLaunchBossRaid: (Chapter, durationMinutes: Int, focus: Boolean) -> Unit,
    onPurifyBoss: (Long) -> Unit, // Reset isBoss manually
    onMarkAsBoss: (Long) -> Unit, // Mark a chapter as isBoss manually
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedChapterForBoss by remember { mutableStateOf<Chapter?>(null) }

    // Chapters eligible for manual boss promotion (not already bosses)
    val nonBossChapters = allChapters.filter { !it.isBoss }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Boss Sector Banner Description
        item {
            ShonenPanel(
                borderColor = NeonRed,
                borderWidth = 1.5.dp,
                backgroundColor = ShonenSurfaceVariant,
                cutCornerSize = 8.dp
            ) {
                Text(
                    text = "► WEAKNESS RADAR & BOSS SECTOR",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonRed,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Studying gaps, missed calendars, or manually flagged trouble subjects manifest as Boss Enemies. Enter the chamber to defeat them!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ShonenTextSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                // Quick metrics count
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ShonenBorder, CutCornerShape(topStart = 4.dp))
                        .background(ShonenSurface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE BOSS BARRIERS:",
                            style = MaterialTheme.typography.labelSmall,
                            color = ShonenTextSecondary
                        )
                        Text(
                            text = "${bossChapters.size} DETECTED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (bossChapters.isNotEmpty()) NeonRed else NeonGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Active Bosses List
        if (bossChapters.isEmpty()) {
            item {
                ShonenPanel(
                    borderColor = ShonenBorder,
                    backgroundColor = ShonenSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚔ ALL CRITICAL WEAKNESSES CRUSHED! ⚔",
                            style = MaterialTheme.typography.titleMedium,
                            color = NeonGreen,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "There are no active boss entities detected in your local databanks. Good job! Use the manual promotor below if some topic is giving you trouble.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShonenTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            items(bossChapters, key = { it.id }) { chapter ->
                BossCard(
                    chapter = chapter,
                    onRaid = { onLaunchBossRaid(chapter, 40, true) }, // Boss battles default to 40 min Focus Boss Raids!
                    onPurify = { onPurifyBoss(chapter.id) }
                )
            }
        }

        // Promote a chapter to Boss Category manually
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "► PROMOTE SUBJECT AREA TO BOSS ENEMY",
                style = MaterialTheme.typography.titleMedium,
                color = ShonenTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (nonBossChapters.isEmpty()) {
            item {
                Text(
                    text = "[No other available story arcs to promote. Create more in the SKILLS node panel.]",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShonenTextSecondary
                )
            }
        } else {
            item {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ShonenBorder, CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
                            .background(ShonenSurface)
                            .clickable { expanded = !expanded }
                            .padding(12.dp)
                    ) {
                        Text(
                            text = selectedChapterForBoss?.let { "[${it.subjectName}] ${it.title}" } ?: "CHOOSE TO FLAG WEAK TOPIC...",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedChapterForBoss != null) NeonRed else ShonenTextSecondary,
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
                        nonBossChapters.forEach { chapter ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "[${chapter.subjectName}] ${chapter.title}",
                                        color = ShonenTextPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    ) 
                                },
                                onClick = {
                                    selectedChapterForBoss = chapter
                                    expanded = false
                                },
                                modifier = Modifier.background(ShonenSurface)
                            )
                        }
                    }

                    selectedChapterForBoss?.let { chapter ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onMarkAsBoss(chapter.id)
                                selectedChapterForBoss = null
                            },
                            shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = NeonRed
                            ),
                            border = BorderStroke(1.dp, NeonRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("confirm_manual_boss_promote")
                        ) {
                            Text(
                                text = "SUMMON BOSS ENEMY (FLAG CRITICAL WEAKNESS) ☠",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BossCard(
    chapter: Chapter,
    onRaid: () -> Unit,
    onPurify: () -> Unit,
    modifier: Modifier = Modifier
) {
    ShonenPanel(
        borderColor = NeonRed,
        borderWidth = 1.3.dp,
        backgroundColor = ShonenSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "☠ BOSS ENEMY ACTIVE",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonRed,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chapter.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = ShonenTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Discipline: ${chapter.subjectName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan,
                    fontSize = 12.sp
                )
            }
            
            // Missed revisions indicator block
            Box(
                modifier = Modifier
                    .border(1.dp, NeonRed, RoundedCornerShape(4.dp))
                    .background(NeonRed.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NEGLECTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = ShonenTextSecondary,
                        fontSize = 8.sp
                    )
                    Text(
                        text = "${chapter.missedCount} TIMES",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Boss Battle actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onRaid,
                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonRed,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1.5f)
                    .height(40.dp)
                    .testTag("launch_boss_raid_${chapter.id}")
            ) {
                Text(
                    text = "SLAY RAID (40 MIN FOCUS)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = onPurify,
                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ShonenSurface,
                    contentColor = ShonenTextSecondary
                ),
                border = BorderStroke(1.dp, ShonenBorderBright),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag("purify_boss_${chapter.id}")
            ) {
                Text(
                    text = "PURIFY ARC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
