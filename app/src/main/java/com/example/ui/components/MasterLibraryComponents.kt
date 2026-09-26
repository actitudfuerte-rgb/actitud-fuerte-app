package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.Client
import com.example.data.model.DayRoutine
import com.example.data.model.Routine
import com.example.data.model.RoutineExercise
import com.example.data.model.WeeklyRoutineHelper
import com.example.data.sync.FirebaseBackupManager
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Sección "BIBLIOTECA MAESTRA" en el Panel Principal.
 * Gestión completa de rutinas semanales (Lunes a Sábado) con edición, renombrado,
 * guardado en base de datos local y sincronización con Firebase.
 */
@Composable
fun MasterLibrarySection(
    routines: List<Routine>,
    clients: List<Client>,
    onSaveRoutine: (Routine) -> Unit,
    onDeleteRoutine: (Routine) -> Unit,
    onRotateRoutine: (targetType: String, clientName: String?, onResult: (Routine?) -> Unit) -> Unit,
    onRequestFirebaseBackup: suspend () -> FirebaseBackupManager.BackupSummary,
    onSyncFirebase: ((FirebaseBackupManager.CloudSyncResult) -> Unit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showCreateOrEditDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<Routine?>(null) }
    var selectedRoutineToView by remember { mutableStateOf<Routine?>(null) }
    var showRotationDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var backupSummary by remember { mutableStateOf<FirebaseBackupManager.BackupSummary?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Encabezado de la Biblioteca Maestra
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(LimeGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = LimeGreen,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "BIBLIOTECA MAESTRA",
                        color = TextLightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Rutinas Semanales (Lunes a Sábados)",
                        color = LimeGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Botón Crear Rutina Semanal
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(LimeGreen)
                    .clickable {
                        routineToEdit = null
                        showCreateOrEditDialog = true
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NUEVA RUTINA", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Herramientas Operativas: Rotador Aleatorio & Respaldo Cloud Firebase
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rotador Antimonotonía
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showRotationDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, Color(0xFF3D3216))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF33260A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Rotar Rutina", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Aleatoria semanal", color = Color(0xFFFBBF24), fontSize = 9.5.sp)
                    }
                }
            }

            // Respaldo Cloud Firebase
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        coroutineScope.launch {
                            val summary = onRequestFirebaseBackup()
                            backupSummary = summary
                            showBackupDialog = true
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, Color(0xFF1B3545))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F2634)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Respaldo Cloud", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Sincronizar Firebase", color = Color(0xFF38BDF8), fontSize = 9.5.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Carrusel Horizontal de Rutinas Semanales
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no hay rutinas registradas. Toca '+ NUEVA RUTINA' para crear el plan semanal de Lunes a Sábados.",
                    color = TextMuted,
                    fontSize = 11.5.sp
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(routines) { routine ->
                    val weeklyDays = WeeklyRoutineHelper.parseWeeklySchedule(routine.exercisesJson)
                    val activeDaysCount = weeklyDays.count { it.exercises.isNotEmpty() }
                    val totalExercisesCount = weeklyDays.sumOf { it.exercises.size }

                    val badgeColor = when (routine.targetType) {
                        "Hombres" -> Color(0xFF60A5FA)
                        "Mujeres" -> Color(0xFFF472B6)
                        else -> LimeGreen
                    }
                    val badgeBg = when (routine.targetType) {
                        "Hombres" -> Color(0xFF172554)
                        "Mujeres" -> Color(0xFF500724)
                        else -> Color(0xFF1C2D11)
                    }

                    Card(
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Fila Superior: Badge de Asignación y Días Activos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(badgeBg)
                                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                                ) {
                                    Text(
                                        text = routine.targetType.uppercase(),
                                        color = badgeColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = LimeGreen,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Lun-Sáb ($totalExercisesCount ej.)",
                                        color = TextLightGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Nombre de la Rutina (Renombrable y Dinámico)
                            Text(
                                text = routine.name,
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Detalles de destinatario o condición
                            if (routine.targetClientNames.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Atleta: ${routine.targetClientNames}",
                                    color = badgeColor,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (routine.specialConditions.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = routine.specialConditions,
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Botones de Acción: "VER / ENTRENAR" y "EDITAR"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Botón Ver / Entrenar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF1E2E16))
                                        .clickable { selectedRoutineToView = routine }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = LimeGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("VER PLAN", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Botón Editar y Renombrar
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF262C36))
                                        .clickable {
                                            routineToEdit = routine
                                            showCreateOrEditDialog = true
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Color(0xFF93C5FD),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("EDITAR", color = Color(0xFF93C5FD), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Botón Compartir por WhatsApp
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF143823))
                                        .clickable {
                                            val matchingClient = clients.firstOrNull { routine.targetClientNames.contains(it.fullName, ignoreCase = true) }
                                            shareRoutineWhatsApp(context, routine, matchingClient?.phone.orEmpty(), matchingClient?.fullName)
                                        }
                                        .padding(horizontal = 9.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📲", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO 1: Crear o Editar Rutina Semanal (Lunes a Sábado) con Guardado Funcional
    if (showCreateOrEditDialog) {
        WeeklyRoutineEditDialog(
            existingRoutine = routineToEdit,
            clients = clients,
            onDismiss = {
                showCreateOrEditDialog = false
                routineToEdit = null
            },
            onSave = { updatedRoutine ->
                onSaveRoutine(updatedRoutine)
                showCreateOrEditDialog = false
                routineToEdit = null
                val actionText = if (routineToEdit != null) "actualizada correctamente" else "guardada en la Biblioteca Maestra"
                Toast.makeText(context, "Rutina semanal $actionText", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIÁLOGO 2: Visualizador Semanal de Rutina con Reproducción de Videos de YouTube
    if (selectedRoutineToView != null) {
        WeeklyRoutineDetailDialog(
            routine = selectedRoutineToView!!,
            onDismiss = { selectedRoutineToView = null },
            onEdit = {
                val current = selectedRoutineToView
                selectedRoutineToView = null
                routineToEdit = current
                showCreateOrEditDialog = true
            },
            onDelete = {
                onDeleteRoutine(it)
                selectedRoutineToView = null
                Toast.makeText(context, "Rutina eliminada de la biblioteca", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // DIÁLOGO 3: Rotación Aleatoria para Usuario / Grupo
    if (showRotationDialog) {
        WeeklyRandomRotationDialog(
            clients = clients,
            routines = routines,
            onDismiss = { showRotationDialog = false }
        )
    }

    // DIÁLOGO 4: Respaldo y Sincronización Cloud Firebase
    if (showBackupDialog && backupSummary != null) {
        FirebaseBackupDialog(
            summary = backupSummary!!,
            onDismiss = { showBackupDialog = false },
            onShareJson = {
                FirebaseBackupManager.shareBackupJson(context, backupSummary!!.jsonPayload)
            },
            onSyncToFirestore = { onResult ->
                onSyncFirebase(onResult)
            }
        )
    }
}

/**
 * Formulario interactivo para Crear y Editar Rutinas Semanales completas (Lunes a Sábados).
 * Permite:
 * 1. Renombrar la rutina (ej: Rutina de entrenamiento 1, Rutina de musculación avanzada, etc.)
 * 2. Asignación (Hombres, Mujeres, Usuarios específicos - Alex Gómez, Carlen Chirinos)
 * 3. Condiciones especiales (salud, edad, lesiones)
 * 4. Navegación por pestañas de días (Lunes a Sábado)
 * 5. Múltiples ejercicios por día con Nombre, Series, Repeticiones y URL de YouTube
 * 6. Botón funcional para Guardar las actualizaciones y ajustes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRoutineEditDialog(
    existingRoutine: Routine?,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSave: (Routine) -> Unit
) {
    val isEditing = existingRoutine != null

    // Estados generales
    var routineName by remember {
        mutableStateOf(existingRoutine?.name ?: "Rutina de entrenamiento ")
    }
    var targetType by remember {
        mutableStateOf(existingRoutine?.targetType ?: "Hombres")
    }
    var specialConditions by remember {
        mutableStateOf(existingRoutine?.specialConditions ?: "")
    }
    var selectedClientNames by remember {
        mutableStateOf(existingRoutine?.targetClientNames ?: "")
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    // Días de la semana cargados
    val initialWeek = remember {
        if (existingRoutine != null && existingRoutine.exercisesJson.isNotBlank()) {
            WeeklyRoutineHelper.parseWeeklySchedule(existingRoutine.exercisesJson)
        } else {
            WeeklyRoutineHelper.defaultEmptyWeek()
        }
    }

    // Estado editable por cada día (Lunes a Sábado)
    val daysState = remember {
        mutableStateListOf<DayRoutine>().apply {
            addAll(initialWeek)
        }
    }

    // Día seleccionado actualmente en el formulario
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    val currentDay = daysState[selectedDayIndex]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = LimeGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isEditing) "Editar Rutina Semanal" else "Nueva Rutina Semanal",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Programa de Lunes a Sábados",
                        color = LimeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Nombre de la Rutina (Renombrable)
                item {
                    OutlinedTextField(
                        value = routineName,
                        onValueChange = { routineName = it },
                        label = { Text("Nombre de la Rutina") },
                        placeholder = { Text("Ej: Rutina de entrenamiento 1, 2, 3...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // 2. Asignación de la Rutina (Desplegable)
                item {
                    Text("Asignación:", color = TextLightGray, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = targetType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Asignar a") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            listOf("Hombres", "Mujeres", "Usuarios específicos").forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        targetType = option
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Si es "Usuarios específicos", selección de atletas
                if (targetType == "Usuarios específicos") {
                    item {
                        Text("Atletas asignados (Alex Gómez, Carlen Chirinos, etc.):", color = Color(0xFFA3E635), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            clients.forEach { c ->
                                val isSelected = selectedClientNames.contains(c.fullName)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) LimeGreen else SurfaceElevated)
                                        .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedClientNames = if (isSelected) {
                                                selectedClientNames.replace(c.fullName, "").replace(", ,", ",").trim(',', ' ')
                                            } else {
                                                if (selectedClientNames.isBlank()) c.fullName else "$selectedClientNames, ${c.fullName}"
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = c.fullName,
                                        color = if (isSelected) Color.Black else TextLightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Condiciones Especiales
                item {
                    OutlinedTextField(
                        value = specialConditions,
                        onValueChange = { specialConditions = it },
                        label = { Text("Condiciones especiales (salud, edad, lesiones)") },
                        placeholder = { Text("Ej: Hipertrofia básica, cuidado articular lumbar, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                // 4. Selector de Días de la Semana (Lunes a Sábado)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DÍAS DE ENTRENAMIENTO (LUNES A SÁBADOS):",
                        color = TextLightGray,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        daysState.forEachIndexed { index, day ->
                            val isSelected = index == selectedDayIndex
                            val count = day.exercises.size
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) LimeGreen else SurfaceElevated)
                                    .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedDayIndex = index }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${day.dayName.uppercase()} ($count)",
                                    color = if (isSelected) Color.Black else TextLightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 5. Configuración del Día Activo: Foco Muscular y Ejercicios
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2217)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E3D25))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Plan para: ${currentDay.dayName.uppercase()}",
                                    color = LimeGreen,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Black
                                )

                                TextButton(
                                    onClick = {
                                        val updatedList = currentDay.exercises.toMutableList()
                                        updatedList.add(
                                            RoutineExercise(
                                                exerciseName = "",
                                                sets = "4",
                                                reps = "10-12",
                                                youtubeUrl = ""
                                            )
                                        )
                                        daysState[selectedDayIndex] = currentDay.copy(exercises = updatedList)
                                    }
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ Ejercicio", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Foco o Grupo Muscular del Día
                            OutlinedTextField(
                                value = currentDay.muscleFocus,
                                onValueChange = { newFocus ->
                                    daysState[selectedDayIndex] = currentDay.copy(muscleFocus = newFocus)
                                },
                                label = { Text("Grupo o Enfoque Muscular del ${currentDay.dayName}") },
                                placeholder = { Text("Ej: Pecho y Tríceps, Pierna y Glúteo, etc.") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LimeGreen,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 6. Lista de Ejercicios del Día Activo
                if (currentDay.exercises.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin ejercicios para el ${currentDay.dayName}. Toca '+ Ejercicio' arriba para agregar.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    items(currentDay.exercises.indices.toList()) { exIndex ->
                        val ex = currentDay.exercises[exIndex]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                            border = BorderStroke(1.dp, SurfaceBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentDay.dayName} - Ejercicio #${exIndex + 1}",
                                        color = TextLightGray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    IconButton(
                                        onClick = {
                                            val updated = currentDay.exercises.toMutableList()
                                            updated.removeAt(exIndex)
                                            daysState[selectedDayIndex] = currentDay.copy(exercises = updated)
                                        },
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar ejercicio",
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Nombre del ejercicio
                                OutlinedTextField(
                                    value = ex.exerciseName,
                                    onValueChange = { newName ->
                                        val updated = currentDay.exercises.toMutableList()
                                        updated[exIndex] = ex.copy(exerciseName = newName)
                                        daysState[selectedDayIndex] = currentDay.copy(exercises = updated)
                                    },
                                    label = { Text("Nombre del ejercicio") },
                                    placeholder = { Text("Ej: Press de Banca, Sentadilla...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LimeGreen,
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Series y Repeticiones
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = ex.sets,
                                        onValueChange = { newSets ->
                                            val updated = currentDay.exercises.toMutableList()
                                            updated[exIndex] = ex.copy(sets = newSets)
                                            daysState[selectedDayIndex] = currentDay.copy(exercises = updated)
                                        },
                                        label = { Text("Series") },
                                        placeholder = { Text("4") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LimeGreen,
                                            unfocusedBorderColor = SurfaceBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    OutlinedTextField(
                                        value = ex.reps,
                                        onValueChange = { newReps ->
                                            val updated = currentDay.exercises.toMutableList()
                                            updated[exIndex] = ex.copy(reps = newReps)
                                            daysState[selectedDayIndex] = currentDay.copy(exercises = updated)
                                        },
                                        label = { Text("Repeticiones") },
                                        placeholder = { Text("10-12") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = LimeGreen,
                                            unfocusedBorderColor = SurfaceBorder,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // URL de YouTube del ejercicio
                                OutlinedTextField(
                                    value = ex.youtubeUrl,
                                    onValueChange = { newUrl ->
                                        val updated = currentDay.exercises.toMutableList()
                                        updated[exIndex] = ex.copy(youtubeUrl = newUrl)
                                        daysState[selectedDayIndex] = currentDay.copy(exercises = updated)
                                    },
                                    label = { Text("URL de video de YouTube") },
                                    placeholder = { Text("https://www.youtube.com/watch?v=...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Videocam,
                                            contentDescription = null,
                                            tint = Color(0xFFFF5252),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = LimeGreen,
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (routineName.isNotBlank()) {
                        // Limpiar ejercicios vacíos antes de guardar
                        val cleanedDays = daysState.map { day ->
                            day.copy(exercises = day.exercises.filter { it.exerciseName.isNotBlank() })
                        }
                        val routineToSave = Routine(
                            id = existingRoutine?.id ?: 0,
                            name = routineName.trim(),
                            targetType = targetType,
                            targetClientNames = selectedClientNames.trim(),
                            specialConditions = specialConditions.trim(),
                            exercisesJson = WeeklyRoutineHelper.weeklyScheduleToJson(cleanedDays),
                            createdAt = existingRoutine?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(routineToSave)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isEditing) "Guardar Actualizaciones y Ajustes" else "Guardar Rutina Semanal",
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextLightGray)
            }
        },
        containerColor = SurfaceDark
    )
}

/**
 * Visualizador interactivo de una rutina semanal completa (Lunes a Sábados).
 * Permite explorar cada día, ver las series, repeticiones y abrir los videos de YouTube.
 */
@Composable
fun WeeklyRoutineDetailDialog(
    routine: Routine,
    onDismiss: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: ((Routine) -> Unit)? = null
) {
    val context = LocalContext.current
    val weeklyDays = WeeklyRoutineHelper.parseWeeklySchedule(routine.exercisesJson)

    // Determinar día inicial según el día actual de la semana (Lunes a Sábado)
    val currentDayIndex = remember {
        val cal = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        when (cal) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            else -> 0
        }
    }

    var selectedDayIndex by remember { mutableIntStateOf(currentDayIndex) }
    val dayRoutine = weeklyDays.getOrElse(selectedDayIndex) { weeklyDays.first() }

    val badgeColor = when (routine.targetType) {
        "Hombres" -> Color(0xFF60A5FA)
        "Mujeres" -> Color(0xFFF472B6)
        else -> LimeGreen
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(routine.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // Botón Editar desde el visor
                    if (onEdit != null) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF93C5FD), modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Asignado: ${routine.targetType} ${if (routine.targetClientNames.isNotBlank()) "(${routine.targetClientNames})" else ""}",
                    color = badgeColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )

                if (routine.specialConditions.isNotBlank()) {
                    Text("Condición: ${routine.specialConditions}", color = TextMuted, fontSize = 10.5.sp)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Selector Horizontal de Días (Lunes a Sábado)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weeklyDays.forEachIndexed { index, d ->
                        val isSelected = index == selectedDayIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) LimeGreen else SurfaceElevated)
                                .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedDayIndex = index }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = d.dayName.uppercase(),
                                color = if (isSelected) Color.Black else TextLightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Foco muscular del día seleccionado
                if (dayRoutine.muscleFocus.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2619))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Enfoque: ${dayRoutine.muscleFocus}",
                            color = LimeGreen,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Lista de ejercicios del día
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (dayRoutine.exercises.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceElevated)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Día de descanso o sin ejercicios programados para el ${dayRoutine.dayName}.",
                                    color = TextMuted,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    } else {
                        items(dayRoutine.exercises) { ex ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, SurfaceBorder)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(ex.exerciseName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Series: ${ex.sets}", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("Reps: ${ex.reps}", color = TextLightGray, fontSize = 11.sp)
                                    }

                                    if (ex.youtubeUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFF3B1212))
                                                .border(1.dp, Color(0xFFCC0000), RoundedCornerShape(6.dp))
                                                .clickable {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ex.youtubeUrl))
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        Toast.makeText(context, "No se pudo abrir el enlace de YouTube", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                                .padding(vertical = 6.dp, horizontal = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Videocam,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFF5252),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Ver técnica en YouTube",
                                                    color = Color(0xFFFF8A80),
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
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
            ) {
                Text("Listo", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = if (onDelete != null) {
            {
                TextButton(onClick = { onDelete(routine) }) {
                    Text("Eliminar Rutina", color = Color(0xFFFF5252))
                }
            }
        } else null,
        containerColor = SurfaceDark
    )
}

/**
 * Funciones de ayuda para formatear y compartir planes de entrenamiento por WhatsApp
 */
fun formatRoutineForWhatsApp(routine: Routine, clientName: String? = null): String {
    val weeklyDays = WeeklyRoutineHelper.parseWeeklySchedule(routine.exercisesJson)
    val sb = StringBuilder()
    sb.append("🏋️‍♂️ *ACTITUD FUERTE FITNESS CLUB*\n")
    sb.append("📋 *PLAN DE ENTRENAMIENTO ASIGNADO*\n\n")
    if (!clientName.isNullOrBlank()) {
        sb.append("👤 *Atleta:* $clientName\n")
    }
    sb.append("🎯 *Rutina:* ${routine.name}\n")
    if (routine.specialConditions.isNotBlank()) {
        sb.append("⚡ *Enfoque/Condición:* ${routine.specialConditions}\n")
    }
    sb.append("\n━━━━━━━━━━━━━━━━━━━━\n")
    weeklyDays.forEach { day ->
        if (day.exercises.isNotEmpty()) {
            sb.append("\n📅 *${day.dayName.uppercase()}*\n")
            day.exercises.forEachIndexed { i, ex ->
                sb.append("${i + 1}. *${ex.exerciseName}* • ${ex.sets} series × ${ex.reps} reps\n")
                if (ex.youtubeUrl.isNotBlank()) {
                    sb.append("   🎥 Video: ${ex.youtubeUrl}\n")
                }
            }
        }
    }
    sb.append("\n━━━━━━━━━━━━━━━━━━━━\n")
    sb.append("🔥 _¡A darlo todo en la sala de musculación!_\n")
    sb.append("💪 *Actitud Fuerte | Alex Gómez*\n")
    return sb.toString()
}

fun shareRoutineWhatsApp(context: Context, routine: Routine, clientPhone: String = "", clientName: String? = null) {
    val message = formatRoutineForWhatsApp(routine, clientName)
    try {
        val cleanPhone = clientPhone.replace("[^0-9]".toRegex(), "")
        val url = if (cleanPhone.isNotEmpty()) {
            val formatted = if (cleanPhone.startsWith("0")) "58" + cleanPhone.substring(1) else cleanPhone
            "https://api.whatsapp.com/send?phone=$formatted&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
        } else {
            "https://api.whatsapp.com/send?text=${java.net.URLEncoder.encode(message, "UTF-8")}"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Rutina Actitud Fuerte", message))
        Toast.makeText(context, "Rutina copiada al portapapeles", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Diálogo para la Rotación Aleatoria Semanal Antimonotonía.
 */
@Composable
fun WeeklyRandomRotationDialog(
    clients: List<Client>,
    routines: List<Routine>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTargetType by remember { mutableStateOf("Hombres") }
    var selectedClient by remember { mutableStateOf(clients.firstOrNull()) }
    var rotatedRoutine by remember { mutableStateOf<Routine?>(null) }
    var hasRotated by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rotación Aleatoria de Rutinas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Selecciona el perfil o atleta para sortear una rutina semanal al azar y evitar la monotonía:",
                    color = TextLightGray,
                    fontSize = 11.5.sp
                )

                // Selector de categoría
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Hombres", "Mujeres", "Usuarios específicos").forEach { cat ->
                        val isSel = selectedTargetType == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFFFBBF24) else SurfaceElevated)
                                .clickable {
                                    selectedTargetType = cat
                                    hasRotated = false
                                    rotatedRoutine = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.take(8),
                                color = if (isSel) Color.Black else TextLightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Si es usuarios específicos, selector de cliente
                if (selectedTargetType == "Usuarios específicos" && clients.isNotEmpty()) {
                    Text("Selecciona el atleta:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(clients) { c ->
                            val isClientSel = selectedClient?.id == c.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isClientSel) LimeGreen.copy(alpha = 0.2f) else SurfaceElevated)
                                    .border(1.dp, if (isClientSel) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                                    .clickable { selectedClient = c }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = c.fullName.take(15),
                                    color = if (isClientSel) LimeGreen else TextLightGray,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Botón para ejecutar la rotación aleatoria
                Button(
                    onClick = {
                        val candidates = routines.filter { r ->
                            if (selectedTargetType == "Usuarios específicos") {
                                r.targetType == "Usuarios específicos" ||
                                        (selectedClient != null && r.targetClientNames.contains(selectedClient!!.fullName, ignoreCase = true))
                            } else {
                                r.targetType.equals(selectedTargetType, ignoreCase = true)
                            }
                        }
                        rotatedRoutine = candidates.shuffled().firstOrNull()
                        hasRotated = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("GIRAR Y ASIGNAR AL AZAR", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }

                // Resultado de la rotación
                if (hasRotated) {
                    if (rotatedRoutine != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF262112)),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("¡RUTINA SEMANAL SELECCIONADA!", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(rotatedRoutine!!.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                if (rotatedRoutine!!.specialConditions.isNotBlank()) {
                                    Text(rotatedRoutine!!.specialConditions, color = TextMuted, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Lista para entrenar de Lunes a Sábados.", color = LimeGreen, fontSize = 11.sp)

                                Spacer(modifier = Modifier.height(10.dp))

                                // BOTONES DE ACCIÓN: WHATSAPP DIRECTO Y COPIAR
                                Button(
                                    onClick = {
                                        val phone = if (selectedTargetType == "Usuarios específicos") selectedClient?.phone.orEmpty() else ""
                                        val name = if (selectedTargetType == "Usuarios específicos") selectedClient?.fullName else null
                                        shareRoutineWhatsApp(context, rotatedRoutine!!, phone, name)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📲", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ENVIAR POR WHATSAPP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Button(
                                    onClick = {
                                        val name = if (selectedTargetType == "Usuarios específicos") selectedClient?.fullName else null
                                        val msg = formatRoutineForWhatsApp(rotatedRoutine!!, name)
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Rutina Actitud Fuerte", msg))
                                        Toast.makeText(context, "Rutina copiada al portapapeles", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📋", fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("COPIAR AL PORTAPAPELES", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No se encontraron rutinas registradas para esta categoría.", color = Color(0xFFFF5252), fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
            ) {
                Text("Cerrar", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceDark
    )
}

/**
 * Diálogo interactivo para auditar, sincronizar directamente en Cloud Firestore
 * y exportar el respaldo JSON de Actitud Fuerte Gym.
 */
@Composable
fun FirebaseBackupDialog(
    summary: FirebaseBackupManager.BackupSummary,
    onDismiss: () -> Unit,
    onShareJson: () -> Unit,
    onSyncToFirestore: ((FirebaseBackupManager.CloudSyncResult) -> Unit) -> Unit
) {
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<FirebaseBackupManager.CloudSyncResult?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSyncing) onDismiss() },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cloud Firestore",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF332000))
                        .border(1.dp, Color(0xFFFF9100).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "ONLINE",
                        color = Color(0xFFFF9100),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Datos listos para sincronizar con tu base de datos de Firebase:",
                    color = TextLightGray,
                    fontSize = 11.5.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    border = BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text("• Atletas/Socios (gym_clients): ${summary.totalClients}", color = Color.White, fontSize = 12.sp)
                        Text("• Rutinas Semanales (gym_routines): ${summary.totalRoutines}", color = LimeGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("• Asistencias / Check-ins (gym_attendances): ${summary.totalAttendances}", color = Color.White, fontSize = 12.sp)
                        Text("• Recibos / Pagos (gym_payments): ${summary.totalPayments}", color = Color.White, fontSize = 12.sp)
                        Text("• Evaluaciones Físicas (gym_measurements): ${summary.totalMeasurements}", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Resultado de la sincronización en vivo
                if (syncResult != null) {
                    val isSuccess = syncResult!!.success
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccess) Color(0xFF142911) else Color(0xFF3B1212)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSuccess) LimeGreen.copy(alpha = 0.6f) else Color(0xFFFF5252)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (isSuccess) LimeGreen else Color(0xFFFF5252),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = syncResult!!.message,
                                color = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFCDD2),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Botón Principal de Sincronización
                Button(
                    onClick = {
                        isSyncing = true
                        onSyncToFirestore { res ->
                            isSyncing = false
                            syncResult = res
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9100),
                        disabledContainerColor = Color(0xFF6B4300)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isSyncing
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "SUBIENDO A CLOUD FIRESTORE...",
                            color = Color.Black,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "SUBIR DATOS A FIRESTORE AHORA",
                            color = Color.Black,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = onShareJson,
                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Exportar JSON", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSyncing
            ) {
                Text("Cerrar", color = TextLightGray, fontSize = 11.sp)
            }
        },
        containerColor = SurfaceDark
    )
}
