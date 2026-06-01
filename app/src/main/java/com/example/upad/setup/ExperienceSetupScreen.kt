package com.example.upad.setup

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExperienceSetupScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val prefs = remember { context.getSharedPreferences("UPAD_PREFS", Context.MODE_PRIVATE) }

    val pantallaPequeña = configuration.screenHeightDp < 650

    // --- CARGA DE ESTADOS CON PERSISTENCIA ---
    var selectedTheme by remember { mutableStateOf(prefs.getString("APP_THEME", "Modo claro") ?: "Modo claro") }
    var audioVolume by remember { mutableFloatStateOf(prefs.getFloat("AUDIO_VOLUME", 0.7f)) }
    var voiceEnabled by remember { mutableStateOf(prefs.getBoolean("VOICE_ENABLED", true)) }

    // --- LÓGICA DE DETECCIÓN DINÁMICA DE MODO OSCURO ---
    val esOscuroActivo = when (selectedTheme) {
        "Modo oscuro" -> true
        "Modo claro" -> false
        else -> isSystemInDarkTheme() // "Según sistema"
    }

    // --- PALETA DE COLORES ADAPTATIVA (REACTIVA AL MODO OSCURO/CLARO) ---
    val colorAzulTEA = Color(0xFF4FC3F7)

    // Transiciones fluidas de color para una mejor experiencia visual
    val colorFondoBase by animateColorAsState(if (esOscuroActivo) Color(0xFF0F172A) else Color(0xFFF8FAFC))
    val colorContenedorBlanco by animateColorAsState(if (esOscuroActivo) Color(0xFF1E293B) else Color.White)
    val colorTextoPrincipal by animateColorAsState(if (esOscuroActivo) Color.White else Color(0xFF1E293B))
    val colorTextoSecundario by animateColorAsState(if (esOscuroActivo) Color(0xFF94A3B8) else Color.Gray)
    val colorSuperficieSeleccionada by animateColorAsState(if (esOscuroActivo) Color(0xFF334155) else Color(0xFFE0F7FA))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA DINÁMICA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(colorContenedorBlanco)
                .padding(
                    top = if (pantallaPequeña) 30.dp else 40.dp,
                    bottom = if (pantallaPequeña) 16.dp else 24.dp,
                    start = 16.dp,
                    end = 24.dp
                )
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "PERSONALIZACIÓN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = colorTextoSecundario,
                modifier = Modifier.padding(start = 12.dp),
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Prepara la experiencia",
                fontSize = if (pantallaPequeña) 24.sp else 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // --- CUERPO RESPONSIVO CON SCROLL ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "APARIENCIA VISUAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorAzulTEA,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Selector de Temas Estilizado y Reactivo
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorContenedorBlanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val themes = listOf("Modo claro", "Modo oscuro", "Según sistema")
                    themes.forEach { theme ->
                        val estaSeleccionado = selectedTheme == theme

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (estaSeleccionado) colorSuperficieSeleccionada else Color.Transparent)
                                .clickable { selectedTheme = theme }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = estaSeleccionado,
                                onClick = { selectedTheme = theme },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colorAzulTEA,
                                    unselectedColor = colorTextoSecundario
                                )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = theme,
                                fontSize = 15.sp,
                                color = if (estaSeleccionado) colorAzulTEA else colorTextoPrincipal,
                                fontWeight = if (estaSeleccionado) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AUDIO Y VOZ (ENTORNO DEL NIÑO)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colorAzulTEA,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tarjeta de Moduladores de Sonido/Voz
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorContenedorBlanco),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Voz en pictogramas", fontWeight = FontWeight.Bold, color = colorTextoPrincipal)
                            Text("Lee las tareas en voz alta", fontSize = 12.sp, color = colorTextoSecundario)
                        }
                        Switch(
                            checked = voiceEnabled,
                            onCheckedChange = { voiceEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = colorAzulTEA,
                                uncheckedTrackColor = colorTextoSecundario.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colorTextoSecundario.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Slider de Volumen
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (voiceEnabled) colorAzulTEA else colorTextoSecundario
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Slider(
                            value = audioVolume,
                            onValueChange = { audioVolume = it },
                            enabled = voiceEnabled, // Se deshabilita si la voz está apagada
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = colorAzulTEA,
                                activeTrackColor = colorAzulTEA,
                                inactiveTrackColor = colorAzulTEA.copy(alpha = 0.24f),
                                disabledThumbColor = colorTextoSecundario.copy(alpha = 0.5f),
                                disabledActiveTrackColor = colorTextoSecundario.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // --- BOTÓN DE CONTROL FIJO INFERIOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colorContenedorBlanco)
                .padding(horizontal = 20.dp, vertical = if (pantallaPequeña) 12.dp else 16.dp)
        ) {
            Button(
                onClick = {
                    // Guardamos todas las preferencias del entorno seleccionadas localmente
                    prefs.edit().apply {
                        putString("APP_THEME", selectedTheme)
                        putFloat("AUDIO_VOLUME", audioVolume)
                        putBoolean("VOICE_ENABLED", voiceEnabled)
                        apply()
                    }
                    onNextClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (pantallaPequeña) 54.dp else 60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("GUARDAR Y CONTINUAR", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}