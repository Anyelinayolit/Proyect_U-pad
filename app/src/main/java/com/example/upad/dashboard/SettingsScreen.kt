package com.example.upad.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit
) {
    val isPremiumUser by routineViewModel.isUserPremium.collectAsState(initial = false)
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    // 🔄 ¡CONEXIÓN GLOBAL! Extraemos los colores del MaterialTheme provisto por MainActivity
    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = colorFondoBase,
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = colorAcabadoPrincipal
                        )
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text("Tu Suscripción", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorTextoSecundario)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremiumUser) colorAcabadoPrincipal.copy(alpha = 0.15f) else colorSuperficieTarjetas
                ),
                border = if (isPremiumUser) androidx.compose.foundation.BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.4f)) else null,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPremiumUser) Icons.Default.WorkspacePremium else Icons.Default.Star,
                            contentDescription = null,
                            tint = colorAcabadoPrincipal,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isPremiumUser) "Plan UPAD Gold ✨" else "Plan Básico",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = colorTextoPrincipal
                            )
                            Text(
                                text = if (isPremiumUser) "Acceso total ilimitado e IA activada" else "Funciones básicas de rutinas",
                                fontSize = 12.sp,
                                color = colorTextoSecundario
                            )
                        }
                    }

                    if (isPremiumUser) {
                        Surface(
                            color = colorAcabadoPrincipal,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                "ACTIVO",
                                color = if (isDarkMode) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Text("Preferencias generales", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorTextoSecundario)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingSwitchRow(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones de rutinas",
                        subtitle = "Recordar tareas pendientes",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        switchColor = colorAcabadoPrincipal,
                        textColor = colorTextoPrincipal,
                        subTextColor = colorTextoSecundario
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = colorFondoBase.copy(alpha = 0.5f))

                    SettingSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = "Modo Oscuro",
                        subtitle = "Cambiar la interfaz a tonos oscuros",
                        checked = isDarkMode,
                        onCheckedChange = { routineViewModel.setDarkMode(it) }, // Cambia el estado global en Firebase/Prefs
                        switchColor = colorAcabadoPrincipal,
                        textColor = colorTextoPrincipal,
                        subTextColor = colorTextoSecundario
                    )
                }
            }

            Text("Aplicación", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colorTextoSecundario)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = colorAcabadoPrincipal)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Idioma de la app", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colorTextoPrincipal)
                            Text("Español", fontSize = 12.sp, color = colorTextoSecundario)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = switchColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
                Text(subtitle, fontSize = 12.sp, color = subTextColor)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = switchColor
            )
        )
    }
}