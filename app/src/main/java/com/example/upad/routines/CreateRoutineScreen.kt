package com.example.upad.routines

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.upad.viewmodel.TaskItem
import com.example.upad.viewmodel.RoutineViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.Calendar

// 🤖 IMPORTACIONES OFICIALES DE VERTEX AI EN FIREBASE
import com.google.firebase.Firebase
import com.google.firebase.vertexai.vertexAI
import com.google.firebase.vertexai.type.generationConfig
import com.google.firebase.vertexai.type.content
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routineTurn: String = "Mañana",
    childName: String = "tu hijo",
    pasosSeleccionados: List<TaskItem>,
    onBackClick: () -> Unit,
    onNavigateToPictogramSearch: () -> Unit,
    onSendRoutine: () -> Unit,
    onRemoveTaskClick: (Int) -> Unit,
    viewModel: RoutineViewModel,
    drawableId: Int
) {
    val isPremiumUser by viewModel.isUserPremium.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    val colorAcabadoPrincipal = MaterialTheme.colorScheme.primary
    val colorFondoBase = MaterialTheme.colorScheme.background
    val colorSuperficieTarjetas = MaterialTheme.colorScheme.surface
    val colorTextoPrincipal = MaterialTheme.colorScheme.onBackground
    val colorTextoSecundario = MaterialTheme.colorScheme.onSurface

    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: "PADRE_TEST"
    }

    var routineName by remember(routineTurn) { mutableStateOf("Rutina de la $routineTurn") }
    var showSendingDialog by remember { mutableStateOf(false) }
    var nombreActividad by remember { mutableStateOf("") }

    // ⏳ CONTROL DE CARGA INDEPENDIENTE PARA LA IA
    var isLoadingAI by remember { mutableStateOf(false) }

    val diasDeLaSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val diasSeleccionados = remember { mutableStateListOf<String>() }

    val diaActualDelReloj = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Lun"
            Calendar.TUESDAY -> "Mar"
            Calendar.WEDNESDAY -> "Mié"
            Calendar.THURSDAY -> "Jue"
            Calendar.FRIDAY -> "Vie"
            Calendar.SATURDAY -> "Sáb"
            Calendar.SUNDAY -> "Dom"
            else -> "Lun"
        }
    }

    var diaFiltroSeleccionado by remember { mutableStateOf(diaActualDelReloj) }

    val pasosFiltradosPorDia = remember(pasosSeleccionados, diaFiltroSeleccionado) {
        pasosSeleccionados.filter { tarea ->
            tarea.dias.isEmpty() || tarea.dias.any { it.uppercase().trim().startsWith(diaFiltroSeleccionado.uppercase()) }
        }
    }

    // ⚡ PREPARACIÓN CORRECTA DEL MODELO GEMINI
    val aiConfig = generationConfig {
        temperature = 0.85f
    }

    val modelGemini = remember {
        Firebase.vertexAI.generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = aiConfig,
            systemInstruction = content {
                text("Eres un psicopedagogo experto en autismo (TEA). Tu único trabajo es sugerir UNA sola actividad aleatoria y útil para el turno del día solicitado. Devuelve SOLO el nombre de la actividad en formato muy corto (máximo 4 palabras), directo y claro (Ej: 'Lavarse los dientes', 'Guardar los juguetes', 'Hora de almorzar'). No repitas saludos, ni des explicaciones, ni agregues números.")
            }
        )
    }

    // 💾 DIÁLOGO DE GUARDADO FINAL (SOLO AL PRESIONAR GUARDAR ABAJO DE LA APP)
    if (showSendingDialog) {
        SendingRoutineDialog(
            routineTurn = routineTurn,
            childName = childName,
            isPremiumUser = isPremiumUser,
            colorFondo = colorSuperficieTarjetas,
            colorTexto = colorTextoPrincipal,
            colorProgreso = colorAcabadoPrincipal,
            onDismiss = { showSendingDialog = false }
        )

        LaunchedEffect(Unit) {
            viewModel.saveAll(currentUserId, routineTurn)
            delay(2000)
            showSendingDialog = false
            onSendRoutine()
        }
    }

    Scaffold(
        containerColor = colorFondoBase,
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = colorSuperficieTarjetas,
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorTextoSecundario),
                        border = BorderStroke(1.dp, colorTextoSecundario.copy(alpha = 0.3f))
                    ) {
                        Text("CANCELAR", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showSendingDialog = true },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal, contentColor = Color.White)
                    ) {
                        Text("GUARDAR", fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(colorSuperficieTarjetas)
                        .padding(top = 20.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAcabadoPrincipal)
                        }

                        if (isPremiumUser) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Row(
                                    modifier = Modifier
                                        .background(colorAcabadoPrincipal.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colorAcabadoPrincipal, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PREMIUM", color = colorAcabadoPrincipal, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        } else {
                            Button(
                                onClick = { /* Navegar a pantalla de planes */ },
                                colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal.copy(alpha = 0.12f)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CardMembership, contentDescription = null, tint = colorAcabadoPrincipal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Basic", color = colorAcabadoPrincipal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "TURNO: ${routineTurn.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colorTextoSecundario.copy(alpha = 0.8f),
                        modifier = Modifier.padding(start = 12.dp),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Editar Actividades",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = colorTextoPrincipal,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    OutlinedTextField(
                        value = routineName,
                        onValueChange = {
                            routineName = it
                            viewModel.updateName(it)
                        },
                        label = { Text("Nombre de la Rutina", color = colorTextoSecundario) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAcabadoPrincipal,
                            unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.3f),
                            focusedTextColor = colorTextoPrincipal,
                            unfocusedTextColor = colorTextoPrincipal
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = colorSuperficieTarjetas),
                        border = BorderStroke(1.dp, colorAcabadoPrincipal.copy(alpha = 0.2f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "AÑADIR ACTIVIDAD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorAcabadoPrincipal,
                                letterSpacing = 0.5.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // ✍️ ENTRADA MANUAL COMPLETAMENTE LIBRE (Nunca se bloquea)
                            OutlinedTextField(
                                value = nombreActividad,
                                onValueChange = { nombreActividad = it },
                                placeholder = { Text("Ej: Estudiar inglés, comer fruta...", color = colorTextoSecundario.copy(alpha = 0.6f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = colorAcabadoPrincipal,
                                    unfocusedBorderColor = colorTextoSecundario.copy(alpha = 0.2f),
                                    focusedTextColor = colorTextoPrincipal,
                                    unfocusedTextColor = colorTextoPrincipal
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(text = "Días programados:", fontSize = 13.sp, color = colorTextoSecundario)

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                diasDeLaSemana.forEach { dia ->
                                    val estaSeleccionado = diasSeleccionados.contains(dia)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (estaSeleccionado) colorAcabadoPrincipal else colorFondoBase)
                                            .clickable {
                                                if (estaSeleccionado) diasSeleccionados.remove(dia)
                                                else diasSeleccionados.add(dia)
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dia,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (estaSeleccionado) Color.White else colorTextoSecundario
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // ➕ BOTÓN MANUAL: Agrega lo que tú escribas arriba
                            Button(
                                onClick = {
                                    if (nombreActividad.isNotEmpty()) {
                                        viewModel.agregarActividadAutomatica(
                                            userId = currentUserId,
                                            turn = routineTurn,
                                            textoCompleto = nombreActividad,
                                            diasSeleccionados = diasSeleccionados.toList()
                                        )
                                        if (diasSeleccionados.isNotEmpty()) {
                                            diaFiltroSeleccionado = diasSeleccionados.first()
                                        }
                                        nombreActividad = ""
                                        diasSeleccionados.clear()
                                    }
                                },
                                enabled = nombreActividad.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth().height(45.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorAcabadoPrincipal, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AÑADIR MANUAL ➕", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // ✨ BOTÓN IA PREMIUM: Solo se activa y genera cuando haces click manual en él
                            if (isPremiumUser) {
                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                isLoadingAI = true

                                                // 1. Determinar los días correctos
                                                val diasAGuardar = if (diasSeleccionados.isEmpty()) {
                                                    listOf(diaFiltroSeleccionado)
                                                } else {
                                                    diasSeleccionados.toList()
                                                }

                                                var sugerenciaTexto = ""

                                                try {
                                                    // Intentar llamar a Gemini en la nube
                                                    val promptIA = "Dame una sola idea de actividad corta y recomendada para la rutina de la $routineTurn de un niño."
                                                    val response = withContext(Dispatchers.IO) {
                                                        modelGemini.generateContent(promptIA)
                                                    }
                                                    sugerenciaTexto = response.text?.trim() ?: ""
                                                } catch (e: Exception) {
                                                    // 🚨 SI GEMINI FALLA (Falta de internet, cuotas, API key), RESPALDO INMEDIATO LOCAL
                                                    sugerenciaTexto = "ACTIVIDAD IA DE PRUEBA"
                                                    android.util.Log.e("UPAD_IA", "Gemini falló: ${e.message}. Usando respaldo local.")
                                                }

                                                // Asegurar que el texto nunca llegue vacío al ViewModel
                                                if (sugerenciaTexto.isEmpty()) {
                                                    sugerenciaTexto = "NUEVA ACTIVIDAD RECOMENDADA"
                                                }

                                                // 2. Enviamos los días correctos y el texto al ViewModel
                                                viewModel.agregarActividadAutomatica(
                                                    userId = currentUserId,
                                                    turn = routineTurn,
                                                    textoCompleto = sugerenciaTexto,
                                                    diasSeleccionados = diasAGuardar
                                                )

                                                // 3. Movemos el filtro visual de la UI para asegurar que se muestre en pantalla
                                                diaFiltroSeleccionado = diasAGuardar.first()

                                                // Limpiar campos de la interfaz
                                                nombreActividad = ""
                                                diasSeleccionados.clear()

                                            } catch (e: Exception) {
                                                android.util.Log.e("UPAD_ERROR", "Error general en flujo IA: ${e.message}")
                                            } finally {
                                                isLoadingAI = false
                                            }
                                        }
                                    }, // 👈 ESTA COMA ES SAGRADA. Separa el onClick del parámetro enabled.
                                    enabled = !isLoadingAI,
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5A059), contentColor = Color.White)
                                ) {
                                    if (isLoadingAI) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("PROCESANDO CON IA...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SUGERIR ACTIVIDAD IA ✨", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "VER AGENDA SEMANAL:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorTextoSecundario,
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(diasDeLaSemana) { dia ->
                            val esElDiaActivo = (dia == diaFiltroSeleccionado)
                            val esHoyDelSistema = (dia == diaActualDelReloj)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            esElDiaActivo -> colorAcabadoPrincipal
                                            esHoyDelSistema -> colorAcabadoPrincipal.copy(alpha = 0.2f)
                                            else -> colorSuperficieTarjetas
                                        }
                                    )
                                    .clickable { diaFiltroSeleccionado = dia }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (esHoyDelSistema) "$dia (Hoy)" else dia,
                                    fontSize = 13.sp,
                                    fontWeight = if (esElDiaActivo || esHoyDelSistema) FontWeight.Bold else FontWeight.Medium,
                                    color = if (esElDiaActivo) Color.White else if (esHoyDelSistema) colorAcabadoPrincipal else colorTextoPrincipal
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ACTIVIDADES DEL ${diaFiltroSeleccionado.uppercase()} (${pasosFiltradosPorDia.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTextoPrincipal,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp)
                )
            }

            if (pasosFiltradosPorDia.isEmpty()) {
                item {
                    Text(
                        text = "No hay actividades para el día $diaFiltroSeleccionado.",
                        fontSize = 14.sp,
                        color = colorTextoSecundario,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 24.dp)
                    )
                }
            } else {
                itemsIndexed(pasosFiltradosPorDia) { _, paso ->
                    val indexRealEnFirebase = pasosSeleccionados.indexOfFirst {
                        it.actividad == paso.actividad && it.dias == paso.dias
                    }

                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 5.dp)) {
                        PasoItemCard(
                            tarea = paso,
                            colorSuperficie = colorSuperficieTarjetas,
                            colorTexto = colorTextoPrincipal,
                            colorTextoSec = colorTextoSecundario,
                            colorDetalle = colorAcabadoPrincipal,
                            onDeleteClick = {
                                if (indexRealEnFirebase != -1) {
                                    onRemoveTaskClick(indexRealEnFirebase)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SendingRoutineDialog(
    routineTurn: String,
    childName: String,
    isPremiumUser: Boolean,
    colorFondo: Color,
    colorTexto: Color,
    colorProgreso: Color,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        shape = RoundedCornerShape(24.dp),
        containerColor = colorFondo,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = colorProgreso,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Guardando cambios en el turno\n$routineTurn...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

@Composable
fun PasoItemCard(
    tarea: TaskItem,
    colorSuperficie: Color,
    colorTexto: Color,
    colorTextoSec: Color,
    colorDetalle: Color,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colorSuperficie),
        border = BorderStroke(1.dp, colorDetalle.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(colorDetalle.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (tarea.imageUrl.isNotEmpty()) {
                    AsyncImage(model = tarea.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = colorDetalle.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tarea.actividad, fontWeight = FontWeight.Bold, color = colorTexto, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (tarea.dias.isNotEmpty()) "Días: ${tarea.dias.joinToString(", ")}" else "Días: Todos los días",
                    fontSize = 12.sp, color = colorTextoSec
                )
            }
            // 🗑️ ACCIÓN DIRECTA DE ELIMINACIÓN INDEPENDIENTE
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF5350))
            }
        }
    }
}