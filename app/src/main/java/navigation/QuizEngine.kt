package com.example.ngilizcequizapp.quiz.engine
import com.example.ngilizcequizapp.LocalAppThemeColors
import com.example.ngilizcequizapp.LocalIsDarkMode
import com.example.ngilizcequizapp.mewScreenBackground
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import com.example.ngilizcequizapp.QuizRepository
import com.example.ngilizcequizapp.QuizTreeProgressStore
import com.example.ngilizcequizapp.UserCosmeticsStore
import navigation.MarketBoostStore
import com.example.ngilizcequizapp.CartoonEnergyBadge
import com.example.ngilizcequizapp.MistakeJarStore
import com.example.ngilizcequizapp.AchievementManager
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.NavHostController

// Projenizdeki özel sınıflar (R dosyası ve SpeechAssistant)

import com.example.ngilizcequizapp.quiz.engine.MainQuizScreen
// Bunlar senin diğer dosyalarındaki fonksiyonlar.
// Paket isminin doğruluğunu kontrol et (Genelde com.example.uygulamaadi.quiz.engine şeklindedir)
import com.example.ngilizcequizapp.quiz.engine.VisualQuizContent
import com.example.ngilizcequizapp.quiz.engine.ListeningQuizContent
import com.example.ngilizcequizapp.quiz.engine.eslestirme
import com.example.ngilizcequizapp.quiz.engine.boslukdoldurma



import androidx.compose.animation.animateContentSize
import com.example.ngilizcequizapp.quiz.engine.VisualQuizContent
import com.example.ngilizcequizapp.quiz.engine.VocabularyLevelDataQuiz
import com.example.ngilizcequizapp.quiz.engine.QuestionType
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.ngilizcequizapp.R
import com.example.ngilizcequizapp.SpeechAssistant
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.ngilizcequizapp.MatchingChip
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

// --- 1. MODELLER (Eksiksiz) ---

enum class sheetQuestionType { VISUAL, LISTENING, MATCHING, FILL_IN_BLANK }

// --- 2. VERİ MODELLERİ ---

// Eğer başka bir yerde kullanıyorsan silme, ama VocabularyLevelData artık buna ihtiyaç duymuyor
data class VocabularyOption(val text: String, val imageRes: Int = 0)
enum class QuestionType {
    VISUAL,         // Görsel sorular
    LISTENING,      // Dinleme soruları
    MATCHING,       // Eşleştirme (eslestirme)
    FILL_IN_BLANK,
    TRANSLATION_BUILDER,
    PATTERN,
    SPEAKING,
    INFO_CARD       // 👈 Bilgi & Anlatım Ekranı (Soru olarak sayılmaz)
}

data class TranslationQuestion(
    val id: Int,
    val qEnglish: String,
    val correctWords: List<String>,
    val extraWords: List<String>,
    val fullAnswerTurkish: String
)

data class PatternQuestion(
    val id: Int,
    val title: String,
    val exampleOne: String,
    val highlightOne: String,
    val exampleTwo: String,
    val highlightTwo: String,
    val questionText: String,
    val correctAnswer: String,
    val options: List<String>
)

data class SpeakingQuestion(
    val id: Int,
    val title: String,
    val sentence: String,
    val characterRes: Int,
    val correctAnswer: String
)

@Composable
fun safePainterResource(resId: Int, fallbackResId: Int = com.example.ngilizcequizapp.R.drawable.cat_logo): androidx.compose.ui.graphics.painter.Painter {
    val context = LocalContext.current
    val targetId = remember(resId) {
        if (resId <= 0) {
            fallbackResId
        } else {
            val exists = try {
                context.resources.getResourceName(resId)
                true
            } catch (_: Exception) {
                false
            }
            if (exists) resId else fallbackResId
        }
    }
    return painterResource(id = targetId)
}

data class VocabularyLevelDataQuiz(
    val id: Int,
    val questionTurkish: String = "",
    val imageRes: Int = 0,
    val correctAnswer: String = "",
    val options: List<String> = emptyList(),
    // Varsayılanları değiştirdik:
    val speakQuestionInTurkish: Boolean = false, // Soru genelde İngilizce
    val speakOptionsInTurkish: Boolean = true,   // Şıklar genelde Türkçe
    val optionImages: List<Int> = emptyList(),
    val questionType: QuestionType,
    val quizList: List<String> = listOf("Genel"),
    val imageSize: Int = 120,
    val imagePadding: Int = 8,
    val transcriptTurkish: String? = null,
    val correctWords: List<String> = emptyList(),
    val extraWords: List<String> = emptyList(),
    val fullAnswerTurkish: String = correctAnswer,
    val patternTitle: String = "",
    val patternExampleOne: String = "",
    val patternHighlightOne: String = "",
    val patternExampleTwo: String = "",
    val patternHighlightTwo: String = "",
    val speakingTitle: String = "",
    val speakingSentence: String = "",
    val speakingCharacterRes: Int = 0,
    val infoTitle: String = "",
    val infoHighlights: List<String> = emptyList(),
    val infoTableHeaders: List<String> = emptyList(),
    val infoTableRows: List<List<String>> = emptyList(),
    val res: Int = 0
) {
    val question: String get() = questionTurkish
    val optionTexts: List<String> get() = options

    fun isCorrect(choice: String?): Boolean {
        return choice?.trim()?.lowercase() == correctAnswer.trim().lowercase()
    }
}

// Ham soru verisi (Repository'den gelen format)
data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val imageRes: Int,
    val correctAnswer: String,
    val options: List<String>,
    val type: QuestionType,
    // ✅ HATA ALAN EKSİK KISIMLAR BURASI:
    val speakQuestionInTurkish: Boolean = false,
    val speakOptionsInTurkish: Boolean = true,
    val imageSize: Int = 120,
    val imagePadding: Int = 8,
    val transcript: String? = null,
    val correctWords: List<String> = emptyList(),
    val extraWords: List<String> = emptyList(),
    val fullAnswerTurkish: String = correctAnswer,
    val patternTitle: String = "",
    val patternExampleOne: String = "",
    val patternHighlightOne: String = "",
    val patternExampleTwo: String = "",
    val patternHighlightTwo: String = "",
    val speakingTitle: String = "",
    val speakingSentence: String = "",
    val speakingCharacterRes: Int = 0
)

// --- 3. QUIZ MOTORU ---

class QuizEngine(val questions: List<QuizQuestion>) {

    // Can miktarını soru sayısına göre hesaplar (Minimum 1 can)
    private val calculatedHearts: Int = if (questions.isNotEmpty()) {
        (questions.size * 0.30).toInt().coerceAtLeast(1)
    } else 3

    var currentIndex by mutableIntStateOf(0)
    var hearts by mutableIntStateOf(calculatedHearts)
    var isFinished by mutableStateOf(false)

    // UI'ın (GorselQuestionUI vb.) kullandığı veri formatına dönüştürme işlemi
    val currentQuestionData: VocabularyLevelDataQuiz?
        get() = questions.getOrNull(currentIndex)?.let { item ->
            VocabularyLevelDataQuiz(
                id = item.id,
                imageRes = item.imageRes,
                correctAnswer = item.correctAnswer,
                options = item.options,
                questionTurkish = item.questionText,
                questionType = item.type, // Sol taraf modeldeki isim, sağ taraf item'daki isim

                // ✅ EKSİK OLAN VE SORUNU ÇÖZECEK OLAN KISIM BURASI:
                speakQuestionInTurkish = item.speakQuestionInTurkish, // Soru dili ayarını aktar
                speakOptionsInTurkish = item.speakOptionsInTurkish,   // Şık dili ayarını aktar

                // Diğer manuel ayarları da buraya ekleyebilirsin
                imageSize = item.imageSize,
                imagePadding = item.imagePadding,
                transcriptTurkish = item.transcript,
                correctWords = item.correctWords,
                extraWords = item.extraWords,
                fullAnswerTurkish = item.fullAnswerTurkish,
                patternTitle = item.patternTitle,
                patternExampleOne = item.patternExampleOne,
                patternHighlightOne = item.patternHighlightOne,
                patternExampleTwo = item.patternExampleTwo,
                patternHighlightTwo = item.patternHighlightTwo,
                speakingTitle = item.speakingTitle,
                speakingSentence = item.speakingSentence,
                speakingCharacterRes = item.speakingCharacterRes
            )
        }

    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentIndex)

    val progress: Float
        get() = if (questions.isEmpty()) 0f else (currentIndex.toFloat() / questions.size.toFloat())

    // Cevabı gönderen ve can/ilerleme durumunu güncelleyen fonksiyon
    fun submitAnswer(isCorrect: Boolean) {
        if (!isCorrect) {
            hearts--
            if (hearts <= 0) {
                isFinished = true
                return
            }
        }

        // Son soru değilse ilerle, son soruysa bitir
        if (currentIndex < questions.size - 1) {
            currentIndex++
        } else {
            isFinished = true
        }
    }

    // Quiz bittiğinde veya tekrar denendiğinde sıfırlama
    fun reset() {
        currentIndex = 0
        hearts = calculatedHearts
        isFinished = false
    }
}

// --- 3. ANA YÖNETİCİ ---

