package com.company.cavitrack.presentation.addupdate.photo

import android.content.pm.PackageManager
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.company.cavitrack.BuildConfig
import com.company.cavitrack.R
import com.company.cavitrack.domain.model.EntityType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
    val isDark = isSystemInDarkTheme()

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get()?.unbindAll()
                } else {
                    // Future hasn't resolved yet — register a listener to unbind when it does
                    cameraProviderFuture.addListener({
                        try { cameraProviderFuture.get()?.unbindAll() } catch (_: Exception) {}
                    }, mainExecutor)
                }
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.w("PhotoUpdateScreen", "Error unbinding camera", e)
                }
            }
        }
    }

    val imageCapture = remember {
        val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
            .setResolutionStrategy(
                androidx.camera.core.resolutionselector.ResolutionStrategy(
                    Size(1280, 720),
                    androidx.camera.core.resolutionselector.ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                )
            )
            .build()
        ImageCapture.Builder()
            .setResolutionSelector(resolutionSelector)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    var photoFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    val error by viewModel.error.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val cannotAttachPhotoMsg = stringResource(R.string.msg_cannot_attach_photo_unsaved)
    val errorImageLoadMsg = stringResource(R.string.error_image_load)
    val errorCameraBindMsg = stringResource(R.string.error_camera_bind)
    val errorCameraCaptureFormat = stringResource(R.string.error_camera_capture)

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
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
                            photoFilePath = file.absolutePath
                        }
                    }
                } catch (e: Exception) {
                    tempFile?.let { if (it.exists()) it.delete() }
                    if (BuildConfig.DEBUG) {
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
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    val hasCameraHardware = remember { context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) }
    val isUploading by viewModel.isUploading.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(42.dp)
                ) {
                    IconButton(onClick = onUpdateComplete) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = stringResource(R.string.label_photo_update),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${entityType.name} photo record",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Content Area
            if (!hasCameraHardware) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NoPhotography,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.msg_no_camera),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(stringResource(R.string.btn_choose_gallery))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onUpdateComplete,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(stringResource(R.string.btn_go_back))
                            }
                        }
                    }
                }
            } else if (hasCameraPermission) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    // Camera viewfinder inside rounded container
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (photoFilePath == null) {
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
                                            if (BuildConfig.DEBUG) {
                                                android.util.Log.w("PhotoUpdateScreen", "Failed to bind camera", e)
                                            }
                                            coroutineScope.launch { snackbarHostState.showSnackbar(errorCameraBindMsg) }
                                        }
                                    }, mainExecutor)
                                    previewView
                                },
                                onRelease = {
                                    try {
                                        if (cameraProviderFuture.isDone) {
                                            cameraProviderFuture.get()?.unbindAll()
                                        }
                                    } catch (_: Exception) {}
                                }
                            )
                        }
                    }

                    // Photo preview overlay if captured
                    if (photoFilePath != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                                    modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                                ) {
                                    AsyncImage(
                                        model = photoFilePath,
                                        contentDescription = "Captured Photo",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(340.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                if (isUploading) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(R.string.msg_saving),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Button(
                                        onClick = {
                                            val currentUri = photoFilePath
                                            if (entityId != null && currentUri != null) {
                                                viewModel.uploadPhotoAndUpdateEntity(entityType, entityId, File(currentUri))
                                            } else {
                                                coroutineScope.launch { snackbarHostState.showSnackbar(cannotAttachPhotoMsg) }
                                            }
                                        },
                                        shape = RoundedCornerShape(24.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier.fillMaxWidth().height(52.dp)
                                    ) {
                                        Icon(Icons.Outlined.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.action_upload_save),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedButton(
                                        onClick = {
                                            val currentUri = photoFilePath
                                            photoFilePath = null
                                            if (currentUri != null) {
                                                val file = File(currentUri)
                                                if (file.exists()) file.delete()
                                            }
                                        },
                                        shape = RoundedCornerShape(24.dp),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.action_retake))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.msg_camera_permission_required),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(stringResource(R.string.btn_grant_permission))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text(stringResource(R.string.btn_choose_gallery))
                            }
                        }
                    }
                }
            }

            // Floating Bottom Control Bar (when in live camera view)
            if (photoFilePath == null && hasCameraHardware && hasCameraPermission) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(52.dp)
                            .clickable { galleryLauncher.launch("image/*") }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoLibrary,
                                contentDescription = stringResource(R.string.btn_gallery),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Shutter Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        border = BorderStroke(4.dp, if (isDark) Color(0xFF334155) else Color(0xFFDBEAFE)),
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                val offlinePhotosDir = File(context.cacheDir, "offline_photos").apply { mkdirs() }
                                val file = File(offlinePhotosDir, "${System.currentTimeMillis()}.jpg")
                                val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

                                imageCapture.takePicture(
                                    outputOptions,
                                    mainExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                            photoFilePath = file.absolutePath
                                        }

                                        override fun onError(exc: ImageCaptureException) {
                                            if (file.exists()) file.delete()
                                            if (BuildConfig.DEBUG) {
                                                android.util.Log.w("PhotoUpdateScreen", "Image capture error", exc)
                                            }
                                            coroutineScope.launch {
                                                val msg = try {
                                                    errorCameraCaptureFormat.format(exc.message.orEmpty())
                                                } catch (_: Exception) {
                                                    exc.message.orEmpty().ifBlank { errorCameraBindMsg }
                                                }
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        }
                                    }
                                )
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Camera,
                                contentDescription = stringResource(R.string.btn_capture),
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Close/Back Button
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(onClick = onUpdateComplete)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.btn_cancel),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}






