package com.example.data.model

enum class ResourceType(val label: String, val badge: String) {
    PDF("Documento PDF", "PDF"),
    AUDIO("Audio / Podcast", "AUDIO"),
    VIDEO("Video / Técnica", "VIDEO")
}

data class AppResource(
    val id: String,
    val title: String,
    val description: String,
    val type: ResourceType,
    val fileName: String,
    val filePath: String,
    val fileSizeBytes: Long,
    val fileSizeFormatted: String,
    val category: String,
    val author: String,
    val dateFormatted: String,
    val isOfficialGuide: Boolean = false
)
