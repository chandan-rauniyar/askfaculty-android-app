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
import com.chandan.askfaculty.data.remote.model.User
import com.chandan.askfaculty.ui.groups.GroupsScreen
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F9FB))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp)
        ) {
            when (selectedTab) {
                DockTab.Home -> {
                    RoleDashboardContent(user, "Student / Faculty Home Dashboard")
                }
                DockTab.Groups -> {
                    GroupsScreen(
                        onGroupClick = { group ->
                            // TODO: Navigate to Group Detail (G2)
                        }
                    )
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
