package com.example.ngilizcequizapp

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.example.ngilizcequizapp.QuizRepository
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent



import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.animation.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.DisposableEffect
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.NightsStay
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

val BrokenBlue = Color(0xFF4A6984)
val WaveWhite = Color(0xFFFFFFFF)
var isDarkMode by mutableStateOf(false)
class MainActivity : ComponentActivity() {

    private lateinit var speechAssistant: SpeechAssistant

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        speechAssistant = SpeechAssistant(this)

        // ✅ Kullanıcıyı yormadan paketi kontrol et ve yoksa yönlendir
        speechAssistant.checkAndDownloadTurkish(this)

        setContent {
            AppRootScreen(speechAssistant = speechAssistant)
        }
    }

    override fun onDestroy() {
        if (::speechAssistant.isInitialized) {
            // Büyük 'D' yerine küçük 'd' kullanıyoruz
            speechAssistant.shutdown()
        }
        super.onDestroy()
    }
}



// ✅ Sınıf artık dışarıda, her yerden import edilebilir!
class SpeechAssistant(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isReady = false
    private val languageIdentifier = LanguageIdentification.getClient()

    // 🔴 EKSİK OLANLAR BURADAYDI:
    private var lastSpeakTime: Long = 0L // Long olduğunu ve 0 olduğunu belirttik

    init {
        tts = TextToSpeech(context, this, "com.google.android.tts")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady = true
            tts?.setSpeechRate(0.8f)
            // ✅ Motor hazır olduktan sonra kontrolü yap
            // Not: Bunun için init'te aldığın context'i bir değişkende saklaman gerekir
            // checkAndDownloadTurkish(myContext)
        }
    }

    fun speak(text: String) {
        val currentTime = System.currentTimeMillis()

        // Zaman kilidi kontrolü
        if (currentTime - lastSpeakTime < 1000) return

        if (!isReady || text.isBlank()) return

        lastSpeakTime = currentTime

        // 🔴 EKSİK OLAN DİĞER TANIM BURADA:
        val hasTurkishChar = text.any { it in "çğıöşüÇĞİÖŞÜıİ" }

        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                android.util.Log.d("TTS_LOG", "📢 TEK ÇAĞRI: $languageCode | Metin: $text")

                val locale = if (languageCode == "tr" || hasTurkishChar || languageCode == "und") {
                    Locale("tr", "TR")
                } else {
                    Locale.US
                }

                tts?.setLanguage(locale)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id_$currentTime")
            }
    }

    fun checkAndDownloadTurkish(context: Context) {
        // 1. Önce motorun dil durumunu sessizce sorgula
        val result = tts?.isLanguageAvailable(Locale("tr", "TR"))

        // 2. SADECE VE SADECE dil dosyaları kesinlikle yoksa (LANG_MISSING_DATA) indir de.
        // Eğer dil yüklüyse (LANG_AVAILABLE) veya ülke destekleniyorsa hiçbir şey yapma.
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            android.util.Log.d("TTS_LOG", "Dosyalar gerçekten eksik, yönlendiriliyor.")

            val installIntent = Intent().apply {
                action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } else {
            // Dil yüklü veya destekleniyor; kullanıcıya hiçbir şey hissettirme.
            android.util.Log.d("TTS_LOG", "Türkçe paketi zaten sistemde mevcut. Sessiz mod aktif.")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

// --- SÖZLÜK OBJESİ (TEK VE EKSİKSİZ HALİ) ---
object AppDictionary {
    var words: Map<String, String> = emptyMap()

    fun initialize(context: Context) {
        if (words.isNotEmpty()) return
        try {
            val jsonString = context.assets.open("dictionary.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<Map<String, String>>() {}.type
            words = Gson().fromJson(jsonString, type)
            Log.d("AppDictionary", "Sözlük başarıyla yüklendi: ${words.size} kelime.")
        } catch (e: Exception) {
            Log.e("AppDictionary", "HATA: Sözlük yüklenemedi! Mesaj: ${e.message}")
        }
    }

    fun getMeaning(word: String): String {
        return words[word.lowercase(Locale.ROOT)] ?: "Meaning not found"
    }
}



// --- QUIZ MODELİ ---
// MainActivity içindeki o meşhur yer:
enum class QuestionType {
    MULTIPLE_CHOICE,
    FILL_IN_BLANK,
    // 👈 Sadece bu 3 satırı ekle, gerisine dokunma!
    VISUAL,
    LISTENING,
    MATCHING
}

data class NewQuestion(
    val type: QuestionType,
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)
// --- QUIZ EKRANI FONKSİYONU ---
// Global bir state olarak MainActivity içinde veya Screen dışında tanımlayabilirsin


@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun AdvancedQuizScreen(
    quizTitle: String = "My Special Day",
    question: RemoteQuestion, // Supabase'den gelen yeni modelimiz
    onNext: (Boolean) -> Unit,
    isDarkMode: Boolean
) {
    // --- 1. DURUM DEĞİŞKENLERİ ---
    var rmsValue by remember { mutableStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }
    var selectedAnswer by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var currentCatImage by remember { mutableIntStateOf(R.drawable.bench) }

    // --- 2. DİNAMİK RENKLER ---
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val surfaceColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF1F1F1)
    val actionBlue = Color(0xFF3897F0)

    val context = LocalContext.current
    val happyCats = listOf(R.drawable.bench, R.drawable.truee, R.drawable.what)
    val angryCats = listOf(R.drawable.wrong, R.drawable.wrongg)

    val startListening = rememberSpeechRecognizerState(
        { rmsValue = it },
        { isListening = it }
    ) { spokenText ->
        val foundOption = question.options.find { it.equals(spokenText, ignoreCase = true) }
        selectedAnswer = foundOption ?: spokenText.lowercase(Locale.getDefault())
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startListening() else Toast.makeText(context, "İzin gerekli", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ÜST BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quizTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.LightGray else Color.Gray.copy(alpha = 0.6f)
            )

            IconButton(onClick = { if (!isAnswered) launcher.launch(android.Manifest.permission.RECORD_AUDIO) }) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Speak", tint = actionBlue)
            }
        }

        // --- DİNAMİK İÇERİK ALANI (Visual veya Text) ---
        Column(
            modifier = Modifier.padding(vertical = 20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🚀 BURASI YENİ: Eğer görsel varsa gösteriyoruz
            if (question.image_url.isNotEmpty()) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.size(180.dp).padding(bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    coil.compose.AsyncImage(
                        model = question.image_url,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            // SORU METNİ
            val textParts = question.text.split("........")
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = textColor, fontSize = 20.sp)) {
                        append(textParts[0])
                    }
                    val wordPlaceholder = if (selectedAnswer.isEmpty()) " ________ " else " $selectedAnswer "
                    withStyle(style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = if (isAnswered) (if (isCorrect) Color(0xFF4CAF50) else Color.Red) else actionBlue,
                        textDecoration = TextDecoration.Underline
                    )) {
                        append(wordPlaceholder)
                    }
                    if (textParts.size > 1) {
                        withStyle(style = SpanStyle(color = textColor, fontSize = 20.sp)) {
                            append(textParts[1])
                        }
                    }
                },
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // --- SEÇENEKLER ---
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            question.options.forEach { option ->
                val isSelected = selectedAnswer == option
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(!isAnswered) { selectedAnswer = option },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) actionBlue else surfaceColor,
                    border = BorderStroke(1.dp, if (isSelected) actionBlue else Color.LightGray.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = option,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // SES DALGASI VEYA KEDİ TEPKİSİ
        if (isListening) {
            WaveformView(rmsDb = rmsValue)
        } else if (isAnswered) {
            Image(painter = painterResource(id = currentCatImage), contentDescription = null, modifier = Modifier.size(140.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // KONTROL BUTONU
        Button(
            onClick = {
                if (!isAnswered) {
                    // 🚀 BURASI YENİ: RemoteQuestion'da 'answer' kullanıyoruz
                    isCorrect = selectedAnswer.trim().lowercase() == question.answer.trim().lowercase()
                    currentCatImage = if (isCorrect) happyCats.random() else angryCats.random()
                    isAnswered = true
                } else {
                    onNext(isCorrect)
                    selectedAnswer = ""
                    isAnswered = false
                }
            },
            enabled = selectedAnswer.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = actionBlue)
        ) {
            Text(text = if (!isAnswered) "CHECK" else "NEXT", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
// --- 1. SORU LİSTESİ (10 SORU) ---
val specialDayQuestions = listOf(
    NewQuestion(QuestionType.FILL_IN_BLANK, "Today is a ........ day for me.", listOf("special", "angry", "cold", "blue", "fast", "small", "sad"), "special"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "I ........ up at seven o'clock.", listOf("wake", "sleep", "run", "eat", "drink", "go", "work"), "wake"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "I wash my ........ and brush my teeth.", listOf("face", "shoe", "book", "car", "desk", "wall", "pen"), "face"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "I wear my favorite ........ today.", listOf("shirt", "cloud", "garden", "milk", "spoon", "floor", "bird"), "shirt"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "I have ........ with my family.", listOf("breakfast", "water", "game", "night", "sun", "tree", "bus"), "breakfast"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "My mother cooks a delicious ........ for me.", listOf("cake", "phone", "chair", "lamp", "grass", "stone", "hat"), "cake"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "I see a big ........ in the garden.", listOf("present", "window", "road", "sea", "mountain", "fire", "ice"), "present"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "My friend gives me a ........ gift.", listOf("nice", "heavy", "dark", "loud", "poor", "wrong", "thick"), "nice"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "We ........ song together.", listOf("sing", "read", "write", "jump", "climb", "swim", "fly"), "sing"),
    NewQuestion(QuestionType.FILL_IN_BLANK, "I feel very ........ at the end of the day.", listOf("happy", "short", "ugly", "busy", "dry", "rich", "thin"), "happy")
)






@Composable
fun rememberSpeechRecognizerState(
    onRmsUpdate: (Float) -> Unit,           // Ses şiddeti için
    onListeningStateChange: (Boolean) -> Unit, // Dinleme durumu için
    onResult: (String) -> Unit              // Sonuç için
): () -> Unit {
    val context = LocalContext.current
    val recognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val intent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
        }
    }

    val listener = remember {
        object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                onListeningStateChange(false) // Dinleme bitti
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                onResult(data?.get(0) ?: "")
            }
            override fun onReadyForSpeech(params: Bundle?) {
                onListeningStateChange(true) // Dinleme başladı
            }
            override fun onRmsChanged(rmsdB: Float) {
                onRmsUpdate(rmsdB) // Ses şiddetini gönder
            }
            override fun onError(error: Int) {
                onListeningStateChange(false)
            }
            override fun onBeginningOfSpeech() {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    DisposableEffect(Unit) {
        recognizer.setRecognitionListener(listener)
        onDispose { recognizer.destroy() }
    }
    return { recognizer.startListening(intent) }
}


@Composable
fun WaveformView(rmsDb: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF4A6984), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(20) {
                // Dinamik yükseklik ayarı
                val barHeight = (10 + (rmsDb.coerceIn(0f, 15f) * 4)).dp
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(barHeight)
                        .background(Color.White, shape = RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

//KARANLIK MOD
// Renk Tanımlamaları
val LightBackground = Color.White
val DarkBackground = Color(0xFF121212) // Derin Siyah

val LightText = Color.Black
val DarkText = Color.White

val LightSurface = Color(0xFFF1F1F1) // Buton arka planı (Açık)
val DarkSurface = Color(0xFF1E1E1E) // Buton arka planı (Koyu)




@Composable
fun MainActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.elevatedButtonColors(containerColor = Color.White, contentColor = color),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 11.sp))
        }
    }
}

// --- 4. WORD ORDERING QUIZ (ANAHTAR KELİME YAMALI) ---


// Bu sınıfı fonksiyonların dışına ekleyin
data class QuizLevelData(
    val name: String,
    val description: String,
    val color: Color
)
@Composable
fun ThemeToggle() {
    var isDarkMode by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isDarkMode) "Dark Mode On" else "Dark Mode Off",
            color = if (isDarkMode) Color.White else Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))

        // 🚨 İŞTE O TUŞ!
        Switch(
            checked = isDarkMode,
            onCheckedChange = { newValue ->
                isDarkMode = newValue // Global değişkeni değiştirir
            },
            thumbContent = {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.NightsStay else Icons.Default.WbSunny,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}
