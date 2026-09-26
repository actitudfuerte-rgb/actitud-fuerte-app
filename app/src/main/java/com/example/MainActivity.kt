package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.manager.AdminConfigManager
import com.example.data.manager.AppResourceManager
import com.example.data.model.AdminAnnouncement
import com.example.data.model.AthleteTier
import com.example.data.model.Client
import com.example.data.model.NotificationType
import com.example.data.model.SocialPost
import com.example.ui.components.BrandHeader
import com.example.ui.components.DigitalIdCardDialog
import com.example.ui.components.NotificationBellButton
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.ResourcesHubDialog
import com.example.ui.components.buildAppNotifications
import com.example.ui.screens.AthleteDirectoryScreen
import com.example.ui.screens.AthleteProfileContent
import com.example.ui.screens.AthleteProfileDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.BroadcastDialog
import com.example.ui.screens.ClientDetailDialog
import com.example.ui.screens.ClientRegistrationDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LeadershipDialog
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OFFICIAL_ADMIN_EMAIL
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.ReportsExportDialog
import com.example.ui.screens.UsersListScreen
import com.example.ui.screens.WorkoutLoadHistoryScreen
import com.example.ui.theme.ActitudFuerteTheme
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdminConfigManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            ActitudFuerteTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

/** Pestañas del Panel Administrativo (Exclusivo Alex Gómez) */
sealed class NavigationTab(val index: Int, val title: String, val icon: ImageVector) {
    object Panel : NavigationTab(0, "Panel", Icons.Default.GridView)
    object Asistencia : NavigationTab(1, "Asistencia", Icons.Default.HowToReg)
    object Pagos : NavigationTab(2, "Pagos", Icons.Default.CreditCard)
    object Progreso : NavigationTab(3, "Progreso", Icons.Default.Timeline)
    object Usuarios : NavigationTab(4, "Socios", Icons.Default.Groups)

    companion object {
        val allTabs = listOf(Panel, Asistencia, Pagos, Progreso, Usuarios)
    }
}

/** Pestañas de la Zona de Usuarios / Atletas */
sealed class UserNavigationTab(val index: Int, val title: String, val icon: ImageVector) {
    object MiPerfil : UserNavigationTab(0, "Mi Perfil", Icons.Default.Person)
    object Historial : UserNavigationTab(1, "Historial", Icons.Default.FitnessCenter)
    object Directorio : UserNavigationTab(2, "Directorio", Icons.Default.Groups)

