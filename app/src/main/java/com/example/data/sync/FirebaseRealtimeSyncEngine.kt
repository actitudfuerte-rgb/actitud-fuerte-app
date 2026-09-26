package com.example.data.sync

import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.example.data.model.SocialPost
import com.example.data.repository.FitnessRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Motor de sincronización bidireccional en tiempo real con Google Cloud Firestore.
 * Conecta el dispositivo local (celular APK u ordenador) y mantiene la consistencia
 * instantánea en ambas direcciones para todas las entidades del gimnasio.
 */
class FirebaseRealtimeSyncEngine(
    private val repository: FitnessRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val firestore by lazy {
        val db = FirebaseFirestore.getInstance()
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            db.firestoreSettings = settings
        } catch (_: Exception) {}
        db
    }

    sealed class SyncStatus {
        object Connecting : SyncStatus()
        data class Live(val lastSyncTimestamp: Long, val message: String = "Nube en vivo sincronizada") : SyncStatus()
        data class Syncing(val message: String) : SyncStatus()
        data class Error(val message: String) : SyncStatus()
    }

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Connecting)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _syncEvents = MutableSharedFlow<String>(extraBufferCapacity = 20, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val syncEvents: SharedFlow<String> = _syncEvents.asSharedFlow()

    private val listeners = mutableListOf<ListenerRegistration>()
    private var isStarted = false

    /**
     * Inicia los 5 escuchadores de eventos en tiempo real hacia Firestore.
     */
    fun startRealtimeSync() {
        if (isStarted) return
        isStarted = true
        _syncStatus.value = SyncStatus.Connecting

        try {
            // 1. Escuchador de Clientes / Atletas
            val clientReg = firestore.collection("gym_clients")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = SyncStatus.Error("Error en sincronización de clientes: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    scope.launch {
                        for (dc in snapshot.documentChanges) {
                            val doc = dc.document
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: (abs(doc.id.hashCode().toLong()) % 1_000_000L + 1000L)
                            when (dc.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val client = Client(
                                        id = id,
                                        fullName = doc.getString("fullName") ?: "",
                                        phone = doc.getString("phone") ?: "",
                                        email = doc.getString("email") ?: "",
                                        membershipPlan = doc.getString("membershipPlan") ?: "Membresía 20$",
                                        paymentFrequency = doc.getString("paymentFrequency") ?: "Mensual",
                                        emergencyContact = doc.getString("emergencyContact") ?: "",
                                        mainObjective = doc.getString("mainObjective") ?: "Hipertrofia y ganancia muscular",
                                        medicalCondition = doc.getString("medicalCondition") ?: "Ninguna de las anteriores",
                                        medicalNotes = doc.getString("medicalNotes") ?: "",
                                        operationalStatus = doc.getString("operationalStatus") ?: "Activo",
                                        registrationTimestamp = doc.getLong("registrationTimestamp") ?: System.currentTimeMillis(),
                                        accessId = doc.getString("accessId") ?: "AF-${id}",
                                        accessPin = doc.getString("accessPin") ?: "",
                                        weeklyRoutine = doc.getString("weeklyRoutine") ?: "",
                                        bio = doc.getString("bio") ?: "Atleta de Alto Rendimiento • Actitud Fuerte",
                                        athleteAlias = doc.getString("athleteAlias") ?: "",
                                        starPr = doc.getString("starPr") ?: "",
                                        socialStatus = doc.getString("socialStatus") ?: "Activo",
                                        avatarUrl = doc.getString("avatarUrl") ?: ""
                                    )
                                    repository.saveClientFromRemote(client)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    repository.deleteClientByIdFromRemote(id)
                                }
                            }
                        }
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                    }
                }
            listeners.add(clientReg)

            // 2. Escuchador de Biblioteca Maestra de Rutinas
            val routineReg = firestore.collection("gym_routines")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = SyncStatus.Error("Error en sincronización de rutinas: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    scope.launch {
                        for (dc in snapshot.documentChanges) {
                            val doc = dc.document
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: (abs(doc.id.hashCode().toLong()) % 1_000_000L + 1000L)
                            when (dc.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val routine = Routine(
                                        id = id,
                                        name = doc.getString("name") ?: "",
                                        targetType = doc.getString("targetType") ?: "General",
                                        targetClientNames = doc.getString("targetClientNames") ?: "",
                                        specialConditions = doc.getString("specialConditions") ?: "",
                                        exercisesJson = doc.getString("exercisesJson") ?: "",
                                        createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                    )
                                    repository.saveRoutineFromRemote(routine)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    repository.deleteRoutineByIdFromRemote(id)
                                }
                            }
                        }
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                    }
                }
            listeners.add(routineReg)

            // 3. Escuchador de Asistencias (Check-ins)
            val attendanceReg = firestore.collection("gym_attendances")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = SyncStatus.Error("Error en sincronización de asistencias: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    scope.launch {
                        for (dc in snapshot.documentChanges) {
                            val doc = dc.document
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: (abs(doc.id.hashCode().toLong()) % 1_000_000L + 1000L)
                            if (dc.type == DocumentChange.Type.ADDED || dc.type == DocumentChange.Type.MODIFIED) {
                                val att = Attendance(
                                    id = id,
                                    clientId = doc.getLong("clientId") ?: 0L,
                                    clientName = doc.getString("clientName") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    dateOnlyString = doc.getString("date") ?: "",
                                    timeOnlyString = doc.getString("time") ?: ""
                                )
                                repository.saveAttendanceFromRemote(att)
                            }
                        }
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                    }
                }
            listeners.add(attendanceReg)

            // 4. Escuchador de Pagos
            val paymentReg = firestore.collection("gym_payments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = SyncStatus.Error("Error en sincronización de pagos: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    scope.launch {
                        for (dc in snapshot.documentChanges) {
                            val doc = dc.document
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: (abs(doc.id.hashCode().toLong()) % 1_000_000L + 1000L)
                            if (dc.type == DocumentChange.Type.ADDED || dc.type == DocumentChange.Type.MODIFIED) {
                                val pay = Payment(
                                    id = id,
                                    clientId = doc.getLong("clientId") ?: 0L,
                                    clientName = doc.getString("clientName") ?: "",
                                    amount = doc.getDouble("amount") ?: 0.0,
                                    planName = doc.getString("planName") ?: "Membresía",
                                    frequency = doc.getString("frequency") ?: "Mensual",
                                    method = doc.getString("method") ?: "Efectivo",
                                    reference = doc.getString("reference") ?: "",
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                                )
                                repository.savePaymentFromRemote(pay)
                            }
                        }
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                    }
                }
            listeners.add(paymentReg)

            // 5. Escuchador de Medidas Antropométricas
            val measurementReg = firestore.collection("gym_measurements")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = SyncStatus.Error("Error en sincronización de medidas: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    scope.launch {
                        for (dc in snapshot.documentChanges) {
                            val doc = dc.document
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: (abs(doc.id.hashCode().toLong()) % 1_000_000L + 1000L)
                            if (dc.type == DocumentChange.Type.ADDED || dc.type == DocumentChange.Type.MODIFIED) {
                                val meas = Measurement(
                                    id = id,
                                    clientId = doc.getLong("clientId") ?: 0L,
                                    timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                    weightKg = doc.getDouble("weightKg") ?: 0.0,
                                    backCm = doc.getDouble("backCm") ?: 0.0,
                                    shouldersCm = doc.getDouble("shouldersCm") ?: 0.0,
                                    armsCm = doc.getDouble("armsCm") ?: 0.0,
                                    hipsCm = doc.getDouble("hipsCm") ?: 0.0,
                                    aiObservation = doc.getString("aiObservation") ?: "",
                                    trainerObservation = doc.getString("trainerObservation") ?: "",
                                    sentViaEmail = doc.getBoolean("sentViaEmail") ?: false,
                                    sentViaWhatsapp = doc.getBoolean("sentViaWhatsapp") ?: false
                                )
                                repository.saveMeasurementFromRemote(meas)
                            }
                        }
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                    }
                }
            listeners.add(measurementReg)

            // 6. Escuchador de Publicaciones de la Red Social e Interacciones en Vivo
            val socialPostReg = firestore.collection("gym_social_posts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _syncStatus.value = SyncStatus.Error("Error en sincronización de posts sociales: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    scope.launch {
                        for (dc in snapshot.documentChanges) {
                            val doc = dc.document
                            val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: (abs(doc.id.hashCode().toLong()) % 1_000_000L + 1000L)
                            when (dc.type) {
                                DocumentChange.Type.ADDED, DocumentChange.Type.MODIFIED -> {
                                    val post = SocialPost(
                                        id = id,
                                        authorId = doc.getLong("authorId") ?: 0L,
                                        authorName = doc.getString("authorName") ?: "",
                                        authorAccessId = doc.getString("authorAccessId") ?: "",
                                        authorAvatarUrl = doc.getString("authorAvatarUrl") ?: "",
                                        contentText = doc.getString("contentText") ?: "",
                                        mediaUrl = doc.getString("mediaUrl") ?: "",
                                        mediaType = doc.getString("mediaType") ?: "IMAGE",
                                        aspectRatio = doc.getString("aspectRatio") ?: "3:4",
                                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                        likesCount = (doc.getLong("likesCount") ?: 0L).toInt(),
                                        commentsCount = (doc.getLong("commentsCount") ?: 0L).toInt(),
                                        isLikedByMe = false,
                                        category = doc.getString("category") ?: "ENTRENAMIENTO",
                                        workoutDetails = doc.getString("workoutDetails") ?: "",
                                        commentsJson = doc.getString("commentsJson") ?: "[]"
                                    )
                                    repository.saveSocialPostFromRemote(post)
                                }
                                DocumentChange.Type.REMOVED -> {
                                    repository.deleteSocialPostByIdFromRemote(id)
                                }
                            }
                        }
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                    }
                }
            listeners.add(socialPostReg)

        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Error("Fallo al inicializar listeners de Firestore: ${e.message}")
        }
    }

    /**
     * Envío en tiempo real a Firestore cuando se crea o modifica un Cliente en la UI.
     */
    fun pushClient(client: Client) {
        scope.launch {
            try {
                _syncStatus.value = SyncStatus.Syncing("Guardando atleta en la nube...")
                val docRef = firestore.collection("gym_clients").document(client.id.toString())
                val map = hashMapOf(
                    "id" to client.id,
                    "accessId" to client.accessId,
                    "accessPin" to client.accessPin,
                    "fullName" to client.fullName,
                    "phone" to client.phone,
                    "email" to client.email,
                    "membershipPlan" to client.membershipPlan,
                    "paymentFrequency" to client.paymentFrequency,
                    "emergencyContact" to client.emergencyContact,
                    "mainObjective" to client.mainObjective,
                    "medicalCondition" to client.medicalCondition,
                    "medicalNotes" to client.medicalNotes,
                    "operationalStatus" to client.operationalStatus,
                    "registrationTimestamp" to client.registrationTimestamp,
                    "weeklyRoutine" to client.weeklyRoutine,
                    "bio" to client.bio,
                    "athleteAlias" to client.athleteAlias,
                    "starPr" to client.starPr,
                    "socialStatus" to client.socialStatus,
                    "avatarUrl" to client.avatarUrl,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Sincronizado en la nube: ${client.fullName}")
                    }
                    .addOnFailureListener { err ->
                        _syncStatus.value = SyncStatus.Error("Error al guardar: ${err.message}")
                        _syncEvents.tryEmit("⚠️ Error al subir atleta: ${err.localizedMessage}")
                    }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error("Excepción: ${e.message}")
            }
        }
    }

    /**
     * Eliminación en tiempo real en Firestore cuando se borra un Cliente en la UI.
     */
    fun deleteClient(clientId: Long) {
        scope.launch {
            try {
                firestore.collection("gym_clients").document(clientId.toString()).delete()
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("🗑️ Atleta eliminado de la nube")
                    }
                    .addOnFailureListener { err ->
                        _syncStatus.value = SyncStatus.Error("Error al eliminar: ${err.message}")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Envío en tiempo real a Firestore cuando se crea o actualiza una Rutina en la UI.
     */
    fun pushRoutine(routine: Routine) {
        scope.launch {
            try {
                _syncStatus.value = SyncStatus.Syncing("Guardando rutina en la nube...")
                val docRef = firestore.collection("gym_routines").document(routine.id.toString())
                val map = hashMapOf(
                    "id" to routine.id,
                    "name" to routine.name,
                    "targetType" to routine.targetType,
                    "targetClientNames" to routine.targetClientNames,
                    "specialConditions" to routine.specialConditions,
                    "exercisesJson" to routine.exercisesJson,
                    "createdAt" to routine.createdAt,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Rutina '${routine.name}' guardada en la nube")
                    }
                    .addOnFailureListener { err ->
                        _syncStatus.value = SyncStatus.Error("Error al guardar rutina: ${err.message}")
                        _syncEvents.tryEmit("⚠️ Error al subir rutina: ${err.localizedMessage}")
                    }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error("Excepción: ${e.message}")
            }
        }
    }

    /**
     * Eliminación en tiempo real en Firestore cuando se borra una Rutina en la UI.
     */
    fun deleteRoutine(routineId: Long) {
        scope.launch {
            try {
                firestore.collection("gym_routines").document(routineId.toString()).delete()
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("🗑️ Rutina eliminada de la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Envío en tiempo real de Asistencia / Check-in.
     */
    fun pushAttendance(attendance: Attendance) {
        scope.launch {
            try {
                val docRef = firestore.collection("gym_attendances").document(attendance.id.toString())
                val map = hashMapOf(
                    "id" to attendance.id,
                    "clientId" to attendance.clientId,
                    "clientName" to attendance.clientName,
                    "timestamp" to attendance.timestamp,
                    "date" to attendance.dateOnlyString,
                    "time" to attendance.timeOnlyString,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Asistencia sincronizada en la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Envío en tiempo real de Recibo / Pago.
     */
    fun pushPayment(payment: Payment) {
        scope.launch {
            try {
                val docRef = firestore.collection("gym_payments").document(payment.id.toString())
                val map = hashMapOf(
                    "id" to payment.id,
                    "clientId" to payment.clientId,
                    "clientName" to payment.clientName,
                    "amount" to payment.amount,
                    "planName" to payment.planName,
                    "frequency" to payment.frequency,
                    "method" to payment.method,
                    "reference" to payment.reference,
                    "timestamp" to payment.timestamp,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Pago de $${payment.amount} sincronizado en la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Envío en tiempo real de Medida Antropométrica.
     */
    fun pushMeasurement(measurement: Measurement) {
        scope.launch {
            try {
                val docRef = firestore.collection("gym_measurements").document(measurement.id.toString())
                val map = hashMapOf(
                    "id" to measurement.id,
                    "clientId" to measurement.clientId,
                    "timestamp" to measurement.timestamp,
                    "weightKg" to measurement.weightKg,
                    "backCm" to measurement.backCm,
                    "shouldersCm" to measurement.shouldersCm,
                    "armsCm" to measurement.armsCm,
                    "hipsCm" to measurement.hipsCm,
                    "aiObservation" to measurement.aiObservation,
                    "trainerObservation" to measurement.trainerObservation,
                    "sentViaEmail" to measurement.sentViaEmail,
                    "sentViaWhatsapp" to measurement.sentViaWhatsapp,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Medidas antropométricas sincronizadas en la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Actualización en tiempo real del estado de un socio (Activo, Inactivo, etc.).
     */
    fun updateClientStatus(clientId: Long, status: String) {
        scope.launch {
            try {
                firestore.collection("gym_clients").document(clientId.toString())
                    .update("operationalStatus", status, "lastSyncedAt", System.currentTimeMillis())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Estado actualizado en la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Envío en tiempo real de una Publicación de la Red Social (con formato vertical 3:4 o texto).
     */
    fun pushSocialPost(post: SocialPost) {
        scope.launch {
            try {
                val docRef = firestore.collection("gym_social_posts").document(post.id.toString())
                val map = hashMapOf(
                    "id" to post.id,
                    "authorId" to post.authorId,
                    "authorName" to post.authorName,
                    "authorAccessId" to post.authorAccessId,
                    "authorAvatarUrl" to post.authorAvatarUrl,
                    "contentText" to post.contentText,
                    "mediaUrl" to post.mediaUrl,
                    "mediaType" to post.mediaType,
                    "aspectRatio" to post.aspectRatio,
                    "timestamp" to post.timestamp,
                    "likesCount" to post.likesCount,
                    "commentsCount" to post.commentsCount,
                    "category" to post.category,
                    "workoutDetails" to post.workoutDetails,
                    "commentsJson" to post.commentsJson,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                docRef.set(map, SetOptions.merge())
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("☁️ Publicación de ${post.authorName} sincronizada en la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    /**
     * Eliminación en tiempo real de una Publicación en Firestore.
     */
    fun deleteSocialPost(postId: Long) {
        scope.launch {
            try {
                firestore.collection("gym_social_posts").document(postId.toString()).delete()
                    .addOnSuccessListener {
                        _syncStatus.value = SyncStatus.Live(System.currentTimeMillis())
                        _syncEvents.tryEmit("🗑️ Publicación eliminada de la nube")
                    }
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        listeners.forEach { it.remove() }
        listeners.clear()
        isStarted = false
    }
}
