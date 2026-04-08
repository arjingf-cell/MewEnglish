package com.example.ngilizcequizapp



import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.paint

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.runtime.remember

import androidx.compose.ui.text.font.FontFamily
import com.example.ngilizcequizapp.quiz.engine.QuestionType
import com.example.ngilizcequizapp.quiz.engine.MainQuizScreen
import androidx.compose.ui.draw.scale
import java.util.Locale
import androidx.compose.material.icons.filled.* // Tüm standart ikonları içeri aktarır
import android.content.Context
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ngilizcequizapp.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONArray
import java.util.*
import com.example.ngilizcequizapp.QuizRepository

private val PureWhite = Color(0xFFFFFFFF)
private val SoftGradientBlue = Color(0xFFE3F2FD)


val bgColor = if (isDarkMode) Color(0xFF121212) else Color.White
val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
val textColor = if (isDarkMode) Color.White else Color(0xFF2C3E50)
val subTextColor = textColor.copy(alpha = 0.7f)
val borderColor = if (isDarkMode) Color(0xFF333333) else Color(0xFFE0F2F1)

val IceBlue = Color(0xFFE3F2FD)      // En üstteki ferah buz mavisi
val DeepMarine = Color(0xFF0D1117)   // Kartlar için koyu lacivert-siyah
val RichBlack = Color(0xFF040608)    // En dipteki ana siyah
val ActionBlue = Color(0xFF2196F3)   // Butonlar ve vurgular için canlı mavi
val BrokenIceBlue = Color(0xFFB0BEC5)
val LightIceBlue = Color(0xFFE1F5FE)


data class DictionaryWord(
    val en: String,
    val tr: String
)

@Composable
fun ActivitySmallCard(
    title: String,
    emoji: String? = null,
    imageRes: Int? = null,
    accentColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) { // <--- BU PARANTEZİN AÇIK OLDUĞUNDAN EMİN OL

    // ✅ BURASI: Fonksiyonun tam gövdesi.
    // Hata alıyorsan bu satır yukarıdaki { parantezinden ÖNCE yazılmış demektir.
    val darkMode = LocalIsDarkMode.current
    val surfaceColor = if (darkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (darkMode) Color.White else Color(0xFF2C3E50)

    Surface(
        modifier = modifier.size(width = 120.dp, height = 130.dp),
        shape = RoundedCornerShape(24.dp),
        color = surfaceColor,
        shadowElevation = if (darkMode) 0.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                } else if (emoji != null) {
                    Text(text = emoji, fontSize = 40.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
object WordDictionary {

    var wordEntries: Map<String, String> = emptyMap()

    fun initialize(context: Context) {
        if (wordEntries.isNotEmpty()) return

        try {
            val jsonString = context.assets
                .open("dictionary.json")
                .bufferedReader()
                .use { it.readText() }

            val jsonArray = JSONArray(jsonString)
            val map = mutableMapOf<String, String>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (!obj.has("en") || !obj.has("tr")) continue

                val en = obj.optString("en").lowercase().trim()
                val tr = obj.optString("tr").trim()

                if (en.isNotEmpty() && tr.isNotEmpty()) {
                    map[en] = tr
                }
            }
            wordEntries = map
            Log.d("DICT_TEST", "Loaded Word count: ${wordEntries.size}")
        } catch (e: Exception) {
            Log.e("DICT_TEST", "Error: ${e.message}")
        }
    }
} // 👈 BU PARANTEZDEN SONRA HİÇBİR ŞEY KALMAMALI
@Composable
fun DictionaryScreen() {
    val wordList = AppDictionary.words.toList().sortedBy { it.first }

    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        item { Text("Sözlüğüm", style = MaterialTheme.typography.headlineLarge, color = Color.Yellow, modifier = Modifier.padding(bottom = 16.dp)) }

        items(wordList) { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = pair.first, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = pair.second, color = Color.Gray, fontSize = 16.sp)
            }
            Divider(color = Color.DarkGray, thickness = 0.5.dp)
        }
    }
}

@Composable
fun WordPopup(
    word: String,
    ttsHelper: com.example.ngilizcequizapp.TextToSpeechHelper, // ✅ Seslendirme için eklendi
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var dictionaryReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        AppDictionary.initialize(context)
        dictionaryReady = true
    }

    val cleanWord = word.lowercase().replace(Regex("[^a-z]"), "")

    val translation = remember(dictionaryReady, cleanWord) {
        if (!dictionaryReady) "Yükleniyor..."
        else AppDictionary.words[cleanWord] ?: "Anlamı sözlükte bulunamadı."
    }

    val VividIceBlue = Color(0xFF5D9CEC)
    val DeepBlue = Color(0xFF2C3E50)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // ✅ HOPARLÖR BUTONU (Sol Üst)
                    IconButton(onClick = { ttsHelper.speak(cleanWord) }) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Listen",
                            tint = VividIceBlue,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    // KAYDET BUTONU (Sağ Üst)
                    IconButton(onClick = {
                        val newWord = SavedWord(word = cleanWord, meaning = translation)
                        if (!WordStorage.savedWords.any { it.word == cleanWord }) {
                            WordStorage.savedWords.add(newWord)
                            WordStorage.saveToDisk(context)
                        }
                        onDismiss()
                    }) {
                        Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = "Kaydet", tint = VividIceBlue, modifier = Modifier.size(30.dp))
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.quiz_cat),
                    contentDescription = "Profesör Kedi",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = cleanWord.uppercase(),
                    color = VividIceBlue,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Box(modifier = Modifier
                .fillMaxWidth()
                .background(VividIceBlue.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(16.dp)) {
                Text(text = "$translation", color = DeepBlue, fontSize = 18.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = VividIceBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Anladım!", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}
// =========================================================================
// I. VERİ MODELLERİ VE MOCK VERİLERİ
// =========================================================================

data class Answer(
    val text: String = "",
    val isCorrect: Boolean = false
)

data class Question(
    val questionText: String = "",
    val answers: List<Answer> = emptyList(),
    val correctTextAnswer: String = "",
    val type: String = "MULTIPLE_CHOICE", // MULTIPLE_CHOICE, FILL_IN_THE_BLANK
)

data class Quiz(
    val id: Int,
    val name: String = "",
    val difficulty: String = "Medium",
    val questions: List<Question> = emptyList(),
)

data class DailyPhrase(
    val english: String,
    val turkish: String
)

data class WordEntry(
    val word: String,
    val translation: String,
    val exampleSentence: String,
    val isMastered: Boolean = false
)

// Mock Quiz Verisi (Sadece test amaçlı)
val mockQuizzes = listOf(
    Quiz(
        id = 1,
        name = "Basit İngilizce Kelimeler",
        difficulty = "Easy",
        questions = listOf(
            Question(
                questionText = "The opposite of 'hot' is...",
                answers = listOf(
                    Answer("Warm"),
                    Answer("Cold", isCorrect = true),
                    Answer("Tepid"),
                    Answer("Burning")
                ),
                type = "MULTIPLE_CHOICE"
            ),
            Question(
                questionText = "Boşluğu Doldur: I enjoy listening to ___ music on the weekend.",
                correctTextAnswer = "loud",
                type = "FILL_IN_THE_BLANK"
            ),
            Question(
                questionText = "A large sea animal is a...",
                answers = listOf(
                    Answer("Cat"),
                    Answer("Whale", isCorrect = true),
                    Answer("Bird"),
                    Answer("Mouse")
                ),
                type = "MULTIPLE_CHOICE"
            )
        )
    ),
    Quiz(
        id = 2,
        name = "Gündelik Konuşmalar",
        difficulty = "Medium",
        questions = listOf(
            Question(
                questionText = "Boşluğu Doldur: She is running ___ late for the meeting.",
                correctTextAnswer = "a little",
                type = "FILL_IN_THE_BLANK"
            ),
            Question(
                questionText = "What do you say when you leave a place at night?",
                answers = listOf(
                    Answer("Good morning"),
                    Answer("Good night", isCorrect = true),
                    Answer("Hello"),
                    Answer("Goodbye")
                ),
                type = "MULTIPLE_CHOICE"
            )
        )
    )
)

val mockDailyStreak = 7 // Örnek Günlük Seri Sayısı
val mockDailyPhrase = DailyPhrase(
    english = "A blessing in disguise",
    turkish = "Her şeyde bir hayır vardır"
)

// Mock Sözlük Verisi
val mockWords = listOf(
    WordEntry("Meticulous", "Titiz, Dikkatli", "The project requires meticulous planning."),
    WordEntry("Ephemeral", "Geçici, Fani", "Fame is often ephemeral."),
    WordEntry("Ubiquitous", "Her yerde var olan", "Smartphones are ubiquitous today."),
    WordEntry("Nefarious", "Kötü Şöhretli, Alçak", "He was involved in a nefarious scheme."),
    WordEntry("Conundrum", "Bilmeceli Sorun, Muamma", "The logistics of the event presented a conundrum."),
    WordEntry("Alacrity", "Şevk, İsteklilik", "She accepted the invitation with alacrity."),
    WordEntry("Mellifluous", "Bal gibi tatlı, Akıcı", "The speaker had a deep, mellifluous voice."),
    WordEntry("Disparate", "Temelden farklı", "The two cultures were widely disparate."),
    WordEntry("Penultimate", "Sondan bir önceki", "This is the penultimate chapter of the book."),
    WordEntry("Juxtapose", "Yan yana koymak", "The artist likes to juxtapose light and darkness.")
)

// Günlük Kelimeler için ilk 10 kelimeyi al (Eğer mockWords 10'dan azsa, tamamını alır)
val mockDailyWords = mockWords.take(10)


// =========================================================================
// II. NAVİGASYON YAPILANDIRMASI (ROTALAR, HOST VE BOTTOM BAR)
// =========================================================================

// Ekran Rotaları ve İkonları
sealed class Screen(
    val route: String,
    val title: String,
    val iconRes: Int? = null,              // PNG için
    val icon: ImageVector? = null          // Material Vector için
) {
    object Splash : Screen("splash_screen", "Splash", icon = Icons.Default.Circle)

    // ✅ PNG Yüklediklerin (iconRes kullanıyoruz)
    object Home : Screen("home_screen", "Ana Sayfa", iconRes = R.drawable.home)
    object Progress : Screen("progress_screen", "İlerleme", iconRes = R.drawable.kupa)
    object Quiz : Screen("quiz_screen", "Quiz", iconRes = R.drawable.quiz_icon)
    object Word : Screen("word_screen", "Sözlük", iconRes = R.drawable.dicti)

    // ✅ PNG Yüklemediklerin (icon kullanmaya devam ediyoruz - Dokunmadık)
    object ListeningLevel : Screen("listening_levels", "Listening", icon = Icons.Default.Hearing)
    object Guide : Screen("guide_screen", "Guide", icon = Icons.Default.Info)
    object ReadingLevel : Screen("reading_levels", "Okuma Seviyesi", icon = Icons.Default.LibraryBooks)

    object ListeningSelection : Screen("listening_selection/{level}", "Quiz", icon = Icons.Default.Audiotrack) {
        fun createRoute(level: String) = "listening_selection/$level"
    }

    object QuizTree : Screen("quiz_tree/{lvl}", "Quiz Haritası", icon = Icons.Default.AccountTree) {
        fun createRoute(lvl: String) = "quiz_tree/$lvl"
    }

    object ReadingContent : Screen("reading_content/{lvl}", "Okuma Parçası", icon = Icons.Default.MenuBook) {
        fun createRoute(lvl: String) = "reading_content/$lvl"
    }

    object QuizDetail : Screen("quiz_detail/{quizId}", "Quiz Detay", icon = Icons.Default.CheckCircle)
}

// Alt Bar Menüsü - Burası artık yeni nesneleri kullanıyor
val bottomBarItems = listOf(Screen.Home, Screen.Progress, Screen.Quiz, Screen.Word)




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    navController: NavHostController,
    drawerState: DrawerState,
    scope: CoroutineScope,
    content: @Composable () -> Unit
) {
    val isDarkMode = LocalIsDarkMode.current

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // --- DİNAMİK RENKLER ---
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val BrokenIceBlue = Color(0xFFB0CFFF)
    val VividIceBlue = Color(0xFF5D9CEC)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = backgroundColor, // Menü arka planı
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                // ✅ HATA ÇÖZÜMÜ: Sadece gerekli parametreleri gönderiyoruz.
                // DrawerContent fonksiyonunda 'modifier' parametresi varsa bile burada yazmana gerek yok,
                // çünkü DrawerContent zaten kendi içinde Modifier'ını yönetiyor.
                DrawerContent(
                    navController = navController,
                    scope = scope,
                    drawerState = drawerState
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Image(
                                painter = painterResource(id = R.drawable.logo_tp_app),
                                contentDescription = "Logo",
                                modifier = Modifier.height(35.dp),
                                contentScale = ContentScale.Fit,
                                // ✅ Karanlık modda logonun beyaza yakın tonda parlaması için filtre
                                colorFilter = if (isDarkMode) ColorFilter.tint(Color.White) else null
                            )
                        },
                        actions = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = BrokenIceBlue
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = BrokenIceBlue.copy(alpha = 0.2f))
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = backgroundColor,
                    tonalElevation = 12.dp
                ) {
                    val items = listOf(Screen.Home, Screen.Progress, Screen.Quiz, Screen.Word)

                    items.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                // ✨ PNG ve Vector Karar Mekanizması + %50 Büyütülmüş Boyutlar
                                if (screen.iconRes != null) {
                                    // PNG yüklü olanlar için (Home, Kupa, Quiz, Dicti)
                                    Image(
                                        painter = painterResource(id = screen.iconRes),
                                        contentDescription = screen.title,
                                        // 🚀 %50 BÜYÜTÜLDÜ: 30dp -> 45dp, 26dp -> 39dp
                                        modifier = Modifier.size(if (isSelected) 45.dp else 39.dp),
                                        alpha = if (isSelected) 1f else 0.6f // Biraz daha belirgin olması için 0.5'ten 0.6'ya çektim
                                    )
                                } else if (screen.icon != null) {
                                    // PNG olmayanlar için Vector ikonlar da orantılı büyütüldü
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(if (isSelected) 36.dp else 30.dp),
                                        tint = if (isSelected) VividIceBlue else VividIceBlue.copy(alpha = 0.5f)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) VividIceBlue else VividIceBlue.copy(alpha = 0.5f),
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 10.sp // İkonlar büyüdüğü için yazıyı 1 tık küçülterek alanı dengeledik
                                )
                            },
                            selected = isSelected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = VividIceBlue,
                                unselectedIconColor = VividIceBlue.copy(alpha = 0.5f),
                                indicatorColor = Color.Transparent
                            ),
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            // ✅ BURASI ÇOK KRİTİK: Bütün sayfaların arka planı buradan kararır
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(backgroundColor)
            ) {
                content()
            }
        }
    }
}
// Bu fonksiyon AppScaffold'un içinde DEĞİL, tamamen dışında olmalı.
@Composable
fun DrawerContent(
    navController: NavHostController? = null,
    scope: CoroutineScope? = null,
    drawerState: DrawerState? = null,
    modifier: Modifier = Modifier
) {
    val isDarkMode = LocalIsDarkMode.current  // ✅ bunu ekle
    val currentBg = if (isDarkMode) Color(0xFF121212) else Color.White
    val currentText = if (isDarkMode) Color.White else Color.Black

    Column(
        modifier = modifier // Gelen modifier'ı buraya işliyoruz
            .fillMaxHeight()
            .width(280.dp)
            .background(currentBg)
            .padding(24.dp)
    ) {
        Text(
            text = "MewEnglish",
            color = Color(0xFF3897F0),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        DrawerItem("Profil", Icons.Default.Person, currentText)
        DrawerItem("İstatistik", Icons.Default.Star, currentText)

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = currentText.copy(alpha = 0.1f))

        // AYARLAR TUŞU
        DrawerItem(
            label = "Settings",
            icon = Icons.Default.Settings,
            textColor = currentText,
            modifier = Modifier.clickable {
                scope?.launch { drawerState?.close() }
                navController?.navigate("settings")
            }
        )
    }
}


