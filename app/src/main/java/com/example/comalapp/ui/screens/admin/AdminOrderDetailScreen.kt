package com.example.comalapp.ui.screens.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.admin.OrderActionsCard
import com.example.comalapp.ui.components.admin.OrderDetailInfoCard
import com.example.comalapp.ui.components.admin.OrderProductsCard
import com.example.comalapp.ui.components.admin.OrderProgressBar
import com.example.comalapp.ui.components.shared.ConfirmDialog
import com.example.comalapp.ui.viewmodel.AdminOrderDetailViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: AdminOrderDetailViewModel = viewModel(
        factory = AdminOrderDetailViewModel.Factory(
            orderId = orderId,
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            orderRepository = container.orderRepository,
            productRepository = container.productRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCancelConfirm by remember { mutableStateOf(false) }

    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { viewModel.validateQrAndDeliver(it) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.qrError) {
        ConfirmDialog(
            title = "QR inválido",
            message = "El código QR escaneado no corresponde a esta orden. Verifica que estés escaneando el ticket correcto.",
            confirmText = "Entendido",
            confirmColor = MaterialTheme.colorScheme.primary,
            dismissText = null,
            onConfirm = { viewModel.clearQrError() },
            onDismiss = { viewModel.clearQrError() },
        )
    }

    if (showCancelConfirm) {
        ConfirmDialog(
            title = "Cancelar orden",
            message = "¿Estás seguro de que deseas cancelar la orden #${orderId.takeLast(5).uppercase()}? Esta acción no se puede deshacer.",
            confirmText = "Cancelar orden",
            dismissText = "Mantener",
            onConfirm = {
                showCancelConfirm = false
                viewModel.cancelOrder()
            },
            onDismiss = { showCancelConfirm = false },
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = MaterialTheme.colorScheme.primary) {
                Column {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        },
                        title = { },
                    )
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 20.dp),
                    ) {
                        Text(
                            text = "DETALLE DE ORDEN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "#${orderId.takeLast(5).uppercase()}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        if (uiState.isLoading || uiState.order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            val order = uiState.order!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OrderProgressBar(currentStatus = order.status)

                OrderDetailInfoCard(order = order)

                OrderProductsCard(items = uiState.summaryItems)

                if (order.status != "cancelled") {
                    OrderActionsCard(
                        status = order.status,
                        onAdvanceStatus = {
                            if (order.status == "ready") {
                                val options = ScanOptions().apply {
                                    setPrompt("Escanea el QR del ticket del estudiante")
                                    setBeepEnabled(true)
                                    setOrientationLocked(true)
                                    setBarcodeImageEnabled(false)
                                }
                                qrLauncher.launch(options)
                            } else {
                                viewModel.advanceStatus()
                            }
                        },
                        onRevertStatus = { viewModel.revertStatus() },
                    )
                }

                if (order.status != "cancelled" && order.status != "delivered") {
                    TextButton(
                        onClick = { showCancelConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Cancelar orden",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}