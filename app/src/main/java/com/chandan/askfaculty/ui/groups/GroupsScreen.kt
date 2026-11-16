package com.chandan.askfaculty.ui.groups

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandan.askfaculty.data.remote.model.GroupDto
import com.chandan.askfaculty.data.remote.model.GroupFilter
import com.chandan.askfaculty.data.remote.model.GroupsUiState
import com.chandan.askfaculty.data.remote.model.UserRole

private val Surface = Color(0xFFF7F9FB)
private val OnSurface = Color(0xFF191C1E)
private val BarSubtitle = Color(0xFF45474C)
private val PrimaryContainer = Color(0xFFF37021)
private val Primary = Color(0xFFA04100)
private val Tertiary = Color(0xFF565E74)
private val ErrorRed = Color(0xFFBA1A1A)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate500 = Color(0xFF64748B)
private val Slate600 = Color(0xFF475569)
private val Slate700 = Color(0xFF334155)

private val CardTitle = Color(0xFF111827)
private val CardSubtitle = Color(0xFF4B5563)
private val Chevron = Color(0xFF9CA3AF)

private val ClassBg = Color(0xFFFFF0E6)
private val SubjectBg = Color(0xFFEFF6FF)
private val SubjectTint = Color(0xFF2563EB)
private val GeneralBg = Color(0xFFF0EEF8)

private val BadgeSolid = Color(0xFFDC2626)
private val BadgeSoftBg = Color(0xFFFEE2E2)
private val BadgeSoftText = Color(0xFFB91C1C)

private val NoticeBg = Color(0xFFFFFBEB)
private val NoticeBorder = Color(0xFFFDE68A)
private val NoticeText = Color(0xFF92400E)
private val NoticeIcon = Color(0xFFB45309)

private val Jakarta: FontFamily = FontFamily.Default
private val Inter: FontFamily = FontFamily.Default

private enum class UnreadBadgeStyle { Solid, Soft }
private val UnreadStyle = UnreadBadgeStyle.Soft

private fun GroupDto.bucket(): GroupFilter = when (type.uppercase()) {
    "CLASS" -> GroupFilter.Classes
    "SUBJECT" -> GroupFilter.Subjects
    else -> GroupFilter.Other
}

private data class TypeStyle(val label: String, val icon: ImageVector, val tint: Color, val bg: Color)

private fun GroupFilter.style(): TypeStyle = when (this) {
    GroupFilter.Classes -> TypeStyle("CLASS", Icons.Outlined.School, Primary, ClassBg)
    GroupFilter.Subjects -> TypeStyle("SUBJECT", Icons.AutoMirrored.Outlined.MenuBook, SubjectTint, SubjectBg)
    else -> TypeStyle("GENERAL", Icons.Outlined.Groups, Tertiary, GeneralBg)
}

private fun GroupDto.subtitle(): String? = when (bucket()) {
    GroupFilter.Classes -> section?.name
    GroupFilter.Subjects -> subject?.department?.name
    else -> description
}?.takeIf { it.isNotBlank() }

private fun GroupDto.readOnlyText(role: UserRole): String? {
    if (role != UserRole.STUDENT || !chatMode.equals("FACULTY_ONLY", ignoreCase = true)) return null
    return if (type.equals("GENERAL", true)) "Official notices · Read-only chat"
    else "Faculty announcements only · Read-only chat"
}

