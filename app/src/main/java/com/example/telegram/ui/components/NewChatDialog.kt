package com.example.telegram.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.data.models.ChatType
import com.example.telegram.ui.theme.SecretChatGreen
import com.example.telegram.ui.theme.TelegramBlue

@Composable
fun NewChatDialog(
    initialType: String,
    onDismiss: () -> Unit,
    onCreate: (title: String, username: String, type: ChatType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var selectedType by remember {
        mutableStateOf(
            when (initialType) {
                "SECRET" -> ChatType.SECRET
                "CHANNEL" -> ChatType.CHANNEL
                "GROUP" -> ChatType.GROUP
                else -> ChatType.DIRECT
            }
        )
    }

    val (dialogTitle, icon, tintColor) = when (selectedType) {
        ChatType.SECRET -> Triple("New Secret Chat", Icons.Default.Lock, SecretChatGreen)
        ChatType.CHANNEL -> Triple("New Channel", Icons.Default.RecordVoiceOver, TelegramBlue)
        ChatType.GROUP -> Triple("New Group", Icons.Default.Group, TelegramBlue)
        else -> Triple("New Chat", Icons.Default.Group, TelegramBlue)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tintColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            when (selectedType) {
                                ChatType.CHANNEL -> "Channel Name"
                                ChatType.GROUP -> "Group Name"
                                else -> "Contact Name"
                            }
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Handle (optional)") },
                    prefix = { Text("@") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_username_input")
                )

                if (selectedType == ChatType.SECRET) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 End-to-end encrypted with zero server logs.",
                        fontSize = 12.sp,
                        color = SecretChatGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title.trim(), username.trim(), selectedType)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = tintColor),
                modifier = Modifier.testTag("create_chat_confirm_btn")
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
