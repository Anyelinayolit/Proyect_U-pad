package com.example.upad.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    // 🔄 CONEXIÓN GLOBAL: Extraemos la paleta reactiva de Material 3
    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser

    val parentName = remember(currentUser) {
        val emailName = currentUser?.email?.substringBefore("@")
        val nameToDisplay = currentUser?.displayName ?: emailName
        if (!nameToDisplay.isNullOrBlank()) nameToDisplay.uppercase() else "PADRE/TUTOR"
    }

    Scaffold(
        containerColor = colorFondoBase,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorAcabadoPrincipal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorSuperficieTarjetas)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Avatar Circular de perfil adaptable
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(colorAcabadoPrincipal.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = colorAcabadoPrincipal, modifier = Modifier.size(50.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tarjeta contenedora de Información básica del usuario adaptable
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ProfileInfoRow(
                        icon = Icons.Default.Person,
                        title = "Nombre de Usuario",
                        value = parentName,
                        iconColor = colorAcabadoPrincipal,
                        textColor = colorTextoPrincipal,
                        subTextColor = colorTextoSecundario
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorFondoBase)
                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        title = "Correo Electrónico",
                        value = currentUser?.email ?: "No disponible",
                        iconColor = colorAcabadoPrincipal,
                        textColor = colorTextoPrincipal,
                        subTextColor = colorTextoSecundario
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón estructurado para desconexión segura en Firebase
            Button(
                onClick = {
                    firebaseAuth.signOut()
                    onLogoutSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)), // Color rojo de alerta fijo intencional
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 12.sp, color = subTextColor, fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 16.sp, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}