package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.ui.components.CommunicationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GeminiCoachService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 1. Genera la Observación del Entrenador para el REPORTE DE MEDICIÓN ANTROPOMÉTRICA
     * basada en las variaciones de las métricas entre las medidas anteriores y las medidas recién tomadas.
     */
    suspend fun generateCoachObservation(
        clientInfo: Client,
        current: Measurement,
        previous: Measurement?
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val prompt = buildMeasurementPrompt(clientInfo, current, previous)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val aiResult = callGeminiApi(apiKey, prompt)
            if (!aiResult.isNullOrBlank()) {
                return@withContext aiResult
            }
        }

        // Motor inteligente local de ciencias del deporte (Fallback)
        return@withContext generateLocalMeasurementAnalysis(clientInfo, current, previous)
    }

    /**
     * 2. Genera la Observación del Entrenador para el REPORTE DE DESEMPEÑO SEMANAL
     * basada en la cantidad de días asistidos y de inasistencia, variando según el buen/mal rendimiento.
     */
    suspend fun generateWeeklyPerformanceObservation(
        clientInfo: Client,
        attendedCount: Int,
        missedCount: Int,
        attendedDays: List<String>,
        missedDays: List<String>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val prompt = buildPerformancePrompt(clientInfo, attendedCount, missedCount, attendedDays, missedDays)

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val aiResult = callGeminiApi(apiKey, prompt)
            if (!aiResult.isNullOrBlank()) {
                return@withContext aiResult
            }
        }

        // Motor inteligente local de rendimiento semanal (Fallback)
        return@withContext generateLocalPerformanceAnalysis(clientInfo, attendedCount, missedCount)
    }

    /**
     * 3. Genera un mensaje de Liderazgo & Cercanía para el Check-in semanal de los jueves (21:00 hs)
     */
    suspend fun generateLeadershipMessage(focusTitle: String, baselineExample: String): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            val prompt = """
                Actúa como el Entrenador Principal de la Sala de Musculación del gimnasio 'Actitud Fuerte'.
                Redacta un mensaje empático y de liderazgo para el check-in semanal de los jueves a las 21:00 hs para enviar por WhatsApp a un alumno.
                Enfoque: $focusTitle.
                Requisitos:
                1. Debe incluir exactamente la etiqueta {nombre} para que el sistema reemplace el nombre del socio.
                2. Tono: Cercano, empático, motivador, enfocado en cuidar al usuario y fortalecer el liderazgo del coach.
                3. Párrafos concisos, legibles y directos para WhatsApp.
                Ejemplo de referencia:
                $baselineExample
            """.trimIndent()

            val aiResult = callGeminiApi(apiKey, prompt)
            if (!aiResult.isNullOrBlank() && aiResult.contains("{nombre}")) {
                return@withContext aiResult
            }
        }

        return@withContext baselineExample
    }

    private fun callGeminiApi(apiKey: String, prompt: String): String? {
        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.9)
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseStr = response.body?.string() ?: ""
                val root = JSONObject(responseStr)
                val candidates = root.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCand = candidates.getJSONObject(0)
                    val content = firstCand.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text", "")
                        if (text.isNotBlank()) {
                            return text.trim()
                        }
                    }
                }
            } else {
                Log.w("GeminiCoachService", "Gemini API error ${response.code}: ${response.message}")
            }
        } catch (e: Exception) {
            Log.e("GeminiCoachService", "Exception calling Gemini API: ${e.message}")
        }
        return null
    }

    private fun buildMeasurementPrompt(client: Client, current: Measurement, previous: Measurement?): String {
        val firstName = CommunicationHelper.getFirstName(client.fullName)
        return buildString {
            append("Actúa como el Entrenador Principal de Sala de Musculación del gimnasio 'Actitud Fuerte'.\n")
            append("Redacta la sección 'Observaciones del Entrenador' para el REPORTE DE MEDICIÓN ANTROPOMÉTRICA del socio $firstName.\n")
            append("Debe ser un párrafo conciso (2 a 4 líneas), directo, técnico y motivador con actitud de fuerza.\n\n")
            append("Objetivo del Atleta: ${client.mainObjective}\n")
            if (previous != null) {
                val dW = current.weightKg - previous.weightKg
                val dB = current.backCm - previous.backCm
                val dS = current.shouldersCm - previous.shouldersCm
                val dA = current.armsCm - previous.armsCm
                val dH = current.hipsCm - previous.hipsCm
                append("Variaciones registradas respecto a la medición anterior:\n")
                append("- Peso: ${current.weightKg} kg (Variación: %+.1f kg, anterior: ${previous.weightKg} kg)\n".format(dW))
                append("- Espalda: ${current.backCm} cm (Variación: %+.1f cm)\n".format(dB))
                append("- Hombros: ${current.shouldersCm} cm (Variación: %+.1f cm)\n".format(dS))
                append("- Brazos: ${current.armsCm} cm (Variación: %+.1f cm)\n".format(dA))
                append("- Caderas: ${current.hipsCm} cm (Variación: %+.1f cm)\n".format(dH))
                append("\nEjemplo de tono esperado:\n")
                append("\"Mantenimiento estable en peso corporal (${current.weightKg} kg) respecto al control anterior. Para salir de la meseta y seguir avanzando hacia tus metas, intensificaremos las sobrecargas progresivas en tu rutina.\"")
            } else {
                append("Medición inicial de partida: Peso ${current.weightKg} kg, Espalda ${current.backCm} cm, Hombros ${current.shouldersCm} cm, Brazos ${current.armsCm} cm, Caderas ${current.hipsCm} cm.")
            }
        }
    }

    private fun buildPerformancePrompt(
        client: Client,
        attendedCount: Int,
        missedCount: Int,
        attendedDays: List<String>,
        missedDays: List<String>
    ): String {
        val firstName = CommunicationHelper.getFirstName(client.fullName)
        val totalDays = attendedCount + missedCount
        return buildString {
            append("Actúa como el Entrenador Principal de Sala de Musculación del gimnasio 'Actitud Fuerte'.\n")
            append("Redacta la sección 'Observaciones del Entrenador' para el REPORTE DE DESEMPEÑO SEMANAL del socio $firstName.\n")
            append("Datos de asistencia de la semana:\n")
            append("- Días asistidos: $attendedCount / $totalDays (${attendedDays.joinToString()})\n")
            append("- Días inasistentes: $missedCount / $totalDays (${missedDays.joinToString()})\n")
            append("Instrucciones estrictas:\n")
            append("1. Evalúa el rendimiento según la relación de asistencia (Excelente si 5-6 días, Bueno si 4 días, Regular si 3 días, Bajo si 1-2 días).\n")
            append("2. Si tuvo buen rendimiento, felicita su constancia y motiva a consolidar. Si tuvo inasistencias o bajo rendimiento, haz un llamado constructivo y firme a recuperar la disciplina y no perder las adaptaciones físicas.\n")
            append("3. Longitud: exactamente 2 a 4 líneas enérgicas y profesionales.")
        }
    }

    private fun generateLocalMeasurementAnalysis(client: Client, current: Measurement, previous: Measurement?): String {
        if (previous == null) {
            return "Punto de partida establecido en sala de musculación (${current.weightKg} kg). Estructura base registrada para el objetivo de ${client.mainObjective.lowercase()}. Enfocaremos las primeras semanas en adaptación neural y técnica estricta."
        }

        val dWeight = current.weightKg - previous.weightKg
        val dArms = current.armsCm - previous.armsCm
        val dShoulders = current.shouldersCm - previous.shouldersCm
        val dHips = current.hipsCm - previous.hipsCm

        return when {
            abs(dWeight) <= 0.4 && dArms >= 0 ->
                "Mantenimiento estable en peso corporal (${current.weightKg} kg) respecto al control anterior. Para salir de la meseta y seguir avanzando hacia tus metas, intensificaremos las sobrecargas progresivas en tu rutina."
            dWeight > 0.4 && (dArms > 0 || dShoulders > 0) ->
                "Excelente progresión anabólica (+%.1f kg). El aumento en brazos y torso confirma ganancia de masa magra efectiva. Continuamos aumentando la intensidad bajo control.".format(dWeight)
            dWeight < -0.4 && dHips <= 0 ->
                "Respuesta positiva en definición y recomposición corporal (-%.1f kg). Reducción efectiva en perímetro abdominal/cadera preservando la densidad muscular en sala.".format(abs(dWeight))
            else ->
                "Control corporal registrado con ${current.weightKg} kg. Ajustaremos las repeticiones efectivas y los descansos entre series para acelerar los resultados hacia tu meta de ${client.mainObjective.lowercase()}."
        }
    }

    private fun generateLocalPerformanceAnalysis(client: Client, attendedCount: Int, missedCount: Int): String {
        val total = (attendedCount + missedCount).coerceAtLeast(6)
        return when {
            attendedCount >= 5 ->
                "Desempeño Excelente. Has completado $attendedCount/$total días de entrenamiento esta semana. Mantienes un gran ritmo y constancia; te animamos a seguir con esa determinación para consolidar tus objetivos."
            attendedCount == 4 ->
                "Desempeño Bueno. Has completado 4/$total días esta semana. Tu constancia es notable, pero te motivamos a sumar ese día restante para maximizar tu progreso en sala de musculación."
            attendedCount == 3 ->
                "Desempeño Regular. Registras 3/$total días entrenados esta semana. El estímulo muscular requiere mayor frecuencia; organízate para retomar los 4 o 5 días y evitar frenar tus avances."
            attendedCount in 1..2 ->
                "Desempeño Bajo. Solo has asistido $attendedCount/$total días esta semana. La regularidad es la clave del cambio físico. ¡Reajusta tu agenda y venimos con todo la próxima semana!"
            else ->
                "Sin asistencia registrada esta semana. Recuerda que la disciplina en sala es fundamental para alcanzar tus objetivos. ¡Te esperamos para retomar con fuerza!"
        }
    }
}
