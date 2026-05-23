package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * A futuristic cybernetic tactical card with angled cut corners, 
 * styled with neon borders to look like a shonen military battle HUD panel.
 */
@Composable
fun ShonenPanel(
    modifier: Modifier = Modifier,
    borderColor: Color = ShonenBorder,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = ShonenSurface,
    cutCornerSize: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = CutCornerShape(topStart = cutCornerSize, bottomEnd = cutCornerSize),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            content()
        }
    }
}

/**
 * A glowing neon progress bar representing a skill XP bar, 
 * showing current progress with cyber details.
 */
@Composable
fun NeonProgressBar(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    color: Color = NeonGreen,
    label: String = "",
    sublabelText: String = "",
    height: Dp = 8.dp
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty() || sublabelText.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ShonenTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = sublabelText,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .background(Color(0xFF151515), shape = RoundedCornerShape(2.dp))
                .border(0.5.dp, ShonenBorder, RoundedCornerShape(2.dp))
        ) {
            // Neon Progress Fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.5f),
                                color
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * A highly visual header pill or tag that acts as a tactical metric 
 * (e.g. STREAK: x5, STATUS: READY).
 */
@Composable
fun HUDMetricTag(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan
) {
    Row(
        modifier = modifier
            .border(1.dp, color, CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
            .background(color.copy(alpha = 0.05f), CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ShonenTextSecondary,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

/**
 * Custom modern tactical tab selector button for fast, clutter-free HUD navigation.
 */
@Composable
fun HUDTabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    accentColor: Color = NeonCyan
) {
    val boxModifier = if (isSelected) {
        Modifier
            .border(1.dp, accentColor, CutCornerShape(topStart = 6.dp))
            .background(accentColor.copy(alpha = 0.1f), CutCornerShape(topStart = 6.dp))
    } else {
        Modifier
            .border(1.dp, ShonenBorder, CutCornerShape(topStart = 6.dp))
            .background(ShonenSurface, CutCornerShape(topStart = 6.dp))
    }

    Box(
        modifier = modifier
            .then(boxModifier)
            .clip(CutCornerShape(topStart = 6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) accentColor else ShonenTextSecondary,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                letterSpacing = 1.sp,
                fontSize = 10.sp
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(1.5.dp)
                        .background(accentColor)
                )
            }
        }
    }
}
