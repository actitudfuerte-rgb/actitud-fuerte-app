package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.FitnessConstants
import com.example.data.model.Payment
import com.example.ui.components.ClientAvatarImage
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import java.util.Locale

@Composable
fun UsersListScreen(
    clients: List<Client>,
    payments: List<Payment> = emptyList(),
    attendances: List<Attendance> = emptyList(),
    onOpenClient: (Client) -> Unit,
    onOpenSocialProfile: ((Client) -> Unit)? = null,
    onRegisterNewClient: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }

    val statusFilters = listOf("Todos", "🚨 Vencidos", "⏰ Por Vencer (1d)") + FitnessConstants.OPERATIONAL_STATUSES

    val filteredClients = remember(searchQuery, selectedStatusFilter, clients, payments) {
        clients.filter { client ->
            val matchesQuery = searchQuery.isBlank() ||
                client.fullName.contains(searchQuery, ignoreCase = true) ||
                client.phone.contains(searchQuery, ignoreCase = true) ||
                client.accessId.contains(searchQuery, ignoreCase = true)

            val dueStatus = CommunicationHelper.getClientMembershipDueStatus(client, payments)

            val matchesStatus = when (selectedStatusFilter) {
                "Todos" -> true
                "🚨 Vencidos" -> dueStatus.isExpired
                "⏰ Por Vencer (1d)" -> dueStatus.isDueSoon
                else -> client.operationalStatus.equals(selectedStatusFilter, ignoreCase = true)
            }

            matchesQuery && matchesStatus
        }
    }

    val expiredCount = remember(clients, payments) {
        clients.count { CommunicationHelper.getClientMembershipDueStatus(it, payments).isExpired }
    }
    val dueSoonCount = remember(clients, payments) {
        clients.count { CommunicationHelper.getClientMembershipDueStatus(it, payments).isDueSoon }
    }

    Box(modifier = Modifier.fillMaxSize().background(BlackBackground)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
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
                        Text("DIRECTORIO GENERAL", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Atletas Registrados", color = TextLightGray, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text("${clients.size} usuarios en base de datos", color = TextMuted, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onRegisterNewClient,
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("NUEVO", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Banner de Alerta de Cobros Pendientes
                if (expiredCount > 0 || dueSoonCount > 0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedStatusFilter = if (expiredCount > 0) "🚨 Vencidos" else "⏰ Por Vencer (1d)"
                            },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF221111)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text("⚠️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ALERTAS DE COBRO AUTOMÁTICAS",
                                        color = Color(0xFFF87171),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "$expiredCount atletas vencidos • $dueSoonCount por vencer (1 día)",
                                        color = TextLightGray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Text(
                                text = "VER FILTRO →",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por nombre, teléfono o ID...", color = TextMuted, fontSize = 12.sp) },
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

            // Status Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(statusFilters) { status ->
                        val isSelected = selectedStatusFilter == status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LimeGreen else SurfaceElevated)
                                .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedStatusFilter = status }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = status,
                                color = if (isSelected) Color.Black else TextLightGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Client Cards
            if (filteredClients.isEmpty()) {
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
                                Icon(Icons.Default.Person, contentDescription = null, tint = TextMuted, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No se encontraron atletas.", color = TextMuted, fontSize = 13.sp)
                                Text("Ajusta los filtros o añade un nuevo usuario.", color = TextMuted.copy(alpha = 0.7f), fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredClients) { client ->
                    val dueStatus = remember(client, payments) {
                        CommunicationHelper.getClientMembershipDueStatus(client, payments)
                    }
                    val streakInfo = remember(client.id, attendances) {
                        CommunicationHelper.calculateWeeklyStreak(client.id, attendances)
                    }

                    val borderColor = when {
                        dueStatus.isExpired -> Color(0xFFEF4444)
                        dueStatus.isDueSoon -> Color(0xFFF59E0B)
                        else -> SurfaceBorder
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable { onOpenClient(client) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceElevated)
                                            .border(
                                                1.dp,
                                                if (dueStatus.isExpired) Color(0xFFEF4444) else if (dueStatus.isDueSoon) Color(0xFFF59E0B) else LimeGreen.copy(alpha = 0.3f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        ClientAvatarImage(
                                            avatarUrl = client.avatarUrl,
                                            fullName = client.fullName,
                                            fallbackColor = if (dueStatus.isExpired) Color(0xFFF87171) else if (dueStatus.isDueSoon) Color(0xFFFBBF24) else LimeGreen,
                                            fallbackTextSize = 14.sp,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = client.fullName,
                                            color = TextLightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "ID: ${client.accessId} • ${client.phone}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${client.membershipPlan} (${client.paymentFrequency})",
                                            color = LimeGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (streakInfo.weeksCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFF331604))
                                                    .border(1.dp, Color(0xFFF97316).copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = "🔥 ${streakInfo.weeksCount} sem",
                                                    color = Color(0xFFFB923C),
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Black,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        StatusBadge(status = client.operationalStatus)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (onOpenSocialProfile != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(LimeGreen.copy(alpha = 0.15f))
                                                    .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                                    .clickable { onOpenSocialProfile(client) }
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text("📸 Perfil Fit", color = LimeGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable { onOpenClient(client) }
                                        ) {
                                            Text("Ficha", color = TextMuted, fontSize = 10.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }

                            // Badge de Alerta de Cobro y Botón de 1 Clic
                            if (dueStatus.isExpired) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E1111))
                                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text("🔴", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = dueStatus.statusLabel.uppercase(),
                                                color = Color(0xFFF87171),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "Corte: ${dueStatus.formattedDueDate} • Monto: $${String.format(Locale.US, "%.2f", dueStatus.feeAmount)}",
                                                color = TextLightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF25D366))
                                            .clickable {
                                                val msg = CommunicationHelper.buildMembershipReminderMessage(
                                                    client = client,
                                                    dueDateFormatted = dueStatus.formattedDueDate,
                                                    amountDue = dueStatus.feeAmount
                                                )
                                                CommunicationHelper.sendWhatsApp(context, client.phone, msg)
                                                Toast.makeText(context, "Abriendo WhatsApp para ${client.fullName}...", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("📲", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "RECORDAR PAGO",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            } else if (dueStatus.isDueSoon) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E1F07))
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Text("⏰", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = dueStatus.statusLabel.uppercase(),
                                                color = Color(0xFFFBBF24),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = "Corte: ${dueStatus.formattedDueDate} • Monto: $${String.format(Locale.US, "%.2f", dueStatus.feeAmount)}",
                                                color = TextLightGray,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFF25D366))
                                            .clickable {
                                                val msg = CommunicationHelper.buildMembershipReminderMessage(
                                                    client = client,
                                                    dueDateFormatted = dueStatus.formattedDueDate,
                                                    amountDue = dueStatus.feeAmount
                                                )
                                                CommunicationHelper.sendWhatsApp(context, client.phone, msg)
                                                Toast.makeText(context, "Abriendo WhatsApp para ${client.fullName}...", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("📲", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "RECORDAR PAGO",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Vigencia: hasta ${dueStatus.formattedDueDate} (${dueStatus.daysRemaining} días restantes)",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Register New Client
        FloatingActionButton(
            onClick = onRegisterNewClient,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = LimeGreen,
            contentColor = Color.Black,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Registrar Atleta", modifier = Modifier.size(28.dp))
        }
    }
}
