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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.Client
import com.example.data.model.idNumber
import com.example.data.model.isActive
import com.example.ui.components.CommunicationHelper
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

data class QuickTemplate(
    val id: String,
    val name: String,
    val body: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BroadcastDialog(
    clients: List<Client>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val templates = remember {
        listOf(
            QuickTemplate(
                id = "general",
                name = "Aviso General",
                body = "¡Hola {nombre}!\n\nTe escribimos del equipo de administración para informarte sobre: "
            ),
            QuickTemplate(
                id = "feriado",
                name = "Horario Feriado",
                body = "¡Hola {nombre}!\n\nTe notificamos que este próximo día festivo abriremos en horario especial de 7:00 am a 1:00 pm. ¡Te esperamos para entrenar!"
            ),
            QuickTemplate(
                id = "mantenimiento",
                name = "Mantenimiento",
                body = "¡Hola {nombre}!\n\nTe informamos que se estará realizando un mantenimiento preventivo en la sala de musculación para brindarte la mejor experiencia."
            ),
            QuickTemplate(
                id = "renovacion",
                name = "Renovación",
                body = "¡Hola {nombre}!\n\nTe recordamos renovar oportunamente tu membresía para continuar disfrutando de tu entrenamiento y seguimiento físico sin interrupciones."
            )
        )
    }

    var selectedTemplate by remember { mutableStateOf(templates[0]) }
    var customMessage by remember { mutableStateOf(templates[0].body) }
    var searchQuery by remember { mutableStateOf("") }

    val selectedClientIds = remember { mutableStateListOf<Long>() }

    val filteredClients = remember(clients, searchQuery) {
        if (searchQuery.isBlank()) clients
        else clients.filter {
            it.fullName.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery) ||
            it.idNumber.contains(searchQuery)
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
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Difusión Masiva por WhatsApp",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Avisos de feriados, horarios, eventos y cuotas",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    // PLANTILLAS RÁPIDAS
                    item {
                        Text(
                            "PLANTILLAS RÁPIDAS",
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
                            templates.forEach { tmpl ->
                                val isSelected = selectedTemplate.id == tmpl.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) LimeGreen else SurfaceDark)
                                        .border(1.dp, if (isSelected) LimeGreen else SurfaceBorder, RoundedCornerShape(20.dp))
                                        .clickable {
                                            selectedTemplate = tmpl
                                            customMessage = tmpl.body
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tmpl.name,
                                        color = if (isSelected) Color.Black else TextLightGray,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Área de Edición del Mensaje
                    item {
                        Text(
                            "CUERPO DEL MENSAJE (El sistema reemplazará {nombre} automáticamente)",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = customMessage,
                            onValueChange = { customMessage = it },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 6,
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

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Plantilla Actitud Fuerte", customMessage)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Texto copiado al portapapeles", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copiar plantilla", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = SurfaceBorder)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Selección de Atletas para envío
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "SELECCIONAR ATLETAS (${filteredClients.size})",
                                color = LimeGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    if (selectedClientIds.size == filteredClients.size) {
                                        selectedClientIds.clear()
                                    } else {
                                        selectedClientIds.clear()
                                        selectedClientIds.addAll(filteredClients.map { it.id })
                                    }
                                }
                            ) {
                                Checkbox(
                                    checked = selectedClientIds.isNotEmpty() && selectedClientIds.size == filteredClients.size,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedClientIds.clear()
                                            selectedClientIds.addAll(filteredClients.map { it.id })
                                        } else {
                                            selectedClientIds.clear()
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = LimeGreen,
                                        checkmarkColor = Color.Black,
                                        uncheckedColor = SurfaceBorder
                                    )
                                )
                                Text("Todos", color = TextLightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Barra de búsqueda rápida
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar atleta por nombre o teléfono...", color = TextMuted, fontSize = 11.sp) },
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
                        val isSelected = selectedClientIds.contains(client.id)
                        val firstName = CommunicationHelper.getFirstName(client.fullName)
                        val personalizedMsg = customMessage.replace("{nombre}", firstName)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SurfaceElevated else SurfaceDark)
                                .border(1.dp, if (isSelected) LimeGreen.copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (isSelected) selectedClientIds.remove(client.id)
                                    else selectedClientIds.add(client.id)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedClientIds.add(client.id)
                                        else selectedClientIds.remove(client.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = LimeGreen,
                                        checkmarkColor = Color.Black,
                                        uncheckedColor = SurfaceBorder
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        client.fullName,
                                        color = TextLightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        if (client.phone.isNotBlank()) "WhatsApp: ${client.phone}" else "Sin teléfono",
                                        color = if (client.phone.isNotBlank()) LimeGreen else TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (client.phone.isNotBlank()) {
                                        CommunicationHelper.sendWhatsApp(context, client.phone, personalizedMsg)
                                    } else {
                                        Toast.makeText(context, "El atleta ${client.fullName} no tiene teléfono registrado", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enviar WA", fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = SurfaceBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
                    ) {
                        Text("Cerrar", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val targets = clients.filter { selectedClientIds.contains(it.id) }
                            if (targets.isEmpty()) {
                                Toast.makeText(context, "Selecciona al menos un atleta de la lista", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Envía al primer atleta seleccionado y notifica
                            val first = targets.first()
                            val firstName = CommunicationHelper.getFirstName(first.fullName)
                            val personalized = customMessage.replace("{nombre}", firstName)
                            CommunicationHelper.sendWhatsApp(context, first.phone, personalized)
                            Toast.makeText(context, "Iniciando difusión para ${targets.size} atletas (${first.fullName})", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedClientIds.isEmpty()) "Enviar a Seleccionados" else "Enviar a (${selectedClientIds.size})",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
