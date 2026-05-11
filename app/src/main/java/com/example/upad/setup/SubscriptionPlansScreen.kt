package com.example.upad.setup

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.R

@Composable
fun SubscriptionPlansScreen(
    onPlanSelected: (String) -> Unit, // CAMBIADO: Ahora recibe un String
    onSkip: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorAmarilloTEA = Color(0xFFFFD54F)
    val colorFondoBase = Color(0xFFF0F4F8)

    // Estado para saber qué plan está seleccionado visualmente
    var selectedPlan by remember { mutableStateOf("premium") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 30.dp, start = 24.dp, end = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¡ELIGE UN PLAN!",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Desbloquea herramientas increíbles para el desarrollo de Mateo.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // --- CONTENIDO DE PLANES ---
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tarjeta Plan Básico
            PlanCard(
                title = "PLAN BÁSICO",
                price = "Gratis",
                imageRes = R.drawable.plan_basico,
                isSelected = selectedPlan == "basico",
                colorTheme = Color.Gray,
                onClick = { selectedPlan = "basico" }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tarjeta Plan Premium
            PlanCard(
                title = "PLAN PREMIUM",
                price = "$9.99 / mes",
                imageRes = R.drawable.plan_premium,
                isSelected = selectedPlan == "premium",
                colorTheme = colorAmarilloTEA,
                onClick = { selectedPlan = "premium" }
            )
        }

        // --- PANEL DE ACCIÓN INFERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                // CAMBIADO: Ahora enviamos el valor de selectedPlan ("basico" o "premium")
                onClick = { onPlanSelected(selectedPlan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
            ) {
                Text("ESCOGER PLAN", fontSize = 20.sp, fontWeight = FontWeight.Black)
            }

            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    "En otro momento",
                    color = Color.LightGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// El componente PlanCard se mantiene igual, ya que solo maneja la parte visual
@Composable
fun PlanCard(
    title: String,
    price: String,
    imageRes: Int,
    isSelected: Boolean,
    colorTheme: Color,
    onClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 12.dp else 2.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, colorAzulTEA) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) colorAzulTEA else Color.DarkGray
                )
                Text(
                    text = price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTheme
                )
                if (title.contains("PREMIUM")) {
                    Text(
                        text = "Pictogramas ilimitados",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (isSelected) {
                RadioButton(
                    selected = true,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(selectedColor = colorAzulTEA)
                )
            }
        }
    }
}