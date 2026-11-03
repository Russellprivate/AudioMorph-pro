package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryTeal

@Composable
fun WaveformView(
    waveform: List<Float>,
    totalDurationMs: Long,
    startMs: Long,
    endMs: Long,
    currentPlayheadMs: Long,
    onRangeChange: (Long, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val duration = totalDurationMs.coerceAtLeast(1000L)
    val displayWaveform = if (waveform.isNotEmpty()) waveform else List(60) { 0.3f }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val unselectedColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val selectionBg = primaryColor.copy(alpha = 0.15f)

    var draggingHandle by remember { mutableStateOf<String?>(null) } // "start", "end", or null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalDurationMs, startMs, endMs) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val tappedMs = (ratio * duration).toLong()

                        // Determine whether tap is closer to start or end
                        val distToStart = kotlin.math.abs(tappedMs - startMs)
                        val distToEnd = kotlin.math.abs(tappedMs - endMs)

                        if (distToStart < distToEnd) {
                            val newStart = tappedMs.coerceAtMost(endMs - 500L)
                            onRangeChange(newStart, endMs)
                        } else {
                            val newEnd = tappedMs.coerceAtLeast(startMs + 500L)
                            onRangeChange(startMs, newEnd)
                        }
                    }
                }
                .pointerInput(totalDurationMs, startMs, endMs) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val startX = (startMs.toFloat() / duration) * size.width
                            val endX = (endMs.toFloat() / duration) * size.width
                            val touchRadius = 40.dp.toPx()

                            draggingHandle = when {
                                kotlin.math.abs(offset.x - startX) <= touchRadius -> "start"
                                kotlin.math.abs(offset.x - endX) <= touchRadius -> "end"
                                offset.x in startX..endX -> "middle"
                                else -> null
                            }
                        },
                        onDragEnd = { draggingHandle = null },
                        onDragCancel = { draggingHandle = null },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val deltaMs = ((dragAmount.x / size.width) * duration).toLong()

                            when (draggingHandle) {
                                "start" -> {
                                    val newStart = (startMs + deltaMs).coerceIn(0L, endMs - 500L)
                                    onRangeChange(newStart, endMs)
                                }
                                "end" -> {
                                    val newEnd = (endMs + deltaMs).coerceIn(startMs + 500L, duration)
                                    onRangeChange(startMs, newEnd)
                                }
                                "middle" -> {
                                    val rangeLen = endMs - startMs
                                    val newStart = (startMs + deltaMs).coerceIn(0L, duration - rangeLen)
                                    val newEnd = newStart + rangeLen
                                    onRangeChange(newStart, newEnd)
                                }
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            val startRatio = (startMs.toFloat() / duration).coerceIn(0f, 1f)
            val endRatio = (endMs.toFloat() / duration).coerceIn(0f, 1f)
            val startX = startRatio * width
            val endX = endRatio * width

            // 1. Draw Selection Range Background
            drawRoundRect(
                color = selectionBg,
                topLeft = Offset(startX, 0f),
                size = Size(endX - startX, height),
                cornerRadius = CornerRadius(8.dp.toPx())
            )

            // 2. Draw Waveform Bars
            val barCount = displayWaveform.size
            val barSpacing = width / barCount.toFloat()
            val barWidth = (barSpacing * 0.65f).coerceAtLeast(2.dp.toPx())

            for (i in 0 until barCount) {
                val amp = displayWaveform[i].coerceIn(0.08f, 1f)
                val barHeight = (amp * (height - 30.dp.toPx())).coerceAtLeast(4.dp.toPx())
                val barX = i * barSpacing + (barSpacing - barWidth) / 2f
                val barY = (height - barHeight) / 2f

                val isInsideSelection = barX in (startX - barWidth)..(endX)
                val color = if (isInsideSelection) {
                    Brush.verticalGradient(
                        colors = listOf(SecondaryTeal, PrimaryViolet),
                        startY = barY,
                        endY = barY + barHeight
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(unselectedColor, unselectedColor),
                        startY = barY,
                        endY = barY + barHeight
                    )
                }

                drawRoundRect(
                    brush = color,
                    topLeft = Offset(barX, barY),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f)
                )
            }

            // 3. Draw Start & End Boundary Handles
            val handleWidth = 4.dp.toPx()
            val handleTopRadius = 8.dp.toPx()

            // Start Line & Knob
            drawRoundRect(
                color = secondaryColor,
                topLeft = Offset(startX - handleWidth / 2f, 0f),
                size = Size(handleWidth, height),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawCircle(
                color = secondaryColor,
                radius = handleTopRadius,
                center = Offset(startX, 12.dp.toPx())
            )

            // End Line & Knob
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(endX - handleWidth / 2f, 0f),
                size = Size(handleWidth, height),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
            drawCircle(
                color = primaryColor,
                radius = handleTopRadius,
                center = Offset(endX, height - 12.dp.toPx())
            )

            // 4. Draw Playhead Indicator (if within duration)
            if (currentPlayheadMs in 0..duration) {
                val playheadX = (currentPlayheadMs.toFloat() / duration) * width
                drawLine(
                    color = Color.White,
                    start = Offset(playheadX, 0f),
                    end = Offset(playheadX, height),
                    strokeWidth = 2.dp.toPx()
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(playheadX, height / 2f)
                )
            }
        }
    }
}
