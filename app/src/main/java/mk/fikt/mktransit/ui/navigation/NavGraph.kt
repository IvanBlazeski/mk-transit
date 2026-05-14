package mk.fikt.mktransit.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mk.fikt.mktransit.ui.screens.auth.ForgotPasswordScreen
import mk.fikt.mktransit.ui.screens.auth.LoginScreen
import mk.fikt.mktransit.ui.screens.auth.RegisterScreen
import mk.fikt.mktransit.ui.screens.auth.RoleSelectionScreen
import mk.fikt.mktransit.ui.screens.auth.WelcomeScreen
import mk.fikt.mktransit.ui.screens.home.HomeScreen
import mk.fikt.mktransit.ui.screens.home.MapScreen
import mk.fikt.mktransit.ui.screens.lines.LineDetailScreen
import mk.fikt.mktransit.ui.screens.messages.ChatScreen
import mk.fikt.mktransit.ui.screens.messages.MessagesScreen
import mk.fikt.mktransit.ui.screens.operator.DriverModeScreen
import mk.fikt.mktransit.ui.screens.operator.OperatorDashboardScreen
import mk.fikt.mktransit.ui.screens.profile.ProfileScreen
import mk.fikt.mktransit.ui.screens.tickets.MyTicketsScreen
import mk.fikt.mktransit.ui.screens.tickets.QRTicketScreen
import mk.fikt.mktransit.ui.screens.tickets.TicketPurchaseScreen
import mk.fikt.mktransit.ui.screens.profile.FavoritesScreen
import mk.fikt.mktransit.ui.screens.tickets.QRScannerScreen


@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    isTablet: Boolean = false
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
                },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.WELCOME) { inclusive = true }
                    }
                },
                isTablet = isTablet
            )
        }
        composable(NavRoutes.FAVORITES) {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onLineClick = { lineId ->
                    navController.navigate(NavRoutes.lineDetail(lineId))
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
                onProfileClick = { navController.navigate(NavRoutes.PROFILE) },
                isTablet = isTablet
            )
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onLogout = {
                    navController.navigate(NavRoutes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                onOperatorClick = { navController.navigate(NavRoutes.OPERATOR_DASHBOARD) },
                onFavoritesClick = { navController.navigate(NavRoutes.FAVORITES) }
            )
        }

        composable(NavRoutes.OPERATOR_DASHBOARD) {
            OperatorDashboardScreen(
                onBack = { navController.popBackStack() },
                onDriverMode = { navController.navigate(NavRoutes.DRIVER_MODE) }
            )
        }

        composable(NavRoutes.DRIVER_MODE) {
            DriverModeScreen(
                onBack = { navController.popBackStack() },
                onScanQR = { navController.navigate(NavRoutes.QR_SCANNER) }
            )
        }

        composable(NavRoutes.QR_SCANNER) {
            QRScannerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.LINE_DETAIL,
            arguments = listOf(navArgument("lineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lineId = backStackEntry.arguments?.getString("lineId") ?: ""
            LineDetailScreen(
                lineId = lineId,
                onBack = { navController.popBackStack() },
                onBuyTicket = { navController.navigate(NavRoutes.ticketPurchase(it)) },
                onContactOperator = { operatorId ->
                    navController.navigate(NavRoutes.chat(operatorId))
                }
            )
        }

        composable(
            route = NavRoutes.TICKET_PURCHASE,
            arguments = listOf(navArgument("lineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lineId = backStackEntry.arguments?.getString("lineId") ?: ""
            TicketPurchaseScreen(
                lineId = lineId,
                lineName = "Bus Line",
                lineNumber = "1",
                onBack = { navController.popBackStack() },
                onPurchaseSuccess = { ticketId ->
                    navController.navigate(NavRoutes.qrTicket(ticketId)) {
                        popUpTo(NavRoutes.HOME)
                    }
                }
            )
        }

        composable(
            route = NavRoutes.QR_TICKET,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            QRTicketScreen(
                ticketId = ticketId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.MAP) {
            MapScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.TICKETS) {
            MyTicketsScreen(
                onBack = { navController.popBackStack() },
                onTicketClick = { ticketId ->
                    navController.navigate(NavRoutes.qrTicket(ticketId))
                }
            )
        }

        composable(NavRoutes.MESSAGES) {
            MessagesScreen(
                onBack = { navController.popBackStack() },
                onConversationClick = { conversationId ->
                    navController.navigate(NavRoutes.chat(conversationId))
                }
            )
        }

        composable(
            route = NavRoutes.CHAT,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(
                conversationId = conversationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}