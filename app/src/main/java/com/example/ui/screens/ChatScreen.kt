package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.db.ChatMessageEntity
import com.example.ui.MainViewModel
import com.example.ui.components.FuturisticNyraOrbCanvas
import com.example.ui.components.NyraOrbState
import com.example.ui.theme.NyraAmber
import com.example.ui.theme.NyraAssistantBubble
import com.example.ui.theme.NyraBlue
import com.example.ui.theme.NyraCardBorder
import com.example.ui.theme.NyraCyan
import com.example.ui.theme.NyraEmerald
import com.example.ui.theme.NyraPurple
import com.example.ui.theme.NyraRose
import com.example.ui.theme.NyraUserBubble

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val chatHistory by viewModel.chatHistory.collectAsState()
    val textInput by viewModel.textInput.collectAsState()
    val isListening by viewModel.speechRecognizer.isListening.collectAsState()
    val rmsLevel by viewModel.speechRecognizer.rmsDbLevel.collectAsState()
    val partialText by viewModel.speechRecognizer.partialText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val isSpeaking by viewModel.tts.isSpeaking.collectAsState()
    val isUserBanned by viewModel.isUserBanned.collectAsStateWithLifecycle()

    val orbState = when {
        isListening -> NyraOrbState.LISTENING
        isProcessing -> NyraOrbState.THINKING
        isSpeaking -> NyraOrbState.SPEAKING
        else -> NyraOrbState.IDLE
    }

    val timelineItems = remember(chatHistory) {
        val list = mutableListOf<ChatTimelineItem>()
        var lastDayKey = ""
        for (msg in chatHistory) {
            val dayKey = getDayKey(msg.timestamp)
            if (dayKey != lastDayKey) {
                lastDayKey = dayKey
                list.add(ChatTimelineItem.DateHeader(dayKey, formatDateHeaderTitle(msg.timestamp)))
            }
            list.add(ChatTimelineItem.MessageItem(msg))
        }
        list
    }

    val listState = rememberLazyListState()

    LaunchedEffect(timelineItems.size) {
        if (timelineItems.isNotEmpty()) {
            listState.animateScrollToItem(timelineItems.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05050D))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Quick Actions & Suggestions Bar
            QuickActionChips(
                onQueryClick = { query ->
                    viewModel.sendQuery(query)
                },
                onSetupClick = {
                    viewModel.selectTab(1)
                }
            )

            // Central AI Orb & Status Banner
            CentralOrbHeader(
                orbState = orbState,
                rmsLevel = rmsLevel,
                partialText = partialText,
                onOrbClick = { viewModel.toggleMicListening() }
            )

            // Chat Timeline in glassmorphic cards
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = timelineItems,
                    key = { item ->
                        when (item) {
                            is ChatTimelineItem.DateHeader -> "header_${item.dayKey}"
                            is ChatTimelineItem.MessageItem -> "msg_${item.message.id}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is ChatTimelineItem.DateHeader -> {
                            DateHeaderChip(text = item.headerText)
                        }
                        is ChatTimelineItem.MessageItem -> {
                            ChatMessageItem(
                                message = item.message,
                                onSpeakClick = {
                                    viewModel.speakText(item.message.messageText)
                                }
                            )
                        }
                    }
                }

                if (isProcessing) {
                    item {
                        AssistantProcessingBubble()
                    }
                }
            }

            if (isUserBanned) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF380808)),
                    border = BorderStroke(1.dp, NyraRose),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚫", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACCOUNT BANNED FOR ABUSIVE LANGUAGE",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You have used abusive language 3 times. Only Admin (vivekgoswamirk@gmail.com) can unblock your account!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            // Chat Text Input Bar
            ChatInputBar(
                textInput = textInput,
                isListening = isListening,
                onTextChanged = { viewModel.onTextInputChanged(it) },
                onSend = { viewModel.sendQuery() },
                onMicClick = { viewModel.toggleMicListening() },
                onFabricImageSelected = { viewModel.analyzeFabricImage(it) }
            )
        }

        // Floating Futuristic Mic Action Button
        FloatingMicButton(
            isListening = isListening,
            orbState = orbState,
            onClick = { viewModel.toggleMicListening() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp)
        )
    }
}