    companion object {
        val allTabs = listOf(MiPerfil, Historial, Directorio)
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsState()
    var userSelectedTab by remember { mutableIntStateOf(0) }

    val clients by viewModel.allClients.collectAsState()
    val totalClients by viewModel.totalClientsCount.collectAsState()
    val activeClients by viewModel.activeClientsCount.collectAsState()

    val attendances by viewModel.allAttendances.collectAsState()
    val todayAttendances by viewModel.todayAttendances.collectAsState()
    val todayCheckInCount by viewModel.todayCheckInCount.collectAsState()

    val payments by viewModel.allPayments.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()

    val measurements by viewModel.allMeasurements.collectAsState()
    val routines by viewModel.allRoutines.collectAsState()
    val selectedClientForDetail by viewModel.selectedClientForDetail.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val socialPosts by viewModel.allSocialPosts.collectAsState()

    val sharedPrefs = remember {
        context.getSharedPreferences("actitud_fuerte_session", android.content.Context.MODE_PRIVATE)
    }

    var isAuthenticated by remember {
        mutableStateOf(sharedPrefs.getBoolean("is_authenticated", false))
    }
    var loggedInUserEmail by remember {
        mutableStateOf(sharedPrefs.getString("logged_in_email", OFFICIAL_ADMIN_EMAIL) ?: OFFICIAL_ADMIN_EMAIL)
    }
    val isUserAdmin = remember(loggedInUserEmail) {
        AdminConfigManager.isAdmin(context, loggedInUserEmail)
    }

    var showRegistrationDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showReportsExportDialog by remember { mutableStateOf(false) }
    var showLeadershipDialog by remember { mutableStateOf(false) }
    var showAdminDirectoryDialog by remember { mutableStateOf(false) }
    var showDigitalIdDialogForClient by remember { mutableStateOf<Client?>(null) }
    val profileBackStack = remember { mutableStateListOf<Client>() }
    var showNotificationCenterDialog by remember { mutableStateOf(false) }
    var showAdminResourcesDialog by remember { mutableStateOf(false) }
    var readNotificationIds by remember(loggedInUserEmail) {
        val saved = sharedPrefs.getStringSet("read_notifications_${loggedInUserEmail.lowercase()}", emptySet()) ?: emptySet()
        mutableStateOf(saved)
    }

    val persistReadNotifications: (Collection<String>) -> Unit = { ids ->
        val updated = readNotificationIds + ids
        readNotificationIds = updated
        sharedPrefs.edit().putStringSet("read_notifications_${loggedInUserEmail.lowercase()}", updated).apply()
    }

    // Manejador del botón y gesto de retroceso global para devolver con precisión exacta al punto previo
    BackHandler(enabled = profileBackStack.isNotEmpty()) {
        profileBackStack.removeAt(profileBackStack.lastIndex)
    }

    val currentTab = if (isUserAdmin) selectedTab else userSelectedTab
    BackHandler(enabled = profileBackStack.isEmpty() && currentTab != 0) {
        if (isUserAdmin) {
            viewModel.selectTab(0)
        } else {
            userSelectedTab = 0
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        AppResourceManager.init(context)
        viewModel.syncEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (!isAuthenticated) {
        LoginScreen(
            clients = clients,
            onLoginSuccess = { email, _ ->
                val verifiedAdmin = AdminConfigManager.isAdmin(context, email)
                sharedPrefs.edit()
                    .putBoolean("is_authenticated", true)
                    .putString("logged_in_email", email)
                    .putBoolean("is_user_admin", verifiedAdmin)
                    .apply()
                loggedInUserEmail = email
                isAuthenticated = true
            }
        )
        return
    }

    // Cliente activo autenticado (para vista de usuario o para autor de posts del admin)
    val currentLoggedInClient = remember(clients, loggedInUserEmail) {
        clients.firstOrNull { it.email.equals(loggedInUserEmail, ignoreCase = true) }
            ?: clients.firstOrNull { it.fullName.contains("Alex", ignoreCase = true) }
            ?: clients.firstOrNull()
            ?: Client(
                id = 9999L,
                fullName = if (isUserAdmin) "Alex Gómez" else "Atleta Actitud Fuerte",
                phone = "0412-0000000",
                email = loggedInUserEmail,
                membershipPlan = "Membresía 20$",
                paymentFrequency = "Mensual",
                emergencyContact = "Contacto de Emergencia",
                mainObjective = "Aumento de fuerza y potencia",
                medicalCondition = "Ninguna de las anteriores",
                athleteAlias = if (isUserAdmin) "COACH ALEX" else "WARRIOR",
                starPr = "180 kg",
                accessId = "AF-1001"
            )
    }

    val athleteTier = remember(currentLoggedInClient, attendances) {
        val userCheckins = attendances.count { it.clientId == currentLoggedInClient.id }
        AthleteTier.getTierForClient(currentLoggedInClient, userCheckins)
    }

    val allWorkoutLogs by viewModel.allWorkoutLogs.collectAsState()
    val userWorkoutLogs = remember(allWorkoutLogs, currentLoggedInClient) {
        allWorkoutLogs.filter { it.clientId == currentLoggedInClient.id }
    }

    val allAnnouncements = remember(socialPosts) {
        socialPosts
            .filter { it.category == "ANUNCIO_OFICIAL" }
            .map { AdminAnnouncement.fromSocialPost(it) }
    }
    val appNotifications = remember(
        socialPosts,
        allAnnouncements,
        currentLoggedInClient,
        clients,
        attendances,
        payments,
        isUserAdmin,
        readNotificationIds
    ) {
        buildAppNotifications(
            currentClient = currentLoggedInClient,
            allPosts = socialPosts,
            announcements = allAnnouncements,
            allClients = clients,
            allAttendances = attendances,
            allPayments = payments,
            isAdmin = isUserAdmin
        ).map { notif ->
            if (readNotificationIds.contains(notif.id)) notif.copy(isRead = true) else notif
        }
    }
    val unreadSystem = remember(appNotifications) {
        appNotifications.any { it.type == NotificationType.SYSTEM && !it.isRead }
    }
    val unreadSocial = remember(appNotifications) {
        appNotifications.any { it.type == NotificationType.SOCIAL && !it.isRead }
    }
    val totalUnreadCount = remember(appNotifications) {
        appNotifications.count { !it.isRead }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BlackBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceDark,
                    contentColor = LimeGreen,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        topBar = {
            BrandHeader(
                trailingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isUserAdmin) {
                            // Acceso a Directorio General (Admin)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable { showAdminDirectoryDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📇", fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("DIRECTORIO", color = Color(0xFF00F0FF), fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }

                            // Botón + Atleta
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, LimeGreen.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                                    .clickable { showRegistrationDialog = true }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("NUEVO", color = LimeGreen, fontWeight = FontWeight.Black, fontSize = 9.sp)
                                }
                            }
                        } else {
                            // Botón visible de Centro de Notificaciones (Exclusivo en el espacio del atleta / usuario donde quedó perfecto)
                            NotificationBellButton(
                                hasUnreadSystem = unreadSystem,
                                hasUnreadSocial = unreadSocial,
                                totalUnreadCount = totalUnreadCount,
                                onClick = { showNotificationCenterDialog = true }
                            )

                            // Badge de Nivel Jerárquico del Usuario Conectado
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(athleteTier.neonColor.copy(alpha = 0.15f))
                                    .border(1.dp, athleteTier.neonColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(athleteTier.neonColor)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentLoggedInClient.athleteAlias.ifBlank { currentLoggedInClient.fullName.split(" ").firstOrNull() ?: "ATLETA" }.uppercase(),
                                        color = athleteTier.neonColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        // Botón de Cerrar Sesión
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    sharedPrefs.edit().clear().apply()
                                    isAuthenticated = false
                                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Cerrar sesión",
                                    tint = TextMuted,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("SALIR", color = TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column {
                Divider(color = SurfaceBorder, thickness = 1.dp)
                if (isUserAdmin) {
                    // Barra de Navegación Administrador (5 Pestañas de Gestión)
                    NavigationBar(
                        containerColor = BlackBackground,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(68.dp)
                    ) {
                        NavigationTab.allTabs.forEach { tab ->
                            val isSelected = selectedTab == tab.index
                            val tabColor = when (tab.index) {
                                0 -> LimeGreen
                                1 -> Color(0xFF38BDF8)
                                2 -> Color(0xFFFBBF24)
                                3 -> Color(0xFFA855F7)
                                4 -> Color(0xFF60A5FA)
                                else -> LimeGreen
                            }
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { viewModel.selectTab(tab.index) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) tabColor else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) tabColor else TextMuted,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 9.5.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = tabColor.copy(alpha = 0.18f)
                                )
                            )
                        }
                    }
                } else {
                    // Barra de Navegación de Atletas / Usuarios (3 Espacios: Mi Perfil, Historial y Directorio)
                    NavigationBar(
                        containerColor = BlackBackground,
                        tonalElevation = 0.dp,
                        modifier = Modifier.height(68.dp)
                    ) {
                        UserNavigationTab.allTabs.forEach { tab ->
                            val isSelected = userSelectedTab == tab.index
                            val tabColor = when (tab.index) {
                                0 -> LimeGreen // Mi Perfil
                                1 -> Color(0xFFFBBF24) // Ámbar / Oro Fuerza para Historial
                                2 -> Color(0xFF00F0FF) // Cyan Eléctrico Directorio
                                else -> LimeGreen
                            }
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { userSelectedTab = tab.index },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        tint = if (isSelected) tabColor else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        color = if (isSelected) tabColor else TextMuted,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = tabColor.copy(alpha = 0.18f)
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BlackBackground)
        ) {
            if (isUserAdmin) {
                // Vistas Exclusivas del Administrador
                when (selectedTab) {
                    0 -> DashboardScreen(
                        totalClients = totalClients,
                        activeClients = activeClients,
                        todayCheckIns = todayCheckInCount,
                        totalIncome = totalIncome ?: 0.0,
                        recentAttendances = todayAttendances,
                        clients = clients,
                        routines = routines,
                        onSaveRoutine = { routine -> viewModel.saveRoutine(routine) },
                        onDeleteRoutine = { routine -> viewModel.deleteRoutine(routine) },
                        onRotateRoutine = { targetType, clientName, onResult ->
                            viewModel.getRandomRoutine(targetType, clientName, onResult)
                        },
                        onRequestFirebaseBackup = { viewModel.createFirebaseBackupSummary() },
                        onSyncFirebase = { onResult -> viewModel.syncWithFirebase(onResult) },
                        syncStatus = syncStatus,
                        onOpenRegister = { showRegistrationDialog = true },
                        onOpenClient = { client -> viewModel.openClientDetail(client) },
                        onQuickCheckIn = { viewModel.selectTab(1) },
                        onNavigateToTab = { tabIndex -> viewModel.selectTab(tabIndex) },
                        onOpenBroadcast = { showBroadcastDialog = true },
                        onOpenReports = { showReportsExportDialog = true },
                        onOpenLeadership = { showLeadershipDialog = true },
                        onOpenResources = { showAdminResourcesDialog = true },
                        onOpenNotifications = { showNotificationCenterDialog = true },
                        unreadSystem = unreadSystem,
                        unreadSocial = unreadSocial,
                        totalUnreadCount = totalUnreadCount,
                        onUpdateAdminPassword = { newPin, onDone ->
                            viewModel.updateSuperAdminPassword(context, newPin, onDone)
                        }
                    )
                    1 -> AttendanceScreen(
                        attendances = todayAttendances,
                        clients = clients,
                        onCheckIn = { client -> viewModel.checkInClient(client) },
                        onOpenClient = { client -> viewModel.openClientDetail(client) }
                    )
                    2 -> PaymentsScreen(
                        payments = payments,
                        clients = clients,
                        totalIncome = totalIncome ?: 0.0,
                        onRecordPayment = { payment -> viewModel.recordPayment(payment) },
                        onOpenClient = { client -> viewModel.openClientDetail(client) }
                    )
                    3 -> ProgressScreen(
                        clients = clients,
                        allMeasurements = measurements,
                        onSaveMeasurement = { m, wa, em -> viewModel.recordMeasurement(m) }
                    )
                    4 -> UsersListScreen(
                        clients = clients,
                        payments = payments,
                        attendances = attendances,
                        onOpenClient = { client -> viewModel.openClientDetail(client) },
                        onOpenSocialProfile = { client -> profileBackStack.add(client) },
                        onRegisterNewClient = { showRegistrationDialog = true }
                    )
                }
            } else {
                // Vistas de la Zona de Usuarios / Atletas (3 Pestañas)
                when (userSelectedTab) {
                    0 -> {
                        // Mi Perfil Fit (con historias 24h, biografía, rutinas y feed 3:4)
                        val athletePosts = socialPosts.filter { it.authorId == currentLoggedInClient.id }
                        val athleteAttendances = attendances.filter { it.clientId == currentLoggedInClient.id }

                        AthleteProfileContent(
                            client = currentLoggedInClient,
                            posts = athletePosts,
                            attendances = athleteAttendances,
                            routines = routines,
                            allPosts = socialPosts,
                            allClients = clients,
                            isAdmin = isUserAdmin,
                            viewerClient = currentLoggedInClient,
                            showBackButton = false,
                            onDismiss = {},
                            onOpenNotifications = { showNotificationCenterDialog = true },
                            unreadSystem = unreadSystem,
                            unreadSocial = unreadSocial,
                            totalUnreadCount = totalUnreadCount,
                            onCreatePost = { post -> viewModel.createSocialPost(post) },
                            onToggleLike = { postId -> viewModel.toggleLikePost(postId) },
                            onDeletePost = { post -> viewModel.deleteSocialPost(post) },
                            onSaveRoutine = { routine -> viewModel.updateWeeklyRoutine(currentLoggedInClient, routine) },
                            onUpdateClient = { updated ->
                                viewModel.saveClient(updated)
                            },
                            onAddComment = { postId, text, replyId, replyAuthor ->
                                viewModel.addCommentToPost(postId, currentLoggedInClient, text, replyId, replyAuthor)
                            },
                            onToggleLikeComment = { postId, commentId ->
                                viewModel.toggleLikeComment(postId, commentId, currentLoggedInClient.id)
                            },
                            onOpenAuthorProfile = { authorClient ->
                                profileBackStack.add(authorClient)
                            }
                        )
                    }
                    1 -> {
                        // Pestaña Historial de Cargas: Registro de peso/reps por ejercicio, comparación y progresión
                        WorkoutLoadHistoryScreen(
                            client = currentLoggedInClient,
                            routines = routines,
                            loadLogs = userWorkoutLogs,
                            onNavigateBack = { userSelectedTab = 0 },
                            onSaveLoadLog = { log ->
                                viewModel.saveWorkoutLoadLog(log)
                                Toast.makeText(context, "¡Carga de ${log.exerciseName} registrada! 💥", Toast.LENGTH_SHORT).show()
                            },
                            onDeleteLoadLog = { log ->
                                viewModel.deleteWorkoutLoadLog(log)
                                Toast.makeText(context, "Registro eliminado del historial", Toast.LENGTH_SHORT).show()
                            },
                            onShareAsPost = { exerciseName, weightKg, reps ->
                                val bragText = "¡Nuevo récord en $exerciseName: ${weightKg.toInt()} kg × $reps reps! 🔥 Sobrecarga progresiva #ActitudFuerte"
                                val post = SocialPost(
                                    authorId = currentLoggedInClient.id,
                                    authorName = currentLoggedInClient.fullName,
                                    authorAccessId = currentLoggedInClient.accessId,
                                    category = "PROGRESO",
                                    contentText = bragText,
                                    timestamp = System.currentTimeMillis()
                                )
                                viewModel.createSocialPost(post)
                                Toast.makeText(context, "¡Récord compartido en el Feed de la Comunidad! 💪", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    2 -> {
                        // Directorio General de Atletas (Sobrio y Unificado)
                        AthleteDirectoryScreen(
                            clients = clients,
                            attendances = attendances,
                            currentLoggedInEmail = loggedInUserEmail,
                            onNavigateBack = { userSelectedTab = 0 },
                            onOpenProfile = { client ->
                                profileBackStack.add(client)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Formulario de Registro de Nuevo Usuario
    if (showRegistrationDialog) {
        ClientRegistrationDialog(
            initialClient = null,
            onDismiss = { showRegistrationDialog = false },
            onSave = { newClient ->
                viewModel.saveClient(newClient) { createdId ->
                    showRegistrationDialog = false
                    Toast.makeText(context, "¡Atleta ${newClient.fullName} registrado con éxito!", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    // Modal Ficha Técnica del Usuario (Panel Administrativo)
    if (selectedClientForDetail != null) {
        val currentClient = selectedClientForDetail!!
        val clientMeasurements = measurements.filter { it.clientId == currentClient.id }
        val clientAttendances = attendances.filter { it.clientId == currentClient.id }
        val clientPayments = payments.filter { it.clientId == currentClient.id }

        ClientDetailDialog(
            client = currentClient,
            measurements = clientMeasurements,
            attendances = clientAttendances,
            payments = clientPayments,
            posts = socialPosts.filter { it.authorId == currentClient.id },
            routines = routines,
            allPosts = socialPosts,
            onDismiss = { viewModel.closeClientDetail() },
            onStatusChange = { newStatus ->
                viewModel.updateOperationalStatus(currentClient.id, newStatus)
            },
            onCheckIn = {
                viewModel.checkInClient(currentClient)
                Toast.makeText(context, "Check-in registrado para ${currentClient.fullName}", Toast.LENGTH_SHORT).show()
            },
            onRecordPayment = { payment ->
                viewModel.recordPayment(payment)
            },
            onSaveMeasurement = { measurement, _, _ ->
                viewModel.recordMeasurement(measurement)
            },
            onSaveRoutine = { routine ->
                viewModel.updateWeeklyRoutine(currentClient, routine)
            },
            onEditClient = { updatedClient ->
                viewModel.saveClient(updatedClient)
            },
            onDeleteClient = { clientToDelete ->
                viewModel.deleteClient(clientToDelete)
                Toast.makeText(context, "Atleta eliminado del sistema", Toast.LENGTH_SHORT).show()
            },
            onCreatePost = { post -> viewModel.createSocialPost(post) },
            onToggleLike = { postId -> viewModel.toggleLikePost(postId) },
            onDeletePost = { post -> viewModel.deleteSocialPost(post) }
        )
    }

    // Modal Perfil de Atleta Independiente con Pila de Navegación (BackStack)
    if (profileBackStack.isNotEmpty()) {
        val athlete = profileBackStack.last()
        val athletePosts = socialPosts.filter { it.authorId == athlete.id }
        val athleteAttendances = attendances.filter { it.clientId == athlete.id }

        AthleteProfileDialog(
            client = athlete,
            posts = athletePosts,
            attendances = athleteAttendances,
            routines = routines,
            allPosts = socialPosts,
            allClients = clients,
            isAdmin = isUserAdmin,
            viewerClient = currentLoggedInClient,
            onOpenNotifications = { showNotificationCenterDialog = true },
            unreadSystem = unreadSystem,
            unreadSocial = unreadSocial,
            totalUnreadCount = totalUnreadCount,
            onDismiss = {
                if (profileBackStack.isNotEmpty()) {
                    profileBackStack.removeAt(profileBackStack.lastIndex)
                }
            },
            onCreatePost = { post -> viewModel.createSocialPost(post) },
            onToggleLike = { postId -> viewModel.toggleLikePost(postId) },
            onDeletePost = { post -> viewModel.deleteSocialPost(post) },
            onSaveRoutine = { routine -> viewModel.updateWeeklyRoutine(athlete, routine) },
            onUpdateClient = { updated ->
                viewModel.saveClient(updated)
                val idx = profileBackStack.indexOfFirst { it.id == updated.id }
                if (idx != -1) {
                    profileBackStack[idx] = updated
                }
            },
            onAddComment = { postId, text, replyId, replyAuthor ->
                val commentAuthor = currentLoggedInClient ?: athlete
                viewModel.addCommentToPost(postId, commentAuthor, text, replyId, replyAuthor)
            },
            onToggleLikeComment = { postId, commentId ->
                val commenterId = currentLoggedInClient?.id ?: athlete.id
                viewModel.toggleLikeComment(postId, commentId, commenterId)
            },
            onOpenAuthorProfile = { authorClient ->
                profileBackStack.add(authorClient)
            }
        )
    }

    // Modal Centro de Notificaciones Unificado (Diferenciación Sistema Ámbar vs Social Cyan)
    if (showNotificationCenterDialog) {
        NotificationCenterDialog(
            notifications = appNotifications,
            onDismiss = {
                persistReadNotifications(appNotifications.map { it.id })
                showNotificationCenterDialog = false
            },
            onMarkAllAsRead = {
                persistReadNotifications(appNotifications.map { it.id })
                Toast.makeText(context, "Todas las notificaciones marcadas como leídas", Toast.LENGTH_SHORT).show()
            },
            onNotificationClick = { notification ->
                persistReadNotifications(listOf(notification.id))
                showNotificationCenterDialog = false

                when (notification.actionTarget) {
                    "POST" -> {
                        if (notification.targetId > 0L) {
                            val relatedPost = socialPosts.firstOrNull { it.id == notification.targetId }
                            if (relatedPost != null) {
                                val author = clients.firstOrNull { it.id == relatedPost.authorId }
                                if (author != null) {
                                    profileBackStack.add(author)
                                }
                            }
                        } else if (isUserAdmin) {
                            viewModel.selectTab(4)
                        } else {
                            userSelectedTab = 0
                        }
                    }
                    "ANNOUNCEMENT" -> {
                        if (isUserAdmin) {
                            showBroadcastDialog = true
                        } else {
                            userSelectedTab = 0
                            val annPost = socialPosts.firstOrNull {
                                it.id == notification.targetId || (it.category == "ANUNCIO_OFICIAL" && it.id == notification.targetId)
                            }
                            if (annPost != null) {
                                val author = clients.firstOrNull { it.id == annPost.authorId }
                                if (author != null) {
                                    profileBackStack.add(author)
                                }
                            } else {
                                Toast.makeText(context, "📢 Comunicado oficial fijado en el muro principal", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    "MEMBERSHIP" -> {
                        if (isUserAdmin) {
                            val targetClient = if (notification.targetId > 0L) {
                                clients.firstOrNull { it.id == notification.targetId }
                            } else null

                            if (targetClient != null) {
                                viewModel.openClientDetail(targetClient)
                            } else {
                                viewModel.selectTab(3)
                            }
                        } else {
                            val athleteClient = currentLoggedInClient ?: clients.firstOrNull { it.id == notification.targetId }
                            if (athleteClient != null) {
                                showDigitalIdDialogForClient = athleteClient
                            }
                        }
                    }
                    "ATTENDANCE" -> {
                        profileBackStack.clear()
                        if (isUserAdmin) {
                            viewModel.selectTab(1)
                        } else {
                            userSelectedTab = 1
                        }
                    }
                    "PAYMENT" -> {
                        profileBackStack.clear()
                        if (isUserAdmin) {
                            viewModel.selectTab(2)
                        } else {
                            val athleteClient = currentLoggedInClient ?: clients.firstOrNull { it.id == notification.targetId }
                            if (athleteClient != null) {
                                showDigitalIdDialogForClient = athleteClient
                            }
                        }
                    }
                    "DIRECTORY" -> {
                        if (isUserAdmin) {
                            showAdminDirectoryDialog = true
                        } else {
                            userSelectedTab = 2
                        }
                    }
                    else -> {
                        if (notification.targetId > 0L) {
                            val client = clients.firstOrNull { it.id == notification.targetId }
                            if (client != null) {
                                if (isUserAdmin) viewModel.openClientDetail(client)
                                else profileBackStack.add(client)
                            }
                        }
                    }
                }
            }
        )
    }

    // Modal Carnet Digital de Atleta (Activado desde notificaciones o perfil)
    if (showDigitalIdDialogForClient != null) {
        DigitalIdCardDialog(
            client = showDigitalIdDialogForClient!!,
            onDismiss = { showDigitalIdDialogForClient = null }
        )
    }

    // Modal Directorio de Atletas (Acceso directo desde el Panel Admin)
    if (showAdminDirectoryDialog) {
        Dialog(
            onDismissRequest = { showAdminDirectoryDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BlackBackground)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceDark)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showAdminDirectoryDialog = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextLightGray)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DIRECTORIO GENERAL DE ATLETAS",
                                color = Color(0xFF00F0FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }
                    }
                    Divider(color = SurfaceBorder, thickness = 1.dp)
                    AthleteDirectoryScreen(
                        clients = clients,
                        attendances = attendances,
                        currentLoggedInEmail = loggedInUserEmail,
                        onOpenProfile = { client ->
                            profileBackStack.add(client)
                        }
                    )
                }
            }
        }
    }

    // Modal Difusión Masiva por WhatsApp
    if (showBroadcastDialog) {
        BroadcastDialog(
            clients = clients,
            onDismiss = { showBroadcastDialog = false }
        )
    }

    // Modal Exportación de Reportes (CSV / Excel / PDF / Resumen)
    if (showReportsExportDialog) {
        ReportsExportDialog(
            clients = clients,
            payments = payments,
            attendances = attendances,
            measurements = measurements,
            totalIncome = totalIncome ?: 0.0,
            onDismiss = { showReportsExportDialog = false }
        )
    }

    // Modal Liderazgo & Cercanía (Check-in 21:00 hs con IA)
    if (showLeadershipDialog) {
        LeadershipDialog(
            clients = clients,
            onDismiss = { showLeadershipDialog = false }
        )
    }

    // Modal Centro de Recursos y Descargas (Acceso directo desde el Panel Admin)
    if (showAdminResourcesDialog) {
        ResourcesHubDialog(
            isAdmin = true,
            uploaderName = currentLoggedInClient.fullName.ifBlank { "Administrador Actitud Fuerte" },
            onDismiss = { showAdminResourcesDialog = false }
        )
    }
}
