package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.ui.components.ExportReportHelper
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import java.util.Locale

@Composable
fun ReportsExportDialog(
    clients: List<Client>,
    payments: List<Payment>,
    attendances: List<Attendance>,
    measurements: List<Measurement>,
    totalIncome: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131711)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF222B1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                // Header (Icono + Título + Subtítulo)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E2818)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = LimeGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Exportación de Reportes",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Descarga en Excel / CSV o comparte resumen",
                            color = TextMuted,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Reporte de Socios (CSV / Excel)
                ReportRowItem(
                    icon = Icons.Default.Groups,
                    title = "Reporte de Socios\n(CSV / Excel)",
                    subtitle = "${clients.size} clientes registrados\ncon planes y contacto.",
                    actionLabel = "Descargar CSV",
                    onAction = { ExportReportHelper.exportClientsCsv(context, clients) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 2. Reporte de Cobros e Ingresos (CSV)
                val thisMonthTotal = payments.sumOf { it.amount }
                ReportRowItem(
                    icon = Icons.Default.Payments,
                    title = "Reporte de Cobros e\nIngresos (CSV)",
                    subtitle = "${payments.size} pagos registrados\n($${String.format(Locale.US, "%.2f", thisMonthTotal)} este mes).",
                    actionLabel = "Descargar CSV",
                    onAction = { ExportReportHelper.exportPaymentsCsv(context, payments) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 3. Historial de Asistencias (CSV)
                ReportRowItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Historial de\nAsistencias (CSV)",
                    subtitle = "${attendances.size} registros de entrada.",
                    actionLabel = "Descargar CSV",
                    onAction = { ExportReportHelper.exportAttendancesCsv(context, attendances) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 4. Mediciones Antropométricas (CSV)
                ReportRowItem(
                    icon = Icons.Default.ShowChart,
                    title = "Mediciones\nAntropométricas (CSV)",
                    subtitle = "${measurements.size} evaluaciones\nregistradas.",
                    actionLabel = "Descargar CSV",
                    onAction = { ExportReportHelper.exportMeasurementsCsv(context, measurements, clients) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 5. Resumen de Gestión para Compartir
                ReportRowItem(
                    icon = Icons.Default.Share,
                    title = "Resumen de Gestión para\nCompartir",
                    subtitle = "Texto formateado listo para\nenviar a WhatsApp o copiar.",
                    actionLabel = "Compartir",
                    onAction = {
                        ExportReportHelper.shareManagementSummary(
                            context = context,
                            clients = clients,
                            payments = payments,
                            attendances = attendances,
                            measurements = measurements,
                            totalIncome = totalIncome
                        )
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 6. Reporte Financiero y de Caja (PDF)
                ReportRowItem(
                    icon = Icons.Default.PictureAsPdf,
                    title = "Reporte Financiero y de\nCaja (PDF)",
                    subtitle = "Documento oficial listo\npara imprimir, archivar o\ncompartir.",
                    actionLabel = "Exportar PDF",
                    onAction = {
                        ExportReportHelper.exportFinancialPdf(
                            context = context,
                            payments = payments,
                            totalIncome = totalIncome
                        )
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Cerrar centrado en tono lima
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cerrar",
                        color = LimeGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C2219)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextLightGray,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.5.sp,
                    lineHeight = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(
                containerColor = LimeGreen,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = actionLabel,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
    }
}
