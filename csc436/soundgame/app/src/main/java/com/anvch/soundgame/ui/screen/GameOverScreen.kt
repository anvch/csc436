package com.anvch.soundgame.ui.screen

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.anvch.soundgame.R

@Composable
fun GameOverScreen(score: Int, onRestart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.game_over), style = MaterialTheme.typography.headlineLarge)
            Text("Score: $score", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(24.dp))

            Button(onClick = onRestart) {
                Text(stringResource(R.string.restart))
            }
        }
    }
}
