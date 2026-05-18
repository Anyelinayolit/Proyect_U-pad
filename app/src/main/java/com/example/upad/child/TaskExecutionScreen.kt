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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun TaskExecutionScreen(
    viewModel: RoutineViewModel,
    turn: String,
    onFinishRoutine: () -> Unit
) {
    val colorFondoNiño = Color(0xFFE1F5FE)
    val colorVerdeExito = Color(0xFF4CAF50)
    val colorNaranjaEspera = Color(0xFFFFB74D)

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

    LaunchedEffect(tasks.size) {
        if (currentTaskIndex >= tasks.size && tasks.isNotEmpty()) {
            currentTaskIndex = 0
        }
    }

    if (mostrandoCelebracionIdivual) {
        LaunchedEffect(Unit) {
            delay(2000)
            mostrandoCelebracionIdivual = false
            currentTaskIndex++
        }
    }

    val diaActualTexto = remember {
        when (java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.MONDAY -> "LUN"
            java.util.Calendar.TUESDAY -> "MAR"
            java.util.Calendar.WEDNESDAY -> "MIÉ"
            java.util.Calendar.THURSDAY -> "JUE"
            java.util.Calendar.FRIDAY -> "VIE"
            java.util.Calendar.SATURDAY -> "SÁB"
            java.util.Calendar.SUNDAY -> "DOM"
            else -> "LUN"
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

                // --- 🛠️ BOTÓN DE LOGRO TOTALMENTE REPARADO ---
                Button(
                    onClick = {
                        // Cambiamos la función vieja por la inteligente basada en nombre y fecha actual
                        viewModel.completeTaskPorNombre(
                            userId = currentUserId,
                            turn = turn,
                            actividadTexto = currentTask.actividad,
                            diaActual = diaActualTexto
                        )
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
                    text = "Lo hiciste increíble. Puedes relajarte un momento mientras tu tutor prepara tu siguiente activity.",
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