package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Entidad para el registro de sobrecarga progresiva e historial de cargas.
 * Permite al atleta registrar los pesos movidos, repeticiones y sensaciones
 * por ejercicio y día de la semana.
 */
@Entity(tableName = "workout_load_logs")
data class WorkoutLoadLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long,
    val clientAccessId: String = "",
    val exerciseName: String,
    val muscleGroup: String = "",
    val dayOfWeek: String = "", // "Lunes", "Martes", etc.
    val weightKg: Double,
    val reps: Int,
    val setsCount: Int = 4,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
}
