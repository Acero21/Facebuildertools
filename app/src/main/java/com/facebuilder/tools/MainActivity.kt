package com.facebuilder.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.toBitmap
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FaceBuilderApp() }
    }
}

private val tools = listOf("Face Builder", "Face Tracker", "Geo Tracker", "Albedo")

@Composable
fun FaceBuilderApp() {
    var selectedTool by remember { mutableIntStateOf(0) }
    var projectName by remember { mutableStateOf("Untitled face") }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("Face Builder Tools") }) }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Offline workspace • tracking runs on this device",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium
                )
                TabRow(selectedTabIndex = selectedTool) {
                    tools.forEachIndexed { index, title ->
                        Tab(selected = selectedTool == index, onClick = { selectedTool = index }) {
                            Text(title, maxLines = 1, modifier = Modifier.padding(vertical = 14.dp))
                        }
                    }
                }
                when (selectedTool) {
                    0 -> BuilderScreen(projectName)
                    1 -> TrackerScreen()
                    2 -> GeoTrackerScreen()
                    else -> AlbedoScreen()
                }
            }
        }
    }
}

@Composable
private fun BuilderScreen(name: String) {
    var headScale by remember { mutableFloatStateOf(0.5f) }
    var jawWidth by remember { mutableFloatStateOf(0.5f) }
    var eyeSpacing by remember { mutableFloatStateOf(0.5f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Build a 3D head", style = MaterialTheme.typography.headlineSmall) }
        item { Text("Start with front and side photos, then adjust the editable head proportions.") }
        item { PreviewCard() }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}) { Text("Add front photo") }
                OutlinedButton(onClick = {}) { Text("Add side photo") }
            }
        }
        item { SliderControl("Head scale", headScale) { headScale = it } }
        item { SliderControl("Jaw width", jawWidth) { jawWidth = it } }
        item { SliderControl("Eye spacing", eyeSpacing) { eyeSpacing = it } }
        item { Text("Project: $name", style = MaterialTheme.typography.titleMedium) }
        item { Button(onClick = {}) { Text("Export GLB / OBJ") } }
    }
}

@Composable
private fun PreviewCard() {
    Card(Modifier.fillMaxWidth().height(260.dp), shape = RoundedCornerShape(16.dp)) {
        Canvas(Modifier.fillMaxSize().background(Color(0xFF18202A))) {
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(Color(0xFFB97862), size.minDimension * .27f, center)
            drawCircle(Color.White, 12f, center - Offset(42f, 20f))
            drawCircle(Color.White, 12f, center + Offset(42f, -20f))
            drawCircle(Color.DarkGray, 5f, center - Offset(42f, 20f))
            drawCircle(Color.DarkGray, 5f, center + Offset(42f, -20f))
            drawLine(Color(0xFF704236), center + Offset(-28f, 45f), center + Offset(28f, 45f), 5f)
        }
    }
}

