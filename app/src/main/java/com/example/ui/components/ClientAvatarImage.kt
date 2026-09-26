package com.example.ui.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceElevated

/**
 * Componente unificado para renderizar avatares de atletas en tiempo real.
 * Soporta de forma transparente:
 * 1. Cadenas Base64 / Data URIs ("data:image/jpeg;base64,...") decodificadas a Bitmap
 * 2. URLs remotas y URIs de contenido local (http, https, content://, file://)
 * 3. Fallback inteligente con las iniciales del atleta sobre fondo oscuro y acento verde lima.
 */
@Composable
fun ClientAvatarImage(
    avatarUrl: String,
    fullName: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = "Foto de perfil",
    fallbackColor: Color = LimeGreen,
    fallbackTextSize: TextUnit = 16.sp
) {
    val trimmedUrl = avatarUrl.trim()

    // 1. Detectar si es una imagen codificada en Base64 o Data URI
    val bitmap = remember(trimmedUrl) {
        if (trimmedUrl.startsWith("data:image") || (!trimmedUrl.startsWith("http") && !trimmedUrl.startsWith("content://") && !trimmedUrl.startsWith("file://") && trimmedUrl.length > 100)) {
            try {
                val cleanBase64 = if (trimmedUrl.contains(",")) {
                    trimmedUrl.substringAfter(",")
                } else {
                    trimmedUrl
                }
                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (_: Exception) {
                null
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            trimmedUrl.startsWith("http") || trimmedUrl.startsWith("content://") || trimmedUrl.startsWith("file://") -> {
                AsyncImage(
                    model = trimmedUrl,
                    contentDescription = contentDescription,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                // Fallback con iniciales del atleta
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = fullName.trim().split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .map { it.first().uppercaseChar() }
                        .joinToString("")
                        .ifBlank { fullName.take(2).uppercase().ifBlank { "AF" } }

                    Text(
                        text = initials,
                        color = fallbackColor,
                        fontSize = fallbackTextSize,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
