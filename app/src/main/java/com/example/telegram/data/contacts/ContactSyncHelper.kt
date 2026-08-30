package com.example.telegram.data.contacts

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.telegram.data.db.ContactEntity
import java.util.UUID

object ContactSyncHelper {

    private val AVATAR_COLORS = listOf(
        "#2481CC", // Bluemoon Blue
        "#E91E63", // Magenta
        "#00C853", // Emerald Green
        "#9C27B0", // Deep Purple
        "#FF9800", // Warm Amber
        "#00BCD4", // Cyan
        "#3F51B5", // Indigo
        "#F44336", // Coral Red
        "#009688", // Teal
        "#673AB7"  // Violet
    )

    /**
     * Check whether READ_CONTACTS permission is granted.
     */
    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Query device contacts from ContactsContract provider.
     */
    fun fetchDeviceContacts(context: Context): List<ContactEntity> {
        val contactList = mutableListOf<ContactEntity>()
        if (!hasContactsPermission(context)) {
            return contactList
        }

        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        val seenNumbers = mutableSetOf<String>()

        try {
            contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val thumbCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                while (cursor.moveToNext()) {
                    val rawId = if (idCol != -1) cursor.getString(idCol) ?: UUID.randomUUID().toString() else UUID.randomUUID().toString()
                    val name = if (nameCol != -1) cursor.getString(nameCol) ?: "Unknown" else "Unknown"
                    val number = if (numCol != -1) cursor.getString(numCol) ?: "" else ""
                    val cleanNumber = number.replace("[^0-9+]".toRegex(), "")

                    if (cleanNumber.isNotBlank() && !seenNumbers.contains(cleanNumber)) {
                        seenNumbers.add(cleanNumber)
                        val photoUri = if (photoCol != -1) cursor.getString(photoCol) else null
                        val thumbUri = if (thumbCol != -1) cursor.getString(thumbCol) else null
                        val profilePic = photoUri ?: thumbUri

                        val colorHex = AVATAR_COLORS[Math.abs(name.hashCode()) % AVATAR_COLORS.size]
                        val cleanUsername = name.lowercase().replace("[^a-z0-9_]".toRegex(), "")

                        contactList.add(
                            ContactEntity(
                                id = "device_$rawId",
                                name = name,
                                username = if (cleanUsername.isNotEmpty()) cleanUsername else "user_$rawId",
                                phoneNumber = number,
                                bio = "Device Contact",
                                avatarColorHex = colorHex,
                                photoUri = profilePic,
                                isOnline = (Math.abs(name.hashCode()) % 3 == 0),
                                lastSeenText = if (Math.abs(name.hashCode()) % 3 == 0) "online" else "last seen recently",
                                isDeviceContact = true
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return contactList
    }

    /**
     * Curated demo contacts with high resolution profile pictures for preview & test scenarios.
     */
    fun getSampleDeviceContacts(): List<ContactEntity> {
        return listOf(
            ContactEntity(
                id = "sample_dev_1",
                name = "Sophia Martinez",
                username = "sophia_m",
                phoneNumber = "+1 (555) 234-8901",
                bio = "Product Designer & Photographer",
                avatarColorHex = "#E91E63",
                photoUri = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200&auto=format&fit=crop&q=80",
                isOnline = true,
                lastSeenText = "online",
                isDeviceContact = true
            ),
            ContactEntity(
                id = "sample_dev_2",
                name = "Liam Alexander",
                username = "liam_alex",
                phoneNumber = "+1 (555) 876-5432",
                bio = "Full-Stack Software Architect",
                avatarColorHex = "#2481CC",
                photoUri = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
                isOnline = true,
                lastSeenText = "online",
                isDeviceContact = true
            ),
            ContactEntity(
                id = "sample_dev_3",
                name = "Emma Watson-Lee",
                username = "emma_wl",
                phoneNumber = "+44 7911 123456",
                bio = "Creative Director at Studio Blue",
                avatarColorHex = "#9C27B0",
                photoUri = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                isOnline = false,
                lastSeenText = "last seen 12m ago",
                isDeviceContact = true
            ),
            ContactEntity(
                id = "sample_dev_4",
                name = "Daniel Kim",
                username = "daniel_kim",
                phoneNumber = "+82 10 9876 5432",
                bio = "Mobile UX & Jetpack Compose Engineer",
                avatarColorHex = "#00C853",
                photoUri = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
                isOnline = true,
                lastSeenText = "online",
                isDeviceContact = true
            ),
            ContactEntity(
                id = "sample_dev_5",
                name = "Olivia Chen",
                username = "olivia_c",
                phoneNumber = "+1 (555) 345-6789",
                bio = "AI & ML Researcher",
                avatarColorHex = "#FF9800",
                photoUri = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200&auto=format&fit=crop&q=80",
                isOnline = false,
                lastSeenText = "last seen 1h ago",
                isDeviceContact = true
            ),
            ContactEntity(
                id = "sample_dev_6",
                name = "Marcus Vance",
                username = "marcus_vance",
                phoneNumber = "+1 (555) 901-2345",
                bio = "Security Engineer & Crypto Lead",
                avatarColorHex = "#3F51B5",
                photoUri = "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=200&auto=format&fit=crop&q=80",
                isOnline = false,
                lastSeenText = "last seen yesterday",
                isDeviceContact = true
            )
        )
    }
}