@Composable
fun MainQuizManager(
    engine: QuizEngine,
    ttsHelper: SpeechAssistant,
    onQuizExit: () -> Unit
) {
    val currentData = engine.currentQuestionData

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        if (engine.isFinished) {
            Sonucekrani(
                hearts = engine.hearts,
                totalQuestions = engine.questions.size,
                onRestart = { engine.reset() },
                onExit = { onQuizExit() }
            )
        } else if (currentData != null) {
            // Tüm soru tiplerini yöneten ana merkez
            when (currentData.questionType) {
                QuestionType.FILL_IN_BLANK -> {
                    boslukdoldurma(
                        data = currentData,
                        progress = engine.progress,
                        health = engine.hearts,
                        ttsHelper = ttsHelper,
                        // ✅ DÜZELTME: Buraya 'selectedText' parametresini ekledik
                        onAnswerResult = { isCorrect, selectedText ->
                            ttsHelper.speak(selectedText) // Kelimeyi seslendir
                            engine.submitAnswer(isCorrect)
                        }
                    )
                }
                else -> {
                    // Varsayılan Soru Görünümü
                    Column(Modifier.fillMaxSize().padding(16.dp)) {
                        // Eğer QuizTopBar sınıfın başka bir yerdeyse buraya ekle
                        /* QuizTopBar(
                            currentIndex = engine.currentIndex,
                            totalQuestions = engine.questions.size,
                            streakCount = 0,
                            hearts = engine.hearts,
                            onClose = onQuizExit
                        ) */

                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = currentData.questionTurkish,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // 🚨 DÜZELTME: option artık String, .text SİLİNDİ
                        currentData.options.forEach { option ->
                            Button(
                                onClick = {
                                    // Karşılaştırmayı direkt String üzerinden yapıyoruz
                                    val isCorrect = (option == currentData.correctAnswer)
                                    ttsHelper.speak(option) // Seslendirme
                                    engine.submitAnswer(isCorrect)
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                // 🚨 DÜZELTME: Direkt option yazıyoruz
                                Text(text = option)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 4. TÜM UI BİLEŞENLERİ (Particle, Shimmer, TopBar - KORUNDU) ---


@Composable
fun QuizTopBar(
    currentIndex: Int,
    totalQuestions: Int,
    streakCount: Int,
    hearts: Int,
    isStreakActive: Boolean, // 🚩 BU SATIRI EKLE
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val goldBrush = remember {
        Brush.horizontalGradient(
            listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))
        )
    }
    val barColor = if (isStreakActive) Color(0xFFFFD700) else Color(0xFF58CC02)
    val isStreakActive = streakCount >= 3
    val fireRes = rememberRawResId("fire")
    val fireComposition by rememberLottieComposition(
        fireRes?.let { LottieCompositionSpec.RawRes(it) } ?: LottieCompositionSpec.RawRes(R.raw.anim_coin)
    )
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -500f, targetValue = 1500f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )
    val progressBrush = when {
        isKingdomTheme -> goldBrush
        isStreakActive -> Brush.linearGradient(
            colors = listOf(Color(0xFF1CB0F6), Color.White, Color(0xFF1CB0F6)),
            start = Offset(shimmerOffset, 0f), end = Offset(shimmerOffset + 300f, 0f)
        )
        else -> SolidColor(Color(0xFF1CB0F6))
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = if (isKingdomTheme) Color(0xFFFFD700) else appColors.textMuted) }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.94f) else (if (isStreakActive) appColors.warning.copy(alpha = 0.2f) else appColors.primarySoft),
                border = if (isKingdomTheme) BorderStroke(1.2.dp, goldBrush) else null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KOMBO x$streakCount",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (isKingdomTheme) Color(0xFFFFD700) else (if (isStreakActive) appColors.warning else appColors.primary)
                    )
                    if (streakCount >= 3) {
                        Spacer(Modifier.width(4.dp))
                        LottieAnimation(
                            composition = fireComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CartoonEnergyBadge()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, null, tint = appColors.danger, modifier = Modifier.size(20.dp))
                    Text(text = " $hearts", fontWeight = FontWeight.Black, fontSize = 14.sp, color = appColors.danger)
                }
            }
        }
        Text(text = "${currentIndex + 1} / $totalQuestions", modifier = Modifier.padding(start = 52.dp, bottom = 2.dp), style = TextStyle(color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary, fontWeight = FontWeight.Black, fontSize = 12.sp))
        Surface(
            color = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.94f) else appColors.surfaceRaised,
            shape = RoundedCornerShape(16.dp),
            border = if (isKingdomTheme) BorderStroke(1.dp, goldBrush) else null,
            modifier = Modifier.fillMaxWidth().height(36.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                Image(painter = painterResource(id = R.drawable.kupa), contentDescription = null, modifier = Modifier.size(26.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f).height(14.dp).clip(CircleShape).background(if (isKingdomTheme) Color(0xFF1E2D4A) else appColors.border.copy(alpha = 0.5f))) {
                    Box(modifier = Modifier.fillMaxWidth(((currentIndex + 1).toFloat() / totalQuestions)).fillMaxHeight().background(progressBrush))
                }
            }
        }
    }
}

@Composable
fun StarParticleEffect(modifier: Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val alpha by infiniteTransition.animateFloat(initialValue = 0.2f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "")
    val scale by infiniteTransition.animateFloat(initialValue = 0.7f, targetValue = 1.3f, animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "")
    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = modifier.size(18.dp).graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale))
}

@Composable
private fun rememberRawResId(name: String): Int? {
    val context = LocalContext.current
    return remember(name) {
        context.resources.getIdentifier(name, "raw", context.packageName).takeIf { it != 0 }
    }
}

private fun normalizeSpeechForQuiz(text: String): String {
    return text
        .lowercase(Locale.ROOT)
        .replace("doesn't", "does not")
        .replace("don't", "do not")
        .replace("isn't", "is not")
        .replace("aren't", "are not")
        .replace("can't", "can not")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}

@Composable
fun Sonucekrani(hearts: Int, totalQuestions: Int, onRestart: () -> Unit, onExit: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    Column(modifier = Modifier.fillMaxSize().mewScreenBackground().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("OYUN BİTTİ", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = appColors.text)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Kalan Can: $hearts", fontSize = 20.sp, color = appColors.danger)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
        ) { Text("TEKRAR DENE", color = Color.White, fontWeight = FontWeight.Bold) }
        TextButton(onClick = onExit) { Text("ÇIKIŞ", color = appColors.textMuted) }
    }
}

