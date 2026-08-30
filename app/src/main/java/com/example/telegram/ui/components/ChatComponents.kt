package com.example.telegram.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.data.db.MessageEntity
import com.example.telegram.data.models.ChatType
import com.example.telegram.data.models.MessageType
import com.example.telegram.ui.theme.TelegramBlue
import com.example.telegram.ui.theme.TelegramDarkBubbleOther
import com.example.telegram.ui.theme.TelegramDarkBubbleSelf
import com.example.telegram.ui.theme.TelegramGreenBubble
import com.example.telegram.ui.theme.TelegramTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    chatType: ChatType,
    isDark: Boolean,
    onReply: (MessageEntity) -> Unit,
    onDelete: (String) -> Unit,
    onReact: (MessageEntity, String) -> Unit,
    onVotePoll: (MessageEntity, Int) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var isVoicePlaying by remember { mutableStateOf(false) }

    val isOutgoing = message.isOutgoing

    val bubbleBg = if (isOutgoing) {
        if (isDark) TelegramDarkBubbleSelf else TelegramGreenBubble
    } else {
        if (isDark) TelegramDarkBubbleOther else Color.White
    }

    val textColor = if (isDark) Color.White else Color(0xFF1E242B)
    val timeColor = if (isDark) Color(0xFF8B9FA9) else Color(0xFF707C88)

    val shape = if (isOutgoing) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 4.dp,
            bottomEnd = 16.dp
        )
    }

    val timeString = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    val reactionsList = remember(message.reactionsJson) {
        if (message.reactionsJson.isEmpty()) emptyList()
        else {
            message.reactionsJson.split(",").mapNotNull { part ->
                val p = part.split(":")
                if (p.size >= 3) {
                    Triple(p[0], p[1].toIntOrNull() ?: 0, p[2].toBoolean())
                } else null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("message_item_${message.id}"),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 64.dp, max = 320.dp)
                    .clip(shape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                    },
                shape = shape,
                color = bubbleBg,
                shadowElevation = if (isDark) 0.dp else 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    // Group sender name
                    if (!isOutgoing && chatType == ChatType.GROUP) {
                        Text(
                            text = message.senderName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TelegramBlue,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Reply Quote Banner
                    if (!message.replyToText.isNullOrEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(TelegramBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replyToSender ?: "Reply",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TelegramBlue
                                )
                                Text(
                                    text = message.replyToText,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    color = timeColor
                                )
                            }
                        }
                    }

                    // Message Content Types
                    when (message.type) {
                        MessageType.VOICE -> {
                            VoicePlayerView(
                                durationSec = message.mediaDurationSec.takeIf { it > 0 } ?: 15,
                                isPlaying = isVoicePlaying,
                                isDark = isDark,
                                onTogglePlay = { isVoicePlaying = !isVoicePlaying }
                            )
                        }
                        MessageType.POLL -> {
                            PollView(
                                message = message,
                                isDark = isDark,
                                onVote = { optId -> onVotePoll(message, optId) }
                            )
                        }
                        MessageType.PHOTO -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF334455)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "📷 Telegram High-Res Photo",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (message.text.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = message.text,
                                    fontSize = 15.sp,
                                    color = textColor
                                )
                            }
                        }
                        else -> {
                            // Text Message
                            Text(
                                text = message.text,
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                                color = textColor
                            )
                        }
                    }

                    // Timestamp and Delivery Status
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = timeColor,
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 2.dp)
                            )
                        }
                        Text(
                            text = timeString,
                            fontSize = 11.sp,
                            color = timeColor
                        )
                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = TelegramBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Reactions list under bubble
            if (reactionsList.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    reactionsList.forEach { (emoji, count, isUserReacted) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isUserReacted) TelegramBlue.copy(alpha = 0.2f)
                                    else if (isDark) TelegramDarkBubbleOther else Color(0xFFF1F3F4)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isUserReacted) TelegramBlue else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onReact(message, emoji) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$emoji $count",
                                fontSize = 11.sp,
                                fontWeight = if (isUserReacted) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUserReacted) TelegramBlue else textColor
                            )
                        }
                    }
                }
            }

            // Long Press Actions Dropdown
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                // Quick emoji reaction bar in dropdown
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("👍", "❤️", "🔥", "🎉", "👏", "😮").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onReact(message, emoji)
                                    showMenu = false
                                }
                                .padding(4.dp)
                        )
                    }
                }

                DropdownMenuItem(
                    text = { Text("Reply") },
                    onClick = {
                        onReply(message)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete(message.id)
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun VoicePlayerView(
    durationSec: Int,
    isPlaying: Boolean,
    isDark: Boolean,
    onTogglePlay: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(TelegramBlue)
                .clickable(onClick = onTogglePlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Stop" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Animated Waveform Canvas
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            ) {
                val barWidth = 3.dp.toPx()
                val gap = 2.dp.toPx()
                val totalBars = (size.width / (barWidth + gap)).toInt()

                val waveformHeights = floatArrayOf(
                    0.3f, 0.6f, 0.9f, 0.4f, 0.7f, 0.2f, 0.8f, 0.5f, 0.9f, 0.4f,
                    0.3f, 0.7f, 0.5f, 0.8f, 0.6f, 0.4f, 0.9f, 0.3f, 0.5f, 0.7f
                )

                for (i in 0 until totalBars) {
                    val x = i * (barWidth + gap)
                    val hFactor = waveformHeights[i % waveformHeights.size]
                    val barHeight = size.height * hFactor
                    val y = (size.height - barHeight) / 2

                    val barColor = if (isPlaying) {
                        val currRatio = i.toFloat() / totalBars.toFloat()
                        if (currRatio <= animatedProgress) TelegramBlue
                        else (if (isDark) Color(0xFF7E8C9A) else Color(0xFFB0BEC5))
                    } else {
                        if (isDark) Color(0xFF7E8C9A) else Color(0xFFB0BEC5)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(2.dp.toPx())
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "0:${durationSec.toString().padStart(2, '0')}",
                fontSize = 11.sp,
                color = if (isDark) Color(0xFF8B9FA9) else Color(0xFF707C88)
            )
        }
    }
}

@Composable
fun PollView(
    message: MessageEntity,
    isDark: Boolean,
    onVote: (Int) -> Unit
) {
    val options = remember(message.pollOptionsJson) {
        if (message.pollOptionsJson.isEmpty()) emptyList()
        else {
            message.pollOptionsJson.split(";").mapNotNull { part ->
                val p = part.split(":")
                if (p.size >= 4) {
                    val id = p[0].toIntOrNull() ?: 0
                    val text = p[1]
                    val votes = p[2].toIntOrNull() ?: 0
                    val voted = p[3].toBoolean()
                    id to Triple(text, votes, voted)
                } else null
            }
        }
    }

    val totalVotes = options.sumOf { it.second.second }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Poll,
                contentDescription = null,
                tint = TelegramBlue,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Anonymous Poll",
                fontSize = 12.sp,
                color = TelegramBlue,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = message.pollQuestion.ifEmpty { message.text },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color.White else Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        options.forEach { (id, data) ->
            val (text, votes, voted) = data
            val percentage = if (totalVotes > 0) votes.toFloat() / totalVotes else 0f
            val percentText = (percentage * 100).toInt()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onVote(id) }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (voted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Voted",
                                tint = TelegramBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = text,
                            fontSize = 14.sp,
                            color = if (voted) TelegramBlue else (if (isDark) Color.White else Color.Black),
                            fontWeight = if (voted) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Text(
                        text = "$percentText%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TelegramBlue
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TelegramBlue,
                    trackColor = if (isDark) Color(0xFF242F3D) else Color(0xFFE0E0E0)
                )
            }
        }

        Text(
            text = "$totalVotes votes",
            fontSize = 11.sp,
            color = if (isDark) Color(0xFF8B9FA9) else Color(0xFF707C88),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
