package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.idNumber
import com.example.data.model.isActive
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportReportHelper {

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, "Adjunto reporte generado desde el Sistema Administrativo Actitud Fuerte.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(intent, title).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\"", "\"\"")
        return if (clean.contains(",") || clean.contains("\n") || clean.contains("\"")) {
            "\"$clean\""
        } else {
            clean
        }
    }

    /**
     * 1. REPORTE DE SOCIOS (CSV / Excel)
     */
    fun exportClientsCsv(context: Context, clients: List<Client>) {
        try {
            val dir = getReportsDir(context)
            val file = File(dir, "Reporte_Socios_ActitudFuerte.csv")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val sb = java.lang.StringBuilder()
            sb.append("\uFEFF") // UTF-8 BOM for Excel compatibility
            sb.append("ID,Cédula,Nombre Completo,Teléfono,Correo,Plan,Frecuencia,Objetivo,Condición Médica,Notas Médicas,Estado,Fecha de Registro\n")

            clients.forEach { c ->
                val regDate = dateFormat.format(Date(c.registrationTimestamp))
                sb.append(
                    listOf(
                        c.id.toString(),
                        c.idNumber,
                        c.fullName,
                        c.phone,
                        c.email,
                        c.membershipPlan,
                        c.paymentFrequency,
                        c.mainObjective,
                        c.medicalCondition,
                        c.medicalNotes,
                        if (c.isActive) "Activo" else "Inactivo",
                        regDate
                    ).joinToString(",") { escapeCsv(it) }
                ).append("\n")
            }

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            Toast.makeText(context, "CSV de Socios generado con éxito (${clients.size} registros)", Toast.LENGTH_SHORT).show()
            shareFile(context, file, "text/csv", "Reporte de Socios (CSV / Excel)")
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando socios: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 2. REPORTE DE COBROS E INGRESOS (CSV)
     */
    fun exportPaymentsCsv(context: Context, payments: List<Payment>) {
        try {
            val dir = getReportsDir(context)
            val file = File(dir, "Reporte_Cobros_ActitudFuerte.csv")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val sb = java.lang.StringBuilder()
            sb.append("\uFEFF")
            sb.append("ID Cobro,Fecha y Hora,ID Cliente,Nombre Cliente,Monto ($),Plan,Modalidad,Método de Pago,Referencia\n")

            payments.forEach { p ->
                val dateStr = dateFormat.format(Date(p.timestamp))
                sb.append(
                    listOf(
                        p.id.toString(),
                        dateStr,
                        p.clientId.toString(),
                        p.clientName,
                        String.format(Locale.US, "%.2f", p.amount),
                        p.planName,
                        p.frequency,
                        p.method,
                        p.reference
                    ).joinToString(",") { escapeCsv(it) }
                ).append("\n")
            }

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            Toast.makeText(context, "CSV de Cobros generado con éxito (${payments.size} registros)", Toast.LENGTH_SHORT).show()
            shareFile(context, file, "text/csv", "Reporte de Cobros e Ingresos (CSV)")
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando cobros: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 3. HISTORIAL DE ASISTENCIAS (CSV)
     */
    fun exportAttendancesCsv(context: Context, attendances: List<Attendance>) {
        try {
            val dir = getReportsDir(context)
            val file = File(dir, "Reporte_Asistencias_ActitudFuerte.csv")

            val sb = java.lang.StringBuilder()
            sb.append("\uFEFF")
            sb.append("ID Asistencia,Fecha,Hora,ID Cliente,Nombre Cliente\n")

            attendances.forEach { a ->
                sb.append(
                    listOf(
                        a.id.toString(),
                        a.dateOnlyString,
                        a.timeOnlyString,
                        a.clientId.toString(),
                        a.clientName
                    ).joinToString(",") { escapeCsv(it) }
                ).append("\n")
            }

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            Toast.makeText(context, "CSV de Asistencias generado (${attendances.size} registros)", Toast.LENGTH_SHORT).show()
            shareFile(context, file, "text/csv", "Historial de Asistencias (CSV)")
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando asistencias: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 4. MEDICIONES ANTROPOMÉTRICAS (CSV)
     */
    fun exportMeasurementsCsv(context: Context, measurements: List<Measurement>, clients: List<Client>) {
        try {
            val dir = getReportsDir(context)
            val file = File(dir, "Reporte_Mediciones_ActitudFuerte.csv")
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            val sb = java.lang.StringBuilder()
            sb.append("\uFEFF")
            sb.append("ID Medición,Fecha,ID Cliente,Nombre Cliente,Peso (kg),Espalda (cm),Hombros (cm),Brazos (cm),Caderas (cm),Observación Entrenador,Observación IA\n")

            measurements.forEach { m ->
                val client = clients.firstOrNull { it.id == m.clientId }
                val clientName = client?.fullName ?: "Cliente #${m.clientId}"
                val dateStr = dateFormat.format(Date(m.timestamp))
                sb.append(
                    listOf(
                        m.id.toString(),
                        dateStr,
                        m.clientId.toString(),
                        clientName,
                        String.format(Locale.US, "%.1f", m.weightKg),
                        String.format(Locale.US, "%.1f", m.backCm),
                        String.format(Locale.US, "%.1f", m.shouldersCm),
                        String.format(Locale.US, "%.1f", m.armsCm),
                        String.format(Locale.US, "%.1f", m.hipsCm),
                        m.trainerObservation,
                        m.aiObservation
                    ).joinToString(",") { escapeCsv(it) }
                ).append("\n")
            }

            FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
            Toast.makeText(context, "CSV de Mediciones generado (${measurements.size} registros)", Toast.LENGTH_SHORT).show()
            shareFile(context, file, "text/csv", "Mediciones Antropométricas (CSV)")
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando mediciones: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 5. RESUMEN DE GESTIÓN PARA COMPARTIR (Texto formateado listo para WhatsApp)
     */
    fun shareManagementSummary(
        context: Context,
        clients: List<Client>,
        payments: List<Payment>,
        attendances: List<Attendance>,
        measurements: List<Measurement>,
        totalIncome: Double
    ) {
        try {
            val activeCount = clients.count { it.isActive }
            val plan12 = clients.count { it.membershipPlan.contains("12") }
            val plan20 = clients.count { it.membershipPlan.contains("20") }
            val nowStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val summary = """
📊 RESUMEN DE GESTIÓN ADMINISTRATIVA
🏋️ ACTITUD FUERTE - SALA DE MUSCULACIÓN
Fecha de Emisión: $nowStr

👥 COMUNIDAD DE ATLETAS:
• Total Atletas Registrados: ${clients.size}
• Atletas Activos: $activeCount
• Atletas Inactivos: ${clients.size - activeCount}
• Membresía $12: $plan12 atletas
• Membresía $20: $plan20 atletas

💵 GESTIÓN FINANCIERA:
• Recaudación Total Registrada: $${String.format(Locale.US, "%.2f", totalIncome)}
• Cantidad de Pagos Registrados: ${payments.size}

📋 ACTIVIDAD EN SALA:
• Registros de Asistencia Totales: ${attendances.size}
• Evaluaciones Antropométricas: ${measurements.size}

📲 PAGO MÓVIL OFICIAL:
${CommunicationHelper.getPagoMovilDetailsText()}

${CommunicationHelper.OFFICIAL_TRAINER_SIGNATURE}
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Resumen de Gestión Actitud Fuerte")
                putExtra(Intent.EXTRA_TEXT, summary)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(Intent.createChooser(intent, "Compartir Resumen de Gestión"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error compartiendo resumen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 6. REPORTE FINANCIERO Y DE CAJA (PDF) CON SELLO Y FIRMA DIGITAL
     */
    fun exportFinancialPdf(
        context: Context,
        payments: List<Payment>,
        totalIncome: Double
    ) {
        try {
            val dir = getReportsDir(context)
            val file = File(dir, "Reporte_Financiero_ActitudFuerte.pdf")

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard (595 x 842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 10f
            }

            val headerPaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 10f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 9f
            }

            val greenPaint = Paint().apply {
                color = AndroidColor.rgb(45, 140, 25)
                textSize = 13f
                isFakeBoldText = true
            }

            val linePaint = Paint().apply {
                color = AndroidColor.LTGRAY
                strokeWidth = 1f
            }

            var y = 45f

            // Encabezado
            canvas.drawText("ACTITUD FUERTE - SALA DE MUSCULACIÓN", 40f, y, titlePaint)
            y += 18f
            canvas.drawText("REPORTE FINANCIERO Y DE CAJA", 40f, y, titlePaint)
            y += 15f
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Fecha de emisión: $dateStr  |  Documento Administrativo Oficial", 40f, y, subtitlePaint)
            y += 12f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Balance
            canvas.drawText("TOTAL RECAUDADO: $${String.format(Locale.US, "%.2f", totalIncome)}", 40f, y, greenPaint)
            y += 16f
            canvas.drawText("Cantidad de transacciones: ${payments.size} pagos procesados", 40f, y, subtitlePaint)
            y += 18f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 18f

            // Cabecera de Tabla
            canvas.drawText("FECHA", 40f, y, headerPaint)
            canvas.drawText("SOCIO / ATLETA", 120f, y, headerPaint)
            canvas.drawText("PLAN", 280f, y, headerPaint)
            canvas.drawText("MÉTODO", 370f, y, headerPaint)
            canvas.drawText("MONTO", 490f, y, headerPaint)
            y += 7f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 14f

            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())

            // Limitamos a 20 para dejar espacio al pie de página con sello y firma
            val paymentsToShow = payments.take(20)
            paymentsToShow.forEach { p ->
                val pDate = dateFormat.format(Date(p.timestamp))
                val clientTruncated = if (p.clientName.length > 22) p.clientName.substring(0, 20) + ".." else p.clientName
                val planTruncated = if (p.planName.length > 14) p.planName.substring(0, 12) + ".." else p.planName

                canvas.drawText(pDate, 40f, y, bodyPaint)
                canvas.drawText(clientTruncated, 120f, y, bodyPaint)
                canvas.drawText(planTruncated, 280f, y, bodyPaint)
                canvas.drawText(p.method, 370f, y, bodyPaint)
                canvas.drawText("$${String.format(Locale.US, "%.2f", p.amount)}", 490f, y, bodyPaint)

                y += 15f
                if (y > 640f) return@forEach
            }

            // Pie de página profesional con Sello Oficial y Firma Digital del Entrenador
            val sealBoxY = 660f
            val sealRect = RectF(40f, sealBoxY, 555f, sealBoxY + 115f)

            val boxBgPaint = Paint().apply {
                color = AndroidColor.rgb(245, 248, 242)
                style = Paint.Style.FILL
            }
            val boxBorderPaint = Paint().apply {
                color = AndroidColor.rgb(100, 160, 40)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            val sealHeaderPaint = Paint().apply {
                color = AndroidColor.rgb(20, 25, 20)
                textSize = 10f
                isFakeBoldText = true
            }
            val sealTrainerPaint = Paint().apply {
                color = AndroidColor.rgb(35, 110, 25)
                textSize = 9.5f
                isFakeBoldText = true
            }
            val sealDetailPaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 8.5f
            }
            val stampBorderPaint = Paint().apply {
                color = AndroidColor.rgb(45, 130, 30)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val stampTextPaint = Paint().apply {
                color = AndroidColor.rgb(45, 130, 30)
                textSize = 7f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            canvas.drawRoundRect(sealRect, 8f, 8f, boxBgPaint)
            canvas.drawRoundRect(sealRect, 8f, 8f, boxBorderPaint)

            var textY = sealBoxY + 18f
            canvas.drawText("💪 ACTITUD FUERTE | SALA DE MUSCULACIÓN", 55f, textY, sealHeaderPaint)
            textY += 15f
            canvas.drawText("🏋️ Entrenador Personal: Alex Gómez", 55f, textY, sealTrainerPaint)
            textY += 13f
            canvas.drawText("📲 Asesoría Técnica & Control de Rendimiento Físico", 55f, textY, sealDetailPaint)
            textY += 13f
            canvas.drawText("🛡️ Registro Oficial Emitido por el Sistema", 55f, textY, sealDetailPaint)
            textY += 13f
            canvas.drawText("Datos de Pago Móvil Oficial: Banco Provincial - Cédula 17380859 - Telf 04145529674 (Tasa BCV)", 55f, textY, sealDetailPaint)

            // Sello digital a la derecha
            val stampRect = RectF(435f, sealBoxY + 12f, 542f, sealBoxY + 74f)
            canvas.drawRoundRect(stampRect, 6f, 6f, stampBorderPaint)
            canvas.drawText("SELLO OFICIAL", 488f, sealBoxY + 28f, stampTextPaint)
            canvas.drawText("ACTITUD FUERTE", 488f, sealBoxY + 41f, stampTextPaint)
            canvas.drawText("FIRMA VERIFICADA", 488f, sealBoxY + 54f, stampTextPaint)
            canvas.drawText("ALEX GÓMEZ", 488f, sealBoxY + 66f, stampTextPaint)

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()

            Toast.makeText(context, "PDF Financiero con Sello Oficial generado con éxito", Toast.LENGTH_SHORT).show()
            shareFile(context, file, "application/pdf", "Reporte Financiero y de Caja (PDF)")
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 7. FICHA DE EVALUACIÓN ANTROPOMÉTRICA DEL ATLETA (PDF) CON FIRMA Y SELLO OFICIAL
     */
    fun exportClientAnthropometricPdf(
        context: Context,
        client: Client,
        measurements: List<Measurement>
    ) {
        try {
            val dir = getReportsDir(context)
            val cleanName = client.fullName.replace("\\s+".toRegex(), "_")
            val file = File(dir, "Ficha_Antropometrica_${cleanName}.pdf")

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 17f
                isFakeBoldText = true
            }

            val subtitlePaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 10f
            }

            val headerPaint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 9.5f
                isFakeBoldText = true
            }

            val bodyPaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 9f
            }

            val highlightPaint = Paint().apply {
                color = AndroidColor.rgb(35, 110, 25)
                textSize = 11f
                isFakeBoldText = true
            }

            val linePaint = Paint().apply {
                color = AndroidColor.LTGRAY
                strokeWidth = 1f
            }

            var y = 45f

            // Encabezado
            canvas.drawText("ACTITUD FUERTE - SALA DE MUSCULACIÓN", 40f, y, titlePaint)
            y += 18f
            canvas.drawText("FICHA TÉCNICA Y DE EVALUACIÓN ANTROPOMÉTRICA", 40f, y, titlePaint)
            y += 15f
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Fecha de emisión: $dateStr  |  Expediente de Control Físico", 40f, y, subtitlePaint)
            y += 12f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Datos del Atleta
            canvas.drawText("DATOS DEL SOCIO / ATLETA:", 40f, y, headerPaint)
            y += 14f
            canvas.drawText("Nombre Completo: ${client.fullName}", 40f, y, bodyPaint)
            canvas.drawText("Cédula: ${if (client.idNumber.isNotBlank()) client.idNumber else "No reg."}", 320f, y, bodyPaint)
            y += 14f
            canvas.drawText("Teléfono: ${client.phone}", 40f, y, bodyPaint)
            canvas.drawText("Membresía: ${client.membershipPlan} (${client.paymentFrequency})", 320f, y, bodyPaint)
            y += 14f
            canvas.drawText("Objetivo Principal: ${client.mainObjective}", 40f, y, highlightPaint)
            y += 14f
            if (client.medicalCondition.isNotBlank() || client.medicalNotes.isNotBlank()) {
                val med = listOf(client.medicalCondition, client.medicalNotes).filter { it.isNotBlank() }.joinToString(" - ")
                canvas.drawText("Condición / Observación Médica: $med", 40f, y, bodyPaint)
                y += 14f
            }
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Tabla de Mediciones
            canvas.drawText("HISTORIAL DE CONTROL ANTROPOMÉTRICO (${measurements.size} registros)", 40f, y, headerPaint)
            y += 14f

            canvas.drawText("FECHA", 40f, y, headerPaint)
            canvas.drawText("PESO", 120f, y, headerPaint)
            canvas.drawText("ESPALDA", 185f, y, headerPaint)
            canvas.drawText("HOMBROS", 255f, y, headerPaint)
            canvas.drawText("BRAZOS", 330f, y, headerPaint)
            canvas.drawText("CADERAS", 395f, y, headerPaint)
            canvas.drawText("OBSERVACIÓN TÉCNICA", 460f, y, headerPaint)
            y += 6f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 15f

            val dateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val sortedMeasurements = measurements.sortedByDescending { it.timestamp }.take(18)

            if (sortedMeasurements.isEmpty()) {
                canvas.drawText("Sin evaluaciones registradas hasta la fecha.", 40f, y, bodyPaint)
                y += 20f
            } else {
                sortedMeasurements.forEach { m ->
                    val mDate = dateFormat.format(Date(m.timestamp))
                    val obs = when {
                        m.trainerObservation.isNotBlank() -> m.trainerObservation
                        m.aiObservation.isNotBlank() -> m.aiObservation
                        else -> "Control OK"
                    }
                    val obsTruncated = if (obs.length > 18) obs.substring(0, 16) + ".." else obs

                    canvas.drawText(mDate, 40f, y, bodyPaint)
                    canvas.drawText("${m.weightKg} kg", 120f, y, bodyPaint)
                    canvas.drawText("${m.backCm} cm", 185f, y, bodyPaint)
                    canvas.drawText("${m.shouldersCm} cm", 255f, y, bodyPaint)
                    canvas.drawText("${m.armsCm} cm", 330f, y, bodyPaint)
                    canvas.drawText("${m.hipsCm} cm", 395f, y, bodyPaint)
                    canvas.drawText(obsTruncated, 460f, y, bodyPaint)

                    y += 15f
                    if (y > 640f) return@forEach
                }
            }

            // Firma Digital del Entrenador & Sello Oficial
            val sealBoxY = 660f
            val sealRect = RectF(40f, sealBoxY, 555f, sealBoxY + 115f)

            val boxBgPaint = Paint().apply {
                color = AndroidColor.rgb(245, 248, 242)
                style = Paint.Style.FILL
            }
            val boxBorderPaint = Paint().apply {
                color = AndroidColor.rgb(100, 160, 40)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            val sealHeaderPaint = Paint().apply {
                color = AndroidColor.rgb(20, 25, 20)
                textSize = 10f
                isFakeBoldText = true
            }
            val sealTrainerPaint = Paint().apply {
                color = AndroidColor.rgb(35, 110, 25)
                textSize = 9.5f
                isFakeBoldText = true
            }
            val sealDetailPaint = Paint().apply {
                color = AndroidColor.DKGRAY
                textSize = 8.5f
            }
            val stampBorderPaint = Paint().apply {
                color = AndroidColor.rgb(45, 130, 30)
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val stampTextPaint = Paint().apply {
                color = AndroidColor.rgb(45, 130, 30)
                textSize = 7f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            canvas.drawRoundRect(sealRect, 8f, 8f, boxBgPaint)
            canvas.drawRoundRect(sealRect, 8f, 8f, boxBorderPaint)

            var textY = sealBoxY + 18f
            canvas.drawText("💪 ACTITUD FUERTE | SALA DE MUSCULACIÓN", 55f, textY, sealHeaderPaint)
            textY += 15f
            canvas.drawText("🏋️ Entrenador Personal: Alex Gómez", 55f, textY, sealTrainerPaint)
            textY += 13f
            canvas.drawText("📲 Asesoría Técnica & Control de Rendimiento Físico", 55f, textY, sealDetailPaint)
            textY += 13f
            canvas.drawText("🛡️ Registro Oficial Emitido por el Sistema", 55f, textY, sealDetailPaint)
            textY += 13f
            canvas.drawText("Documento emitido para ${client.fullName} - Confidencial & Oficial", 55f, textY, sealDetailPaint)

            // Sello digital a la derecha
            val stampRect = RectF(435f, sealBoxY + 12f, 542f, sealBoxY + 74f)
            canvas.drawRoundRect(stampRect, 6f, 6f, stampBorderPaint)
            canvas.drawText("SELLO OFICIAL", 488f, sealBoxY + 28f, stampTextPaint)
            canvas.drawText("ACTITUD FUERTE", 488f, sealBoxY + 41f, stampTextPaint)
            canvas.drawText("FIRMA VERIFICADA", 488f, sealBoxY + 54f, stampTextPaint)
            canvas.drawText("ALEX GÓMEZ", 488f, sealBoxY + 66f, stampTextPaint)

            pdfDocument.finishPage(page)

            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()

            Toast.makeText(context, "Ficha PDF de ${client.fullName} generada con éxito", Toast.LENGTH_SHORT).show()
            shareFile(context, file, "application/pdf", "Ficha Antropométrica - ${client.fullName} (PDF)")
        } catch (e: Exception) {
            Toast.makeText(context, "Error exportando Ficha PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
