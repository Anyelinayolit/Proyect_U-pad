package com.example.upad.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Clase de datos para representar una rutina con su estado
data class RoutineItem(
    val name: String,
    val icon: ImageVector,
    val totalTasks: Int,
    val completedTasks: Int,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDashboardScreen(
    parentName: String = "Laura",
    childName: String = "Mateo",
    onNavigateToCreateRoutine: () -> Unit,
    onRoutineClick: (String) -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    // Datos simulados con progreso
    val routines = listOf(
        RoutineItem("MAÑANA", Icons.Default.LightMode, 5, 3, Color(0xFFFFB74D)),
        RoutineItem("TARDE", Icons.Default.WbTwilight, 4, 1, Color(0xFF81C784)),
        RoutineItem("NOCHE", Icons.Default.NightsStay, 6, 0, Color(0xFF9575CD))
    )

    Scaffold(
        containerColor = colorFondoBase,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRoutine,
                containerColor = colorAzulTEA,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear nueva rutina", modifier = Modifier.size(30.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- CABECERA ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                    .background(Color.White)
                    .padding(32.dp)
            ) {
                Text(
                    text = "¡HOLA, $parentName!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.LightGray
                )
                Text(
                    text = "Rutinas de $childName",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colorAzulTEA
                )
            }

            // --- LISTA DE RUTINAS ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(routines) { routine ->
                    RoutineProgressCard(
                        routine = routine,
                        onClick = { onRoutineClick(routine.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun RoutineProgressCard(
    routine: RoutineItem,
    onClick: () -> Unit
) {
    val progress = routine.completedTasks.toFloat() / routine.totalTasks.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icono de la rutina con fondo circular suave
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(routine.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = routine.icon,
                            contentDescription = null,
                            tint = routine.color,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = routine.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${routine.completedTasks} de ${routine.totalTasks} tareas",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Porcentaje de progreso
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = routine.color
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // BARRA DE PROGRESO
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = routine.color,
                trackColor = routine.color.copy(alpha = 0.1f),
            )
        }
    }
}