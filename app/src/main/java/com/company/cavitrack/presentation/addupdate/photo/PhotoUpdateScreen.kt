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
import androidx.compose.material3.OutlinedButton
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
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get()?.unbindAll()
                }
            } catch (e: Exception) {
                if (com.company.cavitrack.BuildConfig.DEBUG) {
                    android.util.Log.w("PhotoUpdateScreen", "Error unbinding camera", e)
                }
            }
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
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    val cannotAttachPhotoMsg = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.msg_cannot_attach_photo_unsaved)
    val errorImageLoadMsg = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_image_load)
    val errorCameraBindMsg = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_camera_bind)
    val errorCameraCaptureFormat = androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.error_camera_capture)

    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                var tempFile: File? = null
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val offlinePhotosDir = File(context.cacheDir, "offline_photos").apply { mkdirs() }
                    val file = File(offlinePhotosDir, "${System.currentTimeMillis()}_gallery.jpg")
                    tempFile = file
                    inputStream?.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (file.exists()) {
                        withContext(Dispatchers.Main) {
                            photoUri = file.absolutePath
                        }
                    }
                } catch (e: Exception) {
                    tempFile?.let { if (it.exists()) it.delete() }
                    if (com.company.cavitrack.BuildConfig.DEBUG) {
                        android.util.Log.w("PhotoUpdateScreen", "Failed to load image", e)
                    }
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(errorImageLoadMsg)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.isSaved.collect {
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

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    val hasCameraHardware = remember { context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_CAMERA_ANY) }
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if (!hasCameraHardware) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.msg_no_camera))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_choose_gallery))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onUpdateComplete) {
                        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_go_back))
                    }
                }
            }
        } else if (hasCameraPermission) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Camera Preview
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.surfaceProvider = previewView.surfaceProvider
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                if (com.company.cavitrack.BuildConfig.DEBUG) {
                                    android.util.Log.w("PhotoUpdateScreen", "Failed to bind camera", e)
                                }
                                coroutineScope.launch { snackbarHostState.showSnackbar(errorCameraBindMsg) }
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
                                        coroutineScope.launch { snackbarHostState.showSnackbar(cannotAttachPhotoMsg) }
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
                    Text(
                        androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.msg_camera_permission_required),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }) {
                        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_grant_permission))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_choose_gallery))
                    }
                    Button(onClick = onUpdateComplete, modifier = Modifier.padding(top = 16.dp)) {
                        Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_go_back))
                    }
                }
            }
        }

        if (photoUri == null && hasCameraHardware && hasCameraPermission) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_gallery))
                }
                Button(
                    onClick = {
                        val offlinePhotosDir = File(context.cacheDir, "offline_photos").apply { mkdirs() }
                        val file = File(offlinePhotosDir, "${System.currentTimeMillis()}.jpg")
                        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                        imageCapture.takePicture(
                            outputOptions,
                            mainExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    photoUri = file.absolutePath
                                }
                                override fun onError(exc: ImageCaptureException) {
                                    if (file.exists()) file.delete()
                                    if (com.company.cavitrack.BuildConfig.DEBUG) {
                                        android.util.Log.w("PhotoUpdateScreen", "Image capture error", exc)
                                    }
                                    coroutineScope.launch { snackbarHostState.showSnackbar(errorCameraCaptureFormat.format(exc.message ?: "")) }
                                }
                            }
                        )
                    }
                ) {
                    Text(androidx.compose.ui.res.stringResource(com.company.cavitrack.R.string.btn_capture))
                }
            }
        }
    }
}
}





