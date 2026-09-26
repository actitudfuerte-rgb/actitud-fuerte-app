package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.example.data.model.SocialPost
import com.example.data.model.WorkoutLoadLog
import com.example.data.repository.FitnessRepository
import com.example.data.sync.FirebaseBackupManager
import com.example.data.sync.FirebaseRealtimeSyncEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FitnessRepository
    private val realtimeSyncEngine: FirebaseRealtimeSyncEngine

    init {
        val db = AppDatabase.getInstance(application)
        repository = FitnessRepository(
            clientDao = db.clientDao(),
            measurementDao = db.measurementDao(),
            attendanceDao = db.attendanceDao(),
            paymentDao = db.paymentDao(),
            routineDao = db.routineDao(),
            socialPostDao = db.socialPostDao(),
            workoutLoadLogDao = db.workoutLoadLogDao()
        )
        realtimeSyncEngine = FirebaseRealtimeSyncEngine(repository)

        viewModelScope.launch {
            repository.prepopulateIfNeeded()
            // Iniciar sincronización bidireccional en tiempo real con Firestore
            realtimeSyncEngine.startRealtimeSync()
        }
    }

    val syncStatus: StateFlow<FirebaseRealtimeSyncEngine.SyncStatus> = realtimeSyncEngine.syncStatus
    val syncEvents = realtimeSyncEngine.syncEvents

    // Tab navigation: 0: PANEL, 1: ASISTENCIA, 2: PAGOS, 3: PROGRESO, 4: USUARIOS
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // Clients flow
    val allClients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalClientsCount: StateFlow<Int> = repository.totalClientsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeClientsCount: StateFlow<Int> = repository.activeClientsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Attendances
    val allAttendances: StateFlow<List<Attendance>> = repository.allAttendances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val todayAttendances: StateFlow<List<Attendance>> = repository.getTodayAttendances(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayCheckInCount: StateFlow<Int> = repository.getTodayCheckInCount(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Payments
    val allPayments: StateFlow<List<Payment>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double?> = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Measurements
    val allMeasurements: StateFlow<List<Measurement>> = repository.allMeasurements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Biblioteca Maestra de Rutinas
    val allRoutines: StateFlow<List<Routine>> = repository.allRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected client for Ficha Técnica
    private val _selectedClientForDetail = MutableStateFlow<Client?>(null)
    val selectedClientForDetail: StateFlow<Client?> = _selectedClientForDetail.asStateFlow()

    fun openClientDetail(client: Client) {
        _selectedClientForDetail.value = client
    }

    fun closeClientDetail() {
        _selectedClientForDetail.value = null
    }

    // Actions
    fun saveClient(client: Client, onDone: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveClient(client)
            // If the client was open in details, refresh it
            val updated = repository.getClientOnce(if (client.id == 0L) id else client.id)
            if (_selectedClientForDetail.value?.id == client.id) {
                _selectedClientForDetail.value = updated
            }
            if (updated != null) {
                realtimeSyncEngine.pushClient(updated)
            }
            onDone?.invoke(id)
        }
    }

    fun updateOperationalStatus(clientId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateOperationalStatus(clientId, newStatus)
            val updated = repository.getClientOnce(clientId)
            if (_selectedClientForDetail.value?.id == clientId) {
                _selectedClientForDetail.value = updated
            }
            realtimeSyncEngine.updateClientStatus(clientId, newStatus)
        }
    }

    /**
     * Actualiza la clave de acceso del Super Administrador (Alex Gómez)
     * sincronizándola en Room local, SharedPreferences seguro y Firebase Firestore en tiempo real.
     */
    fun updateSuperAdminPassword(context: android.content.Context, newPin: String, onDone: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val clean = newPin.trim()
            if (clean.isBlank()) {
                onDone?.invoke(false)
                return@launch
            }
            // 1. Persistir en AdminConfigManager (SharedPreferences y Flow reactivo)
            com.example.data.manager.AdminConfigManager.updateSuperAdminPin(context, clean)

            // 2. Localizar y actualizar el registro de Alex Gómez en Room (base de datos local)
            val clients = repository.getAllClientsOnce()
            val alexClient = clients.firstOrNull { it.email.equals(com.example.data.manager.AdminConfigManager.SUPER_ADMIN_EMAIL, ignoreCase = true) }
            if (alexClient != null) {
                val updatedAlex = alexClient.copy(accessPin = clean)
                repository.saveClient(updatedAlex)
                if (_selectedClientForDetail.value?.id == updatedAlex.id) {
                    _selectedClientForDetail.value = updatedAlex
                }
                // 3. Sincronización en tiempo real hacia Firestore
                realtimeSyncEngine.pushClient(updatedAlex)
            }
            onDone?.invoke(true)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
            realtimeSyncEngine.deleteClient(client.id)
            if (_selectedClientForDetail.value?.id == client.id) {
                _selectedClientForDetail.value = null
            }
        }
    }

    fun checkInClient(client: Client) {
        viewModelScope.launch {
            val id = repository.recordCheckIn(client)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val now = Date()
            val attendance = Attendance(
                id = id,
                clientId = client.id,
                clientName = client.fullName,
                timestamp = now.time,
                dateOnlyString = dateFormat.format(now),
                timeOnlyString = timeFormat.format(now)
            )
            realtimeSyncEngine.pushAttendance(attendance)
        }
    }

    fun recordPayment(payment: Payment) {
        viewModelScope.launch {
            val id = repository.recordPayment(payment)
            val syncedPayment = if (payment.id == 0L) payment.copy(id = id) else payment
            realtimeSyncEngine.pushPayment(syncedPayment)
        }
    }

    fun recordMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            val id = repository.recordMeasurement(measurement)
            val syncedMeas = if (measurement.id == 0L) measurement.copy(id = id) else measurement
            realtimeSyncEngine.pushMeasurement(syncedMeas)
        }
    }

    fun updateWeeklyRoutine(client: Client, newRoutine: String) {
        viewModelScope.launch {
            val updated = client.copy(weeklyRoutine = newRoutine)
            repository.saveClient(updated)
            if (_selectedClientForDetail.value?.id == client.id) {
                _selectedClientForDetail.value = updated
            }
            realtimeSyncEngine.pushClient(updated)
        }
    }

    // Biblioteca Maestra Actions
    fun saveRoutine(routine: Routine, onDone: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveRoutine(routine)
            val syncedRoutine = if (routine.id == 0L) routine.copy(id = id) else routine
            realtimeSyncEngine.pushRoutine(syncedRoutine)
            onDone?.invoke(id)
        }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
            realtimeSyncEngine.deleteRoutine(routine.id)
        }
    }

    fun getRandomRoutine(targetType: String, clientName: String? = null, onResult: (Routine?) -> Unit) {
        viewModelScope.launch {
            val r = repository.getRandomRoutineFor(targetType, clientName)
            onResult(r)
        }
    }

    // Red Social Actitud Fuerte
    val allSocialPosts: StateFlow<List<SocialPost>> = repository.allSocialPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getSocialPostsForClient(clientId: Long): Flow<List<SocialPost>> =
        repository.getSocialPostsForAuthor(clientId)

    fun createSocialPost(post: SocialPost, onDone: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveSocialPost(post)
            val savedPost = if (post.id == 0L) post.copy(id = id) else post
            realtimeSyncEngine.pushSocialPost(savedPost)
            onDone?.invoke(id)
        }
    }

    fun toggleLikePost(postId: Long) {
        viewModelScope.launch {
            repository.toggleLikePost(postId)
            val updated = repository.getSocialPostOnce(postId)
            if (updated != null) {
                realtimeSyncEngine.pushSocialPost(updated)
            }
        }
    }

    fun addCommentToPost(
        postId: Long,
        author: Client,
        text: String,
        replyToCommentId: String = "",
        replyToAuthorName: String = "",
        onDone: ((SocialPost) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val updated = repository.addCommentToPost(
                postId = postId,
                authorId = author.id,
                authorName = author.fullName,
                authorAvatarUrl = author.avatarUrl,
                text = text,
                replyToCommentId = replyToCommentId,
                replyToAuthorName = replyToAuthorName
            )
            if (updated != null) {
                realtimeSyncEngine.pushSocialPost(updated)
                onDone?.invoke(updated)
            }
        }
    }

    fun toggleLikeComment(
        postId: Long,
        commentId: String,
        clientId: Long,
        onDone: ((SocialPost) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val updated = repository.toggleLikeComment(postId, commentId, clientId)
            if (updated != null) {
                realtimeSyncEngine.pushSocialPost(updated)
                onDone?.invoke(updated)
            }
        }
    }

    fun deleteSocialPost(post: SocialPost) {
        viewModelScope.launch {
            repository.deleteSocialPost(post)
            realtimeSyncEngine.deleteSocialPost(post.id)
        }
    }

    // Historial de Cargas y Sobrecarga Progresiva
    val allWorkoutLogs: StateFlow<List<WorkoutLoadLog>> = repository.allWorkoutLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getWorkoutLogsForClient(clientId: Long): Flow<List<WorkoutLoadLog>> =
        repository.getWorkoutLogsForClient(clientId)

    fun saveWorkoutLoadLog(log: WorkoutLoadLog, onDone: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.saveWorkoutLoadLog(log)
            onDone?.invoke(id)
        }
    }

    fun deleteWorkoutLoadLog(log: WorkoutLoadLog) {
        viewModelScope.launch {
            repository.deleteWorkoutLoadLog(log)
        }
    }

    // Firebase Cloud Firestore Sync
    fun syncWithFirebase(onResult: (FirebaseBackupManager.CloudSyncResult) -> Unit) {
        viewModelScope.launch {
            val clients = repository.getAllClientsOnce()
            val routines = repository.getAllRoutinesOnce()
            val attendances = repository.getAllAttendancesOnce()
            val payments = repository.getAllPaymentsOnce()
            val measurements = repository.getAllMeasurementsOnce()

            val result = FirebaseBackupManager.syncToCloudFirestore(
                clients = clients,
                routines = routines,
                attendances = attendances,
                payments = payments,
                measurements = measurements
            )
            onResult(result)
        }
    }

    // Firebase Backup
    suspend fun createFirebaseBackupSummary(): FirebaseBackupManager.BackupSummary {
        val clients = repository.getAllClientsOnce()
        val routines = repository.getAllRoutinesOnce()
        val attendances = repository.getAllAttendancesOnce()
        val payments = repository.getAllPaymentsOnce()
        val measurements = repository.getAllMeasurementsOnce()

        return FirebaseBackupManager.generateCompleteBackup(
            clients = clients,
            routines = routines,
            attendances = attendances,
            payments = payments,
            measurements = measurements
        )
    }

    override fun onCleared() {
        super.onCleared()
        realtimeSyncEngine.stop()
    }
}
