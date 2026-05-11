package com.example.upad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// --- IMPORTS CRÍTICOS ---
import com.example.upad.auth.WelcomeScreen
import com.example.upad.auth.LoginScreen
import com.example.upad.auth.RegisterScreen
import com.example.upad.auth.ForgotPasswordScreen
import com.example.upad.auth.RoleSelectionScreen
import com.example.upad.child.*
import com.example.upad.dashboard.*
import com.example.upad.routines.*
import com.example.upad.setup.*
import com.example.upad.ui.theme.UPadTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UPadTheme {
                UPadNavigation()
            }
        }
    }
}

@Composable
fun UPadNavigation() {
    val navController = rememberNavController()
    val pasosSeleccionados = remember { mutableStateListOf<String>() }

    NavHost(navController = navController, startDestination = "role_selection") {

        composable("role_selection") {
            RoleSelectionScreen(onRoleSelected = { role: String ->
                if (role == "padre") navController.navigate("welcome")
                else navController.navigate("child_start")
            })
        }

        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") },
                onGoogleSignInClick = { println("Google Sign In") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginClick = { email: String, pass: String -> navController.navigate("parent_dashboard") },
                onForgotPasswordClick = { navController.navigate("forgot_password") },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterComplete = { email: String, pass: String ->
                    navController.navigate("subscription_plans")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(onBackToLogin = { navController.popBackStack() })
        }

        composable("parent_dashboard") {
            RoutineDashboardScreen(
                onNavigateToCreateRoutine = { navController.navigate("create_routine/Nuevo") },
                onRoutineClick = { routineName -> navController.navigate("create_routine/$routineName") }
            )
        }

        composable(
            "create_routine/{routineTurn}",
            arguments = listOf(navArgument("routineTurn") { type = NavType.StringType })
        ) { backStackEntry ->
            val turn = backStackEntry.arguments?.getString("routineTurn") ?: "Mañana"
            CreateRoutineScreen(
                routineTurn = turn,
                childName = "Mateo",
                pasosSeleccionados = pasosSeleccionados,
                onBackClick = { navController.popBackStack() },
                onNavigateToPictogramSearch = { navController.navigate("pictogram_selection") },
                onSendRoutine = {
                    pasosSeleccionados.clear()
                    navController.popBackStack("parent_dashboard", false)
                },
                // Corregido: Se pasa el ID de recurso para evitar error de compilación
                drawableId = com.example.upad.R.drawable.logo_upad
            )
        }

        composable("pictogram_selection") {
            PictogramSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onPictogramSelected = { nombre: String ->
                    pasosSeleccionados.add(nombre)
                    navController.popBackStack()
                }
            )
        }

        // --- RUTAS DEL NIÑO ---
        composable("child_start") {
            ChildStartScreen(onStartRoutineClick = { navController.navigate("child_task_execution") })
        }

        composable("child_task_execution") {
            TaskExecutionScreen(
                onNextStepClick = { navController.navigate("child_task_feedback") },
                // CORRECCIÓN AQUÍ: Se añade el drawableId que pedía el error
                drawableId = com.example.upad.R.drawable.logo_upad
            )
        }

        composable("child_task_feedback") {
            TaskFeedbackScreen(onFeedbackSelected = { navController.navigate("child_routine_completed") })
        }

        composable("child_routine_completed") {
            RoutineCompletedScreen(
                onFinishClick = {
                    navController.navigate("child_start") {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- DASHBOARD Y REPORTES ---
        composable("notifications") { NotificationsScreen(onViewReportClick = { navController.navigate("achievement_report") }) }
        composable("achievement_report") { AchievementReportScreen(onBackClick = { navController.popBackStack() }, onViewDetailsClick = { navController.navigate("activity_details") }) }
        composable("activity_details") { ActivityDetailsScreen(onBack = { navController.popBackStack() }) }

        // --- SETUP ---
        composable("subscription_plans") { SubscriptionPlansScreen(onPlanSelected = { navController.navigate("trial_disclaimer") }, onSkip = { navController.navigate("trial_disclaimer") }) }
        composable("trial_disclaimer") { TrialDisclaimerScreen(onStartTrialClick = { navController.navigate("child_profile_setup") }, onMoreInfoClick = { }) }
        composable("child_profile_setup") { ChildProfileSetupScreen(onBackClick = { navController.popBackStack() }, onSaveClick = { navController.navigate("experience_setup") }) }
        composable("experience_setup") { ExperienceSetupScreen(onBackClick = { navController.popBackStack() }, onNextClick = { navController.navigate("device_pairing") }) }
        composable("device_pairing") { DevicePairingScreen(onDeviceSelected = { navController.navigate("tutorials") }, onNavigateToDashboard = { navController.navigate("parent_dashboard") }) }
        composable("tutorials") { TutorialsScreen(onBackClick = { navController.popBackStack() }, onFinishSetupClick = { navController.navigate("parent_dashboard") }) }
    }
}