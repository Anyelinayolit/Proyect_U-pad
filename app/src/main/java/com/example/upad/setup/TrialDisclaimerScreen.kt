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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrialDisclaimerScreen(
    onStartTrialClick: () -> Unit,
    onMoreInfoClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .border(2.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            Text("Icono App")
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Comience su prueba",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Esta prueba le dará un punto de partida para la intervención y el apoyo adecuado para su hijo para sus rutinas.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartTrialClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vale, empecemos")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Más información",
            color = Color.Blue,
            modifier = Modifier.clickable { onMoreInfoClick() }
        )
    }
}