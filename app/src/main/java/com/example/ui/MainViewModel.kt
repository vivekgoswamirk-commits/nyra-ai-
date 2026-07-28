package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.assistant.NyraAssistantEngine
import com.example.assistant.AppLauncherManager
import com.example.data.db.ChatMessageEntity
import com.example.data.db.NyraDatabase
import com.example.data.model.InstalledApp
import com.example.data.model.PermissionHelper
import com.example.data.model.PermissionItem
import com.example.data.repository.ChatRepository
import com.example.speech.NyraSpeechRecognizer
import com.example.speech.NyraTextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = NyraDatabase.getDatabase(application)
    private val repository = ChatRepository(db.chatMessageDao())

    val assistantEngine = NyraAssistantEngine(application)
    val appLauncherManager = AppLauncherManager(application)
    val tts = NyraTextToSpeech(application)
    val speechRecognizer = NyraSpeechRecognizer(application)

    val chatHistory: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _permissions = MutableStateFlow<List<PermissionItem>>(emptyList())
    val permissions: StateFlow<List<PermissionItem>> = _permissions.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val installedApps: StateFlow<List<InstalledApp>> = _installedApps.asStateFlow()

    private val _textInput = MutableStateFlow("")
    val textInput: StateFlow<String> = _textInput.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Chat, 1: Setup, 2: Apps, 3: Safety
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("user@nyra.app")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _abuseWarningCount = MutableStateFlow(0)
    val abuseWarningCount: StateFlow<Int> = _abuseWarningCount.asStateFlow()

    private val _isUserBanned = MutableStateFlow(false)
    val isUserBanned: StateFlow<Boolean> = _isUserBanned.asStateFlow()

    val apiKeyManager = com.example.data.ApiKeyManager(application)
    val userProfileManager = com.example.data.UserProfileManager(application)

    private val _userProfile = MutableStateFlow(userProfileManager.getUserProfile())
    val userProfile: StateFlow<com.example.data.model.UserProfile> = _userProfile.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    fun openProfileDialog() {
        _showProfileDialog.value = true
    }

    fun dismissProfileDialog() {
        _showProfileDialog.value = false
    }

    fun updateUserProfile(name: String, email: String) {
        userProfileManager.updateUserProfile(name, email)
        _userProfile.value = userProfileManager.getUserProfile()
    }

    fun signInWithGoogle(name: String, email: String, photoUrl: String? = null) {
        userProfileManager.saveAuthSession(name, email, photoUrl, "Google / Firebase")
        _userProfile.value = userProfileManager.getUserProfile()
        if (email.isNotBlank()) {
            _currentUserEmail.value = email
        }
        viewModelScope.launch {
            repository.addMessage(
                sender = "assistant",
                text = "Welcome $name! Google Sign-In successful. Your 10-Digit User ID is ${_userProfile.value.uid}."
            )
        }
    }

    fun signOut() {
        userProfileManager.signOut()
        _userProfile.value = userProfileManager.getUserProfile()
        _showProfileDialog.value = false
    }

    private val _showApiKeyDialog = MutableStateFlow(!apiKeyManager.hasApiKey())
    val showApiKeyDialog: StateFlow<Boolean> = _showApiKeyDialog.asStateFlow()

    private val _isContinuousVoiceMode = MutableStateFlow(true)
    val isContinuousVoiceMode: StateFlow<Boolean> = _isContinuousVoiceMode.asStateFlow()

    fun toggleContinuousVoiceMode() {
        val newMode = !_isContinuousVoiceMode.value
        _isContinuousVoiceMode.value = newMode
        if (newMode) {
            if (!speechRecognizer.isListening.value && !_isProcessing.value) {
                tts.stop()
                speechRecognizer.startListening()
            }
        } else {
            speechRecognizer.stopListening()
        }
    }

    private fun isWakeWordPresent(rawText: String): Boolean {
        val lower = rawText.lowercase(Locale.getDefault())
        val keywords = listOf("nyra", "neera", "naira", "nira", "nyna", "hey nyra", "hello nyra", "hi nyra", "ok nyra")
        return keywords.any { lower.contains(it) }
    }

    fun saveApiKey(key: String) {
        if (key.isNotBlank()) {
            apiKeyManager.saveApiKey(key)
            _showApiKeyDialog.value = false
            viewModelScope.launch {
                repository.addMessage(
                    sender = "assistant",
                    text = "Boss, your Gemini API Key has been saved! Gemini AI features are now active."
                )
            }
            tts.speak("Boss, your Gemini API key has been saved!")
        }
    }

    fun dismissApiKeyDialog() {
        _showApiKeyDialog.value = false
    }

    fun openApiKeyDialog() {
        _showApiKeyDialog.value = true
    }

    init {
        refreshPermissions()
        refreshInstalledApps()

        // Continuous Speech Listener Trigger
        tts.onSpeechCompletedListener = {
            if (_isContinuousVoiceMode.value && !_isUserBanned.value) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(400)
                    if (!speechRecognizer.isListening.value && !_isProcessing.value) {
                        speechRecognizer.startListening()
                    }
                }
            }
        }

        // Auto-restart continuous listening when STT session finishes or errors out
        speechRecognizer.onSessionEndedListener = { hasResult ->
            if (_isContinuousVoiceMode.value && !_isUserBanned.value && !tts.isSpeaking.value && !_isProcessing.value) {
                viewModelScope.launch {
                    kotlinx.coroutines.delay(350)
                    if (!speechRecognizer.isListening.value && !tts.isSpeaking.value && !_isProcessing.value) {
                        speechRecognizer.startListening()
                    }
                }
            }
        }

        // Observe STT recognized results
        viewModelScope.launch {
            speechRecognizer.recognizedResult.collectLatest { query ->
                if (query.isNotBlank()) {
                    if (_isContinuousVoiceMode.value) {
                        if (isWakeWordPresent(query)) {
                            _textInput.value = query
                            sendQuery(query)
                        } else {
                            // Ignored background talk because 'Nyra' was not mentioned
                            _textInput.value = query
                            viewModelScope.launch {
                                kotlinx.coroutines.delay(350)
                                if (!speechRecognizer.isListening.value && !tts.isSpeaking.value && !_isProcessing.value) {
                                    speechRecognizer.startListening()
                                }
                            }
                        }
                    } else {
                        _textInput.value = query
                        sendQuery(query)
                    }
                }
            }
        }

        // Add welcome message if history is empty
        viewModelScope.launch {
            chatHistory.collectLatest { list ->
                if (list.isEmpty()) {
                    repository.addMessage(
                        sender = "assistant",
                        text = "Hello Boss! Main Nyra hoon, aapki cute aur smart AI assistant. Boliye, aaj main aapki kya madad karoon?"
                    )
                }
            }
        }
    }

    fun onTextInputChanged(text: String) {
        _textInput.value = text
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun refreshPermissions() {
        _permissions.value = PermissionHelper.checkAllPermissions(getApplication())
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = appLauncherManager.getInstalledApps()
        }
    }

    fun toggleMicListening() {
        if (speechRecognizer.isListening.value) {
            _isContinuousVoiceMode.value = false
            speechRecognizer.stopListening()
        } else {
            _isContinuousVoiceMode.value = true
            tts.stop()
            speechRecognizer.startListening()
        }
    }

    fun analyzeFabricImage(bitmap: android.graphics.Bitmap) {
        if (_isProcessing.value) return
        _isProcessing.value = true

        viewModelScope.launch {
            repository.addMessage(sender = "user", text = "📷 [Uploaded Fabric Photo for Material Check]")
            val response = assistantEngine.analyzeFabricImage(bitmap)
            repository.addMessage(
                sender = "assistant",
                text = response.text,
                actionType = response.actionType,
                isError = response.isError
            )
            tts.speak(response.text)
            _isProcessing.value = false
        }
    }

    fun setCurrentUserEmail(email: String) {
        _currentUserEmail.value = email.trim()
    }

    fun unblockUserByAdmin() {
        _isUserBanned.value = false
        _abuseWarningCount.value = 0
        viewModelScope.launch {
            val msg = "✅ Admin Notice: Account has been successfully UNBLOCKED by Admin (vivekgoswamirk@gmail.com)! You can now resume using Nyra Assistant."
            repository.addMessage(sender = "assistant", text = msg)
            tts.speak(msg)
        }
    }

    fun sendQuery(userText: String = _textInput.value) {
        val query = userText.trim()
        if (query.isBlank() || _isProcessing.value) return

        _textInput.value = ""
        _isProcessing.value = true

        val isCurrentAdmin = com.example.assistant.NyraSafetyPolicy.isAdmin(_currentUserEmail.value)

        viewModelScope.launch {
            // Save user message to database
            repository.addMessage(sender = "user", text = query)

            // 1. If user is BANNED (and not Admin), block execution
            if (_isUserBanned.value && !isCurrentAdmin) {
                val banText = "🚫 Account Banned: Aapka account abuse/bad language ki wajah se ban kar diya gaya hai. Admin (vivekgoswamirk@gmail.com) hi aapko unblock kar sakte hain."
                repository.addMessage(sender = "assistant", text = banText, isError = true)
                tts.speak(banText)
                _isProcessing.value = false
                return@launch
            }

            // 2. Abuse Language Check (Admin is EXEMPT from abuse filter!)
            if (!isCurrentAdmin && com.example.assistant.NyraSafetyPolicy.containsAbuse(query)) {
                val warnings = _abuseWarningCount.value + 1
                _abuseWarningCount.value = warnings

                if (warnings == 1) {
                    val warnText = "⚠️ Warning 1/2: Abusive ya bad language allowed nahi hai! Agar aapne dobara bad language use ki toh aapka account ban kar diya jayega."
                    repository.addMessage(sender = "assistant", text = warnText, isError = true)
                    tts.speak(warnText)
                    _isProcessing.value = false
                    return@launch
                } else if (warnings == 2) {
                    val warnText = "⚠️ Warning 2/2: Final Warning! Sending abusive messages again will permanently ban your account."
                    repository.addMessage(sender = "assistant", text = warnText, isError = true)
                    tts.speak(warnText)
                    _isProcessing.value = false
                    return@launch
                } else {
                    _isUserBanned.value = true
                    val banText = "🚫 Account Banned: 3 baar warning ke bawajood bad language use karne par aapka account BANNED ho gaya hai! Only Admin (vivekgoswamirk@gmail.com) unblock kar sakte hain."
                    repository.addMessage(sender = "assistant", text = banText, isError = true)
                    tts.speak(banText)
                    _isProcessing.value = false
                    return@launch
                }
            }

            // Process query through assistant engine
            val response = assistantEngine.processQuery(query)

            // Save assistant response to database
            repository.addMessage(
                sender = "assistant",
                text = response.text,
                actionType = response.actionType,
                isError = response.isError,
                isFinancialWarning = response.isFinancialWarning
            )

            // Speak response aloud
            tts.speak(response.text)

            _isProcessing.value = false
        }
    }

    fun speakText(text: String) {
        tts.speak(text)
    }

    fun toggleMuteTts() {
        tts.toggleMute()
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.destroy()
        tts.shutdown()
    }
}
