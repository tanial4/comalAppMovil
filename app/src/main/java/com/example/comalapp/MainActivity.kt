package com.example.comalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.comalapp.ui.navigation.AppNavGraph
import com.example.comalapp.ui.theme.ComalAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComalAppTheme {
                AppNavGraph()
            }
        }
    }
}