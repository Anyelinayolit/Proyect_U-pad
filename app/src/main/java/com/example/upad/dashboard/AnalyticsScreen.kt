package com.example.upad.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.viewmodel.RoutineViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    // --- 1. LEER FLUJOS EN TIEMPO REAL DESDE EL VIEWMODEL ---
    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    val diasSemana = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

    // --- 📅 DETECTOR DEL DÍA DE HOY PARA EL RESUMEN SUPERIOR ---
    val diaActualTexto = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "LUN"
            Calendar.TUESDAY -> "MAR"
            Calendar.WEDNESDAY -> "MIÉ"
            Calendar.THURSDAY -> "JUE"
            Calendar.FRIDAY -> "VIE"
            Calendar.SATURDAY -> "SÁB"
            Calendar.SUNDAY -> "DOM"
            else -> "LUN"
        }
    }

    // --- 2. CÁLCULO GENERAL DE LA JORNADA PARA EL DÍA ACTUAL ---
    val completedManana = tasksManana.count { it.estaCompletadaHoy(diaActualTexto) }
    val completedTarde = tasksTarde.count { it.estaCompletadaHoy(diaActualTexto) }
    val completedNoche = tasksNoche.count { it.estaCompletadaHoy(diaActualTexto) }

    val totalTasksAsignadas = tasksManana.size + tasksTarde.size + tasksNoche.size
    val totalTasksCompletadas = completedManana + completedTarde + completedNoche

    val generalProgress = if (totalTasksAsignadas > 0) totalTasksCompletadas.toFloat() / totalTasksAsignadas.toFloat() else 0f
    val generalPercentage = (generalProgress * 100).toInt()

    Scaffold(
        containerColor = colorFondoBase,
        topBar = {
            TopAppBar(
                title = { Text("Reporte Semanal", fontWeight = FontWeight.Bold, color = Color.DarkGray) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorAzulTEA)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TARJETA DE RESUMEN DE RENDIMIENTO (HOY) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorAzulTEA),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Efectividad de Hoy ($diaActualTexto)", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (totalTasksAsignadas > 0) "$generalPercentage% Logrado" else "Sin actividad",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Progreso distribuido por los tres turnos diarios del niño.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // --- LEYENDA DEL GRÁFICO DE BARRAS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(text = "Mañana", color = Color(0xFFFFB74D), icon = Icons.Default.LightMode)
                    LegendItem(text = "Tarde", color = Color(0xFF81C784), icon = Icons.Default.WbTwilight)
                    LegendItem(text = "Noche", color = Color(0xFF9575CD), icon = Icons.Default.NightsStay)
                }
            }

            Text(
                text = "CUMPLIMIENTO SEMANAL POR TURNOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp)
            )

            // --- PANEL PRINCIPAL DEL GRÁFICO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    diasSemana.forEach { dia ->
                        // Normalizamos la clave del día actual del bucle a 3 letras estandarizadas (LUN, MAR, MIÉ...)
                        val prefix = when(dia) {
                            "MIÉRCOLES" -> "MIÉ"
                            "SÁBADO" -> "SÁB"
                            else -> dia.take(3)
                        }

                        // Filtrado de las rutinas según el día analizado
                        val mTasks = tasksManana.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }
                        val tTasks = tasksTarde.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }
                        val nTasks = tasksNoche.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }

                        // 🔥 REEMPLAZADO ROJO: Buscamos cuántas están completas usando el prefijo dinámico del bucle
                        val pManana = if (mTasks.isNotEmpty()) mTasks.count { it.estaCompletadaHoy(prefix) }.toFloat() / mTasks.size.toFloat() else -1f
                        val pTarde = if (tTasks.isNotEmpty()) tTasks.count { it.estaCompletadaHoy(prefix) }.toFloat() / tTasks.size.toFloat() else -1f
                        val pNoche = if (nTasks.isNotEmpty()) nTasks.count { it.estaCompletadaHoy(prefix) }.toFloat() / nTasks.size.toFloat() else -1f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = dia.lowercase().replaceFirstChar { it.uppercase() },
                                modifier = Modifier
                                    .width(75.dp)
                                    .padding(top = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BarChartRow(progress = pManana, color = Color(0xFFFFB74D), icon = Icons.Default.LightMode)
                                BarChartRow(progress = pTarde, color = Color(0xFF81C784), icon = Icons.Default.WbTwilight)
                                BarChartRow(progress = pNoche, color = Color(0xFF9575CD), icon = Icons.Default.NightsStay)
                            }
                        }

                        if (dia != "DOMINGO") {
                            HorizontalDivider(color = colorFondoBase.copy(alpha = 0.8f), thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BarChartRow(
    progress: Float,
    color: Color,
    icon: ImageVector
) {
    val tieneTareas = progress >= 0f
    val barraProgresoAnimada by animateFloatAsState(
        targetValue = if (tieneTareas) progress else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "BarraAnimada"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (tieneTareas) color else Color.LightGray.copy(alpha = 0.6f),
            modifier = Modifier
                .padding(end = 8.dp)
                .size(16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0E0E0).copy(alpha = 0.6f))
        ) {
            if (tieneTareas && barraProgresoAnimada > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = barraProgresoAnimada)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = if (tieneTareas) "${(progress * 100).toInt()}%" else "--",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = if (tieneTareas) Color.DarkGray else Color.LightGray,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun LegendItem(text: String, color: Color, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.6f.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}