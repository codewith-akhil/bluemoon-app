package com.example.telegram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.telegram.data.models.ChatType
import com.example.telegram.data.models.MessageType
import com.example.telegram.ui.components.CreateChatPromptDialog
import com.example.telegram.ui.components.StartNewChatMenu
import com.example.telegram.ui.components.TelegramDrawer
import com.example.telegram.ui.screens.ActiveCallOverlay
import com.example.telegram.ui.screens.CallsScreen
import com.example.telegram.ui.screens.ChatListScreen
import com.example.telegram.ui.screens.ChatScreen
import com.example.telegram.ui.screens.ContactsScreen
import com.example.telegram.ui.screens.SettingsScreen
import com.example.telegram.ui.screens.StoryViewerScreen
import com.example.telegram.ui.screens.auth.CountryPickerScreen
import com.example.telegram.ui.screens.auth.ErrorStateScreen
import com.example.telegram.ui.screens.auth.LandingScreen
import com.example.telegram.ui.screens.auth.LoadingStateScreen
import com.example.telegram.ui.screens.auth.OtpVerificationScreen
import com.example.telegram.ui.screens.auth.PhoneAuthScreen
import com.example.telegram.ui.screens.auth.SuccessStateScreen
import com.example.telegram.ui.theme.TelegramTheme
import com.example.telegram.ui.viewmodels.AuthScreen
import com.example.telegram.ui.viewmodels.TelegramViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: TelegramViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userSettings by viewModel.userSettings.collectAsState()

            TelegramTheme(darkTheme = userSettings.isDarkMode) {
                TelegramApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TelegramApp(viewModel: TelegramViewModel) {
    val userSettings by viewModel.userSettings.collectAsState()
    val authScreen by viewModel.authScreen.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val inputPhoneNumber by viewModel.inputPhoneNumber.collectAsState()
    val inputOtpCode by viewModel.inputOtpCode.collectAsState()
    val isOtpError by viewModel.isOtpError.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val authErrorTitle by viewModel.authErrorTitle.collectAsState()
    val authLoadingMessage by viewModel.authLoadingMessage.collectAsState()
    val authLoadingSubMessage by viewModel.authLoadingSubMessage.collectAsState()

    // If user is not authenticated and not in MAIN_APP screen, display Authentication flow
    if (!userSettings.isLoggedIn && authScreen != AuthScreen.MAIN_APP) {
        // Auth Back Handler
        BackHandler(enabled = authScreen != AuthScreen.LANDING) {
            when (authScreen) {
                AuthScreen.COUNTRY_PICKER -> viewModel.navigateToAuth(AuthScreen.PHONE_INPUT)
                AuthScreen.PHONE_INPUT -> viewModel.navigateToAuth(AuthScreen.LANDING)
                AuthScreen.OTP_VERIFY -> viewModel.navigateToAuth(AuthScreen.PHONE_INPUT)
                AuthScreen.ERROR -> viewModel.retryAuth()
                AuthScreen.LOADING -> viewModel.navigateToAuth(AuthScreen.PHONE_INPUT)
                AuthScreen.SUCCESS -> viewModel.navigateToAuth(AuthScreen.OTP_VERIFY)
                else -> viewModel.navigateToAuth(AuthScreen.LANDING)
            }
        }

        when (authScreen) {
            AuthScreen.LANDING -> {
                LandingScreen(
                    isDarkMode = userSettings.isDarkMode,
                    onToggleNightMode = { viewModel.toggleDarkMode() },
                    onStartMessaging = { viewModel.navigateToAuth(AuthScreen.PHONE_INPUT) },
                    onSkipToChats = { viewModel.skipToChats() }
                )
            }

            AuthScreen.PHONE_INPUT -> {
                PhoneAuthScreen(
                    selectedCountry = selectedCountry,
                    phoneNumber = inputPhoneNumber,
                    onPhoneNumberChange = { viewModel.setInputPhoneNumber(it) },
                    onOpenCountryPicker = { viewModel.navigateToAuth(AuthScreen.COUNTRY_PICKER) },
                    onSubmitPhoneNumber = { viewModel.submitPhoneNumber() },
                    onBackToLanding = { viewModel.navigateToAuth(AuthScreen.LANDING) },
                    isDarkMode = userSettings.isDarkMode,
                    onToggleNightMode = { viewModel.toggleDarkMode() }
                )
            }

            AuthScreen.COUNTRY_PICKER -> {
                CountryPickerScreen(
                    onCountrySelected = { country ->
                        viewModel.setSelectedCountry(country)
                    },
                    onBack = {
                        viewModel.navigateToAuth(AuthScreen.PHONE_INPUT)
                    }
                )
            }

            AuthScreen.OTP_VERIFY -> {
                OtpVerificationScreen(
                    phoneNumber = inputPhoneNumber,
                    dialCode = selectedCountry.dialCode,
                    otpCode = inputOtpCode,
                    onOtpChange = { viewModel.setInputOtpCode(it) },
                    onVerifyOtp = { code -> viewModel.verifyOtp(code) },
                    onResendCode = { viewModel.resendOtp() },
                    onBack = { viewModel.navigateToAuth(AuthScreen.PHONE_INPUT) },
                    hasError = isOtpError,
                    errorMessage = authErrorMessage
                )
            }

            AuthScreen.LOADING -> {
                LoadingStateScreen(
                    message = authLoadingMessage,
                    subMessage = authLoadingSubMessage,
                    onCancel = { viewModel.navigateToAuth(AuthScreen.PHONE_INPUT) }
                )
            }

            AuthScreen.ERROR -> {
                ErrorStateScreen(
                    title = authErrorTitle,
                    description = authErrorMessage,
                    onRetry = { viewModel.retryAuth() },
                    onChangeNumber = { viewModel.navigateToAuth(AuthScreen.PHONE_INPUT) }
                )
            }

            AuthScreen.SUCCESS -> {
                SuccessStateScreen(
                    phoneNumber = "${selectedCountry.dialCode} $inputPhoneNumber",
                    initialName = userSettings.myName,
                    initialUsername = userSettings.myUsername,
                    onCompleteProfile = { name, username ->
                        viewModel.completeProfileAndLogin(name, username)
                    }
                )
            }

            AuthScreen.MAIN_APP -> { /* Handled below */ }
        }
        return
    }

    // Main App Navigation & Drawer
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("chats") }
    var newChatDialogType by remember { mutableStateOf<String?>(null) }

    val chats by viewModel.filteredChats.collectAsState()
    val stories by viewModel.allStories.collectAsState()
    val contacts by viewModel.allContacts.collectAsState()
    val isSyncingContacts by viewModel.isSyncingContacts.collectAsState()
    val contactSyncMessage by viewModel.contactSyncMessage.collectAsState()
    val calls by viewModel.allCalls.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeChat by viewModel.activeChat.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()
    val activeStory by viewModel.activeStory.collectAsState()
    val activeCall by viewModel.activeCallState.collectAsState()

    // Handle Back Button navigation
    BackHandler(enabled = drawerState.isOpen || activeChat != null || activeStory != null || currentScreen != "chats") {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            activeStory != null -> viewModel.closeStory()
            activeChat != null -> viewModel.closeChat()
            currentScreen != "chats" -> currentScreen = "chats"
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            TelegramDrawer(
                userSettings = userSettings,
                onNavigate = { destination ->
                    if (destination == "saved_messages") {
                        viewModel.openChat("saved_messages")
                    } else {
                        currentScreen = destination
                    }
                },
                onOpenNewChatDialog = { type ->
                    newChatDialogType = type
                },
                onToggleNightMode = {
                    viewModel.toggleDarkMode()
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                onLogOut = {
                    viewModel.logOut()
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // 1. Story Viewer
                activeStory != null -> {
                    StoryViewerScreen(
                        story = activeStory!!,
                        onClose = { viewModel.closeStory() },
                        onSendReply = { reply ->
                            viewModel.sendMessage(
                                text = "Replied to story: $reply",
                                type = MessageType.TEXT
                            )
                        }
                    )
                }

                // 2. Open Chat Conversation
                activeChat != null -> {
                    ChatScreen(
                        chat = activeChat!!,
                        messages = activeChatMessages,
                        isDark = userSettings.isDarkMode,
                        onBack = { viewModel.closeChat() },
                        onSendMessage = { text, replyTo, type, mediaDuration, pollQ, pollOpts ->
                            viewModel.sendMessage(
                                text = text,
                                replyTo = replyTo,
                                type = type,
                                mediaDurationSec = mediaDuration,
                                pollQuestion = pollQ,
                                pollOptions = pollOpts
                            )
                        },
                        onDeleteMessage = { id -> viewModel.deleteMessage(id) },
                        onReactMessage = { msg, emoji -> viewModel.toggleReaction(msg, emoji) },
                        onVotePoll = { msg, optId -> viewModel.votePoll(msg, optId) },
                        onStartVoiceCall = {
                            viewModel.startCall(
                                userId = activeChat!!.id,
                                userName = activeChat!!.title,
                                avatarColor = activeChat!!.avatarColorHex,
                                isVideo = false
                            )
                        },
                        onStartVideoCall = {
                            viewModel.startCall(
                                userId = activeChat!!.id,
                                userName = activeChat!!.title,
                                avatarColor = activeChat!!.avatarColorHex,
                                isVideo = true
                            )
                        }
                    )
                }

                // 3. Main Navigation Destinations
                else -> {
                    when (currentScreen) {
                        "chats" -> {
                            ChatListScreen(
                                chats = chats,
                                contacts = contacts,
                                stories = stories,
                                selectedFolder = selectedFolder,
                                searchQuery = searchQuery,
                                onSelectFolder = { viewModel.setFolder(it) },
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onOpenChat = { chatId -> viewModel.openChat(chatId) },
                                onContactClick = { contact -> viewModel.openOrCreateChatForContact(contact) },
                                onOpenStory = { story -> viewModel.openStory(story) },
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenNewChatMenu = { newChatDialogType = "DIRECT" },
                                onTogglePin = { id, pin -> viewModel.togglePinChat(id, pin) },
                                onToggleMute = { id, mute -> viewModel.toggleMuteChat(id, mute) },
                                onDeleteChat = { id -> viewModel.deleteChat(id) }
                            )
                        }
                        "contacts" -> {
                            ContactsScreen(
                                contacts = contacts,
                                isSyncing = isSyncingContacts,
                                syncMessage = contactSyncMessage,
                                onBack = { currentScreen = "chats" },
                                onSyncContacts = { viewModel.syncDeviceContacts(context) },
                                onContactClick = { contact ->
                                    viewModel.openOrCreateChatForContact(contact)
                                },
                                onAddContact = { name, phone, username ->
                                    viewModel.addContact(name, phone, username)
                                }
                            )
                        }
                        "calls" -> {
                            CallsScreen(
                                calls = calls,
                                activeCall = activeCall,
                                onBack = { currentScreen = "chats" },
                                onStartCall = { userId, userName, avatarColor, isVideo ->
                                    viewModel.startCall(userId, userName, avatarColor, isVideo)
                                },
                                onToggleMute = { viewModel.toggleCallMute() },
                                onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                                onEndCall = { viewModel.endCall() },
                                onDeleteCall = { viewModel.deleteCall(it) }
                            )
                        }
                        "settings" -> {
                            SettingsScreen(
                                userSettings = userSettings,
                                onBack = { currentScreen = "chats" },
                                onUpdateSettings = { viewModel.updateUserSettings(it) },
                                onLogOut = { viewModel.logOut() }
                            )
                        }
                    }
                }
            }

            // Global Active Call Floating / Overlay if in chat/other screen
            if (activeCall != null && activeCall!!.isActive && activeStory == null) {
                ActiveCallOverlay(
                    state = activeCall!!,
                    onToggleMute = { viewModel.toggleCallMute() },
                    onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                    onEndCall = { viewModel.endCall() }
                )
            }
        }
    }

    // Start New Chat Menu / Channel / Group / Secret Chat Dialog
    if (newChatDialogType != null) {
        if (newChatDialogType == "MENU" || newChatDialogType == "DIRECT") {
            StartNewChatMenu(
                contacts = contacts,
                isSyncing = isSyncingContacts,
                syncMessage = contactSyncMessage,
                onDismiss = { newChatDialogType = null },
                onSyncContacts = { viewModel.syncDeviceContacts(context) },
                onContactClick = { contact ->
                    viewModel.openOrCreateChatForContact(contact)
                    newChatDialogType = null
                },
                onCreateChat = { title, username, type ->
                    viewModel.createNewChat(title, username, type)
                    newChatDialogType = null
                }
            )
        } else {
            val type = when (newChatDialogType) {
                "SECRET" -> ChatType.SECRET
                "CHANNEL" -> ChatType.CHANNEL
                "GROUP" -> ChatType.GROUP
                else -> ChatType.DIRECT
            }
            CreateChatPromptDialog(
                type = type,
                onDismiss = { newChatDialogType = null },
                onCreate = { title, username, createdType ->
                    viewModel.createNewChat(title, username, createdType)
                    newChatDialogType = null
                }
            )
        }
    }
}
