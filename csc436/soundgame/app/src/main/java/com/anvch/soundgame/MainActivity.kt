package com.anvch.soundgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.anvch.soundgame.navigation.AppNav
import com.anvch.soundgame.ui.theme.SoundGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SoundGameTheme {
                val navController = rememberNavController()
                AppNav(navController)
            }
        }
    }
}
