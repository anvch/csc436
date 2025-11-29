package com.anvch.soundgame.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anvch.soundgame.R
import com.anvch.soundgame.viewmodel.GameViewModel


@SuppressLint("MissingPermission")
@Composable
fun GameScreen(
    onGameOver: (score: Int) -> Unit,
    vm: GameViewModel = viewModel()
) {
    val playerY by vm.playerY.collectAsState()
    val obstacles by vm.obstacles.collectAsState()
    val score by vm.score.collectAsState()
    val running by vm.gameRunning.collectAsState()
    val countdown by vm.countdown.collectAsState()
    val micDb by vm.micDb.collectAsState()
    val bird = ImageBitmap.imageResource(id = R.drawable.bird)


    LaunchedEffect(true) { vm.startGame() }

    val started by vm.hasStarted.collectAsState()

    if (started && countdown == 0 && !running) {
        onGameOver(score)
    }


    Box(Modifier.fillMaxSize()) {

        Canvas(
            Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    vm.setScreenSize(size.width.toFloat(), size.height.toFloat())
                }
        ) {
            // Player
//            drawCircle(
//                color = Color.Blue,
//                radius = size.minDimension * 0.05f,
//                center = Offset(size.width * 0.15f, playerY)
//            )
            drawImage(
                image = bird,
                dstSize = IntSize(
                    (size.minDimension * 0.1f).toInt(),   // width
                    (size.minDimension * 0.1f).toInt()    // height
                ),
                dstOffset = IntOffset(
                    (size.width * 0.15f).toInt(),
                    (playerY - (size.minDimension * 0.05f)).toInt()
                )
            )


            // Obstacles
            obstacles.forEach { o ->
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(o.x, o.y),
                    size = Size(o.width, o.height)
                )
            }

            // Ground
            drawRect(
                color = Color.DarkGray,
                topLeft = Offset(0f, size.height * 0.82f),
                size = Size(size.width, 8f)
            )
        }

        if (countdown > 0) {
            Text(
                text = countdown.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Red,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Text(
            text = stringResource(R.string.score, score),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        )

        // DEBUG
        Text(
            text = stringResource(R.string.mic_db, "%.1f".format(micDb)),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Blue,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        )

    }
}