@Composable
fun EliteHomeScreen(
    navController: NavHostController,
    showQuizPrompt: Boolean,
    onDismissQuizPrompt: () -> Unit
) {
    // 🎨 Renk Tanımları
    val BrokenIceBlueText = Color(0xFF2C3E50)
    val SoftIceBlue = Color(0xFFB3E5FC)
    val VividIceBlue = Color(0xFF5D9CEC)

    // ✨ Animasyon Tanımları
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnim")
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // --- ARKA PLAN GRADYANI ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SoftIceBlue.copy(alpha = 0.6f), Color.White),
                        startY = 0f,
                        endY = 800f
                    )
                )
        )

        // --- ✨ SABİT LOGO: MEWENGLISH (Yeşil alanda parlayan) ---


        // --- 🚀 ANA İÇERİK LİSTESİ ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 80.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // --- 1. HOŞ GELDİN ---
            item {
                Column {
                    Text(
                        text = "Hoş geldin,",
                        style = MaterialTheme.typography.headlineLarge,
                        color = BrokenIceBlueText,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Bugün İngilizce öğrenmeye hazır mısın?",
                        style = MaterialTheme.typography.titleMedium,
                        color = BrokenIceBlueText.copy(alpha = 0.7f)
                    )
                }
            }

            // --- 2. READING KARTI ---
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clickable { navController.navigate(Screen.ReadingLevel.route) },
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, Color(0xFFE0F2F1))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .background(Color(0xFFF0F8FF), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cat_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Reading", color = BrokenIceBlueText, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "Profesör Kedi ile hikaye oku", color = BrokenIceBlueText.copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                        Icon(imageVector = Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color(0xFF3897F0), modifier = Modifier.size(18.dp))
                    }
                }
            }

            // --- 3. AKSİYON PANELİ ---
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(4) { index ->
                            Box(modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .background(
                                    color = if (index == 0) VividIceBlue else Color(0xFFE5E5EA),
                                    shape = RoundedCornerShape(10.dp)
                                ))
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DashboardSmallCard(title = "Mistake Jar", imageRes = R.drawable.mistake_case, color = Color(0xFFFF6B6B), modifier = Modifier.weight(1f), onClick = { /* MistakeJar */ })
                        DashboardSmallCard(title = "Word", imageRes = R.drawable.kelimelerim, color = VividIceBlue, modifier = Modifier.weight(1f), onClick = { navController.navigate(Screen.Word.route) })
                    }
                }
            }

            // --- 4. GÖREVLER (Mission) ---
            item {
                Column {
                    Text(text = "Bugünkü Mission", color = BrokenIceBlueText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()) {
                        LazyRow(
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(start = 4.dp, end = 4.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item { ActivitySmallCard(title = "Sheet Test", emoji = "🧪", accentColor = Color(0xFFFFAB91), modifier = Modifier.clickable { navController.navigate("sheet_screen") }) }
                            item { ActivitySmallCard(title = "Listening", imageRes = R.drawable.listening, accentColor = Color(0xFF4FC3F7), modifier = Modifier
                                .scale(1.05f)
                                .clickable { navController.navigate(Screen.ListeningLevel.route) }) }
                            item { ActivitySmallCard(title = "Idiom", emoji = "🌟", accentColor = Color(0xFF29B6F6)) }
                        }
                    }
                }
            }

            // --- 5. GUIDE KARTI ---
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { navController.navigate(Screen.Guide.route) },
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier
                            .size(70.dp)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFF5F5F5), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                            Image(painter = painterResource(id = R.drawable.rehber), contentDescription = null, modifier = Modifier.size(50.dp))
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Guide", color = Color(0xFF2C3E50), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text(text = "Uygulama özelliğini keşfet", color = Color(0xFF2C3E50).copy(alpha = 0.6f), fontSize = 13.sp)
                        }
                        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                    }
                }
            }
        } // LazyColumn Sonu

        // --- QUIZ POP-UP ---
        if (showQuizPrompt) {
            AlertDialog(
                onDismissRequest = onDismissQuizPrompt,
                containerColor = Color.White,
                shape = RoundedCornerShape(32.dp),
                confirmButton = {
                    Button(onClick = { onDismissQuizPrompt(); navController.navigate("quiz_path_test") }, colors = ButtonDefaults.buttonColors(containerColor = VividIceBlue)) {
                        Text("Hadi Başlayalım", color = Color.White)
                    }
                },
                dismissButton = { TextButton(onClick = onDismissQuizPrompt) { Text("Belki Sonra", color = Color.Gray) } },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Image(painter = painterResource(id = R.drawable.quiz_cat), contentDescription = null, modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape))
                        Text(text = "Quiz Zamanı!", fontWeight = FontWeight.ExtraBold, color = BrokenIceBlueText)
                    }
                },
                text = { Text(text = "Bugün kısa bir quiz çözerek başlamak ister misin?", textAlign = TextAlign.Center) }
            )
        }
    }
}
@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF3897F0), // Mew Mavisi ikonlar
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MewTopAppBar(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    // 🎨 Renkleri burada tekrar tanımlayalım (Dışarıdan gelmiyorsa hata vermemesi için)
    val BrokenIceBlueText = Color(0xFF2C3E50)
    val VividIceBlue = Color(0xFF1CB0F6) // Logonun ana rengi

    val infiniteTransition = rememberInfiniteTransition(label = "topBarAnim")

    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column {
        TopAppBar(
            title = {
                Text(
                    text = "MewEnglish",
                    modifier = Modifier
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .padding(start = 8.dp),
                    style = TextStyle(
                        fontFamily = FontFamily.Cursive,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VividIceBlue,
                        shadow = Shadow(
                            color = VividIceBlue.copy(alpha = 0.6f),
                            blurRadius = glowRadius,
                            offset = Offset(0f, 0f)
                        )
                    )
                )
            },
            actions = {
                IconButton(onClick = {
                    scope.launch { drawerState.open() }
                }) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = BrokenIceBlueText // Sağdaki menü butonu rengi
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        // Alt çizgiyi de senin buz mavisi tonuna sabitleyelim
        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0xFFE0F2F1)
        )
    }
}


// BU FONKSİYONLARIN HİÇBİRİ BİRBİRİNİN İÇİNDE OLMAMALI!

@Composable
fun ReadingStartCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE53935))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("📚 OKUMA PARÇALARI", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp)
        }
    }
}

@Composable
fun ReadingLevelSelectionScreen(
    navController: NavHostController,
    onLevelSelected: (String) -> Unit
) {
    val levels = listOf(
        ListeningListeningLevelData("A1", "Beginner", Color(0xFFE0F7FA), Color(0xFF1CB0F6)),
        ListeningListeningLevelData("A2", "Elementary", Color(0xFFE1F5FE), Color(0xFF03A9F4)),
        ListeningListeningLevelData("B1", "Intermediate", Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        ListeningListeningLevelData("B2", "Upper", Color(0xFFFFF8E1), Color(0xFFFFC107)),
        ListeningListeningLevelData("C1", "Advanced", Color(0xFFFFEBEE), Color(0xFFE91E63)),
        ListeningListeningLevelData("C2", "Mastery", Color(0xFFF3E5F5), Color(0xFF9C27B0))
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFE3F2FD), Color.White),
        startY = 0f,
        endY = 1500f
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            // Başlık Bölümü
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF2C3E50))
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Reading Level",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2C3E50)
                        )
                    )
                    Text(
                        text = "Okuma becerini seç ve hikayeye başla",
                        fontSize = 14.sp,
                        color = Color(0xFF2C3E50).copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Reading İçin Özelleşmiş Liste
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(levels) { levelItem ->
                    ReadingLevelSelectionCard( // 👈 İsim güncellendi
                        data = levelItem,
                        onClick = { onLevelSelected(levelItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingLevelSelectionCard( // 👈 Yeni isim
    data: ListeningListeningLevelData,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, data.accentColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL: Kitap/Okuma İkonu Alanı
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(data.backgroundColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Reading olduğu için sembolik bir kitap veya harf koyabiliriz
                Text(
                    text = "📖",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // ORTA: Seviye Bilgileri
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.id,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = data.description,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // SAĞ: İlerleme Butonu Görünümü
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(data.backgroundColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = data.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Yardımcı fonksiyon
fun getLevelDescription(level: String): String {
    return when(level) {
        "A1" -> "Başlangıç"
        "A2" -> "Temel"
        "B1" -> "Orta"
        "B2" -> "Üst Orta"
        "C1" -> "İleri"
        "C2" -> "Yetkin"
        else -> ""
    }
}

// 1. LİSTE EKRANI: Seviyeye göre hikayeleri listeler
// Dosyanın en üstündeki importları kontrol et, şu olmalı:
// import androidx.compose.foundation.lazy.items


@Composable
fun ReadingListScreen(
    level: String, // 👈 Seviyeyi dışarıdan (navigasyondan) alıyoruz
    navController: NavHostController,
    onReadingSelected: (String) -> Unit // 👈 Tıklama aksiyonunu alıyoruz
) {
    // 1. ADIM: Hikaye Listeleri (Doğru yer burası)
    val readingMap = mapOf(
        "A1" to listOf("My Special Day", "Daily Routine", "My Family", "My Dream House", "At the Grocery Store", "Summer Weather", "What Time Is It?", "My Favorite Hobbies", "Where Is The Bank?", "Feeling Sick", "Weekend In Paris", "Future Plans"),
        "A2" to listOf("A Trip to London", "Healthy Living", "Shopping Mall", "The New Restaurant", "My Last Vacation", "Interesting Jobs", "Cooking Dinner", "Meeting Friends"),
        "B1" to listOf("Global Economy", "Space Exploration", "Technology Trends", "The Power of Music", "Social Media Impact", "Life in the Future"),
        "B2" to listOf("Climate Change", "History of Art", "Psychology of Success", "Artificial Intelligence", "World Heritage Sites"),
        "C1" to listOf("Advanced Philosophy", "Quantum Mechanics", "Genetic Engineering", "Modern Sociology"),
        "C2" to listOf("Complex Literature Analysis", "Theories of Everything", "Political Strategy")
    )

    // 2. ADIM: Tıklanan seviyeye ait listeyi çekiyoruz
    val readings = readingMap[level] ?: emptyList()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Color(0xFFE3F2FD).copy(alpha = 0.5f),
                        Color(0xFFBBDEFB).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp), // MewEnglish padding standardı
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            item {
                // Geri butonu ve Başlık
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color(0xFF0288D1))
                    }
                    Text(
                        text = "$level Reading",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF0288D1),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            items(readings) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onReadingSelected(item) }, // 👈 Hata burada çözüldü
                    shape = RoundedCornerShape(24.dp), // Daha yumuşak köşeler
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE3F2FD)),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item,
                            color = Color(0xFF2C3E50), // BrokenIceBlueText
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF1CB0F6).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}
val SoftGray = Color(0xFF1C1C1E)    // Kartların iç rengi
// 2. DETAY EKRANI: Seçilen hikayenin içeriğini gösterir



// 3. ETKİLEŞİM ALANI: Metni kelimelere böler ve tıklatır



// Scaffold Yapısı (Bottom Bar)


@Composable
fun QuizListScreen(
    onNavigateToMeeting: () -> Unit,
    onNavigateToThisThat: () -> Unit,
    onNavigateToWord: () -> Unit,      // ✅ KURAL: Word (Singular)
    onNavigateToProgress: () -> Unit,
    onNavigateToQuiz: () -> Unit,      // ✅ KURAL: Quiz (Singular)
    onQuizSelected: (String) -> Unit
) {
    // İçerik buraya...
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val levels = listOf(
        QuizListeningLevelData("A1", "Beginner - Start your journey", Color(0xFF4CAF50)),
        QuizListeningLevelData("A2", "Elementary - Basic communication", Color(0xFF2196F3)),
        QuizListeningLevelData("B1", "Intermediate - Daily conversations", Color(0xFFFF9800)),
        QuizListeningLevelData("B2", "Upper Intermediate - Fluent speaking", Color(0xFFE91E63)),
        QuizListeningLevelData("C1", "Advanced - Complex topics", Color(0xFF9C27B0)),
        QuizListeningLevelData("C2", "Proficiency - Master level", Color(0xFF607D8B))
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFE1F5FE))) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header - Logo buraya gelecek
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quiz", // ✅ KURAL: Bold
                    style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Black),
                    color = Color(0xFF01579B)
                )
                Spacer(modifier = Modifier.height(12.dp))
                // Kategori Chip'leri

            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ ORTA 3 BUTON (İsimler MainQuizButton oldu)


                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 40.dp), thickness = 2.dp, color = Color.White.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Seviye Kartları
                levels.forEach { level ->
                    QuizLevelCard(level = level) { onQuizSelected(level.name) }
                }
                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}


@Composable
fun MainQuizButton( // ✅ İsmi "MainQuizButton" yaparak çakışmayı bitirdik
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp), // ✅ Daha yumuşak, modern kavisler
        color = Color.White,
        shadowElevation = 8.dp // ✅ Daha belirgin ve hoş bir derinlik
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            // İkonun arkasına hafif bir renk katmanı (Modern görünüm sırrı)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$title", // ✅ KURAL: Bold (Değişkeni bold yapıyoruz)
                style = TextStyle(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF2C3E50) // Daha profesyonel bir lacivert-siyah
                )
            )
        }
    }
}
//Alt Quiz Seçeneği İçin Küçük Tasarım
@Composable
fun SubQuizCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF0288D1))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}

@Composable
fun ConnectionLine() {
    Canvas(modifier = Modifier
        .height(30.dp)
        .width(2.dp)) {
        drawLine(
            color = Color(0xFF01579B).copy(alpha = 0.2f),
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
    }
}

// --- YARDIMCI BİLEŞENLER ---

// BU IMPORTU DOSYANIN EN ÜSTÜNE EKLEDİĞİNDEN EMİN OL:
// import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight


// ✅ KURAL: ListeningLevelData terimi bold ve eksiksiz tanımlandı
// ✅ KURAL: ListeningLevelData ismi korundu ve tüm hataları çözecek şekilde güncellendi
data class ListeningLevelData(
    val name: String,
    val desc: String,
    val color: Color,
    // 🚨 BURAYI DÜZELTTİK: 'type' yerine 'QuestionType' yazdık
    val type: QuestionType,
    val quizList: List<String> = listOf("Quiz 1", "Quiz 2", "Quiz 3"),
    val questionText: String = "",
    val imageRes: Int = 0
)

// Soru tiplerini belirleyen Enum (Hata vermemesi için bunu da ekle)


@Composable
fun LevelCard(level: ListeningLevelData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(20.dp),
        color = level.color,
        shadowElevation = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(level.name, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(16.dp))
            Text(level.desc, color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.5f))
        }
    }
}
// Yardımcı Seviye Seçim Bileşeni (Eğer yoksa bunu kullanabilirsin)
@Composable
fun LevelsSelectionScreen(onLevelSelected: (String) -> Unit) {
    // 1. ADIM: Birbirine çok yakın, ufak ton farkları olan Mavi Skalası
    val levels = listOf(
        QuizListeningLevelData("A1", "Başlangıç", Color(0xFFB3E5FC)), // Çok Açık Buz Mavisi
        QuizListeningLevelData("A2", "Temel", Color(0xFF81D4FA)),     // Bir ton koyu
        QuizListeningLevelData("B1", "Orta", Color(0xFF4FC3F7)),      // Orta ton
        QuizListeningLevelData("B2", "Üst Orta", Color(0xFF29B6F6)),  // Belirgin Mavi
        QuizListeningLevelData("C1", "İleri", Color(0xFF03A9F4)),     // Koyu Kırık Mavi
        QuizListeningLevelData("C2", "Master", Color(0xFF0288D1))     // En Koyu Kırık Mavi
    )

    // 2. ADIM: Kırık Buz Mavisi Gradyan Arka Plan
    val iceBlueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE1F5FE), // Üst kısım (Çok ferah)
            Color(0xFFB3E5FC), // Orta kısım
            Color(0xFF81D4FA).copy(alpha = 0.6f) // Alt kısım
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(iceBlueGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Level",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B) // Yazı rengi koyu lacivert/mavi
                ),
                modifier = Modifier.padding(vertical = 32.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(levels) { ListeningLevelData ->
                    QuizLevelCard(
                        level = ListeningLevelData,
                        onCardClick = { onLevelSelected(ListeningLevelData.name) }
                    )
                }
            }
        }
    }
}
@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(key1 = true) {
        delay(2000)
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Arka planın bembeyaz ve ferah kalması iyi bir seçim
        contentAlignment = Alignment.Center
    ) {
        Image(
            // BURAYI DEĞİŞTİRDİK: Eski 'splashscreen' yerine yeni logonu yazdık
            painter = painterResource(id = R.drawable.logo_tp_app),
            contentDescription = "Profesör Kedi",
            modifier = Modifier.size(300.dp), // 400.dp çok büyük gelirse 300'e çekebilirsin
            contentScale = ContentScale.Fit
        )
    }
}
// Genel Yer Tutucu Ekran
@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$title Ekranı Yapım Aşamasında", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
fun DashboardSmallCard(
    title: String,
    icon: ImageVector? = null,
    imageRes: Int? = null,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val isDarkMode = LocalIsDarkMode.current  // ✅ eklendi
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White  // ✅ dinamik
    val textColor = if (isDarkMode) Color.White else Color(0xFF2C3E50)  // ✅ dinamik

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = RoundedCornerShape(24.dp),
            color = surfaceColor,  // ✅ dinamik
            shadowElevation = if (isDarkMode) 0.dp else 6.dp,  // ✅ koyu modda gölge kaldırıldı
            border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (imageRes != null) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.FillBounds
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = textColor,  // ✅ dinamik
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun MistakeJarScreen(navController: NavHostController) {
    val darkMode = LocalIsDarkMode.current
    val bgColor = if (darkMode) Color(0xFF121212) else Color.White
    val textColor = if (darkMode) Color.White else Color(0xFF2C3E50)
    val cardColor = if (darkMode) Color(0xFF1E1E1E) else Color(0xFFFFF3F3)
    val borderColor = if (darkMode) Color(0xFF333333) else Color(0xFFFF6B6B).copy(alpha = 0.3f)

    val mistakes = remember { mutableStateListOf("Accomplish", "Persistence", "Ambiguous", "Reluctant") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)  // ✅ dinamik
            .padding(24.dp)
    ) {
        Text(
            "Hata Torbası 🎒",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = textColor  // ✅ dinamik
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(mistakes) { word ->
                var isChecked by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .alpha(if (isChecked) 0.4f else 1f),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isChecked) Color.LightGray.copy(alpha = 0.2f) else cardColor,  // ✅ dinamik
                    border = BorderStroke(1.dp, borderColor)  // ✅ dinamik
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = word,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = textColor  // ✅ dinamik
                        )
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF6B6B))
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun WordsScreen(navController: NavController) {
    val context = LocalContext.current

    // Kelimeleri asset'ten yükle
    val allWords: List<WordItem> = remember { loadWordsFromAssets(context) }
    var searchQuery by remember { mutableStateOf("") }

    // Senin özel gradyan arka planın
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(SoftGradientBlue, Color.White),
        startY = 0f,
        endY = 1000f
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(24.dp))

            // Başlığın (Korundu)
            Text(
                text = "Dictionary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BrokenIceBlue
            )

            // Arama Çubuğun (Korundu)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query -> searchQuery = query },
                placeholder = { Text("Search word...", color = BrokenIceBlue.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BrokenIceBlue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = PureWhite,
                    unfocusedContainerColor = PureWhite,
                    focusedBorderColor = BrokenIceBlue,
                    unfocusedBorderColor = BrokenIceBlue.copy(alpha = 0.3f)
                )
            )

            // Filtreleme Mantığı
            val filteredWords = allWords.filter { wordItem: WordItem ->
                wordItem.en.contains(searchQuery, ignoreCase = true) ||
                        wordItem.tr.contains(searchQuery, ignoreCase = true)
            }

            // LİSTELEME ALANI
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(items = filteredWords) { displayWord ->
                    // ESKİ WordBalloon SİLİNDİ -> YENİ MAVİ BOOKMARKLI TASARIM GELDİ
                    DictionaryWordRow(
                        word = displayWord.en,
                        meaning = displayWord.tr
                    )
                }
            }
        }
    }
}
// 3. Kelime Kartı (Not Alanlı)
@Composable
fun SavedWordItem(savedWord: SavedWord) {
    // State'i kaydedilen kelimenin mevcut notuna bağlıyoruz
    var note by remember { mutableStateOf(savedWord.userNote) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, Color(0xFFB0CFFF).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Kelime Başlığı
            Text(
                text = savedWord.word,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2C3E50)
            )

            // Kelime Anlamı (Gri yerine daha okunaklı koyu mavi-gri yapıldı)
            Text(
                text = savedWord.meaning,
                color = Color(0xFF546E7A),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kullanıcı Notu Girişi (Placeholder eklendi, içi boş gelir)
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    savedWord.userNote = it // Belleğe kaydeder
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Not ekle...", fontSize = 12.sp, color = Color.LightGray)
                },
                textStyle = TextStyle(fontSize = 13.sp, color = Color.Black),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedBorderColor = Color(0xFF5D9CEC).copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}
