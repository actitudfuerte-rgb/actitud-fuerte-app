package com.example.data.sync

import android.content.Context
import android.content.Intent
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gestor oficial de preparación y sincronización en tiempo real hacia Cloud Firestore.
 * Mantiene la integridad total de los datos de Actitud Fuerte Gym:
 * - Clientes y Fichas Técnicas (`gym_clients`)
 * - Biblioteca Maestra de Rutinas Semanales Lunes-Sábados (`gym_routines`)
 * - Asistencias / Check-ins (`gym_attendances`)
 * - Registro de Pagos (`gym_payments`)
 * - Evaluaciones Antropométricas (`gym_measurements`)
 * - Estado y Auditoría de Sincronización (`gym_metadata/sync_status`)
 */
object FirebaseBackupManager {

    data class BackupSummary(
        val timestamp: Long,
        val formattedDate: String,
        val totalClients: Int,
        val totalRoutines: Int,
        val totalAttendances: Int,
        val totalPayments: Int,
        val totalMeasurements: Int,
        val jsonPayload: String
    )

    data class CloudSyncResult(
        val success: Boolean,
        val message: String,
        val syncedClients: Int = 0,
        val syncedRoutines: Int = 0,
        val syncedAttendances: Int = 0,
        val syncedPayments: Int = 0,
        val syncedMeasurements: Int = 0,
        val timestamp: Long = System.currentTimeMillis()
    )

