package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.ByteArrayOutputStream
import com.example.data.manager.AdminConfigManager
import com.example.data.model.AdminAnnouncement
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.PresetMediaStyle
import com.example.data.model.Routine
import com.example.data.model.SocialComment
import com.example.data.model.SocialPost
import com.example.data.model.SocialPostConstants
import com.example.ui.components.AdminAnnouncementPinnedCard
import com.example.ui.components.AvatarCropDialog
import com.example.ui.components.ClientAvatarImage
import com.example.ui.components.CreateAdminAnnouncementDialog
import com.example.ui.components.NotificationBellButton
import com.example.ui.components.ScheduledAnnouncementsDialog
import com.example.ui.components.DigitalIdCardDialog
import com.example.ui.components.PostMediaImage
import com.example.ui.components.ResourcesHubDialog
import com.example.ui.components.WeeklyRoutineDetailDialog
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

/**
 * PANTALLA DE PERFIL DE ATLETA (FASE 1 - ESTILO INSTAGRAM)
 * Integra cabecera de estadísticas en tiempo real, anillo de historias activo (24h),
 * estandarte de fuerza (PR Insignia), alias deportivo, estatus social exclusivo,
 * biografía fitness, botones de acción rápida ("Editar Perfil") y cuadrícula/feed de publicaciones verticales 3:4.
 */
