package com.example.upad.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Importación nativa de Compose
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExperienceSetupScreen(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    // Estados que controlan la funcionalidad
    var selectedTheme by remember { mutableStateOf("Modo claro") }
    var audioVolume by remember { mutableFloatStateOf(0.7f) }
    var voiceEnabled by remember { mutableStateOf(true) }

    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA CURVA (Estilo Mateo) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 40.dp, bottom = 30.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Text(
                text = "PERSONALIZACIÓN",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Prepara la experiencia",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // --- CUERPO SCROLLABLE ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
        ) {
            Text(
                text = "APARIENCIA VISUAL",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Temas
            val themes = listOf("Modo oscuro", "Modo claro", "Según sistema")
            themes.forEach { theme ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedTheme = theme }, // Clickable nativo directo
                    color = if (selectedTheme == theme) Color.White else Color.Transparent,
                    tonalElevation = if (selectedTheme == theme) 4.dp else 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        RadioButton(
                            selected = (theme == selectedTheme),
                            onClick = { selectedTheme = theme },
                            colors = RadioButtonDefaults.colors(selectedColor = colorAzulTEA)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = theme,
                            fontSize = 16.sp,
                            color = if (selectedTheme == theme) colorAzulTEA else Color.DarkGray,
                            fontWeight = if (selectedTheme == theme) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "AUDIO Y VOZ (PANTALLA NIÑO)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Switch de Lectura
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Voz en pictogramas", fontWeight = FontWeight.Bold)
                        Text("Lee las tareas en voz alta", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = voiceEnabled,
                        onCheckedChange = { voiceEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = colorAzulTEA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Slider de Volumen
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = colorAzulTEA)
                Slider(
                    value = audioVolume,
                    onValueChange = { audioVolume = it },
                    modifier = Modifier.padding(start = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = colorAzulTEA,
                        activeTrackColor = colorAzulTEA,
                        inactiveTrackColor = colorAzulTEA.copy(alpha = 0.24f)
                    )
                )
            }
        }

        // --- BOTÓN FINAL ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(30.dp)
        ) {
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
            ) {
                Text("GUARDAR Y CONTINUAR", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}