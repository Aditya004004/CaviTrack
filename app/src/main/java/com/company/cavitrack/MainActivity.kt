package com.company.cavitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.company.cavitrack.presentation.theme.CaviTrackTheme
import com.company.cavitrack.presentation.navigation.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_CaviTrack)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            CaviTrackTheme {
                MainScreen()
            }
        }
    }
}
