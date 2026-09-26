package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Niveles Jerárquicos de Atletas en la Sala de Musculación Actitud Fuerte (Alex Gómez).
 * Homologados a una paleta sobria deportiva anclada en el Morado Oficial (#A855F7)
 * y el Verde Lima Oficial (#CCFF00), con tonos titanio y carbón.
 */
enum class AthleteTier(
    val title: String,
    val shortName: String,
    val subtitle: String,
    val neonColor: Color,
    val bgGlowColor: Color,
    val iconEmoji: String,
    val rankOrder: Int
) {
    ELITE(
        title = "ÉLITE",
        shortName = "Élite",
        subtitle = "Máxima Autoridad & Disciplina",
        neonColor = Color(0xFFA855F7), // Morado Oficial (igual a Anuncios Oficiales - Admin)
        bgGlowColor = Color(0xFF251033),
        iconEmoji = "🟪",
        rankOrder = 1
    ),
    WARRIOR(
        title = "AVANZADO",
        shortName = "Avanzado",
        subtitle = "Fuerza Consolidada",
        neonColor = Color(0xFFCCFF00), // Verde Lima Oficial
        bgGlowColor = Color(0xFF1B2A16),
        iconEmoji = "🟩",
        rankOrder = 2
    ),
    INTERMEDIATE(
        title = "ACTIVO",
        shortName = "Activo",
        subtitle = "Constancia Deportiva",
        neonColor = Color(0xFF94A3B8), // Plata Táctico / Titanio
        bgGlowColor = Color(0xFF1E293B),
        iconEmoji = "⬜",
        rankOrder = 3
    ),
    INITIATE(
        title = "INICIADO",
        shortName = "Iniciado",
        subtitle = "Adaptación & Fundamentos",
        neonColor = Color(0xFF64748B), // Gris Carbón Pulido
        bgGlowColor = Color(0xFF1A1D24),
        iconEmoji = "▫️",
        rankOrder = 4
    );

    companion object {
        fun getTierForClient(client: Client, attendancesCount: Int = 0): AthleteTier {
            val alias = client.athleteAlias.lowercase()
            val bio = client.bio.lowercase()
            val name = client.fullName.lowercase()
            
            return when {
                alias.contains("leyenda") || alias.contains("titán") || alias.contains("elite") || 
                bio.contains("élite") || bio.contains("leyenda") || name.contains("alex") || attendancesCount >= 18 -> ELITE
                
                alias.contains("guerrero") || alias.contains("fuerza") || bio.contains("avanzado") || attendancesCount >= 10 -> WARRIOR
                
                alias.contains("intermedio") || alias.contains("activo") || attendancesCount >= 4 -> INTERMEDIATE
                
                else -> {
                    // Distribución armoniosa por defecto si es nuevo
                    when ((client.id % 4L).toInt()) {
                        0 -> ELITE
                        1 -> WARRIOR
                        2 -> INTERMEDIATE
                        else -> INITIATE
                    }
                }
            }
        }
    }
}

