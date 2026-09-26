package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Client
import com.example.data.model.DayRoutine
import com.example.data.model.Routine
import com.example.data.model.WeeklyRoutineHelper
import com.example.data.model.WorkoutLoadLog
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import java.util.Calendar

/**
 * Pantalla de Historial de Cargas y Sobrecarga Progresiva para atletas.
 * Permite registrar y comparar el peso movido por ejercicio según la rutina asignada.
 */
@Composable
fun WorkoutLoadHistoryScreen(
    client: Client,
    routines: List<Routine>,
    loadLogs: List<WorkoutLoadLog>,
    onSaveLoadLog: (WorkoutLoadLog) -> Unit,
    onDeleteLoadLog: (WorkoutLoadLog) -> Unit,
    onShareAsPost: ((exerciseName: String, weightKg: Double, reps: Int) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Determinar la rutina asignada al cliente
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
            name = if (isFemale) "Rutina de entrenamiento 2" else "Rutina de entrenamiento 1",
            targetType = expectedGenderTarget
        )
    }

    // Días de la semana
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    val todayDayName = remember {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lunes"
            Calendar.TUESDAY -> "Martes"
            Calendar.WEDNESDAY -> "Miércoles"
            Calendar.THURSDAY -> "Jueves"
            Calendar.FRIDAY -> "Viernes"
            Calendar.SATURDAY -> "Sábado"
            else -> "Lunes"
        }
    }

    var selectedDay by remember { mutableStateOf(todayDayName) }
    var selectedViewMode by remember { mutableIntStateOf(0) } // 0: Rutina & Cargas, 1: Historial Completo
    var selectedExerciseFilter by remember { mutableStateOf("TODOS") }

    // Registro modal
    var showRecordDialog by remember { mutableStateOf(false) }
    var exerciseToRecord by remember { mutableStateOf("") }
    var muscleGroupToRecord by remember { mutableStateOf("") }

    // Filtros rápidos de ejercicios disponibles
    val allUniqueExercises = remember(loadLogs) {
        val baseList = mutableListOf("TODOS", "SENTADILLA", "PRESS BANCA", "PESO MUERTO", "PRESS MILITAR", "DOMINADAS")
        loadLogs.forEach { log ->
            val cleanName = log.exerciseName.trim().uppercase()
            if (cleanName.isNotBlank() && !baseList.any { cleanName.contains(it) || it.contains(cleanName) }) {
                baseList.add(cleanName)
            }
        }
        baseList
    }

    val filteredLogs = remember(loadLogs, selectedExerciseFilter) {
        if (selectedExerciseFilter.equals("TODOS", ignoreCase = true)) {
            loadLogs
        } else {
            loadLogs.filter { log ->
                val name = log.exerciseName.uppercase()
                name.contains(selectedExerciseFilter) ||
                (selectedExerciseFilter == "SENTADILLA" && (name.contains("SENTAD") || name.contains("SQUAT"))) ||
                (selectedExerciseFilter == "PRESS BANCA" && (name.contains("BANCA") || name.contains("BENCH"))) ||
                (selectedExerciseFilter == "PESO MUERTO" && (name.contains("MUERTO") || name.contains("DEADLIFT"))) ||
                (selectedExerciseFilter == "PRESS MILITAR" && (name.contains("MILITAR") || name.contains("OHP"))) ||
                (selectedExerciseFilter == "DOMINADAS" && (name.contains("DOMINADA") || name.contains("PULL UP") || name.contains("PULL-UP")))
            }
        }
    }

    val exercisePR = remember(filteredLogs, selectedExerciseFilter) {
        if (selectedExerciseFilter.equals("TODOS", ignoreCase = true) || filteredLogs.isEmpty()) null
        else filteredLogs.maxByOrNull { it.weightKg }
    }

    // Parsear ejercicios de la rutina asignada
    val weeklyDays = remember(assignedRoutine) {
        WeeklyRoutineHelper.parseWeeklySchedule(assignedRoutine.exercisesJson)
    }
    val currentDayRoutine = remember(weeklyDays, selectedDay) {
        weeklyDays.firstOrNull { it.dayName.equals(selectedDay, ignoreCase = true) }
            ?: DayRoutine(dayName = selectedDay, muscleFocus = "Entrenamiento General")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BlackBackground)
    ) {
        // Cabecera de la Pantalla
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onNavigateBack != null) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextLightGray)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "HISTORIAL DE CARGAS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.5.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "Sobrecarga Progresiva • ${assignedRoutine.name}",
                                color = LimeGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botón para registrar ejercicio libre
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LimeGreen)
                            .clickable {
                                exerciseToRecord = ""
                                muscleGroupToRecord = currentDayRoutine.muscleFocus
                                showRecordDialog = true
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("REGISTRAR", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 9.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selector de Modo: [EJERCICIOS DEL DÍA] vs [HISTORIAL COMPLETO]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedViewMode == 0) LimeGreen.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (selectedViewMode == 0) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedViewMode = 0 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "EJERCICIOS DEL DÍA",
                            color = if (selectedViewMode == 0) LimeGreen else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selectedViewMode == 1) Color(0xFFF59E0B).copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (selectedViewMode == 1) Color(0xFFF59E0B) else SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { selectedViewMode = 1 }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "HISTORIAL COMPLETO (${loadLogs.size})",
                            color = if (selectedViewMode == 1) Color(0xFFF59E0B) else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Divider(color = SurfaceBorder, thickness = 1.dp)

        if (selectedViewMode == 0) {
            // MODO 0: DÍAS DE LA SEMANA Y EJERCICIOS PRECARGADOS
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(daysOfWeek) { day ->
                    val isSelected = selectedDay.equals(day, ignoreCase = true)
                    val isToday = todayDayName.equals(day, ignoreCase = true)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) LimeGreen else SurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) LimeGreen else if (isToday) LimeGreen.copy(alpha = 0.5f) else SurfaceBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedDay = day }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = day.uppercase(),
                                color = if (isSelected) Color.Black else TextLightGray,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            if (isToday) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) Color.Black else LimeGreen)
                                        .padding(horizontal = 3.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "HOY",
                                        color = if (isSelected) LimeGreen else Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 7.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Divider(color = SurfaceBorder, thickness = 0.5.dp)

            // Resumen de Enfoque Muscular del Día
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ENFOQUE: ${currentDayRoutine.muscleFocus.uppercase()}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "${currentDayRoutine.exercises.size} ejercicios programados",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            // Lista de Ejercicios del Día con su Último Registro y Comparativa
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentDayRoutine.exercises, key = { it.exerciseName }) { exercise ->
                    // Buscar registros de este ejercicio para este cliente
                    val exerciseLogs = remember(loadLogs, exercise.exerciseName) {
                        loadLogs.filter { it.exerciseName.equals(exercise.exerciseName, ignoreCase = true) }
                    }
                    val latestLog = exerciseLogs.firstOrNull()
                    val previousLog = if (exerciseLogs.size > 1) exerciseLogs[1] else null

                    ExerciseLoadCard(
                        exerciseName = exercise.exerciseName,
                        targetSets = exercise.sets,
                        targetReps = exercise.reps,
                        muscleGroup = currentDayRoutine.muscleFocus,
                        latestLog = latestLog,
                        previousLog = previousLog,
                        onRecordClick = {
                            exerciseToRecord = exercise.exerciseName
                            muscleGroupToRecord = currentDayRoutine.muscleFocus
                            showRecordDialog = true
                        },
                        onSharePr = { weight, reps ->
                            onShareAsPost?.invoke(exercise.exerciseName, weight, reps)
                        }
                    )
                }

                // Opción para registrar un ejercicio adicional
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceDark)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                exerciseToRecord = ""
                                muscleGroupToRecord = currentDayRoutine.muscleFocus
                                showRecordDialog = true
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "+ REGISTRAR EJERCICIO EXTRA O PERSONALIZADO",
                                color = LimeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                }
            }
        } else {
            // MODO 1: HISTORIAL CRONOLÓGICO COMPLETO CON FILTROS RÁPIDOS
            Column(modifier = Modifier.fillMaxSize()) {
                // Barra de Chips Horizontales para filtrar ejercicio en 1 segundo
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(allUniqueExercises) { chipExercise ->
                        val isSelected = selectedExerciseFilter.equals(chipExercise, ignoreCase = true)
                        val count = if (chipExercise.equals("TODOS", ignoreCase = true)) {
                            loadLogs.size
                        } else {
                            loadLogs.count { it.exerciseName.contains(chipExercise, ignoreCase = true) }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFF59E0B) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFFF59E0B) else SurfaceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedExerciseFilter = chipExercise }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = chipExercise,
                                    color = if (isSelected) Color.Black else TextLightGray,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 10.5.sp
                                )
                                if (count > 0 && !chipExercise.equals("TODOS", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "($count)",
                                        color = if (isSelected) Color.Black.copy(alpha = 0.8f) else Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 0.5.dp)

                // Tarjeta de Récord Personal (PR) cuando se filtra un ejercicio específico
                if (exercisePR != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF241804))
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🏆", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "RÉCORD PERSONAL (PR) • $selectedExerciseFilter",
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${exercisePR.weightKg} kg",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "× ${exercisePR.reps} reps",
                                        color = LimeGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "(${exercisePR.formattedDate})",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFF59E0B))
                                    .clickable {
                                        exerciseToRecord = selectedExerciseFilter
                                        muscleGroupToRecord = exercisePR.muscleGroup
                                        showRecordDialog = true
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "+ NUEVA SERIE",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏋️", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedExerciseFilter.equals("TODOS", ignoreCase = true)) "Sin registros de carga aún" else "Sin registros de $selectedExerciseFilter",
                                color = TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Registra los pesos de tu entrenamiento para llevar tu sobrecarga progresiva día a día.",
                                color = TextMuted,
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredLogs, key = { it.id }) { log ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = log.exerciseName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        if (log.dayOfWeek.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(SurfaceElevated)
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = log.dayOfWeek,
                                                    color = TextMuted,
                                                    fontSize = 8.5.sp
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${log.weightKg} kg × ${log.reps} reps",
                                            color = LimeGreen,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "(${log.setsCount} series)",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }

                                    if (log.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Nota: ${log.notes}",
                                            color = TextLightGray,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "${log.formattedDate} a las ${log.formattedTime}",
                                        color = TextMuted,
                                        fontSize = 9.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            onShareAsPost?.invoke(log.exerciseName, log.weightKg, log.reps)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Compartir PR", tint = LimeGreen, modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { onDeleteLoadLog(log) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para registrar una nueva carga / serie
    if (showRecordDialog) {
        RecordLoadModal(
            defaultExerciseName = exerciseToRecord,
            defaultMuscleGroup = muscleGroupToRecord,
            defaultDay = selectedDay,
            onDismiss = { showRecordDialog = false },
            onSave = { newLog ->
                onSaveLoadLog(
                    newLog.copy(
                        clientId = client.id,
                        clientAccessId = client.accessId
                    )
                )
                showRecordDialog = false
                Toast.makeText(context, "¡Carga guardada con éxito!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
}

/**
 * Tarjeta individual de ejercicio con métricas de sobrecarga progresiva.
 */
@Composable
fun ExerciseLoadCard(
    exerciseName: String,
    targetSets: String,
    targetReps: String,
    muscleGroup: String,
    latestLog: WorkoutLoadLog?,
    previousLog: WorkoutLoadLog?,
    onRecordClick: () -> Unit,
    onSharePr: (Double, Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exerciseName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Objetivo: $targetSets series • $targetReps reps",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                // Botón para registrar carga
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(LimeGreen)
                        .clickable { onRecordClick() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "+ CARGA",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = SurfaceBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Estado de la carga / comparativa
            if (latestLog != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Última sesión: ${latestLog.formattedDate}",
                            color = TextMuted,
                            fontSize = 9.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${latestLog.weightKg} kg",
                                color = LimeGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = " × ${latestLog.reps} reps",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Comparativa con la sesión anterior
                    if (previousLog != null) {
                        val diffKg = latestLog.weightKg - previousLog.weightKg
                        val diffReps = latestLog.reps - previousLog.reps

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (diffKg > 0) Color(0xFF10B981).copy(alpha = 0.2f)
                                    else if (diffKg == 0.0 && diffReps >= 0) LimeGreen.copy(alpha = 0.2f)
                                    else Color(0xFFEF4444).copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when {
                                    diffKg > 0 -> "🔥 +${diffKg} kg"
                                    diffKg == 0.0 && diffReps > 0 -> "⚡ +${diffReps} reps"
                                    diffKg == 0.0 -> "= Mantenido"
                                    else -> "${diffKg} kg"
                                },
                                color = if (diffKg >= 0) LimeGreen else Color(0xFFEF4444),
                                fontWeight = FontWeight.Black,
                                fontSize = 9.5.sp
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Primera marca",
                                color = TextLightGray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sin registro previo aún",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "¡Establece tu marca hoy! ⚡",
                        color = LimeGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Modal interactivo para registrar rápidamente la carga (peso en kg y repeticiones).
 */
@Composable
fun RecordLoadModal(
    defaultExerciseName: String,
    defaultMuscleGroup: String,
    defaultDay: String,
    onDismiss: () -> Unit,
    onSave: (WorkoutLoadLog) -> Unit
) {
    var exerciseName by remember { mutableStateOf(defaultExerciseName) }
    var weightText by remember { mutableStateOf("60.0") }
    var repsText by remember { mutableStateOf("10") }
    var setsCount by remember { mutableIntStateOf(4) }
    var notes by remember { mutableStateOf("") }

    val currentWeight = weightText.toDoubleOrNull() ?: 0.0
    val currentReps = repsText.toIntOrNull() ?: 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceDark)
                    .border(1.2.dp, LimeGreen.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REGISTRAR CARGA",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Sobrecarga progresiva para $defaultDay",
                                color = LimeGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nombre del ejercicio
                    OutlinedTextField(
                        value = exerciseName,
                        onValueChange = { exerciseName = it },
                        label = { Text("Nombre del Ejercicio", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control de Peso (kg) con botones de ajuste rápido (-5, -2.5, +2.5, +5)
                    Text("Peso Levantado (kg):", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(-5.0, -2.5, -1.0).forEach { delta ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceElevated)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val newW = (currentWeight + delta).coerceAtLeast(0.0)
                                        weightText = "%.1f".format(java.util.Locale.US, newW)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$delta", color = TextLightGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            modifier = Modifier.weight(1.8f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = LimeGreen,
                                unfocusedTextColor = LimeGreen
                            )
                        )

                        listOf(1.0, 2.5, 5.0).forEach { delta ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LimeGreen.copy(alpha = 0.2f))
                                    .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .clickable {
                                        val newW = currentWeight + delta
                                        weightText = "%.1f".format(java.util.Locale.US, newW)
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+$delta", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control de Repeticiones con accesos rápidos (6, 8, 10, 12, 15)
                    Text("Repeticiones Logradas:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(6, 8, 10, 12, 15).forEach { repsVal ->
                            val isSelected = currentReps == repsVal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) LimeGreen else SurfaceElevated)
                                    .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { repsText = repsVal.toString() }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$repsVal",
                                    color = if (isSelected) Color.Black else TextLightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Número de Series
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Series realizadas:", color = TextLightGray, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(1, 2, 3, 4, 5).forEach { s ->
                                val isSelected = setsCount == s
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) LimeGreen else SurfaceElevated)
                                        .clickable { setsCount = s },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "$s",
                                        color = if (isSelected) Color.Black else TextLightGray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Notas / Sensaciones
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Sensaciones (ej: Buena técnica, al fallo...)", fontSize = 10.5.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botones Guardar / Cancelar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("CANCELAR", fontSize = 10.5.sp)
                        }

                        Button(
                            onClick = {
                                if (exerciseName.isBlank()) return@Button
                                val log = WorkoutLoadLog(
                                    clientId = 0L,
                                    exerciseName = exerciseName.trim(),
                                    muscleGroup = defaultMuscleGroup,
                                    dayOfWeek = defaultDay,
                                    weightKg = currentWeight,
                                    reps = currentReps,
                                    setsCount = setsCount,
                                    notes = notes.trim(),
                                    timestamp = System.currentTimeMillis()
                                )
                                onSave(log)
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("GUARDAR MARCA", fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
