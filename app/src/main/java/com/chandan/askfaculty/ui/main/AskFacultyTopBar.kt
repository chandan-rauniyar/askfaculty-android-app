package com.chandan.askfaculty.ui.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Colors (from the HTML tailwind config) ──
private val Primary = Color(0xFFF37021)
private val PrimaryEnd = Color(0xFFE05B0D)
private val Ink = Color(0xFF0F172A)
private val Slate500 = Color(0xFF64748B)
private val Slate700 = Color(0xFF334155)
private val BadgeRed = Color(0xFFBA1A1A)

@Composable
fun AskFacultyTopBar(
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
    universityName: String = "Lovely Professional University"
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.10f)
                )
                .clip(shape)
                .background(Color.White.copy(alpha = 0.88f))
                .border(1.dp, Color.White.copy(alpha = 0.95f), shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BrandBlock(universityName = universityName)
            NotificationBell(count = notificationCount, onClick = onNotificationsClick)
        }
    }
}

@Composable
private fun BrandBlock(universityName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    spotColor = Primary.copy(alpha = 0.30f),
                    ambientColor = Primary.copy(alpha = 0.20f)
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Primary, PrimaryEnd))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = "AskFaculty",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "AskFaculty",
                    color = Ink,
                    style = TextStyle(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        lineHeight = 17.sp
                    )
                )
                Spacer(Modifier.width(6.dp))
                PulseDot()
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = universityName,
                color = Slate500,
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.1).sp
                )
            )
        }
    }
}

@Composable
private fun PulseDot() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(Primary)
    )
}

@Composable
private fun NotificationBell(count: Int, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, label = "bellScale")

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interaction,
                indication = ripple(color = Primary),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = "Notifications",
            tint = Slate700,
            modifier = Modifier.size(24.dp)
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .defaultMinSize(minWidth = 17.dp, minHeight = 17.dp)
                    .shadow(2.dp, CircleShape, spotColor = BadgeRed.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(BadgeRed)
                    .border(BorderStroke(2.dp, Color.White), CircleShape)
                    .padding(horizontal = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                )
            }
        }
    }
}
