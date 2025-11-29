package com.anvch.soundgame.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anvch.soundgame.ui.screen.GameScreen
import com.anvch.soundgame.ui.screen.GameOverScreen
import com.anvch.soundgame.ui.screen.StartScreen

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController, startDestination = "start") {

        composable("start") {
            StartScreen(
                onStart = { navController.navigate("game") }
            )
        }

        composable("game") {
            GameScreen(
                onGameOver = { score ->
                    navController.navigate("gameover/$score")
                }
            )
        }

        composable("gameover/{score}") { backStackEntry ->
            val score = backStackEntry.arguments?.getString("score")?.toInt() ?: 0
            GameOverScreen(
                score = score,
                onRestart = {
                    navController.popBackStack("game", inclusive = true)
                    navController.navigate("game")
                }
            )
        }
    }
}
