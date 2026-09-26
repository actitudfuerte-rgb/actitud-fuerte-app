package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.Client
import com.example.data.model.Payment
import com.example.ui.components.CommunicationHelper
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

@Composable
fun PaymentsScreen(
    payments: List<Payment>,
    clients: List<Client>,
    totalIncome: Double,
    onRecordPayment: (Payment) -> Unit,
    onOpenClient: (Client) -> Unit
) {
    val context = LocalContext.current
    var showQuickPaymentDialog by remember { mutableStateOf(false) }
    var selectedClientForPayment by remember { mutableStateOf<Client?>(null) }

    val plan12Count = clients.count { it.membershipPlan.contains("12") }
    val plan20Count = clients.count { it.membershipPlan.contains("20") }

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
                    Text("GESTIÓN FINANCIERA", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text("Control de Pagos", color = TextLightGray, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Text("Membresías $12 y $20", color = TextMuted, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        if (clients.isNotEmpty()) {
                            selectedClientForPayment = clients.first()
                            showQuickPaymentDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("REGISTRAR COBRO", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Resumen Financiero
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RECAUDACIÓN TOTAL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$${String.format(Locale.US, "%.2f", totalIncome)}",
                        color = LimeGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Membresía 12$", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$plan12Count atletas asignados", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text("Membresía 20$", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("$plan20Count atletas asignados", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tarjeta Estratégica de Pago Móvil Oficial
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("MÉTODO DE PAGO MÓVIL DISPONIBLE", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LimeGreen.copy(alpha = 0.15f))
                                .clickable { CommunicationHelper.copyPagoMovilToClipboard(context) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = LimeGreen, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("COPIAR DATOS", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(SurfaceElevated).padding(8.dp)) {
                            Column {
                                Text("Banco", color = TextMuted, fontSize = 9.sp)
                                Text(CommunicationHelper.PAGO_MOVIL_BANK, color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(SurfaceElevated).padding(8.dp)) {
                            Column {
                                Text("Cédula", color = TextMuted, fontSize = 9.sp)
                                Text(CommunicationHelper.PAGO_MOVIL_ID, color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Box(modifier = Modifier.weight(1.3f).clip(RoundedCornerShape(8.dp)).background(SurfaceElevated).padding(8.dp)) {
                            Column {
                                Text("Teléfono", color = TextMuted, fontSize = 9.sp)
                                Text(CommunicationHelper.PAGO_MOVIL_PHONE, color = LimeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "⚡ ${CommunicationHelper.PAGO_MOVIL_RATE_NOTE}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Sección de Cobros Pendientes / Vencidos
        val pendingClients = clients.map { client ->
            client to CommunicationHelper.getClientMembershipDueStatus(client, payments)
        }.filter { it.second.isExpired || it.second.isDueSoon }
            .sortedBy { it.second.daysRemaining }

        if (pendingClients.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "COBROS PENDIENTES (${pendingClients.size})",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        "Recordatorio con 1 clic",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(pendingClients) { (client, dueStatus) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (dueStatus.isExpired) Color(0xFF221111) else Color(0xFF221808)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (dueStatus.isExpired) Color(0xFFEF4444).copy(alpha = 0.7f) else Color(0xFFF59E0B).copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (dueStatus.isExpired) "🔴" else "⏰", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = client.fullName,
                                    color = TextLightGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${dueStatus.statusLabel.uppercase()} • Corte: ${dueStatus.formattedDueDate} • $${String.format(Locale.US, "%.2f", dueStatus.feeAmount)}",
                                color = if (dueStatus.isExpired) Color(0xFFF87171) else Color(0xFFFBBF24),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF25D366))
                                .clickable {
                                    val reminderMsg = CommunicationHelper.buildMembershipReminderMessage(
                                        client = client,
                                        dueDateFormatted = dueStatus.formattedDueDate,
                                        amountDue = dueStatus.feeAmount
                                    )
                                    CommunicationHelper.sendWhatsApp(context, client.phone, reminderMsg)
                                    android.widget.Toast.makeText(context, "Abriendo WhatsApp para ${client.fullName}...", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📲", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "RECORDAR",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Historial de Transacciones
        item {
            Text("HISTORIAL DE TRANSACCIONES (${payments.size})", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (payments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay registros de cobros aún.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(payments) { p ->
                val dateStr = SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault()).format(Date(p.timestamp))
                val client = clients.firstOrNull { it.id == p.clientId }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { if (client != null) onOpenClient(client) }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LimeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(p.clientName, color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${p.planName} • ${p.frequency} • ${p.method}", color = TextMuted, fontSize = 11.sp)
                            Text(dateStr, color = TextMuted.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+$${String.format(Locale.US, "%.0f", p.amount)}",
                                color = LimeGreen,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (p.reference.isNotBlank()) {
                                Text(p.reference, color = TextMuted, fontSize = 9.sp)
                            }
                        }

                        if (client != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    val receipt = CommunicationHelper.buildPaymentReceiptMessage(client, p)
                                    CommunicationHelper.sendWhatsApp(context, client.phone, receipt)
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = "Enviar Recibo WA",
                                    tint = LimeGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showQuickPaymentDialog && selectedClientForPayment != null) {
        RecordPaymentDialog(
            client = selectedClientForPayment!!,
            onDismiss = { showQuickPaymentDialog = false },
            onSavePayment = {
                onRecordPayment(it)
                showQuickPaymentDialog = false
            }
        )
    }
}
