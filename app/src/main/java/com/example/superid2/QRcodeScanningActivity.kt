package com.example.superid2

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WithPermission(
                modifier = Modifier.padding(),
                permission = Manifest.permission.CAMERA,
                permissionTextButton = "Conceder acesso a câmera"
            ) {
                TakePhotoScreen()
            }
        }
    }
}

class BarcodeAnalyzer(
    private val onBarcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            Log.d("QRCode", "Scanned: $value")
                            onBarcodeScanned(value)
                            imageProxy.close()
                            return@addOnSuccessListener
                        }
                    }
                    imageProxy.close()
                }
                .addOnFailureListener {
                    Log.e("QRCode", "Erro ao processar: ${it.message}")
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    imageCaptureUseCase: ImageCapture
){
    val previewUseCase = remember {
        Preview.Builder().build()
    }

    var cameraProvider by remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }

    var cameraControl by remember {
        mutableStateOf<CameraControl?>(null)
    }

    val localContext = LocalContext.current

    val imageAnalyzer = remember(localContext) {
        ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(localContext),
                    BarcodeAnalyzer { result ->
                        Log.d("QRCode", "Resultado do QR: $result")
                    }
                )
            }
    }

    fun rebindCameraProvider(){
        cameraProvider?.let {cameraProvider ->
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                localContext as LifecycleOwner,
                cameraSelector,
                previewUseCase,
                imageCaptureUseCase,
                imageAnalyzer
            )
            cameraControl = camera.cameraControl
        }
    }
    LaunchedEffect(Unit) {
        cameraProvider = ProcessCameraProvider
            .awaitInstance(localContext)
        rebindCameraProvider()
    }

    LaunchedEffect(lensFacing){
        rebindCameraProvider()
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).also {
                previewUseCase.surfaceProvider = it.surfaceProvider
                rebindCameraProvider()
            }
        }
    )
}

@Composable
fun TakePhotoScreen() {
    var lensFacing by remember {
        mutableIntStateOf(CameraSelector.LENS_FACING_FRONT)
    }
    var imageCaptureUseCase = remember {
        ImageCapture.Builder().build()
    }
    val localContext = LocalContext.current


    Box {
        CameraPreview(
            lensFacing = lensFacing,
            imageCaptureUseCase = imageCaptureUseCase
        )

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            Button(
                onClick = {
                    val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
                        File(localContext.externalCacheDir, "image.jpg")
                    ).build()
                    val callback = object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(
                            outputFileResults: ImageCapture.OutputFileResults
                        ) {
                            Log.i("Câmera", "Imagem salva no diretório dentro do app.")
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("Câmera", "Imagem não foi salva" + exception.message)
                        }
                    }
                    imageCaptureUseCase.takePicture(
                        outputFileOptions,
                        ContextCompat.getMainExecutor(localContext), callback
                    )
                }
            ) {
                Text("TakePhoto")
            }
        }
    }
}