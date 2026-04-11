package com.example.comalapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.comalapp.ui.screens.auth.LoginScreen
import com.example.comalapp.ui.screens.auth.RegisterScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.LOGIN,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role) {
                        "student" -> navController.navigate(AppDestinations.STUDENT_MENU) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                        "worker" -> navController.navigate(AppDestinations.WORKER_ORDERS) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                        "admin" -> navController.navigate(AppDestinations.ADMIN_PRODUCTS) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER)
                },
            )
        }

        composable(AppDestinations.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AppDestinations.STUDENT_MENU) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        composable(AppDestinations.STUDENT_MENU) {
            // StudentMenuScreen()
        }

        composable(AppDestinations.WORKER_ORDERS) {
            // WorkerOrdersScreen()
        }

        composable(AppDestinations.ADMIN_PRODUCTS) {
            // AdminProductsScreen()
        }

        composable(
            route = AppDestinations.STUDENT_ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            // StudentOrderDetailScreen(orderId = orderId)
        }

        composable(
            route = AppDestinations.WORKER_ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            // WorkerOrderDetailScreen(orderId = orderId)
        }

        composable(
            route = AppDestinations.ADMIN_PRODUCT_FORM,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            // AdminProductFormScreen(productId = productId)
        }
    }
}