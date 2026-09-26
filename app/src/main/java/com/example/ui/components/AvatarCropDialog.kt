package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
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
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Diálogo interactivo de encuadre y recorte de foto de perfil.
 * Permite mover (pan), ampliar (zoom) y rotar la fotografía para centrar el rostro
 * y visualizar con precisión el resultado en la máscara circular de la app.
 */
@Composable
fun AvatarCropDialog(
    sourceBitmap: Bitmap,
    onDismiss: () -> Unit,
    onCropConfirmed: (String) -> Unit
) {
    var zoomFactor by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var rotationDegrees by remember { mutableIntStateOf(0) }
    var containerSize by remember { mutableStateOf<IntSize>(IntSize.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LimeGreen)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(LimeGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Crop,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "ENCUADRE DE FOTO",
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                "Ajusta y centra tu rostro",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Área visual interactiva de encuadre
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF0D0F12))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .onSizeChanged { containerSize = it }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomFactor = (zoomFactor * zoom).coerceIn(1.0f, 4.0f)
                                panOffsetX += pan.x
                                panOffsetY += pan.y
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (containerSize.width > 0 && containerSize.height > 0) {
                            val w = size.width
                            val h = size.height
                            val circleDiameter = min(w, h) * 0.85f
                            val circleRadius = circleDiameter / 2f
                            val centerX = w / 2f
                            val centerY = h / 2f

                            // 1. Dibujar Bitmap transformado
                            val baseScale = max(
                                circleDiameter / sourceBitmap.width.toFloat(),
                                circleDiameter / sourceBitmap.height.toFloat()
                            )
                            val currentScale = baseScale * zoomFactor

                            val matrix = Matrix().apply {
                                postTranslate(-sourceBitmap.width / 2f, -sourceBitmap.height / 2f)
                                postRotate(rotationDegrees.toFloat())
                                postScale(currentScale, currentScale)
                                postTranslate(centerX + panOffsetX, centerY + panOffsetY)
                            }

                            drawContext.canvas.nativeCanvas.drawBitmap(
                                sourceBitmap,
                                matrix,
                                Paint().apply {
                                    isAntiAlias = true
                                    isFilterBitmap = true
                                }
                            )

                            // 2. Máscara oscura semitransparente exterior con EvenOdd (Garantiza foto 100% visible, clara y nítida en el encuadre)
                            val scrimPath = Path().apply {
                                fillType = PathFillType.EvenOdd
                                addRect(Rect(0f, 0f, size.width, size.height))
                                addOval(Rect(center = Offset(centerX, centerY), radius = circleRadius))
                            }
                            drawPath(
                                path = scrimPath,
                                color = Color.Black.copy(alpha = 0.65f)
                            )

                            // 3. Anillo de referencia del avatar en Verde Lima
                            drawCircle(
                                color = LimeGreen,
                                radius = circleRadius,
                                center = Offset(centerX, centerY),
                                style = Stroke(width = 3.dp.toPx())
                            )
                            // Cruces de guía sutiles en el centro
                            val guideLen = 14.dp.toPx()
                            drawLine(
                                color = LimeGreen.copy(alpha = 0.6f),
                                start = Offset(centerX - guideLen, centerY),
                                end = Offset(centerX + guideLen, centerY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = LimeGreen.copy(alpha = 0.6f),
                                start = Offset(centerX, centerY - guideLen),
                                end = Offset(centerX, centerY + guideLen),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Control de Zoom Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ZoomOut,
                        contentDescription = "Reducir zoom",
                        tint = TextLightGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = zoomFactor,
                        onValueChange = { zoomFactor = it },
                        valueRange = 1.0f..3.5f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = LimeGreen,
                            activeTrackColor = LimeGreen,
                            inactiveTrackColor = SurfaceElevated
                        )
                    )
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = "Aumentar zoom",
                        tint = LimeGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${(zoomFactor * 100).toInt()}%",
                        color = LimeGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(42.dp),
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de herramientas: Rotar y Centrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { rotationDegrees = (rotationDegrees + 90) % 360 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceElevated)
                    ) {
                        Icon(
                            Icons.Default.RotateRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Girar 90°", color = Color.White, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            zoomFactor = 1.0f
                            panOffsetX = 0f
                            panOffsetY = 0f
                            rotationDegrees = 0
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceElevated)
                    ) {
                        Icon(
                            Icons.Default.CenterFocusStrong,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Centrar", color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción principales
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Cancelar", color = TextLightGray, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val croppedBase64 = renderCroppedAvatar(
                                source = sourceBitmap,
                                zoom = zoomFactor,
                                panX = panOffsetX,
                                panY = panOffsetY,
                                rotation = rotationDegrees,
                                containerW = containerSize.width.toFloat(),
                                containerH = containerSize.height.toFloat()
                            )
                            if (croppedBase64 != null) {
                                onCropConfirmed(croppedBase64)
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aceptar Encuadre", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Renderiza el recorte exacto en un bitmap cuadrado de alta definición (640x640)
 * y lo convierte a Base64 con compresión JPEG 92%.
 */
private fun renderCroppedAvatar(
    source: Bitmap,
    zoom: Float,
    panX: Float,
    panY: Float,
    rotation: Int,
    containerW: Float,
    containerH: Float,
    outputSize: Int = 640
): String? {
    return try {
        val outBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(outBitmap)

        val w = if (containerW > 0) containerW else 800f
        val h = if (containerH > 0) containerH else 800f
        val circleDiameter = min(w, h) * 0.85f

        val baseScale = max(
            circleDiameter / source.width.toFloat(),
            circleDiameter / source.height.toFloat()
        )
        val currentScale = baseScale * zoom

        // Factor de escala entre la ventana de previsualización y el bitmap final
        val ratio = outputSize / circleDiameter

        val matrix = Matrix().apply {
            // Centrar el bitmap original
            postTranslate(-source.width / 2f, -source.height / 2f)
            // Rotar
            postRotate(rotation.toFloat())
            // Escalar según zoom del usuario
            postScale(currentScale, currentScale)
            // Mover según paneo del usuario
            postTranslate(panX, panY)
            // Escalar a la resolución de salida
            postScale(ratio, ratio)
            // Posicionar en el centro del nuevo bitmap de salida
            postTranslate(outputSize / 2f, outputSize / 2f)
        }

        val paint = Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
            isDither = true
        }
        canvas.drawBitmap(source, matrix, paint)

        val outStream = ByteArrayOutputStream()
        outBitmap.compress(Bitmap.CompressFormat.JPEG, 92, outStream)
        val bytes = outStream.toByteArray()
        "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
}
