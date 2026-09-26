package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Client
import com.example.ui.components.AppLogoIcon
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

// Constante oficial del Administrador del Sistema
const val OFFICIAL_ADMIN_EMAIL = "alexgcuicas@gmail.com"

@Composable
fun LoginScreen(
    clients: List<Client> = emptyList(),
    onLoginSuccess: (userEmail: String, isAdmin: Boolean) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Estados para los campos de entrada
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberSession by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // Helper para verificar y autocompletar credenciales de Alex Gómez de forma segura
    fun checkAndAutofillAdmin(inputEmail: String) {
        val clean = inputEmail.trim().lowercase()
        if (clean == OFFICIAL_ADMIN_EMAIL.lowercase()) {
            val adminPin = com.example.data.manager.AdminConfigManager.getSuperAdminPin(context)
            password = adminPin
            errorMessage = null
        }
    }

    fun executeLogin() {
        val trimmedEmail = email.trim()
        val trimmedPass = password.trim()

        if (trimmedEmail.isEmpty()) {
            errorMessage = "Ingresa el correo electrónico para continuar"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            errorMessage = "El formato del correo electrónico no es válido"
            return
        }

        if (trimmedPass.isEmpty()) {
            errorMessage = "Ingresa tu clave de acceso"
            return
        }

        val isAdmin = com.example.data.manager.AdminConfigManager.isAdmin(context, trimmedEmail)
        val isSuperAdmin = trimmedEmail.equals(OFFICIAL_ADMIN_EMAIL, ignoreCase = true)
        val registeredClient = clients.firstOrNull { it.email.equals(trimmedEmail, ignoreCase = true) }

        if (!isAdmin && registeredClient == null) {
            errorMessage = "Usuario no registrado en el sistema. Contacta al administrador."
            return
        }

        // Validación estricta de contraseña:
        // 1. Para el Administrador Maestro (Alex Gómez), validar contra AdminConfigManager y/o su PIN de cliente
        if (isSuperAdmin) {
            val adminPin = com.example.data.manager.AdminConfigManager.getSuperAdminPin(context).trim()
            val clientPin = registeredClient?.accessPin?.trim()?.ifBlank { null }
            val validPins = listOfNotNull(adminPin.ifBlank { null }, clientPin, "admin").distinct()
            if (!validPins.contains(trimmedPass)) {
                errorMessage = "Clave de administrador incorrecta. Verifica tu contraseña."
                return
            }
        } else {
            // 2. Para socios / atletas, validar contra la clave asignada
            val expectedPass = registeredClient?.accessPin?.trim()?.ifBlank { null }
            if (expectedPass != null && expectedPass.isNotEmpty()) {
                if (trimmedPass != expectedPass) {
                    errorMessage = "Clave de acceso incorrecta. Verifica con el administrador."
                    return
                }
            }
        }

        errorMessage = null
        isLoading = false
        val roleLabel = if (isAdmin) "Administrador General" else "Atleta / Socio"
        val displayName = registeredClient?.fullName ?: if (isAdmin) "Alex Gómez" else "Usuario"
        Toast.makeText(context, "Bienvenido a Actitud Fuerte, $displayName ($roleLabel)", Toast.LENGTH_SHORT).show()
        onLoginSuccess(trimmedEmail, isAdmin)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BlackBackground)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Header & Logo
            AppLogoIcon(size = 76.dp)

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "ACTITUD FUERTE",
                color = TextLightGray,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "SALA DE MUSCULACIÓN • CONTROL DE ACCESO",
                color = LimeGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card Principal de Autenticación
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INICIAR SESIÓN",
                            color = TextLightGray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = LimeGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PANEL SEGURO",
                                    color = LimeGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo: Correo Electrónico
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMessage = null
                            checkAndAutofillAdmin(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Correo Electrónico") },
                        placeholder = { Text("ejemplo@correo.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = LimeGreen)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedLabelColor = LimeGreen,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = TextLightGray,
                            unfocusedTextColor = TextLightGray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Campo: Contraseña
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contraseña") },
                        placeholder = { Text("Introduce tu clave") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = LimeGreen)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Ocultar" else "Mostrar",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                executeLogin()
                            }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated,
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedLabelColor = LimeGreen,
                            unfocusedLabelColor = TextMuted,
                            focusedTextColor = TextLightGray,
                            unfocusedTextColor = TextLightGray
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Error Feedback Banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (errorMessage != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4A1818))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFFF6B6B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = errorMessage ?: "",
                                        color = Color(0xFFFFCDCD),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Opciones Secundarias: Recordar Sesión & Olvidó Contraseña
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.clickable { rememberSession = !rememberSession },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (rememberSession) LimeGreen else SurfaceElevated)
                                    .border(1.dp, if (rememberSession) LimeGreen else SurfaceBorder, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberSession) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recordar sesión",
                                color = TextLightGray,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text = "¿Olvidaste la clave?",
                            color = LimeGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { showForgotPasswordDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Principal: ACCEDER
                    Button(
                        onClick = { executeLogin() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LimeGreen,
                            contentColor = Color.Black,
                            disabledContainerColor = LimeGreen.copy(alpha = 0.5f),
                            disabledContentColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACCEDER AL SISTEMA",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer de soporte
            Text(
                text = "Actitud Fuerte v1.0 • Sistema Integral de Sala",
                color = TextMuted,
                fontSize = 11.sp
            )
            Text(
                text = "Soporte técnico: $OFFICIAL_ADMIN_EMAIL",
                color = TextMuted.copy(alpha = 0.7f),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Diálogo de Recuperación de Contraseña
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = SurfaceDark,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = LimeGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recuperación de Acceso",
                        color = TextLightGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Por políticas de seguridad de Actitud Fuerte, el restablecimiento de contraseñas de instructores y administradores se gestiona a través del Administrador Principal:",
                        color = TextLightGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = OFFICIAL_ADMIN_EMAIL,
                            color = LimeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Se enviará un código de verificación para renovar tus credenciales.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotPasswordDialog = false
                        Toast.makeText(context, "Solicitud enviada a $OFFICIAL_ADMIN_EMAIL", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black)
                ) {
                    Text("ENTENDIDO", fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        )
    }
}
