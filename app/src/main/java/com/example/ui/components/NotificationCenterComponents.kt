package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AdminAnnouncement
import com.example.data.model.AppNotification
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.NotificationType
import com.example.data.model.Payment
import com.example.data.model.SocialPost
import com.example.data.model.idNumber
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

/**
 * Botón visible de notificación para la barra superior (BrandHeader).
 * Muestra insignias cromáticas diferenciadas:
 * - Ámbar / Dorado (#F59E0B) para notificaciones importantes del SISTEMA (anuncios, membresía, seguridad).
 * - Cyan Eléctrico (#00F0FF) para notificaciones de interacción SOCIAL (likes, comentarios, menciones).
 */
@Composable
fun NotificationBellButton(
    hasUnreadSystem: Boolean,
    hasUnreadSocial: Boolean,
    totalUnreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        hasUnreadSystem && hasUnreadSocial -> Color(0xFFF59E0B)
        hasUnreadSystem -> Color(0xFFF59E0B)
        hasUnreadSocial -> Color(0xFF00F0FF)
        else -> SurfaceBorder
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceElevated)
            .border(1.dp, borderColor.copy(alpha = if (totalUnreadCount > 0) 0.8f else 0.4f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 7.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Icon(
                    imageVector = if (totalUnreadCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = "Centro de Notificaciones",
                    tint = when {
                        hasUnreadSystem -> Color(0xFFF59E0B)
                        hasUnreadSocial -> Color(0xFF00F0FF)
                        else -> TextLightGray
                    },
                    modifier = Modifier.size(17.dp)
                )

                // Indicador de Puntos cromáticos
                if (hasUnreadSystem || hasUnreadSocial) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (hasUnreadSystem) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B)) // Dorado / Ámbar Sistema
                            )
                        }
                        if (hasUnreadSocial) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00F0FF)) // Cyan Eléctrico Social
                            )
                        }
                    }
                }
            }

            if (totalUnreadCount > 0) {
                Text(
                    text = if (totalUnreadCount > 99) "99+" else "$totalUnreadCount",
                    color = if (hasUnreadSystem) Color(0xFFF59E0B) else Color(0xFF00F0FF),
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        }
    }
}

/**
 * Diálogo modal del Centro de Notificaciones con distinción cromática explícita:
 * Sistema (Ámbar/Dorado) vs Social (Cyan/Azul).
 */
