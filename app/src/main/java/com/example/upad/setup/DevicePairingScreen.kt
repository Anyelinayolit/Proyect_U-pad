package com.example.upad.setup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DevicePairingScreen(
    isPaired: Boolean = false,
    onDeviceSelected: (String) -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val devices = listOf("Zte de Alex 1563", "Samsung tab 3 de Mateo", "LG WebOs 41963")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isPaired) {
            Text(
                text = "Búsqueda de dispositivo para vinculación",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(2.dp, Color.Blue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("Radar UI")
            }

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(devices) { device ->
                    Text(
                        text = device,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDeviceSelected(device) }
                            .padding(8.dp)
                    )
                }
            }
        } else {
            Text(
                text = "¡Dispositivo vinculado con éxito!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(2.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Icono")
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Ahora Mateo y tú están vinculados",
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ir a dashboard")
            }
        }
    }
}