@Composable
fun boslukdoldurma(
    data: VocabularyLevelDataQuiz,
    progress: Float,
    health: Int,
    ttsHelper: SpeechAssistant?,
    onAnswerResult: (Boolean, String) -> Unit
) {
    val appColors = LocalAppThemeColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = appColors.primarySoft,
            border = BorderStroke(1.dp, appColors.primary.copy(alpha = 0.3f))
        ) {
            Text(
                "BOŞLUK DOLDURMA",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = appColors.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = data.questionTurkish,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            color = appColors.text
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            data.options.forEach { option ->
                Button(
                    onClick = {
                        val isCorrect = option.trim().equals(data.correctAnswer.trim(), ignoreCase = true)
                        onAnswerResult(isCorrect, option)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.surface,
                        contentColor = appColors.text
                    ),
                    border = BorderStroke(1.5.dp, appColors.border)
                ) {
                    Text(text = option, fontSize = 19.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
//-------------------------------------------------------------------------------------
//
//
//
//
//
// YENİ QUİZ MOTORU
//----------------------------------------------------------------------------------------


@Composable
fun MainQuizScreen(
    quizList: List<VocabularyLevelDataQuiz>,
    ttsHelper: SpeechAssistant,
    navController: NavHostController,
    onQuizComplete: () -> Unit,
    onQuizResult: (correctAnswers: Int, totalQuestions: Int, completedAllQuestions: Boolean) -> Unit = { _, _, _ -> },
    previousResult: QuizTreeProgressStore.Result? = null,
    fullStarCountBefore: Int = 0,
    quizIndex: Int = -1,
    quizTitle: String = "Quiz",
    health: Int = 5,
    isMistakePractice: Boolean = false,
    mistakeEntries: List<com.example.ngilizcequizapp.MistakeEntry> = emptyList()
) {
    val context = LocalContext.current
    var currentIndex by rememberSaveable(quizIndex) { mutableIntStateOf(0) }
    var hearts by rememberSaveable(quizIndex) { mutableIntStateOf(health) }
    var score by rememberSaveable(quizIndex) { mutableIntStateOf(0) }
    var blankAnswers by rememberSaveable(quizIndex) { mutableIntStateOf(0) }
    var consecutiveCorrect by rememberSaveable(quizIndex) { mutableIntStateOf(0) }
    var quizCompleted by rememberSaveable(quizIndex) { mutableStateOf(false) }

    var showSheet by rememberSaveable(quizIndex) { mutableStateOf(false) }
    var isAnswerCorrect by rememberSaveable(quizIndex) { mutableStateOf(false) }
    var triggerStars by remember { mutableStateOf(false) }
    var showComboMilestone by rememberSaveable(quizIndex) { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showNeutralSpeakingSheet by rememberSaveable(quizIndex) { mutableStateOf(false) }
    var showWordsDialog by rememberSaveable(quizIndex) { mutableStateOf(false) }

    val currentQuestion = quizList.getOrNull(currentIndex)
    val isStreakActive = consecutiveCorrect >= 3

    if (quizCompleted || hearts <= 0) {
        val completedAllQuestions = quizCompleted && currentIndex >= quizList.lastIndex
        val outcome = QuizTreeProgressStore.calculateOutcome(
            context = context,
            previousResult = previousResult,
            correctAnswers = score,
            totalQuestions = quizList.size,
            quizIndex = quizIndex - 1,
            isReinforcement = QuizTreeProgressStore.hasReinforcementXp(context, quizIndex - 1)
        )
        val xpMul = if (isMistakePractice) MarketBoostStore.getXpMultiplier(context) else outcome.xpMultiplier
        val finalEarnedXp = if (isMistakePractice) (score * 5 * xpMul) else outcome.earnedXp
        val finalEarnedCoins = if (completedAllQuestions) outcome.earnedCoins else 0
        val finalEarnedGems = if (completedAllQuestions) outcome.earnedGems else 0

        QuizResultScreen(
            navController,
            score,
            quizList.size,
            earnedCoins = finalEarnedCoins,
            earnedGems = finalEarnedGems,
            earnedXp = finalEarnedXp,
            isReinforcement = outcome.isReinforcement,
            blankAnswers = blankAnswers,
            xpMultiplier = xpMul,
            coinMultiplier = outcome.coinMultiplier,
            gemMultiplier = outcome.gemMultiplier,
            onDone = {
                if (isMistakePractice && mistakeEntries.isNotEmpty()) {
                    showWordsDialog = true
                } else {
                    onQuizResult(score, quizList.size, completedAllQuestions)
                    onQuizComplete()
                }
            }
        )

        if (showWordsDialog) {
            com.example.ngilizcequizapp.SaveQuizWordsDialog(
                entries = mistakeEntries,
                onDismiss = {
                    showWordsDialog = false
                    onQuizComplete()
                },
                onFinish = {
                    showWordsDialog = false
                    onQuizComplete()
                }
            )
        }
        return
    }

    if (currentQuestion == null) return

    // ✅ TTS'nin çalışması için lambda'yı (Boolean, String) alacak şekilde güncelledik
    val onAnswerResult: (Boolean, String) -> Unit = { isCorrect: Boolean, selectedText: String ->
        isAnswerCorrect = isCorrect
        var shouldShowSheet = true
        if (isCorrect) {
            val newCombo = consecutiveCorrect + 1
            consecutiveCorrect = newCombo
            score++
            if (isMistakePractice) {
                val entry = mistakeEntries.getOrNull(currentIndex)
                if (entry != null) {
                    MistakeJarStore.markSolved(context, entry.id)
                    AchievementManager.unlockBadge(context, "common_first_fix")
                    if (MistakeJarStore.mistakes.none { !it.solved }) {
                        AchievementManager.unlockBadge(context, "rare_jar_emptier")
                    }
                }
            }
            if (newCombo == 4) {
                showComboMilestone = true
                shouldShowSheet = false
            }
            if (newCombo >= 5 && newCombo % 5 == 0) {
                showConfetti = true
            }
        } else {
            consecutiveCorrect = 0
            hearts--
            if (!isMistakePractice) {
                AchievementManager.unlockBadge(context, "common_first_mistake")
                if (MistakeJarStore.mistakes.size >= 5) {
                    AchievementManager.unlockBadge(context, "regular_jar_fill")
                }
                if (quizIndex >= 0) {
                    MistakeJarStore.record(
                        context = context,
                        quizIndex = quizIndex,
                        quizTitle = quizTitle,
                        questionNumber = currentIndex + 1,
                        question = currentQuestion,
                        selectedAnswer = selectedText
                    )
                }
            }
        }
        showSheet = shouldShowSheet
    }

    // ✅ contentAlignment = Alignment.BottomCenter: Paneli dibe yaslar
    Box(modifier = Modifier.fillMaxSize().mewScreenBackground(), contentAlignment = Alignment.BottomCenter) {

        Column(modifier = Modifier.fillMaxSize()) {
            if (false && currentQuestion.questionType == QuestionType.TRANSLATION_BUILDER) {
                TranslationBuilderScreen(
                    question = TranslationQuestion(
                        id = currentQuestion.id,
                        qEnglish = currentQuestion.questionTurkish,
                        correctWords = currentQuestion.correctWords,
                        extraWords = currentQuestion.extraWords,
                        fullAnswerTurkish = currentQuestion.fullAnswerTurkish
                    ),
                    comboCount = consecutiveCorrect,
                    progress = (currentIndex.toFloat() / quizList.size).coerceIn(0f, 1f),
                    energy = hearts,
                    onClose = onQuizComplete,
                    onSpeak = { ttsHelper.speakEnglish(currentQuestion.questionTurkish) },
                    onAnswer = { isCorr, text ->
                        if (isCorr) {
                            consecutiveCorrect++
                            score++
                            triggerStars = true
                            if (currentIndex >= quizList.size - 1) {
                                quizCompleted = true
                            } else {
                                currentIndex++
                            }
                        } else {
                            onAnswerResult(false, text)
                        }
                    }
                )
            } else {
            QuizTopBar(
                // 1. currentIndex kapısına sendeki currentIndex değişkenini ver
                currentIndex = currentIndex,

                // 2. totalQuestions kapısına sendeki listenin boyutunu ver
                totalQuestions = quizList.size,

                // 3. streakCount kapısına sendeki consecutiveCorrect değişkenini ver
                streakCount = consecutiveCorrect,

                // 4. hearts kapısına sendeki hearts değişkenini ver
                hearts = hearts,

                // 5. isStreakActive kapısına mantıksal kontrolü ver
                isStreakActive = consecutiveCorrect >= 3,

                // 6. onClose kapısına onQuizComplete fonksiyonunu ver
                onClose = { onQuizComplete() }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (currentQuestion.questionType) {

                    QuestionType.VISUAL -> VisualQuizContent(
                        data = currentQuestion,
                        tts = ttsHelper, // ✅ EKSİK OLAN PARAMETRE EKLENDİ
                        onAnswer = { isCorr, text ->
                            onAnswerResult(isCorr, text)
                        }
                    )

                    QuestionType.LISTENING -> ListeningQuizContent(
                        data = currentQuestion,
                        tts = ttsHelper,
                        onAnswer = { text: String ->
                            val isCorrect = text.trim().equals(currentQuestion.correctAnswer.trim(), ignoreCase = true)
                            onAnswerResult(isCorrect, text)
                        }
                    )

                    QuestionType.MATCHING -> eslestirme(
                        data = currentQuestion,
                        onAnswerResult = { isCorr: Boolean ->
                            // Eşleştirmede spesifik bir kelime okunmayacaksa boş bırakıyoruz
                            onAnswerResult(isCorr, "")
                        },
                        health = hearts,
                        progress = (currentIndex.toFloat() / quizList.size)
                    )

                    QuestionType.FILL_IN_BLANK -> boslukdoldurma(
                        data = currentQuestion,
                        onAnswerResult = { isCorr: Boolean, text: String ->
                            onAnswerResult(isCorr, text)
                        },
                        health = hearts,
                        progress = (currentIndex.toFloat() / quizList.size),
                        ttsHelper = ttsHelper
                    )

                    // Buraya diğer tiplerini (varsa) ekleyebilirsin
                    QuestionType.TRANSLATION_BUILDER -> TranslationBuilderScreen(
                        question = TranslationQuestion(
                            id = currentQuestion.id,
                            qEnglish = currentQuestion.questionTurkish,
                            correctWords = currentQuestion.correctWords,
                            extraWords = currentQuestion.extraWords,
                            fullAnswerTurkish = currentQuestion.fullAnswerTurkish
                        ),
                        comboCount = consecutiveCorrect,
                        progress = (currentIndex.toFloat() / quizList.size).coerceIn(0f, 1f),
                        energy = hearts,
                        onClose = onQuizComplete,
                        onSpeak = { ttsHelper.speak(currentQuestion.questionTurkish) },
                        onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                    )

                    QuestionType.PATTERN -> PatternQuizContent(
                        data = currentQuestion,
                        onAnswer = { selected ->
                            onAnswerResult(selected.trim() == currentQuestion.correctAnswer.trim(), selected)
                        }
                    )

                    QuestionType.SPEAKING -> SpeakingQuizContent(
                        data = currentQuestion,
                        tts = ttsHelper,
                        onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) },
                        onCannotSpeak = { showNeutralSpeakingSheet = true }
                    )

                    QuestionType.INFO_CARD -> InfoCardQuizContent(
                        data = currentQuestion,
                        onContinue = {
                            if (currentIndex >= quizList.size - 1) {
                                quizCompleted = true
                            } else {
                                currentIndex++
                            }
                        }
                    )

                    else -> Unit
                }
            }
        }

        // ⭐ YILDIZLAR: Ekranın tam ortasında patlayıp gidecek
        }

        if (showConfetti) {
            ConfettiComboOverlay(onFinished = { showConfetti = false })
        }

        if (showComboMilestone) {
            ComboMilestoneDialog(
                onContinue = {
                    showComboMilestone = false
                    if (currentIndex >= quizList.size - 1 || hearts <= 0) {
                        quizCompleted = true
                    } else {
                        currentIndex++
                    }
                }
            )
        }

        // 🟢 CEVAP PANELİ: Her zaman en altta çıkar
        if (showSheet) {
            QuizResultSheet(
                isCorrect = isAnswerCorrect,
                correctAnswer = currentQuestion.correctAnswer,
                tts = if (currentQuestion.questionType == QuestionType.LISTENING) null else ttsHelper,
                onContinue = {
                    showSheet = false
                    if (currentIndex >= quizList.size - 1 || hearts <= 0) {
                        quizCompleted = true
                    } else {
                        currentIndex++
                    }
                }
            )
        }

        if (showNeutralSpeakingSheet) {
            SpeakingNeutralSheet(
                correctAnswer = currentQuestion.correctAnswer,
                onContinue = {
                    showNeutralSpeakingSheet = false
                    blankAnswers++
                    if (currentIndex >= quizList.size - 1 || hearts <= 0) {
                        quizCompleted = true
                    } else {
                        currentIndex++
                    }
                }
            )
        }
    }
}

data class SpeakingEvaluationResult(
    val isCorrect: Boolean,
    val matchedList: List<Boolean>,
    val mispronouncedCount: Int,
    val totalWords: Int
)

private fun evaluateSpeakingPronunciation(
    sentence: String,
    spokenText: String
): SpeakingEvaluationResult {
    val wordRegex = Regex("[a-zA-Z0-9']+")
    val targetWordsWithRange = wordRegex.findAll(sentence).toList()
    if (targetWordsWithRange.isEmpty()) {
        val normalizedSpoken = normalizeSpeechForQuiz(spokenText)
        val normalizedTarget = normalizeSpeechForQuiz(sentence)
        val isCorr = normalizedSpoken.isNotEmpty() && normalizedSpoken == normalizedTarget
        return SpeakingEvaluationResult(
            isCorrect = isCorr,
            matchedList = emptyList(),
            mispronouncedCount = if (isCorr) 0 else 1,
            totalWords = 1
        )
    }

    val spokenCleanWords = spokenText
        .lowercase(Locale.ROOT)
        .replace("doesn't", "does not")
        .replace("don't", "do not")
        .replace("isn't", "is not")
        .replace("aren't", "are not")
        .replace("can't", "can not")
        .replace(Regex("[^a-z0-9 ]"), " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    val remainingSpoken = spokenCleanWords.toMutableList()
    val matchResults = mutableListOf<Boolean>()

    for (match in targetWordsWithRange) {
        val targetWord = match.value
        val matchIdx = remainingSpoken.indexOfFirst { spoken -> isWordMatch(targetWord, spoken) }
        if (matchIdx >= 0) {
            matchResults.add(true)
            remainingSpoken.removeAt(matchIdx)
        } else {
            matchResults.add(false)
        }
    }

    val totalWords = targetWordsWithRange.size
    val matchedCount = matchResults.count { it }
    val mispronouncedCount = totalWords - matchedCount

    // Pedagojik Kural:
    // 1 kelimelik cümlelerde hata toleransı yok (kelime doğru bilinmeli).
    // 2 kelimelik cümlelerde en fazla 1 kelime yanlış telaffuz edilirse doğru kabul edilir.
    // 3 ve daha fazla kelimelik cümlelerde 1 veya 2 kelime yanlış telaffuz edilirse doğru kabul edilir.
    val isCorrect = when (totalWords) {
        0 -> true
        1 -> mispronouncedCount == 0
        2 -> mispronouncedCount <= 1 && matchedCount >= 1
        else -> mispronouncedCount <= 2 && matchedCount >= 1
    }

    return SpeakingEvaluationResult(
        isCorrect = isCorrect,
        matchedList = matchResults,
        mispronouncedCount = mispronouncedCount,
        totalWords = totalWords
    )
}

private fun isWordMatch(target: String, spoken: String): Boolean {
    val t = target.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
    val s = spoken.lowercase(Locale.ROOT).replace(Regex("[^a-z0-9]"), "")
    if (t == s) return true
    if (t.isEmpty() || s.isEmpty()) return false

    // Kısaltma ve genel kalıplar
    if (t == "dont" && (s == "do" || s == "not" || s == "dont")) return true
    if (t == "doesnt" && (s == "does" || s == "not" || s == "doesnt")) return true
    if (t == "cant" && (s == "can" || s == "not" || s == "cant")) return true

    val maxLen = maxOf(t.length, s.length)
    val minLen = minOf(t.length, s.length)

    // Zor kelimeler için tolerans (Levenshtein mesafesi)
    if (minLen >= 4 && maxLen - minLen <= 1) {
        if (levenshteinDistance(t, s) <= 1) return true
    }
    if (minLen >= 7 && maxLen - minLen <= 2) {
        if (levenshteinDistance(t, s) <= 2) return true
    }
    return false
}

private fun levenshteinDistance(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
    for (i in 0..s1.length) dp[i][0] = i
    for (j in 0..s2.length) dp[0][j] = j
    for (i in 1..s1.length) {
        for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = minOf(
                dp[i - 1][j] + 1,
                dp[i][j - 1] + 1,
                dp[i - 1][j - 1] + cost
            )
        }
    }
    return dp[s1.length][s2.length]
}

@Composable
fun SpeakingQuizContent(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant? = null,
    onAnswer: (Boolean, String) -> Unit,
    onCannotSpeak: () -> Unit
) {
    val context = LocalContext.current
    val localTts = tts ?: remember(context) { SpeechAssistant(context) }
    val coroutineScope = rememberCoroutineScope()
    val isDark = LocalIsDarkMode.current
    val appColors = LocalAppThemeColors.current

    val question = SpeakingQuestion(
        id = data.id,
        title = data.speakingTitle.ifBlank { data.questionTurkish },
        sentence = data.speakingSentence.ifBlank { data.correctAnswer },
        characterRes = data.speakingCharacterRes.takeIf { it != 0 } ?: data.imageRes,
        correctAnswer = data.correctAnswer
    )
    val targetSentence = remember(question.sentence, question.correctAnswer) {
        question.sentence.ifBlank { question.correctAnswer }
    }

    var isRecording by rememberSaveable(data.id) { mutableStateOf(false) }
    var transcript by rememberSaveable(data.id) { mutableStateOf("") }
    var speechError by rememberSaveable(data.id) { mutableStateOf("") }
    var wordEvaluationState by rememberSaveable(data.id) { mutableStateOf<List<Boolean>?>(null) }
    var latestEvaluation by remember(data.id) { mutableStateOf<SpeakingEvaluationResult?>(null) }

    val wordRegex = remember { Regex("[a-zA-Z0-9']+") }
    val targetWordsWithRange = remember(question.sentence) {
        wordRegex.findAll(question.sentence).toList()
    }

    val sentenceAnnotatedString = remember(question.sentence, wordEvaluationState, isDark) {
        val currentEval = wordEvaluationState
        if (currentEval == null || targetWordsWithRange.isEmpty()) {
            AnnotatedString(question.sentence)
        } else {
            buildAnnotatedString {
                var cursor = 0
                val yellowColor = if (isDark) Color(0xFFFACC15) else Color(0xFFD97706)
                val greenColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)

                targetWordsWithRange.forEachIndexed { index, match ->
                    if (match.range.first > cursor) {
                        append(question.sentence.substring(cursor, match.range.first))
                    }
                    val isWordMatched = currentEval.getOrNull(index) ?: true
                    val wordColor = if (isWordMatched) greenColor else yellowColor

                    pushStyle(SpanStyle(color = wordColor, fontWeight = FontWeight.Black))
                    append(match.value)
                    pop()

                    cursor = match.range.last + 1
                }
                if (cursor < question.sentence.length) {
                    append(question.sentence.substring(cursor))
                }
            }
        }
    }

    val micColor by animateColorAsState(
        targetValue = if (isRecording) Color(0xFFE84B6A) else Color(0xFF2F80ED),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "speaking_mic_color"
    )
    val micScale by animateFloatAsState(
        targetValue = if (isRecording) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "speaking_mic_scale"
    )
    val musicTimeRes = rememberRawResId("music_time")
    val singingStudioRes = rememberRawResId("girl_singing_at_mic_in_studio")
    val speakingLottieRes = remember(question.id, musicTimeRes, singingStudioRes) {
        val options = listOfNotNull(musicTimeRes, singingStudioRes)
        when (options.size) {
            0 -> R.raw.badge
            1 -> options.first()
            else -> options[((question.id % options.size) + options.size) % options.size]
        }
    }
    val speakingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(speakingLottieRes))
    val speechRecognizerAvailable = remember {
        SpeechRecognizer.isRecognitionAvailable(context)
    }
    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
        }
    }
    val speechRecognizer = remember {
        if (speechRecognizerAvailable) SpeechRecognizer.createSpeechRecognizer(context) else null
    }
    DisposableEffect(speechRecognizer, question.correctAnswer) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isRecording = true
                speechError = ""
                wordEvaluationState = null
                latestEvaluation = null
            }

            override fun onBeginningOfSpeech() {
                isRecording = true
            }

            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                isRecording = false
            }

            override fun onError(error: Int) {
                isRecording = false
                speechError = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Sesi anlayamadım, tekrar dene."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ses duyamadım, tekrar dene."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni gerekli."
                    else -> "Konuşma algılanamadı, tekrar dene."
                }
            }

            override fun onResults(results: Bundle?) {
                isRecording = false
                val spokenText = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                transcript = spokenText
                if (spokenText.isBlank()) {
                    speechError = "Sesi anlayamadım, tekrar dene."
                    return
                }
                val eval = evaluateSpeakingPronunciation(
                    sentence = targetSentence,
                    spokenText = spokenText
                )
                wordEvaluationState = eval.matchedList
                latestEvaluation = eval

                // Yanlış cevap verilirse doğru telaffuz otomatik okunsun
                if (!eval.isCorrect) {
                    localTts.speakEnglish(targetSentence)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                transcript = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }
        speechRecognizer?.setRecognitionListener(listener)
        onDispose {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (speechRecognizerAvailable) {
                speechRecognizer?.startListening(speechIntent)
            } else {
                speechError = "Bu cihazda konusma tanima servisi bulunamadi."
            }
        } else {
            Toast.makeText(context, "Mikrofon izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }
    var showAudioDisclosureDialog by remember { mutableStateOf(false) }
    fun startSpeakingRecognition() {
        speechError = ""
        transcript = ""
        wordEvaluationState = null
        latestEvaluation = null
        if (!speechRecognizerAvailable) {
            speechError = "Bu cihazda konusma tanima servisi bulunamadi."
            return
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            speechRecognizer?.startListening(speechIntent)
        } else {
            showAudioDisclosureDialog = true
        }
    }

    if (showAudioDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showAudioDisclosureDialog = false },
            containerColor = Color(0xFF131D38),
            shape = RoundedCornerShape(22.dp),
            icon = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Mikrofon İzni Bilgilendirmesi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "MewEnglish, telaffuzunuzu analiz etmek ve konuşma pratiğinizi değerlendirmek amacıyla sesinizi algılar.\n\nSes verileriniz sunucularımızda KESİNLİKLE kaydedilmez veya üçüncü taraflarla paylaşılmaz; tamamen anlık olarak cihazınızda işlenir.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAudioDisclosureDialog = false
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text(
                        text = "Anladım ve İzin Ver",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAudioDisclosureDialog = false }) {
                    Text("Vazgeç", color = Color(0xFF94A3B8))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .mewScreenBackground()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question.title,
            modifier = Modifier.fillMaxWidth(),
            color = appColors.text,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(18.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = appColors.surface,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, appColors.border)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sentenceAnnotatedString,
                    modifier = Modifier.weight(1f),
                    color = appColors.text,
                    fontSize = 22.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        localTts.speakEnglish(targetSentence)
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (wordEvaluationState != null) appColors.primary else appColors.primarySoft,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Seslendir",
                        tint = if (wordEvaluationState != null) Color.White else appColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (wordEvaluationState != null) {
            val mispronouncedCount = wordEvaluationState?.count { !it } ?: 0
            if (mispronouncedCount in 1..2) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = (if (isDark) Color(0xFFFACC15) else Color(0xFFD97706)).copy(alpha = 0.15f),
                    modifier = Modifier.padding(top = 10.dp)
                ) {
                    Text(
                        text = "⚡ Sarı kelimelerin telaffuzuna dikkat et (Kabul edildi)",
                        color = if (isDark) Color(0xFFFACC15) else Color(0xFFB45309),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        LottieAnimation(
            composition = speakingComposition,
            iterations = 1,
            modifier = Modifier
                .size(175.dp)
                .clip(RoundedCornerShape(32.dp))
        )

        Spacer(Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .size(92.dp)
                .scale(micScale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { startSpeakingRecognition() },
            shape = CircleShape,
            color = micColor,
            shadowElevation = 12.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Mikrofon",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = if (isRecording) "Dinleniyor..." else "Mikrofona bas ve tekrar et",
            color = if (isRecording) appColors.danger else appColors.textMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        if (transcript.isNotBlank()) {
            Text(
                text = "\"$transcript\"",
                color = appColors.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (speechError.isNotBlank()) {
            Text(
                text = speechError,
                color = appColors.danger,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (latestEvaluation != null) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    latestEvaluation?.let { eval ->
                        onAnswer(eval.isCorrect, transcript)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (latestEvaluation?.isCorrect == true) appColors.primary else appColors.danger
                )
            ) {
                Text(
                    text = "KONTROL ET",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        TextButton(onClick = onCannotSpeak, modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = "ŞİMDİ KONUŞAMAM",
                color = appColors.textMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun PatternQuizContent(
    data: VocabularyLevelDataQuiz,
    onAnswer: (String) -> Unit
) {
    val appColors = LocalAppThemeColors.current
    val pattern = PatternQuestion(
        id = data.id,
        title = data.patternTitle,
        exampleOne = data.patternExampleOne,
        highlightOne = data.patternHighlightOne,
        exampleTwo = data.patternExampleTwo,
        highlightTwo = data.patternHighlightTwo,
        questionText = data.questionTurkish,
        correctAnswer = data.correctAnswer,
        options = data.options
    )
    var selectedOption by rememberSaveable(data.id) { mutableStateOf<String?>(null) }

    val shuffledOptions = remember(data.id, pattern.options, pattern.correctAnswer) {
        val base = if (pattern.options.isNotEmpty()) pattern.options else listOf(pattern.correctAnswer)
        val withCorrect = if (pattern.correctAnswer.isNotBlank() && !base.contains(pattern.correctAnswer)) {
            listOf(pattern.correctAnswer) + base
        } else base
        val distinctList = withCorrect.distinct()
        if (distinctList.size <= 1) {
            distinctList
        } else {
            val random = kotlin.random.Random(data.id.toLong() * 997L + 13L)
            var shuffled = distinctList.shuffled(random)
            if (shuffled.first() == pattern.correctAnswer) {
                val swapIndex = 1 + ((data.id % (shuffled.size - 1)).toInt())
                val mutable = shuffled.toMutableList()
                val temp = mutable[0]
                mutable[0] = mutable[swapIndex]
                mutable[swapIndex] = temp
                shuffled = mutable
            }
            shuffled
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .mewScreenBackground()
    ) {
        val isCompact = maxHeight < 700.dp
        val isVeryCompact = maxHeight < 620.dp

        val contentPadding = if (isVeryCompact) 12.dp else 16.dp
        val cardSpacing = if (isVeryCompact) 8.dp else 12.dp
        val optionHeight = if (isVeryCompact) 50.dp else if (isCompact) 56.dp else 62.dp
        val optionFontSize = if (isVeryCompact) 16.sp else 18.sp
        val titleFontSize = if (isVeryCompact) 20.sp else if (isCompact) 23.sp else 26.sp
        val questionFontSize = if (isVeryCompact) 17.sp else if (isCompact) 19.sp else 22.sp
        val exampleFontSize = if (isVeryCompact) 15.sp else if (isCompact) 17.sp else 19.sp
        val buttonSpacing = if (isVeryCompact) 6.dp else 10.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = contentPadding, vertical = 10.dp)
        ) {
            // Üst Soru ve Kart Alanı (Taşmayı önlemek için weight(1f) ve içten scroll destekli)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = appColors.primarySoft,
                    border = BorderStroke(1.dp, appColors.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "YENİ KALIP",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        color = appColors.primary,
                        fontSize = if (isVeryCompact) 11.sp else 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Text(
                    text = "Eksik olan kelimeyi seç",
                    color = appColors.text,
                    fontSize = titleFontSize,
                    lineHeight = (titleFontSize.value + 5).sp,
                    fontWeight = FontWeight.Black
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = appColors.surface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, appColors.border)
                ) {
                    Column(
                        modifier = Modifier.padding(if (isVeryCompact) 12.dp else 16.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isVeryCompact) 8.dp else 12.dp)
                    ) {
                        if (pattern.title.isNotBlank()) {
                            Text(
                                text = pattern.title,
                                color = appColors.primary,
                                fontWeight = FontWeight.Black,
                                fontSize = if (isVeryCompact) 14.sp else 16.sp
                            )
                        }

                        if (pattern.exampleOne.isNotBlank()) {
                            HighlightedPatternText(pattern.exampleOne, pattern.highlightOne, exampleFontSize)
                        }
                        if (pattern.exampleTwo.isNotBlank()) {
                            HighlightedPatternText(pattern.exampleTwo, pattern.highlightTwo, exampleFontSize)
                        }

                        val displayedQuestionText = remember(pattern.questionText, pattern.correctAnswer) {
                            val corr = pattern.correctAnswer.trim()
                            if (corr.isNotBlank()) {
                                val index = pattern.questionText.indexOf(corr, ignoreCase = true)
                                if (index >= 0) {
                                    pattern.questionText.substring(0, index) + ".........." + pattern.questionText.substring(index + corr.length)
                                } else {
                                    pattern.questionText
                                }
                            } else {
                                pattern.questionText
                            }
                        }

                        Text(
                            text = displayedQuestionText,
                            color = appColors.text,
                            fontSize = questionFontSize,
                            lineHeight = (questionFontSize.value + 6).sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(cardSpacing))

            // Alt Butonlar ve Kontrol Tuşu Alanı (Asla aşağı taşmaz, her zaman görünür)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                shuffledOptions.forEach { option ->
                    val isSelected = selectedOption == option
                    Button(
                        onClick = { selectedOption = option },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(optionHeight),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) appColors.primarySoft else appColors.surface,
                            contentColor = if (isSelected) appColors.primary else appColors.text
                        ),
                        border = BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, if (isSelected) appColors.primary else appColors.border)
                    ) {
                        Text(option, fontSize = optionFontSize, fontWeight = FontWeight.Black)
                    }
                }

                AnimatedVisibility(visible = selectedOption != null) {
                    Button(
                        onClick = { selectedOption?.let(onAnswer) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isVeryCompact) 52.dp else 58.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                    ) {
                        Text("KONTROL ET", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun HighlightedPatternText(
    sentence: String,
    highlight: String,
    fontSize: TextUnit = 19.sp
) {
    val appColors = LocalAppThemeColors.current
    val highlightColor = appColors.primary
    val text = remember(sentence, highlight) {
        buildAnnotatedString {
            val start = if (highlight.isNotBlank()) sentence.indexOf(highlight) else -1
            if (start < 0) {
                append(sentence)
            } else {
                append(sentence.substring(0, start))
                pushStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Black))
                append(sentence.substring(start, start + highlight.length))
                pop()
                append(sentence.substring(start + highlight.length))
            }
        }
    }
    Text(
        text = text,
        color = appColors.text,
        fontSize = fontSize,
        lineHeight = (fontSize.value + 7).sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun InfoCardQuizContent(
    data: VocabularyLevelDataQuiz,
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    val appColors = LocalAppThemeColors.current

    // Rastgele 3 animasyondan birini seç: party_penguin, binge_panda, astronaut
    val animCandidates = remember(data.id) {
        listOf("party_penguin", "binge_panda", "astronaut").shuffled()
    }
    
    val resolvedAnimRes = remember(data.id) {
        animCandidates.mapNotNull { name ->
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) resId else null
        }.firstOrNull() ?: R.raw.cat_fishing_on_moon
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resolvedAnimRes))

    // Metin İçindeki Vurgulu Kelimelerin Temaya Uygun Renkle Vurgulanması
    val annotatedText = remember(data.questionTurkish, data.infoHighlights, appColors) {
        buildAnnotatedString {
            val fullText = data.questionTurkish
            if (data.infoHighlights.isEmpty()) {
                append(fullText)
            } else {
                val highlightStyle = androidx.compose.ui.text.SpanStyle(
                    color = appColors.primary,
                    fontWeight = FontWeight.Black
                )
                
                val textLower = fullText.lowercase()
                val matches = mutableListOf<Pair<Int, Int>>()
                for (target in data.infoHighlights.filter { it.isNotBlank() }) {
                    val targetLower = target.lowercase()
                    var startPos = 0
                    while (startPos < fullText.length) {
                        val foundIndex = textLower.indexOf(targetLower, startPos)
                        if (foundIndex != -1) {
                            matches.add(foundIndex to (foundIndex + target.length))
                            startPos = foundIndex + target.length
                        } else {
                            break
                        }
                    }
                }

                val sortedMatches = matches.sortedBy { it.first }
                var lastPos = 0
                for ((start, end) in sortedMatches) {
                    if (start >= lastPos) {
                        append(fullText.substring(lastPos, start))
                        withStyle(highlightStyle) {
                            append(fullText.substring(start, end))
                        }
                        lastPos = end
                    }
                }
                if (lastPos < fullText.length) {
                    append(fullText.substring(lastPos))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .mewScreenBackground()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Üst Kısım: Rozet Başlığı ve Animasyon (SABİT - Animasyona scroll yok)
        Surface(shape = RoundedCornerShape(50), color = appColors.primarySoft) {
            Text(
                text = data.infoTitle.ifBlank { "BİLGİ & İPUCU" },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                color = appColors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Rastgele Seçilen Lottie Animasyonu (SABİT)
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(170.dp)
                .padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Alt Kısım: Açıklama Kartı (SCROLL EKLENDİ - Animasyon hariç bu kısım kaydırılabilir)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = appColors.surface,
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, appColors.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (data.questionTurkish.isNotBlank()) {
                        Text(
                            text = annotatedText,
                            color = appColors.text,
                            fontSize = 17.sp,
                            lineHeight = 25.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (data.infoTableHeaders.isNotEmpty() && data.infoTableRows.isNotEmpty()) {
                        if (data.questionTurkish.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        InfoCardTableView(
                            headers = data.infoTableHeaders,
                            rows = data.infoTableRows
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. En Altta Sabit Tıklanabilir Buton (Her zaman görünür ve tıklanabilir)
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = appColors.primary,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "ANLADIM",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
fun InfoCardTableView(
    headers: List<String>,
    rows: List<List<String>>
) {
    if (headers.isEmpty() || rows.isEmpty()) return
    val appColors = LocalAppThemeColors.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = appColors.surface,
        border = BorderStroke(1.5.dp, appColors.primary.copy(alpha = 0.35f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Başlık Satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appColors.primarySoft)
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        color = appColors.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Başlık Altı Ayracı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(appColors.primary.copy(alpha = 0.25f))
            )

            // Veri Satırları
            rows.forEachIndexed { rowIndex, row ->
                val rowBg = if (rowIndex % 2 == 1) appColors.primarySoft.copy(alpha = 0.25f) else Color.Transparent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(rowBg)
                        .padding(vertical = 9.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    row.forEach { cellText ->
                        Text(
                            text = cellText,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            color = appColors.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (rowIndex < rows.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.8.dp)
                            .background(appColors.border.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TranslationBuilderScreen(
    question: TranslationQuestion,
    comboCount: Int,
    progress: Float,
    energy: Int,
    onClose: () -> Unit,
    onSpeak: () -> Unit,
    onAnswer: (Boolean, String) -> Unit
) {
    val safeCorrectWords = remember(question) {
        if (question.correctWords.isNotEmpty()) {
            question.correctWords
        } else if (question.fullAnswerTurkish.isNotBlank()) {
            question.fullAnswerTurkish.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else {
            listOf("answer")
        }
    }
    val allWords = remember(question, safeCorrectWords) {
        val combined = (safeCorrectWords + question.extraWords).filter { it.isNotBlank() }
        if (combined.isEmpty()) listOf("word") else combined
    }

    val questionKey = "${question.id}_${question.qEnglish}"
    var bankIds by remember(questionKey) { mutableStateOf(allWords.indices.toList().shuffled()) }
    var selectedIds by remember(questionKey) { mutableStateOf(emptyList<Int>()) }
    var showWrongHint by remember(questionKey) { mutableStateOf(false) }

    LaunchedEffect(questionKey, allWords) {
        if (bankIds.any { it !in allWords.indices } || bankIds.size != allWords.size) {
            bankIds = allWords.indices.toList().shuffled()
            selectedIds = emptyList()
        }
    }

    val selectedWords = selectedIds.mapNotNull { allWords.getOrNull(it) }
    val targetWords = remember(question, safeCorrectWords) {
        if (question.correctWords.isNotEmpty()) question.correctWords else safeCorrectWords
    }
    val isCorrect = selectedWords.size == targetWords.size &&
        selectedWords.map { it.trim().lowercase() } == targetWords.map { it.trim().lowercase() }

    fun chooseWord(id: Int) {
        showWrongHint = false
        if (id !in selectedIds) {
            selectedIds = selectedIds + id
        }
    }

    fun removeWord(id: Int) {
        showWrongHint = false
        selectedIds = selectedIds.filterNot { it == id }
    }

    val appColors = LocalAppThemeColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mewScreenBackground()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(shape = RoundedCornerShape(50), color = appColors.primarySoft) {
                    Text(
                        "CÜMLE KURMA",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = appColors.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text("Aşağıdaki cümleyi çevir", color = appColors.text, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(46.dp)
                            .background(appColors.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Seslendir", tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(question.qEnglish, color = appColors.text, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            AnswerArea(
                selectedIds = selectedIds,
                allWords = allWords,
                onRemove = ::removeWord,
                modifier = Modifier.fillMaxWidth()
            )

            if (showWrongHint) {
                Text(
                    "Sıralama da önemli. Bir kez daha deneyelim.",
                    color = appColors.danger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            WordBank(
                bankIds = bankIds,
                selectedIds = selectedIds,
                allWords = allWords,
                onChoose = ::chooseWord,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )

            Button(
                onClick = {
                    val answer = selectedWords.joinToString(" ")
                    if (isCorrect) {
                        onAnswer(true, question.fullAnswerTurkish)
                    } else {
                        onAnswer(false, answer)
                    }
                },
                enabled = selectedIds.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.primary,
                    disabledContainerColor = appColors.surfaceRaised,
                    disabledContentColor = appColors.textMuted
                )
            ) {
                Text("KONTROL ET", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnswerArea(
    selectedIds: List<Int>,
    allWords: List<String>,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = modifier
            .heightIn(min = 110.dp),
        shape = RoundedCornerShape(24.dp),
        color = appColors.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, appColors.border)
    ) {
        FlowRow(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedIds.isEmpty()) {
                Text("Cevabını burada oluştur", color = appColors.textMuted, fontSize = 15.sp)
            }
            selectedIds.forEach { id ->
                allWords.getOrNull(id)?.let { word ->
                    WordChip(text = word, selected = true, onClick = { onRemove(id) })
                }
            }
        }
    }
}

@Composable
fun WordChipPlaceholder(text: String, modifier: Modifier = Modifier) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = appColors.surfaceRaised,
        border = BorderStroke(1.dp, appColors.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            color = Color.Transparent,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordBank(
    bankIds: List<Int>,
    selectedIds: List<Int>,
    allWords: List<String>,
    onChoose: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        bankIds.forEach { id ->
            val word = allWords.getOrNull(id) ?: return@forEach
            if (id in selectedIds) {
                WordChipPlaceholder(text = word)
            } else {
                WordChip(text = word, selected = false, onClick = { onChoose(id) })
            }
        }
    }
}

@Composable
fun WordChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) appColors.primarySoft else appColors.surface,
        shadowElevation = if (selected) 9.dp else 5.dp,
        border = BorderStroke(1.dp, if (selected) appColors.primary else appColors.border)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            color = if (selected) appColors.primary else appColors.text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SuccessPanel(visible: Boolean, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    val appColors = LocalAppThemeColors.current
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxWidth(),
        enter = slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { it } + fadeIn(),
        exit = slideOutVertically(animationSpec = spring()) { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = appColors.success,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Doğru!", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = appColors.success)
                ) {
                    Text("DEVAM ET", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ComboMilestoneDialog(onContinue: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    val dogRes = rememberRawResId("flirting_dog")
    val dogComposition by rememberLottieComposition(
        dogRes?.let { LottieCompositionSpec.RawRes(it) } ?: LottieCompositionSpec.RawRes(R.raw.star_badge)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = .45f))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = appColors.surface,
            shadowElevation = 18.dp,
            border = BorderStroke(1.dp, appColors.border)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LottieAnimation(
                    composition = dogComposition,
                    iterations = 1,
                    modifier = Modifier.size(150.dp)
                )
                Text(
                    "İyi ilerliyorsun, devam!",
                    color = appColors.text,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Text("DEVAM ET", fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ConfettiComboOverlay(onFinished: () -> Unit) {
    val confettiRes = rememberRawResId("confeti")
    val confettiComposition by rememberLottieComposition(
        confettiRes?.let { LottieCompositionSpec.RawRes(it) } ?: LottieCompositionSpec.RawRes(R.raw.star_blast)
    )
    LaunchedEffect(Unit) {
        delay(1800)
        onFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = confettiComposition,
            iterations = 1,
            modifier = Modifier.fillMaxWidth().height(320.dp)
        )
    }
}

@Composable
fun SpeakingNeutralSheet(
    correctAnswer: String,
    onContinue: () -> Unit
) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = appColors.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, appColors.border)
    ) {
        Column(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Doğru cevap",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textMuted
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = correctAnswer,
                style = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = appColors.text
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DEVAM ET", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun QuizResultSheet(
    isCorrect: Boolean,
    correctAnswer: String,
    tts: SpeechAssistant? = null,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkMode.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val goldBrush = remember {
        Brush.horizontalGradient(
            listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))
        )
    }
    val sheetBg = when {
        isKingdomTheme -> if (isCorrect) Color(0xFF070F26).copy(alpha = 0.98f) else Color(0xFF200910).copy(alpha = 0.98f)
        isCorrect -> if (isDark) Color(0xFF103326) else Color(0xFFE8F5E9)
        else -> if (isDark) Color(0xFF38151D) else Color(0xFFFFEBEE)
    }
    val titleColor = when {
        isKingdomTheme -> if (isCorrect) Color(0xFFFFD700) else Color(0xFFFF7A90)
        isCorrect -> if (isDark) Color(0xFF55D6A7) else Color(0xFF2E7D32)
        else -> if (isDark) Color(0xFFFF7A90) else Color(0xFFC62828)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = sheetBg,
        border = if (isKingdomTheme) (if (isCorrect) BorderStroke(1.5.dp, goldBrush) else BorderStroke(1.5.dp, Color(0xFFFF7A90))) else null,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCorrect) (if (isKingdomTheme) "Harika! Krallık Başarısı ✨" else "Harika! ✨") else "Doğru Cevap:",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = titleColor)
            )
            if (!isCorrect) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = correctAnswer,
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isKingdomTheme) Color(0xFFFFF7D6) else appColors.text)
                    )
                    if (tts != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { tts.speakEnglish(correctAnswer) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(appColors.danger.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Dinle",
                                tint = appColors.danger,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isKingdomTheme && isCorrect) Color(0xFFD4AF37) else if (isCorrect) appColors.success else appColors.danger
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "DEVAM ET",
                    fontWeight = FontWeight.Black,
                    color = if (isKingdomTheme && isCorrect) Color(0xFF040816) else Color.White
                )
            }
        }
    }
}
@Composable
fun QuizResultScreen(
    navController: NavController,
    score: Int,
    totalQuestions: Int,
    earnedCoins: Int = 0,
    earnedGems: Int = 0,
    earnedXp: Int = 0,
    isReinforcement: Boolean = false,
    blankAnswers: Int = 0,
    xpMultiplier: Int = 1,
    coinMultiplier: Int = 1,
    gemMultiplier: Int = 1,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val safeScore = score.coerceIn(0, totalQuestions.coerceAtLeast(0))
    val safeBlankAnswers = blankAnswers.coerceIn(0, (totalQuestions - safeScore).coerceAtLeast(0))
    val wrongAnswers = (totalQuestions - safeScore - safeBlankAnswers).coerceAtLeast(0)
    val percentage = if (totalQuestions > 0) safeScore * 100 / totalQuestions else 0
    val result = when {
        totalQuestions > 0 && safeScore == totalQuestions -> QuizResultPresentation(
            R.raw.premium, "Muhteşem sonuç!", if (isDark) Color(0xFF35C2FF) else Color(0xFF147D64),
            listOf(
                "$safeScore doğruyla kusursuz bir sonuç çıkardın. Tebrikler!",
                "Bütün soruları doğru cevapladın; emeğinin karşılığı harika görünüyor.",
                "$safeScore sorunun tamamında doğru seçim yaptın. Gerçekten çok iyi!",
                "Tam puan! Bu konudaki hakimiyetin kendini açıkça gösteriyor."
            )
        )
        wrongAnswers == 1 -> QuizResultPresentation(
            R.raw.star_badge, "Harika iş!", if (isDark) Color(0xFF55D6A7) else Color(0xFF2878B5),
            listOf(
                "$safeScore doğru yaptın; yalnızca bir soru seni zorladı. Çok güçlü bir sonuç!",
                "Tek bir soruyla fark kaçtı, ama konuya çok iyi hakimsin.",
                "$safeScore doğru cevapla hedefe çok yaklaştın. Tebrikler!",
                "Neredeyse kusursuzdu. Bir soruyu gözden geçirmen yeterli olacak."
            )
        )
        percentage >= 70 -> QuizResultPresentation(
            R.raw.star_blast, "Çok iyi gidiyorsun!", if (isDark) Color(0xFFFFC857) else Color(0xFF5A6F1D),
            listOf(
                "$safeScore doğru cevapla güçlü bir sonuç elde ettin. Tebrikler!",
                "%$percentage başarı, konuyu iyi kavradığını gösteriyor.",
                "$safeScore soruyu doğru çözdün. Biraz tekrar seni daha da ileri taşıyacak.",
                "Gayet iyi bir sonuç! Zorlandığın sorular gelişimin için güzel bir rehber."
            )
        )
        else -> QuizResultPresentation(
            R.raw.cat_fishing_on_moon, "Her deneme bir adım", if (isDark) Color(0xFFA8BED2) else Color(0xFF52606D),
            listOf(
                "$safeScore doğru cevap, hangi konulara dönmen gerektiğini gösterdi. Sakin sakin devam edebilirsin.",
                "Bu tur öğrenme yolunun bir parçası. Yanlışlarına göz atıp yeniden deneyebilirsin.",
                "$safeScore soruyu doğru yaptın. Küçük tekrarlarla sonuç hızla değişebilir.",
                "Bugünkü sonuç seni tanımlamaz; pratik yaptıkça daha rahat ilerleyeceksin."
            )
        )
    }
    val message = remember(safeScore, totalQuestions) {
        result.messages[(safeScore * 31 + totalQuestions * 17) % result.messages.size]
    }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(result.animationRes))
    val coinComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coin))
    val gemComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_diamond))
    val xpComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.badge))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .mewScreenBackground()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            iterations = 1,
            modifier = Modifier.fillMaxWidth().heightIn(min = 190.dp, max = 280.dp)
        )
        Text(
            result.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            message,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.85f) else appColors.textMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val correctColor = if (isKingdomTheme) Color(0xFFFFD700) else if (isDark) Color(0xFF55D6A7) else Color(0xFF21845B)
            val correctBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.94f) else if (isDark) Color(0xFF103326) else Color(0xFFE7F5EE)
            val blankColor = if (isKingdomTheme) Color(0xFF94A3B8) else if (isDark) Color(0xFFA8BED2) else Color(0xFF60758F)
            val blankBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.94f) else if (isDark) appColors.surfaceRaised else Color(0xFFE9EEF4)
            val wrongColor = if (isKingdomTheme) Color(0xFFFF7A90) else if (isDark) Color(0xFFFF7A90) else Color(0xFFB54747)
            val wrongBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.94f) else if (isDark) Color(0xFF38151D) else Color(0xFFFBECEC)

            ResultCountCard("Doğru", safeScore, correctColor, correctBg, Modifier.weight(1f))
            ResultCountCard("Boş", safeBlankAnswers, blankColor, blankBg, Modifier.weight(1f))
            ResultCountCard("Yanlış", wrongAnswers, wrongColor, wrongBg, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("%$percentage başarı", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (isKingdomTheme) Color(0xFFFFD700) else result.color)
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isKingdomTheme) Color(0xFFD4AF37) else result.color
            )
        ) {
            Text(
                "TAMAMLA",
                color = if (isKingdomTheme) Color(0xFF040816) else Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(14.dp))
        LottieAnimation(
            composition = coinComposition,
            iterations = 1,
            modifier = Modifier.size(72.dp)
        )
        if (coinMultiplier > 1 && earnedCoins > 0) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFB300).copy(alpha = 0.18f),
                border = BorderStroke(1.dp, Color(0xFFFFB300)),
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = "⚡ 2X COIN BOOST AKTİF",
                    color = Color(0xFFFFB300),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
        Text(
            text = when {
                earnedCoins > 0 && isReinforcement ->
                    "+$earnedCoins coin: Yeni kırmızı pekiştirme yıldızların için kazandın.${if (coinMultiplier > 1) " (2X Katlandı!)" else ""}"
                earnedCoins > 0 ->
                    "+$earnedCoins coin: İlk çözümündeki yıldız başarın için kazandın.${if (coinMultiplier > 1) " (2X Katlandı!)" else ""}"
                isReinforcement ->
                    "Bu tur yeni kırmızı yıldız açılmadığı için coin eklenmedi."
                else ->
                    "Coin kazanmak için quizi en az bir yıldızla tamamlayabilirsin."
            },
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.textMuted,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottieAnimation(
                composition = xpComposition,
                iterations = 1,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "+$earnedXp XP",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = appColors.primary
            )
            if (xpMultiplier > 1 && earnedXp > 0) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF8B5CF6).copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6))
                ) {
                    Text(
                        text = "2X BOOST",
                        color = Color(0xFF8B5CF6),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (earnedGems > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LottieAnimation(
                    composition = gemComposition,
                    iterations = 1,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "+$earnedGems elmas kazandın",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.primary
                )
                if (gemMultiplier > 1) {
                    Spacer(Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF00B0FF).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFF00B0FF))
                    ) {
                        Text(
                            text = "2X",
                            color = Color(0xFF00B0FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class QuizResultPresentation(
    val animationRes: Int,
    val title: String,
    val color: Color,
    val messages: List<String>
)

@Composable
private fun ResultCountCard(
    label: String,
    value: Int,
    color: Color,
    background: Color,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = background) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(value.toString(), color = color, fontSize = 28.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = appColors.surfaceRaised,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, appColors.border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 14.sp, color = appColors.textMuted)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}
@Composable
fun VisualQuizContent(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (Boolean, String) -> Unit
) {
    val appColors = LocalAppThemeColors.current
    var selectedOption by remember(data) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = appColors.surface,
            shadowElevation = 4.dp,
            border = BorderStroke(1.dp, appColors.border),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = data.questionTurkish,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                color = appColors.text
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val shuffledOptions = remember(data) {
            val zipped = data.options.zip(data.optionImages.ifEmpty { List(data.options.size) { null } })
            zipped.shuffled()
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(shuffledOptions) { index, pair ->
                val optionText = pair.first
                val isSelected = selectedOption == optionText
                val imageToDisplay = if (pair.second != null && pair.second != 0) pair.second!! else R.drawable.cat_logo

                Surface(
                    modifier = Modifier
                        .aspectRatio(1.10f)
                        .clickable {
                            selectedOption = optionText
                            tts.speakEnglish(optionText)
                        }
                        .border(
                            width = if (isSelected) 3.dp else 1.5.dp,
                            color = if (isSelected) appColors.primary else appColors.border,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) appColors.primarySoft else appColors.surface
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Image(
                            painter = safePainterResource(
                                resId = imageToDisplay,
                                fallbackResId = R.drawable.cat_logo
                            ),
                            contentDescription = optionText,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .sizeIn(maxHeight = 92.dp, maxWidth = 92.dp)
                                .padding(2.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = optionText,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            maxLines = 1,
                            color = if (isSelected) appColors.primary else appColors.text
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (selectedOption != null) {
                    onAnswer(data.isCorrect(selectedOption), selectedOption!!)
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedOption != null) appColors.primary else appColors.border,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "KONTROL ET",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)
            )
        }
    }
}

@Composable
fun ListeningQuizContent(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (String) -> Unit
) {
    val appColors = LocalAppThemeColors.current
    var isTextVisible by remember { mutableStateOf(false) }
    var selectedOption by rememberSaveable(data.id) { mutableStateOf<String?>(null) }
    val primaryColor = appColors.primary
    val shuffledOptions = remember(data.id) { data.options.shuffled() }

    LaunchedEffect(data.id) {
        isTextVisible = false
        selectedOption = null
        tts.speakEnglish(data.question)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- KARAKTER VE KONUŞMA BALONU (Daha büyük ve belirgin) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Image(
                painter = safePainterResource(
                    resId = if (data.imageRes != 0) data.imageRes else R.drawable.professor_cat,
                    fallbackResId = R.drawable.professor_cat
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(105.dp)
                    .padding(2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = appColors.surface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp),
                shadowElevation = 5.dp,
                border = BorderStroke(1.5.dp, appColors.border),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                        if (isTextVisible) {
                            Text(
                                text = data.question,
                                color = appColors.text,
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, lineHeight = 26.sp)
                            )
                        } else {
                            Text(
                                text = "???",
                                color = appColors.textMuted.copy(alpha = 0.6f),
                                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { isTextVisible = !isTextVisible },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isTextVisible) "Gizle" else "Metni Gör",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }

                        IconButton(
                            onClick = {
                                tts.speakEnglish(data.question)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- CEVAP BUTONLARI (Sorunun hemen altında) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            shuffledOptions.forEach { option ->
                val isSelected = selectedOption == option
                Button(
                    onClick = {
                        selectedOption = option
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) appColors.primarySoft else appColors.surface,
                        contentColor = if (isSelected) appColors.primary else appColors.text
                    ),
                    border = BorderStroke(if (isSelected) 2.5.dp else 1.5.dp, if (isSelected) appColors.primary else appColors.border)
                ) {
                    Text(
                        text = option,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = {
                    selectedOption?.let(onAnswer)
                },
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedOption != null) primaryColor else appColors.surface,
                    disabledContainerColor = appColors.surface.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (selectedOption != null) primaryColor else appColors.border)
            ) {
                Text(
                    text = "KONTROL ET",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (selectedOption != null) Color.White else appColors.textMuted
                )
            }
        }
    }
}
@Composable
fun eslestirme(
    data: VocabularyLevelDataQuiz,
    progress: Float,
    health: Int,
    onAnswerResult: (Boolean) -> Unit
) {
    val appColors = LocalAppThemeColors.current
    // ✅ YENİ MODEL UYUMU: data.options artık direkt String listesi!
    val pairs: List<Pair<String, String>> = remember(data) {
        data.options.zip(data.correctAnswer.split(","))
    }

    val leftWords = remember(pairs) { pairs.map { it.first }.shuffled() }
    val rightWords = remember(pairs) { pairs.map { it.second }.shuffled() }

    var selectedLeft by remember(data) { mutableStateOf<String?>(null) }
    var selectedRight by remember(data) { mutableStateOf<String?>(null) }

    val matchedLeftWords = remember(data) { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Kelimeleri Eşleştir",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = appColors.text)
        )
        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // SOL SÜTUN (Türkçe Kelimeler)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                leftWords.forEach { word ->
                    val isMatched = matchedLeftWords.contains(word)
                    MatchingChip(
                        word = word,
                        isSelected = selectedLeft == word,
                        isMatched = isMatched,
                        onClick = { if (!isMatched) selectedLeft = word }
                    )
                }
            }

            // SAĞ SÜTUN (İngilizce Karşılıkları)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rightWords.forEach { word ->
                    val leftOfThisRight = pairs.find { it.second == word }?.first
                    val isMatched = leftOfThisRight != null && matchedLeftWords.contains(leftOfThisRight)

                    MatchingChip(
                        word = word,
                        isSelected = selectedRight == word,
                        isMatched = isMatched,
                        onClick = { if (!isMatched) selectedRight = word }
                    )
                }
            }
        }

        // --- KONTROL MEKANİZMASI ---
        LaunchedEffect(selectedLeft, selectedRight) {
            if (selectedLeft != null && selectedRight != null) {
                val isCorrect = pairs.any {
                    it.first == selectedLeft && it.second == selectedRight
                }

                if (isCorrect) {
                    matchedLeftWords.add(selectedLeft!!)
                    selectedLeft = null
                    selectedRight = null

                    if (matchedLeftWords.size == pairs.size) {
                        onAnswerResult(true)
                    }
                } else {
                    kotlinx.coroutines.delay(500)
                    selectedLeft = null
                    selectedRight = null
                    onAnswerResult(false)
                }
            }
        }
    }
}
@Composable
fun MatchingChip(
    word: String,
    isSelected: Boolean,
    isMatched: Boolean,
    onClick: () -> Unit
) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    val borderColor = when {
        isMatched -> if (isDark) Color(0xFF2E9D78) else Color(0xFFC3F6D5)
        isSelected -> appColors.primary
        else -> appColors.border
    }

    val bgColor = when {
        isMatched -> if (isDark) Color(0xFF103326) else Color(0xFFE8F5E9)
        isSelected -> appColors.primarySoft
        else -> appColors.surface
    }

    Surface(
        modifier = Modifier
            .width(150.dp)
            .height(50.dp)
            .clickable(enabled = !isMatched) { onClick() }
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = word,
                fontWeight = FontWeight.Bold,
                color = when {
                    isMatched -> if (isDark) Color(0xFF55D6A7) else Color(0xFF2E7D32)
                    isSelected -> appColors.primary
                    else -> appColors.text
                }
            )
        }
    }
}

@Composable
fun ComboStarsAnimation(
    isComboActive: Boolean = true,
    onAnimationFinish: () -> Unit
) {
    if (isComboActive) {
        // ✅ Animasyonu tek seferlik (0'dan 1'e) koşturuyoruz
        val animProgress = remember { Animatable(0f) }

        // Ekran açıldığında animasyonu başlat
        LaunchedEffect(Unit) {
            animProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
            )
            // ✅ Animasyon bittiği an MainQuizScreen'e "Ben bittim, beni kaldır" diyoruz
            onAnimationFinish()
        }

        // Değerleri progress'e (0f -> 1f) göre hesapla
        val scale = animProgress.value * 2.5f // 0'dan 2.5 katına büyür
        val opacity = 1f - animProgress.value  // 1'den 0'a doğru solar

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨🌟✨",
                fontSize = 45.sp,
                modifier = Modifier
                    .scale(scale)
                    .graphicsLayer(alpha = opacity) // Görünmez olup gidecek
                    .padding(bottom = 150.dp)
            )
        }
    }
}

//    QUZİLERRRRRRRRRRRRRRRRRRRRRR








