package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.GeminiCoachService
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Routine
import com.example.data.model.WeeklyRoutineHelper
import com.example.ui.components.CommunicationHelper
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Diálogo para ver y editar la Rutina del atleta y enviar por WhatsApp:
 * 1. Enviar rutina del día (Hoy o día seleccionado)
 * 2. Enviar rutina semanal completa
 * 3. Editar o personalizar rutina
 */
@Composable
fun WeeklyRoutineDialog(
    client: Client,
    routines: List<Routine> = emptyList(),
    onDismiss: () -> Unit,
    onSaveRoutine: (String) -> Unit
) {
    val context = LocalContext.current
    var routineText by remember { mutableStateOf(client.weeklyRoutine) }

    // Determinar la rutina asignada de la biblioteca maestra
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

    val todayName = remember { CommunicationHelper.getTodayDayName() }
    val dayTabs = remember {
        listOf("Hoy ($todayName)", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Semana")
    }
    var selectedTab by remember { mutableStateOf(dayTabs.first()) }

    val effectiveDayName = when {
        selectedTab.startsWith("Hoy") -> todayName
        selectedTab == "Semana" -> todayName
        else -> selectedTab
    }

    // Parsear el esquema estructurado si existe
    val parsedSchedule = remember(assignedRoutine) {
        assignedRoutine?.let { WeeklyRoutineHelper.parseWeeklySchedule(it.exercisesJson) } ?: emptyList()
    }
    val dayRoutine = remember(parsedSchedule, effectiveDayName) {
        parsedSchedule.firstOrNull { it.dayName.equals(effectiveDayName, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("RUTINA DE ENTRENAMIENTO", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(client.fullName, color = TextLightGray, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Día / Pestañas
                Text("Selecciona el día a consultar o enviar:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    dayTabs.forEach { tabName ->
                        val isSelected = tabName == selectedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LimeGreen else SurfaceElevated)
                                .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedTab = tabName }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = tabName,
                                color = if (isSelected) Color.Black else TextLightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resumen del día seleccionado
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedTab == "Semana") "VISTA SEMANAL COMPLETA" else "DÍA: ${effectiveDayName.uppercase()}",
                                color = LimeGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (dayRoutine?.muscleFocus?.isNotBlank() == true && selectedTab != "Semana") {
                                Text(
                                    text = dayRoutine.muscleFocus,
                                    color = Color(0xFF60A5FA),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (selectedTab != "Semana" && dayRoutine != null && dayRoutine.exercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${dayRoutine.exercises.size} ejercicios programados para este día:",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            dayRoutine.exercises.forEachIndexed { i, ex ->
                                Text(
                                    text = "${i + 1}. ${ex.exerciseName} (${ex.sets}x${ex.reps})",
                                    color = TextLightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else if (selectedTab != "Semana") {
                            Spacer(modifier = Modifier.height(6.dp))
                            val line = client.weeklyRoutine.lines().firstOrNull { it.trim().startsWith(effectiveDayName, ignoreCase = true) }
                            Text(
                                text = line ?: "Sin ejercicios estructurados en plantilla. Se aplicará el enfoque general de sala.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Plan semanal de Lunes a Sábado para ${client.mainObjective}.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Edición del Plan Semanal / Notas del Entrenador:", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = routineText,
                    onValueChange = { routineText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextLightGray,
                        unfocusedTextColor = TextLightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // BOTÓN 1 PRINCIPAL: ENVIAR RUTINA DEL DÍA POR WHATSAPP
                Button(
                    onClick = {
                        val shareMessage = CommunicationHelper.buildDailyRoutineMessage(
                            client = client,
                            assignedRoutine = assignedRoutine,
                            dayName = effectiveDayName
                        )
                        CommunicationHelper.sendWhatsApp(context, client.phone, shareMessage)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (effectiveDayName.equals(todayName, ignoreCase = true)) "ENVIAR RUTINA DE HOY (WA)" else "ENVIAR RUTINA DE ${effectiveDayName.uppercase()} (WA)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // BOTÓN 2 Y 3: ENVIAR SEMANA COMPLETA Y GUARDAR
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val shareMessage = CommunicationHelper.buildWeeklyRoutineMessage(
                                client = client,
                                assignedRoutine = assignedRoutine
                            )
                            CommunicationHelper.sendWhatsApp(context, client.phone, shareMessage)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = TextLightGray),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ENVIAR SEMANA (WA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onSaveRoutine(routineText)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated, contentColor = LimeGreen),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.6f))
                    ) {
                        Text("GUARDAR CAMBIOS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para ver el Desempeño Semanal y enviar REPORTE DE DESEMPEÑO SEMANAL por WhatsApp
 */
@Composable
fun WeeklyPerformanceDialog(
    client: Client,
    attendances: List<Attendance>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiCoachService() }

    // Cálculo dinámico de la semana actual (Lunes a Sábado)
    val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    val daysFromMonday = if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY

    val mondayCal = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val saturdayCal = (mondayCal.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, 5)
    }

    val rangeFmt = SimpleDateFormat("dd/MM", Locale.getDefault())
    val weekRangeText = "${rangeFmt.format(mondayCal.time)} al ${rangeFmt.format(saturdayCal.time)} (Lunes a Sábado)"

    // Determinar qué días asistió en la semana
    val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val clientAttendanceDates = attendances
        .filter { it.clientId == client.id }
        .map {
            val dCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            dayFmt.format(dCal.time)
        }.toSet()

    val attendedDaysList = mutableListOf<String>()
    val missedDaysList = mutableListOf<String>()

    val streakInfo = remember(client.id, attendances) {
        CommunicationHelper.calculateWeeklyStreak(client.id, attendances)
    }

    for (i in 0 until 6) {
        val currentDayCal = (mondayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i) }
        val dateKey = dayFmt.format(currentDayCal.time)
        val name = dayNames[i]
        if (clientAttendanceDates.contains(dateKey)) {
            attendedDaysList.add(name)
        } else {
            missedDaysList.add(name)
        }
    }

    var trainerObservation by remember { mutableStateOf("") }
    var isGeneratingAI by remember { mutableStateOf(false) }

    fun generateObservation() {
        isGeneratingAI = true
        coroutineScope.launch {
            try {
                val obs = geminiService.generateWeeklyPerformanceObservation(
                    clientInfo = client,
                    attendedCount = attendedDaysList.size,
                    missedCount = missedDaysList.size,
                    attendedDays = attendedDaysList,
                    missedDays = missedDaysList
                )
                trainerObservation = obs
            } catch (e: Exception) {
                trainerObservation = "Desempeño registrado. Has completado ${attendedDaysList.size}/6 días esta semana. Mantén el enfoque y la constancia en sala de musculación."
            } finally {
                isGeneratingAI = false
            }
        }
    }

    LaunchedEffect(client.id) {
        generateObservation()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("REPORTE DE DESEMPEÑO", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Text(client.fullName, color = TextLightGray, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text("Semana: $weekRangeText", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Métricas clave
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(title = "Días Asistidos", value = "${attendedDaysList.size} días", highlight = true, modifier = Modifier.weight(1f))
                    StatCard(title = "Inasistencias", value = "${missedDaysList.size} días", modifier = Modifier.weight(1f))
                    StatCard(title = "Efectividad", value = "${((attendedDaysList.size.toFloat() / 6f) * 100).toInt()}%", modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Streak Card
                if (streakInfo.weeksCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2B1405))
                            .border(1.dp, Color(0xFFF97316), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔥", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "RACHA DE ASISTENCIA: ${streakInfo.weeksCount} SEMANAS CONSECUTIVAS",
                                    color = Color(0xFFFB923C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = streakInfo.motivationPhrase,
                                    color = TextLightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Desglose de Días
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✅ DÍAS ASISTIDOS (${attendedDaysList.size} días):", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (attendedDaysList.isNotEmpty()) attendedDaysList.joinToString(", ") else "Ningún día asistido esta semana",
                            color = TextLightGray,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = SurfaceBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("❌ DÍAS INASISTENTES (${missedDaysList.size} días):", color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (missedDaysList.isNotEmpty()) missedDaysList.joinToString(", ") else "Asistencia perfecta (0 faltas)",
                            color = TextLightGray,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Observaciones del Entrenador por IA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, LimeGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("OBSERVACIONES DEL ENTRENADOR (IA)", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { generateObservation() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                if (isGeneratingAI) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = LimeGreen, strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Refresh, contentDescription = "Regenerar IA", tint = LimeGreen, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = trainerObservation,
                            onValueChange = { trainerObservation = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark,
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextLightGray,
                                unfocusedTextColor = TextLightGray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botones de Acción
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val reportMsg = CommunicationHelper.buildWeeklyPerformanceReport(
                                client = client,
                                weekRangeText = weekRangeText,
                                attendedDays = attendedDaysList,
                                missedDays = missedDaysList,
                                aiObservation = trainerObservation,
                                streakWeeks = streakInfo.weeksCount
                            )
                            CommunicationHelper.sendWhatsApp(context, client.phone, reportMsg)
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ENVIAR POR WHATSAPP", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.8f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CERRAR", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    highlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceElevated)
            .border(1.dp, if (highlight) LimeGreen.copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                color = if (highlight) LimeGreen else TextLightGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
