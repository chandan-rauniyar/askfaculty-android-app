package com.chandan.askfaculty.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chandan.askfaculty.data.remote.model.GroupDto
import com.chandan.askfaculty.data.remote.model.User
import com.chandan.askfaculty.data.remote.model.UserRole
import com.chandan.askfaculty.ui.groups.GroupDetailScreen
import com.chandan.askfaculty.ui.groups.GroupsScreen
import com.chandan.askfaculty.ui.main.AskFacultyTopBar
import com.chandan.askfaculty.ui.main.DockBadge
import com.chandan.askfaculty.ui.main.DockTab
import com.chandan.askfaculty.ui.main.FloatingGlassDock

@Composable
fun HomeShell(
    user: User?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var selectedTab by remember(user?.role) {
        mutableStateOf(
            when (user?.role) {
                "COLLEGE_ADMIN" -> DockTab.AdminDashboard
                "FACULTY" -> DockTab.Home
                else -> DockTab.Home
            }
        )
    }
    val unreadSummary by viewModel.unreadSummary.collectAsState()

    val groupsTotalUnread = unreadSummary.groups.values.sumOf { it.unreadQuestions + it.unreadMessages }
    val doubtsUnread = unreadSummary.unreadQuestionsCount
    val hasAlerts = unreadSummary.unreadMessagesCount > 0 || unreadSummary.unreadQuestionsCount > 0

    val badges = mapOf(
        DockTab.Groups to if (groupsTotalUnread > 0) DockBadge.Count(groupsTotalUnread, Color(0xFFDC2626)) else null,
        DockTab.Doubts to if (doubtsUnread > 0) DockBadge.Count(doubtsUnread, Color(0xFFF37021)) else null,
        DockTab.FacultyQuestions to if (doubtsUnread > 0) DockBadge.Count(doubtsUnread, Color(0xFFF37021)) else null,
        DockTab.Alerts to if (hasAlerts) DockBadge.Dot else null
    ).filterValues { it != null }.mapValues { it.value!! }

    val userRoleEnum = when (user?.role) {
        "FACULTY" -> UserRole.FACULTY
        "COLLEGE_ADMIN" -> UserRole.COLLEGE_ADMIN
        else -> UserRole.STUDENT
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // AskFaculty Top Bar shown ONLY when on Home or AdminDashboard
            if (selectedTab == DockTab.Home || selectedTab == DockTab.AdminDashboard) {
                AskFacultyTopBar(
                    notificationCount = doubtsUnread + groupsTotalUnread,
                    onNotificationsClick = { selectedTab = DockTab.Alerts }
                )
            }

            // Screen Content with bottom padding so content scrolls behind the floating dock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 120.dp)
            ) {
                when (selectedTab) {
                    DockTab.Home -> {
                        RoleDashboardContent(user, "Student / Faculty Home Dashboard")
                    }
                    DockTab.Groups -> {
                        var selectedGroup by remember { mutableStateOf<GroupDto?>(null) }
                        if (selectedGroup != null) {
                            GroupDetailScreen(
                                groupId = selectedGroup!!.id,
                                onBack = { selectedGroup = null }
                            )
                        } else {
                            GroupsScreen(
                                role = userRoleEnum,
                                onOpenGroup = { group ->
                                    selectedGroup = group
                                },
                                onOpenNotifications = {
                                    selectedTab = DockTab.Alerts
                                }
                            )
                        }
                    }
                    DockTab.Doubts -> {
                        PlaceholderScreen("Doubts Inbox (My Doubts)")
                    }
                    DockTab.FacultyQuestions -> {
                        PlaceholderScreen("Faculty Questions Answering Queue")
                    }
                    DockTab.FacultyMessages -> {
                        PlaceholderScreen("Faculty Messages & Chat Inbox")
                    }
                    DockTab.Alerts -> {
                        PlaceholderScreen("Alerts / Notifications")
                    }
                    DockTab.Profile -> {
                        ProfileContent(user, onLogout)
                    }
                    DockTab.AdminDashboard -> {
                        RoleDashboardContent(user, "College Admin Dashboard")
                    }
                    DockTab.AdminUsers -> {
                        PlaceholderScreen("Admin Users Management")
                    }
                    DockTab.AdminAcademics -> {
                        PlaceholderScreen("Admin Academics Setup")
                    }
                }
            }
        }

        FloatingGlassDock(
            selected = selectedTab,
            userRole = user?.role,
            onTabSelected = { tab -> selectedTab = tab },
            badges = badges,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun RoleDashboardContent(user: User?, title: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome, ${user?.name ?: user?.email ?: "User"}!",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Badge {
            Text(text = "Role: ${user?.role ?: "UNKNOWN"}")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ProfileContent(user: User?, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile: ${user?.name ?: "User"}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Email: ${user?.email}")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Role: ${user?.role}")
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Log Out")
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}
