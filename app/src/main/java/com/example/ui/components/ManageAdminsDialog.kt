package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.manager.AdminConfigManager
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Diálogo de Administración del Sistema para Alex Gómez.
 * Permite visualizar el Administrador Maestro (alexgcuicas@gmail.com),
 * cambiar la clave de acceso de administrador en tiempo real con sincronización,
 * y agregar/remover nuevos administradores por correo electrónico.
 */
@Composable
fun ManageAdminsDialog(
    onDismiss: () -> Unit,
    onUpdatePassword: ((String, (Boolean) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    var newAdminEmail by remember { mutableStateOf("") }
    var adminList by remember { mutableStateOf(AdminConfigManager.getAllAdmins(context)) }

    // Estados para cambio de clave del Administrador Maestro
    var currentAdminPin by remember { mutableStateOf(AdminConfigManager.getSuperAdminPin(context)) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isUpdatingPassword by remember { mutableStateOf(false) }
    var passwordSuccessMessage by remember { mutableStateOf<String?>(null) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, LimeGreen.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LimeGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GESTIÓN DE ADMINISTRADORES",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Permisos y control del sistema",
                                color = TextMuted,
                                fontSize = 10.5.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECCIÓN EXCLUSIVA: CAMBIO DE CLAVE DEL ADMINISTRADOR MAESTRO
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF192515)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LimeGreen.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LockReset,
                                contentDescription = null,
                                tint = LimeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLAVE DE ACCESO DEL ADMINISTRADOR",
                                color = LimeGreen,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Text(
                            text = "Modifica y sincroniza en tiempo real tu clave de ingreso (${AdminConfigManager.SUPER_ADMIN_EMAIL}).",
                            color = TextMuted,
                            fontSize = 9.5.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )

                        // Fila de inputs de contraseña
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = {
                                    newPassword = it
                                    passwordErrorMessage = null
                                    passwordSuccessMessage = null
                                },
                                placeholder = { Text("Nueva Clave", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LimeGreen,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = SurfaceElevated,
                                    unfocusedContainerColor = SurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    passwordErrorMessage = null
                                    passwordSuccessMessage = null
                                },
                                placeholder = { Text("Confirmar", color = TextMuted, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LimeGreen,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = SurfaceElevated,
                                    unfocusedContainerColor = SurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Toggle visibilidad y botón guardar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isPasswordVisible = !isPasswordVisible }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Icon(
                                    if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isPasswordVisible) "Ocultar" else "Ver clave",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val trimmedNew = newPassword.trim()
                                    val trimmedConf = confirmPassword.trim()
                                    if (trimmedNew.length < 3) {
                                        passwordErrorMessage = "La clave debe tener al menos 3 caracteres"
                                        return@Button
                                    }
                                    if (trimmedNew != trimmedConf) {
                                        passwordErrorMessage = "Las contraseñas no coinciden"
                                        return@Button
                                    }
                                    isUpdatingPassword = true
                                    passwordErrorMessage = null
                                    passwordSuccessMessage = null

                                    val applyUpdate = {
                                        AdminConfigManager.updateSuperAdminPin(context, trimmedNew)
                                        currentAdminPin = trimmedNew
                                        newPassword = ""
                                        confirmPassword = ""
                                        isUpdatingPassword = false
                                        passwordSuccessMessage = "¡Clave actualizada y sincronizada con éxito! 🔐"
                                        Toast.makeText(context, "Clave de Administrador actualizada", Toast.LENGTH_SHORT).show()
                                    }

                                    if (onUpdatePassword != null) {
                                        onUpdatePassword(trimmedNew) { success ->
                                            applyUpdate()
                                        }
                                    } else {
                                        applyUpdate()
                                    }
                                },
                                enabled = !isUpdatingPassword && newPassword.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LimeGreen,
                                    contentColor = Color.Black,
                                    disabledContainerColor = LimeGreen.copy(alpha = 0.3f),
                                    disabledContentColor = Color.Black.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                if (isUpdatingPassword) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        color = Color.Black,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ACTUALIZAR CLAVE", fontWeight = FontWeight.Black, fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }

                        // Mensajes de éxito / error
                        if (passwordSuccessMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = passwordSuccessMessage ?: "",
                                color = LimeGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (passwordErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = passwordErrorMessage ?: "",
                                color = Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Formulario para Agregar Nuevo Administrador
                Text(
                    text = "AGREGAR NUEVO ADMINISTRADOR",
                    color = LimeGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newAdminEmail,
                        onValueChange = { newAdminEmail = it },
                        placeholder = { Text("ejemplo@gmail.com", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LimeGreen,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val clean = newAdminEmail.trim()
                            if (clean.isBlank() || !clean.contains("@")) {
                                Toast.makeText(context, "Ingresa un correo electrónico válido", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val added = AdminConfigManager.addAdmin(context, clean)
                            if (added) {
                                adminList = AdminConfigManager.getAllAdmins(context)
                                newAdminEmail = ""
                                Toast.makeText(context, "Administrador agregado con éxito", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "El correo ya es administrador o no es válido", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LimeGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AGREGAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de Administradores Actuales
                Text(
                    text = "ADMINISTRADORES AUTORIZADOS (${adminList.size})",
                    color = TextLightGray,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(adminList) { email ->
                        val isSuperAdmin = email.equals(AdminConfigManager.SUPER_ADMIN_EMAIL, ignoreCase = true)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuperAdmin) Color(0xFF192515) else SurfaceElevated
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSuperAdmin) LimeGreen.copy(alpha = 0.6f) else SurfaceBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(
                                        imageVector = if (isSuperAdmin) Icons.Default.Shield else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSuperAdmin) LimeGreen else TextLightGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isSuperAdmin) "Alex Gómez" else email.substringBefore("@"),
                                                color = Color.White,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isSuperAdmin) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(LimeGreen)
                                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        "PRINCIPAL",
                                                        color = Color.Black,
                                                        fontSize = 8.5.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = email,
                                            color = TextMuted,
                                            fontSize = 10.5.sp
                                        )
                                    }
                                }

                                if (!isSuperAdmin) {
                                    IconButton(
                                        onClick = {
                                            AdminConfigManager.removeAdmin(context, email)
                                            adminList = AdminConfigManager.getAllAdmins(context)
                                            Toast.makeText(context, "Administrador revocado", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
