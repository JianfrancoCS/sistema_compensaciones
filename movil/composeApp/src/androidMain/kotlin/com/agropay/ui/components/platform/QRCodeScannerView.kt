package com.agropay.ui.components.platform

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
actual fun QRCodeScannerView(
    modifier: Modifier,
    onCodeScanned: (String) -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                hasPermission = true
            } else {
                onPermissionDenied()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasPermission) {
        Box(modifier = modifier.fillMaxSize()) {
            // Vista de la cámara
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                            val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_EAN_13, Barcode.FORMAT_CODE_128).build()
                            val scanner = BarcodeScanning.getClient(options)
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isNotEmpty()) {
                                            barcodes.first().rawValue?.let {
                                                cameraProvider.unbindAll()
                                                onCodeScanned(it)
                                            }
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            }
                        }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay con recuadro de escaneo
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Tamaño del recuadro de escaneo (70% del ancho de la pantalla)
                val scanSize = canvasWidth * 0.7f
                val left = (canvasWidth - scanSize) / 2
                val top = (canvasHeight - scanSize) / 2

                // Dibujar overlay oscuro con recorte en el centro
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset.Zero,
                    size = Size(canvasWidth, top)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(0f, top + scanSize),
                    size = Size(canvasWidth, canvasHeight - top - scanSize)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(0f, top),
                    size = Size(left, scanSize)
                )
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    topLeft = Offset(left + scanSize, top),
                    size = Size(canvasWidth - left - scanSize, scanSize)
                )

                // Dibujar bordes del recuadro de escaneo
                val cornerLength = 40f
                val strokeWidth = 8f

                // Esquinas del recuadro
                // Esquina superior izquierda
                drawLine(Color.White, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
                drawLine(Color.White, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)

                // Esquina superior derecha
                drawLine(Color.White, Offset(left + scanSize - cornerLength, top), Offset(left + scanSize, top), strokeWidth)
                drawLine(Color.White, Offset(left + scanSize, top), Offset(left + scanSize, top + cornerLength), strokeWidth)

                // Esquina inferior izquierda
                drawLine(Color.White, Offset(left, top + scanSize - cornerLength), Offset(left, top + scanSize), strokeWidth)
                drawLine(Color.White, Offset(left, top + scanSize), Offset(left + cornerLength, top + scanSize), strokeWidth)

                // Esquina inferior derecha
                drawLine(Color.White, Offset(left + scanSize, top + scanSize - cornerLength), Offset(left + scanSize, top + scanSize), strokeWidth)
                drawLine(Color.White, Offset(left + scanSize - cornerLength, top + scanSize), Offset(left + scanSize, top + scanSize), strokeWidth)
            }

            // Texto de instrucción
            Text(
                text = "Coloca el código QR dentro del recuadro",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 48.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}
