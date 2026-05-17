package com.example.comalapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.comalapp.ui.screens.admin.AdminDashboardScreen
import com.example.comalapp.ui.screens.admin.AdminOrderDetailScreen
import com.example.comalapp.ui.screens.admin.AdminOrdersScreen
import com.example.comalapp.ui.screens.admin.AdminProductFormScreen
import com.example.comalapp.ui.screens.admin.AdminProductsScreen
import com.example.comalapp.ui.screens.admin.AdminUsersScreen
import com.example.comalapp.ui.screens.admin.AdminWorkerFormScreen
import com.example.comalapp.ui.screens.admin.AdminWorkersScreen
import com.example.comalapp.ui.screens.auth.ForgotPasswordScreen
import com.example.comalapp.ui.screens.auth.LoginScreen
import com.example.comalapp.ui.screens.auth.RegisterScreen
import com.example.comalapp.ui.screens.student.StudentCartScreen
import com.example.comalapp.ui.screens.student.StudentHomeScreen
import com.example.comalapp.ui.screens.student.StudentMenuScreen
import com.example.comalapp.ui.screens.student.StudentNotificationsScreen
import com.example.comalapp.ui.screens.student.StudentOrderConfirmScreen
import com.example.comalapp.ui.screens.student.StudentOrderHistoryScreen
import com.example.comalapp.ui.screens.student.StudentOrderStatusScreen
import com.example.comalapp.ui.screens.student.StudentProfileScreen
import com.example.comalapp.ui.screens.student.StudentTicketScreen
import com.example.comalapp.ui.screens.worker.WorkerHomeScreen
import com.example.comalapp.ui.screens.worker.WorkerOrderDetailScreen
import com.example.comalapp.ui.screens.worker.WorkerOrdersScreen
import com.example.comalapp.ui.screens.worker.WorkerProductsScreen
import com.example.comalapp.ui.screens.worker.WorkerQrScannerScreen
import com.example.comalapp.ui.viewmodel.StudentCartViewModel
import com.example.comalapp.ui.viewmodel.StudentHomeViewModel
import com.example.comalapp.ui.viewmodel.StudentNotificationsViewModel

private val studentTabRoutes = setOf(
    AppDestinations.STUDENT_HOME,
    AppDestinations.STUDENT_MENU,
    AppDestinations.STUDENT_ORDER_HISTORY,
    AppDestinations.STUDENT_PROFILE,
)

private val adminTabRoutes = setOf(
    AppDestinations.ADMIN_DASHBOARD,
    AppDestinations.ADMIN_PRODUCTS,
    AppDestinations.ADMIN_USERS,
    AppDestinations.ADMIN_ORDERS,
    AppDestinations.ADMIN_WORKERS,
)

private val workerTabRoutes = setOf(
    AppDestinations.WORKER_HOME,
    AppDestinations.WORKER_ORDERS,
    AppDestinations.WORKER_PRODUCTS,
    AppDestinations.WORKER_QR_SCANNER,
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
            popUpTo(AppDestinations.STUDENT_HOME) { inclusive = false }
            launchSingleTop = true
        }
    } else {
        navController.navigate(targetRoute)
    }
}

