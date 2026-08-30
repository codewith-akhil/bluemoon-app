package com.example.telegram.data.repository

import com.example.telegram.data.db.AppDatabase
import com.example.telegram.data.db.CallEntity
import com.example.telegram.data.db.ChatEntity
import com.example.telegram.data.db.ContactEntity
import com.example.telegram.data.db.MessageEntity
import com.example.telegram.data.db.StoryEntity
import com.example.telegram.data.models.CallDirection
import com.example.telegram.data.models.CallType
import com.example.telegram.data.models.ChatType
import com.example.telegram.data.models.MessageStatus
import com.example.telegram.data.models.MessageType
import com.example.telegram.data.models.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class TelegramRepository(private val database: AppDatabase) {
    private val dao = database.telegramDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings = _userSettings.asStateFlow()

    init {
        scope.launch {
            checkAndSeedData()
        }
    }

    val allChats: Flow<List<ChatEntity>> = dao.getAllChats()
    val allContacts: Flow<List<ContactEntity>> = dao.getAllContacts()
    val allCalls: Flow<List<CallEntity>> = dao.getAllCalls()
    val allStories: Flow<List<StoryEntity>> = dao.getAllStories()

    fun getChatById(chatId: String): Flow<ChatEntity?> = dao.getChatById(chatId)
    fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>> = dao.getMessagesForChat(chatId)

    suspend fun sendMessage(
        chatId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String = "",
        mediaDurationSec: Int = 0,
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSender: String? = null,
        pollQuestion: String = "",
        pollOptionsJson: String = ""
    ) {
        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            id = messageId,
            chatId = chatId,
            senderId = "me",
            senderName = _userSettings.value.myName,
            text = text,
            timestamp = now,
            isOutgoing = true,
            status = MessageStatus.READ,
            type = type,
            mediaUrl = mediaUrl,
            mediaDurationSec = mediaDurationSec,
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
            replyToSender = replyToSender,
            pollQuestion = pollQuestion,
            pollOptionsJson = pollOptionsJson
        )
        dao.insertMessage(message)

        val preview = when (type) {
            MessageType.VOICE -> "🎤 Voice message (${mediaDurationSec}s)"
            MessageType.PHOTO -> "📷 Photo"
            MessageType.POLL -> "📊 Poll: $pollQuestion"
            MessageType.VIDEO_NOTE -> "📹 Video message"
            MessageType.FILE -> "📎 Document"
            MessageType.LOCATION -> "📍 Location"
            else -> text
        }

        val currentChat = dao.getChatById(chatId).first()
        if (currentChat != null) {
            dao.updateChat(
                currentChat.copy(
                    lastMessage = preview,
                    lastMessageTime = now
                )
            )
        }
    }

    suspend fun deleteMessage(messageId: String) {
        dao.deleteMessage(messageId)
    }

    suspend fun togglePinChat(chatId: String, currentPin: Boolean) {
        dao.setChatPinned(chatId, !currentPin)
    }

    suspend fun toggleMuteChat(chatId: String, currentMute: Boolean) {
        dao.setChatMuted(chatId, !currentMute)
    }

    suspend fun deleteChat(chatId: String) {
        dao.deleteChat(chatId)
        dao.clearMessagesForChat(chatId)
    }

    suspend fun createNewChat(
        title: String,
        username: String,
        type: ChatType,
        avatarColor: String = "#2481CC",
        initialMessage: String = "Chat created"
    ): String {
        val chatId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val chat = ChatEntity(
            id = chatId,
            title = title,
            username = username,
            type = type,
            avatarColorHex = avatarColor,
            lastMessage = initialMessage,
            lastMessageTime = now,
            memberCount = if (type == ChatType.GROUP) 2 else if (type == ChatType.CHANNEL) 100 else 1
        )
        dao.insertChat(chat)

        val welcomeMsg = MessageEntity(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            senderId = "system",
            senderName = "Telegram",
            text = initialMessage,
            timestamp = now,
            isOutgoing = false,
            status = MessageStatus.READ,
            type = MessageType.TEXT
        )
        dao.insertMessage(welcomeMsg)
        return chatId
    }

    suspend fun markStorySeen(storyId: String) {
        dao.markStorySeen(storyId)
    }

    suspend fun logCall(
        userId: String,
        userName: String,
        avatarColor: String,
        type: CallType,
        direction: CallDirection,
        durationSec: Int = 0
    ) {
        val call = CallEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            userName = userName,
            userAvatarColor = avatarColor,
            callType = type,
            direction = direction,
            timestamp = System.currentTimeMillis(),
            durationSec = durationSec
        )
        dao.insertCall(call)
    }

    suspend fun deleteCall(callId: String) {
        dao.deleteCall(callId)
    }

    fun updateSettings(updated: UserSettings) {
        _userSettings.value = updated
    }

    suspend fun toggleReaction(message: MessageEntity, emoji: String) {
        // Simple reaction toggle logic
        val reactions = if (message.reactionsJson.isEmpty()) {
            "$emoji:1:true"
        } else {
            val parts = message.reactionsJson.split(",").filter { it.isNotEmpty() }.toMutableList()
            var found = false
            for (i in parts.indices) {
                val sub = parts[i].split(":")
                if (sub.isNotEmpty() && sub[0] == emoji) {
                    val count = sub.getOrNull(1)?.toIntOrNull() ?: 1
                    val userReacted = sub.getOrNull(2)?.toBoolean() ?: false
                    if (userReacted) {
                        if (count > 1) {
                            parts[i] = "$emoji:${count - 1}:false"
                        } else {
                            parts.removeAt(i)
                        }
                    } else {
                        parts[i] = "$emoji:${count + 1}:true"
                    }
                    found = true
                    break
                }
            }
            if (!found) {
                parts.add("$emoji:1:true")
            }
            parts.joinToString(",")
        }
        dao.updateMessage(message.copy(reactionsJson = reactions))
    }

    suspend fun votePoll(message: MessageEntity, optionId: Int) {
        if (message.pollOptionsJson.isEmpty()) return
        val options = message.pollOptionsJson.split(";").map { optStr ->
            val p = optStr.split(":")
            val id = p.getOrNull(0)?.toIntOrNull() ?: 0
            val text = p.getOrNull(1) ?: ""
            var votes = p.getOrNull(2)?.toIntOrNull() ?: 0
            var voted = p.getOrNull(3)?.toBoolean() ?: false
            if (id == optionId) {
                if (!voted) {
                    votes += 1
                    voted = true
                }
            }
            "$id:$text:$votes:$voted"
        }.joinToString(";")
        dao.updateMessage(message.copy(pollOptionsJson = options))
    }

    private suspend fun checkAndSeedData() {
        val existingChats = dao.getAllChats().first()
        if (existingChats.isNotEmpty()) return

        val now = System.currentTimeMillis()

        // 1. Saved Messages
        val savedChat = ChatEntity(
            id = "saved_messages",
            title = "Saved Messages",
            username = "saved",
            type = ChatType.SAVED_MESSAGES,
            avatarColorHex = "#2481CC",
            lastMessage = "Pinned: Deployment release checklist & notes",
            lastMessageTime = now - 1000 * 60 * 5,
            isPinned = true
        )

        // 2. Official Bluemoon News Channel
        val telegramNewsChat = ChatEntity(
            id = "bluemoon_news",
            title = "Bluemoon News",
            username = "bluemoon",
            type = ChatType.CHANNEL,
            avatarColorHex = "#229ED9",
            lastMessage = "🚀 Bluemoon 11.5: Introducing Stories for Channels, Boost perks, and enhanced Mini Apps!",
            lastMessageTime = now - 1000 * 60 * 30,
            unreadCount = 2,
            isPinned = true,
            isVerified = true,
            memberCount = 8420000
        )

        // 3. Pavel Durov
        val pavelChat = ChatEntity(
            id = "durov",
            title = "Pavel Durov",
            username = "durov",
            type = ChatType.DIRECT,
            avatarColorHex = "#1D72B8",
            lastMessage = "Privacy is not for sale, and human rights should not be compromised.",
            lastMessageTime = now - 1000 * 60 * 120,
            isOnline = true,
            isVerified = true
        )

        // 4. Secret Chat with Alice
        val secretChat = ChatEntity(
            id = "secret_alice",
            title = "Alice Vance",
            username = "alice_vance",
            type = ChatType.SECRET,
            avatarColorHex = "#00C853",
            lastMessage = "🔒 End-to-end encrypted secret chat established.",
            lastMessageTime = now - 1000 * 60 * 60 * 4,
            isOnline = true
        )

        // 5. Android & Compose Developers Group
        val composeDevsGroup = ChatEntity(
            id = "compose_devs",
            title = "Android & Compose Global",
            username = "compose_global",
            type = ChatType.GROUP,
            avatarColorHex = "#3DDC84",
            lastMessage = "Alex: Which architecture do you prefer for large scale apps?",
            lastMessageTime = now - 1000 * 60 * 15,
            unreadCount = 5,
            memberCount = 14250
        )

        // 6. Elena Rostova
        val elenaChat = ChatEntity(
            id = "elena_r",
            title = "Elena Rostova",
            username = "elena_r",
            type = ChatType.DIRECT,
            avatarColorHex = "#E91E63",
            lastMessage = "🎤 Voice message (14s)",
            lastMessageTime = now - 1000 * 60 * 45,
            isOnline = true
        )

        // 7. Tech Wire Daily
        val techWire = ChatEntity(
            id = "tech_wire",
            title = "TechWire Daily",
            username = "techwire",
            type = ChatType.CHANNEL,
            avatarColorHex = "#FF9800",
            lastMessage = "Quantum computing milestones announced in latest physics report ⚛️",
            lastMessageTime = now - 1000 * 60 * 60 * 8,
            unreadCount = 1,
            memberCount = 420000
        )

        dao.insertChats(listOf(savedChat, telegramNewsChat, pavelChat, secretChat, composeDevsGroup, elenaChat, techWire))

        // Seed Messages for each
        val messages = mutableListOf<MessageEntity>()

        // Saved Messages seed
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "saved_messages",
                senderId = "me",
                senderName = "Me",
                text = "Key Project Goals:\n1. Zero latency message synchronization\n2. Gorgeous Telegram Material 3 layout\n3. Built-in voice message waveform player\n4. Stories carousel and full viewer",
                timestamp = now - 1000 * 60 * 60 * 2,
                isOutgoing = true,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
                isPinned = true
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "saved_messages",
                senderId = "me",
                senderName = "Me",
                text = "Voice note: Meeting recap with engineering leads",
                timestamp = now - 1000 * 60 * 5,
                isOutgoing = true,
                status = MessageStatus.READ,
                type = MessageType.VOICE,
                mediaDurationSec = 32
            )
        )

        // Bluemoon News seed
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "bluemoon_news",
                senderId = "bluemoon",
                senderName = "Bluemoon News",
                text = "📢 Major Update: Bluemoon brings ultra-smooth 120 FPS animations, enhanced voice transcriptions, and interactive story reactions to millions around the world!",
                timestamp = now - 1000 * 60 * 60 * 5,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
                reactionsJson = "🔥:1420:false,❤️:890:true,🚀:2300:false"
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "bluemoon_news",
                senderId = "bluemoon",
                senderName = "Bluemoon News",
                text = "🚀 Bluemoon 11.5: Introducing Stories for Channels, Boost perks, and enhanced Mini Apps!",
                timestamp = now - 1000 * 60 * 30,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
                reactionsJson = "👏:450:false,🔥:3100:false"
            )
        )

        // Durov chat seed
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "durov",
                senderId = "durov",
                senderName = "Pavel Durov",
                text = "Hey there! How is the new Telegram Android build running?",
                timestamp = now - 1000 * 60 * 180,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "durov",
                senderId = "me",
                senderName = "Me",
                text = "It's running super smooth with Jetpack Compose and Room offline persistence! 🚀",
                timestamp = now - 1000 * 60 * 150,
                isOutgoing = true,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
                replyToSender = "Pavel Durov",
                replyToText = "Hey there! How is the new Telegram Android build running?"
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "durov",
                senderId = "durov",
                senderName = "Pavel Durov",
                text = "Privacy is not for sale, and human rights should not be compromised.",
                timestamp = now - 1000 * 60 * 120,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
                reactionsJson = "❤️:4:true,🔥:12:false"
            )
        )

        // Secret chat seed
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "secret_alice",
                senderId = "system",
                senderName = "Telegram",
                text = "🔒 Alice Vance invited you to a secret chat.\n• End-to-end encryption\n• Leaves no trace on our servers\n• Has a self-destruct timer\n• Forwarding is disabled",
                timestamp = now - 1000 * 60 * 60 * 4,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "secret_alice",
                senderId = "alice_vance",
                senderName = "Alice Vance",
                text = "Here are the private cryptographic keys for the new cluster 🔐",
                timestamp = now - 1000 * 60 * 60 * 3,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT
            )
        )

        // Compose Devs Group
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "compose_devs",
                senderId = "marcus_k",
                senderName = "Marcus Kotlin",
                text = "Welcome to the global Android developers community! Check our pinned guidelines.",
                timestamp = now - 1000 * 60 * 60 * 10,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT,
                isPinned = true
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "compose_devs",
                senderId = "alex_c",
                senderName = "Alex Chen",
                text = "Community Poll:",
                timestamp = now - 1000 * 60 * 15,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.POLL,
                pollQuestion = "Which Kotlin UI framework do you use the most in 2025?",
                pollOptionsJson = "1:Jetpack Compose:245:true;2:Compose Multiplatform:120:false;3:XML Views:18:false"
            )
        )

        // Elena Rostova
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "elena_r",
                senderId = "elena_r",
                senderName = "Elena Rostova",
                text = "Listen to this audio memo from the design review today:",
                timestamp = now - 1000 * 60 * 50,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.TEXT
            )
        )
        messages.add(
            MessageEntity(
                id = UUID.randomUUID().toString(),
                chatId = "elena_r",
                senderId = "elena_r",
                senderName = "Elena Rostova",
                text = "Voice message",
                timestamp = now - 1000 * 60 * 45,
                isOutgoing = false,
                status = MessageStatus.READ,
                type = MessageType.VOICE,
                mediaDurationSec = 14
            )
        )

        dao.insertMessages(messages)

        // Seed Contacts
        val contacts = listOf(
            ContactEntity("durov", "Pavel Durov", "durov", "+971 50 123 4567", "Telegram Founder & CEO", "#1D72B8", true, "online"),
            ContactEntity("alice_vance", "Alice Vance", "alice_vance", "+1 (555) 392-1084", "Security Researcher", "#00C853", true, "online"),
            ContactEntity("elena_r", "Elena Rostova", "elena_r", "+44 7700 900077", "UX & Motion Designer", "#E91E63", true, "online"),
            ContactEntity("alex_c", "Alex Chen", "alexchen_dev", "+1 (555) 987-6543", "Android Architecture Enthusiast", "#9C27B0", false, "last seen 25m ago"),
            ContactEntity("sarah_connor", "Sarah Connor", "sarah_c", "+1 (555) 234-5678", "AI Safety & Systems", "#FF5722", false, "last seen 2h ago"),
            ContactEntity("viktor_n", "Viktor Novak", "viktorn", "+49 151 23456789", "Distributed Systems", "#3F51B5", false, "last seen yesterday")
        )
        dao.insertContacts(contacts)

        // Seed Calls
        val calls = listOf(
            CallEntity(UUID.randomUUID().toString(), "durov", "Pavel Durov", "#1D72B8", CallType.VOICE, CallDirection.INCOMING, now - 1000 * 60 * 60 * 12, 184),
            CallEntity(UUID.randomUUID().toString(), "elena_r", "Elena Rostova", "#E91E63", CallType.VIDEO, CallDirection.OUTGOING, now - 1000 * 60 * 60 * 24, 420),
            CallEntity(UUID.randomUUID().toString(), "alex_c", "Alex Chen", "#9C27B0", CallType.VOICE, CallDirection.MISSED, now - 1000 * 60 * 60 * 48, 0)
        )
        dao.insertCalls(calls)

        // Seed Stories
        val stories = listOf(
            StoryEntity("story_1", "durov", "Pavel", "#1D72B8", "", "Building Telegram features in Dubai ☀️", now - 1000 * 60 * 45, false),
            StoryEntity("story_2", "elena_r", "Elena", "#E91E63", "", "Beautiful sunset in Zurich! 🏔️✨", now - 1000 * 60 * 90, false),
            StoryEntity("story_3", "alice_vance", "Alice", "#00C853", "", "New cryptographic verification algorithm released! 🚀", now - 1000 * 60 * 180, true)
        )
        dao.insertStories(stories)
    }
}
