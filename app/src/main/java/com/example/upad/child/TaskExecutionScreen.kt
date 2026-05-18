package com.example.upad.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth // <-- Importamos FirebaseAuth para el ID
import kotlinx.coroutines.delay

@Composable
fun TaskExecutionScreen(
    viewModel: RoutineViewModel,
    turn: String,
    onFinishRoutine: () -> Unit // Nota: Este ya no cerrará la app de golpe
) {
    val colorFondoNiño = Color(0xFFE1F5FE)
    val colorVerdeExito = Color(0xFF4CAF50)
    val colorNaranjaEspera = Color(0xFFFFB74D)

    // Obtenemos el ID del usuario actual de Firebase de forma segura
    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: "PADRE_TEST"
    }

    val tasks by when (turn.uppercase()) {
        "MAÑANA" -> viewModel.tasksManana.collectAsState()
        "TARDE" -> viewModel.tasksTarde.collectAsState()
        else -> viewModel.tasksNoche.collectAsState()
    }

    var currentTaskIndex by remember { mutableIntStateOf(0) }
    var mostrandoCelebracionIdivual by remember { mutableStateOf(false) }

    // Si el padre agrega actividades nuevas desde el adulto, reiniciamos el índice si es necesario
    LaunchedEffect(tasks.size) {
        if (currentTaskIndex >= tasks.size && tasks.isNotEmpty()) {
            // Si estábamos en modo descanso y el papá metió más tareas, regresamos a la acción
            currentTaskIndex = 0
        }
    }

    if (mostrandoCelebracionIdivual) {
        LaunchedEffect(Unit) {
            delay(2000) // 2 segundos de estrellitas animadas
            mostrandoCelebracionIdivual = false
            currentTaskIndex++ // Avanza en el contador
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoNiño),
        contentAlignment = Alignment.Center
    ) {
        if (tasks.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF0288D1))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Buscando tus actividades...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
            }
        }
        // --- ESTADO 1: ANIMACIÓN INTERMEDIA DE ESTRELLITAS ---
        else if (mostrandoCelebracionIdivual) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(text = "✨ ⭐ ✨", fontSize = 80.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "¡BIEN HECHO!", fontSize = 42.sp, fontWeight = FontWeight.Black, color = colorVerdeExito, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "✨ ¡Eres genial! ✨", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0288D1), textAlign = TextAlign.Center)
            }
        }
        // --- ESTADO 2: MIENTRAS EL NIÑO TENGA TAREAS EN LA LISTA ---
        else if (currentTaskIndex < tasks.size) {
            val currentTask = tasks[currentTaskIndex]

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Text(
                    text = "ACTIVIDAD ${currentTaskIndex + 1} DE ${tasks.size}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1).copy(alpha = 0.8f)
                )

                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier.size(300.dp).padding(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (currentTask.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = currentTask.imageUrl,
                                contentDescription = currentTask.actividad,
                                modifier = Modifier.fillMaxSize().padding(20.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text(
                                text = if (currentTask.actividad.isNotEmpty()) currentTask.actividad.take(1).uppercase() else "?",
                                fontSize = 90.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                Text(
                    text = currentTask.actividad.uppercase(),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "⏱️ Tiempo: ${currentTask.duration} min",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )

                // --- BOTÓN DE LOGRO CORREGIDO ---
                Button(
                    onClick = {
                        // Enviamos los 3 parámetros correctos incluyendo el ID dinámico del usuario
                        viewModel.completeTask(currentUserId, turn, currentTaskIndex)
                        mostrandoCelebracionIdivual = true
                    },
                    modifier = Modifier.fillMaxWidth(0.85f).height(80.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorVerdeExito),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (currentTaskIndex == tasks.size - 1) "¡TERMINAR! 🎉" else "¡HECHO! 👍",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
        // --- ESTADO 3: SOLUCIÓN AL MODO DESCANSO (ESPERA REACTIVA) ---
        else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text(
                    text = "🛋️ ✨ O_o",
                    fontSize = 90.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "¡TIEMPO DE DESCANSO!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1565C0),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Lo hiciste increíble. Puedes relajarte un momento mientras tu tutor prepara tu siguiente actividad.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                CircularProgressIndicator(
                    color = colorNaranjaEspera,
                    strokeWidth = 6.dp,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}