package mk.fikt.mktransit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mk.fikt.mktransit.ui.screens.auth.LoginScreen
import mk.fikt.mktransit.ui.screens.auth.RegisterScreen
import mk.fikt.mktransit.ui.screens.auth.WelcomeScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.WELCOME
    ) {
        // Welcome
        composable(NavRoutes.WELCOME) {
            WelcomeScreen(
                onEmailClick = { navController.navigate(NavRoutes.LOGIN) },
                onGoogleClick = { /* подоцна */ },
                onFacebookClick = { /* подоцна */ },
                onAnonymousClick = { navController.navigate(NavRoutes.HOME) }
            )
        }

        // Login
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(NavRoutes.FORGOT_PASSWORD) },
                onBack = { navController.popBackStack() }
            )
        }

        // Register
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.ROLE_SELECTION) {
                        popUpTo(NavRoutes.WELCOME) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(NavRoutes.LOGIN) },
                onBack = { navController.popBackStack() }
            )
        }

        // Placeholder за Home (привремено)
        composable(NavRoutes.HOME) {
            androidx.compose.material3.Text("Home Screen — Coming Soon!")
        }

        // Placeholder за Role Selection
        composable(NavRoutes.ROLE_SELECTION) {
            androidx.compose.material3.Text("Role Selection — Coming Soon!")
        }

        // Placeholder за Forgot Password
        composable(NavRoutes.FORGOT_PASSWORD) {
            androidx.compose.material3.Text("Forgot Password — Coming Soon!")
        }
    }
}