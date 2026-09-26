package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Attendance
import com.example.data.model.AthleteTier
import com.example.data.model.Client
import com.example.data.model.idNumber
import com.example.ui.components.ClientAvatarImage
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

/**
 * DIRECTORIO GENERAL DE ATLETAS (HALL DE FICHAS TÉCNICAS JERÁRQUICAS)
 * Muestra las fichas técnicas en forma de rectángulos con 4 colores fluorescentes:
 * 1. Morado Fluorescente (Élite / Leyenda)
 * 2. Verde Lima Neón (Avanzado / Guerrero)
 * 3. Cyan Eléctrico (Intermedio / Activo)
 * 4. Naranja / Oro Fuego (Iniciado / Nueva Fuerza)
 * Cada ficha incluye foto, nombre, rango y el botón de acción "Ver perfil fit".
 */
@Composable
fun AthleteDirectoryScreen(
    clients: List<Client>,
    attendances: List<Attendance> = emptyList(),
    currentLoggedInEmail: String = "",
    onNavigateBack: (() -> Unit)? = null,
    onOpenProfile: (Client) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<AthleteTier?>(null) }

    // Conteo por jerarquía
    val eliteCount = remember(clients, attendances) {
        clients.count { AthleteTier.getTierForClient(it, attendances.count { a -> a.clientId == it.id }) == AthleteTier.ELITE }
    }
    val warriorCount = remember(clients, attendances) {
        clients.count { AthleteTier.getTierForClient(it, attendances.count { a -> a.clientId == it.id }) == AthleteTier.WARRIOR }
    }
    val intermediateCount = remember(clients, attendances) {
        clients.count { AthleteTier.getTierForClient(it, attendances.count { a -> a.clientId == it.id }) == AthleteTier.INTERMEDIATE }
    }
    val initiateCount = remember(clients, attendances) {
        clients.count { AthleteTier.getTierForClient(it, attendances.count { a -> a.clientId == it.id }) == AthleteTier.INITIATE }
    }

    // Filtrado de clientes
    val filteredClients = remember(clients, attendances, searchQuery, selectedTierFilter) {
        clients.filter { client ->
            val matchesQuery = searchQuery.isBlank() ||
                    client.fullName.contains(searchQuery, ignoreCase = true) ||
                    client.athleteAlias.contains(searchQuery, ignoreCase = true) ||
                    client.idNumber.contains(searchQuery, ignoreCase = true)

            val clientTier = AthleteTier.getTierForClient(
                client,
                attendances.count { it.clientId == client.id }
            )
            val matchesTier = selectedTierFilter == null || clientTier == selectedTierFilter

            matchesQuery && matchesTier
        }.sortedBy { client ->
            val tier = AthleteTier.getTierForClient(client, attendances.count { it.clientId == client.id })
            tier.rankOrder
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ENCABEZADO DE SALA Y AUTORIDAD VISUAL
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                Text(
                                    text = "DIRECTORIO DE ATLETAS",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "ALEX GÓMEZ • SALA DE MUSCULACIÓN",
                                    color = LimeGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, LimeGreen.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "${clients.size} ATLETAS",
                                color = LimeGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Roster oficial de atletas de la sala. Consulta las fichas técnicas y accede a sus perfiles fit oficiales.",
                        color = TextLightGray,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // RESUMEN DE LOS 4 NIVELES
        item {
            Column {
                Text(
                    text = "JERARQUÍA DE ATLETAS",
                    color = TextMuted,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TierMetricPill(
                        tier = AthleteTier.ELITE,
                        count = eliteCount,
                        isSelected = selectedTierFilter == AthleteTier.ELITE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedTierFilter = if (selectedTierFilter == AthleteTier.ELITE) null else AthleteTier.ELITE
                        }
                    )
                    TierMetricPill(
                        tier = AthleteTier.WARRIOR,
                        count = warriorCount,
                        isSelected = selectedTierFilter == AthleteTier.WARRIOR,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedTierFilter = if (selectedTierFilter == AthleteTier.WARRIOR) null else AthleteTier.WARRIOR
                        }
                    )
                    TierMetricPill(
                        tier = AthleteTier.INTERMEDIATE,
                        count = intermediateCount,
                        isSelected = selectedTierFilter == AthleteTier.INTERMEDIATE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedTierFilter = if (selectedTierFilter == AthleteTier.INTERMEDIATE) null else AthleteTier.INTERMEDIATE
                        }
                    )
                    TierMetricPill(
                        tier = AthleteTier.INITIATE,
                        count = initiateCount,
                        isSelected = selectedTierFilter == AthleteTier.INITIATE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedTierFilter = if (selectedTierFilter == AthleteTier.INITIATE) null else AthleteTier.INITIATE
                        }
                    )
                }
            }
        }

        // BUSCADOR RÁPIDO
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar atleta por nombre o alias...", color = TextMuted, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LimeGreen,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )
        }

        // LISTA DE FICHAS TÉCNICAS (RECTÁNGULOS DE COLORES FLUORESCENTES)
        if (filteredClients.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron atletas con los criterios seleccionados.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            items(filteredClients, key = { it.id }) { client ->
                val clientAttendances = attendances.count { it.clientId == client.id }
                val tier = AthleteTier.getTierForClient(client, clientAttendances)

                AthleteTechnicalCard(
                    client = client,
                    tier = tier,
                    attendancesCount = clientAttendances,
                    isMe = client.email.equals(currentLoggedInEmail, ignoreCase = true),
                    onOpenProfile = { onOpenProfile(client) }
                )
            }
        }
    }
}

/**
 * Chip métrico superior con color fluorescente
 */