    /**
     * Sincroniza atómicamente todos los datos locales hacia Google Cloud Firestore.
     * Utiliza lotes (WriteBatch) para máxima velocidad, eficiencia en cuota y consistencia ACID.
     */
    suspend fun syncToCloudFirestore(
        clients: List<Client>,
        routines: List<Routine>,
        attendances: List<Attendance>,
        payments: List<Payment>,
        measurements: List<Measurement>
    ): CloudSyncResult = withContext(Dispatchers.IO) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))

            var batch = firestore.batch()
            var opCount = 0

            suspend fun commitBatchIfNeeded(force: Boolean = false) {
                if (opCount > 0 && (opCount >= 400 || force)) {
                    suspendCancellableCoroutine<Unit> { continuation ->
                        batch.commit()
                            .addOnSuccessListener { continuation.resume(Unit) }
                            .addOnFailureListener { e -> continuation.resumeWithException(e) }
                    }
                    batch = firestore.batch()
                    opCount = 0
                }
            }

            // 1. Colección gym_clients
            clients.forEach { c ->
                val docRef = firestore.collection("gym_clients").document(c.id.toString())
                val map = hashMapOf(
                    "id" to c.id,
                    "accessId" to c.accessId,
                    "fullName" to c.fullName,
                    "phone" to c.phone,
                    "email" to c.email,
                    "membershipPlan" to c.membershipPlan,
                    "paymentFrequency" to c.paymentFrequency,
                    "emergencyContact" to c.emergencyContact,
                    "mainObjective" to c.mainObjective,
                    "medicalCondition" to c.medicalCondition,
                    "operationalStatus" to c.operationalStatus,
                    "registrationTimestamp" to c.registrationTimestamp,
                    "weeklyRoutine" to c.weeklyRoutine,
                    "lastSyncedAt" to now
                )
                batch.set(docRef, map, SetOptions.merge())
                opCount++
                commitBatchIfNeeded()
            }

            // 2. Colección gym_routines (Biblioteca Maestra Semanal)
            routines.forEach { r ->
                val docRef = firestore.collection("gym_routines").document(r.id.toString())
                val map = hashMapOf(
                    "id" to r.id,
                    "name" to r.name,
                    "targetType" to r.targetType,
                    "targetClientNames" to r.targetClientNames,
                    "specialConditions" to r.specialConditions,
                    "exercisesJson" to r.exercisesJson,
                    "createdAt" to r.createdAt,
                    "lastSyncedAt" to now
                )
                batch.set(docRef, map, SetOptions.merge())
                opCount++
                commitBatchIfNeeded()
            }

            // 3. Colección gym_attendances (Check-ins)
            attendances.forEach { a ->
                val docRef = firestore.collection("gym_attendances").document(a.id.toString())
                val map = hashMapOf(
                    "id" to a.id,
                    "clientId" to a.clientId,
                    "clientName" to a.clientName,
                    "timestamp" to a.timestamp,
                    "date" to a.dateOnlyString,
                    "time" to a.timeOnlyString,
                    "lastSyncedAt" to now
                )
                batch.set(docRef, map, SetOptions.merge())
                opCount++
                commitBatchIfNeeded()
            }

            // 4. Colección gym_payments (Cobros y Recibos)
            payments.forEach { p ->
                val docRef = firestore.collection("gym_payments").document(p.id.toString())
                val map = hashMapOf(
                    "id" to p.id,
                    "clientId" to p.clientId,
                    "clientName" to p.clientName,
                    "amount" to p.amount,
                    "planName" to p.planName,
                    "frequency" to p.frequency,
                    "method" to p.method,
                    "reference" to p.reference,
                    "timestamp" to p.timestamp,
                    "lastSyncedAt" to now
                )
                batch.set(docRef, map, SetOptions.merge())
                opCount++
                commitBatchIfNeeded()
            }

            // 5. Colección gym_measurements (Antropometría y Evaluaciones)
            measurements.forEach { m ->
                val docRef = firestore.collection("gym_measurements").document(m.id.toString())
                val map = hashMapOf(
                    "id" to m.id,
                    "clientId" to m.clientId,
                    "timestamp" to m.timestamp,
                    "weightKg" to m.weightKg,
                    "backCm" to m.backCm,
                    "shouldersCm" to m.shouldersCm,
                    "armsCm" to m.armsCm,
                    "hipsCm" to m.hipsCm,
                    "aiObservation" to m.aiObservation,
                    "trainerObservation" to m.trainerObservation,
                    "lastSyncedAt" to now
                )
                batch.set(docRef, map, SetOptions.merge())
                opCount++
                commitBatchIfNeeded()
            }

            // 6. Metadata de Sincronización
            val metaRef = firestore.collection("gym_metadata").document("sync_status")
            val metaMap = hashMapOf(
                "app" to "Actitud Fuerte Gym",
                "lastSyncTimestamp" to now,
                "lastSyncDate" to dateStr,
                "totalClients" to clients.size,
                "totalRoutines" to routines.size,
                "totalAttendances" to attendances.size,
                "totalPayments" to payments.size,
                "totalMeasurements" to measurements.size,
                "status" to "SYNC_OK"
            )
            batch.set(metaRef, metaMap, SetOptions.merge())
            opCount++

            commitBatchIfNeeded(force = true)

            CloudSyncResult(
                success = true,
                message = "Sincronización completada con éxito en Cloud Firestore ($dateStr)",
                syncedClients = clients.size,
                syncedRoutines = routines.size,
                syncedAttendances = attendances.size,
                syncedPayments = payments.size,
                syncedMeasurements = measurements.size,
                timestamp = now
            )
        } catch (e: Exception) {
            CloudSyncResult(
                success = false,
                message = "Error en sincronización Firebase: ${e.localizedMessage ?: e.message}",
                timestamp = System.currentTimeMillis()
            )
        }
    }

    fun generateCompleteBackup(
        clients: List<Client>,
        routines: List<Routine>,
        attendances: List<Attendance>,
        payments: List<Payment>,
        measurements: List<Measurement>
    ): BackupSummary {
        val root = JSONObject()
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(now))

        root.put("app", "Actitud Fuerte Gym")
        root.put("version", "1.0")
        root.put("backup_timestamp", now)
        root.put("backup_date", dateStr)
        root.put("firebase_ready", true)
        root.put("target_database", "Cloud Firestore")

        // 1. Colección Clients
        val clientsArray = JSONArray()
        clients.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("accessId", c.accessId)
            obj.put("fullName", c.fullName)
            obj.put("phone", c.phone)
            obj.put("email", c.email)
            obj.put("membershipPlan", c.membershipPlan)
            obj.put("paymentFrequency", c.paymentFrequency)
            obj.put("emergencyContact", c.emergencyContact)
            obj.put("mainObjective", c.mainObjective)
            obj.put("medicalCondition", c.medicalCondition)
            obj.put("operationalStatus", c.operationalStatus)
            obj.put("registrationTimestamp", c.registrationTimestamp)
            obj.put("weeklyRoutine", c.weeklyRoutine)
            clientsArray.put(obj)
        }
        root.put("collection_clients", clientsArray)

        // 2. Colección Biblioteca Maestra de Rutinas
        val routinesArray = JSONArray()
        routines.forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("name", r.name)
            obj.put("targetType", r.targetType)
            obj.put("targetClientNames", r.targetClientNames)
            obj.put("specialConditions", r.specialConditions)
            obj.put("exercises", JSONArray(if (r.exercisesJson.isBlank()) "[]" else r.exercisesJson))
            obj.put("createdAt", r.createdAt)
            routinesArray.put(obj)
        }
        root.put("collection_routines", routinesArray)

        // 3. Colección Asistencias
        val attArray = JSONArray()
        attendances.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("clientId", a.clientId)
            obj.put("clientName", a.clientName)
            obj.put("timestamp", a.timestamp)
            obj.put("date", a.dateOnlyString)
            obj.put("time", a.timeOnlyString)
            attArray.put(obj)
        }
        root.put("collection_attendances", attArray)

        // 4. Colección Pagos
        val payArray = JSONArray()
        payments.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("clientId", p.clientId)
            obj.put("clientName", p.clientName)
            obj.put("amount", p.amount)
            obj.put("planName", p.planName)
            obj.put("frequency", p.frequency)
            obj.put("method", p.method)
            obj.put("reference", p.reference)
            obj.put("timestamp", p.timestamp)
            payArray.put(obj)
        }
        root.put("collection_payments", payArray)

        // 5. Colección Medidas
        val measArray = JSONArray()
        measurements.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("clientId", m.clientId)
            obj.put("timestamp", m.timestamp)
            obj.put("weightKg", m.weightKg)
            obj.put("backCm", m.backCm)
            obj.put("shouldersCm", m.shouldersCm)
            obj.put("armsCm", m.armsCm)
            obj.put("hipsCm", m.hipsCm)
            obj.put("aiObservation", m.aiObservation)
            obj.put("trainerObservation", m.trainerObservation)
            measArray.put(obj)
        }
        root.put("collection_measurements", measArray)

        val jsonStr = root.toString(2)

        return BackupSummary(
            timestamp = now,
            formattedDate = dateStr,
            totalClients = clients.size,
            totalRoutines = routines.size,
            totalAttendances = attendances.size,
            totalPayments = payments.size,
            totalMeasurements = measurements.size,
            jsonPayload = jsonStr
        )
    }

    fun shareBackupJson(context: Context, jsonPayload: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TITLE, "Respaldo Firebase Actitud Fuerte")
            putExtra(Intent.EXTRA_TEXT, jsonPayload)
            type = "application/json"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Exportar Respaldo Firebase"))
    }
}
