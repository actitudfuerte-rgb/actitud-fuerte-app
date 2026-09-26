package com.example.data.manager

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AppResource
import com.example.data.model.ResourceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object AppResourceManager {

    private const val TAG = "AppResourceManager"
    private const val OFFICIAL_GUIDE_FILENAME = "Guia_Primer_Ingreso_y_Beneficios_Actitud_Fuerte.pdf"

    private val _resources = MutableStateFlow<List<AppResource>>(emptyList())
    val resources: StateFlow<List<AppResource>> = _resources.asStateFlow()

    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resourcesDir = File(context.filesDir, "resources").apply { mkdirs() }
                val guideFile = File(resourcesDir, OFFICIAL_GUIDE_FILENAME)

                // Asegurar que la guía oficial en PDF esté presente físicamente en storage interno
                if (!guideFile.exists() || guideFile.length() == 0L) {
                    copyOfficialGuide(context, guideFile)
                }

                loadResources(context, resourcesDir, guideFile)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing AppResourceManager", e)
            }
        }
    }

    private fun copyOfficialGuide(context: Context, targetFile: File) {
        try {
            // Intentar cargar desde assets
            val assetManager = context.assets
            val hasAsset = try {
                assetManager.open(OFFICIAL_GUIDE_FILENAME).use { true }
            } catch (e: Exception) {
                false
            }

            if (hasAsset) {
                assetManager.open(OFFICIAL_GUIDE_FILENAME).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                // Fallback: Si no está en assets, verificar si existe en la raíz o public
                val localRoot = File("/Guia_Primer_Ingreso_y_Beneficios_Actitud_Fuerte.pdf")
                val localPublic = File("/public/Guia_Primer_Ingreso_y_Beneficios_Actitud_Fuerte.pdf")
                val src = when {
                    localRoot.exists() -> localRoot
                    localPublic.exists() -> localPublic
                    else -> null
                }
                if (src != null) {
                    src.inputStream().use { input ->
                        FileOutputStream(targetFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying official guide", e)
        }
    }

    private fun loadResources(context: Context, resourcesDir: File, guideFile: File) {
        val manifestFile = File(resourcesDir, "manifest.json")
        val list = mutableListOf<AppResource>()

        val defaultGuide = AppResource(
            id = "official_guide_af",
            title = "Guía de Primer Ingreso y Beneficios Actitud Fuerte",
            description = "Manual oficial completo de bienvenida, normativas de bioseguridad, horarios, metodología de entrenamiento y beneficios del atleta.",
            type = ResourceType.PDF,
            fileName = OFFICIAL_GUIDE_FILENAME,
            filePath = guideFile.absolutePath,
            fileSizeBytes = if (guideFile.exists()) guideFile.length() else 21500L,
            fileSizeFormatted = formatFileSize(if (guideFile.exists()) guideFile.length() else 21500L),
            category = "Guías Oficiales",
            author = "Dirección Actitud Fuerte",
            dateFormatted = "Oficial 2026",
            isOfficialGuide = true
        )

        if (manifestFile.exists()) {
            try {
                val jsonStr = manifestFile.readText()
                val array = JSONArray(jsonStr)
                var hasOfficial = false
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val isOfficial = obj.optBoolean("isOfficialGuide", false)
                    if (isOfficial) hasOfficial = true
                    list.add(
                        AppResource(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            description = obj.optString("description", ""),
                            type = ResourceType.valueOf(obj.optString("type", "PDF")),
                            fileName = obj.getString("fileName"),
                            filePath = obj.getString("filePath"),
                            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                            fileSizeFormatted = obj.optString("fileSizeFormatted", "0 KB"),
                            category = obj.optString("category", "General"),
                            author = obj.optString("author", "Administrador"),
                            dateFormatted = obj.optString("dateFormatted", "2026"),
                            isOfficialGuide = isOfficial
                        )
                    )
                }
                if (!hasOfficial) {
                    list.add(0, defaultGuide)
                    saveManifest(manifestFile, list)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing manifest, loading default guide", e)
                list.clear()
                list.add(defaultGuide)
                saveManifest(manifestFile, list)
            }
        } else {
            list.add(defaultGuide)
            saveManifest(manifestFile, list)
        }

        _resources.value = list
    }

    private fun saveManifest(manifestFile: File, list: List<AppResource>) {
        try {
            val array = JSONArray()
            for (res in list) {
                val obj = JSONObject().apply {
                    put("id", res.id)
                    put("title", res.title)
                    put("description", res.description)
                    put("type", res.type.name)
                    put("fileName", res.fileName)
                    put("filePath", res.filePath)
                    put("fileSizeBytes", res.fileSizeBytes)
                    put("fileSizeFormatted", res.fileSizeFormatted)
                    put("category", res.category)
                    put("author", res.author)
                    put("dateFormatted", res.dateFormatted)
                    put("isOfficialGuide", res.isOfficialGuide)
                }
                array.put(obj)
            }
            manifestFile.writeText(array.toString(2))
        } catch (e: Exception) {
            Log.e(TAG, "Error saving manifest", e)
        }
    }

    suspend fun addResource(
        context: Context,
        title: String,
        description: String,
        category: String,
        type: ResourceType,
        sourceUri: Uri,
        author: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val resourcesDir = File(context.filesDir, "resources").apply { mkdirs() }
            val manifestFile = File(resourcesDir, "manifest.json")

            // Obtener nombre original del archivo
            var origName = "recurso_${System.currentTimeMillis()}"
            context.contentResolver.query(sourceUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    origName = cursor.getString(nameIndex) ?: origName
                }
            }

            // Normalizar extensión según el tipo
            val finalExt = when (type) {
                ResourceType.PDF -> if (origName.endsWith(".pdf", ignoreCase = true)) "" else ".pdf"
                ResourceType.AUDIO -> if (origName.contains(".")) "" else ".mp3"
                ResourceType.VIDEO -> if (origName.contains(".")) "" else ".mp4"
            }
            val sanitizedName = origName.replace(" ", "_") + finalExt
            val destFile = File(resourcesDir, "${System.currentTimeMillis()}_$sanitizedName")

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext false

            val sizeBytes = destFile.length()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val newResource = AppResource(
                id = UUID.randomUUID().toString(),
                title = title.ifBlank { origName },
                description = description,
                type = type,
                fileName = sanitizedName,
                filePath = destFile.absolutePath,
                fileSizeBytes = sizeBytes,
                fileSizeFormatted = formatFileSize(sizeBytes),
                category = category.ifBlank { "General" },
                author = author.ifBlank { "Administrador" },
                dateFormatted = dateFormat,
                isOfficialGuide = false
            )

            val currentList = _resources.value.toMutableList()
            currentList.add(1.coerceAtMost(currentList.size), newResource)
            _resources.value = currentList
            saveManifest(manifestFile, currentList)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding resource", e)
            false
        }
    }

    suspend fun deleteResource(context: Context, resourceId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val resourcesDir = File(context.filesDir, "resources")
            val manifestFile = File(resourcesDir, "manifest.json")
            val currentList = _resources.value.toMutableList()
            val target = currentList.firstOrNull { it.id == resourceId } ?: return@withContext false

            // No permitir borrar la guía oficial base (para preservar siempre el material oficial)
            if (target.isOfficialGuide) {
                return@withContext false
            }

            val file = File(target.filePath)
            if (file.exists()) {
                file.delete()
            }

            currentList.removeAll { it.id == resourceId }
            _resources.value = currentList
            saveManifest(manifestFile, currentList)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting resource", e)
            false
        }
    }

    fun downloadResourceToDevice(context: Context, resource: AppResource): Pair<Boolean, String> {
        return try {
            val sourceFile = File(resource.filePath)
            if (!sourceFile.exists() || sourceFile.length() == 0L) {
                // Si es la guía oficial y no está el archivo, regenerarlo
                if (resource.isOfficialGuide) {
                    copyOfficialGuide(context, sourceFile)
                }
                if (!sourceFile.exists()) {
                    return Pair(false, "El archivo fuente no se encuentra disponible")
                }
            }

            val mimeType = when (resource.type) {
                ResourceType.PDF -> "application/pdf"
                ResourceType.AUDIO -> "audio/*"
                ResourceType.VIDEO -> "video/*"
            }

            val displayName = resource.fileName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Pair(false, "No se pudo crear el archivo en Descargas")

                resolver.openOutputStream(uri)?.use { out ->
                    sourceFile.inputStream().use { input ->
                        input.copyTo(out)
                    }
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                Pair(true, "Guardado exitosamente en tu carpeta Descargas")
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                downloadsDir.mkdirs()
                val target = File(downloadsDir, displayName)
                sourceFile.copyTo(target, overwrite = true)
                Pair(true, "Guardado en Descargas: ${target.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading resource", e)
            Pair(false, "Error al descargar: ${e.localizedMessage}")
        }
    }

    fun openResource(context: Context, resource: AppResource) {
        try {
            val sourceFile = File(resource.filePath)
            if (!sourceFile.exists()) {
                if (resource.isOfficialGuide) {
                    copyOfficialGuide(context, sourceFile)
                }
            }

            if (!sourceFile.exists()) {
                Toast.makeText(context, "Archivo no disponible", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sourceFile
            )

            val mimeType = when (resource.type) {
                ResourceType.PDF -> "application/pdf"
                ResourceType.AUDIO -> "audio/*"
                ResourceType.VIDEO -> "video/*"
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir con"))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening resource", e)
            Toast.makeText(context, "No hay aplicación compatible instalada para abrir este archivo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0))
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
