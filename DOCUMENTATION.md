# Bluemoon Android (Jetpack Compose) — Technical & User Documentation

## 1. Overview

This project is a native Android messaging application built with **Jetpack Compose**, **Kotlin Coroutines / Flow**, **Room Persistence Library**, and **Material Design 3 (M3)**. It provides a real-time, fluid Bluemoon messaging experience including authentication, multi-type chat threads, real-time search, voice/video calls, stories, and settings customization.

---

## 2. Key Architecture & Features

### 2.1 Architecture
The application adheres to the **MVVM (Model-View-ViewModel)** architectural pattern:
- **UI Layer (`com.example.telegram.ui`)**: Declarative UI built exclusively with Jetpack Compose.
  - **Screens (`ui/screens`)**: Full-screen composables (Chat list, Conversation, Calls, Stories, Contacts, Settings, Authentication).
  - **Components (`ui/components`)**: Reusable widgets (Drawer, StoryBar, Call modals, Chat dialogs).
  - **Theme (`ui/theme`)**: Telegram color schemes (Light / Dark mode), typography, shapes.
  - **ViewModel (`ui/viewmodels`)**: `TelegramViewModel` managing UI state, flows, search queries, active calls, and user settings.
- **Data Layer (`com.example.telegram.data`)**:
  - **Database (`data/db`)**: SQLite with Room (`AppDatabase`, `TelegramDao`, `ChatEntity`, `MessageEntity`, `ContactEntity`, `StoryEntity`, `UserSettingsEntity`).
  - **Repository (`data/repository`)**: `TelegramRepository` providing reactive database operations and initial seeded demo data.
  - **Models (`data/models`)**: Data classes, enums (`ChatType`, `MessageStatus`, `AttachmentType`).

---

## 3. Screen Breakdown & Workflows

### 3.1 Authentication & Onboarding
- **Landing Screen (`LandingScreen.kt`)**: 
  - Telegram paper airplane brand badge.
  - Carousel with 6 feature slides (*Fast, Free, Powerful, Secure, Cloud-Based, Unlimited Storage*).
  - "Start Messaging" CTA and quick demo login.
- **Phone Number Authentication (`PhoneAuthScreen.kt`)**:
  - Interactive country selector (Country name, dial code, flag emoji).
  - Custom Telegram numeric keyboard with letters and backspace.
  - Verification permission modals.
- **Country Picker (`CountryPickerScreen.kt`)**:
  - Searchable country directory with dial codes and flags.
- **OTP Verification Screen (`OtpVerificationScreen.kt`)**:
  - 6-digit individual rounded input boxes with focused blue styling.
  - 60-second real-time countdown timer with SMS / Voice call resend options.
  - Auto-verification upon entering the 6th digit.
  - Demo autofill shortcuts.
- **Status Screens (`AuthStatusScreens.kt`)**:
  - Loading State with pulsing indicators.
  - Error State with retry actions.
  - Success State & Profile Setup (First Name, Username, Avatar Color).

### 3.2 Main Chat Interface & Search
- **Chat List (`ChatListScreen.kt`)**:
  - **Top Search Bar**: Instant unified search across chats and contacts.
  - **Filter Chips**: Filter by *All*, *Chats*, *Contacts*, *Channels*, *Groups*, and *Secret Chats*.
  - **Suggested Contacts Carousel**: Quick access to frequent contacts on search focus.
  - **Contact & Chat Results**: Shows matched contacts with online status and direct message CTA, alongside matching conversation threads.
  - **Folder Tabs**: *All Chats*, *Personal*, *Channels*, *Groups*, *Secret*.
  - **Stories Bar**: Horizontal preview of active user stories with unread ring gradients.
  - **Floating Action Button**: New Chat / Channel / Group / Secret Chat creator.

### 3.3 Active Conversation & Messaging (`ChatScreen.kt`)
- **Header**: User/Group info, avatar, online status, Voice/Video call shortcuts, options menu.
- **Message List**:
  - Speech bubbles with sent/delivered/read checkmarks.
  - Media & Attachment cards (Photos, Voice messages, Files, Locations).
  - Reply context, message reactions, and pin indicators.
- **Input Bar**:
  - Emoji & Sticker picker drawer.
  - Attachment menu (Gallery, File, Location, Contact, Poll).
  - Voice recording button with hold-to-record animation.
  - Instant text sender.

### 3.4 Voice & Video Calling (`ActiveCallScreen.kt`)
- Voice and HD Video call overlay.
- End-to-end encryption emoji fingerprint.
- Controls: Mute, Speaker toggle, Video toggle, End Call.

### 3.5 Stories Viewer (`StoryViewerScreen.kt`)
- Full-screen media viewer with segment progress bars.
- User profile info, caption, reply bar, and heart reactions.

### 3.6 Settings & Personalization (`SettingsScreen.kt`)
- Account Profile display with customizable avatar colors.
- Dark / Light Night Mode switcher.
- Notifications & Sound toggles.
- Privacy & Security settings.
- Data & Storage usage settings.
- Log Out & Account switching.

---

## 4. Local Database Schema

| Table | Entity | Description |
| :--- | :--- | :--- |
| `chats` | `ChatEntity` | Conversation threads (Title, type, avatar color, pinned/muted state, unread counts) |
| `messages` | `MessageEntity` | Chat messages (Text, sender, timestamp, read status, attachments, reactions) |
| `contacts` | `ContactEntity` | Address book contacts (Name, phone, username, online status, avatar color) |
| `stories` | `StoryEntity` | User 24h stories (Media URL, caption, timestamp, viewed state) |
| `user_settings` | `UserSettingsEntity` | Current logged-in user profile, night mode, security, and app preferences |

---

## 5. Development & Build Commands

- **Compile Android Applet**:
  `compile_applet` (checks compilation and builds APK)
- **Run Local Unit Tests**:
  `gradle :app:testDebugUnitTest`
