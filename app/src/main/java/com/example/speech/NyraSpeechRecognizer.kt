package com.example.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class NyraSpeechRecognizer(private val context: Context) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _rmsDbLevel = MutableStateFlow(0f)
    val rmsDbLevel: StateFlow<Float> = _rmsDbLevel.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    private val _recognizedResult = MutableSharedFlow<String>()
    val recognizedResult: SharedFlow<String> = _recognizedResult.asSharedFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    var onSessionEndedListener: ((hasResult: Boolean) -> Unit)? = null

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@NyraSpeechRecognizer)
            }
        } else {
            _errorMessage.value = "Speech recognition is not available on this device."
        }
    }

    fun startListening() {
        _errorMessage.value = null
        _partialText.value = ""

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _errorMessage.value = "Speech recognition is unavailable on this device."
            return
        }

        if (speechRecognizer == null) {
            initRecognizer()
        }

        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.d(TAG, "Cancel prior recognizer: ${e.message}")
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer: ${e.message}")
            _errorMessage.value = e.localizedMessage
            _isListening.value = false
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        _isListening.value = false
    }

    fun cancel() {
        speechRecognizer?.cancel()
        _isListening.value = false
        _rmsDbLevel.value = 0f
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        _errorMessage.value = null
    }

    override fun onBeginningOfSpeech() {
        _isListening.value = true
    }

    override fun onRmsChanged(rmsdB: Float) {
        _rmsDbLevel.value = rmsdB
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        _rmsDbLevel.value = 0f
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _rmsDbLevel.value = 0f
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required, Boss!"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input received"
            else -> "Speech recognition error"
        }
        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_CLIENT) {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying recognizer: ${e.message}")
            }
            speechRecognizer = null
        }
        if (error != SpeechRecognizer.ERROR_NO_MATCH && error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            _errorMessage.value = message
        }
        onSessionEndedListener?.invoke(false)
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _rmsDbLevel.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            _partialText.value = text
            _recognizedResult.tryEmit(text)
            onSessionEndedListener?.invoke(true)
        } else {
            onSessionEndedListener?.invoke(false)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _partialText.value = matches[0]
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    companion object {
        private const val TAG = "NyraSpeechRec"
    }
}
