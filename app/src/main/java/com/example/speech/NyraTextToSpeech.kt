package com.example.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class NyraTextToSpeech(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var pendingSpeechText: String? = null

    var onSpeechCompletedListener: (() -> Unit)? = null

    companion object {
        private const val TAG = "NyraTextToSpeech"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Try Hindi-India, then English-India, then English-US
            var langResult = tts?.setLanguage(Locale("hi", "IN"))
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                langResult = tts?.setLanguage(Locale("en", "IN"))
            }
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }

            // Natural, cute female voice settings: pitch 1.25f gives a sweet, clear female tone
            tts?.setPitch(1.25f)
            tts?.setSpeechRate(1.00f)

            // Try picking an available offline female voice
            try {
                val voices = tts?.voices
                if (!voices.isNullOrEmpty()) {
                    val offlineVoices = voices.filter { !it.isNetworkConnectionRequired }
                    
                    val femaleVoice = offlineVoices.firstOrNull { voice ->
                        val name = voice.name.lowercase(Locale.getDefault())
                        (name.contains("female") || name.contains("woman") || name.contains("fem") || 
                         name.contains("hi-in") || name.contains("en-in") || name.contains("cmn") || name.contains("hia")) &&
                                !name.contains("male") && !name.contains("-m-")
                    } ?: offlineVoices.firstOrNull { voice ->
                        val name = voice.name.lowercase(Locale.getDefault())
                        !name.contains("male") && !name.contains("-m-")
                    }

                    if (femaleVoice != null) {
                        tts?.voice = femaleVoice
                        Log.d(TAG, "Selected Cute Female Voice: ${femaleVoice.name}")
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Voice selection fallback to engine default: ${e.message}")
            }

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    onSpeechCompletedListener?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                    Log.e(TAG, "TTS Utterance Error code: $errorCode")
                }
            })

            _isReady.value = true
            Log.d(TAG, "TTS Initialized successfully")

            // Speak any pending text requested while initializing
            pendingSpeechText?.let { text ->
                pendingSpeechText = null
                speak(text)
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    fun speak(text: String) {
        if (_isMuted.value) return
        if (text.isBlank()) return

        // Clean text for smooth speech output
        val cleanSpeechText = text
            .replace(Regex("https?://\\S+"), "link")
            .replace(Regex("[*#_`~>•\\\\-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanSpeechText.isBlank()) return

        if (!_isReady.value) {
            // Queue pending text if TTS is still initializing
            pendingSpeechText = cleanSpeechText
            return
        }

        try {
            val result = tts?.speak(
                cleanSpeechText,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "NyraTTS_${System.currentTimeMillis()}"
            )
            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "TTS speak returned ERROR")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during TTS speak: ${e.message}")
        }
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        _isSpeaking.value = false
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            stop()
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
