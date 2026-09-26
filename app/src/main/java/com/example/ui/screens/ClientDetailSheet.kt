package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.FitnessConstants
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.example.data.model.SocialPost
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.DigitalIdCardDialog
import com.example.ui.components.ExportReportHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.StatusActiveColor
import com.example.ui.theme.StatusInactiveColor
import com.example.ui.theme.StatusSanctionedColor
import com.example.ui.theme.StatusSickColor
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FICHA TÉCNICA DEL USUARIO - ACTITUD FUERTE
 */
@Composable
fun ClientDetailDialog(
    client: Client,
    measurements: List<Measurement>,
    attendances: List<Attendance>,
    payments: List<Payment>,
    posts: List<SocialPost> = emptyList(),
    routines: List<Routine> = emptyList(),
    allPosts: List<SocialPost> = emptyList(),
    onDismiss: () -> Unit,
    onStatusChange: (String) -> Unit,
    onCheckIn: () -> Unit,
    onRecordPayment: (Payment) -> Unit,
    onSaveMeasurement: (Measurement, Boolean, Boolean) -> Unit,
    onSaveRoutine: (String) -> Unit,
    onEditClient: (Client) -> Unit,
    onDeleteClient: (Client) -> Unit,
    onCreatePost: (SocialPost) -> Unit = {},
    onToggleLike: (Long) -> Unit = {},
    onDeletePost: (SocialPost) -> Unit = {}
) {
    val context = LocalContext.current

    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showIdCardDialog by remember { mutableStateOf(false) }
    var showAssignAccessDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var showPerformanceDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showProfileSummaryDialog by remember { mutableStateOf(false) }
    var showAthleteSocialProfileDialog by remember { mutableStateOf(false) }

    val latestMeasurement = measurements.firstOrNull()
    val previousMeasurement = if (measurements.size > 1) measurements[1] else null

    // Rutina asignada desde la biblioteca maestra para el atleta
    val assignedRoutine = remember(client, routines) {
        val isFemale = client.gender.equals("Mujer", ignoreCase = true) || client.fullName.contains("Carlen", ignoreCase = true)
        val expectedGenderTarget = if (isFemale) "Mujeres" else "Hombres"
        routines.firstOrNull { r ->
            r.targetType.equals("Usuarios específicos", ignoreCase = true) && r.targetClientNames.contains(client.fullName, ignoreCase = true)
        } ?: routines.firstOrNull { r ->
            r.targetType.equals(expectedGenderTarget, ignoreCase = true)
        } ?: routines.firstOrNull { r ->
            r.targetType.equals(if (isFemale) "Hombres" else "Mujeres", ignoreCase = true)
        } ?: routines.firstOrNull()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 14.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, LimeGreen.copy(alpha = 0.55f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header: Title & Close Button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FICHA TÉCNICA DEL USUARIO",
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = client.fullName,
                                color = TextLightGray,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "ID: ${client.accessId} • ${client.phone}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 1. Pestaña desplegable: ESTATUS OPERATIVO DEL USUARIO
                item {
                    Text(
                        text = "ESTATUS OPERATIVO DEL USUARIO",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { statusDropdownExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(status = client.operationalStatus)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Toca para cambiar estatus", color = TextMuted, fontSize = 12.sp)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LimeGreen)
                        }

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false },
                            modifier = Modifier.background(SurfaceElevated)
                        ) {
                            FitnessConstants.OPERATIONAL_STATUSES.forEach { statusOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            StatusBadge(status = statusOption)
                                        }
                                    },
                                    onClick = {
                                        onStatusChange(statusOption)
                                        statusDropdownExpanded = false
                                        Toast.makeText(context, "Estatus actualizado a: $statusOption", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. SECCIÓN: ACCIONES RÁPIDAS (Check-in, Cobrar a usuario, Medidas)
                item {
                    Text(
                        text = "ACCIONES RÁPIDAS",
                        color = LimeGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón Check-in
                        QuickActionButton(
                            title = "Check-in",
                            icon = Icons.Default.CheckCircle,
                            onClick = onCheckIn,
                            highlight = true,
                            modifier = Modifier.weight(1f)
                        )

                        // Botón Cobrar a usuario
                        QuickActionButton(
                            title = "Cobrar",
                            icon = Icons.Default.Payments,
                            onClick = { showPaymentDialog = true },
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )

                        // Botón Medidas (toma de medidas corporales e IA)
                        QuickActionButton(
                            title = "Medidas",
                            icon = Icons.Default.Straighten,
                            onClick = { showMeasurementDialog = true },
                            highlight = false,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2.5. Gamificación & Racha de Disciplina (Fidelización)
                item {
                    val streakInfo = remember(client.id, attendances) {
                        CommunicationHelper.calculateWeeklyStreak(client.id, attendances)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (streakInfo.weeksCount > 0) Color(0xFF241205) else Color(0xFF191D17)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (streakInfo.weeksCount > 0) Color(0xFFF97316).copy(alpha = 0.7f) else LimeGreen.copy(alpha = 0.35f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🔥", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "RACHA DE DISCIPLINA",
                                            color = if (streakInfo.weeksCount > 0) Color(0xFFFB923C) else TextLightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.8.sp
                                        )
                                        Text(
                                            text = if (streakInfo.weeksCount > 0) {
                                                "${streakInfo.weeksCount} semanas consecutivas de asistencia"
                                            } else {
                                                "Sin racha acumulada aún"
                                            },
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (streakInfo.weeksCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFF97316).copy(alpha = 0.25f))
                                            .border(1.dp, Color(0xFFF97316), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 9.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "${streakInfo.weeksCount} SEM",
                                            color = Color(0xFFFB923C),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = streakInfo.motivationPhrase,
                                color = TextLightGray,
                                fontSize = 11.5.sp
                            )

                            if (streakInfo.weeksCount > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF25D366))
                                        .clickable {
                                            val celebMsg = CommunicationHelper.buildStreakCelebrationMessage(
                                                client = client,
                                                streakWeeks = streakInfo.weeksCount
                                            )
                                            CommunicationHelper.sendWhatsApp(context, client.phone, celebMsg)
                                            android.widget.Toast.makeText(context, "Abriendo WhatsApp para felicitar a ${client.fullName}...", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📲", fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FELICITAR POR WHATSAPP (RECONOCER DISCIPLINA)",
                                        color = Color.White,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Ficha Técnica Completa (Datos del Formulario)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "DETALLE DE REGISTRO & MEMBRESÍA",
                                color = TextLightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            InfoRow("Plan de Membresía:", client.membershipPlan, highlight = true)
                            InfoRow("Modalidad de Pago:", client.paymentFrequency)
                            InfoRow("Correo Electrónico:", client.email.ifBlank { "No registrado" })
                            InfoRow("Contacto Emergencia:", client.emergencyContact.ifBlank { "No asignado" })
                            InfoRow("Objetivo Principal:", client.mainObjective)
                            InfoRow("Condición / Nota Médica:", client.medicalCondition)
                            if (client.medicalNotes.isNotBlank()) {
                                InfoRow("Detalle Médico:", client.medicalNotes)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Últimas Medidas Corporales & Observación IA
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BlackBackground),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MEDIDAS CORPORALES RECIENTES",
                                    color = LimeGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (latestMeasurement != null) {
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(latestMeasurement.timestamp))
                                    Text(dateStr, color = TextMuted, fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (latestMeasurement != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    MetricChip("Peso", "${latestMeasurement.weightKg} kg")
                                    MetricChip("Espalda", "${latestMeasurement.backCm} cm")
                                    MetricChip("Hombros", "${latestMeasurement.shouldersCm} cm")
                                    MetricChip("Brazos", "${latestMeasurement.armsCm} cm")
                                    MetricChip("Caderas", "${latestMeasurement.hipsCm} cm")
                                }

                                val obs = latestMeasurement.trainerObservation.ifBlank { latestMeasurement.aiObservation }
                                if (obs.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceElevated)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text("OBSERVACIÓN DEL ENTRENADOR (IA):", color = LimeGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(obs, color = TextLightGray, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Enviar por WhatsApp con Firma Digital Oficial
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(LimeGreen.copy(alpha = 0.12f))
                                            .border(1.dp, LimeGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                val report = CommunicationHelper.buildMeasurementReport(client, latestMeasurement, previousMeasurement)
                                                CommunicationHelper.sendWhatsApp(context, client.phone, report)
                                            }
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ENVIAR WA", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Exportar Ficha PDF con Sello Oficial del Entrenador
                                    Row(
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1B2416))
                                            .border(1.dp, Color(0xFF2E4025), RoundedCornerShape(8.dp))
                                            .clickable {
                                                ExportReportHelper.exportClientAnthropometricPdf(context, client, measurements)
                                            }
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("FICHA PDF (SELLO)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Text(
                                    text = "Sin mediciones registradas aún. Toca 'Medidas' en Acciones Rápidas para tomar la primera.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // 5. BOTONES DE LA FICHA TÉCNICA DEL USUARIO
                item {
                    Text(
                        text = "HERRAMIENTAS & GESTIÓN DEL ATLETA",
                        color = TextLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Botón Destacado: PERFIL SOCIAL DE ATLETA (ESTILO INSTAGRAM)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF833AB4),
                                        Color(0xFFFD1D1D),
                                        Color(0xFFFCB045)
                                    )
                                )
                            )
                            .clickable { showAthleteSocialProfileDialog = true }
                            .padding(1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(11.dp))
                                .background(SurfaceDark)
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📸", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "PERFIL DE ATLETA (ESTILO INSTAGRAM)",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "Feed 3:4 • Historias 24h • Posts • Bio Deportiva",
                                        color = LimeGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Botones en grilla estilizada:
                    // - Recordatorio Membresía WA
                    // - Bienvenida WA
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionMenuButton(
                            title = "Recordatorio Membresía",
                            icon = Icons.Default.NotificationsActive,
                            onClick = {
                                val msg = CommunicationHelper.buildMembershipReminderMessage(client)
                                CommunicationHelper.sendWhatsApp(context, client.phone, msg)
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ActionMenuButton(
                            title = "Bienvenida WA",
                            icon = Icons.Default.WavingHand,
                            onClick = {
                                val msg = CommunicationHelper.buildWelcomeMessage(client)
                                CommunicationHelper.sendWhatsApp(context, client.phone, msg)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // - Rutina de hoy (WA) [Envío directo con formato diario oficial]
                    // - Rutina semanal [Gestión de rutina y días]
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionMenuButton(
                            title = "Rutina de hoy (WA)",
                            icon = Icons.Default.Send,
                            color = LimeGreen,
                            onClick = {
                                val msg = CommunicationHelper.buildDailyRoutineMessage(
                                    client = client,
                                    assignedRoutine = assignedRoutine,
                                    dayName = CommunicationHelper.getTodayDayName()
                                )
                                CommunicationHelper.sendWhatsApp(context, client.phone, msg)
                                Toast.makeText(context, "Enviando rutina de hoy a ${client.fullName} por WhatsApp...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        ActionMenuButton(
                            title = "Rutina semanal",
                            icon = Icons.Default.FitnessCenter,
                            onClick = { showRoutineDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // - Desempeño semanal
                    // - Ver perfil fit
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionMenuButton(
                            title = "Desempeño semanal",
                            icon = Icons.Default.Timeline,
                            onClick = { showPerformanceDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        ActionMenuButton(
                            title = "Ver Perfil Fit",
                            icon = Icons.Default.Person,
                            onClick = { showAthleteSocialProfileDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // - Acceso / ID (Asignar Clave Login)
                    // - Editar información
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionMenuButton(
                            title = "Acceso / ID",
                            icon = Icons.Default.Lock,
                            onClick = { showAssignAccessDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                        ActionMenuButton(
                            title = "Editar información",
                            icon = Icons.Default.Edit,
                            onClick = { showEditDialog = true },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // - Eliminar
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActionMenuButton(
                            title = "Eliminar",
                            icon = Icons.Default.Delete,
                            onClick = { showDeleteConfirmDialog = true },
                            color = StatusSanctionedColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // - Cerrar
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CERRAR FICHA TÉCNICA", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // Modal para toma de medidas
    if (showMeasurementDialog) {
        NewMeasurementDialog(
            client = client,
            previousMeasurement = latestMeasurement,
            onDismiss = { showMeasurementDialog = false },
            onSaveMeasurement = { m, wa, em ->
                onSaveMeasurement(m, wa, em)
                showMeasurementDialog = false
                Toast.makeText(context, "Medición corporal guardada con éxito", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal para cobrar a usuario
    if (showPaymentDialog) {
        RecordPaymentDialog(
            client = client,
            onDismiss = { showPaymentDialog = false },
            onSavePayment = { p ->
                onRecordPayment(p)
                showPaymentDialog = false
                Toast.makeText(context, "Cobro de ${p.amount}$ registrado correctamente", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal credencial / ID
    if (showIdCardDialog) {
        DigitalIdCardDialog(client = client, onDismiss = { showIdCardDialog = false })
    }

    // Modal rutina semanal
    if (showRoutineDialog) {
        WeeklyRoutineDialog(
            client = client,
            routines = routines,
            onDismiss = { showRoutineDialog = false },
            onSaveRoutine = { routine ->
                onSaveRoutine(routine)
                Toast.makeText(context, "Rutina semanal actualizada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal desempeño semanal
    if (showPerformanceDialog) {
        WeeklyPerformanceDialog(
            client = client,
            attendances = attendances,
            onDismiss = { showPerformanceDialog = false }
        )
    }

    // Modal edición
    if (showEditDialog) {
        ClientRegistrationDialog(
            initialClient = client,
            onDismiss = { showEditDialog = false },
            onSave = { updatedClient ->
                onEditClient(updatedClient)
                showEditDialog = false
                Toast.makeText(context, "Información de atleta actualizada", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal confirmación eliminar
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Eliminar Atleta", color = StatusSanctionedColor, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar permanentemente a ${client.fullName}? Esta acción borrará todas sus asistencias, cobros y mediciones.",
                    color = TextLightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClient(client)
                        showDeleteConfirmDialog = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSanctionedColor, contentColor = Color.White)
                ) {
                    Text("ELIMINAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("CANCELAR", color = TextLightGray)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Modal perfil de usuario / hábitos
    if (showProfileSummaryDialog) {
        ProfileSummaryDialog(client = client, onDismiss = { showProfileSummaryDialog = false })
    }

    // Modal Asignar Acceso / Clave de Login (Punto 2)
    if (showAssignAccessDialog) {
        AssignAccessCredentialsDialog(
            client = client,
            onDismiss = { showAssignAccessDialog = false },
            onSaveCredentials = { newAccessId, newAccessPin ->
                val updated = client.copy(accessId = newAccessId, accessPin = newAccessPin)
                onEditClient(updated)
                showAssignAccessDialog = false
                Toast.makeText(context, "Credenciales de login sincronizadas para ${client.fullName}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal Perfil de Atleta (Fase 1 - Estilo Instagram con Historias 24h & Feed 3:4)
    if (showAthleteSocialProfileDialog) {
        AthleteProfileDialog(
            client = client,
            posts = posts,
            attendances = attendances,
            routines = routines,
            allPosts = allPosts,
            onDismiss = { showAthleteSocialProfileDialog = false },
            onCreatePost = onCreatePost,
            onToggleLike = onToggleLike,
            onDeletePost = onDeletePost,
            onSaveRoutine = onSaveRoutine,
            onUpdateClient = onEditClient
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (highlight) LimeGreen else SurfaceElevated)
            .border(1.dp, if (highlight) LimeGreen else SurfaceBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (highlight) Color.Black else LimeGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (highlight) Color.Black else TextLightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ActionMenuButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color = TextLightGray,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (color == TextLightGray) LimeGreen else color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun InfoRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = if (highlight) LimeGreen else TextLightGray,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MetricChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = LimeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Diálogo rápido para Cobrar a usuario
 */
@Composable
fun RecordPaymentDialog(
    client: Client,
    onDismiss: () -> Unit,
    onSavePayment: (Payment) -> Unit
) {
    val context = LocalContext.current
    val defaultAmount = if (client.membershipPlan.contains("12")) 12.0 else 20.0
    var amount by remember { mutableStateOf(defaultAmount.toString()) }
    var method by remember { mutableStateOf("Pago Móvil") }
    var reference by remember { mutableStateOf("") }
    var sendReceiptViaWa by remember { mutableStateOf(true) }

    val methods = listOf("Pago Móvil", "Efectivo", "Transferencia", "Zelle", "Tarjeta")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("COBRAR A USUARIO", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(client.fullName, color = TextLightGray, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("Plan: ${client.membershipPlan} (${client.paymentFrequency})", color = TextMuted, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(12.dp))

                // Tarjeta de Datos Pago Móvil Provincial
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("PAGO MÓVIL PROVINCIAL", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LimeGreen.copy(alpha = 0.15f))
                                    .clickable { CommunicationHelper.copyPagoMovilToClipboard(context) }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Copiar", color = LimeGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Banco: ${CommunicationHelper.PAGO_MOVIL_BANK} • CI: ${CommunicationHelper.PAGO_MOVIL_ID}\nTeléfono: ${CommunicationHelper.PAGO_MOVIL_PHONE} (Tasa BCV)",
                            color = TextLightGray,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Monto ($):", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                CustomInputField(
                    label = "",
                    value = amount,
                    onValueChange = { amount = it },
                    leadingIcon = Icons.Default.Payments,
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Método de Pago:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.take(3).forEach { m ->
                        val sel = method == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (sel) LimeGreen else SurfaceElevated)
                                .clickable { method = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(m, color = if (sel) Color.Black else TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.drop(3).forEach { m ->
                        val sel = method == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (sel) LimeGreen else SurfaceElevated)
                                .clickable { method = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(m, color = if (sel) Color.Black else TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                CustomInputField(
                    label = "Referencia / Recibo (opcional):",
                    value = reference,
                    onValueChange = { reference = it },
                    leadingIcon = Icons.Default.Rule,
                    placeholder = "Ej: REF-1092"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Checkbox Enviar Recibo por WhatsApp
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { sendReceiptViaWa = !sendReceiptViaWa },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sendReceiptViaWa,
                        onCheckedChange = { sendReceiptViaWa = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LimeGreen,
                            checkmarkColor = Color.Black,
                            uncheckedColor = SurfaceBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enviar Recibo detallado por WhatsApp", color = TextLightGray, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CANCELAR", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: defaultAmount
                            val p = Payment(
                                clientId = client.id,
                                clientName = client.fullName,
                                timestamp = System.currentTimeMillis(),
                                amount = amt,
                                planName = client.membershipPlan,
                                frequency = client.paymentFrequency,
                                method = method,
                                reference = reference.trim()
                            )
                            if (sendReceiptViaWa) {
                                val receiptMsg = CommunicationHelper.buildPaymentReceiptMessage(client, p)
                                CommunicationHelper.sendWhatsApp(context, client.phone, receiptMsg)
                            }
                            onSavePayment(p)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("REGISTRAR PAGO", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

/**
 * Perfil y Hábitos del usuario
 */
@Composable
fun ProfileSummaryDialog(client: Client, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PERFIL DEPORTIVO", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(client.fullName, color = TextLightGray, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                InfoRow("Objetivo Clave:", client.mainObjective, highlight = true)
                InfoRow("Condición Física:", client.medicalCondition)
                InfoRow("Modalidad de Fraccionamiento:", client.paymentFrequency)
                InfoRow("ID Gimnasio:", client.accessId)

                Spacer(modifier = Modifier.height(12.dp))
                Text("DISCIPLINA EN SALA DE MUSCULACIÓN", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "El atleta mantiene plan activo enfocado en ${client.mainObjective.lowercase()}. Se recomienda seguimiento continuo de medidas cada 15 a 30 días con retroalimentación del entrenador.",
                    color = TextLightGray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ENTENDIDO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Diálogo para asignar ID de Acceso y Clave/PIN de Login del atleta
 */
@Composable
fun AssignAccessCredentialsDialog(
    client: Client,
    onDismiss: () -> Unit,
    onSaveCredentials: (accessId: String, accessPin: String) -> Unit
) {
    var accessId by remember { mutableStateOf(client.accessId.ifBlank { "AF-${client.id}" }) }
    var accessPin by remember { mutableStateOf(client.accessPin) }
    var isPinVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LimeGreen)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(LimeGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ACCESO Y LOGIN AL SISTEMA", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        Text(client.fullName, color = TextLightGray, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("📧 Correo Registrado para Login:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(client.email, color = LimeGreen, fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "El socio ingresará este correo y la clave que le asignes en la pantalla inicial de Login.",
                            color = TextLightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Código / ID de Atleta:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                CustomInputField(
                    label = "",
                    value = accessId,
                    onValueChange = { accessId = it.uppercase() },
                    leadingIcon = Icons.Default.Badge,
                    placeholder = "Ej: AF-8521"
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text("Clave / PIN de Acceso (Login):", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = accessPin,
                    onValueChange = { accessPin = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: 1234 o contraseña", color = TextMuted, fontSize = 13.sp) },
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = LimeGreen) },
                    trailingIcon = {
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                if (isPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextLightGray,
                        unfocusedTextColor = TextLightGray,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CANCELAR", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            onSaveCredentials(accessId.trim(), accessPin.trim())
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("GUARDAR Y SINCRONIZAR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