private fun navigateAdminTab(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String,
) {
    if (targetRoute == currentRoute) return
    navController.navigate(targetRoute) {
        popUpTo(AppDestinations.ADMIN_DASHBOARD) {
            saveState = true
            inclusive = false
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun navigateWorkerTab(
    navController: NavHostController,
    currentRoute: String?,
    targetRoute: String,
) {
    if (targetRoute == currentRoute) return
    navController.navigate(targetRoute) {
        popUpTo(AppDestinations.WORKER_HOME) {
            saveState = true
            inclusive = false
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.LOGIN,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container

    LaunchedEffect(Unit) {
        var isFirstEmission = true
        container.authRepository.observeAuthState().collect { isAuthenticated ->
            if (isFirstEmission) {
                isFirstEmission = false
                if (isAuthenticated) {
                    val uid = container.authRepository.currentUserId() ?: return@collect
                    container.userRepository.getUserById(uid).onSuccess { user ->
                        val destination = when (user.role) {
                            "student" -> AppDestinations.STUDENT_GRAPH
                            "worker"  -> AppDestinations.WORKER_GRAPH
                            "admin"   -> AppDestinations.ADMIN_GRAPH
                            else      -> return@onSuccess
                        }
                        navController.navigate(destination) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                    }
                }
            } else if (!isAuthenticated) {
                val authRoutes = setOf(
                    AppDestinations.LOGIN,
                    AppDestinations.REGISTER,
                    AppDestinations.FORGOT_PASSWORD,
                )
                if (navController.currentDestination?.route !in authRoutes) {
                    navController.navigate(AppDestinations.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

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
                        "worker" -> navController.navigate(AppDestinations.WORKER_GRAPH) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                        "admin" -> navController.navigate(AppDestinations.ADMIN_GRAPH) {
                            popUpTo(AppDestinations.LOGIN) { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(AppDestinations.FORGOT_PASSWORD)
                },
            )
        }

        composable(AppDestinations.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
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
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val homeViewModel = studentHomeViewModel(backStack, navController, container)
                val notificationsViewModel = studentNotificationsViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

                StudentHomeScreen(
                    currentRoute = currentRoute,
                    notificationCount = notificationsUiState.unreadCount,
                    cartItemCount = cartUiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = {
                        navController.navigate(AppDestinations.STUDENT_CART)
                    },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onViewOrderStatus = { orderId ->
                        navController.navigate(AppDestinations.studentOrderStatus(orderId))
                    },
                    onAddToCart = { product, qty ->
                        repeat(qty) { cartViewModel.addProduct(product) }
                    },
                    homeViewModel = homeViewModel,
                )
            }

            composable(AppDestinations.STUDENT_MENU) { backStack ->
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val notificationsViewModel = studentNotificationsViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

                StudentMenuScreen(
                    currentRoute = currentRoute,
                    notificationCount = notificationsUiState.unreadCount,
                    cartItemCount = cartUiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = {
                        navController.navigate(AppDestinations.STUDENT_CART)
                    },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onAddToCart = { product, qty ->
                        repeat(qty) { cartViewModel.addProduct(product) }
                    },
                )
            }

            composable(AppDestinations.STUDENT_CART) { backStack ->
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val notificationsViewModel = studentNotificationsViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

                StudentCartScreen(
                    currentRoute = currentRoute,
                    notificationCount = notificationsUiState.unreadCount,
                    cartItemCount = cartUiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = { },
                    onNavigate = { route ->
                        navigateFromSecondary(navController, currentRoute, route)
                    },
                    onOrderConfirmed = {
                        navController.navigate(AppDestinations.STUDENT_ORDER_CONFIRM)
                    },
                    cartViewModel = cartViewModel,
                )
            }

            composable(AppDestinations.STUDENT_ORDER_CONFIRM) { backStack ->
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val homeViewModel = studentHomeViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(cartUiState.orderConfirmed) {
                    if (cartUiState.orderConfirmed) {
                        val orderId = cartUiState.confirmedOrderId ?: return@LaunchedEffect
                        cartViewModel.resetConfirmed()
                        navController.navigate(AppDestinations.studentOrderStatus(orderId)) {
                            popUpTo(AppDestinations.STUDENT_CART) { inclusive = true }
                        }
                    }
                }

                StudentOrderConfirmScreen(
                    items = cartUiState.items,
                    userName = homeUiState.user?.fullName ?: "",
                    onConfirm = { cartViewModel.confirmOrder() },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppDestinations.STUDENT_ORDER_STATUS,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                StudentOrderStatusScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                    onGoHome = {
                        navController.navigate(AppDestinations.STUDENT_HOME) {
                            popUpTo(AppDestinations.STUDENT_GRAPH) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onViewTicket = {
                        navController.navigate(AppDestinations.studentTicket(orderId))
                    },
                )
            }

            composable(AppDestinations.STUDENT_ORDER_HISTORY) { backStack ->
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val notificationsViewModel = studentNotificationsViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

                StudentOrderHistoryScreen(
                    currentRoute = currentRoute,
                    notificationCount = notificationsUiState.unreadCount,
                    cartItemCount = cartUiState.totalItemCount,
                    onNotificationsClick = {
                        navController.navigate(AppDestinations.STUDENT_NOTIFICATIONS)
                    },
                    onCartClick = { navController.navigate(AppDestinations.STUDENT_CART) },
                    onNavigate = { route ->
                        navigateStudentTab(navController, currentRoute, route)
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(AppDestinations.studentOrderStatus(orderId))
                    },
                )
            }

            composable(AppDestinations.STUDENT_PROFILE) { backStack ->
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val notificationsViewModel = studentNotificationsViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

                StudentProfileScreen(
                    currentRoute = currentRoute,
                    notificationCount = notificationsUiState.unreadCount,
                    cartItemCount = cartUiState.totalItemCount,
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
                val cartViewModel = studentCartViewModel(backStack, navController, container)
                val notificationsViewModel = studentNotificationsViewModel(backStack, navController, container)
                val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
                val notificationsUiState by notificationsViewModel.uiState.collectAsStateWithLifecycle()

                StudentNotificationsScreen(
                    currentRoute = currentRoute,
                    notificationCount = notificationsUiState.unreadCount,
                    cartItemCount = cartUiState.totalItemCount,
                    onNotificationsClick = { },
                    onCartClick = { navController.navigate(AppDestinations.STUDENT_CART) },
                    onNavigate = { route ->
                        navigateFromSecondary(navController, currentRoute, route)
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(AppDestinations.studentOrderStatus(orderId))
                    },
                    notificationsViewModel = notificationsViewModel,
                )
            }

            composable(
                route = AppDestinations.STUDENT_ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
            }

            composable(
                route = AppDestinations.STUDENT_TICKET,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                StudentTicketScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                    onTrackOrder = {
                        navController.navigate(AppDestinations.studentOrderStatus(orderId)) {
                            popUpTo(AppDestinations.studentTicket(orderId)) { inclusive = true }
                        }
                    },
                    onGoMenu = {
                        navController.navigate(AppDestinations.STUDENT_MENU) {
                            popUpTo(AppDestinations.STUDENT_HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        navigation(
            route = AppDestinations.WORKER_GRAPH,
            startDestination = AppDestinations.WORKER_HOME,
        ) {
            composable(AppDestinations.WORKER_HOME) {
                WorkerHomeScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateWorkerTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.WORKER_GRAPH) { inclusive = true }
                        }
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(AppDestinations.workerOrderDetail(orderId))
                    },
                    onSeeAllOrders = {
                        navigateWorkerTab(navController, currentRoute, AppDestinations.WORKER_ORDERS)
                    },
                    onScanQr = {
                        navigateWorkerTab(navController, currentRoute, AppDestinations.WORKER_QR_SCANNER)
                    },
                )
            }

            composable(AppDestinations.WORKER_ORDERS) {
                WorkerOrdersScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateWorkerTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.WORKER_GRAPH) { inclusive = true }
                        }
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(AppDestinations.workerOrderDetail(orderId))
                    },
                )
            }

            composable(AppDestinations.WORKER_PRODUCTS) {
                WorkerProductsScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateWorkerTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.WORKER_GRAPH) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppDestinations.WORKER_QR_SCANNER) {
                WorkerQrScannerScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateWorkerTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.WORKER_GRAPH) { inclusive = true }
                        }
                    },
                )
            }

            composable(
                route = AppDestinations.WORKER_ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                WorkerOrderDetailScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        navigation(
            route = AppDestinations.ADMIN_GRAPH,
            startDestination = AppDestinations.ADMIN_DASHBOARD,
        ) {
            composable(AppDestinations.ADMIN_DASHBOARD) {
                AdminDashboardScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navigateAdminTab(navController, currentRoute, route)
                    },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.ADMIN_GRAPH) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppDestinations.ADMIN_PRODUCTS) {
                AdminProductsScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navigateAdminTab(navController, currentRoute, route)
                    },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.ADMIN_GRAPH) { inclusive = true }
                        }
                    },
                    onAddProduct = {
                        navController.navigate(AppDestinations.adminProductForm())
                    },
                    onEditProduct = { productId ->
                        navController.navigate(AppDestinations.adminProductForm(productId))
                    },
                )
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
                AdminProductFormScreen(
                    productId = productId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(AppDestinations.ADMIN_ORDERS) {
                AdminOrdersScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateAdminTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.ADMIN_GRAPH) { inclusive = true }
                        }
                    },
                    onOrderClick = { orderId ->
                        navController.navigate(AppDestinations.adminOrderDetail(orderId))
                    },
                )
            }

            composable(AppDestinations.ADMIN_USERS) {
                AdminUsersScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateAdminTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.ADMIN_GRAPH) { inclusive = true }
                        }
                    },
                )
            }

            composable(AppDestinations.ADMIN_WORKERS) {
                AdminWorkersScreen(
                    currentRoute = currentRoute,
                    onNavigate = { route -> navigateAdminTab(navController, currentRoute, route) },
                    onLogout = {
                        navController.navigate(AppDestinations.LOGIN) {
                            popUpTo(AppDestinations.ADMIN_GRAPH) { inclusive = true }
                        }
                    },
                    onAddWorker = {
                        navController.navigate(AppDestinations.ADMIN_WORKER_FORM)
                    },
                )
            }

            composable(AppDestinations.ADMIN_WORKER_FORM) {
                AdminWorkerFormScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                )
            }

            composable(
                route = AppDestinations.ADMIN_ORDER_DETAIL,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                AdminOrderDetailScreen(
                    orderId = orderId,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = AppDestinations.ADMIN_USER_DETAIL,
                arguments = listOf(navArgument("userId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            }
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
            notificationRepository = container.notificationRepository,
        ),
    )
}

@Composable
private fun studentHomeViewModel(
    backStack: NavBackStackEntry,
    navController: NavHostController,
    container: com.example.comalapp.data.AppContainer,
): StudentHomeViewModel {
    val parentEntry = remember(backStack) {
        navController.getBackStackEntry(AppDestinations.STUDENT_GRAPH)
    }
    return viewModel(
        viewModelStoreOwner = parentEntry,
        factory = StudentHomeViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            productRepository = container.productRepository,
            orderRepository = container.orderRepository,
        ),
    )
}

@Composable
private fun studentNotificationsViewModel(
    backStack: NavBackStackEntry,
    navController: NavHostController,
    container: com.example.comalapp.data.AppContainer,
): StudentNotificationsViewModel {
    val parentEntry = remember(backStack) {
        navController.getBackStackEntry(AppDestinations.STUDENT_GRAPH)
    }
    return viewModel(
        viewModelStoreOwner = parentEntry,
        factory = StudentNotificationsViewModel.Factory(
            authRepository = container.authRepository,
            notificationRepository = container.notificationRepository,
        ),
    )
}

