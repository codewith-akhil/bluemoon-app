package com.example.telegram.data.models

enum class ChatType {
    DIRECT,
    SECRET,
    GROUP,
    CHANNEL,
    BOT,
    SAVED_MESSAGES
}

enum class MessageType {
    TEXT,
    PHOTO,
    VOICE,
    VIDEO_NOTE,
    POLL,
    LOCATION,
    FILE
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

data class User(
    val id: String,
    val name: String,
    val username: String,
    val phoneNumber: String,
    val bio: String = "",
    val avatarColorHex: String = "#2481CC",
    val isOnline: Boolean = false,
    val lastSeenText: String = "recently",
    val isVerified: Boolean = false,
    val isPremium: Boolean = false
)

data class PollOptionItem(
    val id: Int,
    val text: String,
    val votes: Int = 0,
    val isUserVoted: Boolean = false
)

data class ReactionItem(
    val emoji: String,
    val count: Int,
    val isUserReacted: Boolean = false
)

data class StoryItem(
    val id: String,
    val userId: String,
    val userName: String,
    val avatarColorHex: String,
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSeen: Boolean = false
)

data class CallLogItem(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatarColor: String,
    val callType: CallType,
    val direction: CallDirection,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSec: Int = 0
)

data class UserSettings(
    val myName: String = "sne...",
    val myUsername: String = "Sneeeeinuk",
    val myPhone: String = "+91 9746109569",
    val myBio: String = "Follow the rules",
    val avatarColorHex: String = "#4CAF50",
    val photoUri: String? = null,
    val isDarkMode: Boolean = false,
    val textSize: Float = 16f,
    val chatWallpaper: String = "Classic Doodle",
    val notificationsEnabled: Boolean = true,
    val secretChatTimerSeconds: Int = 0,
    val isPremiumActive: Boolean = true,
    val passcodeLocked: Boolean = false,
    val isLoggedIn: Boolean = false
)
