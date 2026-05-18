package com.example.upad.child

import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    val database = remember { FirebaseDatabase.getInstance().reference }

    // --- 📡 FIJADO: ESCUCHA CONSTANTE EN TIEMPO REAL DESDE LA NUBE ---
    LaunchedEffect(deviceId) {
        // Cambiado a addValueEventListener para que el niño dependa ÚNICAMENTE de los cambios en la nube de Firebase,
        // sin importar el estado del teléfono del padre.
        database.child("dispositivos_niños").child(deviceId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("padreId")) {
                        val pId = snapshot.child("padreId").getValue(String::class.java) ?: ""
                        padreIdAsociado = pId
                        estaVinculado = true

                        // Sincroniza las rutinas de este padre de forma directa e independiente
                        routineViewModel.cargarRutinasDesdeFirebase(pId)
                        cargando = false
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
                                            cargando = false
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoNiño),
        contentAlignment = Alignment.Center
    ) {
        if (cargando) {
            CircularProgressIndicator(color = colorAzulTEA)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                if (!estaVinculado) {
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
                    Text(
                        text = "¡Todo listo para empezar tus actividades!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF004D40),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    Button(
                        onClick = {
                            // 🔥 Aseguramos una última consulta limpia al repositorio antes de evaluar el salto
                            if (padreIdAsociado.isNotEmpty()) {
                                routineViewModel.cargarRutinasDesdeFirebase(padreIdAsociado)
                            }

                            val calendar = Calendar.getInstance()
                            val numeroDia = calendar.get(Calendar.DAY_OF_WEEK)

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

                            val mañanaFiltradas = tasksManana.filter { tarea ->
                                if (tarea.dias.isEmpty()) true else {
                                    tarea.dias.any { d -> listaVariacionesDia.contains(d.uppercase().trim()) }
                                }
                            }

                            val tardeFiltradas = tasksTarde.filter { tarea ->
                                if (tarea.dias.isEmpty()) true else {
                                    tarea.dias.any { d -> listaVariacionesDia.contains(d.uppercase().trim()) }
                                }
                            }

                            val nocheFiltradas = tasksNoche.filter { tarea ->
                                if (tarea.dias.isEmpty()) true else {
                                    tarea.dias.any { d -> listaVariacionesDia.contains(d.uppercase().trim()) }
                                }
                            }

                            val primeraMananaPendiente = mañanaFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
                            val primeraTardePendiente = tardeFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }
                            val primeraNochePendiente = nocheFiltradas.firstOrNull { !it.estaCompletadaHoy(diaActualTexto) }

                            // 🗺️ ENRUTADOR DINÁMICO
                            when {
                                primeraMananaPendiente != null -> {
                                    onNavigateToTask(primeraMananaPendiente.actividad, "MAÑANA")
                                }
                                primeraTardePendiente != null -> {
                                    onNavigateToTask(primeraTardePendiente.actividad, "TARDE")
                                }
                                primeraNochePendiente != null -> {
                                    onNavigateToTask(primeraNochePendiente.actividad, "NOCHE")
                                }
                                else -> {
                                    onNavigateToCompleted()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(80.dp),
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