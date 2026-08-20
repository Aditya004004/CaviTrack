package com.company.cavitrack.presentation.addupdate.photo

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.Executor

@Composable
fun PhotoUpdateScreen(
    entityType: String,
    entityId: String?,
    viewModel: PhotoUpdateViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onUpdateComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = ContextCompat.getMainExecutor(context)
    var photoUri by remember { mutableStateOf<String?>(null) }
    
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    DisposableEffect(lifecycleOwner) {
        onDispose {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }
    
    val isSaved by viewModel.isSaved.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    LaunchedEffect(isSaved) {
        if (isSaved) {
            onUpdateComplete()
        }
    }

    var hasCameraPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) 
    }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (error != null) {
            Text("Error: $error", color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }

        if (photoUri == null && hasCameraPermission) {
            AndroidView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, executor)
                    previewView
                }
            )
        } else if (photoUri == null && !hasCameraPermission) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Camera permission is required.")
            }
        } else {
            // Show captured photo or success
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Photo Captured: $photoUri")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (entityId != null) {
                            viewModel.uploadPhotoAndUpdateEntity(entityType, entityId, File(photoUri!!))
                        } else {
                            // If it's a new item, they should probably create it first. 
                            // But for now, we just pass onUpdateComplete if not possible
                            onUpdateComplete()
                        }
                    }) {
                        Text("Upload and Save")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { photoUri = null }) {
                        Text("Retake")
                    }
                }
            }
        }

        if (photoUri == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { /* TODO open gallery */ }) {
                    Text("Gallery")
                }
                Button(
                    onClick = {
                        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCapture.takePicture(
                            outputOptions,
                            executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    photoUri = file.absolutePath
                                }
                                override fun onError(exc: ImageCaptureException) {
                                    exc.printStackTrace()
                                }
                            }
                        )
                    }
                ) {
                    Text("Capture")
                }
            }
        }
    }
}
