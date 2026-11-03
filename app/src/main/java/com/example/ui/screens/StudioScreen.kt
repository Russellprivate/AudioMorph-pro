package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.StringsLocalization
import com.example.model.AudioFormat
import com.example.ui.components.WaveformView
import com.example.ui.components.formatTime
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studioState by viewModel.studioState.collectAsState()
    val playerState by viewModel.playerManager.state.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()
    val libraryItems by viewModel.libraryItems.collectAsState()
    val language = appSettings.language

    val currentAudio = studioState.selectedAudioItem

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Text(
                    text = StringsLocalization.get("trim_title", language),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    text = if (currentAudio != null) currentAudio.title else StringsLocalization.get("select_audio_to_edit", language),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // If no audio loaded yet, show empty picker state
        if (currentAudio == null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCut,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = StringsLocalization.get("select_audio_to_edit", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = if (libraryItems.isNotEmpty()) "Choose any converted track below to start cutting and editing"
                            else "Convert a video or try a sample audio track to test trimming tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (libraryItems.isNotEmpty()) {
                            Button(
                                onClick = { viewModel.loadAudioForTrimming(libraryItems.first()) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("load_first_audio_button")
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit \"${libraryItems.first().title}\"")
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { viewModel.selectTab(ScreenTab.CONVERT) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(StringsLocalization.get("tab_convert", language))
                                }
                                OutlinedButton(
                                    onClick = {
                                        viewModel.loadDemoVideo()
                                        viewModel.selectTab(ScreenTab.CONVERT)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(StringsLocalization.get("demo_video", language))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Waveform & Range Selection Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsLocalization.get("trim_range", language),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Selected Duration Pill
                            val selectedDur = (studioState.endMs - studioState.startMs).coerceAtLeast(0L)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Selected: ${formatPreciseTime(selectedDur)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Visual Waveform
                        WaveformView(
                            waveform = studioState.waveform,
                            totalDurationMs = studioState.totalDurationMs,
                            startMs = studioState.startMs,
                            endMs = studioState.endMs,
                            currentPlayheadMs = playerState.currentPositionMs,
                            onRangeChange = { start, end ->
                                viewModel.updateTrimRange(start, end)
                            },
                            modifier = Modifier.testTag("trim_waveform_view")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Precision Time Fine-Tuning Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Start Time Block
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StringsLocalization.get("start_time", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatPreciseTime(studioState.startMs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    StepButton(label = "-1s") {
                                        viewModel.updateTrimRange(studioState.startMs - 1000L, studioState.endMs)
                                    }
                                    StepButton(label = "-.1s") {
                                        viewModel.updateTrimRange(studioState.startMs - 100L, studioState.endMs)
                                    }
                                    StepButton(label = "+.1s") {
                                        viewModel.updateTrimRange(studioState.startMs + 100L, studioState.endMs)
                                    }
                                    StepButton(label = "+1s") {
                                        viewModel.updateTrimRange(studioState.startMs + 1000L, studioState.endMs)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // End Time Block
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = StringsLocalization.get("end_time", language),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatPreciseTime(studioState.endMs),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    StepButton(label = "-1s") {
                                        viewModel.updateTrimRange(studioState.startMs, studioState.endMs - 1000L)
                                    }
                                    StepButton(label = "-.1s") {
                                        viewModel.updateTrimRange(studioState.startMs, studioState.endMs - 100L)
                                    }
                                    StepButton(label = "+.1s") {
                                        viewModel.updateTrimRange(studioState.startMs, studioState.endMs + 100L)
                                    }
                                    StepButton(label = "+1s") {
                                        viewModel.updateTrimRange(studioState.startMs, studioState.endMs + 1000L)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Preview Player Button
                        OutlinedButton(
                            onClick = {
                                if (playerState.isPlaying) {
                                    viewModel.playerManager.togglePlayPause()
                                } else {
                                    viewModel.playTrimPreview()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("play_trim_preview_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (playerState.isPlaying) StringsLocalization.get("pause", language)
                                else StringsLocalization.get("play_selection", language),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Audio Editing Effects Card (Fade, Volume, Speed)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Audio Effects & Enhancements",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Fade In Slider (0 to 5s)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsLocalization.get("fade_in", language),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = String.format("%.1f s", studioState.trimSettings.fadeInSec),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = studioState.trimSettings.fadeInSec,
                            onValueChange = {
                                viewModel.updateTrimSettings(studioState.trimSettings.copy(fadeInSec = it))
                            },
                            valueRange = 0f..5f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Fade Out Slider (0 to 5s)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsLocalization.get("fade_out", language),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = String.format("%.1f s", studioState.trimSettings.fadeOutSec),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = studioState.trimSettings.fadeOutSec,
                            onValueChange = {
                                viewModel.updateTrimSettings(studioState.trimSettings.copy(fadeOutSec = it))
                            },
                            valueRange = 0f..5f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Volume Boost (50% to 200%)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = StringsLocalization.get("volume_boost", language),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${(studioState.trimSettings.volumeBoost * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (studioState.trimSettings.volumeBoost > 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Slider(
                            value = studioState.trimSettings.volumeBoost,
                            onValueChange = {
                                viewModel.updateTrimSettings(studioState.trimSettings.copy(volumeBoost = it))
                            },
                            valueRange = 0.5f..2.0f,
                            steps = 14,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Playback Speed Chips
                        Text(
                            text = StringsLocalization.get("speed", language),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                val isSelected = studioState.trimSettings.speed == spd
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateTrimSettings(studioState.trimSettings.copy(speed = spd))
                                    },
                                    label = { Text("${spd}x", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Save Format Chips
                        Text(
                            text = StringsLocalization.get("audio_format", language),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AudioFormat.entries.forEach { fmt ->
                                val isSelected = studioState.trimSettings.outputFormat == fmt
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.updateTrimSettings(studioState.trimSettings.copy(outputFormat = fmt))
                                    },
                                    label = { Text(fmt.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Export Trimmed Audio Button
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.exportTrimmedAudio() },
                        enabled = !studioState.isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("export_trimmed_audio_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (studioState.isProcessing) StringsLocalization.get("processing_edit", language)
                            else StringsLocalization.get("save_cut", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    AnimatedVisibility(visible = studioState.isProcessing) {
                        Column(modifier = Modifier.padding(top = 10.dp)) {
                            LinearProgressIndicator(
                                progress = { studioState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            // Export Result Card
            studioState.trimmedResultItem?.let { trimmed ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = StringsLocalization.get("edit_complete", language),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${trimmed.title} (${formatTime(trimmed.durationMs)})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.playAudio(trimmed) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(StringsLocalization.get("play", language))
                                }
                                OutlinedButton(
                                    onClick = { viewModel.shareAudio(context, trimmed) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(StringsLocalization.get("share", language))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun StepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatPreciseTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val tenths = (ms % 1000) / 100
    return String.format("%02d:%02d.%d", minutes, seconds, tenths)
}