@Composable
private fun TierMetricPill(
    tier: AthleteTier,
    count: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SurfaceElevated else SurfaceDark)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) LimeGreen else SurfaceBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${tier.iconEmoji} ${tier.shortName}",
                color = if (isSelected) LimeGreen else TextLightGray,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * FICHA TÉCNICA DE ATLETA (CONCEPTO BANNER DEPORTIVO CORPORATIVO - CREATIVE STUDIO)
 * Adapta el diseño visual de alto impacto de la referencia gráfica respetando las
 * dimensiones exactas preestablecidas para no alterar la densidad ni el orden del directorio:
 * 1. Zona Izquierda: Avatar circular de alta definición enmarcado por una medialuna / halo de
 *    acento curvo y una matriz sutil de puntos geométricos de fondo.
 * 2. Zona Central: Tipografía pesada y jerarquizada en blanco y acento corporativo, insignia oficial
 *    (CEO / TÚ), barra horizontal de acento moderno (idéntica a la referencia), extracto armónico
 *    de la biografía del atleta (2 líneas máximas con elipsis) e indicadores técnicos (ID + Entrenos).
 * 3. Zona Derecha: Corte angular diagonal característico del banner con gradiente dinámico y botón "Ver".
 */
@Composable
fun AthleteTechnicalCard(
    client: Client,
    tier: AthleteTier,
    attendancesCount: Int,
    isMe: Boolean = false,
    onOpenProfile: () -> Unit
) {
    val isCeoOrAdmin = remember(client.fullName, client.email) {
        client.email.equals("alexgcuicas@gmail.com", ignoreCase = true) ||
        client.fullName.contains("CEO", ignoreCase = true)
    }
    val accentColor = if (isCeoOrAdmin) LimeGreen else tier.neonColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onOpenProfile() },
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.2.dp,
            accentColor.copy(alpha = if (isCeoOrAdmin) 0.65f else 0.4f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            SurfaceDark,
                            SurfaceDark,
                            accentColor.copy(alpha = 0.08f)
                        )
                    )
                )
        ) {
            // Fondo decorativo geométrico estilo banner (Creative Studio):
            // 1) Matriz de puntos sutil en la zona izquierda
            // 2) Corte angular diagonal en la esquina derecha
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height

                // Corte diagonal en la esquina derecha
                val cutWidth = (w * 0.16f).coerceIn(40f, 95f)
                val path = Path().apply {
                    moveTo(w, 0f)
                    lineTo(w - cutWidth, h)
                    lineTo(w, h)
                    close()
                }
                drawPath(
                    path = path,
                    color = accentColor.copy(alpha = 0.15f)
                )

                // Línea de acento diagonal
                drawLine(
                    color = accentColor.copy(alpha = 0.35f),
                    start = Offset(w, 0f),
                    end = Offset(w - cutWidth, h),
                    strokeWidth = 2f
                )

                // Matriz de puntos decorativos detrás del avatar (3 columnas x 5 filas)
                val dotRadius = 1.3f
                val dotSpacingX = 7f
                val dotSpacingY = 7f
                val startX = 14f
                val startY = 16f
                for (col in 0..2) {
                    for (row in 0..4) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.12f),
                            radius = dotRadius,
                            center = Offset(startX + col * dotSpacingX, startY + row * dotSpacingY)
                        )
                    }
                }
            }

            // Fila de contenido principal respetando dimensiones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Zona izquierda: Avatar circular con medialuna de acento estilo Creative Studio
                Box(
                    modifier = Modifier.size(62.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Medialuna de acento que abraza el avatar por la izquierda
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = accentColor,
                            startAngle = 100f,
                            sweepAngle = 160f,
                            useCenter = false,
                            style = Stroke(width = 3.5f),
                            topLeft = Offset(1.5f, 1.5f),
                            size = Size(size.width - 3f, size.height - 3f)
                        )
                    }

                    // Avatar fotográfico circular
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, CircleShape)
                    ) {
                        ClientAvatarImage(
                            avatarUrl = client.avatarUrl,
                            fullName = client.fullName,
                            contentScale = ContentScale.Crop,
                            fallbackTextSize = 18.sp,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Micro-insignia de jerarquía sobre el borde del avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(19.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D0F12))
                            .border(1.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(tier.iconEmoji, fontSize = 9.5.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // 2. Zona central: Nombre, Barra de Acento, Biografía e Indicadores
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Fila de Nombre con Insignia Oficial
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = client.fullName,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (isCeoOrAdmin) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LimeGreen)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("CEO", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 8.sp)
                            }
                        } else if (isMe) {
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LimeGreen)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("TÚ", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 8.sp)
                            }
                        }
                    }

                    // Subtítulo de Rol / Rango + Barra de acento horizontal estilo Creative Studio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            text = if (isCeoOrAdmin) "CEO • Actitud Fuerte" else "${tier.shortName} • Musculación",
                            color = accentColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Extracto de Biografía Adaptada
                    Text(
                        text = client.bio.ifBlank { "Atleta de Alto Rendimiento • Sala de Musculación Actitud Fuerte" },
                        color = TextLightGray,
                        fontSize = 10.sp,
                        lineHeight = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Micro-indicadores técnicos con datos reales
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ID:", color = TextMuted, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = client.idNumber,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text("•", color = TextMuted, fontSize = 8.sp)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$attendancesCount entrenos",
                                color = TextLightGray,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // 3. Zona derecha: Botón de acción rápido
                androidx.compose.material3.OutlinedButton(
                    onClick = onOpenProfile,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, accentColor),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceElevated.copy(alpha = 0.9f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 34.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ver",
                            color = accentColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}