@Composable
fun WordItemCard(word: String, meaning: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, Color(0xFFB0CFFF).copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Küçük bir işaret noktası
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color(0xFF5D9CEC), CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = word, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = meaning, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun WordItemRow(word: String, meaning: String) {
    // Kaydetme durumunu takip etmek için (Opsiyonel: tıklandığında rengi değişsin diye)
    var isSaved by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = word, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = meaning, color = Color.Gray, fontSize = 14.sp)
            }

            // İŞTE EKSİK OLAN KAYDET BUTONU
            IconButton(onClick = {
                isSaved = !isSaved
                // Buraya ileride: database.save(word) gelecek
            }) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                    contentDescription = "Kaydet",
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
@Composable
fun ProgressScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        Text("İlerlemen", color = Color(0xFF2C3E50), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // İlerleme çubuğu (Mew Mavisi)
        LinearProgressIndicator(
            progress = { 0.6f },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape),
            color = Color(0xFF3897F0), // Mew Mavisi
            trackColor = Color(0xFFE0F2F1) // Buz mavisi kanal
        )
    }
}

// Quizler Ekranı (Quizzes)
@Composable
fun QuizzesScreen(navController: NavHostController) { // Buraya navController parametresini ekledik
    val MewBlue = Color(0xFF3897F0)
    val BrokenIceBlueText = Color(0xFF2C3E50)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Sınavlar", // "Quizzes" yerine bunu da Türkçe yapabiliriz
            style = MaterialTheme.typography.headlineLarge,
            color = MewBlue,
            fontWeight = FontWeight.ExtraBold
        )

        // ... (Kalan Quiz kodların)
    }
}

data class MyWord(
    val en: String,
    val tr: String
)

// Sözlük Ekranı (Words)

@Composable
fun WordBalloon(word: WordItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PureWhite,
        border = BorderStroke(1.dp, BrokenIceBlue.copy(alpha = 0.5f)),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.en,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = BrokenIceBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = word.tr,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Black
                )
            }
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = BrokenIceBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// BU FONKSİYON SADECE BİR KEZ VE EN ALTTA OLMALI

// Sadece bu temiz hali kalsın

@Composable
fun WordRow(word: WordItem, isSpecial: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSpecial) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = word.en,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isSpecial) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = word.tr,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Listen",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}


// JSON'daki her bir kelimeyi temsil eder
@Serializable
data class WordItem(
    val en: String,
    val tr: String
)