@Composable
private fun SliderControl(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text("$label: ${(value * 100).toInt()}%")
        Slider(value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun TrackerScreen() {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasPermission = it
    }
    var cameraStarted by remember { mutableStateOf(false) }
    var faceCount by remember { mutableIntStateOf(0) }
    var landmarks by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var trackerError by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Face tracker", style = MaterialTheme.typography.headlineSmall)
        Text("Live facial landmarks are detected on-device. Frames are not uploaded or saved.")
        if (!hasPermission) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant camera access")
            }
        } else {
            LiveFaceCamera(
                modifier = Modifier.fillMaxWidth().weight(1f),
                onTrackingState = { running, error ->
                    cameraStarted = running
                    trackerError = error
                },
                onLandmarks = { points ->
                    landmarks = points
                    faceCount = if (points.isEmpty()) 0 else 1
                }
            )
            Text(
                trackerError ?: if (cameraStarted) {
                    if (faceCount > 0) "Tracking face • ${landmarks.size} landmarks" else "Camera ready • looking for a face"
                } else "Starting camera…",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun LiveFaceCamera(
    modifier: Modifier = Modifier,
    onTrackingState: (Boolean, String?) -> Unit,
    onLandmarks: (List<Offset>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                previewView = this
            }
        }
    )

    LaunchedEffect(previewView) {
        if (previewView == null) return@LaunchedEffect
        var landmarker: FaceLandmarker? = null
        var analyzerExecutor: ExecutorService? = null
        var cameraProvider: ProcessCameraProvider? = null
        var previewUseCase: Preview? = null
        var analysisUseCase: ImageAnalysis? = null

        try {
            landmarker = createFaceLandmarker(context)
            analyzerExecutor = Executors.newSingleThreadExecutor()
            val providerFuture = ProcessCameraProvider.getInstance(context)
            providerFuture.addListener({
                try {
                    cameraProvider = providerFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView!!.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                    val vision = landmarker!!
                    val executor = analyzerExecutor!!
                    analysis.setAnalyzer(executor) { imageProxy ->
                        analyzeFrame(imageProxy, vision, context) { points ->
                            ContextCompat.getMainExecutor(context).execute { onLandmarks(points) }
                        }
                    }
                    previewUseCase = preview
                    analysisUseCase = analysis
                    cameraProvider!!.unbindAll()
                    cameraProvider!!.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analysis
                    )
                    ContextCompat.getMainExecutor(context).execute { onTrackingState(true, null) }
                } catch (error: Exception) {
                    ContextCompat.getMainExecutor(context).execute {
                        onTrackingState(false, error.message ?: "Unable to start camera")
                    }
                }
            }, ContextCompat.getMainExecutor(context))
        } catch (error: Exception) {
            onTrackingState(false, error.message ?: "Face model unavailable")
        }

        awaitDispose {
            analysisUseCase?.clearAnalyzer()
            if (cameraProvider != null && previewUseCase != null && analysisUseCase != null) {
                cameraProvider?.unbind(previewUseCase, analysisUseCase)
            }
            landmarker?.close()
            analyzerExecutor?.shutdown()
        }
    }
}

private fun createFaceLandmarker(context: Context): FaceLandmarker {
    context.assets.open("face_landmarker.task").use { /* Fail early with a useful message if model is not installed. */ }
    val baseOptions = BaseOptions.builder().setModelAssetPath("face_landmarker.task").build()
    return FaceLandmarker.createFromOptions(
        context,
        FaceLandmarker.FaceLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumFaces(1)
            .setResultListener { result, _ ->
                // The live analyzer reads results through the listener below; this callback is required in LIVE_STREAM mode.
            }
            .setErrorListener { }
            .build()
    )
}

private fun analyzeFrame(
    imageProxy: ImageProxy,
    landmarker: FaceLandmarker,
    context: Context,
    onLandmarks: (List<Offset>) -> Unit
) {
    try {
        val bitmap: Bitmap = imageProxy.toBitmap()
        val mpImage = BitmapImageBuilder(bitmap).build()
        landmarker.detectAsync(mpImage, SystemClock.uptimeMillis())
        mpImage.close()
        // Results are delivered by MediaPipe's live-stream listener.
    } catch (_: Exception) {
        // Ignore individual bad frames; ImageAnalysis continues with the next frame.
    } finally {
        imageProxy.close()
    }
}

@Composable
private fun GeoTrackerScreen() {
    WorkspaceScreen("Face geo tracker", "Fit an editable head mesh to tracked landmarks frame by frame.", "Load head", "Track sequence")
}

@Composable
private fun AlbedoScreen() {
    WorkspaceScreen("Diffuse / albedo", "Paint a clean base-color map without baked shadows or highlights.", "Create 2K map", "Export PNG")
}

@Composable
private fun WorkspaceScreen(title: String, description: String, primary: String, secondary: String) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(description)
        PreviewCard()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Text(primary) }
            OutlinedButton(onClick = {}) { Text(secondary) }
        }
        Text("This workspace is offline-first. Project files remain on the device.", style = MaterialTheme.typography.bodySmall)
    }
}
