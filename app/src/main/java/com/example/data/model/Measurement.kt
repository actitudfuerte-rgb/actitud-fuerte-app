package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para registro de medidas corporales y progreso del atleta.
 */
@Entity(
    tableName = "measurements",
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
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val weightKg: Double,
    val backCm: Double,
    val shouldersCm: Double,
    val armsCm: Double,
    val hipsCm: Double,
    val aiObservation: String = "",       // Generada con IA comparando con la anterior
    val trainerObservation: String = "",  // Observación final del entrenador
    val sentViaEmail: Boolean = false,
    val sentViaWhatsapp: Boolean = false
)
