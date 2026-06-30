package com.adabarbulescu.aquatap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun WaterBottleView(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "WaterFillProgress",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "WaveAnimation")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "WaveOffset",
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primaryContainer
    val bottleOutlineColor = MaterialTheme.colorScheme.outline
    val bottleBackgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val strokeWidth = 4.dp.toPx()
            val cornerRadius = 24.dp.toPx()
            
            // Bottle dimensions
            val neckWidth = width * 0.35f
            val neckHeight = height * 0.12f
            
            val bottlePath = Path().apply {
                // Top neck
                moveTo((width - neckWidth) / 2f, strokeWidth / 2f)
                lineTo((width + neckWidth) / 2f, strokeWidth / 2f)
                lineTo((width + neckWidth) / 2f, neckHeight)
                
                // Shoulder and body
                lineTo(width - (strokeWidth / 2f), neckHeight + cornerRadius)
                lineTo(width - (strokeWidth / 2f), height - cornerRadius)
                
                // Bottom
                quadraticTo(
                    width - (strokeWidth / 2f), height - (strokeWidth / 2f),
                    width - cornerRadius, height - (strokeWidth / 2f)
                )
                lineTo(cornerRadius, height - (strokeWidth / 2f))
                quadraticTo(
                    strokeWidth / 2f, height - (strokeWidth / 2f),
                    strokeWidth / 2f, height - cornerRadius
                )
                
                // Back up
                lineTo(strokeWidth / 2f, neckHeight + cornerRadius)
                lineTo((width - neckWidth) / 2f, neckHeight)
                close()
            }

            // Draw bottle background
            drawPath(
                path = bottlePath,
                color = bottleBackgroundColor
            )

            // Draw the water fill
            clipPath(bottlePath) {
                val fillHeight = (height - strokeWidth) * animatedProgress
                val waveHeight = 4.dp.toPx()
                
                val waterPath = Path().apply {
                    val startY = height - fillHeight
                    moveTo(0f, startY)
                    
                    if (animatedProgress > 0f && animatedProgress < 1f) {
                        // Draw an animated wave
                        for (x in 0..width.toInt() step 5) {
                            val relativeX = x.toFloat() / width
                            val y = startY + sin(relativeX * 2 * Math.PI.toFloat() + waveOffset) * waveHeight
                            lineTo(x.toFloat(), y)
                        }
                        lineTo(width, startY)
                    } else {
                        lineTo(width, startY)
                    }
                    
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = waterPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(secondaryColor, primaryColor),
                        startY = height - fillHeight - waveHeight,
                        endY = height
                    )
                )
                
                // Subtle reflection on the water
                if (animatedProgress > 0.1f) {
                   drawPath(
                       path = waterPath,
                       color = Color.White.copy(alpha = 0.1f),
                       style = Stroke(width = 2.dp.toPx())
                   )
                }
            }

            // Add a subtle glass shine/highlight
            val shinePath = Path().apply {
                moveTo(width * 0.2f, neckHeight + cornerRadius * 2)
                lineTo(width * 0.2f, height - cornerRadius * 3)
            }
            drawPath(
                path = shinePath,
                color = Color.White.copy(alpha = 0.2f),
                style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // Draw bottle outline
            drawPath(
                path = bottlePath,
                color = bottleOutlineColor,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}
