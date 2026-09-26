package com.example.ui.screens

import android.graphics.Paint
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.ExportReportHelper
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ProgressMetric(val label: String, val unit: String, val tag: String) {
    BIOMETRIC_DUAL("Biometría Dual (Músculo vs Grasa)", "cm / pts", "⚡ BIOMETRÍA NEÓN DUAL"),
    WEIGHT("Peso Corporal", "kg", "PESO"),
    ARMS("Brazos", "cm", "BRAZOS"),
    BACK("Espalda", "cm", "ESPALDA"),
    SHOULDERS("Hombros", "cm", "HOMBROS"),
    HIPS("Caderas", "cm", "CADERAS");

    fun extractValue(m: Measurement): Double = when (this) {
        BIOMETRIC_DUAL -> (m.armsCm * 1.4 + m.backCm * 0.8 + m.shouldersCm * 0.8) / 3.0
        WEIGHT -> m.weightKg
        ARMS -> m.armsCm
        BACK -> m.backCm
        SHOULDERS -> m.shouldersCm
        HIPS -> m.hipsCm
    }

    fun extractSecondaryValue(m: Measurement): Double = m.hipsCm
}

@Composable
fun ProgressScreen(
    clients: List<Client>,
    allMeasurements: List<Measurement>,
    onSaveMeasurement: (Measurement, Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedClientId by remember(clients) {
        mutableStateOf(clients.firstOrNull()?.id ?: 0L)
    }
    var showNewMeasurementDialog by remember { mutableStateOf(false) }
    var selectedMetric by remember { mutableStateOf(ProgressMetric.BIOMETRIC_DUAL) }

    val selectedClient = clients.firstOrNull { it.id == selectedClientId }
    val clientMeasurements = allMeasurements.filter { it.clientId == selectedClientId }
    val previousMeasurement = clientMeasurements.firstOrNull()

    val chronologicalMeasurements = remember(clientMeasurements) {
        clientMeasurements.sortedBy { it.timestamp }
    }
    val initialMeasurement = chronologicalMeasurements.firstOrNull()
    val latestMeasurement = chronologicalMeasurements.lastOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("EVOLUCIÓN & ANTROPOMETRÍA", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Progreso Muscular", color = TextLightGray, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("Análisis comparativo con IA", color = TextMuted, fontSize = 12.sp)
                }

                if (selectedClient != null) {
                    Button(
                        onClick = { showNewMeasurementDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("TOMAR MEDIDAS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("SELECCIONA UN ATLETA:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Athlete selector horizontal chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clients) { c ->
                    val isSelected = c.id == selectedClientId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LimeGreen else SurfaceElevated)
                            .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedClientId = c.id }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = c.fullName,
                            color = if (isSelected) Color.Black else TextLightGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (selectedClient == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay atletas registrados en el sistema.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            // Athlete Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 10.dp)
                            ) {
                                Text(
                                    text = selectedClient.fullName,
                                    color = TextLightGray,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: ${selectedClient.accessId} • Objetivo: ${selectedClient.mainObjective}",
                                    color = LimeGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, if (clientMeasurements.isNotEmpty()) LimeGreen.copy(alpha = 0.35f) else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = if (clientMeasurements.isNotEmpty()) LimeGreen else TextMuted,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "${clientMeasurements.size} ${if (clientMeasurements.size == 1) "toma" else "tomas"}",
                                        color = if (clientMeasurements.isNotEmpty()) LimeGreen else TextLightGray,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            if (clientMeasurements.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No hay medidas registradas para ${selectedClient.fullName}.", color = TextMuted, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { showNewMeasurementDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("TOMAR PRIMERA MEDICIÓN", fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            } else {
                // 1. Gráfico Interactivo de Evolución Temporal
                item {
                    ProgressEvolutionChart(
                        chronologicalMeasurements = chronologicalMeasurements,
                        selectedMetric = selectedMetric,
                        onSelectMetric = { selectedMetric = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Historial Detallado de Mediciones
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HISTORIAL DE MEDICIONES CORPORALES", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        if (clientMeasurements.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1B2416))
                                    .border(1.dp, Color(0xFF2E4025), RoundedCornerShape(6.dp))
                                    .clickable {
                                        ExportReportHelper.exportClientAnthropometricPdf(context, selectedClient, clientMeasurements)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FICHA PDF", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(clientMeasurements) { m ->
                    val dateStr = SimpleDateFormat("dd MMMM yyyy • hh:mm a", Locale("es", "ES")).format(Date(m.timestamp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dateStr, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Actitud Fuerte AI", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Grid of 5 measurements
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricChip("PESO", "${m.weightKg} kg")
                                MetricChip("ESPALDA", "${m.backCm} cm")
                                MetricChip("HOMBROS", "${m.shouldersCm} cm")
                                MetricChip("BRAZOS", "${m.armsCm} cm")
                                MetricChip("CADERAS", "${m.hipsCm} cm")
                            }

                            val obs = m.trainerObservation.ifBlank { m.aiObservation }
                            if (obs.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Observación técnica del entrenador:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = obs,
                                    color = TextLightGray,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (showNewMeasurementDialog && selectedClient != null) {
        NewMeasurementDialog(
            client = selectedClient,
            previousMeasurement = previousMeasurement,
            onDismiss = { showNewMeasurementDialog = false },
            onSaveMeasurement = { m, wa, em ->
                onSaveMeasurement(m, wa, em)
                showNewMeasurementDialog = false
                Toast.makeText(context, "Medidas guardadas correctamente", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Tarjeta de Transformación Real "Antes vs Ahora"
 */
@Composable
fun BeforeAfterTransformationCard(
    client: Client,
    initial: Measurement,
    latest: Measurement,
    onShareWhatsApp: () -> Unit,
    onExportPdf: () -> Unit
) {
    val daysBetween = remember(initial.timestamp, latest.timestamp) {
        ((latest.timestamp - initial.timestamp) / (1000L * 60L * 60L * 24L)).coerceAtLeast(0)
    }

    val initDateStr = remember(initial.timestamp) {
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(initial.timestamp))
    }
    val latestDateStr = remember(latest.timestamp) {
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(latest.timestamp))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💪", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "TRANSFORMACIÓN: ANTES VS AHORA",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$initDateStr ➔ $latestDateStr ($daysBetween días)",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$daysBetween DÍAS",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filas comparativas
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                BeforeAfterMetricRow("PESO CORPORAL", initial.weightKg, latest.weightKg, "kg")
                BeforeAfterMetricRow("BRAZOS", initial.armsCm, latest.armsCm, "cm")
                BeforeAfterMetricRow("ESPALDA", initial.backCm, latest.backCm, "cm")
                BeforeAfterMetricRow("HOMBROS", initial.shouldersCm, latest.shouldersCm, "cm")
                BeforeAfterMetricRow("CADERAS", initial.hipsCm, latest.hipsCm, "cm")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de acción: WhatsApp y Ficha PDF Oficial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón WhatsApp con firma digital oficial
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF25D366))
                        .clickable { onShareWhatsApp() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📲", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ENVIAR POR WA",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Botón Ficha PDF con Sello Oficial del Entrenador
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B2416))
                        .border(1.dp, Color(0xFF2E4025), RoundedCornerShape(8.dp))
                        .clickable { onExportPdf() }
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "FICHA PDF (SELLO)",
                        color = LimeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun BeforeAfterMetricRow(
    label: String,
    initialVal: Double,
    latestVal: Double,
    unit: String
) {
    val diff = latestVal - initialVal
    val diffFormatted = String.format(Locale.US, "%.1f", kotlin.math.abs(diff))
    val isPositive = diff > 0.05
    val isNegative = diff < -0.05
    val sign = if (isPositive) "+" else if (isNegative) "-" else ""

    val deltaBg = when {
        isPositive -> Color(0xFF193818)
        isNegative -> Color(0xFF382312)
        else -> SurfaceDark
    }
    val deltaTextColor = when {
        isPositive -> LimeGreen
        isNegative -> Color(0xFFFB923C)
        else -> TextMuted
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextLightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.1f)
        )

        Row(
            modifier = Modifier.weight(1.4f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${String.format(Locale.US, "%.1f", initialVal)} $unit",
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text("➔", color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "${String.format(Locale.US, "%.1f", latestVal)} $unit",
                color = Color.White,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(deltaBg)
                .border(1.dp, deltaTextColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = if (diff == 0.0) "0.0 $unit" else "$sign$diffFormatted $unit",
                color = deltaTextColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Gráfico Interactivo de Evolución Temporal (Canvas Nativo)
 */
@Composable
fun ProgressEvolutionChart(
    chronologicalMeasurements: List<Measurement>,
    selectedMetric: ProgressMetric,
    onSelectMetric: (ProgressMetric) -> Unit
) {
    var selectedPointIndex by remember(chronologicalMeasurements, selectedMetric) {
        mutableStateOf<Int?>(null)
    }

    val dataPoints = remember(chronologicalMeasurements, selectedMetric) {
        chronologicalMeasurements.map { m ->
            Pair(m, selectedMetric.extractValue(m))
        }
    }

    val baselineVal = dataPoints.firstOrNull()?.second ?: 0.0
    val latestVal = dataPoints.lastOrNull()?.second ?: 0.0
    val totalDelta = latestVal - baselineVal
    val isPositiveDelta = totalDelta > 0.05
    val isNegativeDelta = totalDelta < -0.05
    val sign = if (isPositiveDelta) "+" else if (isNegativeDelta) "-" else ""
    val deltaFormatted = String.format(Locale.US, "%.1f", kotlin.math.abs(totalDelta))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header con ícono
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CURVA DE EVOLUCIÓN TEMPORAL",
                        color = LimeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selector horizontal de métrica
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(ProgressMetric.values()) { metric ->
                    val isSelected = metric == selectedMetric
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LimeGreen else SurfaceElevated)
                            .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onSelectMetric(metric)
                                selectedPointIndex = null
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${metric.tag} (${metric.unit})",
                            color = if (isSelected) Color.Black else TextLightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Resumen de la métrica activa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(selectedMetric.label.uppercase(), color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", latestVal),
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = selectedMetric.unit,
                            color = LimeGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (dataPoints.size >= 2) {
                    val deltaBg = when {
                        isPositiveDelta -> Color(0xFF193818)
                        isNegativeDelta -> Color(0xFF382312)
                        else -> SurfaceElevated
                    }
                    val deltaColor = when {
                        isPositiveDelta -> LimeGreen
                        isNegativeDelta -> Color(0xFFFB923C)
                        else -> TextMuted
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(deltaBg)
                            .border(1.dp, deltaColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    isPositiveDelta -> Icons.Default.TrendingUp
                                    isNegativeDelta -> Icons.Default.TrendingDown
                                    else -> Icons.Default.TrendingFlat
                                },
                                contentDescription = null,
                                tint = deltaColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Total: $sign$deltaFormatted ${selectedMetric.unit}",
                                color = deltaColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            if (dataPoints.size < 2) {
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Registra al menos 2 tomas de medidas para trazar la curva de progreso.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else {
                val isDual = selectedMetric == ProgressMetric.BIOMETRIC_DUAL
                val secondaryPoints = remember(chronologicalMeasurements, selectedMetric) {
                    if (isDual) {
                        chronologicalMeasurements.map { m ->
                            Pair(m, selectedMetric.extractSecondaryValue(m))
                        }
                    } else emptyList()
                }

                val activePoint = selectedPointIndex?.let { dataPoints.getOrNull(it) } ?: dataPoints.last()
                val activeSecPoint = if (isDual) (selectedPointIndex?.let { secondaryPoints.getOrNull(it) } ?: secondaryPoints.lastOrNull()) else null
                val activeDateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(activePoint.first.timestamp))

                Spacer(modifier = Modifier.height(8.dp))

                // Leyenda e Inspección interactiva
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Inspección: $activeDateStr",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(LimeGreen))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isDual) "Músculo: ${String.format(Locale.US, "%.1f", activePoint.second)}" else "${String.format(Locale.US, "%.1f", activePoint.second)} ${selectedMetric.unit}",
                                    color = LimeGreen,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (isDual && activeSecPoint != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFBBF24)))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Grasa: ${String.format(Locale.US, "%.1f", activeSecPoint.second)} cm",
                                        color = Color(0xFFFBBF24),
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    if (isDual) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Curva Verde Lima: Índice Masa Muscular", color = LimeGreen.copy(alpha = 0.8f), fontSize = 9.5.sp)
                            Text("Curva Ámbar: Control Grasa / Caderas", color = Color(0xFFFBBF24).copy(alpha = 0.8f), fontSize = 9.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Canvas Native Chart con Neón Dual
                val values = if (isDual) {
                    dataPoints.map { it.second } + secondaryPoints.map { it.second }
                } else {
                    dataPoints.map { it.second }
                }
                val rawMin = values.minOrNull() ?: 0.0
                val rawMax = values.maxOrNull() ?: 0.0
                val span = (rawMax - rawMin)
                val pad = if (span == 0.0) 1.0 else span * 0.15
                val chartMin = (rawMin - pad).coerceAtLeast(0.0)
                val chartMax = rawMax + pad
                val chartRange = (chartMax - chartMin).coerceAtLeast(0.5)

                var pointOffsets by remember { mutableStateOf<List<Offset>>(emptyList()) }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .pointerInput(dataPoints, secondaryPoints) {
                            detectTapGestures { tapOffset ->
                                if (pointOffsets.isNotEmpty()) {
                                    val closest = pointOffsets.indices.minByOrNull { i ->
                                        kotlin.math.abs(pointOffsets[i].x - tapOffset.x)
                                    }
                                    if (closest != null) {
                                        selectedPointIndex = closest
                                    }
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val leftPad = 40.dp.toPx()
                        val rightPad = 20.dp.toPx()
                        val topPad = 20.dp.toPx()
                        val bottomPad = 28.dp.toPx()
                        val w = size.width - leftPad - rightPad
                        val h = size.height - topPad - bottomPad

                        val textPaint = Paint().apply {
                            color = android.graphics.Color.parseColor("#888888")
                            textSize = 10.sp.toPx()
                            isAntiAlias = true
                        }

                        // Grid lines
                        val gridSteps = 2
                        for (g in 0..gridSteps) {
                            val ratio = g.toFloat() / gridSteps
                            val yPos = topPad + ratio * h
                            val yVal = chartMax - ratio * chartRange
                            drawLine(
                                color = SurfaceBorder.copy(alpha = 0.6f),
                                start = Offset(leftPad, yPos),
                                end = Offset(size.width - rightPad, yPos),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                String.format(Locale.US, "%.1f", yVal),
                                4.dp.toPx(),
                                yPos + 4.dp.toPx(),
                                textPaint
                            )
                        }

                        // Primary points (Músculo / Métricas primarias)
                        val primaryOffsets = dataPoints.indices.map { i ->
                            val x = leftPad + i * (w / (dataPoints.size - 1).coerceAtLeast(1))
                            val y = topPad + h - ((dataPoints[i].second - chartMin) / chartRange).toFloat() * h
                            Offset(x, y)
                        }
                        pointOffsets = primaryOffsets

                        // 1. ÁREA NEÓN DE MÚSCULO (LIME)
                        if (primaryOffsets.isNotEmpty()) {
                            val fillPath = Path().apply {
                                moveTo(primaryOffsets.first().x, topPad + h)
                                primaryOffsets.forEach { pt -> lineTo(pt.x, pt.y) }
                                lineTo(primaryOffsets.last().x, topPad + h)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(LimeGreen.copy(alpha = 0.35f), LimeGreen.copy(alpha = 0.02f)),
                                    startY = topPad,
                                    endY = topPad + h
                                )
                            )

                            // Línea Curva Neón Verde Lima
                            val strokePath = Path().apply {
                                moveTo(primaryOffsets.first().x, primaryOffsets.first().y)
                                for (k in 1 until primaryOffsets.size) {
                                    lineTo(primaryOffsets[k].x, primaryOffsets[k].y)
                                }
                            }
                            drawPath(
                                path = strokePath,
                                color = LimeGreen,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // 2. CURVA SECUNDARIA NEÓN ÁMBAR (GRASA / CADERAS) si está en modo Dual
                        if (isDual && secondaryPoints.isNotEmpty()) {
                            val secondaryOffsets = secondaryPoints.indices.map { i ->
                                val x = leftPad + i * (w / (secondaryPoints.size - 1).coerceAtLeast(1))
                                val y = topPad + h - ((secondaryPoints[i].second - chartMin) / chartRange).toFloat() * h
                                Offset(x, y)
                            }

                            // Glow translúcido ámbar
                            val secFillPath = Path().apply {
                                moveTo(secondaryOffsets.first().x, topPad + h)
                                secondaryOffsets.forEach { pt -> lineTo(pt.x, pt.y) }
                                lineTo(secondaryOffsets.last().x, topPad + h)
                                close()
                            }
                            drawPath(
                                path = secFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.22f), Color.Transparent),
                                    startY = topPad,
                                    endY = topPad + h
                                )
                            )

                            // Línea Curva Neón Ámbar
                            val secStrokePath = Path().apply {
                                moveTo(secondaryOffsets.first().x, secondaryOffsets.first().y)
                                for (k in 1 until secondaryOffsets.size) {
                                    lineTo(secondaryOffsets[k].x, secondaryOffsets[k].y)
                                }
                            }
                            drawPath(
                                path = secStrokePath,
                                color = Color(0xFFFBBF24),
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Puntos ámbar
                            secondaryOffsets.forEachIndexed { idx, pt ->
                                val isSelected = (selectedPointIndex ?: (secondaryPoints.size - 1)) == idx
                                if (isSelected) {
                                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)
                                    drawCircle(color = Color(0xFFFBBF24), radius = 3.5.dp.toPx(), center = pt)
                                } else {
                                    drawCircle(color = Color(0xFFFBBF24), radius = 4.dp.toPx(), center = pt)
                                    drawCircle(color = BlackBackground, radius = 2.dp.toPx(), center = pt)
                                }
                            }
                        }

                        // Puntos Verde Lima y Fechas X
                        val datePaint = Paint().apply {
                            color = android.graphics.Color.parseColor("#AAAAAA")
                            textSize = 9.sp.toPx()
                            textAlign = Paint.Align.CENTER
                            isAntiAlias = true
                        }

                        primaryOffsets.forEachIndexed { idx, pt ->
                            val isSelected = (selectedPointIndex ?: (dataPoints.size - 1)) == idx
                            if (isSelected) {
                                drawCircle(color = Color.White, radius = 7.dp.toPx(), center = pt)
                                drawCircle(color = LimeGreen, radius = 4.dp.toPx(), center = pt)
                            } else {
                                drawCircle(color = LimeGreen, radius = 5.dp.toPx(), center = pt)
                                drawCircle(color = BlackBackground, radius = 2.5.dp.toPx(), center = pt)
                            }

                            val dt = SimpleDateFormat("dd/MM", Locale.getDefault())
                                .format(Date(dataPoints[idx].first.timestamp))
                            drawContext.canvas.nativeCanvas.drawText(
                                dt,
                                pt.x,
                                size.height - 4.dp.toPx(),
                                datePaint
                            )
                        }
                    }
                }
            }
        }
    }
}
