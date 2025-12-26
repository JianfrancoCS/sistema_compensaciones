package com.agropay.ui.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Un Composable multiplataforma para escanear códigos QR.
 * La implementación real (actual) será provista por cada plataforma (Android/iOS).
 *
 * @param modifier El modificador para aplicar a este Composable.
 * @param onCodeScanned Una función callback que se invoca cuando se escanea un código con éxito.
 */
@Composable
expect fun QRCodeScannerView(
    modifier: Modifier = Modifier,
    onCodeScanned: (String) -> Unit,
    onPermissionDenied: () -> Unit
)
