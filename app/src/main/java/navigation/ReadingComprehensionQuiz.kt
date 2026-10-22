package com.example.ngilizcequizapp

import android.content.Context
import navigation.MarketBoostStore
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

data class ReadingQuizQuestion(val prompt: String, val correctAnswer: String, val options: List<String>)

object ReadingProgressStore {
    private const val PREFS = "reading_quiz_rewards"
    private fun key(title: String) = "completed_${title.hashCode()}"

    fun isCompleted(context: Context, title: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key(title), false)

    fun markStarted(context: Context, title: String) {
        if (!isCompleted(context, title)) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong("${key(title)}_started", System.currentTimeMillis()).apply()
        }
    }

    fun lastStartedAt(context: Context, title: String): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong("${key(title)}_started", 0L)

    fun complete(context: Context, title: String, correct: Int, total: Int): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val wasCompleted = prefs.getBoolean(key(title), false)
        
        val perfect = correct == total
        val isCurrentlyPerfect = prefs.getBoolean("${key(title)}_perfect", false)
        
        val editor = prefs.edit()
        if (perfect && !isCurrentlyPerfect) {
            editor.putBoolean("${key(title)}_perfect", true)
        }
        
        if (wasCompleted) {
            editor.apply()
            return 0 to 0
        }
        
        val xpMul = MarketBoostStore.getXpMultiplier(context)
        val coinMul = MarketBoostStore.getCoinMultiplier(context)
        val xp = correct * 5 * xpMul
        val coins = correct * 10 * coinMul
        QuizTreeProgressStore.addXp(context, xp)
        QuizTreeProgressStore.addCoins(context, coins)
        
        editor.putBoolean(key(title), true)
            .putInt("${key(title)}_xp", xp)
            .putInt("${key(title)}_coins", coins)
            .apply()
            
        MissionManager.onReadingCompleted(context)
        return xp to coins
    }

    fun isPerfect(context: Context, title: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("${key(title)}_perfect", false)

    fun earnedXp(context: Context, title: String): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("${key(title)}_xp", 0)

    fun earnedCoins(context: Context, title: String): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("${key(title)}_coins", 0)
}

fun normalizeAndShuffleQuestion(raw: ReadingQuizQuestion): ReadingQuizQuestion {
    val cleanPrompt = raw.prompt.trim()
    val cleanCorrect = raw.correctAnswer.trim()
    val cleanOptions = raw.options
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(java.util.Locale.ROOT) }
        .toMutableList()

    if (cleanOptions.none { it.equals(cleanCorrect, ignoreCase = true) }) {
        cleanOptions.add(cleanCorrect)
    }

    val shuffled = cleanOptions.shuffled()
    val matchedCorrect = shuffled.firstOrNull { it.equals(cleanCorrect, ignoreCase = true) } ?: cleanCorrect

    return ReadingQuizQuestion(
        prompt = cleanPrompt,
        correctAnswer = matchedCorrect,
        options = shuffled
    )
}

