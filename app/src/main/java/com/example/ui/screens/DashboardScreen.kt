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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Routine
import com.example.data.sync.FirebaseBackupManager
import com.example.data.sync.FirebaseRealtimeSyncEngine
import com.example.ui.components.ManageAdminsDialog
import com.example.ui.components.MasterLibrarySection
import com.example.ui.components.NotificationBellButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

@Composable
fun DashboardScreen(
    totalClients: Int,
    activeClients: Int,
    todayCheckIns: Int,
    totalIncome: Double,
    recentAttendances: List<Attendance>,
    clients: List<Client>,
    routines: List<Routine>,
    onSaveRoutine: (Routine) -> Unit,
    onDeleteRoutine: (Routine) -> Unit,
    onRotateRoutine: (targetType: String, clientName: String?, onResult: (Routine?) -> Unit) -> Unit,
    onRequestFirebaseBackup: suspend () -> FirebaseBackupManager.BackupSummary,
    onSyncFirebase: ((FirebaseBackupManager.CloudSyncResult) -> Unit) -> Unit = {},
    syncStatus: FirebaseRealtimeSyncEngine.SyncStatus = FirebaseRealtimeSyncEngine.SyncStatus.Connecting,
    onOpenRegister: () -> Unit,
    onOpenClient: (Client) -> Unit,
    onQuickCheckIn: () -> Unit,
    onNavigateToTab: (Int) -> Unit,
    onOpenBroadcast: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenLeadership: () -> Unit,
    onOpenResources: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    unreadSystem: Boolean = false,
    unreadSocial: Boolean = false,
    totalUnreadCount: Int = 0,
    onUpdateAdminPassword: ((String, (Boolean) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    var showManageAdminsDialog by remember { mutableStateOf(false) }

    if (showManageAdminsDialog) {
        ManageAdminsDialog(
            onDismiss = { showManageAdminsDialog = false },
            onUpdatePassword = onUpdateAdminPassword
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Hero Card: Actitud Fuerte Call To Action
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PANEL ADMINISTRATIVO",
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Sala de Musculación",
                                color = TextLightGray,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        val (badgeText, badgeColor, textColor) = when (syncStatus) {
                            is FirebaseRealtimeSyncEngine.SyncStatus.Live -> Triple("🟢 NUBE EN VIVO", LimeGreen, Color.Black)
                            is FirebaseRealtimeSyncEngine.SyncStatus.Syncing -> Triple("🔄 TRANSMITIENDO...", Color(0xFFFF9100), Color.Black)
                            is FirebaseRealtimeSyncEngine.SyncStatus.Connecting -> Triple("🟡 CONECTANDO...", Color(0xFFF5B041), Color.Black)
                            is FirebaseRealtimeSyncEngine.SyncStatus.Error -> Triple("🔴 NUBE LOCAL", Color(0xFFFF5252), Color.White)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de Notificaciones del Administrador en el Dashboard
                            NotificationBellButton(
                                hasUnreadSystem = unreadSystem,
                                hasUnreadSocial = unreadSocial,
                                totalUnreadCount = totalUnreadCount,
                                onClick = onOpenNotifications
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(badgeText, color = textColor, fontSize = 9.5.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botones de Acción del Panel: Registrar Atleta y Gestionar Administradores
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenRegister,
                            modifier = Modifier.weight(1.15f),
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("REGISTRAR ATLETA", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showManageAdminsDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, LimeGreen),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp), tint = LimeGreen)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("ADMINS", fontWeight = FontWeight.Black, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ESTADO GENERAL (4 KPI Cards replica de Estado general.jpg)
        item {
            Text("ESTADO GENERAL", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(10.dp))

            // Row 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Socios Activos
                EstadoGeneralCard(
                    title = "Socios Activos",
                    value = "$activeClients / $totalClients",
                    subtitle = "Con membresía al día",
                    icon = Icons.Default.Groups,
                    iconTint = LimeGreen,
                    iconBg = Color(0xFF26331C),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(4) }
                )
                // Asistencia Hoy
                EstadoGeneralCard(
                    title = "Asistencia Hoy",
                    value = "$todayCheckIns",
                    subtitle = "Entradas registradas",
                    icon = Icons.Default.HowToReg,
                    iconTint = Color(0xFF38BDF8),
                    iconBg = Color(0xFF163338),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(1) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Ingresos Mes
                EstadoGeneralCard(
                    title = "Ingresos Mes",
                    value = "$${String.format(Locale.US, "%.0f", totalIncome)}",
                    subtitle = "Cobrado en el periodo",
                    icon = Icons.Default.AttachMoney,
                    iconTint = LimeGreen,
                    iconBg = Color(0xFF26331C),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) }
                )
                // Por cobrar/vencidos
                EstadoGeneralCard(
                    title = "Por cobrar/vencidos",
                    value = "${clients.size}",
                    subtitle = "0 ya vencidos",
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFF5B041),
                    iconBg = Color(0xFF4A3816),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToTab(2) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ALERTAS DE COBRO Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFE74C3C),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ALERTAS DE COBRO (${clients.size})",
                    color = Color(0xFFE74C3C),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // List of Alertas de Cobro clients
        if (clients.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No hay alertas de cobro pendientes.", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            items(clients) { client ->
                val amount = client.membershipPlan.filter { it.isDigit() }.ifEmpty { "12" }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onOpenClient(client) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161A14)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF5A4518))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "POR VENCER",
                                    color = Color(0xFFF5B041),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = client.fullName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Cuota: $$amount.0 •",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        // WhatsApp Pay button with Pago Móvil data
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222B1C))
                                .border(1.dp, LimeGreen.copy(alpha = 0.5f), CircleShape)
                                .clickable {
                                    val msg = """
                                        ¡Hola ${client.fullName}! Te escribimos de Actitud Fuerte para recordarte el pago de tu ${client.membershipPlan} ($$amount.0 a tasa BCV).
                                        
                                        Datos de Pago Móvil:
                                        • Banco: Provincial
                                        • Cédula: 17380859
                                        • Teléfono: 04145529674
                                        
                                        Por favor envíanos tu comprobante. ¡Gracias por entrenar con nosotros!
                                    """.trimIndent()
                                    val encoded = Uri.encode(msg)
                                    val phoneClean = client.phone.replace("+", "").replace(" ", "")
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phoneClean&text=$encoded"))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar Cobro WhatsApp",
                                tint = LimeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Gestión, Comunicación Oficial y Reportes (Réplica 3 Botones)
        item {
            Text(
                "COMUNICACIÓN Y GESTIÓN OFICIAL",
                color = TextLightGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            com.example.ui.components.ManagementActionButtons(
                onOpenBroadcast = onOpenBroadcast,
                onOpenReports = onOpenReports,
                onOpenLeadership = onOpenLeadership,
                onOpenResources = onOpenResources
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Biblioteca Maestra (Reemplazo exacto de "ACCESO DIRECTO A SALA")
        item {
            MasterLibrarySection(
                routines = routines,
                clients = clients,
                onSaveRoutine = onSaveRoutine,
                onDeleteRoutine = onDeleteRoutine,
                onRotateRoutine = onRotateRoutine,
                onRequestFirebaseBackup = onRequestFirebaseBackup,
                onSyncFirebase = onSyncFirebase
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Recent Activity / Atletas en sala hoy
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ASISTENCIAS RECIENTES", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Ver todas (${recentAttendances.size})",
                    color = LimeGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToTab(1) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (recentAttendances.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay check-ins registrados hoy aún.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(recentAttendances.take(5)) { att ->
                val client = clients.firstOrNull { it.id == att.clientId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceDark)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .clickable { if (client != null) onOpenClient(client) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LimeGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(att.clientName, color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Check-in: ${att.timeOnlyString}", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun EstadoGeneralCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(116.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(13.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextLightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Column {
                Text(
                    text = value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
