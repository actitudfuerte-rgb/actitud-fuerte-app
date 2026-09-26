package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.GeminiCoachService
import com.example.data.model.Client
import com.example.data.model.isActive
import com.example.ui.components.CommunicationHelper
import com.example.ui.components.LeadershipFocusItem
import com.example.ui.components.LeadershipFocusManager
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LeadershipDialog(
    clients: List<Client>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiCoachService() }

    val focuses = remember { LeadershipFocusManager.FOCUS_ITEMS }
    var selectedFocus by remember { mutableStateOf(focuses[0]) }

    var currentMessage by remember {
        mutableStateOf(LeadershipFocusManager.getMessageTemplate(context, selectedFocus.id))
    }

    var isRegeneratingAi by remember { mutableStateOf(false) }
    var daysRemaining by remember { mutableStateOf(LeadershipFocusManager.getDaysUntilNextRenewal(context)) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredClients = remember(clients, searchQuery) {
        val active = clients.filter { it.isActive }
        if (searchQuery.isBlank()) active
        else active.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BlackBackground),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(LimeGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Liderazgo & Cercanía",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceElevated)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("JUEVES 21:00 HS", color = LimeGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                "Mensajes con IA para saber cómo se sienten tus alumnos",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    // SELECCIONA EL ENFOQUE PARA HOY
                    item {
                        Text(
                            "SELECCIONA EL ENFOQUE PARA HOY",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            focuses.forEach { focus ->
                                val isSelected = selectedFocus.id == focus.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) LimeGreen else SurfaceDark)
                                        .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedFocus = focus
                                            currentMessage = LeadershipFocusManager.getMessageTemplate(context, focus.id)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = focus.title,
                                        color = if (isSelected) Color.Black else TextLightGray,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Tarjeta de Estado del Ciclo IA de 30 días
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = LimeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            "Generado por Inteligencia Artificial",
                                            color = TextLightGray,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Renovación cíclica cada 30 días (Restan $daysRemaining días)",
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                if (isRegeneratingAi) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = LimeGreen,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                isRegeneratingAi = true
                                                Toast.makeText(context, "Renovando enfoques con IA...", Toast.LENGTH_SHORT).show()
                                                val success = LeadershipFocusManager.renewAllWithAi(context, geminiService)
                                                currentMessage = LeadershipFocusManager.getMessageTemplate(context, selectedFocus.id)
                                                daysRemaining = LeadershipFocusManager.getDaysUntilNextRenewal(context)
                                                isRegeneratingAi = false
                                                if (success) {
                                                    Toast.makeText(context, "¡Enfoques renovados con éxito!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Enfoques actualizados con base oficial", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f)),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Renovar IA", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Cuadro editable del Mensaje Sugerido
                    item {
                        Text(
                            "MENSAJE SUGERIDO PARA EL ALUMNO ({nombre} se personaliza por socio)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = currentMessage,
                            onValueChange = {
                                currentMessage = it
                                LeadershipFocusManager.saveMessageTemplate(context, selectedFocus.id, it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            maxLines = 8,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextLightGray,
                                unfocusedTextColor = TextLightGray,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Check-in Liderazgo", currentMessage)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Mensaje copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copiar texto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = SurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Atletas Activos para Enviar
                    item {
                        Text(
                            "ENVIAR A ALUMNOS (${filteredClients.size} activos)",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar alumno por nombre...", color = TextMuted, fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LimeGreen,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextLightGray,
                                unfocusedTextColor = TextLightGray,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(filteredClients) { client ->
                        val personalizedMsg = LeadershipFocusManager.formatMessageForClient(currentMessage, client)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceDark)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    client.fullName,
                                    color = TextLightGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (client.phone.isNotBlank()) "WhatsApp: ${client.phone}" else "Sin teléfono registrado",
                                    color = if (client.phone.isNotBlank()) LimeGreen else TextMuted,
                                    fontSize = 10.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (client.phone.isNotBlank()) {
                                        CommunicationHelper.sendWhatsApp(context, client.phone, personalizedMsg)
                                    } else {
                                        Toast.makeText(context, "El alumno no tiene teléfono registrado", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enviar WA", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Cerrar",
                        color = LimeGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(horizontal = 24.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
