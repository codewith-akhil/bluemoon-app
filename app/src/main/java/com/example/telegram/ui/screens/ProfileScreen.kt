package com.example.telegram.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.data.models.UserSettings
import com.example.telegram.ui.components.ContactAvatar
import com.example.telegram.ui.theme.SecretChatGreen
import com.example.telegram.ui.theme.TelegramBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userSettings: UserSettings,
    onNavigateToSettings: () -> Unit,
    onUpdateSettings: (UserSettings) -> Unit,
    onLogOut: () -> Unit,
    onAddPost: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableStateOf("Posts") } // "Posts" or "Archived Posts"
    var showMenu by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showEditInfoDialog by remember { mutableStateOf(false) }
    var showSetPhotoDialog by remember { mutableStateOf(false) }
    var showAddPostDialog by remember { mutableStateOf(false) }

    val userAvatarColor = remember(userSettings.avatarColorHex) {
        try {
            Color(android.graphics.Color.parseColor(userSettings.avatarColorHex))
        } catch (_: Exception) {
            Color(0xFF4CAF50)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // Room for sticky bottom nav
                .testTag("profile_screen")
        ) {
            // 1. Top Bar with QR Scanner (left) and 3-dots Menu (right)
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { showQrDialog = true },
                        modifier = Modifier.testTag("profile_qr_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "QR Code",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("profile_more_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Profile") },
                                onClick = {
                                    showMenu = false
                                    showEditInfoDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Change Photo Color") },
                                onClick = {
                                    showMenu = false
                                    showSetPhotoDialog = true
                                },
                                leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Profile Link") },
                                onClick = {
                                    showMenu = false
                                    clipboardManager.setText(AnnotatedString("https://t.me/${userSettings.myUsername}"))
                                    Toast.makeText(context, "Profile link copied!", Toast.LENGTH_SHORT).show()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Log Out", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onLogOut()
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // 2. Profile Avatar & Name Header (Matching Screenshot)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Large Green / Colored Avatar with Initial "S"
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(userAvatarColor)
                        .clickable { showSetPhotoDialog = true }
                        .testTag("profile_avatar"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userSettings.myName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User Name
                Text(
                    text = userSettings.myName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle "online"
                Text(
                    text = "online",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Quick Action Buttons Row (Set Photo, Edit Info, Settings)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "Set Photo",
                    onClick = { showSetPhotoDialog = true },
                    modifier = Modifier.weight(1f)
                )

                ProfileActionButton(
                    icon = Icons.Default.Edit,
                    label = "Edit Info",
                    onClick = { showEditInfoDialog = true },
                    modifier = Modifier.weight(1f)
                )

                ProfileActionButton(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Contact Information Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    // Mobile
                    ProfileInfoRow(
                        title = userSettings.myPhone,
                        subtitle = "Mobile",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(userSettings.myPhone))
                            Toast.makeText(context, "Phone number copied!", Toast.LENGTH_SHORT).show()
                        }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )

                    // Bio
                    ProfileInfoRow(
                        title = userSettings.myBio.ifBlank { "No bio added yet" },
                        subtitle = "Bio",
                        onClick = { showEditInfoDialog = true }
                    )

                    Divider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )

                    // Username
                    ProfileInfoRow(
                        title = "@${userSettings.myUsername}",
                        subtitle = "Username",
                        onClick = {
                            clipboardManager.setText(AnnotatedString("@${userSettings.myUsername}"))
                            Toast.makeText(context, "Username copied!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. Tabs: Posts & Archived Posts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileTabPill(
                    label = "Posts",
                    isSelected = selectedTab == "Posts",
                    onClick = { selectedTab = "Posts" }
                )

                ProfileTabPill(
                    label = "Archived Posts",
                    isSelected = selectedTab == "Archived Posts",
                    onClick = { selectedTab = "Archived Posts" }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 6. Posts Empty State Content (Matching Screenshot)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedTab == "Posts") "No posts yet..." else "No archived posts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Publish photos or videos to display on your profile",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // "Add a post" Blue Pill Button
                Button(
                    onClick = { showAddPostDialog = true },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    modifier = Modifier.testTag("add_post_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add a post",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Edit Info Dialog
    if (showEditInfoDialog) {
        var editName by remember { mutableStateOf(userSettings.myName) }
        var editBio by remember { mutableStateOf(userSettings.myBio) }
        var editUsername by remember { mutableStateOf(userSettings.myUsername) }
        var editPhone by remember { mutableStateOf(userSettings.myPhone) }

        AlertDialog(
            onDismissRequest = { showEditInfoDialog = false },
            title = { Text("Edit Profile Info", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Mobile Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        prefix = { Text("@") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateSettings(
                            userSettings.copy(
                                myName = editName.ifBlank { "User" },
                                myBio = editBio,
                                myUsername = editUsername.ifBlank { "user" },
                                myPhone = editPhone
                            )
                        )
                        showEditInfoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditInfoDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Set Photo Color Dialog
    if (showSetPhotoDialog) {
        val colorOptions = listOf(
            "#4CAF50" to "Emerald Green",
            "#2481CC" to "Telegram Blue",
            "#E91E63" to "Pink Ruby",
            "#9C27B0" to "Royal Purple",
            "#FF9800" to "Sunset Orange",
            "#009688" to "Teal Cyan",
            "#3F51B5" to "Indigo"
        )

        AlertDialog(
            onDismissRequest = { showSetPhotoDialog = false },
            title = { Text("Choose Avatar Color", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a background color for your profile avatar:")
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colorOptions.take(4).forEach { (hex, _) ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        onUpdateSettings(userSettings.copy(avatarColorHex = hex))
                                        showSetPhotoDialog = false
                                    }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colorOptions.drop(4).forEach { (hex, _) ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        onUpdateSettings(userSettings.copy(avatarColorHex = hex))
                                        showSetPhotoDialog = false
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSetPhotoDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Add Post Dialog
    if (showAddPostDialog) {
        var postCaption by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddPostDialog = false },
            title = { Text("Publish to Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Share a photo note or message with your contacts:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = postCaption,
                        onValueChange = { postCaption = it },
                        placeholder = { Text("What's on your mind?") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (postCaption.isNotBlank()) {
                            onAddPost(postCaption.trim())
                            Toast.makeText(context, "Post added to your profile!", Toast.LENGTH_SHORT).show()
                            showAddPostDialog = false
                        }
                    },
                    enabled = postCaption.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // QR Code Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = { Text("My Profile QR Code", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "@${userSettings.myUsername}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Scan to chat with me on Bluemoon",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString("https://t.me/${userSettings.myUsername}"))
                        Toast.makeText(context, "Profile link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showQrDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("action_${label.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileTabPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val pillBg = if (isSelected) TelegramBlue.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isSelected) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = pillBg,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
    }
}
