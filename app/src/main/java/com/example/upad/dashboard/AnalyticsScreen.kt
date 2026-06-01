package com.example.upad.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.upad.viewmodel.RoutineViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    routineViewModel: RoutineViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPremium: () -> Unit
) {
    // 🔄 CONEXIÓN GLOBAL: Extraemos la paleta reactiva del tema
    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    // Paleta fija para los turnos (garantiza contraste en claro y oscuro)
    val colorTurnoManana = Color(0xFFFFB74D)
    val colorTurnoTarde = Color(0xFF81C784)
    val colorTurnoNoche = Color(0xFF9575CD)

    // Colores semánticos adaptativos para los banners premium
    val colorFondoIAActiva = Color(0xFF2E7D32).copy(alpha = 0.15f)
    val colorTextoIAActiva = Color(0xFF81C784)

    // --- 1. LEER ESTADO PREMIUM ACTUALIZADO SIN RESETEAR LA UI ---
    val isPremium by routineViewModel.isUserPremium.collectAsStateWithLifecycle()

    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    val diasSemana = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

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

    // --- 2. CÁLCULO GENERAL DE LA JORNADA ---
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
                title = { Text("Reporte Semanal", fontWeight = FontWeight.Bold, color = colorTextoPrincipal) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colorAcabadoPrincipal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorSuperficieTarjetas)
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
                colors = CardDefaults.cardColors(containerColor = colorAcabadoPrincipal),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(text = "Mañana", color = colorTurnoManana, icon = Icons.Default.LightMode, textColor = colorTextoPrincipal)
                    LegendItem(text = "Tarde", color = colorTurnoTarde, icon = Icons.Default.WbTwilight, textColor = colorTextoPrincipal)
                    LegendItem(text = "Noche", color = colorTurnoNoche, icon = Icons.Default.NightsStay, textColor = colorTextoPrincipal)
                }
            }

            Text(
                text = "CUMPLIMIENTO SEMANAL POR TURNOS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = colorTextoSecundario.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            // --- PANEL PRINCIPAL DEL GRÁFICO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    diasSemana.forEach { dia ->
                        val prefix = when(dia) {
                            "MIÉRCOLES" -> "MIÉ"
                            "SÁBADO" -> "SÁB"
                            else -> dia.take(3)
                        }

                        val mTasks = tasksManana.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }
                        val tTasks = tasksTarde.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }
                        val nTasks = tasksNoche.filter { it.dias.any { d -> d.uppercase().startsWith(prefix) } || it.dias.isEmpty() }

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
                                color = colorTextoPrincipal
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BarChartRow(progress = pManana, color = colorTurnoManana, icon = Icons.Default.LightMode, textColor = colorTextoPrincipal)
                                BarChartRow(progress = pTarde, color = colorTurnoTarde, icon = Icons.Default.WbTwilight, textColor = colorTextoPrincipal)
                                BarChartRow(progress = pNoche, color = colorTurnoNoche, icon = Icons.Default.NightsStay, textColor = colorTextoPrincipal)
                            }
                        }

                        if (dia != "DOMINGO") {
                            HorizontalDivider(color = colorFondoBase.copy(alpha = 0.5f), thickness = 1.dp)
                        }
                    }
                }
            }

            // --- 🤖 SECCIÓN MÓDULO DE IA (PREMIUM ADAPTATIVO) ---
            Text(
                text = "RECOMENDACIONES COMPORTAMENTALES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = colorTextoSecundario.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            if (isPremium) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colorFondoIAActiva),
                    border = BorderStroke(1.dp, colorTextoIAActiva.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colorTextoIAActiva, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("UPAD AI Mind Activo", fontWeight = FontWeight.Black, fontSize = 14.sp, color = colorTextoIAActiva)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Detectamos que el bloque de la 'Tarde' los jueves disminuye su efectividad. Sugerimos adelantar 15 minutos el pictograma de descanso para prevenir sobrecarga sensorial.",
                            fontSize = 13.sp,
                            color = colorTextoPrincipal,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                    border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = colorTextoSecundario.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Desbloquea el asistente de IA para predecir crisis y optimizar descansos.",
                                fontSize = 13.sp,
                                color = colorTextoSecundario,
                                lineHeight = 18.sp
                            )
                        }
                        TextButton(onClick = onNavigateToPremium) {
                            Text("PRO", color = colorAcabadoPrincipal, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun BarChartRow(progress: Float, color: Color, icon: ImageVector, textColor: Color) {
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
            tint = if (tieneTareas) color else textColor.copy(alpha = 0.2f),
            modifier = Modifier
                .padding(end = 8.dp)
                .size(16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(CircleShape)
                .background(textColor.copy(alpha = 0.1f))
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
            color = if (tieneTareas) textColor else textColor.copy(alpha = 0.3f),
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun LegendItem(text: String, color: Color, icon: ImageVector, textColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor.copy(alpha = 0.8f))
    }
}