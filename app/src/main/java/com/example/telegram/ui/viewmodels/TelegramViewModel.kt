package com.example.telegram.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegram.data.db.AppDatabase
import com.example.telegram.data.db.CallEntity
import com.example.telegram.data.db.ChatEntity
import com.example.telegram.data.db.ContactEntity
import com.example.telegram.data.db.MessageEntity
import com.example.telegram.data.db.StoryEntity
import com.example.telegram.data.models.CallDirection
import com.example.telegram.data.models.CallType
import com.example.telegram.data.models.ChatType
import com.example.telegram.data.models.Country
import com.example.telegram.data.models.CountryData
import com.example.telegram.data.models.MessageType
import com.example.telegram.data.models.UserSettings
import com.example.telegram.data.repository.TelegramRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthScreen {
    LANDING,
    PHONE_INPUT,
    COUNTRY_PICKER,
    OTP_VERIFY,
    LOADING,
    ERROR,
    SUCCESS,
    MAIN_APP
}

enum class ChatFolder {
    ALL,
    PERSONAL,
    CHANNELS,
    GROUPS,
    SECRET
}

data class ActiveCallState(
    val isActive: Boolean = false,
    val userId: String = "",
    val userName: String = "",
    val avatarColorHex: String = "#2481CC",
    val isVideo: Boolean = false,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isConnecting: Boolean = true
)

class TelegramViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TelegramRepository(database)

    val userSettings: StateFlow<UserSettings> = repository.userSettings

    // Authentication States
    private val _authScreen = MutableStateFlow(AuthScreen.LANDING)
    val authScreen = _authScreen.asStateFlow()

    private val _selectedCountry = MutableStateFlow(CountryData.defaultCountry)
    val selectedCountry = _selectedCountry.asStateFlow()

    private val _inputPhoneNumber = MutableStateFlow("")
    val inputPhoneNumber = _inputPhoneNumber.asStateFlow()

    private val _inputOtpCode = MutableStateFlow("")
    val inputOtpCode = _inputOtpCode.asStateFlow()

    private val _authLoadingMessage = MutableStateFlow("Connecting to Telegram...")
    val authLoadingMessage = _authLoadingMessage.asStateFlow()

    private val _authLoadingSubMessage = MutableStateFlow("Please wait while we establish secure connection")
    val authLoadingSubMessage = _authLoadingSubMessage.asStateFlow()

    private val _authErrorTitle = MutableStateFlow("Verification Failed")
    val authErrorTitle = _authErrorTitle.asStateFlow()

    private val _authErrorMessage = MutableStateFlow("The verification code entered is incorrect.")
    val authErrorMessage = _authErrorMessage.asStateFlow()

    private val _isOtpError = MutableStateFlow(false)
    val isOtpError = _isOtpError.asStateFlow()

    private val _selectedFolder = MutableStateFlow(ChatFolder.ALL)
    val selectedFolder = _selectedFolder.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    private val _activeStory = MutableStateFlow<StoryEntity?>(null)
    val activeStory = _activeStory.asStateFlow()

    private val _activeCallState = MutableStateFlow<ActiveCallState?>(null)
    val activeCallState = _activeCallState.asStateFlow()

    private var callTimerJob: Job? = null

    val rawChats: StateFlow<List<ChatEntity>> = repository.allChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredChats: StateFlow<List<ChatEntity>> = combine(
        repository.allChats,
        _selectedFolder,
        _searchQuery
    ) { chats, folder, query ->
        chats.filter { chat ->
            val matchesFolder = when (folder) {
                ChatFolder.ALL -> true
                ChatFolder.PERSONAL -> chat.type == ChatType.DIRECT || chat.type == ChatType.SAVED_MESSAGES
                ChatFolder.CHANNELS -> chat.type == ChatType.CHANNEL
                ChatFolder.GROUPS -> chat.type == ChatType.GROUP
                ChatFolder.SECRET -> chat.type == ChatType.SECRET
            }
            val matchesQuery = query.isEmpty() ||
                    chat.title.contains(query, ignoreCase = true) ||
                    chat.username.contains(query, ignoreCase = true) ||
                    chat.lastMessage.contains(query, ignoreCase = true)

            matchesFolder && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCalls: StateFlow<List<CallEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStories: StateFlow<List<StoryEntity>> = repository.allStories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChat: StateFlow<ChatEntity?> = _activeChatId.flatMapLatest { id ->
        if (id != null) repository.getChatById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeChatMessages: StateFlow<List<MessageEntity>> = _activeChatId.flatMapLatest { id ->
        if (id != null) repository.getMessagesForChat(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFolder(folder: ChatFolder) {
        _selectedFolder.value = folder
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openChat(chatId: String) {
        _activeChatId.value = chatId
    }

    fun closeChat() {
        _activeChatId.value = null
    }

    fun openStory(story: StoryEntity) {
        _activeStory.value = story
        viewModelScope.launch {
            repository.markStorySeen(story.id)
        }
    }

    fun closeStory() {
        _activeStory.value = null
    }

    fun sendMessage(
        text: String,
        replyTo: MessageEntity? = null,
        type: MessageType = MessageType.TEXT,
        mediaDurationSec: Int = 0,
        pollQuestion: String = "",
        pollOptions: List<String> = emptyList()
    ) {
        val chatId = _activeChatId.value ?: return
        if (text.isBlank() && type == MessageType.TEXT) return

        viewModelScope.launch {
            val pollOptionsJson = if (type == MessageType.POLL && pollOptions.isNotEmpty()) {
                pollOptions.mapIndexed { idx, opt ->
                    "${idx + 1}:$opt:0:false"
                }.joinToString(";")
            } else ""

            repository.sendMessage(
                chatId = chatId,
                text = text,
                type = type,
                mediaDurationSec = mediaDurationSec,
                replyToMessageId = replyTo?.id,
                replyToText = replyTo?.text,
                replyToSender = replyTo?.senderName,
                pollQuestion = pollQuestion,
                pollOptionsJson = pollOptionsJson
            )

            // Simulate quick reply from contact in direct chats if not saved_messages
            val currentChat = rawChats.value.find { it.id == chatId }
            if (currentChat != null && (currentChat.type == ChatType.DIRECT || currentChat.type == ChatType.SECRET)) {
                simulateAutoResponse(currentChat, text)
            }
        }
    }

    private fun simulateAutoResponse(chat: ChatEntity, userText: String) {
        viewModelScope.launch {
            delay(1800)
            val replyText = when {
                userText.contains("hello", ignoreCase = true) || userText.contains("hi", ignoreCase = true) ->
                    "Hey! Glad to hear from you. Everything is functioning securely."
                userText.contains("crypto", ignoreCase = true) || userText.contains("key", ignoreCase = true) ->
                    "All communications and cryptographic handshakes verified end-to-end 🔒"
                userText.contains("call", ignoreCase = true) ->
                    "Sure, feel free to tap the call button at the top whenever you're ready!"
                else ->
                    "Got it! Message received and synced across all devices 👍"
            }
            repository.sendMessage(
                chatId = chat.id,
                text = replyText,
                type = MessageType.TEXT
            )
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun togglePinChat(chatId: String, currentPin: Boolean) {
        viewModelScope.launch {
            repository.togglePinChat(chatId, currentPin)
        }
    }

    fun toggleMuteChat(chatId: String, currentMute: Boolean) {
        viewModelScope.launch {
            repository.toggleMuteChat(chatId, currentMute)
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
            if (_activeChatId.value == chatId) {
                _activeChatId.value = null
            }
        }
    }

    fun toggleReaction(message: MessageEntity, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(message, emoji)
        }
    }

    fun votePoll(message: MessageEntity, optionId: Int) {
        viewModelScope.launch {
            repository.votePoll(message, optionId)
        }
    }

    fun createNewChat(title: String, username: String, type: ChatType) {
        viewModelScope.launch {
            val colors = listOf("#2481CC", "#E91E63", "#00C853", "#FF9800", "#9C27B0", "#3F51B5")
            val randomColor = colors.random()
            val initialMsg = when (type) {
                ChatType.SECRET -> "🔒 Secret chat created with end-to-end encryption."
                ChatType.CHANNEL -> "📢 Channel created. Broadcast messages here."
                ChatType.GROUP -> "👥 Group created. Invite members to collaborate."
                else -> "Chat started"
            }
            val id = repository.createNewChat(title, username, type, randomColor, initialMsg)
            _activeChatId.value = id
        }
    }

    fun openOrCreateChatForContact(contact: ContactEntity) {
        viewModelScope.launch {
            val existingChat = rawChats.value.find { it.id == contact.id || (contact.username.isNotEmpty() && it.username == contact.username) }
            if (existingChat != null) {
                _activeChatId.value = existingChat.id
            } else {
                val newId = repository.createNewChat(
                    title = contact.name,
                    username = contact.username,
                    type = ChatType.DIRECT,
                    avatarColor = contact.avatarColorHex,
                    initialMessage = "Hey there! Let's chat."
                )
                _activeChatId.value = newId
            }
        }
    }

    fun startCall(userId: String, userName: String, avatarColor: String, isVideo: Boolean) {
        _activeCallState.value = ActiveCallState(
            isActive = true,
            userId = userId,
            userName = userName,
            avatarColorHex = avatarColor,
            isVideo = isVideo,
            durationSeconds = 0,
            isConnecting = true
        )
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            delay(2000)
            _activeCallState.value = _activeCallState.value?.copy(isConnecting = false)
            while (true) {
                delay(1000)
                _activeCallState.value = _activeCallState.value?.let { current ->
                    current.copy(durationSeconds = current.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleCallMute() {
        _activeCallState.value = _activeCallState.value?.let { current ->
            current.copy(isMuted = !current.isMuted)
        }
    }

    fun toggleCallSpeaker() {
        _activeCallState.value = _activeCallState.value?.let { current ->
            current.copy(isSpeakerOn = !current.isSpeakerOn)
        }
    }

    fun endCall() {
        val current = _activeCallState.value
        callTimerJob?.cancel()
        callTimerJob = null
        if (current != null && current.isActive) {
            viewModelScope.launch {
                repository.logCall(
                    userId = current.userId,
                    userName = current.userName,
                    avatarColor = current.avatarColorHex,
                    type = if (current.isVideo) CallType.VIDEO else CallType.VOICE,
                    direction = CallDirection.OUTGOING,
                    durationSec = current.durationSeconds
                )
            }
        }
        _activeCallState.value = null
    }

    fun deleteCall(callId: String) {
        viewModelScope.launch {
            repository.deleteCall(callId)
        }
    }

    fun updateUserSettings(settings: UserSettings) {
        repository.updateSettings(settings)
    }

    fun toggleDarkMode() {
        val current = userSettings.value
        updateUserSettings(current.copy(isDarkMode = !current.isDarkMode))
    }

    // --- Authentication Flow Methods ---

    fun navigateToAuth(screen: AuthScreen) {
        _authScreen.value = screen
    }

    fun setSelectedCountry(country: Country) {
        _selectedCountry.value = country
        _authScreen.value = AuthScreen.PHONE_INPUT
    }

    fun setInputPhoneNumber(phone: String) {
        _inputPhoneNumber.value = phone
    }

    fun setInputOtpCode(code: String) {
        _inputOtpCode.value = code
        if (_isOtpError.value) {
            _isOtpError.value = false
            _authErrorMessage.value = ""
        }
    }

    fun submitPhoneNumber() {
        val phone = _inputPhoneNumber.value.trim()
        if (phone.length < 4) return

        viewModelScope.launch {
            _authLoadingMessage.value = "Sending SMS Code..."
            _authLoadingSubMessage.value = "Sending 6-digit verification code to ${_selectedCountry.value.dialCode} $phone"
            _authScreen.value = AuthScreen.LOADING

            delay(1200) // Simulating network handshake

            _inputOtpCode.value = ""
            _isOtpError.value = false
            _authScreen.value = AuthScreen.OTP_VERIFY
        }
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _authLoadingMessage.value = "Verifying Code..."
            _authLoadingSubMessage.value = "Checking cryptographic keys and cloud credentials"
            _authScreen.value = AuthScreen.LOADING

            delay(1400)

            // Valid code (123456 or standard code not equal to 999999)
            if (code == "123456" || (code.length == 6 && code != "999999")) {
                _isOtpError.value = false
                _authScreen.value = AuthScreen.SUCCESS
            } else {
                // Invalid code error simulation
                _isOtpError.value = true
                _authErrorTitle.value = "Invalid Code"
                _authErrorMessage.value = "The code you entered is invalid. Please try again or request a new code."
                _authScreen.value = AuthScreen.ERROR
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            _authLoadingMessage.value = "Resending Code..."
            _authLoadingSubMessage.value = "Requesting new SMS code via carrier network"
            _authScreen.value = AuthScreen.LOADING

            delay(1000)

            _inputOtpCode.value = ""
            _isOtpError.value = false
            _authScreen.value = AuthScreen.OTP_VERIFY
        }
    }

    fun retryAuth() {
        _inputOtpCode.value = ""
        _isOtpError.value = false
        _authScreen.value = AuthScreen.OTP_VERIFY
    }

    fun completeProfileAndLogin(name: String, username: String) {
        viewModelScope.launch {
            val phone = "${_selectedCountry.value.dialCode} ${_inputPhoneNumber.value}"
            updateUserSettings(
                userSettings.value.copy(
                    myName = name,
                    myUsername = username,
                    myPhone = if (_inputPhoneNumber.value.isNotEmpty()) phone else userSettings.value.myPhone,
                    isLoggedIn = true
                )
            )
            _authScreen.value = AuthScreen.MAIN_APP
        }
    }

    fun logOut() {
        viewModelScope.launch {
            updateUserSettings(userSettings.value.copy(isLoggedIn = false))
            _authScreen.value = AuthScreen.LANDING
            _inputPhoneNumber.value = ""
            _inputOtpCode.value = ""
            _isOtpError.value = false
            _activeChatId.value = null
        }
    }

    fun skipToChats() {
        updateUserSettings(userSettings.value.copy(isLoggedIn = true))
        _authScreen.value = AuthScreen.MAIN_APP
    }
}
