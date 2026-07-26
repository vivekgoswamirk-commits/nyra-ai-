package com.example.assistant

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLaunchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class AssistantResponse(
    val text: String,
    val actionType: String = "CHAT",
    val isError: Boolean = false,
    val isFinancialWarning: Boolean = false
)

class NyraAssistantEngine(private val context: Context) {

    private val appLauncherManager = AppLauncherManager(context)
    private var isFlashlightOn = false

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun processQuery(rawQuery: String): AssistantResponse = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            return@withContext AssistantResponse("Boss, I didn't catch that. Please try again.")
        }

        // 1. Check Financial / Security Safety Policy FIRST
        if (NyraSafetyPolicy.isFinancialOrRestrictedOperation(query)) {
            return@withContext AssistantResponse(
                text = NyraSafetyPolicy.FINANCIAL_SAFETY_WARNING,
                actionType = "SAFETY_WARNING",
                isFinancialWarning = true
            )
        }

        var lowerQuery = query.lowercase(Locale.getDefault())

        // 0. Wake Word & Greetings Trigger
        if (lowerQuery == "hey nyra" || lowerQuery == "hello nyra" || lowerQuery == "hi nyra" || lowerQuery == "nyra" ||
            lowerQuery == "hello" || lowerQuery == "hi" || lowerQuery == "hey" || lowerQuery == "namaste") {
            return@withContext AssistantResponse(
                text = "Namaste Boss! Main Nyra hu. YouTube kholna ho, phone lagana ho ya koi sawaal ho, bataiye kya madad karu?",
                actionType = "WAKE_WORD"
            )
        }

        if (lowerQuery.startsWith("hey nyra ") || lowerQuery.startsWith("hello nyra ")) {
            lowerQuery = lowerQuery.removePrefix("hey nyra ").removePrefix("hello nyra ").trim()
        }

        // 1. Phone Calling Command ("Call Rahul", "Call 9876543210", "Papa ko call karo", "Phone lagao Rahul")
        if (lowerQuery.contains("call") || lowerQuery.contains("phone lagao") || lowerQuery.contains("phone karo") || lowerQuery.contains("call karo") || lowerQuery.contains("ko call")) {
            return@withContext handlePhoneCall(query, lowerQuery)
        }

        // 2. WhatsApp Message Command ("WhatsApp message to Rahul Hello", "WhatsApp par message karo")
        if (lowerQuery.contains("whatsapp")) {
            return@withContext handleWhatsAppMessage(query, lowerQuery)
        }

        // 3. Amazon Saree Search & Recommendations ("Saree dekhao", "Saree show karo", "Show sarees on Amazon")
        if (lowerQuery.contains("saree") || lowerQuery.contains("sari") || lowerQuery.contains("sarees")) {
            return@withContext handleAmazonSareeSearch()
        }

        // 4. Fabric Material Identification ("Konsa fabric hai", "Fabric dekho", "Kapda konsa hai")
        if (lowerQuery.contains("fabric") || lowerQuery.contains("kapda") || lowerQuery.contains("material dekho") || lowerQuery.contains("febric")) {
            return@withContext AssistantResponse(
                text = "Boss, kapde ka photo chat me upload karein ya camera icon se photo lein! Main Gemini AI Vision se kapde ka fabric (Cotton, Silk, Linen, Georgette, etc.) aur washing care details batati hu.",
                actionType = "FABRIC_INFO"
            )
        }

        // 2. App Launch Commands ("Open WhatsApp", "Open Camera", "Kholo Instagram", etc.)
        if (isAppLaunchIntent(lowerQuery)) {
            val (result, speechText) = appLauncherManager.findAndLaunchApp(query)
            val action = when (result) {
                is AppLaunchResult.Success -> "APP_LAUNCH"
                is AppLaunchResult.NotInstalled -> "APP_NOT_INSTALLED"
                is AppLaunchResult.Error -> "APP_ERROR"
            }
            return@withContext AssistantResponse(
                text = speechText,
                actionType = action,
                isError = result is AppLaunchResult.Error
            )
        }

        // 3. Safe System Control Commands
        // Time command
        if (lowerQuery.contains("time") || lowerQuery.contains("samay") || lowerQuery.contains("baj rha")) {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val dateSdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            val currentTime = sdf.format(Date())
            val currentDate = dateSdf.format(Date())
            return@withContext AssistantResponse(
                text = "Boss, the current time is $currentTime on $currentDate.",
                actionType = "TIME"
            )
        }

        // Battery command
        if (lowerQuery.contains("battery") || lowerQuery.contains("charge") || lowerQuery.contains("charging")) {
            val batteryStatus = getBatteryInfo()
            return@withContext AssistantResponse(
                text = "Boss, your device $batteryStatus",
                actionType = "DEVICE_CONTROL"
            )
        }

        // Flashlight command
        if (lowerQuery.contains("flashlight") || lowerQuery.contains("torch")) {
            val responseText = toggleFlashlight(lowerQuery)
            return@withContext AssistantResponse(
                text = responseText,
                actionType = "DEVICE_CONTROL"
            )
        }

        // Open Settings command
        if (lowerQuery.contains("settings") || lowerQuery.contains("setting kholo")) {
            return@withContext try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                AssistantResponse("Opening device settings, Boss!", actionType = "DEVICE_CONTROL")
            } catch (e: Exception) {
                AssistantResponse("Boss, unable to open settings right now.", actionType = "DEVICE_CONTROL", isError = true)
            }
        }

        // Alarm command
        if (lowerQuery.contains("alarm") || lowerQuery.contains("jagao") || lowerQuery.contains("wake me up")) {
            return@withContext setDeviceAlarm(lowerQuery)
        }

        // 4. Gemini AI / Natural Language Conversation
        val aiResponseText = callGeminiApiOrFallback(query)
        AssistantResponse(
            text = aiResponseText,
            actionType = "CHAT"
        )
    }

    private fun isAppLaunchIntent(lowerQuery: String): Boolean {
        val triggers = listOf("open ", "launch ", "start ", "kholo ", "chalu karo ", "run ", "play ", "go to ", "show ")
        if (triggers.any { lowerQuery.startsWith(it) }) return true

        val suffixes = listOf(" kholo", " open karo", " chalu karo", " open", " launch karo", " play karo", " play")
        if (suffixes.any { lowerQuery.endsWith(it) }) return true

        val commonApps = listOf("youtube", "whatsapp", "instagram", "facebook", "chrome", "google", "camera", "gallery", "settings")
        if (commonApps.any { lowerQuery.contains(it) }) return true

        // If exact or contains installed app name
        val installed = appLauncherManager.getInstalledApps()
        val matchFound = installed.any { app ->
            val appNameLower = app.appName.lowercase(Locale.getDefault())
            lowerQuery == appNameLower || lowerQuery == "open $appNameLower" || lowerQuery.contains(appNameLower)
        }
        return matchFound
    }

    private fun getBatteryInfo(): String {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = context.registerReceiver(null, intentFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val batteryPct = if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).toInt() else 100
            val statusText = if (isCharging) "is currently charging" else "is on battery power"

            "battery level is at $batteryPct% and $statusText."
        } catch (e: Exception) {
            "battery details are currently unavailable."
        }
    }

    private fun toggleFlashlight(lowerQuery: String): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return "Boss, flashlight hardware is not detected on this device."

            val turnOn = lowerQuery.contains("on") || lowerQuery.contains("chalu") || !isFlashlightOn
            cameraManager.setTorchMode(cameraId, turnOn)
            isFlashlightOn = turnOn
            if (turnOn) "Flashlight turned on, Boss!" else "Flashlight turned off, Boss!"
        } catch (e: Exception) {
            "Boss, unable to toggle flashlight (${e.localizedMessage})."
        }
    }

    private fun callGeminiApiOrFallback(userPrompt: String): String {
        val apiKeyManager = com.example.data.ApiKeyManager(context)
        val apiKey = apiKeyManager.getApiKey()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val systemInstructionText = "You are Nyra, an intelligent, friendly, and ultra-reliable Android AI Assistant. ALWAYS address the user as 'Boss' in every single response. Keep your answers concise, helpful, and natural for speech output. Never perform or promise financial, banking, or password operations."

                val jsonPayload = JSONObject().apply {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
                    })
                    put("contents", JSONArray().put(
                        JSONObject().put("parts", JSONArray().put(
                            JSONObject().put("text", userPrompt)
                        ))
                    ))
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonPayload.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBodyStr = response.body?.string()

                if (response.isSuccessful && !responseBodyStr.isNullOrEmpty()) {
                    val rootJson = JSONObject(responseBodyStr)
                    val candidates = rootJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val contentObj = firstCandidate.optJSONObject("content")
                        val partsArr = contentObj?.optJSONArray("parts")
                        if (partsArr != null && partsArr.length() > 0) {
                            val text = partsArr.getJSONObject(0).optString("text")
                            if (text.isNotBlank()) {
                                return formatBossResponse(text)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NyraEngine", "Gemini API call failed: ${e.message}")
            }
        }

        // Smart Offline / Fallback response generator with "Boss" personality
        return generateOfflineBossResponse(userPrompt)
    }

    private fun formatBossResponse(text: String): String {
        var clean = text.trim()
        if (!clean.contains("Boss", ignoreCase = true)) {
            clean = "Boss, $clean"
        }
        return clean
    }

    private fun generateOfflineBossResponse(prompt: String): String {
        val lower = prompt.lowercase(Locale.getDefault())
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello Boss! How can I assist you on your device today?"
            lower.contains("who are you") || lower.contains("what is your name") ->
                "I am Nyra, your personal AI assistant Boss! Ready to open apps, tell the time, and control your device."
            lower.contains("how are you") ->
                "I am running at peak performance, Boss! How are you doing today?"
            lower.contains("thank") ->
                "You are very welcome, Boss! Always at your service."
            else ->
                "Boss, I've processed your request: '$prompt'. Let me know if you need any apps opened or device settings checked!"
        }
    }

    private fun setDeviceAlarm(lowerQuery: String): AssistantResponse {
        return try {
            var hour = 5
            var minutes = 0
            var isPm = false

            if (lowerQuery.contains("pm") || lowerQuery.contains("shaam") || lowerQuery.contains("raat")) {
                isPm = true
            }

            val numbers = Regex("\\d+").findAll(lowerQuery).map { it.value.toInt() }.toList()
            if (numbers.isNotEmpty()) {
                val rawHour = numbers[0]
                if (rawHour in 0..23) {
                    hour = rawHour
                }
                if (numbers.size >= 2) {
                    val rawMin = numbers[1]
                    if (rawMin in 0..59) {
                        minutes = rawMin
                    }
                }
            }

            if (isPm && hour < 12) {
                hour += 12
            } else if (!isPm && (lowerQuery.contains("am") || lowerQuery.contains("subah") || lowerQuery.contains("morning")) && hour == 12) {
                hour = 0
            }

            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, "Nyra AI Alarm")
                putExtra(AlarmClock.EXTRA_SKIP_UI, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            val amPmStr = if (hour >= 12) "PM" else "AM"
            val formattedMin = String.format(Locale.getDefault(), "%02d", minutes)

            AssistantResponse(
                text = "Boss, 5 baje ka alarm aapke phone clock me set kar diya hai ($displayHour:$formattedMin $amPmStr)! Subah time pe jag jaana.",
                actionType = "ALARM_SET"
            )
        } catch (e: Exception) {
            Log.e("NyraAssistant", "Failed to set alarm", e)
            AssistantResponse("Boss, alarm clock open kar diya hai. Please alarm confirm karein.", actionType = "ALARM_ERROR", isError = true)
        }
    }

    private fun handlePhoneCall(rawQuery: String, lowerQuery: String): AssistantResponse {
        return try {
            val phoneDigits = lowerQuery.replace(Regex("[^0-9+]"), "")
            var targetNameOrNumber = ""

            if (phoneDigits.length >= 7) {
                targetNameOrNumber = phoneDigits
            } else {
                val cleaned = lowerQuery
                    .replace("call karo", "")
                    .replace("call lagao", "")
                    .replace("phone lagao", "")
                    .replace("phone karo", "")
                    .replace("ko call", "")
                    .replace("call", "")
                    .trim()
                targetNameOrNumber = if (cleaned.isNotBlank()) cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } else "Contact"
            }

            var dialUri = Uri.parse("tel:${if (phoneDigits.length >= 7) phoneDigits else ""}")

            if (phoneDigits.length < 7 && targetNameOrNumber.isNotBlank()) {
                val searchedNumber = searchContactNumber(targetNameOrNumber)
                if (!searchedNumber.isNullOrBlank()) {
                    dialUri = Uri.parse("tel:$searchedNumber")
                }
            }

            val intent = Intent(Intent.ACTION_DIAL, dialUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            AssistantResponse(
                text = "Boss, $targetNameOrNumber ko call lagane ke liye dialer open kar diya hai! Simply confirm dial button.",
                actionType = "PHONE_CALL"
            )
        } catch (e: Exception) {
            Log.e("NyraAssistant", "Failed to place call", e)
            AssistantResponse("Boss, phone dialer open nahi ho paya. Please check permissions.", actionType = "CALL_ERROR", isError = true)
        }
    }

    private fun searchContactNumber(contactName: String): String? {
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$contactName%"),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (numIdx != -1) return it.getString(numIdx)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun handleWhatsAppMessage(rawQuery: String, lowerQuery: String): AssistantResponse {
        return try {
            val phoneDigits = lowerQuery.replace(Regex("[^0-9+]"), "")

            val intent = if (phoneDigits.length >= 10) {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$phoneDigits")).apply {
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://whatsapp.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }

            context.startActivity(intent)

            AssistantResponse(
                text = "Boss, WhatsApp khol diya hai! Contact select karke message bhej dijiye.",
                actionType = "WHATSAPP_MSG"
            )
        } catch (e: Exception) {
            Log.e("NyraAssistant", "Failed to launch WhatsApp", e)
            AssistantResponse("Boss, WhatsApp launch karne me error aaya.", actionType = "WHATSAPP_ERROR", isError = true)
        }
    }

    private fun handleAmazonSareeSearch(): AssistantResponse {
        return try {
            val amazonUri = Uri.parse("https://www.amazon.in/s?k=saree")
            val intent = Intent(Intent.ACTION_VIEW, amazonUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)

            val sareeRecommendations = """
Boss, Amazon par Sarees ki search khol di hai! Here are top recommended saree types for you:

1. 🌟 Banarasi Silk Saree - Traditional royal wedding choice
2. 🌸 Georgette Designer Saree - Lightweight & elegant daily wear
3. 💎 Kanjeevaram Silk - Rich South Indian silk fabric
4. ✨ Organza Floral Saree - Modern partywear trend
5. 🍃 Pure Cotton Saree - Comfortable breathable daily wear
            """.trimIndent()

            AssistantResponse(
                text = sareeRecommendations,
                actionType = "AMAZON_SAREE_SEARCH"
            )
        } catch (e: Exception) {
            AssistantResponse("Boss, Amazon search open karne me error aaya.", actionType = "AMAZON_ERROR", isError = true)
        }
    }

    suspend fun analyzeFabricImage(bitmap: Bitmap): AssistantResponse = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AssistantResponse(
                text = "Boss, photo ke mutabiq ye fine quality woven fabric lag raha hai. (For detailed Gemini AI Vision breakdown, please configure GEMINI_API_KEY in settings).",
                actionType = "FABRIC_ANALYSIS"
            )
        }

        try {
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Image = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val prompt = "Analyze this fabric/clothing photo in detail for the user. Identify: 1. Material/Fabric Type (e.g. Cotton, Pure Silk, Georgette, Linen, Denim, Polyester, Satin, Velvet, Organza), 2. Weave and Texture Feel, 3. Recommended Care & Washing Instructions. Reply clearly in simple Hindi/Hinglish addressed to 'Boss'."

            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().put(
                    JSONObject().put("parts", JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                        put(JSONObject().put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        }))
                    })
                ))
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonPayload.toString().toRequestBody(mediaType)
            val request = Request.Builder().url(url).post(requestBody).build()

            val response = okHttpClient.newCall(request).execute()
            val responseStr = response.body?.string()

            if (response.isSuccessful && !responseStr.isNullOrEmpty()) {
                val rootJson = JSONObject(responseStr)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val textPart = candidates.getJSONObject(0).optJSONObject("content")?.optJSONArray("parts")?.getJSONObject(0)?.optString("text")
                    if (!textPart.isNullOrBlank()) {
                        return@withContext AssistantResponse(
                            text = textPart,
                            actionType = "FABRIC_ANALYSIS"
                        )
                    }
                }
            }
            AssistantResponse("Boss, kapde ka fabric identify karne me thodi samasya aayi. Dobara try karein!", actionType = "FABRIC_ERROR", isError = true)
        } catch (e: Exception) {
            Log.e("NyraAssistant", "Fabric analysis error", e)
            AssistantResponse("Boss, fabric analysis failed: ${e.localizedMessage}", actionType = "FABRIC_ERROR", isError = true)
        }
    }
}
