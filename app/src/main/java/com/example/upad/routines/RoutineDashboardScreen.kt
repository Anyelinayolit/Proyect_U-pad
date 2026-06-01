package com.example.upad.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.Star
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    routineViewModel: com.example.upad.viewmodel.RoutineViewModel,
    onNavigateToCreateRoutine: (String) -> Unit,
    onRoutineClick: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToConnection: () -> Unit = {},
    onNavigateToDeviceManagement: () -> Unit = {},
    onNavigateToChangePlan: () -> Unit = {}
) {
    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser
    val context = androidx.compose.ui.platform.LocalContext.current

    val esPremium by routineViewModel.isUserPremium.collectAsState()
    val isDarkMode by routineViewModel.isDarkMode.collectAsState()

    val colorDinamicoSuscripcion = if (esPremium) Color(0xFFC5A059) else colorAcabadoPrincipal

    var parentName by remember {
        mutableStateOf(
            context.getSharedPreferences("UPAD_PREFS", android.content.Context.MODE_PRIVATE)
                .getString("PARENT_NAME", "PADRE/TUTOR") ?: "PADRE/TUTOR"
        )
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val emailName = currentUser.email?.substringBefore("@")?.uppercase()
            val nameToDisplay = currentUser.displayName?.uppercase() ?: emailName
            if (!nameToDisplay.isNullOrBlank()) {
                parentName = nameToDisplay
                context.getSharedPreferences("UPAD_PREFS", android.content.Context.MODE_PRIVATE)
                    .edit().putString("PARENT_NAME", nameToDisplay).apply()
            }
        }
    }

    val childText = "tu hijo"
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val diasSemana = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

    val diaDeHoy = remember {
        val formato = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).uppercase()
        val formateado = formato.replace("É", "E").replace("Í", "I").replace("Á", "A")
        if (diasSemana.contains(formateado)) formateado else "LUNES"
    }

    // El número real de hoy del sistema (ej: 1, 15, 28...)
    val numeroDeHoyReal = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH) }

    var diaSeleccionado by remember { mutableStateOf(diaDeHoy) }
    var diaNumeroSeleccionado by remember { mutableStateOf(numeroDeHoyReal) }
    var mostrarCalendarioCompleto by remember { mutableStateOf(false) }

    // --- 📅 MOTOR DE CALENDARIO MENSUAL ESTILO GOOGLE CALENDAR ---
    val infoMesActual = remember {
        val cal = Calendar.getInstance()
        val nombreMes = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale("es", "ES"))?.uppercase() ?: ""
        val anio = cal.get(Calendar.YEAR)
        val maxDias = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val listaDiasDelMes = (1..maxDias).map { dia ->
            val tempCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, dia) }
            val nombreDiaAsignado = when (tempCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "LUNES"
                Calendar.TUESDAY -> "MARTES"
                Calendar.WEDNESDAY -> "MIÉRCOLES"
                Calendar.THURSDAY -> "JUEVES"
                Calendar.FRIDAY -> "VIERNES"
                Calendar.SATURDAY -> "SÁBADO"
                Calendar.SUNDAY -> "DOMINGO"
                else -> "LUNES"
            }
            Pair(dia, nombreDiaAsignado)
        }
        Triple("$nombreMes $anio", maxDias, listaDiasDelMes)
    }

    val tituloMes = infoMesActual.first
    val diasDelMesArray = infoMesActual.third

    val allTasksManana by routineViewModel.tasksManana.collectAsState()
    val allTasksTarde by routineViewModel.tasksTarde.collectAsState()
    val allTasksNoche by routineViewModel.tasksNoche.collectAsState()

    val prefijoDiaSeleccionado = when (diaSeleccionado) {
        "MIÉRCOLES", "MIERCOLES" -> "MIÉ"
        "SÁBADO", "SABADO" -> "SÁB"
        else -> diaSeleccionado.take(3)
    }

    val tasksMananaFiltradas = allTasksManana.filter { task ->
        task.dias.isEmpty() || task.dias.any { it.uppercase().trim().startsWith(prefijoDiaSeleccionado.uppercase()) }
    }

    val tasksTardeFiltradas = allTasksTarde.filter { task ->
        task.dias.isEmpty() || task.dias.any { it.uppercase().trim().startsWith(prefijoDiaSeleccionado.uppercase()) }
    }

    val tasksNocheFiltradas = allTasksNoche.filter { task ->
        task.dias.isEmpty() || task.dias.any { it.uppercase().trim().startsWith(prefijoDiaSeleccionado.uppercase()) }
    }

    val routines = listOf(
        RoutineItem(
            name = "MAÑANA",
            icon = Icons.Default.LightMode,
            totalTasks = tasksMananaFiltradas.size,
            completedTasks = tasksMananaFiltradas.count { it.estaCompletadaHoy(prefijoDiaSeleccionado) },
            color = Color(0xFFFFB74D)
        ),
        RoutineItem(
            name = "TARDE",
            icon = Icons.Default.WbTwilight,
            totalTasks = tasksTardeFiltradas.size,
            completedTasks = tasksTardeFiltradas.count { it.estaCompletadaHoy(prefijoDiaSeleccionado) },
            color = Color(0xFF81C784)
        ),
        RoutineItem(
            name = "NOCHE",
            icon = Icons.Default.NightsStay,
            totalTasks = tasksNocheFiltradas.size,
            completedTasks = tasksNocheFiltradas.count { it.estaCompletadaHoy(prefijoDiaSeleccionado) },
            color = Color(0xFF9575CD)
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colorSuperficieTarjetas,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorDinamicoSuscripcion)
                        .padding(24.dp)
                ) {
                    Column {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = parentName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = currentUser?.email ?: "Gestor de Rutinas", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val drawerColors = NavigationDrawerItemDefaults.colors(
                    unselectedIconColor = colorTextoSecundario,
                    unselectedTextColor = colorTextoPrincipal
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700)) },
                    label = {
                        Text(
                            text = if (esPremium) "Cambiar a Plan Básico" else "Cambiar a Plan Premium",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        if (esPremium) {
                            routineViewModel.cancelPremium()
                        } else {
                            onNavigateToChangePlan()
                        }
                    },
                    colors = drawerColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Análisis de Desempeño", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToAnalytics() },
                    colors = drawerColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    label = { Text("Bloquear Dispositivo", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onNavigateToDeviceManagement()
                    },
                    colors = drawerColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Link, contentDescription = null) },
                    label = { Text("Conectar con el Niño (Código)", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToConnection() },
                    colors = drawerColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Mi Perfil", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                    colors = drawerColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Ajustes", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToSettings() },
                    colors = drawerColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            containerColor = colorFondoBase,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onNavigateToCreateRoutine(diaSeleccionado) },
                    containerColor = colorDinamicoSuscripcion,
                    contentColor = if (esPremium && !isDarkMode) Color.Black else Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Crear nueva rutina", modifier = Modifier.size(30.dp))
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                            .background(colorSuperficieTarjetas)
                            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú", tint = colorDinamicoSuscripcion, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "¡HOLA, $parentName!", fontSize = 14.sp, fontWeight = FontWeight.Black, color = colorTextoSecundario.copy(alpha = 0.6f))

                        Text(
                            text = if (esPremium) "Rutinas de $childText ⭐" else "Rutinas de $childText",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = colorDinamicoSuscripcion
                        )
                    }
                }

                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, top = 4.dp, end = 24.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (diaSeleccionado == diaDeHoy && diaNumeroSeleccionado == numeroDeHoyReal) {
                                    "PROGRAMA DE HOY ($diaDeHoy $numeroDeHoyReal)"
                                } else {
                                    "PROGRAMA DEL $diaSeleccionado" + if (diaNumeroSeleccionado > 0) " $diaNumeroSeleccionado" else ""
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (diaSeleccionado == diaDeHoy && diaNumeroSeleccionado == numeroDeHoyReal) colorDinamicoSuscripcion else colorTextoSecundario
                            )

                            IconButton(
                                onClick = { mostrarCalendarioCompleto = !mostrarCalendarioCompleto },
                                modifier = Modifier.background(if (mostrarCalendarioCompleto) colorDinamicoSuscripcion.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Planificar otros días",
                                    tint = colorDinamicoSuscripcion
                                )
                            }
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(diasSemana) { dia ->
                                val esElSeleccionado = dia == diaSeleccionado
                                val esHoyReal = dia == diaDeHoy
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (esElSeleccionado) colorDinamicoSuscripcion
                                            else if (esHoyReal) colorDinamicoSuscripcion.copy(alpha = 0.15f)
                                            else colorSuperficieTarjetas
                                        )
                                        .clickable {
                                            diaSeleccionado = dia
                                            // Si coincide con hoy de forma real, le volvemos a poner su número de hoy, sino ocultamos el número de forma sutil
                                            diaNumeroSeleccionado = if (dia == diaDeHoy) numeroDeHoyReal else -1
                                        }
                                        .padding(horizontal = 18.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dia,
                                        color = if (esElSeleccionado) {
                                            if (esPremium && !isDarkMode) Color.Black else Color.White
                                        } else if (esHoyReal) {
                                            colorDinamicoSuscripcion
                                        } else {
                                            colorTextoPrincipal
                                        },
                                        fontWeight = if (esElSeleccionado || esHoyReal) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 🗓️ CALENDARIO INTERACTIVO ESTILO GOOGLE CALENDAR ---
                item {
                    AnimatedVisibility(visible = mostrarCalendarioCompleto) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.12f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tituloMes,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colorDinamicoSuscripcion,
                                        letterSpacing = 0.5.sp
                                    )
                                    Text(
                                        text = "VISTA MENSUAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorTextoSecundario.copy(alpha = 0.6f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val inicialesDias = listOf("D", "L", "M", "M", "J", "V", "S")
                                    inicialesDias.forEach { letra ->
                                        Text(
                                            text = letra,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorTextoSecundario.copy(alpha = 0.5f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val calendarAux = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
                                val espacioVacioInicial = calendarAux.get(Calendar.DAY_OF_WEEK) - 1

                                var diaProcesadoIndex = 0
                                val totalCeldasNecesarias = espacioVacioInicial + diasDelMesArray.size
                                val filasDeSemanas = (totalCeldasNecesarias + 6) / 7

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (semana in 0 until filasDeSemanas) {
                                        Row(modifier = Modifier.fillMaxWidth()) {
                                            for (diaSemanaFila in 0 until 7) {
                                                val posicionCelda = semana * 7 + diaSemanaFila

                                                if (posicionCelda < espacioVacioInicial || diaProcesadoIndex >= diasDelMesArray.size) {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                } else {
                                                    val datosDelDia = diasDelMesArray[diaProcesadoIndex]
                                                    val numeroDia = datosDelDia.first
                                                    val nombreDiaCompleto = datosDelDia.second

                                                    val esElSeleccionadoHoy = numeroDia == diaNumeroSeleccionado
                                                    val esDiaActualDelMes = numeroDia == numeroDeHoyReal

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(1f)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when {
                                                                    esElSeleccionadoHoy -> colorDinamicoSuscripcion
                                                                    esDiaActualDelMes -> colorDinamicoSuscripcion.copy(alpha = 0.15f)
                                                                    else -> Color.Transparent
                                                                }
                                                            )
                                                            .clickable {
                                                                diaSeleccionado = nombreDiaCompleto
                                                                diaNumeroSeleccionado = numeroDia
                                                                mostrarCalendarioCompleto = false
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = numeroDia.toString(),
                                                            fontSize = 13.sp,
                                                            fontWeight = if (esElSeleccionadoHoy || esDiaActualDelMes) FontWeight.Black else FontWeight.Medium,
                                                            color = when {
                                                                esElSeleccionadoHoy -> if (esPremium && !isDarkMode) Color.Black else Color.White
                                                                esDiaActualDelMes -> colorDinamicoSuscripcion
                                                                else -> colorTextoPrincipal
                                                            }
                                                        )
                                                    }
                                                    diaProcesadoIndex++
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // --- 🎯 ACTUALIZACIÓN DE ETIQUETA DINÁMICA CON NÚMERO DE DÍA ---
                item {
                    Text(
                        text = if (diaNumeroSeleccionado > 0) {
                            "BLOQUES DE ACTIVIDAD - $diaSeleccionado $diaNumeroSeleccionado"
                        } else {
                            "BLOQUES DE ACTIVIDAD - $diaSeleccionado"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTextoSecundario,
                        modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp)
                    )
                }

                items(routines) { routine ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        RoutineProgressCard(
                            routine = routine,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorTextoSec = colorTextoSecundario,
                            onClick = { onRoutineClick(routine.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineProgressCard(
    routine: RoutineItem,
    colorSuperficie: Color,
    colorTexto: Color,
    colorTextoSec: Color,
    onClick: () -> Unit
) {
    val progress = if (routine.totalTasks > 0) routine.completedTasks.toFloat() / routine.totalTasks.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, colorTextoSec.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(routine.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = routine.icon, contentDescription = null, tint = routine.color, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = routine.name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = colorTexto)
                        Text(text = "${routine.completedTasks} de ${routine.totalTasks} tareas", fontSize = 14.sp, color = colorTextoSec)
                    }
                }
                Text(text = "${(progress * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Black, color = routine.color)
            }
            Spacer(modifier = Modifier.height(20.dp))
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