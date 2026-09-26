package com.example.data.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class NotificationType(
    val displayName: String,
    val primaryColor: Color,
    val badgeBgColor: Color,
    val iconEmoji: String
) {
    SYSTEM(
        displayName = "SISTEMA",
        primaryColor = Color(0xFFF59E0B), // Dorado / Ámbar Neón
        badgeBgColor = Color(0x33F59E0B),
        iconEmoji = "📢"
    ),
    SOCIAL(
        displayName = "COMUNIDAD",
        primaryColor = Color(0xFF00F0FF), // Cyan Eléctrico
        badgeBgColor = Color(0x3300F0FF),
        iconEmoji = "💬"
    )
}

data class AppNotification(
    val id: String = UUID.randomUUID().toString(),
    val type: NotificationType,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val actionTarget: String = "GENERAL", // "ANNOUNCEMENT", "POST", "MEMBERSHIP", "COMMUNITY"
    val targetId: Long = 0L,
    val customEmoji: String = ""
) {
    val relativeTimeString: String
        get() {
            val now = System.currentTimeMillis()
            val diffMs = now - timestamp
            val minutes = diffMs / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            return when {
                minutes < 1 -> "Ahora mismo"
                minutes < 60 -> "Hace ${minutes}m"
                hours < 24 -> "Hace ${hours}h"
                days == 1L -> "Ayer"
                days < 7 -> "Hace ${days}d"
                else -> {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
}
