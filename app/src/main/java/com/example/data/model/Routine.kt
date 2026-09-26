package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

/**
 * Entidad para la Biblioteca Maestra de Rutinas en la sala de musculación Actitud Fuerte.
 * Permite gestionar y guardar rutinas completas de la semana (Lunes a Sábado).
 */
@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // Ej: "Rutina de entrenamiento 1", "Rutina de entrenamiento 2"
    val targetType: String,              // "Hombres", "Mujeres", "Usuarios específicos"
    val targetClientNames: String = "",  // Ej: "Alex Gómez, Carlen Chirinos"
    val specialConditions: String = "",  // Ej: "Salud, edad, lesiones, adaptación inicial"
    val exercisesJson: String = "",      // JSON con los días de la semana (Lunes a Sábado) y sus ejercicios
    val createdAt: Long = System.currentTimeMillis()
)

data class RoutineExercise(
    val exerciseName: String,            // Nombre de ejercicios
    val sets: String,                    // Series (ej: "4")
    val reps: String,                    // Repeticiones (ej: "10-12")
    val youtubeUrl: String               // URL de video de YouTube del ejercicio
)

data class DayRoutine(
    val dayName: String,                 // "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"
    val muscleFocus: String = "",        // Ej: "Pecho & Tríceps", "Piernas & Glúteos", "Espalda & Core"
    val exercises: List<RoutineExercise> = emptyList()
)

object WeeklyRoutineHelper {
    val DAYS_OF_WEEK = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

    fun defaultEmptyWeek(): List<DayRoutine> {
        return DAYS_OF_WEEK.map { day ->
            DayRoutine(dayName = day, muscleFocus = "", exercises = emptyList())
        }
    }

    fun parseWeeklySchedule(json: String): List<DayRoutine> {
        if (json.isBlank()) return defaultEmptyWeek()
        return try {
            val array = JSONArray(json)
            val parsedDaysMap = mutableMapOf<String, DayRoutine>()

            for (i in 0 until array.length()) {
                val item = array.opt(i)
                if (item is JSONObject && item.has("dayName")) {
                    val dayName = item.optString("dayName")
                    val muscleFocus = item.optString("muscleFocus", "")
                    val exercisesArray = item.optJSONArray("exercises") ?: JSONArray()
                    val exercisesList = mutableListOf<RoutineExercise>()

                    for (j in 0 until exercisesArray.length()) {
                        val exObj = exercisesArray.getJSONObject(j)
                        exercisesList.add(
                            RoutineExercise(
                                exerciseName = exObj.optString("exerciseName"),
                                sets = exObj.optString("sets"),
                                reps = exObj.optString("reps"),
                                youtubeUrl = exObj.optString("youtubeUrl")
                            )
                        )
                    }
                    parsedDaysMap[dayName] = DayRoutine(
                        dayName = dayName,
                        muscleFocus = muscleFocus,
                        exercises = exercisesList
                    )
                }
            }

            // Si vino formato plano legacy de ejercicios sin día
            if (parsedDaysMap.isEmpty() && array.length() > 0) {
                val legacyExercises = RoutineExerciseHelper.parseExercises(json)
                return DAYS_OF_WEEK.mapIndexed { index, day ->
                    if (index == 0) {
                        DayRoutine(dayName = day, muscleFocus = "Entrenamiento General", exercises = legacyExercises)
                    } else {
                        DayRoutine(dayName = day, muscleFocus = "", exercises = emptyList())
                    }
                }
            }

            // Asegurar que siempre contenga los 6 días en orden
            DAYS_OF_WEEK.map { day ->
                parsedDaysMap[day] ?: DayRoutine(dayName = day, muscleFocus = "", exercises = emptyList())
            }
        } catch (e: Exception) {
            defaultEmptyWeek()
        }
    }

    fun weeklyScheduleToJson(days: List<DayRoutine>): String {
        val rootArray = JSONArray()
        for (day in days) {
            val dayObj = JSONObject()
            dayObj.put("dayName", day.dayName)
            dayObj.put("muscleFocus", day.muscleFocus)

            val exArray = JSONArray()
            for (ex in day.exercises) {
                val exObj = JSONObject()
                exObj.put("exerciseName", ex.exerciseName)
                exObj.put("sets", ex.sets)
                exObj.put("reps", ex.reps)
                exObj.put("youtubeUrl", ex.youtubeUrl)
                exArray.put(exObj)
            }
            dayObj.put("exercises", exArray)
            rootArray.put(dayObj)
        }
        return rootArray.toString()
    }
}

object RoutineExerciseHelper {
    fun parseExercises(json: String): List<RoutineExercise> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<RoutineExercise>()
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                if (obj.has("exerciseName")) {
                    list.add(
                        RoutineExercise(
                            exerciseName = obj.optString("exerciseName"),
                            sets = obj.optString("sets"),
                            reps = obj.optString("reps"),
                            youtubeUrl = obj.optString("youtubeUrl")
                        )
                    )
                } else if (obj.has("exercises")) {
                    // Viene como día anidado
                    val nested = obj.optJSONArray("exercises") ?: continue
                    for (k in 0 until nested.length()) {
                        val exObj = nested.getJSONObject(k)
                        list.add(
                            RoutineExercise(
                                exerciseName = exObj.optString("exerciseName"),
                                sets = exObj.optString("sets"),
                                reps = exObj.optString("reps"),
                                youtubeUrl = exObj.optString("youtubeUrl")
                            )
                        )
                    }
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun exercisesToJson(exercises: List<RoutineExercise>): String {
        val array = JSONArray()
        for (ex in exercises) {
            val obj = JSONObject()
            obj.put("exerciseName", ex.exerciseName)
            obj.put("sets", ex.sets)
            obj.put("reps", ex.reps)
            obj.put("youtubeUrl", ex.youtubeUrl)
            array.put(obj)
        }
        return array.toString()
    }
}

