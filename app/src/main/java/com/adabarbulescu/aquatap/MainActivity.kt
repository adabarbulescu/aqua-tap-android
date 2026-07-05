package com.adabarbulescu.aquatap

import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.adabarbulescu.aquatap.state.HydrationViewModel
import com.adabarbulescu.aquatap.ui.AquaTapScreen
import com.adabarbulescu.aquatap.ui.theme.AquaTapTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val hydrationViewModel by viewModels<HydrationViewModel> { HydrationViewModel.Factory }
    private var nfcAdapter: NfcAdapter? = null
    
    private var lastScanTime: Long = 0
    private val scanCooldownMillis = 1500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        enableEdgeToEdge()

        setContent {
            val state by hydrationViewModel.state.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                hydrationViewModel.events.collect { event ->
                    when (event) {
                        HydrationViewModel.UiEvent.IntakeRecorded -> {
                            triggerHapticFeedback()
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar("250 ml added")
                            }
                        }
                        HydrationViewModel.UiEvent.BottlePaired -> {
                            triggerHapticFeedback()
                            scope.launch {
                                snackbarHostState.showSnackbar("Bottle paired successfully!")
                            }
                        }
                        HydrationViewModel.UiEvent.WrongBottle -> {
                            triggerHapticFeedback() // Maybe a different vibration for error?
                            scope.launch {
                                snackbarHostState.showSnackbar("This is not your paired bottle.")
                            }
                        }
                        HydrationViewModel.UiEvent.NoBottlePaired -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please pair your bottle first.")
                            }
                        }
                    }
                }
            }

            AquaTapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AquaTapScreen(
                        state = state,
                        snackbarHostState = snackbarHostState,
                        onSimulateScan = { 
                            // For simulation, we'll just record intake directly if paired,
                            // or simulate a scan with a fake ID.
                            if (state.pairedTagId == null && !state.isPairingEnabled) {
                                scope.launch { snackbarHostState.showSnackbar("Pair a bottle first (use simulation ID)") }
                            } else {
                                hydrationViewModel.handleNfcScan(state.pairedTagId ?: "SIM_TAG_123")
                            }
                        },
                        onReset = { hydrationViewModel.resetDailyProgress() },
                        onTogglePairing = { hydrationViewModel.togglePairingMode(it) },
                        onUnpair = { hydrationViewModel.unpairBottle() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableNfcReaderMode()
    }

    override fun onPause() {
        super.onPause()
        disableNfcReaderMode()
    }

    private fun enableNfcReaderMode() {
        nfcAdapter?.enableReaderMode(
            this,
            { tag ->
                val tagId = tag.id.joinToString("") { "%02x".format(it) }
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastScanTime >= scanCooldownMillis) {
                    lastScanTime = currentTime
                    runOnUiThread {
                        hydrationViewModel.handleNfcScan(tagId)
                    }
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    private fun disableNfcReaderMode() {
        nfcAdapter?.disableReaderMode(this)
    }

    private fun triggerHapticFeedback() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}
