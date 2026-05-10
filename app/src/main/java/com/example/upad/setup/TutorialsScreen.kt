package com.example.upad.setup

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialsScreen(
    onBackClick: () -> Unit,
    onFinishSetupClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tutoriales") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(2.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Video 1")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "¿Cómo crear una actividad?", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { }) {
                Text("Ver videotutorial")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .border(2.dp, Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text("Video 2")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "¿Cómo modificar los pasos?", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { }) {
                Text("Ver videotutorial")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onFinishSetupClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}