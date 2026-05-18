package com.example.upad.routines

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineScreen(
    routineTurn: String = "Mañana",
    childName: String = "tu hijo",
    pasosSeleccionados: List<TaskItem>, // Lista en crudo desde Firebase
    onBackClick: () -> Unit,
    onNavigateToPictogramSearch: () -> Unit,
    onSendRoutine: () -> Unit,
    onRemoveTaskClick: (Int) -> Unit,
    viewModel: RoutineViewModel,
    drawableId: Int
) {
    val currentUserId = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: "PADRE_TEST"
    }

    var routineName by remember(routineTurn) { mutableStateOf("Rutina de la $routineTurn") }
    var showSendingDialog by remember { mutableStateOf(false) }

    var nombreActividad by remember { mutableStateOf("") }
    val diasDeLaSemana = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    val diasSeleccionados = remember { mutableStateListOf<String>() }

    // --- 📅 DETECTOR INTELIGENTE DEL DÍA ACTUAL DE LA SEMANA ---
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

    // El filtro del adulto se inicializa de forma automática en el día presente del sistema
    var diaFiltroSeleccionado by remember { mutableStateOf(diaActualDelReloj) }

    val colorAzulTEA = Color(0xFF4FC3F7)
    val colorAmarilloTEA = Color(0xFFFFD54F)
    val colorFondoBase = Color(0xFFF0F4F8)

    // --- 🛠️ FILTRADO INTELIGENTE REACTIVO PARA LA INTERFAZ ---
    // Filtramos la lista en tiempo real para que coincida exactamente con las reglas de negocio
    val pasosFiltradosPorDia = remember(pasosSeleccionados, diaFiltroSeleccionado) {
        pasosSeleccionados.filter { tarea ->
            tarea.dias.isEmpty() || tarea.dias.any { it.uppercase().trim().startsWith(diaFiltroSeleccionado.uppercase()) }
        }
    }

    if (showSendingDialog) {
        SendingRoutineDialog(
            routineTurn = routineTurn,
            childName = childName,
            onDismiss = { showSendingDialog = false }
        )

        LaunchedEffect(Unit) {
            viewModel.saveAll(currentUserId, routineTurn)
            delay(2000)
            showSendingDialog = false
            onSendRoutine()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorFondoBase)
    ) {
        // --- CABECERA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 45.dp, bottomEnd = 45.dp))
                .background(Color.White)
                .padding(top = 40.dp, bottom = 24.dp, start = 16.dp, end = 24.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = colorAzulTEA)
            }
            Text(
                text = "TURNO: ${routineTurn.uppercase()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.LightGray,
                modifier = Modifier.padding(start = 12.dp)
            )
            Text(
                text = "Editar Actividades",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colorAzulTEA,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = routineName,
                onValueChange = {
                    routineName = it
                    viewModel.updateName(it)
                },
                label = { Text("Nombre de la Rutina") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorAzulTEA),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- FORMULARIO NUEVA ACTIVIDAD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "NUEVA ACTIVIDAD AUTOMÁTICA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorAzulTEA
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nombreActividad,
                        onValueChange = { nombreActividad = it },
                        placeholder = { Text("Ej: Estudiar inglés, Lavar los platos...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Días programados:", fontSize = 13.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        diasDeLaSemana.forEach { dia ->
                            val estaSeleccionado = diasSeleccionados.contains(dia)
                            FilterChip(
                                selected = estaSeleccionado,
                                onClick = {
                                    if (estaSeleccionado) diasSeleccionados.remove(dia)
                                    else diasSeleccionados.add(dia)
                                },
                                label = { Text(dia, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (nombreActividad.isNotEmpty()) {
                                viewModel.agregarActividadAutomatica(
                                    userId = currentUserId,
                                    turn = routineTurn,
                                    textoCompleto = nombreActividad,
                                    diasSeleccionados = diasSeleccionados.toList()
                                )
                                // Seteamos el visor del día de forma automática al primer día asignado
                                if(diasSeleccionados.isNotEmpty()){
                                    diaFiltroSeleccionado = diasSeleccionados.first()
                                }
                                nombreActividad = ""
                                diasSeleccionados.clear()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.6f.dp))
                        Text("AÑADIR ACTIVIDAD ➕", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- BARRA SELECTORA SEMANAL INTEGRADORA ---
            Text(
                text = "VER AGENDA SEMANAL:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
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
                                    esElDiaActivo -> colorAzulTEA
                                    esHoyDelSistema -> colorAzulTEA.copy(alpha = 0.15f)
                                    else -> Color.White
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
                            color = if (esElDiaActivo) Color.White else if (esHoyDelSistema) colorAzulTEA else Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // El contador ahora lee dinámicamente la lista filtrada por el día seleccionado
                Text(
                    text = "ACTIVIDADES DEL ${diaFiltroSeleccionado.uppercase()} (${pasosFiltradosPorDia.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Button(
                    onClick = { /* IA Logic */ },
                    colors = ButtonDefaults.buttonColors(containerColor = colorAmarilloTEA),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.6f.dp))
                    Text("Sugerir con IA", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- LISTADO TOTALMENTE SANEADO POR DÍA ---
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (pasosFiltradosPorDia.isEmpty()) {
                    item {
                        Text(
                            text = "No hay actividades para el día ${diaFiltroSeleccionado}.\nUsa el panel superior para registrar tareas.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp)
                        )
                    }
                } else {
                    itemsIndexed(pasosFiltradosPorDia) { indexFiltrado, paso ->
                        // Buscamos el índice real de la lista completa para eliminar de forma segura en Firebase
                        val indexRealEnFirebase = pasosSeleccionados.indexOfFirst {
                            it.actividad == paso.actividad && it.dias == paso.dias
                        }

                        PasoItemCard(
                            tarea = paso,
                            onDeleteClick = {
                                if (indexRealEnFirebase != -1) {
                                    onRemoveTaskClick(indexRealEnFirebase)
                                    viewModel.saveAll(currentUserId, routineTurn)
                                }
                            }
                        )
                    }
                }
            }
        }

        // --- SECCIÓN DE ACCIONES INFERIORES ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text("CANCELAR", fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            Button(
                onClick = { showSendingDialog = true },
                modifier = Modifier.weight(1f).height(60.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorAzulTEA)
            ) {
                Text("GUARDAR RUTINA", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun SendingRoutineDialog(routineTurn: String, childName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFF4FC3F7), strokeWidth = 5.dp, modifier = Modifier.size(60.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Guardando cambios en el turno\n$routineTurn...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    )
}

@Composable
fun PasoItemCard(tarea: TaskItem, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFF0F4F8)),
                contentAlignment = Alignment.Center
            ) {
                if (tarea.imageUrl.isNotEmpty()) {
                    coil.compose.AsyncImage(model = tarea.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.LightGray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tarea.actividad,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontSize = 15.sp
                )
                if (tarea.dias.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Días: ${tarea.dias.joinToString(", ")}",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Días: Todos los días",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50), // <-- Corregido aquí
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFEF5350))
            }
        }
    }
}