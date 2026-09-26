package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.Client
import com.example.ui.theme.BlackBackground
import com.example.ui.theme.LimeGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextLightGray
import com.example.ui.theme.TextMuted

@Composable
fun DigitalIdCardDialog(
    client: Client,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, LimeGreen.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CREDENCIAL DIGITAL",
                        color = LimeGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pass Card Surface
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlackBackground)
                        .border(1.dp, LimeGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_actitud_fuerte_logo),
                                    contentDescription = "Logo",
                                    tint = LimeGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "ACTITUD FUERTE",
                                        color = TextLightGray,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "GYM MEMBER PASS",
                                        color = LimeGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            StatusBadge(status = client.operationalStatus)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Client Name & ID / PIN
                        Text(
                            text = client.fullName.uppercase(),
                            color = TextLightGray,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp
                        )
                        val emailOrId = client.email.ifBlank { client.accessId }
                        val pinCode = client.accessPin.ifBlank { "1234" }
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ID: $emailOrId",
                                color = LimeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "•",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "PIN: $pinCode",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFF262626), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Details Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("PLAN ASIGNADO", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(client.membershipPlan, color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("PAGO", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(client.paymentFrequency, color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column {
                                Text("OBJETIVO", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(client.mainObjective.take(16) + "...", color = TextLightGray, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Simulated Barcode / QR
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = "QR", tint = Color.Black, modifier = Modifier.size(36.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "* ${client.accessId} *",
                                        color = Color.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        letterSpacing = 2.sp
                                    )
                                    Text("ACCESO SALA MUSCULACIÓN", color = Color(0xFF555555), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Acciones de Envío por WhatsApp y Copia de Credenciales
                val accessIdOrEmail = client.email.ifBlank { client.accessId }
                val accessPin = client.accessPin.ifBlank { "1234" }
                val whatsappMessage = """
«¡Hola, ${client.fullName}! 💪 Bienvenido(a) a ACTITUD FUERTE - Sala de Musculación.
Tus credenciales de acceso a la app son:
🆔 ID de Acceso: $accessIdOrEmail
🔑 Clave / PIN: $accessPin
Descarga o ingresa a la aplicación y entrena con la mejor actitud. ¡Nos vemos en la sala! 🔥»
""".trimIndent()

                val cleanDigits = client.phone.filter { it.isDigit() }
                val phoneWithPrefix = when {
                    cleanDigits.startsWith("58") -> cleanDigits
                    cleanDigits.startsWith("0") -> "58" + cleanDigits.substring(1)
                    cleanDigits.length == 10 && cleanDigits.startsWith("4") -> "58" + cleanDigits
                    else -> cleanDigits
                }

                Button(
                    onClick = {
                        try {
                            val sendUrl = if (phoneWithPrefix.isNotBlank()) {
                                "https://api.whatsapp.com/send?phone=$phoneWithPrefix&text=${android.net.Uri.encode(whatsappMessage)}"
                            } else {
                                "https://api.whatsapp.com/send?text=${android.net.Uri.encode(whatsappMessage)}"
                            }
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(sendUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Credenciales Actitud Fuerte", whatsappMessage))
                            android.widget.Toast.makeText(context, "Credenciales copiadas al portapapeles", android.widget.Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Text("📲 Enviar por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Credenciales Actitud Fuerte", whatsappMessage))
                        android.widget.Toast.makeText(context, "Credenciales copiadas al portapapeles", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceElevated),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextLightGray)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = LimeGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar Texto de Credenciales", color = TextLightGray, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }


            }
        }
    }
}
