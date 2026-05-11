package com.example.upad.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationsScreen(
    onViewReportClick: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)
    val colorVerdeLogro = Color(0xFF81C784)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ESTILO U-PAD ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 60.dp, bottom = 30.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "AVISOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 2.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Notificaciones",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colorAzulTEA,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = colorAzulTEA.copy(alpha = 0.3f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // --- LISTA DE NOTIFICACIONES ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                NotificationItem(
                    title = "Mateo terminó su rutina",
                    description = "Lavarse los dientes completado a las 7:45",
                    time = "Hace 5 min",
                    iconColor = colorVerdeLogro
                )
            }

            // Ejemplo de otra notificación para ver cómo se llena
            item {
                NotificationItem(
                    title = "Rutina pendiente",
                    description = "Mateo aún no ha iniciado 'Vestirse'",
                    time = "Hace 30 min",
                    iconColor = Color(0xFFFFB74D) // Naranja amigable
                )
            }
        }

        // --- BOTÓN INFERIOR ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = onViewReportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    "VER REPORTE DE HOY",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    description: String,
    time: String,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Círculo de icono
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "U-Pad",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    Text(
                        text = time,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Color.DarkGray
                )
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}