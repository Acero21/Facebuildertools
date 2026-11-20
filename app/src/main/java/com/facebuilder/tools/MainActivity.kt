package com.facebuilder.tools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FaceBuilderApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceBuilderApp() {
    Scaffold(topBar = { TopAppBar(title = { Text("Face Builder Tools") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Offline Face Studio")
            Button(onClick = {}) { Text("Face Builder") }
            Button(onClick = {}) { Text("Face Tracker") }
            Button(onClick = {}) { Text("Face Geo Tracker") }
            Button(onClick = {}) { Text("Diffuse / Albedo") }
        }
    }
}