@Composable
fun CentralOrbHeader(
    orbState: NyraOrbState,
    rmsLevel: Float,
    partialText: String,
    onOrbClick: () -> Unit
) {
    val (statusLabel, statusColor) = when (orbState) {
        NyraOrbState.IDLE -> "Nyra Online • Tap Orb to Speak" to NyraCyan
        NyraOrbState.LISTENING -> "Listening to Boss..." to NyraBlue
        NyraOrbState.THINKING -> "Thinking & Processing..." to NyraPurple
        NyraOrbState.SPEAKING -> "Nyra Speaking..." to NyraEmerald
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Futuristic Central AI Orb Canvas with 100+ Particles
        Box(
            modifier = Modifier
                .size(220.dp)
                .clickable { onOrbClick() }
                .testTag("nyra_ai_orb_canvas"),
            contentAlignment = Alignment.Center
        ) {
            FuturisticNyraOrbCanvas(
                state = orbState,
                rmsLevel = rmsLevel,
                orbSize = 220.dp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // State Status Tag
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = statusColor.copy(alpha = 0.15f),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(listOf(statusColor.copy(alpha = 0.6f), statusColor.copy(alpha = 0.3f)))
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (orbState == NyraOrbState.LISTENING && partialText.isNotBlank()) "\"$partialText\"" else statusLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun FloatingMicButton(
    isListening: Boolean,
    orbState: NyraOrbState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fabPulse")
    val fabScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    val fabColor = when (orbState) {
        NyraOrbState.LISTENING -> NyraBlue
        NyraOrbState.THINKING -> NyraPurple
        NyraOrbState.SPEAKING -> NyraEmerald
        else -> NyraCyan
    }

    FloatingActionButton(
        onClick = onClick,
        containerColor = fabColor,
        contentColor = Color.Black,
        shape = CircleShape,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
        modifier = modifier
            .scale(if (isListening) fabScale else 1.0f)
            .testTag("floating_mic_fab")
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Voice Input",
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun QuickActionChips(
    onQueryClick: (String) -> Unit,
    onSetupClick: () -> Unit
) {
    val chips = listOf(
        "Hey Nyra",
        "Saree dekhao",
        "Call Rahul",
        "WhatsApp message to Rahul",
        "Konsa fabric hai",
        "Set Alarm 5:00 AM",
        "Open WhatsApp",
        "What's the time?",
        "Check battery"
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Surface(
                onClick = onSetupClick,
                shape = RoundedCornerShape(20.dp),
                color = NyraPurple.copy(alpha = 0.25f),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NyraPurple, NyraCyan))),
                modifier = Modifier.testTag("permissions_setup_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Setup",
                        tint = NyraCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Permissions Setup",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(chips) { chipText ->
            Surface(
                onClick = { onQueryClick(chipText) },
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF121124),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NyraCardBorder, NyraCardBorder))),
                modifier = Modifier.testTag("chip_${chipText.lowercase().replace(" ", "_")}")
            ) {
                Text(
                    text = chipText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFE2E8F0),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

sealed class ChatTimelineItem {
    data class DateHeader(val dayKey: String, val headerText: String) : ChatTimelineItem()
    data class MessageItem(val message: ChatMessageEntity) : ChatTimelineItem()
}

private fun getDayKey(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}

private fun formatDateHeaderTitle(timestamp: Long): String {
    val now = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = timestamp }

    val dateStr = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date(timestamp))

    val isSameYear = now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    val isSameDay = isSameYear && now.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameDay -> "📅 Today • $dateStr"
        isYesterday -> "📅 Yesterday • $dateStr"
        else -> "📅 $dateStr"
    }
}

@Composable
fun DateHeaderChip(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1B1938),
            border = BorderStroke(1.dp, NyraPurple.copy(alpha = 0.5f))
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = NyraCyan,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    onSpeakClick: () -> Unit
) {
    val isUser = message.sender == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bubbleColor = if (isUser) NyraUserBubble else NyraAssistantBubble
    val bubbleShape = if (isUser) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NyraPurple.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_nyra_avatar_1785034562294),
                        contentDescription = "Nyra Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = bubbleShape,
                color = if (message.isFinancialWarning) NyraAmber.copy(alpha = 0.15f) else bubbleColor,
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        if (message.isFinancialWarning) listOf(NyraAmber, NyraAmber)
                        else if (isUser) listOf(NyraCyan.copy(alpha = 0.4f), NyraPurple.copy(alpha = 0.4f))
                        else listOf(NyraCardBorder, NyraCardBorder)
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .testTag(if (isUser) "user_message_bubble" else "assistant_message_bubble")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.isFinancialWarning) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Safety Policy",
                                tint = NyraAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Financial Safety Shield Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = NyraAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = message.messageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )

                        if (!isUser) {
                            IconButton(
                                onClick = onSpeakClick,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Speak aloud",
                                    tint = NyraCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssistantProcessingBubble() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = NyraAssistantBubble,
            border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(NyraCyan, NyraPurple)))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(NyraCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nyra is thinking, Boss...",
                    style = MaterialTheme.typography.labelSmall,
                    color = NyraCyan
                )
            }
        }
    }
}

@Composable
fun ChatInputBar(
    textInput: String,
    isListening: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit,
    onFabricImageSelected: (Bitmap) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                onFabricImageSelected(bitmap)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    Surface(
        color = Color(0xFF0F0E1C),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { photoLauncher.launch("image/*") },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1C38))
                    .testTag("fabric_camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Fabric Photo",
                    tint = NyraCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChanged,
                placeholder = {
                    Text(
                        text = "Type 'Hey Nyra', 'Saree dekhao'...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NyraCyan,
                    unfocusedBorderColor = Color(0xFF262342),
                    focusedContainerColor = Color(0xFF070712),
                    unfocusedContainerColor = Color(0xFF070712),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (textInput.isNotBlank()) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NyraCyan)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
