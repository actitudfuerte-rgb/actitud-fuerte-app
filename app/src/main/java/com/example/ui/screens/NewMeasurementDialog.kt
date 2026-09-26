package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.ai.GeminiCoachService
import com.example.data.model.Client
import com.example.data.model.Measurement
import com.example.ui.components.CommunicationHelper
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted
import kotlinx.coroutines.launch

@Composable
fun NewMeasurementDialog(
    client: Client,
    previousMeasurement: Measurement?,
    onDismiss: () -> Unit,
    onSaveMeasurement: (Measurement, Boolean, Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val geminiService = remember { GeminiCoachService() }

    var weightKg by remember { mutableStateOf(previousMeasurement?.weightKg?.toString() ?: "75.0") }
    var backCm by remember { mutableStateOf(previousMeasurement?.backCm?.toString() ?: "100.0") }
    var shouldersCm by remember { mutableStateOf(previousMeasurement?.shouldersCm?.toString() ?: "115.0") }
    var armsCm by remember { mutableStateOf(previousMeasurement?.armsCm?.toString() ?: "36.0") }
    var hipsCm by remember { mutableStateOf(previousMeasurement?.hipsCm?.toString() ?: "94.0") }

    var trainerObservation by remember { mutableStateOf("") }
    var isGeneratingAI by remember { mutableStateOf(false) }

    var sendViaWhatsapp by remember { mutableStateOf(true) }
    var sendViaEmail by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Generar observación con IA
    fun requestAiObservation() {
        val w = weightKg.toDoubleOrNull() ?: return
        val b = backCm.toDoubleOrNull() ?: return
        val s = shouldersCm.toDoubleOrNull() ?: return
        val a = armsCm.toDoubleOrNull() ?: return
        val h = hipsCm.toDoubleOrNull() ?: return

        val currentCandidate = Measurement(
            clientId = client.id,
            weightKg = w,
            backCm = b,
            shouldersCm = s,
            armsCm = a,
            hipsCm = h
        )

        isGeneratingAI = true
        coroutineScope.launch {
            try {
                val generated = geminiService.generateCoachObservation(
                    clientInfo = client,
                    current = currentCandidate,
                    previous = previousMeasurement
                )
                trainerObservation = generated
            } finally {
                isGeneratingAI = false
            }
        }
    }

    LaunchedEffect(Unit) {
        // Auto trigger initial observation draft
        requestAiObservation()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACCIONES RÁPIDAS", color = LimeGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        Text("Toma de Medidas Corporales", color = TextLightGray, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Text("Atleta: ${client.fullName}", color = TextMuted, fontSize = 12.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Previous Measurement Reference Pill
                if (previousMeasurement != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Medición anterior: ${previousMeasurement.weightKg} kg | Espalda: ${previousMeasurement.backCm} cm | Hombros: ${previousMeasurement.shouldersCm} cm | Brazos: ${previousMeasurement.armsCm} cm | Cad: ${previousMeasurement.hipsCm} cm",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Grid of 5 measurements
                Text("MEDIDAS CORPORALES (NUEVA MEDICIÓN)", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeasureInputItem(
                        label = "Peso (KG)",
                        value = weightKg,
                        onValueChange = { weightKg = it },
                        modifier = Modifier.weight(1f)
                    )
                    MeasureInputItem(
                        label = "Espalda (CM)",
                        value = backCm,
                        onValueChange = { backCm = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MeasureInputItem(
                        label = "Hombros (CM)",
                        value = shouldersCm,
                        onValueChange = { shouldersCm = it },
                        modifier = Modifier.weight(1f)
                    )
                    MeasureInputItem(
                        label = "Brazos (CM)",
                        value = armsCm,
                        onValueChange = { armsCm = it },
                        modifier = Modifier.weight(1f)
                    )
                    MeasureInputItem(
                        label = "Caderas (CM)",
                        value = hipsCm,
                        onValueChange = { hipsCm = it },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Observación del entrenador generada por IA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Observación del entrenador (IA)",
                            color = LimeGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { requestAiObservation() },
                        enabled = !isGeneratingAI,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LimeGreen),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isGeneratingAI) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = LimeGreen, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generando...", fontSize = 11.sp)
                        } else {
                            Text("Reanalizar con IA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = trainerObservation,
                    onValueChange = { trainerObservation = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp),
                    placeholder = { Text("Escribe o genera la observación técnica de progreso...", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor = LimeGreen,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextLightGray,
                        unfocusedTextColor = TextLightGray
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = SurfaceBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Opciones de envío de reporte al usuario
                Text("ENVÍO DE REPORTE AL ATLETA", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Elige los canales de notificación para este registro:", color = TextMuted, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { sendViaWhatsapp = !sendViaWhatsapp }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sendViaWhatsapp,
                        onCheckedChange = { sendViaWhatsapp = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LimeGreen,
                            checkmarkColor = Color.Black,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enviar reporte al usuario por WhatsApp", color = TextLightGray, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { sendViaEmail = !sendViaEmail }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sendViaEmail,
                        onCheckedChange = { sendViaEmail = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = LimeGreen,
                            checkmarkColor = Color.Black,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enviar reporte al usuario por Correo Electrónico", color = TextLightGray, fontSize = 13.sp)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = errorMessage ?: "", color = Color(0xFFFF453A), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Dos botones requeridos: "Cancelar" y "Guardar medición"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CANCELAR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val w = weightKg.toDoubleOrNull()
                            val b = backCm.toDoubleOrNull()
                            val s = shouldersCm.toDoubleOrNull()
                            val a = armsCm.toDoubleOrNull()
                            val h = hipsCm.toDoubleOrNull()

                            if (w == null || b == null || s == null || a == null || h == null) {
                                errorMessage = "Por favor ingresa valores numéricos válidos en las 5 medidas"
                                return@Button
                            }

                            val newM = Measurement(
                                clientId = client.id,
                                timestamp = System.currentTimeMillis(),
                                weightKg = w,
                                backCm = b,
                                shouldersCm = s,
                                armsCm = a,
                                hipsCm = h,
                                aiObservation = trainerObservation,
                                trainerObservation = trainerObservation,
                                sentViaEmail = sendViaEmail,
                                sentViaWhatsapp = sendViaWhatsapp
                            )

                            // Enviar por los canales seleccionados
                            if (sendViaWhatsapp) {
                                val report = CommunicationHelper.buildMeasurementReport(client, newM, previousMeasurement)
                                CommunicationHelper.sendWhatsApp(context, client.phone, report)
                            }

                            if (sendViaEmail && client.email.isNotBlank()) {
                                val report = CommunicationHelper.buildMeasurementReport(client, newM, previousMeasurement)
                                CommunicationHelper.sendEmail(
                                    context,
                                    client.email,
                                    "Reporte de Medidas Corporales - Actitud Fuerte",
                                    report
                                )
                            }

                            onSaveMeasurement(newM, sendViaWhatsapp, sendViaEmail)
                        },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GUARDAR MEDICIÓN", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MeasureInputItem(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated,
                focusedBorderColor = LimeGreen,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextLightGray,
                unfocusedTextColor = TextLightGray
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}
