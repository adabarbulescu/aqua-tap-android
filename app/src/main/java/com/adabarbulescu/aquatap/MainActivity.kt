package com.adabarbulescu.aquatap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.adabarbulescu.aquatap.state.HydrationViewModel
import com.adabarbulescu.aquatap.ui.AquaTapScreen
import com.adabarbulescu.aquatap.ui.theme.AquaTapTheme

class MainActivity : ComponentActivity() {

    private val hydrationViewModel by viewModels<HydrationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val state by hydrationViewModel.state.collectAsState()

            AquaTapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AquaTapScreen(
                        state = state,
                        onSimulateScan = { hydrationViewModel.recordIntake() },
                        onReset = { hydrationViewModel.resetDailyProgress() }
                    )
                }
            }
        }
    }
}
