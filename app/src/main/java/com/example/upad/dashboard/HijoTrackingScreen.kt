package com.example.upad.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.upad.viewmodel.TrackingViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijoTrackingScreen(
    hijoId: String,
    onNavigateBack: () -> Unit,
    trackingViewModel: TrackingViewModel = viewModel()
) {
    val context = LocalContext.current
    val idPadre = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
    val firestore = remember { FirebaseFirestore.getInstance() }

    val dispositivosConectados = remember { mutableStateListOf<DispositivoVinculado>() }
    var dispositivoSeleccionado by remember { mutableStateOf<DispositivoVinculado?>(null) }

    // 📍 Estados de ubicación limpios sin ciudades fijas precargadas
    var miUbicacionReal by remember { mutableStateOf<LatLng?>(null) }
    val datosUbicacionNino by trackingViewModel.ubicacion.collectAsState()

    var tienePermisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState()

    val launcherPermisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { aceptado ->
        tienePermisoUbicacion = aceptado
    }

    LaunchedEffect(Unit) {
        if (!tienePermisoUbicacion) {
            launcherPermisos.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 📡 ESCUCHA DE UBICACIÓN DEL PADRE EN TIEMPO REAL (Cada 5 segundos de forma continua)
    LaunchedEffect(tienePermisoUbicacion) {
        if (tienePermisoUbicacion) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 5000L
                ).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val pos = LatLng(location.latitude, location.longitude)
                            miUbicacionReal = pos

                            // Si el niño no ha cargado coordenadas válidas, la cámara te sigue a ti primero
                            val ninoEsInvalido = datosUbicacionNino == null ||
                                    (datosUbicacionNino?.latitud == 0.0 && datosUbicacionNino?.longitud == 0.0)

                            if (ninoEsInvalido) {
                                cameraPositionState.position = CameraPosition.fromLatLngZoom(pos, 15f)
                            }
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )

            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    // 📡 Lista de dispositivos vinculados en tiempo real
    LaunchedEffect(idPadre) {
        if (idPadre.isNotEmpty()) {
            firestore.collection("dispositivos_niños")
                .whereEqualTo("padreId", idPadre)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener

                    dispositivosConectados.clear()
                    snapshot?.documents?.forEach { doc ->
                        dispositivosConectados.add(
                            DispositivoVinculado(
                                id = doc.id,
                                modelo = doc.getString("modelo") ?: "Dispositivo"
                            )
                        )
                    }

                    // Auto-selección inteligente al abrir la pantalla
                    if (dispositivosConectados.isNotEmpty() && dispositivoSeleccionado == null) {
                        val preferido = dispositivosConectados.find { it.id == hijoId }
                        dispositivoSeleccionado = preferido ?: dispositivosConectados.first()
                    }
                }
        }
    }

    // Disparador del ViewModel al cambiar de teléfono seleccionado
    LaunchedEffect(dispositivoSeleccionado) {
        dispositivoSeleccionado?.let {
            trackingViewModel.iniciarRastreoHijo(it.id)
        }
    }

    // 🎯 ENCUADRE DE CÁMARA ESTILO WHATSAPP (Ajuste automático dinámico)
    LaunchedEffect(datosUbicacionNino, miUbicacionReal) {
        val infoNino = datosUbicacionNino
        val infoPadre = miUbicacionReal

        // Comprobamos que el niño tenga coordenadas reales y no la posición por defecto (0,0) de Ghana
        if (infoNino != null && infoNino.latitud != 0.0 && infoNino.longitud != 0.0) {
            val coordenadasHijo = LatLng(infoNino.latitud, infoNino.longitud)

            if (infoPadre != null) {
                // Si están los dos, los encuadra juntos
                val limites = LatLngBounds.Builder()
                    .include(infoPadre)
                    .include(coordenadasHijo)
                    .build()

                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngBounds(limites, 150),
                    durationMs = 1000
                )
            } else {
                // Si solo está el niño, va directo a él
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(coordenadasHijo, 16f),
                    durationMs = 1000
                )
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ubicar a mi Hijo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver al Dashboard",
                            tint = Color(0xFFC5A059)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = tienePermisoUbicacion),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                // 👤 Marcador de tu Ubicación Real
                miUbicacionReal?.let { posPadre ->
                    MarkerComposable(
                        state = MarkerState(position = posPadre),
                        title = "Tu ubicación"
                    ) {
                        Box(
                            modifier = Modifier
                                .size(45.dp)
                                .background(Color(0xFF007AFF), CircleShape)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👨‍🦰", fontSize = 22.sp)
                            }
                        }
                    }
                }

                // 👶 Marcador de la Ubicación Real del Menor (Se renderiza si no es 0,0)
                datosUbicacionNino?.let { info ->
                    if (info.latitud != 0.0 && info.longitud != 0.0) {
                        val coordenadasHijo = LatLng(info.latitud, info.longitud)
                        MarkerComposable(
                            state = MarkerState(position = coordenadasHijo),
                            title = dispositivoSeleccionado?.modelo ?: "Hijo"
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(45.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👦", fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Carrusel horizontal superior de dispositivos
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(dispositivosConectados) { dispositivo ->
                        val esElSeleccionado = dispositivo.id == dispositivoSeleccionado?.id

                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (esElSeleccionado) Color(0xFFC5A059) else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier.clickable { dispositivoSeleccionado = dispositivo }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (esElSeleccionado) "🎯" else "📱",
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = dispositivo.modelo,
                                    color = if (esElSeleccionado) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}