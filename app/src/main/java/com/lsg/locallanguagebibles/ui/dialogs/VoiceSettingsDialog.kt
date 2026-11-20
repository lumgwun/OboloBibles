package com.lsg.locallanguagebibles.ui.dialogs

import android.speech.tts.Voice
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lsg.locallanguagebibles.audio.AudioState
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceSettingsDialog(
    audioState: AudioState,
    onSpeechRateChange: (Float) -> Unit,
    onPitchChange: (Float) -> Unit,
    onVoiceSelect: (Voice) -> Unit,
    onDismiss: () -> Unit
) {
    var rate by remember(audioState.speechRate) { mutableFloatStateOf(audioState.speechRate) }
    var pitch by remember(audioState.pitch) { mutableFloatStateOf(audioState.pitch) }
    var voiceExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Audio Reader Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Speech Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Reading Speed", style = MaterialTheme.typography.bodyMedium)
                        Text("${((rate * 10).roundToInt() / 10f)}x", style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = rate,
                        onValueChange = {
                            rate = it
                            onSpeechRateChange(it)
                        },
                        valueRange = 0.5f..2.0f,
                        steps = 15
                    )
                }

                // Pitch
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", style = MaterialTheme.typography.bodyMedium)
                        Text("${((pitch * 10).roundToInt() / 10f)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Slider(
                        value = pitch,
                        onValueChange = {
                            pitch = it
                            onPitchChange(it)
                        },
                        valueRange = 0.5f..1.5f,
                        steps = 10
                    )
                }

                // Voice selector
                if (audioState.availableVoices.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = voiceExpanded,
                        onExpandedChange = { voiceExpanded = !voiceExpanded }
                    ) {
                        OutlinedTextField(
                            value = audioState.selectedVoice?.name ?: "Default Voice",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Voice") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = voiceExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = voiceExpanded,
                            onDismissRequest = { voiceExpanded = false }
                        ) {
                            audioState.availableVoices.take(15).forEach { voice ->
                                DropdownMenuItem(
                                    text = { Text("${voice.name} (${voice.locale.displayName})") },
                                    onClick = {
                                        onVoiceSelect(voice)
                                        voiceExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
