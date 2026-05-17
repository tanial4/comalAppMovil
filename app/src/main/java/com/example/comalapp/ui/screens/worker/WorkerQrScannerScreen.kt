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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.comalapp.ui.components.worker.WorkerScaffold
import com.example.comalapp.ui.theme.OrderStatusColor
import com.example.comalapp.ui.theme.violet
import com.example.comalapp.ui.viewmodel.WorkerQrScannerViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun WorkerQrScannerScreen(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = (context.applicationContext as ComalApplication).container
    val viewModel: WorkerQrScannerViewModel = viewModel(
        factory = WorkerQrScannerViewModel.Factory(
            authRepository = container.authRepository,
            userRepository = container.userRepository,
            orderRepository = container.orderRepository,
            notificationRepository = container.notificationRepository,
        )
    )

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { viewModel.processScannedQr(it) }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (uiState.qrError) {
        AlertDialog(
            onDismissRequest = { viewModel.clearQrError() },
            title = {
                Text(
                    text = "QR no válido",
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Text(
                    text = "El código no corresponde a ninguna orden en estado \"Lista para recoger\".",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearQrError() }) {
                    Text("Entendido")
                }
            },
        )
    }

    WorkerScaffold(
        currentRoute = currentRoute,
        label = "TRABAJADOR",
        title = "Escanear QR",
        subtitle = "Confirmar entregas",
        userName = uiState.user?.fullName ?: "",
        userEmail = uiState.user?.email ?: "",
        onLogout = onLogout,
        onNavigate = onNavigate,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                uiState.isProcessing -> {
                    CircularProgressIndicator(color = violet)
                }

                uiState.deliverySuccess -> {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = OrderStatusColor.ready,
                            modifier = Modifier.size(72.dp),
                        )
                        Text(
                            text = "Entrega confirmada",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Orden #${uiState.deliveredOrderId?.takeLast(5)?.uppercase() ?: ""} marcada como entregada.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.clearDeliverySuccess() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text("Escanear otra")
                        }
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp),
                        )
                        Text(
                            text = "Escanea el QR del ticket",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Solo se aceptan órdenes en estado \"Lista para recoger\".",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val options = ScanOptions().apply {
                                    setPrompt("Apunta la cámara al QR del estudiante")
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
                            Text("Abrir escáner")
                        }
                    }
                }
            }
        }
    }
}