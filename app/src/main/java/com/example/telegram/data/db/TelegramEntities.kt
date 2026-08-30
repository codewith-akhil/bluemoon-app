package com.example.telegram.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.telegram.data.models.CallDirection
import com.example.telegram.data.models.CallType
import com.example.telegram.data.models.ChatType
import com.example.telegram.data.models.MessageStatus
import com.example.telegram.data.models.MessageType

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val username: String = "",
    val type: ChatType = ChatType.DIRECT,
    val avatarColorHex: String = "#2481CC",
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isOnline: Boolean = false,
    val isVerified: Boolean = false,
    val memberCount: Int = 1,
    val draft: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val status: MessageStatus = MessageStatus.READ,
    val type: MessageType = MessageType.TEXT,
    val mediaUrl: String = "",
    val mediaDurationSec: Int = 0,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val isPinned: Boolean = false,
    val reactionsJson: String = "", // list of emoji:count:isUserReacted
    val pollOptionsJson: String = "", // poll options data
    val pollQuestion: String = ""
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String = "",
    val phoneNumber: String = "",
    val bio: String = "",
    val avatarColorHex: String = "#2481CC",
    val isOnline: Boolean = false,
    val lastSeenText: String = "recently",
    val photoUri: String? = null,
    val isDeviceContact: Boolean = false
)

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userAvatarColor: String,
    val callType: CallType,
    val direction: CallDirection,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int = 0
)

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val avatarColorHex: String,
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false
)
