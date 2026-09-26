package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Registro de check-in / asistencia a sala de musculación.
 */
@Entity(
    tableName = "attendances",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateOnlyString: String, // e.g., "2026-09-19"
    val timeOnlyString: String  // e.g., "08:30 AM"
)

/**
 * Registro de cobros y pagos de membresía.
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val amount: Double,
    val planName: String,         // "Membresía 12$" o "Membresía 20$"
    val frequency: String,        // "Semanal", "Quincenal", "Mensual"
    val method: String = "Efectivo", // "Efectivo", "Transferencia", "Pago Móvil", "Zelle"
    val reference: String = ""
)
