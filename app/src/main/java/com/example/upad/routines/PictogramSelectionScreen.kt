package com.example.upad.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
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

@Composable
fun PictogramSelectionScreen(
    onBackClick: () -> Unit,
    onPictogramSelected: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    // LISTA EXPANDIDA DE PICTOGRAMAS
    val mockPictograms = listOf(
        "LAVARSE LOS DIENTES", "IR AL BAÑO", "DUCHARSE", "PEINARSE",
        "DESAYUNAR", "ALMORZAR", "CENAR", "TOMAR AGUA",
        "VESTIRSE", "PONERSE ZAPATOS", "GUARDAR ROPA",
        "IR AL COLEGIO", "HACER TAREA", "LEER", "TERAPIA",
        "JUGAR SOLO", "COMPARTIR JUGUETES", "VER TV", "TABLET",
        "DORMIR", "ORDENAR CUARTO", "PASEO EN PARQUE"
    ).filter { it.contains(searchQuery.uppercase()) } // Filtro funcional básico

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
                text = "BIBLIOTECA DE APOYO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Selecciona un Paso",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // BUSCADOR FUNCIONAL
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Baño, Almuerzo...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = colorAzulTEA) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = colorAzulTEA,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // GRILLA
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(mockPictograms) { title ->
                    PictogramCard(
                        title = title,
                        onClick = { onPictogramSelected(title) }
                    )
                }
            }

            // BOTÓN DE CARGA
            Button(
                onClick = { /* Lógica Firebase */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = colorAzulTEA)
                Spacer(modifier = Modifier.width(12.dp))
                Text("AÑADIR FOTO PROPIA", color = colorAzulTEA, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PictogramCard(title: String, onClick: () -> Unit) {
    val colorAzulTEA = Color(0xFF4FC3F7)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f) // Mantiene las tarjetas proporcionales
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp).fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFF8F9FA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = colorAzulTEA.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp, // Un poco más pequeño para que quepan textos largos
                color = Color.DarkGray,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
    @Composable
    fun SendingRoutineDialog(
        routineTurn: String,
        childName: String,
        onDismiss: () -> Unit
    ) {
        val colorAzulTEA = Color(0xFF4FC3F7)

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {}, // No necesitamos botones, se cierra al terminar
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Indicador de carga estilizado
                    CircularProgressIndicator(
                        color = colorAzulTEA,
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

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Casi listo",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        )
    }
}