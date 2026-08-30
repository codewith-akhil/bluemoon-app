package com.example.telegram.ui.navigation

sealed class Screen(val route: String) {
    object Chats : Screen("chats")
    object Contacts : Screen("contacts")
    object Calls : Screen("calls")
    object Settings : Screen("settings")
    object SavedMessages : Screen("saved_messages")
}
