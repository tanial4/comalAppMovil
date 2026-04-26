package com.example.comalapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.screens.auth.LoginScreen
import com.example.comalapp.ui.screens.auth.RegisterScreen
import com.example.comalapp.ui.screens.student.StudentCartScreen
import com.example.comalapp.ui.screens.student.StudentHomeScreen
import com.example.comalapp.ui.screens.student.StudentMenuScreen
import com.example.comalapp.ui.screens.student.StudentNotificationsScreen
import com.example.comalapp.ui.screens.student.StudentOrderHistoryScreen
import com.example.comalapp.ui.screens.student.StudentProfileScreen
import com.example.comalapp.ui.viewmodel.StudentCartViewModel

private val studentTabRoutes = setOf(
    AppDestinations.STUDENT_HOME,
    AppDestinations.STUDENT_MENU,
    AppDestinations.STUDENT_ORDER_HISTORY,
    AppDestinations.STUDENT_PROFILE,
)

private fun navigateStudentTab(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String,
) {
    if (targetRoute == currentRoute) return
    navController.navigate(targetRoute) {
        popUpTo(AppDestinations.STUDENT_HOME) {
            saveState = true
            inclusive = false
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateFromSecondary(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String,
) {
    if (targetRoute == currentRoute) return
    if (targetRoute in studentTabRoutes) {
        navController.navigate(targetRoute) {
            popUpTo(AppDestinations.STUDENT_HOME) {
                inclusive = false
            }
            launchSingleTop = true
        }
    } else {
        navController.navigate(targetRoute)
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.LOGIN,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable(AppDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    when (role) {
                        "student" -> navController.navigate(AppDestinations.STUDENT_GRAPH) {
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
                    navController.navigate(AppDestinations.STUDENT_GRAPH) {
                        popUpTo(AppDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
            )
        }

        navigation(
            route = AppDestinations.STUDENT_GRAPH,
            startDestination = AppDestinations.STUDENT_HOME,
        ) {
            composable(AppDestinations.STUDENT_HOME) { backStack ->
                val context = LocalContext.current
                val container = (context.applicationContext as ComalApplication).container
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

                StudentHomeScreen(
                    currentRoute = currentRoute,
                    notificationCount = 0,
                    cartItemCount = uiState.totalItemCount,
                    userName = "",
                    hasActiveOrder = false,
                    activeOrderId = "",
                    activeOrderStatus = "",
                    activeOrderProductCount = 0,
                    activeOrderEstimatedMinutes = null,
                    products = emptyList(),
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = {
                        navController.navigate(AppDestinations.STUDENT_CART)
                    },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onViewOrderStatus = {
                        navController.navigate(AppDestinations.STUDENT_ORDER_STATUS)
                    },
                    onAddToCart = { product -> cartViewModel.addProduct(product) },
                )
            }

            composable(AppDestinations.STUDENT_MENU) { backStack ->
                val context = LocalContext.current
                val container = (context.applicationContext as ComalApplication).container
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

                StudentMenuScreen(
                    currentRoute = currentRoute,
                    notificationCount = 0,
                    cartItemCount = uiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = {
                        navController.navigate(AppDestinations.STUDENT_CART)
                    },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onAddToCart = { product -> cartViewModel.addProduct(product) },
                )
            }

            composable(AppDestinations.STUDENT_CART) { backStack ->
                val context = LocalContext.current
                val container = (context.applicationContext as ComalApplication).container
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

                StudentCartScreen(
                    currentRoute = currentRoute,
                    notificationCount = 0,
                    cartItemCount = uiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = { },
                    onNavigate = { route ->
                        navigateFromSecondary(navController, currentRoute, route)
                    },
                    onOrderConfirmed = {
                        navController.navigate(AppDestinations.STUDENT_ORDER_STATUS) {
                            popUpTo(AppDestinations.STUDENT_CART) { inclusive = true }
                        }
                    },
                    cartViewModel = cartViewModel,
                )
            }

            composable(AppDestinations.STUDENT_ORDER_STATUS) {
                // StudentOrderStatusScreen()
            }

            composable(AppDestinations.STUDENT_ORDER_HISTORY) { backStack ->
                val context = LocalContext.current
                val container = (context.applicationContext as ComalApplication).container
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

                StudentOrderHistoryScreen(
                    currentRoute = currentRoute,
                    notificationCount = 0,
                    cartItemCount = uiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = { navController.navigate(AppDestinations.STUDENT_CART) },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(AppDestinations.studentOrderDetail(orderId))
                    },
                )
            }

            composable(AppDestinations.STUDENT_PROFILE) { backStack ->
                val context = LocalContext.current
                val container = (context.applicationContext as ComalApplication).container
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

                StudentProfileScreen(
                    currentRoute = currentRoute,
                    notificationCount = 0,
                    cartItemCount = uiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = { navController.navigate(AppDestinations.STUDENT_CART) },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.STUDENT_GRAPH) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppDestinations.STUDENT_NOTIFICATIONS) { backStack ->
                val context = LocalContext.current
                val container = (context.applicationContext as ComalApplication).container
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()

                StudentNotificationsScreen(
                    currentRoute = currentRoute,
                    notificationCount = 0,
                    cartItemCount = uiState.totalItemCount,
                    onNotificationsClick = { },
                    onCartClick = { navController.navigate(AppDestinations.STUDENT_CART) },
                    onNavigate = { route ->
                        navigateFromSecondary(navController, currentRoute, route)
                    },
                )
            }

            composable(
                route = AppDestinations.STUDENT_ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                // StudentOrderDetailScreen(orderId = orderId)
            }
        }

        composable(AppDestinations.WORKER_ORDERS) {
            // WorkerOrdersScreen()
        }

        composable(AppDestinations.ADMIN_PRODUCTS) {
            // AdminProductsScreen()
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

@Composable
private fun studentCartViewModel(
    backStack: NavBackStackEntry,
    navController: NavHostController,
    container: com.example.comalapp.data.AppContainer,
): StudentCartViewModel {
    val parentEntry = remember(backStack) {
        navController.getBackStackEntry(AppDestinations.STUDENT_GRAPH)
    }
    return viewModel(
        viewModelStoreOwner = parentEntry,
        factory = StudentCartViewModel.Factory(
            orderRepository = container.orderRepository,
            authRepository = container.authRepository,
        ),
    )
}