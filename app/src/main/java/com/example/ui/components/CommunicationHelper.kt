package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.example.data.model.WeeklyRoutineHelper
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Helper de Comunicación & Gestión de Mensajería Oficial para Actitud Fuerte
 */
object CommunicationHelper {

    // ==========================================
    // DATOS OFICIALES DE PAGO MÓVIL
    // ==========================================
    const val PAGO_MOVIL_BANK = "Provincial"
    const val PAGO_MOVIL_ID = "17380859"
    const val PAGO_MOVIL_PHONE = "04145529674"
    const val PAGO_MOVIL_RATE_NOTE = "Los pagos se deben realizar a tasa BCV."

    // ==========================================
    // FIRMA DIGITAL & SELLO OFICIAL DEL ENTRENADOR
    // ==========================================
    const val OFFICIAL_TRAINER_NAME = "Alex Gómez"
    const val OFFICIAL_TRAINER_ROLE = "Entrenador Personal"
    const val OFFICIAL_TRAINER_BRAND = "ACTITUD FUERTE | SALA DE MUSCULACIÓN"

    const val OFFICIAL_TRAINER_SIGNATURE = "💪 Actitud Fuerte | 🏋️ Alex Gómez | 📲 Asesoría Técnica"

    /**
     * Devuelve el bloque formateado de Pago Móvil listo para incluir en WhatsApp
     */
    fun getPagoMovilDetailsText(): String {
        return """
            📲 DATOS PARA PAGO MÓVIL:
            Banco: $PAGO_MOVIL_BANK
            Cédula de identidad: $PAGO_MOVIL_ID
            Teléfono: $PAGO_MOVIL_PHONE
            $PAGO_MOVIL_RATE_NOTE
        """.trimIndent()
    }

