package com.chandan.askfaculty.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── AskFaculty Brand Tokens ──
private val BrandOrange = Color(0xFFF37021)
private val BrandDark = Color(0xFF9E4300)
private val Slate500 = Color(0xFF64748B)
private val Slate600 = Color(0xFF475569)
private val SlateInk = Color(0xFF0F172A)
private val RedBadge = Color(0xFFDC2626)
private val PillStart = Color(0xFFFFF0E6)
private val PillEnd = Color(0xFFFFE2D1)

// ── Tabs ──
enum class DockTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector
) {
    // Student & Common
    Home("home", "Home", Icons.Outlined.Home, Icons.Filled.Home),
    Groups("groups", "Groups", Icons.Outlined.Groups, Icons.Filled.Groups),
    Doubts("doubts", "Doubts", Icons.Outlined.HelpOutline, Icons.Filled.QuestionAnswer),
    Alerts("alerts", "Alerts", Icons.Outlined.Notifications, Icons.Filled.Notifications),
    Profile("profile", "Profile", Icons.Outlined.Person, Icons.Filled.Person),

    // Faculty specific
    FacultyQuestions("questions", "Questions", Icons.Outlined.HelpOutline, Icons.Filled.QuestionAnswer),
    FacultyMessages("messages", "Messages", Icons.Outlined.Chat, Icons.Filled.Chat),

    // Admin specific
    AdminDashboard("dashboard", "Dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
    AdminUsers("users", "Users", Icons.Outlined.SupervisorAccount, Icons.Filled.SupervisorAccount),
    AdminAcademics("academics", "Academics", Icons.Outlined.School, Icons.Filled.School)
}

// ── Badges ──
sealed interface DockBadge {
    data class Count(val value: Int, val color: Color = RedBadge) : DockBadge
    object Dot : DockBadge
}

// ── Public entry point ──
@Composable
fun FloatingGlassDock(
    selected: DockTab,
    userRole: String?,
    onTabSelected: (DockTab) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<DockTab, DockBadge> = emptyMap()
) {
    val tabs = remember(userRole) {
        when (userRole) {
            "COLLEGE_ADMIN" -> listOf(
                DockTab.AdminDashboard,
                DockTab.AdminUsers,
                DockTab.AdminAcademics,
                DockTab.Groups,
                DockTab.Profile
            )
            "FACULTY" -> listOf(
                DockTab.Home,
                DockTab.FacultyQuestions,
                DockTab.Groups,
                DockTab.FacultyMessages,
                DockTab.Profile
            )
            else -> listOf(
                DockTab.Home,
                DockTab.Groups,
                DockTab.Doubts,
                DockTab.Alerts,
                DockTab.Profile
            )
        }
    }

    GlassIsland(modifier = modifier) {
        tabs.forEach { tab ->
            DockItem(
                tab = tab,
                selected = tab == selected,
                badge = badges[tab],
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

// ── Glass container ──
@Composable
private fun GlassIsland(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = shape,
                ambientColor = SlateInk.copy(alpha = 0.10f),
                spotColor = SlateInk.copy(alpha = 0.14f)
            ),
        shape = shape,
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

// ── One tab ──
@Composable
private fun DockItem(
    tab: DockTab,
    selected: Boolean,
    badge: DockBadge?,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val iconTint by animateColorAsState(if (selected) BrandOrange else Slate600, label = "iconTint")

    val containerModifier = Modifier
        .animateContentSize()
        .clip(shape)
        .then(
            if (selected) Modifier
                .background(Brush.linearGradient(listOf(PillStart, PillEnd)))
                .border(1.dp, BrandOrange.copy(alpha = 0.22f), shape)
            else Modifier
        )
        .clickable(role = Role.Tab, onClick = onClick)
        .semantics { contentDescription = tab.label }

    if (selected) {
        Row(
            modifier = containerModifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = tab.iconSelected,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = tab.label,
                color = BrandDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.2).sp
            )
        }
    } else {
        Column(
            modifier = containerModifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                when (badge) {
                    is DockBadge.Count -> CountBadge(
                        badge, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-6).dp)
                    )
                    DockBadge.Dot -> DotBadge(
                        Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp)
                    )
                    null -> Unit
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = tab.label,
                color = Slate500,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Badges ──
@Composable
private fun CountBadge(badge: DockBadge.Count, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(1.5.dp)
    ) {
        Box(
            modifier = Modifier
                .defaultMinSize(minWidth = 15.dp, minHeight = 15.dp)
                .clip(CircleShape)
                .background(badge.color)
                .padding(horizontal = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (badge.value > 99) "99+" else badge.value.toString(),
                color = Color.White,
                style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 11.sp)
            )
        }
    }
}

@Composable
private fun DotBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White)
            .padding(1.5.dp)
            .size(8.dp)
            .clip(CircleShape)
            .background(RedBadge)
    )
}