@Composable
fun ReadingComprehensionQuiz(title: String, storyText: String, ttsHelper: SpeechAssistant, onClose: () -> Unit) {
    val context = LocalContext.current
    val questions = remember(title, storyText) {
        buildReadingQuestions(title, storyText).map { normalizeAndShuffleQuestion(it) }
    }
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var checked by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var earnedXp by remember { mutableIntStateOf(0) }
    var earnedCoins by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    val question = questions[index]
    val isCorrect = selected?.trim()?.equals(question.correctAnswer.trim(), ignoreCase = true) == true
    val solvedQuestions = (index + if (checked) 1 else 0).coerceIn(0, questions.size)
    val quizProgress = if (questions.isNotEmpty()) solvedQuestions.toFloat() / questions.size else 0f
    val animatedQuizProgress by animateFloatAsState(
        targetValue = quizProgress,
        animationSpec = tween(durationMillis = 650),
        label = "readingQuizProgress"
    )

    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val isSpaceTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }
    val isDark = LocalIsDarkMode.current

    val appColors = LocalAppThemeColors.current
    val accentBlue = when {
        isKingdomTheme -> Color(0xFFFFD700)
        isSpaceTheme -> Color(0xFF8B5CF6)
        else -> appColors.primary
    }
    val navy = when {
        isKingdomTheme -> Color(0xFFFFF7D6)
        isSpaceTheme -> Color(0xFFF8FAFC)
        else -> appColors.text
    }
    val muted = when {
        isKingdomTheme -> Color(0xFFD4AF37)
        isSpaceTheme -> Color(0xFF94A3B8)
        else -> appColors.textMuted
    }
    val paleBlue = when {
        isKingdomTheme -> Color(0xFFFFD700).copy(alpha = 0.15f)
        isSpaceTheme -> Color(0xFF8B5CF6).copy(alpha = 0.20f)
        else -> appColors.primarySoft
    }
    val screenBackground = when {
        isKingdomTheme -> Color(0xFF070F26)
        isSpaceTheme -> Color(0xFF0C132C)
        else -> appColors.background
    }
    val cardColor = when {
        isKingdomTheme -> Color(0xFF0F1A3A)
        isSpaceTheme -> Color(0xFF131B3B)
        else -> appColors.surface
    }
    val themeBorder = when {
        isKingdomTheme -> Color(0xFFFFD700).copy(alpha = 0.35f)
        isSpaceTheme -> Color(0xFF8B5CF6).copy(alpha = 0.35f)
        else -> appColors.border
    }

    val quizCatName = remember(title) {
        if (kotlin.random.Random.nextBoolean()) "readingcat2" else "readingcat3"
    }
    val quizCatImage = readingQuizDrawableOrFallback(quizCatName, R.drawable.readingcat)

    BackHandler(enabled = !finished) { confirmExit = true }

    Scaffold(
        containerColor = screenBackground,
        topBar = {
            Surface(
                color = cardColor,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, themeBorder.copy(alpha = 0.2f))
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { if (finished) onClose() else confirmExit = true }) {
                            Icon(Icons.Default.Close, "Kapat", tint = navy)
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(paleBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(quizCatImage),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(title, fontWeight = FontWeight.Black, color = navy, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 15.sp)
                            Text("Okuduğunu anlama", fontSize = 11.sp, color = muted, maxLines = 1)
                        }
                        Spacer(Modifier.width(6.dp))
                        CartoonEnergyBadge()
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { ttsHelper.speakEnglish(storyText) }, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Default.Headphones, "Metni dinle", tint = accentBlue)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedQuizProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = accentBlue,
                            trackColor = paleBlue
                        )
                        Text(
                            "$solvedQuestions/${questions.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = navy
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (!finished) Surface(
                color = cardColor,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, themeBorder.copy(alpha = 0.2f))
            ) {
                Button(
                    onClick = {
                        if (!checked) {
                            checked = true
                            if (isCorrect) correctCount++
                        } else if (index < questions.lastIndex) {
                            index++
                            selected = null
                            checked = false
                        } else {
                            val reward = ReadingProgressStore.complete(context, title, correctCount, questions.size)
                            earnedXp = reward.first
                            earnedCoins = reward.second
                            finished = true
                        }
                    },
                    enabled = selected != null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentBlue,
                        contentColor = if (isKingdomTheme) Color(0xFF070F26) else Color.White
                    )
                ) {
                    Text(
                        if (!checked) "Kontrol Et" else if (index == questions.lastIndex) "Sonucu Gör" else "Sonraki Soru",
                        fontWeight = FontWeight.Bold,
                        color = if (isKingdomTheme) Color(0xFF070F26) else Color.White
                    )
                }
            }
        }
    ) { padding ->
        if (finished) {
            ReadingQuizResult(Modifier.padding(padding), correctCount, questions.size, earnedXp, earnedCoins, onClose)
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(screenBackground),
                contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 110.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = cardColor,
                        shadowElevation = 5.dp,
                        border = BorderStroke(1.dp, themeBorder)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Reading Quiz", color = accentBlue, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                Text(title, color = navy, fontSize = 22.sp, lineHeight = 27.sp, fontWeight = FontWeight.Black)
                                Spacer(Modifier.height(8.dp))
                                Text("$solvedQuestions / ${questions.size} soru çözüldü", color = muted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Image(
                                painter = painterResource(quizCatImage),
                                contentDescription = null,
                                modifier = Modifier.size(82.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(22.dp), color = cardColor, border = BorderStroke(1.dp, themeBorder)) {
                        Text(storyText, Modifier.padding(18.dp), color = navy, lineHeight = 25.sp)
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(22.dp), color = cardColor, shadowElevation = 4.dp, border = BorderStroke(1.dp, themeBorder)) {
                        Column(Modifier.padding(18.dp)) {
                            Text("${index + 1} / ${questions.size}", color = accentBlue, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(question.prompt, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Black, color = navy)
                        }
                    }
                }
                question.options.forEach { option ->
                    item {
                        val chosen = selected?.trim()?.equals(option.trim(), ignoreCase = true) == true
                        val isOptionCorrect = option.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                        val color = when {
                            checked && isOptionCorrect -> if (isKingdomTheme) Color(0xFF163824) else if (isSpaceTheme || isDark) Color(0xFF1E3A2B) else Color(0xFFDDF4E8)
                            checked && chosen -> if (isKingdomTheme) Color(0xFF451A1A) else if (isSpaceTheme || isDark) Color(0xFF3E1F1F) else Color(0xFFFFE4E4)
                            chosen -> if (isKingdomTheme) Color(0xFF1F2B48) else if (isSpaceTheme || isDark) cardColor.copy(alpha = 0.9f) else Color(0xFFE0F3FD)
                            else -> cardColor
                        }
                        val optionTextColor = when {
                            checked && isOptionCorrect -> if (isKingdomTheme || isSpaceTheme || isDark) Color(0xFF86EFAC) else Color(0xFF15803D)
                            checked && chosen -> if (isKingdomTheme || isSpaceTheme || isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C)
                            chosen -> accentBlue
                            else -> navy
                        }
                        Surface(
                            Modifier.fillMaxWidth().clickable(enabled = !checked) { selected = option },
                            shape = RoundedCornerShape(18.dp),
                            color = color,
                            shadowElevation = if (chosen) 3.dp else 1.dp,
                            border = BorderStroke(
                                width = if (chosen) 2.dp else 1.dp,
                                color = when {
                                    checked && isOptionCorrect -> Color(0xFF22C55E)
                                    checked && chosen -> Color(0xFFEF4444)
                                    chosen -> accentBlue
                                    else -> themeBorder
                                }
                            )
                        ) {
                            Text(
                                option,
                                Modifier.padding(16.dp),
                                fontWeight = FontWeight.SemiBold,
                                color = optionTextColor
                            )
                        }
                    }
                }
                if (checked) item {
                    if (isCorrect) ReadingQuestionReward() else Text("Doğru cevap: ${question.correctAnswer}", color = Color(0xFFE55353), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (confirmExit) ReadingExitDialog(
        onStay = { confirmExit = false },
        onExit = { confirmExit = false; onClose() }
    )
}

@Composable
private fun ReadingQuestionReward() {
    val coin by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coin))
    val xp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.badge))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        RewardAnimation(coin, "+10 coin", Color(0xFFE5A400), 64)
        RewardAnimation(xp, "+5 XP", Color(0xFF2781B8), 64)
    }
}