// JSON dosyasını okuyan sihirli fonksiyon
// Sadece bu kalsın, diğer iki loadWordsFromAssets fonksiyonunu tamamen sil
fun loadWordsFromAssets(context: Context): List<WordItem> {
    val wordList = mutableListOf<WordItem>()
    try {
        // Dosyayı açmayı dene
        val inputStream = context.assets.open("dictionary.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = org.json.JSONArray(jsonString)
        for (i in 0 until jsonArray.length()) {
            try {
                val obj = jsonArray.getJSONObject(i)
                // getString yerine optString kullanmak daha güvenlidir (değer yoksa çökmez)
                wordList.add(
                    WordItem(
                        en = obj.optString("en", "Unknown"),
                        tr = obj.optString("tr", "Bilinmiyor")
                    )
                )
            } catch (innerE: Exception) {
                // Sadece o kelimede hata varsa atla, listeyi bozma
                continue
            }
        }
    } catch (e: Exception) {
        // Logcat'te "JSON_ERROR" yazısını arat, hatayı orada göreceksin
        android.util.Log.e("JSON_ERROR", "Dosya okunamadı: ${e.message}")
    }
    return wordList
}
@Composable
fun WordCard(english: String, turkish: String, accentColor: Color, textColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF9FBFF), // Kart içi çok hafif mavi
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = english.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 17.sp
                )
                Text(
                    text = turkish,
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            // Hoparlör ikonu (Görseli tamamlamak için)
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
// Kelime Kartı Bileşeni
@Composable
fun WordCard(wordEntry: WordEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Detayları Göster */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = wordEntry.word,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary // Kelimeyi Sarı ile vurgula
                )
                Text(
                    text = wordEntry.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = wordEntry.exampleSentence,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Kelimeyi ezberlediniz mi ikonu (şimdilik pasif)
            Icon(
                Icons.Default.Bookmark,
                contentDescription = "Kaydedildi",
                tint = if (wordEntry.isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Günlük Seri Kartı Bileşeni
@Composable
fun DailyStreakCard(streakDays: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = "Günlük Seri",
                    tint = MaterialTheme.colorScheme.primary, // Sarı ateş
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streakDays",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Günlük Seri",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

// Günün Deyimi Kartı Bileşeni (Uzun ve Kırmızı İkonlu)
@Composable
fun PhraseOfTheDayCard(phrase: DailyPhrase, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(140.dp), // Kart yüksekliği ayarlandı
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocalFireDepartment, // Ateş emojisi kullanıldı
                contentDescription = "Günün Deyimi",
                tint = MaterialTheme.colorScheme.secondary, // Kırmızı
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = phrase.english,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "(${phrase.turkish})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Kullanıcının isteği doğrultusunda alt kısma uzun metin eklendi
                Text(
                    text = "Bu deyim, beklenmedik zorlukların veya kötü görünen olayların bile zamanla kişiye fayda sağlayabileceği anlamına gelir.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

// Quiz Kartı Bileşeni
@Composable
fun QuizCard(quiz: Quiz, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LibraryBooks,
                contentDescription = "Quiz İkonu",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary // Sarı İkon
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${quiz.questions.size} Soru | Zorluk: ${quiz.difficulty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Filled.ArrowForward,
                contentDescription = "İlerle",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// =========================================================================
// IV. QUIZ DETAY VE ÇÖZÜM UYGULAMALARI (QuizDetailScreen ve İç Bileşenler)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizDetailScreen(navController: NavHostController, quizId: Int?) {
    val context = LocalContext.current

    // 1. Verileri Repository'den çekiyoruz (Örnek: getQuizOne)
    val questions = remember { QuizRepository.getQuizOne() }

    // 2. Ses yardımcıları (Hibrit Yapı)
    val speechAssistant = remember { SpeechAssistant(context) }
    val turkishTts = remember { TurkishTextToSpeechHelper(context) }

    // 3. Can Hesaplama (%30 Mantığı)
    val initialHearts = remember(questions) {
        kotlin.math.ceil(questions.size * 0.3).toInt().coerceAtLeast(1)
    }

    // 4. State'ler
    var currentIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(initialHearts) }
    var quizCompleted by remember { mutableStateOf(false) }

    // 5. Mevcut Soru
    val currentQuestion = questions.getOrNull(currentIndex)

    // Arkaplan Gradiyenti
    val iceBlueGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA))
    )

    // Merkezi Cevap Yönetimi
    fun handleAnswer(isCorrect: Boolean) {
        if (isCorrect) {
            score++
        } else {
            hearts--
        }

        // Bitiş Kontrolü (Can biterse veya sorular biterse)
        if (hearts <= 0 || currentIndex >= questions.size - 1) {
            quizCompleted = true
        } else {
            currentIndex++
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(iceBlueGradient)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- 🟢 ÜST BAR (Geri Butonu ve Can Göstergesi) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color(0xFF01579B)
                    )
                }

                // Dinamik Can Göstergesi (Kalp İkonlu)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = hearts.toString(),
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF01579B))
                    )
                }
            }

            if (quizCompleted) {
                QuizResultScreen(
                    navController = navController,
                    score = score,
                    totalQuestions = questions.size,
                    onDone = { navController.popBackStack() }
                )
            } else if (currentQuestion != null) {
                // --- 🔵 İLERLEME ÇUBUĞU ---
                QuestionProgress(currentIndex + 1, questions.size)

                Spacer(modifier = Modifier.height(32.dp))

                // --- 🎯 SORU İÇERİK ALANI ---
                Box(modifier = Modifier.weight(1f)) {
                    when (currentQuestion.questionType) {

                        QuestionType.VISUAL -> {
                            VisualQuizContent(
                                data = currentQuestion,
                                tts = speechAssistant,
                                onAnswer = { isCorrect, selectedText ->
                                    speechAssistant.speak(selectedText)
                                    handleAnswer(isCorrect)
                                }
                            )
                        }

                        QuestionType.LISTENING -> {
                            ListeningQuizContent(
                                data = currentQuestion,
                                tts = speechAssistant,
                                ttsTurkish = turkishTts,
                                onAnswer = { isCorrect ->
                                    handleAnswer(isCorrect)
                                }
                            )
                        }

                        QuestionType.MATCHING -> {
                            eslestirme(
                                data = currentQuestion,
                                onAnswerResult = { isCorrect ->
                                    handleAnswer(isCorrect)
                                },
                                health = hearts,
                                progress = (currentIndex + 1).toFloat() / questions.size
                            )
                        }

                        QuestionType.FILL_IN_BLANK -> {
                            boslukdoldurma(
                                data = currentQuestion,
                                onAnswerResult = { isCorrect, selectedText ->
                                    speechAssistant.speak(selectedText)
                                    handleAnswer(isCorrect)
                                },
                                health = hearts,
                                progress = (currentIndex + 1).toFloat() / questions.size,
                                ttsHelper = speechAssistant
                            )
                        }

                        else -> {
                            Text("Bilinmeyen Soru Tipi", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}



                // --- 🎯 SORU MOTORU BİTTİ ---
// Quiz Sonuç Ekranı
@Composable
fun QuizResultScreen(
    navController: NavHostController,
    score: Int,
    totalQuestions: Int, // ✅ Parametre ismin bu
    onDone: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFE1F5FE)
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ✅ Siyah Kedi Görseli (Instagram stili kutu yerine senin kedi olsun)
            Image(
                painter = painterResource(id = R.drawable.cat_logo), // Siyah kedi dosya adın
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Quiz Completed!", // ✅ KURAL: Bold
                style = TextStyle(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your Score", // ✅ KURAL: Bold
                style = TextStyle(fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            )

            // ✅ HATA BURADA DÜZELTİLDİ: 'total' yerine 'totalQuestions' yazıldı
            Text(
                text = "$score / $totalQuestions",
                style = TextStyle(
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF01579B)
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF01579B),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text = "Back to Home", // ✅ KURAL: Bold
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
// Soru İlerleme Çubuğu
@Composable
fun QuestionProgress(current: Int, total: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Soru $current / $total",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { current.toFloat() / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary, // Sarı ilerleme
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

// Çoktan Seçmeli Soru Bileşeni
@Composable
fun MultipleChoiceQuestion(
    question: Question,
    onAnswerSelected: (isCorrect: Boolean) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<Answer?>(null) }
    var isAnswered by remember { mutableStateOf(false) }

    // Renkleri temadan al
    val correctHighlight = MaterialTheme.colorScheme.primary // Sarı
    val wrongHighlight = MaterialTheme.colorScheme.secondary // Kırmızı

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 24.dp),
            color = MaterialTheme.colorScheme.onBackground
        )

        question.answers.forEach { answer ->
            val isCurrentSelection = selectedAnswer == answer
            val isCorrect = answer.isCorrect

            val buttonColor = when {
                !isAnswered -> MaterialTheme.colorScheme.surfaceVariant
                isCurrentSelection && isCorrect -> correctHighlight // Doğru Sarı
                isCurrentSelection && !isCorrect -> wrongHighlight // Yanlış Kırmızı
                !isCurrentSelection && isCorrect -> correctHighlight.copy(alpha = 0.5f) // Pasif Doğru Sarı
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            val contentColor = when {
                isAnswered && (isCurrentSelection && isCorrect) -> MaterialTheme.colorScheme.onPrimary
                isAnswered && (isCurrentSelection && !isCorrect) -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSurface
            }

            val borderColor = when {
                !isAnswered -> MaterialTheme.colorScheme.outline
                isCurrentSelection && isCorrect -> correctHighlight
                isCurrentSelection && !isCorrect -> wrongHighlight
                !isCurrentSelection && isCorrect -> correctHighlight.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.outlineVariant
            }

            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable(enabled = !isAnswered) {
                        selectedAnswer = answer
                        isAnswered = true
                        // onAnswerSelected(answer.isCorrect) // Bu çağırma, Sonraki Soru butonuna taşındı.
                    },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, borderColor),
                colors = CardDefaults.outlinedCardColors(containerColor = buttonColor, contentColor = contentColor),
                elevation = CardDefaults.outlinedCardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = answer.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.Medium
                    )
                    AnimatedVisibility(
                        visible = isAnswered,
                        enter = fadeIn(tween(500)),
                        exit = fadeOut(tween(500))
                    ) {
                        val iconColor = if (isCorrect) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = if (isCorrect) "Doğru" else "Yanlış",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bir sonraki soruya geçme butonu (Kırmızı)
        AnimatedVisibility(
            visible = isAnswered,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = { onAnswerSelected(selectedAnswer?.isCorrect ?: false) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = wrongHighlight) // Kırmızı
            ) {
                Text("Sonraki Soru", color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

// Boşluk Doldurma Soru Bileşeni
@Composable
fun FillInTheBlankQuestion(
    question: Question,
    onAnswerSubmitted: (isCorrect: Boolean) -> Unit
) {
    var userAnswer by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val correctColor = ActionBlue // Elit mavi
    val wrongColor = Color.Red

    val parts = question.questionText.split("___")
    val annotatedText = buildAnnotatedString {
        if (parts.isNotEmpty()) append(parts[0])
        if (isSubmitted && isCorrect != null) {
            withStyle(SpanStyle(color = if (isCorrect == true) correctColor else wrongColor, fontWeight = FontWeight.Black)) {
                append(" ${question.correctTextAnswer.uppercase()} ")
            }
        } else {
            withStyle(SpanStyle(color = ActionBlue, fontWeight = FontWeight.Bold)) { append(" (____) ") }
        }
        if (parts.size > 1) append(parts[1])
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = annotatedText, style = MaterialTheme.typography.headlineSmall, color = Color.White)

        OutlinedTextField(
            value = userAnswer,
            onValueChange = { userAnswer = it },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitted,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ActionBlue)
        )

        Button(
            onClick = {
                if (!isSubmitted) {
                    keyboardController?.hide()
                    isCorrect = userAnswer.trim().equals(question.correctTextAnswer.trim(), ignoreCase = true)
                    isSubmitted = true
                } else {
                    onAnswerSubmitted(isCorrect == true)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ActionBlue)
        ) {
            Text(if (!isSubmitted) "Cevabı Kontrol Et" else "Sonraki Soru")
        }
    }
}

// --- BU FONKSİYONLAR ARTIK DIŞARIDA VE BAĞIMSIZ ---


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EliteReadingEngine(content: String) { // İsmi benzersiz yaptık
    val context = LocalContext.current
    var dictionary by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    LaunchedEffect(Unit) { dictionary = loadLargeDictionary(context) }

    val words = content.split(" ")
    FlowRow {
        words.forEach { word ->
            val cleanWord = word.replace(Regex("[^a-zA-Z]"), "").lowercase()
            val translation = dictionary[cleanWord]
            Text(
                text = "$word ",
                modifier = Modifier.clickable(enabled = translation != null) { /* Dialog aç */ },
                color = if (translation != null) Color.White else Color.Gray
            )
        }
    }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingSectionContent(content: String) {
    val context = LocalContext.current
    var dictionary by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    var selectedWordForDialog by remember { mutableStateOf<String?>(null) }
    var selectedTranslation by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val loadedDict = withContext(Dispatchers.IO) { loadLargeDictionary(context) }
        dictionary = loadedDict
    }

    val words = remember(content) { content.split(Regex("\\s+")).filter { it.isNotBlank() } }

    Box {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            words.forEach { word ->
                val cleanWord = word.filter { it.isLetter() }.lowercase().trim()
                val translation = dictionary[cleanWord]

                Text(
                    text = "$word ",
                    modifier = Modifier
                        .clickable(enabled = translation != null) {
                            selectedWordForDialog = cleanWord
                            selectedTranslation = translation
                        }
                        .padding(vertical = 2.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (translation != null) Color(0xFF0288D1) else Color.Black,
                    fontWeight = if (translation != null) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 18.sp
                )
            }
        }

        if (selectedWordForDialog != null && selectedTranslation != null) {
            AlertDialog(
                onDismissRequest = {
                    selectedWordForDialog = null
                    selectedTranslation = null
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedWordForDialog!!.replaceFirstChar { it.uppercase() },
                            color = Color(0xFF2C3E50),
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )

                        // DÜZELTİLEN KAYDETME BUTONU
                        IconButton(onClick = {
                            // 1. Ortak depoya (WordStorage) ekleme yapıyoruz
                            WordStorage.savedWords.add(
                                SavedWord(
                                    word = selectedWordForDialog!!,
                                    meaning = selectedTranslation!!
                                )
                            )
                            // 2. Bildirim veriyoruz
                            android.widget.Toast.makeText(context, "${selectedWordForDialog} listeye eklendi!", android.widget.Toast.LENGTH_SHORT).show()

                            // 3. Pop-up'ı kapatıyoruz
                            selectedWordForDialog = null
                        }) {
                            Icon(
                                imageVector = Icons.Default.BookmarkAdd,
                                contentDescription = "Kaydet",
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                text = {
                    Text(
                        text = "Anlamı: $selectedTranslation",
                        color = Color.DarkGray,
                        fontSize = 18.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        selectedWordForDialog = null
                        selectedTranslation = null
                    }) {
                        Text("Anladım", color = Color(0xFF0288D1), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

data class SavedWord(
    val word: String,
    val meaning: String,
    var userNote: String = ""
)


object WordStorage {
    private const val PREFS_NAME = "mew_english_prefs"
    private const val KEY_SAVED_WORDS = "saved_words"

    // Ekranların yenilenmesini sağlayan liste (RAM)
    val savedWords = mutableStateListOf<SavedWord>()

    // Verileri Cihaz Hafızasına Kaydet
    fun saveToDisk(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(savedWords.toList())
        sharedPreferences.edit().putString(KEY_SAVED_WORDS, json).apply()
    }

    // Verileri Cihaz Hafızasından Yükle
    fun loadFromDisk(context: Context) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = sharedPreferences.getString(KEY_SAVED_WORDS, null)
        if (json != null) {
            val type = object : TypeToken<List<SavedWord>>() {}.type
            val list: List<SavedWord> = Gson().fromJson(json, type)

            savedWords.clear()
            savedWords.addAll(list)
        }
    }
}

@Composable
fun DictionaryWordRow(word: String, meaning: String) {
    val context = LocalContext.current

    // Uygulama açıldığında kelime kayıtlı mı diye listeye bakıyoruz
    var isSaved by remember {
        mutableStateOf(WordStorage.savedWords.any { it.word.equals(word, ignoreCase = true) })
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.replaceFirstChar { it.uppercase() },
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF2C3E50)
                )
                Text(text = meaning, color = Color.Gray, fontSize = 14.sp)
            }

            // --- KAYDETME VE KALICI HAFIZAYA YAZMA ---
            IconButton(onClick = {
                if (!isSaved) {
                    // 1. RAM listesine ekle
                    WordStorage.savedWords.add(SavedWord(word, meaning, ""))
                    isSaved = true
                    // 2. Telefonun kalıcı hafızasına kaydet
                    WordStorage.saveToDisk(context)
                    android.widget.Toast.makeText(context, "$word kaydedildi!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    // 1. RAM listesinden sil
                    WordStorage.savedWords.removeAll { it.word.equals(word, ignoreCase = true) }
                    isSaved = false
                    // 2. Güncel (silinmiş) halini kalıcı hafızaya kaydet
                    WordStorage.saveToDisk(context)
                    android.widget.Toast.makeText(context, "Listeden çıkarıldı", android.widget.Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkAdd,
                    contentDescription = "Kaydet",
                    tint = Color(0xFF0288D1), // İstediğin mavi tonu
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}
// --- BU KODU DOSYANIN EN ALTINA VEYA ESKİLERİNİ SİLİP YERİNE YAPIŞTIR ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadingDetailScreen(
    storyTitle: String, // Hikaye başlığı
    navController: NavController,
    // ttsHelper'ı opsiyonel yapıyoruz ( = null ), böylece navigasyonda yazmana gerek kalmaz
    ttsHelper: com.example.ngilizcequizapp.TextToSpeechHelper? = null
) {
    var showQuizPrompt by remember { mutableStateOf(false) }
    var showWordPopup by remember { mutableStateOf(false) }
    var selectedWordState by remember { mutableStateOf("") }

    val content = allStories[storyTitle] ?: "İçerik yükleniyor..."
    val scrollState = rememberScrollState()

    // ✅ Hata Çözümü: 'originalWords' hatasını gidermek için içeriği kelimelere bölüyoruz
    val wordsList = remember(content) {
        content.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFE3F2FD).copy(alpha = 0.5f))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // BAŞLIK ALANI
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "A1 LEVEL",
                    color = Color(0xFF0288D1),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = storyTitle, // 👈 'title' yerine 'storyTitle' yazdık
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
            }

            // METİN KARTI
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // ✅ Hata Çözümü: 'word' tipini belirledik ve 'wordsList' üzerinden döndük
                        wordsList.forEach { word: String ->
                            val cleanWord = word.replace(Regex("[^a-zA-Z]"), "").lowercase().trim()

                            Text(
                                text = "$word ",
                                modifier = Modifier
                                    .clickable {
                                        if (cleanWord.isNotEmpty()) {
                                            selectedWordState = cleanWord
                                            showWordPopup = true
                                        }
                                    }
                                    .padding(vertical = 2.dp),
                                style = TextStyle(fontSize = 18.sp, color = Color(0xFF2C3E50))
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { showQuizPrompt = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(65.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Okumayı Bitirdim! +20 XP", fontWeight = FontWeight.ExtraBold)
            }
        }

        // ✅ Hata Çözümü: Artık ttsHelper parametresini gönderiyoruz
        if (showWordPopup && ttsHelper != null) { // 👈 ttsHelper varsa göster diyoruz
            WordPopup(
                word = selectedWordState,
                ttsHelper = ttsHelper, // Artık null değil, kesinleşti!
                onDismiss = { showWordPopup = false }
            )
        }
    }
}
fun loadLargeDictionary(context: Context): Map<String, String> {
    val dictionaryMap = mutableMapOf<String, String>()
    try {
        context.assets.open("dictionary.json").bufferedReader().use { reader ->
            val jsonString = reader.readText()

            // DÜZELTME: Dosyan bir Liste ([]) olduğu için JSONArray kullanıyoruz
            val jsonArray = org.json.JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                // JSON içindeki "en" ve "tr" alanlarını alıyoruz
                val en = item.getString("en").lowercase().trim()
                val tr = item.getString("tr")
                dictionaryMap[en] = tr
            }
        }
        android.util.Log.d("SOZLUK_BASARI", "Toplam ${dictionaryMap.size} kelime yüklendi.")
    } catch (e: Exception) {
        android.util.Log.e("SözlükHatası", "Hata: ${e.message}")
        e.printStackTrace()
    }
    return dictionaryMap
}

// BU FONKSİYONU AYNI DOSYANIN EN ALTINA EKLE (VEYA BURADA KALSIN)

// --- BURASI ReadingDetailScreen'in DIŞIDIR ---

@Composable
fun ReadingSection(title: String, content: String) {
    // Karmaşık FlowRow yerine standart Text kullanıyoruz
    // Bu sayede @OptIn hatasından tamamen kurtuluyoruz
    Text(
        text = content,
        color = Color.White,
        fontSize = 18.sp,
        lineHeight = 28.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
fun HomeScreenContent() {
    // Senin buz mavisi rengin
    val BrokenIceBlue = Color(0xFFB0CFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // --- 1. GÜNLÜK HEDEF BAR (4 Segment) ---
        Text(
            text = "Günlük Hedefin",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            color = if (index == 0) BrokenIceBlue else Color(0xFFE5E5EA),
                            shape = RoundedCornerShape(10.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. HATA KUMBARASI VE KELİMELERİM (Yan Yana) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hata Kumbarası
            ActionCard(
                title = "Hata Kumbaram",
                subtitle = "5 Yeni Hata",
                icon = Icons.Default.Warning, // Geçici ikon
                color = Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f)
            )
            // Kelimelerim
            ActionCard(
                title = "Kelimelerim",
                subtitle = "124 Kayıt",
                icon = Icons.Default.Star,
                color = BrokenIceBlue,
                modifier = Modifier.weight(1f)
            )
        }

        // Buradan aşağısı senin Okuma Parçaların olacak...
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun HomeActionArea(
    onMistakeClick: () -> Unit,
    onWordsClick: () -> Unit
) {
    val BrokenIceBlue = Color(0xFFB0CFFF) // Senin favori rengin

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // --- 1. GÜNLÜK HEDEF BAR (4 Segment) ---
        Text(
            text = "Günlük Hedefin",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(
                            // Örnek: İlk iki bar dolu
                            color = if (index < 2) BrokenIceBlue else Color(0xFFE5E5EA),
                            shape = RoundedCornerShape(10.dp)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. YAN YANA AKSİYON KARTLARI ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hata Kumbarası Butonu
            ActionCard(
                title = "Hata Kumbaram",
                info = "5 Hata Var",
                icon = Icons.Default.AutoDelete,
                iconColor = Color(0xFFFF6B6B),
                modifier = Modifier.weight(1f),
                onClick = onMistakeClick
            )
            // Kelimelerim Butonu
            ActionCard(
                title = "Kelimelerim",
                info = "124 Kelime",
                icon = Icons.Default.BookmarkBorder,
                iconColor = BrokenIceBlue,
                modifier = Modifier.weight(1f),
                onClick = onWordsClick
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    info: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF7F9FC), // Çok hafif gri-mavi tonu
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            Text(text = info, color = Color.Gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun MyWordsScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }

    // Kaydedilen kelimeleri gözlemlenebilir bir state olarak alıyoruz
    // Not: WordStorage.savedWords'ün bir SnapshotStateList olması anlık güncellenme için önemlidir
    val allSavedWords = WordStorage.savedWords

    val filteredWords by remember(searchQuery, allSavedWords.size) {
        derivedStateOf {
            allSavedWords.filter { it.word.contains(searchQuery, ignoreCase = true) }
        }
    }

    // Ekranın tamamına yayılan Buz Mavisi Gradyan
    val iceBlueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE1F5FE), // Üst
            Color(0xFFB3E5FC), // Orta
            Color(0xFF81D4FA)  // Alt
        )
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(iceBlueGradient)) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Kelimelerim",
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Black),
                color = Color(0xFF01579B)
            )

            // Arama Çubuğu (Daha şeffaf ve temaya uygun)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                placeholder = { Text("Kelime ara...", color = Color(0xFF01579B).copy(alpha = 0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF01579B)) },
                shape = RoundedCornerShape(15.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    // Çerçeve renkleri
                    focusedBorderColor = Color(0xFF01579B),
                    unfocusedBorderColor = Color(0xFF01579B).copy(alpha = 0.3f),

                    // İç dolgu (container) rengi
                    focusedContainerColor = Color.White.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.5f),

                    // Yazı ve imleç renkleri
                    focusedTextColor = Color(0xFF01579B),
                    unfocusedTextColor = Color(0xFF01579B),
                    cursorColor = Color(0xFF01579B)
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                if (filteredWords.isEmpty()) {
                    item {
                        Text(
                            text = if (searchQuery.isEmpty()) "Henüz kelime kaydetmedin." else "Sonuç bulunamadı.",
                            color = Color(0xFF01579B).copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 20.dp)
                        )
                    }
                }

                items(filteredWords) { wordItem ->
                    // BURASI GÜNCELLENDİ: Bookmark butonu içeren yeni tasarım
                    SavedWordItemWithDelete(
                        wordItem = wordItem,
                        onRemove = {
                            // WordStorage içinde silme fonksiyonunu çağırıyoruz
                            WordStorage.savedWords.remove(wordItem)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedWordItemWithDelete(wordItem: SavedWord, onRemove: () -> Unit) { // 'WordData' yerine 'SavedWord' yapıldı
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.8f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Not: Eğer 'SavedWord' içinde 'word' ve 'meaning' isimleri farklıysa
                // (Örn: term, translation) onları buradaki isimlerle değiştir.
                Text(
                    text = wordItem.word,
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF01579B)
                )
                Text(
                    text = wordItem.meaning,
                    style = TextStyle(fontSize = 15.sp),
                    color = Color(0xFF2C3E50).copy(alpha = 0.7f)
                )
            }

            // Bookmark şeklinde sil butonu
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Kaldır",
                    tint = Color(0xFF0288D1),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
// OKUMA PARÇALARI

// A1 Seviyesi Paragraf Arşivi
val allStories = mapOf(
    "My Special Day" to """
        Today is a very special day for me. My name is Ali, and I want to tell you about my morning. 
        I usually wake up at seven o'clock, but today is different. Today is my birthday! 
        The sun is shining through my window. I feel very happy and energetic. 
        I get out of bed and go to the kitchen. I can smell fresh coffee and toasted bread. 
        My mother and father are there. They say, 'Happy Birthday, Ali!' and they give me a big hug. 
        Everything feels wonderful today.
    """.trimIndent(),

    "Daily Routine" to """
        My name is Alex. I am a student. I wake up at 7 o’clock every day. 
        I wash my face and brush my teeth. Then I have breakfast. I drink tea and eat bread and cheese.
        After breakfast, I go to school. I listen to my teacher and take notes.
        At 12 o’clock, I have lunch. In the evening, I go home and have dinner with my family. 
        At night, I read a book. I go to bed at 11 o’clock.
    """.trimIndent(),

    "My Family" to """
        I have a small family. There are four people in my family: my father, my mother, my sister, and me. 
        My father is a doctor. He is very kind. My mother is a teacher. She loves books. 
        My sister is five years old. She is very funny. We live in a big house with a garden. 
        I love my family very much.
    """.trimIndent(),

    "My Dream House" to """
        I want to live in a beautiful house. My dream house is near the sea. 
        It has two floors and four rooms. The living room is big and white. 
        The kitchen is modern. There is a balcony with a great view. 
        I want a small garden for my flowers. It is a very peaceful and happy place.
    """.trimIndent(),

    "At the Grocery Store" to """
        I am at the grocery store now. I need some food for dinner. 
        I want to buy milk, eggs, and bread. I also need some fresh fruit. 
        I like apples and bananas. I see the vegetables. I buy tomatoes and onions. 
        I go to the cashier and pay for my food. Now I can go home.
    """.trimIndent(),

    "Summer Weather" to """
        The weather is very hot today. It is summer. The sky is blue and the sun is bright. 
        I am wearing a t-shirt and shorts. My friends and I are at the beach. 
        We swim in the sea and play games. In the evening, it is warm. 
        I love summer because the days are long and sunny.
    """.trimIndent(),

    "What Time Is It?" to """
        Time is very important. I have a busy day today. 
        At 8 o'clock, I have a meeting. At 10 o'clock, I visit my grandmother. 
        We drink tea together. At 1 o'clock, I meet my friend for lunch. 
        In the afternoon, I go to the gym. I always check my watch because I don't want to be late.
    """.trimIndent(),

    "My Favorite Hobbies" to """
        I have many hobbies. I like playing the guitar and listening to music. 
        On weekends, I go to the park and ride my bike. 
        I also love taking photos of nature. Cooking is another hobby for me. 
        I like making pizza for my friends. Hobbies make my life very interesting and fun.
    """.trimIndent(),

    "Where Is The Bank?" to """
        Excuse me, I am lost. I am looking for the bank. 
        Is it near here? Go straight and turn left at the pharmacy. 
        The bank is next to the library. It is a big grey building. 
        Thank you very much for your help! You are welcome. Have a nice day.
    """.trimIndent(),

    "Feeling Sick" to """
        Today, I don't feel well. I have a headache and a cold. 
        I think I have a fever. I stay in bed and rest. 
        I drink a lot of water and orange juice. My mother makes chicken soup for me. 
        I need to see a doctor tomorrow. I hope I feel better soon.
    """.trimIndent(),

    "Weekend In Paris" to """
        I am very excited. I am in Paris for the weekend. 
        I want to see the Eiffel Tower and visit museums. 
        The city is very beautiful and old. I eat delicious croissants for breakfast. 
        I walk on the streets and take many pictures. Paris is a wonderful city for a trip.
    """.trimIndent(),

    "Future Plans" to """
        I have big plans for the future. Next year, I want to learn a new language. 
        I also want to travel to Spain and Italy. I plan to work in a big company. 
        I want to buy a new car and help my parents. I study hard every day to achieve my dreams.
    """.trimIndent()
)


@OptIn(ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun InteractiveQuizScreen(question: NewQuestion, onNext: (Boolean) -> Unit) {
    val context = LocalContext.current
    var selectedAnswer by remember { mutableStateOf("") }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var currentCatImage by remember { mutableIntStateOf(R.drawable.bench) }

    val actionBlue = Color(0xFF0288D1)
    val happyCats = listOf(R.drawable.bench, R.drawable.truee, R.drawable.what)
    val angryCats = listOf(R.drawable.wrong, R.drawable.wrongg)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- SORU ALANI ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val displayText = question.questionText.split("........")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = displayText[0], fontSize = 22.sp, fontWeight = FontWeight.Medium)

                AnimatedContent(targetState = selectedAnswer, label = "WordAnim") { target ->
                    Text(
                        text = if (target.isEmpty()) " ________ " else " $target ",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAnswered) (if (isCorrect) Color(0xFF4CAF50) else Color.Red) else actionBlue
                    )
                }

                if (displayText.size > 1) {
                    Text(text = displayText[1], fontSize = 22.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- SEÇENEKLER ---
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            question.options.forEach { option ->
                val isSelected = selectedAnswer == option
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable(!isAnswered) { selectedAnswer = option },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) actionBlue else Color(0xFFF5F5F5)
                ) {
                    Text(text = option, modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp))

                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- KEDİ TEPKİSİ ---
        if (isAnswered) {
            Image(painter = painterResource(id = currentCatImage), contentDescription = null, modifier = Modifier.size(180.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BUTON VE SES MANTIĞI ---
        Button(
            onClick = {
                if (!isAnswered) {
                    isCorrect = selectedAnswer.trim().lowercase() == question.correctAnswer.trim().lowercase()
                    currentCatImage = if (isCorrect) happyCats.random() else angryCats.random()
                    isAnswered = true

                    // SES ÇALMA (Dosyalar gelene kadar yorum satırına aldık)
                    /* try {
                        val resId = if (isCorrect) R.raw.correct else R.raw.wrong
                        val mp: MediaPlayer? = MediaPlayer.create(context, resId)
                        mp?.start()
                        mp?.setOnCompletionListener { player -> player.release() }
                    } catch (e: Exception) {
                        // Ses hatası olsa bile uygulama kapanmaz
                    }
                    */
                } else {
                    onNext(isCorrect)
                    isAnswered = false
                    selectedAnswer = ""
                }
            },
            enabled = selectedAnswer.isNotEmpty(), // Hiçbir şey seçilmemişse buton basılmaz
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = if (!isAnswered) "KONTROL ET" else "SIRADAKİ SORU",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } // Column Kapanışı
} // InteractiveQuizScreen Kapanışı




// ... geri kalan kodlar aynı


@Composable
fun QuizAppNavigation(
    speechAssistant: SpeechAssistant,
    isDarkMode: Boolean,              // ✅ bunu ekle
    onToggleDarkMode: () -> Unit      // ✅ bunu ekle
) {
    var isDarkMode by remember { mutableStateOf(false) }

    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current


    val dictionaryWords = remember { loadWordsFromAssets(context) }
    var showQuizPrompt by rememberSaveable { mutableStateOf(true) }




    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            DarkEliteHomeScreen(
                navController = navController,
                showQuizPrompt = true,
                onDismissQuizPrompt = {},
                isDarkMode = isDarkMode
            )
        }
        composable("home") {
            DarkEliteHomeScreen(
                navController = navController,
                isDarkMode = isDarkMode, // Global değişkeni buraya gönderiyoruz
                showQuizPrompt = true,
                onDismissQuizPrompt = { /* ... */ }
            )
        }
        // NavHost içinde şuraya ekle:
        composable("settings") {
            // 🚨 DİKKAT: isDarkModeActive parametresine güvenme,
            // doğrudan globaldeki 'isDarkMode' değişkenini kullan.
            SettingsScreen(
                isDarkMode = isDarkMode, // 👈 Burası global 'var' olan değişken olmalı
                onThemeChange = { newValue ->
                    isDarkMode = newValue // 🚀 Global değişkeni güncelliyoruz
                    android.util.Log.d("MewEnglish", "Yeni Durum: $isDarkMode")
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("quiz_tree") {
            QuizTreeScreen(navController = navController)
        }

        // Test rotalarını tek tek veya dinamik tanımlayabiliriz.
        // Senin istediğin 5 test:
        // 1. ADIM: Döngü aralığını genişletiyoruz (Örn: 1..20)
        // 1. ADIM: Döngü aralığını 10'a çıkarıyoruz
        for (i in 1..14) {
            composable("test_$i") {
                val quizList = when (i) {
                    1 -> QuizRepository.getQuizOne()
                    2 -> QuizRepository.getQuizTwo()
                    3 -> QuizRepository.getQuizThree()
                    4 -> QuizRepository.getQuizFour()
                    5 -> QuizRepository.getQuizFive()
                    6 -> QuizRepository.getQuizSix()
                    7 -> QuizRepository.getQuizSeven()
                    8 -> QuizRepository.getQuizEight()
                    9 -> QuizRepository.getQuizNine()
                    10 -> QuizRepository.getQuizTen()
                    11 -> QuizRepository.getQuizEleven()
                    12 -> QuizRepository.getQuizTwelve()
                    13 -> QuizRepository.getQuizThirteen()
                    14 -> QuizRepository.getQuizFourteen()

                    else -> QuizRepository.getQuizOne()
                }

                MainQuizScreen(
                    quizList = quizList,
                    ttsHelper = speechAssistant,
                    navController = navController,
                    onQuizComplete = { navController.popBackStack() }
                )
            }
        }
        // 1. SEVİYE SEÇİM EKRANI
        composable("ReadingLevelSelection") {
            ReadingLevelSelectionScreen(
                navController = navController,
                onLevelSelected = { levelId ->
                    // Burada levelId "A1" geliyorsa, navigasyon "ReadingList/A1"e gitmeli.
                    // Eğer burası boşsa veya yanlış rotaya gidiyorsa buton tepki vermez.
                    navController.navigate("ReadingList/$levelId")
                }
            )
        }

// 2. ÖRNEK: A1 ROTASI (Diğer 5'i de buna benzemeli)
        composable("ReadingList/A1") {
            ReadingListScreen(
                level = "A1",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

// 1. SEVİYE SEÇİM ANA EKRANI
        composable("ReadingLevelSelection") {
            ReadingLevelSelectionScreen(
                navController = navController,
                onLevelSelected = { selectedLevel ->
                    // selectedLevel buradan "A1", "B2" vb. olarak gelir ve
                    // aşağıdaki uygun rotaya yönlendirilir.
                    navController.navigate("ReadingList/$selectedLevel")
                }
            )
        }

// 2. A1 SEVİYE LİSTESİ
        composable("ReadingList/A1") {
            ReadingListScreen(
                level = "A1",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

// 3. A2 SEVİYE LİSTESİ
        composable("ReadingList/A2") {
            ReadingListScreen(
                level = "A2",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

// 4. B1 SEVİYE LİSTESİ
        composable("ReadingList/B1") {
            ReadingListScreen(
                level = "B1",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

// 5. B2 SEVİYE LİSTESİ
        composable("ReadingList/B2") {
            ReadingListScreen(
                level = "B2",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

// 6. C1 SEVİYE LİSTESİ
        composable("ReadingList/C1") {
            ReadingListScreen(
                level = "C1",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

// 7. C2 SEVİYE LİSTESİ
        composable("ReadingList/C2") {
            ReadingListScreen(
                level = "C2",
                navController = navController,
                onReadingSelected = { storyTitle ->
                    navController.navigate("ReadingDetail/$storyTitle")
                }
            )
        }

        // ✅ 1. ADIM: Merkezi Quiz Navigasyonunu Bağladık
        // Bu satır sayesinde "test_1", "test_2" gibi rotalar artık tanınacak.


        // HOME
        composable(Screen.Home.route) {
            AppScaffold(navController, drawerState, scope) {
                DarkEliteHomeScreen(  // ✅ buraya taşındı
                    navController = navController,
                    showQuizPrompt = showQuizPrompt,
                    onDismissQuizPrompt = { showQuizPrompt = false },
                    isDarkMode = isDarkMode
                )
            }
        }
        // progress ekranı
        composable(route = "progress_screen") {
            ProgressScreen(navController = navController)
        }

        composable("sheet_test_screen") {
            SheetTestScreen(navController)
        }
        // QuizAppNavigation.kt içindeki NavHost'un içine şunu yaz:


        // Navigasyon (NavHost) içindeki tanımlama
        composable(Screen.Guide.route) {
            // Uygulamanın Instagram stili alt barını korumak için AppScaffold içine alıyoruz
            AppScaffold(navController, drawerState, scope) {
                GuideScreen(navController)
            }
        }


        // WORDS
        composable(Screen.Word.route) {
            AppScaffold(navController, drawerState, scope) {
                MyWordsScreen(navController)
            }
        }

        // QUIZ LIST
        composable(Screen.Quiz.route) { // ✅ 'Quizzes' -> 'Quiz' (Singular)
            AppScaffold(navController, drawerState, scope) {
                QuizListScreen(
                    onNavigateToWord = { navController.navigate(Screen.Word.route) }, // ✅ Singular
                    onNavigateToQuiz = { /* Zaten bu ekrandayız */ }, // ✅ Singular
                    onNavigateToMeeting = {
                        // Gerekirse buraya meeting rotası eklenebilir
                    },
                    onNavigateToProgress = {
                        navController.navigate(Screen.Progress.route)
                    },
                    onNavigateToThisThat = {
                        // Gerekirse buraya this_that rotası eklenebilir
                    },
                    onQuizSelected = { quizName ->
                        // Seviye belirleme mantığı korunuyor
                        val level = if (quizName.contains("A2")) "A2" else "A1"
                        navController.navigate(Screen.QuizTree.createRoute(level))
                    }
                )
            }
        }

        // QUIZ TREE
        composable(
            route = Screen.QuizTree.route,
            arguments = listOf(navArgument("lvl") { type = NavType.StringType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getString("lvl") ?: "A1"

            AppScaffold(navController, drawerState, scope) {
                QuizTreeScreen(
                    navController = navController, // Birinci parametre
                    levelName = level              // İkinci parametre
                )
            }
        }


        // LISTENING LEVELS
        composable(Screen.ListeningLevel.route) {
            ListeningLevelSelectionScreen(
                navController = navController,
                onLevelSelected = { level ->
                    navController.navigate(Screen.ListeningSelection.createRoute(level))
                }
            )
        }
        // --- READING (OKUMA) NAVİGASYONU ---
        composable(Screen.ReadingLevel.route) {
            AppScaffold(navController, drawerState, scope) {
                // Burada Reading seviye seçimi ekranın olmalı
                // Eğer henüz bu ekranı yapmadıysan geçici bir ekran koyabilirsin
                ReadingLevelSelectionScreen(
                    navController = navController,
                    onLevelSelected = { level ->
                        // Örnek: navController.navigate(Screen.ReadingDetail.createRoute(level))
                    }
                )
            }
        }


        // LISTENING QUIZ
        composable(
            route = Screen.ListeningSelection.route,
            arguments = listOf(navArgument("level") { type = NavType.StringType })
        ) { backStackEntry ->
            val level = backStackEntry.arguments?.getString("level") ?: "A1"

            // ✅ Tipi açıkça belirterek (SpeechAssistant) hatayı çözüyoruz
            val assistant: SpeechAssistant? = speechAssistant

            if (assistant != null) {

                ListeningQuizScreen(
                    navController = navController,
                    level = level,
                    speechAssistant = speechAssistant // Artık hata vermeyecektir
                )
            }
        }
    }
}

@Composable
fun PlaceholderTestScreen(navController: NavHostController, title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Çok Yakında!", fontWeight = FontWeight.Bold)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ParagraphReadingScreen(
    paragraph: String,
    ttsHelper: com.example.ngilizcequizapp.TextToSpeechHelper,
    dictionary: List<WordItem>, // ✅ HATA BURADA: Bu satırın olduğundan emin ol!
    onFinished: (Int) -> Unit
) {
    // ... fonksiyonun geri kalanı

    // --- 1. STATE VE CONTEXT TANIMLARI ---
    val context = LocalContext.current
    var selectedWordForPopup by remember { mutableStateOf<String?>(null) }
    var spokenWords by remember { mutableStateOf(setOf<String>()) }
    var rmsValue by remember { mutableStateOf(0f) }
    var isListening by remember { mutableStateOf(false) }

    // ✅ HATA ÇÖZÜMÜ: originalWords burada tanımlandı
    val originalWords = remember(paragraph) {
        paragraph.split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    // Renk Ayarları (isDarkMode projenizde global değilse 'false' yapabilirsiniz)
    var isDarkMode by remember { mutableStateOf(false) }
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFF5F7F9)
    val surfaceColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color.Black
    val mewBlue = Color(0xFF3897F0)

    // --- 2. POPUP GÖSTERİMİ ---
    selectedWordForPopup?.let { word ->
        WordPopup(
            word = word,
            ttsHelper = ttsHelper, // ✅ Hoparlör desteği için paslandı
            onDismiss = { selectedWordForPopup = null }
        )
    }

    // --- 3. SES TANIMA BAĞLANTISI ---
    val startListening = rememberSpeechRecognizerState(
        onRmsUpdate = { rmsValue = it },
        onListeningStateChange = { isListening = it },
        onResult = { result ->
            val newWords = result.lowercase().split(" ")
            spokenWords = spokenWords + newWords
        }
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> if (isGranted) startListening() }

    // --- 4. ARAYÜZ (UI) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pronunciation Test",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = mewBlue
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = surfaceColor,
            shadowElevation = 2.dp
        ) {
            FlowRow(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                originalWords.forEach { word ->
                    val cleanWord = word.lowercase().replace(Regex("[^a-z]"), "")
                    val isCorrect = spokenWords.contains(cleanWord)

                    Text(
                        text = "$word ",
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCorrect) Color(0xFF4CAF50) else textColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clickable {
                                // ✅ Tıklanınca popup'ı tetikler (Singular/Tekil temizlenmiş kelime ile)
                                selectedWordForPopup = cleanWord
                            }
                            .padding(horizontal = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isListening) {
            WaveformView(rmsDb = rmsValue)
        }

        Spacer(modifier = Modifier.height(20.dp))

        FloatingActionButton(
            onClick = { permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
            containerColor = mewBlue,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(70.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Speak", modifier = Modifier.size(30.dp))
        }

        Text(
            text = if (isListening) "Listening..." else "Tap to speak",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}
val pronunciationTestParagraph = """
    Today is a special day for me. I wake up at seven o'clock and wash my face. 
    I wear my favorite shirt and have breakfast with my family. 
    In the garden, I see a big present. My friend gives me a nice gift. 
    We sing a song together and I feel very happy.
""".trimIndent()


// ✅ DOĞRU: Fonksiyonun içinde
@Composable
fun QuizNavigationManager(
    ttsHelper: com.example.ngilizcequizapp.TextToSpeechHelper
) {
    // 1. Durum State'leri
    var selectedQuizTitle by remember { mutableStateOf<String?>(null) }
    var selectedStoryContent by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // ✅ SÖZLÜĞÜ BURADA YÜKLÜYORUZ (Hatanın çözümü)
    val dictionaryWords = remember { loadWordsFromAssets(context) }

    when {
        // Ana Liste Ekranı
        selectedQuizTitle == null && selectedStoryContent == null -> {
            QuizListScreen(
                onNavigateToMeeting = { selectedQuizTitle = "My Special Day" },
                onNavigateToThisThat = { selectedQuizTitle = "This / That Quiz" },
                onNavigateToWord = { /* ... */ },
                onNavigateToProgress = { /* ... */ },
                onNavigateToQuiz = { /* ... */ },
                onQuizSelected = { title -> selectedQuizTitle = title }
            )
        }

        // Seviye Seçildiğinde (A1, A2 vb.)
        (selectedQuizTitle == "A1" || selectedQuizTitle == "A2") && selectedStoryContent == null -> {
            val currentStories = if (selectedQuizTitle == "A1") allStories else a2Stories

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$selectedQuizTitle Reading List",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                currentStories.forEach { (title, content) ->
                    Button(
                        onClick = { selectedStoryContent = content },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "$title")
                    }
                }

                TextButton(onClick = { selectedQuizTitle = null }) {
                    Text("Geri Dön")
                }
            }
        }

        // ✅ OKUMA EKRANI: Dictionary parametresi buraya eklendi
        selectedStoryContent != null -> {
            ParagraphReadingScreen(
                paragraph = selectedStoryContent!!,
                ttsHelper = ttsHelper,
                dictionary = dictionaryWords, // ✅ ARTIK BULUNABİLİR (Hata giderildi)
                onFinished = {
                    selectedStoryContent = null
                }
            )
        }
    }
}
// Gelen kelime skorunu kontrol eden basit mantık
fun evaluatePronunciation(wordScore: Float): String {
    return when {
        wordScore >= 0.92f -> "GREEN"   // Çok katı! Gerçek başarı.
        wordScore >= 0.60f -> "ORANGE"  // Turuncu: "Hadi bir daha dene" bölgesi.
        else -> "BLACK"                 // Hata Torbası.
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var isDarkMode by remember { mutableStateOf(false) }
    val bgColor = if (isDarkMode) Color(0xFF121212) else Color.White
    val txtColor = if (isDarkMode) Color.White else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = txtColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = txtColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode", color = txtColor, fontSize = 18.sp)
                // ✅ KARANLIK MOD ARTIK SADECE BURADA
                Switch(checked = isDarkMode, onCheckedChange = { isDarkMode = it })
            }
        }
    }
}

data class QuizListeningLevelData(
    val name: String,
    val desc: String,
    val color: Color,
    val quizList: List<String> = listOf("Quiz 1", "Quiz 2", "Quiz 3")
)

@Composable
fun QuizLevelCard(level: QuizListeningLevelData, onCardClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp) // Kartlar arası nefes payı
            .clickable { onCardClick() },
        shape = RoundedCornerShape(28.dp), // ✅ Daha modern, "elle çizilmiş" gibi yumuşak
        color = level.color,
        shadowElevation = 10.dp // ✅ Daha derin gölge
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // İkon alanı
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star, // Örn: Yıldız ikonu
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Level ${level.name}", // ✅ KURAL: Bold
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = level.desc, // QuizListeningLevelData sınıfında ne yazıyorsa o (desc/description)
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun QuizTreeScreen(
    levelName: String,
    onBack: () -> Unit,
    onQuizSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // Arka plan resmini yükleyip dikeyde tekrar etmesini (Repeat) sağlıyoruz
    val bgImage = ImageBitmap.imageResource(id = R.drawable.quiztreescreen) // Resim ismini kontrol et
    val backgroundBrush = remember(bgImage) {
        ShaderBrush(
            ImageShader(
                image = bgImage,
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated
            )
        )
    }

    val deepBlue = Color(0xFF01579B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(id = R.drawable.quiztreescreen),
                contentScale = ContentScale.FillBounds, // Ekran genişliğine yayar
                // Eğer dikeyde tekrar etmesini istiyorsan TileMode kullanmalıyız.
                // Ama basitçe resmi görmek için şimdilik bunu kullan:
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // --- ÜST BAR ---
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = deepBlue
                    )
                }
                Text(
                    text = "$levelName Haritası", // Bold kuralına uygun
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = deepBlue
                    )
                )
            }

            // --- HARİTA GÖVDESİ ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. SATIR: Giriş Düğümü
                QuizNode(
                    title = "Giriş",
                    isLocked = false,
                    color = Color(0xFF0288D1),
                    onClick = { onQuizSelected("meeting_quiz_screen") }
                )

                BranchingLine()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Sol, Orta ve Sağ Dallar
                    QuizPathColumn(color = Color(0xFF0288D1), depth = 9, onQuizClick = onQuizSelected)
                    QuizPathColumn(color = Color(0xFF4FC3F7), depth = 9, onQuizClick = onQuizSelected)
                    QuizPathColumn(color = deepBlue, depth = 9, onQuizClick = onQuizSelected)
                }
            }
        }
    }
}
@Composable
fun QuizPathColumn(color: Color, depth: Int, onQuizClick: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in 1..depth) {
            QuizNode(
                title = "",
                isLocked = false, // Test için kilitleri açtım kanka
                color = color,
                // ✅ Her bir node artık anahtar ismimizi gönderiyor
                onClick = { onQuizClick("meeting_quiz_screen") }
            )
            if (i < depth) {
                VerticalConnectorLine()
            }
        }
    }
}
// Hata aldığın QuizNode fonksiyonu tam olarak bu:
@Composable
fun QuizNode(
    title: String,
    isLocked: Boolean,
    color: Color,
    onClick: () -> Unit // Parametre ismini onClick olarak kullanıyoruz
) {
    Surface(
        modifier = Modifier
            .size(75.dp)
            .padding(4.dp)
            .clickable(enabled = !isLocked) { onClick() },
        shape = CircleShape,
        color = if (isLocked) Color(0xFFE0E0E0) else color,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
@Composable
fun ConnectorLine(height: Dp) {
    Canvas(modifier = Modifier
        .height(height)
        .width(2.dp)) {
        drawLine(color = Color.LightGray, start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 6f)
    }
}


@Composable
fun QuizAppContainer(navController: NavController) { // navController'ı parametre olarak ekledik
    // Hangi seviyenin (A1, A2 vs.) seçildiğini tutar
    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var activeQuiz by remember { mutableStateOf<String?>(null) } // Aktif quizi takip etmek için

    if (activeQuiz != null) {
        // BURASI QUIZ EKRANI (Örneğin This/That veya Meeting)
        // activeQuiz durumuna göre quizi başlatan kod buraya gelecek
    } else if (selectedLevel == null) {
        // --- 1-3-3 ANA LİSTE EKRANI ---
        // ✅ TÜM HATALARI ÇÖZEN KISIM: Eksik parametreleri buraya ekledik
        QuizListScreen(
            onNavigateToMeeting = {
                activeQuiz = "Meeting"
            },
            onNavigateToThisThat = {
                activeQuiz = "ThisThat"
            },
            onNavigateToWord = {
                navController.navigate("word_screen")
            },
            onNavigateToProgress = {
                navController.navigate("progress_screen")
            },
            onNavigateToQuiz = {
                // Zaten bu ekrandayız, liste başına dönebilir
            },
            onQuizSelected = { levelName ->
                selectedLevel = levelName
            }
        )
    } else {
        // HARİTA (TREE) EKRANI
        QuizTreeScreen(
            levelName = selectedLevel!!,
            onBack = { selectedLevel = null },
            onQuizSelected = { quizName ->
                activeQuiz = quizName
            }
        )
    }
}
@Composable
fun VerticalConnectorLine() {
    Canvas(modifier = Modifier
        .height(30.dp)
        .width(2.dp)) {
        drawLine(color = Color(0xFFBDBDBD), start = Offset(0f, 0f), end = Offset(0f, size.height), strokeWidth = 6f)
    }
}

@Composable
fun BranchingLine() {
    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)) {
        val midX = size.width / 2
        // Ana hattan 3'e dağılan çizim
        drawLine(Color(0xFFBDBDBD), Offset(midX, 0f), Offset(midX, 30f), 6f)
        drawLine(Color(0xFFBDBDBD), Offset(size.width * 0.17f, 30f), Offset(size.width * 0.83f, 30f), 6f)
        drawLine(Color(0xFFBDBDBD), Offset(size.width * 0.17f, 30f), Offset(size.width * 0.17f, 60f), 6f)
        drawLine(Color(0xFFBDBDBD), Offset(midX, 30f), Offset(midX, 60f), 6f)
        drawLine(Color(0xFFBDBDBD), Offset(size.width * 0.83f, 30f), Offset(size.width * 0.83f, 60f), 6f)
    }
}
@Composable
fun QuizNode(title: String, isLocked: Boolean, color: Color) {
    Surface(
        modifier = Modifier
            .size(75.dp)
            .padding(4.dp),
        shape = CircleShape,
        color = if (isLocked) Color(0xFFE0E0E0) else color,
        shadowElevation = 6.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLocked) {
                Icon(Icons.Default.Lock, null, tint = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text(text = title, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
// Bu sınıfın MyWordsScreen dosyasında veya modellerin olduğu yerde olmalı
data class WordData(
    val word: String,    // Eğer burası 'term' ise aşağıda wordItem.term yazmalısın
    val meaning: String  // Eğer burası 'definition' ise aşağıda wordItem.definition yazmalısın
)
// Eski TextToSpeechHelper'ı bulup silebilirsin
// veya ismini şu şekilde güncelleyebilirsin:

class SpeechAssistant(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isReady = true
                // A1 seviyesi için hızı biraz yavaş tutuyoruz
                tts?.setSpeechRate(0.8f)
            }
        }
    }

    // ✅ HATAYI ÇÖZEN GÜNCEL FONKSİYON
    fun speak(text: String, isTurkish: Boolean = false) { // isTurkish parametresini ekledik
        if (isReady) {
            // 1. Dili anlık olarak değiştiriyoruz
            val locale = if (isTurkish) Locale("tr", "TR") else Locale.US
            tts?.setLanguage(locale)

            // 2. Seslendir
            // NOT: params kullanımı modern Android cihazlarda daha stabil çalışır
            val params = android.os.Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id_${System.currentTimeMillis()}")
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "unique_id")
        }
    }

    fun shutDown() {
        tts?.stop()
        tts?.shutdown()
    }
}
//QUİZ EKRANI KODLARI
data class WordQuiz(
    val questionText: String,      // Kullanıcıya verilen Türkçe yönerge
    val correctOrder: List<String>, // Doğru sıralama: ["My", "name", "is", "Professor", "Cat"]
    val shuffledWords: List<String>,// Karıştırılmış hali: ["is", "My", "Professor", "Cat", "name"]
    val imageResId: Int            // Kullanılacak PNG (meeting.png vb.)
)
data class WordOption(
    val text: String,
    var isSelected: Boolean = false
)


@Composable
fun WordBubble(
    word: String,
    isSelected: Boolean, // Kelimenin seçilip yukarı gidip gitmediğini kontrol eder
    isAbove: Boolean = false, // Yeni: Kelime yukarıdaki kutuda mı?
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() }, // HATA 1 DÜZELTİLDİ: Artık her zaman tıklanabilir, böylece geri alınabilir.
        shape = RoundedCornerShape(12.dp),
        shadowElevation = if (isSelected && !isAbove) 0.dp else 2.dp, // Seçilenlerin yeri düzleşir
        tonalElevation = 4.dp,
        // HATA 2 DÜZELTİLDİ: Yukarıdaki kelime (isAbove) seçilse bile beyaz kalır, aşağıda kalan yer gri olur.
        color = when {
            isAbove -> Color.White // Yukarıdaki kelime her zaman canlı beyaz
            isSelected -> Color.LightGray // Aşağıda kalan boş yer yer tutucu olarak gri
            else -> Color(0xFFF0F0F0) // Henüz seçilmemiş kelimeler
        }
    ) {
        Text(
            text = word,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
            // Seçilen kelime aşağıda "hayalet" gibi yer tutsun diye transparan, yukarıda ise siyah görünür.
            color = if (isSelected && !isAbove) Color.Transparent else Color.Black
        )
    }
}
@Composable
fun WordOrderingQuiz(
    quizStep: QuizStep,
    ttsHelper: SpeechAssistant, // ✅ KURAL: SpeechAssistant kullanımı
    onResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val selectedWords = remember(quizStep.id) { mutableStateListOf<String>() }
    val shuffledWords = remember(quizStep.id) { quizStep.correctSentence.shuffled() }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // 1. Üst Kısım: Talimat ve Hoparlör
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quizStep.instruction,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold // ✅ KURAL: Bold
            )

            IconButton(onClick = { ttsHelper.speak(quizStep.correctSentence.joinToString(" ")) }) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Seslendir",
                    tint = Color(0xFF2196F3)
                )
            }
        }

        // 2. Cümle Boşluğu
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                selectedWords.forEach { word ->
                    WordBubble(word = word, isSelected = true, isAbove = true) {
                        selectedWords.remove(word)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Kelime Havuzu
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            shuffledWords.forEachIndexed { index, word ->
                val usedCount = selectedWords.count { it == word }
                val shuffledCount = shuffledWords.take(index + 1).count { it == word }
                val isUsed = usedCount >= shuffledCount

                WordBubble(word = word, isSelected = isUsed, isAbove = false) {
                    if (!isUsed) selectedWords.add(word)
                }
            }
        }

        // --- ANAHTAR KELİME BÖLÜMÜ (YAMA DÜZENLENDİ) ---
        if (quizStep.wordMap.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F8E9), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFC5E1A5), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Key Word (Anahtar Kelimeler):", // ✅ KURAL: Bold & Singular
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF388E3C)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                val wordsText = quizStep.wordMap.entries.joinToString("  |  ") { (english, turkish) ->
                    "$english: $turkish"
                }

                Text(
                    text = wordsText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } // ✅ Column burada kapandı
        } // ✅ 'if' bloğu burada kapandı

        Spacer(modifier = Modifier.weight(1f))

        // 4. KONTROL ET TUŞU
        Button(
            onClick = {
                val isCorrect = selectedWords.toList() == quizStep.correctSentence
                playSystemSound(context, isCorrect)

                if (isCorrect) {
                    ttsHelper.speak(quizStep.correctSentence.joinToString(" "))
                }
                onResult(isCorrect)
            },
            enabled = selectedWords.size == quizStep.correctSentence.size,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                disabledContainerColor = Color(0xFFE0E0E0)
            )
        ) {
            Text(
                text = "KONTROL ET",
                style = TextStyle(fontWeight = FontWeight.Black, fontSize = 16.sp)
            )
        }
    } // ✅ Ana Column burada kapandı
}
sealed class QuizResult {
    object None : QuizResult()
    object Correct : QuizResult()
    // 'message' parametresini buraya ekliyoruz:
    data class Wrong(val message: String) : QuizResult()
}
@Composable
fun FinalSuccessScreen(onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Senin istediğin o özel kedi görseli
        Image(
            painter = painterResource(id = getDrawableId("quiz_cat")),
            contentDescription = "Başarı Kedisi",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tebrikler!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Tanışma bölümünü başarıyla tamamladın.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRestart,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Baştan Başla")
        }
    }
}
@Composable
fun getDrawableId(resourceName: String): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(resourceName, "drawable", context.packageName)
}

// QUİZLERRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR""
val meetingQuizSteps = listOf(
    // 1. Aşama: Merhaba ve Kendini Tanıtma
    QuizStep(
        id = 1,
        instruction = ".Merhaba! Benim ismim Profesör Kedi",
        correctSentence = listOf("Hi", "my", "name", "is", "Professor", "Cat"),
        imageRes = "what_is_your", // What_is_your.png
        type = "teaching"
    ),
    // 2. Aşama: Nerelisin?
    QuizStep(
        id = 2,
        instruction = "Sen nerelisin?",
        correctSentence = listOf("Where", "are", "you", "from"),
        imageRes = "meeting_stranger", // meeting_stranger.png
        type = "teaching"
    ),
    // 3. Aşama: Sınıfta Quiz Deneyimi (Çoktan Seçmeli)
    QuizStep(
        id = 3,
        instruction = "Nereli olduğunu söylemek için 'I am from ...' ",
        correctSentence = listOf("I", "am", "from", "Turkey"),
        imageRes = "meeting_in_class", // meeting_in_class.png
        type = "quiz"
    ),
    // 4. Aşama: Dışarıda Karşılaşma (Kontrollü Yazım)
    QuizStep(
        id = 4,
        instruction = "Tanıştığıma memnun oldum!",
        correctSentence = listOf("Nice", "to", "meet", "you"),
        imageRes = "meeting", // meeting.png
        type = "ordering"
    ),
    // 7. Aşama: Nasılsın? / İyiyim (Yarı Kontrollü)
    QuizStep(
        id = 7,
        instruction = "Nasılsın? İyiyim, teşekkürler.",
        correctSentence = listOf("How", "are", "you", "I", "am", "fine", "thanks"),
        imageRes = "meeting",
        type = "ordering"
    ),
    // 8. Aşama: Nerelisin? (Yarı Kontrollü)
    QuizStep(
        id = 8,
        instruction = "Sen nerelisin?",
        correctSentence = listOf("Where", "are", "you", "from"),
        imageRes = "meeting_stranger",
        type = "ordering"
    ),
    // 10. Aşama: Türküm vs Türkiyeliyim
    QuizStep(
        id = 10,
        instruction = "Ben Türk'üm.",
        correctSentence = listOf("I", "am", "Turkish"),
        imageRes = "quiz_cat", // Tanıtım amaçlı
        type = "teaching"
    ),
    // 11. Aşama: Serbest Yazım (Kendini Tanıt)
    QuizStep(
        id = 11,
        instruction = "Şimdi sıra sende! Kendini tanıt.",
        correctSentence = listOf("my", "name", "is"), // Kontrol kelimeleri
        imageRes = "quiz_cat",
        type = "free_writing"
    )
)
@Composable
fun QuizImage(imageRes: Any) { // 'imageName: String' yerine 'imageRes: Any' yaptık
    val context = LocalContext.current

    // Gelen verinin tipine göre Resource ID'yi buluyoruz
    val resourceId = remember(imageRes) {
        when (imageRes) {
            is Int -> imageRes // Eğer zaten sayıysa (R.drawable.quiz_resim_1) direkt kullan
            is String -> { // Eğer metinse ("near_table") ID'sini sistemden bul
                val id = context.resources.getIdentifier(
                    imageRes.lowercase(),
                    "drawable",
                    context.packageName
                )
                id
            }
            else -> 0
        }
    }

    // Eğer geçerli bir resim bulunduysa çiz, bulunamadıysa varsayılan bir logo göster
    if (resourceId != 0) {
        Image(
            painter = painterResource(id = resourceId),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        // Resim bulunamazsa kedi logonu buraya koyabilirsin
        Image(
            painter = painterResource(id = R.drawable.cat_logo),
            contentDescription = "Varsayılan Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
fun FreeWritingQuiz(quizStep: QuizStep, onResult: (Boolean) -> Unit) {
    var userInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Resim ID'sini dinamik olarak belirleyelim (Any -> Int)
    val finalImageId = remember(quizStep.imageRes) {
        when (val res = quizStep.imageRes) {
            is Int -> res
            is String -> {
                val id = context.resources.getIdentifier(res, "drawable", context.packageName)
                if (id != 0) id else R.drawable.cat_logo
            }
            else -> R.drawable.cat_logo
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Excel'den gelen veya döngüyle atanan görsel
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.size(200.dp)
        ) {
            Image(
                painter = painterResource(id = finalImageId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Talimat metni
        Text(
            text = quizStep.instruction,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Yazma alanı
        OutlinedTextField(
            value = userInput,
            onValueChange = { userInput = it },
            label = { Text("Cevabını buraya yaz...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.weight(1f)) // Butonu aşağıya iter

        // Cevap Gönder Butonu
        Button(
            onClick = {
                val input = userInput.lowercase().trim()
                // Senin mantığın: İçinde belirli kalıplar geçiyor mu?
                val isCorrect = input.contains("my name is") || input.contains("i am")
                onResult(isCorrect)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
        ) {
            Text("Cevabı Gönder", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(ttsHelper: SpeechAssistant) {
    // 1️⃣ Navigasyon ve Tema State'lerini Tanımlıyoruz (Hataları Çözen Kısım)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // ui/theme/Color.kt içinde olmalı
    val VividIceBlue = Color(0xFF00E5FF) // Veya senin belirlediğin o canlı mavi kodu
    val BrokenIceBlue = Color(0xFFACE5EE) // Eğer bunu da kullanıyorsan ekle

    // Karanlık mod durumunu temadan veya manuel state'den alıyoruz
    val isDarkMode = LocalIsDarkMode.current
    val backgroundColor = if (isDarkMode) Color(0xFF121212) else Color.White

    var showQuizPrompt by remember { mutableStateOf(true) }
    var selectedQuizName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = backgroundColor,
                tonalElevation = 12.dp
            ) {
                val items = listOf(Screen.Home, Screen.Progress, Screen.Quiz, Screen.Word)

                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                // ✅ Quiz dışına çıkınca seçili quiz'i sıfırla
                                if (screen.route != Screen.Quiz.route) {
                                    selectedQuizName = null
                                }

                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            if (screen.iconRes != null) {
                                Image(
                                    painter = painterResource(id = screen.iconRes),
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(if (isSelected) 45.dp else 39.dp),
                                    alpha = if (isSelected) 1f else 0.6f,
                                    colorFilter = if (isDarkMode) ColorFilter.tint(Color.White) else null
                                )
                            }
                        },
                        label = {
                            Text(
                                text = screen.title,
                                color = if (isSelected) VividIceBlue else VividIceBlue.copy(alpha = 0.5f),
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {

            // 2️⃣ Sayfalar Arası Geçişi Sağlayan NavHost
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Home.route) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Home Screen") }
                }
                composable(Screen.Progress.route) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Progress") }
                }
                composable(Screen.Quiz.route) {
                    // Quiz Mantığı: Eğer bir quiz seçiliyse onu, değilse listeyi göster
                    when (selectedQuizName) {
                        "A1 Haritası" -> QuizMapScreen(onStepSelected = { selectedQuizName = "Tanışma Quizi" })
                        "Tanışma Quizi" -> MeetingQuizApp(ttsHelper)
                        else -> QuizListScreen(
                            onNavigateToMeeting = { selectedQuizName = "Tanışma Quizi" },
                            onNavigateToThisThat = { selectedQuizName = "ThisThat Quiz" },
                            onQuizSelected = { name -> selectedQuizName = name },
                            onNavigateToWord = { navController.navigate(Screen.Word.route) },
                            onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                            onNavigateToQuiz = { /* Zaten buradayız */ }
                        )
                    }
                }
                composable(Screen.Word.route) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Word List") }
                }
            }

            // 3️⃣ İlk Açılışta Gelen Quiz Sorusu
            if (showQuizPrompt) {
                AlertDialog(
                    onDismissRequest = { showQuizPrompt = false },
                    title = { Text("Quiz Time!") },
                    text = { Text("Would you like to take a quiz?") },
                    confirmButton = {
                        Button(onClick = {
                            navController.navigate(Screen.Quiz.route)
                            selectedQuizName = "A1 Haritası"
                            showQuizPrompt = false
                        }) { Text("Yes") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showQuizPrompt = false }) { Text("Later") }
                    }
                )
            }
        }
    }
}
// BU FONKSİYON EKSİK OLDUĞU İÇİN HATA ALIYORDUN:
@Composable
fun MeetingQuizApp(ttsHelper: SpeechAssistant) {
    var currentStepIndex by remember { mutableStateOf(0) }
    var showFinalScreen by remember { mutableStateOf(false) }
    var currentResult by remember { mutableStateOf<QuizResult>(QuizResult.None) }

    val currentStep = meetingQuizSteps.getOrNull(currentStepIndex)

    if (showFinalScreen || currentStep == null) {
        FinalSuccessScreen(onRestart = {
            currentStepIndex = 0
            showFinalScreen = false
        })
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                QuizImage(currentStep.imageRes)

                // Her yeni adımda ekranı temizlemek için 'key' kullanıyoruz
                key(currentStep.id) {
                    when (currentStep.type) {
                        "free_writing" -> FreeWritingQuiz(currentStep) { isCorrect ->
                            currentResult = if (isCorrect) QuizResult.Correct else QuizResult.Wrong("Harika!")
                        }
                        else -> WordOrderingQuiz(currentStep, ttsHelper) { isCorrect ->
                            currentResult = if (isCorrect) QuizResult.Correct
                            else QuizResult.Wrong(currentStep.correctSentence.joinToString(" "))
                        }
                    }
                }
            }

            ResultOverlay(
                result = currentResult,
                onNext = {
                    currentResult = QuizResult.None
                    if (currentStepIndex < meetingQuizSteps.size - 1) currentStepIndex++
                    else showFinalScreen = true
                }
            )
        }
    }
}

@Composable
fun ResultOverlay(result: QuizResult, onNext: () -> Unit) {
    if (result == QuizResult.None) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val imageRes = if (result is QuizResult.Correct) "truee" else "wrong"

            Image(
                painter = painterResource(id = getDrawableId(imageRes)),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )

            if (result is QuizResult.Wrong) {
                Text(
                    text = result.message, // Doğru cümle burada görünür
                    color = Color.White,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Button(onClick = onNext) {
                Text("Devam Et")
            }
        }
    }
}
@Composable
fun QuizMapScreen(onStepSelected: (Int) -> Unit) {
    val scrollState = rememberScrollState()

    // Kırık Buz Mavisi Gradyan Renkleri
    val iceBlueGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFE1F5FE), // Çok açık buz mavisi
            Color(0xFFB3E5FC), // Orta buz mavisi
            Color(0xFF81D4FA)  // Kırık buz mavisi (Broken Blue tonuna yakın)
        )
    )

    val quizTitles = listOf(
        "Meeting Quiz", "This / That Qui", "Color Quiz",
        "Number Quiz", "Family Member Quiz", "Body Part Quiz",
        "School Object Quiz", "Animal Quiz", "Fruit & Vegetable Quiz",
        "Verb Part 1", "Verb Part 2", "Am / Is / Are Quiz",
        "Can / Can't Quiz", "Daily Routine Quiz", "Job Quiz",
        "Feeling Quiz", "House Part Quiz", "Preposition Quiz",
        "Present Simple Part 1", "Present Simple Part 2",
        "Adjective Quiz", "Weather Quiz"
    )

    // Arka planı kaplayan Box
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(iceBlueGradient) // Gradyan burada uygulanıyor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 24.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Learning Pat",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF01579B), // Koyu mavi başlık
                    shadow = Shadow(color = Color.White.copy(alpha = 0.5f), blurRadius = 8f)
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // --- 1. EN ÜSTTEKİ TEK BUTON ---
            QuizStepCircle(
                title = quizTitles[0],
                isLocked = false,
                onClick = { onStepSelected(0) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            MapConnectionLine()

            // --- 2. ALTTAKİ 3'LÜ SIRALAR ---
            val remainingQuizzes = quizTitles.drop(1)
            val rows = remainingQuizzes.chunked(3)

            rows.forEachIndexed { rowIndex, rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rowItems.forEachIndexed { columnIndex, title ->
                        val globalIndex = 1 + (rowIndex * 3) + columnIndex
                        QuizStepCircle(
                            title = title,
                            isLocked = globalIndex > 1,
                            onClick = { onStepSelected(globalIndex) }
                        )
                    }
                }

                if (rowIndex < rows.size - 1) {
                    MapConnectionLine()
                }
            }
        }
    }
}
fun playSound(context: Context, resId: Int) {
    val mediaPlayer = MediaPlayer.create(context, resId)
    mediaPlayer.setOnCompletionListener { it.release() } // Ses bitince belleği temizle
    mediaPlayer.start()
}
fun playSystemSound(context: Context, isCorrect: Boolean) {
    // Context üzerinden AudioManager servisini alıyoruz
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager

    val soundEffect = if (isCorrect) {
        android.media.AudioManager.FX_KEY_CLICK // Doğru: Standart tıklama
    } else {
        // Yanlış: Daha belirgin bir navigasyon/hata sesi
        android.media.AudioManager.FX_FOCUS_NAVIGATION_UP
    }

    // Sesi çalması için komut gönderiyoruz
    audioManager.playSoundEffect(soundEffect)
}

//That/this farkını öğreten quiz



@Composable
fun FullDemonstrativesLearningScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. KISIM: THIS & THAT (TEKİLLER) ---
        Image(
            painter = painterResource(id = R.drawable.this_that_cat_image),
            contentDescription = "This That Teaching",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Text(
            text = "THIS & THAT Farkı (Tekiller)",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2196F3))
        )

        Spacer(modifier = Modifier.height(16.dp))

        TeachingPoint(title = "THIS (Bu)", description = "Yakındaki TEK nesne.", color = Color(0xFF4CAF50))
        TeachingPoint(title = "THAT (O)", description = "Uzaktaki TEK nesne.", color = Color(0xFFF44336))

        Text("Örnekler:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        ExampleSentence("This is a car.", " (Bu bir araba - Yakın)")
        ExampleSentence("That is a star.", " (O bir yıldız - Uzak)")
        ExampleSentence("This is my bag.", " (Bu benim çantam - Yakın)")
        ExampleSentence("That is a bird.", " (O bir kuş - Uzak)")
        ExampleSentence("This is an apple.", " (Bu bir elma - Yakın)")

        // --- ARA GEÇİŞ: 20 SORU (THIS & THAT) ---
        QuizDivider("THIS & THAT Quiz: 20 Soru Başlıyor")

        // NOT: Burada normalde kodunuzdaki Quiz bileşenini çağıracaksınız.
        // 10 This + 10 That sorusu aşağıda liste olarak verilmiştir.



        Text(
            text = "THESE & THOSE Farkı (Çoğullar)",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9C27B0))
        )

        Spacer(modifier = Modifier.height(16.dp))

        TeachingPoint(title = "THESE (Bunlar)", description = "Yakındaki ÇOĞUL nesneler.", color = Color(0xFF00BCD4))
        TeachingPoint(title = "THOSE (Onlar)", description = "Uzaktaki ÇOĞUL nesneler.", color = Color(0xFFFF9800))

        Text("Örnekler:", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        ExampleSentence("These are cats.", " (Bunlar kediler - Yakın)")
        ExampleSentence("Those are planes.", " (Onlar uçaklar - Uzak)")
        ExampleSentence("These are my pens.", " (Bunlar benim kalemlerim - Yakın)")
        ExampleSentence("Those are big trees.", " (Onlar büyük ağaçlar - Uzak)")
        ExampleSentence("These are flowers.", " (Bunlar çiçekler - Yakın)")

        // --- ARA GEÇİŞ: 20 SORU (THESE & THOSE) ---
        QuizDivider("THESE & THOSE Quiz: 20 Soru Başlıyor")
    }
}

@Composable
fun QuizDivider(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEB3B))
    ) {
        Text(text, modifier = Modifier
            .padding(16.dp)
            .align(Alignment.CenterHorizontally), fontWeight = FontWeight.Bold)
    }
}
@Composable
fun TeachingPoint(title: String, description: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ExampleSentence(englishSentence: String, turkishDescription: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp)) {
                    append(englishSentence)
                }
                withStyle(style = SpanStyle(color = Color.Gray, fontSize = 14.sp)) {
                    append(turkishDescription)
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

// That is This is Quiz soruları
val thisThatQuizSteps = listOf(
    // Sayfa 1: 10 Adet THIS (Yakın Nesneler)
    QuizStep(id = 1, instruction = "Bu bir masadır.", correctSentence = listOf("This", "is", "a", "table"), imageRes = "near_table", type = "ordering", wordMap = mapOf("table" to "masa")),
    QuizStep(id = 2, instruction = "Bu bir köpektir.", correctSentence = listOf("This", "is", "a", "dog"), imageRes = "near_dog", type = "ordering", wordMap = mapOf("dog" to "köpek")),
    QuizStep(id = 3, instruction = "Bu bir anahtardır.", correctSentence = listOf("This", "is", "a", "key"), imageRes = "near_key", type = "ordering", wordMap = mapOf("key" to "anahtar")),
    QuizStep(id = 4, instruction = "Bu bir şapkadır.", correctSentence = listOf("This", "is", "a", "hat"), imageRes = "near_hat", type = "ordering", wordMap = mapOf("hat" to "şapka")),
    QuizStep(id = 5, instruction = "Bu bir lambadır.", correctSentence = listOf("This", "is", "a", "lamp"), imageRes = "near_lamp", type = "ordering", wordMap = mapOf("lamp" to "lamba")),
    QuizStep(id = 6, instruction = "Bu bir fincandır.", correctSentence = listOf("This", "is", "a", "cup"), imageRes = "near_cup", type = "ordering", wordMap = mapOf("cup" to "fincan")),
    QuizStep(id = 7, instruction = "Bu bir kapıdır.", correctSentence = listOf("This", "is", "a", "door"), imageRes = "near_door", type = "ordering", wordMap = mapOf("door" to "kapı")),
    QuizStep(id = 8, instruction = "Bu bir penceredir.", correctSentence = listOf("This", "is", "a", "window"), imageRes = "near_window", type = "ordering", wordMap = mapOf("window" to "pencere")),
    QuizStep(id = 9, instruction = "Bu bir çantadır.", correctSentence = listOf("This", "is", "a", "bag"), imageRes = "near_bag", type = "ordering", wordMap = mapOf("bag" to "çanta")),
    QuizStep(id = 10, instruction = "Bu bir kalemdir.", correctSentence = listOf("This", "is", "a", "pencil"), imageRes = "near_pencil", type = "ordering", wordMap = mapOf("pencil" to "kalem")),

    // Sayfa 2: 10 Adet THAT (Uzak Nesneler)
    QuizStep(id = 11, instruction = "O bir yıldızdır.", correctSentence = listOf("That", "is", "a", "star"), imageRes = "far_star", type = "ordering", wordMap = mapOf("star" to "yıldız")),
    QuizStep(id = 12, instruction = "O bir ağaçtır.", correctSentence = listOf("That", "is", "a", "tree"), imageRes = "far_tree", type = "ordering", wordMap = mapOf("tree" to "ağaç")),
    QuizStep(id = 13, instruction = "O bir kuştur.", correctSentence = listOf("That", "is", "a", "bird"), imageRes = "far_bird", type = "ordering", wordMap = mapOf("bird" to "kuş")),
    QuizStep(id = 14, instruction = "O bir arabadır.", correctSentence = listOf("That", "is", "a", "car"), imageRes = "far_car", type = "ordering", wordMap = mapOf("car" to "araba")),
    QuizStep(id = 15, instruction = "O bir uçaktır.", correctSentence = listOf("That", "is", "a", "plane"), imageRes = "far_plane", type = "ordering", wordMap = mapOf("plane" to "uçak")),
    QuizStep(id = 16, instruction = "O bir evdir.", correctSentence = listOf("That", "is", "a", "house"), imageRes = "far_house", type = "ordering", wordMap = mapOf("house" to "ev")),
    QuizStep(id = 17, instruction = "O bir buluttur.", correctSentence = listOf("That", "is", "a", "cloud"), imageRes = "far_cloud", type = "ordering", wordMap = mapOf("cloud" to "bulut")),
    QuizStep(id = 18, instruction = "O bir bisiklettir.", correctSentence = listOf("That", "is", "a", "bicycle"), imageRes = "far_bicycle", type = "ordering", wordMap = mapOf("bicycle" to "bisiklet")),
    QuizStep(id = 19, instruction = "O bir çocuktur.", correctSentence = listOf("That", "is", "a", "child"), imageRes = "far_child", type = "ordering", wordMap = mapOf("child" to "çocuk")),
    QuizStep(id = 20, instruction = "O bir kutudur.", correctSentence = listOf("That", "is", "a", "box"), imageRes = "far_box", type = "ordering", wordMap = mapOf("box" to "kutu"))
)

//These Those farkı quizleri
val theseThoseQuizSteps = listOf(
    // Sayfa 3: 10 Adet THESE (Yakın Çoğul Nesneler)
    QuizStep(id = 21, instruction = "Bunlar elmalardır.", correctSentence = listOf("These", "are", "apples"), imageRes = "near_apples", type = "ordering", wordMap = mapOf("apple" to "elma")),
    QuizStep(id = 22, instruction = "Bunlar kitaplardır.", correctSentence = listOf("These", "are", "books"), imageRes = "near_books", type = "ordering", wordMap = mapOf("book" to "kitap")),
    QuizStep(id = 23, instruction = "Bunlar kalemlerdir.", correctSentence = listOf("These", "are", "pencils"), imageRes = "near_pencils", type = "ordering", wordMap = mapOf("pencil" to "kalem")),
    QuizStep(id = 24, instruction = "Bunlar ayakkabılardır.", correctSentence = listOf("These", "are", "shoes"), imageRes = "near_shoes", type = "ordering", wordMap = mapOf("shoe" to "ayakkabı")),
    QuizStep(id = 25, instruction = "Bunlar çiçeklerdir.", correctSentence = listOf("These", "are", "flowers"), imageRes = "near_flowers", type = "ordering", wordMap = mapOf("flower" to "çiçek")),
    QuizStep(id = 26, instruction = "Bunlar kedilerdir.", correctSentence = listOf("These", "are", "cats"), imageRes = "near_cats", type = "ordering", wordMap = mapOf("cat" to "kedi")),
    QuizStep(id = 27, instruction = "Bunlar toplardır.", correctSentence = listOf("These", "are", "balls"), imageRes = "near_balls", type = "ordering", wordMap = mapOf("ball" to "top")),
    QuizStep(id = 28, instruction = "Bunlar ellerdir.", correctSentence = listOf("These", "are", "hands"), imageRes = "near_hands", type = "ordering", wordMap = mapOf("hand" to "el")),
    QuizStep(id = 29, instruction = "Bunlar bardaklardır.", correctSentence = listOf("These", "are", "glasses"), imageRes = "near_glasses", type = "ordering", wordMap = mapOf("glass" to "bardak")),
    QuizStep(id = 30, instruction = "Bunlar telefonlardır.", correctSentence = listOf("These", "are", "phones"), imageRes = "near_phones", type = "ordering", wordMap = mapOf("phone" to "telefon")),

    // Sayfa 4: 10 Adet THOSE (Uzak Çoğul Nesneler)
    QuizStep(id = 31, instruction = "Onlar kuşlardır.", correctSentence = listOf("Those", "are", "birds"), imageRes = "far_birds", type = "ordering", wordMap = mapOf("bird" to "kuş")),
    QuizStep(id = 32, instruction = "Onlar ağaçlardır.", correctSentence = listOf("Those", "are", "trees"), imageRes = "far_trees", type = "ordering", wordMap = mapOf("tree" to "ağaç")),
    QuizStep(id = 33, instruction = "Onlar arabalardır.", correctSentence = listOf("Those", "are", "cars"), imageRes = "far_cars", type = "ordering", wordMap = mapOf("car" to "araba")),
    QuizStep(id = 34, instruction = "Onlar yıldızlardır.", correctSentence = listOf("Those", "are", "stars"), imageRes = "far_stars", type = "ordering", wordMap = mapOf("star" to "yıldız")),
    QuizStep(id = 35, instruction = "Onlar uçaklardır.", correctSentence = listOf("Those", "are", "planes"), imageRes = "far_planes", type = "ordering", wordMap = mapOf("plane" to "uçak")),
    QuizStep(id = 36, instruction = "Onlar bulutlardır.", correctSentence = listOf("Those", "are", "clouds"), imageRes = "far_clouds", type = "ordering", wordMap = mapOf("cloud" to "bulut")),
    QuizStep(id = 37, instruction = "Onlar çocuklardır.", correctSentence = listOf("Those", "are", "children"), imageRes = "far_children", type = "ordering", wordMap = mapOf("child" to "çocuk")),
    QuizStep(id = 38, instruction = "Onlar binalardır.", correctSentence = listOf("Those", "are", "buildings"), imageRes = "far_buildings", type = "ordering", wordMap = mapOf("building" to "bina")),
    QuizStep(id = 39, instruction = "Onlar dağlardır.", correctSentence = listOf("Those", "are", "mountains"), imageRes = "far_mountains", type = "ordering", wordMap = mapOf("mountain" to "dağ")),
    QuizStep(id = 40, instruction = "Onlar gemilerdir.", correctSentence = listOf("Those", "are", "ships"), imageRes = "far_ships", type = "ordering", wordMap = mapOf("ship" to "gemi"))
)




val a2Stories = mapOf(
    "A New Job" to """
        My brother has a very interesting new job now. He works in a big office in the center of the city
        . He is a manager in a famous company. Every morning, he wakes up early and takes a fast train to work. He always carries a professional black bag.
         Inside the bag, there is a silver laptop, a blue pen, and a small notebook. He likes his colleague very much because everyone is very helpful and kind.
          In the afternoon, he has a short break. He usually eats a fresh sandwich and drinks a hot coffee.
           His office has a large window with a great view of the park. Sometimes, he has an important meeting with a client.
            He needs to speak English because the company is international. He feels very happy because his salary is good.
             He wants to buy a new car next year. After a long day, he comes back home by bus. He feels a bit tired but very satisfied with his career.
    """.trimIndent(),

    "Healthy Food" to """
    Eating a healthy meal is very important for every person. I try to have a good habit every day. For breakfast, I eat an egg, some white cheese, and a green olive.
     I also eat an apple or an orange because a fruit has many vitamin parts. For lunch, I usually have a fresh salad. I put a tomato,
      a cucumber, and a red pepper in it. I don't like a sugary soda or a sweet juice. I prefer to drink a glass of cold water. For dinner,
       my mother cooks a delicious chicken or a fresh fish. She uses a natural oil to make the food healthy. I always eat a piece of bread with my soup.
        I think a fast food burger is not good for the body. If a person eats a vegetable every day, they feel very energetic.
         I also like to drink a cup of herbal tea before I go to sleep. This lifestyle is the best way to live a long life.
         """.trimIndent(),
)


@Composable
fun ListeningQuizScreen(
    navController: NavHostController,
    level: String,
    speechAssistant: SpeechAssistant
) {
    // --- STATE'LER ---
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    var showWrong by remember { mutableStateOf(false) }
    var correctCount by remember { mutableStateOf(0) }

    val transcript = """
        The woman is standing in front of the stove in her bright kitchen.
        The kitchen is very tidy and clean, with everything in its place.
        She is frying potatoes in a pan, and the food smells very good.
        The woman looks calm and focused while she is cooking.
        She enjoys preparing a simple meal in her clean and comfortable kitchen.
    """.trimIndent()

    // --- CEVAP KONTROL ---
    fun checkAnswer(option: Int) {
        selectedOption = option
        if (option == 1) {
            correctCount++
            showSuccess = true
        } else {
            showWrong = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // -------- ANA İÇERİK --------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Metni dinledikten sonra hangi resimden bahsettiğini anlamaya çalışınız ve o resme tıklayınız.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 level parametresi artık gerçekten kullanılıyor (uyarı da gider)
            Text(
                text = level,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Listen and Choose",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuizImageCard(
                    imageRes = R.drawable.listening_quiz_1,
                    isSelected = selectedOption == 1,
                    onClick = { checkAnswer(1) },
                    modifier = Modifier.weight(1f)
                )

                QuizImageCard(
                    imageRes = R.drawable.listening_quiz_2,
                    isSelected = selectedOption == 2,
                    onClick = { checkAnswer(2) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        speechAssistant.speak(transcript) // ✅ nullable DEĞİL
                    },
                shape = CircleShape,
                border = BorderStroke(3.dp, Color(0xFF0288D1))
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Play Audio",
                    modifier = Modifier.padding(32.dp),
                    tint = Color(0xFF0288D1)
                )
            }
        }

        // -------- DOĞRU CEVAP --------
        if (showSuccess) {
            ResultOverlay(
                imageRes = R.drawable.truee,
                text = "🎉 You answered correctly!",
                buttonText = "Continue"
            ) {
                showSuccess = false
                selectedOption = null
            }
        }

        // -------- YANLIŞ CEVAP --------
        if (showWrong) {
            ResultOverlay(
                imageRes = R.drawable.wrong,
                text = "❌ Wrong answer",
                buttonText = "Try again"
            ) {
                showWrong = false
                selectedOption = null
            }
        }
    }
}





@Composable
fun ListeningLevelSelectionScreen(
    navController: NavHostController,
    onLevelSelected: (String) -> Unit
) {
    // Yeni sınıf ismini (ListeningListeningLevelData) kullanıyoruz
    val levels = listOf(
        ListeningListeningLevelData("A1", "Beginner", Color(0xFFE1F5FE), Color(0xFF03A9F4)),
        ListeningListeningLevelData("A2", "Elementary", Color(0xFFE1F5FE), Color(0xFF03A9F4)),
        ListeningListeningLevelData("B1", "Intermediate", Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        ListeningListeningLevelData("B2", "Upper", Color(0xFFFFF3E0), Color(0xFFFB8C00)),
        ListeningListeningLevelData("C1", "Advanced", Color(0xFFFFEBEE), Color(0xFFE91E63)),
        ListeningListeningLevelData("C2", "Mastery", Color(0xFFFFEBEE), Color(0xFFE91E63))
    )

    // Senin özel gradyan arka planın (WordsScreen ile aynı)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(SoftGradientBlue, Color.White),
        startY = 0f,
        endY = 1000f
    )


    Box(modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Başlık
            Text(
                text = "Listening Level",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = BrokenIceBlue
            )

            Text(
                text = "Select your Target",
                style = MaterialTheme.typography.bodyMedium,
                color = BrokenIceBlue.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Seviye Listesi
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(levels) { levelItem ->
                    LevelSelectionCard(
                        data = levelItem, // Burada hata almamak için parametre ismini eşitledik
                        onClick = { onLevelSelected(levelItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelSelectionCard(data: ListeningListeningLevelData, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White, // PureWhite değişkenin yoksa Color.White kullan
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // SOL: İleride resim yüklenecek alan
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(data.backgroundColor, RoundedCornerShape(16.dp)), // ARTIK HATA VERMEZ
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = data.accentColor.copy(alpha = 0.4f), // ARTIK HATA VERMEZ
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // ORTA: Bilgiler
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = data.id, // ARTIK HATA VERMEZ
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = data.accentColor
                )
                Text(
                    text = data.description, // ARTIK HATA VERMEZ
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Veri Modeli
data class ListeningListeningLevelData(
    val id: String,          // Örn: "A1"
    val description: String, // Örn: "Beginner"
    val backgroundColor: Color,
    val accentColor: Color
)

@Composable
fun OptionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF5D9CEC) else Color(0xFFF0F8FF)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFF3897F0)) else null
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color(0xFF2C3E50), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun QuizImageCard(imageRes: Int, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(
            width = if (isSelected) 4.dp else 2.dp,
            color = if (isSelected) Color(0xFF5D9CEC) else Color(0xFFF0F2F1)
        ),
        color = Color(0xFFF8FAFB)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun ListeningQuizResultScreen(
    navController: NavController,
    correctCount: Int,
    totalQuestions: Int
) {
    val scoreColor = when {
        correctCount < totalQuestions * 0.4 -> Color(0xFFE74C3C)   // 🔴
        correctCount <= totalQuestions * 0.6 -> Color(0xFFF39C12)  // 🟠
        else -> Color(0xFF2ECC71)                                  // 🟢
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.quiz_cat),
                contentDescription = null,
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "$correctCount / $totalQuestions",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Listening Quiz Result",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(onClick = {
                navController.popBackStack()
            }) {
                Text("Back")
            }
        }
    }
}


data class ListeningQuestion(
    val transcript: String,
    val correctAnswer: Int
)



@Composable
fun ResultOverlay(
    imageRes: Int,
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = text,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}
@Composable
fun GuideScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Uygulama Guide",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Bu bölümde uygulamanın kullanımı hakkında bilgiler yer alacak.",
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { navController.popBackStack() }) {
            Text("Geri Dön")
        }
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Üst Başlık ve Geri Butonu
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Tema Ayarı Bölümü
        Text(
            text = "Appearance",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Dark Mode",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isDarkMode) "Enabled" else "Disabled",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // 🚨 İŞTE O TUŞ!
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { newValue ->
                        android.util.Log.d("MewEnglish", "Dark Mode Değişimi: $newValue") // 👈 Bu satırı ekle
                        onThemeChange(newValue)
                    }
                )
            }
        }

    } // 👈 Surface bloğunu kapatan parantez
}
// BOTTOM SHEET EKRANI

