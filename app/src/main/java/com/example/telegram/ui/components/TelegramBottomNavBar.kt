package com.example.telegram.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PeopleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.ui.theme.TelegramBlue

enum class DashboardTab(val id: String, val title: String) {
    CHATS("chats", "Chats"),
    CONTACTS("contacts", "Contacts"),
    SETTINGS("settings", "Settings"),
    PROFILE("profile", "Profile")
}

@Composable
fun TelegramBottomNavBar(
    selectedTab: String,
    unreadChatsCount: Int = 215,
    userInitial: String = "S",
    userAvatarColor: Color = Color(0xFF4CAF50),
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("sticky_bottom_nav_bar"),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column {
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Chats Tab
                BottomNavItem(
                    label = "Chats",
                    isSelected = selectedTab == "chats",
                    badgeText = if (unreadChatsCount > 0) "$unreadChatsCount" else null,
                    onClick = { onTabSelected("chats") },
                    testTag = "nav_tab_chats",
                    icon = { isSelected ->
                        Icon(
                            imageVector = if (isSelected) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Chats",
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // 2. Contacts Tab
                BottomNavItem(
                    label = "Contacts",
                    isSelected = selectedTab == "contacts",
                    onClick = { onTabSelected("contacts") },
                    testTag = "nav_tab_contacts",
                    icon = { isSelected ->
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Person else Icons.Outlined.PersonOutline,
                            contentDescription = "Contacts",
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // 3. Settings Tab
                BottomNavItem(
                    label = "Settings",
                    isSelected = selectedTab == "settings",
                    onClick = { onTabSelected("settings") },
                    testTag = "nav_tab_settings",
                    icon = { isSelected ->
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // 4. Profile Tab (Green Circle Avatar with user initial)
                BottomNavItem(
                    label = "Profile",
                    isSelected = selectedTab == "profile",
                    onClick = { onTabSelected("profile") },
                    testTag = "nav_tab_profile",
                    icon = { isSelected ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(userAvatarColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitial.take(1).uppercase(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    isSelected: Boolean,
    badgeText: String? = null,
    testTag: String,
    onClick: () -> Unit,
    icon: @Composable (isSelected: Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val pillBackground by animateColorAsState(
        targetValue = if (isSelected) TelegramBlue.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(200),
        label = "pill_bg"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(pillBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon(isSelected)

                // Badge Indicator for Unread Messages
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .offset(x = 14.dp, y = (-8).dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TelegramBlue)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
