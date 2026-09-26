package com.example.ui.components

import android.content.Context
import android.content.SharedPreferences
import com.example.data.ai.GeminiCoachService
import com.example.data.model.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LeadershipFocusItem(
    val id: String,
    val title: String,
    val baselineTemplate: String
)

object LeadershipFocusManager {

    private const val PREFS_NAME = "actitud_fuerte_leadership_prefs"
    private const val KEY_LAST_RENEWAL = "key_last_renewal_timestamp"
    private const val RENEWAL_CYCLE_MS = 30L * 24 * 60 * 60 * 1000L // 30 días

    val FOCUS_ITEMS = listOf(
        LeadershipFocusItem(
            id = "sensaciones",
            title = "Sensaciones físicas y cargas",
            baselineTemplate = """
Hola {nombre}, buenas noches 👋

Te escribo porque quiero saber cómo te sientes hasta hoy con el entrenamiento que hemos aplicado estos últimos días.

¿Tienes alguna pregunta? ¿Dudas? ¿Cómo notas que tu cuerpo lo ha tomado?

Recuerda que descansar bien esta noche es clave para asimilar el esfuerzo. ¡Estoy atento por si necesitamos ajustar algo para mañana!
            """.trimIndent()
        ),
        LeadershipFocusItem(
            id = "dudas_posturas",
            title = "Dudas, posturas y confianza",
            baselineTemplate = """
Hola {nombre}, ¡buenas noches! 👋

Hago una pausa este jueves para saludarte y preguntarte: ¿tienes alguna duda sobre los ejercicios, técnicas o cargas que estuvimos practicando esta semana?

Mi compromiso como tu entrenador es que entrenes con absoluta confianza, comodidad y sin riesgo de lesión. ¡Cuentas conmigo al 100%!
            """.trimIndent()
        ),
        LeadershipFocusItem(
            id = "motivacion_cierre",
            title = "Motivación y cierre de semana",
            baselineTemplate = """
Hola {nombre}, buenas noches 👋

Ya casi completamos la semana y quería reconocerte el gran compromiso y disciplina que estás demostrando. ¡Tu constancia se nota!

¿Cómo te sientes de energía y disposición para la sesión de mañana? ¡Vamos a cerrar la semana con la mejor actitud fuerte! 💪🔥
            """.trimIndent()
        ),
        LeadershipFocusItem(
            id = "cuidado_descanso",
            title = "Cuidado, descanso e hidratación",
            baselineTemplate = """
Hola {nombre}, ¡buenas noches! 👋

Paso a saludarte y recordarte que una gran parte de tu evolución ocurre mientras descansas e hidratas tu cuerpo después de darlo todo en el gimnasio.

¿Cómo vienes de energía y recuperación muscular hoy? Si notas alguna fatiga especial, mañana podemos modular la intensidad sin problema. ¡Que tengas un excelente descanso!
            """.trimIndent()
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getMessageTemplate(context: Context, focusId: String): String {
        val prefs = getPrefs(context)
        val defaultItem = FOCUS_ITEMS.firstOrNull { it.id == focusId } ?: FOCUS_ITEMS[0]
        return prefs.getString("focus_$focusId", defaultItem.baselineTemplate) ?: defaultItem.baselineTemplate
    }

    fun saveMessageTemplate(context: Context, focusId: String, message: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString("focus_$focusId", message).apply()
    }

    fun getLastRenewalTimestamp(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_RENEWAL, 0L)
    }

    fun getDaysUntilNextRenewal(context: Context): Int {
        val last = getLastRenewalTimestamp(context)
        if (last == 0L) return 30
        val elapsed = System.currentTimeMillis() - last
        val remainingMs = (RENEWAL_CYCLE_MS - elapsed).coerceAtLeast(0L)
        return (remainingMs / (24 * 60 * 60 * 1000L)).toInt()
    }

    suspend fun renewAllWithAi(context: Context, geminiService: GeminiCoachService): Boolean = withContext(Dispatchers.IO) {
        var anySuccess = false
        val prefs = getPrefs(context)
        val editor = prefs.edit()

        for (item in FOCUS_ITEMS) {
            try {
                val newMsg = geminiService.generateLeadershipMessage(item.title, item.baselineTemplate)
                if (newMsg.isNotBlank() && newMsg.contains("{nombre}")) {
                    editor.putString("focus_${item.id}", newMsg)
                    anySuccess = true
                }
            } catch (e: Exception) {
                // Keep existing or baseline
            }
        }

        editor.putLong(KEY_LAST_RENEWAL, System.currentTimeMillis())
        editor.apply()
        return@withContext anySuccess
    }

    fun formatMessageForClient(template: String, client: Client): String {
        val firstName = CommunicationHelper.getFirstName(client.fullName)
        return template.replace("{nombre}", firstName)
    }
}
