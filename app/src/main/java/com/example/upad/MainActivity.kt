package com.example.upad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.upad.auth.*
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

    NavHost(navController = navController, startDestination = "role_selection") {

        composable("role_selection") {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    if (role == "padre") {
                        navController.navigate("welcome")
                    } else {
                        navController.navigate("child_start")
                    }
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("login") {
            // Se usan guiones bajos para ignorar los parámetros por ahora
            LoginScreen(
                onLoginClick = { _, _ -> navController.navigate("parent_dashboard") },
                onForgotPasswordClick = { navController.navigate("forgot_password") }
            )
        }

        composable("register") {
            // CORRECCIÓN: Se usan (_, _) para que coincida con la nueva lógica de RegisterScreen
            RegisterScreen(
                onRegisterComplete = { _, _ -> navController.navigate("subscription_plans") }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("subscription_plans") {
            SubscriptionPlansScreen(
                onPlanSelected = { navController.navigate("trial_disclaimer") },
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
                onBackClick = { navController.popBackStack() },
                onNavigateToPictogramSearch = { navController.navigate("pictogram_selection") },
                onSendRoutine = { navController.popBackStack("parent_dashboard", false) }
            )
        }

        composable("pictogram_selection") {
            PictogramSelectionScreen(
                onBackClick = { navController.popBackStack() },
                onPictogramSelected = { navController.popBackStack() }
            )
        }

        composable("child_start") {
            ChildStartScreen(
                onStartRoutineClick = { navController.navigate("child_task_execution") }
            )
        }

        composable("child_task_execution") {
            TaskExecutionScreen(
                onNextStepClick = { navController.navigate("child_task_feedback") }
            )
        }

        composable("child_task_feedback") {
            TaskFeedbackScreen(
                onFeedbackSelected = { navController.navigate("child_routine_completed") }
            )
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

        composable("notifications") {
            NotificationsScreen(
                onViewReportClick = { navController.navigate("achievement_report") }
            )
        }

        composable("achievement_report") {
            AchievementReportScreen(
                onBackClick = { navController.popBackStack() },
                onViewDetailsClick = { navController.navigate("activity_details") } // Conexión del botón
            )
        }

        // Nueva ruta para los detalles de actividad
        composable("activity_details") {
            ActivityDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}