package com.example.ngilizcequizapp.quiz.engine
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
    SPEAKING
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

data class VocabularyLevelDataQuiz(
    val id: Int,
    val questionTurkish: String,
    val imageRes: Int,
    val correctAnswer: String,
    val options: List<String>,
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
    val speakingCharacterRes: Int = 0
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
    val progressBrush = if (isStreakActive) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1CB0F6), Color.White, Color(0xFF1CB0F6)),
            start = Offset(shimmerOffset, 0f), end = Offset(shimmerOffset + 300f, 0f)
        )
    } else SolidColor(Color(0xFF1CB0F6))

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.Gray) }
            Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF4F0FF)) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KOMBO x$streakCount",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = Color(0xFF6A4BFF)
                    )
                    if (streakCount >= 3) {
                        Spacer(Modifier.width(4.dp))
                        LottieAnimation(
                            composition = fireComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, null, tint = Color.Red, modifier = Modifier.size(28.dp))
                Text(text = " $hearts", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.Red)
            }
        }
        Text(text = "${currentIndex + 1} / $totalQuestions", modifier = Modifier.padding(start = 52.dp), style = TextStyle(color = Color(0xFF1CB0F6), fontWeight = FontWeight.Black, fontSize = 13.sp))
        Surface(color = Color(0xFFF0F0F0), shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp)) {
                Image(painter = painterResource(id = R.drawable.kupa), contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f).height(14.dp).clip(CircleShape).background(Color.White.copy(0.5f))) {
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
    Column(modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("OYUN BİTTİ", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Kalan Can: $hearts", fontSize = 20.sp, color = Color.Red)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) { Text("TEKRAR DENE") }
        TextButton(onClick = onExit) { Text("ÇIKIŞ") }
    }
}

@Composable
fun boslukdoldurma(
    data: VocabularyLevelDataQuiz,
    progress: Float,
    health: Int,
    ttsHelper: SpeechAssistant?,
    // ✅ DÜZELTME: (Boolean) yerine (Boolean, String) yaparak sözleşmeyi eşitledik
    onAnswerResult: (Boolean, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Boşluğu Doldur:", fontSize = 18.sp, color = Color.Gray)
        Text(data.questionTurkish, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        data.options.forEach { option ->
            Button(
                onClick = {
                    // ✅ DÜZELTME: Hem sonucu hem de seçilen kelimeyi yukarı gönderiyoruz
                    val isCorrect = option.lowercase() == data.correctAnswer.lowercase()

                    // Artık iki parametre gönderdiği için 'Argument type mismatch' hatası biter
                    onAnswerResult(isCorrect, option)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = option, fontSize = 16.sp)
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
    health: Int = 5
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(health) }
    var score by remember { mutableIntStateOf(0) }
    var blankAnswers by remember { mutableIntStateOf(0) }
    var consecutiveCorrect by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    var showSheet by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var triggerStars by remember { mutableStateOf(false) }
    var showComboMilestone by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var showNeutralSpeakingSheet by remember { mutableStateOf(false) }

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
        QuizResultScreen(
            navController,
            score,
            quizList.size,
            earnedCoins = if (completedAllQuestions) outcome.earnedCoins else 0,
            earnedGems = if (completedAllQuestions) outcome.earnedGems else 0,
            earnedXp = outcome.earnedXp,
            isReinforcement = outcome.isReinforcement,
            blankAnswers = blankAnswers,
            onDone = {
                onQuizResult(score, quizList.size, completedAllQuestions)
                onQuizComplete()
            }
        )
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
        showSheet = shouldShowSheet
    }

    // ✅ contentAlignment = Alignment.BottomCenter: Paneli dibe yaslar
    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.BottomCenter) {

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
                            val isCorrect = text.lowercase() == currentQuestion.correctAnswer.lowercase()
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
                        onSpeak = { ttsHelper.speakEnglish(currentQuestion.questionTurkish) },
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
                        onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) },
                        onCannotSpeak = { showNeutralSpeakingSheet = true }
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

@Composable
fun SpeakingQuizContent(
    data: VocabularyLevelDataQuiz,
    onAnswer: (Boolean, String) -> Unit,
    onCannotSpeak: () -> Unit
) {
    val context = LocalContext.current
    val question = SpeakingQuestion(
        id = data.id,
        title = data.speakingTitle.ifBlank { data.questionTurkish },
        sentence = data.speakingSentence.ifBlank { data.correctAnswer },
        characterRes = data.speakingCharacterRes.takeIf { it != 0 } ?: data.imageRes,
        correctAnswer = data.correctAnswer
    )
    var isRecording by rememberSaveable(data.id) { mutableStateOf(false) }
    var transcript by rememberSaveable(data.id) { mutableStateOf("") }
    var speechError by rememberSaveable(data.id) { mutableStateOf("") }
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
                    SpeechRecognizer.ERROR_NO_MATCH -> "Sesi anlayamadim, tekrar dene."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ses duyamadim, tekrar dene."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni gerekli."
                    else -> "Konusma algilanamadi, tekrar dene."
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
                    speechError = "Sesi anlayamadim, tekrar dene."
                    return
                }
                val isCorrect = normalizeSpeechForQuiz(spokenText) == normalizeSpeechForQuiz(question.correctAnswer)
                onAnswer(isCorrect, spokenText)
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
    fun startSpeakingRecognition() {
        speechError = ""
        transcript = ""
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
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FBFF))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = question.title,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF13233A),
            fontSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, Color(0xFFDDEAF7))
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Ses",
                    tint = Color(0xFF2F80ED),
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEAF3FF), CircleShape)
                        .padding(8.dp)
                )
                Spacer(Modifier.width(14.dp))
                Text(
                    text = question.sentence,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF172B4D),
                    fontSize = 22.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(Modifier.height(26.dp))

        LottieAnimation(
            composition = speakingComposition,
            iterations = 1,
            modifier = Modifier
                .size(190.dp)
                .clip(RoundedCornerShape(32.dp))
        )

        Spacer(Modifier.weight(1f))

        Surface(
            modifier = Modifier
                .size(96.dp)
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
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isRecording) "Dinleniyor..." else "Mikrofona bas ve tekrar et",
            color = if (isRecording) Color(0xFFE84B6A) else Color(0xFF60758F),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        if (transcript.isNotBlank()) {
            Text(
                text = "\"$transcript\"",
                color = Color(0xFF13233A),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        if (speechError.isNotBlank()) {
            Text(
                text = speechError,
                color = Color(0xFFB54747),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        TextButton(onClick = onCannotSpeak, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "ŞİMDİ KONUŞAMAM",
                color = Color(0xFF60758F),
                fontSize = 14.sp,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FBFF))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(shape = RoundedCornerShape(50), color = Color(0xFFEAF3FF)) {
            Text(
                text = "YENI KALIP",
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                color = Color(0xFF246BFE),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
        }

        Text(
            text = "Eksik olan kelimeyi seç",
            color = Color(0xFF13233A),
            fontSize = 27.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Black
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 7.dp,
            border = BorderStroke(1.dp, Color(0xFFE2ECF6))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (pattern.title.isNotBlank()) {
                    Text(pattern.title, color = Color(0xFF7B5CFF), fontWeight = FontWeight.Black, fontSize = 15.sp)
                }
                HighlightedPatternText(pattern.exampleOne, pattern.highlightOne)
                HighlightedPatternText(pattern.exampleTwo, pattern.highlightTwo)
                
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
                    color = Color(0xFF13233A),
                    fontSize = 21.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pattern.options.forEach { option ->
                val isSelected = selectedOption == option
                Button(
                    onClick = { selectedOption = option },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFFEAF3FF) else Color.White,
                        contentColor = Color(0xFF13233A)
                    ),
                    border = BorderStroke(2.dp, if (isSelected) Color(0xFF2F80ED) else Color(0xFFD9E7F5))
                ) {
                    Text(option, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
            AnimatedVisibility(visible = selectedOption != null) {
                Button(
                    onClick = { selectedOption?.let(onAnswer) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                ) {
                    Text("KONTROL ET", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun HighlightedPatternText(sentence: String, highlight: String) {
    val highlightColor = Color(0xFF7B5CFF)
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
        color = Color(0xFF344760),
        fontSize = 19.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    )
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
    val allWords = remember(question) { question.correctWords + question.extraWords }
    val bankIds by rememberSaveable(question.id) { mutableStateOf(allWords.indices.toList().shuffled()) }
    var selectedIds by rememberSaveable(question.id) { mutableStateOf(emptyList<Int>()) }
    var showWrongHint by rememberSaveable(question.id) { mutableStateOf(false) }
    val selectedWords = selectedIds.map { allWords[it] }
    val isCorrect = selectedWords == question.correctWords

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7FBFF))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (false) Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = .08f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("KOMBO x$comboCount", color = Color(0xFF8D7CFF), fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape),
                        color = Color(0xFF38D67A),
                        trackColor = Color.White.copy(alpha = .12f)
                    )
                }
                Surface(shape = RoundedCornerShape(22.dp), color = Color.White.copy(alpha = .08f)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF30B8FF), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("$energy", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(50), color = Color(0xFFEAF3FF)) {
                    Text(
                        "YENI KELIME",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color(0xFF246BFE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Text("Aşağıdaki cümleyi çevir", color = Color(0xFF13233A), fontSize = 27.sp, fontWeight = FontWeight.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF1CB0F6), CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Seslendir", tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(question.qEnglish, color = Color(0xFF172B4D), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
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
                    color = Color(0xFFFF8A9A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            WordBank(
                bankIds = bankIds,
                selectedIds = selectedIds,
                allWords = allWords,
                onChoose = ::chooseWord,
                modifier = Modifier.weight(1f)
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
                    .height(58.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1CB0F6),
                    disabledContainerColor = Color(0xFFE9F1FA),
                    disabledContentColor = Color(0xFF8A9AAF)
                )
            ) {
                Text("KONTROL ET", fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }

        if (false) SuccessPanel(
            visible = isCorrect,
            onContinue = { onAnswer(true, question.fullAnswerTurkish) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
    Surface(
        modifier = modifier
            .heightIn(min = 120.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color(0xFFDDEAF7))
    ) {
        FlowRow(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (selectedIds.isEmpty()) {
                Text("Cevabını burada oluştur", color = Color(0xFF8A9AAF), fontSize = 15.sp)
            }
            selectedIds.forEach { id ->
                WordChip(text = allWords[id], selected = true, onClick = { onRemove(id) })
            }
        }
    }
}

@Composable
fun WordChipPlaceholder(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF2F4F8),
        border = BorderStroke(1.dp, Color(0xFFE2ECF6))
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
            if (id in selectedIds) {
                WordChipPlaceholder(text = allWords[id])
            } else {
                WordChip(text = allWords[id], selected = false, onClick = { onChoose(id) })
            }
        }
    }
}

@Composable
fun WordChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFFEAF3FF) else Color.White,
        shadowElevation = if (selected) 9.dp else 5.dp,
        border = BorderStroke(1.dp, if (selected) Color(0xFF2F80ED) else Color(0xFFD9E7F5))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
            color = Color(0xFF13233A),
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SuccessPanel(visible: Boolean, onContinue: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier.fillMaxWidth(),
        enter = slideInVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) { it } + fadeIn(),
        exit = slideOutVertically(animationSpec = spring()) { it } + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            color = Color(0xFF38D67A),
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Doğru!", color = Color(0xFF102118), fontSize = 30.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF102118))
                ) {
                    Text("DEVAM ET", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ComboMilestoneDialog(onContinue: () -> Unit) {
    val dogRes = rememberRawResId("flirting_dog")
    val dogComposition by rememberLottieComposition(
        dogRes?.let { LottieCompositionSpec.RawRes(it) } ?: LottieCompositionSpec.RawRes(R.raw.star_badge)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = .24f))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 18.dp
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
                    color = Color(0xFF13233A),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED))
                ) {
                    Text("DEVAM ET", fontWeight = FontWeight.Black)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Dogru cevap",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF60758F)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = correctAnswer,
                style = TextStyle(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF13233A)
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F80ED)),
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
    onContinue: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCorrect) "Harika! ✨" else "Doğru Cevap:",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828))
            )
            if (!isCorrect) {
                Text(text = correctAnswer, style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Black))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFFF5252)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("DEVAM ET", fontWeight = FontWeight.Bold, color = Color.White)
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
    onDone: () -> Unit
) {
    val safeScore = score.coerceIn(0, totalQuestions.coerceAtLeast(0))
    val safeBlankAnswers = blankAnswers.coerceIn(0, (totalQuestions - safeScore).coerceAtLeast(0))
    val wrongAnswers = (totalQuestions - safeScore - safeBlankAnswers).coerceAtLeast(0)
    val percentage = if (totalQuestions > 0) safeScore * 100 / totalQuestions else 0
    val result = when {
        totalQuestions > 0 && safeScore == totalQuestions -> QuizResultPresentation(
            R.raw.premium, "Muhteşem sonuç!", Color(0xFF147D64),
            listOf(
                "$safeScore doğruyla kusursuz bir sonuç çıkardın. Tebrikler!",
                "Bütün soruları doğru cevapladın; emeğinin karşılığı harika görünüyor.",
                "$safeScore sorunun tamamında doğru seçim yaptın. Gerçekten çok iyi!",
                "Tam puan! Bu konudaki hakimiyetin kendini açıkça gösteriyor."
            )
        )
        wrongAnswers == 1 -> QuizResultPresentation(
            R.raw.star_badge, "Harika iş!", Color(0xFF2878B5),
            listOf(
                "$safeScore doğru yaptın; yalnızca bir soru seni zorladı. Çok güçlü bir sonuç!",
                "Tek bir soruyla fark kaçtı, ama konuya çok iyi hakimsin.",
                "$safeScore doğru cevapla hedefe çok yaklaştın. Tebrikler!",
                "Neredeyse kusursuzdu. Bir soruyu gözden geçirmen yeterli olacak."
            )
        )
        percentage >= 70 -> QuizResultPresentation(
            R.raw.star_blast, "Çok iyi gidiyorsun!", Color(0xFF5A6F1D),
            listOf(
                "$safeScore doğru cevapla güçlü bir sonuç elde ettin. Tebrikler!",
                "%$percentage başarı, konuyu iyi kavradığını gösteriyor.",
                "$safeScore soruyu doğru çözdün. Biraz tekrar seni daha da ileri taşıyacak.",
                "Gayet iyi bir sonuç! Zorlandığın sorular gelişimin için güzel bir rehber."
            )
        )
        else -> QuizResultPresentation(
            R.raw.cat_fishing_on_moon, "Her deneme bir adım", Color(0xFF52606D),
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
            .background(Color(0xFFF7F9FA))
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
            color = Color(0xFF17212B),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            message,
            fontSize = 17.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF425466),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultCountCard("Doğru", safeScore, Color(0xFF21845B), Color(0xFFE7F5EE), Modifier.weight(1f))
            ResultCountCard("Boş", safeBlankAnswers, Color(0xFF60758F), Color(0xFFE9EEF4), Modifier.weight(1f))
            ResultCountCard("Yanlış", wrongAnswers, Color(0xFFB54747), Color(0xFFFBECEC), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("%$percentage başarı", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = result.color)
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = result.color)
        ) {
            Text("TAMAMLA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(14.dp))
        LottieAnimation(
            composition = coinComposition,
            iterations = 1,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = when {
                earnedCoins > 0 && isReinforcement ->
                    "+$earnedCoins coin: Yeni kırmızı pekiştirme yıldızların için kazandın."
                earnedCoins > 0 ->
                    "+$earnedCoins coin: İlk çözümündeki yıldız başarın için kazandın."
                isReinforcement ->
                    "Bu tur yeni kırmızı yıldız açılmadığı için coin eklenmedi."
                else ->
                    "Coin kazanmak için quizi en az bir yıldızla tamamlayabilirsin."
            },
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF52606D),
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
                color = Color(0xFF24527A)
            )
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
                    color = Color(0xFF24527A)
                )
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 14.sp, color = Color.Gray)
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
    var selectedOption by remember(data) { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Text(
                // 🚨 "Hangisi" ve soru işaretleri kaldırıldı, sadece kelime kaldı.
                text = data.questionTurkish,
                modifier = Modifier.padding(20.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp, // Kelime tek başına kalacağı için fontu biraz büyüttüm
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        val shuffledOptions = remember(data) {
            val zipped = data.options.zip(data.optionImages.ifEmpty { List(data.options.size) { null } })
            zipped.shuffled()
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(shuffledOptions) { index, pair ->
                val optionText = pair.first
                val isSelected = selectedOption == optionText
                val imageToDisplay = pair.second ?: R.drawable.cat_logo

                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            selectedOption = optionText
                            tts.speakEnglish(optionText)
                        }
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Color(0xFFE3F2FD) else Color.White
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        // VisualQuizContent içindeki Image kısmını bul ve şununla değiştir:
                        Image(
                            painter = painterResource(
                                id = imageToDisplay
                            ),
                            contentDescription = optionText,
                            modifier = Modifier
                                .size(data.imageSize.dp)
                                .padding(data.imagePadding.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = optionText,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF1CB0F6) else Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (selectedOption != null) {
                    onAnswer(data.isCorrect(selectedOption), selectedOption!!)
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(58.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedOption != null) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)
            )
        ) {
            Text(
                text = "KONTROL ET",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)
            )
        }
    } // 🚨 Column burada kapanıyor
} // 🚨 VisualQuizContent fonksiyonu burada kapanıyor

