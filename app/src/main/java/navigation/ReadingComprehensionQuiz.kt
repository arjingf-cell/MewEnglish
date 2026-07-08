package com.example.ngilizcequizapp

import android.content.Context
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

private data class ReadingQuizQuestion(val prompt: String, val correctAnswer: String, val options: List<String>)

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
        
        val xp = correct * 5
        val coins = correct * 10
        QuizTreeProgressStore.addXp(context, xp)
        QuizTreeProgressStore.addCoins(context, coins)
        
        editor.putBoolean(key(title), true)
            .putInt("${key(title)}_xp", xp)
            .putInt("${key(title)}_coins", coins)
            .apply()
            
        return xp to coins
    }

    fun isPerfect(context: Context, title: String): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("${key(title)}_perfect", false)

    fun earnedXp(context: Context, title: String): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("${key(title)}_xp", 0)

    fun earnedCoins(context: Context, title: String): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt("${key(title)}_coins", 0)
}

@Composable
fun ReadingComprehensionQuiz(title: String, storyText: String, ttsHelper: SpeechAssistant, onClose: () -> Unit) {
    val context = LocalContext.current
    val questions = remember(title, storyText) { buildReadingQuestions(title, storyText) }
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var checked by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var earnedXp by remember { mutableIntStateOf(0) }
    var earnedCoins by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var confirmExit by remember { mutableStateOf(false) }
    val question = questions[index]
    val isCorrect = selected == question.correctAnswer
    val solvedQuestions = (index + if (checked) 1 else 0).coerceIn(0, questions.size)
    val quizProgress = if (questions.isNotEmpty()) solvedQuestions.toFloat() / questions.size else 0f
    val animatedQuizProgress by animateFloatAsState(
        targetValue = quizProgress,
        animationSpec = tween(durationMillis = 650),
        label = "readingQuizProgress"
    )
    val appColors = LocalAppThemeColors.current
    val accentBlue = appColors.primary
    val navy = appColors.text
    val muted = appColors.textMuted
    val paleBlue = appColors.primarySoft
    val screenBackground = appColors.background
    val cardColor = appColors.surface
    val quizCatName = remember(title) {
        if (kotlin.random.Random.nextBoolean()) "readingcat2" else "readingcat3"
    }
    val quizCatImage = readingQuizDrawableOrFallback(quizCatName, R.drawable.readingcat)

    BackHandler(enabled = !finished) { confirmExit = true }

    Scaffold(
        containerColor = screenBackground,
        topBar = {
            Surface(color = cardColor, shadowElevation = 3.dp) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().height(78.dp).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (finished) onClose() else confirmExit = true }) { Icon(Icons.Default.Close, "Kapat", tint = navy) }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(paleBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(quizCatImage),
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(title, fontWeight = FontWeight.Black, color = navy, maxLines = 1)
                        Text("Okuduğunu anlama", fontSize = 12.sp, color = muted)
                    }
                    ReadingQuizProgressPie(
                        progress = animatedQuizProgress,
                        solved = solvedQuestions,
                        total = questions.size,
                        accentColor = accentBlue,
                        trackColor = paleBlue,
                        textColor = navy
                    )
                    Spacer(Modifier.width(6.dp))
                    IconButton(onClick = { ttsHelper.speakEnglish(storyText) }) {
                        Icon(Icons.Default.Headphones, "Metni dinle", tint = accentBlue)
                    }
                }
            }
        },
        bottomBar = {
            if (!finished) Surface(color = Color.White, shadowElevation = 8.dp) {
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
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue)
                ) { Text(if (!checked) "Kontrol Et" else if (index == questions.lastIndex) "Sonucu Gör" else "Sonraki Soru", fontWeight = FontWeight.Bold) }
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
                    .mewScreenBackground(),
                contentPadding = PaddingValues(18.dp, 18.dp, 18.dp, 110.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = cardColor,
                        shadowElevation = 5.dp
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
                    Surface(shape = RoundedCornerShape(22.dp), color = cardColor, border = BorderStroke(1.dp, appColors.border)) {
                        Text(storyText, Modifier.padding(18.dp), color = Color(0xFF30495E), lineHeight = 25.sp)
                    }
                }
                item {
                    Surface(shape = RoundedCornerShape(22.dp), color = cardColor, shadowElevation = 4.dp) {
                        Column(Modifier.padding(18.dp)) {
                            Text("${index + 1} / ${questions.size}", color = accentBlue, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text(question.prompt, fontSize = 21.sp, lineHeight = 27.sp, fontWeight = FontWeight.Black, color = navy)
                        }
                    }
                }
                question.options.forEach { option ->
                    item {
                        val chosen = selected == option
                        val color = when {
                            checked && option == question.correctAnswer -> Color(0xFFDDF4E8)
                            checked && chosen -> Color(0xFFFFE4E4)
                            chosen -> Color(0xFFE0F3FD)
                            else -> cardColor
                        }
                        Surface(
                            Modifier.fillMaxWidth().clickable(enabled = !checked) { selected = option },
                            shape = RoundedCornerShape(18.dp), color = color,
                            shadowElevation = if (chosen) 3.dp else 1.dp,
                            border = BorderStroke(if (chosen) 2.dp else 1.dp, if (chosen) accentBlue else Color(0xFFD3E2EA))
                        ) { Text(option, Modifier.padding(16.dp), fontWeight = FontWeight.SemiBold, color = Color(0xFF30495E)) }
                    }
                }
                if (checked) item {
                    if (isCorrect) ReadingQuestionReward() else Text("Doğru cevap: ${question.correctAnswer}", color = Color(0xFFC94E4E), fontWeight = FontWeight.Bold)
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
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp, 20.dp, 24.dp, 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { LottieAnimation(celebration, iterations = 1, modifier = Modifier.size(220.dp)) }
        item {
            Text("Reading tamamlandı", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF23415A), textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("$correct doğru · ${total - correct} yanlış", color = Color(0xFF607D8B), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RewardAnimation(coin, "+$coinAmount coin", Color(0xFFE5A400), 90)
                RewardAnimation(xp, "+$xpAmount XP", Color(0xFF2781B8), 90)
            }
            Spacer(Modifier.height(28.dp))
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(8.dp)) { Text("TAMAM", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun ReadingExitDialog(onStay: () -> Unit, onExit: () -> Unit) {
    val context = LocalContext.current
    val failRes = remember { context.resources.getIdentifier("fail", "raw", context.packageName).takeIf { it != 0 } ?: R.raw.cat_fishing_on_moon }
    val fail by rememberLottieComposition(LottieCompositionSpec.RawRes(failRes))
    AlertDialog(
        onDismissRequest = onStay,
        icon = { LottieAnimation(fail, iterations = 1, modifier = Modifier.size(130.dp)) },
        title = { Text("Reading'den ayrılmak istiyor musun?", textAlign = TextAlign.Center) },
        text = { Text("Şimdi ayrılırsan bu turdaki coin ve XP ödülleri hesabına eklenmeyecek. Daha sonra yeniden deneyebilirsin.", textAlign = TextAlign.Center) },
        confirmButton = { TextButton(onClick = onExit) { Text("AYRIL", color = Color(0xFFD04C4C)) } },
        dismissButton = { TextButton(onClick = onStay) { Text("DEVAM ET") } }
    )
}

private fun buildReadingQuestions(title: String, text: String): List<ReadingQuizQuestion> {
    val others = allStories.filterKeys { it != title }
    val words = significantWords(text)
    val word = words.firstOrNull() ?: title.split(" ").first()
    val distractors = others.values.flatMap(::significantWords).filterNot { it in words }.distinct().take(3)
    val ending = sentences(text).lastOrNull() ?: text.trim()
    val otherEndings = others.values.mapNotNull { sentences(it).lastOrNull() }.take(3)
    return listOf(
        ReadingQuizQuestion("Okuduğun metnin başlığı hangisidir?", title, (listOf(title) + others.keys.take(3)).shuffled()),
        ReadingQuizQuestion("Aşağıdaki kelimelerden hangisi metinde geçiyor?", word, (listOf(word) + distractors).shuffled()),
        ReadingQuizQuestion("Metin hangi cümleyle bitiyor?", ending, (listOf(ending) + otherEndings).shuffled())
    )
}

private fun significantWords(text: String): List<String> {
    val stops = setOf("about", "after", "again", "because", "every", "there", "their", "these", "today", "very", "where", "which", "with", "want", "have", "this", "that", "from", "they")
    return Regex("[A-Za-z]{5,}").findAll(text).map { it.value.lowercase() }.filterNot { it in stops }.distinct().sortedByDescending { it.length }.toList()
}

private fun sentences(text: String) = text.trim().split(Regex("(?<=[.!?])\\s+")).map(String::trim).filter(String::isNotBlank)
