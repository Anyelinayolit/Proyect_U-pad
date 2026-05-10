package com.example.upad.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationsScreen(
    onViewReportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Notificaciones",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "U pad • Hace 5 min", color = androidx.compose.ui.graphics.Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Mateo terminó su rutina", fontWeight = FontWeight.Bold)
                Text(text = "Lavarse los dientes completado a las 7:45")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onViewReportClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver reporte de hoy")
        }
    }
}