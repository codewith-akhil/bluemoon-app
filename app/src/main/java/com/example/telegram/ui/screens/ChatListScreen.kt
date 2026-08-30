package com.example.telegram.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegram.data.db.ChatEntity
import com.example.telegram.data.db.ContactEntity
import com.example.telegram.data.db.StoryEntity
import com.example.telegram.data.models.ChatType
import com.example.telegram.ui.components.ContactAvatar
import com.example.telegram.ui.components.StoryBar
import com.example.telegram.ui.theme.SecretChatGreen
import com.example.telegram.ui.theme.TelegramBlue
import com.example.telegram.ui.viewmodels.ChatFolder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SearchFilterType(val label: String) {
    ALL("All"),
    CHATS("Chats"),
    CONTACTS("Contacts"),
    CHANNELS("Channels"),
    GROUPS("Groups"),
    SECRET("Secret")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chats: List<ChatEntity>,
    contacts: List<ContactEntity> = emptyList(),
    stories: List<StoryEntity> = emptyList(),
    selectedFolder: ChatFolder,
    searchQuery: String,
    onSelectFolder: (ChatFolder) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenChat: (String) -> Unit,
    onContactClick: (ContactEntity) -> Unit = {},
    onOpenStory: (StoryEntity) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenNewChatMenu: () -> Unit,
    onTogglePin: (String, Boolean) -> Unit,
    onToggleMute: (String, Boolean) -> Unit,
    onDeleteChat: (String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var searchFilter by remember { mutableStateOf(SearchFilterType.ALL) }

    // Filter contacts matching the search query
    val matchingContacts = remember(contacts, searchQuery, searchFilter) {
        if (searchFilter == SearchFilterType.CHATS || searchFilter == SearchFilterType.CHANNELS || searchFilter == SearchFilterType.GROUPS || searchFilter == SearchFilterType.SECRET) {
            emptyList()
        } else if (searchQuery.isBlank()) {
            emptyList()
        } else {
            contacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                        contact.username.contains(searchQuery, ignoreCase = true) ||
                        contact.phoneNumber.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Filter chats matching the search query & search filter
    val matchingChats = remember(chats, searchQuery, searchFilter) {
        chats.filter { chat ->
            val matchesTypeFilter = when (searchFilter) {
                SearchFilterType.ALL -> true
                SearchFilterType.CHATS -> chat.type == ChatType.DIRECT || chat.type == ChatType.SAVED_MESSAGES
                SearchFilterType.CONTACTS -> false
                SearchFilterType.CHANNELS -> chat.type == ChatType.CHANNEL
                SearchFilterType.GROUPS -> chat.type == ChatType.GROUP
                SearchFilterType.SECRET -> chat.type == ChatType.SECRET
            }

            val matchesText = searchQuery.isBlank() ||
                    chat.title.contains(searchQuery, ignoreCase = true) ||
                    chat.username.contains(searchQuery, ignoreCase = true) ||
                    chat.lastMessage.contains(searchQuery, ignoreCase = true)

            matchesTypeFilter && matchesText
        }
    }

    val hasSearchResults = matchingContacts.isNotEmpty() || matchingChats.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (isSearchActive) {
                // Active Search Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onSearchQueryChange("")
                                isSearchActive = false
                            },
                            modifier = Modifier.testTag("search_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = {
                                Text(
                                    "Search chats, contacts, channels...",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onSearchQueryChange("") },
                                        modifier = Modifier.testTag("search_clear_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("chat_search_input")
                        )
                    }

                    // Search Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SearchFilterType.values().forEach { filterType ->
                            val isSelected = searchFilter == filterType
                            FilterChip(
                                selected = isSelected,
                                onClick = { searchFilter = filterType },
                                label = {
                                    Text(
                                        text = filterType.label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TelegramBlue.copy(alpha = 0.15f),
                                    selectedLabelColor = TelegramBlue
                                ),
                                modifier = Modifier.testTag("search_chip_${filterType.name}")
                            )
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }

                // Search Results / Suggested Content
                if (searchQuery.isBlank()) {
                    // Empty Search Prompt & Suggested Contacts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_suggestions_view")
                    ) {
                        if (contacts.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Recent & Frequent Contacts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TelegramBlue,
                                    modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp)
                                )
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(contacts.take(8)) { contact ->
                                        SuggestedContactItem(
                                            contact = contact,
                                            onClick = {
                                                onContactClick(contact)
                                                isSearchActive = false
                                            }
                                        )
                                    }
                                }
                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                                )
                            }
                        }

                        item {
                            Text(
                                text = "Recent Chats",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                            )
                        }

                        items(chats.take(6), key = { it.id }) { chat ->
                            ChatItemRow(
                                chat = chat,
                                onClick = {
                                    onOpenChat(chat.id)
                                    isSearchActive = false
                                },
                                onTogglePin = { onTogglePin(chat.id, chat.isPinned) },
                                onToggleMute = { onToggleMute(chat.id, chat.isMuted) },
                                onDelete = { onDeleteChat(chat.id) }
                            )
                            Divider(
                                modifier = Modifier.padding(start = 76.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                } else if (!hasSearchResults) {
                    // No Results Found
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TelegramBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "There were no chats or contacts matching \"$searchQuery\"",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Search Results List (Split by Contacts & Existing Chats)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_results_list")
                    ) {
                        // Section 1: Matching Contacts
                        if (matchingContacts.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Contacts (${matchingContacts.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TelegramBlue
                                    )
                                }
                            }

                            items(matchingContacts, key = { it.id }) { contact ->
                                SearchContactItemRow(
                                    contact = contact,
                                    onClick = {
                                        onContactClick(contact)
                                        isSearchActive = false
                                    }
                                )
                                Divider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            }
                        }

                        // Section 2: Matching Existing Conversations
                        if (matchingChats.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = TelegramBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Chats & Conversations (${matchingChats.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TelegramBlue
                                    )
                                }
                            }

                            items(matchingChats, key = { it.id }) { chat ->
                                ChatItemRow(
                                    chat = chat,
                                    onClick = {
                                        onOpenChat(chat.id)
                                        isSearchActive = false
                                    },
                                    onTogglePin = { onTogglePin(chat.id, chat.isPinned) },
                                    onToggleMute = { onToggleMute(chat.id, chat.isMuted) },
                                    onDelete = { onDeleteChat(chat.id) }
                                )
                                Divider(
                                    modifier = Modifier.padding(start = 76.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            } else {
                // Standard Top App Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "Bluemoon",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onOpenDrawer,
                            modifier = Modifier.testTag("open_drawer_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Navigation Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_action_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Prominent Search Bar at top of Chat List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .clickable { isSearchActive = true }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("chat_search_bar")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search chats, contacts, channels...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Folder Tabs (All, Personal, Channels, Groups, Secret)
                ScrollableTabRow(
                    selectedTabIndex = selectedFolder.ordinal,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = TelegramBlue,
                    indicator = { tabPositions ->
                        if (selectedFolder.ordinal < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedFolder.ordinal]),
                                color = TelegramBlue
                            )
                        }
                    },
                    divider = {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                ) {
                    ChatFolder.values().forEach { folder ->
                        val title = when (folder) {
                            ChatFolder.ALL -> "All Chats"
                            ChatFolder.PERSONAL -> "Personal"
                            ChatFolder.CHANNELS -> "Channels"
                            ChatFolder.GROUPS -> "Groups"
                            ChatFolder.SECRET -> "Secret"
                        }
                        Tab(
                            selected = selectedFolder == folder,
                            onClick = { onSelectFolder(folder) },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedFolder == folder) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier.testTag("folder_tab_${folder.name}")
                        )
                    }
                }

                // Telegram Stories Bar
                if (stories.isNotEmpty()) {
                    StoryBar(
                        stories = stories,
                        onStoryClick = onOpenStory,
                        onAddStoryClick = onOpenNewChatMenu
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                }

                // Main Chats List
                if (chats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No chats in this folder",
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 72.dp)
                            .testTag("chats_list")
                    ) {
                        items(chats, key = { it.id }) { chat ->
                            ChatItemRow(
                                chat = chat,
                                onClick = { onOpenChat(chat.id) },
                                onTogglePin = { onTogglePin(chat.id, chat.isPinned) },
                                onToggleMute = { onToggleMute(chat.id, chat.isMuted) },
                                onDelete = { onDeleteChat(chat.id) }
                            )
                            Divider(
                                modifier = Modifier.padding(start = 76.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button (New Message) - Positioned comfortably above the sticky bottom nav
        FloatingActionButton(
            onClick = onOpenNewChatMenu,
            containerColor = TelegramBlue,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 84.dp, end = 16.dp)
                .testTag("new_message_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "New Chat"
            )
        }
    }
}

@Composable
fun SuggestedContactItem(
    contact: ContactEntity,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .width(62.dp)
            .testTag("suggested_contact_${contact.id}")
    ) {
        ContactAvatar(
            name = contact.name,
            avatarColorHex = contact.avatarColorHex,
            photoUri = contact.photoUri,
            size = 48.dp,
            isOnline = contact.isOnline,
            showOnlineDot = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = contact.name.split(" ").firstOrNull() ?: contact.name,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SearchContactItemRow(
    contact: ContactEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("search_contact_${contact.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = contact.name,
            avatarColorHex = contact.avatarColorHex,
            photoUri = contact.photoUri,
            size = 50.dp,
            isOnline = contact.isOnline,
            showOnlineDot = true
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (contact.username.isNotEmpty()) {
                    Text(
                        text = "@${contact.username}",
                        fontSize = 13.sp,
                        color = TelegramBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = if (contact.isOnline) "online" else contact.phoneNumber,
                    fontSize = 12.sp,
                    color = if (contact.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onClick,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Message",
                tint = TelegramBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ChatItemRow(
    chat: ChatEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val timeString = remember(chat.lastMessageTime) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(chat.lastMessageTime))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { showContextMenu = true }
                )
            }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("chat_row_${chat.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = chat.title,
            avatarColorHex = chat.avatarColorHex,
            photoUri = null,
            size = 54.dp,
            isOnline = chat.isOnline,
            showOnlineDot = true,
            isSavedMessages = chat.type == ChatType.SAVED_MESSAGES
        )

        Spacer(modifier = Modifier.width(14.dp))

        // Center Content (Title, Badges, Last Message)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.type == ChatType.SECRET) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secret Chat",
                        tint = SecretChatGreen,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                } else if (chat.type == ChatType.CHANNEL) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Channel",
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                } else if (chat.type == ChatType.GROUP) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Group",
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                }

                Text(
                    text = chat.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (chat.type == ChatType.SECRET) SecretChatGreen else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (chat.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = TelegramBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (chat.isMuted) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = "Muted",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = chat.lastMessage,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right side (Timestamp, Pin, Unread Badge)
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timeString,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (chat.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp).padding(end = 4.dp)
                    )
                }

                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (chat.isMuted) Color.Gray else TelegramBlue)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chat.unreadCount}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Long Press Context Menu
        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (chat.isPinned) "Unpin" else "Pin") },
                onClick = {
                    onTogglePin()
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text(if (chat.isMuted) "Unmute" else "Mute") },
                onClick = {
                    onToggleMute()
                    showContextMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Delete Chat") },
                onClick = {
                    onDelete()
                    showContextMenu = false
                }
            )
        }
    }
}
