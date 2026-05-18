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
    onNavigateToTask: (actividadNombre: String, turno: String) -> Unit, // Si hay una tarea pendiente
    onNavigateToCompleted: () -> Unit // Si ya terminó todas las tareas de este turno
) {
    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorFondoNiño = Color(0xFFE1F5FE) // Azul pastel calmante para el espectro autista
    val context = LocalContext.current

    // ID único de la tablet/celular del niño para que Firebase sepa de qué dispositivo se trata
    val deviceId = remember { Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) }

    // Estados de la pantalla corregidos con "by" e imports correctos
    var codigoNiño by remember { mutableStateOf("------") }
    var estaVinculado by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }
    var padreIdAsociado by remember { mutableStateOf("") }

    val database = remember { FirebaseDatabase.getInstance().reference }

    // --- LOGICA DE CONEXIÓN A FIREBASE EN TIEMPO REAL ---
    LaunchedEffect(deviceId) {
        // 1. Verificar si este dispositivo ya fue vinculado en el pasado
        database.child("dispositivos_niños").child(deviceId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("padreId")) {
                        val pId = snapshot.child("padreId").getValue(String::class.java) ?: ""
                        padreIdAsociado = pId
                        estaVinculado = true
                        // Cargamos las rutinas del padre en segundo plano para tenerlas listas
                        routineViewModel.cargarRutinasDesdeFirebase(pId)
                        cargando = false
                    } else {
                        // 2. Si es nuevo, generamos un código al azar de 6 dígitos
                        val nuevoCodigo = (100000..999999).random().toString()
                        codigoNiño = nuevoCodigo

                        // Guardamos en Firebase el token temporal diciendo que esta tablet espera un padre
                        val datosConexion = mapOf(
                            "deviceId" to deviceId,
                            "estado" to "esperando",
                            "padreId" to ""
                        )

                        database.child("codigos_vinculacion").child(nuevoCodigo).setValue(datosConexion)

                        // 3. Nos quedamos escuchando en vivo el documento hasta que el padre digite el número
                        database.child("codigos_vinculacion").child(nuevoCodigo)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(codeSnapshot: DataSnapshot) {
                                    if (codeSnapshot.exists()) {
                                        val estado = codeSnapshot.child("estado").getValue(String::class.java)
                                        val padreId = codeSnapshot.child("padreId").getValue(String::class.java)

                                        // Si el padre metió el código con éxito:
                                        if (estado == "enlazado" && !padreId.isNullOrEmpty()) {
                                            // Guardamos la relación permanente en la base de datos
                                            database.child("dispositivos_niños").child(deviceId).child("padreId").setValue(padreId)
                                            // Borramos el token para liberar espacio y que expire
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

    // --- LEER LOS ESTADOS DE TAREAS DESDE EL VIEWMODEL ---
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
                    // --- ESTADO A: EL NIÑO MUESTRA EL CÓDIGO AL PADRE ---
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
                    // --- ESTADO B: EL DISPOSITIVO FUE VINCULADO CON ÉXITO ---
                    Text(
                        text = "¡Todo listo para empezar tus actividades!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF004D40),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    // EL BOTÓN SE QUEDA: Pero al pulsarlo procesa la lógica automática de tiempo y día
                    Button(
                        onClick = {
                            // 1. Obtener día actual y hora del reloj del dispositivo
                            val calendar = Calendar.getInstance()
                            val numeroDia = calendar.get(Calendar.DAY_OF_WEEK)
                            val horaActual = calendar.get(Calendar.HOUR_OF_DAY)

                            val prefixDia = when (numeroDia) {
                                Calendar.MONDAY -> "LUN"
                                Calendar.TUESDAY -> "MAR"
                                Calendar.WEDNESDAY -> "MIÉ"
                                Calendar.THURSDAY -> "JUE"
                                Calendar.FRIDAY -> "VIE"
                                Calendar.SATURDAY -> "SÁB"
                                Calendar.SUNDAY -> "DOM"
                                else -> ""
                            }

                            // 2. Seleccionar la lista del turno que le corresponde por la hora
                            val (listaFiltradaPorTurno, turnoKey) = when {
                                // Mañana (6:00 AM a 11:59 AM)
                                horaActual in 6..11 -> {
                                    Pair(tasksManana.filter { it.dias.any { d -> d.uppercase().startsWith(prefixDia) } || it.dias.isEmpty() }, "MAÑANA")
                                }
                                // Tarde (12:00 PM a 5:59 PM)
                                horaActual in 12..17 -> {
                                    Pair(tasksTarde.filter { it.dias.any { d -> d.uppercase().startsWith(prefixDia) } || it.dias.isEmpty() }, "TARDE")
                                }
                                // Noche (6:00 PM en adelante)
                                else -> {
                                    Pair(tasksNoche.filter { it.dias.any { d -> d.uppercase().startsWith(prefixDia) } || it.dias.isEmpty() }, "NOCHE")
                                }
                            }

                            // 3. Buscar automáticamente la primera tarea que no esté terminada
                            val primeraTareaPendiente = listaFiltradaPorTurno.firstOrNull { !it.isCompleted }

                            if (primeraTareaPendiente != null) {
                                // Si hay tareas pendientes para este momento, viaja directo a ejecutarla
                                onNavigateToTask(primeraTareaPendiente.actividad, turnoKey)
                            } else {
                                // Si ya completó todo el bloque asignado hoy, va a la pantalla de felicitación/completado
                                onNavigateToCompleted()
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