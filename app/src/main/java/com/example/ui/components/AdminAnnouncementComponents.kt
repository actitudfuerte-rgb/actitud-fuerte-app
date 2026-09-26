package com.example.ui.components

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.AdminAnnouncement
import com.example.data.model.AnnouncementTheme
import com.example.data.model.SocialPost
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Cuadro de Anuncio Administrativo Fijado con fondo dinámico, vigencia de 24 horas,
 * reproductor de nota de voz y soporte para archivos adjuntos (imágenes y PDFs).
 */
@Composable
fun AdminAnnouncementPinnedCard(
    announcement: AdminAnnouncement?,
    isAdmin: Boolean = false,
    scheduledCount: Int = 0,
    onOpenCreateDialog: () -> Unit,
    onOpenScheduledDialog: () -> Unit = {},
    onToggleStrengthAck: ((Long) -> Unit)? = null,
    onOpenComments: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlayingAudio by remember { mutableStateOf(false) }
    var audioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var audioProgress by remember { mutableStateOf(0f) }

    // Limpiar reproductor al salir del composable
    DisposableEffect(announcement?.id) {
        onDispose {
            audioPlayer?.stop()
            audioPlayer?.release()
            audioPlayer = null
            isPlayingAudio = false
        }
    }

    LaunchedEffect(isPlayingAudio) {
        while (isPlayingAudio) {
            val player = audioPlayer
            if (player != null && player.isPlaying) {
                val current = player.currentPosition
                val total = player.duration.coerceAtLeast(1)
                audioProgress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            } else {
                isPlayingAudio = false
                audioProgress = 0f
            }
            delay(100)
        }
    }

    if (announcement != null && !announcement.isExpired) {
        val theme = announcement.theme
        val brush = Brush.verticalGradient(
            listOf(Color(theme.topGradientColor), Color(theme.bottomGradientColor))
        )
        val accentColor = Color(theme.accentColor)

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(brush)
                .border(1.2.dp, accentColor.copy(alpha = theme.borderAlpha), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Column {
                // Cabecera del Anuncio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(theme.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ANUNCIO OFICIAL • ADMIN",
                            color = accentColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge de vigencia 24h
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏳ ${announcement.remainingTimeString}",
                                color = accentColor,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            if (scheduledCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.25f))
                                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(6.dp))
                                        .clickable { onOpenScheduledDialog() }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "📅 $scheduledCount",
                                        color = Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            // Botón exclusivo para que el administrador emita un nuevo comunicado
                            IconButton(
                                onClick = onOpenCreateDialog,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar o publicar nuevo comunicado",
                                    tint = accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Mensaje de Texto del Anuncio
                if (announcement.contentText.isNotBlank()) {
                    Text(
                        text = announcement.contentText,
                        color = Color.White,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Reproductor de Nota de Voz (Audio)
                if (announcement.audioUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    if (isPlayingAudio) {
                                        audioPlayer?.pause()
                                        isPlayingAudio = false
                                    } else {
                                        try {
                                            if (audioPlayer == null) {
                                                audioPlayer = MediaPlayer().apply {
                                                    val audioFile = getAudioFileFromSource(context, announcement.audioUrl)
                                                    if (audioFile != null) {
                                                        setDataSource(audioFile.absolutePath)
                                                        prepare()
                                                    }
                                                    setOnCompletionListener {
                                                        isPlayingAudio = false
                                                        audioProgress = 0f
                                                    }
                                                }
                                            }
                                            audioPlayer?.start()
                                            isPlayingAudio = true
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No se pudo reproducir la nota de voz", Toast.LENGTH_SHORT).show()
                                            isPlayingAudio = false
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(accentColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlayingAudio) "Pausar" else "Reproducir",
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "🎙️ Nota de voz del Entrenador",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        if (announcement.audioDurationSeconds > 0) "${announcement.audioDurationSeconds}s" else "Audio",
                                        color = accentColor,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { audioProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = accentColor,
                                    trackColor = Color.White.copy(alpha = 0.2f),
                                )
                            }
                        }
                    }
                }

                // Archivo Adjunto (Imagen o PDF)
                if (announcement.attachmentUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    when (announcement.attachmentType) {
                        "IMAGE" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            ) {
                                PostMediaImage(
                                    mediaUrl = announcement.attachmentUrl,
                                    contentDescription = "Imagen adjunta del comunicado",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 120.dp, max = 240.dp)
                                )
                            }
                        }
                        "PDF" -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        openAttachment(context, announcement.attachmentUrl, "application/pdf")
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = announcement.attachmentName.ifBlank { "Documento_Oficial.pdf" },
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Toca para abrir documento PDF",
                                        color = accentColor,
                                        fontSize = 9.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Interacciones Oficiales: 💪 Acuse de Fuerza y 💬 Preguntas al Coach
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = accentColor.copy(alpha = 0.25f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón 1: 💪 Acuse de Fuerza / Actitud Fuerte
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (announcement.isLikedByMe) accentColor.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.4f))
                            .border(
                                1.dp,
                                if (announcement.isLikedByMe) accentColor else accentColor.copy(alpha = 0.35f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onToggleStrengthAck?.invoke(announcement.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (announcement.isLikedByMe) "💪🔥" else "💪", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (announcement.isLikedByMe) "ACUSE DE FUERZA DADO" else "ACUSE DE FUERZA",
                                color = if (announcement.isLikedByMe) accentColor else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (announcement.likesCount > 0) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(accentColor)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "${announcement.likesCount}",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Botón 2: 💬 Comentarios / Preguntas al Coach
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable { onOpenComments?.invoke(announcement.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💬", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (announcement.commentsCount > 0) "PREGUNTAS (${announcement.commentsCount})" else "PREGUNTAR",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    } else {
        if (isAdmin) {
            // Estado sin anuncio activo o expirado (permite al admin publicar uno nuevo con un clic)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("📢", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "TABLÓN ADMINISTRATIVO",
                                color = LimeGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Sin comunicados en las últimas 24h",
                                color = TextLightGray,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Button(
                        onClick = onOpenCreateDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EMITIR", fontSize = 11.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            // Cuadro institucional oficial de lectura para atletas y usuarios regulares (sin botón EMITIR)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📌", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ANUNCIO OFICIAL • ADMIN",
                                color = LimeGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "alexgcuicas@gmail.com",
                            color = TextMuted,
                            fontSize = 10.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📢 Bienvenidos a Actitud Fuerte. Mantén tu asistencia al día, consulta tu rutina diaria y comparte tus progresos con toda la comunidad.",
                        color = Color.White,
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}

/**
 * Diálogo para redactar y emitir comunicados oficiales con vigencia de 24 horas,
 * selección de fondo dinámico, nota de voz y adjuntos.
 */
@Composable
fun CreateAdminAnnouncementDialog(
    currentAdminName: String,
    onDismiss: () -> Unit,
    onPublishAnnouncement: (AdminAnnouncement) -> Unit
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf(AnnouncementTheme.OFFICIAL) }
    var textMessage by remember { mutableStateOf("") }

    // Grabación / Selección de Nota de Voz
    var audioUrl by remember { mutableStateOf("") }
    var audioDurationSeconds by remember { mutableIntStateOf(0) }
    var isRecording by remember { mutableStateOf(false) }
    var recordTime by remember { mutableIntStateOf(0) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }

    // Adjuntos
    var attachmentUrl by remember { mutableStateOf("") }
    var attachmentName by remember { mutableStateOf("") }
    var attachmentType by remember { mutableStateOf("NONE") } // "IMAGE", "PDF", "NONE"

    // Cronómetro de grabación
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            recordTime++
        }
    }

    // Limpieza de grabador al cerrar
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (_: Exception) {}
        }
    }

    // Lanzador para permisos de grabación de micrófono
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startAudioRecording(
                context = context,
                onStarted = { recorder, file ->
                    mediaRecorder = recorder
                    currentAudioFile = file
                    isRecording = true
                    recordTime = 0
                },
                onError = { err ->
                    Toast.makeText(context, "Error al iniciar grabación: $err", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Permiso de micrófono requerido para grabar nota de voz", Toast.LENGTH_SHORT).show()
        }
    }

    // Selector de Imagen
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val base64 = compressImageUriToBase64(context, uri)
            if (base64 != null) {
                attachmentUrl = base64
                attachmentName = "Imagen_Adjunta.jpg"
                attachmentType = "IMAGE"
            }
        }
    }

    // Selector de Documento PDF
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val bytes = inputStream.readBytes()
                    inputStream.close()
                    val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    attachmentUrl = "data:application/pdf;base64,$base64"
                    attachmentName = getFileNameFromUri(context, uri) ?: "Documento_Oficial.pdf"
                    attachmentType = "PDF"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo cargar el archivo PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(8.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, LimeGreen)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LimeGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "COMUNICADO ADMINISTRATIVO",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Fijado en Perfiles • 24 Horas",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Selector de Fondo de Color Dinámico
                Text("1. Fondo y Estilo Dinámico:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AnnouncementTheme.values().forEach { theme ->
                        val isSelected = selectedTheme == theme
                        val themeColor = Color(theme.accentColor)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(theme.topGradientColor), Color(theme.bottomGradientColor))
                                    )
                                )
                                .border(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) themeColor else SurfaceBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTheme = theme }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(theme.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = theme.title.split(" ").first(),
                                    color = if (isSelected) themeColor else TextLightGray,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Mensaje de Texto
                Text("2. Mensaje o Comunicado:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp),
                    placeholder = { Text("Escribe las indicaciones, horarios, avisos de pago o anuncios...", color = TextMuted, fontSize = 12.sp) },
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

                // 3. Grabador de Nota de Voz
                Text("3. Nota de Voz (Audio):", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    if (audioUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = LimeGreen)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Nota de voz grabada (${audioDurationSeconds}s)", color = Color.White, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                audioUrl = ""
                                audioDurationSeconds = 0
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Eliminar audio", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isRecording) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = Color(0xFFEF4444))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Grabando nota de voz: ${recordTime}s", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {
                                        try {
                                            mediaRecorder?.stop()
                                            mediaRecorder?.release()
                                            mediaRecorder = null
                                            isRecording = false
                                            audioDurationSeconds = recordTime
                                            val file = currentAudioFile
                                            if (file != null && file.exists()) {
                                                val bytes = FileInputStream(file).use { it.readBytes() }
                                                audioUrl = "data:audio/mp4;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                                                Toast.makeText(context, "Nota de voz lista", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            isRecording = false
                                            Toast.makeText(context, "Error al guardar nota de voz", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DETENER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text("Puedes adjuntar una nota de voz", color = TextMuted, fontSize = 11.5.sp)
                                Button(
                                    onClick = {
                                        val hasPermission = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.RECORD_AUDIO
                                        ) == PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            startAudioRecording(
                                                context = context,
                                                onStarted = { recorder, file ->
                                                    mediaRecorder = recorder
                                                    currentAudioFile = file
                                                    isRecording = true
                                                    recordTime = 0
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        } else {
                                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("GRABAR AUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Adjuntar Archivos (Imágenes o PDFs)
                Text("4. Archivos Adjuntos (Opcional):", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                if (attachmentUrl.isNotBlank()) {
                    if (attachmentType == "IMAGE") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, LimeGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("FOTO ADJUNTA (VISTA PREVIA)", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = {
                                        attachmentUrl = ""
                                        attachmentName = ""
                                        attachmentType = "NONE"
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Quitar foto", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            ) {
                                PostMediaImage(
                                    mediaUrl = attachmentUrl,
                                    contentDescription = "Vista previa de la imagen adjunta",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = attachmentName.ifBlank { "Imagen_Adjunta.jpg" },
                                    color = TextLightGray,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("✅ Decodificación verificada", color = LimeGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceElevated)
                                .border(1.dp, LimeGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = attachmentName,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                            IconButton(onClick = {
                                attachmentUrl = ""
                                attachmentName = ""
                                attachmentType = "NONE"
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar adjunto", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                imagePickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ IMAGEN", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ PDF", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Modalidad de Publicación: Inmediata vs Programada
                Text("5. Modo de Emisión:", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                var isScheduledMode by remember { mutableStateOf(false) }

                // Estados de Fecha Abierta y Hora/Minuto/Segundo Exactos
                val nowCal = remember {
                    Calendar.getInstance().apply {
                        add(Calendar.MINUTE, 5) // Inicialmente 5 minutos adelante
                    }
                }
                var scheduledYear by remember { mutableIntStateOf(nowCal.get(Calendar.YEAR)) }
                var scheduledMonth by remember { mutableIntStateOf(nowCal.get(Calendar.MONTH)) }
                var scheduledDay by remember { mutableIntStateOf(nowCal.get(Calendar.DAY_OF_MONTH)) }
                var scheduledHour by remember { mutableIntStateOf(nowCal.get(Calendar.HOUR_OF_DAY)) }
                var scheduledMinute by remember { mutableIntStateOf(nowCal.get(Calendar.MINUTE)) }
                var scheduledSecond by remember { mutableIntStateOf(0) }
                var scheduledDurationHours by remember { mutableIntStateOf(24) } // 24, 48, 72, 168

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isScheduledMode) LimeGreen.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (!isScheduledMode) LimeGreen else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { isScheduledMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "⚡ PUBLICAR AHORA",
                            color = if (!isScheduledMode) LimeGreen else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isScheduledMode) Color(0xFFF59E0B).copy(alpha = 0.2f) else SurfaceElevated)
                            .border(1.dp, if (isScheduledMode) Color(0xFFF59E0B) else SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable { isScheduledMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "📅 PROGRAMAR FECHA/HORA",
                            color = if (isScheduledMode) Color(0xFFF59E0B) else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                if (isScheduledMode) {
                    val currentTargetCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, scheduledYear)
                        set(Calendar.MONTH, scheduledMonth)
                        set(Calendar.DAY_OF_MONTH, scheduledDay)
                        set(Calendar.HOUR_OF_DAY, scheduledHour)
                        set(Calendar.MINUTE, scheduledMinute)
                        set(Calendar.SECOND, scheduledSecond)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val isScheduleInFuture = currentTargetCal.timeInMillis > System.currentTimeMillis()
                    val formattedDateText = remember(scheduledYear, scheduledMonth, scheduledDay) {
                        val cal = Calendar.getInstance().apply { set(scheduledYear, scheduledMonth, scheduledDay) }
                        SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES")).format(cal.time)
                            .replaceFirstChar { it.uppercase() }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            // Selector de Fecha Abierta (Cualquier día, mes y año)
                            Text("Día de Emisión (Fecha Abierta):", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                scheduledYear = y
                                                scheduledMonth = m
                                                scheduledDay = d
                                            },
                                            scheduledYear,
                                            scheduledMonth,
                                            scheduledDay
                                        ).apply {
                                            datePicker.minDate = System.currentTimeMillis() - 1000
                                        }.show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formattedDateText,
                                        color = Color.White,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("ELEGIR 📅", color = Color(0xFFF59E0B), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Accesos rápidos de fecha
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    0 to "Hoy",
                                    1 to "Mañana",
                                    2 to "+2 días",
                                    7 to "+1 sem"
                                ).forEach { (offsetDays, label) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                val c = Calendar.getInstance()
                                                if (offsetDays > 0) c.add(Calendar.DAY_OF_YEAR, offsetDays)
                                                scheduledYear = c.get(Calendar.YEAR)
                                                scheduledMonth = c.get(Calendar.MONTH)
                                                scheduledDay = c.get(Calendar.DAY_OF_MONTH)
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = TextLightGray, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Selector de Hora, Minutos y Segundos Exactos
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Hora, Minutos y Segundos Exactos:", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                        .clickable {
                                            TimePickerDialog(
                                                context,
                                                { _, h, m ->
                                                    scheduledHour = h
                                                    scheduledMinute = m
                                                },
                                                scheduledHour,
                                                scheduledMinute,
                                                true
                                            ).show()
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("⏱️ RELOJ", color = Color(0xFFF59E0B), fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // 3 Controles Numéricos Interactivos (Horas, Minutos, Segundos)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // HORA (00 - 23)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { scheduledHour = (scheduledHour + 1) % 24 },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir hora", tint = Color(0xFFF59E0B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(width = 46.dp, height = 36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%02d", scheduledHour),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    IconButton(
                                        onClick = { scheduledHour = if (scheduledHour - 1 < 0) 23 else scheduledHour - 1 },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar hora", tint = Color(0xFFF59E0B))
                                    }
                                    Text("HORA", color = TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(":", color = Color(0xFFF59E0B), fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))

                                // MINUTOS (00 - 59)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { scheduledMinute = (scheduledMinute + 1) % 60 },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir minuto", tint = Color(0xFFF59E0B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(width = 46.dp, height = 36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%02d", scheduledMinute),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    IconButton(
                                        onClick = { scheduledMinute = if (scheduledMinute - 1 < 0) 59 else scheduledMinute - 1 },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar minuto", tint = Color(0xFFF59E0B))
                                    }
                                    Text("MIN", color = TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(":", color = Color(0xFFF59E0B), fontSize = 20.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 8.dp))

                                // SEGUNDOS (00 - 59)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    IconButton(
                                        onClick = { scheduledSecond = (scheduledSecond + 1) % 60 },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Subir segundo", tint = Color(0xFFF59E0B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(width = 46.dp, height = 36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format(Locale.US, "%02d", scheduledSecond),
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                    IconButton(
                                        onClick = { scheduledSecond = if (scheduledSecond - 1 < 0) 59 else scheduledSecond - 1 },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Bajar segundo", tint = Color(0xFFF59E0B))
                                    }
                                    Text("SEG", color = TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Atajos rápidos de tiempo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    5 to "+5m",
                                    15 to "+15m",
                                    30 to "+30m",
                                    60 to "+1h"
                                ).forEach { (addMinutes, label) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SurfaceDark)
                                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                            .clickable {
                                                val c = Calendar.getInstance().apply {
                                                    set(Calendar.YEAR, scheduledYear)
                                                    set(Calendar.MONTH, scheduledMonth)
                                                    set(Calendar.DAY_OF_MONTH, scheduledDay)
                                                    set(Calendar.HOUR_OF_DAY, scheduledHour)
                                                    set(Calendar.MINUTE, scheduledMinute)
                                                    set(Calendar.SECOND, scheduledSecond)
                                                    add(Calendar.MINUTE, addMinutes)
                                                }
                                                scheduledYear = c.get(Calendar.YEAR)
                                                scheduledMonth = c.get(Calendar.MONTH)
                                                scheduledDay = c.get(Calendar.DAY_OF_MONTH)
                                                scheduledHour = c.get(Calendar.HOUR_OF_DAY)
                                                scheduledMinute = c.get(Calendar.MINUTE)
                                                scheduledSecond = c.get(Calendar.SECOND)
                                            }
                                            .padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(label, color = TextLightGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Vigencia activa tras publicarse (Mantiene las opciones favoritas del usuario)
                            Text("Vigencia activa tras publicarse:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    24 to "24 Horas",
                                    48 to "48 Horas",
                                    72 to "3 Días",
                                    168 to "7 Días"
                                ).forEach { (hours, label) ->
                                    val isSelected = scheduledDurationHours == hours
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) LimeGreen.copy(alpha = 0.25f) else SurfaceDark)
                                            .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                                            .clickable { scheduledDurationHours = hours }
                                            .padding(vertical = 5.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            color = if (isSelected) LimeGreen else TextLightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mensaje Informativo de Caducidad y Programación en tiempo real
                if (isScheduledMode) {
                    val currentTargetCal = Calendar.getInstance().apply {
                        set(Calendar.YEAR, scheduledYear)
                        set(Calendar.MONTH, scheduledMonth)
                        set(Calendar.DAY_OF_MONTH, scheduledDay)
                        set(Calendar.HOUR_OF_DAY, scheduledHour)
                        set(Calendar.MINUTE, scheduledMinute)
                        set(Calendar.SECOND, scheduledSecond)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val isScheduleInFuture = currentTargetCal.timeInMillis > System.currentTimeMillis()
                    val targetFormatted = SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm:ss", Locale.getDefault()).format(currentTargetCal.time)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isScheduleInFuture) Color(0xFFF59E0B).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.dp, if (isScheduleInFuture) Color(0xFFF59E0B) else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "📅 Emisión Programada: $targetFormatted",
                                color = if (isScheduleInFuture) Color(0xFFF59E0B) else Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isScheduleInFuture) {
                                    "⏱️ Se publicará automáticamente en el segundo exacto seleccionado y se mantendrá fijado durante $scheduledDurationHours horas."
                                } else {
                                    "⚠️ La fecha y hora seleccionada ya pasaron en el reloj. Ajusta los selectores a un momento futuro."
                                },
                                color = Color.White,
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "⏱️ Regla de Vigencia: Este comunicado se emitirá de inmediato, expirará automáticamente a las 24 horas y se sincronizará en tiempo real en todos los perfiles.",
                            color = TextMuted,
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Botones de Acción
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
                        Text("CANCELAR", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (textMessage.isBlank() && audioUrl.isBlank() && attachmentUrl.isBlank()) {
                                Toast.makeText(context, "Por favor incluye un mensaje, nota de voz o adjunto", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val publishTime: Long
                            val expiresAt: Long
                            if (isScheduledMode) {
                                val targetCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, scheduledYear)
                                    set(Calendar.MONTH, scheduledMonth)
                                    set(Calendar.DAY_OF_MONTH, scheduledDay)
                                    set(Calendar.HOUR_OF_DAY, scheduledHour)
                                    set(Calendar.MINUTE, scheduledMinute)
                                    set(Calendar.SECOND, scheduledSecond)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                if (targetCal.timeInMillis <= System.currentTimeMillis()) {
                                    Toast.makeText(context, "⚠️ Por favor selecciona una fecha y hora futura para programar la emisión", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                publishTime = targetCal.timeInMillis
                                expiresAt = publishTime + (scheduledDurationHours * 60 * 60 * 1000L)
                            } else {
                                publishTime = System.currentTimeMillis()
                                expiresAt = publishTime + (24 * 60 * 60 * 1000L)
                            }

                            val announcement = AdminAnnouncement(
                                authorName = currentAdminName.ifBlank { "Alex Gómez" },
                                contentText = textMessage.trim(),
                                theme = selectedTheme,
                                audioUrl = audioUrl,
                                audioDurationSeconds = audioDurationSeconds,
                                attachmentUrl = attachmentUrl,
                                attachmentName = attachmentName,
                                attachmentType = attachmentType,
                                timestamp = System.currentTimeMillis(),
                                scheduledPublishTime = publishTime,
                                expiresAt = expiresAt
                            )
                            onPublishAnnouncement(announcement)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScheduledMode) Color(0xFFF59E0B) else LimeGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (isScheduledMode) "PROGRAMAR EMISIÓN" else "PUBLICAR (24H)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

/**
 * Diálogo para administrar la bandeja de comunicados oficiales programados a futuro.
 * Permite al administrador revisar, adelantar la emisión ("Publicar Ahora") o cancelar comunicados futuros.
 */
@Composable
fun ScheduledAnnouncementsDialog(
    scheduledAnnouncements: List<AdminAnnouncement>,
    onDismiss: () -> Unit,
    onPublishNow: (AdminAnnouncement) -> Unit,
    onDeleteScheduled: (AdminAnnouncement) -> Unit
) {
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
                        .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                text = "COMUNICADOS PROGRAMADOS",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${scheduledAnnouncements.size} programados para emisión automática",
                                color = Color(0xFFF59E0B),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                if (scheduledAnnouncements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📅", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No hay comunicados programados",
                                color = TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Puedes programar anuncios para que el sistema los emita y fije automáticamente en semanas o días futuros.",
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(scheduledAnnouncements, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDark)
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "📅 ${item.scheduledDateString}",
                                                color = Color(0xFFF59E0B),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.5.sp
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SurfaceElevated)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = item.theme.title,
                                                color = TextLightGray,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (item.contentText.isNotBlank()) {
                                        Text(
                                            text = item.contentText,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            maxLines = 4,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (item.audioUrl.isNotBlank() || item.attachmentUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            if (item.audioUrl.isNotBlank()) {
                                                Text("🎙️ Con audio de voz", color = LimeGreen, fontSize = 10.sp)
                                            }
                                            if (item.attachmentUrl.isNotBlank()) {
                                                Text(if (item.attachmentType == "IMAGE") "📷 Con fotografía adjunta" else "📎 Con documento adjunto", color = Color(0xFF38BDF8), fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    if (item.attachmentType == "IMAGE" && item.attachmentUrl.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(95.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                                        ) {
                                            PostMediaImage(
                                                mediaUrl = item.attachmentUrl,
                                                contentDescription = "Foto adjunta",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Divider(color = SurfaceBorder, thickness = 0.5.dp)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onDeleteScheduled(item) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("CANCELAR", fontSize = 10.sp)
                                        }

                                        Button(
                                            onClick = { onPublishNow(item) },
                                            modifier = Modifier.weight(1.3f),
                                            colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("PUBLICAR YA", fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helpers de Audio y Archivos
private fun startAudioRecording(
    context: Context,
    onStarted: (MediaRecorder, File) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val cacheFile = File(context.cacheDir, "admin_voice_note_${System.currentTimeMillis()}.mp4")
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        recorder.setOutputFile(cacheFile.absolutePath)
        recorder.prepare()
        recorder.start()
        onStarted(recorder, cacheFile)
    } catch (e: Exception) {
        onError(e.message ?: "Error desconocido al iniciar grabación")
    }
}

private fun getAudioFileFromSource(context: Context, audioSource: String): File? {
    return try {
        if (audioSource.startsWith("data:audio")) {
            val base64 = audioSource.substringAfter(",")
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val tempFile = File(context.cacheDir, "play_voice_note.mp4")
            FileOutputStream(tempFile).use { it.write(bytes) }
            tempFile
        } else if (audioSource.startsWith("/")) {
            File(audioSource)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun openAttachment(context: Context, attachmentUrl: String, mimeType: String) {
    try {
        if (attachmentUrl.startsWith("data:application/pdf")) {
            val base64 = attachmentUrl.substringAfter(",")
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val pdfFile = File(context.cacheDir, "comunicado_oficial.pdf")
            FileOutputStream(pdfFile).use { it.write(bytes) }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachmentUrl))
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "No se encontró aplicación para abrir este archivo", Toast.LENGTH_SHORT).show()
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex != -1) {
                cursor.getString(nameIndex)
            } else null
        }
    } catch (_: Exception) {
        null
    }
}

private fun compressImageUriToBase64(context: Context, uri: Uri): String? {
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
        val options = android.graphics.BitmapFactory.Options().apply {
            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
        }
        val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()
        if (originalBitmap == null) return null

        val maxDimension = 1280
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scale = if (width > height) {
            maxDimension.toFloat() / width
        } else {
            maxDimension.toFloat() / height
        }

        val scaledBitmap = if (scale < 1.0f) {
            android.graphics.Bitmap.createScaledBitmap(
                originalBitmap,
                (width * scale).toInt().coerceAtLeast(1),
                (height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            originalBitmap
        }

        val rotatedBitmap = if (rotationDegrees != 0f) {
            val matrix = android.graphics.Matrix().apply { postRotate(rotationDegrees) }
            android.graphics.Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
        } else {
            scaledBitmap
        }

        // SOLUCIÓN PERMANENTE PARA FOTOS OSCURAS:
        // Dibujamos sobre lienzo con fondo blanco sólido para prevenir que la transparencia (alfa)
        // se transforme en fondo negro al comprimir en formato JPEG:
        val cleanBitmap = android.graphics.Bitmap.createBitmap(
            rotatedBitmap.width,
            rotatedBitmap.height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(cleanBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(rotatedBitmap, 0f, 0f, null)

        val outputStream = java.io.ByteArrayOutputStream()
        cleanBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 88, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (_: Exception) {
        null
    }
}
