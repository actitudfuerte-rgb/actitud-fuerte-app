package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.manager.AppResourceManager
import com.example.data.model.AppResource
import com.example.data.model.ResourceType
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesHubDialog(
    isAdmin: Boolean,
    uploaderName: String = "Administrador Actitud Fuerte",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val resources by AppResourceManager.resources.collectAsState()

    var selectedFilter by remember { mutableStateOf<ResourceType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var resourceToDelete by remember { mutableStateOf<AppResource?>(null) }

    val filteredResources = remember(resources, selectedFilter, searchQuery) {
        resources.filter { item ->
            val matchesType = selectedFilter == null || item.type == selectedFilter
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.category.contains(searchQuery, ignoreCase = true)
            matchesType && matchesSearch
        }
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
                // Barra Superior
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = TextLightGray)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📁", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CENTRO DE RECURSOS",
                                    color = LimeGreen,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = if (isAdmin) "Descargas de atletas y carga administrativa" else "Material oficial, audios y guías descargables",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (isAdmin) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LimeGreen)
                                .clickable { showUploadDialog = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SUBIR", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                // Buscador y Filtros Rápidos
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar guías, audios, técnica o normativas...", color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            ResourceFilterChip(
                                label = "Todos (${resources.size})",
                                isSelected = selectedFilter == null,
                                onClick = { selectedFilter = null }
                            )
                        }
                        item {
                            ResourceFilterChip(
                                label = "📄 PDFs & Guías (${resources.count { it.type == ResourceType.PDF }})",
                                isSelected = selectedFilter == ResourceType.PDF,
                                onClick = { selectedFilter = ResourceType.PDF }
                            )
                        }
                        item {
                            ResourceFilterChip(
                                label = "🎧 Audios (${resources.count { it.type == ResourceType.AUDIO }})",
                                isSelected = selectedFilter == ResourceType.AUDIO,
                                onClick = { selectedFilter = ResourceType.AUDIO }
                            )
                        }
                        item {
                            ResourceFilterChip(
                                label = "🎥 Videos (${resources.count { it.type == ResourceType.VIDEO }})",
                                isSelected = selectedFilter == ResourceType.VIDEO,
                                onClick = { selectedFilter = ResourceType.VIDEO }
                            )
                        }
                    }
                }

                // Lista de Recursos Disponibles
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Banner informativo de la Guía Oficial y descargas
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF132215)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(LimeGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "MATERIAL OFICIAL Y EDUCATIVO",
                                        color = LimeGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Puedes descargar los archivos directamente en la memoria de tu teléfono o abrirlos inmediatamente en el lector.",
                                        color = TextLightGray,
                                        fontSize = 10.5.sp
                                    )
                                }
                            }
                        }
                    }

                    if (filteredResources.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No se encontraron recursos en esta categoría", color = TextMuted, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(filteredResources, key = { it.id }) { resource ->
                            ResourceItemCard(
                                resource = resource,
                                isAdmin = isAdmin,
                                onDownload = {
                                    val (success, message) = AppResourceManager.downloadResourceToDevice(context, resource)
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                },
                                onOpen = {
                                    AppResourceManager.openResource(context, resource)
                                },
                                onDelete = {
                                    resourceToDelete = resource
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(30.dp))
                    }
                }
            }
        }
    }

    // Modal para Subir Archivo (Administrador)
    if (showUploadDialog) {
        UploadResourceDialog(
            defaultAuthor = uploaderName,
            onDismiss = { showUploadDialog = false },
            onUpload = { title, desc, cat, type, uri ->
                coroutineScope.launch {
                    val success = AppResourceManager.addResource(
                        context = context,
                        title = title,
                        description = desc,
                        category = cat,
                        type = type,
                        sourceUri = uri,
                        author = uploaderName
                    )
                    if (success) {
                        Toast.makeText(context, "✅ Recurso publicado exitosamente", Toast.LENGTH_SHORT).show()
                        showUploadDialog = false
                    } else {
                        Toast.makeText(context, "❌ Error al subir el recurso", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // Confirmación de Borrado (Admin)
    if (resourceToDelete != null) {
        AlertDialog(
            onDismissRequest = { resourceToDelete = null },
            title = { Text("Eliminar Recurso", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar permanentemente \"${resourceToDelete?.title}\"? Esta acción no se puede deshacer.",
                    color = TextLightGray
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = resourceToDelete
                        resourceToDelete = null
                        if (target != null) {
                            coroutineScope.launch {
                                val ok = AppResourceManager.deleteResource(context, target.id)
                                if (ok) {
                                    Toast.makeText(context, "Recurso eliminado", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No se pudo eliminar el recurso", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { resourceToDelete = null }) {
                    Text("Cancelar", color = TextLightGray)
                }
            },
            containerColor = SurfaceDark,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun ResourceItemCard(
    resource: AppResource,
    isAdmin: Boolean,
    onDownload: () -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val (typeColor, typeIcon, typeLabel) = when (resource.type) {
        ResourceType.PDF -> Triple(Color(0xFFEF4444), Icons.Default.PictureAsPdf, "PDF")
        ResourceType.AUDIO -> Triple(Color(0xFFA855F7), Icons.Default.Headphones, "AUDIO")
        ResourceType.VIDEO -> Triple(Color(0xFF00F0FF), Icons.Default.Movie, "VIDEO")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (resource.isOfficialGuide) LimeGreen.copy(alpha = 0.8f) else SurfaceBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Tipo, Categoría e Indicador Oficial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .border(1.dp, typeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(typeIcon, contentDescription = null, tint = typeColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(typeLabel, color = typeColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(resource.category, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (resource.isOfficialGuide) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LimeGreen.copy(alpha = 0.2f))
                            .border(1.dp, LimeGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text("⭐ OFICIAL AF", color = LimeGreen, fontSize = 8.5.sp, fontWeight = FontWeight.Black)
                    }
                } else if (isAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título
            Text(
                text = resource.title,
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (resource.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resource.description,
                    color = TextLightGray,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Metadatos: Tamaño y Autor
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📦 ${resource.fileSizeFormatted}  •  ${resource.author}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
                Text(
                    text = resource.dateFormatted,
                    color = TextMuted,
                    fontSize = 9.5.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones de Acción: Descargar en Teléfono y Abrir/Ver
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Descargar
                Button(
                    onClick = onDownload,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LimeGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("DESCARGAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                // Botón Abrir / Ver
                OutlinedButton(
                    onClick = onOpen,
                    modifier = Modifier.weight(0.9f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    Icon(
                        if (resource.type == ResourceType.VIDEO || resource.type == ResourceType.AUDIO) Icons.Default.PlayArrow else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = typeColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (resource.type == ResourceType.PDF) "ABRIR" else "REPRODUCIR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LimeGreen else SurfaceElevated)
            .border(
                1.dp,
                if (isSelected) LimeGreen else SurfaceBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextLightGray,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
        )
    }
}

@Composable
fun UploadResourceDialog(
    defaultAuthor: String,
    onDismiss: () -> Unit,
    onUpload: (title: String, description: String, category: String, type: ResourceType, fileUri: Uri) -> Unit
) {
    var selectedType by remember { mutableStateOf(ResourceType.PDF) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Guías & Protocolos") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            selectedFileName = uri.lastPathSegment ?: "archivo_seleccionado"
        }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .background(Color.Transparent),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LimeGreen.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SUBIR NUEVO RECURSO", color = LimeGreen, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("Panel Administrativo Actitud Fuerte", color = TextMuted, fontSize = 10.5.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .clickable { if (!isUploading) onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = TextLightGray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Tipo de Archivo (PDF, AUDIO, VIDEO)
                Text("TIPO DE ARCHIVO:", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf(
                        Triple(ResourceType.PDF, "📄 PDF", Color(0xFFEF4444)),
                        Triple(ResourceType.AUDIO, "🎧 Audio", Color(0xFFA855F7)),
                        Triple(ResourceType.VIDEO, "🎥 Video", Color(0xFF00F0FF))
                    )
                    types.forEach { (type, label, col) ->
                        val isSelected = selectedType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) col.copy(alpha = 0.25f) else SurfaceElevated)
                                .border(1.2.dp, if (isSelected) col else SurfaceBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedType = type
                                    selectedFileUri = null
                                    selectedFileName = null
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón Selección de Archivo
                val mimeType = when (selectedType) {
                    ResourceType.PDF -> "application/pdf"
                    ResourceType.AUDIO -> "audio/*"
                    ResourceType.VIDEO -> "video/*"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .border(
                            1.dp,
                            if (selectedFileUri != null) LimeGreen else SurfaceBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            filePickerLauncher.launch(mimeType)
                        }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (selectedFileUri != null) LimeGreen else TextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (selectedFileUri != null) "Archivo seleccionado:" else "Toca para elegir archivo ($mimeType)",
                                color = if (selectedFileUri != null) LimeGreen else TextLightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedFileName ?: "Formatos permitidos según tipo",
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campos de Entrada
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título del recurso", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categoría
                Text("Categoría:", color = TextLightGray, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val categories = listOf("Guías & Protocolos", "Técnica & Fuerza", "Nutrición & Hábitos", "Podcast & Motivación", "Normativas")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (category == cat) LimeGreen.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(1.dp, if (category == cat) LimeGreen else SurfaceBorder, RoundedCornerShape(6.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (category == cat) LimeGreen else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción o instrucciones para el atleta", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        val uri = selectedFileUri
                        if (uri == null) {
                            return@Button
                        }
                        if (title.isBlank()) {
                            return@Button
                        }
                        isUploading = true
                        onUpload(title, description, category, selectedType, uri)
                    },
                    enabled = selectedFileUri != null && title.isNotBlank() && !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LimeGreen,
                        contentColor = Color.Black,
                        disabledContainerColor = SurfaceElevated,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PROCESANDO...", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PUBLICAR RECURSO EN EL SISTEMA", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
