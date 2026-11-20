package com.lsg.locallanguagebibles.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.lsg.obolobibles.data.model.BibleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class AudioState(
    val isPlaying: Boolean = false,
    val currentVerseIndex: Int = -1,
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val availableVoices: List<Voice> = emptyList(),
    val selectedVoice: Voice? = null
)

class AudioReaderManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)

    private val _audioState = MutableStateFlow(AudioState())
    val audioState: StateFlow<AudioState> = _audioState.asStateFlow()

    private var playlist: List<BibleEntity> = emptyList()

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: return
                _audioState.value = _audioState.value.copy(
                    isPlaying = true,
                    currentVerseIndex = index
                )
            }

            override fun onDone(utteranceId: String?) {
                val nextIndex = (utteranceId?.toIntOrNull() ?: -1) + 1
                if (nextIndex in playlist.indices) {
                    speakVerseAtIndex(nextIndex)
                } else {
                    _audioState.value = _audioState.value.copy(
                        isPlaying = false,
                        currentVerseIndex = -1
                    )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _audioState.value = _audioState.value.copy(
                    isPlaying = false
                )
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.ENGLISH
            val voices = tts?.voices?.toList() ?: emptyList()
            _audioState.value = _audioState.value.copy(
                availableVoices = voices,
                selectedVoice = tts?.voice ?: voices.firstOrNull()
            )
        }
    }

    fun startPlaylist(verses: List<BibleEntity>, startIndex: Int = 0) {
        playlist = verses
        if (startIndex in playlist.indices) {
            speakVerseAtIndex(startIndex)
        }
    }

    private fun speakVerseAtIndex(index: Int) {
        if (index !in playlist.indices) return
        val verse = playlist[index]
        tts?.setSpeechRate(_audioState.value.speechRate)
        tts?.setPitch(_audioState.value.pitch)
        _audioState.value.selectedVoice?.let { tts?.voice = it }

        val textToSpeak = "Verse ${verse.verse}. ${verse.text}"
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, index.toString())
    }

    fun pauseOrResume() {
        if (_audioState.value.isPlaying) {
            tts?.stop()
            _audioState.value = _audioState.value.copy(isPlaying = false)
        } else {
            val idx = if (_audioState.value.currentVerseIndex >= 0) _audioState.value.currentVerseIndex else 0
            if (playlist.isNotEmpty()) {
                speakVerseAtIndex(idx)
            }
        }
    }

    fun stop() {
        tts?.stop()
        _audioState.value = _audioState.value.copy(
            isPlaying = false,
            currentVerseIndex = -1
        )
    }

    fun setSpeechRate(rate: Float) {
        _audioState.value = _audioState.value.copy(speechRate = rate)
        tts?.setSpeechRate(rate)
    }

    fun setPitch(pitch: Float) {
        _audioState.value = _audioState.value.copy(pitch = pitch)
        tts?.setPitch(pitch)
    }

    fun setVoice(voice: Voice) {
        _audioState.value = _audioState.value.copy(selectedVoice = voice)
        tts?.voice = voice
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
