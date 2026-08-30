package com.example.telegram.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.data.models.UserSettings
import com.example.telegram.ui.theme.TelegramBlue
import com.example.telegram.ui.theme.TelegramLightBlue

@Composable
fun TelegramDrawer(
    userSettings: UserSettings,
    onNavigate: (String) -> Unit,
    onOpenNewChatDialog: (String) -> Unit,
    onToggleNightMode: () -> Unit,
    onCloseDrawer: () -> Unit,
    onLogOut: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .testTag("telegram_drawer")
    ) {
        // Drawer Header with Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            TelegramBlue,
                            TelegramLightBlue
                        )
                    )
                )
                .statusBarsPadding()
                .clickable {
                    onCloseDrawer()
                    onNavigate("profile")
                }
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userSettings.myName.take(1).uppercase(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Dark/Light Mode toggle button in header
                    IconButton(
                        onClick = onToggleNightMode,
                        modifier = Modifier.testTag("toggle_night_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (userSettings.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Night Mode",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userSettings.myName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = userSettings.myPhone,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Text(
                    text = "@${userSettings.myUsername}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu Items
        DrawerMenuItem(
            icon = Icons.Default.Group,
            title = "New Group",
            onClick = {
                onCloseDrawer()
                onOpenNewChatDialog("GROUP")
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Lock,
            title = "New Secret Chat",
            onClick = {
                onCloseDrawer()
                onOpenNewChatDialog("SECRET")
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.RecordVoiceOver,
            title = "New Channel",
            onClick = {
                onCloseDrawer()
                onOpenNewChatDialog("CHANNEL")
            }
        )

        Divider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        DrawerMenuItem(
            icon = Icons.Default.People,
            title = "Contacts",
            onClick = {
                onCloseDrawer()
                onNavigate("contacts")
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Call,
            title = "Calls",
            onClick = {
                onCloseDrawer()
                onNavigate("calls")
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Bookmark,
            title = "Saved Messages",
            onClick = {
                onCloseDrawer()
                onNavigate("saved_messages")
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Settings,
            title = "Settings",
            onClick = {
                onCloseDrawer()
                onNavigate("settings")
            }
        )

        Divider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        DrawerMenuItem(
            icon = Icons.Default.PersonAdd,
            title = "Invite Friends",
            onClick = {
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Star,
            title = "Telegram Features",
            onClick = {
                onCloseDrawer()
                onNavigate("settings")
            }
        )

        Divider(
            modifier = Modifier.padding(vertical = 6.dp),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )

        DrawerMenuItem(
            icon = Icons.Default.ExitToApp,
            title = "Log Out",
            onClick = {
                onCloseDrawer()
                onLogOut()
            }
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
