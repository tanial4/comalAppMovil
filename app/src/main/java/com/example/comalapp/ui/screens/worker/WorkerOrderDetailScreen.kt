package com.example.comalapp.ui.screens.worker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.comalapp.ComalApplication
import com.example.comalapp.ui.components.admin.OrderDetailInfoCard
import com.example.comalapp.ui.components.admin.OrderProductsCard
import com.example.comalapp.ui.components.admin.OrderProgressBar
import com.example.comalapp.ui.components.shared.ConfirmDialog
import com.example.comalapp.ui.theme.OrderStatusColor
import com.example.comalapp.ui.viewmodel.WorkerOrderDetailViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: WorkerOrderDetailViewModel = viewModel(
        factory = WorkerOrderDetailViewModel.Factory(
            orderId = orderId,
            orderRepository = container.orderRepository,
            productRepository = container.productRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
            message = "El código QR no corresponde a esta orden. Verifica que el estudiante muestre el ticket correcto.",
            confirmText = "Entendido",
            confirmColor = MaterialTheme.colorScheme.primary,
            dismissText = null,
            onConfirm = { viewModel.clearQrError() },
            onDismiss = { viewModel.clearQrError() },
        )
    }

    val order = uiState.order

    val advanceLabel = when (order?.status) {
        "pending"   -> "Iniciar preparación"
        "preparing" -> "Marcar como lista"
        else        -> null
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
        when {
            uiState.deliverySuccess -> {
                DeliverySuccessContent(
                    orderId = orderId,
                    onBack = onBack,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            uiState.isLoading || order == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OrderDetailInfoCard(order = order)

                    OrderProgressBar(currentStatus = order.status)

                    OrderProductsCard(items = uiState.summaryItems)

                    if (order.status != "delivered" && order.status != "cancelled") {
                        if (order.status == "ready") {
                            Button(
                                onClick = {
                                    val options = ScanOptions().apply {
                                        setPrompt("Escanea el QR del ticket del estudiante")
                                        setBeepEnabled(true)
                                        setOrientationLocked(true)
                                        setBarcodeImageEnabled(false)
                                    }
                                    qrLauncher.launch(options)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text("Confirmar entrega con QR")
                            }
                        } else if (advanceLabel != null) {
                            Button(
                                onClick = { viewModel.advanceStatus() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {
                                Text(advanceLabel)
                            }
                        }
                    }

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Volver a la lista",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun DeliverySuccessContent(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = OrderStatusColor.ready,
                modifier = Modifier.size(88.dp),
            )

            Text(
                text = "¡Entrega confirmada!",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "La orden #${orderId.takeLast(5).uppercase()} fue entregada exitosamente al estudiante.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Volver a órdenes")
            }
        }
    }
}