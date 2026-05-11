package com.example.upad.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routineTurn: String = "Mañana",
    childName: String = "Mateo",
    // Agregamos la lista como parámetro para que persista al volver de la otra pantalla
    pasosSeleccionados: List<String> = emptyList(),
    onBackClick: () -> Unit,
    onNavigateToPictogramSearch: () -> Unit,
    onSendRoutine: () -> Unit,
    drawableId: Int
) {
    var routineName by remember { mutableStateOf("") }
    var showSendingDialog by remember { mutableStateOf(false) }

    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorAmarilloTEA = Color(0xFFFFD54F)
    val colorFondoBase = Color(0xFFF0F4F8)

    // --- LÓGICA DEL DIÁLOGO ---
    if (showSendingDialog) {
        SendingRoutineDialog(
            routineTurn = routineTurn,
            childName = childName,
            onDismiss = { showSendingDialog = false }
        )

        LaunchedEffect(Unit) {
            delay(3000)
            showSendingDialog = false
            onSendRoutine()
        }
    }

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
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Text(
                text = "TURNO: ${routineTurn.uppercase()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Nueva Rutina",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = routineName,
                onValueChange = { routineName = it },
                label = { Text("Nombre de la Rutina (ej: Día de Escuela)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorAzulTEA),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PASOS DE LA RUTINA",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Button(
                    onClick = { /* IA logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = colorAmarilloTEA),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sugerir con IA", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- AQUÍ ESTÁ EL CAMBIO: USAMOS LA LISTA DINÁMICA ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Si la lista está vacía, podemos mostrar un mensajito o nada
                items(pasosSeleccionados) { paso ->
                    PasoItemCard(paso)
                }

                item {
                    OutlinedButton(
                        onClick = onNavigateToPictogramSearch,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, colorAzulTEA.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colorAzulTEA)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AGREGAR PASO", color = colorAzulTEA, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- PANEL DE ACCIÓN INFERIOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("CANCELAR", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Button(
                onClick = { showSendingDialog = true },
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
            ) {
                Text("ENVIAR", fontWeight = FontWeight.Black)
            }
        }
    }
}

// ... (Los componentes SendingRoutineDialog y PasoItemCard se mantienen igual)

// --- DIÁLOGO PERSONALIZADO ESTILO U-PAD ---
@Composable
fun SendingRoutineDialog(
    routineTurn: String,
    childName: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF4FC3F7),
                    strokeWidth = 5.dp,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Enviando rutina de\n$routineTurn a $childName...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    )
}

@Composable
fun PasoItemCard(nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = nombre,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}