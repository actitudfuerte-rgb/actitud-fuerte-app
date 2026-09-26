package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    attendances: List<Attendance>,
    clients: List<Client>,
    onCheckIn: (Client) -> Unit,
    onOpenClient: (Client) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val todayDisplay = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("es", "ES")).format(Date())

    val filteredClientsForCheckin = remember(searchQuery, clients) {
        if (searchQuery.isBlank()) emptyList()
        else clients.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            it.accessId.contains(searchQuery, ignoreCase = true)
        }
    }

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
                    Text("CONTROL DE SALA", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Asistencias de la Semana", color = TextLightGray, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text(todayDisplay.replaceFirstChar { it.uppercase() }, color = TextMuted, fontSize = 12.sp)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ATLETAS", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${clients.size}", color = LimeGreen, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar for Quick Check-In
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar atleta para check-in (Nombre, ID o Telf)...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LimeGreen) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark,
                    focusedBorderColor = LimeGreen,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = TextLightGray,
                    unfocusedTextColor = TextLightGray
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Search Results Section
        if (searchQuery.isNotBlank()) {
            item {
                Text("RESULTADOS DE BÚSQUEDA (${filteredClientsForCheckin.size})", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (filteredClientsForCheckin.isEmpty()) {
                item {
                    Text("No se encontraron atletas con ese criterio.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                }
            } else {
                items(filteredClientsForCheckin) { client ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(client.fullName, color = TextLightGray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("ID: ${client.accessId} • ${client.membershipPlan}", color = TextMuted, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = client.operationalStatus)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LimeGreen)
                                    .clickable {
                                        onCheckIn(client)
                                        searchQuery = ""
                                        Toast.makeText(context, "Check-in registrado para ${client.fullName}", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("CHECK-IN", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Section Title for Attendance Cards
        item {
            Text("FICHAS DE ASISTENCIA (SEMANA)", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (clients.isEmpty()) {
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
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No hay atletas registrados.", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(clients) { client ->
                ClientAttendanceCard(
                    client = client,
                    allAttendances = attendances,
                    onCheckIn = onCheckIn,
                    onOpenClient = onOpenClient,
                    onOpenPerformanceReport = { cl ->
                        // Send performance report via WhatsApp
                        val clientAtts = attendances.filter { it.clientId == cl.id }
                        val nameWithoutLastName = cl.fullName.split(" ").firstOrNull() ?: cl.fullName
                        val msg = """
                            🏋️ REPORTE DE DESEMPEÑO SEMANAL
                            Socio: $nameWithoutLastName
                            
                            Hola $nameWithoutLastName, aquí tienes el balance de tu asistencia esta semana en Actitud Fuerte. ¡Sigue entrenando con constancia!
                        """.trimIndent()
                        val encoded = Uri.encode(msg)
                        val phoneClean = cl.phone.replace("+", "").replace(" ", "")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phoneClean&text=$encoded"))
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ClientAttendanceCard(
    client: Client,
    allAttendances: List<Attendance>,
    onCheckIn: (Client) -> Unit,
    onOpenClient: (Client) -> Unit,
    onOpenPerformanceReport: (Client) -> Unit
) {
    val context = LocalContext.current
    
    // Calculate current week (Monday to Saturday)
    val todayCal = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateStr = dateFormat.format(todayCal.time)

    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val dayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    val clientAtts = allAttendances.filter { it.clientId == client.id }
    
    data class DayData(
        val dayName: String,
        val dateStr: String,
        val attended: Boolean,
        val isToday: Boolean,
        val isPast: Boolean
    )

    val weekDays = mutableListOf<DayData>()
    for (i in 0..5) {
        val dateStr = dateFormat.format(calendar.time)
        val attended = clientAtts.any { it.dateOnlyString == dateStr }
        val isToday = (dateStr == todayDateStr)
        val isPast = (dateStr.compareTo(todayDateStr) < 0)
        weekDays.add(DayData(dayNames[i], dateStr, attended, isToday, isPast))
        calendar.add(Calendar.DATE, 1)
    }
    
    val attendedCount = weekDays.count { it.attended }
    val absencesCount = weekDays.count { it.isPast && !it.attended }

    val streakInfo = remember(client.id, allAttendances) {
        CommunicationHelper.calculateWeeklyStreak(client.id, allAttendances)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161A14)),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Name & Membership + Streak Badge + 5/6 Días Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client.fullName,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Membresía ${client.membershipPlan.filter { it.isDigit() }.ifEmpty { "12" }}$",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (streakInfo.weeksCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF331604))
                                .border(1.dp, Color(0xFFF97316), RoundedCornerShape(12.dp))
                                .clickable {
                                    Toast.makeText(context, "${client.fullName}: ${streakInfo.motivationPhrase}", Toast.LENGTH_LONG).show()
                                }
                                .padding(horizontal = 9.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔥", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${streakInfo.weeksCount} SEM",
                                    color = Color(0xFFFB923C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF26331C))
                            .border(1.dp, LimeGreen.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$attendedCount/6 DÍAS",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            if (streakInfo.weeksCount >= 3 || streakInfo.isPerfectThisWeek) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (streakInfo.isPerfectThisWeek) Color(0xFF1E2D1A) else Color(0xFF281305))
                        .border(
                            1.dp,
                            if (streakInfo.isPerfectThisWeek) LimeGreen.copy(alpha = 0.5f) else Color(0xFFF97316).copy(alpha = 0.5f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (streakInfo.isPerfectThisWeek) "⭐" else "🔥", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (streakInfo.isPerfectThisWeek) {
                            "¡Asistencia perfecta esta semana! ($attendedCount/6 días completados)"
                        } else {
                            "¡Racha de fuego! ${streakInfo.weeksCount} semanas consecutivas entrenando"
                        },
                        color = if (streakInfo.isPerfectThisWeek) LimeGreen else Color(0xFFFB923C),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day Buttons Row (Lun, Mar, Mié, Jue, Vie, Sáb)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weekDays.forEach { day ->
                    val isAttended = day.attended
                    val isAbsence = day.isPast && !day.attended
                    val isTodayPending = day.isToday && !day.attended

                    val bgColor = when {
                        isAttended -> LimeGreen
                        isAbsence -> Color(0xFF381212)
                        isTodayPending -> Color(0xFF222B1C)
                        else -> Color(0xFF1E241A)
                    }

                    val borderColor = when {
                        isAttended -> LimeGreen
                        isAbsence -> Color(0xFFEF4444)
                        isTodayPending -> LimeGreen.copy(alpha = 0.8f)
                        else -> SurfaceBorder
                    }

                    val dayTextColor = when {
                        isAttended -> Color.Black
                        isAbsence -> Color(0xFFFCA5A5)
                        isTodayPending -> LimeGreen
                        else -> TextLightGray
                    }

                    val symbolText = when {
                        isAttended -> "✓"
                        isAbsence -> "✗"
                        isTodayPending -> "HOY"
                        else -> "-"
                    }

                    val symbolColor = when {
                        isAttended -> Color.Black
                        isAbsence -> Color(0xFFEF4444)
                        isTodayPending -> LimeGreen
                        else -> TextMuted
                    }

                    val symbolSize = if (symbolText == "HOY") 10.sp else 14.sp

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(
                                width = 1.dp,
                                color = borderColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                if (day.isToday && !day.attended) {
                                    onCheckIn(client)
                                    Toast.makeText(context, "Asistencia registrada para ${client.fullName}", Toast.LENGTH_SHORT).show()
                                } else {
                                    onOpenClient(client)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = day.dayName,
                                color = dayTextColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = symbolText,
                                color = symbolColor,
                                fontSize = symbolSize,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row: Attendance stats + Desempeño button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asistió: $attendedCount d • Inasistencias: $absencesCount d",
                    color = TextLightGray,
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF222B1C))
                        .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            onOpenPerformanceReport(client)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = LimeGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Desempeño",
                            color = LimeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

