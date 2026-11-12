package com.chandan.askfaculty.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.chandan.askfaculty.data.remote.model.User
import com.chandan.askfaculty.ui.groups.GroupsScreen

sealed class HomeTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : HomeTab("home", "Home", Icons.Default.Home)
    object Groups : HomeTab("groups", "Groups", Icons.Default.Group)
    object Doubts : HomeTab("doubts", "Doubts", Icons.Default.QuestionAnswer)
    object Alerts : HomeTab("alerts", "Alerts", Icons.Default.Notifications)
    object Profile : HomeTab("profile", "Profile", Icons.Default.Person)
}

@Composable
fun HomeShell(
    user: User?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf<HomeTab>(HomeTab.Home) }
    val tabs = listOf(HomeTab.Home, HomeTab.Groups, HomeTab.Doubts, HomeTab.Alerts, HomeTab.Profile)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                is HomeTab.Home -> {
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
                            text = "Home Dashboard Overview",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                is HomeTab.Groups -> {
                    GroupsScreen(
                        onGroupClick = { group ->
                            // TODO: Navigate to Group Detail (G2)
                        }
                    )
                }
                is HomeTab.Doubts -> {
                    PlaceholderScreen("Doubts Inbox (My Doubts)")
                }
                is HomeTab.Alerts -> {
                    PlaceholderScreen("Alerts / Notifications")
                }
                is HomeTab.Profile -> {
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
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Log Out")
                        }
                    }
                }
            }
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