@Composable
fun NotificationCenterDialog(
    notifications: List<AppNotification>,
    onDismiss: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit
) {
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: Todas, 1: Sistema, 2: Social

    val filteredNotifications = remember(notifications, selectedFilterTab) {
        when (selectedFilterTab) {
            1 -> notifications.filter { it.type == NotificationType.SYSTEM }
            2 -> notifications.filter { it.type == NotificationType.SOCIAL }
            else -> notifications
        }
    }

    val unreadSystemCount = remember(notifications) {
        notifications.count { it.type == NotificationType.SYSTEM && !it.isRead }
    }
    val unreadSocialCount = remember(notifications) {
        notifications.count { it.type == NotificationType.SOCIAL && !it.isRead }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "CENTRO DE NOTIFICACIONES",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Actividad del sistema y de la comunidad",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (notifications.any { !it.isRead }) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { onMarkAllAsRead() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("LEER TODAS", color = LimeGreen, fontWeight = FontWeight.Bold, fontSize = 8.5.sp)
                            }
                        }
                    }
                }

                // Leyenda Explicativa de Colores
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceElevated)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "SISTEMA & COMUNICADOS",
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00F0FF))
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "COMUNIDAD & RED SOCIAL",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                // Selector de Categoría (Pestañas)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tabs = listOf(
                        Triple(0, "TODAS (${notifications.size})", LimeGreen),
                        Triple(1, "⚡ SISTEMA ($unreadSystemCount)", Color(0xFFF59E0B)),
                        Triple(2, "💬 SOCIAL ($unreadSocialCount)", Color(0xFF00F0FF))
                    )

                    tabs.forEach { (index, title, tabColor) ->
                        val isSelected = selectedFilterTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) tabColor.copy(alpha = 0.18f) else SurfaceElevated)
                                .border(1.dp, if (isSelected) tabColor else SurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedFilterTab = index }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) tabColor else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                // Lista de Notificaciones
                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔔", fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Sin notificaciones en esta categoría",
                                color = TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Los avisos de la administración y las interacciones sociales aparecerán aquí.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotifications, key = { it.id }) { notif ->
                            NotificationItemCard(
                                notification = notif,
                                onClick = { onNotificationClick(notif) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val isSystem = notification.type == NotificationType.SYSTEM
    val accentColor = if (isSystem) Color(0xFFF59E0B) else Color(0xFF00F0FF)
    val cardBg = if (notification.isRead) SurfaceDark else SurfaceElevated

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (!notification.isRead) accentColor.copy(alpha = 0.65f) else SurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono / Badge representativo
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (notification.customEmoji.isNotBlank()) notification.customEmoji else notification.type.iconEmoji,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge de categoría
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = notification.type.displayName,
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Text(
                        text = notification.relativeTimeString,
                        color = TextMuted,
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    color = TextLightGray,
                    fontSize = 10.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Punto de no leído
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
    }
}

/**
 * Genera notificaciones en tiempo real para el usuario conectado
 * discriminando entre alertas del Sistema (Ámbar) e interacciones Sociales (Cyan).
 * Admite contexto completo de Asistencias, Pagos, Membresías y Comunicados.
 */
fun buildAppNotifications(
    currentClient: Client?,
    allPosts: List<SocialPost>,
    announcements: List<AdminAnnouncement>,
    allClients: List<Client> = emptyList(),
    allAttendances: List<Attendance> = emptyList(),
    allPayments: List<Payment> = emptyList(),
    isAdmin: Boolean = false
): List<AppNotification> {
    val list = mutableListOf<AppNotification>()

    // 1. Comunicados Oficiales (SISTEMA - Ámbar)
    announcements.filter { it.isCurrentlyLive }.forEach { ann ->
        list.add(
            AppNotification(
                id = "ann_${ann.id}",
                title = "📢 Comunicado Oficial",
                message = ann.contentText.ifBlank { "Nuevo comunicado oficial emitido por ${ann.authorName}." },
                type = NotificationType.SYSTEM,
                timestamp = ann.scheduledPublishTime.coerceAtLeast(ann.timestamp),
                customEmoji = ann.theme.emoji,
                actionTarget = "ANNOUNCEMENT",
                targetId = ann.id
            )
        )
    }

    if (isAdmin) {
        // Alertas específicas para Administrador
        // a) Asistencias de hoy
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayAttendances = allAttendances.filter { it.dateOnlyString == today }
        if (todayAttendances.isNotEmpty()) {
            list.add(
                AppNotification(
                    id = "admin_attendance_today",
                    title = "🏋️ Asistencias de Hoy (${todayAttendances.size})",
                    message = "${todayAttendances.size} atleta(s) han registrado check-in hoy. Toca para ver la lista en Asistencias.",
                    type = NotificationType.SYSTEM,
                    timestamp = todayAttendances.maxOf { it.timestamp },
                    customEmoji = "📋",
                    actionTarget = "ATTENDANCE",
                    targetId = 0L
                )
            )
        }

        // b) Pagos registrados
        if (allPayments.isNotEmpty()) {
            val latestPayment = allPayments.maxByOrNull { it.timestamp }
            if (latestPayment != null) {
                val client = allClients.firstOrNull { it.id == latestPayment.clientId }
                val amountFormatted = String.format(Locale.US, "%.0f", latestPayment.amount)
                list.add(
                    AppNotification(
                        id = "admin_pay_${latestPayment.id}",
                        title = "💵 Pago Registrado: $$amountFormatted",
                        message = "Cobro registrado para ${client?.fullName ?: "Atleta"}. Toca para abrir el panel de Pagos & Finanzas.",
                        type = NotificationType.SYSTEM,
                        timestamp = latestPayment.timestamp,
                        customEmoji = "💳",
                        actionTarget = "PAYMENT",
                        targetId = latestPayment.clientId
                    )
                )
            }
        }

        // c) Atletas en el sistema
        allClients.sortedByDescending { it.registrationTimestamp }.take(2).forEach { c ->
            list.add(
                AppNotification(
                    id = "admin_client_${c.id}",
                    title = "⭐ Ficha de Atleta: ${c.fullName}",
                    message = "Plan: ${c.membershipPlan} • ID: ${c.idNumber}. Toca para consultar su Ficha Técnica.",
                    type = NotificationType.SYSTEM,
                    timestamp = c.registrationTimestamp,
                    customEmoji = "👤",
                    actionTarget = "MEMBERSHIP",
                    targetId = c.id
                )
            )
        }
    } else {
        // Alertas específicas para Atletas
        // a) Estado de Membresía y Carnet Digital
        if (currentClient != null && currentClient.id > 0) {
            list.add(
                AppNotification(
                    id = "membership_active_${currentClient.id}",
                    title = "🛡️ Membresía Actitud Fuerte",
                    message = "Plan: ${currentClient.membershipPlan} • Estado: ${currentClient.operationalStatus}. Toca para ver tu Carnet Digital.",
                    type = NotificationType.SYSTEM,
                    timestamp = System.currentTimeMillis() - 86400000L,
                    customEmoji = "🪪",
                    actionTarget = "MEMBERSHIP",
                    targetId = currentClient.id
                )
            )

            // b) Último entrenamiento registrado
            val myAttendances = allAttendances.filter { it.clientId == currentClient.id }
            if (myAttendances.isNotEmpty()) {
                val lastAtt = myAttendances.maxByOrNull { it.timestamp }
                if (lastAtt != null) {
                    list.add(
                        AppNotification(
                            id = "my_att_${lastAtt.id}",
                            title = "🏋️ Entrenamiento Registrado",
                            message = "¡Check-in confirmado para el ${lastAtt.dateOnlyString}! Toca para ver tu Historial de Cargas.",
                            type = NotificationType.SYSTEM,
                            timestamp = lastAtt.timestamp,
                            customEmoji = "🔥",
                            actionTarget = "ATTENDANCE",
                            targetId = currentClient.id
                        )
                    )
                }
            }
        }
    }

    // 3. Interacciones Sociales (SOCIAL - Cyan)
    val myPosts = if (currentClient != null) allPosts.filter { it.authorId == currentClient.id } else emptyList()
    myPosts.forEach { post ->
        if (post.likesCount > 0) {
            list.add(
                AppNotification(
                    id = "like_post_${post.id}",
                    title = "❤️ Interacción en tu Publicación",
                    message = "A ${post.likesCount} atleta(s) les gustó tu publicación '${post.contentText.take(30)}...'",
                    type = NotificationType.SOCIAL,
                    timestamp = post.timestamp,
                    customEmoji = "🔥",
                    actionTarget = "POST",
                    targetId = post.id
                )
            )
        }
        val comments = post.getCommentsList().filter { it.authorId != (currentClient?.id ?: 0L) }
        comments.take(3).forEach { c ->
            list.add(
                AppNotification(
                    id = "comm_${c.id}",
                    title = "💬 Nuevo Comentario de ${c.authorName}",
                    message = "\"${c.text}\" en tu publicación.",
                    type = NotificationType.SOCIAL,
                    timestamp = c.timestamp,
                    customEmoji = "💬",
                    actionTarget = "POST",
                    targetId = post.id
                )
            )
        }
    }

    // Otras publicaciones recientes de la comunidad
    allPosts.filter { it.category != "ANUNCIO_OFICIAL" && it.authorId != (currentClient?.id ?: 0L) }
        .take(3)
        .forEach { post ->
            list.add(
                AppNotification(
                    id = "comm_feed_${post.id}",
                    title = "⚡ ${post.authorName} compartió un post",
                    message = post.contentText.take(50),
                    type = NotificationType.SOCIAL,
                    timestamp = post.timestamp,
                    customEmoji = "🏋️",
                    actionTarget = "POST",
                    targetId = post.id
                )
            )
        }

    return list.sortedByDescending { it.timestamp }
}
