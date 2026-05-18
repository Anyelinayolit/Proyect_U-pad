package com.example.upad.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    onNavigateToConnection: () -> Unit = {}
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoBase = Color(0xFFF0F4F8)

    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    val currentUser = firebaseAuth.currentUser
    val context = androidx.compose.ui.platform.LocalContext.current

    // 🛠️ LEER DIRECTAMENTE DESDE SHAREDPREFERENCES AL INICIAR
    var parentName by remember {
        mutableStateOf(
            context.getSharedPreferences("UPAD_PREFS", android.content.Context.MODE_PRIVATE)
                .getString("PARENT_NAME", "PADRE/TUTOR") ?: "PADRE/TUTOR"
        )
    }

    // Por si acaso cambia el usuario en caliente, mantenemos el respaldo de Firebase
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val emailName = currentUser.email?.substringBefore("@")?.uppercase()
            val nameToDisplay = currentUser.displayName?.uppercase() ?: emailName
            if (!nameToDisplay.isNullOrBlank()) {
                parentName = nameToDisplay
                // Actualizamos el disco por si acaso
                context.getSharedPreferences("UPAD_PREFS", android.content.Context.MODE_PRIVATE)
                    .edit().putString("PARENT_NAME", nameToDisplay).apply()
            }
        }
    }

    val childText = "tu hijo"

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // --- DETECCIÓN VEHICULAR DEL DÍA ACTUAL DE LA SEMANA ---
    val diasSemana = listOf("LUNES", "MARTES", "MIÉRCOLES", "JUEVES", "VIERNES", "SÁBADO", "DOMINGO")

    val diaDeHoy = remember {
        val formato = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()).uppercase()
        val formateado = formato.replace("É", "E").replace("Í", "I").replace("Á", "A")
        if (diasSemana.contains(formateado)) formateado else "LUNES"
    }

    // Inicializa de forma nativa en el día real en curso
    var diaSeleccionado by remember { mutableStateOf(diaDeHoy) }
    var mostrarCalendarioCompleto by remember { mutableStateOf(false) }

    // Escucha directa de flujos desde la nube
    val allTasksManana by routineViewModel.tasksManana.collectAsState()
    val allTasksTarde by routineViewModel.tasksTarde.collectAsState()
    val allTasksNoche by routineViewModel.tasksNoche.collectAsState()

    // --- FILTRADO ADAPTATIVO POR DÍAS CORREGIDO ---
    val tasksMananaFiltradas = allTasksManana.filter { task ->
        task.dias.any { it.uppercase().startsWith(diaSeleccionado.take(3)) } || task.dias.isEmpty()
    }

    val tasksTardeFiltradas = allTasksTarde.filter { task ->
        task.dias.any { it.uppercase().startsWith(diaSeleccionado.take(3)) } || task.dias.isEmpty()
    }

    val tasksNocheFiltradas = allTasksNoche.filter { task ->
        task.dias.any { it.uppercase().startsWith(diaSeleccionado.take(3)) } || task.dias.isEmpty()
    }

    val routines = listOf(
        RoutineItem(
            name = "MAÑANA",
            icon = Icons.Default.LightMode,
            totalTasks = tasksMananaFiltradas.size,
            completedTasks = tasksMananaFiltradas.count { it.isCompleted },
            color = Color(0xFFFFB74D)
        ),
        RoutineItem(
            name = "TARDE",
            icon = Icons.Default.WbTwilight,
            totalTasks = tasksTardeFiltradas.size,
            completedTasks = tasksTardeFiltradas.count { it.isCompleted },
            color = Color(0xFF81C784)
        ),
        RoutineItem(
            name = "NOCHE",
            icon = Icons.Default.NightsStay,
            totalTasks = tasksNocheFiltradas.size,
            completedTasks = tasksNocheFiltradas.count { it.isCompleted },
            color = Color(0xFF9575CD)
        )
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorAzulTEA)
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
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, tint = colorAzulTEA) },
                    label = { Text("Análisis de Desempeño", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToAnalytics() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Link, contentDescription = null, tint = colorAzulTEA) },
                    label = { Text("Conectar con el Niño (Código)", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToConnection() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null, tint = colorAzulTEA) },
                    label = { Text("Mi Perfil", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToProfile() },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = colorAzulTEA) },
                    label = { Text("Ajustes", fontWeight = FontWeight.Medium) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNavigateToSettings() },
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
                    containerColor = colorAzulTEA,
                    contentColor = Color.White,
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
                            .background(Color.White)
                            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
                    ) {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.align(Alignment.Start)
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Abrir menú", tint = colorAzulTEA, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "¡HOLA, $parentName!", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.LightGray)
                        Text(text = "Rutinas de $childText", fontSize = 28.sp, fontWeight = FontWeight.Black, color = colorAzulTEA)
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
                                text = if (diaSeleccionado == diaDeHoy) "PROGRAMA DE HOY ($diaDeHoy)" else "PROGRAMA DEL $diaSeleccionado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (diaSeleccionado == diaDeHoy) colorAzulTEA else Color.Gray
                            )

                            IconButton(
                                onClick = { mostrarCalendarioCompleto = !mostrarCalendarioCompleto },
                                modifier = Modifier.background(if (mostrarCalendarioCompleto) colorAzulTEA.copy(alpha = 0.15f) else Color.Transparent, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Planificar otros días",
                                    tint = if (mostrarCalendarioCompleto) Color(0xFF0288D1) else colorAzulTEA
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
                                            if (esElSeleccionado) colorAzulTEA
                                            else if (esHoyReal) colorAzulTEA.copy(alpha = 0.1f)
                                            else Color.White
                                        )
                                        .clickable { diaSeleccionado = dia }
                                        .padding(horizontal = 18.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dia,
                                        color = if (esElSeleccionado) Color.White else if (esHoyReal) colorAzulTEA else Color.DarkGray,
                                        fontWeight = if (esElSeleccionado || esHoyReal) FontWeight.Black else FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(visible = mostrarCalendarioCompleto) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "PLANIFICACIÓN SEMANAL COMPLETA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                                Row(modifier = Modifier.fillMaxWidth().background(colorFondoBase, RoundedCornerShape(8.dp)).padding(6.dp)) {
                                    Text("Día", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Mañana", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    Text("Tarde", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                    Text("Noche", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.Center)
                                }
                                diasSemana.forEach { dia ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { diaSeleccionado = dia; mostrarCalendarioCompleto = false }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = dia.take(3) + ".", modifier = Modifier.weight(1.2f), fontWeight = if (dia == diaSeleccionado) FontWeight.Black else FontWeight.Normal, color = if (dia == diaSeleccionado) Color(0xFF0288D1) else Color.DarkGray, fontSize = 13.sp)
                                        Text("☀️", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp)
                                        Text("🌤️", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp)
                                        Text("🌙", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "BLOQUES DE ACTIVIDAD - $diaSeleccionado",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 24.dp, top = 8.dp, end = 24.dp)
                    )
                }

                items(routines) { routine ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        RoutineProgressCard(
                            routine = routine,
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
    onClick: () -> Unit
) {
    val progress = if (routine.totalTasks > 0) routine.completedTasks.toFloat() / routine.totalTasks.toFloat() else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Text(text = routine.name, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.DarkGray)
                        Text(text = "${routine.completedTasks} de ${routine.totalTasks} tareas", fontSize = 14.sp, color = Color.Gray)
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