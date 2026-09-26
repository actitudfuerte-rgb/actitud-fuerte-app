package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

/**
 * Entidad que representa un comentario dentro de una publicación de la comunidad.
 */
data class SocialComment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val authorId: Long,
    val authorName: String,
    val authorAvatarUrl: String = "",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val replyToCommentId: String = "",
    val replyToAuthorName: String = "",
    val likesCount: Int = 0,
    val likedByClientIds: List<Long> = emptyList()
)

/**
 * Entidad que representa una publicación en la Red Social de Actitud Fuerte Gym.
 * Admite publicaciones tipo microblogging (texto) e imágenes/videos verticales
 * con relación de aspecto 3:4 (estándar de feed de Instagram) y notas de audio.
 */
@Entity(tableName = "social_posts")
data class SocialPost(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val authorId: Long,
    val authorName: String,
    val authorAccessId: String,
    val authorAvatarUrl: String = "",
    val contentText: String,
    val mediaUrl: String = "",
    val mediaType: String = "IMAGE", // "IMAGE", "VIDEO", "TEXT_ONLY", "AUDIO"
    val aspectRatio: String = "3:4",  // Relación de aspecto vertical 3:4
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val category: String = "ENTRENAMIENTO", // "TRANSFORMACIÓN", "PR_FUERZA", "MOTIVACIÓN", "RUTINA", "HISTORIA_24H", "ADMIN_ANNOUNCEMENT"
    val workoutDetails: String = "",
    val commentsJson: String = "[]"
) {
    fun getCommentsList(): List<SocialComment> {
        return SocialPostUtils.parseComments(commentsJson)
    }
}

object SocialPostUtils {
    fun parseComments(jsonStr: String?): List<SocialComment> {
        if (jsonStr.isNullOrBlank() || jsonStr == "[]") return emptyList()
        val list = mutableListOf<SocialComment>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val likedArray = obj.optJSONArray("likedByClientIds")
                val likedList = mutableListOf<Long>()
                if (likedArray != null) {
                    for (j in 0 until likedArray.length()) {
                        likedList.add(likedArray.optLong(j))
                    }
                }
                list.add(
                    SocialComment(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        authorId = obj.optLong("authorId", 0L),
                        authorName = obj.optString("authorName", "Atleta"),
                        authorAvatarUrl = obj.optString("authorAvatarUrl", ""),
                        text = obj.optString("text", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        replyToCommentId = obj.optString("replyToCommentId", ""),
                        replyToAuthorName = obj.optString("replyToAuthorName", ""),
                        likesCount = obj.optInt("likesCount", likedList.size),
                        likedByClientIds = likedList
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeComments(comments: List<SocialComment>): String {
        val array = JSONArray()
        for (c in comments) {
            val obj = JSONObject().apply {
                put("id", c.id)
                put("authorId", c.authorId)
                put("authorName", c.authorName)
                put("authorAvatarUrl", c.authorAvatarUrl)
                put("text", c.text)
                put("timestamp", c.timestamp)
                put("replyToCommentId", c.replyToCommentId)
                put("replyToAuthorName", c.replyToAuthorName)
                put("likesCount", c.likesCount)
                val likedArr = JSONArray()
                c.likedByClientIds.forEach { likedArr.put(it) }
                put("likedByClientIds", likedArr)
            }
            array.put(obj)
        }
        return array.toString()
    }
}

object SocialPostConstants {
    val CATEGORIES = listOf(
        "ENTRENAMIENTO",
        "PR_FUERZA",
        "TRANSFORMACIÓN",
        "MOTIVACIÓN",
        "NUTRICIÓN"
    )

    // Gradientes e imágenes de demostración deportiva para publicaciones 3:4
    val PRESET_MEDIA_STYLES = listOf(
        PresetMediaStyle(
            id = "beast_mode",
            title = "🔥 Modo Bestia",
            tag = "PR_FUERZA",
            backgroundHex = "#1E2A18",
            accentHex = "#D4FF00",
            iconEmoji = "🏋️‍♂️"
        ),
        PresetMediaStyle(
            id = "transformation",
            title = "💪 Transformación",
            tag = "TRANSFORMACIÓN",
            backgroundHex = "#131E2A",
            accentHex = "#38BDF8",
            iconEmoji = "📈"
        ),
        PresetMediaStyle(
            id = "leg_day",
            title = "🦵 Día de Pierna",
            tag = "ENTRENAMIENTO",
            backgroundHex = "#281726",
            accentHex = "#EC4899",
            iconEmoji = "⚡"
        ),
        PresetMediaStyle(
            id = "discipline",
            title = "🛡️ Disciplina Pura",
            tag = "MOTIVACIÓN",
            backgroundHex = "#232314",
            accentHex = "#FBBF24",
            iconEmoji = "🏆"
        )
    )
}

data class PresetMediaStyle(
    val id: String,
    val title: String,
    val tag: String,
    val backgroundHex: String,
    val accentHex: String,
    val iconEmoji: String
)