private val TopBarHeight = 60.dp
private val TopBarGap = 4.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    viewModel: GroupsViewModel = hiltViewModel(),
    role: UserRole = UserRole.STUDENT,
    onOpenGroup: (GroupDto) -> Unit,
    onOpenNotifications: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val visible = remember(state.groups, state.filter) {
        if (state.filter == GroupFilter.All) state.groups else state.groups.filter { it.bucket() == state.filter }
    }

    val err = state.error

    Box(modifier = modifier.fillMaxSize().background(Surface)) {
        PullToRefreshBox(isRefreshing = state.refreshing, onRefresh = { viewModel.refresh(userInitiated = true) }, modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = statusTop + TopBarGap + TopBarHeight + 16.dp,
                    bottom = bottom + 120.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { FilterChips(selected = state.filter, onSelect = viewModel::setFilter) }

                when {
                    state.loading -> item { CenterBox { CircularProgressIndicator(color = PrimaryContainer) } }
                    err != null && state.groups.isEmpty() -> item { ErrorBox(err) { viewModel.refresh(true) } }
                    visible.isEmpty() -> item { CenterBox { Text("No groups yet", color = Slate500, fontSize = 14.sp) } }
                    else -> items(visible, key = { it.id }) { g ->
                        GroupCard(g, state.unreadByGroup[g.id] ?: 0, role) { onOpenGroup(g) }
                    }
                }
            }
        }

        FloatingTopBar(
            title = "Groups",
            subtitle = "Your classes and subject groups",
            bellCount = state.bellCount,
            onBell = onOpenNotifications,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = statusTop + TopBarGap)
                .padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun FloatingTopBar(
    title: String, subtitle: String, bellCount: Int, onBell: () -> Unit, modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TopBarHeight)
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(.06f), spotColor = Color.Black.copy(.08f))
            .clip(shape)
            .background(Color.White.copy(alpha = .95f))
            .border(1.dp, Slate200.copy(alpha = .8f), shape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).shadow(1.dp, CircleShape).clip(CircleShape).background(ClassBg),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Group, null, tint = PrimaryContainer, modifier = Modifier.size(24.dp)) }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                color = OnSurface, lineHeight = 20.sp)
            Text(subtitle, fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                color = BarSubtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            IconButton(onClick = onBell, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Outlined.Notifications, "Notifications", tint = OnSurface, modifier = Modifier.size(24.dp))
            }
            if (bellCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                        .defaultMinSize(minWidth = 17.dp, minHeight = 17.dp)
                        .shadow(2.dp, CircleShape, spotColor = ErrorRed.copy(alpha = 0.4f))
                        .clip(CircleShape)
                        .background(ErrorRed)
                        .border(BorderStroke(2.dp, Color.White), CircleShape)
                        .padding(horizontal = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (bellCount > 99) "99+" else bellCount.toString(),
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
}

@Composable
private fun FilterChips(selected: GroupFilter, onSelect: (GroupFilter) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        GroupFilter.entries.forEach { f ->
            val on = f == selected
            val shape = RoundedCornerShape(percent = 50)
            Box(
                Modifier
                    .height(36.dp)
                    .shadow(if (on) 4.dp else 1.dp, shape,
                        ambientColor = PrimaryContainer.copy(.3f), spotColor = PrimaryContainer.copy(.3f))
                    .clip(shape)
                    .background(if (on) PrimaryContainer else Color.White)
                    .then(if (on) Modifier else Modifier.border(BorderStroke(1.dp, Slate200), shape))
                    .clickable { onSelect(f) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(f.label, fontFamily = Jakarta, fontSize = 12.sp,
                    fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (on) Color.White else Slate700)
            }
        }
    }
}

@Composable
private fun GroupCard(group: GroupDto, unread: Int, role: UserRole, onClick: () -> Unit) {
    val style = group.bucket().style()
    val cardShape = RoundedCornerShape(16.dp)
    val iconShape = RoundedCornerShape(12.dp)
    val notice = group.readOnlyText(role)

    Column(
        Modifier.fillMaxWidth()
            .shadow(1.dp, cardShape, ambientColor = Color.Black.copy(.05f), spotColor = Color.Black.copy(.08f))
            .clip(cardShape)
            .background(Color.White)
            .border(1.dp, Slate200, cardShape)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).shadow(1.dp, iconShape).clip(iconShape).background(style.bg),
                    contentAlignment = Alignment.Center,
                ) { Icon(style.icon, null, tint = style.tint, modifier = Modifier.size(26.dp)) }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(group.name, fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        lineHeight = 22.sp, color = CardTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            style.label, color = style.tint, fontFamily = Jakarta, fontWeight = FontWeight.Bold,
                            fontSize = 11.sp, letterSpacing = 0.5.sp,
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(style.bg)
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                        group.subtitle()?.let {
                            Spacer(Modifier.width(6.dp))
                            Text("· $it", fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp,
                                color = CardSubtitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (unread > 0) UnreadBadge(unread)
                Icon(Icons.Outlined.ChevronRight, null, tint = Chevron, modifier = Modifier.size(20.dp))
            }
        }

        if (notice != null) {
            Spacer(Modifier.height(12.dp))
            val ns = RoundedCornerShape(8.dp)
            Row(
                Modifier.fillMaxWidth().clip(ns).background(NoticeBg).border(1.dp, NoticeBorder, ns)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Lock, null, tint = NoticeIcon, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text(notice, fontFamily = Jakarta, fontWeight = FontWeight.Medium, fontSize = 11.sp, color = NoticeText)
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val soft = UnreadStyle == UnreadBadgeStyle.Soft
    val n = if (count > 99) "99+" else "$count"
    Box(
        Modifier.defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .shadow(1.dp, CircleShape).clip(RoundedCornerShape(percent = 50))
            .background(if (soft) BadgeSoftBg else BadgeSolid)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(if (soft) "$n unread" else n, color = if (soft) BadgeSoftText else Color.White,
            fontFamily = Jakarta, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) =
    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) { content() }

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) = CenterBox {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, color = Slate600, fontSize = 14.sp, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) { Text("Retry", color = Primary, fontWeight = FontWeight.Bold) }
    }
}