@Composable
fun AthleteProfileContent(
    client: Client,
    posts: List<SocialPost>,
    attendances: List<Attendance>,
    routines: List<Routine> = emptyList(),
    allPosts: List<SocialPost> = emptyList(),
    allClients: List<Client> = emptyList(),
    isAdmin: Boolean = false,
    viewerClient: Client? = null,
    showBackButton: Boolean = true,
    onDismiss: () -> Unit = {},
    onCreatePost: (SocialPost) -> Unit,
    onToggleLike: (Long) -> Unit,
    onDeletePost: (SocialPost) -> Unit,
    onSaveRoutine: (String) -> Unit,
    onUpdateClient: ((Client) -> Unit)? = null,
    onAddComment: ((Long, String, String, String) -> Unit)? = null,
    onToggleLikeComment: ((Long, String) -> Unit)? = null,
    onOpenAuthorProfile: ((Client) -> Unit)? = null,
    onOpenNotifications: () -> Unit = {},
    unreadSystem: Boolean = false,
    unreadSocial: Boolean = false,
    totalUnreadCount: Int = 0
) {
    val context = LocalContext.current
    var currentClient by remember(client) { mutableStateOf(client) }
    val currentViewer = viewerClient ?: currentClient
    val effectiveIsAdmin = remember(isAdmin) { isAdmin }
    val isOwnProfile = remember(currentViewer, client) {
        (currentViewer.id != 0L && currentViewer.id == client.id) ||
        (currentViewer.accessId.isNotBlank() && currentViewer.accessId.equals(client.accessId, ignoreCase = true)) ||
        (currentViewer.fullName.isNotBlank() && currentViewer.fullName.equals(client.fullName, ignoreCase = true))
    }
    val canManageProfile = isOwnProfile || effectiveIsAdmin
    val canDeletePost = remember(effectiveIsAdmin, currentViewer) {
        { post: SocialPost ->
            effectiveIsAdmin || post.authorId == currentViewer.id || (currentViewer.accessId.isNotBlank() && post.authorAccessId == currentViewer.accessId)
        }
    }
    var feedTab by remember { mutableIntStateOf(0) } // 0: Mis Publicaciones, 1: Muro de la Comunidad
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Grid 3:4, 1: Feed lista

    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var showIdCardDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showResourcesDialog by remember { mutableStateOf(false) }
    var showCreateAnnouncementDialog by remember { mutableStateOf(false) }
    var showScheduledAnnouncementsDialog by remember { mutableStateOf(false) }
    var selectedPostForDetail by remember { mutableStateOf<SocialPost?>(null) }
    var selectedPostForComments by remember { mutableStateOf<SocialPost?>(null) }
    var showStoryViewerDialog by remember { mutableStateOf(false) }

    // Detección de Anuncios Administrativos: Vigentes vs Programados a Futuro
    val allAnnouncements = remember(allPosts) {
        allPosts
            .filter { it.category == "ANUNCIO_OFICIAL" }
            .map { AdminAnnouncement.fromSocialPost(it) }
    }
    val scheduledAnnouncements = remember(allAnnouncements) {
        allAnnouncements.filter { it.isScheduled }
    }
    val activeAnnouncement = remember(allAnnouncements) {
        allAnnouncements
            .filter { it.isCurrentlyLive }
            .maxByOrNull { it.scheduledPublishTime }
    }

    // Cálculo de racha de asistencia (días consecutivos aproximados)
    val streakDays = remember(attendances) {
        if (attendances.isEmpty()) 0 else (attendances.size * 2).coerceAtMost(28)
    }

    val displayPosts = remember(feedTab, posts, allPosts, client) {
        val raw = if (feedTab == 0) {
            if (allPosts.isNotEmpty()) allPosts.filter { it.authorId == client.id } else posts
        } else {
            if (allPosts.isNotEmpty()) allPosts else posts
        }
        raw.filter { 
            it.category != "HISTORIA_24H" && 
            it.category != "HISTORIA" && 
            it.category != "ANUNCIO_OFICIAL" 
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar de Navegación limpia y minimalista (protegida contra deformaciones)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (showBackButton) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextLightGray)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⚡", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("MI PERFIL FIT", color = LimeGreen, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(LimeGreen.copy(alpha = 0.15f))
                                .border(1.dp, LimeGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = currentClient.accessId.ifBlank { "AF-1001" },
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        if (canManageProfile) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = {
                                showEditProfileDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Perfil", tint = LimeGreen)
                            }
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                // 2. Contenido del Perfil
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Cabecera Instagram: Avatar con Anillo de Historia + Estadísticas
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Avatar circular con Anillo de Historias activo degradado
                                Box(
                                    modifier = Modifier
                                        .size(86.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.sweepGradient(
                                                listOf(
                                                    LimeGreen,
                                                    Color(0xFF38BDF8),
                                                    Color(0xFFFBBF24),
                                                    LimeGreen
                                                )
                                            )
                                        )
                                        .padding(3.dp)
                                        .clickable { showStoryViewerDialog = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(BlackBackground)
                                            .padding(3.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(SurfaceElevated),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            ClientAvatarImage(
                                                avatarUrl = currentClient.avatarUrl,
                                                fullName = currentClient.fullName,
                                                fallbackTextSize = 24.sp,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }

                                // Contadores de Estadísticas (Posts, Asistencias, Racha)
                                Row(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProfileStatColumn(count = posts.size.toString(), label = "Posts")
                                    ProfileStatColumn(count = attendances.size.toString(), label = "Entrenos")
                                    ProfileStatColumn(count = "$streakDays días", label = "Racha 💪")
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Nombre del Usuario
                            Text(
                                text = currentClient.fullName,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Biografía
                            Text(
                                text = currentClient.bio.ifBlank { "Atleta de Alto Rendimiento • Sala de Musculación Actitud Fuerte" },
                                color = TextLightGray,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Botones de Acción estilo Instagram (Editar Perfil, Rutina, Carnet QR, Publicar)
                            // Control estricto de privacidad: los visitantes regulares no pueden editar, ni ver datos sensibles (rutina, carnet), ni publicar en perfiles ajenos
                            if (canManageProfile) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    // Botón 1: Editar Perfil
                                    Row(
                                        modifier = Modifier
                                            .weight(1.15f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.2.dp, LimeGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                            .clickable { showEditProfileDialog = true }
                                            .padding(vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Editar Perfil", color = LimeGreen, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                                    }

                                    // Botón 2: Rutina Semanal (Información de entrenamiento)
                                    Row(
                                        modifier = Modifier
                                            .weight(0.95f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                            .clickable { showRoutineDialog = true }
                                            .padding(vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rutina", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // Botón 3: Carnet QR (Datos de membresía y acceso)
                                    Row(
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                            .clickable { showIdCardDialog = true }
                                            .padding(vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Carnet", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(7.dp))

                                // Fila 2: Botón de Recursos (Ecosistema de Descarga de Guías, Audios, Videos) y + Post
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    // Botón Recursos: Disponible para el usuario para descargar archivos, y para el Administrador para cargar y gestionar
                                    Row(
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.2.dp, Color(0xFF00F0FF).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                            .clickable { showResourcesDialog = true }
                                            .padding(vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text("Recursos", color = Color(0xFF00F0FF), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF00F0FF).copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(if (effectiveIsAdmin) "CARGA/DESCARGA" else "GUÍAS & MEDIA", color = Color(0xFF00F0FF), fontSize = 8.sp, fontWeight = FontWeight.Black)
                                        }
                                    }

                                    // Botón Publicar (+ Post) (Solo en perfil propio)
                                    if (isOwnProfile) {
                                        Row(
                                            modifier = Modifier
                                                .weight(0.7f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(LimeGreen)
                                                .clickable { showCreatePostDialog = true }
                                                .padding(vertical = 9.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Post", color = Color.Black, fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            } else {
                                // Vista de perfil ajeno para atleta visitante:
                                // Badge visual elegante + Acceso directo a Recursos Oficiales de Actitud Fuerte
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.FitnessCenter,
                                            contentDescription = null,
                                            tint = LimeGreen,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "PERFIL PÚBLICO",
                                            color = TextLightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .weight(0.85f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDark)
                                            .border(1.2.dp, Color(0xFF00F0FF).copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                            .clickable { showResourcesDialog = true }
                                            .padding(vertical = 9.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Recursos", color = Color(0xFF00F0FF), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                             // 4. Caja de Anuncio Oficial del Administrador (Vigencia 24h, Audio, Adjuntos, Fondo Dinámico y Programados)
                            AdminAnnouncementPinnedCard(
                                announcement = activeAnnouncement,
                                isAdmin = effectiveIsAdmin,
                                scheduledCount = scheduledAnnouncements.size,
                                onOpenCreateDialog = { showCreateAnnouncementDialog = true },
                                onOpenScheduledDialog = { showScheduledAnnouncementsDialog = true },
                                onToggleStrengthAck = { annId -> onToggleLike(annId) },
                                onOpenComments = { annId ->
                                    val post = allPosts.firstOrNull { it.id == annId } ?: posts.firstOrNull { it.id == annId }
                                    if (post != null) {
                                        selectedPostForComments = post
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Si no hay anuncio activo pero el admin tiene anuncios programados a futuro
                            if (activeAnnouncement == null && effectiveIsAdmin && scheduledAnnouncements.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SurfaceDark)
                                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                        .clickable { showScheduledAnnouncementsDialog = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("📅", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${scheduledAnnouncements.size} Anuncio(s) programado(s) a futuro",
                                                color = Color(0xFFF59E0B),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text("VER BANDEJA →", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    // 4.5 Insignias & Hitos de Disciplina (Gamificación del Gimnasio)
                    item {
                        AthleteDisciplineBadgesSection(
                            attendances = attendances,
                            streakDays = streakDays
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // 5. Selector de Pestañas: Mis Publicaciones vs Muro de la Comunidad
                    item {
                        TabRow(
                            selectedTabIndex = feedTab,
                            containerColor = SurfaceDark,
                            contentColor = LimeGreen,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[feedTab]),
                                    color = LimeGreen,
                                    height = 2.dp
                                )
                            }
                        ) {
                            Tab(
                                selected = feedTab == 0,
                                onClick = { feedTab = 0 },
                                text = {
                                    Text(
                                        "👤 Mis Publicaciones",
                                        color = if (feedTab == 0) LimeGreen else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                            Tab(
                                selected = feedTab == 1,
                                onClick = { feedTab = 1 },
                                text = {
                                    Text(
                                        "🌍 Muro de la Comunidad",
                                        color = if (feedTab == 1) LimeGreen else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }

                    // 6. Contenido de las pestañas
                    if (displayPosts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (feedTab == 0) "📸" else "🌍", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (feedTab == 0) "Aún no tienes publicaciones" else "No hay publicaciones en la comunidad",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        if (feedTab == 0) "Comparte tu primer entrenamiento o récord personal." else "Sé el primero en compartir algo con los demás atletas.",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = { showCreatePostDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                                    ) {
                                        Text("+ Crear Publicación (3:4)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(displayPosts) { post ->
                            SocialPostFeedCard(
                                post = post,
                                allClients = allClients,
                                canDelete = canDeletePost(post),
                                onToggleLike = { onToggleLike(post.id) },
                                onDeletePost = { onDeletePost(post) },
                                onOpenComments = { selectedPostForComments = post },
                                onOpenAuthorProfile = onOpenAuthorProfile
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

    // Modal: Visor y Creador de Historias Reales (24 Horas)
    if (showStoryViewerDialog) {
        StoryViewerDialog(
            client = currentClient,
            attendances = attendances,
            allPosts = allPosts,
            onPublishStory = if (isOwnProfile) { { newStory -> onCreatePost(newStory) } } else null,
            onDeleteStory = if (isOwnProfile || effectiveIsAdmin) { { storyToDelete -> onDeletePost(storyToDelete) } } else null,
            onDismiss = { showStoryViewerDialog = false }
        )
    }

    // Modal: Crear Publicación (Admite solo texto tipo X, fotos y videos 3:4)
    // Estrictamente restringido al propio perfil del usuario autenticado
    if (showCreatePostDialog && isOwnProfile) {
        CreateSocialPostDialog(
            client = currentViewer,
            onDismiss = { showCreatePostDialog = false },
            onPublish = { newPost ->
                onCreatePost(newPost)
                showCreatePostDialog = false
                Toast.makeText(context, "¡Publicado en el Feed de Actitud Fuerte! 🔥", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Emitir Comunicado Administrativo Oficial (24 Horas o Programado)
    if (showCreateAnnouncementDialog) {
        CreateAdminAnnouncementDialog(
            currentAdminName = currentClient.fullName,
            onDismiss = { showCreateAnnouncementDialog = false },
            onPublishAnnouncement = { newAnnouncement ->
                val post = newAnnouncement.toSocialPost()
                onCreatePost(post)
                val msg = if (newAnnouncement.isScheduled) {
                    "Comunicado oficial programado para emitirse automáticamente 📅"
                } else {
                    "Comunicado oficial publicado con vigencia de 24h 🔥"
                }
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Bandeja de Comunicados Oficiales Programados a Futuro
    if (showScheduledAnnouncementsDialog) {
        ScheduledAnnouncementsDialog(
            scheduledAnnouncements = scheduledAnnouncements,
            onDismiss = { showScheduledAnnouncementsDialog = false },
            onPublishNow = { item ->
                val updated = item.copy(
                    scheduledPublishTime = System.currentTimeMillis(),
                    expiresAt = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
                )
                onCreatePost(updated.toSocialPost())
                showScheduledAnnouncementsDialog = false
                Toast.makeText(context, "¡Comunicado emitido inmediatamente! 📢", Toast.LENGTH_SHORT).show()
            },
            onDeleteScheduled = { item ->
                onDeletePost(item.toSocialPost())
                showScheduledAnnouncementsDialog = false
                Toast.makeText(context, "Comunicado programado cancelado", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Detalle de Publicación
    if (selectedPostForDetail != null) {
        SocialPostDetailDialog(
            post = selectedPostForDetail!!,
            allClients = allClients,
            canDelete = canDeletePost(selectedPostForDetail!!),
            onDismiss = { selectedPostForDetail = null },
            onToggleLike = {
                onToggleLike(selectedPostForDetail!!.id)
                selectedPostForDetail = selectedPostForDetail!!.copy(
                    isLikedByMe = !selectedPostForDetail!!.isLikedByMe,
                    likesCount = if (selectedPostForDetail!!.isLikedByMe) selectedPostForDetail!!.likesCount - 1 else selectedPostForDetail!!.likesCount + 1
                )
            },
            onDelete = {
                onDeletePost(selectedPostForDetail!!)
                selectedPostForDetail = null
            },
            onOpenComments = {
                val p = selectedPostForDetail!!
                selectedPostForDetail = null
                selectedPostForComments = p
            },
            onOpenAuthorProfile = onOpenAuthorProfile
        )
    }

    // Modal: Comentarios Interactivos de la Comunidad
    if (selectedPostForComments != null) {
        val targetPost = selectedPostForComments!!
        val livePost = allPosts.firstOrNull { it.id == targetPost.id } ?: targetPost
        PostCommentsDialog(
            post = livePost,
            currentClient = currentViewer,
            allClients = allClients,
            onDismiss = { selectedPostForComments = null },
            onAddComment = { commentText, replyId, replyAuthor ->
                onAddComment?.invoke(livePost.id, commentText, replyId, replyAuthor)
            },
            onToggleLikeComment = { commentId ->
                onToggleLikeComment?.invoke(livePost.id, commentId)
            },
            onOpenAuthorProfile = onOpenAuthorProfile
        )
    }

    // Modal: Rutina Asignada (Visor Interactivo Maestro con Día Actual)
    if (showRoutineDialog && canManageProfile) {
        val assignedRoutine = remember(client, routines) {
            val isFemale = client.gender.equals("Mujer", ignoreCase = true) || client.fullName.contains("Carlen", ignoreCase = true)
            val expectedGenderTarget = if (isFemale) "Mujeres" else "Hombres"
            routines.firstOrNull { r ->
                r.targetType.equals("Usuarios específicos", ignoreCase = true) && r.targetClientNames.contains(client.fullName, ignoreCase = true)
            } ?: routines.firstOrNull { r ->
                r.targetType.equals(expectedGenderTarget, ignoreCase = true)
            } ?: routines.firstOrNull { r ->
                r.targetType.equals(if (isFemale) "Hombres" else "Mujeres", ignoreCase = true)
            } ?: routines.firstOrNull() ?: Routine(
                id = 0,
                name = if (isFemale) "Rutina de entrenamiento 2" else "Rutina de entrenamiento 1",
                targetType = expectedGenderTarget,
                specialConditions = client.mainObjective,
                exercisesJson = ""
            )
        }

        WeeklyRoutineDetailDialog(
            routine = assignedRoutine,
            onDismiss = { showRoutineDialog = false }
        )
    }

    // Modal: Carnet QR
    if (showIdCardDialog && canManageProfile) {
        DigitalIdCardDialog(
            client = currentClient,
            onDismiss = { showIdCardDialog = false }
        )
    }

    // Modal: Editar Perfil Fit (Punto 3)
    if (showEditProfileDialog && canManageProfile) {
        EditSocialProfileDialog(
            client = currentClient,
            onDismiss = { showEditProfileDialog = false },
            onSaveProfile = { updated ->
                currentClient = updated
                onUpdateClient?.invoke(updated)
                showEditProfileDialog = false
                Toast.makeText(context, "Perfil de atleta actualizado y sincronizado 🔥", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Centro de Recursos y Descargas (Ecosistema PDF, Audio, Video y Guía Oficial)
    if (showResourcesDialog) {
        ResourcesHubDialog(
            isAdmin = effectiveIsAdmin,
            uploaderName = if (effectiveIsAdmin) (viewerClient?.fullName ?: "Administrador Actitud Fuerte") else (currentClient.fullName.ifBlank { "Atleta Actitud Fuerte" }),
            onDismiss = { showResourcesDialog = false }
        )
    }
}

/**
 * Dialog wrapper para AthleteProfileContent
 */
@Composable
fun AthleteProfileDialog(
    client: Client,
    posts: List<SocialPost>,
    attendances: List<Attendance>,
    routines: List<Routine> = emptyList(),
    allPosts: List<SocialPost> = emptyList(),
    allClients: List<Client> = emptyList(),
    isAdmin: Boolean = false,
    viewerClient: Client? = null,
    onDismiss: () -> Unit,
    onCreatePost: (SocialPost) -> Unit,
    onToggleLike: (Long) -> Unit,
    onDeletePost: (SocialPost) -> Unit,
    onSaveRoutine: (String) -> Unit,
    onUpdateClient: ((Client) -> Unit)? = null,
    onAddComment: ((Long, String, String, String) -> Unit)? = null,
    onToggleLikeComment: ((Long, String) -> Unit)? = null,
    onOpenAuthorProfile: ((Client) -> Unit)? = null,
    onOpenNotifications: () -> Unit = {},
    unreadSystem: Boolean = false,
    unreadSocial: Boolean = false,
    totalUnreadCount: Int = 0
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AthleteProfileContent(
            client = client,
            posts = posts,
            attendances = attendances,
            routines = routines,
            allPosts = allPosts,
            allClients = allClients,
            isAdmin = isAdmin,
            viewerClient = viewerClient,
            showBackButton = true,
            onDismiss = onDismiss,
            onCreatePost = onCreatePost,
            onToggleLike = onToggleLike,
            onDeletePost = onDeletePost,
            onSaveRoutine = onSaveRoutine,
            onUpdateClient = onUpdateClient,
            onAddComment = onAddComment,
            onToggleLikeComment = onToggleLikeComment,
            onOpenAuthorProfile = onOpenAuthorProfile,
            onOpenNotifications = onOpenNotifications,
            unreadSystem = unreadSystem,
            unreadSocial = unreadSocial,
            totalUnreadCount = totalUnreadCount
        )
    }
}

@Composable
fun ProfileStatColumn(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
        Text(text = label, color = TextLightGray, fontSize = 11.sp)
    }
}

@Composable
fun StoryHighlightItem(emoji: String, title: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SurfaceDark)
                .border(1.dp, LimeGreen.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, color = TextLightGray, fontSize = 10.5.sp, maxLines = 1)
    }
}

/**
 * Cuadrícula de fotos estilo Instagram con relación de aspecto vertical 3:4.
 */
@Composable
fun SocialPostsGrid(
    posts: List<SocialPost>,
    onPostClick: (SocialPost) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(1.dp)
    ) {
        val rows = posts.chunked(3)
        rows.forEach { rowPosts ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                rowPosts.forEach { post ->
                    val isRealMedia = post.mediaUrl.startsWith("content://") || post.mediaUrl.startsWith("file://") || post.mediaUrl.startsWith("http")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isRealMedia) Brush.verticalGradient(listOf(Color.Black, Color.Black)) else getPostBackgroundBrush(post.mediaUrl))
                            .clickable { onPostClick(post) }
                    ) {
                        if (isRealMedia) {
                            AsyncImage(
                                model = post.mediaUrl,
                                contentDescription = post.contentText,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                        )
                                    )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(6.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = post.category,
                                        color = LimeGreen,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (post.mediaType == "VIDEO") {
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = "Video",
                                        tint = LimeGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }

                            Column {
                                if (post.workoutDetails.isNotBlank()) {
                                    Text(
                                        text = post.workoutDetails,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💪", fontSize = 10.sp)
                                    Text(
                                        text = "${post.likesCount}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
                // Si la fila tiene menos de 3 elementos, rellenar espacios vacíos
                val emptySlots = 3 - rowPosts.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Tarjeta completa de Publicación en el Feed (Admite estilo X de solo texto y fotos/videos 3:4 con PostMediaImage)
 */
@Composable
fun SocialPostFeedCard(
    post: SocialPost,
    allClients: List<Client> = emptyList(),
    canDelete: Boolean = false,
    onToggleLike: () -> Unit,
    onDeletePost: () -> Unit,
    onOpenComments: () -> Unit = {},
    onOpenAuthorProfile: ((Client) -> Unit)? = null
) {
    val dateStr = remember(post.timestamp) {
        SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()).format(Date(post.timestamp))
    }

    // Resolución de avatar del autor: campo directo o búsqueda por ID en la lista de atletas
    val resolvedAvatarUrl = remember(post.authorAvatarUrl, post.authorId, post.authorAccessId, allClients) {
        post.authorAvatarUrl.ifBlank {
            allClients.firstOrNull { it.id == post.authorId || it.accessId == post.authorAccessId }?.avatarUrl.orEmpty()
        }
    }

    val authorClient = remember(post.authorId, post.authorAccessId, post.authorName, allClients) {
        allClients.firstOrNull { 
            (it.id != 0L && it.id == post.authorId) || 
            (it.accessId.isNotBlank() && it.accessId == post.authorAccessId) || 
            it.fullName.equals(post.authorName, ignoreCase = true) 
        }
    }

    val isTextOnly = post.mediaType == "TEXT_ONLY" || post.mediaUrl.isBlank()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column {
            // Cabecera del autor con Foto de Perfil real y nombre (Clickeable hacia perfil)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = authorClient != null && onOpenAuthorProfile != null) {
                            authorClient?.let { onOpenAuthorProfile?.invoke(it) }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LimeGreen.copy(alpha = 0.2f))
                            .border(1.2.dp, LimeGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ClientAvatarImage(
                            avatarUrl = resolvedAvatarUrl,
                            fullName = post.authorName,
                            fallbackTextSize = 13.sp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(post.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(13.dp))
                        }
                        Text(dateStr, color = TextMuted, fontSize = 10.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(post.category, color = LimeGreen, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                    }
                    if (canDelete) {
                        Spacer(modifier = Modifier.width(4.dp))
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = TextLightGray, modifier = Modifier.size(18.dp))
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🗑️ Eliminar publicación", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        showMenu = false
                                        onDeletePost()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // CUERPO DE LA PUBLICACIÓN
            if (isTextOnly) {
                // FORMATO TIPO X (Microblogging de solo texto sin caja multimedia)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = post.contentText,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    )

                    if (post.workoutDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, LimeGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡ ", fontSize = 12.sp)
                                Text(
                                    text = post.workoutDetails,
                                    color = LimeGreen,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            } else {
                // PUBLICACIÓN MULTIMEDIA (FOTO/VIDEO 3:4 con PostMediaImage compatible con Base64)
                if (post.contentText.isNotBlank()) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = post.contentText,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f),
                    contentAlignment = Alignment.Center
                ) {
                    PostMediaImage(
                        mediaUrl = post.mediaUrl,
                        contentDescription = post.contentText,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Sombra suave en extremos
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent, Color.Black.copy(alpha = 0.65f))
                                )
                            )
                    )

                    if (post.mediaType == "VIDEO") {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .border(1.5.dp, LimeGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Reproducir Video",
                                tint = LimeGreen,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    if (post.workoutDetails.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.85f))
                                .border(1.dp, LimeGreen.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡ ", fontSize = 12.sp)
                                Text(
                                    text = post.workoutDetails,
                                    color = LimeGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Barra de Interacciones (Like, Comentar, Marca Actitud Fuerte)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleLike() }
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Apoyar",
                            tint = if (post.isLikedByMe) Color(0xFFEF4444) else TextLightGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${post.likesCount}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenComments() }
                    ) {
                        Icon(Icons.Default.ModeComment, contentDescription = "Comentar", tint = LimeGreen, modifier = Modifier.size(19.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(text = "${post.commentsCount}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text("ACTITUD FUERTE", color = LimeGreen.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
            }
        }
    }
}

/**
 * Modal para ver en grande una publicación tocada desde la cuadrícula 3:4.
 */
@Composable
fun SocialPostDetailDialog(
    post: SocialPost,
    allClients: List<Client> = emptyList(),
    canDelete: Boolean = false,
    onDismiss: () -> Unit,
    onToggleLike: () -> Unit,
    onDelete: () -> Unit,
    onOpenComments: () -> Unit = {},
    onOpenAuthorProfile: ((Client) -> Unit)? = null
) {
    val isTextOnly = post.mediaType == "TEXT_ONLY" || post.mediaUrl.isBlank()

    val authorClient = remember(post.authorId, post.authorAccessId, post.authorName, allClients) {
        allClients.firstOrNull { 
            (it.id != 0L && it.id == post.authorId) || 
            (it.accessId.isNotBlank() && it.accessId == post.authorAccessId) || 
            it.fullName.equals(post.authorName, ignoreCase = true) 
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = authorClient != null && onOpenAuthorProfile != null) {
                                authorClient?.let {
                                    onDismiss()
                                    onOpenAuthorProfile?.invoke(it)
                                }
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(post.authorName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(14.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (canDelete) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = TextLightGray)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🗑️ Eliminar publicación", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            showMenu = false
                                            onDelete()
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                        }
                    }
                }

                if (!isTextOnly) {
                    // Vista 3:4 con PostMediaImage (Compatible con Base64 multiplataforma)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 4f),
                        contentAlignment = Alignment.Center
                    ) {
                        PostMediaImage(
                            mediaUrl = post.mediaUrl,
                            contentDescription = post.contentText,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                    )
                                )
                        )
                        if (post.mediaType == "VIDEO") {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .border(1.5.dp, LimeGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayCircle,
                                    contentDescription = "Reproducir Video",
                                    tint = LimeGreen,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        if (post.workoutDetails.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(14.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.85f))
                                    .border(1.dp, LimeGreen.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("⚡ ", fontSize = 13.sp)
                                    Text(
                                        text = post.workoutDetails,
                                        color = LimeGreen,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onToggleLike() }
                            ) {
                                Icon(
                                    imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (post.isLikedByMe) Color(0xFFEF4444) else TextLightGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${post.likesCount} apoyos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onOpenComments() }
                            ) {
                                Icon(
                                    Icons.Default.ModeComment,
                                    contentDescription = "Comentarios",
                                    tint = LimeGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${post.commentsCount}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Text(post.category, color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        post.contentText,
                        color = Color.White,
                        fontSize = if (isTextOnly) 15.sp else 13.sp,
                        lineHeight = if (isTextOnly) 22.sp else 18.sp
                    )

                    if (isTextOnly && post.workoutDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, LimeGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚡ ", fontSize = 12.sp)
                                Text(
                                    text = post.workoutDetails,
                                    color = LimeGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal Interactivo para ver y redactar comentarios en tiempo real en las publicaciones.
 * Permite responder comentarios de otros usuarios o propios en cualquier publicación.
 */
@Composable
fun PostCommentsDialog(
    post: SocialPost,
    currentClient: Client,
    allClients: List<Client> = emptyList(),
    onDismiss: () -> Unit,
    onAddComment: (text: String, replyToCommentId: String, replyToAuthorName: String) -> Unit,
    onToggleLikeComment: ((String) -> Unit)? = null,
    onOpenAuthorProfile: ((Client) -> Unit)? = null
) {
    val context = LocalContext.current
    var commentInput by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<SocialComment?>(null) }
    val comments = remember(post.commentsJson) { post.getCommentsList() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("💬", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Comentarios (${comments.size})",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                "Publicación de ${post.authorName}",
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                // Comments List
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔥", fontSize = 38.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Sé el primero en comentar",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Motiva a ${post.authorName} con un mensaje de apoyo y actitud fuerte.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(comments) { comment ->
                            val resolvedCommentAvatar = remember(comment.authorAvatarUrl, comment.authorId, allClients) {
                                comment.authorAvatarUrl.ifBlank {
                                    allClients.firstOrNull { it.id == comment.authorId }?.avatarUrl.orEmpty()
                                }
                            }
                            val commentAuthorClient = remember(comment.authorId, comment.authorName, allClients) {
                                allClients.firstOrNull { 
                                    (it.id != 0L && it.id == comment.authorId) || 
                                    it.fullName.equals(comment.authorName, ignoreCase = true) 
                                }
                            }
                            val isCommentLiked = remember(comment.likedByClientIds, currentClient.id) {
                                comment.likedByClientIds.contains(currentClient.id)
                            }
                            val timeAgo = remember(comment.timestamp) {
                                val diff = System.currentTimeMillis() - comment.timestamp
                                when {
                                    diff < 60_000L -> "Ahora"
                                    diff < 3600_000L -> "${diff / 60_000L}m"
                                    diff < 86400_000L -> "${diff / 3600_000L}h"
                                    else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(comment.timestamp))
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceElevated)
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(LimeGreen.copy(alpha = 0.2f))
                                        .border(1.dp, LimeGreen, CircleShape)
                                        .clickable(enabled = commentAuthorClient != null && onOpenAuthorProfile != null) {
                                            commentAuthorClient?.let {
                                                onDismiss()
                                                onOpenAuthorProfile?.invoke(it)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    ClientAvatarImage(
                                        avatarUrl = resolvedCommentAvatar,
                                        fullName = comment.authorName,
                                        fallbackTextSize = 11.sp,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = comment.authorName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.clickable(enabled = commentAuthorClient != null && onOpenAuthorProfile != null) {
                                                commentAuthorClient?.let {
                                                    onDismiss()
                                                    onOpenAuthorProfile?.invoke(it)
                                                }
                                            }
                                        )
                                        Text(
                                            timeAgo,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    // Indicador de respuesta a un comentario previo
                                    if (comment.replyToAuthorName.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "↳ En respuesta a @${comment.replyToAuthorName}",
                                            color = LimeGreen,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        comment.text,
                                        color = TextLightGray,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp
                                    )

                                    // Acciones del comentario: Responder y Me Gusta
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Botón para Responder al comentario
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .clickable {
                                                    replyingToComment = comment
                                                    val firstName = comment.authorName.split(" ").firstOrNull() ?: comment.authorName
                                                    commentInput = "@$firstName "
                                                }
                                                .padding(vertical = 2.dp, horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ModeComment,
                                                contentDescription = "Responder",
                                                tint = LimeGreen,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Responder",
                                                color = LimeGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Botón Me Gusta del Comentario
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isCommentLiked) Color(0xFFEF4444).copy(alpha = 0.15f) else Color.Transparent)
                                                .clickable {
                                                    onToggleLikeComment?.invoke(comment.id)
                                                }
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isCommentLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Me gusta",
                                                tint = if (isCommentLiked) Color(0xFFEF4444) else TextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            if (comment.likesCount > 0) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "${comment.likesCount}",
                                                    color = if (isCommentLiked) Color(0xFFEF4444) else TextMuted,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                // Barra informativa de Respuesta activa (si está respondiendo)
                if (replyingToComment != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ModeComment,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Respondiendo a @${replyingToComment?.authorName}",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = {
                                replyingToComment = null
                                if (commentInput.startsWith("@")) {
                                    commentInput = ""
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancelar respuesta",
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Input Bar at bottom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(LimeGreen.copy(alpha = 0.2f))
                            .border(1.dp, LimeGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        ClientAvatarImage(
                            avatarUrl = currentClient.avatarUrl,
                            fullName = currentClient.fullName,
                            fallbackTextSize = 11.sp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = {
                            Text(
                                if (replyingToComment != null) "Escribe tu respuesta..." else "Escribe un comentario... 🔥",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = BlackBackground,
                            unfocusedContainerColor = BlackBackground,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                val replyId = replyingToComment?.id.orEmpty()
                                val replyAuthor = replyingToComment?.authorName.orEmpty()
                                onAddComment(commentInput.trim(), replyId, replyAuthor)
                                commentInput = ""
                                replyingToComment = null
                                Toast.makeText(context, "¡Comentario publicado! 🔥", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(LimeGreen, CircleShape)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Enviar Comentario",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal para componer una nueva publicación (Admite formato solo texto tipo X, fotos y videos 3:4 con compresión Base64).
 */
@Composable
fun CreateSocialPostDialog(
    client: Client,
    onDismiss: () -> Unit,
    onPublish: (SocialPost) -> Unit
) {
    val context = LocalContext.current
    var postFormat by remember { mutableStateOf("TEXT") } // "TEXT", "IMAGE", "VIDEO"
    var captionText by remember { mutableStateOf("") }
    var workoutNotes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ENTRENAMIENTO") }
    var selectedMediaPreset by remember { mutableStateOf(SocialPostConstants.PRESET_MEDIA_STYLES.first()) }
    var selectedMediaUri by remember { mutableStateOf<String?>(null) }
    var selectedMediaType by remember { mutableStateOf("IMAGE") }

    // Selector de Fotos con compresión inmediata a Base64 para visualización garantizada en PC y Móvil
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val base64 = compressImageUriToBase64(context, uri)
            selectedMediaUri = base64 ?: uri.toString()
            selectedMediaType = "IMAGE"
            postFormat = "IMAGE"
        }
    }

    // Selector de Videos / Archivos Multimedia con captura de miniatura HD
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val thumb = extractVideoThumbnailBase64(context, uri)
            selectedMediaUri = thumb ?: uri.toString()
            val mime = context.contentResolver.getType(uri)
            selectedMediaType = if (mime?.contains("video") == true || uri.toString().contains("video")) "VIDEO" else "IMAGE"
            postFormat = if (selectedMediaType == "VIDEO") "VIDEO" else "IMAGE"
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    // Cabecera del diálogo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NUEVA PUBLICACIÓN",
                                color = LimeGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⚡", fontSize = 13.sp)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SELECTOR DE FORMATO: SOLO TEXTO (ESTILO X) / FOTO 3:4 / VIDEO
                    Text("FORMATO DE PUBLICACIÓN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            Triple("TEXT", "📝 Solo Texto (Tipo X)", "TEXT"),
                            Triple("IMAGE", "📷 Foto (3:4)", "IMAGE"),
                            Triple("VIDEO", "🎥 Video", "VIDEO")
                        ).forEach { (formatKey, label, _) ->
                            val isSel = postFormat == formatKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) LimeGreen else Color.Transparent)
                                    .clickable {
                                        postFormat = formatKey
                                        if (formatKey == "TEXT") {
                                            selectedMediaUri = null
                                        }
                                    }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSel) Color.Black else TextLightGray,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Selector de Categoría
                    Text("CATEGORÍA DE ENTRENAMIENTO", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(SocialPostConstants.CATEGORIES) { cat ->
                            val isSel = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) LimeGreen else SurfaceElevated)
                                    .border(1.dp, if (isSel) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSel) Color.Black else TextLightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (postFormat == "TEXT") {
                        // FORMATO TIPO X: Composición de texto limpio sin contenedor multimedia
                        Text("MENSAJE O REFLEXIÓN (ESTILO X)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            placeholder = { Text("¿Qué estás entrenando o pensando hoy? Comparte tus ideas con la comunidad...", fontSize = 13.sp, color = TextMuted) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Detalle opcional de PR / Marca
                        Text("MARCA PERSONAL O EJERCICIO (OPCIONAL)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = workoutNotes,
                            onValueChange = { workoutNotes = it },
                            placeholder = { Text("Ej: Press Banca 100kg 4x8 • Peso Muerto 160kg", fontSize = 11.sp, color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            singleLine = true
                        )
                    } else {
                        // FORMATO FOTO/VIDEO 3:4
                        Text("ESTILO DE FONDO / PRESETS DE FUERZA", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SocialPostConstants.PRESET_MEDIA_STYLES) { preset ->
                                val isSel = preset.id == selectedMediaPreset.id && selectedMediaUri == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) LimeGreen.copy(alpha = 0.2f) else SurfaceElevated)
                                        .border(1.dp, if (isSel) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                                        .clickable {
                                            selectedMediaPreset = preset
                                            selectedMediaUri = null
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Text(preset.title, color = if (isSel) LimeGreen else TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("VISTA PREVIA 3:4 (FOTO O VIDEO)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.68f)
                                    .aspectRatio(3f / 4f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, LimeGreen, RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (postFormat == "VIDEO") {
                                            videoPickerLauncher.launch("video/*")
                                        } else {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedMediaUri != null) {
                                    PostMediaImage(
                                        mediaUrl = selectedMediaUri!!,
                                        contentDescription = "Foto o Video Seleccionado",
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Gradiente
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                                )
                                            )
                                    )

                                    IconButton(
                                        onClick = { selectedMediaUri = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.7f))
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Eliminar recurso", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }

                                    if (selectedMediaType == "VIDEO") {
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.7f))
                                                .border(1.dp, LimeGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.PlayCircle, contentDescription = "Video", tint = LimeGreen, modifier = Modifier.size(34.dp))
                                        }
                                    }
                                } else {
                                    // Botón interactivo central
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .background(LimeGreen.copy(alpha = 0.2f))
                                                .border(1.5.dp, LimeGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Subir foto o video",
                                                tint = LimeGreen,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "TOCA AQUÍ PARA SUBIR",
                                            color = LimeGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = if (postFormat == "VIDEO") "VIDEO (3:4)" else "FOTO (3:4 Base64)",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Detalle del levantamiento
                        Text("DETALLE DEL LEVANTAMIENTO / PR", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = workoutNotes,
                            onValueChange = { workoutNotes = it },
                            label = { Text("Ejercicio y peso levantado (ej. Sentadilla 140kg • 4x6)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Reflexión / Mensaje
                        Text("PIE DE FOTO / MENSAJE DE COMUNIDAD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            label = { Text("Escribe una reflexión o mensaje...", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón Publicar
                    Button(
                        onClick = {
                            val finalMediaUrl = when {
                                postFormat == "TEXT" -> ""
                                selectedMediaUri != null -> selectedMediaUri!!
                                else -> selectedMediaPreset.id
                            }
                            val finalMediaType = when {
                                postFormat == "TEXT" -> "TEXT_ONLY"
                                selectedMediaUri != null -> selectedMediaType
                                else -> "PRESET_STYLE"
                            }
                            val post = SocialPost(
                                authorId = client.id,
                                authorName = client.fullName,
                                authorAccessId = client.accessId,
                                authorAvatarUrl = client.avatarUrl,
                                contentText = captionText.trim(),
                                mediaUrl = finalMediaUrl,
                                mediaType = finalMediaType,
                                aspectRatio = "3:4",
                                timestamp = System.currentTimeMillis(),
                                category = selectedCategory,
                                workoutDetails = workoutNotes.trim()
                            )
                            onPublish(post)
                        },
                        enabled = (postFormat == "TEXT" && captionText.isNotBlank()) || (postFormat != "TEXT"),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (postFormat == "TEXT") "PUBLICAR EN ACTITUD FUERTE (TIPO X)" else "COMPARTIR EN COMUNIDAD FIT (3:4)",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal Visor y Creador de Historias Reales (24 horas)
 * Permite tanto visualizar historias activas (foto/texto) como subir nuevas historias con Base64.
 */
@Composable
fun StoryViewerDialog(
    client: Client,
    attendances: List<Attendance>,
    allPosts: List<SocialPost> = emptyList(),
    onPublishStory: ((SocialPost) -> Unit)? = null,
    onDeleteStory: ((SocialPost) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activeStories = remember(allPosts, client.id, client.accessId) {
        allPosts.filter { post ->
            (post.authorId == client.id || post.authorAccessId == client.accessId) &&
            (post.category == "HISTORIA_24H" || post.category == "HISTORIA") &&
            System.currentTimeMillis() - post.timestamp < 24 * 3600 * 1000L
        }.sortedByDescending { it.timestamp }
    }

    var isCreatingStory by remember { mutableStateOf(false) }
    var storyText by remember { mutableStateOf("") }
    var storyPhotoUri by remember { mutableStateOf<String?>(null) }
    var selectedBgIndex by remember { mutableIntStateOf(0) }

    val bgGradients = listOf(
        listOf(Color(0xFF1B2A16), Color(0xFF0F1A0E), Color.Black),
        listOf(Color(0xFF1E1B4B), Color(0xFF0F172A), Color.Black),
        listOf(Color(0xFF3B0764), Color(0xFF180828), Color.Black),
        listOf(Color(0xFF450A0A), Color(0xFF1A0505), Color.Black)
    )

    val storyPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val base64 = compressImageUriToBase64(context, uri)
            storyPhotoUri = base64 ?: uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(bgGradients[selectedBgIndex])
                    )
                    .border(1.5.dp, LimeGreen, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Barra superior de la historia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LimeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            ClientAvatarImage(
                                avatarUrl = client.avatarUrl,
                                fullName = client.fullName,
                                fallbackTextSize = 13.sp,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(client.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(
                                if (isCreatingStory) "Creando nueva historia" else "Historia 24 Horas",
                                color = LimeGreen,
                                fontSize = 9.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isCreatingStory && activeStories.isNotEmpty() && onDeleteStory != null) {
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = Color.White)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🗑️ Eliminar historia", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
                                        onClick = {
                                            showMenu = false
                                            val currentStory = activeStories.first()
                                            onDeleteStory(currentStory)
                                        }
                                    )
                                }
                            }
                        }
                        if (!isCreatingStory && onPublishStory != null) {
                            IconButton(onClick = { isCreatingStory = true }) {
                                Icon(Icons.Default.Add, contentDescription = "Subir historia", tint = LimeGreen)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                        }
                    }
                }

                // CUERPO CENTRAL DE LA HISTORIA
                if (isCreatingStory) {
                    // MODO: CREAR NUEVA HISTORIA (FOTO O TEXTO)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (storyPhotoUri != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .aspectRatio(3f / 4f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, LimeGreen, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                PostMediaImage(
                                    mediaUrl = storyPhotoUri!!,
                                    contentDescription = "Foto de Historia",
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = { storyPhotoUri = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f))
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // Selector de foto o color de fondo
                            Button(
                                onClick = {
                                    storyPhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
                            ) {
                                Text("📷 Subir Foto de la Galería", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Paleta de gradientes
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                bgGradients.indices.forEach { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(bgGradients[index]))
                                            .border(if (selectedBgIndex == index) 2.dp else 0.dp, LimeGreen, CircleShape)
                                            .clickable { selectedBgIndex = index }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = storyText,
                            onValueChange = { storyText = it },
                            placeholder = { Text("Escribe tu frase o momento del día...", color = TextMuted, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = TextLightGray,
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder
                            ),
                            maxLines = 3
                        )
                    }

                    // Botones de acción en creación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isCreatingStory = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", color = TextLightGray, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (onPublishStory != null) {
                                    val story = SocialPost(
                                        authorId = client.id,
                                        authorName = client.fullName,
                                        authorAccessId = client.accessId,
                                        authorAvatarUrl = client.avatarUrl,
                                        contentText = storyText.trim(),
                                        mediaUrl = storyPhotoUri.orEmpty(),
                                        mediaType = if (storyPhotoUri != null) "IMAGE" else "TEXT_ONLY",
                                        aspectRatio = "9:16",
                                        timestamp = System.currentTimeMillis(),
                                        category = "HISTORIA_24H"
                                    )
                                    onPublishStory(story)
                                    isCreatingStory = false
                                }
                            },
                            enabled = storyPhotoUri != null || storyText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Publicar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                } else if (activeStories.isNotEmpty()) {
                    // MODO: VISOR DE HISTORIA ACTIVA
                    val currentStory = activeStories.first()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (currentStory.mediaUrl.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .aspectRatio(3f / 4f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                PostMediaImage(
                                    mediaUrl = currentStory.mediaUrl,
                                    contentDescription = currentStory.contentText,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (currentStory.contentText.isNotBlank()) {
                            Text(
                                text = currentStory.contentText,
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Toca para cerrar • Expira en 24h", color = TextMuted, fontSize = 11.sp)
                    }
                } else {
                    // MODO: SIN HISTORIAS ACTIVAS (INVITACIÓN A SUBIR HISTORIA)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📸", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "HISTORIAS DE 24 HORAS",
                            color = LimeGreen,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comparte fotos o frases de tus entrenamientos del día. Desaparecen automáticamente tras 24 horas.",
                            color = TextLightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceDark)
                                .border(1.dp, LimeGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Check-ins acumulados: ${attendances.size}",
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botón para subir historia
                    Button(
                        onClick = { isCreatingStory = true },
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ SUBIR MI HISTORIA (24H)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun getPostBackgroundBrush(mediaId: String): Brush {
    return when (mediaId) {
        "beast_mode" -> Brush.verticalGradient(listOf(Color(0xFF1F2E14), Color(0xFF0F170A)))
        "transformation" -> Brush.verticalGradient(listOf(Color(0xFF132338), Color(0xFF0A111C)))
        "leg_day" -> Brush.verticalGradient(listOf(Color(0xFF33162C), Color(0xFF180A15)))
        "discipline" -> Brush.verticalGradient(listOf(Color(0xFF2E2612), Color(0xFF161208)))
        else -> Brush.verticalGradient(listOf(Color(0xFF1F2E14), Color(0xFF0E170B)))
    }
}

fun getCategoryEmoji(category: String): String {
    return when (category) {
        "PR_FUERZA" -> "🏋️‍♂️"
        "TRANSFORMACIÓN" -> "📈"
        "ENTRENAMIENTO" -> "⚡"
        "MOTIVACIÓN" -> "🏆"
        "NUTRICIÓN" -> "🥗"
        else -> "💪"
    }
}

/**
 * Comprime y procesa una imagen seleccionada a resolución HD (hasta 1440px a 92% de calidad)
 * corrigiendo la orientación del sensor (EXIF) y preservando fidelidad cromática ARGB_8888.
 */
fun compressImageUriToBase64(context: android.content.Context, uri: Uri, maxDimension: Int = 1440, quality: Int = 92): String? {
    return try {
        // 1. Detección de orientación EXIF para evitar rotaciones involuntarias
        var rotationDegrees = 0f
        try {
            val exifStream = context.contentResolver.openInputStream(uri)
            if (exifStream != null) {
                val exif = android.media.ExifInterface(exifStream)
                val orientation = exif.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                exifStream.close()
                rotationDegrees = when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }
        } catch (_: Exception) {}

        // 2. Decodificación en ARGB_8888 de alta fidelidad sin submuestreo agresivo
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val originalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        if (originalBitmap == null) return null

        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }

        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scale).toInt().coerceAtLeast(1),
                (height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            originalBitmap
        }

        val finalBitmap = if (rotationDegrees != 0f) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        } else {
            scaledBitmap
        }

        val outputStream = ByteArrayOutputStream()
        finalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodifica una URI de imagen a Bitmap con corrección EXIF y resolución adecuada para recorte.
 */
fun decodeUriToBitmap(context: android.content.Context, uri: Uri, maxDim: Int = 1600): Bitmap? {
    return try {
        var rotationDegrees = 0f
        try {
            val exifStream = context.contentResolver.openInputStream(uri)
            if (exifStream != null) {
                val exif = android.media.ExifInterface(exifStream)
                val orientation = exif.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_NORMAL
                )
                exifStream.close()
                rotationDegrees = when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            }
        } catch (_: Exception) {}

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bmp = BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        if (bmp == null) return null

        val w = bmp.width
        val h = bmp.height
        val scale = if (w > h) maxDim.toFloat() / w else maxDim.toFloat() / h
        val scaled = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(bmp, (w * scale).toInt().coerceAtLeast(1), (h * scale).toInt().coerceAtLeast(1), true)
        } else {
            bmp
        }

        if (rotationDegrees != 0f) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, matrix, true)
        } else {
            scaled
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * Decodifica una cadena Base64 Data URI a un objeto Bitmap.
 */
fun decodeBase64ToBitmap(dataUri: String): Bitmap? {
    return try {
        val clean = if (dataUri.contains(",")) dataUri.substringAfter(",") else dataUri
        val bytes = Base64.decode(clean, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) {
        null
    }
}

/**
 * Extrae un fotograma clave del video en alta definición utilizando MediaMetadataRetriever
 * para utilizarlo como portada HD de la publicación de video.
 */
fun extractVideoThumbnailBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val frame = retriever.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: retriever.frameAtTime
        retriever.release()
        if (frame != null) {
            val maxDimension = 1440
            val width = frame.width
            val height = frame.height
            val scale = if (width > height) maxDimension.toFloat() / width else maxDimension.toFloat() / height
            val scaled = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(frame, (width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1), true)
            } else {
                frame
            }
            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 92, outputStream)
            val byteArray = outputStream.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * DIÁLOGO EDITAR PERFIL ESTILO INSTAGRAM
 * - Foto de perfil interactiva con selector visual
 * - Nombre de usuario
 * - Biografía
 * Se eliminaron Alias deportivo y PR insignia conforme a la solicitud.
 */
@Composable
fun EditSocialProfileDialog(
    client: Client,
    onDismiss: () -> Unit,
    onSaveProfile: (Client) -> Unit
) {
    val context = LocalContext.current
    var avatarUrl by remember { mutableStateOf(client.avatarUrl) }
    var fullName by remember { mutableStateOf(client.fullName) }
    var gender by remember {
        mutableStateOf(
            if (client.gender.isNotBlank()) client.gender
            else if (client.fullName.contains("Carlen", ignoreCase = true)) "Mujer"
            else "Hombre"
        )
    }
    var bio by remember { mutableStateOf(client.bio) }

    var pendingCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCropDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = decodeUriToBitmap(context, uri)
            if (bitmap != null) {
                pendingCropBitmap = bitmap
                showCropDialog = true
            } else {
                Toast.makeText(context, "No se pudo procesar la imagen seleccionada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun adjustExistingAvatar() {
        val bitmap = decodeBase64ToBitmap(avatarUrl)
        if (bitmap != null) {
            pendingCropBitmap = bitmap
            showCropDialog = true
        } else {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    val maxNameLength = 45
    val maxBioLength = 150

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(12.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LimeGreen)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera del Diálogo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(LimeGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "EDITAR PERFIL",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Información y Foto",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Foto de Perfil estilo Instagram
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(2.dp, LimeGreen, CircleShape)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ClientAvatarImage(
                            avatarUrl = avatarUrl,
                            fullName = fullName,
                            fallbackTextSize = 28.sp,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )

                        // Badge de cámara superpuesto
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LimeGreen)
                                .border(2.dp, SurfaceDark, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = "Cambiar foto",
                                tint = Color.Black,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (avatarUrl.isNotBlank()) "Cambiar" else "Subir foto",
                                color = LimeGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (avatarUrl.isNotBlank()) {
                            TextButton(
                                onClick = { adjustExistingAvatar() }
                            ) {
                                Icon(
                                    Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Encuadrar",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(
                                onClick = { avatarUrl = "" }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "Quitar",
                                    color = Color(0xFFEF4444),
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Nombre Completo [Límite 35 caracteres]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nombre del usuario:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "[${fullName.length}/$maxNameLength]",
                        color = if (fullName.length >= maxNameLength) Color(0xFFEF4444) else LimeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { if (it.length <= maxNameLength) fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = LimeGreen) },
                    placeholder = { Text("Ej: Alex Gómez", color = TextMuted, fontSize = 13.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextLightGray,
                        unfocusedTextColor = TextLightGray,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Género del Atleta
                Text("Género del Atleta:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Hombre", "Mujer").forEach { g ->
                        val selected = gender.equals(g, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) LimeGreen else SurfaceElevated)
                                .border(1.dp, if (selected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { gender = g }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (g == "Hombre") "♂ Hombre" else "♀ Mujer",
                                color = if (selected) Color.Black else TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Biografía [Límite 150 caracteres]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Biografía:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "[${bio.length}/$maxBioLength]",
                        color = if (bio.length >= maxBioLength) Color(0xFFEF4444) else LimeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= maxBioLength) bio = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null, tint = LimeGreen) },
                    placeholder = { Text("Escribe algo sobre ti...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextLightGray,
                        unfocusedTextColor = TextLightGray,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de Acción
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
                            val trimmedName = fullName.trim().ifBlank { client.fullName }
                            val updated = client.copy(
                                fullName = trimmedName,
                                gender = gender,
                                bio = bio.trim(),
                                avatarUrl = avatarUrl
                            )
                            onSaveProfile(updated)
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("GUARDAR PERFIL", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (showCropDialog && pendingCropBitmap != null) {
        AvatarCropDialog(
            sourceBitmap = pendingCropBitmap!!,
            onDismiss = {
                showCropDialog = false
                pendingCropBitmap = null
            },
            onCropConfirmed = { croppedBase64 ->
                avatarUrl = croppedBase64
                showCropDialog = false
                pendingCropBitmap = null
                Toast.makeText(context, "Encuadre aplicado exitosamente ✨", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/**
 * Modelo de datos para las Insignias de Gamificación de Disciplina en Actitud Fuerte
 */
data class DisciplineBadge(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: String,
    val isUnlocked: Boolean,
    val progress: Float,
    val progressText: String,
    val accentColor: Color
)

/**
 * Sección de Insignias y Gamificación de Disciplina del Atleta
 */
@Composable
fun AthleteDisciplineBadgesSection(
    attendances: List<Attendance>,
    streakDays: Int
) {
    val currentMonthAttendances = remember(attendances) {
        val now = java.util.Calendar.getInstance()
        val currentMonth = now.get(java.util.Calendar.MONTH)
        val currentYear = now.get(java.util.Calendar.YEAR)
        attendances.count { a ->
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = a.timestamp }
            cal.get(java.util.Calendar.MONTH) == currentMonth && cal.get(java.util.Calendar.YEAR) == currentYear
        }
    }

    val badges = remember(attendances, streakDays, currentMonthAttendances) {
        listOf(
            DisciplineBadge(
                id = "15_month",
                title = "Medalla 15 Asistencias",
                subtitle = "En el mes en curso",
                icon = "🏆",
                isUnlocked = currentMonthAttendances >= 15,
                progress = (currentMonthAttendances.toFloat() / 15f).coerceIn(0f, 1f),
                progressText = "$currentMonthAttendances / 15 días",
                accentColor = Color(0xFFFBBF24) // Oro
            ),
            DisciplineBadge(
                id = "4_weeks",
                title = "Fuego 4 Semanas",
                subtitle = "Racha ininterrumpida",
                icon = "🔥",
                isUnlocked = streakDays >= 28,
                progress = (streakDays.toFloat() / 28f).coerceIn(0f, 1f),
                progressText = "$streakDays / 28 días",
                accentColor = Color(0xFFFF5722) // Fuego neón
            ),
            DisciplineBadge(
                id = "warrior",
                title = "Espíritu Guerrero",
                subtitle = "5 días consecutivos",
                icon = "⚡",
                isUnlocked = streakDays >= 5 || attendances.size >= 5,
                progress = (streakDays.toFloat() / 5f).coerceIn(0f, 1f),
                progressText = "${streakDays.coerceAtMost(5)} / 5 días",
                accentColor = LimeGreen
            ),
            DisciplineBadge(
                id = "iron",
                title = "Titán del Gym",
                subtitle = "Compromiso de hierro",
                icon = "🛡️",
                isUnlocked = attendances.isNotEmpty(),
                progress = if (attendances.isNotEmpty()) 1f else 0f,
                progressText = if (attendances.isNotEmpty()) "Activo" else "0 días",
                accentColor = Color(0xFF38BDF8) // Neón Cyan
            )
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏅", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INSIGNIAS & HITOS DE DISCIPLINA",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
                val unlockedCount = badges.count { it.isUnlocked }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (unlockedCount > 0) Color(0xFF2E2211) else SurfaceElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$unlockedCount/${badges.size} Desbloqueadas",
                        color = if (unlockedCount > 0) Color(0xFFFBBF24) else TextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(badges) { badge ->
                    BadgeItemCard(badge = badge)
                }
            }
        }
    }
}

@Composable
private fun BadgeItemCard(badge: DisciplineBadge) {
    Box(
        modifier = Modifier
            .width(135.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (badge.isUnlocked) badge.accentColor.copy(alpha = 0.12f) else SurfaceElevated)
            .border(
                1.dp,
                if (badge.isUnlocked) badge.accentColor else SurfaceBorder,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (badge.isUnlocked) badge.accentColor.copy(alpha = 0.25f) else SurfaceDark)
                        .border(
                            1.dp,
                            if (badge.isUnlocked) badge.accentColor else SurfaceBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = badge.icon, fontSize = 16.sp)
                }

                if (badge.isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badge.accentColor)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "DESBLOQUEADA",
                            color = Color.Black,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = badge.subtitle,
                color = TextMuted,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (!badge.isUnlocked) {
                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = badge.accentColor,
                    trackColor = SurfaceDark
                )
                Spacer(modifier = Modifier.height(3.dp))
            }

            Text(
                text = badge.progressText,
                color = if (badge.isUnlocked) badge.accentColor else TextMuted,
                fontSize = 9.sp,
                fontWeight = if (badge.isUnlocked) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

