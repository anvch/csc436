package com.anvch.soundgame.viewmodel

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anvch.soundgame.audio.AudioRecorderHelper
import com.anvch.soundgame.model.Obstacle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel : ViewModel() {

    val countdown = MutableStateFlow(3)
    val playerY = MutableStateFlow(0f)
    val obstacles = MutableStateFlow<List<Obstacle>>(emptyList())
    val score = MutableStateFlow(0)
    val gameRunning = MutableStateFlow(false)
    val hasStarted = MutableStateFlow(false)

    private val audio = AudioRecorderHelper()

    // Screen dimensions
    private var screenWidth = 0f
    private var screenHeight = 0f

    fun setScreenSize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
    }

    // Base scale for sizes
    private val baseSize get() = minOf(screenWidth, screenHeight)

    // Game metrics
    private val groundY get() = screenHeight * 0.82f
    private val playerRadius get() = baseSize * 0.05f
    private val maxJumpHeight get() = baseSize * 0.45f
    private val obstacleSpeed get() = baseSize * 0.30f
    private val playerX get() = screenWidth * 0.15f
    private val obstacleGapRatio = 0.35f
    private val obstacleWidth get() = baseSize * 0.12f

    // Physics
    private var velocityY = 0f
    private val gravity = -2000f
    private val impulseFactor = 2200f
    private var smoothedDb = -80f

    // Obstacle ID tracking for score
    private var nextObstacleId = 0
    private val passedObstacleIds = mutableSetOf<Int>()

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startGame() {
        hasStarted.value = true
        gameRunning.value = true
        score.value = 0
        velocityY = 0f
        smoothedDb = -80f
        obstacles.value = emptyList()
        passedObstacleIds.clear()
        nextObstacleId = 0
        countdown.value = 3

        // Player starts just above ground
        playerY.value = groundY - playerRadius

        // Start microphone input
        audio.start { db ->
            smoothedDb = 0.15f * db + 0.85f * smoothedDb
        }

        // Countdown coroutine
        viewModelScope.launch {
            for (i in 3 downTo 1) {
                countdown.value = i
                delay(1000)
            }
            countdown.value = 0
        }

        // Game loop
        viewModelScope.launch {
            var last = System.currentTimeMillis()
            while (gameRunning.value) {
                val now = System.currentTimeMillis()
                val dt = (now - last) / 1000f
                last = now

                update(dt)
                delay(16)
            }
        }
    }

    private fun update(dt: Float) {
        if (countdown.value > 0) return

        // Jump physics
        val loudEnough = smoothedDb > -15f
        if (loudEnough) {
            val force = ((smoothedDb + 15f) / 50f).coerceIn(0f, 1f)
            velocityY += force * impulseFactor * dt
        }

        velocityY += gravity * dt

        val newY = playerY.value + velocityY * dt
        playerY.value = newY.coerceIn(groundY - maxJumpHeight, groundY - playerRadius)

        // Move obstacles
        obstacles.value = obstacles.value
            .map { it.copy(x = it.x - obstacleSpeed * dt) }
            .filter { it.x + it.width > 0 }

        // Spawn obstacles
        if (obstacles.value.isEmpty() || obstacles.value.last().x < screenWidth * 0.6f) {
            spawnObstacle()
        }

        // Collision detection
        val r = playerRadius
        for (o in obstacles.value) {
            val hit = playerX + r > o.x &&
                    playerX - r < o.x + o.width &&
                    playerY.value + r > o.y &&
                    playerY.value - r < o.y + o.height

            if (hit) triggerGameOver()
        }

        // Increment score by obstacle pair passed
        obstacles.value.chunked(2).forEach { pair ->
            val pairId = pair[0].id
            if (pair[0].x + obstacleWidth < playerX && pairId !in passedObstacleIds) {
                score.value++
                passedObstacleIds.add(pairId)
            }
        }
    }

    private fun spawnObstacle() {
        if (screenHeight == 0f) return

        val gapHeight = screenHeight * obstacleGapRatio
        val padding = screenHeight * 0.1f
        val gapTopMin = padding
        val gapTopMax = groundY - gapHeight - padding
        val gapTop = Random.nextFloat() * (gapTopMax - gapTopMin) + gapTopMin

        // Bottom obstacle
        val bottomHeight = groundY - (gapTop + gapHeight)
        val bottomObstacle = Obstacle(
            id = nextObstacleId,
            x = screenWidth + obstacleWidth,
            y = gapTop + gapHeight,
            width = obstacleWidth,
            height = bottomHeight
        )

        // Top obstacle
        val topObstacle = Obstacle(
            id = nextObstacleId,
            x = screenWidth + obstacleWidth,
            y = 0f,
            width = obstacleWidth,
            height = gapTop
        )

        nextObstacleId++
        obstacles.value = obstacles.value + listOf(topObstacle, bottomObstacle)
    }

    private fun triggerGameOver() {
        gameRunning.value = false
        audio.stop()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun resetGame() = startGame()

    override fun onCleared() {
        audio.stop()
        super.onCleared()
    }
}
