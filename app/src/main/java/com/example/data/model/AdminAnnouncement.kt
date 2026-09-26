package com.example.data.model

import org.json.JSONObject

/**
 * Paleta de temas dinámicos para los comunicados oficiales del Administrador.
 */
enum class AnnouncementTheme(
    val id: String,
    val title: String,
    val emoji: String,
    val topGradientColor: Long,
    val bottomGradientColor: Long,
    val accentColor: Long,
    val borderAlpha: Float
) {
    OFFICIAL(
        id = "official",
        title = "Actitud Fuerte (Oficial)",
        emoji = "💪",
        topGradientColor = 0xFF142410,
        bottomGradientColor = 0xFF0B1209,
        accentColor = 0xFFCCFF00,
        borderAlpha = 0.75f
    ),
    URGENT(
        id = "urgent",
        title = "Alerta / Urgente",
        emoji = "🚨",
        topGradientColor = 0xFF351505,
        bottomGradientColor = 0xFF180A02,
        accentColor = 0xFFF59E0B,
        borderAlpha = 0.85f
    ),
    INFO(
        id = "info",
        title = "Informativo / Eventos",
        emoji = "📢",
        topGradientColor = 0xFF0A2238,
        bottomGradientColor = 0xFF05101A,
        accentColor = 0xFF38BDF8,
        borderAlpha = 0.75f
    ),
    MOTIVATION(
        id = "motivation",
        title = "Motivación / Retos",
        emoji = "🔥",
        topGradientColor = 0xFF2B0F3A,
        bottomGradientColor = 0xFF13061A,
        accentColor = 0xFFA855F7,
        borderAlpha = 0.80f
    )
}

/**
 * Modelo de datos para Anuncios Administrativos Fijados con vigencia de 24 horas.
 * Se serializa y sincroniza en tiempo real como un SocialPost de categoría ANUNCIO_OFICIAL.
 */
data class AdminAnnouncement(
    val id: Long = 0,
    val authorName: String = "Alex Gómez",
    val authorRole: String = "CEO & Head Coach • Actitud Fuerte",
    val authorEmail: String = "alexgcuicas@gmail.com",
    val contentText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val scheduledPublishTime: Long = timestamp, // Marca de tiempo programada de emisión
    val expiresAt: Long = timestamp + (24 * 60 * 60 * 1000L), // Vigencia a partir de emisión
    val theme: AnnouncementTheme = AnnouncementTheme.OFFICIAL,
    val audioUrl: String = "", // Archivo o Base64 de la nota de voz
    val audioDurationSeconds: Int = 0,
    val attachmentUrl: String = "", // Base64 o Uri de la imagen o PDF
    val attachmentName: String = "",
    val attachmentType: String = "NONE", // "IMAGE", "PDF", "NONE"
    val likesCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val commentsCount: Int = 0
) {
    val isScheduled: Boolean
        get() = scheduledPublishTime > System.currentTimeMillis()

    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt

    val isCurrentlyLive: Boolean
        get() = System.currentTimeMillis() >= scheduledPublishTime && !isExpired

    /**
     * Retorna el tiempo restante amigable para el usuario (ej: "23h 45m" o "Expira pronto")
     */
    val remainingTimeString: String
        get() {
            val remainingMs = expiresAt - System.currentTimeMillis()
            if (remainingMs <= 0) return "Expirado"
            val totalMinutes = remainingMs / (1000 * 60)
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
        }

    val scheduledDateString: String
        get() {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm:ss", java.util.Locale.getDefault())
            return sdf.format(java.util.Date(scheduledPublishTime))
        }

    fun toSocialPost(): SocialPost {
        val meta = JSONObject().apply {
            put("scheduledPublishTime", scheduledPublishTime)
            put("expiresAt", expiresAt)
            put("themeId", theme.id)
            put("audioUrl", audioUrl)
            put("audioDuration", audioDurationSeconds)
            put("attachmentUrl", attachmentUrl)
            put("attachmentName", attachmentName)
            put("attachmentType", attachmentType)
            put("authorRole", authorRole)
            put("authorEmail", authorEmail)
        }
        return SocialPost(
            id = id,
            authorId = 999999L,
            authorName = authorName,
            authorAccessId = "ADMIN_OFFICIAL",
            contentText = contentText,
            mediaUrl = attachmentUrl,
            mediaType = if (attachmentType != "NONE") attachmentType else if (audioUrl.isNotBlank()) "AUDIO" else "TEXT_ONLY",
            timestamp = if (scheduledPublishTime > timestamp) scheduledPublishTime else timestamp,
            likesCount = likesCount,
            commentsCount = commentsCount,
            isLikedByMe = isLikedByMe,
            category = "ANUNCIO_OFICIAL",
            workoutDetails = meta.toString()
        )
    }

    companion object {
        fun fromSocialPost(post: SocialPost): AdminAnnouncement {
            var theme = AnnouncementTheme.OFFICIAL
            var scheduledPublishTime = post.timestamp
            var expiresAt = post.timestamp + (24 * 60 * 60 * 1000L)
            var audioUrl = ""
            var audioDuration = 0
            var attachmentUrl = post.mediaUrl
            var attachmentName = ""
            var attachmentType = if (post.mediaType in listOf("IMAGE", "PDF")) post.mediaType else "NONE"
            var authorRole = "CEO & Head Coach • Actitud Fuerte"
            var authorEmail = "alexgcuicas@gmail.com"

            if (post.workoutDetails.isNotBlank()) {
                try {
                    val json = JSONObject(post.workoutDetails)
                    scheduledPublishTime = json.optLong("scheduledPublishTime", post.timestamp)
                    expiresAt = json.optLong("expiresAt", scheduledPublishTime + (24 * 60 * 60 * 1000L))
                    val themeId = json.optString("themeId", "official")
                    theme = AnnouncementTheme.values().firstOrNull { it.id == themeId } ?: AnnouncementTheme.OFFICIAL
                    audioUrl = json.optString("audioUrl", "")
                    audioDuration = json.optInt("audioDuration", 0)
                    attachmentUrl = json.optString("attachmentUrl", post.mediaUrl)
                    attachmentName = json.optString("attachmentName", "")
                    attachmentType = json.optString("attachmentType", attachmentType)
                    authorRole = json.optString("authorRole", authorRole)
                    authorEmail = json.optString("authorEmail", authorEmail)
                } catch (_: Exception) {}
            }

            return AdminAnnouncement(
                id = post.id,
                authorName = post.authorName,
                authorRole = authorRole,
                authorEmail = authorEmail,
                contentText = post.contentText,
                timestamp = post.timestamp,
                scheduledPublishTime = scheduledPublishTime,
                expiresAt = expiresAt,
                theme = theme,
                audioUrl = audioUrl,
                audioDurationSeconds = audioDuration,
                attachmentUrl = attachmentUrl,
                attachmentName = attachmentName,
                attachmentType = attachmentType,
                likesCount = post.likesCount,
                isLikedByMe = post.isLikedByMe,
                commentsCount = post.commentsCount
            )
        }
    }
}
