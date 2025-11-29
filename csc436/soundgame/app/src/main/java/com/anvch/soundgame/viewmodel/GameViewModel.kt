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

    // New: expose dB from microphone
    val micDb = MutableStateFlow(0f)

    private val audio = AudioRecorderHelper()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private var velocityY = 0f


    fun setScreenSize(width: Float, height: Float) {
        screenWidth = width
        screenHeight = height
    }

    private val baseSize get() = minOf(screenWidth, screenHeight)
    private val groundY get() = screenHeight * 0.82f
    private val playerRadius get() = baseSize * 0.05f
    private val maxJumpHeight get() = baseSize * 0.15f
    private val obstacleSpeed get() = baseSize * 0.30f
    private val playerX get() = screenWidth * 0.15f
    private val obstacleGapRatio = 0.35f
    private val obstacleWidth get() = baseSize * 0.12f
    private val gravity = 500f


    private var nextObstacleId = 0
    private val passedObstacleIds = mutableSetOf<Int>()

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startGame() {
        hasStarted.value = true
        gameRunning.value = true
        score.value = 0
        obstacles.value = emptyList()
        passedObstacleIds.clear()
        nextObstacleId = 0
        countdown.value = 3
        playerY.value = groundY - playerRadius

        // Start microphone input, update micDb each time
        audio.start { db ->
            micDb.value = db
            println("Mic dB = $db")
        }

        // Countdown
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

        // Jump logic: trigger on sound above threshold, regardless of height
        val jumpThresholdDb = -40f
        if (micDb.value > jumpThresholdDb) {
            velocityY = -maxJumpHeight * 2f  // upward impulse on jump
            println("Jump! playerY=${playerY.value}, micDb=${micDb.value}, velocityY=$velocityY")
        }

        // Gravity always applies
        velocityY += gravity * dt
        playerY.value += velocityY * dt
        println("No jump! playerY=${playerY.value}, micDb=${micDb.value}, velocityY=$velocityY")

        // Clamp to ground (don't fall below)
        if (playerY.value > groundY - playerRadius) {
            playerY.value = groundY - playerRadius
            velocityY = 0f
        }

        // Optional: clamp to top of screen
        if (playerY.value < playerRadius) {
            playerY.value = playerRadius
            velocityY = 0f
        }

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
            if (hit) {
                println("Collision detected! Game Over at playerY=${playerY.value}")
                triggerGameOver()
            }
        }

        // Increment score
        obstacles.value.chunked(2).forEach { pair ->
            val pairId = pair[0].id
            if (pair[0].x + obstacleWidth < playerX && pairId !in passedObstacleIds) {
                score.value++
                passedObstacleIds.add(pairId)
                println("Score incremented: ${score.value}")
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

        val bottomHeight = groundY - (gapTop + gapHeight)
        val bottomObstacle = Obstacle(
            id = nextObstacleId,
            x = screenWidth + obstacleWidth,
            y = gapTop + gapHeight,
            width = obstacleWidth,
            height = bottomHeight
        )
        val topObstacle = Obstacle(
            id = nextObstacleId,
            x = screenWidth + obstacleWidth,
            y = 0f,
            width = obstacleWidth,
            height = gapTop
        )

        nextObstacleId++
        obstacles.value = obstacles.value + listOf(topObstacle, bottomObstacle)
        println("Spawned obstacle id=${nextObstacleId - 1}, topHeight=$gapTop, bottomHeight=$bottomHeight")
    }

    private fun triggerGameOver() {
        gameRunning.value = false
        audio.stop()
        println("Game stopped.")
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun resetGame() = startGame()

    override fun onCleared() {
        audio.stop()
        super.onCleared()
    }
}
