package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySession
import com.example.ui.components.HUDMetricTag
import com.example.ui.components.ShonenPanel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BattleLogScreen(
    sessions: List<StudySession>,
    currentStreak: Int,
    totalHours: Double,
    totalXp: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        // Stats Summary Card
        item {
            ShonenPanel(
                borderColor = ShonenBorder,
                backgroundColor = ShonenSurface,
                cutCornerSize = 6.dp
            ) {
                Text(
                    text = "► COMBAT HISTORY ARCHIVES",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "Historical recordings of core mental conditioning and tactical training.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ShonenTextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HUDMetricTag(label = "STREAK", value = "$currentStreak days", color = NeonYellow)
                    HUDMetricTag(label = "BATTLES", value = "${sessions.size} logged", color = NeonCyan)
                    HUDMetricTag(label = "XP GATHERED", value = "$totalXp total", color = NeonGreen)
                }
            }
        }

        item {
            Text(
                text = "► CHRONICLES TIMELINE",
                style = MaterialTheme.typography.titleMedium,
                color = ShonenTextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (sessions.isEmpty()) {
            item {
                ShonenPanel(borderColor = ShonenBorder, backgroundColor = ShonenSurfaceVariant) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO ADVENTURE RECORDS DETECTED",
                            style = MaterialTheme.typography.titleMedium,
                            color = ShonenTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Complete your first mission on the TRAINING screen to begin recording history!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ShonenTextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                TimelineLogItem(session = session)
            }
        }
    }
}

@Composable
fun TimelineLogItem(
    session: StudySession,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val formattedDate = sdf.format(Date(session.timestamp))

    // Determine color coding from session outcome
    val (statusLabel, statusColor, textDesc) = when {
        session.sessionType == "BOSS_BATTLE" && session.outcome == "VICTORY" -> {
            Triple("BOSS SLAIN", NeonRed, "Crushed weakness in [${session.subjectName}] - ${session.chapterTitle}")
        }
        session.sessionType == "SKIPPED_REVISION" -> {
            Triple("FLED DUEL", Color(0xFF555555), "Skipped critical scheduling on [${session.subjectName}] - ${session.chapterTitle}")
        }
        session.sessionType == "REVISION" -> {
            Triple("MINIDUEL VICTORY", NeonCyan, "Conducted revision sparring: [${session.subjectName}] - ${session.chapterTitle}")
        }
        else -> {
            Triple("VICTORY", NeonGreen, "Completed focus training on [${session.subjectName}] - ${session.chapterTitle}")
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // lets Timeline line match the actual card height!
    ) {
        // Elegant Left-Hand Timeline line drawing column
        Box(
            modifier = Modifier
                .width(36.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Background line
            Canvas(modifier = Modifier.fillMaxHeight()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                drawLine(
                    color = ShonenBorder,
                    start = Offset(canvasWidth / 2, 0f),
                    end = Offset(canvasWidth / 2, canvasHeight),
                    strokeWidth = 2.dp.toPx()
                )
            }
            
            // Floating center circle
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(12.dp)
                    .border(2.dp, statusColor, RoundedCornerShape(6.dp))
                    .background(ShonenBackground, RoundedCornerShape(6.dp))
            )
        }

        // Action Data card on the right
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clip(CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
                .border(
                    width = 1.dp,
                    color = if (session.sessionType == "SKIPPED_REVISION") ShonenBorder else statusColor.copy(alpha = 0.3f),
                    shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp)
                )
                .background(ShonenSurfaceVariant)
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = ShonenTextSecondary,
                        fontSize = 9.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = textDesc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ShonenTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (session.durationMinutes > 0) {
                        Text(
                            text = "Duration: ${session.durationMinutes} mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = ShonenTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    if (session.xpGained > 0) {
                        Text(
                            text = "Reward: +${session.xpGained} XP",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
