package com.adabarbulescu.aquatap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adabarbulescu.aquatap.domain.HydrationCalculator
import com.adabarbulescu.aquatap.domain.HydrationState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AquaTapScreen(
    state: HydrationState,
    snackbarHostState: SnackbarHostState,
    onSimulateScan: () -> Unit,
    onReset: () -> Unit,
    onTogglePairing: (Boolean) -> Unit,
    onUnpair: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = HydrationCalculator.progress(
        totalIntakeMl = state.totalIntakeMl,
        dailyGoalMl = state.dailyGoalMl
    )

    val remainingMl = HydrationCalculator.remaining(
        totalIntakeMl = state.totalIntakeMl,
        dailyGoalMl = state.dailyGoalMl
    )

    val percentage = (progress * 100).toInt()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeaderSection(
                pairedTagId = state.pairedTagId,
                isPairingEnabled = state.isPairingEnabled,
                onTogglePairing = onTogglePairing,
                onUnpair = onUnpair
            )

            WaterBottleView(
                progress = progress,
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Daily progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${state.totalIntakeMl} / ${state.dailyGoalMl} ml",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            IntakeStats(
                totalIntakeMl = state.totalIntakeMl,
                remainingMl = remainingMl,
                percentage = percentage,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSimulateScan,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Simulate NFC scan")
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Reset")
                }
            }

            RecentIntakeSection(state.history, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun HeaderSection(
    pairedTagId: String?,
    isPairingEnabled: Boolean,
    onTogglePairing: (Boolean) -> Unit,
    onUnpair: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AquaTap",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "NFC-powered hydration tracking",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            if (pairedTagId != null) {
                TextButton(onClick = onUnpair) {
                    Text("Unpair")
                }
            }
        }

        Box(modifier = Modifier.padding(top = 8.dp)) {
            if (pairedTagId == null) {
                if (isPairingEnabled) {
                    Text(
                        text = "Scanning for bottle... Tap your tag now.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Button(
                        onClick = { onTogglePairing(true) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Pair your bottle")
                    }
                }
            } else {
                val displayedId = if (pairedTagId.length > 6) {
                    "****${pairedTagId.takeLast(4)}"
                } else {
                    pairedTagId
                }
                Text(
                    text = "Paired with bottle: $displayedId",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun RecentIntakeSection(
    history: List<com.adabarbulescu.aquatap.domain.IntakeEvent>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Recent intake",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (history.isEmpty()) {
            Text(
                text = "No scans recorded yet.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            history.forEach { event ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "+${event.amountMl} ml",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = formatTimestamp(event.timestampMillis),
                        style = MaterialTheme.typography.bodySmall
                    )

                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatTimestamp(timestampMillis: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(timestampMillis))
}
