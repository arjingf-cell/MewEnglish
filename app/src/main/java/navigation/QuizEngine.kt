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
import android.content.Context
import java.util.Locale
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.TextStyle
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
    TRANSLATION // Boşluk doldurma (boslukdoldurma)
}
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
    val transcriptTurkish: String? = null
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
    val transcript: String? = null
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
                transcriptTurkish = item.transcript
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
                    if (isStreakActive) StarParticleEffect(Modifier.align(Alignment.CenterEnd))
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
    health: Int = 5
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(health) }
    var score by remember { mutableIntStateOf(0) }
    var consecutiveCorrect by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    var showSheet by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var triggerStars by remember { mutableStateOf(false) }

    val currentQuestion = quizList.getOrNull(currentIndex)
    val isStreakActive = consecutiveCorrect >= 3

    if (quizCompleted || hearts <= 0) {
        QuizResultScreen(navController, score, quizList.size, onDone = { onQuizComplete() })
        return
    }

    if (currentQuestion == null) return

    // ✅ TTS'nin çalışması için lambda'yı (Boolean, String) alacak şekilde güncelledik
    val onAnswerResult: (Boolean, String) -> Unit = { isCorrect: Boolean, selectedText: String ->
        ttsHelper.speak(selectedText)
        // 🔊 Şıkka tıklandığı an seslendirme yapması için:
        ttsHelper.speak(selectedText)

        isAnswerCorrect = isCorrect
        if (isCorrect) {
            consecutiveCorrect++
            score++
            triggerStars = true // ⭐ Yıldızları tetikle
        } else {
            consecutiveCorrect = 0
            hearts--
        }
        showSheet = true
    }

    // ✅ contentAlignment = Alignment.BottomCenter: Paneli dibe yaslar
    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.BottomCenter) {

        Column(modifier = Modifier.fillMaxSize()) {
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
                    else -> Unit
                }
            }
        }

        // ⭐ YILDIZLAR: Ekranın tam ortasında patlayıp gidecek
        if (triggerStars) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ComboStarsAnimation(onAnimationFinish = { triggerStars = false })
            }
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
    navController: NavController, // Navigasyon için gerekli
    score: Int,
    totalQuestions: Int,
    onDone: () -> Unit // Butona basınca ne olacağı
) {
    val percentage = if (totalQuestions > 0) (score.toFloat() / totalQuestions * 100).toInt() else 0
    val wrongAnswers = totalQuestions - score

    // 🐈 Yüzdeye göre KEDİLİ Mesaj ve İkon Belirleme
    val (message, messageColor, catIcon) = when {
        percentage == 100 -> Triple("Vaay, patilerim alev aldı! 🔥 Tam bir dil canavarı Miyav! 🏆", Color(0xFF4CAF50), "🐱🔥")
        percentage >= 80 -> Triple("Miyavtastic! Neredeyse mükemmel bir av! 🌟 Kutlarım!", Color(0xFF8BC34A), "😸✨")
        percentage >= 50 -> Triple("İyi gidiyorsun! Biraz daha mama yersen efsane olacaksın. 💪🐱", Color(0xFFFF9800), "😼🐟")
        percentage >= 20 -> Triple("Fena değil ama kelimeleri tekrar etmelisin, tırmık yeme! 📚", Color(0xFFFF5722), "😿📖")
        else -> Triple("Vazgeçme! Her hata yeni bir tırmalama fırsatıdır. Miyav! 🌱", Color(0xFFFF5252), "😿🌿")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 📊 DAİRESEL YÜZDE GÖSTERGESİ ---
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier.size(180.dp),
                color = messageColor,
                strokeWidth = 14.dp,
                trackColor = messageColor.copy(alpha = 0.1f)
            )
            Text(text = catIcon, style = TextStyle(fontSize = 72.sp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "%$percentage",
            style = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Black, color = messageColor)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF01579B), lineHeight = 30.sp),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 📊 DOĞRU / YANLIŞ KARTLARI ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = Color(0xFFE8F5E9)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Doğru", color = Color(0xFF2E7D32), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$score", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                }
            }

            Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(20.dp), color = Color(0xFFFFEBEE)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Yanlış", color = Color(0xFFC62828), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("$wrongAnswers", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFFC62828))
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(65.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(containerColor = messageColor)
        ) {
            Text("TAMAMLA (MİYAV!)", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(data.options) { index, optionText ->
                val isSelected = selectedOption == optionText
                val imageToDisplay = data.optionImages.getOrNull(index) ?: R.drawable.cat_logo

                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            selectedOption = optionText
                            tts.speak(optionText)
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
                            // 🚨 ESKİ HALİ: painterResource(id = data.imageRes) -> Bu hepsini aynı yapar.
                            // ✅ YENİ HALİ: Aşağıdaki satır her şık için listedeki ilgili resmi çeker.
                            painter = painterResource(
                                id = data.optionImages.getOrNull(index) ?: R.drawable.cat_logo
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

    // ✅ YENİ: Soru dili kontrolü ile otomatik seslendirme
    LaunchedEffect(data.id) {
        isTextVisible = false
        // ✅ DÜZELTİLDİ: Artık isTurkish parametresi gönderilmiyor
        tts.speak(data.question)
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
                                // ✅ Parametreyi sildik, ML Kit otomatik algılayacak
                                tts.speak(data.question)
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
            data.options.forEach { option ->
                Button(
                    onClick = {
                        // ✅ Burada da sadece 'option' metnini göndermek yeterli
                        tts.speak(option)
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

    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var selectedRight by remember { mutableStateOf<String?>(null) }

    val matchedLeftWords = remember { mutableStateListOf<String>() }

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