@Composable
fun ListeningQuizContent(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (String) -> Unit
) {
    var isTextVisible by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFF1CB0F6)
    val shuffledOptions = remember(data.id) { data.options.shuffled() }

    // ✅ YENİ: Soru dili kontrolü ile otomatik seslendirme
    LaunchedEffect(data.id) {
        isTextVisible = false
        tts.speakEnglish(data.question)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // --- KARAKTER VE KONUŞMA BALONU ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
        ) {
            // ✅ MANUEL BOYUTLANDIRMA BURADA UYGULANIYOR
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(data.imageSize.dp) // Manuel size
                    .padding(data.imagePadding.dp) // Manuel padding
            )

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp),
                shadowElevation = 6.dp,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Soru Metni Alanı (Gizli/Açık)
                    Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                        if (isTextVisible) {
                            Text(
                                text = data.question,
                                color = Color.Black,
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            )
                        } else {
                            Text(
                                text = "???",
                                color = Color.Gray.copy(alpha = 0.3f),
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { isTextVisible = !isTextVisible }) {
                            Text(
                                text = if (isTextVisible) "Gizle" else "Metni Gör",
                                fontSize = 12.sp,
                                color = primaryColor
                            )
                        }

                        IconButton(
                            onClick = {
                                tts.speakEnglish(data.question)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = primaryColor)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- CEVAP BUTONLARI ---
        // --- CEVAP BUTONLARI (GÜNCELLENDİ) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            shuffledOptions.forEach { option ->
                Button(
                    onClick = {
                        onAnswer(option)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    border = BorderStroke(2.dp, Color(0xFFE5E5E5))
                ) {
                    Text(
                        text = option,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    )
                }
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
    // ✅ YENİ MODEL UYUMU: data.options artık direkt String listesi!
    val pairs: List<Pair<String, String>> = remember(data) {
        // .map { it.text } kısmını sildik çünkü zaten String elimizde.
        // data.options: ["Elma", "Armut"], correctAnswer: "apple,pear" varsayalım.
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
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B4B4B))
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
    val borderColor = when {
        isMatched -> Color(0xFFC3F6D5)
        isSelected -> Color(0xFF1CB0F6)
        else -> Color(0xFFE5E5E5)
    }

    val bgColor = when {
        isMatched -> Color(0xFFE8F5E9)
        isSelected -> Color(0xFFE3F2FD)
        else -> Color.White
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
                color = if (isMatched) Color(0xFF4CAF50) else Color.Black
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








