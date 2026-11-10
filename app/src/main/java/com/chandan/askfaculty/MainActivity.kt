package com.chandan.askfaculty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.chandan.askfaculty.ui.health.HealthScreen
import com.chandan.askfaculty.ui.theme.AskFacultyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AskFacultyTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HealthScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
