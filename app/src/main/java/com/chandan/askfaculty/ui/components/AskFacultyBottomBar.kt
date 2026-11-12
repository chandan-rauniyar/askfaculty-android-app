package com.chandan.askfaculty.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chandan.askfaculty.navigation.MainDestination

@Composable
fun AskFacultyBottomBar(
    currentRoute: String,
    userRole: String?,
    onNavigate: (MainDestination) -> Unit,
    modifier: Modifier = Modifier,
    groupsBadgeCount: Int = 4,
    doubtsBadgeCount: Int = 2,
    hasUnreadAlerts: Boolean = true
) {
    val destinations = if (userRole == "COLLEGE_ADMIN") {
        listOf(
            MainDestination.ADMIN_DASHBOARD,
            MainDestination.ADMIN_USERS,
            MainDestination.ADMIN_ACADEMICS,
            MainDestination.GROUPS,
            MainDestination.PROFILE
        )
    } else {
        listOf(
            MainDestination.HOME,
            MainDestination.GROUPS,
            MainDestination.DOUBTS,
            MainDestination.ALERTS,
            MainDestination.PROFILE
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0x22000000),
                ambientColor = Color(0x10000000)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            destinations.forEach { destination ->
                val isSelected = currentRoute == destination.route

                BottomNavItem(
                    destination = destination,
                    isSelected = isSelected,
                    badgeCount = when (destination) {
                        MainDestination.GROUPS -> groupsBadgeCount
                        MainDestination.DOUBTS -> doubtsBadgeCount
                        else -> 0
                    },
                    showBadgeDot = destination == MainDestination.ALERTS && hasUnreadAlerts,
                    onClick = { onNavigate(destination) }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    destination: MainDestination,
    isSelected: Boolean,
    badgeCount: Int,
    showBadgeDot: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .animateContentSize()
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(
                if (isSelected) {
                    Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFF0E6), Color(0xFFFFE5D6))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                } else {
                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            containerColor = Color(0xFFBA1A1A),
                            contentColor = Color.White
                        ) {
                            Text(
                                text = "$badgeCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (showBadgeDot) {
                        Badge(
                            containerColor = Color(0xFFBA1A1A),
                            modifier = Modifier.size(6.dp)
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                    contentDescription = destination.label,
                    tint = if (isSelected) Color(0xFFF37021) else Color(0xFF75777E),
                    modifier = Modifier.size(22.dp)
                )
            }

            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = destination.label,
                        color = Color(0xFF9E4300),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
