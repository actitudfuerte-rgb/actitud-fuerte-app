package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.screens.getPostBackgroundBrush
import com.example.ui.theme.LimeGreen

/**
 * Visualizador multimedia reactivo y multiplataforma para las publicaciones y feed.
 * Soporta de manera transparente:
 * 1. Data URIs en Base64 (data:image/...) evitando pantallas negras en ordenadores y web.
 * 2. URIs nativos de Android y URLs remotas de Cloud Storage / Firestore.
 * 3. Fondos y gradientes temáticos predefinidos cuando no hay fotografía cargada.
 */
@Composable
fun PostMediaImage(
    mediaUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (mediaUrl.isBlank()) {
        Box(
            modifier = modifier.background(Color(0xFF141913)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(36.dp))
        }
        return
    }

    val isBase64 = mediaUrl.startsWith("data:image") || (mediaUrl.length > 200 && !mediaUrl.startsWith("http") && !mediaUrl.startsWith("content://") && !mediaUrl.startsWith("file://"))

    if (isBase64) {
        val bitmap = remember(mediaUrl) {
            try {
                val cleanBase64 = if (mediaUrl.contains(",")) {
                    mediaUrl.substringAfter(",")
                } else {
                    mediaUrl
                }
                val decodedBytes = android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT)
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
            } catch (e: Exception) {
                null
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            Box(
                modifier = modifier.background(Color(0xFF141913)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(36.dp))
            }
        }
    } else if (mediaUrl.startsWith("content://") || mediaUrl.startsWith("file://") || mediaUrl.startsWith("http")) {
        AsyncImage(
            model = mediaUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    } else {
        // Estilo preestablecido o gradiente deportivo
        Box(
            modifier = modifier.background(getPostBackgroundBrush(mediaUrl)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen.copy(alpha = 0.8f), modifier = Modifier.size(42.dp))
        }
    }
}
