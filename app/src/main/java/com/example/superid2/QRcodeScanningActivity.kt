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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.File

class QrScannerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WithPermission(
                permission = Manifest.permission.CAMERA
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
                       buscarDocLogin(result)
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
    }
}

fun buscarDocLogin(tokenLogin: String) {
    val firestore = Firebase.firestore
    val usuarioAtual = Firebase.auth.currentUser

    firestore.collection("login")
        .whereEqualTo("loginToken", tokenLogin)
        .get()
        .addOnSuccessListener { resultado ->
            if (!resultado.isEmpty) {
                val documentoLogin = resultado.documents.first()
                documentoLogin.reference.update("uid", usuarioAtual?.uid)
            }
        }
}

