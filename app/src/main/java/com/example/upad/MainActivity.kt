package com.example.upad

import androidx.compose.ui.Modifier
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

// Importaciones de tus pantallas
import com.example.upad.child.TaskExecutionScreen
import com.example.upad.auth.ForgotPasswordScreen
import com.example.upad.auth.LoginScreen
import com.example.upad.auth.RegisterScreen
import com.example.upad.auth.RoleSelectionScreen
import com.example.upad.auth.WelcomeScreen
import com.example.upad.child.ChildStartScreen
import com.example.upad.child.RoutineCompletedScreen
import com.example.upad.child.TaskFeedbackScreen
import com.example.upad.dashboard.AchievementReportScreen
import com.example.upad.dashboard.ActivityDetailsScreen
import com.example.upad.dashboard.NotificationsScreen
import com.example.upad.dashboard.RoutineDashboardScreen
import com.example.upad.data.FirebaseRepository
import com.example.upad.routines.CreateRoutineScreen
import com.example.upad.routines.PictogramSelectionScreen
import com.example.upad.setup.ChildProfileSetupScreen
import com.example.upad.setup.DevicePairingScreen
import com.example.upad.setup.ExperienceSetupScreen
import com.example.upad.setup.SubscriptionPlansScreen
import com.example.upad.setup.TrialDisclaimerScreen
import com.example.upad.setup.TutorialsScreen

import com.example.upad.viewmodel.RoutineViewModelFactory
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import android.app.Activity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// subpantallas
import com.example.upad.dashboard.ProfileScreen
import com.example.upad.dashboard.SettingsScreen
import com.example.upad.viewmodel.RoutineViewModel
import com.example.upad.dashboard.ConnectionScreen
import com.example.upad.dashboard.AnalyticsScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            UPadNavigation()
        }
    }
}

