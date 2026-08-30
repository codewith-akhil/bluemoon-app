package com.example.telegram.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.data.db.ChatEntity
import com.example.telegram.data.db.MessageEntity
import com.example.telegram.data.models.ChatType
import com.example.telegram.data.models.MessageType
import com.example.telegram.ui.components.MessageBubble
import com.example.telegram.ui.theme.SecretChatGreen
import com.example.telegram.ui.theme.TelegramBlue
import com.example.telegram.ui.theme.TelegramLightBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chat: ChatEntity,
    messages: List<MessageEntity>,
    isDark: Boolean,
    onBack: () -> Unit,
    onSendMessage: (text: String, replyTo: MessageEntity?, type: MessageType, mediaDuration: Int, pollQuestion: String, pollOptions: List<String>) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onReactMessage: (MessageEntity, String) -> Unit,
    onVotePoll: (MessageEntity, Int) -> Unit,
    onStartVoiceCall: () -> Unit,
    onStartVideoCall: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var showAttachSheet by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showEmojiSheet by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val chatAvatarColor = try {
        Color(android.graphics.Color.parseColor(chat.avatarColorHex))
    } catch (e: Exception) {
        TelegramBlue
    }

    val subtitle = when (chat.type) {
        ChatType.SECRET -> "🔒 end-to-end encrypted"
        ChatType.CHANNEL -> "${chat.memberCount} subscribers"
        ChatType.GROUP -> "${chat.memberCount} members"
        ChatType.SAVED_MESSAGES -> "Your personal cloud"
        else -> if (chat.isOnline) "online" else "last seen recently"
    }

    val pinnedMessage = messages.findLast { it.isPinned }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) MaterialTheme.colorScheme.background else TelegramLightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_screen")
    ) {
        // Chat Top App Bar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { /* Profile info */ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(chatAvatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.title.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (chat.type == ChatType.SECRET) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = SecretChatGreen,
                                    modifier = Modifier.size(14.dp).padding(end = 2.dp)
                                )
                            }
                            Text(
                                text = chat.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = if (chat.isOnline || chat.type == ChatType.SECRET) (if (chat.type == ChatType.SECRET) SecretChatGreen else TelegramBlue)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            actions = {
                if (chat.type == ChatType.DIRECT || chat.type == ChatType.SECRET) {
                    IconButton(
                        onClick = onStartVoiceCall,
                        modifier = Modifier.testTag("chat_voice_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onStartVideoCall,
                        modifier = Modifier.testTag("chat_video_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("chat_more_menu_btn")
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
                        text = { Text("Search in Chat") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Mute Notifications") },
                        onClick = { showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear History") },
                        onClick = { showMenu = false }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Pinned Message Banner
        if (pinnedMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { /* Scroll to pinned */ }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(TelegramBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pinned Message",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TelegramBlue
                    )
                    Text(
                        text = pinnedMessage.text,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = TelegramBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Secret Chat Security Info Header Card
        if (chat.type == ChatType.SECRET && messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SecretChatGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Secret Chat",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SecretChatGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Uses end-to-end encryption\n• Leaves no trace on our servers\n• Has a self-destruct timer\n• Does not allow forwarding",
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("messages_lazy_column")
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(
                    message = message,
                    chatType = chat.type,
                    isDark = isDark,
                    onReply = { replyingToMessage = it },
                    onDelete = onDeleteMessage,
                    onReact = onReactMessage,
                    onVotePoll = onVotePoll
                )
            }
        }

        // Reply preview bar
        AnimatedVisibility(visible = replyingToMessage != null) {
            replyingToMessage?.let { replyMsg ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(32.dp)
                            .background(TelegramBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Replying to ${replyMsg.senderName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelegramBlue
                        )
                        Text(
                            text = replyMsg.text,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { replyingToMessage = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = Color.Gray)
                    }
                }
            }
        }

        // Emoji & Sticker Sheet Bar
        AnimatedVisibility(visible = showEmojiSheet) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Stickers & Emojis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TelegramBlue,
                        modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("😀", "😂", "🚀", "❤️", "🔥", "🎉", "👏", "🐱", "✨", "💯").forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 28.sp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        textInput += emoji
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Chat Input Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji button
                IconButton(
                    onClick = { showEmojiSheet = !showEmojiSheet },
                    modifier = Modifier.testTag("emoji_toggle_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji & Stickers",
                        tint = if (showEmojiSheet) TelegramBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Text Input
                TextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Message") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                )

                // Attachment paperclip
                IconButton(
                    onClick = { showAttachSheet = true },
                    modifier = Modifier.testTag("chat_attach_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach media",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Send or Voice Record Action Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue)
                        .clickable {
                            if (textInput.isNotBlank()) {
                                onSendMessage(
                                    textInput.trim(),
                                    replyingToMessage,
                                    MessageType.TEXT,
                                    0,
                                    "",
                                    emptyList()
                                )
                                textInput = ""
                                replyingToMessage = null
                                showEmojiSheet = false
                            } else {
                                // Simulate Voice Note Recording (tap mic)
                                onSendMessage(
                                    "Voice message",
                                    replyingToMessage,
                                    MessageType.VOICE,
                                    (10..45).random(),
                                    "",
                                    emptyList()
                                )
                                replyingToMessage = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (textInput.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                        contentDescription = if (textInput.isNotBlank()) "Send Message" else "Send Voice Note",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Attachment Bottom Sheet
    if (showAttachSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Share Content",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        title = "Gallery",
                        color = Color(0xFF00B0FF),
                        onClick = {
                            showAttachSheet = false
                            onSendMessage(
                                "Sunset in the mountains",
                                replyingToMessage,
                                MessageType.PHOTO,
                                0,
                                "",
                                emptyList()
                            )
                            replyingToMessage = null
                        }
                    )

                    AttachmentOption(
                        icon = Icons.Default.Poll,
                        title = "Poll",
                        color = Color(0xFFFFB300),
                        onClick = {
                            showAttachSheet = false
                            showPollDialog = true
                        }
                    )

                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        title = "Location",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            showAttachSheet = false
                            onSendMessage(
                                "📍 Live Location: Downtown Metropolis",
                                replyingToMessage,
                                MessageType.LOCATION,
                                0,
                                "",
                                emptyList()
                            )
                            replyingToMessage = null
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Create Poll Dialog
    if (showPollDialog) {
        var pollQ by remember { mutableStateOf("") }
        var opt1 by remember { mutableStateOf("") }
        var opt2 by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPollDialog = false },
            title = { Text("New Poll", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pollQ,
                        onValueChange = { pollQ = it },
                        label = { Text("Ask a question") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = opt1,
                        onValueChange = { opt1 = it },
                        label = { Text("Option 1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = opt2,
                        onValueChange = { opt2 = it },
                        label = { Text("Option 2") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pollQ.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()) {
                            onSendMessage(
                                pollQ,
                                replyingToMessage,
                                MessageType.POLL,
                                0,
                                pollQ,
                                listOf(opt1, opt2)
                            )
                            showPollDialog = false
                            replyingToMessage = null
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = TelegramBlue)
                ) {
                    Text("Create Poll")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPollDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
