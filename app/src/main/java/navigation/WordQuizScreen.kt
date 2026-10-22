package com.example.ngilizcequizapp

import android.content.Context
import navigation.MarketBoostStore
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import kotlinx.coroutines.launch
import kotlin.math.abs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WordSwipeLimitManager {
    private const val PREFS_NAME = "word_swipe_quiz_prefs"
    private const val KEY_LAST_DATE = "last_word_swipe_date"

    private fun getTodayDateStr(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun hasPlayedToday(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_DATE, "") == getTodayDateStr()
    }

    fun canPlay(context: Context): Boolean {
        if (com.example.ngilizcequizapp.UserProfileManager.isPremium(context)) {
            return true
        }
        return !hasPlayedToday(context)
    }

    fun recordPlay(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LAST_DATE, getTodayDateStr()).apply()
    }
}

enum class QuizScreenState {
    OVERVIEW,
    SWIPING,
    EMPTY_WARNING,
    FINISHED
}

data class WordSwipeQuizQuestion(
    val word: SavedWord,
    val optionLeft: String,
    val optionRight: String,
    val isLeftCorrect: Boolean,
    val pos: WordPOS
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordQuizScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    LaunchedEffect(Unit) {
        WordStorage.loadFromDisk(context)
    }

    val savedWords = WordStorage.savedWords

    var screenState by remember { mutableStateOf(QuizScreenState.OVERVIEW) }
    var selectedCategoryTab by remember { mutableStateOf("Kaydedilmiş") }

    // Dialog state for selecting quiz categories
    var showStartDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    val isPremium = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
        com.example.ngilizcequizapp.UserProfileManager.isPremium(context)
    }
    val hasPlayedToday = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
        WordSwipeLimitManager.hasPlayedToday(context)
    }
    var selKaydedilmis by remember { mutableStateOf(true) }
    var selTekrarEdilmis by remember { mutableStateOf(true) }
    var selPekistirilmis by remember { mutableStateOf(true) }
    var selOgrenilmis by remember { mutableStateOf(false) }

    // Active Quiz State
    var activeQuestions by remember { mutableStateOf<List<WordSwipeQuizQuestion>>(emptyList()) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var totalCoinsEarned by remember { mutableIntStateOf(0) }
    var usedCategoriesForQuiz by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Gradient background for the whole screen
    val mainGradient = Brush.verticalGradient(
        colors = if (isDark) listOf(
            appColors.background,
            appColors.backgroundAlt,
            appColors.surfaceRaised
        ) else listOf(
            Color(0xFFE3F2FD),
            Color(0xFFBBDEFB),
            Color(0xFF90CAF9)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(mainGradient)
    ) {
        when (screenState) {
            QuizScreenState.OVERVIEW -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(48.dp))

                    // Top Bar with Back Icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .background(appColors.surface, CircleShape)
                                .size(40.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = appColors.text)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Kelime Quizi",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = appColors.text
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Category Statistics Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf("Kaydedilmiş", "Tekrar Edilmiş", "Pekiştirilmiş", "Öğrenilmiş")
                        val colors = listOf(
                            Color(0xFFFFB74D), // Amber
                            Color(0xFF4FC3F7), // Light Blue
                            Color(0xFF81C784), // Light Green
                            Color(0xFFBA68C8)  // Purple
                        )

                        categories.forEachIndexed { index, cat ->
                            val count = savedWords.count { it.currentStatus.equals(cat, ignoreCase = true) }
                            val isSelected = selectedCategoryTab.equals(cat, ignoreCase = true)

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategoryTab = cat },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) appColors.surface else appColors.surface.copy(alpha = 0.65f)
                                ),
                                border = if (isSelected) BorderStroke(2.dp, colors[index]) else BorderStroke(1.dp, appColors.border),
                                elevation = CardDefaults.cardElevation(if (isSelected) 6.dp else 1.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(colors[index], CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = cat,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textMuted,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$count",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colors[index]
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 👑 Freemium / Premium Quiz Kısıt Rozeti
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPremium) Color(0xFFFFD700).copy(alpha = 0.15f) else if (hasPlayedToday) Color(0xFFEF4444).copy(alpha = 0.12f) else Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, if (isPremium) Color(0xFFFFD700) else if (hasPlayedToday) Color(0xFFEF4444).copy(alpha = 0.4f) else Color(0xFF10B981).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = if (isPremium) "👑 Premium: Sınırsız Kelime Quizi" else if (hasPlayedToday) "🔒 Günlük Hak: 1/1 (Bugün Kullanıldı)" else "⚡ Freemium: Günde 1 Quiz Hakkı (Hazır)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPremium) (if (isDark) Color(0xFFFFDF7A) else Color(0xFFB45309)) else if (hasPlayedToday) Color(0xFFEF4444) else Color(0xFF10B981)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Start Quiz Button
                    Button(
                        onClick = {
                            if (WordSwipeLimitManager.canPlay(context)) {
                                showStartDialog = true
                            } else {
                                showLimitDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isPremium && hasPlayedToday) Color(0xFF64748B) else Color(0xFF1E88E5),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Icon(
                            if (!isPremium && hasPlayedToday) Icons.Default.Lock else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (!isPremium && hasPlayedToday) "Günlük Limit Doldu (Sınırsız Aç ✨)" else "Quizi Başlat",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "$selectedCategoryTab Kelimeleri (${savedWords.count { it.currentStatus.equals(selectedCategoryTab, ignoreCase = true) }})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.text
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Words list under selected category tab
                    val tabWords = savedWords.filter { it.currentStatus.equals(selectedCategoryTab, ignoreCase = true) }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 30.dp)
                    ) {
                        if (tabWords.isEmpty()) {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = appColors.surface,
                                    border = BorderStroke(1.dp, appColors.border)
                                ) {
                                    Text(
                                        text = "Bu kategoride henüz kelime bulunmuyor.",
                                        color = appColors.textMuted,
                                        modifier = Modifier.padding(20.dp),
                                        textAlign = TextAlign.Center,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            items(tabWords) { item ->
                                val pos = remember(item.word, item.meaning) {
                                    WordPOSHelper.detectPOS(item.word, item.meaning)
                                }
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = appColors.surface,
                                    border = BorderStroke(1.dp, appColors.border),
                                    shadowElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = item.word.replaceFirstChar { it.uppercase() },
                                                    fontSize = 17.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = appColors.text
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE)
                                                ) {
                                                    Text(
                                                        text = pos.displayName,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF0369A1),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.meaning,
                                                fontSize = 14.sp,
                                                color = appColors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Selection Dialog
                if (showStartDialog) {
                    AlertDialog(
                        onDismissRequest = { showStartDialog = false },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = appColors.surface,
                        title = {
                            Text(
                                text = "Quiz Kategori Seçimi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = appColors.primary
                            )
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Quizde hangi kelime türlerinin sorulmasını istersin?",
                                    fontSize = 14.sp,
                                    color = appColors.textMuted
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val opts = listOf(
                                    "Kaydedilmiş" to selKaydedilmis,
                                    "Tekrar Edilmiş" to selTekrarEdilmis,
                                    "Pekiştirilmiş" to selPekistirilmis,
                                    "Öğrenilmiş" to selOgrenilmis
                                )

                                opts.forEach { (name, checked) ->
                                    val wordCount = savedWords.count { it.currentStatus.equals(name, ignoreCase = true) }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                when (name) {
                                                    "Kaydedilmiş" -> selKaydedilmis = !selKaydedilmis
                                                    "Tekrar Edilmiş" -> selTekrarEdilmis = !selTekrarEdilmis
                                                    "Pekiştirilmiş" -> selPekistirilmis = !selPekistirilmis
                                                    "Öğrenilmiş" -> selOgrenilmis = !selOgrenilmis
                                                }
                                            }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { isChecked ->
                                                when (name) {
                                                    "Kaydedilmiş" -> selKaydedilmis = isChecked
                                                    "Tekrar Edilmiş" -> selTekrarEdilmis = isChecked
                                                    "Pekiştirilmiş" -> selPekistirilmis = isChecked
                                                    "Öğrenilmiş" -> selOgrenilmis = isChecked
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1E88E5))
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$name ($wordCount)",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = appColors.text
                                        )
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showStartDialog = false
                                    val selectedCats = mutableSetOf<String>()
                                    if (selKaydedilmis) selectedCats.add("Kaydedilmiş")
                                    if (selTekrarEdilmis) selectedCats.add("Tekrar Edilmiş")
                                    if (selPekistirilmis) selectedCats.add("Pekiştirilmiş")
                                    if (selOgrenilmis) selectedCats.add("Öğrenilmiş")

                                    val pool = savedWords.filter { w ->
                                        selectedCats.any { cat -> cat.equals(w.currentStatus, ignoreCase = true) }
                                    }

                                    if (pool.size < 2) {
                                        screenState = QuizScreenState.EMPTY_WARNING
                                    } else {
                                        WordSwipeLimitManager.recordPlay(context)
                                        usedCategoriesForQuiz = selectedCats
                                        activeQuestions = prepareQuizQuestions(pool, savedWords)
                                        currentQuestionIndex = 0
                                        correctCount = 0
                                        screenState = QuizScreenState.SWIPING
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                            ) {
                                Text("Başlat", fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showStartDialog = false }) {
                                Text("Vazgeç", color = appColors.textMuted)
                            }
                        }
                    )
                }

                // 💎 Freemium Günlük Limit Dialog
                if (showLimitDialog) {
                    AlertDialog(
                        onDismissRequest = { showLimitDialog = false },
                        shape = RoundedCornerShape(24.dp),
                        containerColor = appColors.surface,
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🔒 Günlük Limit Ulaşıldı", fontWeight = FontWeight.Black, fontSize = 18.sp, color = appColors.text)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Kelime kaydırma quizi Freemium planda günde 1 kez ile sınırlıdır.",
                                    fontSize = 14.sp,
                                    color = appColors.text
                                )
                                Text(
                                    text = if (AppConfig.IS_MARKET_ENABLED) {
                                        "Bugünkü hakkını kullandın. Sınırsız pratik yapmak ve tüm özellikleri açmak için Premium'a geçebilirsin!"
                                    } else {
                                        "Bugünkü hakkını kullandın. Yarın yeni kelimelerle pratik yapmaya devam edebilirsin!"
                                    },
                                    fontSize = 13.sp,
                                    color = appColors.textMuted
                                )
                            }
                        },
                        confirmButton = {
                            if (AppConfig.IS_MARKET_ENABLED) {
                                Button(
                                    onClick = {
                                        showLimitDialog = false
                                        navController.navigate("premium_screen")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Text("Premium'a Geç ✨", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        showLimitDialog = false
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                                ) {
                                    Text("Anladım", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLimitDialog = false }) {
                                Text("Kapat", color = appColors.textMuted)
                            }
                        }
                    )
                }
            }

            QuizScreenState.EMPTY_WARNING -> {
                EmptyWordsWarningView(
                    onBack = { screenState = QuizScreenState.OVERVIEW },
                    onGoToWords = {
                        screenState = QuizScreenState.OVERVIEW
                        navController.navigate("words_screen")
                    }
                )
            }

            QuizScreenState.SWIPING -> {
                if (currentQuestionIndex < activeQuestions.size) {
                    val q = activeQuestions[currentQuestionIndex]
                    SwipeCardQuizView(
                        question = q,
                        questionIndex = currentQuestionIndex,
                        totalQuestions = activeQuestions.size,
                        onAnswered = { isCorrect ->
                            if (isCorrect) {
                                correctCount++
                                updateWordStatusOnCorrect(context, q.word)
                            }
                            if (currentQuestionIndex + 1 < activeQuestions.size) {
                                currentQuestionIndex++
                            } else {
                                // Finished Quiz Round
                                val baseCoins = calculateQuizCoins(usedCategoriesForQuiz)
                                val coinMul = MarketBoostStore.getCoinMultiplier(context)
                                totalCoinsEarned = baseCoins * coinMul
                                if (totalCoinsEarned > 0) {
                                    QuizTreeProgressStore.addCoins(context, totalCoinsEarned)
                                }
                                screenState = QuizScreenState.FINISHED
                            }
                        },
                        onQuit = { screenState = QuizScreenState.OVERVIEW }
                    )
                } else {
                    screenState = QuizScreenState.FINISHED
                }
            }

            QuizScreenState.FINISHED -> {
                QuizFinishedView(
                    totalQuestions = activeQuestions.size,
                    correctCount = correctCount,
                    earnedCoins = totalCoinsEarned,
                    onDone = { screenState = QuizScreenState.OVERVIEW }
                )
            }
        }
    }
}

private fun prepareQuizQuestions(pool: List<SavedWord>, allSavedWords: List<SavedWord>): List<WordSwipeQuizQuestion> {
    val shuffledPool = pool.shuffled()
    return shuffledPool.map { word ->
        val pos = WordPOSHelper.detectPOS(word.word, word.meaning)
        val distractor = WordPOSHelper.getDistractorForPOS(word.word, word.meaning, pos, allSavedWords)
        val isLeftCorrect = (0..1).random() == 0

        val left = if (isLeftCorrect) word.meaning else distractor
        val right = if (isLeftCorrect) distractor else word.meaning

        WordSwipeQuizQuestion(
            word = word,
            optionLeft = left,
            optionRight = right,
            isLeftCorrect = isLeftCorrect,
            pos = pos
        )
    }
}

private fun updateWordStatusOnCorrect(context: Context, targetWord: SavedWord) {
    val found = WordStorage.savedWords.firstOrNull { it.word.equals(targetWord.word, ignoreCase = true) }
    if (found != null) {
        val nextStatus = when (found.currentStatus) {
            "Kaydedilmiş" -> "Tekrar Edilmiş"
            "Tekrar Edilmiş" -> "Pekiştirilmiş"
            "Pekiştirilmiş" -> "Öğrenilmiş"
            else -> "Öğrenilmiş"
        }
        found.currentStatus = nextStatus
        WordStorage.saveToDisk(context)
    }
}

private fun calculateQuizCoins(usedCategories: Set<String>): Int {
    val hasKaydedilmis = usedCategories.any { it.equals("Kaydedilmiş", ignoreCase = true) }
    val hasTekrar = usedCategories.any { it.equals("Tekrar Edilmiş", ignoreCase = true) }
    val hasPekistirilmis = usedCategories.any { it.equals("Pekiştirilmiş", ignoreCase = true) }
    val hasOgrenilmisOnly = usedCategories.size == 1 && usedCategories.contains("Öğrenilmiş")

    return if (hasOgrenilmisOnly) {
        0
    } else {
        var coins = 0
        if (hasKaydedilmis) coins += 10
        if (hasTekrar) coins += 10
        if (hasPekistirilmis) coins += 10
        if (coins == 0) 10 else coins
    }
}

@Composable
fun SwipeCardQuizView(
    question: WordSwipeQuizQuestion,
    questionIndex: Int,
    totalQuestions: Int,
    onAnswered: (isCorrect: Boolean) -> Unit,
    onQuit: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    val offsetX = remember(questionIndex) { Animatable(0f) }
    val cardAlpha = remember(questionIndex) { Animatable(0f) }
    var isDragging by remember(questionIndex) { mutableStateOf(false) }

    // 🌟 Yeni soru yüklendiğinde 500ms (Yarım saniye) içinde Fade-in gerçekleşir
    LaunchedEffect(questionIndex) {
        cardAlpha.snapTo(0f)
        cardAlpha.animateTo(1f, animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing))
    }

    val currentOffsetX = offsetX.value
    val swipeThreshold = 160f

    // Dynamic Ice-Blue & Glow intensity based on drag distance
    val dragAbs = abs(currentOffsetX)
    val glowAlpha = (dragAbs / 250f).coerceIn(0f, 0.7f)
    val glowColor = when {
        currentOffsetX > 40f -> Color(0xFF4CAF50).copy(alpha = glowAlpha)
        currentOffsetX < -40f -> Color(0xFF0288D1).copy(alpha = glowAlpha)
        else -> if (isDark) appColors.primary.copy(alpha = (dragAbs / 400f).coerceIn(0f, 0.3f)) else Color(0xFFB0CFFF).copy(alpha = (dragAbs / 300f).coerceIn(0f, 0.4f))
    }

    // Swiping Off-Screen Helper Function
    fun triggerSwipe(isRight: Boolean, isCorrect: Boolean) {
        coroutineScope.launch {
            val targetX = if (isRight) 1400f else -1400f
            offsetX.animateTo(
                targetValue = targetX,
                animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing)
            )
            onAnswered(isCorrect)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = if (isDark) listOf(glowColor, appColors.background, appColors.backgroundAlt) else listOf(glowColor, Color(0xFFE3F2FD), Color(0xFF90CAF9)),
                    radius = 1400f
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            // Üst Header: Kapat Butonu ve İlerleme Çubuğu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .background(appColors.surface, CircleShape)
                        .size(42.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = appColors.text)
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp)
                        .height(14.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = appColors.surfaceRaised
                ) {
                    LinearProgressIndicator(
                        progress = (questionIndex + 1).toFloat() / totalQuestions.toFloat(),
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF1E88E5),
                        trackColor = Color.Transparent
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = appColors.surface,
                    border = BorderStroke(1.dp, appColors.border)
                ) {
                    Text(
                        text = "${questionIndex + 1}/$totalQuestions",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = appColors.text,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 🌟 Tinder Swipe Card (Ekran Dışına Uçma & 500ms Fade-in İçerir)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .graphicsLayer(
                        translationX = currentOffsetX,
                        rotationZ = currentOffsetX * 0.035f,
                        alpha = cardAlpha.value
                    )
                    .pointerInput(questionIndex) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                if (offsetX.value > swipeThreshold) {
                                    triggerSwipe(isRight = true, isCorrect = !question.isLeftCorrect)
                                } else if (offsetX.value < -swipeThreshold) {
                                    triggerSwipe(isRight = false, isCorrect = question.isLeftCorrect)
                                } else {
                                    coroutineScope.launch {
                                        offsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                coroutineScope.launch {
                                    offsetX.animateTo(0f, animationSpec = spring())
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.88f),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    border = BorderStroke(
                        width = if (dragAbs > 40f) 3.dp else 1.dp,
                        color = when {
                            currentOffsetX > 40f -> Color(0xFF4CAF50)
                            currentOffsetX < -40f -> Color(0xFF0288D1)
                            else -> appColors.border
                        }
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Rozetler (Kelime Türü & Durumu)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE)
                                ) {
                                    Text(
                                        text = question.pos.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0288D1),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = question.word.currentStatus,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF57C00),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // İngilizce Kelime Alanı (Merkez)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 16.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isDark) appColors.surfaceRaised else Color(0xFFE3F2FD),
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Translate,
                                            contentDescription = null,
                                            tint = Color(0xFF1E88E5),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = question.word.word.replaceFirstChar { it.uppercase() },
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = appColors.text,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Doğru karşılığı seçmek için kartı kaydır veya butona tıkla",
                                    fontSize = 12.sp,
                                    color = appColors.textMuted,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // Alt Türkçe Seçenekler (Sol & Sağ Butonları)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Sol Seçenek Kartı
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(96.dp)
                                        .clickable {
                                            triggerSwipe(isRight = false, isCorrect = question.isLeftCorrect)
                                        },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (currentOffsetX < -40f) (if (isDark) appColors.primary.copy(alpha = 0.25f) else Color(0xFFE0F2FE)) else appColors.surfaceRaised
                                    ),
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = if (currentOffsetX < -40f) Color(0xFF0288D1) else appColors.border
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.ArrowBack,
                                                contentDescription = null,
                                                tint = Color(0xFF0288D1),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "SOL",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0288D1)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = question.optionLeft,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.text,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }

                                // Sağ Seçenek Kartı
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(96.dp)
                                        .clickable {
                                            triggerSwipe(isRight = true, isCorrect = !question.isLeftCorrect)
                                        },
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (currentOffsetX > 40f) (if (isDark) Color(0xFF166534).copy(alpha = 0.35f) else Color(0xFFE8F5E9)) else appColors.surfaceRaised
                                    ),
                                    border = BorderStroke(
                                        width = 2.dp,
                                        color = if (currentOffsetX > 40f) Color(0xFF4CAF50) else appColors.border
                                    ),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "SAĞ",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF2E7D32)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = question.optionRight,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = appColors.text,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun EmptyWordsWarningView(
    onBack: () -> Unit,
    onGoToWords: () -> Unit
) {
    val appColors = LocalAppThemeColors.current
    val pandaComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.binge_panda))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = appColors.surface,
            border = BorderStroke(1.dp, appColors.border),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = pandaComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(160.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Henüz Yeterli Kelime Kaydedilmedi!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = appColors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Quiz oluşturabilmek için seçilen kategoride en az 2 kelimeye ihtiyaç var. Okuma parçalarından veya sözlükten yeni kelimeler kaydedebilirsin!",
                    fontSize = 13.sp,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onGoToWords,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kelime Kaydet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onBack) {
                    Text("Ana Ekrana Dön", color = appColors.textMuted)
                }
            }
        }
    }
}

@Composable
fun QuizFinishedView(
    totalQuestions: Int,
    correctCount: Int,
    earnedCoins: Int,
    onDone: () -> Unit
) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val coinComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coin))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = appColors.surface,
            border = BorderStroke(1.dp, appColors.border),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = coinComposition,
                    iterations = 1,
                    modifier = Modifier.size(140.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Quiz Tamamlandı!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = appColors.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Skorun: $correctCount / $totalQuestions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.text
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (earnedCoins > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDark) Color(0xFF78350F).copy(alpha = 0.35f) else Color(0xFFFFF8E1),
                        border = BorderStroke(1.dp, Color(0xFFFFB300))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            val coinMul = MarketBoostStore.getCoinMultiplier(LocalContext.current)
                            Text(
                                text = "+$earnedCoins Coin Kazandın!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
                            )
                            if (coinMul > 1) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFB300),
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Text(
                                        text = "2X",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Öğrenilmiş kelimeler pratik edildi! (Coin verilmez)",
                        fontSize = 13.sp,
                        color = appColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onDone,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Tamamla", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
