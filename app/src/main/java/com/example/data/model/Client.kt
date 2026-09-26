package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un cliente registrado en la sala de musculación Actitud Fuerte.
 */
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phone: String,
    val email: String,
    val membershipPlan: String,      // "Membresía 12$" o "Membresía 20$"
    val paymentFrequency: String,    // "Semanal", "Quincenal", "Mensual"
    val emergencyContact: String,    // Nombre y teléfono de contacto de emergencia
    val gender: String = "Hombre",   // "Hombre", "Mujer"
    val mainObjective: String,       // 1 de los 5 objetivos principales
    val medicalCondition: String,    // Condición o nota médica
    val medicalNotes: String = "",
    val operationalStatus: String = "Activo", // "Activo", "Inactivo", "Enfermo", "Sancionado"
    val registrationTimestamp: Long = System.currentTimeMillis(),
    val accessId: String = "",       // Código de acceso único (e.g., AF-4821)
    val accessPin: String = "",      // Clave o PIN de acceso al sistema (Login)
    val weeklyRoutine: String = "Lunes: Pecho y Tríceps\nMartes: Espalda y Bíceps\nMiércoles: Pierna Completa\nJueves: Hombro y Trapecio\nViernes: Core y Cardio Funcional\nSábado: Acondicionamiento y Fuerza",
    val bio: String = "Atleta de Alto Rendimiento • Actitud Fuerte",
    val athleteAlias: String = "",   // Alias deportivo (ej: "Titán")
    val starPr: String = "",         // PR Estrella (ej: "Sentadilla 140kg • 4x6")
    val socialStatus: String = "Activo", // "Activo", "Inactivo", "Desconectado"
    val avatarUrl: String = ""       // Foto de perfil sincronizada en tiempo real (Base64 o URL)
)

val Client.isActive: Boolean
    get() = operationalStatus.equals("Activo", ignoreCase = true)

val Client.idNumber: String
    get() = accessId.ifBlank { "AF-${1000 + id}" }

object FitnessConstants {
    val OBJECTIVES = listOf(
        "Pérdida de grasa y definición",
        "Hipertrofia y ganancia muscular",
        "Aumento de fuerza y potencia",
        "Tonificación y resistencia general",
        "Salud integral y readaptación física"
    )

    val MEDICAL_CONDITIONS = listOf(
        "Ninguna de las anteriores",
        "Lesión de hombro / manguito rotador",
        "Lumbalgia / hernia discal",
        "Lesión de rodilla / meniscos",
        "Hipertensión / condición cardiovascular",
        "Asma / afección respiratoria",
        "Diabetes / control metabólico",
        "Otra condición especial"
    )

    val MEMBERSHIP_PLANS = listOf(
        "Membresía 12$",
        "Membresía 20$"
    )

    val PAYMENT_FREQUENCIES = listOf(
        "Semanal",
        "Quincenal",
        "Mensual"
    )

    val OPERATIONAL_STATUSES = listOf(
        "Activo",
        "Inactivo",
        "Enfermo",
        "Sancionado"
    )
}
