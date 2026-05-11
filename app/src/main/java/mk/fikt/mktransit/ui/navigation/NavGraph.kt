package mk.fikt.mktransit.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mk.fikt.mktransit.ui.screens.auth.ForgotPasswordScreen
import mk.fikt.mktransit.ui.screens.auth.LoginScreen
import mk.fikt.mktransit.ui.screens.auth.RegisterScreen
import mk.fikt.mktransit.ui.screens.auth.RoleSelectionScreen
import mk.fikt.mktransit.ui.screens.auth.WelcomeScreen
import mk.fikt.mktransit.ui.screens.home.HomeScreen
import mk.fikt.mktransit.ui.screens.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.WELCOME
    ) {
        composable(NavRoutes.WELCOME) {
            WelcomeScreen(
                onEmailClick = { navController.navigate(NavRoutes.LOGIN) },
                onGoogleClick = { },
                onFacebookClick = { },
                onAnonymousClick = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

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

        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ROLE_SELECTION) {
            RoleSelectionScreen(
                onRoleSelected = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.ROLE_SELECTION) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(
                onLineClick = { lineId ->
                    navController.navigate(NavRoutes.lineDetail(lineId))
                },
                onMapClick = { navController.navigate(NavRoutes.MAP) },
                onTicketsClick = { navController.navigate(NavRoutes.TICKETS) },
                onMessagesClick = { navController.navigate(NavRoutes.MESSAGES) },
                onProfileClick = { navController.navigate(NavRoutes.PROFILE) }
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(NavRoutes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MAP) {
            Text("Map — Coming Soon!")
        }

        composable(NavRoutes.TICKETS) {
            Text("Tickets — Coming Soon!")
        }

        composable(NavRoutes.MESSAGES) {
            Text("Messages — Coming Soon!")
        }

        composable(NavRoutes.LINE_DETAIL) {
            Text("Line Detail — Coming Soon!")
        }
    }
}