@Composable
fun UPadNavigation() {
    val navController = rememberNavController()

    // Inicializamos el Repositorio y el ViewModel
    val repository = remember { FirebaseRepository() }
    val routineViewModel: RoutineViewModel = viewModel(
        factory = com.example.upad.viewmodel.RoutineViewModelFactory(repository)
    )

    // --- SOLUCIÓN PARA VER LA HORA Y BATERÍA ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        val activity = view.context as Activity
        val window = activity.window
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = "role_selection"
            ) {
                // --- SECCIÓN AUTENTICACIÓN Y ROLES ENRUTADOS ---
                composable("role_selection") {
                    RoleSelectionScreen(
                        onRoleSelected = { role ->
                            when (role) {
                                "padre_directo" -> {
                                    navController.navigate("parent_dashboard") {
                                        popUpTo("role_selection") { inclusive = true }
                                    }
                                }
                                "padre" -> {
                                    navController.navigate("welcome")
                                }
                                else -> {
                                    navController.navigate("child_start")
                                }
                            }
                        }
                    )
                }

                composable("welcome") {
                    WelcomeScreen(
                        onNavigateToLogin = { navController.navigate("login") },
                        onNavigateToRegister = { navController.navigate("register") },
                        onLoginExitoso = {
                            navController.navigate("parent_dashboard") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    )
                }

                composable("login") {
                    LoginScreen(
                        onLoginClick = { _, _ ->
                            navController.navigate("parent_dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onForgotPasswordClick = { navController.navigate("forgot_password") },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("register") {
                    RegisterScreen(
                        onRegisterComplete = { _, _ -> navController.navigate("subscription_plans") },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("forgot_password") {
                    ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
                }

                // --- SECCIÓN CONFIGURACIÓN (SETUP) ---
                composable("subscription_plans") {
                    SubscriptionPlansScreen(
                        onPlanSelected = { planType ->
                            if (planType == "premium") navController.navigate("child_profile_setup")
                            else navController.navigate("trial_disclaimer")
                        },
                        onSkip = { navController.navigate("trial_disclaimer") }
                    )
                }

                composable("trial_disclaimer") {
                    TrialDisclaimerScreen(
                        onStartTrialClick = { navController.navigate("child_profile_setup") },
                        onMoreInfoClick = { }
                    )
                }

                composable("child_profile_setup") {
                    ChildProfileSetupScreen(
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { navController.navigate("experience_setup") }
                    )
                }

                composable("experience_setup") {
                    ExperienceSetupScreen(
                        onBackClick = { navController.popBackStack() },
                        onNextClick = { navController.navigate("device_pairing") }
                    )
                }

                composable("device_pairing") {
                    DevicePairingScreen(
                        onDeviceSelected = { navController.navigate("tutorials") },
                        onNavigateToDashboard = { navController.navigate("parent_dashboard") }
                    )
                }

                composable("tutorials") {
                    TutorialsScreen(
                        onBackClick = { navController.popBackStack() },
                        onFinishSetupClick = { navController.navigate("parent_dashboard") }
                    )
                }

                // --- SECCIÓN PADRE (DASHBOARD Y RUTINAS) ---
                composable("parent_dashboard") {
                    LaunchedEffect(Unit) {
                        routineViewModel.cargarRutinasDesdeFirebase("PADRE_TEST")
                    }
                    RoutineDashboardScreen(
                        routineViewModel = routineViewModel,
                        onNavigateToCreateRoutine = { turn ->
                            navController.navigate("create_routine/$turn")
                        },
                        onRoutineClick = { turn ->
                            navController.navigate("create_routine/$turn")
                        },
                        onNavigateToProfile = { navController.navigate("profile") },
                        onNavigateToSettings = { navController.navigate("settings") },
                        onNavigateToConnection = { navController.navigate("connection_code") },
                        onNavigateToAnalytics = { navController.navigate("analytics") }
                    )
                }

                composable("profile") {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLogoutSuccess = {
                            navController.navigate("welcome") {
                                popUpTo("parent_dashboard") { inclusive = true }
                            }
                        }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable("connection_code") {
                    ConnectionScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onLinkSuccess = { navController.popBackStack() }
                    )
                }

                composable("analytics") {
                    LaunchedEffect(Unit) {
                        routineViewModel.cargarRutinasDesdeFirebase("PADRE_TEST")
                    }
                    AnalyticsScreen(
                        routineViewModel = routineViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "create_routine/{routineTurn}",
                    arguments = listOf(navArgument("routineTurn") { type = NavType.StringType })
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString("routineTurn") ?: "Mañana"

                    val listaPasosTurno by when (turn.uppercase()) {
                        "MAÑANA" -> routineViewModel.tasksManana.collectAsState()
                        "TARDE" -> routineViewModel.tasksTarde.collectAsState()
                        else -> routineViewModel.tasksNoche.collectAsState()
                    }

                    CreateRoutineScreen(
                        routineTurn = turn,
                        childName = "tu hijo",
                        pasosSeleccionados = listaPasosTurno,
                        onBackClick = { navController.popBackStack() },
                        onNavigateToPictogramSearch = { navController.navigate("pictogram_selection/$turn") },
                        onRemoveTaskClick = { index -> routineViewModel.removeTask(turn, index) },
                        onSendRoutine = {
                            routineViewModel.saveAll("PADRE_TEST", turn)
                            navController.navigate("parent_dashboard")
                        },
                        viewModel = routineViewModel,
                        // 🛠️ FIX: Se cambia por el recurso de ID genérico de Android si tu logo no compila temporalmente
                        drawableId = android.R.drawable.ic_menu_manage
                    )
                }

                composable(
                    route = "pictogram_selection/{routineTurn}",
                    arguments = listOf(navArgument("routineTurn") { type = NavType.StringType })
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString("routineTurn") ?: "Mañana"

                    PictogramSelectionScreen(
                        viewModel = routineViewModel,
                        onBackClick = { navController.popBackStack() },
                        onPictogramSelected = { description, url ->
                            routineViewModel.addTask(turn, description, url)
                            navController.popBackStack()
                        }
                    )
                }

                // --- 🔥 SECCIÓN NIÑO CORREGIDA CON INTEGRADOR AUTOMÁTICO RE-DISEÑADO ---
                composable("child_start") {
                    ChildStartScreen(
                        routineViewModel = routineViewModel,
                        onNavigateToTask = { actividadNombre, turn ->
                            // El botón automático calcula la tarea vigente y lo inyecta a la pantalla de ejecución
                            navController.navigate("child_task_execution/$turn")
                        },
                        onNavigateToCompleted = {
                            navController.navigate("child_routine_completed")
                        }
                    )
                }

                composable(
                    route = "child_task_execution/{routineTurn}",
                    arguments = listOf(navArgument("routineTurn") { type = NavType.StringType })
                ) { backStackEntry ->
                    val turn = backStackEntry.arguments?.getString("routineTurn") ?: "MAÑANA"

                    LaunchedEffect(turn) {
                        routineViewModel.cargarRutinasDesdeFirebase("PADRE_TEST")
                    }

                    TaskExecutionScreen(
                        viewModel = routineViewModel,
                        turn = turn,
                        onFinishRoutine = {
                            navController.navigate("child_task_feedback/Rutina Completada")
                        }
                    )
                }

                composable(
                    route = "child_task_feedback/{activityName}",
                    arguments = listOf(navArgument("activityName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val activityName = backStackEntry.arguments?.getString("activityName") ?: "Actividad"

                    TaskFeedbackScreen(
                        activityName = activityName,
                        onFeedbackSelected = { _ ->
                            navController.navigate("child_routine_completed")
                        }
                    )
                }

                composable("child_routine_completed") {
                    RoutineCompletedScreen(
                        onFinishClick = {
                            navController.navigate("child_start") {
                                popUpTo("child_start") { inclusive = true }
                            }
                        }
                    )
                }

                // --- SECCIÓN REPORTES DETALLADOS ---
                composable("notifications") {
                    NotificationsScreen(onViewReportClick = { navController.navigate("achievement_report") })
                }

                composable("achievement_report") {
                    AchievementReportScreen(
                        onBackClick = { navController.popBackStack() },
                        onViewDetailsClick = { navController.navigate("activity_details") }
                    )
                }

                composable("activity_details") {
                    LaunchedEffect(Unit) {
                        routineViewModel.cargarRutinasDesdeFirebase("PADRE_TEST")
                    }
                    ActivityDetailsScreen(
                        viewModel = routineViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}