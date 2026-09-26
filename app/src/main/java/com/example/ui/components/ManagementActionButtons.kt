package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LimeGreen
import java.util.Calendar

/**
 * Replicación exacta de los tres botones horizontales de gestión y comunicación.
 * Los tres botones mantienen dimensiones exactamente idénticas.
 * Incorpora sutiles toques de color armónicos para jerarquía visual sin saturar.
 * El botón "Liderazgo y Cercanía." se resalta automáticamente los días jueves.
 */
@Composable
fun ManagementActionButtons(
    onOpenBroadcast: () -> Unit,
    onOpenReports: () -> Unit,
    onOpenLeadership: () -> Unit,
    onOpenResources: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isThursday = remember {
        Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Difusión Masiva por WhatsApp
        ActionHorizontalCard(
            title = "Difusión Masiva por WhatsApp",
            subtitle = "Avisos de feriados, horarios y cuotas",
            icon = Icons.Default.Campaign,
            iconTint = Color(0xFF22C55E),
            iconBg = Color(0xFF16321F),
            onClick = onOpenBroadcast
        )

        // 2. Descargar Reportes (Excel / CSV)
        ActionHorizontalCard(
            title = "Descargar Reportes (Excel / CSV)",
            subtitle = "Socios, cobros, asistencias y balance",
            icon = Icons.Default.FileDownload,
            iconTint = Color(0xFF38BDF8),
            iconBg = Color(0xFF132A3B),
            onClick = onOpenReports
        )

        // 3. Liderazgo y Cercanía.
        ActionHorizontalCard(
            title = "Liderazgo y Cercanía.",
            subtitle = "Mensajes para saber cómo se sienten tus alumnos",
            icon = Icons.Default.ChatBubble,
            iconTint = if (isThursday) Color.Black else LimeGreen,
            iconBg = if (isThursday) LimeGreen else Color(0xFF263819),
            onClick = onOpenLeadership,
            isHighlighted = isThursday,
            highlightBadge = if (isThursday) "¡HOY!" else null
        )

        // 4. Recursos & Biblioteca Multimedia (PDFs, Audios, Videos)
        ActionHorizontalCard(
            title = "Recursos & Biblioteca Multimedia",
            subtitle = "Guías oficiales, audios de coaching y videos",
            icon = Icons.Default.FolderSpecial,
            iconTint = Color(0xFF00F0FF),
            iconBg = Color(0xFF10333E),
            onClick = onOpenResources
        )
    }
}

@Composable
fun ActionHorizontalCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    highlightBadge: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFF1E2616) else Color(0xFF141712)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isHighlighted) 2.dp else 1.dp,
            color = if (isHighlighted) LimeGreen else Color(0xFF2E3824)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular Badge con acento de color correspondiente
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(iconBg)
                        .border(1.dp, iconTint.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Textos Título y Subtítulo
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (highlightBadge != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LimeGreen)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = highlightBadge,
                                    color = Color.Black,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = if (isHighlighted) Color(0xFFE2EED3) else Color(0xFFC0C7B8),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Flecha Chevron Right
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (isHighlighted) LimeGreen else Color(0xFFD4DEC8),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
