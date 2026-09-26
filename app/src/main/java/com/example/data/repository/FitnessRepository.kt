package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.local.ClientDao
import com.example.data.local.MeasurementDao
import com.example.data.local.PaymentDao
import com.example.data.local.RoutineDao
import com.example.data.local.SocialPostDao
import com.example.data.local.WorkoutLoadLogDao
import com.example.data.model.Attendance
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.data.model.Payment
import com.example.data.model.Routine
import com.example.data.model.RoutineExercise
import com.example.data.model.RoutineExerciseHelper
import com.example.data.model.DayRoutine
import com.example.data.model.SocialComment
import com.example.data.model.SocialPost
import com.example.data.model.SocialPostUtils
import com.example.data.model.WeeklyRoutineHelper
import com.example.data.model.WorkoutLoadLog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class FitnessRepository(
    private val clientDao: ClientDao,
    private val measurementDao: MeasurementDao,
    private val attendanceDao: AttendanceDao,
    private val paymentDao: PaymentDao,
    private val routineDao: RoutineDao,
    private val socialPostDao: SocialPostDao,
    private val workoutLoadLogDao: WorkoutLoadLogDao
) {
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    val totalClientsCount: Flow<Int> = clientDao.getTotalClientsCount()
    val activeClientsCount: Flow<Int> = clientDao.getActiveClientsCount()
    val allAttendances: Flow<List<Attendance>> = attendanceDao.getAllAttendances()
    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()
    val allMeasurements: Flow<List<Measurement>> = measurementDao.getAllMeasurements()
    val totalIncome: Flow<Double?> = paymentDao.getTotalIncome()
    val allRoutines: Flow<List<Routine>> = routineDao.getAllRoutines()
    val allSocialPosts: Flow<List<SocialPost>> = socialPostDao.getAllPosts()
    val allWorkoutLogs: Flow<List<WorkoutLoadLog>> = workoutLoadLogDao.getAllLogs()

    fun getWorkoutLogsForClient(clientId: Long): Flow<List<WorkoutLoadLog>> =
        workoutLoadLogDao.getLogsForClient(clientId)

    fun getWorkoutLogsForExercise(clientId: Long, exerciseName: String): Flow<List<WorkoutLoadLog>> =
        workoutLoadLogDao.getLogsForExercise(clientId, exerciseName)

    suspend fun getLatestLogForExercise(clientId: Long, exerciseName: String): WorkoutLoadLog? =
        workoutLoadLogDao.getLatestLogForExercise(clientId, exerciseName)

    suspend fun saveWorkoutLoadLog(log: WorkoutLoadLog): Long {
        return if (log.id == 0L) {
            workoutLoadLogDao.insertLog(log)
        } else {
            workoutLoadLogDao.updateLog(log)
            log.id
        }
    }

    suspend fun deleteWorkoutLoadLog(log: WorkoutLoadLog) {
        workoutLoadLogDao.deleteLog(log)
    }

    suspend fun getAllWorkoutLogsOnce(): List<WorkoutLoadLog> =
        workoutLoadLogDao.getAllLogsOnce()

    fun getSocialPostsForAuthor(authorId: Long): Flow<List<SocialPost>> =
        socialPostDao.getPostsForAuthor(authorId)

    suspend fun getSocialPostsForAuthorOnce(authorId: Long): List<SocialPost> =
        socialPostDao.getPostsForAuthorOnce(authorId)

    suspend fun getAllSocialPostsOnce(): List<SocialPost> =
        socialPostDao.getAllPostsOnce()

    suspend fun saveSocialPost(post: SocialPost): Long {
        return if (post.id == 0L) {
            socialPostDao.insertPost(post)
        } else {
            socialPostDao.updatePost(post)
            post.id
        }
    }

    suspend fun deleteSocialPost(post: SocialPost) {
        socialPostDao.deletePost(post)
    }

    suspend fun toggleLikePost(postId: Long): Boolean {
        val post = socialPostDao.getPostById(postId) ?: return false
        val newLiked = !post.isLikedByMe
        val newCount = if (newLiked) post.likesCount + 1 else (post.likesCount - 1).coerceAtLeast(0)
        socialPostDao.updateLike(postId, newCount, newLiked)
        return newLiked
    }

    suspend fun addCommentToPost(
        postId: Long,
        authorId: Long,
        authorName: String,
        authorAvatarUrl: String,
        text: String,
        replyToCommentId: String = "",
        replyToAuthorName: String = ""
    ): SocialPost? {
        val post = socialPostDao.getPostById(postId) ?: return null
        val comments = SocialPostUtils.parseComments(post.commentsJson).toMutableList()
        val newComment = SocialComment(
            id = java.util.UUID.randomUUID().toString(),
            authorId = authorId,
            authorName = authorName,
            authorAvatarUrl = authorAvatarUrl,
            text = text.trim(),
            timestamp = System.currentTimeMillis(),
            replyToCommentId = replyToCommentId,
            replyToAuthorName = replyToAuthorName
        )
        comments.add(newComment)
        val updated = post.copy(
            commentsCount = comments.size,
            commentsJson = SocialPostUtils.serializeComments(comments)
        )
        socialPostDao.updatePost(updated)
        return updated
    }

    suspend fun toggleLikeComment(
        postId: Long,
        commentId: String,
        clientId: Long
    ): SocialPost? {
        val post = socialPostDao.getPostById(postId) ?: return null
        val comments = SocialPostUtils.parseComments(post.commentsJson).toMutableList()
        val index = comments.indexOfFirst { it.id == commentId }
        if (index == -1) return null

        val comment = comments[index]
        val currentLikes = comment.likedByClientIds.toMutableList()
        val alreadyLiked = currentLikes.contains(clientId)

        if (alreadyLiked) {
            currentLikes.remove(clientId)
        } else {
            currentLikes.add(clientId)
        }

        val updatedComment = comment.copy(
            likesCount = currentLikes.size,
            likedByClientIds = currentLikes
        )
        comments[index] = updatedComment

        val updatedPost = post.copy(
            commentsJson = SocialPostUtils.serializeComments(comments)
        )
        socialPostDao.updatePost(updatedPost)
        return updatedPost
    }

    suspend fun getAllClientsOnce(): List<Client> = clientDao.getAllClientsOnce()
    suspend fun getAllRoutinesOnce(): List<Routine> = routineDao.getAllRoutinesOnce()
    suspend fun getAllAttendancesOnce(): List<Attendance> = attendanceDao.getAllAttendancesOnce()
    suspend fun getAllPaymentsOnce(): List<Payment> = paymentDao.getAllPaymentsOnce()
    suspend fun getAllMeasurementsOnce(): List<Measurement> = measurementDao.getAllMeasurementsOnce()

    fun getClient(id: Long): Flow<Client?> = clientDao.getClientById(id)
    suspend fun getClientOnce(id: Long): Client? = clientDao.getClientByIdOnce(id)

    suspend fun saveRoutine(routine: Routine): Long {
        return if (routine.id == 0L) {
            routineDao.insertRoutine(routine)
        } else {
            routineDao.updateRoutine(routine)
            routine.id
        }
    }

    suspend fun deleteRoutine(routine: Routine) {
        routineDao.deleteRoutine(routine)
    }

    suspend fun getRandomRoutineFor(targetType: String, clientName: String? = null): Routine? {
        val routines = routineDao.getAllRoutinesOnce()
        if (routines.isEmpty()) return null

        val filtered = routines.filter { r ->
            when {
                r.targetType.equals(targetType, ignoreCase = true) -> true
                r.targetType.equals("Usuarios específicos", ignoreCase = true) && !clientName.isNullOrBlank() -> {
                    r.targetClientNames.contains(clientName, ignoreCase = true)
                }
                else -> false
            }
        }

        return if (filtered.isNotEmpty()) {
            filtered.random()
        } else {
            // Si no hay filtro exacto, retornar una rutina aleatoria del catálogo general
            routines.random()
        }
    }

    fun getMeasurementsForClient(clientId: Long): Flow<List<Measurement>> =
        measurementDao.getMeasurementsForClient(clientId)

    suspend fun getLatestMeasurementForClient(clientId: Long): Measurement? =
        measurementDao.getLatestMeasurementForClient(clientId)

    suspend fun getLastTwoMeasurementsForClient(clientId: Long): List<Measurement> =
        measurementDao.getLastTwoMeasurementsForClient(clientId)

    fun getAttendancesForClient(clientId: Long): Flow<List<Attendance>> =
        attendanceDao.getAttendancesForClient(clientId)

    fun getTodayAttendances(dateStr: String): Flow<List<Attendance>> =
        attendanceDao.getAttendancesByDate(dateStr)

    fun getTodayCheckInCount(dateStr: String): Flow<Int> =
        attendanceDao.getTodayCheckInCount(dateStr)

    fun getPaymentsForClient(clientId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForClient(clientId)

    suspend fun saveClient(client: Client): Long {
        val clientWithId = if (client.accessId.isBlank()) {
            val randomNum = Random.nextInt(1000, 9999)
            client.copy(accessId = "AF-$randomNum")
        } else client

        val id = if (clientWithId.id == 0L) {
            clientDao.insertClient(clientWithId)
        } else {
            clientDao.updateClient(clientWithId)
            clientWithId.id
        }

        val photoToSync = clientWithId.avatarUrl
        if (photoToSync.isNotBlank()) {
            socialPostDao.updateAuthorAvatar(id, clientWithId.accessId, photoToSync)
        }

        return id
    }

    suspend fun updateOperationalStatus(clientId: Long, status: String) {
        clientDao.updateOperationalStatus(clientId, status)
    }

    suspend fun deleteClient(client: Client) {
        clientDao.deleteClient(client)
    }

    suspend fun recordCheckIn(client: Client): Long {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val now = Date()

        val attendance = Attendance(
            clientId = client.id,
            clientName = client.fullName,
            timestamp = now.time,
            dateOnlyString = dateFormat.format(now),
            timeOnlyString = timeFormat.format(now)
        )
        return attendanceDao.insertAttendance(attendance)
    }

    suspend fun recordPayment(payment: Payment): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun recordMeasurement(measurement: Measurement): Long {
        return measurementDao.insertMeasurement(measurement)
    }

    // Métodos dedicados para recibir y asimilar datos desde Cloud Firestore en tiempo real
    suspend fun saveClientFromRemote(client: Client) {
        clientDao.insertClient(client)
    }

    suspend fun deleteClientByIdFromRemote(id: Long) {
        clientDao.deleteClientById(id)
    }

    suspend fun saveRoutineFromRemote(routine: Routine) {
        routineDao.insertRoutine(routine)
    }

    suspend fun deleteRoutineByIdFromRemote(id: Long) {
        routineDao.deleteRoutineById(id)
    }

    suspend fun saveAttendanceFromRemote(attendance: Attendance) {
        attendanceDao.insertAttendance(attendance)
    }

    suspend fun savePaymentFromRemote(payment: Payment) {
        paymentDao.insertPayment(payment)
    }

    suspend fun saveMeasurementFromRemote(measurement: Measurement) {
        measurementDao.insertMeasurement(measurement)
    }

    suspend fun saveSocialPostFromRemote(post: SocialPost) {
        val current = socialPostDao.getPostById(post.id)
        val postToSave = if (current != null) {
            post.copy(isLikedByMe = current.isLikedByMe)
        } else {
            post
        }
        socialPostDao.insertPost(postToSave)
    }

    suspend fun deleteSocialPostByIdFromRemote(id: Long) {
        socialPostDao.deletePostById(id)
    }

    suspend fun getSocialPostOnce(id: Long): SocialPost? = socialPostDao.getPostById(id)

    /**
     * Poblado inicial y limpieza de datos exigida: Solo alexgcuicas@gmail.com y carlenchirinos.cc@gmail.com
     */
    suspend fun prepopulateIfNeeded() {
        val existingClients = clientDao.getAllClientsOnce()

        val hasAlex = existingClients.any { it.email.equals("alexgcuicas@gmail.com", true) }
        val hasCarlen = existingClients.any { it.email.equals("carlenchirinos.cc@gmail.com", true) }

        // Asegurar que el nombre oficial en el registro de Alex sea "ALEX GÓMEZ ! CEO ACTITUD FUERTE" y su biografía oficial
        val alexOfficialName = "ALEX GÓMEZ ! CEO ACTITUD FUERTE"
        val alexOfficialBio = "Fundador & CEO de Actitud Fuerte. Forjando atletas con disciplina inquebrantable, ciencia del entrenamiento y pasión por el alto rendimiento muscular."
        val existingAlex = existingClients.firstOrNull { it.email.equals("alexgcuicas@gmail.com", true) }
        if (existingAlex != null) {
            val shouldUpdate = existingAlex.fullName != alexOfficialName || existingAlex.bio != alexOfficialBio || existingAlex.gender != "Hombre"
            if (shouldUpdate) {
                clientDao.updateClient(
                    existingAlex.copy(
                        fullName = alexOfficialName,
                        bio = alexOfficialBio,
                        gender = "Hombre"
                    )
                )
            }
        }

        val existingCarlen = existingClients.firstOrNull { it.email.equals("carlenchirinos.cc@gmail.com", true) }
        if (existingCarlen != null && existingCarlen.gender != "Mujer") {
            clientDao.updateClient(existingCarlen.copy(gender = "Mujer"))
        }

        if (!hasAlex) {
            val aId = clientDao.insertClient(
                Client(
                    fullName = alexOfficialName,
                    phone = "+584145529674",
                    email = "alexgcuicas@gmail.com",
                    membershipPlan = "Membresía 20$",
                    paymentFrequency = "Mensual",
                    emergencyContact = "Familiar Alex (+584141112233)",
                    gender = "Hombre",
                    mainObjective = "Hipertrofia y ganancia muscular",
                    medicalCondition = "Ninguna de las anteriores",
                    operationalStatus = "Activo",
                    accessId = "AF-8521",
                    bio = alexOfficialBio
                )
            )
            // Initial measurement & payment & attendance for Alex
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            measurementDao.insertMeasurement(
                Measurement(
                    clientId = aId,
                    timestamp = now - 15 * dayMs,
                    weightKg = 77.0,
                    backCm = 102.0,
                    shouldersCm = 116.0,
                    armsCm = 36.0,
                    hipsCm = 95.0,
                    aiObservation = "Punto de partida óptimo para desarrollo de masa muscular limpia en Actitud Fuerte.",
                    trainerObservation = "Enfoque en técnica y sobrecarga progresiva.",
                    sentViaEmail = true,
                    sentViaWhatsapp = true
                )
            )
            paymentDao.insertPayment(
                Payment(
                    clientId = aId,
                    clientName = "Alex Gómez",
                    timestamp = now - 5 * dayMs,
                    amount = 20.0,
                    planName = "Membresía 20$",
                    frequency = "Mensual",
                    method = "Pago Móvil",
                    reference = "REF-1234"
                )
            )
        }

        if (!hasCarlen) {
            val cId = clientDao.insertClient(
                Client(
                    fullName = "Carlen Chirinos",
                    phone = "+584121234567",
                    email = "carlenchirinos.cc@gmail.com",
                    membershipPlan = "Membresía 12$",
                    paymentFrequency = "Mensual",
                    emergencyContact = "Familiar Carlen (+584129998877)",
                    gender = "Mujer",
                    mainObjective = "Pérdida de grasa y definición",
                    medicalCondition = "Ninguna de las anteriores",
                    operationalStatus = "Activo",
                    accessId = "AF-9432"
                )
            )
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            measurementDao.insertMeasurement(
                Measurement(
                    clientId = cId,
                    timestamp = now - 15 * dayMs,
                    weightKg = 62.0,
                    backCm = 90.0,
                    shouldersCm = 102.0,
                    armsCm = 29.0,
                    hipsCm = 94.0,
                    aiObservation = "Evaluación inicial favorable para tonificación y recomposición corporal.",
                    trainerObservation = "Trabajo cardiovascular y fuerza resistencia.",
                    sentViaEmail = true,
                    sentViaWhatsapp = true
                )
            )
            paymentDao.insertPayment(
                Payment(
                    clientId = cId,
                    clientName = "Carlen Chirinos",
                    timestamp = now - 2 * dayMs,
                    amount = 12.0,
                    planName = "Membresía 12$",
                    frequency = "Mensual",
                    method = "Pago Móvil",
                    reference = "REF-5678"
                )
            )
        }

        // Poblado inicial de la Biblioteca Maestra de Rutinas con Semanas Completas (Lunes a Sábado)
        val existingRoutines = routineDao.getAllRoutinesOnce()
        // Si no hay rutinas o si las existentes tienen el formato legacy de 1 solo día, inicializar con semana completa
        val needsWeeklyRefresh = existingRoutines.isEmpty() || existingRoutines.all { !it.exercisesJson.contains("\"dayName\"") }
        if (needsWeeklyRefresh) {
            if (existingRoutines.isNotEmpty()) {
                existingRoutines.forEach { routineDao.deleteRoutine(it) }
            }

            // 1. RUTINA 1 HOMBRES (Semana Completa Lunes a Sábado)
            val rutina1HombresSemana = listOf(
                DayRoutine(
                    dayName = "Lunes",
                    muscleFocus = "Pecho y Tríceps (Empuje)",
                    exercises = listOf(
                        RoutineExercise("Press de Banca Plano con Barra", "4", "10-12", "https://www.youtube.com/watch?v=rT7DgCr-3pg"),
                        RoutineExercise("Press Inclinado con Mancuernas", "4", "10", "https://www.youtube.com/watch?v=8iPEnn-ltC8"),
                        RoutineExercise("Aperturas con Mancuernas en Banco Plano", "3", "12", "https://www.youtube.com/watch?v=eozdVDA78K0"),
                        RoutineExercise("Fondos en Paralelas para Pecho/Tríceps", "3", "10", "https://www.youtube.com/watch?v=2z8JmcrW-As"),
                        RoutineExercise("Extensiones de Tríceps en Polea Alta", "4", "12-15", "https://www.youtube.com/watch?v=2-LAMcpzODU")
                    )
                ),
                DayRoutine(
                    dayName = "Martes",
                    muscleFocus = "Espalda y Bíceps (Tracción)",
                    exercises = listOf(
                        RoutineExercise("Remo con Barra Pendlay", "4", "10", "https://www.youtube.com/watch?v=FWJR5Ve8gkQ"),
                        RoutineExercise("Jalón al Pecho en Polea Alta", "4", "10-12", "https://www.youtube.com/watch?v=eGo4IYlbE5g"),
                        RoutineExercise("Remo Gironda sentado en polea", "3", "12", "https://www.youtube.com/watch?v=GZbfZ033f74"),
                        RoutineExercise("Curl de Bíceps con Barra Z", "4", "10-12", "https://www.youtube.com/watch?v=kwG2ipFRgfo"),
                        RoutineExercise("Curl Martillo con Mancuernas", "3", "12", "https://www.youtube.com/watch?v=zC3nLlEvin4")
                    )
                ),
                DayRoutine(
                    dayName = "Miércoles",
                    muscleFocus = "Pierna Completa (Fuerza Base)",
                    exercises = listOf(
                        RoutineExercise("Sentadilla Libre con Barra", "4", "10", "https://www.youtube.com/watch?v=aclHkVaku9U"),
                        RoutineExercise("Prensa de Piernas 45°", "4", "12", "https://www.youtube.com/watch?v=IZxyjW7MPJQ"),
                        RoutineExercise("Extensiones de Cuádriceps en Máquina", "3", "15", "https://www.youtube.com/watch?v=YyvSfVjQeL0"),
                        RoutineExercise("Curl Femoral Tumbado", "4", "12", "https://www.youtube.com/watch?v=1Tq3QdYUuHs"),
                        RoutineExercise("Elevaciones de Gemelos de Pie", "4", "15-20", "https://www.youtube.com/watch?v=-M4-G8p8fmc")
                    )
                ),
                DayRoutine(
                    dayName = "Jueves",
                    muscleFocus = "Hombros, Trapecio y Abdomen",
                    exercises = listOf(
                        RoutineExercise("Press Militar con Mancuernas Sentado", "4", "10", "https://www.youtube.com/watch?v=qEwKCR5JCog"),
                        RoutineExercise("Elevaciones Laterales con Mancuerna", "4", "12-15", "https://www.youtube.com/watch?v=3VcKaXpzqRo"),
                        RoutineExercise("Pájaros / Deltoides Posterior en Polea", "3", "15", "https://www.youtube.com/watch?v=H530fW3kW4E"),
                        RoutineExercise("Encogimientos con Barra para Trapecio", "4", "12", "https://www.youtube.com/watch?v=g6qbq481ER8"),
                        RoutineExercise("Crunch Abdominal en Polea y Plancha", "4", "20 reps / 45s", "https://www.youtube.com/watch?v=ASdvN_XEl_c")
                    )
                ),
                DayRoutine(
                    dayName = "Viernes",
                    muscleFocus = "Brazos & Enfoque Pectoral",
                    exercises = listOf(
                        RoutineExercise("Press de Banca Inclinado con Barra", "4", "10", "https://www.youtube.com/watch?v=SrqOu55lrYU"),
                        RoutineExercise("Cruce de Poleas para Pectoral", "3", "12-15", "https://www.youtube.com/watch?v=taI4XduLpTk"),
                        RoutineExercise("Press Francés con Barra Z en Banco", "4", "10", "https://www.youtube.com/watch?v=k_Sn3e_pG_U"),
                        RoutineExercise("Curl Concentrado con Mancuerna", "3", "12 c/u", "https://www.youtube.com/watch?v=0AUGkch3tzc"),
                        RoutineExercise("Fondos en Banco para Tríceps", "3", "15", "https://www.youtube.com/watch?v=0326dy_-CzM")
                    )
                ),
                DayRoutine(
                    dayName = "Sábado",
                    muscleFocus = "Cadena Posterior & Core Funcional",
                    exercises = listOf(
                        RoutineExercise("Peso Muerto Rumano con Mancuernas", "4", "10", "https://www.youtube.com/watch?v=JCXUYuzwNrM"),
                        RoutineExercise("Hiperextensiones Lumbares en Banco", "3", "15", "https://www.youtube.com/watch?v=ph3pddpKzzw"),
                        RoutineExercise("Dominadas o Jalón Supino", "4", "8-10", "https://www.youtube.com/watch?v=eGo4IYlbE5g"),
                        RoutineExercise("Plancha Isométrica y Rueda Abdominal", "4", "1 min / 12 reps", "https://www.youtube.com/watch?v=ASdvN_XEl_c")
                    )
                )
            )

            routineDao.insertRoutine(
                Routine(
                    name = "Rutina de entrenamiento 1",
                    targetType = "Hombres",
                    specialConditions = "Fuerza y Desarrollo Muscular Progresivo",
                    exercisesJson = WeeklyRoutineHelper.weeklyScheduleToJson(rutina1HombresSemana)
                )
            )

            // 2. RUTINA 2 HOMBRES (Semana Completa Lunes a Sábado)
            val rutina2HombresSemana = listOf(
                DayRoutine(
                    dayName = "Lunes",
                    muscleFocus = "Torso Superior (Potencia)",
                    exercises = listOf(
                        RoutineExercise("Press de Banca Plano con Mancuernas", "4", "10", "https://www.youtube.com/watch?v=8iPEnn-ltC8"),
                        RoutineExercise("Dominadas con Agarre Neutro o Prono", "4", "8-10", "https://www.youtube.com/watch?v=eGo4IYlbE5g"),
                        RoutineExercise("Press Militar de Pie con Barra", "3", "10", "https://www.youtube.com/watch?v=qEwKCR5JCog"),
                        RoutineExercise("Remo con Mancuerna a una Mano", "4", "10 c/u", "https://www.youtube.com/watch?v=roCP6wCXPqo")
                    )
                ),
                DayRoutine(
                    dayName = "Martes",
                    muscleFocus = "Piernas y Glúteos (Hipertrofia)",
                    exercises = listOf(
                        RoutineExercise("Sentadilla Frontal o Goblet con Mancuerna", "4", "10", "https://www.youtube.com/watch?v=aclHkVaku9U"),
                        RoutineExercise("Zancadas Caminando con Mancuernas", "3", "12 pasos", "https://www.youtube.com/watch?v=QOVaHwm-Q6U"),
                        RoutineExercise("Peso Muerto Rumano con Barra", "4", "10", "https://www.youtube.com/watch?v=JCXUYuzwNrM"),
                        RoutineExercise("Gemelos en Máquina Costurera Sentado", "4", "15", "https://www.youtube.com/watch?v=-M4-G8p8fmc")
                    )
                ),
                DayRoutine(
                    dayName = "Miércoles",
                    muscleFocus = "Espalda Alta, Romboides y Bíceps",
                    exercises = listOf(
                        RoutineExercise("Remo en Barra T o con Soporte", "4", "10", "https://www.youtube.com/watch?v=FWJR5Ve8gkQ"),
                        RoutineExercise("Pullover en Polea Alta con Cuerda", "3", "15", "https://www.youtube.com/watch?v=eGo4IYlbE5g"),
                        RoutineExercise("Curl de Bíceps en Banco Scott / Predicador", "4", "10", "https://www.youtube.com/watch?v=kwG2ipFRgfo"),
                        RoutineExercise("Curl Martillo en Polea Baja", "3", "12", "https://www.youtube.com/watch?v=zC3nLlEvin4")
                    )
                ),
                DayRoutine(
                    dayName = "Jueves",
                    muscleFocus = "Pectoral & Tríceps (Volumen)",
                    exercises = listOf(
                        RoutineExercise("Press Inclinado con Barra", "4", "10", "https://www.youtube.com/watch?v=SrqOu55lrYU"),
                        RoutineExercise("Fondos en Paralelas Lastrados o Asistidos", "4", "10-12", "https://www.youtube.com/watch?v=2z8JmcrW-As"),
                        RoutineExercise("Extensiones de Tríceps tras Nuca con Cuerda", "4", "12", "https://www.youtube.com/watch?v=2-LAMcpzODU"),
                        RoutineExercise("Aperturas en Poleas desde Abajo", "3", "15", "https://www.youtube.com/watch?v=taI4XduLpTk")
                    )
                ),
                DayRoutine(
                    dayName = "Viernes",
                    muscleFocus = "Hombros y Brazos Super-Set",
                    exercises = listOf(
                        RoutineExercise("Press Arnold con Mancuernas", "4", "10", "https://www.youtube.com/watch?v=qEwKCR5JCog"),
                        RoutineExercise("Elevaciones Laterales Unilaterales en Polea", "4", "12 c/u", "https://www.youtube.com/watch?v=3VcKaXpzqRo"),
                        RoutineExercise("Face Pulls con Cuerda para Deltoides", "4", "15", "https://www.youtube.com/watch?v=H530fW3kW4E"),
                        RoutineExercise("Super-Set: Curl con Barra + Extensiones Tríceps", "4", "10 + 12", "https://www.youtube.com/watch?v=kwG2ipFRgfo")
                    )
                ),
                DayRoutine(
                    dayName = "Sábado",
                    muscleFocus = "Pierna Enfoque Isquios y Core",
                    exercises = listOf(
                        RoutineExercise("Prensa de Piernas Inclinada pies altos", "4", "12", "https://www.youtube.com/watch?v=IZxyjW7MPJQ"),
                        RoutineExercise("Curl Femoral Sentado en Máquina", "4", "12", "https://www.youtube.com/watch?v=1Tq3QdYUuHs"),
                        RoutineExercise("Elevaciones de Piernas Colgado en Barra", "4", "15", "https://www.youtube.com/watch?v=ASdvN_XEl_c"),
                        RoutineExercise("Rueda Abdominal o Ab Rollout", "3", "12", "https://www.youtube.com/watch?v=ASdvN_XEl_c")
                    )
                )
            )

            routineDao.insertRoutine(
                Routine(
                    name = "Rutina de entrenamiento 2",
                    targetType = "Hombres",
                    specialConditions = "Hipertrofia y Empuje/Tracción Avanzada",
                    exercisesJson = WeeklyRoutineHelper.weeklyScheduleToJson(rutina2HombresSemana)
                )
            )

            // 3. RUTINA 1 MUJERES (Semana Completa Lunes a Sábado)
            val rutina1MujeresSemana = listOf(
                DayRoutine(
                    dayName = "Lunes",
                    muscleFocus = "Glúteo y Cadena Posterior (Foco Máximo)",
                    exercises = listOf(
                        RoutineExercise("Hip Thrust con Barra en Banco", "4", "10-12", "https://www.youtube.com/watch?v=SEdqd1n012g"),
                        RoutineExercise("Peso Muerto Rumano con Mancuernas", "4", "10-12", "https://www.youtube.com/watch?v=f54yE2V5i_0"),
                        RoutineExercise("Sentadilla Búlgara con Mancuernas", "3", "10 c/u", "https://www.youtube.com/watch?v=2C-uNgKwPLE"),
                        RoutineExercise("Abductores en Máquina (Tronco Inclinado)", "4", "15-20", "https://www.youtube.com/watch?v=Gk6l3L3Vw30")
                    )
                ),
                DayRoutine(
                    dayName = "Martes",
                    muscleFocus = "Espalda Estilizada y Hombros",
                    exercises = listOf(
                        RoutineExercise("Jalón al Pecho en Polea Alta Agarre Neutro", "4", "12", "https://www.youtube.com/watch?v=CAwf7n6Luuc"),
                        RoutineExercise("Remo Gironda en Polea Baja", "3", "12", "https://www.youtube.com/watch?v=GZbfZ033f74"),
                        RoutineExercise("Press de Hombros Sentada con Mancuernas", "3", "12", "https://www.youtube.com/watch?v=B-aVuyhvLHU"),
                        RoutineExercise("Elevaciones Laterales Controladas", "3", "15", "https://www.youtube.com/watch?v=3VcKaXpzqRo"),
                        RoutineExercise("Face Pulls en Polea Alta", "3", "15", "https://www.youtube.com/watch?v=H530fW3kW4E")
                    )
                ),
                DayRoutine(
                    dayName = "Miércoles",
                    muscleFocus = "Cuádriceps y Aductores",
                    exercises = listOf(
                        RoutineExercise("Prensa de Piernas 45° con Pies Separados", "4", "12", "https://www.youtube.com/watch?v=IZxyjW7MPJQ"),
                        RoutineExercise("Sentadilla Goblet Profunda con Mancuerna", "4", "12", "https://www.youtube.com/watch?v=aclHkVaku9U"),
                        RoutineExercise("Extensiones de Cuádriceps", "3", "15", "https://www.youtube.com/watch?v=YyvSfVjQeL0"),
                        RoutineExercise("Aductores en Máquina", "4", "15", "https://www.youtube.com/watch?v=Gk6l3L3Vw30"),
                        RoutineExercise("Gemelos de Pie en Escalón", "3", "20", "https://www.youtube.com/watch?v=-M4-G8p8fmc")
                    )
                ),
                DayRoutine(
                    dayName = "Jueves",
                    muscleFocus = "Glúteo Aislamiento y Core Plano",
                    exercises = listOf(
                        RoutineExercise("Puente de Glúteo en Suelo con Mancuerna", "4", "15", "https://www.youtube.com/watch?v=OUgsJ8-Vigk"),
                        RoutineExercise("Patada de Glúteo en Polea Baja", "4", "12 c/u", "https://www.youtube.com/watch?v=SEdqd1n012g"),
                        RoutineExercise("Paso Lateral con Banda Elástica", "3", "20 pasos", "https://www.youtube.com/watch?v=Gk6l3L3Vw30"),
                        RoutineExercise("Plancha Abdominal Isométrica", "4", "45 seg", "https://www.youtube.com/watch?v=ASdvN_XEl_c"),
                        RoutineExercise("Vacío Abdominal / Vacuum y Elevación Pelvis", "3", "15", "https://www.youtube.com/watch?v=ASdvN_XEl_c")
                    )
                ),
                DayRoutine(
                    dayName = "Viernes",
                    muscleFocus = "Brazos Tonificados y Pectoral",
                    exercises = listOf(
                        RoutineExercise("Flexiones Asistidas o en Banco", "3", "10-12", "https://www.youtube.com/watch?v=rT7DgCr-3pg"),
                        RoutineExercise("Fondos en Banco para Tríceps", "3", "12", "https://www.youtube.com/watch?v=0326dy_-CzM"),
                        RoutineExercise("Extensiones de Tríceps con Cuerda", "3", "15", "https://www.youtube.com/watch?v=2-LAMcpzODU"),
                        RoutineExercise("Curl de Bíceps con Mancuernas Alterno", "3", "12", "https://www.youtube.com/watch?v=kwG2ipFRgfo")
                    )
                ),
                DayRoutine(
                    dayName = "Sábado",
                    muscleFocus = "Full Body & Tonificación Activa",
                    exercises = listOf(
                        RoutineExercise("Hip Thrust Unilateral con Peso Corporal", "3", "12 c/u", "https://www.youtube.com/watch?v=SEdqd1n012g"),
                        RoutineExercise("Peso Muerto Sumo con Mancuerna", "4", "12", "https://www.youtube.com/watch?v=f54yE2V5i_0"),
                        RoutineExercise("Remo con Mancuerna a 1 Brazo", "3", "12 c/u", "https://www.youtube.com/watch?v=roCP6wCXPqo"),
                        RoutineExercise("Cardio HIIT en Bicicleta o Caminadora", "1", "20 min", "https://www.youtube.com/watch?v=dJlFm4wvTXw")
                    )
                )
            )

            routineDao.insertRoutine(
                Routine(
                    name = "Rutina de entrenamiento 1",
                    targetType = "Mujeres",
                    specialConditions = "Tonificación Glúteo, Piernas y Cintura",
                    exercisesJson = WeeklyRoutineHelper.weeklyScheduleToJson(rutina1MujeresSemana)
                )
            )

            // 4. RUTINA 2 MUJERES (Semana Completa Lunes a Sábado)
            val rutina2MujeresSemana = listOf(
                DayRoutine(
                    dayName = "Lunes",
                    muscleFocus = "Glúteo & Isquiotibiales",
                    exercises = listOf(
                        RoutineExercise("Hip Thrust con Pausa Isométrica 2s", "4", "10", "https://www.youtube.com/watch?v=SEdqd1n012g"),
                        RoutineExercise("Peso Muerto Rumano con Barra", "4", "12", "https://www.youtube.com/watch?v=f54yE2V5i_0"),
                        RoutineExercise("Curl Femoral Tumbado en Máquina", "3", "15", "https://www.youtube.com/watch?v=1Tq3QdYUuHs"),
                        RoutineExercise("Abductores en Polea de Pie", "3", "15 c/u", "https://www.youtube.com/watch?v=Gk6l3L3Vw30")
                    )
                ),
                DayRoutine(
                    dayName = "Martes",
                    muscleFocus = "Torso Superior, Hombro y Espalda",
                    exercises = listOf(
                        RoutineExercise("Remo con Mancuernas en Banco Inclinado", "4", "12", "https://www.youtube.com/watch?v=roCP6wCXPqo"),
                        RoutineExercise("Press Militar con Mancuernas", "3", "12", "https://www.youtube.com/watch?v=B-aVuyhvLHU"),
                        RoutineExercise("Jalón al Pecho en Polea", "4", "10", "https://www.youtube.com/watch?v=CAwf7n6Luuc"),
                        RoutineExercise("Elevaciones Frontales y Laterales Combinadas", "3", "12", "https://www.youtube.com/watch?v=3VcKaXpzqRo")
                    )
                ),
                DayRoutine(
                    dayName = "Miércoles",
                    muscleFocus = "Cuádriceps & Glúteo Medio",
                    exercises = listOf(
                        RoutineExercise("Sentadilla Búlgara en Multipower / Mancuernas", "3", "10 c/u", "https://www.youtube.com/watch?v=2C-uNgKwPLE"),
                        RoutineExercise("Prensa Inclinada Pies Medios", "4", "12", "https://www.youtube.com/watch?v=IZxyjW7MPJQ"),
                        RoutineExercise("Step Up en Banco con Mancuernas", "3", "12 c/u", "https://www.youtube.com/watch?v=aclHkVaku9U"),
                        RoutineExercise("Extensiones de Cuádriceps en Máquina", "3", "15", "https://www.youtube.com/watch?v=YyvSfVjQeL0")
                    )
                ),
                DayRoutine(
                    dayName = "Jueves",
                    muscleFocus = "Tríceps, Bíceps y Core Definido",
                    exercises = listOf(
                        RoutineExercise("Extensiones de Tríceps en Polea Alta", "3", "12", "https://www.youtube.com/watch?v=2-LAMcpzODU"),
                        RoutineExercise("Curl Martillo con Mancuernas", "3", "12", "https://www.youtube.com/watch?v=zC3nLlEvin4"),
                        RoutineExercise("Plancha Lateral con Elevación de Pierna", "3", "30 seg c/u", "https://www.youtube.com/watch?v=ASdvN_XEl_c"),
                        RoutineExercise("Elevación de Rodillas en Paralelas / Silla Romana", "4", "15", "https://www.youtube.com/watch?v=ASdvN_XEl_c")
                    )
                ),
                DayRoutine(
                    dayName = "Viernes",
                    muscleFocus = "Hipertrofia Glúteo y Espalda",
                    exercises = listOf(
                        RoutineExercise("Hip Thrust en Máquina o Barra Libre", "4", "12", "https://www.youtube.com/watch?v=SEdqd1n012g"),
                        RoutineExercise("Sentadilla Sumo con Mancuerna Pesada", "4", "12", "https://www.youtube.com/watch?v=aclHkVaku9U"),
                        RoutineExercise("Remo Gironda Agarre Estrecho", "3", "12", "https://www.youtube.com/watch?v=GZbfZ033f74"),
                        RoutineExercise("Pull-over en Polea", "3", "15", "https://www.youtube.com/watch?v=eGo4IYlbE5g")
                    )
                ),
                DayRoutine(
                    dayName = "Sábado",
                    muscleFocus = "Circuito Metabólico & Quema Grasa",
                    exercises = listOf(
                        RoutineExercise("Kettlebell Swing o Balanceo con Mancuerna", "4", "15", "https://www.youtube.com/watch?v=dJlFm4wvTXw"),
                        RoutineExercise("Zancadas Alternas sin Peso", "3", "20 pasos", "https://www.youtube.com/watch?v=QOVaHwm-Q6U"),
                        RoutineExercise("Crunches Abdominales y Planchas", "4", "20 / 45s", "https://www.youtube.com/watch?v=ASdvN_XEl_c"),
                        RoutineExercise("Trote Suave o Caminata Inclinada", "1", "25 min", "https://www.youtube.com/watch?v=dJlFm4wvTXw")
                    )
                )
            )

            routineDao.insertRoutine(
                Routine(
                    name = "Rutina de entrenamiento 2",
                    targetType = "Mujeres",
                    specialConditions = "Tonificación Glúteo & Acondicionamiento Semanal",
                    exercisesJson = WeeklyRoutineHelper.weeklyScheduleToJson(rutina2MujeresSemana)
                )
            )

            // 5. RUTINA 3 USUARIOS ESPECÍFICOS (Alex Gómez, Carlen Chirinos - Semana Completa Lunes a Sábado)
            val rutina3EspecificosSemana = listOf(
                DayRoutine(
                    dayName = "Lunes",
                    muscleFocus = "Readaptación Funcional & Core Articular",
                    exercises = listOf(
                        RoutineExercise("Activación Articular y Movilidad Dinámica", "3", "10 min", "https://www.youtube.com/watch?v=dJlFm4wvTXw"),
                        RoutineExercise("Puente de Glúteo en Suelo Controlado", "4", "15", "https://www.youtube.com/watch?v=OUgsJ8-Vigk"),
                        RoutineExercise("Remo con Mancuerna a 1 Brazo en Banco", "3", "12 c/u", "https://www.youtube.com/watch?v=roCP6wCXPqo"),
                        RoutineExercise("Plancha Abdominal sobre Rodillas", "3", "30 seg", "https://www.youtube.com/watch?v=ASdvN_XEl_c")
                    )
                ),
                DayRoutine(
                    dayName = "Martes",
                    muscleFocus = "Fuerza Básica Sin Impacto",
                    exercises = listOf(
                        RoutineExercise("Sentadilla a la Silla / Box Squat", "3", "12", "https://www.youtube.com/watch?v=aclHkVaku9U"),
                        RoutineExercise("Press de Pecho en Suelo con Mancuernas (Floor Press)", "3", "12", "https://www.youtube.com/watch?v=8iPEnn-ltC8"),
                        RoutineExercise("Jalón al Pecho Polea con Carga Moderada", "3", "12", "https://www.youtube.com/watch?v=CAwf7n6Luuc"),
                        RoutineExercise("Paseo del Granjero con Mancuernas Ligeras", "3", "40 seg", "https://www.youtube.com/watch?v=dJlFm4wvTXw")
                    )
                ),
                DayRoutine(
                    dayName = "Miércoles",
                    muscleFocus = "Estiramientos, Postura y Movilidad Lumbar",
                    exercises = listOf(
                        RoutineExercise("Gato-Camello y Movilidad Espinal", "3", "12", "https://www.youtube.com/watch?v=dJlFm4wvTXw"),
                        RoutineExercise("Bird-Dog (Pájaro-Perro) para Lumbar", "3", "10 c/u", "https://www.youtube.com/watch?v=ASdvN_XEl_c"),
                        RoutineExercise("Caminata Suave en Cinta Plana", "1", "20 min", "https://www.youtube.com/watch?v=dJlFm4wvTXw")
                    )
                ),
                DayRoutine(
                    dayName = "Jueves",
                    muscleFocus = "Fuerza de Miembros Inferiores Guiada",
                    exercises = listOf(
                        RoutineExercise("Prensa 45° a Rango Seguro", "3", "12", "https://www.youtube.com/watch?v=IZxyjW7MPJQ"),
                        RoutineExercise("Extensiones de Piernas Carga Progresiva", "3", "15", "https://www.youtube.com/watch?v=YyvSfVjQeL0"),
                        RoutineExercise("Curl Femoral Tumbado Suave", "3", "12", "https://www.youtube.com/watch?v=1Tq3QdYUuHs"),
                        RoutineExercise("Elevación de Talones en Suelo", "3", "15", "https://www.youtube.com/watch?v=-M4-G8p8fmc")
                    )
                ),
                DayRoutine(
                    dayName = "Viernes",
                    muscleFocus = "Tren Superior y Salud de Hombro",
                    exercises = listOf(
                        RoutineExercise("Rotaciones de Manguito Rotador con Banda", "3", "15 c/u", "https://www.youtube.com/watch?v=H530fW3kW4E"),
                        RoutineExercise("Press de Pecho en Máquina Guiada", "3", "12", "https://www.youtube.com/watch?v=rT7DgCr-3pg"),
                        RoutineExercise("Remo Sentado con Agarre Abierto", "3", "12", "https://www.youtube.com/watch?v=GZbfZ033f74"),
                        RoutineExercise("Curl de Bíceps con Apoyo de Espalda", "3", "12", "https://www.youtube.com/watch?v=kwG2ipFRgfo")
                    )
                ),
                DayRoutine(
                    dayName = "Sábado",
                    muscleFocus = "Regeneración Activa y Respiración Diafragmática",
                    exercises = listOf(
                        RoutineExercise("Circuito de Movilidad Articular Completa", "3", "15 min", "https://www.youtube.com/watch?v=dJlFm4wvTXw"),
                        RoutineExercise("Descompresión Lumbar en Barra", "3", "20 seg", "https://www.youtube.com/watch?v=ASdvN_XEl_c"),
                        RoutineExercise("Elongación de Pectorales y Cadena Posterior", "3", "30 seg c/u", "https://www.youtube.com/watch?v=dJlFm4wvTXw")
                    )
                )
            )

            routineDao.insertRoutine(
                Routine(
                    name = "Rutina de entrenamiento 3",
                    targetType = "Usuarios específicos",
                    targetClientNames = "Alex Gómez, Carlen Chirinos",
                    specialConditions = "Readaptación, Cuidado Articular y Estabilidad",
                    exercisesJson = WeeklyRoutineHelper.weeklyScheduleToJson(rutina3EspecificosSemana)
                )
            )
        }

        // Poblado inicial de publicaciones de muestra en la red social
        val existingPosts = socialPostDao.getAllPostsOnce()
        if (existingPosts.isEmpty()) {
            val clients = clientDao.getAllClientsOnce()
            val alex = clients.firstOrNull { it.email.equals("alexgcuicas@gmail.com", true) }
            val carlen = clients.firstOrNull { it.email.equals("carlenchirinos.cc@gmail.com", true) }

            if (alex != null) {
                socialPostDao.insertPost(
                    SocialPost(
                        authorId = alex.id,
                        authorName = alex.fullName,
                        authorAccessId = alex.accessId,
                        contentText = "¡Nuevo récord personal en sentadilla profunda! 140kg x 6 repeticiones. La disciplina y el enfoque no se negocian en Actitud Fuerte. 💪🔥",
                        mediaUrl = "beast_mode",
                        mediaType = "IMAGE",
                        aspectRatio = "3:4",
                        timestamp = System.currentTimeMillis() - 86400000L * 2,
                        likesCount = 14,
                        commentsCount = 3,
                        category = "PR_FUERZA",
                        workoutDetails = "Sentadillas 140kg • 4x6"
                    )
                )
                socialPostDao.insertPost(
                    SocialPost(
                        authorId = alex.id,
                        authorName = alex.fullName,
                        authorAccessId = alex.accessId,
                        contentText = "Completada la rutina de pecho y tríceps de hoy. Constancia cada semana, gracias al coach Alex Gómez por las correcciones técnicas. 🏋️‍♂️",
                        mediaUrl = "discipline",
                        mediaType = "IMAGE",
                        aspectRatio = "3:4",
                        timestamp = System.currentTimeMillis() - 86400000L * 5,
                        likesCount = 9,
                        commentsCount = 1,
                        category = "ENTRENAMIENTO",
                        workoutDetails = "Press Banca + Fondos"
                    )
                )
            }

            if (carlen != null) {
                socialPostDao.insertPost(
                    SocialPost(
                        authorId = carlen.id,
                        authorName = carlen.fullName,
                        authorAccessId = carlen.accessId,
                        contentText = "Día de pierna y glúteos liquidado con éxito. Cada repetición cuenta cuando hay una meta clara. ¡A seguir con todo! ⚡🦵",
                        mediaUrl = "leg_day",
                        mediaType = "IMAGE",
                        aspectRatio = "3:4",
                        timestamp = System.currentTimeMillis() - 86400000L * 1,
                        likesCount = 18,
                        commentsCount = 4,
                        category = "ENTRENAMIENTO",
                        workoutDetails = "Prensa 45° + Hip Thrust"
                    )
                )
            }
        }
    }
}
