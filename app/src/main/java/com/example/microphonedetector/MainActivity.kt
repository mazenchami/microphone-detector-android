package com.example.microphonedetector

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.microphonedetector.ui.theme.MicrophoneDetectorTheme

class MainActivity : ComponentActivity() {
    private lateinit var audioRecorder: StereoAudioProcessor

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted
            audioRecorder.startRecording()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioRecorder = StereoAudioProcessor()

        setContent {
            MicrophoneDetectorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AudioControlUI()
                }
            }
        }
    }

    @Composable
    fun AudioControlUI() {
        var isRecording by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
            Button(
                onClick = {
                    if (!isRecording) {
                        // Check if the permission is already granted
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            // Permission is granted, start recording
                            audioRecorder.startRecording()
                        } else {
                            // Request permission
                            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    } else {
                        // Stop recording
                        audioRecorder.stopRecording()
                    }
                    isRecording = !isRecording
                },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }

            Text(
                text = if (isRecording) "Recording..." else "Tap 'Start Recording' to begin",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
