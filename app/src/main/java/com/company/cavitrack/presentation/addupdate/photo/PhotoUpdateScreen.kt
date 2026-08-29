package com.company.cavitrack.presentation.addupdate.photo







import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import kotlinx.coroutines.withContext
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import kotlinx.coroutines.launch

@Composable
fun PhotoUpdateScreen(
    entityType: com.company.cavitrack.domain.model.EntityType,
    entityId: String?,
    viewModel: PhotoUpdateViewModel = hiltViewModel(),
    onUpdateComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
    val isSavedState = rememberUpdatedState(viewModel.isSaved.value)
    val isUploadingState = rememberUpdatedState(isUploading)

    DisposableEffect(photoUri) {
        onDispose {
            val uri = photoUri
            if (uri != null && !isSavedState.value && !isUploadingState.value) {
                val file = File(uri)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (error != null) {
            Text("Error: $error", color = androidx.compose.material3.MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }

        if (hasCameraPermission) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Camera Preview (always mounted to prevent slow rebinding on retake)
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
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
                                Toast.makeText(context, "Failed to bind camera: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }, executor)
                        previewView
                    }
                )

                // Overlay captured photo and controls if a photo is taken
                if (photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .androidx.compose.foundation.background(androidx.compose.material3.MaterialTheme.colorScheme.background),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
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
                                        Toast.makeText(context, "Cannot attach photo to an unsaved new item. Create it first.", Toast.LENGTH_LONG).show()
                                    }
                                }, enabled = !isUploading) {
                                    Text("Upload and Save")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { 
                                    val currentUri = photoUri
                                    photoUri = null
                                    if (currentUri != null) {
                                        val file = File(currentUri)
                                        if (file.exists()) file.delete()
                                    }
                                }, enabled = !isUploading) {
                                    Text("Retake")
                                }
                            }
                        }
                    }
                }
            }
        } else if (!hasCameraPermission) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Camera permission is required.")
                    Button(onClick = onUpdateComplete, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Go Back")
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
                        coroutineScope.launch(Dispatchers.IO) {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val offlinePhotosDir = File(context.filesDir, "offline_photos").apply { mkdirs() }
                            val file = File(offlinePhotosDir, "${System.currentTimeMillis()}_gallery.jpg")
                            inputStream?.use { input ->
                                file.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            if (file.exists()) {
                                // Downscale immediately to save disk space and memory
                                withContext(Dispatchers.Default) {
                                    val options = android.graphics.BitmapFactory.Options().apply {
                                        inJustDecodeBounds = true
                                    }
                                    android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
                                    val maxDim = 1280
                                    var inSampleSize = 1
                                    if (options.outHeight > maxDim || options.outWidth > maxDim) {
                                        val halfHeight = options.outHeight / 2
                                        val halfWidth = options.outWidth / 2
                                        while ((halfHeight / inSampleSize) >= maxDim || (halfWidth / inSampleSize) >= maxDim) {
                                            inSampleSize *= 2
                                        }
                                    }
                                    val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                                        this.inSampleSize = inSampleSize
                                    }
                                    try {
                                        val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                                        if (bitmap != null) {
                                            try {
                                                java.io.FileOutputStream(file).use { out ->
                                                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
                                                }
                                            } finally {
                                                bitmap.recycle()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    photoUri = file.absolutePath
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
                        val offlinePhotosDir = File(context.filesDir, "offline_photos").apply { mkdirs() }
                        val file = File(offlinePhotosDir, "${System.currentTimeMillis()}.jpg")
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
                                    Toast.makeText(context, "Failed to capture image: ${exc.message}", Toast.LENGTH_SHORT).show()
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





