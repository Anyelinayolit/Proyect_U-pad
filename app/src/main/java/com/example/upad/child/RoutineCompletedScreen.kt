package com.example.upad.child

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R

@Composable
fun RoutineCompletedScreen(
    nextActivityPreview: String = "¡VAMOS AL COLEGIO!",
    onFinishClick: () -> Unit
) {
    // Manteniendo la paleta de colores TEA profesional
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorVerdeTEA = Color(0xFF81C784)
    val colorFondoBase = Color(0xFFF0F4F8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA DE ÉXITO ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡LO LOGRASTE!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = colorVerdeTEA, // Verde para indicar éxito
                textAlign = TextAlign.Center
            )
            Text(
                text = "Completaste toda la actividad",
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // --- ILUSTRACIÓN DE CELEBRACIÓN ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Cargamos tu imagen llamada "celebracion"
                    Image(
                        painter = painterResource(id = R.drawable.celebracion),
                        contentDescription = "Celebración",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(16.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        // --- PANEL DE PRÓXIMA ACTIVIDAD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "PRÓXIMA ACTIVIDAD:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = nextActivityPreview,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Rutinas escolares cargadas",
                fontSize = 14.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // BOTÓN SIGUIENTE PROFESIONAL
            Button(
                onClick = onFinishClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text(
                    text = "SIGUIENTE ➔",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}