    /**
     * Copia los datos de Pago Móvil al portapapeles del dispositivo
     */
    fun copyPagoMovilToClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(
            "Pago Móvil Actitud Fuerte",
            "Banco: $PAGO_MOVIL_BANK\nCédula: $PAGO_MOVIL_ID\nTeléfono: $PAGO_MOVIL_PHONE\n(Tasa BCV)"
        )
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Datos de Pago Móvil copiados al portapapeles", Toast.LENGTH_SHORT).show()
    }

    /**
     * Abre WhatsApp con el mensaje pre-cargado
     */
    fun sendWhatsApp(context: Context, rawPhone: String, message: String) {
        try {
            val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
            val encodedMsg = URLEncoder.encode(message, "UTF-8")
            val url = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendEmail(context: Context, toEmail: String, subject: String, body: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Enviar reporte por correo"))
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir la app de correo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Extrae solo el primer nombre del usuario (sin el apellido)
     */
    fun getFirstName(fullName: String): String {
        return fullName.trim().split("\\s+".toRegex()).firstOrNull()?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        } ?: "Atleta"
    }

    /**
     * Calcula la fecha de vencimiento según la modalidad de pago a partir de un timestamp:
     * - Semanal: 7 días
     * - Quincenal: 15 días
     * - Mensual: 29 días
     */
    fun calculateNextDueDate(fromTimestamp: Long, frequency: String): Date {
        val cal = Calendar.getInstance().apply { timeInMillis = fromTimestamp }
        val daysToAdd = when {
            frequency.contains("Semanal", ignoreCase = true) -> 7
            frequency.contains("Quincenal", ignoreCase = true) -> 15
            else -> 29 // Mensual
        }
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return cal.time
    }

    data class MembershipDueStatus(
        val nextDueDate: Date,
        val formattedDueDate: String,
        val daysRemaining: Int,
        val isExpired: Boolean,
        val isDueSoon: Boolean, // 0 or 1 days
        val statusLabel: String,
        val feeAmount: Double
    )

    /**
     * Calcula el estado de vencimiento del atleta contrastando la fecha de su último pago
     * (o su fecha de registro inicial) contra la fecha actual del sistema.
     */
    fun getClientMembershipDueStatus(client: Client, clientPayments: List<Payment>): MembershipDueStatus {
        val lastPayment = clientPayments.filter { it.clientId == client.id }.maxByOrNull { it.timestamp }
        val baseTimestamp = lastPayment?.timestamp ?: client.registrationTimestamp
        val nextDueDate = calculateNextDueDate(baseTimestamp, client.paymentFrequency)

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dueCal = Calendar.getInstance().apply {
            time = nextDueDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMillis = dueCal.timeInMillis - todayCal.timeInMillis
        val diffDays = (diffMillis / (1000L * 60L * 60L * 24L)).toInt()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val feeAmount = calculateFeeAmount(client.membershipPlan, client.paymentFrequency)

        val isExpired = diffDays < 0
        val isDueSoon = diffDays in 0..1

        val statusLabel = when {
            diffDays < -1 -> "Vencido hace ${-diffDays}d"
            diffDays == -1 -> "Venció ayer"
            diffDays == 0 -> "Vence hoy"
            diffDays == 1 -> "Vence mañana (1d)"
            else -> "Al día (${diffDays}d)"
        }

        return MembershipDueStatus(
            nextDueDate = nextDueDate,
            formattedDueDate = dateFormat.format(nextDueDate),
            daysRemaining = diffDays,
            isExpired = isExpired,
            isDueSoon = isDueSoon,
            statusLabel = statusLabel,
            feeAmount = feeAmount
        )
    }

    // =========================================================================
    // GAMIFICACIÓN Y RACHAS DE ASISTENCIA (STREAK 🔥)
    // =========================================================================
    data class StreakInfo(
        val weeksCount: Int,
        val isPerfectThisWeek: Boolean,
        val daysThisWeek: Int,
        val label: String,
        val motivationPhrase: String
    )

    /**
     * Calcula las semanas consecutivas en las que el atleta ha asistido a entrenar.
     * Si la semana actual está en curso y aún no asiste, mantiene la racha activa de semanas pasadas.
     */
    fun calculateWeeklyStreak(clientId: Long, allAttendances: List<Attendance>): StreakInfo {
        val clientAtts = allAttendances.filter { it.clientId == clientId }
        if (clientAtts.isEmpty()) {
            return StreakInfo(
                weeksCount = 0,
                isPerfectThisWeek = false,
                daysThisWeek = 0,
                label = "Sin racha",
                motivationPhrase = "¡Comienza tu racha esta semana!"
            )
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Obtener lunes de la semana actual a medianoche
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 1. Contar asistencias de la semana actual (lunes a sábado)
        var daysThisWeek = 0
        val currentWeekDays = mutableListOf<String>()
        val tempCal = cal.clone() as Calendar
        for (i in 0..5) {
            val dStr = dateFormat.format(tempCal.time)
            currentWeekDays.add(dStr)
            if (clientAtts.any { it.dateOnlyString == dStr }) {
                daysThisWeek++
            }
            tempCal.add(Calendar.DATE, 1)
        }

        val isPerfectThisWeek = daysThisWeek >= 5

        // 2. Evaluar semanas consecutivas hacia atrás
        var consecutiveWeeks = 0
        val hasAttendedCurrentWeek = daysThisWeek > 0

        if (hasAttendedCurrentWeek) {
            consecutiveWeeks = 1
        }

        // Revisar semanas pasadas (semana -1, -2, etc. hasta 52 semanas)
        val pastWeekCal = cal.clone() as Calendar
        var checkLimit = 52
        var continueStreak = true

        while (checkLimit > 0 && continueStreak) {
            checkLimit--
            pastWeekCal.add(Calendar.DATE, -7) // Retroceder 1 semana al lunes anterior

            var attendedInThisPastWeek = 0
            val pastDaysCal = pastWeekCal.clone() as Calendar
            for (i in 0..5) {
                val dStr = dateFormat.format(pastDaysCal.time)
                if (clientAtts.any { it.dateOnlyString == dStr }) {
                    attendedInThisPastWeek++
                }
                pastDaysCal.add(Calendar.DATE, 1)
            }

            if (attendedInThisPastWeek > 0) {
                consecutiveWeeks++
            } else {
                // Si en la semana actual aún no ha asistido (ej: lunes temprano), pero asistió la semana pasada,
                // no cortamos abruptamente sino que mantenemos la racha que traía.
                continueStreak = false
            }
        }

        val label = when {
            consecutiveWeeks >= 4 -> "🔥 $consecutiveWeeks SEMANAS IMPARABLE"
            consecutiveWeeks in 1..3 -> "🔥 $consecutiveWeeks SEM${if (consecutiveWeeks > 1) "S" else ""}"
            else -> "Inicia tu racha"
        }

        val motivationPhrase = when {
            consecutiveWeeks >= 4 -> "¡Llevas $consecutiveWeeks semanas de disciplina indestructible! Sigue así 🔥💪"
            consecutiveWeeks in 1..3 -> "¡Racha activa de $consecutiveWeeks semana${if (consecutiveWeeks > 1) "s" else ""}! La constancia construye resultados 🔥"
            daysThisWeek > 0 -> "¡Excelente inicio de semana! A por la racha completa 🚀"
            else -> "¡Cada entrenamiento cuenta! Activa tu fuego esta semana 🔥"
        }

        return StreakInfo(
            weeksCount = consecutiveWeeks,
            isPerfectThisWeek = isPerfectThisWeek,
            daysThisWeek = daysThisWeek,
            label = label,
            motivationPhrase = motivationPhrase
        )
    }

    /**
     * Mensaje de felicitación por racha de asistencia para fidelizar y motivar al atleta
     */
    fun buildStreakCelebrationMessage(client: Client, streakWeeks: Int): String {
        val firstName = getFirstName(client.fullName)
        return """
🔥 ¡FELICIDADES POR TU DISCIPLINA, $firstName! 🔥

Desde el equipo de Actitud Fuerte queremos reconocer tu constancia inquebrantable:
🏆 ¡Llevas $streakWeeks semanas consecutivas entrenando sin rendirte!

La constancia es lo que separa el deseo de los verdaderos resultados. ¡Sigue con ese fuego encendido y a superar tus límites! 💪⚡

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    fun calculateFeeAmount(plan: String, frequency: String): Double {
        val baseMonthly = if (plan.contains("12")) 12.0 else 20.0
        return when {
            frequency.contains("Semanal", ignoreCase = true) -> baseMonthly / 4.0
            frequency.contains("Quincenal", ignoreCase = true) -> baseMonthly / 2.0
            else -> baseMonthly
        }
    }

    // =========================================================================
    // 1. REPORTE DE DESEMPEÑO SEMANAL
    // =========================================================================
    fun buildWeeklyPerformanceReport(
        client: Client,
        weekRangeText: String, // Ej: "14/09 al 19/09 (Lunes a Sábado)"
        attendedDays: List<String>,
        missedDays: List<String>,
        aiObservation: String,
        streakWeeks: Int = 0
    ): String {
        val firstName = getFirstName(client.fullName)
        val attendedCount = attendedDays.size
        val missedCount = missedDays.size

        val attendedListStr = if (attendedDays.isNotEmpty()) {
            attendedDays.joinToString(", ")
        } else {
            "Ninguno registrado esta semana"
        }

        val missedListStr = if (missedDays.isNotEmpty()) {
            missedDays.joinToString(", ")
        } else {
            "Ninguno (Asistencia perfecta)"
        }

        val streakText = if (streakWeeks > 0) {
            "\n🔥 RACHA DE DISCIPLINA:\n¡Llevas $streakWeeks semana${if (streakWeeks > 1) "s" else ""} consecutiva${if (streakWeeks > 1) "s" else ""} entrenando sin fallar con Actitud Fuerte! 💪🔥\n"
        } else {
            ""
        }

        return """
🏋️ REPORTE DE DESEMPEÑO SEMANAL
Socio: $firstName
Semana: $weekRangeText
$streakText
Hola $firstName, aquí tienes el balance de tu asistencia esta semana:

✅ Días Asistidos ($attendedCount días):
$attendedListStr

❌ Días Inasistentes ($missedCount días):
$missedListStr

📝 Observaciones del Entrenador:
$aiObservation

¡Te esperamos con la mejor energía la próxima semana!

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    // =========================================================================
    // 2. REPORTE DE MEDICIÓN ANTROPOMÉTRICA
    // =========================================================================
    fun buildMeasurementReport(
        client: Client,
        current: Measurement,
        previous: Measurement? = null
    ): String {
        val firstName = getFirstName(client.fullName)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date(current.timestamp))

        fun formatVariation(curr: Double, prev: Double?, unit: String): String {
            if (prev == null) return "Variación: 0.0 $unit"
            val diff = curr - prev
            val sign = if (diff > 0) "+" else if (diff < 0) "-" else ""
            return "Variación: $sign${String.format(Locale.US, "%.1f", kotlin.math.abs(diff))} $unit"
        }

        val vWeight = formatVariation(current.weightKg, previous?.weightKg, "kg")
        val vBack = formatVariation(current.backCm, previous?.backCm, "cm")
        val vShoulders = formatVariation(current.shouldersCm, previous?.shouldersCm, "cm")
        val vArms = formatVariation(current.armsCm, previous?.armsCm, "cm")
        val vHips = formatVariation(current.hipsCm, previous?.hipsCm, "cm")

        val obs = if (current.trainerObservation.isNotBlank()) {
            current.trainerObservation
        } else if (current.aiObservation.isNotBlank()) {
            current.aiObservation
        } else {
            "Mantenimiento estable y control técnico establecido en sala. Continuamos intensificando las sobrecargas progresivas."
        }

        return """
📊 REPORTE DE MEDICIÓN ANTROPOMÉTRICA
Usuario: $firstName
Fecha de Medición: $dateStr

=====================
MEDICIÓN ANTROPOMÉTRICA
* Peso: ${String.format(Locale.US, "%.1f", current.weightKg)} KG ($vWeight)
* Espalda: ${String.format(Locale.US, "%.1f", current.backCm)} CM ($vBack)
* Hombros: ${String.format(Locale.US, "%.1f", current.shouldersCm)} CM ($vShoulders)
* Brazos: ${String.format(Locale.US, "%.1f", current.armsCm)} CM ($vArms)
* Caderas: ${String.format(Locale.US, "%.1f", current.hipsCm)} CM ($vHips)
=====================

📝 Observaciones del Entrenador:
$obs

¡Seguimos comprometidos con tu evolución física!

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    // =========================================================================
    // 2.1. REPORTE COMPARATIVO DE EVOLUCIÓN (ANTES VS AHORA)
    // =========================================================================
    // 3. DETALLES DEL RECIBO DE PAGO
    // =========================================================================
    fun buildPaymentReceiptMessage(
        client: Client,
        payment: Payment,
        receiptNumber: String = ""
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val paymentDateStr = dateFormat.format(Date(payment.timestamp))
        val nextDueDate = calculateNextDueDate(payment.timestamp, payment.frequency)
        val nextDueDateStr = dateFormat.format(nextDueDate)
        val effectiveReceiptNo = receiptNumber.ifBlank {
            "REC-${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())}-${(100 + (payment.id % 900))}"
        }

        val frequencyLabel = when {
            payment.frequency.contains("Semanal", ignoreCase = true) -> "Semanal ($${String.format(Locale.US, "%.0f", payment.amount)})"
            payment.frequency.contains("Quincenal", ignoreCase = true) -> "Quincenal ($${String.format(Locale.US, "%.0f", payment.amount)})"
            else -> "Mensual ($${String.format(Locale.US, "%.0f", payment.amount)})"
        }

        return """
Estimado/a ${client.fullName},

Confirmamos la recepción exitosa de su pago de nuestro servicio de Entrenador personal en sala de musculación. A continuación los detalles de su transacción:

====================================
DETALLES DEL RECIBO
====================================
Número de recibo: $effectiveReceiptNo
Fecha de Pago: $paymentDateStr
Concepto: Entrenador personal en sala de musculación.
Monto Pagado: $${String.format(Locale.US, "%.2f", payment.amount)}
Método de Pago: ${if (payment.method.isNotBlank()) payment.method else "Pago Móvil"}
Modalidad de pago: $frequencyLabel
Próxima Fecha de Vencimiento: $nextDueDateStr
====================================

📲 DATOS DE PAGO MÓVIL (Para futuros pagos):
Banco: $PAGO_MOVIL_BANK
Cédula de identidad: $PAGO_MOVIL_ID
Teléfono: $PAGO_MOVIL_PHONE
$PAGO_MOVIL_RATE_NOTE
====================================

¡Gracias por entrenar con nosotros y mantener tus metas activas!

Si tienes alguna duda o consulta, puedes acercarte a nosotros o responder a este mensaje.

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    // =========================================================================
    // 4. RECORDATORIO DE MEMBRESÍA PRÓXIMA A VENCER O VENCIDA
    // =========================================================================
    fun buildMembershipReminderMessage(
        client: Client,
        dueDateFormatted: String = "",
        amountDue: Double? = null
    ): String {
        val firstName = getFirstName(client.fullName)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val effectiveDueDate = if (dueDateFormatted.isNotBlank()) {
            dueDateFormatted
        } else {
            dateFormat.format(calculateNextDueDate(client.registrationTimestamp, client.paymentFrequency))
        }

        val fee = amountDue ?: calculateFeeAmount(client.membershipPlan, client.paymentFrequency)
        val planName = client.membershipPlan
        val frequencyName = client.paymentFrequency

        return """
Hola $firstName,

Te escribimos para recordarte que tu membresía ($planName) está próxima a vencer o ha vencido hoy:

📋 Membresía: $planName ($frequencyName)
📅 Fecha de corte: $effectiveDueDate
💵 Monto a pagar: $${String.format(Locale.US, "%.2f", fee)}

📲 DATOS PARA PAGO MÓVIL:
Banco: $PAGO_MOVIL_BANK
Cédula de identidad: $PAGO_MOVIL_ID
Teléfono: $PAGO_MOVIL_PHONE
$PAGO_MOVIL_RATE_NOTE

Para continuar con tu rutina de entrenamiento sin interrupciones, te invitamos a renovar el servicio y a consultar los métodos de pagos disponibles.

¡Te esperamos en el gym!

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    // =========================================================================
    // 5. MENSAJE DE BIENVENIDA (NUEVO INGRESO)
    // =========================================================================
    fun buildWelcomeMessage(
        client: Client,
        validUntilFormatted: String = "",
        feeAmount: Double? = null
    ): String {
        val firstName = getFirstName(client.fullName)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val validDate = if (validUntilFormatted.isNotBlank()) {
            validUntilFormatted
        } else {
            dateFormat.format(calculateNextDueDate(client.registrationTimestamp, client.paymentFrequency))
        }

        val fee = feeAmount ?: calculateFeeAmount(client.membershipPlan, client.paymentFrequency)
        val frequencyLabel = when {
            client.paymentFrequency.contains("Semanal", ignoreCase = true) -> "Semanal ($${String.format(Locale.US, "%.0f", fee)})"
            client.paymentFrequency.contains("Quincenal", ignoreCase = true) -> "Quincenal ($${String.format(Locale.US, "%.0f", fee)})"
            else -> "Mensual ($${String.format(Locale.US, "%.0f", fee)})"
        }

        return """
¡Bienvenido/a a este gran equipo! - Actitud Fuerte!

¡Hola $firstName!

Nos alegra darte la más cordial bienvenida a nuestra comunidad fitness.

Tu registro se ha completado con éxito:
• Plan ${client.membershipPlan}
• Tarifa mensual: $${String.format(Locale.US, "%.2f", fee)}
• Modalidad de pago: $frequencyLabel
• Membresía vigente hasta: $validDate

📲 DATOS PARA PAGO MÓVIL:
Banco: $PAGO_MOVIL_BANK
Cédula de identidad: $PAGO_MOVIL_ID
Teléfono: $PAGO_MOVIL_PHONE
$PAGO_MOVIL_RATE_NOTE

Recuerda que cuentas con asesoría de entrenadores, control de asistencia y seguimiento periódico de tus medidas y progreso físico en nuestro sistema.

¡A darlo todo en cada entrenamiento!

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    // =========================================================================
    // 6. RUTINAS DE ENTRENAMIENTO POR WHATSAPP (RUTINA DEL DÍA & SEMANAL)
    // =========================================================================

    /**
     * Retorna el nombre del día actual en español (Lunes a Domingo)
     */
    fun getTodayDayName(): String {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            Calendar.SUNDAY -> "Domingo"
            else -> "Lunes"
        }
    }

    /**
     * Construye el mensaje oficial de la RUTINA DEL DÍA para enviar por WhatsApp.
     * Solo envía la rutina correspondiente al día especificado (por defecto hoy).
     */
    fun buildDailyRoutineMessage(
        client: Client,
        assignedRoutine: Routine? = null,
        dayName: String = getTodayDayName(),
        customNotes: String = ""
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val todayDate = dateFormat.format(Date())

        val parsedSchedule = assignedRoutine?.let {
            WeeklyRoutineHelper.parseWeeklySchedule(it.exercisesJson)
        } ?: emptyList()

        val matchingDay = parsedSchedule.firstOrNull { it.dayName.equals(dayName, ignoreCase = true) }

        val routineContentBuilder = StringBuilder()
        val muscleFocus = matchingDay?.muscleFocus?.takeIf { it.isNotBlank() }

        if (dayName.equals("Domingo", ignoreCase = true)) {
            routineContentBuilder.append("🛌 *Día de Descanso Programado*\nHoy corresponde descanso y recuperación muscular activa. Hidrátate bien y prepárate para iniciar la semana con la mejor energía.")
        } else if (matchingDay != null && matchingDay.exercises.isNotEmpty()) {
            matchingDay.exercises.forEachIndexed { idx, ex ->
                val num = idx + 1
                routineContentBuilder.append("$num. *${ex.exerciseName}*\n")
                routineContentBuilder.append("   • Series: ${ex.sets} | Repeticiones: ${ex.reps}\n")
                if (ex.youtubeUrl.isNotBlank()) {
                    routineContentBuilder.append("   • Video de técnica: ${ex.youtubeUrl}\n")
                }
                if (idx < matchingDay.exercises.size - 1) routineContentBuilder.append("\n")
            }
        } else {
            // Extraer del texto plano de weeklyRoutine si existe la línea del día
            val dayLine = client.weeklyRoutine.lines().firstOrNull {
                it.trim().startsWith(dayName, ignoreCase = true)
            }
            if (dayLine != null && dayLine.isNotBlank()) {
                val cleanedText = dayLine.substringAfter(":").trim()
                routineContentBuilder.append("• *Enfoque del día:* ${cleanedText.ifBlank { dayLine }}")
            } else if (client.weeklyRoutine.isNotBlank()) {
                routineContentBuilder.append(client.weeklyRoutine.trim())
            } else {
                routineContentBuilder.append("• Rutina personalizada a realizar según indicaciones del entrenador en sala.")
            }
        }

        val focusHeader = if (!muscleFocus.isNullOrBlank()) {
            "\n🔥 Enfoque: *$muscleFocus*"
        } else ""

        val customNotesSection = if (customNotes.isNotBlank()) {
            "\n📝 Nota del Entrenador:\n$customNotes\n"
        } else ""

        return """
🏋️‍♂️ *RUTINA DEL DÍA - ACTITUD FUERTE* 🏋️‍♂️
Atleta: *${client.fullName}*
📅 Día: *${dayName.uppercase()} ($todayDate)*$focusHeader
🎯 Objetivo: *${client.mainObjective}*

📋 *EJERCICIOS PROGRAMADOS:*
${routineContentBuilder.toString().trim()}
$customNotesSection
💡 *Pautas Técnicas de Sala:*
• Calentamiento articular dinámico (5 a 10 min).
• Prioriza la técnica limpia y el rango completo antes del incremento de carga.
• Controla la respiración y anota tus pesos para sobrecarga progresiva.

⚡ _¡A darlo todo con disciplina y fuerza!_

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }

    /**
     * Construye el mensaje oficial de la RUTINA SEMANAL COMPLETA (Lunes a Sábado) para WhatsApp.
     */
    fun buildWeeklyRoutineMessage(
        client: Client,
        assignedRoutine: Routine? = null,
        customNotes: String = ""
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val parsedSchedule = assignedRoutine?.let {
            WeeklyRoutineHelper.parseWeeklySchedule(it.exercisesJson)
        } ?: emptyList()

        val bodyBuilder = StringBuilder()

        if (parsedSchedule.any { it.exercises.isNotEmpty() }) {
            parsedSchedule.forEach { day ->
                bodyBuilder.append("📅 *${day.dayName.uppercase()}*")
                if (day.muscleFocus.isNotBlank()) {
                    bodyBuilder.append(" (${day.muscleFocus})")
                }
                bodyBuilder.append(":\n")
                if (day.exercises.isEmpty()) {
                    bodyBuilder.append("   • Descanso activo o acondicionamiento libre\n\n")
                } else {
                    day.exercises.forEachIndexed { idx, ex ->
                        bodyBuilder.append("   ${idx + 1}. ${ex.exerciseName} (${ex.sets}x${ex.reps})\n")
                    }
                    bodyBuilder.append("\n")
                }
            }
        } else if (client.weeklyRoutine.isNotBlank()) {
            bodyBuilder.append(client.weeklyRoutine.trim())
            bodyBuilder.append("\n")
        } else {
            bodyBuilder.append("Plan semanal estructurado en sala de musculación.\n")
        }

        val customNotesSection = if (customNotes.isNotBlank()) {
            "\n📝 Nota del Entrenador:\n$customNotes\n"
        } else ""

        return """
🏋️‍♂️ *RUTINA SEMANAL COMPLETA - ACTITUD FUERTE* 🏋️‍♂️
Atleta: *${client.fullName}*
🎯 Objetivo: *${client.mainObjective}*
📅 Fecha: *$dateStr*

📋 *PLAN DE LA SEMANA (LUNES A SÁBADO):*
${bodyBuilder.toString().trim()}
$customNotesSection
⚡ _¡La disciplina constante es la que construye campeones!_

$OFFICIAL_TRAINER_SIGNATURE
        """.trimIndent()
    }
}