@Composable
private fun RewardAnimation(composition: com.airbnb.lottie.LottieComposition?, text: String, color: Color, size: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        LottieAnimation(composition, iterations = 1, modifier = Modifier.size(size.dp))
        Text(text, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun ReadingQuizProgressPie(
    progress: Float,
    solved: Int,
    total: Int,
    accentColor: Color,
    trackColor: Color,
    textColor: Color
) {
    Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = accentColor,
            trackColor = trackColor,
            strokeWidth = 7.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Text("$solved/$total", color = textColor.copy(alpha = .65f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun readingQuizDrawableOrFallback(name: String, fallback: Int): Int {
    val context = LocalContext.current
    return remember(name, fallback) {
        context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 } ?: fallback
    }
}

@Composable
private fun ReadingQuizResult(modifier: Modifier, correct: Int, total: Int, xpAmount: Int, coinAmount: Int, onDone: () -> Unit) {
    val context = LocalContext.current
    val celebrationRes = remember {
        listOf("businessman_flies_up_with_rocket", "champion", "trophy")
            .map { context.resources.getIdentifier(it, "raw", context.packageName) }
            .filter { it != 0 }.randomOrNull() ?: R.raw.premium
    }
    val celebration by rememberLottieComposition(LottieCompositionSpec.RawRes(celebrationRes))
    val coin by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coin))
    val xp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.badge))
    val appColors = LocalAppThemeColors.current
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { LottieAnimation(celebration, iterations = 1, modifier = Modifier.size(220.dp)) }
        item {
            Text("Reading tamamlandı", fontSize = 28.sp, fontWeight = FontWeight.Black, color = appColors.text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("$correct doğru · ${total - correct} yanlış", color = appColors.textMuted, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(22.dp))
            val xpMul = MarketBoostStore.getXpMultiplier(context)
            val coinMul = MarketBoostStore.getCoinMultiplier(context)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RewardAnimation(coin, "+$coinAmount coin", Color(0xFFE5A400), 90)
                    if (coinMul > 1 && coinAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFB300).copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color(0xFFFFB300)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "2X BOOST",
                                color = Color(0xFFFFB300),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    RewardAnimation(xp, "+$xpAmount XP", Color(0xFF2781B8), 90)
                    if (xpMul > 1 && xpAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF8B5CF6).copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color(0xFF8B5CF6)),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "2X BOOST",
                                color = Color(0xFF8B5CF6),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(8.dp)) { Text("TAMAM", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ReadingExitDialog(onStay: () -> Unit, onExit: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    val context = LocalContext.current
    val failRes = remember { context.resources.getIdentifier("fail", "raw", context.packageName).takeIf { it != 0 } ?: R.raw.cat_fishing_on_moon }
    val fail by rememberLottieComposition(LottieCompositionSpec.RawRes(failRes))
    AlertDialog(
        onDismissRequest = onStay,
        containerColor = appColors.surface,
        icon = { LottieAnimation(fail, iterations = 1, modifier = Modifier.size(130.dp)) },
        title = { Text("Reading'den ayrılmak istiyor musun?", textAlign = TextAlign.Center, color = appColors.text) },
        text = { Text("Şimdi ayrılırsan bu turdaki coin ve XP ödülleri hesabına eklenmeyecek. Daha sonra yeniden deneyebilirsin.", textAlign = TextAlign.Center, color = appColors.textMuted) },
        confirmButton = { TextButton(onClick = onExit) { Text("AYRIL", color = Color(0xFFD04C4C)) } },
        dismissButton = { TextButton(onClick = onStay) { Text("DEVAM ET", color = appColors.primary) } }
    )
}

private fun buildReadingQuestions(title: String, text: String): List<ReadingQuizQuestion> {
    ReadingDataRepository.getQuestionsFor(title)?.let { return it }
    val sentences = sentences(text)
    val words = significantWords(text)
    val word = words.firstOrNull() ?: "routine"
    val ending = sentences.lastOrNull() ?: text.trim()
    return listOf(
        ReadingQuizQuestion(
            prompt = "Okuduğunuz metnin ana fikri nedir?",
            correctAnswer = title,
            options = listOf(title, "Günlük Yaşam ve Alışkanlıklar", "Şehir ve Çevre Gözlemleri", "Gelecek Planları ve Hedefler")
        ),
        ReadingQuizQuestion(
            prompt = "Metinde geçen önemli kavramlardan biri hangisidir?",
            correctAnswer = word,
            options = listOf(word, "architecture", "meteorology", "archaeology")
        ),
        ReadingQuizQuestion(
            prompt = "Metin aşağıdaki düşüncelerden hangisiyle tamamlanmaktadır?",
            correctAnswer = ending,
            options = listOf(
                ending,
                "Everything was ready for the upcoming week.",
                "The journey ended much earlier than expected.",
                "They decided to return home immediately."
            )
        )
    )
}

private fun significantWords(text: String): List<String> {
    val stops = setOf("about", "after", "again", "because", "every", "there", "their", "these", "today", "very", "where", "which", "with", "want", "have", "this", "that", "from", "they")
    return Regex("[A-Za-z]{5,}").findAll(text).map { it.value.lowercase() }.filterNot { it in stops }.distinct().sortedByDescending { it.length }.toList()
}

private fun sentences(text: String) = text.trim().split(Regex("(?<=[.!?])\\s+")).map(String::trim).filter(String::isNotBlank)
