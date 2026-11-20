package com.facebuilder.tools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp

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
        Scaffold(topBar = {
            TopAppBar(title = { Text("Face Builder Tools") })
        }) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Text(
                    "Offline workspace • no photos leave your device",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelMedium
                )
                TabRow(selectedTabIndex = selectedTool) {
                    tools.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTool == index,
                            onClick = { selectedTool = index },
                            text = { Text(title, maxLines = 1) }
                        )
                    }
                }
                when (selectedTool) {
                    0 -> BuilderScreen(projectName, { projectName = it })
                    1 -> TrackerScreen()
                    2 -> GeoTrackerScreen()
                    else -> AlbedoScreen()
                }
            }
        }
    }
}

@Composable
private fun BuilderScreen(name: String, onNameChange: (String) -> Unit) {
    var headScale by remember { mutableFloatStateOf(0.5f) }
    var jawWidth by remember { mutableFloatStateOf(0.5f) }
    var eyeSpacing by remember { mutableFloatStateOf(0.5f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Build a 3D head", style = MaterialTheme.typography.headlineSmall) }
        item { Text("Start with a front and side photo. The local fitting engine will use these landmarks to create an editable head.") }
        item { PreviewCard() }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { }) { Text("Add front photo") }
                OutlinedButton(onClick = { }) { Text("Add side photo") }
            }
        }
        item { SliderControl("Head scale", headScale) { headScale = it } }
        item { SliderControl("Jaw width", jawWidth) { jawWidth = it } }
        item { SliderControl("Eye spacing", eyeSpacing) { eyeSpacing = it } }
        item { Text("Project: $name", style = MaterialTheme.typography.titleMedium) }
        item { Button(onClick = { }) { Text("Export GLB / OBJ") } }
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
    WorkspaceScreen("Face tracker", "Track 468 facial landmarks from camera or imported video.", "Open camera", "Import video")
}

@Composable
private fun GeoTrackerScreen() {
    WorkspaceScreen("Face geo tracker", "Fit the editable head mesh to tracked landmarks frame by frame.", "Load head", "Track sequence")
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
            Button(onClick = { }) { Text(primary) }
            OutlinedButton(onClick = { }) { Text(secondary) }
        }
        Text("This workspace is offline-first. Processing and project files remain on the device.", style = MaterialTheme.typography.bodySmall)
    }
}
