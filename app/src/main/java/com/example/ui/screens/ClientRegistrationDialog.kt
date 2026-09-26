package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.example.data.model.Client
import com.example.data.model.FitnessConstants
import com.example.ui.components.CommunicationHelper
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

/**
 * Herramienta principal de registro de nuevo usuario / edición en Actitud Fuerte
 */
@Composable
fun ClientRegistrationDialog(
    initialClient: Client? = null,
    onDismiss: () -> Unit,
    onSave: (Client) -> Unit
) {
    var fullName by remember { mutableStateOf(initialClient?.fullName ?: "") }
    var gender by remember { mutableStateOf(initialClient?.gender ?: "Hombre") }
    var phone by remember { mutableStateOf(initialClient?.phone ?: "") }
    var email by remember { mutableStateOf(initialClient?.email ?: "") }
    var membershipPlan by remember { mutableStateOf(initialClient?.membershipPlan ?: FitnessConstants.MEMBERSHIP_PLANS[1]) }
    var paymentFrequency by remember { mutableStateOf(initialClient?.paymentFrequency ?: FitnessConstants.PAYMENT_FREQUENCIES[2]) }
    var emergencyContact by remember { mutableStateOf(initialClient?.emergencyContact ?: "") }
    var mainObjective by remember { mutableStateOf(initialClient?.mainObjective ?: FitnessConstants.OBJECTIVES[0]) }
    var medicalCondition by remember { mutableStateOf(initialClient?.medicalCondition ?: FitnessConstants.MEDICAL_CONDITIONS[0]) }
    var medicalNotes by remember { mutableStateOf(initialClient?.medicalNotes ?: "") }

    val context = LocalContext.current
    var sendWelcomeViaWa by remember { mutableStateOf(initialClient == null) }

    var planExpanded by remember { mutableStateOf(false) }
    var freqExpanded by remember { mutableStateOf(false) }
    var objectiveExpanded by remember { mutableStateOf(false) }
    var medicalExpanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialClient == null) "NUEVO ATLETA" else "EDITAR INFORMACIÓN",
                            color = LimeGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (initialClient == null) "Formulario de Registro" else initialClient.fullName,
                            color = TextLightGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Nombre y apellido
                CustomInputField(
                    label = "Nombre y apellido *",
                    value = fullName,
                    onValueChange = { fullName = it },
                    leadingIcon = Icons.Default.Person,
                    placeholder = "Ej: Carlos Mendoza"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Género del Atleta (Hombre / Mujer)
                Text("Género del Atleta *", color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Hombre", "Mujer").forEach { g ->
                        val selected = gender.equals(g, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) LimeGreen else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (selected) LimeGreen else SurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { gender = g }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (g == "Hombre") "♂ Hombre" else "♀ Mujer",
                                color = if (selected) Color.Black else TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Teléfono / WhatsApp
                CustomInputField(
                    label = "Teléfono / WhatsApp *",
                    value = phone,
                    onValueChange = { phone = it },
                    leadingIcon = Icons.Default.Phone,
                    placeholder = "+58 414 1234567",
                    keyboardType = KeyboardType.Phone
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Correo electrónico
                CustomInputField(
                    label = "Correo electrónico *",
                    value = email,
                    onValueChange = { email = it },
                    leadingIcon = Icons.Default.Email,
                    placeholder = "carlos@ejemplo.com",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Plan de membresía (pestaña / selector $12 y $20)
                Text("Plan de Membresía *", color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessConstants.MEMBERSHIP_PLANS.forEach { plan ->
                        val selected = membershipPlan == plan
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) LimeGreen else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (selected) LimeGreen else SurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { membershipPlan = plan }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = plan,
                                color = if (selected) Color.Black else TextLightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Modalidad de pago asignada (semanal, quincenal, mensual)
                Text(
                    "Modalidad de pago asignada (fraccionamiento habitual) *",
                    color = TextLightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessConstants.PAYMENT_FREQUENCIES.forEach { freq ->
                        val selected = paymentFrequency == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) LimeGreen.copy(alpha = 0.2f) else SurfaceElevated)
                                .border(
                                    1.dp,
                                    if (selected) LimeGreen else SurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { paymentFrequency = freq }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = freq,
                                color = if (selected) LimeGreen else TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. Contacto de emergencia
                CustomInputField(
                    label = "Contacto de emergencia (Nombre y Teléfono) *",
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    leadingIcon = Icons.Default.ContactPhone,
                    placeholder = "Ej: Elena Mendoza (+58 412 9876543)"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 7. Objetivo principal del usuario (Desplegable 5 objetivos más comunes)
                Text("Objetivo principal del usuario *", color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { objectiveExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(mainObjective, color = TextLightGray, fontSize = 13.sp)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LimeGreen)
                        }
                    }
                    DropdownMenu(
                        expanded = objectiveExpanded,
                        onDismissRequest = { objectiveExpanded = false },
                        modifier = Modifier.background(SurfaceElevated)
                    ) {
                        FitnessConstants.OBJECTIVES.forEach { obj ->
                            DropdownMenuItem(
                                text = { Text(obj, color = TextLightGray, fontSize = 13.sp) },
                                onClick = {
                                    mainObjective = obj
                                    objectiveExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 8. Condición o nota médica (Desplegable opciones más frecuentes)
                Text("Condición o nota médica *", color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { medicalExpanded = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(medicalCondition, color = TextLightGray, fontSize = 13.sp)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = LimeGreen)
                        }
                    }
                    DropdownMenu(
                        expanded = medicalExpanded,
                        onDismissRequest = { medicalExpanded = false },
                        modifier = Modifier.background(SurfaceElevated)
                    ) {
                        FitnessConstants.MEDICAL_CONDITIONS.forEach { med ->
                            DropdownMenuItem(
                                text = { Text(med, color = TextLightGray, fontSize = 13.sp) },
                                onClick = {
                                    medicalCondition = med
                                    medicalExpanded = false
                                }
                            )
                        }
                    }
                }

                if (medicalCondition != "Ninguna de las anteriores") {
                    Spacer(modifier = Modifier.height(8.dp))
                    CustomInputField(
                        label = "Detalle médico adicional (opcional)",
                        value = medicalNotes,
                        onValueChange = { medicalNotes = it },
                        leadingIcon = Icons.Default.MedicalServices,
                        placeholder = "Ej: Hernia L4-L5, evitar sentadilla libre pesada"
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = errorMessage ?: "", color = Color(0xFFFF453A), fontSize = 12.sp)
                }

                if (initialClient == null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { sendWelcomeViaWa = !sendWelcomeViaWa },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = sendWelcomeViaWa,
                            onCheckedChange = { sendWelcomeViaWa = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = LimeGreen,
                                checkmarkColor = Color.Black,
                                uncheckedColor = SurfaceBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar Mensaje de Bienvenida por WhatsApp", color = TextLightGray, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones Guardar y Cancelar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            if (fullName.isBlank()) {
                                errorMessage = "Por favor ingresa el nombre y apellido"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = "Por favor ingresa el teléfono/whatsapp"
                                return@Button
                            }
                            val clientToSave = (initialClient ?: Client(
                                fullName = "",
                                phone = "",
                                email = "",
                                membershipPlan = "",
                                paymentFrequency = "",
                                emergencyContact = "",
                                mainObjective = "",
                                medicalCondition = ""
                            )).copy(
                                fullName = fullName.trim(),
                                gender = gender,
                                phone = phone.trim(),
                                email = email.trim(),
                                membershipPlan = membershipPlan,
                                paymentFrequency = paymentFrequency,
                                emergencyContact = emergencyContact.trim(),
                                mainObjective = mainObjective,
                                medicalCondition = medicalCondition,
                                medicalNotes = medicalNotes.trim()
                            )

                            if (sendWelcomeViaWa && clientToSave.phone.isNotBlank()) {
                                val welcomeMsg = CommunicationHelper.buildWelcomeMessage(clientToSave)
                                CommunicationHelper.sendWhatsApp(context, clientToSave.phone, welcomeMsg)
                            }

                            onSave(clientToSave)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("GUARDAR Y CREAR FICHA", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, color = TextLightGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(18.dp)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated,
                focusedBorderColor = LimeGreen,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextLightGray,
                unfocusedTextColor = TextLightGray
            ),
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}
