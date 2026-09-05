package com.company.cavitrack.presentation.addupdate.photo







import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import kotlinx.coroutines.withContext
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import com.company.cavitrack.domain.model.EntityType
import androidx.lifecycle.flowWithLifecycle

@Composable
fun PhotoUpdateScreen(
    entityType: EntityType,
    entityId: String?,
    viewModel: PhotoUpdateViewModel = hiltViewModel(),
    onUpdateComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val cameraExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    val imageCapture = remember {
        val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
            .setResolutionStrategy(
                androidx.camera.core.resolutionselector.ResolutionStrategy(
                    android.util.Size(1280, 720),
                    androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()
        ImageCapture.Builder()
            .setResolutionSelector(resolutionSelector)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }
    
    var photoUri by rememberSaveable { mutableStateOf<String?>(null) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(viewModel.isSaved, lifecycleOwner) {
        viewModel.isSaved
            .flowWithLifecycle(lifecycleOwner.lifecycle, androidx.lifecycle.Lifecycle.State.STARTED)
            .collect {
                onUpdateComplete()
            }
    }

    LaunchedEffect(error) {
        error?.let { err ->
            snackbarHostState.showSnackbar(err.asString(context))
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
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No camera available on this device.")
            Button(onClick = onUpdateComplete, modifier = Modifier.padding(top = 16.dp)) {
                Text("Go Back")
            }
        }
        return
    }

    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()
    val isUploadingState = rememberUpdatedState(isUploading)

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (hasCameraPermission) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Camera Preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                if (lifecycleOwner.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.INITIALIZED)) {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageCapture
                                    )
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                coroutineScope.launch { snackbarHostState.showSnackbar("Failed to bind camera: ${e.message}") }
                            }
                        }, mainExecutor)
                        previewView
                    }
                )

                // Overlay captured photo and controls if a photo is taken
                if (photoUri != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            coil.compose.AsyncImage(
                                model = photoUri,
                                contentDescription = "Captured Photo",
                                modifier = Modifier.fillMaxWidth().height(300.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isUploading) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.msg_saving))
                            } else {
                                Button(onClick = {
                                    val currentUri = photoUri
                                    if (entityId != null && currentUri != null) {
                                        viewModel.uploadPhotoAndUpdateEntity(entityType, entityId, File(currentUri))
                                    } else {
                                        coroutineScope.launch { snackbarHostState.showSnackbar("Cannot attach photo to an unsaved new item. Create it first.") }
                                    }
                                }, enabled = true) {
                                    Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.action_upload_save))
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { 
                                    val currentUri = photoUri
                                    photoUri = null
                                    if (currentUri != null) {
                                        val file = File(currentUri)
                                        if (file.exists()) file.delete()
                                    }
                                }, enabled = true) {
                                    Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.action_retake))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission is required.", modifier = Modifier.padding(bottom = 16.dp))
                    Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                        Text("Grant Permission")
                    }
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
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val offlinePhotosDir = File(context.cacheDir, "offline_photos").apply { mkdirs() }
                                val file = File(offlinePhotosDir, "${System.currentTimeMillis()}_gallery.jpg")
                                inputStream?.use { input ->
                                    file.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                if (file.exists()) {
                                    // Downscale immediately to save disk space and memory
                                    com.company.cavitrack.util.ImageUtil.downscaleImage(file)

                                    withContext(Dispatchers.Main) {
                                        photoUri = file.absolutePath
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    coroutineScope.launch { snackbarHostState.showSnackbar("Failed to load image: ${e.message}") }
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
                        coroutineScope.launch(Dispatchers.IO) {
                            val offlinePhotosDir = File(context.cacheDir, "offline_photos").apply { mkdirs() }
                            val file = File(offlinePhotosDir, "${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                            
                            withContext(Dispatchers.Main) {
                                imageCapture.takePicture(
                                    outputOptions,
                                    mainExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            photoUri = file.absolutePath
                                        }
                                        override fun onError(exc: ImageCaptureException) {
                                            exc.printStackTrace()
                                            coroutineScope.launch { snackbarHostState.showSnackbar("Failed to capture image: ${exc.message}") }
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Text("Capture")
                }
            }
        }
    }
}
}





