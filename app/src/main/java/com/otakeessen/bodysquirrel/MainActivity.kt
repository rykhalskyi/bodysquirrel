package com.otakeessen.bodysquirrel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.otakeessen.bodysquirrel.ui.MainScreen
import com.otakeessen.bodysquirrel.ui.theme.BodySquirrelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BodySquirrelTheme {
                MainScreen()
            }
        }
    }
}
