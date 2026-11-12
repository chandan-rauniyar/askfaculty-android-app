package com.chandan.askfaculty.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SupervisorAccount
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    GROUPS("groups", "Groups", Icons.Outlined.Groups, Icons.Outlined.Groups),
    DOUBTS("doubts", "Doubts", Icons.Outlined.HelpOutline, Icons.Outlined.HelpOutline),
    ALERTS("alerts", "Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    PROFILE("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person),

    ADMIN_DASHBOARD("admin_dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    ADMIN_USERS("admin_users", "Users", Icons.Filled.SupervisorAccount, Icons.Outlined.SupervisorAccount),
    ADMIN_ACADEMICS("admin_academics", "Academics", Icons.Filled.School, Icons.Outlined.School)
}
