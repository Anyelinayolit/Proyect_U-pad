package com.example.upad.child

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R

@Composable
fun TaskExecutionScreen(
    activityNumber: Int = 1,
    activityName: String = "Bañarse",
    stepNumber: Int = 1,
    stepName: String = "ME QUITO LA ROPA Y ENTRO EN LA DUCHA",
    drawableId: Int, // Obligatorio pasar el ID de la imagen
    progress: Float = 0.25f,
    isLastStep: Boolean = false,
    onNextStepClick: () -> Unit
) {
    // Colores Animados y Profesionales
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorAmarilloTEA = Color(0xFFFFD54F)
    val colorVerdeTEA = Color(0xFF81C784)
    val colorFondoBase = Color(0xFFF0F4F8) // Un gris muy azulado y suave

    // Estado para el volteo de la imagen
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "FlipAnimation"
    )

    // Reiniciar volteo al cambiar de paso
    LaunchedEffect(stepName) { isFlipped = false }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ENMARCADA (Con bordes inferiores redondeados) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Color.White)
                .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Actividad $activityNumber: ${activityName.uppercase()}",
                fontSize = 32.sp, // Grande
                fontWeight = FontWeight.Black, // Muy grueso
                color = colorAzulTEA,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Paso $stepNumber: $stepName",
                fontSize = 18.sp, // Más chica que el título
                fontWeight = FontWeight.Medium,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // --- EL PICTOGRAMA CENTRAL (Largo, Grande e Inmersivo) ---
        // Usamos un weight para que ocupe todo el espacio central
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp) // Pequeño margen lateral
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 14f * density
                }
                .clickable { isFlipped = !isFlipped },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // LA IMAGEN PRINCIPAL (Ocupa casi todo el Card)
                        Image(
                            painter = painterResource(id = drawableId),
                            contentDescription = stepName,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp), // Margen interno mínimo
                            contentScale = ContentScale.Fit // Ajusta la imagen larga
                        )
                    } else {
                        // Cara trasera (Refuerzo)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f }
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.btn_star_big_on), // Icono de estrella
                                contentDescription = null,
                                tint = colorAmarilloTEA,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "¡Vas súper bien!",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAzulTEA,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // --- PANEL DE CONTROL INFERIOR (Actualizado con Porcentaje y Pasos) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila de información de progreso
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PASO $stepNumber DE 4", // Aquí puedes cambiar el 4 por totalSteps si lo pasas por parámetro
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = colorAzulTEA
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            // Barra de progreso mejorada
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp) // Un poquito más gruesa para que se vea mejor
                    .clip(RoundedCornerShape(10.dp)),
                color = colorAmarilloTEA,
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Gigante
            Button(
                onClick = onNextStepClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLastStep) colorVerdeTEA else colorAzulTEA
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = if (isLastStep) "¡TERMINAR! 🎉" else "SIGUIENTE PASO",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}