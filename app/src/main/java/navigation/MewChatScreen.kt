package com.example.ngilizcequizapp

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MewChatScreen(navController: NavController) {
    val context = LocalContext.current
    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val isSpaceTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isSpaceThemeActive(context) }
    val isKingdomTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isKingdomThemeActive(context) }
    val isDark = LocalIsDarkMode.current
    val appColors = LocalAppThemeColors.current

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Text-To-Speech for hearing Robot Mew speak
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try English first
                tts?.language = Locale.US
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    fun speakText(textToSpeak: String) {
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "mew_chat_id")
    }

    // Chat Message History
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                text = "Hello! I am Robot Mew, your AI assistant and language coach. What would you like to talk about today?",
                isFromUser = false
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }

    // Kingdom Gold Brush
    val kingdomGoldBrush = remember {
        Brush.linearGradient(
            listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))
        )
    }

    // Theme-based colors
    val screenBg = when {
        isKingdomTheme -> Color.Transparent
        isSpaceTheme -> Color(0xFF070B1E)
        isDark -> Color(0xFF0F172A)
        else -> Color(0xFFF8FAFC)
    }

    val headerBg = when {
        isKingdomTheme -> Color(0xFF08122B).copy(alpha = 0.95f)
        isSpaceTheme -> Color(0xFF0F172A).copy(alpha = 0.95f)
        isDark -> Color(0xFF1E293B)
        else -> Color.White
    }

    val headerBorder = when {
        isKingdomTheme -> BorderStroke(1.2.dp, kingdomGoldBrush)
        isSpaceTheme -> BorderStroke(1.2.dp, Color(0xFF00E5FF).copy(alpha = 0.6f))
        else -> BorderStroke(1.dp, appColors.border)
    }

    val botBubbleColor = when {
        isKingdomTheme -> Color(0xFF0F1E3D)
        isSpaceTheme -> Color(0xFF1A1F3D)
        isDark -> Color(0xFF1E293B)
        else -> Color.White
    }

    val userBubbleColor = when {
        isKingdomTheme -> Color(0xFFD4AF37)
        isSpaceTheme -> Color(0xFF00E5FF)
        else -> appColors.primary
    }

    val userTextColor = when {
        isKingdomTheme -> Color(0xFF0B1120)
        isSpaceTheme -> Color(0xFF070B1E)
        else -> Color.White
    }

    val botTextColor = when {
        isKingdomTheme -> Color(0xFFFFF7D6)
        else -> appColors.text
    }

    val isPremiumUser = remember(cosmeticsRevision) { UserProfileManager.isPremium(context) }
    var remainingQuota by remember { mutableIntStateOf(ChatRateLimiter.getRemainingQuota(context, isPremiumUser)) }

    // Send logic with Rate Limiting (Cooldown & Daily Quota)
    fun sendMessage(textToSend: String) {
        val trimmed = textToSend.trim()
        if (trimmed.isBlank()) return

        // 1. Anti-Spam / Cooldown & Günlük Kota Kontrolü
        val check = ChatRateLimiter.checkCanSend(context, isPremiumUser)
        if (!check.first) {
            android.widget.Toast.makeText(context, check.second, android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        ChatRateLimiter.onMessageSent(context)
        remainingQuota = ChatRateLimiter.getRemainingQuota(context, isPremiumUser)

        messages.add(ChatMessage(text = trimmed, isFromUser = true))
        inputText = ""
        keyboardController?.hide()

        coroutineScope.launch {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)

            // Show typing indicator
            isTyping = true

            // Live AI call via Gemini
            val aiText = GeminiService.getGeminiResponse(messages.toList())

            isTyping = false
            messages.add(ChatMessage(text = aiText, isFromUser = false))
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Quick Roleplay Prompts (No Emojis)
    val starterPrompts = listOf(
        "Kahve Siparişi" to "Can I order a cup of coffee please?",
        "Havalimanı" to "Where is gate 4 for the flight to London?",
        "Günlük Sohbet" to "How was your day, Robot Mew?",
        "Soru Sor" to "Can you explain the difference between 'make' and 'do'?",
        "Bilim ve Teknoloji" to "What do you think about artificial intelligence and space travel?",
        "Felsefe ve Yaşam" to "What is the secret to staying motivated every day?"
    )

    Scaffold(
        containerColor = if (isSpaceTheme || isKingdomTheme) Color.Transparent else appColors.background,
            topBar = {
                Surface(
                    color = headerBg,
                    border = headerBorder,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else appColors.text
                            )
                        }

                        // Robot Mew Avatar with Royal Crown & Online Glow
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isKingdomTheme) Color(0xFF132247) else Color(0xFFEDE9FE))
                                .border(
                                    1.5.dp,
                                    if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else Color(0xFF6366F1),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.robot_mew),
                                contentDescription = "Robot Mew",
                                modifier = Modifier.size(42.dp),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Robot Mew",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else appColors.text
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                            }
                            Text(
                                text = "Akıllı Asistan • Kalan Kota: $remainingQuota",
                                fontSize = 11.sp,
                                color = if (isKingdomTheme) Color(0xFFD4AF37) else appColors.textMuted
                            )
                        }

                        // Premium Badge (No Emojis)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isKingdomTheme) Color(0xFFD4AF37).copy(alpha = 0.2f) else Color(0xFF9C27B0).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "AI Asistan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isKingdomTheme) Color(0xFFFFD700) else Color(0xFF9C27B0),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Quick Roleplay Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(starterPrompts) { (label, promptText) ->
                        Surface(
                            onClick = { sendMessage(promptText) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark || isKingdomTheme || isSpaceTheme) Color(0xFF1E293B).copy(alpha = 0.85f) else Color(0xFFEDE9FE),
                            border = BorderStroke(
                                1.dp,
                                if (isKingdomTheme) Color(0xFFFFD700).copy(alpha = 0.4f) else if (isSpaceTheme) Color(0xFF00E5FF).copy(alpha = 0.4f) else Color(0xFF6366F1).copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else Color(0xFF6366F1),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = if (isKingdomTheme) Color(0xFFFFD700).copy(alpha = 0.25f) else appColors.border.copy(alpha = 0.5f)
                )

                // Message List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            isKingdomTheme = isKingdomTheme,
                            isSpaceTheme = isSpaceTheme,
                            botBubbleColor = botBubbleColor,
                            userBubbleColor = userBubbleColor,
                            botTextColor = botTextColor,
                            userTextColor = userTextColor,
                            appColors = appColors,
                            onSpeakClick = { speakText(msg.text) }
                        )
                    }

                    if (isTyping) {
                        item {
                            TypingIndicator(
                                botBubbleColor = botBubbleColor,
                                botTextColor = botTextColor
                            )
                        }
                    }
                }

                // Input Bar
                Surface(
                    color = headerBg,
                    border = headerBorder,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Dilediğin dilde veya konuda bir şey yaz...",
                                    fontSize = 13.sp,
                                    color = appColors.textMuted
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp, max = 100.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else appColors.primary,
                                unfocusedBorderColor = appColors.border.copy(alpha = 0.5f),
                                focusedTextColor = botTextColor,
                                unfocusedTextColor = botTextColor
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FloatingActionButton(
                            onClick = { sendMessage(inputText) },
                            containerColor = if (isKingdomTheme) Color(0xFFD4AF37) else if (isSpaceTheme) Color(0xFF00E5FF) else appColors.primary,
                            contentColor = userTextColor,
                            shape = CircleShape,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gönder",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    isKingdomTheme: Boolean,
    isSpaceTheme: Boolean,
    botBubbleColor: Color,
    userBubbleColor: Color,
    botTextColor: Color,
    userTextColor: Color,
    appColors: AppThemeColors,
    onSpeakClick: () -> Unit
) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.robot_mew),
                contentDescription = null,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .border(
                        1.dp,
                        if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else Color(0xFF6366F1),
                        CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) userBubbleColor else botBubbleColor,
                border = if (!isUser && isKingdomTheme) BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f))
                else if (!isUser && isSpaceTheme) BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.35f))
                else if (!isUser) BorderStroke(1.dp, appColors.border.copy(alpha = 0.5f)) else null,
                shadowElevation = 1.5.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp,
                        color = if (isUser) userTextColor else botTextColor,
                        fontWeight = FontWeight.Medium
                    )

                    // Audio speaker button for Robot Mew's messages
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSpeakClick() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Seslendir",
                                tint = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Dinle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color(0xFF00E5FF) else Color(0xFF6366F1)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TypingIndicator(botBubbleColor: Color, botTextColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 40.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = botBubbleColor,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Robot Mew yazıyor...",
                    fontSize = 11.sp,
                    color = botTextColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



