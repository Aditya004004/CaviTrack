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
import androidx.lifecycle.compose.LocalLifecycleOwner
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

    val hasCameraHardware = remember { context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_ANY) }
    if (!hasCameraHardware) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text("No camera available on this device.")
            Button(onClick = onUpdateComplete, modifier = Modifier.padding(top = 16.dp)) {
                Text("Go Back")
            }
        }
        return
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
                            android.widget.Toast.makeText(context, "Failed to bind camera: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                    coil.compose.AsyncImage(
                        model = photoUri,
                        contentDescription = "Captured Photo",
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
                    
                    if (isUploading) {
                        androidx.compose.material3.CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Uploading...")
                    } else {
                        Button(onClick = {
                            val currentUri = photoUri
                            if (entityId != null && currentUri != null) {
                                viewModel.uploadPhotoAndUpdateEntity(entityType, entityId, File(currentUri))
                            } else {
                                // Realistically, we should either save the photo path to pass to the creation screen,
                                // or block photo capture until the entity is created. For now, show an explicit error.
                                android.widget.Toast.makeText(context, "Cannot attach photo to an unsaved new item. Create it first.", android.widget.Toast.LENGTH_LONG).show()
                            }
                        }, enabled = !isUploading) {
                            Text("Upload and Save")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { photoUri = null }, enabled = !isUploading) {
                            Text("Retake")
                        }
                    }
                }
            }
        }

        if (photoUri == null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val file = File(context.cacheDir, "${System.currentTimeMillis()}_gallery.jpg")
                            inputStream?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (file.exists()) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    photoUri = file.absolutePath
                                    isCameraReady = false
                                }
                            }
                        }
                    }
                }
                Button(onClick = { galleryLauncher.launch("image/*") }) {
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
                                    if (cameraProviderFuture.isDone) {
                                        cameraProviderFuture.get().unbindAll()
                                    }
                                    photoUri = file.absolutePath
                                }
                                override fun onError(exc: ImageCaptureException) {
                                    exc.printStackTrace()
                                    android.widget.Toast.makeText(context, "Failed to capture image: ${exc.message}", android.widget.Toast.LENGTH_SHORT).show()
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
