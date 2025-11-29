package com.anvch.soundgame.ui.start

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StartScreen(onStart: () -> Unit) {

    var hasPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) onStart()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sound Runner Game", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            ) {
                Text("Start Game")
            }
        }
    }
}
