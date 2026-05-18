package com.example.upad.child

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TaskFeedbackScreen(
    activityName: String, // ¡Llega directo desde la actividad que el niño acaba de hacer!
    onFeedbackSelected: (String) -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA DE REFORZAMIENTO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 30.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿CÓMO TE SIENTES?",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                // Aquí se pinta en mayúsculas la actividad que llegó del adulto en tiempo real
                text = "Sobre la actividad: ${activityName.uppercase()}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )
        }

        // --- CONTENEDOR DE EMOCIONES (Diseño limpio de tus compañeros) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila para Feliz y Neutral
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeedbackCard(
                    modifier = Modifier.weight(1f),
                    emoji = "😊",
                    label = "FELIZ",
                    color = Color(0xFF81C784), // Verde éxito
                    onClick = { onFeedbackSelected("feliz") }
                )
                FeedbackCard(
                    modifier = Modifier.weight(1f),
                    emoji = "😐",
                    label = "NEUTRAL",
                    color = Color(0xFFFFD54F), // Amarillo
                    onClick = { onFeedbackSelected("neutral") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fila para Triste
            FeedbackCard(
                modifier = Modifier.fillMaxWidth(0.6f),
                emoji = "🙁",
                label = "TRISTE",
                color = Color(0xFFE57373), // Rojo suave
                onClick = { onFeedbackSelected("triste") }
            )
        }

        // --- ESPACIADOR INFERIOR SEGURO ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
        )
    }
}

@Composable
fun FeedbackCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 54.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}