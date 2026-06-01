package com.example.upad.child

import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

@Composable
fun ChildStartScreen(
    routineViewModel: RoutineViewModel,
    onNavigateToTask: (actividadNombre: String, turno: String) -> Unit,
    onNavigateToCompleted: () -> Unit
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoNiño = Color(0xFFE1F5FE)
    val context = LocalContext.current

    val deviceId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }

    var codigoNiño by remember { mutableStateOf("------") }
    var estaVinculado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var padreIdAsociado by remember { mutableStateOf("") }

    // 🎯 Estado local para forzar el modo Premium en la pantalla del niño si el padre lo es
    var esPremiumPorPadre by remember { mutableStateOf(false) }

    val database = remember { FirebaseDatabase.getInstance().reference }

    // --- 📡 ESCUCHA CONSTANTE EN TIEMPO REAL DESDE LA NUBE ---
    LaunchedEffect(deviceId) {
        database.child("dispositivos_niños").child(deviceId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("padreId")) {
                        val pId = snapshot.child("padreId").getValue(String::class.java) ?: ""
                        padreIdAsociado = pId
                        estaVinculado = true

                        routineViewModel.cargarRutinasDesdeFirebase(pId)

                        // 🔍 ESCUCHA PREMIUM: Buscamos si el padre es premium en la base de datos
                        database.child("usuarios").child(pId).child("isPremium")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    esPremiumPorPadre = userSnapshot.getValue(Boolean::class.java) ?: false
                                    cargando = false
                                }
                                override fun onCancelled(error: DatabaseError) { cargando = false }
                            })
                    } else {
                        val nuevoCodigo = (100000..999999).random().toString()
                        codigoNiño = nuevoCodigo

                        val datosConexion = mapOf(
                            "deviceId" to deviceId,
                            "estado" to "esperando",
                            "padreId" to ""
                        )
                        database.child("codigos_vinculacion").child(nuevoCodigo).setValue(datosConexion)

                        database.child("codigos_vinculacion").child(nuevoCodigo)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(codeSnapshot: DataSnapshot) {
                                    if (codeSnapshot.exists()) {
                                        val estado = codeSnapshot.child("estado").getValue(String::class.java)
                                        val padreId = codeSnapshot.child("padreId").getValue(String::class.java)

                                        if (estado == "enlazado" && !padreId.isNullOrEmpty()) {
                                            database.child("dispositivos_niños").child(deviceId).child("padreId").setValue(padreId)
                                            database.child("codigos_vinculacion").child(nuevoCodigo).removeValue()

                                            padreIdAsociado = padreId
                                            estaVinculado = true
                                            routineViewModel.cargarRutinasDesdeFirebase(padreId)

                                            // 🔍 ESCUCHA PREMIUM (Al vincular por primera vez)
                                            database.child("usuarios").child(padreId).child("isPremium")
                                                .addValueEventListener(object : ValueEventListener {
                                                    override fun onDataChange(userSnapshot: DataSnapshot) {
                                                        esPremiumPorPadre = userSnapshot.getValue(Boolean::class.java) ?: false
                                                        cargando = false
                                                    }
                                                    override fun onCancelled(error: DatabaseError) { cargando = false }
                                                })
                                        } else {
                                            cargando = false
                                        }
                                    }
                                }
                                override fun onCancelled(error: DatabaseError) { cargando = false }
                            })
                    }
                }
                override fun onCancelled(error: DatabaseError) { cargando = false }
            })
    }

    // --- ESCUCHA EN TIEMPO REAL DESDE EL VIEWMODEL ---
    val tasksManana by routineViewModel.tasksManana.collectAsState()
    val tasksTarde by routineViewModel.tasksTarde.collectAsState()
    val tasksNoche by routineViewModel.tasksNoche.collectAsState()

    // --- CÁLCULO DE FILTROS DIARIOS ---
    val calendar = Calendar.getInstance()
    val numeroDia = calendar.get(Calendar.DAY_OF_WEEK)
    val horaActual = calendar.get(Calendar.HOUR_OF_DAY)

    val diaActualTexto = when (numeroDia) {
        Calendar.MONDAY -> "LUN"
        Calendar.TUESDAY -> "MAR"
        Calendar.WEDNESDAY -> "MIÉ"
        Calendar.THURSDAY -> "JUE"
        Calendar.FRIDAY -> "VIE"
        Calendar.SATURDAY -> "SÁB"
        Calendar.SUNDAY -> "DOM"
        else -> "LUN"
    }

    val listaVariacionesDia = when (numeroDia) {
        Calendar.MONDAY -> listOf("LUN", "LUNES")
        Calendar.TUESDAY -> listOf("MAR", "MARTES")
        Calendar.WEDNESDAY -> listOf("MIÉ", "MIERCOLES", "MIÉRCOLES")
        Calendar.THURSDAY -> listOf("JUE", "JUEVES")
        Calendar.FRIDAY -> listOf("VIE", "VIERNES")
        Calendar.SATURDAY -> listOf("SÁB", "SABADO", "SÁBADO")
        Calendar.SUNDAY -> listOf("DOM", "DOMINGO")
        else -> emptyList()
    }

    val mañanaFiltradas = tasksManana.filter { t -> t.dias.isEmpty() || t.dias.any { d -> listaVariacionesDia.contains(d.uppercase().trim()) } }
    val tardeFiltradas = tasksTarde.filter { t -> t.dias.isEmpty() || t.dias.any { d -> listaVariacionesDia.contains(d.uppercase().trim()) } }
    val nocheFiltradas = tasksNoche.filter { t -> t.dias.isEmpty() || t.dias.any { d -> listaVariacionesDia.contains(d.uppercase().trim()) } }

    val pendientesManana = mañanaFiltradas.count { !it.estaCompletadaHoy(diaActualTexto) }
    val pendientesTarde = tardeFiltradas.count { !it.estaCompletadaHoy(diaActualTexto) }
    val pendientesNoche = nocheFiltradas.count { !it.estaCompletadaHoy(diaActualTexto) }

    val ejecutarEnrutadorAutomatico = {
        if (padreIdAsociado.isNotEmpty()) {
            routineViewModel.cargarRutinasDesdeFirebase(padreIdAsociado)
        }
        val primeraMananaPendiente = mañanaFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
        val primeraTardePendiente = tardeFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
        val primeraNochePendiente = nocheFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }

        when {
            primeraMananaPendiente != null -> onNavigateToTask(primeraMananaPendiente.actividad, "MAÑANA")
            primeraTardePendiente != null -> onNavigateToTask(primeraTardePendiente.actividad, "TARDE")
            primeraNochePendiente != null -> onNavigateToTask(primeraNochePendiente.actividad, "NOCHE")
            else -> onNavigateToCompleted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (esPremiumPorPadre) Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))) else Brush.verticalGradient(listOf(colorFondoNiño, colorFondoNiño))),
        contentAlignment = Alignment.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(color = colorAzulTEA)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                if (!estaVinculado) {
                    // --- PANTALLA DE VINCULACIÓN ---
                    Text(
                        text = "¡Hola! Dile a tus papás que escriban este código en su teléfono:",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF01579B),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = if (codigoNiño.length == 6) "${codigoNiño.take(3)} ${codigoNiño.drop(3)}" else codigoNiño,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black,
                            color = colorAzulTEA,
                            modifier = Modifier.padding(horizontal = 40.dp, vertical = 20.dp),
                            letterSpacing = 4.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    CircularProgressIndicator(color = colorAzulTEA, strokeWidth = 5.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Esperando a conectar...", color = Color.Gray, fontSize = 15.sp)

                } else {
                    if (esPremiumPorPadre) {
                        // ✨ MODO PREMIUM: Dashboard Gamificado Visual interactivo
                        val saludoEmotivo = when {
                            horaActual in 6..12 -> "¡Buenos días, Campeón! ☀️"
                            horaActual in 13..18 -> "¡Buenas tardes! ¡A por todas! 🌤️"
                            else -> "¡Buenas noches, hora de descansar! 🌙"
                        }

                        Text(
                            text = saludoEmotivo,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1B5E20),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Presiona un bloque para ver tus misiones de hoy",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4E342E),
                            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ChildBlockPremiumCard(
                                titulo = "RUTINA DE LA MAÑANA",
                                pendientes = pendientesManana,
                                total = mañanaFiltradas.size,
                                icono = Icons.Default.LightMode,
                                colorBase = Color(0xFFFFB74D),
                                onClick = {
                                    val primera = mañanaFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
                                    if (primera != null) onNavigateToTask(primera.actividad, "MAÑANA") else ejecutarEnrutadorAutomatico()
                                }
                            )

                            ChildBlockPremiumCard(
                                titulo = "RUTINA DE LA TARDE",
                                pendientes = pendientesTarde,
                                total = tardeFiltradas.size,
                                icono = Icons.Default.WbTwilight,
                                colorBase = Color(0xFF81C784),
                                onClick = {
                                    val primera = tardeFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
                                    if (primera != null) onNavigateToTask(primera.actividad, "TARDE") else ejecutarEnrutadorAutomatico()
                                }
                            )

                            ChildBlockPremiumCard(
                                titulo = "RUTINA DE LA NOCHE",
                                pendientes = pendientesNoche,
                                total = nocheFiltradas.size,
                                icono = Icons.Default.NightsStay,
                                colorBase = Color(0xFF9575CD),
                                onClick = {
                                    val primera = nocheFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
                                    if (primera != null) onNavigateToTask(primera.actividad, "NOCHE") else ejecutarEnrutadorAutomatico()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { ejecutarEnrutadorAutomatico() },
                            modifier = Modifier.fillMaxWidth(0.85f).height(65.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(20.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                        ) {
                            Text("MISIONES AUTOMÁTICAS 🚀", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }

                    } else {
                        // 🎒 MODO BÁSICO: Interfaz estándar minimalista con el botón único original
                        Text(
                            text = "¡Todo listo para empezar tus actividades!",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF004D40),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = { ejecutarEnrutadorAutomatico() },
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(85.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(24.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text("VER MIS TAREAS 🚀", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChildBlockPremiumCard(
    titulo: String,
    pendientes: Int,
    total: Int,
    icono: ImageVector,
    colorBase: Color,
    onClick: () -> Unit
) {
    val completadas = total - pendientes
    val estaTodoListo = total > 0 && pendientes == 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(2.dp, if (estaTodoListo) Color(0xFF4CAF50) else colorBase.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colorBase.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icono, contentDescription = null, tint = colorBase, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = titulo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF263238)
                    )
                    Text(
                        text = if (total == 0) "Sin tareas para hoy" else if (estaTodoListo) "¡Bloque completado! 🎉" else "$completadas de $total hechas",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estaTodoListo) Color(0xFF388E3C) else Color.Gray
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (estaTodoListo) Color(0xFFE8F5E9) else colorBase),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (estaTodoListo) "✅" else "$pendientes",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (estaTodoListo) Color(0xFF388E3C) else Color.White
                )
            }
        }
    }
}