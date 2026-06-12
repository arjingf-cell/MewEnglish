package com.example.ngilizcequizapp
import com.google.mlkit.nl.languageid.LanguageIdentification
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.BiasAlignment
// --- ANDROID & TTS ---
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import com.example.ngilizcequizapp.SpeechAssistant
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale
import android.content.Intent
// --- COMPOSE CORE & RUNTIME ---
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment

// --- COMPOSE UI & GRAPHICS ---
import androidx.compose.ui.graphics.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.animateLottieCompositionAsState
// --- COMPOSE LAYOUT ---
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*

// --- COMPOSE ANIMATION ---
import androidx.compose.animation.*
import androidx.compose.animation.core.*

// --- MATERIAL DESIGN & ICONS ---
import androidx.compose.material3.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*

// --- NAVIGATION ---
import androidx.navigation.*
import com.example.ngilizcequizapp.QuizRepository.QuizPathConnector
// --- PROJECT SPECIFIC (Burası senin sınıfların) ---
import com.example.ngilizcequizapp.quiz.engine.QuestionType
import com.example.ngilizcequizapp.quiz.engine.VocabularyLevelDataQuiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.geometry.Offset

import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.collections.mapOf


// Diğer motorları ve yardımcı sınıfları buraya ekle
@Composable
fun QuizStepCircle(
    title: String,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    // Tasarımı modernize ettik: Kilitli değilse yeşil ve gölgeli, kilitliyse gri.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(enabled = !isLocked) { onClick() } // Kilitliyse tıklanamaz
    ) {
        Box(
            modifier = Modifier
                .size(80.dp) // Biraz büyüttük daha okunaklı olsun
                .background(
                    if (!isLocked) Color(0xFF58CC02) else Color(0xFFE5E5E5),
                    CircleShape
                )
                .border(
                    width = 4.dp,
                    color = if (!isLocked) Color(0xFF46A302) else Color(0xFFCECECE),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // İkon seçimi
            // İkon seçimi
            Icon(
                // ✅ Star yerine Check (tik) koyduk, bu hata vermez ve "tamamlandı" hissi verir
                imageVector = if (!isLocked) Icons.Default.Check else Icons.Default.Lock,
                contentDescription = null,
                tint = if (!isLocked) Color.White else Color(0xFFAFAFAF),
                modifier = Modifier.size(35.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ünite Başlığı
        Text(
            text = title, // Örn: Unit 1
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold, // ✅ KURAL: Bold
                textAlign = TextAlign.Center,
                color = if (!isLocked) Color(0xFF4B4B4B) else Color.Gray
            ),
            maxLines = 1
        )
    }
}

@Composable
fun MapConnectionLine() {
    Box(
        modifier = Modifier
            .width(12.dp)
            .height(40.dp)
            .background(Color(0xFFE5E5E5), shape = RoundedCornerShape(6.dp))
    )
}

fun getSectionTheme(sectionIndex: Int): SectionTheme {
    return sectionThemes[sectionIndex % sectionThemes.size]
}

// bağlantı çizgileri
@Composable
fun ConnectionLine(
    modifier: Modifier = Modifier,
    height: Dp,
    color: Color = Color(0xFFE5E5EA) // Gri tonu
) {
    Box(
        modifier = modifier
            .width(8.dp) // Çizgi kalınlığı
            .height(height)
            .background(color, RoundedCornerShape(4.dp))
    )
}



// --- 🎨 RENK PALETİ (Görselden Alınan) ---
val ColorDuoBlue = Color(0xFF1CB0F6)
val ColorDuoGreen = Color(0xFF58CC02)
val ColorDuoGrey = Color(0xFFE0E0E0)
val ColorDuoShadow = Color(0x33000000) // Hafif gölge




// QuizTreeScreen.kt içine güncellenmiş hali
data class SectionTheme(
    val primaryColor: Color,
    val borderColor: Color,
    val solvedColor: Color,
    val lockedColor: Color = Color(0xFFE0E0E0),
    val headerGradientStart: Color,
    val headerGradientEnd: Color
)

val sectionThemes = listOf(
    // Bölüm 1 – Mavi (Orijinal)
    SectionTheme(
        primaryColor = Color(0xFF1CB0F6),
        borderColor = Color(0xFF0091D5),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFF1CB0F6),
        headerGradientEnd = Color(0xFF0091D5)
    ),
    // Bölüm 2 – Mor
    SectionTheme(
        primaryColor = Color(0xFF9B59B6),
        borderColor = Color(0xFF7D3C98),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFF9B59B6),
        headerGradientEnd = Color(0xFF7D3C98)
    ),
    // Bölüm 3 – Turuncu
    SectionTheme(
        primaryColor = Color(0xFFE67E22),
        borderColor = Color(0xFFCA6F1E),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFFE67E22),
        headerGradientEnd = Color(0xFFCA6F1E)
    ),
    // Bölüm 4 – Kırmızı
    SectionTheme(
        primaryColor = Color(0xFFE74C3C),
        borderColor = Color(0xFFC0392B),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFFE74C3C),
        headerGradientEnd = Color(0xFFC0392B)
    ),
    // Bölüm 5 – Teal
    SectionTheme(
        primaryColor = Color(0xFF1ABC9C),
        borderColor = Color(0xFF148F77),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFF1ABC9C),
        headerGradientEnd = Color(0xFF148F77)
    ),
    // Bölüm 6 – Sarı/Altın
    SectionTheme(
        primaryColor = Color(0xFFF39C12),
        borderColor = Color(0xFFD68910),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFFF39C12),
        headerGradientEnd = Color(0xFFD68910)
    ),
    // Bölüm 7 – Pembe
    SectionTheme(
        primaryColor = Color(0xFFE91E8C),
        borderColor = Color(0xFFC21472),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFFE91E8C),
        headerGradientEnd = Color(0xFFC21472)
    ),
    // Bölüm 8 – Koyu Mavi
    SectionTheme(
        primaryColor = Color(0xFF2C3E87),
        borderColor = Color(0xFF1A256E),
        solvedColor = Color(0xFF58CC02),
        headerGradientStart = Color(0xFF2C3E87),
        headerGradientEnd = Color(0xFF1A256E)
    )
)

@Composable
fun QuizTreeScreen(navController: NavController, levelName: String = "A1") {
    val lastSolvedIndex by remember { mutableIntStateOf(200) }
    val allSteps = remember { QuizDataHolder.quizSteps }
    val mapItems = remember { buildMapItems(allSteps) }
    val scrollState = rememberLazyListState()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        topBar = {
            // ✅ YAMA: Animasyonlu ve ayrık TopBar kullanımı
            QuizTreeTopBar(
                navController = navController,
                levelName = levelName,
                coins = 1250,
                gems = 85
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ✅ YAMA: %60 muhabbeti ve Professor Cat olan banner
                item {
                    ProgressBannerCard(screenWidth)
                }

                itemsIndexed(
                    items = mapItems,
                    key = { _, item ->
                        when(item) {
                            is MapItem.QuizNode -> "q_${item.quizIndex}"
                            is MapItem.UnitHeader -> "u_${item.unitNumber}"
                            is MapItem.SectionHeader -> "s_${item.sectionNumber}"
                        }
                    }
                ) { index, mapItem ->
                    when (mapItem) {
                        is MapItem.QuizNode -> {
                            val quizIndex = mapItem.quizIndex
                            val isSolved = quizIndex <= lastSolvedIndex
                            val isLocked = if (quizIndex == 0) false else quizIndex > lastSolvedIndex + 1

                            // ✅ HİZALAMA: Görseldeki gibi zigzag akışı
                            val alignment = when {
                                quizIndex == 0 -> Alignment.Center
                                quizIndex % 2 == 1 -> Alignment.CenterStart
                                else -> Alignment.CenterEnd
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .height(90.dp),
                                contentAlignment = alignment
                            ) {
                                DuoQuizNode(
                                    step = mapItem.step,
                                    isSolved = isSolved,
                                    isLocked = isLocked,
                                    sectionIndex = mapItem.sectionIndex,
                                    currentIndex = index,
                                    totalQuestions = mapItems.size,
                                    nodeColor = Color.White,
                                    onClick = {
                                        if (!isLocked) {
                                            navController.navigate("test_${quizIndex + 1}")
                                        }
                                    }
                                )
                            }
                        }
                        is MapItem.UnitHeader -> UnitHeaderCard(mapItem.unitNumber, mapItem.title)
                        is MapItem.SectionHeader -> MasteryCard()
                    }
                }
            }
        }
    }
}
// ✅ YENİ TOP BAR TASARIMI (Görseldeki gibi)
@Composable
fun QuizTreeTopBar(
    navController: NavController,
    levelName: String,
    coins: Int,
    gems: Int,
    currentIndex: Int = 0,
    totalQuestions: Int = 0,
    streakCount: Int = 0,
    hearts: Int = 5,
    isStreakActive: Boolean = false,
    onClose: () -> Unit = { navController.popBackStack() }
) {
    // ✅ Lottie kompozisyonlarını doğru şekilde yüklüyoruz
    val coinComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coin))
    val gemComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_diamond))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SOL: Altın
                AnimatedResourceTag(
                    composition = coinComposition,
                    value = coins.toString()
                )

                // SAĞ: Elmas
                AnimatedResourceTag(
                    composition = gemComposition,
                    value = gems.toString()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Geri Tuşu ve Başlık Kısmı
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Quiz Tree",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E293B)
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Eco,
                            contentDescription = null,
                            tint = Color(0xFF81D4FA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Learn step by step, grow every day!",
                        style = TextStyle(fontSize = 12.sp, color = Color.Gray)
                    )
                }
            }
        }
    }
}
@Composable
fun ProgressBannerCard(screenWidth: androidx.compose.ui.unit.Dp) {
    // Hatırladığımız o kritik eşik değeri
    val progressValue = 0.60f
    val progressPercent = (progressValue * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp) // Görseldeki derinlik için biraz artırdım
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- İLERLEME HALKASI ---
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(65.dp)) {
                    CircularProgressIndicator(
                        progress = { progressValue },
                        color = Color(0xFF42A5F5), // MewEnglish ana mavisi
                        trackColor = Color(0xFFE3F2FD),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "$progressPercent%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // --- METİN ALANI ---
                Column(modifier = Modifier.weight(1f)) {
                    // %60'a ulaşıldığı için özel bir tebrik mesajı
                    Text(
                        text = if (progressPercent >= 60) "Almost there, Arji!" else "Keep going!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (progressPercent >= 60)
                            "You've passed the 60% mark. Professor Cat is proud!"
                        else "Complete more quizzes to grow your tree!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        lineHeight = 14.sp
                    )
                }

                // Karakterin gelmesi için sağda güvenli alan bırakıyoruz
                Spacer(modifier = Modifier.width(60.dp))
            }

            // --- PROFESSOR CAT (REHBER KARAKTER) ---
            // Karakterin konumu ve büyüklüğü emülatörde görselle tam uyuşmalı
            Image(
                painter = painterResource(id = R.drawable.professor_cat),
                contentDescription = "Professor Cat Guide",
                modifier = Modifier
                    .size(90.dp) // Biraz daha belirgin olması için büyüttüm
                    .align(Alignment.CenterEnd)
                    .offset(x = (5).dp, y = (10).dp) // Kartın köşesinden hafif taşma efekti
            )
        }
    }
}

@Composable
fun MasteryCard() {
    Card(
        modifier = Modifier
            .padding(vertical = 24.dp)
            .width(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Star, // Kupa ikonu yerine star
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = Color(0xFFB0BEC5)
            )
            Text("10. Mastery", fontWeight = FontWeight.Bold)
            Text("Finish all to unlock!", fontSize = 10.sp, color = Color.Gray)
        }
    }
}
@Composable
fun ResourceTag(icon: ImageVector, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF1F5F9),
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFFFA000))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
        }
    }
}
@Composable
fun QuizTreeBanner(screenWidth: Dp) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.horizontalGradient(listOf(Color(0xFFE0EAFC), Color(0xFFCFDEF3)))
        )) {
            // 🐱 Profesör Kedi (Sağdan Sola Fade ve %60 Genişlik)
            Image(
                painter = painterResource(id = R.drawable.professor_cat),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.60f) // En fazla %60
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithContent {
                        val colors = listOf(Color.Black, Color.Transparent)
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = colors,
                                startX = size.width,
                                endX = 0f // Sağdan sola doğru fade
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            )

            // ✍️ Metin Alanı (%45 Genişlik ve Kelime Bölmeme)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.45f) // En fazla %45[cite: 1]
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Keep going!",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A2B5E)),
                    softWrap = true // Kelime bölünmez[cite: 1]
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Complete more quizzes to grow your tree!",
                    style = TextStyle(fontSize = 12.sp, color = Color(0xFF4466AA)),
                    softWrap = true // Kelime bütünlüğü korunur[cite: 1]
                )
            }
        }
    }
}
@Composable
fun PipeConnection(width: Dp, height: Dp, pawCount: Int, isSolved: Boolean, isHorizontal: Boolean = false) {
    val color = if (isSolved) Color(0xFFD7F0D8) else Color(0xFFF2F2F2)
    val pawRes = if (isSolved) R.drawable.green_paw else R.drawable.black_paw

    Box(
        modifier = Modifier
            .size(width, height)
            .background(color), // Köşe yumuşatmayı kaldırdık ki kutulara tam otursun
        contentAlignment = Alignment.Center
    ) {
        if (isHorizontal) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(pawCount) { idx ->
                    Image(painter = painterResource(id = pawRes), contentDescription = null, modifier = Modifier.size(14.dp).padding(horizontal = 2.dp))
                }
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(pawCount) { idx ->
                    Image(painter = painterResource(id = pawRes), contentDescription = null, modifier = Modifier.size(14.dp).padding(vertical = 2.dp))
                }
            }
        }
    }
}

@Composable
fun QuizNodeWrapper(
    step: QuizStep,
    index: Int,
    sectionIndex: Int, // Bu parametreyi eklemelisin
    lastSolvedIndex: Int,
    navController: NavController,
    unitColors: List<Color>, // Renk listesini dışarıdan almalı
    modifier: Modifier = Modifier
) {
    // 1. Soru çözüldü mü?
    val isSolved = index <= lastSolvedIndex

    // 2. Soru kilitli mi?
    val isLocked = if (index == 0) false else index > lastSolvedIndex + 1

    // 🎨 Renk ve Hizalama Hesabı (15'li döngüye göre)
    val colorIdx = (index / 15) % unitColors.size
    val currentColor = unitColors[colorIdx]

    // Yılanlama hizalaması (Sinüs)
    val posInCycle = index % 15
    val angle = (posInCycle.toFloat() / 15f) * 2f * Math.PI.toFloat()
    val horizontalBias = kotlin.math.sin(angle).toFloat() * 0.75f
    val alignment = BiasAlignment(horizontalBias, 0f)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        DuoQuizNode(
            step = step, // 'item.step' yerine sadece 'step'
            isSolved = isSolved,
            isLocked = isLocked,
            sectionIndex = sectionIndex,
            nodeColor = currentColor,
            totalQuestions = 10,
            currentIndex = index,
            onClick = {
                if (!isLocked) {
                    val routeNumber = index + 1 // 'item.quizIndex' yerine 'index'
                    navController.navigate("test_$routeNumber")
                }
            }
        )
    }
}

// --- 💡 YARDIMCI COMPOSABLE: DuoQuizNode (Senin PNG'lerini çeken versiyon) ---
@Composable
fun DuoQuizNode(
    step: QuizStep,
    modifier: Modifier = Modifier,
    isSolved: Boolean,
    isLocked: Boolean,
    sectionIndex: Int,
    nodeColor: Color,
    currentIndex: Int,     // Yamalanan: image_6b08aa.jpg hatasını çözer
    totalQuestions: Int,   // Yamalanan: image_6b08aa.jpg hatasını çözer
    onClick: () -> Unit
) {
    val actionBlue = Color(0xFF42A5F5)
    val lockedBackground = Color(0xFFF1F5F9)

    // Yamalanan: İlk adım her zaman açık olsun mantığı
    val finalLockedState = remember(step.id, isLocked) {
        if (step.id == 1) false else isLocked
    }

    // Yamalanan: Görseldeki gibi zigzag akışı için dinamik genişlik
    val dynamicWidth = when {
        currentIndex == 0 -> 280.dp // İlk ünite geniş başlasın
        else -> 180.dp              // Diğerleri ikili sıra için dar olsun
    }

    Card(
        modifier = modifier
            .padding(vertical = 10.dp, horizontal = 12.dp)
            .width(dynamicWidth)
            .height(72.dp)
            .clickable(enabled = !finalLockedState) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (finalLockedState) lockedBackground else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // İkon Bölümü (Tik/Kilit)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSolved) actionBlue.copy(alpha = 0.1f) else Color.Transparent)
                    .border(1.5.dp, if (finalLockedState) Color(0xFFCBD5E1) else actionBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (finalLockedState) {
                    Icon(
                        imageVector = Icons.Default.Lock, // Yamalanan: 'padlock' yerine sistem ikonu
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF94A3B8)
                    )
                } else if (isSolved) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = actionBlue, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = step.qmoji, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${step.id}. ${step.title}",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (finalLockedState) Color(0xFF94A3B8) else Color(0xFF1E293B)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!finalLockedState) {
                    Row(modifier = Modifier.padding(top = 2.dp)) {
                        repeat(3) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (isSolved) Color(0xFFFFB300) else Color(0xFFE2E8F0))
                        }
                    }
                }
            }
        }
    }
}
data class QuizStep(
    val id: Int,
    val title: String = "",
    val imageRes: Any,
    val qmoji: String = "",
    val instruction: String = "",
    val correctSentence: List<String> = emptyList(),
    val type: String = "ordering",
    val wordMap: Map<String, String> = emptyMap()
)
sealed class MapItem {
    data class QuizNode(val step: QuizStep, val quizIndex: Int, val sectionIndex: Int) : MapItem()
    data class SectionHeader(val sectionNumber: Int, val unitNumber: Int, val title: String) : MapItem()
    data class UnitHeader(val unitNumber: Int, val title: String) : MapItem()
}
val unitTitles = listOf(
    "🌱 Ünite 1: Temel İfadeler",
    "🏠 Ünite 2: Günlük Hayat",
    "🌍 Ünite 3: Çevremiz",
    "💬 Ünite 4: İletişim",
    "🎓 Ünite 5: Eğitim & Kariyer",
    "🌟 Ünite 6: Duygular & Kişilik",
    "🚀 Ünite 7: Macera & Keşif",
    "🏆 Ünite 8: Ustalaşma"
)

val sectionTitlesAll = listOf(
    // Ünite 1 – Bölümler 1-5
    "Tanışma & Selamlaşma", "Am / Is / Are", "Aile", "Ev ve Ev Eşyaları", "Temel Soru Kalıpları",
    // Ünite 2 – Bölümler 6-10
    "Ev & Aile", "Yiyecek & İçecek", "Alışveriş", "Ulaşım", "Zaman & Tarihler",
    // Ünite 3 – Bölümler 11-15
    "Hava Durumu", "Doğa & Çevre", "Şehir Hayatı", "Hayvanlar", "Sporlar",
    // Ünite 4 – Bölümler 16-20
    "Telefon & İnternet", "Sosyal Medya", "İfadeler & Duygular", "Fikirler Paylaşma", "Tartışma & Uzlaşma",
    // Ünite 5 – Bölümler 21-25
    "Okul & Dersler", "Meslekler", "İş Hayatı", "Teknoloji", "Gelecek Planları",
    // Ünite 6 – Bölümler 26-30
    "Kişilik Özellikleri", "Duygular", "İlişkiler", "Sağlık & Spor", "Motivasyon",
    // Ünite 7 – Bölümler 31-35
    "Seyahat & Tatil", "Kültür & Sanat", "Macera Sporları", "Uzay & Bilim", "Keşifler",
    // Ünite 8 – Bölümler 36-40
    "Karma Tekrar I", "Karma Tekrar II", "İleri Gramer", "Deyimler", "Şampiyonluk Sınavı"
)
object QuizDataHolder {
    val quizSteps = listOf(
        // ─── ÜNİTE 1 / BÖLÜM 1 ───
        QuizStep(id = 1,   title = "Tanışma I",      qmoji = "👋",  imageRes = 0),
        QuizStep(id = 2,   title = "Tanışma II",     qmoji = "👨‍👩‍👧‍👦", imageRes = 0),
        QuizStep(id = 3,   title = "Nesneler",       qmoji = "🍎",  imageRes = 0),
        QuizStep(id = 4,   title = "Renkler",        qmoji = "🎨",  imageRes = 0),
        QuizStep(id = 5,   title = "Sayılar",        qmoji = "🔢",  imageRes = 0),
        // ─── ÜNİTE 1 / BÖLÜM 2 ───
        QuizStep(id = 6,   title = "Am / Is",        qmoji = "🤝",  imageRes = 0),
        QuizStep(id = 7,   title = "Are (Çoğul)",    qmoji = "🐱",  imageRes = 0),
        QuizStep(id = 8,   title = "Olumsuz Yapı",   qmoji = "🎒",  imageRes = 0),
        QuizStep(id = 9,   title = "Soru Kalıbı",    qmoji = "⏰",  imageRes = 0),
        QuizStep(id = 10,  title = "Karma Final",    qmoji = "🏠",  imageRes = 0),
        // ─── ÜNİTE 1 / BÖLÜM 3 ───
        QuizStep(id = 11,  title = "Evin Odaları",   qmoji = "👤",  imageRes = 0),
        QuizStep(id = 12,  title = "Özneler II",     qmoji = "👥",  imageRes = 0),
        QuizStep(id = 13,  title = "İyelik",         qmoji = "💼",  imageRes = 0),
        QuizStep(id = 14,  title = "Bu / Şu",        qmoji = "👉",  imageRes = 0),
        QuizStep(id = 15,  title = "Özne Tekrar",    qmoji = "🎯",  imageRes = 0),
        // ─── ÜNİTE 1 / BÖLÜM 4 ───
        QuizStep(id = 16,  title = "Renkler I",      qmoji = "🔴",  imageRes = 0),
        QuizStep(id = 17,  title = "Renkler II",     qmoji = "🔵",  imageRes = 0),
        QuizStep(id = 18,  title = "Sayılar I",      qmoji = "1️⃣",  imageRes = 0),
        QuizStep(id = 19,  title = "Sayılar II",     qmoji = "🔟",  imageRes = 0),
        QuizStep(id = 20,  title = "Renk+Sayı Mix",  qmoji = "🌈",  imageRes = 0),
        // ─── ÜNİTE 1 / BÖLÜM 5 ───
        QuizStep(id = 21,  title = "Ne? Soru",       qmoji = "❓",  imageRes = 0),
        QuizStep(id = 22,  title = "Kim? Soru",      qmoji = "🧐",  imageRes = 0),
        QuizStep(id = 23,  title = "Nerede?",        qmoji = "📍",  imageRes = 0),
        QuizStep(id = 24,  title = "Ne Zaman?",      qmoji = "🕐",  imageRes = 0),
        QuizStep(id = 25,  title = "Ünite 1 Final",  qmoji = "🏆",  imageRes = 0),
        // ─── ÜNİTE 2 / BÖLÜM 6 ───
        QuizStep(id = 26,  title = "Aile I",         qmoji = "👨‍👩‍👧", imageRes = 0),
        QuizStep(id = 27,  title = "Aile II",        qmoji = "👴",  imageRes = 0),
        QuizStep(id = 28,  title = "Ev Eşyaları",    qmoji = "🛋️",  imageRes = 0),
        QuizStep(id = 29,  title = "Odalar",         qmoji = "🚪",  imageRes = 0),
        QuizStep(id = 30,  title = "Ev Hayatı Mix",  qmoji = "🏠",  imageRes = 0),
        // ─── ÜNİTE 2 / BÖLÜM 7 ───
        QuizStep(id = 31,  title = "Meyveler",       qmoji = "🍓",  imageRes = 0),
        QuizStep(id = 32,  title = "Sebzeler",       qmoji = "🥦",  imageRes = 0),
        QuizStep(id = 33,  title = "İçecekler",      qmoji = "☕",  imageRes = 0),
        QuizStep(id = 34,  title = "Yemekler",       qmoji = "🍔",  imageRes = 0),
        QuizStep(id = 35,  title = "Yiyecek Final",  qmoji = "🍽️",  imageRes = 0),
        // ─── ÜNİTE 2 / BÖLÜM 8 ───
        QuizStep(id = 36,  title = "Market",         qmoji = "🛒",  imageRes = 0),
        QuizStep(id = 37,  title = "Kıyafet",        qmoji = "👗",  imageRes = 0),
        QuizStep(id = 38,  title = "Fiyat Sor",      qmoji = "💰",  imageRes = 0),
        QuizStep(id = 39,  title = "Ödeme",          qmoji = "💳",  imageRes = 0),
        QuizStep(id = 40,  title = "Alışveriş Mix",  qmoji = "🛍️",  imageRes = 0),
        // ─── ÜNİTE 2 / BÖLÜM 9 ───
        QuizStep(id = 41,  title = "Araçlar I",      qmoji = "🚗",  imageRes = 0),
        QuizStep(id = 42,  title = "Araçlar II",     qmoji = "✈️",  imageRes = 0),
        QuizStep(id = 43,  title = "Yön Tarif",      qmoji = "🗺️",  imageRes = 0),
        QuizStep(id = 44,  title = "Bilet Al",       qmoji = "🎫",  imageRes = 0),
        QuizStep(id = 45,  title = "Ulaşım Final",   qmoji = "🚀",  imageRes = 0),
        // ─── ÜNİTE 2 / BÖLÜM 10 ───
        QuizStep(id = 46,  title = "Günler",         qmoji = "📅",  imageRes = 0),
        QuizStep(id = 47,  title = "Aylar",          qmoji = "🗓️",  imageRes = 0),
        QuizStep(id = 48,  title = "Mevsimler",      qmoji = "🌸",  imageRes = 0),
        QuizStep(id = 49,  title = "Saat",           qmoji = "⏱️",  imageRes = 0),
        QuizStep(id = 50,  title = "Ünite 2 Final",  qmoji = "🌟",  imageRes = 0),
        // ─── ÜNİTE 3 / BÖLÜM 11 ───
        QuizStep(id = 51,  title = "Hava I",         qmoji = "☀️",  imageRes = 0),
        QuizStep(id = 52,  title = "Hava II",        qmoji = "🌧️",  imageRes = 0),
        QuizStep(id = 53,  title = "Mevsim Hava",    qmoji = "❄️",  imageRes = 0),
        QuizStep(id = 54,  title = "Tahmin",         qmoji = "🌤️",  imageRes = 0),
        QuizStep(id = 55,  title = "Hava Final",     qmoji = "🌦️",  imageRes = 0),
        // ─── ÜNİTE 3 / BÖLÜM 12 ───
        QuizStep(id = 56,  title = "Doğa I",         qmoji = "🌳",  imageRes = 0),
        QuizStep(id = 57,  title = "Doğa II",        qmoji = "🏞️",  imageRes = 0),
        QuizStep(id = 58,  title = "Bitkiler",       qmoji = "🌺",  imageRes = 0),
        QuizStep(id = 59,  title = "Çevre Koru",     qmoji = "♻️",  imageRes = 0),
        QuizStep(id = 60,  title = "Doğa Final",     qmoji = "🍃",  imageRes = 0),
        // ─── ÜNİTE 3 / BÖLÜM 13 ───
        QuizStep(id = 61,  title = "Şehir I",        qmoji = "🏙️",  imageRes = 0),
        QuizStep(id = 62,  title = "Şehir II",       qmoji = "🏛️",  imageRes = 0),
        QuizStep(id = 63,  title = "Binalar",        qmoji = "🏢",  imageRes = 0),
        QuizStep(id = 64,  title = "Yer Tarifi",     qmoji = "🗺️",  imageRes = 0),
        QuizStep(id = 65,  title = "Şehir Final",    qmoji = "🚲",  imageRes = 0),
        // ─── ÜNİTE 3 / BÖLÜM 14 ───
        QuizStep(id = 66,  title = "Evcil Hayvan",   qmoji = "🐕",  imageRes = 0),
        QuizStep(id = 67,  title = "Vahşi Hayvan",   qmoji = "🦁",  imageRes = 0),
        QuizStep(id = 68,  title = "Deniz Hayvanı",  qmoji = "🐬",  imageRes = 0),
        QuizStep(id = 69,  title = "Kuşlar",         qmoji = "🦜",  imageRes = 0),
        QuizStep(id = 70,  title = "Hayvan Final",   qmoji = "🐾",  imageRes = 0),
        // ─── ÜNİTE 3 / BÖLÜM 15 ───
        QuizStep(id = 71,  title = "Top Sporları",   qmoji = "⚽",  imageRes = 0),
        QuizStep(id = 72,  title = "Su Sporları",    qmoji = "🏊",  imageRes = 0),
        QuizStep(id = 73,  title = "Kış Sporları",   qmoji = "⛷️",  imageRes = 0),
        QuizStep(id = 74,  title = "Spor Terimleri", qmoji = "🥇",  imageRes = 0),
        QuizStep(id = 75,  title = "Ünite 3 Final",  qmoji = "🌍",  imageRes = 0),
        // ─── ÜNİTE 4 / BÖLÜM 16 ───
        QuizStep(id = 76,  title = "Telefon",        qmoji = "📱",  imageRes = 0),
        QuizStep(id = 77,  title = "İnternet",       qmoji = "🌐",  imageRes = 0),
        QuizStep(id = 78,  title = "Sosyal Medya",   qmoji = "📸",  imageRes = 0),
        QuizStep(id = 79,  title = "E-posta",        qmoji = "📧",  imageRes = 0),
        QuizStep(id = 80,  title = "Dijital Mix",    qmoji = "💻",  imageRes = 0),
        // ─── ÜNİTE 4 / BÖLÜM 17 ───
        QuizStep(id = 81,  title = "Duygular I",     qmoji = "😊",  imageRes = 0),
        QuizStep(id = 82,  title = "Duygular II",    qmoji = "😢",  imageRes = 0),
        QuizStep(id = 83,  title = "Tepkiler",       qmoji = "😲",  imageRes = 0),
        QuizStep(id = 84,  title = "His Anlatma",    qmoji = "💬",  imageRes = 0),
        QuizStep(id = 85,  title = "Duygu Final",    qmoji = "❤️",  imageRes = 0),
        // ─── ÜNİTE 4 / BÖLÜM 18 ───
        QuizStep(id = 86,  title = "Fikir Söyle",    qmoji = "💡",  imageRes = 0),
        QuizStep(id = 87,  title = "Katılma",        qmoji = "👍",  imageRes = 0),
        QuizStep(id = 88,  title = "Reddedme",       qmoji = "👎",  imageRes = 0),
        QuizStep(id = 89,  title = "Öneri",          qmoji = "🤔",  imageRes = 0),
        QuizStep(id = 90,  title = "Fikir Final",    qmoji = "🗣️",  imageRes = 0),
        // ─── ÜNİTE 4 / BÖLÜM 19 ───
        QuizStep(id = 91,  title = "Tartışma",       qmoji = "⚖️",  imageRes = 0),
        QuizStep(id = 92,  title = "Uzlaşma",        qmoji = "🤝",  imageRes = 0),
        QuizStep(id = 93,  title = "Özür",           qmoji = "🙏",  imageRes = 0),
        QuizStep(id = 94,  title = "Teşekkür",       qmoji = "🎁",  imageRes = 0),
        QuizStep(id = 95,  title = "Nezaket Final",  qmoji = "🌷",  imageRes = 0),
        // ─── ÜNİTE 4 / BÖLÜM 20 ───
        QuizStep(id = 96,  title = "Resmi Dil",      qmoji = "📝",  imageRes = 0),
        QuizStep(id = 97,  title = "Gayri Resmi",    qmoji = "😎",  imageRes = 0),
        QuizStep(id = 98,  title = "Slang I",        qmoji = "🤙",  imageRes = 0),
        QuizStep(id = 99,  title = "Slang II",       qmoji = "✌️",  imageRes = 0),
        QuizStep(id = 100, title = "Ünite 4 Final",  qmoji = "💬",  imageRes = 0),
        // ─── ÜNİTE 5 / BÖLÜM 21 ───
        QuizStep(id = 101, title = "Okul Eşyası",    qmoji = "📚",  imageRes = 0),
        QuizStep(id = 102, title = "Dersler",        qmoji = "🔬",  imageRes = 0),
        QuizStep(id = 103, title = "Sınıf Dili",     qmoji = "✏️",  imageRes = 0),
        QuizStep(id = 104, title = "Not Sistemi",    qmoji = "📊",  imageRes = 0),
        QuizStep(id = 105, title = "Okul Final",     qmoji = "🎓",  imageRes = 0),
        // ─── ÜNİTE 5 / BÖLÜM 22 ───
        QuizStep(id = 106, title = "Meslekler I",    qmoji = "👨‍⚕️", imageRes = 0),
        QuizStep(id = 107, title = "Meslekler II",   qmoji = "👩‍🍳", imageRes = 0),
        QuizStep(id = 108, title = "İş Yerleri",     qmoji = "🏥",  imageRes = 0),
        QuizStep(id = 109, title = "Görevler",       qmoji = "📋",  imageRes = 0),
        QuizStep(id = 110, title = "Meslek Final",   qmoji = "🔑",  imageRes = 0),
        // ─── ÜNİTE 5 / BÖLÜM 23 ───
        QuizStep(id = 111, title = "İş Başvurusu",   qmoji = "📄",  imageRes = 0),
        QuizStep(id = 112, title = "Toplantı",       qmoji = "🤵",  imageRes = 0),
        QuizStep(id = 113, title = "Proje",          qmoji = "📐",  imageRes = 0),
        QuizStep(id = 114, title = "İş Yazışma",     qmoji = "✉️",  imageRes = 0),
        QuizStep(id = 115, title = "İş Hayatı Mix",  qmoji = "💼",  imageRes = 0),
        // ─── ÜNİTE 5 / BÖLÜM 24 ───
        QuizStep(id = 116, title = "Bilgisayar",     qmoji = "🖥️",  imageRes = 0),
        QuizStep(id = 117, title = "Yazılım",        qmoji = "⌨️",  imageRes = 0),
        QuizStep(id = 118, title = "Yapay Zeka",     qmoji = "🤖",  imageRes = 0),
        QuizStep(id = 119, title = "Geleceğin Tech", qmoji = "🔭",  imageRes = 0),
        QuizStep(id = 120, title = "Ünite 5 Final",  qmoji = "🎓",  imageRes = 0),
        // ─── ÜNİTE 6 / BÖLÜM 25 ───
        QuizStep(id = 121, title = "Kişilik I",      qmoji = "😇",  imageRes = 0),
        QuizStep(id = 122, title = "Kişilik II",     qmoji = "😈",  imageRes = 0),
        QuizStep(id = 123, title = "Güçlü Yönler",  qmoji = "💪",  imageRes = 0),
        QuizStep(id = 124, title = "Zayıf Yönler",  qmoji = "😅",  imageRes = 0),
        QuizStep(id = 125, title = "Kişilik Final",  qmoji = "🌟",  imageRes = 0),
        // ─── ÜNİTE 6 / BÖLÜM 26 ───
        QuizStep(id = 126, title = "Mutluluk",       qmoji = "😄",  imageRes = 0),
        QuizStep(id = 127, title = "Üzüntü",        qmoji = "😔",  imageRes = 0),
        QuizStep(id = 128, title = "Korku",          qmoji = "😨",  imageRes = 0),
        QuizStep(id = 129, title = "Öfke",           qmoji = "😤",  imageRes = 0),
        QuizStep(id = 130, title = "Duygu Karması",  qmoji = "🎭",  imageRes = 0),
        // ─── ÜNİTE 6 / BÖLÜM 27 ───
        QuizStep(id = 131, title = "Arkadaşlık",     qmoji = "👫",  imageRes = 0),
        QuizStep(id = 132, title = "Aşk",            qmoji = "💕",  imageRes = 0),
        QuizStep(id = 133, title = "Ayrılık",        qmoji = "💔",  imageRes = 0),
        QuizStep(id = 134, title = "Güven",          qmoji = "🤝",  imageRes = 0),
        QuizStep(id = 135, title = "İlişki Final",   qmoji = "💞",  imageRes = 0),
        // ─── ÜNİTE 6 / BÖLÜM 28 ───
        QuizStep(id = 136, title = "Vücut",          qmoji = "💪",  imageRes = 0),
        QuizStep(id = 137, title = "Hastalıklar",    qmoji = "🤒",  imageRes = 0),
        QuizStep(id = 138, title = "Doktora Git",    qmoji = "👨‍⚕️", imageRes = 0),
        QuizStep(id = 139, title = "Egzersiz",       qmoji = "🏋️",  imageRes = 0),
        QuizStep(id = 140, title = "Ünite 6 Final",  qmoji = "💖",  imageRes = 0),
        // ─── ÜNİTE 7 / BÖLÜM 29 ───
        QuizStep(id = 141, title = "Seyahat I",      qmoji = "✈️",  imageRes = 0),
        QuizStep(id = 142, title = "Seyahat II",     qmoji = "🧳",  imageRes = 0),
        QuizStep(id = 143, title = "Otel",           qmoji = "🏨",  imageRes = 0),
        QuizStep(id = 144, title = "Turistik Yer",   qmoji = "🗽",  imageRes = 0),
        QuizStep(id = 145, title = "Seyahat Final",  qmoji = "🌏",  imageRes = 0),
        // ─── ÜNİTE 7 / BÖLÜM 30 ───
        QuizStep(id = 146, title = "Müzik",          qmoji = "🎵",  imageRes = 0),
        QuizStep(id = 147, title = "Sinema",         qmoji = "🎬",  imageRes = 0),
        QuizStep(id = 148, title = "Tiyatro",        qmoji = "🎭",  imageRes = 0),
        QuizStep(id = 149, title = "Sanat",          qmoji = "🖼️",  imageRes = 0),
        QuizStep(id = 150, title = "Kültür Final",   qmoji = "🎨",  imageRes = 0),
        // ─── ÜNİTE 7 / BÖLÜM 31 ───
        QuizStep(id = 151, title = "Tırmanma",       qmoji = "🧗",  imageRes = 0),
        QuizStep(id = 152, title = "Sörf",           qmoji = "🏄",  imageRes = 0),
        QuizStep(id = 153, title = "Paraşüt",        qmoji = "🪂",  imageRes = 0),
        QuizStep(id = 154, title = "Ekstrem Spor",   qmoji = "🏂",  imageRes = 0),
        QuizStep(id = 155, title = "Macera Final",   qmoji = "🗻",  imageRes = 0),
        // ─── ÜNİTE 7 / BÖLÜM 32 ───
        QuizStep(id = 156, title = "Gezegenler",     qmoji = "🪐",  imageRes = 0),
        QuizStep(id = 157, title = "Uzay Aracı",     qmoji = "🛸",  imageRes = 0),
        QuizStep(id = 158, title = "Astronot",       qmoji = "👨‍🚀", imageRes = 0),
        QuizStep(id = 159, title = "Evren",          qmoji = "🌌",  imageRes = 0),
        QuizStep(id = 160, title = "Ünite 7 Final",  qmoji = "🚀",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 33 ───
        QuizStep(id = 161, title = "Tekrar I",       qmoji = "🔁",  imageRes = 0),
        QuizStep(id = 162, title = "Tekrar II",      qmoji = "📖",  imageRes = 0),
        QuizStep(id = 163, title = "Tekrar III",     qmoji = "✍️",  imageRes = 0),
        QuizStep(id = 164, title = "Tekrar IV",      qmoji = "🎧",  imageRes = 0),
        QuizStep(id = 165, title = "Karma I Final",  qmoji = "⭐",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 34 ───
        QuizStep(id = 166, title = "Karma Gramer I", qmoji = "📝",  imageRes = 0),
        QuizStep(id = 167, title = "Karma Kelime",   qmoji = "🔤",  imageRes = 0),
        QuizStep(id = 168, title = "Karma Dinle",    qmoji = "🎧",  imageRes = 0),
        QuizStep(id = 169, title = "Karma Eşleştir", qmoji = "🔗",  imageRes = 0),
        QuizStep(id = 170, title = "Karma II Final", qmoji = "⭐⭐", imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 35 ───
        QuizStep(id = 171, title = "Zaman Kipler",   qmoji = "⏳",  imageRes = 0),
        QuizStep(id = 172, title = "Sıfatlar",       qmoji = "🎯",  imageRes = 0),
        QuizStep(id = 173, title = "Zarflar",        qmoji = "⚡",  imageRes = 0),
        QuizStep(id = 174, title = "Edatlar",        qmoji = "🔧",  imageRes = 0),
        QuizStep(id = 175, title = "Gramer Final",   qmoji = "📚",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 36 ───
        QuizStep(id = 176, title = "Deyimler I",     qmoji = "💬",  imageRes = 0),
        QuizStep(id = 177, title = "Deyimler II",    qmoji = "🗣️",  imageRes = 0),
        QuizStep(id = 178, title = "Atasözleri",     qmoji = "📜",  imageRes = 0),
        QuizStep(id = 179, title = "İfadeler",       qmoji = "🎭",  imageRes = 0),
        QuizStep(id = 180, title = "Deyim Final",    qmoji = "✨",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 37 ───
        QuizStep(id = 181, title = "Şampiyon I",     qmoji = "🥇",  imageRes = 0),
        QuizStep(id = 182, title = "Şampiyon II",    qmoji = "🥈",  imageRes = 0),
        QuizStep(id = 183, title = "Şampiyon III",   qmoji = "🥉",  imageRes = 0),
        QuizStep(id = 184, title = "Şampiyon IV",    qmoji = "🏅",  imageRes = 0),
        QuizStep(id = 185, title = "Büyük Final",    qmoji = "🏆",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 38 (BONUS) ───
        QuizStep(id = 186, title = "Bonus I",        qmoji = "🎁",  imageRes = 0),
        QuizStep(id = 187, title = "Bonus II",       qmoji = "🎊",  imageRes = 0),
        QuizStep(id = 188, title = "Bonus III",      qmoji = "🎉",  imageRes = 0),
        QuizStep(id = 189, title = "Bonus IV",       qmoji = "🌈",  imageRes = 0),
        QuizStep(id = 190, title = "Bonus Final",    qmoji = "💫",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 39 (ELİT) ───
        QuizStep(id = 191, title = "Elit I",         qmoji = "💎",  imageRes = 0),
        QuizStep(id = 192, title = "Elit II",        qmoji = "👑",  imageRes = 0),
        QuizStep(id = 193, title = "Elit III",       qmoji = "⚜️",  imageRes = 0),
        QuizStep(id = 194, title = "Elit IV",        qmoji = "🔮",  imageRes = 0),
        QuizStep(id = 195, title = "Elit Final",     qmoji = "🌠",  imageRes = 0),
        // ─── ÜNİTE 8 / BÖLÜM 40 (EFSANE) ───
        QuizStep(id = 196, title = "Efsane I",       qmoji = "🔥",  imageRes = 0),
        QuizStep(id = 197, title = "Efsane II",      qmoji = "⚡",  imageRes = 0),
        QuizStep(id = 198, title = "Efsane III",     qmoji = "🌊",  imageRes = 0),
        QuizStep(id = 199, title = "Efsane IV",      qmoji = "🦋",  imageRes = 0),
        QuizStep(id = 200, title = "200. Quiz!",     qmoji = "🎆",  imageRes = 0)
    )
}
fun buildMapItems(steps: List<QuizStep>): List<MapItem> {
    val items = mutableListOf<MapItem>()
    var quizIndex = 0

    // Toplam 200 quiz / 15 quiz = Yaklaşık 14 bölüm (son bölüm 5 quiz kalacak şekilde)
    // Ya da doğrudan quiz sayısı bitene kadar dönen bir yapı daha sağlıklı olur:
    val totalQuizzes = steps.size
    val quizzesPerSection = 15
    val sectionsPerUnit = 4 // Her 60 quizde bir (4 section * 15 quiz) yeni ünite

    var currentSectionIndex = 0

    while (quizIndex < totalQuizzes) {
        val unitIndex = currentSectionIndex / sectionsPerUnit
        val unitNumber = unitIndex + 1
        val sectionNumber = currentSectionIndex + 1
        val sectionInUnit = currentSectionIndex % sectionsPerUnit

        // --- 1. ÜNİTE BAŞLIĞI ---
        // Her yeni ünite başlangıcında (0., 4., 8. sectionlarda) ekle
        if (sectionInUnit == 0 && quizIndex % (quizzesPerSection * sectionsPerUnit) == 0) {
            items.add(MapItem.UnitHeader(
                unitNumber = unitNumber,
                title = if (unitIndex < unitTitles.size) unitTitles[unitIndex] else "🏅 Ünite $unitNumber"
            ))
        }

        // --- 2. BÖLÜM BAŞLIĞI ---
        val sectionTitle = if (currentSectionIndex < sectionTitlesAll.size) {
            sectionTitlesAll[currentSectionIndex]
        } else {
            "Bölüm $sectionNumber"
        }

        items.add(MapItem.SectionHeader(
            sectionNumber = sectionNumber,
            unitNumber = unitNumber,
            title = sectionTitle
        ))

        // --- 3. QUIZ DÜĞÜMLERİ (15 Adet) ---
        for (i in 0 until quizzesPerSection) {
            if (quizIndex < totalQuizzes) {
                items.add(MapItem.QuizNode(
                    step = steps[quizIndex],
                    quizIndex = quizIndex,
                    sectionIndex = currentSectionIndex
                ))
                quizIndex++
            }
        }

        currentSectionIndex++
    }
    return items
}
@Composable
fun UnitHeaderCard(unitNumber: Int, title: String) {
    val theme = getSectionTheme((unitNumber - 1) * 5) // Ünitenin ilk bölümünün teması

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 8.dp,
            color = theme.primaryColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(theme.headerGradientStart, theme.headerGradientEnd)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ÜNİTE $unitNumber",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f),
                            letterSpacing = 3.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}
@Composable
fun SectionHeaderCard(sectionNumber: Int, unitNumber: Int, title: String) {
    val sectionIndex = sectionNumber - 1
    val theme = getSectionTheme(sectionIndex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol çizgi
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, theme.primaryColor.copy(alpha = 0.5f))
                    )
                )
        )

        // Bölüm etiketi
        Surface(
            shape = RoundedCornerShape(50.dp),
            color = theme.primaryColor.copy(alpha = 0.15f),
            border = BorderStroke(1.5.dp, theme.primaryColor.copy(alpha = 0.6f)),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BÖLÜM $sectionNumber",
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.primaryColor,
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        // Sağ çizgi
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(theme.primaryColor.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
        )
    }
}
data class VocabularyOption(
    val text: String,
    val imageRes: Int
)

// ✅ GÜNCEL VE TEKİL MODEL: Tüm quiz tiplerini (Görsel, Dinleme, Eşleştirme) kapsar.
// ✅ 1. ADIM: 'type.VISUAL' hatasını bitiren sabitler objesi


// ✅ 2. ADIM: Geliştirilmiş ve Yamalanmış VocabularyLevelData
data class VocabularyLevelData(
    val id: Int,
    val questionTurkish: String,       // Örn: "Elma"
    val correctAnswer: String,         // Örn: "apple"
    val options: List<VocabularyOption>,
    val type: QuestionType,            // VISUAL / LISTENING / MATCHING
    val imageRes: Int = 0,
    val quizList: List<String> = listOf("Genel")
) {

    // Eski kod uyumluluğu için
    val question: String
        get() = questionTurkish

    // Sadece metin listesini döndürür (Boşluk doldurma vs için)
    val optionTexts: List<String>
        get() = options.map { it.text }

    // Obje üzerinden kontrol
    fun isCorrect(option: VocabularyOption?): Boolean {
        return option?.text?.trim()?.lowercase() ==
                correctAnswer.trim().lowercase()
    }

    // String üzerinden kontrol
    fun isCorrectText(selectedText: String?): Boolean {
        return selectedText?.trim()?.lowercase() ==
                correctAnswer.trim().lowercase()
    }
}
@Composable
fun MainQuizResult(
    navController: NavHostController,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tebrik Görseli (Varsa)
        Image(
            painter = painterResource(id = R.drawable.cat_logo),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tebrikler kanka!", // ✅ Bold
            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        )

        Text(
            text = "Bu aşamayı başarıyla geçtin.",
            style = TextStyle(fontSize = 16.sp, color = Color.Gray),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CB0F6))
        ) {
            Text("HARİTAYA DÖN", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

val vocabularyQuizList = listOf(
    VocabularyLevelData(
        id = 1,
        questionTurkish = "Bardak",
        correctAnswer = "glass",
        options = listOf(
            VocabularyOption("mug", R.drawable.cat_logo),
            VocabularyOption("glass", R.drawable.cat_logo),
            VocabularyOption("pot", R.drawable.cat_logo),
            VocabularyOption("milk", R.drawable.cat_logo)
        ),
        type = QuestionType.VISUAL,
        imageRes = R.drawable.cat_logo,
        quizList = listOf("Kitchen")
    ),
    VocabularyLevelData(
        id = 2,
        questionTurkish = "Elma",
        correctAnswer = "apple",
        options = listOf(
            VocabularyOption("apple", R.drawable.cat_logo),
            VocabularyOption("banana", R.drawable.cat_logo),
            VocabularyOption("orange", R.drawable.cat_logo),
            VocabularyOption("grape", R.drawable.cat_logo)
        ),
        type = QuestionType.VISUAL,
        imageRes = R.drawable.cat_logo,
        quizList = listOf("Fruits")
    )
)


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun boslukdoldurma(
    data: VocabularyLevelDataQuiz,
    progress: Float,
    health: Int,
    ttsHelper: SpeechAssistant,
    // ❌ ESKİSİ: onAnswerResult: (Boolean) -> Unit
    onAnswerResult: (Boolean, String) -> Unit // ✅ YENİSİ: String parametresini buraya ekledik!
) {
    // Yeni soru geldiğinde (data değiştiğinde) listeleri sıfırla
    val selectedWords = remember(data) { mutableStateListOf<String>() }
    val wordPool = remember(data) {
        // 🛠️ DÜZELTME: 'optionTexts' yerine 'options' (Repository ile uyum için)
        mutableStateListOf<String>().apply { addAll(data.options.shuffled()) }
    }

    var isChecked by remember(data) { mutableStateOf(false) }

    // Cevap kontrolü
    val currentAnswerString = selectedWords.joinToString(" ")
    // ✅ Boşlukları temizleyip küçük harfe çevirerek daha güvenli kontrol yapıyoruz
    val isCorrect = currentAnswerString.trim().lowercase() == data.correctAnswer.trim().lowercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. SORU GÖRSELİ VE SES ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF7F7F7),
                border = BorderStroke(2.dp, Color(0xFFE5E5E5))
            ) {
                Image(
                    painter = painterResource(id = data.imageRes),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(20.dp))
            Surface(
                modifier = Modifier
                    .size(65.dp)
                    .clickable { ttsHelper.speak(data.correctAnswer) },
                shape = CircleShape,
                color = Color(0xFF1CB0F6),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- 3. BOŞLUK ALANI ---
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    val color = when {
                        !isChecked -> Color(0xFFE5E5E5)
                        isCorrect -> Color(0xFF58CC02)
                        else -> Color(0xFFEA2B2B)
                    }
                    drawLine(color, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth)
                },
            horizontalArrangement = Arrangement.Center
        ) {
            selectedWords.forEach { word ->
                key(word) {
                    WordChip(
                        word = word,
                        color = Color.White,
                        // ✅ Doğruysa yeşil, yanlışsa kırmızı metin
                        textColor = if (isChecked) (if (isCorrect) Color(0xFF58CC02) else Color(0xFFEA2B2B)) else Color(0xFF1CB0F6)
                    ) {
                        if (!isChecked) {
                            selectedWords.remove(word)
                            wordPool.add(word)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 4. KELİME HAVUZU ---
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            wordPool.forEach { word ->
                key(word) {
                    WordChip(
                        word = word,
                        color = Color(0xFFF1F1F1),
                        textColor = Color.Black
                    ) {
                        if (!isChecked) {
                            selectedWords.add(word)
                            wordPool.remove(word)
                            ttsHelper.speak(word) // 🔊 Kelimeyi seslendir
                        }
                    }
                }
            }
        }

        // --- 5. BUTON ---
        Button(
            onClick = {
                if (!isChecked) {
                    isChecked = true
                    // ✅ İlk tıkta sonucu merkeze bildiriyoruz
                    onAnswerResult(isCorrect, currentAnswerString)
                } else {
                    // ✅ İkinci tıkta (Devam Et), MainQuizScreen zaten sheet üzerinden akışı yönetiyor
                    // Ama burayı da boş geçmiyoruz
                }
            },
            enabled = selectedWords.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedWords.isEmpty()) Color(0xFFE5E5E5) else Color(0xFF58CC02)
            )
        ) {
            Text(
                text = if (!isChecked) "KONTROL ET" else "DEVAM ET",
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            )
        }
    }
}

// Kelime butonları için yardımcı bileşen
@Composable
fun WordChip(word: String, color: Color, textColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = BorderStroke(2.dp, Color(0xFFDCDCDC)),
        shadowElevation = 2.dp
    ) {
        Text(
            text = word,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = textColor)
        )
    }
}
@Composable
fun VisualChoiceQuiz(
    data: VocabularyLevelData,
    onAnswerResult: (Boolean) -> Unit
) {
    var selectedOption by remember { mutableStateOf<VocabularyOption?>(null) }
    var isChecked by remember { mutableStateOf(false) }

    // ✅ ŞIKLARI KARIŞTIRMA MANTIĞI
    // Sadece data.id değiştiğinde (yeni soru geldiğinde) listeyi karıştırır.
    val shuffledOptions = remember(data.id) {
        data.options.shuffled()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = data.question,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ✅ Orijinal data.options yerine shuffledOptions kullanıyoruz
            items(shuffledOptions) { option ->

                val isCorrect = option.text == data.correctAnswer
                val isThisSelected = selectedOption == option

                val borderColor = when {
                    !isChecked && isThisSelected -> Color(0xFF1CB0F6)
                    isChecked && isCorrect -> Color(0xFF58CC02)
                    isChecked && isThisSelected && !isCorrect -> Color(0xFFEA2B2B)
                    else -> Color(0xFFE5E5E5)
                }

                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable(enabled = !isChecked) {
                            selectedOption = option
                        },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(3.dp, borderColor),
                    color = if (isChecked && isCorrect)
                        Color(0xFFD7FFB8)
                    else
                        Color.White
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = option.imageRes),
                            contentDescription = null,
                            modifier = Modifier.size(70.dp)
                        )

                        Text(
                            text = option.text,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (!isChecked) {
                    isChecked = true
                    onAnswerResult(
                        data.isCorrect(selectedOption)
                    )
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (selectedOption == null)
                        Color.LightGray
                    else
                        Color(0xFF58CC02)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = if (!isChecked) "KONTROL ET" else "DEVAM ET",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
fun imageOf(option: String): Int {
    return when (option.lowercase()) {
        "water", "su" -> R.drawable.water
        "apple", "elma" -> R.drawable.apple
        "book", "kitap" -> R.drawable.book
        "car", "araba" -> R.drawable.car
        "cat", "kedi" -> R.drawable.cat
        "dog", "köpek" -> R.drawable.dog
        "fish", "balık" -> R.drawable.fish
        "bird", "kuş" -> R.drawable.bird
        else -> R.drawable.cat_logo
    }
}
val SoftIceBlue = Color(0xFFB3E5FC)
fun Modifier.eliteIceGradient(): Modifier = this.background(
    brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFB3E5FC).copy(alpha = 0.6f),
            Color.White
        ),
        startY = 0f,
        endY = 800f
    )
)
@Composable
fun eslestirme(
    data: VocabularyLevelDataQuiz, // 👈 'Quiz' ekledik
    onAnswerResult: (Boolean) -> Unit,
    health: Int,
    progress: Float
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
                    // Doğru eşleşme
                    matchedLeftWords.add(selectedLeft!!)
                    val currentSelectionLeft = selectedLeft // Referans için tutuyoruz

                    selectedLeft = null
                    selectedRight = null

                    // ✅ KRİTİK: Son kelime eşleştiyse bitir
                    if (matchedLeftWords.size == pairs.size) {
                        kotlinx.coroutines.delay(300) // Kullanıcı son eşleşmeyi görsün
                        onAnswerResult(true) // Üst katmana "bitir" emri gönder
                    }
                } else {
                    // Yanlış eşleşme
                    kotlinx.coroutines.delay(500)
                    selectedLeft = null
                    selectedRight = null
                    onAnswerResult(false) // Can azaltma tetiklemesi
                }
            }
        }
    }
}


@Composable
fun MatchingChip(word: String, isSelected: Boolean, isMatched: Boolean, onClick: () -> Unit) {
    val borderColor = when {
        isMatched -> Color(0xFF58CC02) // Yeşil (Eşleşti)
        isSelected -> Color(0xFF1CB0F6) // Mavi (Seçili)
        else -> Color(0xFFE5E5E5) // Gri (Boşta)
    }

    val backgroundColor = when {
        isMatched -> Color(0xFFD7FFB8)
        isSelected -> Color(0xFFDDF4FF)
        else -> Color.White
    }

    Surface(
        modifier = Modifier
            .width(150.dp)
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor,
        shadowElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .eliteIceGradient()
        ) {

        }

        Box(contentAlignment = Alignment.Center) {
            Text(
                text = word,
                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isMatched) Color(0xFF58CC02) else Color.Black)
            )
        }
    }
}

@Composable
fun DialogOptionButton(optionText: String, onClick: () -> Unit) {
    // Yazıyı satırlarına ayırıyoruz: "Hot please" ve "(Sıcak lütfen)"
    val parts = optionText.split("\n")
    val mainText = parts[0]
    val subText = if (parts.size > 1) parts[1] else ""

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .height(75.dp), // Türkçe alt yazı için yüksekliği artırdık
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = mainText, // Örn: "Hot please"
                color = Color.Black,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
            if (subText.isNotEmpty()) {
                Text(
                    text = subText, // Örn: "(Sıcak lütfen)"
                    color = Color.Gray,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
                )
            }
        }
    }
}

@Composable
fun ListeningQuizContent(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (Boolean) -> Unit
) {
    var isTextVisible by remember { mutableStateOf(false) }
    val primaryColor = Color(0xFF1CB0F6)

    // ✅ ŞIKLARI KARIŞTIRMA MANTIĞI
    // remember(data.id) sayesinde sadece soru değiştiğinde bir kez karıştırılır.
    // Böylece her recomposition (ekran yenilenmesi) olduğunda şıkların yeri durduk yere değişmez.
    val shuffledOptions = remember(data.id) {
        data.options.shuffled()
    }

    LaunchedEffect(data.id) {
        isTextVisible = false
        tts.speak(data.question)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .eliteIceGradient()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // --- KARAKTER VE KONUŞMA BALONU ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp)
        ) {
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(data.imageSize.dp)
                    .padding(data.imagePadding.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isTextVisible,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Text(
                                text = data.question,
                                color = Color.Black,
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            )
                        }

                        if (!isTextVisible) {
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
                            onClick = { tts.speak(data.question) },
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

        Spacer(modifier = Modifier.height(20.dp))

        // --- CEVAP ŞIKLARI (KARIŞTIRILMIŞ LİSTE) ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Burada data.options yerine shuffledOptions kullanıyoruz
            shuffledOptions.forEach { option ->
                DialogOptionButton(optionText = option) {
                    tts.speak(option)
                    val isCorrect = option.equals(data.correctAnswer, ignoreCase = true)
                    onAnswer(isCorrect)
                }
            }
        }
    }
}
// QUİZ BAŞNAGIÇ 1. QUİZ

// ✅ KURAL: Görsel soru başlıkları Türkçe ("Hangisi ...") olarak güncellendi.
// ✅ KURAL: İlk 5 Görsel -> Sonra (1 Dinleme + 1 Eşleştirme) döngüsü.
// ✅ KURAL: Yıldızlar () kaldırıldı.


@Composable
fun VisualQuestionTitle(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF4B4B4B)
        ),
        modifier = Modifier.padding(bottom = 20.dp)
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GorselQuizScreen(
    navController: NavHostController,
    tts: SpeechAssistant,
    onAnswer: (Boolean) -> Unit
) {
    // ✅ VocabularyLevelData → VocabularyLevelDataQuiz
    val questions: List<VocabularyLevelDataQuiz> = remember { QuizRepository.getQuizOne() }

    var currentIndex by remember { mutableIntStateOf(0) }
    var wrongAnswersCount by remember { mutableIntStateOf(0) }
    var showFailedDialog by remember { mutableStateOf(false) }
    var showResultSheet by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }
    var correctAnswerText by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val maxAllowedErrors = (questions.size * 0.3).toInt()
    val progress = if (questions.isNotEmpty()) currentIndex.toFloat() / questions.size else 0f

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.weight(1f).height(10.dp).clip(CircleShape),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.width(16.dp))
                val remainingRights = (maxAllowedErrors + 1) - wrongAnswersCount
                Text(
                    text = "❤️ $remainingRights",
                    color = if (remainingRights <= 1) Color.Red else Color(0xFFE91E63),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (currentIndex < questions.size) {
                val currentQuestion = questions[currentIndex]

                when (currentQuestion.questionType) {
                    QuestionType.VISUAL -> GorselQuestionUI(
                        data = currentQuestion,
                        tts = tts,
                        onAnswer = { isCorrect ->
                            lastAnswerCorrect = isCorrect
                            correctAnswerText = currentQuestion.correctAnswer
                            showResultSheet = true
                        }
                    )

                    QuestionType.LISTENING -> ListeningQuizUI(
                        data = currentQuestion,
                        tts = tts,
                        currentIndex = currentIndex,
                        totalQuestions = questions.size,
                        onAnswer = { isCorrect ->
                            lastAnswerCorrect = isCorrect
                            correctAnswerText = currentQuestion.correctAnswer
                            showResultSheet = true
                        }
                    )

                    QuestionType.MATCHING -> eslestirme(
                        data = currentQuestion,
                        progress = progress,
                        health = (maxAllowedErrors + 1) - wrongAnswersCount,
                        onAnswerResult = { isCorrect ->
                            lastAnswerCorrect = isCorrect
                            correctAnswerText = currentQuestion.correctAnswer
                            showResultSheet = true
                        }
                    )

                    else -> {
                        Text("Bilinmeyen Soru Tipi", color = Color.Red)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tebrikler, Quiz başarıyla tamamlandı!", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Ana Menüye Dön")
                    }
                }
            }
        }

        if (showFailedDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Maalesef Başaramadın", color = Color.Red) },
                text = { Text("Hata limitini (%30) aştın.") },
                confirmButton = {
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Haritaya Dön")
                    }
                }
            )
        }

        if (showResultSheet) {
            ModalBottomSheet(
                onDismissRequest = { showResultSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (lastAnswerCorrect) {
                        Text(text = "🎉 Tebrikler!!", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Soruyu doğru bildiniz!", fontSize = 18.sp)
                    } else {
                        Text(text = "❌ Üzgünüz!", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.Red)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Cevabınız yanlış.", fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Doğru cevap:", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = correctAnswerText, fontWeight = FontWeight.Bold, color = Color(0xFF1CB0F6))
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showResultSheet = false
                            if (!lastAnswerCorrect) {
                                wrongAnswersCount++
                                if (wrongAnswersCount > maxAllowedErrors) {
                                    showFailedDialog = true
                                    return@Button
                                }
                            }
                            currentIndex++
                            onAnswer(lastAnswerCorrect)
                        }
                    ) {
                        Text("Devam Et")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

// Cevap kontrolü için yardımcı küçük fonksiyon (Kod kalabalığını önler)
private fun handleAnswer(isCorrect: Boolean, onSuccess: () -> Unit, onFailure: () -> Unit) {
    if (isCorrect) onSuccess() else onFailure()
}



@Composable
fun ListeningQuizUI(
    data: VocabularyLevelDataQuiz,
    currentIndex: Int,
    totalQuestions: Int,
    tts: SpeechAssistant,
    onAnswer: (Boolean) -> Unit
) {
    var isTextVisible by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isChecked by remember { mutableStateOf(false) }

    // ✅ Seçenekler her yeni soruda karışık sıralanır
    // remember(data.id) → soru değişince yeniden karıştırır,
    // ama aynı soruda recomposition olursa karışmaz (sabit kalır)
    val shuffledOptions by remember(data.id) {
        mutableStateOf(data.options.shuffled())
    }

    // ✅ Soru değişince reset + ses
    LaunchedEffect(data.id) {
        isTextVisible = false
        selectedOption = null
        isChecked = false
        tts.speak(data.questionTurkish)
    }

    val primaryColor = Color(0xFF1CB0F6)

    val radialGradient = Brush.radialGradient(
        colors = listOf(Color.White, Color(0xFFB3E5FC)),
        radius = 1500f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(radialGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- PROGRESS BAR ---
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${currentIndex + 1} / $totalQuestions",
                    color = primaryColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 50.dp, bottom = 4.dp)
                )

                Surface(
                    color = Color(0xFFF0F0F0),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.kupa),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        LinearProgressIndicator(
                            progress = (currentIndex + 1).toFloat() / totalQuestions,
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(CircleShape),
                            color = primaryColor,
                            trackColor = Color.White.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // --- SORU ALANI ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = data.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color(0xFFF0F0F0)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 40.dp)
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isTextVisible
                            ) {
                                Text(
                                    text = data.questionTurkish,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!isTextVisible) {
                                Text(
                                    text = "???",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Gray.copy(alpha = 0.5f)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = {
                                isTextVisible = !isTextVisible
                            }) {
                                Text(
                                    text = if (isTextVisible) "Gizle" else "İpucu",
                                    color = primaryColor
                                )
                            }

                            IconButton(onClick = {
                                tts.speak(data.questionTurkish)
                            }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = primaryColor)
                            }
                        }
                    }
                }
            }

            // --- SEÇENEKLER (karışık sırada) ---
            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                border = BorderStroke(
                    2.dp,
                    primaryColor.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    // ✅ data.options yerine shuffledOptions kullanılıyor
                    shuffledOptions.forEach { option ->

                        val isSelected = selectedOption == option

                        val buttonColor = when {
                            isChecked && option == data.correctAnswer ->
                                Color(0xFFC8E6C9)

                            isChecked && isSelected && option != data.correctAnswer ->
                                Color(0xFFFFCDD2)

                            isSelected ->
                                Color(0xFFE3F2FD)

                            else -> Color.White
                        }

                        Button(
                            onClick = {
                                if (!isChecked) {
                                    selectedOption = option
                                    tts.speak(option)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .height(65.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor
                            ),
                            shape = RoundedCornerShape(25.dp),
                            border = BorderStroke(
                                if (isSelected) 3.dp else 2.dp,
                                if (isSelected) primaryColor else Color.Black.copy(0.1f)
                            )
                        ) {
                            Text(
                                text = option,
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- KONTROL BUTONU ---
            Button(
                onClick = {
                    if (!isChecked) {
                        isChecked = true
                    } else {
                        onAnswer(selectedOption == data.correctAnswer)
                    }
                },
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isChecked)
                        Color(0xFF4CAF50)
                    else
                        primaryColor
                )
            ) {
                Text(
                    text = if (isChecked) "DEVAM ET" else "KONTROL ET",
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )
            }
        }
    }
}

// 1. ADIM: Sınırlayıcıyı Kaldıran Yeni UI Fonksiyonu
@Composable
fun VisualQuizContent(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (Boolean, String) -> Unit // ✅ İki parametreli (Boolean, String)
) {
    var selectedOption by remember(data) { mutableStateOf<String?>(null) }
    var isChecked by remember(data) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(16.dp)
    ) {
        Text(
            text = "Doğru seçeneği belirleyin",
            style = TextStyle(fontSize = 18.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Üstteki Dinleme Alanı
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { tts.speak(data.correctAnswer) },
                modifier = Modifier.size(42.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ses),
                    contentDescription = "Dinle",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = data.question,
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF1CB0F6))
            )
        }

        // Izgara Şıklar (Grid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(data.options) { optionText ->
                val isThisSelected = selectedOption == optionText

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isThisSelected) Color(0xFF1CB0F6) else Color.White)
                        .border(
                            width = 3.dp,
                            color = if (isThisSelected) Color(0xFF0091D5) else Color(0xFFE5E5E5),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable {
                            if (!isChecked) {
                                selectedOption = optionText
                                tts.speak(optionText) // Seçince seslendirir
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = optionText,
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isThisSelected) Color.White else Color.Black
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Kontrol Et Butonu
        Button(
            onClick = {
                if (!isChecked && selectedOption != null) {
                    isChecked = true
                    // ✅ DÜZELTME: Hem doğruluğu (Boolean) hem seçilen metni (String) gönderiyoruz
                    onAnswer(data.isCorrect(selectedOption), selectedOption!!)
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(65.dp).padding(bottom = 8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(selectedOption != null) Color(0xFF1CB0F6) else Color(0xFFE5E5E5),
                contentColor = Color.White
            )
        ) {
            Text(
                text = if (isChecked) "DEVAM ET" else "KONTROL ET",
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Black)
            )
        }
    }
}
@Composable
fun VisualOptionCard(
    optionText: String,
    imageRes: Int,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(12.dp)
    ) {
        // GÖRSEL ARKASINA MAVİ YUVARLAK VE BÜYÜTÜLMÜŞ PNG
        Box(
            modifier = Modifier
                .size(105.dp)
                .background(
                    color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE3F2FD),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = optionText,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = optionText,
            style = TextStyle(
                fontWeight = FontWeight.Bold, // ✅ KURAL: Bold
                fontSize = 18.sp,
                color = if (isSelected) Color.White else Color(0xFF4B4B4B)
            )
        )
    }
}

@Composable
fun VocabularyImageCard(
    option: VocabularyOption, // ✅ VocabularyOption tipinde veri bekliyor
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Seçim durumuna göre renkleri belirliyoruz
    val borderColor = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)
    val backgroundColor = if (isSelected) Color(0xFFDDF4FF) else Color.White
    val textColor = if (isSelected) Color(0xFF1CB0F6) else Color(0xFF4B4B4B)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Kare formunda
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp), // Instagram tarzı kavisli köşeler
        border = BorderStroke(2.dp, borderColor),
        color = backgroundColor,
        shadowElevation = if (isSelected) 0.dp else 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(12.dp)
        ) {
            // Görsel Alanı
            Image(
                painter = painterResource(id = option.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Kelime Metni
            Text(
                text = option.text,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, // ✅ KURAL: Bold
                    color = textColor
                )
            )
        }
    }
}


// ✅ UNRESOLVED REFERENCE HATASINI BİTİREN FONKSİYONLAR
@Composable
fun ResultPanelContent(
    isCorrect: Boolean, // Bunu eklediğinden emin ol
    correctAnswer: String,
    onContinue: () -> Unit
) {
    Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Yanlış Cevap!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text("Doğru cevap: $correctAnswer", modifier = Modifier.padding(vertical = 12.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("ANLADIM")
        }
    }
}

// --- 🛠️ EKSİK OLAN YARDIMCI COMPOSABLE'LAR ---

@Composable
fun TopQuizBar(hearts: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = hearts.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
    }
}



// BOTTOM SHEET EKRANI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetTestScreen(navController: NavHostController) {

    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Button(onClick = { showSheet = true }) {
                Text("Sheet Aç")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Geri Dön")
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Sheet Çalışıyor 🎉",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = { showSheet = false }) {
                        Text("Kapat")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    navController: NavHostController,
    tts: SpeechAssistant,
    ttsHelper: SpeechAssistant,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var hearts by remember { mutableIntStateOf(5) }
    var streakCount by remember { mutableIntStateOf(0) }
    var showResultPanel by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val questions = remember { QuizRepository.getQuizOne() }
    val currentQuestion = questions.getOrNull(currentIndex)
    val totalQuestions = questions.size

    val onAnswerResult: (Boolean, String) -> Unit = { isCorrect, selectedText ->
        tts.speak(selectedText)
        isAnswerCorrect = isCorrect
        if (isCorrect) {
            streakCount++
        } else {
            streakCount = 0
            hearts--
        }
        showResultPanel = true
    }

    fun handleNextStep() {
        showResultPanel = false
        if (currentIndex < totalQuestions - 1) {
            currentIndex++
        } else {
            navController.popBackStack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.White) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

                // ✅ YAMA: Artık tüm parametreler TopBar tanımıyla eşleşiyor
                QuizTreeTopBar(
                    navController = navController,
                    levelName = "A1",
                    coins = 1250,
                    gems = 85,
                    currentIndex = currentIndex,
                    totalQuestions = totalQuestions, // questions.size yerine direkt değişkeni verdik
                    streakCount = streakCount,
                    hearts = hearts,
                    isStreakActive = streakCount >= 3,
                    onClose = { navController.popBackStack() }
                )

                Box(modifier = Modifier.weight(1f)) {
                    currentQuestion?.let { question ->
                        when (question.questionType) {
                            QuestionType.VISUAL -> VisualQuizContent(
                                data = question,
                                tts = tts,
                                onAnswer = onAnswerResult
                            )

                            QuestionType.LISTENING -> ListeningQuizContent(
                                data = question,
                                tts = tts,
                                onAnswer = { isCorrect ->
                                    onAnswerResult(isCorrect, "")
                                }
                            )

                            QuestionType.MATCHING -> eslestirme(
                                data = question,
                                onAnswerResult = { isCorr: Boolean ->
                                    onAnswerResult(isCorr, "")
                                },
                                health = hearts,
                                progress = if (totalQuestions > 0) currentIndex.toFloat() / totalQuestions else 0f
                            )

                            QuestionType.FILL_IN_BLANK -> boslukdoldurma(
                                data = question,
                                onAnswerResult = onAnswerResult,
                                health = hearts,
                                progress = if (totalQuestions > 0) currentIndex.toFloat() / totalQuestions else 0f,
                                ttsHelper = tts
                            )
                            else -> { /* Hata logu veya boş alan */ }
                        }
                    }
                }
            }
        }

        if (showResultPanel && currentQuestion != null) {
            ModalBottomSheet(
                onDismissRequest = { showResultPanel = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ResultPanelContent(
                    isCorrect = isAnswerCorrect,
                    correctAnswer = currentQuestion.correctAnswer,
                    onContinue = {
                        if (hearts <= 0) {
                            navController.popBackStack()
                        } else {
                            handleNextStep()
                        }
                    }
                )
            }
        }
    }
}
sealed class QuizQuestion {

    data class Visual(
        val id: Int,
        val question: String,
        val options: List<String>,
        val correctAnswer: String,
        val imageRes: Int
    ) : QuizQuestion()

    data class Listening(
        val id: Int,
        val question: String,
        val options: List<String>,
        val correctAnswer: String
    ) : QuizQuestion()

    data class Matching(
        val id: Int,
        val question: String,
        val pairs: List<Pair<String, String>>
    ) : QuizQuestion()
}

@Composable
fun GorselQuestionUI(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = data.questionTurkish, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))

        Spacer(modifier = Modifier.height(16.dp))

        data.options.forEach { option ->
            Button(
                onClick = {
                    // data.isCorrect artık String bekliyor, option da String!
                    onAnswer(data.isCorrect(option))
                    tts.speak(option)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                // EĞER BURADA HATA ALIYORSAN: option bir nesne kalmış demektir.
                // VocabularyLevelData içindeki 'options'ın List<String> olduğundan emin ol.
                Text(text = option)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TranslationQuizScreen(
    currentQuestion: VocabularyLevelDataQuiz,
    onQuizFailed: () -> Unit,
    onQuizSuccess: () -> Unit
) {
    val correctAnswerText = currentQuestion.correctAnswer
    val correctWords = remember(correctAnswerText) { correctAnswerText.split(" ") }

    val wordPool = remember(correctWords) {
        mutableStateListOf<String>().apply { addAll(currentQuestion.options.shuffled()) }
    }
    val selectedWords = remember { mutableStateListOf<String>() }

    val sheetState = rememberModalBottomSheetState()
    var showResultSheet by remember { mutableStateOf(false) }
    var lastAnswerCorrect by remember { mutableStateOf(false) }
    var isCheckButtonEnabled by remember { mutableStateOf(true) }
    var wrongAnswersCount by remember { mutableStateOf(0) }
    val maxAllowedErrors = 3

    // Her satırın taşıyabileceği yaklaşık max genişlik için ölçüm
    // Kelimeler 3 satıra bölünür: satır 1 → ilk N kelime, satır 2 → sonraki N, satır 3 → kalanlar
    // Ama biz FlowRow ile yapacağız, sadece 3 satırı Canvas ile çizeceğiz

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // ─────────────────────────────────────────────────────────
            // 1. Solda Türkçe metin, sağda resim
            // ─────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentQuestion.questionTurkish,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = currentQuestion.imageRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─────────────────────────────────────────────────────────
            // 2. Mavi kutucuk + sağ uçta büyük yuvarlak tik
            // ─────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(end = 28.dp)
                        .background(Color(0xFF1CB0F6), RoundedCornerShape(14.dp))
                        .padding(start = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "Yukarıdaki metni çevirin",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(56.dp)
                        .background(Color(0xFF1CB0F6), CircleShape)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─────────────────────────────────────────────────────────
            // 3. CEVAP ALANI: 3 kesikli çizgi + üzerine kelimeler
            //
            // Mantık:
            //   • Her satır yüksekliği 52.dp
            //   • Canvas arka planda 3 yatay kesikli çizgi çizer
            //   • FlowRow kelimeleri sırayla bu çizgilerin üstüne yerleştirir
            //   • Kelimeye tıklayınca geri havuza döner
            // ─────────────────────────────────────────────────────────
            val lineHeight = 52.dp
            val answerAreaHeight = lineHeight * 3

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(answerAreaHeight)
            ) {
                // Arka plan: 3 kesikli yatay çizgi
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val singleLine = size.height / 3
                    for (i in 1..3) {
                        drawLine(
                            color = Color(0xFFCCCCCC),
                            start = Offset(0f, singleLine * i),
                            end = Offset(size.width, singleLine * i),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f),
                            strokeWidth = 3f
                        )
                    }
                }

                // Seçilen kelimeler — her kelime çizgi satırına oturur gibi hizalanır
                // FlowRow kelimeyi bir sonraki satıra geçince alt çizgiye oturur
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = Int.MAX_VALUE
                ) {
                    selectedWords.forEach { word ->
                        Box(
                            modifier = Modifier
                                .height(44.dp)            // Satır yüksekliğine otur
                                .padding(horizontal = 4.dp)
                                .background(Color.White, RoundedCornerShape(8.dp))
                                .border(1.5.dp, Color(0xFF1CB0F6), RoundedCornerShape(8.dp))
                                .clickable(enabled = isCheckButtonEnabled) {
                                    selectedWords.remove(word)
                                    wordPool.add(word)
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word,
                                fontSize = 15.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Alan boşsa yönlendirici gri metin
                if (selectedWords.isEmpty()) {
                    Text(
                        text = "Kelimelere tıklayarak cümle kur",
                        color = Color(0xFFBBBBBB),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 8.dp, top = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─────────────────────────────────────────────────────────
            // 4. KELİME HAVUZU — altta tek tek kutucuklar
            //
            // Örnek: "I" | "went" | "to" | "school" | "today" | "and" | "it" | "was" | "empty"
            // Her birine tıklayınca yukarıdaki çizgi alanına eklenir
            // ─────────────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF8F9FA),
                border = BorderStroke(1.dp, Color(0xFFE8ECF0))
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    wordPool.forEach { word ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(
                                    1.5.dp,
                                    Color(0xFFDDE3EA),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = isCheckButtonEnabled) {
                                    selectedWords.add(word)
                                    wordPool.remove(word)
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = word,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ─────────────────────────────────────────────────────────
            // 5. KONTROL ET butonu
            // ─────────────────────────────────────────────────────────
            Button(
                onClick = {
                    isCheckButtonEnabled = false
                    val userResponse = selectedWords.joinToString(" ")
                    lastAnswerCorrect = currentQuestion.isCorrect(userResponse)
                    showResultSheet = true
                },
                enabled = isCheckButtonEnabled && selectedWords.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1CB0F6),
                    disabledContainerColor = Color(0xFFB3E5FC)
                ),
                border = BorderStroke(3.dp, Color(0xFF0072BB))
            ) {
                Text(
                    text = "KONTROL ET",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // SONUÇ BOTTOM SHEET
    // ─────────────────────────────────────────────────────────────────
    if (showResultSheet) {
        ModalBottomSheet(
            onDismissRequest = { showResultSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (lastAnswerCorrect) {
                    Text(
                        text = "🎉 Tebrikler!!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Soruyu doğru bildiniz!", fontSize = 18.sp)
                } else {
                    Text(
                        text = "❌ Üzgünüz!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Cevabınız yanlış.", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Doğru cevap:", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = correctAnswerText,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1CB0F6)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1CB0F6)
                    ),
                    onClick = {
                        showResultSheet = false
                        isCheckButtonEnabled = true
                        if (!lastAnswerCorrect) {
                            wrongAnswersCount++
                            if (wrongAnswersCount > maxAllowedErrors) {
                                onQuizFailed()
                                return@Button
                            }
                        }
                        onQuizSuccess()
                    }
                ) {
                    Text(
                        "Devam Et",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

object QuizRepository {



    )
    fun getQuizTwo(): List<VocabularyLevelDataQuiz> {
        return listOf(
            createListening(
                id = 1, // ID'leri sıralı yapmak takibi kolaylaştırır
                qTurkish = "Hello",
                res = R.drawable.kisi1,
                ansEnglish = "Merhaba",
                opts = listOf("Merhaba", "Nasılsın", "Nasıl gidiyor") // Doğru şık listede olmalı
            ),
            createListening(
                id = 2,
                qTurkish = "How are you?",private val imageMap = mapOf(
                    // --- Temel Nesneler ---
                    "water" to R.drawable.water,
            "apple" to R.drawable.apple,
            "book" to R.drawable.book,
            "car" to R.drawable.car,
            "cat" to R.drawable.cat,
            "dog" to R.drawable.dog,
            "bird" to R.drawable.bird,
            "fish" to R.drawable.fish,
            "tea" to R.drawable.tea,
            "milk" to R.drawable.milk,
            "juice" to R.drawable.juice,
            "bread" to R.drawable.bread,
            "sugar" to R.drawable.sugar,
            "honey" to R.drawable.honey,
            "hot" to R.drawable.hot,
            "cold" to R.drawable.cold,
            "teacher" to R.drawable.teacher,
            "nurse" to R.drawable.nurse,
            "student" to R.drawable.student,
            "partner" to R.drawable.partner,
            "friend" to R.drawable.friend,

            // --- Karakterler & Uygulama Görselleri ---
            "kisi1" to R.drawable.kisi1,
            "kisi2" to R.drawable.kisi2,
            "kisi3" to R.drawable.kisi3,
            "kisi4" to R.drawable.kisi4,
            "kedi1" to R.drawable.kedi1,
            "kedi2" to R.drawable.kedi2,
            "kedi3" to R.drawable.kedi3,
            "kedi4" to R.drawable.kedi4,
            "cat_logo" to R.drawable.cat_logo, // Uygulama ana logosu ve maskot

            // --- Aile Üyeleri ---
            "father" to R.drawable.dad,
            "mother" to R.drawable.mother,
            "brother" to R.drawable.brother,
            "sister" to R.drawable.sister,
            "grandfather" to R.drawable.grandfather,
            "grandmother" to R.drawable.grandmother,
            "baby" to R.drawable.baby,
            "family_photo" to R.drawable.family,

            // --- Ev & Mekanlar ---
            "house" to R.drawable.house,
            "garden" to R.drawable.garden,
            "balcony" to R.drawable.balcony,
            "garage" to R.drawable.garage,
            "roof" to R.drawable.roof,
            "door" to R.drawable.door,
            "window" to R.drawable.window,
            "bedroom" to R.drawable.bedroom,
            "livingroom" to R.drawable.livingroom,
            "kitchen" to R.drawable.kitchen,
            "bathroom" to R.drawable.bathroom,
            "diningroom" to R.drawable.diningroom,
            "laundryroom" to R.drawable.laundryroom,

            // --- Eşyalar & Mobilyalar ---
            "washing machine" to R.drawable.washingmachine,
            "refrigerator" to R.drawable.refrigerator,
            "wardrobe" to R.drawable.wardrobe,
            "curtain" to R.drawable.curtain,
            "shelf" to R.drawable.shelf,
            "drawer" to R.drawable.drawer,
            "oven" to R.drawable.oven,
            "cooker" to R.drawable.cooker,
            "carpet" to R.drawable.carpet,
            "mirror" to R.drawable.mirror,
            "bed" to R.drawable.bed,
            "chair" to R.drawable.chair,
            "dining table" to R.drawable.diningtable,
            "lamp" to R.drawable.lamp,
            "sofa" to R.drawable.sofa,
            "television" to R.drawable.television,
            "table" to R.drawable.table,
            "bbq" to R.drawable.bbq,
            "fence" to R.drawable.fence,

            // --- Eylemler (Yeni Eklenenler) ---
            "eat" to R.drawable.eat,
            "drink" to R.drawable.drink,
            "sleep" to R.drawable.sleep,
            "read" to R.drawable.read,
            "write" to R.drawable.write,
            "run" to R.drawable.run,
            "walk" to R.drawable.walk,
            "watch" to R.drawable.watch,
            "listen" to R.drawable.listen,
            "jump" to R.drawable.jump,
            "sing" to R.drawable.sing,
            "speak" to R.drawable.speak,
            "open" to R.drawable.open,
            "play" to R.drawable.play,
            "sit" to R.drawable.sit,
            "stand" to R.drawable.stand,
            "look" to R.drawable.look,
            "dance" to R.drawable.dance,
            "go" to R.drawable.go,
            "brush" to R.drawable.brush,
            "come" to R.drawable.come,
            "wake up" to R.drawable.wakeup,
            "fork" to R.drawable.fork,
            "knife" to R.drawable.knife,
            "spoon" to R.drawable.spoon,
            "plate" to R.drawable.plate,
            "glass" to R.drawable.glass,
            "pan" to R.drawable.pan,
            "bowl" to R.drawable.bowl,
            "pot" to R.drawable.pot,
            "cup" to R.drawable.cup,
            "napkin" to R.drawable.napkin,
            "sink" to R.drawable.sink,
            "kitchen cabinet" to R.drawable.cupboard, // Opsiyonel: Mutfak dolabı için
            "kettle" to R.drawable.kettle,
            "toaster" to R.drawable.toaster,
            "tray" to R.drawable.tray,
            "grater" to R.drawable.grater,
            "peeler" to R.drawable.peeler,
            "grate" to R.drawable.grate,
            "carry" to R.drawable.carry,
            "pour" to R.drawable.pour,
            "peel" to R.drawable.peel,
            "take off" to R.drawable.takeoff,
            "black cat" to R.drawable.blackcat,
            "iron" to R.drawable.iron,
            "wear" to R.drawable.wear,
            "shoe" to R.drawable.shoe








                    res = R.drawable.kisi3,
                ansEnglish = "Nasılsın?",
                opts = listOf("Nasılsın?", "Merhaba", "Nerelisin")
            ),
            createListening(
                id = 3,
                qTurkish = "I am fine",
                res = R.drawable.kisi4,
                ansEnglish = "İyiyim",
                opts = listOf("Muhteşemim", "Kötüyüm", "İyiyim")
            ),
            createListening(
                id = 4,
                qTurkish = "Hi",
                res = R.drawable.kedi3,
                ansEnglish = "Selam", // Farklılık olsun diye Selam/Merhaba
                opts = listOf("Nasılsın", "Görüşürüz", "Selam")
            ),
            createListening(
                id = 5,
                qTurkish = "I am fine, thank you!",
                res = R.drawable.kisi1,
                ansEnglish = "İyiyim, teşekkürler",
                opts = listOf("İyiyim", "İyiyim, teşekkürler", "Merhaba")
            ),
            createListening(
                id = 6,
                qTurkish = "What is your name?",
                res = R.drawable.kisi2,
                ansEnglish = "Adın ne?",
                opts = listOf("Adın ne?", "Nerelisin?", "Nasılsın?")
            ),
            createListening(
                id = 7,
                qTurkish = "Name",
                res = R.drawable.kisi2,
                ansEnglish = "İsim",
                opts = listOf("İsim", "Soyisim", "Adres")
            ),
            createListening(
                id = 8,
                qTurkish = "What is your name?",
                res = R.drawable.kisi2,
                ansEnglish = "İsmin ne?",
                opts = listOf("İsmin ne?", "Nerelisin?", "Nasıl?")
            ),
            createListening(
                id = 9,
                qTurkish = "What?",
                res = R.drawable.kisi2,
                ansEnglish = "Ne?",
                opts = listOf("Ne?", "Neresi?", "Nasıl?")
            ),
            // Eşleştirme sorusunun yapısını düzelttik:
            createMatching(
                id = 10,
                questionText = "Soruları cevaplarla eşleştir", // qTurkish -> questionText
                image = R.drawable.cat_logo,                 // res -> image
                pairs = "How are you?,How?,What is your name?,What?,I am fine", // ansEnglish -> pairs
                turkishOptions = listOf("Nasılsın?", "Nasıl", "İsmin ne?", "Ne", "İyiyim") // opts -> turkishOptions
            )
        )
    }
    fun getQuizOne(): List<VocabularyLevelDataQuiz> {
        return listOf(
            createListening(
                id = 11,
                qTurkish = "Nice to meet you!",
                res = R.drawable.kisi3,
                ansEnglish = "Tanıştığımıza memnun oldum!",
                opts = listOf("Tanıştığımıza memnun oldum!", "Nasılsın?", "Görüşürüz")
            ),
            createListening(
                id = 12,
                qTurkish = "Where are you from?",
                res = R.drawable.kisi2,
                ansEnglish = "Nerelisin?",
                opts = listOf("Nerelisin?", "Adın ne?", "Kaç yaşındasın?")
            ),
            createListening(
                id = 13,
                qTurkish = "I am from Turkey.",
                res = R.drawable.kisi4,
                ansEnglish = "Türkiyeli'yim.",
                opts = listOf("Almanya'danım", "Türkiyeli'yim.", "Burada yaşıyorum")
            ),
            createListening(
                id = 14,
                qTurkish = "Good morning!",
                res = R.drawable.kedi3,
                ansEnglish = "Günaydın!",
                opts = listOf("İyi akşamlar!", "Günaydın!", "İyi geceler!")
            ),
            createListening(
                id = 15,
                qTurkish = "Have a nice day!",
                res = R.drawable.kisi1,
                ansEnglish = "İyi günler!",
                opts = listOf("İyi günler!", "Hoşça kal!", "Merhaba")
            ),
            createListening(
                id = 16,
                qTurkish = "See you later!",
                res = R.drawable.kisi3,
                ansEnglish = "Sonra görüşürüz!",
                opts = listOf("Merhaba", "Güle güle", "Sonra görüşürüz!")
            ),
            createListening(
                id = 17,
                qTurkish = "Are you a student?",
                res = R.drawable.kisi2,
                ansEnglish = "Öğrenci misin?",
                opts = listOf("Öğrenci misin?", "Öğretmen misin?", "Doktor musun?")
            ),
            createListening(
                id = 18,
                qTurkish = "Yes, I am.",
                res = R.drawable.kisi4,
                ansEnglish = "Evet, öyleyim.",
                opts = listOf("Hayır, değilim.", "Evet, öyleyim.", "Belki")
            ),
            createListening(
                id = 19,
                qTurkish = "And you?",
                res = R.drawable.kisi1,
                ansEnglish = "Ya sen?",
                opts = listOf("Neden?", "Nasıl?", "Ya sen?")
            ),
            // Eşleştirme sorusu ile kapanış
            createMatching(
                id = 20,
                questionText = "Kalıpları doğru karşılıklarıyla eşleştir",
                image = R.drawable.cat_logo,
                pairs = "Nice to meet you,Where are you from,Good morning,See you,Student",
                turkishOptions = listOf("Memnun oldum", "Nerelisin", "Günaydın", "Görüşürüz", "Öğrenci")
            )
        )
    }

    // --- QUIZ 2 (Burası Boş Kalmış, 1'i Kopyaladım Şimdilik) ---


    // --- QUIZ 3 ---
    fun getQuizThree(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. YENİ: Yapı Taşı
            createListening(
                id = 41,
                qTurkish = "Good",
                res = R.drawable.kisi1,
                ansEnglish = "İyi",
                opts = listOf("İyi", "Kötü", "Hoş")
            ),
            // 2. YENİ: Yapı Taşı
            createListening(
                id = 42,
                qTurkish = "Morning",
                res = R.drawable.kedi3,
                ansEnglish = "Sabah",
                opts = listOf("Sabah", "Akşam", "Gece")
            ),
            // 3. TEKRAR: Önceki Quizden
            createListening(
                id = 43,
                qTurkish = "How are you?",
                res = R.drawable.kisi3,
                ansEnglish = "Nasılsın?",
                opts = listOf("Nasılsın?", "Nerelisin?", "Adın ne?")
            ),
            // 4. YENİ: Yapı Taşı
            createListening(
                id = 44,
                qTurkish = "Night",
                res = R.drawable.kisi4,
                ansEnglish = "Gece",
                opts = listOf("Gün", "Gece", "Öğle")
            ),
            // 5. TEKRAR: Önceki Quizden
            createListening(
                id = 45,
                qTurkish = "I am fine",
                res = R.drawable.kisi1,
                ansEnglish = "İyiyim",
                opts = listOf("Yorgunum", "İyiyim", "Kötüyüm")
            ),
            // 6. YENİ: Yapı Taşı
            createListening(
                id = 46,
                qTurkish = "Afternoon",
                res = R.drawable.kisi2,
                ansEnglish = "Öğleden sonra",
                opts = listOf("Sabah", "Öğleden sonra", "Akşam")
            ),
            // 7. TEKRAR: Önceki Quizden
            createListening(
                id = 47,
                qTurkish = "What is your name?",
                res = R.drawable.kisi2,
                ansEnglish = "Adın ne?",
                opts = listOf("Adın ne?", "Kaç yaşındasın?", "Nerelisin?")
            ),
            // 8. YENİ: Yapı Taşı
            createListening(
                id = 48,
                qTurkish = "Thanks",
                res = R.drawable.kedi3,
                ansEnglish = "Teşekkürler",
                opts = listOf("Merhaba", "Lütfen", "Teşekkürler")
            ),
            // 9. TEKRAR: Önceki Quizden
            createListening(
                id = 49,
                qTurkish = "Where are you from?",
                res = R.drawable.kisi1,
                ansEnglish = "Nerelisin?",
                opts = listOf("Nasılsın?", "Nerelisin?", "Kimsin?")
            ),
            // 10. YENİ: Yapı Taşı
            createListening(
                id = 50,
                qTurkish = "My name is Ali",
                res = R.drawable.kisi3,
                ansEnglish = "Benim adım Ali",
                opts = listOf("Ali burada", "Benim adım Ali", "Nasılsın Ali?")
            ),
            // 11. TEKRAR: Önceki Quizden
            createListening(
                id = 51,
                qTurkish = "Nice to meet you",
                res = R.drawable.kedi4,
                ansEnglish = "Tanıştığımıza memnun oldum",
                opts = listOf("Tanıştığımıza memnun oldum", "Daha sonra görüşürüz", "Günaydın")
            ),
            // 12. KARIŞIK EŞLEŞTİRME (Kapanış)
            createMatching(
                id = 52,
                questionText = "Son bir kontrol: Eşleştir!",
                image = R.drawable.cat_logo,
                pairs = "Morning,Night,Thanks,Good,I am fine",
                turkishOptions = listOf("Sabah", "Gece", "Teşekkürler", "İyi", "İyiyim")
            )
        )
    }


    fun getQuizFour(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- BÖLÜM 1: ZAMAN DİLİMLERİ ---
            createListening(1, "evening", R.drawable.kedi1, "akşam", listOf("akşam", "gece", "sabah")),
            createListening(2, "Good evening!", R.drawable.kisi2, "İyi akşamlar!", listOf("İyi akşamlar!", "Günaydın!", "İyi geceler!")),
            createListening(3, "afternoon", R.drawable.kisi3, "Öğleden sonra", listOf("Öğleden sonra", "sabah", "akşam")),
            createListening(4, "Good afternoon!", R.drawable.kisi4, "Tünaydın!", listOf("Tünaydın!", "Günaydın!", "İyi akşamlar!")),
            createListening(5, "night", R.drawable.kedi3, "gece", listOf("gece", "gündüz", "akşam")),
            createListening(6, "Good night!", R.drawable.kedi4, "İyi geceler!", listOf("İyi geceler!", "İyi akşamlar!", "Güle güle")),

            // --- BÖLÜM 2: DURUM SORMA ---
            createListening(7, "How are you doing?", R.drawable.kisi1, "Nasıl gidiyor?", listOf("Nasıl gidiyor?", "Ne yapıyorsun?", "Neredesin?")),
            createListening(8, "I am great!", R.drawable.kisi2, "Harikayım!", listOf("Harikayım!", "Yorgunum", "Üzgünüm")),
            createListening(9, "Not bad.", R.drawable.kisi3, "Fena değil.", listOf("Fena değil.", "Kötü.", "Çok iyi.")),

            createMatching(10, "Zamanları ve durumları eşleştir", R.drawable.cat_logo, // Eşleştirmede logo kalabilir
                "evening,afternoon,great,not bad,night",
                listOf("Akşam", "Öğleden sonra", "Harika", "Fena değil", "Gece")),

            // --- BÖLÜM 3: KİŞİSEL BİLGİLER ---
            createListening(11, "Where are you from?", R.drawable.kisi4, "Nerelisin?", listOf("Nerelisin?", "Neredesin?", "Nereye gidiyorsun?")),
            createListening(12, "Turkey", R.drawable.kedi3, "Türkiye", listOf("Türkiye", "Almanya", "Fransa")),
            createListening(13, "I am from Turkey.", R.drawable.kedi4, "Türkiyeli'yim.", listOf("Türkiyeli'yim.", "Türkiye'deyim.", "Türkiye'yi seviyorum.")),
            createListening(14, "How old are you?", R.drawable.kisi1, "Kaç yaşındasın?", listOf("Kaç yaşındasın?", "Nasılsın?", "Adın ne?")),

            // --- BÖLÜM 4: NEZAKET KALIPLARI ---
            createListening(15, "Excuse me?", R.drawable.kedi2, "Affedersiniz", listOf("Affedersiniz", "Özür dilerim", "Rica ederim")),
            createListening(16, "I am sorry.", R.drawable.kedi3, "Özür dilerim", listOf("Özür dilerim", "Affedersiniz", "Sorun değil")),
            createListening(17, "You are welcome.", R.drawable.kisi4, "Rica ederim", listOf("Rica ederim", "Hoş geldiniz", "Hoşça kalın")),
            createListening(18, "See you tomorrow!", R.drawable.kedi3, "Yarın görüşürüz!", listOf("Yarın görüşürüz!", "Sonra görüşürüz!", "Dün görüştük!")),
            createListening(19, "Tomorrow", R.drawable.kedi4, "Yarın", listOf("Dün", "Bugün", "Yarın")),

            // --- FİNAL EŞLEŞTİRME ---
            createMatching(20, "Hepsini doğru eşleştir", R.drawable.cat_logo,
                "excuse me,sorry,welcome,tomorrow,Turkey",
                listOf("Affedersiniz", "Özür dilerim", "Rica ederim", "Yarın", "Türkiye"))
        )
    }
    fun getQuizFive(): List<VocabularyLevelDataQuiz> {
        return listOf(
            createListening(1, "Hello!", R.drawable.kedi1, "Merhaba", listOf("Güle Güle", "Merhaba", "Teşekkürler")),
            createListening(2, "How are you?", R.drawable.kedi2, "Nasılsın?", listOf("Adım Ali.", "Adın ne?", "Nasılsın?")),
            createListening(3, "I am fine, thanks.", R.drawable.kisi2, "İyiyim, teşekkürler.", listOf("İyiyim, teşekkürler.", "Tanıştığımıza memnun oldum.", "Merhaba.")),
            createListening(4, "What is your name?", R.drawable.kisi3, "Adın ne?", listOf("İyiyim.", "Adın ne?", "Nasılsın?")),
            createListening(5, "Nice to meet you.", R.drawable.kedi4, "Tanıştığımıza memnun oldum.", listOf("Öğrenciyim.", "Görüşürüz.", "Tanıştığımıza memnun oldum.")),
            createMatching(6, "İngilizce ifadeleri Türkçeleriyle eşleştir", R.drawable.kedi3, "hello,where are you from,how are you,what is your name,meet", listOf("Merhaba","Nerelisin", "Nasılsın?", "Adın ne?", "Tanışmak")),
            createListening(7, "Where are you from?", R.drawable.kisi2, "Nerelisin?", listOf("Yirmi yaşındayım.", "Nerelisin?", "Öğretmenim.")),
            createListening(8, "Are you a student?", R.drawable.kisi4, "Öğrenci misin?", listOf("Hayır, teşekkürler.", "İyiyim.", "Öğrenci misin?")),
            createListening(9, "How old are you?", R.drawable.kisi1, "Kaç yaşındasın?", listOf("Kaç yaşındasın?", "Buralıyım.", "Mutluyum.")),
            createListening(10, "Thank you!", R.drawable.kisi1, "Teşekkür ederim!", listOf("Affedersiniz.", "Teşekkür ederim!", "Üzgünüm.")),
            createListening(11, "I am sorry.", R.drawable.kedi1, "Üzgünüm.", listOf("Teşekkürler.", "Hoş geldiniz.", "Üzgünüm.")),
            createMatching(12, "İfadelerin Türkçe karşılıklarını eşleştir", R.drawable.kedi3, "thank you,i am sorry,where are you from,how old are you", listOf("Teşekkür ederim", "Üzgünüm", "Nerelisin?", "Kaç yaşındasın?")),
            createListening(13, "See you later!", R.drawable.kedi1, "Sonra görüşürüz!", listOf("Merhaba!", "Sonra görüşürüz!", "Lütfen!")),
            createListening(14, "Have a nice day!", R.drawable.kisi4, "İyi günler!", listOf("İyi günler!", "Günaydın!", "Selam!")),
            createMatching(15, "Vedalaşma ifadelerini eşleştir", R.drawable.kedi4, "goodbye,have a nice day,see you,take care", listOf("Hoşça kal", "İyi günler", "Görüşürüz", "Kendine iyi bak"))
        )
    }
    fun getQuizSix(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. Durum sorgulama korelasyonu (Fine - Bad - Tired)
            createListening(1, "How are you?", R.drawable.kedi1, "I am fine, thanks.", listOf("I feel bad.", "I am fine, thanks.", "I am a bit tired.")),

            // 2. İsim sorma korelasyonu (Ali - Ayşe - Arji)
            createListening(2, "What is your name?", R.drawable.kisi1, "My name is Ali.", listOf("Her name is Ayşe.", "My name is Ali.", "Your name is Arjin.")),

            // 3. Tanışma memnuniyeti korelasyonu (Nice to meet you - Pleased - Me too)
            createListening(3, "Nice to meet you.", R.drawable.kedi2, "Nice to meet you, too.", listOf("Nice to meet you, too.", "Goodbye.", "How are you?")),

            // 4. Memleket korelasyonu (Turkey - England - Germany)
            createListening(4, "Where are you from?", R.drawable.kisi2, "I am from Turkey.", listOf("I am from Germany.", "I am from England.", "I am from Turkey.")),

            // 5. Meslek korelasyonu (Student - Teacher - Doctor)
            createListening(5, "are you a student?", R.drawable.kisi3, "I am a student.", listOf("He is a doctor.", "I am a student.", "You are a teacher.")),

            // 6. Eşleştirme
            createMatching(6, "Kelimeleri eşleştir", R.drawable.kedi3, "how are you,name,from,meet", listOf("Fine, thanks", "Ali", "Turkey", "Nice to meet you")),

            // 7. Yaş korelasyonu (Numerical relations)
            createListening(7, "How old are you?", R.drawable.kisi4, "I am twenty years old.", listOf("I am eighteen.", "I am twenty years old.", "I am thirty.")),

            // 8. Selamlaşma zamanı korelasyonu (Morning - Evening - Night)
            createListening(8, "Good morning!", R.drawable.kedi4, "Good morning!", listOf("Good evening!", "Good morning!", "Good night!")),

            // 9. Rica/Minnet korelasyonu (Welcome - No problem - My pleasure)
            createListening(9, "Thank you very much!", R.drawable.kisi1, "You are welcome.", listOf("You are welcome.", "Not at all.", "No problem.")),

            // 10. Vedalaşma korelasyonu (See you - Later - Soon)
            createListening(10, "What is your name?", R.drawable.kedi1, "My name is Furkan", listOf("See you later!", "My name is Furkan", "Nice to see you!")),

            // 11. Karşılama korelasyonu (Welcome - Thanks)
            createListening(11, "Welcome to our class!", R.drawable.kisi2, "Thank you!", listOf("Thank you!", "Goodbye.", "How are you?")),

            // 12. Eşleştirme
            createMatching(12, "Kelimeleri eşleştir", R.drawable.kedi3, "morning,night,later,care", listOf("Good morning", "Good night", "See you later", "Take care")),

            // 13. Okul/Eğitim seviyesi korelasyonu (University - High School - Primary)


            // 14. Dil yeteneği korelasyonu (Speak - Understand - Learn)
            createListening(13, "Where are you from?", R.drawable.kedi2, "I am from Turkey.", listOf("No, I don't.", "I am from Turkey", "I study Turkish.")),

            // 15. Eşleştirme
            createMatching(14, "Kelimeleri eşleştir", R.drawable.kedi4, "hi,bye,thanks,sorry", listOf("Hello", "Goodbye", "You are welcome", "It is okay"))
        )
    }

    fun getQuizSeven(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. I (Ben) - Meslek Korelasyonu
            createFillInBlank(1, "Ben bir öğrenciyim", R.drawable.kisi1, "I am a student",
                listOf("I", "am", "a", "student")),

            // 2. You (Sen) - Aile/Rol Korelasyonu
            createListening(2, "You are my brother.", R.drawable.brother, "Sen benim erkek kardeşimsin",
                listOf("Sen benim erkek kardeşimsin", "Sen benim babamsın", "Sen benim arkadaşımsın.")),

            // 3. He (O - Erkek) - Meslek Korelasyonu
            createListening(3, "He is a doctor.", R.drawable.doctor, "O bir doktor",
                listOf("O bir doktor.", "Sen bir doktorsun", "Biz doktoruz")),

            // 4. She (O - Kadın) - Aile/Rol Korelasyonu
            createListening(4, "She is a mother.", R.drawable.mother, "O bir anne",
                listOf("O bir anne", "O bir baba", "O bir öğretmen")),

            // 5. It (O - Cansız/Hayvan) - Durum Korelasyonu
            createListening(5, "It is a cat.", R.drawable.cat, "Bu bir kedidir.",
                listOf("bu bir kedidir.", "Bu bir köpektir", "Bu bir kuştur")),

            // 6. Eşleştirme - Temel Zamirler ve Yardımcı Fiiller
            createMatching(6, "Cümleleri uygun şekilde tamamlayınız.", R.drawable.kedi3,
                "I ,You ,He",
                listOf("am a teacher", "are my friend", "is my father")),

            // 7. We (Biz) - Meslek Korelasyonu (Çoğul)
            createListening(7, "We are teachers.", R.drawable.kedi3, "Biz öğretmeniz",
                listOf("Onlar öğretmenler", "Biz öğretmeniz", "Siz öğretmensiniz")),

            // 8. They (Onlar) - Aile Korelasyonu (Çoğul)
            createListening(8, "They are my family.", R.drawable.family, "Onlar benim ailem.",
                listOf("Siz benim ailemsiniz", "Onlar benim ailem.", "Biz bir aileyiz")),

            // 9. Karma - Meslek (Doctor/Nurse/Patient)
            createListening(9, "I am a nurse.", R.drawable.nurse, "Ben bir hemşireyim",
                listOf("Ben bir hemşireyim", "Sen bir hemşiresin", "O bir hemşire")),

            // 10. Karma - Aile (Father/Son/Daughter)
            createListening(10, "He is a father.", R.drawable.dad, "O benim babam",
                listOf("O benim arkadaşım", "O benim annem", "O benim babam")),

            // 11. Karışık Zamir Testi
            createListening(11, "You are a teacher.", R.drawable.kedi2, "Sen bir öğretmensin",
                listOf("Sen bir öğretmensin", "Ben bir öğretmenim", "Onlar öğretmen")),

            // 12. Eşleştirme - Meslekler ve Roller
            createMatching(12, "Kelimeleri eşleştir", R.drawable.kedi3,
                "student,doctor,teacher,mother",
                listOf("student", "doctor", "teacher", "mother")),

            // 13. Çoğul Nesne (It/They Ayrımı)
            createListening(13, "They are books.", R.drawable.shelf, "Bunlar benim kitaplarım",
                listOf("Bu kahverengi", "Bu bir kitaplık", "Bunlar benim kitaplarım")),

            // 14. Karma - Arkadaşlık
            createListening(14, "I am your friend", R.drawable.kisi4, "Ben senin arkadaşınım",
                listOf("Ben senin arkadaşınım", "Siz benim arkadaşlarımsınız", "Biz arkadaşız")),

            // 15. Eşleştirme - Çoğul Zamirler
            createMatching(15, "Kelimeleri eşleştir", R.drawable.kedi4,
                "we,they,it,you",
                listOf("Biz", "Onlar", "O", "Sen"))
        )
    }
    fun getQuizEight(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. Hatırlatma - Selamlaşma
            createListening(1, "Good morning", R.drawable.kisi1, "Günaydın",
                listOf("Günaydın", "Tünaydın", "İyi akşamlar")),

            // 2. Yeni Kelime - Mekan
            createListening(2, "School", R.drawable.kisi2, "Okul",
                listOf("Okul", "Hastane", "Ev")),

            // 3. Pratik - Mekan Cümlesi
            createListening(3, "I am at school.", R.drawable.kisi2, "Ben okuldayım",
                listOf("Ben okuldayım", "O okulda", "Sen okuldasın")),

            // 4. Hatırlatma - Selamlaşma
            createListening(4, "Good afternoon", R.drawable.kisi3, "Tünaydın",
                listOf("Tünaydın", "Günaydın", "İyi geceler")),

            // 5. Yeni Kelime - Mekan
            createListening(5, "Hospital", R.drawable.doctor, "Hastane",
                listOf("Hastane", "Okul", "Ofis")),

            // 6. Pratik - Mekan Cümlesi
            createListening(6, "He is at the hospital.", R.drawable.doctor, "O hastanede",
                listOf("O hastanede", "Ben hastanedeyim", "Onlar hastanede")),

            // 7. Yeni Kelime - Durum/Sıfat
            createListening(7, "Happy", R.drawable.kedi3, "Mutlu",
                listOf("Mutlu", "Üzgün", "Yorgun")),

            // 8. Pratik - Durum Cümlesi
            createListening(8, "She is happy.", R.drawable.kedi4, "O mutlu",
                listOf("O mutlu", "Ben mutluyum", "O yorgun")),

            // 9. Yeni Kelime - Mekan
            createListening(9, "Home", R.drawable.kedi1, "Ev",
                listOf("Ev", "Bahçe", "Sınıf")),

            // 10. Pratik - Mekan Cümlesi
            createListening(10, "We are at home.", R.drawable.family, "Biz evdeyiz",
                listOf("Biz evdeyiz", "Siz evdesiniz", "Onlar evde")),

            // 11. Harman - Selam + Cümle
            createListening(11, "Hello, I am at home.", R.drawable.kisi1, "Merhaba, ben evdeyim",
                listOf("Merhaba, ben evdeyim", "Selam, o evde", "Merhaba, biz evdeyiz")),

            // 12. Harman - Selam + Zamir + Mekan
            createListening(12, "Good afternoon. She is at the hospital.", R.drawable.nurse, "Tünaydın. O hastanede.",
                listOf("Tünaydın. O hastanede.", "Günaydın. O okulda.", "Tünaydın. Ben hastanedeyim.")),

            // 13. Harman - Durum + Mekan
            createListening(13, "I am at school and I am happy.", R.drawable.kisi2, "Okuldayım ve mutluyum",
                listOf("Okuldayım ve mutluyum", "Okuldayım ve yorgunum", "Evdeyim ve mutluyum")),

            // 14. Karma - En Uzun Harman
            createListening(14, "Good morning. He is at home..", R.drawable.kisi3, "Günaydın. O evde.",
                listOf("Günaydın. O evde", "Merhaba. Ben evdeyim", "Günaydın. Onlar evde.")),

            // 15. FİNAL - 6'LI DEV EŞLEŞTİRME (Kelimeler, Kalıplar ve Cümleler)
            createMatching(15, "Kelimeleri ve cümleleri Türkçe karşılıklarıyla eşleştiriniz.", R.drawable.kedi3,
                "Hospital,School,Good morning,I am at home,She is happy,Tired",
                listOf("Hastane", "Okul", "Günaydın", "Ben evdeyim", "O mutlu", "Yorgun"))
        )
    }
    fun getQuizNine(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- 1. SET: TEACHER ---
            // 1. Visual (Soru Türkçe, Cevap İngilizce, 4. şık 'teacher' sabit)
            createVisual(1, "'Öğretmen' hangisidir?", R.drawable.kedi4, "teacher",
                listOf("student", "partner", "friend", "teacher")),

            // 2. Listening (Soru İngilizce SES/METİN, Şıklar TÜRKÇE)
            createListening(2, "My teacher is ready.", R.drawable.kedi4, "Benim öğretmenim hazır.",
                listOf("Benim öğretmenim hazır.", "Ben hazırım.", "Öğretmenim mutlu.")),

            // --- 2. SET: STUDENT ---
            // 3. Visual
            createVisual(3, "'Öğrenci' hangisidir?", R.drawable.student, "student",
                listOf("teacher", "partner", "friend", "student")),

            // 4. Listening
            createListening(4, "My student is at school.", R.drawable.student, "Benim öğrencim okulda.",
                listOf("Benim öğrencim okulda.", "Öğretmenim okulda.", "Öğrencim evde.")),

            // --- 3. SET: PARTNER ---
            // 5. Visual
            createVisual(5, "'Eş / Partner' hangisidir?", R.drawable.partner, "partner",
                listOf("friend", "student", "family", "partner")),

            // 6. Listening
            createListening(6, "My partner is tired.", R.drawable.partner, "Benim partnerim yorgun.",
                listOf("Benim partnerim yorgun.", "Partnerim uykulu.", "Ben yorgunum.")),

            // --- 4. SET: FRIEND ---
            // 7. Visual
            createVisual(7, "'Arkadaş' hangisidir?", R.drawable.friend, "friend",
                listOf("partner", "student", "nurse", "friend")),

            // 8. Listening
            createListening(8, "My friend is happy.", R.drawable.friend, "Benim arkadaşım mutlu.",
                listOf("Benim arkadaşım mutlu.", "Ben mutluyum.", "Arkadaşım üzgün.")),

            // --- 5. EKSTRA LISTENING (3 ADET) ---
            // 9. Listening (Açlık - kedi4)
            createListening(9, "My cat is hungry.", R.drawable.kedi4, "Benim kedim aç.",
                listOf("Benim kedim aç.", "Kedim susamış.", "Ben açım.")),

            // 10. Listening (Susuzluk - kedi2)
            createListening(10, "I am thirsty.", R.drawable.kedi4, "Susadım.",
                listOf("Susadım.", "Acıktım.", "Yorgunum.")),

            // 11. Listening (Mekan - hospital)
            createListening(11, "My partner is at the hospital.", R.drawable.nurse, "Partnerim hastanede.",
                listOf("Partnerim hastanede.", "Öğretmenim okulda.", "Partnerim evde.")),

            // --- 6. FINAL MATCHING ---
            // 12. Matching (Türkçe - İngilizce Eşleştirme)
            createMatching(12, "Türkçe kelimeleri İngilizce karşılıklarıyla eşleştiriniz.", R.drawable.kedi3,
                "Öğretmen,Arkadaş,Aç,Benim partnerim,Hazır,Susadım",
                listOf("Teacher", "Friend", "Hungry", "My partner", "Ready", "I am thirsty"))
        )
    }
    fun getQuizTen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. SORU (Soru) - Doğru: 2. Şık
            createListening(1, "Are you terrified?", R.drawable.cat_logo, "Sen çok mu korktun?",
                listOf("O çok mu korktu?", "Sen çok mu korktun?", "Biz çok mu korktuk?")),

            // 2. SORU (Olumlu) - Doğru: 1. Şık
            createListening(2, "He is a great scientist.", R.drawable.cat_logo, "O harika bir bilim insanıdır.",
                listOf("O harika bir bilim insanıdır.", "Ben harika bir bilim insanıyım.", "Onlar harika bilim insanlarıdır.")),

            // 3. SORU (Olumsuz Soru) - Doğru: 3. Şık
            createListening(3, "Aren't they energetic?", R.drawable.cat_logo, "Onlar enerjik değil mi?",
                listOf("Biz enerjik değil miyiz?", "Sen enerjik değil misin?", "Onlar enerjik değil mi?")),

            // 4. SORU (Olumsuz) - Doğru: 2. Şık
            createListening(4, "I am not jealous.", R.drawable.cat_logo, "Ben kıskanç değilim.",
                listOf("O kıskanç değil.", "Ben kıskanç değilim.", "Sen kıskanç değilsin.")),

            // 5. SORU (Soru) - Doğru: 3. Şık
            createListening(5, "Is the box heavy?", R.drawable.cat_logo, "Kutu ağır mı?",
                listOf("Kutu hafif mi?", "Kutu yeni mi?", "Kutu ağır mı?")),

            // 6. SORU (Olumlu) - Doğru: 1. Şık
            createListening(6, "The elephant is huge.", R.drawable.cat_logo, "Fil kocamandır.",
                listOf("Fil kocamandır.", "Kedi kocamandır.", "Fil küçücüktür.")),

            // 7. SORU (Olumsuz Soru) - Doğru: 2. Şık
            createListening(7, "Am I not right?", R.drawable.cat_logo, "Haklı değil miyim?",
                listOf("Sen haklı değil misin?", "Haklı değil miyim?", "Haklı mıyım?")),

            // 8. SORU (Olumsuz) - Doğru: 1. Şık
            createListening(8, "We are not clumsy.", R.drawable.cat_logo, "Biz sakar değiliz.",
                listOf("Biz sakar değiliz.", "Onlar sakar değil.", "Sen sakar değilsin.")),

            // 9. SORU (Soru) - Doğru: 3. Şık
            createListening(9, "Is he a hero?", R.drawable.cat_logo, "O bir kahraman mı?",
                listOf("O bir düşman mı?", "Ben bir kahraman mıyım?", "O bir kahraman mı?")),

            // 10. SORU (Olumlu) - Doğru: 2. Şık
            createListening(10, "She is a manager.", R.drawable.cat_logo, "O bir müdürdür.",
                listOf("Ben bir müdürüm.", "O bir müdürdür.", "Biz müdürüz.")),

            // 11. SORU (Olumsuz Soru) - Doğru: 1. Şık
            createListening(11, "Is he not your enemy?", R.drawable.cat_logo, "O senin düşmanın değil mi?",
                listOf("O senin düşmanın değil mi?", "O senin dostun değil mi?", "Düşmanım değil miyim?")),

            // 12. SORU (Olumsuz) - Doğru: 3. Şık
            createListening(12, "It is not dangerous.", R.drawable.cat_logo, "O tehlikeli değil.",
                listOf("O tehlikeli mi?", "Biz tehlikeli değiliz.", "O tehlikeli değil.")),

            // 13. SORU (Soru) - Doğru: 2. Şık
            createListening(13, "Are they stubborn?", R.drawable.cat_logo, "Onlar inatçı mı?",
                listOf("Biz inatçı mıyız?", "Onlar inatçı mı?", "O inatçı mı?")),

            // 14. SORU (Olumlu) - Doğru: 1. Şık
            createListening(14, "I am a calm person.", R.drawable.cat_logo, "Ben sakin biriyim.",
                listOf("Ben sakin biriyim.", "Sen sakin birisin.", "O sakin biridir.")),

            // 15. SORU (Olumsuz Soru) - Doğru: 3. Şık
            createListening(15, "Are you not a writer?", R.drawable.cat_logo, "Sen bir yazar değil misin?",
                listOf("O bir yazar değil mi?", "Ben bir yazar değil miyim?", "Sen bir yazar değil misin?")),

            // 16. SORU (Olumsuz) - Doğru: 1. Şık
            createListening(16, "They are not selfish.", R.drawable.cat_logo, "Onlar bencil değil.",
                listOf("Onlar bencil değil.", "Biz bencil değiliz.", "O bencil değil.")),

            // 17. SORU (Soru) - Doğru: 2. Şık
            createListening(17, "Is it an ancient city?", R.drawable.cat_logo, "O antik bir şehir mi?",
                listOf("O yeni bir şehir mi?", "O antik bir şehir mi?", "O gürültülü bir şehir mi?")),

            // 18. SORU (Olumlu) - Doğru: 3. Şık
            createListening(18, "We are lucky.", R.drawable.cat_logo, "Biz şanslıyız.",
                listOf("Onlar şanslılar.", "Ben şanslıyım.", "Biz şanslıyız.")),

            // 19. SORU (Olumsuz Soru) - Doğru: 3. Şık
            createListening(19, "Aren't you a waiter?", R.drawable.cat_logo, "Garson değil misin?",
                listOf("Garson musun?", "O garson değil mi?", "Garson değil misin?")),

            // 20. SORU (Karma Eşleştirme)
            createMatching(20, "Son tekrar!", R.drawable.cat_logo,
                "Hero,Enemy,Ancient,Huge",
                listOf("Kahraman", "Düşman", "Antik", "Kocaman"))
        )
    }
    fun getQuizEleven(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. SORU: Görsel (Father/Dad)
            createVisual(
                id = 1,
                questionTurkish = "Baba",
                imageRes = R.drawable.dad,
                correctAnswer = "father",
                options = listOf("father", "mother", "sister", "brother"),
                size = 180 // 🚨 Burayı 180 yaparak resimleri manuel olarak büyüttük
            ),

            // 2. SORU: Görsel (Mother/Mom)
            createVisual(2, "'Anne' hangisi?", R.drawable.mother, "mother", listOf("brother", "mother", "father", "baby")),

            // 3. SORU: Dinleme (Kalıp Cümle)
            createListening(3, "This is my brother.", R.drawable.brother, "Bu benim erkek kardeşim.",
                listOf("Bu benim babam.", "Bu benim erkek kardeşim.", "Bu benim arkadaşım.")),

            // 4. SORU: Görsel (Sister)
            createVisual(4, "'Kız Kardeş' hangisi?", R.drawable.sister, "sister", listOf("sister", "brother", "mother", "father")),

            // 5. SORU: Dinleme (Soru Kalıbı)
            createListening(5, "Is she your mother?", R.drawable.mother, "O senin annen mi?",
                listOf("O senin kız kardeşin mi?", "O senin arkadaşın mı?", "O senin annen mi?")),

            // 6. SORU: Görsel (Baby)
            createVisual(6, "'Bebek' hangisi?", R.drawable.baby, "baby", listOf("father", "sister", "baby", "brother")),

            // 7. SORU: Dinleme (Olumlu Cümle)
            createListening(7, "He is a great father.", R.drawable.dad, "O harika bir babadır.",
                listOf("O harika bir babadır.", "O harika bir bilim insanıdır.", "O benim erkek kardeşim.")),

            // 8. SORU: Eşleştirme (Kelime Bilgisi)
            createMatching(8, "Aile üyelerini eşleştir", R.drawable.cat_logo, "father,mother,sister,brother,baby",
                listOf("Baba", "Anne", "Kız Kardeş", "Erkek Kardeş", "Bebek")),

            // 9. SORU: Dinleme (Zamir Kontrolü)
            createListening(9, "They are my family.", R.drawable.family, "Onlar benim ailem.",

                listOf("Biz bir aileyiz.", "Onlar benim ailem.", "Sen benim ailemsin.")),

            // 10. SORU: Görsel (Aile Fotoğrafı)
            createVisual(10, "'Aile' hangisi?", R.drawable.family, "family", listOf("family", "brother", "sister", "father")),

            // 11. SORU: Dinleme (Olumsuz Cümle)
            createListening(11, "She is not my sister.", R.drawable.sister, "O benim kız kardeşim değil.",
                listOf("O benim annem değil.", "O benim kız kardeşim değil.", "O benim arkadaşım değil.")),

            // 12. SORU: Görsel (Grandmother)
            createVisual(12, "'Büyükanne/Nene' hangisi?", R.drawable.grandmother, "grandmother", listOf("grandmother", "mother", "sister", "baby")),

            // 13. SORU: Görsel (Grandfather)
            createVisual(13, "'Büyükbaba/Dede' hangisi?", R.drawable.grandfather, "grandfather", listOf("father", "brother", "grandfather", "baby")),

            // 14. SORU: Dinleme (Tanıştırma)
            createListening(14, "Meet my family.", R.drawable.family, "Ailemle tanış.",
                listOf("Arkadaşımla tanış.", "Ailemle tanış.", "Öğretmenimle tanış.")),

            // 15. SORU: Eşleştirme (Büyükler)
            createMatching(
                15,
                "Büyükleri eşleştir",
                R.drawable.kisi1,
                "grandfather,grandmother,family,sister,brother,mother", // Tüm İngilizce kelimeleri tek bir String içinde topladık
                listOf("Büyükbaba", "Büyükanne", "Aile", "kız kardeş", "erkek kardeş", "anne") // Türkçe karşılıklar listesi
            )
        )
    }
    fun getQuizTwelve(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. SORU: Görsel (Brother - Kucağında bebek olan kel adam)
            createVisual(1, "'Erkek Kardeş' hangisi?", R.drawable.brother, "brother",
                listOf("brother", "father", "grandfather", "baby")),

            // 2. SORU: Görsel (Sister - Pembe elbiseli, sırt çantalı kız)
            createVisual(2, "'Kız Kardeş' hangisi?", R.drawable.sister, "sister",
                listOf("mother", "sister", "grandmother", "baby")),

            // 3. SORU: Dinleme (Kız Kardeş ve Çanta detayı)
            createListening(3, "This is my sister. She is a student.", R.drawable.sister, "Bu benim kız kardeşim. O bir öğrencidir.",
                listOf("Bu benim annem. O bir öğretmendir.", "Bu benim kız kardeşim. O bir öğrencidir.", "Bu benim arkadaşım. O bir doktordur.")),

            // 4. SORU: Görsel (Grandmother - Boynu eğik, tatlı yaşlı teyze)
            createVisual(4, "'Anneanne/Babaanne' hangisi?", R.drawable.grandmother, "grandmother",
                listOf("mother", "sister", "grandmother", "baby")),

            // 5. SORU: Dinleme (Büyükanne)
            createListening(5, "She is my grandmother.", R.drawable.grandmother, "O benim anneannem.",
                listOf("O benim annem.", "O benim anneannem.", "O benim kız kardeşim.")),

            // 6. SORU: Görsel (Grandfather - Gözlüklü, hırkalı dede)
            createVisual(6, "'Dede/Büyükbaba' hangisi?", R.drawable.grandfather, "grandfather",
                listOf("father", "brother", "grandfather", "baby")),

            // 7. SORU: Dinleme (Dede)
            createListening(7, "Is he your grandfather?", R.drawable.grandfather, "O senin deden mi?",
                listOf("O senin baban mı?", "O senin erkek kardeşin mi?", "O senin deden mi?")),

            // 8. SORU: Görsel (Baby - Beşikteki bebek)
            createVisual(8, "'Bebek' hangisi?", R.drawable.baby, "baby",
                listOf("baby", "brother", "sister", "father")),

            // 9. SORU: Dinleme (Bebek)
            createListening(9, "The baby is in the bed.", R.drawable.baby, "Bebek yataktadır.",
                listOf("Bebek bahçededir.", "Bebek yataktadır.", "Bebek arabadadır.")),

            // 10. SORU: Görsel (Family Photo - Çerçeveli aile fotoğrafı)
            createVisual(10, "'Aile' hangisi?", R.drawable.family, "family",
                listOf("family", "sister", "brother", "family")),

            // 11. SORU: Dinleme (Geniş Aile)
            createListening(11, "I love my family.", R.drawable.family, "Ailemi seviyorum.",
                listOf("Ailemi seviyorum.", "Arkadaşlarımı seviyorum.", "Kedimi seviyorum.")),

            // 12. SORU: Görsel (Mother - Pembe kıyafetli anne)
            createVisual(12, "'Anne' hangisi?", R.drawable.mother, "mother",
                listOf("mother", "father", "sister", "brother")),

            // 13. SORU: Dinleme (Anne)
            createListening(13, "My mother is very kind.", R.drawable.mother, "Annem çok naziktir.",
                listOf("Annem çok naziktir.", "Annem çok sinirlidir.", "Annem çok yorgundur.")),

            // 14. SORU: Eşleştirme (Tüm Aile)
            createMatching(14, "Kelimeleri eşleştir", R.drawable.cat_logo, "grandfather,grandmother,brother,sister,baby",
                listOf("Dede", "Anneanne", "Erkek Kardeş", "Kız Kardeş", "Bebek")),

            // 15. SORU: Dinleme (Final Sorusu)
            createListening(15, "This is a big family.", R.drawable.family, "Bu büyük bir ailedir.",
                listOf("Bu küçük bir ailedir.", "Bu büyük bir ailedir.", "Bu yeni bir ailedir."))
        )
    }
    fun getQuizThirteen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. SORU: Görsel (Father)
            createVisual(1, "'Baba' hangisi?", R.drawable.dad, "father", listOf("father", "mother", "sister", "brother")),

            // 2. SORU: Dinleme (Mother)
            createListening(2, "This is my mother.", R.drawable.mother, "Bu benim annem.",
                listOf("Bu benim annem.", "Bu benim kız kardeşim.", "Bu benim anneannem.")),

            // 3. SORU: Görsel (Brother - Bebek tutan)
            createVisual(3, "'Erkek Kardeş' hangisi?", R.drawable.brother, "brother", listOf("brother", "father", "grandfather", "baby")),

            // 4. SORU: Dinleme (Sister - Çantalı)
            createListening(4, "My sister is a student.", R.drawable.sister, "Kız kardeşim bir öğrencidir.",
                listOf("Kız kardeşim bir öğrencidir.", "Kız kardeşim bir öğretmendir.", "Kız kardeşim bir doktordur.")),

            // 5. SORU: Görsel (Grandmother)
            createVisual(5, "'Anneanne' hangisi?", R.drawable.grandmother, "grandmother", listOf("grandmother", "mother", "sister", "baby")),

            // 6. SORU: Dinleme (Grandfather)
            createListening(6, "He is my grandfather.", R.drawable.grandfather, "O benim dedemdir.",
                listOf("O benim babamdır.", "O benim dedemdir.", "O benim erkek kardeşimdir.")),

            // 7. SORU: Görsel (Baby - Beşikteki)
            createVisual(7, "'Bebek' hangisi?", R.drawable.baby, "baby", listOf("baby", "brother", "sister", "father")),

            // 8. SORU: Dinleme (Family)
            createListening(8, "Meet my family.", R.drawable.family, "Ailemle tanış.",
                listOf("Arkadaşımla tanış.", "Ailemle tanış.", "Öğretmenimle tanış.")),

            // 9. SORU: Görsel (Mother - Pembe Elbiseli)
            createVisual(9, "Anne hangisi?", R.drawable.mother, "mother", listOf("mother", "sister", "grandmother", "baby")),

            // 10. SORU: Dinleme (Brother)
            createListening(10, "Is he your brother?", R.drawable.brother, "O senin erkek kardeşin mi?",
                listOf("O senin baban mı?", "O senin erkek kardeşin mi?", "O senin deden mi?")),

            // 11. SORU: Görsel (Grandfather - Gözlüklü)
            createVisual(11, "Dede hangisi?", R.drawable.grandfather, "grandfather", listOf("grandfather", "father", "brother", "baby")),

            // 12. SORU: Dinleme (Grandmother - Nazik)
            createListening(12, "My grandmother is kind.", R.drawable.grandmother, "Anneannem naziktir.",
                listOf("Anneannem yaşlıdır.", "Anneannem naziktir.", "Anneannem mutludur.")),

            // 13. SORU: DEV EŞLEŞTİRME (Sıfatlar ve Fiiller SADECE BURADA)
            // Bütün sıfat ve fiil yükünü tek soruda topluyoruz
            createMatching(
                id = 13,
                questionText = "Sıfatları ve Fiilleri Eşleştir",
                image = R.drawable.kisi2,
                pairs = "old,young,happy,love,meet,help,sleep,hold,big,kind",
                turkishOptions = listOf("Yaşlı", "Genç", "Mutlu", "Sevmek", "Tanışmak", "Yardım Etmek", "Uyumak")
            )
        )
    }

    fun getQuizFourteen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. QUESTION: kisi1 (Brother) - Student
            createVisual(1, "Who is a good student?", R.drawable.kisi1, "My brother is a good student.",
                listOf("My brother is a good student.", "My brother is small.", "My brother is old.")),

            // 2. QUESTION: Listening (kisi1) - Drive
            createListening(2, "Listen and choose: How does he drive?", R.drawable.kedi4, "My brother drives well.",
                listOf("My brother drives well.", "My brother runs fast.", "My brother cooks food.")),

            // 3. QUESTION: kisi2 (Mother) - Travel
            createVisual(3, "Does she like to travel?", R.drawable.kisi2, "My mother likes to travel.",
                listOf("My mother likes to travel.", "My mother drives well.", "My mother runs fast.")),

            // 4. QUESTION: Listening (kisi2) - Delicious
            createListening(4, "Listen and choose: Is the food delicious?", R.drawable.kisi2, "My mother cooks delicious food.",
                listOf("My mother cooks delicious food.", "My mother speaks fast.", "My mother walks slowly.")),

            // 5. QUESTION: kisi3 (Sister) - Fast
            createVisual(5, "Does she run fast?", R.drawable.kisi3, "My sister runs very fast.",
                listOf("My sister runs very fast.", "My sister cooks well.", "My sister is very old.")),

            // 6. QUESTION: Listening (kisi3) - Fast
            createListening(6, "Listen and choose: How does she run?", R.drawable.kisi3, "My sister runs very fast.",
                listOf("My sister runs very fast.", "My sister sleeps quietly.", "My sister sings well.")),

            // 7. QUESTION: kisi4 (Grandfather) - Slowly
            createVisual(7, "Does he speak slowly?", R.drawable.kisi4, "My grandfather speaks slowly.",
                listOf("My grandfather speaks slowly.", "My grandfather runs fast.", "My grandfather drives well.")),

            // 8. QUESTION: Listening (kisi4) - Slowly
            createListening(8, "Listen and choose: How does he speak?", R.drawable.kisi4, "My grandfather speaks slowly.",
                listOf("My grandfather speaks slowly.", "My grandfather runs fast.", "My grandfather cooks cake.")),

            // 9. QUESTION: kedi3 (Grandmother) - Kind
            createVisual(9, "Is she very kind?", R.drawable.kedi3, "My grandmother is very kind.",
                listOf("My grandmother is very kind.", "My grandmother drives fast.", "My grandmother is a student.")),

            // 10. QUESTION: Listening (kedi3) - Help
            createListening(10, "Listen and choose: Does she help you?", R.drawable.kedi3, "My grandmother helps me.",
                listOf("My grandmother helps me.", "My grandmother drives a car.", "My grandmother is sleeping.")),

            // 11. QUESTION: kedi4 (Baby) - Quietly
            createVisual(11, "Does the baby sleep quietly?", R.drawable.kedi4, "The baby sleeps quietly.",
                listOf("The baby sleeps quietly.", "The baby drives well.", "The baby speaks fast.")),

            // 12. QUESTION: Listening (kedi4) - Quietly
            createListening(12, "Listen and choose: How does the baby sleep?", R.drawable.kedi4, "The baby sleeps quietly.",
                listOf("The baby makes noise.", "The baby sleeps quietly.", "The baby runs fast.")),

            // 13. QUESTION: MATCHING (English to Turkish)
            createMatching(
                id = 13,
                questionText = "Match the English words with Turkish",
                image = R.drawable.cat_logo,
                pairs = "drive,cook,run,speak,sleep,well,delicious,fast,slowly,quietly",
                turkishOptions = listOf("Sürmek", "Pişirmek", "Koşmak", "Konuşmak", "Uyumak", "İyi", "Lezzetli", "Hızlı", "Yavaşça", "Sessizce")
            )
        )
    }
    fun getQuizFifteen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. Soru
            createListening(
                id = 1,
                qTurkish = "Nasılsın?", // Türkçe metin
                res = R.drawable.kisi1,
                ansEnglish = "İyiyim",
                opts = listOf("İyiyim", "Kötü"),
                qTr = true,  // 👈 BURASI MUTLAKA TRUE OLMALI!
                optTr = true // 👈 BURASI MUTLAKA TRUE OLMALI!
            )
        )
    }
    fun getQuizSixteen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- 1. BÖLÜM: VISUAL (Hangisi ...?) ---
            createVisual(1601, "Hangisi balkon?", R.drawable.balcony, "balcony", listOf("garage", "balcony", "roof", "door")),
            createVisual(1602, "Hangisi garaj?", R.drawable.garage, "garage", listOf("house", "fence", "garden", "garage")),
            createVisual(1603, "Hangisi çarı?", R.drawable.roof, "roof", listOf("roof", "window", "bbq", "door")),
            createVisual(1604, "Hangisi barbekü?", R.drawable.bbq, "bbq", listOf("garden", "bbq", "fence", "house")),
            createVisual(1605, "Hangisi çit?", R.drawable.fence, "fence", listOf("fence", "balcony", "roof", "window")),
            createVisual(1606, "Hangisi bahçe?", R.drawable.garden, "garden", listOf("garage", "house", "garden", "door")),
            createVisual(1607, "Hangisi kapı?", R.drawable.door, "door", listOf("door", "window", "balcony", "roof")),
            createVisual(1608, "Hangisi pencere?", R.drawable.window, "window", listOf("window", "door", "fence", "bbq")),
            createVisual(1609, "Hangisi ev?", R.drawable.house, "house", listOf("garden", "garage", "house", "balcony")),
            // --- 2. BÖLÜM: LISTENING (Türkçe Doğal Cümle -> İngilizce Karşılığı) ---
            createListening(10, "Balkonumuz oldukça küçük.", R.drawable.balcony, "My balcony is so small.",
                listOf("Our balcony is quite large.", "My balcony is so small.", "Your balcony is old."), qTr = true),

            createListening(11, "Arabam garajda.", R.drawable.garage, "My car is in the garage.",
                listOf("My car is in the house.", "My car is in the garage.", "My car is in the garden."), qTr = true),

            createListening(12, "Çatımız kıpkırmızı.", R.drawable.roof, "Our roof is very red.",
                listOf("The roof is blue.", "Our roof is very red.", "Their roof is black."), qTr = true),

            createListening(13, "mangal yapalım.", R.drawable.bbq, "Let's have a BBQ.",
                listOf("Let's have a BBQ.", "Let's eat dinner.", "Let's go home."), qTr = true),

            createListening(14, "Çit kahverengi", R.drawable.fence, "The fence is brown.",
                listOf("The fence is brown.", "Our garden fence is very white.", "The fence is wood."), qTr = true),

            createListening(15, "Bahçem gerçekten çok güzel.", R.drawable.garden, "My garden is really beautiful.",
                listOf("The garden is big.", "My garden is really beautiful.", "My garden is small."), qTr = true),

            createListening(16, "Lütfen şu kapıyı aç.", R.drawable.door, "Please open that door.",
                listOf("Close the door.", "Please open that door.", "Lock the door."), qTr = true),
            createMatching(
                id = 17,
                questionText = "Kelimeleri Eşleştir - 1",
                image = R.drawable.house,
                pairs = "house,garden,garage,balcony,roof",
                turkishOptions = listOf("Ev", "Bahçe", "Garaj", "Balkon", "Çatı")
            ),

            createMatching(
                id = 18,
                questionText = "Kelimeleri Eşleştir - 2",
                image = R.drawable.garden,
                pairs = "door,window,fence,bbq,large",
                turkishOptions = listOf("Kapı", "Pencere", "Çit", "Mangal", "Geniş")
            ),

            createListening(19, "Pencereden dışarıya bak.", R.drawable.window, "Look out of the window.",
                listOf("Close the window.", "Look out of the window.", "Clean the window."), qTr = true),

            createListening(20, "İşte benim güzel evim.", R.drawable.house, "Here is my beautiful house.",
                listOf("This is my school.", "Here is my beautiful house.", "This is my garden."), qTr = true),





        )
    }
    fun getQuizSeventeen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- 1. BÖLÜM: VISUAL (Hangisi ...?) ---
            createVisual(1701, "Hangisi yatak odası?", R.drawable.bedroom, "bedroom", listOf("kitchen", "bedroom", "garage", "bathroom")),
            createVisual(1702, "Hangisi mutfak?", R.drawable.kitchen, "kitchen", listOf("diningroom", "laundryroom", "kitchen", "house")),
            createVisual(1703, "Hangisi yemek odası?", R.drawable.diningroom, "diningroom", listOf("livingroom", "balcony", "diningroom", "roof")),
            createVisual(1704, "Hangisi banyo?", R.drawable.bathroom, "bathroom", listOf("kitchen", "bathroom", "bedroom", "garage")),
            createVisual(1705, "Hangisi çamaşır odası?", R.drawable.laundryroom, "laundryroom", listOf("bedroom", "kitchen", "diningroom", "laundryroom")),
            createVisual(1706, "Hangisi oturma odası", R.drawable.livingroom, "livingroom", listOf("livingroom", "kitchen", "bedroom", "house")),

            // --- 2. BÖLÜM: LISTENING (İngilizce Cümle -> Türkçe Şıklar) ---
            createListening(1707, "My bedroom is quite quiet.", R.drawable.bedroom, "Yatak odam oldukça sessiz.",
                listOf("Yatak odam çok büyük.", "Yatak odam oldukça sessiz.", "Yatak odası çok dağınık."), qTr = false),

            createListening(1708, "I am cooking in the kitchen.", R.drawable.kitchen, "Mutfakta yemek yapıyorum.",
                listOf("Bahçede yemek yiyorum.", "Mutfak çok temiz.", "Mutfakta yemek yapıyorum."), qTr = false),

            createListening(1709, "The laundry room is tidy.", R.drawable.laundryroom, "Çamaşır odası düzenli.",
                listOf("Çamaşır odası düzenli.", "Banyo çok küçük.", "Ev bomboş."), qTr = false),

            createListening(1710, "Let's meet in the dining room.", R.drawable.diningroom, "Yemek odasında buluşalım.",
                listOf("Garaja gidelim.", "Yemek odası kırmızı.", "Yemek odasında buluşalım."), qTr = false),

            createListening(1711, "My bathroom is blue.", R.drawable.bathroom, "Banyom mavidir.",
                listOf("Banyo beyazdır.", "Banyom mavidir.", "Banyom yenidir."), qTr = false),

            // --- ESKİ KONULAR TEKRAR ---
            createListening(1712, "Our garden is really large.", R.drawable.garden, "Bahçemiz gerçekten çok geniş.",
                listOf("Bahçemiz küçük.", "Bahçem çok güzel.", "Bahçemiz gerçekten çok geniş."), qTr = false),

            createListening(1713, "Close the garage door.", R.drawable.garage, "Garajın kapısını kapat.",
                listOf("Garajın kapısını kapat.", "Pencereyi aç.", "Evin kapısını kilitle."), qTr = false),

            createListening(1714, "Let's drink tea on the balcony.", R.drawable.balcony, "Balkonda çay içelim.",
                listOf("Balkon çok büyük.", "Balkonda çay içelim.", "Evin içindeyiz."), qTr = false),

            // --- 3. BÖLÜM: EŞLEŞTİRME ---
            createMatching(
                id = 1715,
                questionText = "Odaları Eşleştir",
                image = R.drawable.house,
                pairs = "bedroom,kitchen,bathroom,garage,garden",
                turkishOptions = listOf("Yatak Odası", "Mutfak", "Banyo", "Garaj", "Bahçe")
            ),

            createMatching(
                id = 1716,
                questionText = "Detayları Eşleştir",
                image = R.drawable.kisi4,
                pairs = "livingroom,diningroom,laundryroom,house,balcony",
                turkishOptions = listOf("Oturma Odası", "Yemek Odası", "Çamaşır Odası", "Ev", "Balkon")
            ),

            createListening(1717, "I am resting in the living room.", R.drawable.livingroom, "Oturma odasında dinleniyorum.",
                listOf("Yatak odasında uyuyorum.", "Mutfaktayım.", "Oturma odasında dinleniyorum."), qTr = false)
        )
    }
    fun getQuizEighteen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- 1. BÖLÜM: VISUAL (Görsel Tanıma) ---
            createVisual(1801, "Hangisi çamaşır makinesi?", R.drawable.washingmachine, "washing machine", listOf("oven", "washing machine", "refrigerator", "cooker")),
            createVisual(1802, "Hangisi buzdolabı?", R.drawable.refrigerator, "refrigerator", listOf("wardrobe", "shelf", "drawer", "refrigerator")),
            createVisual(1803, "Hangisi fırın?", R.drawable.oven, "oven", listOf("cooker", "oven", "washing machine", "lamp")),
            createVisual(1804, "Hangisi perde?", R.drawable.curtain, "curtain", listOf("carpet", "curtain", "mirror", "table")),
            createVisual(1805, "Hangisi gardırop?", R.drawable.wardrobe, "wardrobe", listOf("shelf", "drawer", "wardrobe", "bed")),
            createVisual(1806, "Hangisi ayna?", R.drawable.mirror, "mirror", listOf("mirror", "television", "lamp", "chair")),

            // --- 2. BÖLÜM: LISTENING (İngilizce Cümle -> Türkçe Şıklar) ---
            // Senin istediğin format: Günlük hayattan doğal cümleler
            createListening(1807, "The refrigerator is in the kitchen.", R.drawable.refrigerator, "Buzdolabı mutfaktadır.",
                listOf("Buzdolabı mutfaktadır.", "Mutfak çok sıcak.", "Buzdolabı bozuk."), qTr = false),

            createListening(1808, "Turn on the lamp, please.", R.drawable.lamp, "Lütfen lambayı aç.",
                listOf("Lamba çok parlak.", "Lütfen lambayı aç.", "Işığı kapatma."), qTr = false),

            createListening(1809, "I am watching television in the living room.", R.drawable.television, "Oturma odasında televizyon izliyorum.",
                listOf("Televizyon çok büyük.", "Oturma odasında televizyon izliyorum.", "Oturma odasında dinleniyorum."), qTr = false),

            createListening(1810, "Put the clothes in the washing machine.", R.drawable.washingmachine, "Kıyafetleri çamaşır makinesine koy.",
                listOf("Kıyafetleri çamaşır makinesine koy.", "Çamaşır odası çok dağınık.", "Makineyi hemen çalıştır."), qTr = false),

            createListening(1811, "Books are on the shelf.", R.drawable.shelf, "Kitaplar rafın üzerinde",
                listOf("Raf çok yüksek.", "Kitaplar çok eski.", "Kitaplar rafın üzerinde"), qTr = false),

            createListening(1812, "The carpet is very soft.", R.drawable.carpet, "Halı çok yumuşak.",
                listOf("Halı çok kirli.", "Halı çok yumuşak.", "Yerde halı yok."), qTr = false),

            createListening(1813, "Open the drawer", R.drawable.drawer, "Çekmeceyi aç",
                listOf("Çekmeceyi aç", "Dolabı hemen kapat.", "Çekmece çok sıkışmış."), qTr = false),

            // --- 3. BÖLÜM: ESKİ KONULARLA HARMANLANMIŞ MIX ---
            createListening(1814, "The dining table is in the dining room.", R.drawable.diningtable, "Yemek masası yemek odasındadır.",
                listOf("Yemek masası mutfakta.", "Yemek masası yemek odasındadır.", "Yemek odası çok geniş."), qTr = false),

            createListening(1815, "The mirror is in the bathroom", R.drawable.mirror, "Ayna banyodadır.",
                listOf("Ayna banyodadır.", "Banyo aynası kırık.", "Aynaya bakıyorum."), qTr = false),

            createListening(1816, "I am sitting on the chair.", R.drawable.chair, "Sandalyede oturuyorum.",
                listOf("Sandalye çok rahat.", "Sandalyede oturuyorum.", "Masanın üstündeyim."), qTr = false),

            // --- 4. BÖLÜM: EŞLEŞTİRME (Matching) ---
            createMatching(
                id = 1817,
                questionText = "Eşyaları Eşleştir",
                image = R.drawable.house,
                pairs = "cooker,oven,curtain,refrigerator,soda",
                turkishOptions = listOf("Ocak", "Fırın", "Perde", "Buzdolabı", "Kanepe")
            ),

            createMatching(
                id = 1818,
                questionText = "Mobilyaları Eşleştir",
                image = R.drawable.kisi4,
                pairs = "bed,table,chair,wardrobe,carpet",
                turkishOptions = listOf("Yatak", "Masa", "Sandalye", "Gardırop", "Halı")
            )
        )
    }
    fun getQuizNineteen(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- 1. BÖLÜM: VISUAL (6 Adet - Karışık Şıklar) ---
            createVisual(1901, "Hangisi fırın?", R.drawable.oven, "oven",
                listOf("washingmachine", "refrigerator", "oven", "cooker")), // Doğru cevap 3. şık

            createVisual(1902, "Hangisi halı?", R.drawable.carpet, "carpet",
                listOf("curtain", "shelf", "drawer", "carpet")), // Doğru cevap 4. şık

            createVisual(1903, "Hangisi lamba?", R.drawable.lamp, "lamp",
                listOf("television", "lamp", "mirror", "chair")), // Doğru cevap 2. şık

            createVisual(1904, "Hangisi çamaşır makinesi?", R.drawable.washingmachine, "washing machine",
                listOf("washingmachine", "refrigerator", "oven", "carpet")), // Doğru cevap 1. şık

            createVisual(1905, "Hangisi çekmece?", R.drawable.drawer, "drawer",
                listOf("shelf", "table", "drawer", "wardrobe")), // Doğru cevap 3. şık

            createVisual(1906, "Hangisi buzdolabı?", R.drawable.refrigerator, "refrigerator",
                listOf("cooker", "refrigerator", "oven", "washing machine")), // Doğru cevap 2. şık

            // --- 2. BÖLÜM: LISTENING (Basit There is / There are - 7 Soru) ---
            createListening(1907, "bed is in the bedrook", R.drawable.bed, "Yatak, yatak odasındadır.",
                listOf("Yatak, yatak odasındadır.", "Yatak odası çok büyük.", "Yatakta uyuyorum."), qTr = true),

            createListening(1908, "We have two chair in the kitchen", R.drawable.diningroom, "Mutfakta iki sandalyemiz var",
                listOf("Mutfakta iki masa var.", "Mutfakta iki sandalyemiz var", "Sandalyeler mutfakta değil."), qTr = true),

            createListening(1909, "The big mirror is in the bathroom.", R.drawable.mirror, "Büyük ayna banyodadır.",
                listOf("Büyük ayna banyodadır.", "Banyo aynası çok eski.", "Banyoda ayna yok."), qTr = true),

            createListening(1910, "We have two lamps in the living room.", R.drawable.lamp, "Oturma odasında iki lambamız var.",
                listOf("Oturma odası çok karanlık.", "Lambayı açar mısın?", "Oturma odasında iki lambamız var."), qTr = true),

            createListening(1911, "The television is on the table.", R.drawable.television, "Televizyon masasnın üstünde.",
                listOf("Televizyon masasnın üstünde.", "Masanın üzerinde iki televizyon var.", "Televizyon izliyoruz."), qTr = true),

            createListening(1912, "The cooker is in the kitchen", R.drawable.cooker, "Ocak mutfaktadır.",
                listOf("Mutfakta bir fırın var.", "Mutfak çok küçük.", "Ocak mutfaktadır."), qTr = true),

            createListening(1913, "The books are on the shelf", R.drawable.shelf, "Kitaplar rafta.",
                listOf("Kitaplar rafta.", "Raf kitaplarla dolu.", "Kitaplarımı arıyorum."), qTr = true),

            // --- 3. BÖLÜM: EŞLEŞTİRME (Matching) ---
            createMatching(
                id = 1914,
                questionText = "Eşyaları Eşleştir",
                image = R.drawable.house,
                pairs = "washing machine,refrigerator,gardrobe,diningtable,drawer",
                turkishOptions = listOf("Çamaşır Makinesi", "Buzdolabı", "Gardırop", "Yemek Masası", "Çekmece")
            ),

            createMatching(
                id = 1915,
                questionText = "Sayıları Eşleştir",
                image = R.drawable.kisi4,
                pairs = "bed,lamp,chair,curtain,shelf",
                turkishOptions = listOf("yatak", "lamba", "sandalye", "perde", "raf")
            )
        )
    }
    fun getQuizTwenty(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- 1. BÖLÜM: VISUAL (7 Adet - Karışık Şıklar) ---
            createVisual(2001, "Hangisi gardırop?", R.drawable.wardrobe, "wardrobe",
                listOf("shelf", "drawer", "wardrobe", "bed")), // 3. şık

            createVisual(2002, "Hangisi yemek masası?", R.drawable.diningtable, "dining table",
                listOf("diningtable", "table", "chair", "cooker")), // 1. şık

            createVisual(2003, "Hangisi bahçe?", R.drawable.garden, "garden",
                listOf("garage", "balcony", "garden", "roof")), // 3. şık

            createVisual(2004, "Hangisi çamaşır makinesi?", R.drawable.washingmachine, "washing machine",
                listOf("refrigerator", "oven", "cooker", "washing machine")), // 4. şık

            createVisual(2005, "Hangisi ayna?", R.drawable.mirror, "mirror",
                listOf("television", "mirror", "lamp", "curtain")), // 2. şık

            createVisual(2006, "Hangisi kanepe?", R.drawable.sofa, "sofa",
                listOf("sofa", "bed", "chair", "diningtable")), // 1. şık

            // Eşleştirme yerine eklenen yeni Visual
            createVisual(2007, "Hangisi çamaşır makinesi?", R.drawable.washingmachine, "washing machine",
                listOf("oven", "washing machine", "refrigerator", "cooker")), // 2. şık

            // --- 2. BÖLÜM: LISTENING (8 Adet - Karmaşık Yapılar ve Tekrar) ---
            createListening(2008, "The refrigerator is very cold.", R.drawable.refrigerator, "Buzdolabı çok soğuk.",
                listOf("Buzdolabı çok küçük.", "Buzdolabı çok soğuk.", "Mutfak çok soğuk."), qTr = false),

            createListening(2009, "Close the curtain in the bedroom.", R.drawable.curtain, "Yatak odasındaki perdeyi kapat.",
                listOf("Yatak odasındaki perdeyi kapat.", "Salondaki pencereyi aç.", "Perde çok kirli."), qTr = false),

            createListening(2010, "We have a big house with a garden.", R.drawable.house, "Bahçeli büyük bir evimiz var.",
                listOf("Evimiz çok küçük.", "Bahçede büyük bir ev var.", "Bahçeli büyük bir evimiz var."), qTr = false),

            createListening(2011, "We have a mirror in the bathroom.", R.drawable.bathroom, "Banyoda bir aynamız var.",
                listOf("Banyoda bir lamba var.", "Banyoda bir aynamız var.", "Banyo masmavidir."), qTr = false),

            createListening(2012, "The cooker is in the kitchen.", R.drawable.cooker, "Ocak mutfaktadır.",
                listOf("Ocak mutfaktadır.", "Fırın mutfaktadır.", "Mutfak çok geniştir."), qTr = false),

            createListening(2013, "The balcony is so small", R.drawable.balcony, "Balkon çok küçük.",
                listOf("Balkon çok küçük.", "Balkona bak.", "Lütfen balkona gel."), qTr = false),

            createListening(2014, "The carpet is in the living room.", R.drawable.carpet, "Halı oturma odasındadır.",
                listOf("Halı yatak odasındadır.", "Halı oturma odasındadır.", "Kanepe oturma odasındadır."), qTr = false),

            // Eşleştirme yerine eklenen yeni Listening
            createListening(2015, "We have a small carpet in the bedroom", R.drawable.bedroom, "Yatak odasında küçük bir halımız var.",
                listOf("Duvarda bir raf var.", "Yatak odasında küçük bir halımız var.", "Duvarda bir tablo var."), qTr = false)
        )
    }


    fun getQuizTwentyOne(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- KOLAY SEVİYE (8 ADET) ---
            // 1. Görsel Quiz (Tanıtım) - Soru TR, Cevap EN
            createVisual(
                id = 1,
                questionTurkish = "Yemek yemek",
                imageRes = R.drawable.eat,
                correctAnswer = "eat",
                options = listOf("eat", "drink", "run", "sleep")
            ),
            // 2. Görsel Quiz (Tanıtım)
            createVisual(
                id = 2,
                questionTurkish = "İçmek",
                imageRes = R.drawable.drink,
                correctAnswer = "drink",
                options = listOf("write", "read", "drink", "walk")
            ),
            // 3. Listening (İngilizce Soru -> Türkçe Cevap)
            createListening(
                id = 3,
                qTurkish = "I eat.",
                res = R.drawable.eat,
                ansEnglish = "Ben yerim.",
                opts = listOf("Ben yerim.", "Ben içerim.", "Ben uyurum.")
            ),
            // 4. Listening
            createListening(
                id = 4,
                qTurkish = "You drink.",
                res = R.drawable.drink,
                ansEnglish = "Sen içersin.",
                opts = listOf("Sen okursun.", "Sen içersin.", "Sen yazarsın.")
            ),
            // 5. Listening
            createListening(
                id = 5,
                qTurkish = "He sleeps.",
                res = R.drawable.sleep,
                ansEnglish = "O uyur.",
                opts = listOf("O uyur.", "O koşar.", "O yürür.")
            ),
            // 6. Listening
            createListening(
                id = 6,
                qTurkish = "He walk.",
                res = R.drawable.walk,
                ansEnglish = "O yürürler.",
                opts = listOf("Biz yürürüz.", "O yürürler.", "Siz yürürsünüz.")
            ),
            // 7. Listening
            createListening(
                id = 7,
                qTurkish = "He run.",
                res = R.drawable.run,
                ansEnglish = "O koşar.",
                opts = listOf("O koşar.", "Onlar koşarlar.", "Ben koşarım.")
            ),
            // 8. Listening
            createListening(
                id = 8,
                qTurkish = "I write.",
                res = R.drawable.write,
                ansEnglish = "Ben yazarım.",
                opts = listOf("Ben okurum.", "Ben yazarım.", "Ben izlerim.")
            ),

            // --- NORMAL SEVİYE (4 ADET) ---
            // 9. Cümle Kurma (Bağlam)
            createListening(
                id = 9,
                qTurkish = "I read a book.",
                res = R.drawable.read,
                ansEnglish = "Ben bir kitap okurum.",
                opts = listOf("Ben bir kitap okurum.", "Ben bir kitap yazarım.", "Ben kitap severim.")
            ),
            // 10. Cümle Kurma
            createListening(
                id = 10,
                qTurkish = "She watches TV.",
                res = R.drawable.watch,
                ansEnglish = "O televizyon izler.",
                opts = listOf("O radyo dinler.", "O kitap okur.", "O televizyon izler.")
            ),
            // 11. Cümle Kurma
            createListening(
                id = 11,
                qTurkish = "They listen to music.",
                res = R.drawable.listen,
                ansEnglish = "Onlar müzik dinlerler.",
                opts = listOf("Onlar müzik dinlerler.", "Onlar müzik yaparlar.", "Onlar dans ederler.")
            ),
            // 12. Cümle Kurma
            createListening(
                id = 12,
                qTurkish = "The student writes.",
                res = R.drawable.write,
                ansEnglish = "Öğrenci yazı yazar.",
                opts = listOf("Öğretmen okur.", "Öğrenci yazı yazar.", "Öğrenci uyur.")
            ),

            // --- ZOR SEVİYE (3 ADET) ---
            // 13. Matching (Pekiştirme)
            createMatching(
                id = 13,
                questionText = "Kelimeleri eşleştir",
                image = R.drawable.cat_logo,
                pairs = "eat,drink,sleep,read,write",
                turkishOptions = listOf("yemek", "içmek", "uyumak", "okumak", "yazmak")
            ),
            // 14. Matching (Pekiştirme)
            createMatching(
                id = 14,
                questionText = "Eylemleri eşleştir",
                image = R.drawable.cat_logo,
                pairs = "run,walk,watch,listen,book",
                turkishOptions = listOf("koşmak", "yürümek", "izlemek", "dinlemek", "kitap")
            ),
            // 15. Listening (Karma Final)
            createListening(
                id = 15,
                qTurkish = "I read a book and drink water.",
                res = R.drawable.read,
                ansEnglish = "Kitap okurum ve su içerim.",
                opts = listOf("Kitap okurum ve su içerim.", "Kitap yazarım ve yemek yerim.", "Müzik dinlerim ve uyurum.")
            )
        )
    }
    fun getQuizTwentyTwo(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- KOLAY SEVİYE (8 ADET) ---
            // 1. Görsel Quiz (Hatırlatma)
            createVisual(
                id = 1,
                questionTurkish = "Okumak",
                imageRes = R.drawable.read,
                correctAnswer = "read",
                options = listOf("read", "write", "listen", "watch")
            ),
            // 2. Görsel Quiz (Hatırlatma)
            createVisual(
                id = 2,
                questionTurkish = "Koşmak",
                imageRes = R.drawable.run,
                correctAnswer = "run",
                options = listOf("walk", "run", "eat", "sleep")
            ),
            // 3. Listening
            createListening(
                id = 3,
                qTurkish = "I drink water.",
                res = R.drawable.drink,
                ansEnglish = "Ben su içerim.",
                opts = listOf("Ben su içerim.", "Süt içerim.", "Su yerim.")
            ),
            // 4. Listening
            createListening(
                id = 4,
                qTurkish = "She reads.",
                res = R.drawable.read,
                ansEnglish = "O okur.",
                opts = listOf("O okur.", "O yazar.", "O izler.")
            ),
            // 5. Listening
            createListening(
                id = 5,
                qTurkish = "I sleep at home.",
                res = R.drawable.sleep,
                ansEnglish = "Ben evde uyurum.",
                opts = listOf("Biz okulda uyuruz.", "Ben evde uyurum.", "Biz evde yeriz.")
            ),
            // 6. Listening
            createListening(
                id = 6,
                qTurkish = "You walk.",
                res = R.drawable.walk,
                ansEnglish = "Sen yürürsün.",
                opts = listOf("Sen yürürsün.", "O yürür.", "Biz yürürüz.")
            ),
            // 7. Listening
            createListening(
                id = 7,
                qTurkish = "They watch.",
                res = R.drawable.watch,
                ansEnglish = "Onlar izlerler.",
                opts = listOf("O dinler.", "Onlar izlerler.", "O okur.")
            ),
            // 8. Listening
            createListening(
                id = 8,
                qTurkish = "He listens.",
                res = R.drawable.listen,
                ansEnglish = "O dinler.",
                opts = listOf("Biz dinleriz.", "Siz dinlersiniz.", "O dinler.")
            ),

            // --- NORMAL SEVİYE (4 ADET) ---
            // 9. Listening (Mekan + Fiil)
            createListening(
                id = 9,
                qTurkish = "The doctor eats at the hospital.",
                res = R.drawable.eat,
                ansEnglish = "Doktor hastanede yemek yer.",
                opts = listOf("Doktor hastanede yemek yer.", "Hemşire hastanede uyur.", "Doktor okulda yer.")
            ),
            // 10. Listening (Zamir + Fiil + Obje)
            createListening(
                id = 10,
                qTurkish = "We write a book.",
                res = R.drawable.write,
                ansEnglish = "Bir kitap yazarız.",
                opts = listOf("Bir kitap okurum.", "Bir kitap yazarız..", "Bir kitap alırım.")
            ),
            // 11. Listening (Olumsuz hissi veren farklı şıklar)
            createListening(
                id = 11,
                qTurkish = "We run at school.",
                res = R.drawable.run,
                ansEnglish = "Okulda koşarız.",
                opts = listOf("Evde koşarız.", "Okulda yürürüz.", "Okulda koşarız.")
            ),
            // 12. Listening (Karma)
            createListening(
                id = 12,
                qTurkish = "I listen to music",
                res = R.drawable.listen,
                ansEnglish = "Ben müzik dinlerim.",
                opts = listOf("Arkadaşım beni dinler.", "Ben müzik dinlerim.", "Arkadaşım beni izler.")
            ),

            // --- ZOR SEVİYE (3 ADET) ---
            // 13. Matching (Fiil - Nesne/Mekan eşleşmesi)
            createMatching(
                id = 13,
                questionText = "Anlamlı eşleştir",
                image = R.drawable.cat_logo,
                pairs = "read,drink,sleep,watch,walk",
                turkishOptions = listOf("okumak", "içmek", "uyumak", "izlemek", "yürümek")
            ),
            // 14. Matching (Zamir - Fiil eşleşmesi)
            createMatching(
                id = 14,
                questionText = "Kelimeleri tamamla",
                image = R.drawable.cat_logo,
                pairs = "write,run,listen,eat,book",
                turkishOptions = listOf("yazmak", "koşmak", "dinlemek", "yemek", "kitap")
            ),
            // 15. Listening (En Karmaşık Cümle)
            createListening(
                id = 15,
                qTurkish = "He is a student and he writes in a book.",
                res = R.drawable.write,
                ansEnglish = "O bir öğrencidir ve bir kitaba yazar.",
                opts = listOf("O bir öğrencidir ve bir kitaba yazar.", "O bir öğretmendir ve kitap okur.", "O bir öğrencidir ve uyur.")
            )
        )
    }
    fun getQuizTwentyThree(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- KOLAY SEVİYE (8 ADET) ---
            // 1. Görsel Quiz (Yeni Kelime)
            createVisual(
                id = 1,
                questionTurkish = "Zıplamak",
                imageRes = R.drawable.jump,
                correctAnswer = "jump",
                options = listOf("jump", "run", "walk", "sing")
            ),
            // 2. Görsel Quiz (Yeni Kelime)
            createVisual(
                id = 2,
                questionTurkish = "Şarkı söylemek",
                imageRes = R.drawable.sing,
                correctAnswer = "sing",
                options = listOf("speak", "listen", "sing", "eat")
            ),
            // 3. Listening
            createListening(
                id = 3,
                qTurkish = "I speak English.",
                res = R.drawable.speak,
                ansEnglish = "İngilizce konuşurum.",
                opts = listOf("İngilizce konuşurum.", "İngilizce yazarım.", "İngilizce okurum.")
            ),
            // 4. Listening
            createListening(
                id = 4,
                qTurkish = "Open the door.",
                res = R.drawable.open,
                ansEnglish = "Kapıyı aç.",
                opts = listOf("Kapıyı kapat.", "Kapıyı aç.", "Kapıya bak.")
            ),
            // 5. Listening
            createListening(
                id = 5,
                qTurkish = "She writes",
                res = R.drawable.write,
                ansEnglish = "O yazar",
                opts = listOf("O dinler", "O yazar", "Pencereyi sil.")
            ),
            // 6. Listening
            createListening(
                id = 6,
                qTurkish = "She sings.",
                res = R.drawable.sing,
                ansEnglish = "O şarkı söyler.",
                opts = listOf("O şarkı söyler.", "O konuşur.", "O dinler.")
            ),
            // 7. Listening
            createListening(
                id = 7,
                qTurkish = "We jump.",
                res = R.drawable.jump,
                ansEnglish = "Biz zıplarız.",
                opts = listOf("Biz koşarız.", "Biz zıplarız.", "Biz yürürüz.")
            ),
            // 8. Listening
            createListening(
                id = 8,
                qTurkish = "They speak.",
                res = R.drawable.speak,
                ansEnglish = "Onlar konuşurlar.",
                opts = listOf("Onlar konuşurlar.", "Onlar yazarlar.", "Onlar okurlar.")
            ),

            // --- NORMAL SEVİYE (4 ADET) ---
            // 9. Listening
            createListening(
                id = 9,
                qTurkish = "Open your book.",
                res = R.drawable.book,
                ansEnglish = "Kitabını aç.",
                opts = listOf("Kitabını kapat.", "Kitabını aç.", "Kitabını oku.")
            ),
            // 10. Listening
            createListening(
                id = 10,
                qTurkish = "I listen and sing.",
                res = R.drawable.sing,
                ansEnglish = "Dinlerim ve şarkı söylerim.",
                opts = listOf("Dinlerim ve şarkı söylerim.", "Okurum ve yazarım.", "İzlerim ve konuşurum.")
            ),
            // 11. Listening
            createListening(
                id = 11,
                qTurkish = "Open the book and read.",
                res = R.drawable.write,
                ansEnglish = "Kitabı aç ve oku.",
                opts = listOf("Kitabı aç ve oku.", "Kitabı kapat ve uyu.", "Kitabı yaz ve uyu.")
            ),
            // 12. Listening
            createListening(
                id = 12,
                qTurkish = "He speaks and we listen.",
                res = R.drawable.speak,
                ansEnglish = "O konuşur ve biz dinleriz.",
                opts = listOf("O konuşur ve biz dinleriz.", "O yazar ve biz okuruz.", "O söyler ve biz izleriz.")
            ),

            // --- ZOR SEVİYE (3 ADET) ---
            // 13. Matching (Yeni Kelimeler Odaklı)
            createMatching(
                id = 13,
                questionText = "Eylemleri eşleştir",
                image = R.drawable.cat_logo,
                pairs = "jump,sing,speak,open,close",
                turkishOptions = listOf("zıplamak", "şarkı söylemek", "konuşmak", "açmak", "kapatmak")
            ),
            // 14. Matching (Genel Karma)
            createMatching(
                id = 14,
                questionText = "Karma eşleştirme",
                image = R.drawable.cat_logo,
                pairs = "eat,drink,sleep,read,write",
                turkishOptions = listOf("yemek", "içmek", "uyumak", "okumak", "yazmak")
            ),
            // 15. Listening (Final Karma Cümle)
            createListening(
                id = 15,
                qTurkish = "I open the book, read and speak English.",
                res = R.drawable.read,
                ansEnglish = "Kitabı açarım, okurum ve İngilizce konuşurum.",
                opts = listOf("Kitabı açarım, okurum ve İngilizce konuşurum.", "Kitabı kapatırım ve uyurum.", "Kitabı yazarım ve dinlerim.")
            )
        )
    }

    fun getQuizTwentyFour(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- KOLAY SEVİYE (8 ADET) ---
            // 1. Görsel Quiz (Yeni Kelimelerden)
            createVisual(
                id = 1,
                questionTurkish = "Şarkı söylemek",
                imageRes = R.drawable.sing,
                correctAnswer = "sing",
                options = listOf("sing", "speak", "jump", "listen")
            ),
            // 2. Görsel Quiz (Mekan/Eylem Hatırlatma)
            createVisual(
                id = 2,
                questionTurkish = "Yürümek",
                imageRes = R.drawable.walk,
                correctAnswer = "walk",
                options = listOf("run", "walk", "jump", "eat")
            ),
            // 3. Listening
            createListening(
                id = 3,
                qTurkish = "I jump.",
                res = R.drawable.jump,
                ansEnglish = "Zıplarım.",
                opts = listOf("Zıplarım.", "Koşarım.", "Uyurum.")
            ),
            // 4. Listening
            createListening(
                id = 4,
                qTurkish = "You speak.",
                res = R.drawable.speak,
                ansEnglish = "Sen konuşursun.",
                opts = listOf("Sen okursun.", "Sen yazarsın.", "Sen konuşursun.")
            ),
            // 5. Listening
            createListening(
                id = 5,
                qTurkish = "Open the door.",
                res = R.drawable.open,
                ansEnglish = "Kapıyı aç.",
                opts = listOf("Kapıyı aç.", "Kapıyı kapat.", "Kitabı aç.")
            ),
            // 6. Listening
            createListening(
                id = 6,
                qTurkish = "He drinks water.",
                res = R.drawable.drink,
                ansEnglish = "O su içer.",
                opts = listOf("O su içer.", "O süt içer.", "O süt yer.")
            ),
            // 7. Listening
            createListening(
                id = 7,
                qTurkish = "I walk at school.",
                res = R.drawable.walk,
                ansEnglish = "Okulda yürürüm.",
                opts = listOf("Evde yürürüz.", "Okulda yürürüm.", "Okulda koşarız.")
            ),
            // 8. Listening
            createListening(
                id = 8,
                qTurkish = "They watch TV.",
                res = R.drawable.watch,
                ansEnglish = "Onlar televizyon izlerler.",
                opts = listOf("Onlar müzik dinlerler.", "Onlar televizyon izlerler.", "Onlar kitap okurlar.")
            ),

            // --- NORMAL SEVİYE (4 ADET) ---
            // 9. Karma Cümle
            createListening(
                id = 9,
                qTurkish = "I open my book and read.",
                res = R.drawable.open,
                ansEnglish = "Kitabımı açarım ve okurum.",
                opts = listOf("Kitabımı açarım ve okurum.", "Kitabımı kapatırım ve uyurum.", "Kitabımı yazarım ve dinlerim.")
            ),
            // 10. Karma Cümle
            createListening(
                id = 10,
                qTurkish = "Open the door and sleep.",
                res = R.drawable.sleep,
                ansEnglish = "Kapıyı aç ve uyu.",
                opts = listOf("Pencereyi aç ve bak.", "Kapıyı aç ve uyu.", "Kapıyı kapat ve uyu.")
            ),
            // 11. Karma Cümle
            createListening(
                id = 11,
                qTurkish = "The student speaks and the teacher listens.",
                res = R.drawable.kedi4,
                ansEnglish = "Öğrenci konuşur ve öğretmen dinler.",
                opts = listOf("Öğrenci okur ve öğretmen yazar.", "Öğrenci yazar ve öğretmen dinler.", "Öğrenci konuşur ve öğretmen izler.")
            ),
            // 12. Karma Cümle
            createListening(
                id = 12,
                qTurkish = "We run in the morning.",
                res = R.drawable.run,
                ansEnglish = "Sabah koşarız.",
                opts = listOf("Akşam koşarız.", "Sabah yürürüz.", "Sabah koşarız.")
            ),

            // --- ZOR SEVİYE (3 ADET) ---
            // 13. Matching (Tüm Fiiller)
            createMatching(
                id = 13,
                questionText = "Fiilleri eşleştir",
                image = R.drawable.cat_logo,
                pairs = "jump,sing,speak,open,close",
                turkishOptions = listOf("zıplamak", "şarkı söylemek", "konuşmak", "açmak", "kapatmak")
            ),
            // 14. Matching (Tüm Fiiller 2)
            createMatching(
                id = 14,
                questionText = "Eylemleri eşleştir",
                image = R.drawable.cat_logo,
                pairs = "eat,drink,sleep,watch,listen",
                turkishOptions = listOf("yemek", "içmek", "uyumak", "izlemek", "dinlemek")
            ),
            // 15. Listening (Final Karma Cümle)
            createListening(
                id = 15,
                qTurkish = "I speak English, sing and jump.",
                res = R.drawable.jump,
                ansEnglish = "İngilizce konuşurum, şarkı söylerim ve zıplarım.",
                opts = listOf("İngilizce konuşurum, şarkı söylerim ve zıplarım.", "İngilizce okurum, yazarım ve yürürüm.", "İngilizce dinlerim ve uyurum.")
            )
        )
    }

    fun getQuizTwentyFive(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- KOLAY SEVİYE (8 ADET) ---
            // 1. Görsel Quiz (Yeni Kelime)
            createVisual(
                id = 1,
                questionTurkish = "Oynamak",
                imageRes = R.drawable.play,
                correctAnswer = "play",
                options = listOf("play", "sit", "stand", "walk")
            ),
            // 2. Görsel Quiz (Yeni Kelime)
            createVisual(
                id = 2,
                questionTurkish = "Oturmak",
                imageRes = R.drawable.sit,
                correctAnswer = "sit",
                options = listOf("stand", "sit", "look", "sleep")
            ),
            // 3. Listening
            createListening(
                id = 3,
                qTurkish = "I play.",
                res = R.drawable.play,
                ansEnglish = "Ben oynarım.",
                opts = listOf("Ben oynarım.", "Ben koşarım.", "Ben zıplarım.")
            ),
            // 4. Listening
            createListening(
                id = 4,
                qTurkish = "Sit down.",
                res = R.drawable.sit,
                ansEnglish = "Otur.",
                opts = listOf("Ayağa kalk.", "Otur.", "Bak.")
            ),
            // 5. Listening
            createListening(
                id = 5,
                qTurkish = "Stand up.",
                res = R.drawable.stand,
                ansEnglish = "Ayağa kalk.",
                opts = listOf("Otur.", "Ayağa kalk.", "Yürü.")
            ),
            // 6. Listening
            createListening(
                id = 6,
                qTurkish = "Look at the cat.",
                res = R.drawable.look,
                ansEnglish = "Kediye bak.",
                opts = listOf("Kediye bak.", "Kediyi dinle.", "Kediyle oyna.")
            ),
            // 7. Listening
            createListening(
                id = 7,
                qTurkish = "We dance.",
                res = R.drawable.dance,
                ansEnglish = "Dans ederiz.",
                opts = listOf("Dans ederiz.", "Şarkı söyleriz.", "Otururuz.")
            ),
            // 8. Listening
            createListening(
                id = 8,
                qTurkish = "I play football.",
                res = R.drawable.football,
                ansEnglish = "O futbol oynar.",
                opts = listOf("O futbol izler.", "O futbol oynar.", "O futbolu sever.")
            ),

            // --- NORMAL SEVİYE (4 ADET) ---
            // 9. Karma Cümle
            createListening(
                id = 9,
                qTurkish = "Sit and read a book.",
                res = R.drawable.sit,
                ansEnglish = "Otur ve bir kitap oku.",
                opts = listOf("Otur ve bir kitap oku.", "Ayağa kalk ve yürü.", "Otur ve şarkı söyle.")
            ),
            // 10. Karma Cümle
            createListening(
                id = 10,
                qTurkish = "Stand up and walk.",
                res = R.drawable.stand,
                ansEnglish = "Ayağa kalk ve yürü.",
                opts = listOf("Otur ve bekle.", "Ayağa kalk ve yürü.", "Ayağa kalk ve zıpla.")
            ),
            // 11. Karma Cümle
            createListening(
                id = 11,
                qTurkish = "I look and I speak.",
                res = R.drawable.look,
                ansEnglish = "Bakarım ve konuşurum.",
                opts = listOf("Bakarım ve konuşurum.", "Dinlerim ve yazarım.", "Bakarım ve şarkı söylerim.")
            ),
            // 12. Karma Cümle
            createListening(
                id = 12,
                qTurkish = "We sing and dance at school.",
                res = R.drawable.dance,
                ansEnglish = "Okulda şarkı söyler ve dans ederiz.",
                opts = listOf("Okulda şarkı söyler ve dans ederiz.", "Okulda oyun oynarız.", "Okulda otururuz.")
            ),

            // --- ZOR SEVİYE (3 ADET) ---
            // 13. Matching (Yeni Kelimeler)
            createMatching(
                id = 13,
                questionText = "Eylemleri eşleştir",
                image = R.drawable.cat_logo,
                pairs = "play,sit,stand,look,dance",
                turkishOptions = listOf("oynamak", "oturmak", "ayakta durmak", "bakmak", "dans etmek")
            ),
            // 14. Matching (Öncekilerle Karma)
            createMatching(
                id = 14,
                questionText = "Karma eşleştirme",
                image = R.drawable.cat_logo,
                pairs = "jump,sing,speak,open,listen",
                turkishOptions = listOf("zıplamak", "şarkı söylemek", "konuşmak", "açmak", "dinlemek")
            ),
            // 15. Listening (Final Karma)
            createListening(
                id = 15,
                qTurkish = "I open the book, sit and read.",
                res = R.drawable.sit,
                ansEnglish = "Kitabı açarım, oturur ve okurum.",
                opts = listOf("Kitabı açarım, oturur ve okurum.", "Kitabı yazarım ve bakarım.", "Kitabı kapatırım ve uyurum.")
            )
        )
    }
    fun getQuizTwentySix(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- KOLAY SEVİYE (8 ADET) ---
            // 1. Görsel Quiz (Yeni Kelime Tekrar)
            createVisual(
                id = 1,
                questionTurkish = "Ayakta durmak",
                imageRes = R.drawable.stand,
                correctAnswer = "stand",
                options = listOf("stand", "sit", "jump", "walk")
            ),
            // 2. Görsel Quiz (Yeni Kelime Tekrar)
            createVisual(
                id = 2,
                questionTurkish = "Bakmak",
                imageRes = R.drawable.look,
                correctAnswer = "look",
                options = listOf("look", "watch", "listen", "read")
            ),
            // 3. Listening & Çeviri
            createListening(
                id = 3,
                qTurkish = "I play with my friend.",
                res = R.drawable.friend,
                ansEnglish = "Arkadaşımla oynarım.",
                opts = listOf("Arkadaşımla oynarım.", "Arkadaşımla okurum.", "Arkadaşımla yürürüm.")
            ),
            // 4. Listening & Çeviri
            createListening(
                id = 4,
                qTurkish = "He sits on the chair.",
                res = R.drawable.sit,
                ansEnglish = "O sandalyede oturur.",
                opts = listOf("O sandalyede uyur.", "O sandalyede oturur.", "O sandalyede yazar.")
            ),
            // 5. Listening & Çeviri
            createListening(
                id = 5,
                qTurkish = "Stand up and look at me.",
                res = R.drawable.stand,
                ansEnglish = "Ayağa kalk ve bana bak.",
                opts = listOf("Otur ve beni dinle.", "Ayağa kalk ve bana bak.", "Ayağa kalk ve yürü.")
            ),
            // 6. Listening & Çeviri
            createListening(
                id = 6,
                qTurkish = "We dance and sing.",
                res = R.drawable.dance,
                ansEnglish = "Dans ederiz ve şarkı söyleriz.",
                opts = listOf("Dans ederiz ve şarkı söyleriz.", "Oynarız ve zıplarız.", "Okuruz ve yazarız.")
            ),
            // 7. Listening & Çeviri
            createListening(
                id = 7,
                qTurkish = "I open the book.",
                res = R.drawable.book,
                ansEnglish = "Kitabı açarım.",
                opts = listOf("Kitabı açarım.", "Kitabı okurum.", "Kitabı yazarım.")
            ),
            // 8. Listening & Çeviri
            createListening(
                id = 8,
                qTurkish = "He listen to the music.",
                res = R.drawable.listen,
                ansEnglish = "O müzik dinler.",
                opts = listOf("O öğretmene bakar.", "O müzik dinler.", "O öğretmene yazarr.")
            ),

            // --- NORMAL SEVİYE (4 ADET) ---
            // 9. Listening & Çeviri (Geniş Cümle)
            createListening(
                id = 9,
                qTurkish = "The cat jumps and plays.",
                res = R.drawable.kedi3,
                ansEnglish = "Kedi zıplar ve oynar.",
                opts = listOf("Kedi uyur ve yer.", "Kedi zıplar ve oynar.", "Kedi koşar ve bakar.")
            ),
            // 10. Listening & Çeviri (Mekanlı Cümle)
            createListening(
                id = 10,
                qTurkish = "I walk at school and speak English.",
                res = R.drawable.walk,
                ansEnglish = "Okulda yürürüm ve İngilizce konuşurum.",
                opts = listOf("Okulda yürürüm ve İngilizce konuşurum.", "Okulda koşarım ve İngilizce okurum.", "Evde yürürüm ve İngilizce yazarım.")
            ),
            // 11. Listening & Çeviri
            createListening(
                id = 11,
                qTurkish = "Sit down, please, and listen.",
                res = R.drawable.sit,
                ansEnglish = "Lütfen otur ve dinle.",
                opts = listOf("Lütfen ayağa kalk ve konuş.", "Lütfen otur ve dinle.", "Lütfen otur ve bak.")
            ),
            // 12. Listening & Çeviri
            createListening(
                id = 12,
                qTurkish = "My brother reads a book at home.",
                res = R.drawable.read,
                ansEnglish = "Erkek kardeşim evde bir kitap okur.",
                opts = listOf("Babam evde bir kitap okur.", "Erkek kardeşim evde bir kitap okur.", "Erkek kardeşim okulda yazar.")
            ),

            // --- ZOR SEVİYE (3 ADET) ---
            // 13. Matching (Yeni Kelimeler)
            createMatching(
                id = 13,
                questionText = "Anlamlı eşleştir",
                image = R.drawable.cat_logo,
                pairs = "play,sit,stand,look,dance",
                turkishOptions = listOf("oynamak", "oturmak", "ayakta durmak", "bakmak", "dans etmek")
            ),
            // 14. Matching (Fiil Karma)
            createMatching(
                id = 14,
                questionText = "Eylemleri tamamla",
                image = R.drawable.cat_logo,
                pairs = "jump,sing,speak,open,listen",
                turkishOptions = listOf("zıplamak", "şarkı söylemek", "konuşmak", "açmak", "dinlemek")
            ),
            // 15. Listening & Çeviri (Final Karma)
            createListening(
                id = 15,
                qTurkish = "I stand up, open the door and walk.",
                res = R.drawable.open,
                ansEnglish = "Ayağa kalkarım, kapıyı açarım ve yürürüm.",
                opts = listOf("Ayağa kalkarım, kapıyı açarım ve yürürüm.", "Otururum, kapıyı açarım ve bakarım.", "Ayağa kalkarım, kapıyı kapatırım ve koşarım.")
            )
        )
    }
    fun getQuizTwentySeven(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL (1) - SIT (1. Kullanım)
            createVisual(
                id = 1,
                questionTurkish = "Oturmak",
                imageRes = R.drawable.sit,
                correctAnswer = "sit",
                options = listOf("sit", "stand", "dance", "play")
            ),

            // 2. LISTENING (1) - EAT (1. Kullanım)
            createListening(
                id = 2,
                qTurkish = "I eat an apple.",
                res = R.drawable.eat,
                ansEnglish = "Bir elma yerim.",
                opts = listOf("Bir elma yerim.", "Bir elma alırım.", "Bir elma veririm.")
            ),

            // 3. GÖRSEL (2) - DANCE (1. Kullanım)
            createVisual(
                id = 3,
                questionTurkish = "Dans etmek",
                imageRes = R.drawable.dance,
                correctAnswer = "dance",
                options = listOf("dance", "sing", "jump", "play")
            ),

            // 4. LISTENING (2) - SIT Tekrar (2. Kullanım - Araya 2 soru girdi)
            createListening(
                id = 4,
                qTurkish = "Sit down, please.",
                res = R.drawable.sit,
                ansEnglish = "Lütfen otur.",
                opts = listOf("Lütfen bak.", "Lütfen otur.", "Lütfen kalk.")
            ),

            // 5. LISTENING (3) - WALK (1. Kullanım)
            createListening(
                id = 5,
                qTurkish = "We walk at school.",
                res = R.drawable.walk,
                ansEnglish = "Okulda yürürüz.",
                opts = listOf("Okulda yürürüz.", "Okulda koşarız.", "Okulda oynarız.")
            ),

            // 6. GÖRSEL (3) - JUMP (1. Kullanım)
            createVisual(
                id = 6,
                questionTurkish = "Zıplamak",
                imageRes = R.drawable.jump,
                correctAnswer = "jump",
                options = listOf("jump", "run", "stand", "walk")
            ),

            // 7. GÖRSEL (4) - STAND (1. Kullanım)
            createVisual(
                id = 7,
                questionTurkish = "Ayakta durmak",
                imageRes = R.drawable.stand,
                correctAnswer = "stand",
                options = listOf("stand", "sit", "look", "play")
            ),

            // 8. LISTENING (4) - DANCE Tekrar (2. Kullanım - Araya 4 soru girdi)
            createListening(
                id = 8,
                qTurkish = "They dance.",
                res = R.drawable.dance,
                ansEnglish = "Onlar dans ederler.",
                opts = listOf("Onlar dans ederler.", "Onlar şarkı söylerler.", "Onlar gülerler.")
            ),

            // 9. LISTENING (5) - JUMP Tekrar (2. Kullanım - Araya 2 soru girdi)
            createListening(
                id = 9,
                qTurkish = "Jump and play.",
                res = R.drawable.jump,
                ansEnglish = "Zıpla ve oyna.",
                opts = listOf("Koş ve oyna.", "Zıpla ve oyna.", "Zıpla ve bak.")
            ),

            // 10. GÖRSEL (5) - PLAY (1. Kullanım)
            createVisual(
                id = 10,
                questionTurkish = "Oynamak",
                imageRes = R.drawable.play,
                correctAnswer = "play",
                options = listOf("play", "eat", "sit", "stand")
            ),

            // 11. LISTENING (6) - STAND Tekrar (2. Kullanım - Araya 3 soru girdi)
            createListening(
                id = 11,
                qTurkish = "I stand up.",
                res = R.drawable.stand,
                ansEnglish = "Ayağa kalkarım.",
                opts = listOf("Ayağa kalkarım.", "Otururum.", "Yürürüm.")
            ),

            // 12. LISTENING (7) - WRITE (1. Kullanım)
            createListening(
                id = 12,
                qTurkish = "The student writes a book.",
                res = R.drawable.write,
                ansEnglish = "Öğrenci bir kitap yazar.",
                opts = listOf("Öğrenci bir kitap okur.", "Öğrenci bir kitap yazar.", "Öğrenci bir kitap bakar.")
            ),

            // 13. GÖRSEL (6) - LOOK (1. Kullanım)
            createVisual(
                id = 13,
                questionTurkish = "Bakmak",
                imageRes = R.drawable.look,
                correctAnswer = "look",
                options = listOf("look", "watch", "read", "listen")
            ),

            // 14. LISTENING (8) - DRINK (1. Kullanım)
            createListening(
                id = 14,
                qTurkish = "He drinks water.",
                res = R.drawable.drink,
                ansEnglish = "O su içer.",
                opts = listOf("O süt içer.", "O su içer.", "O süt yer.")
            ),

            // 15. EŞLEŞTİRME (1)
            createMatching(
                id = 15,
                questionText = "Son Tekrar",
                image = R.drawable.cat_logo,
                pairs = "read,write,listen,speak,open",
                turkishOptions = listOf("okumak", "yazmak", "dinlemek", "konuşmak", "açmak")
            )
        )
    }
    fun getQuizTwentyEight(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL (1) - WAKE UP (1. Kullanım)
            createVisual(
                id = 1,
                questionTurkish = "Uyanmak",
                imageRes = R.drawable.wakeup,
                correctAnswer = "wake up",
                options = listOf("wake up", "sleep", "sit", "stand")
            ),

            // 2. LISTENING (1) - WASH (1. Kullanım)
            createListening(
                id = 2,
                qTurkish = "I wash my hands.",
                res = R.drawable.wash,
                ansEnglish = "Ellerimi yıkarım.",
                opts = listOf("Ellerimi yıkarım.", "Yüzümü yıkarım.", "Ellerime bakarım.")
            ),

            // 3. GÖRSEL (2) - BRUSH (1. Kullanım)
            createVisual(
                id = 3,
                questionTurkish = "Fırçalamak",
                imageRes = R.drawable.brush,
                correctAnswer = "brush",
                options = listOf("wash", "brush", "look", "read")
            ),

            // 4. LISTENING (2) - WAKE UP Tekrar (2. Kullanım - Araya 2 soru girdi)
            createListening(
                id = 4,
                qTurkish = "He wakes up.",
                res = R.drawable.wakeup,
                ansEnglish = "O uyanır.",
                opts = listOf("O uyur.", "O uyanır.", "O oturur.")
            ),

            // 5. LISTENING (3) - GO (1. Kullanım)
            createListening(
                id = 5,
                qTurkish = "Go to school.",
                res = R.drawable.go,
                ansEnglish = "Okula git.",
                opts = listOf("Okula git.", "Okuldan gel.", "Okulda oyna.")
            ),

            // 6. GÖRSEL (3) - COME (1. Kullanım)
            createVisual(
                id = 6,
                questionTurkish = "Gelmek",
                imageRes = R.drawable.come,
                correctAnswer = "come",
                options = listOf("go", "come", "run", "walk")
            ),

            // 7. GÖRSEL (4) - WASH Tekrar (2. Kullanım - Wash my face ile değişti)
            createVisual(
                id = 7,
                questionTurkish = "Yüzümü yıkarım",
                imageRes = R.drawable.wash,
                correctAnswer = "wash my face",
                options = listOf("wash my face", "brush my teeth", "drink water", "eat apple")
            ),

            // 8. LISTENING (4) - BRUSH Tekrar (2. Kullanım)
            createListening(
                id = 8,
                qTurkish = "Brush your teeth.",
                res = R.drawable.brush,
                ansEnglish = "Dişlerini fırçala.",
                opts = listOf("Yüzünü yıka.", "Dişlerini fırçala.", "Dişlerine bak.")
            ),

            // 9. LISTENING (5) - Eski Kelime: DRINK (1. Kullanım - Come here yerine)
            createListening(
                id = 9,
                qTurkish = "I drink water.",
                res = R.drawable.drink,
                ansEnglish = "Su içerim.",
                opts = listOf("Su içerim.", "Süt içerim.", "Çay içerim.")
            ),

            // 10. GÖRSEL (5) - GO Tekrar (2. Kullanım)
            createVisual(
                id = 10,
                questionTurkish = "Gitmek",
                imageRes = R.drawable.go,
                correctAnswer = "go",
                options = listOf("go", "come", "walk", "run")
            ),

            // 11. LISTENING (6) - SLEEP (1. Kullanım)
            createListening(
                id = 11,
                qTurkish = "I sleep at home.",
                res = R.drawable.sleep,
                ansEnglish = "Evde uyurum.",
                opts = listOf("Evde uyurum.", "Evde otururum.", "Evde yerim.")
            ),

            // 12. LISTENING (7) - WATCH (1. Kullanım)
            createListening(
                id = 12,
                qTurkish = "Watch television.",
                res = R.drawable.watch,
                ansEnglish = "Televizyon izle.",
                opts = listOf("Kitap oku.", "Televizyon izle.", "Müzik dinle.")
            ),

            // 13. GÖRSEL (6) - LISTEN (1. Kullanım)
            createVisual(
                id = 13,
                questionTurkish = "Dinlemek",
                imageRes = R.drawable.listen,
                correctAnswer = "listen",
                options = listOf("listen", "speak", "read", "write")
            ),

            // 14. LISTENING (8) - RUN (1. Kullanım)
            createListening(
                id = 14,
                qTurkish = "Run in the garden.",
                res = R.drawable.run,
                ansEnglish = "Bahçede koş.",
                opts = listOf("Bahçede oyna.", "Bahçede koş.", "Bahçede yürü.")
            ),

            // 15. EŞLEŞTİRME (1)
            createMatching(
                id = 15,
                questionText = "Son Tekrar",
                image = R.drawable.cat_logo,
                pairs = "wake up,wash,brush,go,come",
                turkishOptions = listOf("uyanmak", "yıkamak", "fırçalamak", "gitmek", "gelmek")
            )
        )
    }
    fun getQuizTwentyNine(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL (1) - COME (1. Kullanım)
            createVisual(
                id = 1,
                questionTurkish = "Gelmek",
                imageRes = R.drawable.come,
                correctAnswer = "come",
                options = listOf("come", "go", "run", "walk")
            ),

            // 2. LISTENING (1) - SPEAK (1. Kullanım) - "I" yerine "We"
            createListening(
                id = 2,
                qTurkish = "We speak English.",
                res = R.drawable.speak,
                ansEnglish = "İngilizce konuşuruz.",
                opts = listOf("İngilizce konuşuruz.", "İngilizce okuruz.", "İngilizce yazarız.")
            ),

            // 3. GÖRSEL (2) - READ (1. Kullanım)
            createVisual(
                id = 3,
                questionTurkish = "Okumak",
                imageRes = R.drawable.read,
                correctAnswer = "read",
                options = listOf("read", "write", "look", "watch")
            ),

            // 4. LISTENING (2) - COME Tekrar (2. Kullanım)
            createListening(
                id = 4,
                qTurkish = "Come here, please.",
                res = R.drawable.come,
                ansEnglish = "Lütfen buraya gel.",
                opts = listOf("Lütfen oraya git.", "Lütfen buraya gel.", "Lütfen burada otur.")
            ),

            // 5. LISTENING (3) - READ Tekrar (2. Kullanım)
            createListening(
                id = 5,
                qTurkish = "Read a book.",
                res = R.drawable.book,
                ansEnglish = "Bir kitap oku.",
                opts = listOf("Bir kitap yaz.", "Bir kitap oku.", "Bir kitaba bak.")
            ),

            // 6. GÖRSEL (3) - GO (1. Kullanım)
            createVisual(
                id = 6,
                questionTurkish = "Gitmek",
                imageRes = R.drawable.go,
                correctAnswer = "go",
                options = listOf("go", "come", "run", "walk")
            ),

            // 7. GÖRSEL (4) - SING (1. Kullanım)
            createVisual(
                id = 7,
                questionTurkish = "Şarkı söylemek",
                imageRes = R.drawable.sing,
                correctAnswer = "sing",
                options = listOf("sing", "speak", "listen", "dance")
            ),

            // 8. LISTENING (4) - GO Tekrar (2. Kullanım)
            createListening(
                id = 8,
                qTurkish = "I go to school.",
                res = R.drawable.go,
                ansEnglish = "Okula giderim.",
                opts = listOf("Okula giderim.", "Okuldan gelirim.", "Okulda oynarım.")
            ),

            // 9. LISTENING (5) - SPEAK Tekrar (2. Kullanım) - "She" yerine "We"
            createListening(
                id = 9,
                qTurkish = "We speak Turkish.",
                res = R.drawable.speak,
                ansEnglish = "Türkçe konuşuruz.",
                opts = listOf("Türkçe yazarız.", "Türkçe okuruz.", "Türkçe konuşuruz.")
            ),

            // 10. GÖRSEL (5) - WATCH (1. Kullanım)
            createVisual(
                id = 10,
                questionTurkish = "İzlemek",
                imageRes = R.drawable.watch,
                correctAnswer = "watch",
                options = listOf("watch", "look", "listen", "read")
            ),

            // 11. LISTENING (6) - SING Tekrar (2. Kullanım)
            createListening(
                id = 11,
                qTurkish = "Sing a song.",
                res = R.drawable.sing,
                ansEnglish = "Bir şarkı söyle.",
                opts = listOf("Bir şarkı söyle.", "Bir şarkı dinle.", "Dans et.")
            ),

            // 12. LISTENING (7) - WATCH Tekrar (2. Kullanım) - Resim: cat
            createListening(
                id = 12,
                qTurkish = "Watch the cat.",
                res = R.drawable.cat,
                ansEnglish = "Kediyi izle.",
                opts = listOf("Kediyi izle.", "Kediye bak.", "Kediyi dinle.")
            ),

            // 13. GÖRSEL (6) - OPEN (1. Kullanım)
            createVisual(
                id = 13,
                questionTurkish = "Açmak",
                imageRes = R.drawable.open,
                correctAnswer = "open",
                options = listOf("open", "look", "play", "sit")
            ),

            // 14. LISTENING (8) - OPEN Tekrar (2. Kullanım) - Resim: book
            createListening(
                id = 14,
                qTurkish = "Open the book.",
                res = R.drawable.book,
                ansEnglish = "Kitabı aç.",
                opts = listOf("Kitabı oku.", "Kitabı aç.", "Kitabı kapat.")
            ),

            // 15. EŞLEŞTİRME (1)
            createMatching(
                id = 15,
                questionText = "Son Tekrar: Eylemler",
                image = R.drawable.cat_logo,
                pairs = "eat,drink,sleep,run,walk",
                turkishOptions = listOf("yemek", "içmek", "uyumak", "koşmak", "yürümek")
            )
        )
    }
    fun getQuizThirty(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL - WAKE UP
            createVisual(
                id = 1,
                questionTurkish = "Erken uyan",
                imageRes = R.drawable.wakeup,
                correctAnswer = "wake up early",
                options = listOf("wake up early", "sleep late", "go to school", "wash face")
            ),

            // 2. LISTENING - EAT
            createListening(
                id = 2,
                qTurkish = "The student eats a red apple.",
                res = R.drawable.eat,
                ansEnglish = "Öğrenci kırmızı bir elma yer.",
                opts = listOf("Öğrenci kırmızı bir elma yer.", "Öğrenci elma suyu içer.", "Öğrenci elma satın alır.")
            ),

            // 3. GÖRSEL - BRUSH
            createVisual(
                id = 3,
                questionTurkish = "Dişlerini fırçala",
                imageRes = R.drawable.brush,
                correctAnswer = "brush your teeth",
                options = listOf("wash your hands", "brush your teeth", "drink your milk", "read your book")
            ),

            // 4. LISTENING - DRINK
            createListening(
                id = 4,
                qTurkish = "We drink cold water every day.",
                res = R.drawable.drink,
                ansEnglish = "Her gün soğuk su içeriz.",
                opts = listOf("Her gün soğuk süt içeriz.", "Her gün soğuk su içeriz.", "Bazen soğuk su içeriz.")
            ),

            // 5. LISTENING - WAKE UP (Resim: look)
            createListening(
                id = 5,
                qTurkish = "He wakes up and looks at the cat.",
                res = R.drawable.look,
                ansEnglish = "O uyanır ve kediye bakar.",
                opts = listOf("O uyanır ve kediye bakar.", "O uyanır ve süt içer.", "O uyur ve kediye bakar.")
            ),

            // 6. GÖRSEL - READ
            createVisual(
                id = 6,
                questionTurkish = "Kitap okumak",
                imageRes = R.drawable.read,
                correctAnswer = "read a book",
                options = listOf("write a book", "read a book", "look at book", "open book")
            ),

            // 7. GÖRSEL - WASH
            createVisual(
                id = 7,
                questionTurkish = "Ellerini yıkamak",
                imageRes = R.drawable.wash,
                correctAnswer = "wash hands",
                options = listOf("wash hands", "wash face", "brush teeth", "drink water")
            ),

            // 8. LISTENING - SPEAK (Resim: kedi3)
            createListening(
                id = 8,
                qTurkish = "The teacher speaks English now.",
                res = R.drawable.kedi3,
                ansEnglish = "Öğretmen şimdi İngilizce konuşuyor.",
                opts = listOf("Öğretmen şimdi İngilizce yazıyor.", "Öğretmen şimdi İngilizce konuşuyor.", "Öğretmen İngilizce öğreniyor.")
            ),

            // 9. LISTENING - LISTEN (Müzik dinleme odaklı)
            createListening(
                id = 9,
                qTurkish = "Listen to music and dance.",
                res = R.drawable.listen,
                ansEnglish = "Müzik dinle ve dans et.",
                opts = listOf("Müzik dinle ve uyu.", "Müzik dinle ve dans et.", "Şarkı söyle ve dans et.")
            ),

            // 10. GÖRSEL - GO
            createVisual(
                id = 10,
                questionTurkish = "Okula gitmek",
                imageRes = R.drawable.go,
                correctAnswer = "go to school",
                options = listOf("go to school", "come from school", "walk to school", "run at school")
            ),

            // 11. LISTENING - WASH
            createListening(
                id = 11,
                qTurkish = "Wash your hands and eat.",
                res = R.drawable.wash,
                ansEnglish = "Ellerini yıka ve yemek ye.",
                opts = listOf("Yüzünü yıka ve yemek ye.", "Ellerini yıka ve yemek ye.", "Ellerini yıka ve su iç.")
            ),

            // 12. LISTENING - PLAY (Bebek ve oyuncak)
            createListening(
                id = 12,
                qTurkish = "The baby plays with toys.",
                res = R.drawable.play,
                ansEnglish = "Bebek oyuncaklarla oynuyor.",
                opts = listOf("Bebek oyuncaklarla oynuyor.", "Bebek bahçede koşuyor.", "Bebek süt içiyor.")
            ),

            // 13. GÖRSEL - RUN
            createVisual(
                id = 13,
                questionTurkish = "Bahçede koşmak",
                imageRes = R.drawable.run,
                correctAnswer = "run in the garden",
                options = listOf("run in the garden", "walk in the garden", "play in the garden", "jump in the garden")
            ),

            // 14. LISTENING - OPEN
            createListening(
                id = 14,
                qTurkish = "Open the window and look out.",
                res = R.drawable.open,
                ansEnglish = "Pencereyi aç ve dışarı bak.",
                opts = listOf("Kapıyı aç ve dışarı bak.", "Pencereyi aç ve dışarı bak.", "Pencereyi kapat ve bak.")
            ),

            // 15. LISTENING - SLEEP (Resim: cat)
            createListening(
                id = 15,
                qTurkish = "The cat sleeps on the book.",
                res = R.drawable.cat,
                ansEnglish = "Kedi kitabın üzerinde uyur.",
                opts = listOf("Kedi kitabın üzerinde uyur.", "Kedi kitabın yanına oturur.", "Kedi kitapla oynar.")
            ),

            // 16. GÖRSEL - DANCE
            createVisual(
                id = 16,
                questionTurkish = "Müzikle dans etmek",
                imageRes = R.drawable.dance,
                correctAnswer = "dance with music",
                options = listOf("sing with music", "dance with music", "listen to music", "play music")
            ),

            // 17. LISTENING - WRITE
            createListening(
                id = 17,
                qTurkish = "Write your name here, please.",
                res = R.drawable.write,
                ansEnglish = "Lütfen ismini buraya yaz.",
                opts = listOf("Lütfen ismini buraya yaz.", "Lütfen ismini oku.", "Lütfen buraya bak.")
            ),

            // 18. LISTENING - STAND
            createListening(
                id = 18,
                qTurkish = "Stand up and speak English.",
                res = R.drawable.stand,
                ansEnglish = "Ayağa kalk ve İngilizce konuş.",
                opts = listOf("Otur ve İngilizce konuş.", "Ayağa kalk ve İngilizce konuş.", "Ayağa kalk ve şarkı söyle.")
            ),

            // 19. LISTENING - COME (Resim: sit)
            createListening(
                id = 19,
                qTurkish = "Come here and sit down.",
                res = R.drawable.sit,
                ansEnglish = "Buraya gel ve otur.",
                opts = listOf("Buraya gel ve otur.", "Oraya git ve otur.", "Buraya gel ve bak.")
            ),

            // 20. EŞLEŞTİRME - GENEL FİNAL
            createMatching(
                id = 20,
                questionText = "Level 1 Final Eşleştirmesi",
                image = R.drawable.cat_logo,
                pairs = "wake up,wash,brush,go,come,dance",
                turkishOptions = listOf("uyanmak", "yıkamak", "fırçalamak", "gitmek", "gelmek", "dans etmek")
            )
        )
    }
    fun getQuizThirtyOne(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- İLK 5 SORU: GÖRSEL TANIMA (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - FORK
            createVisual(
                id = 1,
                questionTurkish = "çatal?",
                imageRes = R.drawable.fork,
                correctAnswer = "fork",
                options = listOf("fork", "knife", "spoon", "plate")
            ),
            // 2. GÖRSEL - KNIFE
            createVisual(
                id = 2,
                questionTurkish = "bıçak?",
                imageRes = R.drawable.knife,
                correctAnswer = "knife",
                options = listOf("knife", "spoon", "glass", "plate")
            ),
            // 3. GÖRSEL - SPOON
            createVisual(
                id = 3,
                questionTurkish = "kaşık?",
                imageRes = R.drawable.spoon,
                correctAnswer = "spoon",
                options = listOf("spoon", "fork", "plate", "glass")
            ),
            // 4. GÖRSEL - PLATE
            createVisual(
                id = 4,
                questionTurkish = "tabak?",
                imageRes = R.drawable.plate,
                correctAnswer = "plate",
                options = listOf("plate", "glass", "spoon", "knife")
            ),
            // 5. GÖRSEL - GLASS
            createVisual(
                id = 5,
                questionTurkish = "bardak?",
                imageRes = R.drawable.glass,
                correctAnswer = "glass",
                options = listOf("glass", "plate", "fork", "knife")
            ),

            // --- BASİT CÜMLELER ---
            // 6. LISTENING - EAT & PLATE
            createListening(
                id = 6,
                qTurkish = "Eat on the plate.",
                res = R.drawable.eat,
                ansEnglish = "Tabakta yemek ye.",
                opts = listOf("Tabakta yemek ye.", "Tabakta meyve ye.", "Tabağı mutfağa götür.")
            ),

            // 7. LISTENING - GIVE & FORK
            createListening(
                id = 7,
                qTurkish = "Give me the fork.",
                res = R.drawable.fork,
                ansEnglish = "Bana çatalı ver.",
                opts = listOf("Bana çatalı ver.", "Çatalı bana göster.", "Çatalı hemen bul.")
            ),

            // 8. LISTENING - DRINK & GLASS
            createListening(
                id = 8,
                qTurkish = "Drink from the glass.",
                res = R.drawable.drink,
                ansEnglish = "Bardaktan iç.",
                opts = listOf("Bardaktan iç.", "Bardağa bak.", "Bardağı yıka.")
            ),

            // 9. LISTENING - LOOK & KNIFE
            createListening(
                id = 9,
                qTurkish = "Look at the knife.",
                res = R.drawable.look,
                ansEnglish = "Bıçağa bak.",
                opts = listOf("Bıçağa bak.", "Bıçağı kullan.", "Bıçağı bana ver.")
            ),

            // 10. LISTENING - GIVE & SPOON
            createListening(
                id = 10,
                qTurkish = "Give me a spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Bana bir kaşık ver.",
                opts = listOf("Bana bir kaşık yıka.", "Bana bir kaşık ver.", "Kaşığı masaya koy.")
            ),

            // 11. LISTENING - RUN & KITCHEN
            createListening(
                id = 11,
                qTurkish = "Run to the kitchen.",
                res = R.drawable.run,
                ansEnglish = "Mutfağa koş.",
                opts = listOf("Mutfağa koş.", "Mutfakta oyna.", "Mutfaktan gel.")
            ),

            // 12. LISTENING - WASH
            createListening(
                id = 12,
                qTurkish = "Wash your hands.",
                res = R.drawable.wash,
                ansEnglish = "Ellerini yıka.",
                opts = listOf("Yüzünü yıka.", "Ellerini yıka.", "Ellerini kurula.")
            ),

            // 13. GÖRSEL - KNIFE (TR Soru - EN Cevap)
            createVisual(
                id = 13,
                questionTurkish = "bıçak?",
                imageRes = R.drawable.knife,
                correctAnswer = "knife",
                options = listOf("knife", "spoon", "fork", "plate")
            ),

            // 14. LISTENING - BASİT ÇEVİRİ
            createListening(
                id = 14,
                qTurkish = "A big spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Büyük bir kaşık.",
                opts = listOf("Küçük bir kaşık.", "Büyük bir kaşık.", "Büyük bir çatal.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Eşyaları",
                image = R.drawable.cat_logo,
                pairs = "fork,knife,spoon,plate,glass",
                turkishOptions = listOf("çatal", "bıçak", "kaşık", "tabak", "bardak")
            )
        )
    }
    fun getQuizThirtyTwo(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- GÖRSEL TANIMA (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - PLATE
            createVisual(
                id = 1,
                questionTurkish = "tabak?",
                imageRes = R.drawable.plate,
                correctAnswer = "plate",
                options = listOf("plate", "glass", "spoon", "knife")
            ),
            // 2. GÖRSEL - SPOON
            createVisual(
                id = 2,
                questionTurkish = "kaşık?",
                imageRes = R.drawable.spoon,
                correctAnswer = "spoon",
                options = listOf("spoon", "fork", "plate", "knife")
            ),
            // 3. GÖRSEL - GLASS
            createVisual(
                id = 3,
                questionTurkish = "bardak?",
                imageRes = R.drawable.glass,
                correctAnswer = "glass",
                options = listOf("glass", "plate", "fork", "knife")
            ),
            // 4. GÖRSEL - FORK
            createVisual(
                id = 4,
                questionTurkish = "çatal?",
                imageRes = R.drawable.fork,
                correctAnswer = "fork",
                options = listOf("fork", "knife", "spoon", "glass")
            ),
            // 5. GÖRSEL - KNIFE
            createVisual(
                id = 5,
                questionTurkish = "bıçak?",
                imageRes = R.drawable.knife,
                correctAnswer = "knife",
                options = listOf("knife", "fork", "glass", "plate")
            ),

            // --- LISTENING ODAKLI FİİL ÖĞRETİMİ ---
            // 6. LISTENING - BRING & GLASS
            createListening(
                id = 6,
                qTurkish = "Bring the glass.",
                res = R.drawable.glass,
                ansEnglish = "Bardağı getir.",
                opts = listOf("Bardağı getir.", "Bardağı kır.", "Bardağı sakla.")
            ),

            // 7. LISTENING - TAKE & KNIFE
            createListening(
                id = 7,
                qTurkish = "Take the knife.",
                res = R.drawable.knife,
                ansEnglish = "Bıçağı al.",
                opts = listOf("Bıçağı fırlat.", "Bıçağı al.", "Bıçağı yıka.")
            ),

            // 8. LISTENING - BRING & MILK
            createListening(
                id = 8,
                qTurkish = "Bring the milk.",
                res = R.drawable.milk,
                ansEnglish = "Sütü getir.",
                opts = listOf("Sütü getir.", "Sütü iç.", "Sütü dök.")
            ),

            // 9. LISTENING - PUT & SPOON
            createListening(
                id = 9,
                qTurkish = "Put the spoon on the plate.",
                res = R.drawable.spoon,
                ansEnglish = "Kaşığı tabağa koy.",
                opts = listOf("Kaşığı tabağa koy.", "Kaşığı tabaktan al.", "Kaşığı tabağa fırlat.")
            ),

            // 10. LISTENING - TAKE & PLATE
            createListening(
                id = 10,
                qTurkish = "Take the plate.",
                res = R.drawable.plate,
                ansEnglish = "Tabağı al.",
                opts = listOf("Tabağı al.", "Tabağa bak.", "Tabağı kır.")
            ),

            // 11. LISTENING - BRING & FORK
            createListening(
                id = 11,
                qTurkish = "Bring the fork, please.",
                res = R.drawable.fork,
                ansEnglish = "Lütfen çatalı getir.",
                opts = listOf("Lütfen çatalı yıka.", "Lütfen çatalı getir.", "Lütfen çatalı bırak.")
            ),

            // 12. LISTENING - PUT & GLASS
            createListening(
                id = 12,
                qTurkish = "Put the glass here.",
                res = R.drawable.glass,
                ansEnglish = "Bardağı buraya koy.",
                opts = listOf("Bardağı buraya koy.", "Bardağı buraya getir.", "Bardağı buradan al.")
            ),

            // 13. LISTENING - GIVE & MILK
            createListening(
                id = 13,
                qTurkish = "Give me the milk.",
                res = R.drawable.milk,
                ansEnglish = "Bana sütü ver.",
                opts = listOf("Bana sütü ver.", "Sütü bana göster.", "Sütü dolaba koy.")
            ),

            // 14. LISTENING - TAKE & GLASS
            createListening(
                id = 14,
                qTurkish = "Take a glass of water.",
                res = R.drawable.glass,
                ansEnglish = "Bir bardak su al.",
                opts = listOf("Bir bardak su iç.", "Bir bardak su al.", "Bardağa su doldur.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak & Eylemler",
                image = R.drawable.cat_logo,
                pairs = "bring,put,take,plate,glass",
                turkishOptions = listOf("getirmek", "koymak", "almak", "tabak", "bardak")
            )
        )
    }
    fun getQuizThirtyThree(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- YENİ KELİME TANITIMI (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - PAN
            createVisual(
                id = 1,
                questionTurkish = "tava?",
                imageRes = R.drawable.pan,
                correctAnswer = "pan",
                options = listOf("pan", "plate", "bowl", "spoon")
            ),
            // 2. GÖRSEL - BOWL
            createVisual(
                id = 2,
                questionTurkish = "kase?",
                imageRes = R.drawable.bowl,
                correctAnswer = "bowl",
                options = listOf("bowl", "plate", "glass", "pan")
            ),

            // --- LISTENING ODAKLI PEKİŞTİRME ---
            // 3. LISTENING - TAKE & PAN
            createListening(
                id = 3,
                qTurkish = "Take the pan.",
                res = R.drawable.pan,
                ansEnglish = "Tavayı al.",
                opts = listOf("Tavayı al.", "Tavayı yıka.", "Tavayı getir.")
            ),

            // 4. LISTENING - BRING & BOWL
            createListening(
                id = 4,
                qTurkish = "Bring a bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Bir kase getir.",
                opts = listOf("Bir kase getir.", "Bir kase al.", "Bir kase koy.")
            ),

            // 5. LISTENING - PUT & PAN
            createListening(
                id = 5,
                qTurkish = "Put the pan here.",
                res = R.drawable.pan,
                ansEnglish = "Tavayı buraya koy.",
                opts = listOf("Tavayı buraya koy.", "Tavayı oradan al.", "Tavayı mutfağa götür.")
            ),

            // 6. GÖRSEL - FORK (TR Soru - EN Cevap)
            createVisual(
                id = 6,
                questionTurkish = "çatal?",
                imageRes = R.drawable.fork,
                correctAnswer = "fork",
                options = listOf("fork", "knife", "pan", "bowl")
            ),

            // 7. LISTENING - GIVE & BOWL
            createListening(
                id = 7,
                qTurkish = "Give me the bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Bana kaseyi ver.",
                opts = listOf("Bana kaseyi ver.", "Kaseyi masaya koy.", "Kaseyi mutfakta yıka.")
            ),

            // 8. LISTENING - TAKE & BOWL
            createListening(
                id = 8,
                qTurkish = "Take the bowl and eat.",
                res = R.drawable.bowl,
                ansEnglish = "Kaseyi al ve yemek ye.",
                opts = listOf("Kaseyi al ve yemek ye.", "Kaseyi getir ve bak.", "Kaseyi masaya koy.")
            ),

            // 9. LISTENING - BRING & PAN
            createListening(
                id = 9,
                qTurkish = "Bring the pan, please.",
                res = R.drawable.pan,
                ansEnglish = "Lütfen tavayı getir.",
                opts = listOf("Lütfen tavayı yıka.", "Lütfen tavayı getir.", "Lütfen tavayı al.")
            ),

            // 10. LISTENING - PUT & BOWL
            createListening(
                id = 10,
                qTurkish = "Put the bowl on the table.",
                res = R.drawable.bowl,
                ansEnglish = "Kaseyi masanın üzerine koy.",
                opts = listOf("Kaseyi masanın üzerine koy.", "Kaseyi masadan al.", "Kaseye süt koy.")
            ),

            // 11. GÖRSEL - KNIFE (TR Soru - EN Cevap)
            createVisual(
                id = 11,
                questionTurkish = "bıçak?",
                imageRes = R.drawable.knife,
                correctAnswer = "knife",
                options = listOf("knife", "pan", "bowl", "plate")
            ),

            // 12. LISTENING - WASH & PAN
            createListening(
                id = 12,
                qTurkish = "Wash the pan in the kitchen.",
                res = R.drawable.pan,
                ansEnglish = "Tavayı mutfakta yıka.",
                opts = listOf("Tavayı mutfakta yıka.", "Tavayı mutfağa götür.", "Tavayı mutfakta ara.")
            ),

            // 13. LISTENING - LOOK & BOWL
            createListening(
                id = 13,
                qTurkish = "Look at the bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Kaseye bak.",
                opts = listOf("Kaseye bak.", "Kaseyi al.", "Kaseyi getir.")
            ),

            // 14. LISTENING - BRING & WATER
            createListening(
                id = 14,
                qTurkish = "Bring the water and a glass.",
                res = R.drawable.glass,
                ansEnglish = "Suyu ve bir bardağı getir.",
                opts = listOf("Suyu ve bir bardağı getir.", "Suyu bardağa doldur.", "Bardaktan su iç.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Yeni Mutfak Kelimeleri",
                image = R.drawable.cat_logo,
                pairs = "pan,bowl,bring,put,take",
                turkishOptions = listOf("tava", "kase", "getirmek", "koymak", "almak")
            )
        )
    }
    fun getQuizThirtyFour(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. LISTENING - GIVE & PAN
            createListening(
                id = 1,
                qTurkish = "Give me the pan.",
                res = R.drawable.pan,
                ansEnglish = "Bana tavayı ver.",
                opts = listOf("Bana tavayı ver.", "Tavayı masaya koy.", "Tavayı mutfakta yıka.")
            ),

            // 2. GÖRSEL - BOWL (TR Soru - EN Cevap)
            createVisual(
                id = 2,
                questionTurkish = "kase?",
                imageRes = R.drawable.bowl,
                correctAnswer = "bowl",
                options = listOf("bowl", "plate", "pan", "glass")
            ),

            // 3. LISTENING - TAKE & GLASS
            createListening(
                id = 3,
                qTurkish = "Take a glass of water.",
                res = R.drawable.glass,
                ansEnglish = "Bir bardak su al.",
                opts = listOf("Bir bardak su iç.", "Bir bardak su al.", "Bardağı masaya getir.")
            ),

            // 4. LISTENING - BRING & FORK
            createListening(
                id = 4,
                qTurkish = "Bring the fork and knife.",
                res = R.drawable.fork,
                ansEnglish = "Çatalı ve bıçağı getir.",
                opts = listOf("Çatalı ve bıçağa bak.", "Çatalı ve bıçağı getir.", "Çatalı ve bıçağı yıka.")
            ),

            // 5. LISTENING - PUT & PLATE
            createListening(
                id = 5,
                qTurkish = "Put the apple on the plate.",
                res = R.drawable.plate,
                ansEnglish = "Elmayı tabağa koy.",
                opts = listOf("Elmayı tabağa koy.", "Elmayı tabaktan al.", "Elmayı tabağa getir.")
            ),

            // 6. GÖRSEL - SPOON (TR Soru - EN Cevap)
            createVisual(
                id = 6,
                questionTurkish = "kaşık?",
                imageRes = R.drawable.spoon,
                correctAnswer = "spoon",
                options = listOf("spoon", "fork", "knife", "bowl")
            ),

            // 7. LISTENING - TAKE & BOWL
            createListening(
                id = 7,
                qTurkish = "Take the bowl to the kitchen.",
                res = R.drawable.bowl,
                ansEnglish = "Kaseyi mutfağa götür.",
                opts = listOf("Kaseyi mutfağa götür.", "Kaseyi mutfakta yıka.", "Kaseyi mutfaktan getir.")
            ),

            // 8. LISTENING - GIVE & SPOON
            createListening(
                id = 8,
                qTurkish = "Give me a big spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Bana büyük bir kaşık ver.",
                opts = listOf("Bana büyük bir kaşık ver.", "Bana büyük bir bıçak ver.", "Büyük kaşığı tabağa koy.")
            ),

            // 9. LISTENING - BRING & PAN
            createListening(
                id = 9,
                qTurkish = "Bring the pan here.",
                res = R.drawable.pan,
                ansEnglish = "Tavayı buraya getir.",
                opts = listOf("Tavayı buraya getir.", "Tavayı buraya koy.", "Tavayı buradan al.")
            ),

            // 10. GÖRSEL - KNIFE (TR Soru - EN Cevap)
            createVisual(
                id = 10,
                questionTurkish = "bıçak?",
                imageRes = R.drawable.knife,
                correctAnswer = "knife",
                options = listOf("knife", "fork", "spoon", "pan")
            ),

            // 11. LISTENING - PUT & GLASS
            createListening(
                id = 11,
                qTurkish = "Put the glass on the table.",
                res = R.drawable.glass,
                ansEnglish = "Bardağı masanın üzerine koy.",
                opts = listOf("Bardağı masanın üzerine koy.", "Bardağı masadan al.", "Bardağı mutfağa getir.")
            ),

            // 12. LISTENING - WASH & PLATE
            createListening(
                id = 12,
                qTurkish = "Wash the plate and bowl.",
                res = R.drawable.plate,
                ansEnglish = "Tabağı ve kaseyi yıka.",
                opts = listOf("Tabağı ve kaseyi yıka.", "Tabağı ve kaseyi getir.", "Tabağı ve kaseyi al.")
            ),

            // 13. LISTENING - LOOK & FORK
            createListening(
                id = 13,
                qTurkish = "Look at the fork.",
                res = R.drawable.fork,
                ansEnglish = "Çatala bak.",
                opts = listOf("Çatala bak.", "Çatalı al.", "Çatalı getir.")
            ),

            // 14. LISTENING - KARMA PEKİŞTİRME
            createListening(
                id = 14,
                qTurkish = "Bring the milk and a glass.",
                res = R.drawable.milk,
                ansEnglish = "Sütü ve bir bardağı getir.",
                opts = listOf("Sütü ve bir bardağı getir.", "Sütü bardağa doldur.", "Bardaktan süt iç.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Ürünleri",
                image = R.drawable.cat_logo,
                pairs = "fork,knife,spoon,plate,glass,pan,bowl",
                turkishOptions = listOf("çatal", "bıçak", "kaşık", "tabak", "bardak", "tava", "kase")
            )
        )
    }
    fun getQuizThirtyFive(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- YENİ KELİME TANITIMI (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - POT
            createVisual(
                id = 1,
                questionTurkish = "tencere?",
                imageRes = R.drawable.pot,
                correctAnswer = "pot",
                options = listOf("pot", "pan", "plate", "bowl")
            ),
            // 2. GÖRSEL - CUP
            createVisual(
                id = 2,
                questionTurkish = "fincan?",
                imageRes = R.drawable.cup,
                correctAnswer = "cup",
                options = listOf("cup", "glass", "bowl", "plate")
            ),
            // 3. GÖRSEL - NAPKIN
            createVisual(
                id = 3,
                questionTurkish = "peçete?",
                imageRes = R.drawable.napkin,
                correctAnswer = "napkin",
                options = listOf("napkin", "spoon", "fork", "knife")
            ),

            // --- LISTENING ODAKLI PEKİŞTİRME ---
            // 4. LISTENING - BRING & POT
            createListening(
                id = 4,
                qTurkish = "Bring the big pot.",
                res = R.drawable.pot,
                ansEnglish = "Büyük tencereyi getir.",
                opts = listOf("Büyük tencereyi getir.", "Büyük tencereyi al.", "Büyük tencereyi yıka.")
            ),

            // 5. LISTENING - TAKE & CUP
            createListening(
                id = 5,
                qTurkish = "Take a cup of tea.",
                res = R.drawable.cup,
                ansEnglish = "Bir fincan çay al.",
                opts = listOf("Bir fincan çay iç.", "Bir fincan çay al.", "Bir fincan çay yap.")
            ),

            // 6. LISTENING - GIVE & NAPKIN
            createListening(
                id = 6,
                qTurkish = "Give me a napkin, please.",
                res = R.drawable.napkin,
                ansEnglish = "Lütfen bana bir peçete ver.",
                opts = listOf("Lütfen bana bir peçete ver.", "Lütfen peçeteyi çöpe at.", "Lütfen bir peçete al.")
            ),

            // 7. LISTENING - PUT & POT
            createListening(
                id = 7,
                qTurkish = "Put the pot on the table.",
                res = R.drawable.pot,
                ansEnglish = "Tencereyi masaya koy.",
                opts = listOf("Tencereyi masaya koy.", "Tencereyi masadan al.", "Tencereyi ocağa koy.")
            ),

            // 8. LISTENING - BRING & CUP
            createListening(
                id = 8,
                qTurkish = "Bring two cups.",
                res = R.drawable.cup,
                ansEnglish = "İki fincan getir.",
                opts = listOf("İki fincan getir.", "İki fincan al.", "İki fincanı yıka.")
            ),

            // 9. LISTENING - TAKE & NAPKIN
            createListening(
                id = 9,
                qTurkish = "Take the napkin.",
                res = R.drawable.napkin,
                ansEnglish = "Peçeteyi al.",
                opts = listOf("Peçeteyi al.", "Peçeteyi ver.", "Peçeteye bak.")
            ),

            // 10. LISTENING - WASH & POT
            createListening(
                id = 10,
                qTurkish = "Wash the pot.",
                res = R.drawable.pot,
                ansEnglish = "Tencereyi yıka.",
                opts = listOf("Tencereyi yıka.", "Tencereyi getir.", "Tencereyi al.")
            ),

            // 11. GÖRSEL - PAN (TR Soru - EN Cevap)
            createVisual(
                id = 11,
                questionTurkish = "tava?",
                imageRes = R.drawable.pan,
                correctAnswer = "pan",
                options = listOf("pan", "pot", "bowl", "napkin")
            ),

            // 12. LISTENING - GIVE & CUP
            createListening(
                id = 12,
                qTurkish = "Give the cup to me.",
                res = R.drawable.cup,
                ansEnglish = "Fincanı bana ver.",
                opts = listOf("Fincanı bana ver.", "Fincanı ona ver.", "Fincanı masaya koy.")
            ),

            // 13. LISTENING - PUT & NAPKIN
            createListening(
                id = 13,
                qTurkish = "Put the napkin on the plate.",
                res = R.drawable.napkin,
                ansEnglish = "Peçeteyi tabağa koy.",
                opts = listOf("Peçeteyi tabağa koy.", "Peçeteyi tabaktan al.", "Peçeteyi tabağın yanına koy.")
            ),

            // 14. LISTENING - BRING & MILK
            createListening(
                id = 14,
                qTurkish = "Bring the milk and a cup.",
                res = R.drawable.milk,
                ansEnglish = "Sütü ve bir fincanı getir.",
                opts = listOf("Sütü ve bir fincanı getir.", "Sütü fincuna doldur.", "Fincandan süt iç.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Son Mutfak Kelimeleri",
                image = R.drawable.cat_logo,
                pairs = "pot,cup,napkin,pan,bowl",
                turkishOptions = listOf("tencere", "fincan", "peçete", "tava", "kase")
            )
        )
    }
    fun getQuizThirtySix(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. LISTENING - BRING & POT
            createListening(
                id = 1,
                qTurkish = "Bring the pot.",
                res = R.drawable.pot,
                ansEnglish = "Tencereyi getir.",
                opts = listOf("Tencereyi getir.", "Tencereyi al.", "Tencereyi yıka.")
            ),

            // 2. LISTENING - TAKE & NAPKIN
            createListening(
                id = 2,
                qTurkish = "Take a napkin.",
                res = R.drawable.napkin,
                ansEnglish = "Bir peçete al.",
                opts = listOf("Bir peçete ver.", "Bir peçete al.", "Peçeteye bak.")
            ),

            // 3. GÖRSEL - CUP (TR Soru - EN Cevap)
            createVisual(
                id = 3,
                questionTurkish = "fincan?",
                imageRes = R.drawable.cup,
                correctAnswer = "cup",
                options = listOf("cup", "glass", "bowl", "plate")
            ),

            // 4. LISTENING - PUT & BOWL
            createListening(
                id = 4,
                qTurkish = "Put the spoon in the bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Kaşığı kasenin içine koy.",
                opts = listOf("Kaşığı kaseden al.", "Kaşığı kasenin içine koy.", "Kaşığı kasenin yanına getir.")
            ),

            // 5. LISTENING - GIVE & PAN
            createListening(
                id = 5,
                qTurkish = "Give me the pan.",
                res = R.drawable.pan,
                ansEnglish = "Bana tavayı ver.",
                opts = listOf("Bana tavayı ver.", "Tavayı ocağa koy.", "Tavayı mutfağa götür.")
            ),

            // 6. LISTENING - BRING & CUP
            createListening(
                id = 6,
                qTurkish = "Bring a cup of tea.",
                res = R.drawable.cup,
                ansEnglish = "Bir fincan çay getir.",
                opts = listOf("Bir fincan çay iç.", "Bir fincan çay getir.", "Bir fincan çay yap.")
            ),

            // 7. GÖRSEL - POT (TR Soru - EN Cevap)
            createVisual(
                id = 7,
                questionTurkish = "tencere?",
                imageRes = R.drawable.pot,
                correctAnswer = "pot",
                options = listOf("pot", "pan", "plate", "bowl")
            ),

            // 8. LISTENING - TAKE & FORK
            createListening(
                id = 8,
                qTurkish = "Take the fork and knife.",
                res = R.drawable.fork,
                ansEnglish = "Çatalı ve bıçağı al.",
                opts = listOf("Çatalı ve bıçağı getir.", "Çatalı ve bıçağı yıka.", "Çatalı ve bıçağı al.")
            ),

            // 9. LISTENING - PUT & NAPKIN
            createListening(
                id = 9,
                qTurkish = "Put the napkin on the plate.",
                res = R.drawable.napkin,
                ansEnglish = "Peçeteyi tabağa koy.",
                opts = listOf("Peçeteyi tabağa koy.", "Peçeteyi tabaktan al.", "Peçeteyi masaya getir.")
            ),

            // 10. LISTENING - GIVE & GLASS
            createListening(
                id = 10,
                qTurkish = "Give me a glass of water.",
                res = R.drawable.glass,
                ansEnglish = "Bana bir bardak su ver.",
                opts = listOf("Bana bir bardak su ver.", "Bir bardak su iç.", "Bardağa su doldur.")
            ),

            // 11. GÖRSEL - PAN (TR Soru - EN Cevap)
            createVisual(
                id = 11,
                questionTurkish = "tava?",
                imageRes = R.drawable.pan,
                correctAnswer = "pan",
                options = listOf("pan", "pot", "plate", "bowl")
            ),

            // 12. LISTENING - BRING & PLATE
            createListening(
                id = 12,
                qTurkish = "Bring the clean plate.",
                res = R.drawable.plate,
                ansEnglish = "Temiz tabağı getir.",
                opts = listOf("Temiz tabağı getir.", "Tabağı masaya koy.", "Tabağı mutfakta yıka.")
            ),

            // 13. LISTENING - WASH & CUP
            createListening(
                id = 13,
                qTurkish = "Wash the cup.",
                res = R.drawable.cup,
                ansEnglish = "Fincanı yıka.",
                opts = listOf("Fincanı yıka.", "Fincanı getir.", "Fincanı masaya koy.")
            ),

            // 14. LISTENING - LOOK & BOWL
            createListening(
                id = 14,
                qTurkish = "Look at the bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Kaseye bak.",
                opts = listOf("Kaseye bak.", "Kaseyi al.", "Kaseyi mutfağa götür.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Nesneleri",
                image = R.drawable.cat_logo,
                pairs = "pot,cup,napkin,fork,spoon",
                turkishOptions = listOf("tencere", "fincan", "peçete", "çatal", "kaşık")
            )
        )
    }
    fun getQuizThirtySeven(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- YENİ KELİME TANITIMI (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - REFRIGERATOR
            createVisual(
                id = 1,
                questionTurkish = "buzdolabı?",
                imageRes = R.drawable.refrigerator,
                correctAnswer = "refrigerator",
                options = listOf("refrigerator", "oven", "sink", "pot")
            ),
            // 2. GÖRSEL - OVEN
            createVisual(
                id = 2,
                questionTurkish = "fırın?",
                imageRes = R.drawable.oven,
                correctAnswer = "oven",
                options = listOf("oven", "refrigerator", "pan", "plate")
            ),
            // 3. GÖRSEL - SINK
            createVisual(
                id = 3,
                questionTurkish = "lavabo?",
                imageRes = R.drawable.sink,
                correctAnswer = "sink",
                options = listOf("sink", "oven", "cup", "napkin")
            ),

            // --- LISTENING ODAKLI PEKİŞTİRME ---
            // 4. LISTENING - OPEN & REFRIGERATOR
            createListening(
                id = 4,
                qTurkish = "Open the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını aç.",
                opts = listOf("Buzdolabını aç.", "Buzdolabını kapat.", "Buzdolabını temizle.")
            ),

            // 5. LISTENING - LOOK & OVEN
            createListening(
                id = 5,
                qTurkish = "Look at the oven.",
                res = R.drawable.oven,
                ansEnglish = "Fırına bak.",
                opts = listOf("Fırına bak.", "Fırını aç.", "Fırını temizle.")
            ),

            // 6. LISTENING - WASH & SINK
            createListening(
                id = 6,
                qTurkish = "Wash the plate in the sink.",
                res = R.drawable.sink,
                ansEnglish = "Tabağı lavaboda yıka.",
                opts = listOf("Tabağı lavaboda yıka.", "Tabağı lavaboya koy.", "Tabağı lavabodan al.")
            ),

            // 7. LISTENING - PUT & REFRIGERATOR
            createListening(
                id = 7,
                qTurkish = "Put the milk in the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Sütü buzdolabına koy.",
                opts = listOf("Sütü buzdolabına koy.", "Sütü buzdolabından al.", "Sütü buzdolabına getir.")
            ),

            // 8. LISTENING - TAKE & OVEN
            createListening(
                id = 8,
                qTurkish = "Take the pan from the oven.",
                res = R.drawable.oven,
                ansEnglish = "Tavayı fırından al.",
                opts = listOf("Tavayı fırına koy.", "Tavayı fırından al.", "Tavayı fırına getir.")
            ),

            // 9. LISTENING - BRING & NAPKIN
            createListening(
                id = 9,
                qTurkish = "Bring a napkin from the kitchen.",
                res = R.drawable.napkin,
                ansEnglish = "Mutfaktan bir peçete getir.",
                opts = listOf("Mutfaktan bir peçete getir.", "Mutfaktaki peçeteyi al.", "Peçeteyi mutfağa koy.")
            ),

            // 10. LISTENING - GIVE & CUP
            createListening(
                id = 10,
                qTurkish = "Give me a cup of water.",
                res = R.drawable.cup,
                ansEnglish = "Bana bir fincan su ver.",
                opts = listOf("Bana bir fincan su ver.", "Fincana su doldur.", "Bir fincan su iç.")
            ),

            // 11. GÖRSEL - POT (TR Soru - EN Cevap)
            createVisual(
                id = 11,
                questionTurkish = "tencere?",
                imageRes = R.drawable.pot,
                correctAnswer = "pot",
                options = listOf("pot", "oven", "sink", "refrigerator")
            ),

            // 12. LISTENING - GO & SINK
            createListening(
                id = 12,
                qTurkish = "Go to the sink and wash your hands.",
                res = R.drawable.sink,
                ansEnglish = "Lavaboya git ve ellerini yıka.",
                opts = listOf("Lavaboya git ve ellerini yıka.", "Lavaboda tabağı yıka.", "Lavabodan su al.")
            ),

            // 13. LISTENING - PUT & OVEN
            createListening(
                id = 13,
                qTurkish = "Put the chicken in the oven.",
                res = R.drawable.oven,
                ansEnglish = "Tavuğu fırına koy.",
                opts = listOf("Tavuğu fırına koy.", "Tavuğu fırından al.", "Tavuğu fırında pişir.")
            ),

            // 14. LISTENING - OPEN & REFRIGERATOR
            createListening(
                id = 14,
                qTurkish = "Open the refrigerator and take the milk.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını aç ve sütü al.",
                opts = listOf("Buzdolabını aç ve sütü al.", "Buzdolabını kapat ve sütü koy.", "Buzdolabına bak ve sütü getir.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Üniteleri",
                image = R.drawable.cat_logo,
                pairs = "refrigerator,oven,sink,pot,pan",
                turkishOptions = listOf("buzdolabı", "fırın", "lavabo", "tencere", "tava")
            )
        )
    }
    fun getQuizThirtyEight(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. LISTENING - CLEAN & PLATE
            createListening(
                id = 1,
                qTurkish = "Clean the plate.",
                res = R.drawable.plate,
                ansEnglish = "Tabağı temizle.",
                opts = listOf("Tabağı temizle.", "Tabağı kirlet.", "Tabağı kır.")
            ),

            // 2. LISTENING - FILL & GLASS
            createListening(
                id = 2,
                qTurkish = "Fill the glass.",
                res = R.drawable.glass,
                ansEnglish = "Bardağı doldur.",
                opts = listOf("Bardağı boşalt.", "Bardağı doldur.", "Bardağı getir.")
            ),

            // 3. LISTENING - SHOW & POT
            createListening(
                id = 3,
                qTurkish = "Show me the pot.",
                res = R.drawable.pot,
                ansEnglish = "Bana tencereyi göster.",
                opts = listOf("Bana tencereyi ver.", "Bana tencereyi göster.", "Tencereye bak.")
            ),

            // 4. LISTENING - CLEAN & REFRIGERATOR
            createListening(
                id = 4,
                qTurkish = "Clean the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını temizle.",
                opts = listOf("Buzdolabını aç.", "Buzdolabını temizle.", "Buzdolabını kapat.")
            ),

            // 5. GÖRSEL - SINK (TR Soru - EN Cevap)
            createVisual(
                id = 5,
                questionTurkish = "lavabo?",
                imageRes = R.drawable.sink,
                correctAnswer = "sink",
                options = listOf("sink", "oven", "pan", "cup")
            ),

            // 6. LISTENING - FILL & BOWL
            createListening(
                id = 6,
                qTurkish = "Fill the bowl with milk.",
                res = R.drawable.bowl,
                ansEnglish = "Kaseyi sütle doldur.",
                opts = listOf("Kaseyi sütle doldur.", "Kaseye süt dök.", "Kaseden süt iç.")
            ),

            // 7. LISTENING - SHOW & KNIFE
            createListening(
                id = 7,
                qTurkish = "Show the knife to me.",
                res = R.drawable.knife,
                ansEnglish = "Bıçağı bana göster.",
                opts = listOf("Bıçağı bana ver.", "Bıçağı bana göster.", "Bıçağı oraya koy.")
            ),

            // 8. LISTENING - CLEAN & SINK
            createListening(
                id = 8,
                qTurkish = "Clean the sink, please.",
                res = R.drawable.sink,
                ansEnglish = "Lütfen lavaboyu temizle.",
                opts = listOf("Lütfen lavaboda yıka.", "Lütfen lavaboyu temizle.", "Lütfen lavaboya git.")
            ),

            // 9. LISTENING - FILL & CUP
            createListening(
                id = 9,
                qTurkish = "Fill the cup.",
                res = R.drawable.cup,
                ansEnglish = "Fincanı doldur.",
                opts = listOf("Fincanı doldur.", "Fincanı al.", "Fincanı yıka.")
            ),

            // 10. GÖRSEL - OVEN (TR Soru - EN Cevap)
            createVisual(
                id = 10,
                questionTurkish = "fırın?",
                imageRes = R.drawable.oven,
                correctAnswer = "oven",
                options = listOf("oven", "refrigerator", "pot", "plate")
            ),

            // 11. LISTENING - SHOW & NAPKIN
            createListening(
                id = 11,
                qTurkish = "Show me the napkin.",
                res = R.drawable.napkin,
                ansEnglish = "Bana peçeteyi göster.",
                opts = listOf("Bana peçeteyi ver.", "Bana peçeteyi göster.", "Peçeteyi al.")
            ),

            // 12. LISTENING - CLEAN & PAN
            createListening(
                id = 12,
                qTurkish = "Clean the pan in the kitchen.",
                res = R.drawable.pan,
                ansEnglish = "Tavayı mutfakta temizle.",
                opts = listOf("Tavayı mutfakta temizle.", "Tavayı mutfaktan getir.", "Tavayı mutfağa koy.")
            ),

            // 13. LISTENING - FILL & POT
            createListening(
                id = 13,
                qTurkish = "Fill the pot with water.",
                res = R.drawable.pot,
                ansEnglish = "Tencereyi suyla doldur.",
                opts = listOf("Tencereyi suyla doldur.", "Tencereye su getir.", "Tencereden su iç.")
            ),

            // 14. LISTENING - SHOW & SPOON
            createListening(
                id = 14,
                qTurkish = "Show the spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Kaşığı göster.",
                opts = listOf("Kaşığı göster.", "Kaşığı al.", "Kaşığı ver.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Eylemler ve Mutfak",
                image = R.drawable.cat_logo,
                pairs = "clean,fill,show,refrigerator,oven",
                turkishOptions = listOf("temizlemek", "doldurmak", "göstermek", "buzdolabı", "fırın")
            )
        )
    }
    fun getQuizThirtyNine(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. LISTENING - OPEN & REFRIGERATOR
            createListening(
                id = 1,
                qTurkish = "Open the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını aç.",
                opts = listOf("Buzdolabını aç.", "Buzdolabını kapat.", "Buzdolabına bak.")
            ),

            // 2. LISTENING - CLOSE & OVEN
            createListening(
                id = 2,
                qTurkish = "Close the oven.",
                res = R.drawable.oven,
                ansEnglish = "Fırını kapat.",
                opts = listOf("Fırını aç.", "Fırını kapat.", "Fırını temizle.")
            ),

            // 3. LISTENING - FIND & FORK
            createListening(
                id = 3,
                qTurkish = "Find the fork.",
                res = R.drawable.fork,
                ansEnglish = "Çatalı bul.",
                opts = listOf("Çatalı al.", "Çatalı getir.", "Çatalı bul.")
            ),

            // 4. LISTENING - OPEN & OVEN
            createListening(
                id = 4,
                qTurkish = "Open the oven, please.",
                res = R.drawable.oven,
                ansEnglish = "Lütfen fırını aç.",
                opts = listOf("Lütfen fırını aç.", "Lütfen fırını kapat.", "Lütfen fırına bak.")
            ),

            // 5. GÖRSEL - PAN (TR Soru - EN Cevap)
            createVisual(
                id = 5,
                questionTurkish = "tava?",
                imageRes = R.drawable.pan,
                correctAnswer = "pan",
                options = listOf("pan", "pot", "bowl", "plate")
            ),

            // 6. LISTENING - CLOSE & REFRIGERATOR
            createListening(
                id = 6,
                qTurkish = "Close the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını kapat.",
                opts = listOf("Buzdolabını kapat.", "Buzdolabını aç.", "Buzdolabını temizle.")
            ),

            // 7. LISTENING - FIND & KNIFE
            createListening(
                id = 7,
                qTurkish = "Find the knife in the kitchen.",
                res = R.drawable.knife,
                ansEnglish = "Mutfaktaki bıçağı bul.",
                opts = listOf("Mutfaktaki bıçağı bul.", "Mutfaktaki bıçağı yıka.", "Mutfaktaki bıçağı getir.")
            ),

            // 8. LISTENING - OPEN & MILK
            createListening(
                id = 8,
                qTurkish = "Open the refrigerator and take the milk.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını aç ve sütü al.",
                opts = listOf("Buzdolabını aç ve sütü al.", "Buzdolabını kapat ve sütü koy.", "Buzdolabına bak ve sütü ver.")
            ),

            // 9. LISTENING - CLOSE & POT
            createListening(
                id = 9,
                qTurkish = "Close the pot.",
                res = R.drawable.pot,
                ansEnglish = "Tencereyi kapat.",
                opts = listOf("Tencereyi aç.", "Tencereyi kapat.", "Tencereyi yıka.")
            ),

            // 10. GÖRSEL - NAPKIN (TR Soru - EN Cevap)
            createVisual(
                id = 10,
                questionTurkish = "peçete?",
                imageRes = R.drawable.napkin,
                correctAnswer = "napkin",
                options = listOf("napkin", "fork", "spoon", "cup")
            ),

            // 11. LISTENING - FIND & SPOON
            createListening(
                id = 11,
                qTurkish = "Find a big spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Büyük bir kaşık bul.",
                opts = listOf("Büyük bir kaşık al.", "Büyük bir kaşık bul.", "Büyük bir kaşık ver.")
            ),

            // 12. LISTENING - CLEAN & SINK
            createListening(
                id = 12,
                qTurkish = "Clean the sink.",
                res = R.drawable.sink,
                ansEnglish = "Lavaboyu temizle.",
                opts = listOf("Lavaboyu temizle.", "Lavaboda yıka.", "Lavaboya bak.")
            ),

            // 13. LISTENING - FILL & GLASS
            createListening(
                id = 13,
                qTurkish = "Fill the glass with water.",
                res = R.drawable.glass,
                ansEnglish = "Bardağı suyla doldur.",
                opts = listOf("Bardağı suyla doldur.", "Bardaktan su iç.", "Bardağı masaya koy.")
            ),

            // 14. LISTENING - SHOW & BOWL
            createListening(
                id = 14,
                qTurkish = "Show me the bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Bana kaseyi göster.",
                opts = listOf("Bana kaseyi göster.", "Kaseyi masaya koy.", "Kaseyi bana ver.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Eylemler ve Mutfak",
                image = R.drawable.cat_logo,
                pairs = "open,close,find,pot,pan",
                turkishOptions = listOf("açmak", "kapatmak", "bulmak", "tencere", "tava")
            )
        )
    }
    fun getQuizForty(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL TANITIM (TR Soru - EN Cevap)
            createVisual(
                id = 1,
                questionTurkish = "pişirmek?",
                imageRes = R.drawable.cook,
                correctAnswer = "cook",
                options = listOf("cook", "clean", "fill", "open")
            ),

            // 2. LISTENING - COOK & POT
            createListening(
                id = 2,
                qTurkish = "Cook the chicken in the pot.",
                res = R.drawable.pot,
                ansEnglish = "Tavuğu tencerede pişir.",
                opts = listOf("Tavuğu tencerede pişir.", "Tavuğu tencereye koy.", "Tavuğu tencereden al.")
            ),

            // 3. LISTENING - CLEAN & OVEN
            createListening(
                id = 3,
                qTurkish = "Clean the oven.",
                res = R.drawable.oven,
                ansEnglish = "Fırını temizle.",
                opts = listOf("Fırını temizle.", "Fırını aç.", "Fırını kapat.")
            ),

            // 4. LISTENING - FILL & CUP
            createListening(
                id = 4,
                qTurkish = "Fill the cup with tea.",
                res = R.drawable.cup,
                ansEnglish = "Fincanı çayla doldur.",
                opts = listOf("Fincanı çayla doldur.", "Fincandan çay iç.", "Fincanı masaya getir.")
            ),

            // 5. LISTENING - SHOW & REFRIGERATOR
            createListening(
                id = 5,
                qTurkish = "Show me the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Bana buzdolabını göster.",
                opts = listOf("Bana buzdolabını göster.", "Buzdolabını aç.", "Buzdolabını kapat.")
            ),

            // 6. LISTENING - OPEN & PAN
            createListening(
                id = 6,
                qTurkish = "Open the pan and look.",
                res = R.drawable.pan,
                ansEnglish = "Tavayı aç ve bak.",
                opts = listOf("Tavayı aç ve bak.", "Tavayı al ve git.", "Tavayı temizle ve koy.")
            ),

            // 7. LISTENING - CLOSE & REFRIGERATOR
            createListening(
                id = 7,
                qTurkish = "Close the refrigerator, please.",
                res = R.drawable.refrigerator,
                ansEnglish = "Lütfen buzdolabını kapat.",
                opts = listOf("Lütfen buzdolabını kapat.", "Lütfen buzdolabını aç.", "Lütfen buzdolabını temizle.")
            ),

            // 8. LISTENING - FIND & SPOON
            createListening(
                id = 8,
                qTurkish = "Find a small spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Küçük bir kaşık bul.",
                opts = listOf("Küçük bir kaşık bul.", "Küçük bir kaşık al.", "Küçük bir kaşık ver.")
            ),

            // 9. LISTENING - COOK & PAN
            createListening(
                id = 9,
                qTurkish = "Cook the egg in the pan.",
                res = R.drawable.pan,
                ansEnglish = "Yumurtayı tavada pişir.",
                opts = listOf("Yumurtayı tavada pişir.", "Yumurtayı tavaya koy.", "Yumurtayı tavadan al.")
            ),

            // 10. LISTENING - CLEAN & SINK
            createListening(
                id = 10,
                qTurkish = "Clean the sink after cooking.",
                res = R.drawable.sink,
                ansEnglish = "Pişirdikten sonra lavaboyu temizle.",
                opts = listOf("Pişirdikten sonra lavaboyu temizle.", "Lavaboda tabağı yıka.", "Lavaboya su doldur.")
            ),

            // 11. LISTENING - FILL & POT
            createListening(
                id = 11,
                qTurkish = "Fill the pot with water and cook.",
                res = R.drawable.pot,
                ansEnglish = "Tencereyi suyla doldur ve pişir.",
                opts = listOf("Tencereyi suyla doldur ve pişir.", "Tencereye su koy ve bak.", "Tenceredeki suyu boşalt.")
            ),

            // 12. LISTENING - SHOW & NAPKIN
            createListening(
                id = 12,
                qTurkish = "Show the napkin to me.",
                res = R.drawable.napkin,
                ansEnglish = "Peçeteyi bana göster.",
                opts = listOf("Peçeteyi bana göster.", "Peçeteyi bana ver.", "Peçeteyi tabağa koy.")
            ),

            // 13. LISTENING - FIND & PLATE
            createListening(
                id = 13,
                qTurkish = "Find a clean plate.",
                res = R.drawable.plate,
                ansEnglish = "Temiz bir tabak bul.",
                opts = listOf("Temiz bir tabak bul.", "Tabağı temizle.", "Tabağı mutfağa getir.")
            ),

            // 14. LISTENING - CLOSE & OVEN
            createListening(
                id = 14,
                qTurkish = "Close the oven now.",
                res = R.drawable.oven,
                ansEnglish = "Fırını şimdi kapat.",
                opts = listOf("Fırını şimdi kapat.", "Fırını şimdi aç.", "Fırını şimdi temizle.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Eylemleri",
                image = R.drawable.cat_logo,
                pairs = "cook,clean,fill,open,close",
                turkishOptions = listOf("pişirmek", "temizlemek", "doldurmak", "açmak", "kapatmak")
            )
        )
    }
    fun getQuizFortyOne(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- YENİ KELİME TANITIMI (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - BOIL
            createVisual(
                id = 1,
                questionTurkish = "kaynamak?",
                imageRes = R.drawable.boil,
                correctAnswer = "boil",
                options = listOf("boil", "cut", "stir", "mash")
            ),
            // 2. GÖRSEL - CUT
            createVisual(
                id = 2,
                questionTurkish = "kesmek?",
                imageRes = R.drawable.cut,
                correctAnswer = "cut",
                options = listOf("cut", "mash", "boil", "wash")
            ),
            // 3. GÖRSEL - STIR
            createVisual(
                id = 3,
                questionTurkish = "karıştırmak?",
                imageRes = R.drawable.stir,
                correctAnswer = "stir",
                options = listOf("stir", "boil", "fill", "open")
            ),
            // 4. GÖRSEL - MASH
            createVisual(
                id = 4,
                questionTurkish = "ezmek?",
                imageRes = R.drawable.mash,
                correctAnswer = "mash",
                options = listOf("mash", "cut", "cook", "close")
            ),

            // --- LISTENING ODAKLI PEKİŞTİRME ---
            // 5. LISTENING - BOIL & POT
            createListening(
                id = 5,
                qTurkish = "Boil the water in the pot.",
                res = R.drawable.pot,
                ansEnglish = "Suyu tencerede kaynat.",
                opts = listOf("Suyu tencerede kaynat.", "Suyu tencereye doldur.", "Suyu tencereden al.")
            ),

            // 6. LISTENING - CUT & KNIFE
            createListening(
                id = 6,
                qTurkish = "Cut the bread with the knife.",
                res = R.drawable.knife,
                ansEnglish = "Ekmeği bıçakla kes.",
                opts = listOf("Ekmeği bıçakla kes.", "Ekmeği bıçakla karıştır.", "Ekmeği bıçakla ez.")
            ),

            // 7. LISTENING - STIR & SPOON
            createListening(
                id = 7,
                qTurkish = "Stir the soup with a spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Çorbayı bir kaşıkla karıştır.",
                opts = listOf("Çorbayı bir kaşıkla karıştır.", "Çorbayı bir kaşıkla iç.", "Çorbayı kaşığa koy.")
            ),

            // 8. LISTENING - MASH & BOWL
            createListening(
                id = 8,
                qTurkish = "Mash the potato in the bowl.",
                res = R.drawable.bowl,
                ansEnglish = "Patatesi kasede ez.",
                opts = listOf("Patatesi kasede ez.", "Patatesi kasede yıka.", "Patatesi kaseye koy.")
            ),

            // 9. LISTENING - STIR & POT
            createListening(
                id = 9,
                qTurkish = "Stir the food in the pot.",
                res = R.drawable.pot,
                ansEnglish = "Tenceredeki yemeği karıştır.",
                opts = listOf("Tenceredeki yemeği karıştır.", "Tenceredeki yemeği ye.", "Tencereye yemek koy.")
            ),

            // 10. LISTENING - CUT & PLATE
            createListening(
                id = 10,
                qTurkish = "Cut the meat on the plate.",
                res = R.drawable.plate,
                ansEnglish = "Eti tabağın üzerinde kes.",
                opts = listOf("Eti tabağın üzerinde kes.", "Eti tabağa koy.", "Eti tabağa getir.")
            ),

            // 11. LISTENING - BOIL & CUP
            createListening(
                id = 11,
                qTurkish = "Fill the cup and boil.",
                res = R.drawable.cup,
                ansEnglish = "Fincanı doldur ve kaynat.",
                opts = listOf("Fincanı doldur ve kaynat.", "Fincanı yıka ve kurula.", "Fincanı masaya koy.")
            ),

            // 12. LISTENING - MASH & FORK
            createListening(
                id = 12,
                qTurkish = "Mash it with a fork.",
                res = R.drawable.fork,
                ansEnglish = "Onu bir çatalla ez.",
                opts = listOf("Onu bir çatalla ez.", "Onu bir çatalla al.", "Onu bir çatalla göster.")
            ),

            // 13. LISTENING - COOK & STIR
            createListening(
                id = 13,
                qTurkish = "Cook and stir.",
                res = R.drawable.cook,
                ansEnglish = "Pişir ve karıştır.",
                opts = listOf("Pişir ve karıştır.", "Kes ve temizle.", "Kaynat ve doldur.")
            ),

            // 14. LISTENING - FIND & BOIL
            createListening(
                id = 14,
                qTurkish = "Find the boiling water.",
                res = R.drawable.boil,
                ansEnglish = "Kaynayan suyu bul.",
                opts = listOf("Kaynayan suyu bul.", "Kaynayan suyu al.", "Kaynayan suyu getir.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Pişirme Teknikleri",
                image = R.drawable.cat_logo,
                pairs = "boil,cut,stir,mash,cook",
                turkishOptions = listOf("kaynamak", "kesmek", "karıştırmak", "ezmek", "pişirmek")
            )
        )
    }
    fun getQuizFortyTwo(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. LISTENING - BOIL & POT
            createListening(
                id = 1,
                qTurkish = "Boil the water in the pot.",
                res = R.drawable.pot,
                ansEnglish = "Suyu tencerede kaynat.",
                opts = listOf("Suyu tencerede kaynat.", "Suyu tencerede karıştır.", "Suyu tencereye doldur.")
            ),

            // 2. LISTENING - STIR & SPOON
            createListening(
                id = 2,
                qTurkish = "Stir the soup with a spoon.",
                res = R.drawable.spoon,
                ansEnglish = "Çorbayı bir kaşıkla karıştır.",
                opts = listOf("Çorbayı bir kaşıkla karıştır.", "Çorbayı bir kaşıkla iç.", "Çorbayı kaşıkla ez.")
            ),

            // 3. LISTENING - MASH & FORK
            createListening(
                id = 3,
                qTurkish = "Mash the potato with a fork.",
                res = R.drawable.fork,
                ansEnglish = "Patatesi bir çatalla ez.",
                opts = listOf("Patatesi bir çatalla ez.", "Patatesi bir çatalla kes.", "Patatesi bir çatalla al.")
            ),

            // 4. LISTENING - CUT & KNIFE
            createListening(
                id = 4,
                qTurkish = "Cut the meat with a knife.",
                res = R.drawable.knife,
                ansEnglish = "Eti bir bıçakla kes.",
                opts = listOf("Eti bir bıçakla kes.", "Eti bir bıçakla temizle.", "Eti bir bıçakla pişir.")
            ),

            // 5. GÖRSEL - REFRIGERATOR (TR Soru - EN Cevap)
            createVisual(
                id = 5,
                questionTurkish = "buzdolabı?",
                imageRes = R.drawable.refrigerator,
                correctAnswer = "refrigerator",
                options = listOf("refrigerator", "oven", "sink", "pot")
            ),

            // 6. LISTENING - OPEN & CLOSE
            createListening(
                id = 6,
                qTurkish = "Open the refrigerator and close the oven.",
                res = R.drawable.oven,
                ansEnglish = "Buzdolabını aç ve fırını kapat.",
                opts = listOf("Buzdolabını aç ve fırını kapat.", "Buzdolabını kapat ve fırını aç.", "Buzdolabına bak ve fırını temizle.")
            ),

            // 7. LISTENING - FILL & GLASS
            createListening(
                id = 7,
                qTurkish = "Fill the glass with water.",
                res = R.drawable.glass,
                ansEnglish = "Bardağı suyla doldur.",
                opts = listOf("Bardağı suyla doldur.", "Bardağı lavaboda yıka.", "Bardağı masadan al.")
            ),

            // 8. LISTENING - CLEAN & SINK
            createListening(
                id = 8,
                qTurkish = "Clean the sink.",
                res = R.drawable.sink,
                ansEnglish = "Lavaboyu temizle.",
                opts = listOf("Lavaboyu temizle.", "Lavaboda yıka.", "Lavaboya bak.")
            ),

            // 9. LISTENING - FIND & NAPKIN
            createListening(
                id = 9,
                qTurkish = "Find a napkin.",
                res = R.drawable.napkin,
                ansEnglish = "Bir peçete bul.",
                opts = listOf("Bir peçete bul.", "Bir peçete ver.", "Bir peçete al.")
            ),

            // 10. LISTENING - SHOW & PLATE
            createListening(
                id = 10,
                qTurkish = "Show me the plate.",
                res = R.drawable.plate,
                ansEnglish = "Bana tabağı göster.",
                opts = listOf("Bana tabağı göster.", "Tabağı masaya koy.", "Tabağı bana ver.")
            ),

            // 11. GÖRSEL - COOK (TR Soru - EN Cevap)
            createVisual(
                id = 11,
                questionTurkish = "pişirmek?",
                imageRes = R.drawable.cook,
                correctAnswer = "cook",
                options = listOf("cook", "boil", "stir", "chop")
            ),

            // 12. LISTENING - BRING & CUP
            createListening(
                id = 12,
                qTurkish = "Bring a cup of tea.",
                res = R.drawable.cup,
                ansEnglish = "Bir fincan çay getir.",
                opts = listOf("Bir fincan çay getir.", "Bir fincan çay iç.", "Bir fincan çay yap.")
            ),

            // 13. LISTENING - TAKE & PAN
            createListening(
                id = 13,
                qTurkish = "Take the pan from the oven.",
                res = R.drawable.oven,
                ansEnglish = "Tavayı fırından al.",
                opts = listOf("Tavayı fırından al.", "Tavayı fırına koy.", "Tavayı fırında temizle.")
            ),

            // 14. LISTENING - MIX RECAP
            createListening(
                id = 14,
                qTurkish = "Chop, stir and cook.",
                res = R.drawable.cook,
                ansEnglish = "Doğra, karıştır ve pişir.",
                opts = listOf("Doğra, karıştır ve pişir.", "Kes, kaynat ve ez.", "Yıka, doldur ve temizle.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Ustası",
                image = R.drawable.cat_logo,
                pairs = "boil,stir,mash,sink,refrigerator",
                turkishOptions = listOf("kaynatmak", "karıştırmak", "ezmek", "lavabo", "buzdolabı")
            )
        )
    }
    fun getQuizFortyThree(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- YENİ KELİME TANITIMI (SORU TR - CEVAP EN) ---
            // 1. GÖRSEL - TEA
            createVisual(
                id = 1,
                questionTurkish = "çay?",
                imageRes = R.drawable.tea,
                correctAnswer = "tea",
                options = listOf("tea", "cup", "water", "milk")
            ),
            // 2. GÖRSEL - TABLE
            createVisual(
                id = 2,
                questionTurkish = "masa?",
                imageRes = R.drawable.table,
                correctAnswer = "table",
                options = listOf("table", "chair", "cupboard", "plate")
            ),
            // 3. GÖRSEL - CHAIR
            createVisual(
                id = 3,
                questionTurkish = "sandalye?",
                imageRes = R.drawable.chair,
                correctAnswer = "chair",
                options = listOf("chair", "table", "oven", "sink")
            ),
            // 4. GÖRSEL - CUPBOARD
            createVisual(
                id = 4,
                questionTurkish = "dolap?",
                imageRes = R.drawable.cupboard,
                correctAnswer = "cupboard",
                options = listOf("cupboard", "fridge", "table", "bowl")
            ),

            // --- LISTENING ODAKLI PEKİŞTİRME ---
            // 5. LISTENING - OPEN & CUPBOARD
            createListening(
                id = 5,
                qTurkish = "Open the cupboard.",
                res = R.drawable.cupboard,
                ansEnglish = "Dolabı aç.",
                opts = listOf("Dolabı aç.", "Dolabı kapat.", "Dolabı temizle.")
            ),

            // 6. LISTENING - PUT & TABLE
            createListening(
                id = 6,
                qTurkish = "Put the glass on the table.",
                res = R.drawable.table,
                ansEnglish = "Bardağı masanın üzerine koy.",
                opts = listOf("Bardağı masanın üzerine koy.", "Bardağı masadan al.", "Bardağı masaya getir.")
            ),

            // 7. LISTENING - FIND & CHAIR
            createListening(
                id = 7,
                qTurkish = "Find the chair.",
                res = R.drawable.chair,
                ansEnglish = "Sandalyeyi bul.",
                opts = listOf("Sandalyeye bak.", "Sandalyeyi bul.", "Sandalyeyi getir.")
            ),

            // 8. LISTENING - DRINK & TEA
            createListening(
                id = 8,
                qTurkish = "Drink the hot tea.",
                res = R.drawable.tea,
                ansEnglish = "Sıcak çayı iç.",
                opts = listOf("Sıcak çayı iç.", "Sıcak çayı dök.", "Sıcak çay yap.")
            ),

            // 9. LISTENING - CLOSE & CUPBOARD
            createListening(
                id = 9,
                qTurkish = "Close the cupboard, please.",
                res = R.drawable.cupboard,
                ansEnglish = "Lütfen dolabı kapat.",
                opts = listOf("Lütfen dolabı kapat.", "Lütfen dolabı aç.", "Lütfen dolabı temizle.")
            ),

            // 10. LISTENING - STIR & TEA
            createListening(
                id = 10,
                qTurkish = "Stir your tea.",
                res = R.drawable.tea,
                ansEnglish = "Çayını karıştır.",
                opts = listOf("Çayını iç.", "Çayını karıştır.", "Çayını dök.")
            ),

            // 11. LISTENING - CLEAN & TABLE
            createListening(
                id = 11,
                qTurkish = "Clean the table after breakfast.",
                res = R.drawable.table,
                ansEnglish = "Kahvaltıdan sonra masayı temizle.",
                opts = listOf("Kahvaltıdan sonra masayı temizle.", "Masayı mutfağa götür.", "Masaya tabak koy.")
            ),

            // 12. LISTENING - BRING & CHAIR
            createListening(
                id = 12,
                qTurkish = "Bring a chair from the kitchen.",
                res = R.drawable.chair,
                ansEnglish = "Mutfaktan bir sandalye getir.",
                opts = listOf("Mutfaktan bir sandalye getir.", "Sandalyeyi mutfağa götür.", "Sandalyeye mutfakta bak.")
            ),

            // 13. LISTENING - FILL & CUP
            createListening(
                id = 13,
                qTurkish = "Fill the cup with tea.",
                res = R.drawable.tea,
                ansEnglish = "Fincanı çayla doldur.",
                opts = listOf("Fincanı çayla doldur.", "Fincandan çay iç.", "Fincana çay koy.")
            ),

            // 14. LISTENING - SHOW & CUPBOARD
            createListening(
                id = 14,
                qTurkish = "Show me the cupboard.",
                res = R.drawable.cupboard,
                ansEnglish = "Bana dolabı göster.",
                opts = listOf("Bana dolabı göster.", "Dolabı aç.", "Dolabı kapat.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Ev Mobilyaları",
                image = R.drawable.cat_logo,
                pairs = "tea,table,chair,cupboard,sink",
                turkishOptions = listOf("çay", "masa", "sandalye", "dolap", "lavabo")
            )
        )
    }
    fun getQuizFortyFour(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- GÖRSEL TANIMA (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - KETTLE
            createVisual(
                id = 1,
                questionTurkish = "su ısıtıcısı?",
                imageRes = R.drawable.kettle,
                correctAnswer = "kettle",
                options = listOf("kettle", "toaster", "pot", "sink")
            ),
            // 2. GÖRSEL - TOASTER
            createVisual(
                id = 2,
                questionTurkish = "ekmek kızartma makinesi?",
                imageRes = R.drawable.toaster,
                correctAnswer = "toaster",
                options = listOf("toaster", "oven", "refrigerator", "tray")
            ),
            // 3. GÖRSEL - TRAY
            createVisual(
                id = 3,
                questionTurkish = "tepsi?",
                imageRes = R.drawable.tray,
                correctAnswer = "tray",
                options = listOf("tray", "plate", "bowl", "pan")
            ),

            // --- DOLAYLI ANLATIMLI LISTENING PEKİŞTİRME ---
            // 4. LISTENING - KETTLE & WATER (Dolaylı: Water/Fill)
            createListening(
                id = 4,
                qTurkish = "Fill the kettle with water.",
                res = R.drawable.kettle,
                ansEnglish = "Su ısıtıcısını suyla doldur.",
                opts = listOf("Su ısıtıcısını suyla doldur.", "Su ısıtıcısını mutfağa götür.", "Su ısıtıcısını temizle.")
            ),

            // 5. LISTENING - TOASTER & BREAD (Dolaylı: Bread/Hot)
            createListening(
                id = 5,
                qTurkish = "Put the bread in the toaster.",
                res = R.drawable.toaster,
                ansEnglish = "Ekmeği kızartma makinesine koy.",
                opts = listOf("Ekmeği kızartma makinesinden al.", "Ekmeği kızartma makinesine koy.", "Ekmeği tabağa koy.")
            ),

            // 6. LISTENING - TRAY & TEA (Dolaylı: Tea/Bring)
            createListening(
                id = 6,
                qTurkish = "Bring the tea on the tray.",
                res = R.drawable.tray,
                ansEnglish = "Çayı tepside getir.",
                opts = listOf("Çayı tepside getir.", "Çayı mutfakta iç.", "Çayı tepsiye dök.")
            ),

            // 7. LISTENING - KETTLE & HOT (Dolaylı: Hot/Look)
            createListening(
                id = 7,
                qTurkish = "The kettle is very hot.",
                res = R.drawable.kettle,
                ansEnglish = "Su ısıtıcısı çok sıcak.",
                opts = listOf("Su ısıtıcısı çok sıcak.", "Su ısıtıcısı çok soğuk.", "Su ısıtıcısı çok büyük.")
            ),

            // 8. GÖRSEL - GRATER (TR Soru - EN Cevap)
            createVisual(
                id = 8,
                questionTurkish = "rende?",
                imageRes = R.drawable.grater,
                correctAnswer = "grater",
                options = listOf("grater", "peeler", "knife", "fork")
            ),

            // 9. LISTENING - PEELER & APPLE (Dolaylı: Apple/Take)
            createListening(
                id = 9,
                qTurkish = "Take the peeler for the apple.",
                res = R.drawable.peeler,
                ansEnglish = "Elma için soyacağı al.",
                opts = listOf("Elma için soyacağı al.", "Elmayı soyacakla yıka.", "Elmayı tabağa koy.")
            ),

            // 10. LISTENING - TRAY & GIVE (Dolaylı: Give)
            createListening(
                id = 10,
                qTurkish = "Give me the big tray.",
                res = R.drawable.tray,
                ansEnglish = "Bana büyük tepsiyi ver.",
                opts = listOf("Bana büyük tepsiyi ver.", "Tepsiyi mutfağa götür.", "Tepsiyi masaya koy.")
            ),

            // 11. GÖRSEL - PEELER (TR Soru - EN Cevap)
            createVisual(
                id = 11,
                questionTurkish = "soyacak?",
                imageRes = R.drawable.peeler,
                correctAnswer = "peeler",
                options = listOf("peeler", "grater", "spoon", "knife")
            ),

            // 12. LISTENING - GRATER & CLEAN (Dolaylı: Clean)
            createListening(
                id = 12,
                qTurkish = "Clean the grater now.",
                res = R.drawable.grater,
                ansEnglish = "Rendeyi şimdi temizle.",
                opts = listOf("Rendeyi şimdi temizle.", "Rendeyi masaya koy.", "Rendeyi mutfaktan getir.")
            ),

            // 13. LISTENING - TOASTER & COLD (Dolaylı: Cold/Look)
            createListening(
                id = 13,
                qTurkish = "The toaster is cold.",
                res = R.drawable.toaster,
                ansEnglish = "Ekmek kızartma makinesi soğuk.",
                opts = listOf("Ekmek kızartma makinesi soğuk.", "Ekmek kızartma makinesi sıcak.", "Ekmek kızartma makinesi yeni.")
            ),

            // 14. LISTENING - KETTLE & TEA (Dolaylı: Tea/Bring)
            createListening(
                id = 14,
                qTurkish = "Bring the kettle for the tea.",
                res = R.drawable.kettle,
                ansEnglish = "Çay için su ısıtıcısını getir.",
                opts = listOf("Çay için su ısıtıcısını getir.", "Su ısıtıcısına çay doldur.", "Su ısıtıcısını yıka.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Modern Mutfak",
                image = R.drawable.cat_logo,
                pairs = "kettle,toaster,tray,grater,peeler",
                turkishOptions = listOf("su ısıtıcısı", "kızartma makinesi", "tepsi", "rende", "soyacak")
            )
        )
    }
    fun getQuizFortyFive(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // --- GÖRSEL TANIMA (TR Soru - EN Cevap) ---
            // 1. GÖRSEL - REFRIGERATOR
            createVisual(
                id = 1,
                questionTurkish = "buzdolabı?",
                imageRes = R.drawable.refrigerator,
                correctAnswer = "refrigerator",
                options = listOf("refrigerator", "oven", "sink", "dishwasher")
            ),
            // 2. GÖRSEL - DISHWASHER
            createVisual(
                id = 2,
                questionTurkish = "bulaşık makinesi?",
                imageRes = R.drawable.dishwasher,
                correctAnswer = "dishwasher",
                options = listOf("dishwasher", "washing machine", "oven", "kettle")
            ),
            // 3. GÖRSEL - POUR (Dökmek)
            createVisual(
                id = 3,
                questionTurkish = "dökmek?",
                imageRes = R.drawable.pour,
                correctAnswer = "pour",
                options = listOf("pour", "fill", "wash", "open")
            ),
            // 4. GÖRSEL - APPLE (Dry Yerine Eski Kelime Tekrarı)
            createVisual(
                id = 4,
                questionTurkish = "elma?",
                imageRes = R.drawable.apple,
                correctAnswer = "apple",
                options = listOf("apple", "bread", "milk", "sugar")
            ),
            // 5. GÖRSEL - OVEN
            createVisual(
                id = 5,
                questionTurkish = "firın?",
                imageRes = R.drawable.oven,
                correctAnswer = "oven",
                options = listOf("oven", "toaster", "kettle", "sink")
            ),

            // --- DOLAYLI ANLATIM & TEKRAR ODAKLI LISTENING ---
            // 6. LISTENING - OPEN & REFRIGERATOR
            createListening(
                id = 6,
                qTurkish = "Open the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını aç.",
                opts = listOf("Buzdolabını aç.", "Buzdolabını kapat.", "Buzdolabını temizle.")
            ),

            // 7. LISTENING - CLOSE & OVEN
            createListening(
                id = 7,
                qTurkish = "Close the oven, it is hot.",
                res = R.drawable.oven,
                ansEnglish = "Fırını kapat, o sıcak.",
                opts = listOf("Fırını kapat, o sıcak.", "Fırını aç, o sıcak.", "Fırını temizle, o soğuk.")
            ),

            // 8. LISTENING - POUR & GLASS (R.drawable.pour Güncellemesi)
            createListening(
                id = 8,
                qTurkish = "Pour the water into the glass.",
                res = R.drawable.pour,
                ansEnglish = "Suyu bardağa dök.",
                opts = listOf("Suyu bardağa dök.", "Suyu bardaktan iç.", "Bardağı suyla doldur.")
            ),

            // 9. LISTENING - WASH & PLATE (R.drawable.wash Güncellemesi)
            createListening(
                id = 9,
                qTurkish = "Wash the plate in the sink.",
                res = R.drawable.wash,
                ansEnglish = "Tabağı lavaboda yıka.",
                opts = listOf("Tabağı lavaboda yıka.", "Tabağı masaya koy.", "Tabağı kurula.")
            ),

            // 10. LISTENING - DISHWASHER & CLEAN
            createListening(
                id = 10,
                qTurkish = "The dishwasher is clean.",
                res = R.drawable.dishwasher,
                ansEnglish = "Bulaşık makinesi temiz.",
                opts = listOf("Bulaşık makinesi temiz.", "Bulaşık makinesi kirli.", "Bulaşık makinesi yeni.")
            ),

            // 11. LISTENING - APPLE & REFRIGERATOR (Eski Kelime Tekrarı)
            createListening(
                id = 11,
                qTurkish = "The apple is in the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Elma buzdolabının içinde.",
                opts = listOf("Elma buzdolabının içinde.", "Elmayı buzdolabından al.", "Elmayı masaya koy.")
            ),

            // 12. LISTENING - FILL & SINK
            createListening(
                id = 12,
                qTurkish = "Fill the sink with water.",
                res = R.drawable.sink,
                ansEnglish = "Lavaboyu suyla doldur.",
                opts = listOf("Lavaboyu suyla doldur.", "Lavaboyu temizle.", "Lavaboyu kurula.")
            ),

            // 13. LISTENING - POUR & CUP
            createListening(
                id = 13,
                qTurkish = "Pour the tea into the cup.",
                res = R.drawable.pour,
                ansEnglish = "Çayı fincana dök.",
                opts = listOf("Çayı fincana dök.", "Çayı fincanda getir.", "Fincanı çayla doldur.")
            ),

            // 14. LISTENING - CLOSE & DOOR
            createListening(
                id = 14,
                qTurkish = "Close the kitchen door.",
                res = R.drawable.door,
                ansEnglish = "Mutfak kapısını kapat.",
                opts = listOf("Mutfak kapısını kapat.", "Mutfak kapısını aç.", "Mutfak kapısına bak.")
            ),

            // 15. EŞLEŞTİRME
            createMatching(
                id = 15,
                questionText = "Mutfak Düzeni",
                image = R.drawable.cat_logo,
                pairs = "open,close,pour,wash,apple",
                turkishOptions = listOf("açmak", "kapatmak", "dökmek", "yıkamak", "elma")
            )
        )
    }
    fun getQuizFortySix(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL - REFRIGERATOR
            createVisual(
                id = 1,
                questionTurkish = "buzdolabı?",
                imageRes = R.drawable.refrigerator,
                correctAnswer = "refrigerator",
                options = listOf("refrigerator", "oven", "sink", "dishwasher")
            ),

            // 2. LISTENING - BOIL & KETTLE
            createListening(
                id = 2,
                qTurkish = "Boil the water in the kettle.",
                res = R.drawable.kettle,
                ansEnglish = "Suyu su ısıtıcısında kaynat.",
                opts = listOf("Suyu su ısıtıcısında kaynat.", "Suyu bardağa dök.", "Su ısıtıcısını yıka.")
            ),

            // 3. GÖRSEL - PEELER
            createVisual(
                id = 3,
                questionTurkish = "soyacak?",
                imageRes = R.drawable.peeler,
                correctAnswer = "peeler",
                options = listOf("peeler", "grater", "knife", "spoon")
            ),

            // 4. LISTENING - PEEL & APPLE
            createListening(
                id = 4,
                qTurkish = "Peel the apple with the peeler.",
                res = R.drawable.peel,
                ansEnglish = "Elmayı soyacak ile soy.",
                opts = listOf("Elmayı soyacak ile soy.", "Elmayı rende ile rendele.", "Elmayı tabağa koy.")
            ),

            // 5. GÖRSEL - DISHWASHER
            createVisual(
                id = 5,
                questionTurkish = "bulaşık makinesi?",
                imageRes = R.drawable.dishwasher,
                correctAnswer = "dishwasher",
                options = listOf("dishwasher", "washing machine", "oven", "sink")
            ),

            // 6. LISTENING - WASH & DISHWASHER
            createListening(
                id = 6,
                qTurkish = "Wash the plates in the dishwasher.",
                res = R.drawable.dishwasher,
                ansEnglish = "Tabakları bulaşık makinesinde yıka.",
                opts = listOf("Tabakları bulaşık makinesinde yıka.", "Tabakları lavaboda kurula.", "Tabakları fırına koy.")
            ),

            // 7. GÖRSEL - GRATER
            createVisual(
                id = 7,
                questionTurkish = "rende?",
                imageRes = R.drawable.grater,
                correctAnswer = "grater",
                options = listOf("grater", "peeler", "pan", "pot")
            ),

            // 8. LISTENING - GRATE & CHEESE
            createListening(
                id = 8,
                qTurkish = "Grate the cheese for the pizza.",
                res = R.drawable.grate,
                ansEnglish = "Pizza için peyniri rendele.",
                opts = listOf("Pizza için peyniri rendele.", "Pizza için peyniri soy.", "Peyniri buzdolabına koy.")
            ),

            // 9. GÖRSEL - TRAY
            createVisual(
                id = 9,
                questionTurkish = "tepsi?",
                imageRes = R.drawable.tray,
                correctAnswer = "tray",
                options = listOf("tray", "plate", "bowl", "cup")
            ),

            // 10. LISTENING - CARRY & TRAY
            createListening(
                id = 10,
                qTurkish = "Carry the tray to the kitchen.",
                res = R.drawable.tray,
                ansEnglish = "Tepsiyi mutfağa taşı.",
                opts = listOf("Tepsiyi mutfağa taşı.", "Tepsiyi masaya koy.", "Tepsiyi lavaboda yıka.")
            ),

            // 11. GÖRSEL - POUR
            createVisual(
                id = 11,
                questionTurkish = "dökmek?",
                imageRes = R.drawable.pour,
                correctAnswer = "pour",
                options = listOf("pour", "fill", "wash", "dry")
            ),

            // 12. LISTENING - POUR & GLASS
            createListening(
                id = 12,
                qTurkish = "Pour the water into the glass.",
                res = R.drawable.pour,
                ansEnglish = "Suyu bardağa dök.",
                opts = listOf("Suyu bardağa dök.", "Suyu bardaktan iç.", "Bardağı suyla doldur.")
            ),

            // 13. GÖRSEL - OVEN
            createVisual(
                id = 13,
                questionTurkish = "fırın?",
                imageRes = R.drawable.oven,
                correctAnswer = "oven",
                options = listOf("oven", "toaster", "kettle", "sink")
            ),

            // 14. LISTENING - OPEN & REFRIGERATOR
            createListening(
                id = 14,
                qTurkish = "Open the refrigerator.",
                res = R.drawable.refrigerator,
                ansEnglish = "Buzdolabını aç.",
                opts = listOf("Buzdolabını aç.", "Buzdolabını kapat.", "Buzdolabını temizle.")
            ),

            // 15. GÖRSEL - TOASTER
            createVisual(
                id = 15,
                questionTurkish = "ekmek kızartma makinesi?",
                imageRes = R.drawable.toaster,
                correctAnswer = "toaster",
                options = listOf("toaster", "oven", "kettle", "grater")
            ),

            // 16. LISTENING - HEAT & BREAD
            createListening(
                id = 16,
                qTurkish = "Heat the bread in the toaster.",
                res = R.drawable.toaster,
                ansEnglish = "Ekmeği kızartma makinesinde ısıt.",
                opts = listOf("Ekmeği kızartma makinesinde ısıt.", "Ekmeği fırında pişir.", "Ekmeği masaya getir.")
            ),

            // 17. GÖRSEL - SINK
            createVisual(
                id = 17,
                questionTurkish = "lavabo?",
                imageRes = R.drawable.sink,
                correctAnswer = "sink",
                options = listOf("sink", "refrigerator", "dishwasher", "oven")
            ),

            // 18. LISTENING - FILL & SINK
            createListening(
                id = 18,
                qTurkish = "Fill the sink with water.",
                res = R.drawable.sink,
                ansEnglish = "Lavaboyu suyla doldur.",
                opts = listOf("Lavaboyu suyla doldur.", "Lavaboyu temizle.", "Lavaboyu kurula.")
            ),

            // 19. GÖRSEL - NAPKIN
            createVisual(
                id = 19,
                questionTurkish = "peçete?",
                imageRes = R.drawable.napkin,
                correctAnswer = "napkin",
                options = listOf("napkin", "tray", "plate", "spoon")
            ),

            // 20. EŞLEŞTİRME FİNALİ
            createMatching(
                id = 20,
                questionText = "Mutfak ve Eylemler Finali",
                image = R.drawable.cat_logo,
                pairs = "boil,peel,carry,grate,pour",
                turkishOptions = listOf("kaynatmak", "soymak", "taşımak", "rendelemek", "dökmek")
            )
        )
    }
    fun getQuizFortySeven(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL - SHIRT
            createVisual(
                id = 1,
                questionTurkish = "gömlek?",
                imageRes = R.drawable.shirt,
                correctAnswer = "shirt",
                options = listOf("trousers", "shirt", "shoe", "jacket") // 👈 2. sıra
            ),

            // 2. LISTENING - SHIRT (DÜZELTİLDİ)
            createListening(
                id = 2,
                qTurkish = "I have a shirt.",
                res = R.drawable.shirt,
                ansEnglish = "Benim bir gömleğim var.",
                opts = listOf("Yeni bir pantolon satın al.", "Benim bir gömleğim var.", "Bu ceket çok büyük.") // 👈 2. sıra
            ),

            // 3. GÖRSEL - TROUSERS
            createVisual(
                id = 3,
                questionTurkish = "pantolon?",
                imageRes = R.drawable.trousers,
                correctAnswer = "trousers",
                options = listOf("shirt", "jacket", "hat", "trousers") // 👈 4. sıra
            ),

            // 4. LISTENING - TROUSERS (DÜZELTİLDİ)
            createListening(
                id = 4,
                qTurkish = "He wears blue trousers.",
                res = R.drawable.trousers,
                ansEnglish = "O, mavi pantolon giyer.",
                opts = listOf("Bu gömlek çok temiz.", "Benim mavi bir şapkam var.", "O, mavi pantolon giyer.") // 👈 3. sıra
            ),

            // 5. GÖRSEL - SHOE
            createVisual(
                id = 5,
                questionTurkish = "ayakkabı?",
                imageRes = R.drawable.shoe,
                correctAnswer = "shoe",
                options = listOf("shoe", "hat", "shirt", "trousers") // 👈 1. sıra
            ),

            // 6. LISTENING - SHOE
            createListening(
                id = 6,
                qTurkish = "This shoe is old.",
                res = R.drawable.shoe,
                ansEnglish = "Bu ayakkabı eski.",
                opts = listOf("Bu ayakkabı eski.", "Benim yeni bir şapkam var.", "Bu pantolon çok uzun.") // 👈 1. sıra
            ),

            // 7. GÖRSEL - HAT
            createVisual(
                id = 7,
                questionTurkish = "şapka?",
                imageRes = R.drawable.hat,
                correctAnswer = "hat",
                options = listOf("shoe", "jacket", "hat", "shirt") // 👈 3. sıra
            ),

            // 8. LISTENING - HAT
            createListening(
                id = 8,
                qTurkish = "Where is my hat?",
                res = R.drawable.hat,
                ansEnglish = "Benim şapkam nerede?",
                opts = listOf("Bu ceket kimin?", "Benim şapkam nerede?", "Gömlek dolabın içinde.") // 👈 2. sıra
            ),

            // 9. GÖRSEL - JACKET
            createVisual(
                id = 9,
                questionTurkish = "ceket?",
                imageRes = R.drawable.jacket,
                correctAnswer = "jacket",
                options = listOf("shirt", "trousers", "shoe", "jacket") // 👈 4. sıra
            ),

            // 10. LISTENING - JACKET (DÜZELTİLDİ)
            createListening(
                id = 10,
                qTurkish = "I like this blue jacket.",
                res = R.drawable.jacket,
                ansEnglish = "Ben bu mavi ceketi seviyorum.",
                opts = listOf("Mavi bir şapka istiyorum.", "Yeni bir ayakkabı aldım.", "Ben bu mavi ceketi seviyorum.") // 👈 3. sıra
            ),

            // 11. GÖRSEL - SOCK
            createVisual(
                id = 11,
                questionTurkish = "çorap?",
                imageRes = R.drawable.sock,
                correctAnswer = "sock",
                options = listOf("skirt", "sock", "belt", "tie") // 👈 2. sıra
            ),

            // 12. LISTENING - SKIRT
            createListening(
                id = 12,
                qTurkish = "She has a yellow skirt.",
                res = R.drawable.skirt,
                ansEnglish = "Onun sarı bir eteği var.",
                opts = listOf("Onun sarı bir eteği var.", "Bu gömlek çok kısa.", "Yeni bir kemer aldım.") // 👈 1. sıra
            ),

            // 13. GÖRSEL - BELT
            createVisual(
                id = 13,
                questionTurkish = "kemer?",
                imageRes = R.drawable.belt,
                correctAnswer = "belt",
                options = listOf("tie", "glove", "belt", "pocket") // 👈 3. sıra
            ),

            // 14. LISTENING - TIE
            createListening(
                id = 14,
                qTurkish = "This is a beautiful tie.",
                res = R.drawable.tie,
                ansEnglish = "Bu güzel bir kravat.",
                opts = listOf("Bu ceket çok sıcak.", "O siyah bir ayakkabı giyer.", "Bu güzel bir kravat.") // 👈 3. sıra
            ),

            // 15. GÖRSEL - GLOVE
            createVisual(
                id = 15,
                questionTurkish = "eldiven?",
                imageRes = R.drawable.glove,
                correctAnswer = "glove",
                options = listOf("glove", "pocket", "sock", "shirt") // 👈 1. sıra
            ),

            // 16. EŞLEŞTİRME FİNALİ
            createMatching(
                id = 16,
                questionText = "Kıyafetler ve Aksesuarlar Finali",
                image = R.drawable.cat_logo,
                pairs = "shirt,trousers,shoe,hat,jacket",
                turkishOptions = listOf("gömlek", "pantolon", "ayakkabı", "şapka", "ceket")
            )
        )
    }
    fun getQuizFortyEight(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL - SHIRT
            createVisual(
                id = 1,
                questionTurkish = "gömlek?",
                imageRes = R.drawable.shirt,
                correctAnswer = "shirt",
                options = listOf("shirt", "jacket", "trousers", "shoe") // 👈 1. sıra
            ),

            // 2. LISTENING - SHIRT
            createListening(
                id = 2,
                qTurkish = "I want a new shirt.",
                res = R.drawable.shirt,
                ansEnglish = "Ben yeni bir gömlek istiyorum.",
                opts = listOf("Bu eski bir ayakkabı.", "Ben yeni bir gömlek istiyorum.", "Mavi pantolonu getir.") // 👈 2. sıra
            ),

            // 3. GÖRSEL - TROUSERS
            createVisual(
                id = 3,
                questionTurkish = "pantolon?",
                imageRes = R.drawable.trousers,
                correctAnswer = "trousers",
                options = listOf("skirt", "belt", "trousers", "shirt") // 👈 3. sıra
            ),

            // 4. LISTENING - TROUSERS
            createListening(
                id = 4,
                qTurkish = "My trousers are clean.",
                res = R.drawable.trousers,
                ansEnglish = "Benim pantolonum temiz.",
                opts = listOf("Benim pantolonum temiz.", "Bu siyah bir kravat.", "Yeni bir şapka aldım.") // 👈 1. sıra
            ),

            // 5. GÖRSEL - SHOE
            createVisual(
                id = 5,
                questionTurkish = "ayakkabı?",
                imageRes = R.drawable.shoe,
                correctAnswer = "shoe",
                options = listOf("hat", "shoe", "glove", "sock") // 👈 2. sıra
            ),

            // 6. LISTENING - SHOE (DÜZELTİLDİ)
            createListening(
                id = 6,
                qTurkish = "Where is my orange shoe?",
                res = R.drawable.shoe,
                ansEnglish = "Benim turuncu ayakkabım nerede?",
                opts = listOf("Sarı eteğin nerede?", "Kırmızı ceket dolapta.", "Benim turuncu ayakkabım nerede?") // 👈 3. sıra
            ),

            // 7. GÖRSEL - HAT
            createVisual(
                id = 7,
                questionTurkish = "şapka?",
                imageRes = R.drawable.hat,
                correctAnswer = "hat",
                options = listOf("shirt", "jacket", "shoe", "hat") // 👈 4. sıra
            ),

            // 8. LISTENING - HAT
            createListening(
                id = 8,
                qTurkish = "She wears a big hat.",
                res = R.drawable.hat,
                ansEnglish = "O, büyük bir şapka takar.",
                opts = listOf("O, büyük bir şapka takar.", "Benim beyaz bir gömleğim var.", "Bu kemer çok eski.") // 👈 1. sıra
            ),

            // 9. GÖRSEL - JACKET
            createVisual(
                id = 9,
                questionTurkish = "ceket?",
                imageRes = R.drawable.jacket,
                correctAnswer = "jacket",
                options = listOf("trousers", "jacket", "shirt", "tie") // 👈 2. sıra
            ),

            // 10. LISTENING - JACKET
            createListening(
                id = 10,
                qTurkish = "This jacket is very warm.",
                res = R.drawable.jacket,
                ansEnglish = "Bu ceket çok sıcak.",
                opts = listOf("Bu güzel bir kravat.", "Bu ceket çok sıcak.", "Mavi bir çorap istiyorum.") // 👈 2. sıra
            ),

            // 11. GÖRSEL - SOCK
            createVisual(
                id = 11,
                questionTurkish = "çorap?",
                imageRes = R.drawable.sock,
                correctAnswer = "sock",
                options = listOf("glove", "pocket", "shoe", "sock") // 👈 4. sıra
            ),

            // 12. LISTENING - SKIRT
            createListening(
                id = 12,
                qTurkish = "I like this green skirt.",
                res = R.drawable.skirt,
                ansEnglish = "Ben bu yeşil eteği seviyorum.",
                opts = listOf("Ben bu yeşil eteği seviyorum.", "Kırmızı ceket çok güzel.", "Benim beyaz bir gömleğim var.") // 👈 1. sıra
            ),

            // 13. GÖRSEL - BELT
            createVisual(
                id = 13,
                questionTurkish = "kemer?",
                imageRes = R.drawable.belt,
                correctAnswer = "belt",
                options = listOf("belt", "tie", "hat", "trousers") // 👈 1. sıra
            ),

            // 14. LISTENING - TIE (DÜZELTİLDİ)
            createListening(
                id = 14,
                qTurkish = "He has a red tie.",
                res = R.drawable.tie,
                ansEnglish = "Onun kırmızı bir kravatı var.",
                opts = listOf("Benim yeni bir gömleğim var.", "Onun kırmızı bir kravatı var.", "Bu siyah bir pantolon.") // 👈 2. sıra
            ),

            // 15. GÖRSEL - GLOVE
            createVisual(
                id = 15,
                questionTurkish = "eldiven?",
                imageRes = R.drawable.glove,
                correctAnswer = "glove",
                options = listOf("sock", "glove", "pocket", "belt") // 👈 2. sıra
            ),

            // 16. EŞLEŞTİRME FİNALİ
            createMatching(
                id = 16,
                questionText = "Kıyafetler ve Aksesuarlar Tekrar Finali",
                image = R.drawable.cat_logo,
                pairs = "sock,skirt,belt,tie,glove",
                turkishOptions = listOf("çorap", "etek", "kemer", "kravat", "eldiven")
            )
        )
    }
    fun getQuizFortyNine(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. KELİME: WEAR
            createListening(
                id = 1,
                qTurkish = "wear",
                res = R.drawable.blackcat,
                ansEnglish = "giymek / takmak",
                opts = listOf("yıkamak", "giymek / takmak", "çıkarmak") // 👈 2. sıra
            ),
            // 2. PEKİŞTİREÇ: WEAR
            createListening(
                id = 2,
                qTurkish = "Wear your jacket.",
                res = R.drawable.man,
                ansEnglish = "Ceketini giy.",
                opts = listOf("Gömleğini yıka.", "Ceketini giy.", "Ayakkabını çıkar.") // 👈 2. sıra
            ),

            // 3. KELİME: TAKE OFF
            createListening(
                id = 3,
                qTurkish = "take off",
                res = R.drawable.woman,
                ansEnglish = "çıkarmak",
                opts = listOf("çıkarmak", "ütülemek", "giyinmek") // 👈 1. sıra
            ),
            // 4. PEKİŞTİREÇ: TAKE OFF
            createListening(
                id = 4,
                qTurkish = "Take off your hat.",
                res = R.drawable.takeoff,
                ansEnglish = "Şapkanı çıkar.",
                opts = listOf("Şapkanı çıkar.", "Kravatını tak.", "Pantolonunu ütüle.") // 👈 1. sıra
            ),

            // 5. KELİME: WASH
            createListening(
                id = 5,
                qTurkish = "wash",
                res = R.drawable.man,
                ansEnglish = "yıkamak",
                opts = listOf("ütülemek", "giymek", "yıkamak") // 👈 3. sıra
            ),
            // 6. PEKİŞTİREÇ: WASH
            createListening(
                id = 6,
                qTurkish = "Wash the white shirt.",
                res = R.drawable.shirt,
                ansEnglish = "Beyaz gömleği yıka.",
                opts = listOf("Mavi pantolonu giy.", "Siyah ayakkabıyı temizle.", "Beyaz gömleği yıka.") // 👈 3. sıra
            ),

            // 7. KELİME: IRON
            createListening(
                id = 7,
                qTurkish = "iron",
                res = R.drawable.iron,
                ansEnglish = "ütülemek",
                opts = listOf("ütülemek", "çıkarmak", "yıkamak") // 👈 1. sıra
            ),
            // 8. PEKİŞTİREÇ: IRON
            createListening(
                id = 8,
                qTurkish = "Iron your trousers.",
                res = R.drawable.trousers,
                ansEnglish = "Pantolonunu ütüle.",
                opts = listOf("Pantolonunu ütüle.", "Eteğini çamaşır makinesine at.", "Kemerini dolaba koy.") // 👈 1. sıra
            ),

            // 9. KELİME: PUT ON
            createListening(
                id = 9,
                qTurkish = "put on",
                res = R.drawable.shoe,
                ansEnglish = "giyinmek / üstüne geçirmek",
                opts = listOf("çıkarmak", "giyinmek / üstüne geçirmek", "ütülemek") // 👈 2. sıra
            ),
            // 10. PEKİŞTİREÇ: PUT ON
            createListening(
                id = 10,
                qTurkish = "Put on your shoe.",
                res = R.drawable.shoe,
                ansEnglish = "Ayakkabını giy.",
                opts = listOf("Eldivenini çıkar.", "Ayakkabını giy.", "Ceketini ütüle.") // 👈 2. sıra
            ),

            // --- KARMA TEKRAR VE FARKLI KOMBİNASYONLAR ---

            // 11. WEAR (Farklı nesne ile tekrar)
            createListening(
                id = 11,
                qTurkish = "He wears a red tie.",
                res = R.drawable.tie,
                ansEnglish = "O, kırmızı bir kravat takar.",
                opts = listOf("O, yeni bir gömlek ister.", "O, siyah bir ceket giyer.", "O, kırmızı bir kravat takar.") // 👈 3. sıra
            ),

            // 12. TAKE OFF (Farklı nesne ile tekrar)
            createListening(
                id = 12,
                qTurkish = "Take off your shoe.",
                res = R.drawable.shoe,
                ansEnglish = "Ayakkabını çıkar.",
                opts = listOf("Ayakkabını çıkar.", "Çorabını yıka.", "Şapkanı tak.") // 👈 1. sıra
            ),

            // 13. WASH (Geniş zaman çekimiyle tekrar)
            createListening(
                id = 13,
                qTurkish = "I wash my sock.",
                res = R.drawable.sock,
                ansEnglish = "Ben çorabımı yıkarım.",
                opts = listOf("Ben kemerimi arıyorum.", "Ben çorabımı yıkarım.", "Ben bir ceket istiyorum.") // 👈 2. sıra
            ),

            // 14. IRON (Geniş zaman çekimiyle tekrar)
            createListening(
                id = 14,
                qTurkish = "I iron my shirt.",
                res = R.drawable.iron,
                ansEnglish = "Ben gömleğimi ütülerim.",
                opts = listOf("Ben şapkamı çıkarırım.", "Ben ayakkabımı temizlerim.", "Ben gömleğimi ütülerim.") // 👈 3. sıra
            ),

            // 15. PUT ON (Farklı nesne ile tekrar)
            createListening(
                id = 15,
                qTurkish = "Put on your warm jacket.",
                res = R.drawable.wear,
                ansEnglish = "Sıcak ceketini giy.",
                opts = listOf("Sıcak ceketini giy.", "Eski şapkanı çıkar.", "Mavi pantolonunu ütüle.") // 👈 1. sıra
            ),

            // 16. EŞLEŞTİRME FİNALİ
            createMatching(
                id = 16,
                questionText = "Kıyafet Eylemleri Finali",
                image = R.drawable.cat_logo,
                pairs = "wear,take off,wash,iron,put on",
                turkishOptions = listOf("giymek/takmak", "çıkarmak", "yıkamak", "ütülemek", "giyinmek")
            )
        )
    }
    fun getQuizFifty(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL PEKİŞTİRME - SHIRT
            createVisual(
                id = 1,
                questionTurkish = "gömlek?",
                imageRes = R.drawable.shirt,
                correctAnswer = "shirt",
                options = listOf("trousers", "shirt", "shoe", "jacket") // 👈 2. sıra
            ),

            // 2. ÇEVİRİ PEKİŞTİRME - WEAR & SHIRT
            createListening(
                id = 2,
                qTurkish = "I wear a white shirt.",
                res = R.drawable.shirt,
                ansEnglish = "Ben beyaz bir gömlek giyerim.",
                opts = listOf("Ben beyaz bir gömlek giyerim.", "Ceketimi temizlerim.", "Yeni bir şapka istiyorum.") // 👈 1. sıra
            ),

            // 3. GÖRSEL PEKİŞTİRME - TROUSERS
            createVisual(
                id = 3,
                questionTurkish = "pantolon?",
                imageRes = R.drawable.trousers,
                correctAnswer = "trousers",
                options = listOf("skirt", "belt", "hat", "trousers") // 👈 4. sıra
            ),

            // 4. ÇEVİRİ PEKİŞTİRME - IRON & TROUSERS
            createListening(
                id = 4,
                qTurkish = "Iron your blue trousers.",
                res = R.drawable.trousers,
                ansEnglish = "Mavi pantolonunu ütüle.",
                opts = listOf("Siyah ayakkabını giy.", "Mavi pantolonunu ütüle.", "Yeşil eteğini yıka.") // 👈 2. sıra
            ),

            // 5. GÖRSEL PEKİŞTİRME - SHOE
            createVisual(
                id = 5,
                questionTurkish = "ayakkabı?",
                imageRes = R.drawable.shoe,
                correctAnswer = "shoe",
                options = listOf("shoe", "sock", "glove", "hat") // 👈 1. sıra
            ),

            // 6. ÇEVİRİ PEKİŞTİRME - TAKE OFF & SHOE
            createListening(
                id = 6,
                qTurkish = "Take off your old shoe.",
                res = R.drawable.shoe,
                ansEnglish = "Eski ayakkabını çıkar.",
                opts = listOf("Kırmızı ceketini giy.", "Yeni gömleğini yıka.", "Eski ayakkabını çıkar.") // 👈 3. sıra
            ),

            // 7. GÖRSEL PEKİŞTİRME - HAT
            createVisual(
                id = 7,
                questionTurkish = "şapka?",
                imageRes = R.drawable.hat,
                correctAnswer = "hat",
                options = listOf("shirt", "jacket", "hat", "tie") // 👈 3. sıra
            ),

            // 8. ÇEVİRİ PEKİŞTİRME - PUT ON & HAT
            createListening(
                id = 8,
                qTurkish = "Put on your warm hat.",
                res = R.drawable.hat,
                ansEnglish = "Sıcak şapkanı tak.",
                opts = listOf("Sıcak şapkanı tak.", "Siyah kravatını çıkar.", "Çorabını çamaşır makinesine at.") // 👈 1. sıra
            ),

            // 9. GÖRSEL PEKİŞTİRME - JACKET
            createVisual(
                id = 9,
                questionTurkish = "ceket?",
                imageRes = R.drawable.jacket,
                correctAnswer = "jacket",
                options = listOf("shirt", "trousers", "shoe", "jacket") // 👈 4. sıra
            ),

            // 10. ÇEVİRİ PEKİŞTİRME - WASH & JACKET
            createListening(
                id = 10,
                qTurkish = "Wash your dirty jacket.",
                res = R.drawable.jacket,
                ansEnglish = "Kirli ceketini yıka.",
                opts = listOf("Temiz gömleğini ütüle.", "Kirli ceketini yıka.", "Yeni kemerini tak.") // 👈 2. sıra
            ),

            // 11. GÖRSEL PEKİŞTİRME - SKIRT
            createVisual(
                id = 11,
                questionTurkish = "etek?",
                imageRes = R.drawable.skirt,
                correctAnswer = "skirt",
                options = listOf("skirt", "sock", "belt", "glove") // 👈 1. sıra
            ),

            // 12. ÇEVİRİ PEKİŞTİRME - WEAR & SKIRT
            createListening(
                id = 12,
                qTurkish = "She wears a green skirt.",
                res = R.drawable.skirt,
                ansEnglish = "O, yeşil bir etek giyer.",
                opts = listOf("O, beyaz bir gömlek ütüler.", "O, yeni bir çorap ister.", "O, yeşil bir etek giyer.") // 👈 3. sıra
            ),

            // 13. GÖRSEL PEKİŞTİRME - TIE
            createVisual(
                id = 13,
                questionTurkish = "kravat?",
                imageRes = R.drawable.tie,
                correctAnswer = "tie",
                options = listOf("pocket", "belt", "tie", "shirt") // 👈 3. sıra
            ),

            // 14. ÇEVİRİ PEKİŞTİRME - PUT ON & TIE
            createListening(
                id = 14,
                qTurkish = "Put on your red tie.",
                res = R.drawable.tie,
                ansEnglish = "Kırmızı kravatını tak.",
                opts = listOf("Kırmızı kravatını tak.", "Siyah pantolonunu ütüle.", "Mavi gömleğini yıka.") // 👈 1. sıra
            ),

            // 15. GÖRSEL PEKİŞTİRME - BELT
            createVisual(
                id = 15,
                questionTurkish = "kemer?",
                imageRes = R.drawable.belt,
                correctAnswer = "belt",
                options = listOf("glove", "belt", "sock", "jacket") // 👈 2. sıra
            ),

            // 16. EŞLEŞTİRME BÜYÜK FİNALİ
            createMatching(
                id = 16,
                questionText = "50. Seviye Kıyafet Ustası Finali",
                image = R.drawable.cat_logo, // Profesör Kedi başarılar diler!
                pairs = "wear,take off,wash,iron,put on",
                turkishOptions = listOf("giymek/takmak", "çıkarmak", "yıkamak", "ütülemek", "giyinmek")
            )
        )
    }
    fun getQuizFiftyOne(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. GÖRSEL PEKİŞTİRME - SOCK
            createVisual(
                id = 1,
                questionTurkish = "çorap?",
                imageRes = R.drawable.sock,
                correctAnswer = "sock",
                options = listOf("glove", "sock", "shoe", "belt") // 👈 2. sıra
            ),

            // 2. ÇEVİRİ PEKİŞTİRME - WASH & SOCK
            createListening(
                id = 2,
                qTurkish = "Wash your dirty sock.",
                res = R.drawable.sock,
                ansEnglish = "Kirli çorabını yıka.",
                opts = listOf("Mavi pantolonunu ütüle.", "Yeni bir şapka tak.", "Kirli çorabını yıka.") // 👈 3. sıra
            ),

            // 3. GÖRSEL PEKİŞTİRME - GLOVE
            createVisual(
                id = 3,
                questionTurkish = "eldiven?",
                imageRes = R.drawable.glove,
                correctAnswer = "glove",
                options = listOf("glove", "pocket", "tie", "shirt") // 👈 1. sıra
            ),

            // 4. ÇEVİRİ PEKİŞTİRME - TAKE OFF & GLOVE
            createListening(
                id = 4,
                qTurkish = "Take off your warm glove.",
                res = R.drawable.glove,
                ansEnglish = "Sıcak eldivenini çıkar.",
                opts = listOf("Sıcak eldivenini çıkar.", "Siyah kemerini tak.", "Kırmızı kravatını yıka.") // 👈 1. sıra
            ),

            // 5. GÖRSEL PEKİŞTİRME - BELT
            createVisual(
                id = 5,
                questionTurkish = "kemer?",
                imageRes = R.drawable.belt,
                correctAnswer = "belt",
                options = listOf("tie", "skirt", "belt", "trousers") // 👈 3. sıra
            ),

            // 6. ÇEVİRİ PEKİŞTİRME - PUT ON & BELT
            createListening(
                id = 6,
                qTurkish = "Put on your black belt.",
                res = R.drawable.belt,
                ansEnglish = "Siyah kemerini tak.",
                opts = listOf("Beyaz gömleğini ütüle.", "Siyah kemerini tak.", "Eski ayakkabını çıkar.") // 👈 2. sıra
            ),

            // 7. GÖRSEL PEKİŞTİRME - SKIRT
            createVisual(
                id = 7,
                questionTurkish = "etek?",
                imageRes = R.drawable.skirt,
                correctAnswer = "skirt",
                options = listOf("shirt", "jacket", "hat", "skirt") // 👈 4. sıra
            ),

            // 8. ÇEVİRİ PEKİŞTİRME - IRON & SKIRT
            createListening(
                id = 8,
                qTurkish = "Iron your yellow skirt.",
                res = R.drawable.skirt,
                ansEnglish = "Sarı eteğini ütüle.",
                opts = listOf("Sarı eteğini ütüle.", "Mavi ceketini giy.", "Yeni çorabını yıka.") // 👈 1. sıra
            ),

            // 9. GÖRSEL PEKİŞTİRME - TIE
            createVisual(
                id = 9,
                questionTurkish = "kravat?",
                imageRes = R.drawable.tie,
                correctAnswer = "tie",
                options = listOf("shirt", "tie", "belt", "shoe") // 👈 2. sıra
            ),

            // 10. ÇEVİRİ PEKİŞTİRME - WEAR & TIE
            createListening(
                id = 10,
                qTurkish = "I wear a blue tie.",
                res = R.drawable.tie,
                ansEnglish = "Ben mavi bir kravat takarım.",
                opts = listOf("Ben yeşil bir ceket severim.", "Siyah pantolonumu giyerim.", "Ben mavi bir kravat takarım.") // 👈 3. sıra
            ),

            // 11. GÖRSEL PEKİŞTİRME - JACKET
            createVisual(
                id = 11,
                questionTurkish = "ceket?",
                imageRes = R.drawable.jacket,
                correctAnswer = "jacket",
                options = listOf("trousers", "jacket", "shirt", "hat") // 👈 2. sıra
            ),

            // 12. ÇEVİRİ PEKİŞTİRME - PUT ON & JACKET
            createListening(
                id = 12,
                qTurkish = "Put on your new jacket.",
                res = R.drawable.jacket,
                ansEnglish = "Yeni ceketini giy.",
                opts = listOf("Yeni ceketini giy.", "Eski şapkanı çıkar.", "Kirli gömleğini yıka.") // 👈 1. sıra
            ),

            // 13. GÖRSEL PEKİŞTİRME - SHIRT
            createVisual(
                id = 13,
                questionTurkish = "gömlek?",
                imageRes = R.drawable.shirt,
                correctAnswer = "shirt",
                options = listOf("shoe", "trousers", "jacket", "shirt") // 👈 4. sıra
            ),

            // 14. ÇEVİRİ PEKİŞTİRME - IRON & SHIRT
            createListening(
                id = 14,
                qTurkish = "Do you iron your shirt?",
                res = R.drawable.shirt,
                ansEnglish = "Gömleğini ütüler misin?",
                opts = listOf("Gömleğin temiz mi?", "Gömleğini ütüler misin?", "Yeni bir gömlek ister misin?") // 👈 2. sıra
            ),

            // 15. GÖRSEL PEKİŞTİRME - SHOE
            createVisual(
                id = 15,
                questionTurkish = "ayakkabı?",
                imageRes = R.drawable.shoe,
                correctAnswer = "shoe",
                options = listOf("shoe", "sock", "glove", "hat") // 👈 1. sıra
            ),

            // 16. EŞLEŞTİRME BÜYÜK FİNALİ
            createMatching(
                id = 16,
                questionText = "Kıyafet ve Eylem Kombin Finali",
                image = R.drawable.cat_logo,
                pairs = "shirt,trousers,shoe,hat,jacket",
                turkishOptions = listOf("gömlek", "pantolon", "ayakkabı", "şapka", "ceket")
            )
        )
    }
    fun getQuizFiftyTwo(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. KELİME: DRESS
            createListening(
                id = 1,
                qTurkish = "Coat",
                res = R.drawable.coat,
                ansEnglish = "palto",
                opts = listOf("palto", "elbise", "bot") // 👈 2. sıra
            ),
            // 2. PEKİŞTİREÇ: DRESS
            createListening(
                id = 2,
                qTurkish = "She wears a beautiful dress.",
                res = R.drawable.dress,
                ansEnglish = "O, güzel bir elbise giyer.",
                opts = listOf("O, güzel bir elbise giyer.", "Bu yeni bir bottur.", "Gömleğin küçük bir cebi var.") // 👈 1. sıra
            ),

            // 3. KELİME: COAT
            createListening(
                id = 3,
                qTurkish = "coat",
                res = R.drawable.coat,
                ansEnglish = "palto / kaban",
                opts = listOf("cep", "elbise", "palto / kaban") // 👈 3. sıra
            ),
            // 4. PEKİŞTİREÇ: COAT
            createListening(
                id = 4,
                qTurkish = "Put on your warm coat.",
                res = R.drawable.coat,
                ansEnglish = "Sıcak paltonu giy.",
                opts = listOf("Kirli botunu çıkar.", "Sıcak paltonu giy.", "Güzel elbiseni yıka.") // 👈 2. sıra
            ),

            // 5. KELİME: BOOT
            createListening(
                id = 5,
                qTurkish = "drees",
                res = R.drawable.dress,
                ansEnglish = "elbisse",
                opts = listOf("elbise", "palto", "cep") // 👈 1. sıra
            ),
            // 6. PEKİŞTİREÇ: BOOT
            createListening(
                id = 6,
                qTurkish = "Take off your dirty boot.",
                res = R.drawable.boot,
                ansEnglish = "Kirli botunu çıkar.",
                opts = listOf("Paltonu dolaba koy.", "Elbisen çok temiz.", "Kirli botunu çıkar.") // 👈 3. sıra
            ),

            // 7. KELİME: POCKET
            createListening(
                id = 7,
                qTurkish = "pocket",
                res = R.drawable.pocket,
                ansEnglish = "cep",
                opts = listOf("elbise", "cep", "palto") // 👈 2. sıra
            ),
            // 8. PEKİŞTİREÇ: POCKET
            createListening(
                id = 8,
                qTurkish = "My shirt has a small pocket.",
                res = R.drawable.pocket,
                ansEnglish = "Gömleğimin küçük bir cebi var.",
                opts = listOf("Gömleğimin küçük bir cebi var.", "Siyah botunu temizle.", "Bu sıcak bir paltodur.") // 👈 1. sıra
            ),

            // --- KARMA PEKİŞTİRME VE TEMEL REKURSİF KALIPLAR ---

            // 9. DRESS (DÜZELTİLDİ)
            createListening(
                id = 9,
                qTurkish = "I wear a new dress.",
                res = R.drawable.dress,
                ansEnglish = "Ben yeni bir elbise giyerim.",
                opts = listOf("Paltonu ütülemek istiyorum.", "Ben yeni bir elbise giyerim.", "Botlarım nerede?") // 👈 2. sıra
            ),

            // 10. COAT
            createListening(
                id = 10,
                qTurkish = "Is this coat clean?",
                res = R.drawable.coat,
                ansEnglish = "Bu palto temiz mi?",
                opts = listOf("Elbisen yeni mi?", "Cebinde ne var?", "Bu palto temiz mi?") // 👈 3. sıra
            ),

            // 11. BOOT (DÜZELTİLDİ)
            createListening(
                id = 11,
                qTurkish = "Where is my brown boot?",
                res = R.drawable.boot,
                ansEnglish = "Benim kahverengi botum nerede?",
                opts = listOf("Benim kahverengi botum nerede?", "Güzel elbiseyi yıkadım.", "Paltonu üzerine geçirmelisin.") // 👈 1. sıra
            ),

            // 12. POCKET
            createListening(
                id = 12,
                qTurkish = "The dress has no pocket.",
                res = R.drawable.pocket,
                ansEnglish = "Elbisenin cebi yok.",
                opts = listOf("Palton çok kalın.", "Elbisenin cebi yok.", "Yeni bir bot aldım.") // 👈 2. sıra
            ),

            // 13. DRESS & COAT
            createListening(
                id = 13,
                qTurkish = "She washes the dress and coat.",
                res = R.drawable.coat,
                ansEnglish = "O, elbiseyi ve paltoyu yıkar.",
                opts = listOf("O, elbiseyi ve paltoyu yıkar.", "O, botunu ve şapkasını çıkarır.", "O, cebine bakıyor.") // 👈 1. sıra
            ),

            // 14. BOOT & POCKET
            createListening(
                id = 14,
                qTurkish = "I have a key in my pocket.",
                res = R.drawable.pocket,
                ansEnglish = "Cebimde bir anahtar var.",
                opts = listOf("Botlarım çamurlu.", "Cebimde bir anahtar var.", "Yeni bir kaban giyiyorum.") // 👈 2. sıra
            ),

            // 15. COAT & BOOT
            createListening(
                id = 15,
                qTurkish = "Put on your coat and boot.",
                res = R.drawable.boot,
                ansEnglish = "Paltonu ve botunu giy.",
                opts = listOf("Elbiseni ve kemerini çıkar.", "Gömleğini ütüle.", "Paltonu ve botunu giy.") // 👈 3. sıra
            ),

            // 16. EŞLEŞTİRME FİNALİ
            createMatching(
                id = 16,
                questionText = "Yeni Kıyafetler Finali",
                image = R.drawable.cat_logo,
                pairs = "dress,coat,boot,pocket",
                turkishOptions = listOf("elbise", "palto", "bot", "cep")
            )
        )
    }
    fun getQuizFiftyThree(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. KELİME: BUY
            createListening(
                id = 1,
                qTurkish = "buy",
                res = R.drawable.dress,
                ansEnglish = "satın almak",
                opts = listOf("ütülemek", "bulmak", "satın almak") // 👈 3. sıra
            ),
            // 2. PEKİŞTİREÇ: DRESS (RED DRESS)
            createListening(
                id = 2,
                qTurkish = "Wash your red dress.",
                res = R.drawable.dress,
                ansEnglish = "Kırmızı elbiseni yıka.",
                opts = listOf("Kırmızı elbiseni yıka.", "Siyah paltonu ütüle.", "Kirli botunu çıkar.") // 👈 1. sıra
            ),

            // 3. KELİME: FIND (COAT KALDIRILDI, FİİL EKLENDİ)
            createListening(
                id = 3,
                qTurkish = "find",
                res = R.drawable.pocket,
                ansEnglish = "bulmak",
                opts = listOf("bakmak", "bulmak", "satın almak") // 👈 2. sıra
            ),
            // 4. PEKİŞTİREÇ: COAT
            createListening(
                id = 4,
                qTurkish = "His coat is very big.",
                res = R.drawable.coat,
                ansEnglish = "Onun paltosu çok büyüktür.",
                opts = listOf("Benim elbisem yenidir.", "Onun paltosu çok büyüktür.", "Cebinde ne var?") // 👈 2. sıra
            ),

            // 5. KELİME: BOOT
            createListening(
                id = 5,
                qTurkish = "boot",
                res = R.drawable.boot,
                ansEnglish = "bot / çizme",
                opts = listOf("bot / çizme", "palto", "elbise") // 👈 1. sıra
            ),
            // 6. PEKİŞTİREÇ: BOOT
            createListening(
                id = 6,
                qTurkish = "I wash my brown boot.",
                res = R.drawable.boot,
                ansEnglish = "Ben kahverengi botumu yıkarım.",
                opts = listOf("O, güzel elbiseyi giyer.", "Sıcak kabanını üzerine al.", "Ben kahverengi botumu yıkarım.") // 👈 3. sıra
            ),

            // 7. KELİME: POCKET
            createListening(
                id = 7,
                qTurkish = "pocket",
                res = R.drawable.pocket,
                ansEnglish = "cep",
                opts = listOf("palto", "cep", "bot") // 👈 2. sıra
            ),
            // 8. PEKİŞTİREÇ: POCKET
            createListening(
                id = 8,
                qTurkish = "What is in your pocket?",
                res = R.drawable.pocket,
                ansEnglish = "Cebinde ne var?",
                opts = listOf("Cebinde ne var?", "Bu senin elbisen mi?", "Yeni bir bot mu aldın?") // 👈 1. sıra
            ),

            // --- FİİLLER VE PEKİŞTİREÇLERİ ---

            // 9. TEKRAR PEKİŞTİREÇ: BUY & RED DRESS
            createListening(
                id = 9,
                qTurkish = "I buy a red dress.",
                res = R.drawable.dress,
                ansEnglish = "Ben kırmızı bir elbise satın alırım.",
                opts = listOf("Ben kırmızı bir elbise satın alırım.", "Mavi paltonu ütülerim.", "Kahverengi botumu bulurum.") // 👈 1. sıra
            ),

            // 10. TEKRAR PEKİŞTİREÇ: FIND & POCKET
            createListening(
                id = 10,
                qTurkish = "Find a key in your pocket.",
                res = R.drawable.pocket,
                ansEnglish = "Cebinde bir anahtar bul.",
                opts = listOf("Sıcak paltonu giy.", "Cebinde bir anahtar bul.", "Kırmızı elbiseni yıka.") // 👈 2. sıra
            ),

            // 11. YENİ BİRLEŞİM: POCKET & IRON
            createListening(
                id = 11,
                qTurkish = "I iron my shirt.",
                res = R.drawable.shirt,
                ansEnglish = "Ben gömleğimi ütülerim.",
                opts = listOf("Ben gömleğimi ütülerim.", "Yeni bir bot aldım.", "Palton çok temiz.") // 👈 1. sıra
            ),

            // 12. POCKET TANIMI
            createListening(
                id = 12,
                qTurkish = "This pocket is small.",
                res = R.drawable.pocket,
                ansEnglish = "Bu cep küçüktür.",
                opts = listOf("Kaban çok sıcaktır.", "Bu cep küçüktür.", "Elbise yenidir.") // 👈 2. sıra
            ),

            // 13. SADECE "I WANT" VE GÖRSEL REVIZYONU
            createListening(
                id = 13,
                qTurkish = "I want.",
                res = R.drawable.man,
                ansEnglish = "Ben istiyorum.",
                opts = listOf("Ben söylüyorum.", "Ben istiyorum.", "Ben giyiyorum.") // 👈 2. sıra
            ),

            // 14. YENİ FİİL: LOOK AT
            createListening(
                id = 14,
                qTurkish = "look at",
                res = R.drawable.look,
                ansEnglish = "bakmak",
                opts = listOf("satın almak", "bulmak", "bakmak") // 👈 3. sıra
            ),
            // 15. PEKİŞTİREÇ: LOOK AT & COAT
            createListening(
                id = 15,
                qTurkish = "Look at my new coat.",
                res = R.drawable.look,
                ansEnglish = "Yeni palto / kabanıma bak.",
                opts = listOf("Kahverengi botunu çıkar.", "Yeni palto / kabanıma bak.", "Kırmızı elbiseni ütüle.") // 👈 2. sıra
            ),

            // 16. PEKİŞTİREÇ: LOOK AT & BROWN BOOT
            createListening(
                id = 16,
                qTurkish = "Look at your brown boot.",
                res = R.drawable.look,
                ansEnglish = "Kahverengi botuna bak.",
                opts = listOf("Kahverengi botuna bak.", "Cebindeki parayı bul.", "Yeni bir elbise satın al.") // 👈 1. sıra
            ),

            // 17. EŞLEŞTİRME NİHAİ FİNALİ
            createMatching(
                id = 17,
                questionText = "Yeni Kıyafetler ve Eylemler Karma Finali",
                image = R.drawable.cat_logo,
                pairs = "buy,find,look at,dress,coat",
                turkishOptions = listOf("satın almak", "bulmak", "bakmak", "elbise", "palto")
            )
        )
    }
    fun getQuizFiftyFour(): List<VocabularyLevelDataQuiz> {
        return listOf(
            // 1. KELİME: BUY
            createListening(
                id = 1,
                qTurkish = "buy",
                res = R.drawable.dress,
                ansEnglish = "satın almak",
                opts = listOf("bakmak", "bulmak", "satın almak") // 👈 3. sıra
            ),
            // 2. PEKİŞTİREÇ: BUY
            createListening(
                id = 2,
                qTurkish = "I buy a brown boot.",
                res = R.drawable.boot,
                ansEnglish = "Ben kahverengi bir bot satın alırım.",
                opts = listOf("Ben kahverengi bir bot satın alırım.", "Mavi elbiseni yıka.", "Yeni paltona bak.") // 👈 1. sıra
            ),

            // 3. KELİME: FIND
            createListening(
                id = 3,
                qTurkish = "find",
                res = R.drawable.pocket,
                ansEnglish = "bulmak",
                opts = listOf("bulmak", "ütülemek", "giymek") // 👈 1. sıra
            ),
            // 4. PEKİŞTİREÇ: FIND
            createListening(
                id = 4,
                qTurkish = "Find your black belt.",
                res = R.drawable.belt,
                ansEnglish = "Siyah kemerini bul.",
                opts = listOf("Kırmızı elbiseni ütüle.", "Siyah kemerini bul.", "Kirli çorabını yıka.") // 👈 2. sıra
            ),

            // 5. KELİME: LOOK AT
            createListening(
                id = 5,
                qTurkish = "look at",
                res = R.drawable.look,
                ansEnglish = "bakmak",
                opts = listOf("çıkarmak", "bakmak", "satın almak") // 👈 2. sıra
            ),
            // 6. PEKİŞTİREÇ: LOOK AT
            createListening(
                id = 6,
                qTurkish = "Look at my red dress.",
                res = R.drawable.dress,
                ansEnglish = "Benim kırmızı elbiseme bak.",
                opts = listOf("Eski gömleğini ütüle.", "Yeni botunu giy.", "Benim kırmızı elbiseme bak.") // 👈 3. sıra
            ),

            // 7. KELİME: WASH
            createListening(
                id = 7,
                qTurkish = "wash",
                res = R.drawable.shirt,
                ansEnglish = "yıkamak",
                opts = listOf("bulmak", "yıkamak", "bakmak") // 👈 2. sıra
            ),
            // 8. PEKİŞTİREÇ: WASH
            createListening(
                id = 8,
                qTurkish = "Wash your dirty coat.",
                res = R.drawable.coat,
                ansEnglish = "Kirli paltonu yıka.",
                opts = listOf("Kirli paltonu yıka.", "Güzel elbiseni ütüle.", "Siyah ayakkabını çıkar.") // 👈 1. sıra
            ),

            // 9. KELİME: IRON
            createListening(
                id = 9,
                qTurkish = "iron",
                res = R.drawable.trousers,
                ansEnglish = "ütülemek",
                opts = listOf("ütülemek", "satın almak", "giyinmek") // 👈 1. sıra
            ),
            // 10. PEKİŞTİREÇ: IRON
            createListening(
                id = 10,
                qTurkish = "She irons her new shirt.",
                res = R.drawable.shirt,
                ansEnglish = "O, yeni gömleğini ütüler.",
                opts = listOf("O, kahverengi botunu arar.", "O, yeni gömleğini ütüler.", "O, sıcak kabanını giyer.") // 👈 2. sıra
            ),

            // --- İLERİ DÜZEY KARMA REKURSİF KALIPLAR ---

            // 11. WEAR & COAT
            createListening(
                id = 11,
                qTurkish = "He wears a warm coat.",
                res = R.drawable.coat,
                ansEnglish = "O, sıcak bir palto giyer.",
                opts = listOf("O, temiz bir gömlek ister.", "O, eski bir bot bulur.", "O, sıcak bir palto giyer.") // 👈 3. sıra
            ),

            // 12. TAKE OFF & BOOT
            createListening(
                id = 12,
                qTurkish = "Take off your brown boot.",
                res = R.drawable.boot,
                ansEnglish = "Kahverengi botunu çıkar.",
                opts = listOf("Kahverengi botunu çıkar.", "Kırmızı elbiseni satın al.", "Küçük cebine bak.") // 👈 1. sıra
            ),

            // 13. PUT ON & DRESS
            createListening(
                id = 13,
                qTurkish = "Put on your red dress.",
                res = R.drawable.dress,
                ansEnglish = "Kırmızı elbiseni giy.",
                opts = listOf("Siyah kemerini bul.", "Kırmızı elbiseni giy.", "Mavi pantolonunu yıka.") // 👈 2. sıra
            ),

            // 14. FIND & POCKET (Arama/Bulma Kombini)
            createListening(
                id = 14,
                qTurkish = "I find a key in my pocket.",
                res = R.drawable.pocket,
                ansEnglish = "Cebimde bir anahtar buluyorum.",
                opts = listOf("Cebimde bir anahtar buluyorum.", "Elbisemi ütülüyorum.", "Yeni bir kaban satın alıyorum.") // 👈 1. sıra
            ),

            // 15. LOOK AT & SHIRT
            createListening(
                id = 15,
                qTurkish = "Look at my blue shirt.",
                res = R.drawable.shirt,
                ansEnglish = "Mavi gömleğime bak.",
                opts = listOf("Sarı eteğini yıka.", "Mavi gömleğime bak.", "Kirli çorabını çıkar.") // 👈 2. sıra
            ),

            // 16. EŞLEŞTİRME BÜYÜK FİNALİ
            createMatching(
                id = 16,
                questionText = "Büyük Kıyafet Eylemleri Ustası",
                image = R.drawable.cat_logo,
                pairs = "buy,find,look at,wash,iron",
                turkishOptions = listOf("satın almak", "bulmak", "bakmak", "yıkamak", "ütülemek")
            )
        )
    }



















    // ─────────────────────────────────────────────────────────────────────────────
// createTranslation yardımcı fonksiyonu
// VocabularyLevelDataQuiz modelini TRANSLATION tipiyle oluşturur.
//
// Kullanımı:
//   createTranslation(
//       id = 1,
//       questionTurkish = "Ben istiyorum.",
//       imageRes = R.drawable.man,
//       correctAnswer = "I want",
//       options = listOf("I", "want", "you", "look")
//   )
//
// NOT: QuestionType.TRANSLATION enum'unuza ekli olmalı.
//      Yoksa FILL_IN_BLANK veya yeni bir tip ekleyebilirsiniz.
// ─────────────────────────────────────────────────────────────────────────────

    private fun createTranslation(
        id: Int,
        questionTurkish: String,
        imageRes: Int,
        correctAnswer: String,
        options: List<String>
    ): VocabularyLevelDataQuiz {
        return VocabularyLevelDataQuiz(
            id = id,
            questionTurkish = questionTurkish,
            imageRes = imageRes,
            correctAnswer = correctAnswer.trim(),   // baştaki/sondaki boşlukları temizle
            options = options,
            questionType = QuestionType.TRANSLATION // QuestionType enum'una TRANSLATION ekleyin
        )
    }





















    // 🛠️ NORMALİZE: Kelimeyi temizler ve resim anahtarına uygun hale getirir
    private fun normalize(text: String): String {
        return text
            .lowercase(Locale.ROOT)
            .replace(" please", "")
            .trim()
    }

    // 🛠️ CREATE VISUAL: Buradaki emptyList() sorununu düzelttik
    // 🛠️ CREATE VISUAL: Boyut ve Padding eklendi
    private fun createVisual(
        id: Int,
        questionTurkish: String,
        imageRes: Int,
        correctAnswer: String,
        options: List<String>,
        size: Int = 120,
        padding: Int = 8
    ): VocabularyLevelDataQuiz {

        // 🚨 OTOMATİK EŞLEŞTİRME BURADA YAPILIYOR:
        val autoGeneratedImages = options.map { option ->
            val key = normalize(option) // "book" -> "book"
            // imageMap içinde "book" anahtarını arar, bulamazsa kedi logosu basar
            imageMap[key] ?: R.drawable.cat_logo
        }

        return VocabularyLevelDataQuiz(
            id = id,
            questionTurkish = questionTurkish,
            imageRes = imageRes,
            correctAnswer = normalize(correctAnswer),
            options = options,
            questionType = QuestionType.VISUAL,
            // ✅ Otomatik oluşturulan listeyi buraya veriyoruz:
            optionImages = autoGeneratedImages,
            imageSize = size,
            imagePadding = padding
        )
    }

    // 🛠️ CREATE LISTENING: Boyut ve Padding eklendi
    // 🛠️ CREATE LISTENING: Transkript desteği eklendi
    private fun createListening(
        id: Int,
        qTurkish: String,
        res: Int,
        ansEnglish: String,
        opts: List<String>,
        qTr: Boolean = false,   // Manuel kontrol
        optTr: Boolean = true,  // Manuel kontrol
        size: Int = 150,
        padding: Int = 8,
        transcript: String? = null
    ): VocabularyLevelDataQuiz {
        return VocabularyLevelDataQuiz(
            id = id,
            questionTurkish = qTurkish,
            imageRes = res,
            correctAnswer = ansEnglish.trim(),
            options = opts,
            questionType = QuestionType.LISTENING,
            speakQuestionInTurkish = qTr,  // Burası parametreye bağlandı
            speakOptionsInTurkish = optTr, // Burası parametreye bağlandı
            imageSize = size,
            imagePadding = padding,
            transcriptTurkish = transcript
        )
    }

    // 🛠️ CREATE MATCHING: Boyut ve Padding eklendi
    private fun createMatching(
        id: Int,
        questionText: String,
        image: Int,
        pairs: String,
        turkishOptions: List<String>,
        size: Int = 120, // ✅ EKLENDİ
        padding: Int = 8  // ✅ EKLENDİ
    ): VocabularyLevelDataQuiz {
        val englishWords = pairs.split(",")
        val optionImages = englishWords.map { word ->
            val key = normalize(word)
            imageMap[key] ?: R.drawable.cat_logo
        }

        return VocabularyLevelDataQuiz(
            id = id,
            questionTurkish = questionText,
            imageRes = image,
            correctAnswer = pairs,
            options = turkishOptions,
            questionType = QuestionType.MATCHING,
            optionImages = optionImages,
            imageSize = size,      // ✅ BURAYA BAĞLANDI
            imagePadding = padding  // ✅ BURAYA BAĞLANDI
        )
    }
    private fun createTranslation(
        id: Int,
        questionTurkish: String, // Ekranda görünecek Türkçe Cümle
        imageRes: Int,           // Sol taraftaki görsel bölmesi
        correctAnswer: String,   // Kullanıcının dizmesi gereken doğru İngilizce cümle (Örn: "I want a dress")
        options: List<String>,   // Aşağıdaki havuzda görünecek kelimeler (Doğru kelimeler + Çeldiriciler)
        size: Int = 100,
        padding: Int = 8
    ): VocabularyLevelDataQuiz {
        return VocabularyLevelDataQuiz(
            id = id,
            questionTurkish = questionTurkish,
            imageRes = imageRes,
            correctAnswer = correctAnswer.trim(),
            options = options,
            questionType = QuestionType.FILL_IN_BLANK, // 👈 Enum sınıfındaki FILL_IN_BLANK yapısına bağladık
            imageSize = size,
            imagePadding = padding
        )
    }
    // 🛠️ 4. CREATE TRANSLATION (FILL_IN_BLANK Tipini Kullanan Versiyon)
    private fun createFillInBlank(
        id: Int,
        questionTurkish: String, // Ekranda görünecek Türkçe Cümle
        imageRes: Int,           // Sağ taraftaki görsel bölmesi
        correctAnswer: String,   // Kullanıcının dizmesi gereken doğru İngilizce cümle
        options: List<String>    // Aşağıdaki havuzda görünecek kelimeler (Doğru kelimeler + Çeldiriciler)
    ): VocabularyLevelDataQuiz {
        return VocabularyLevelDataQuiz(
            id = id,
            questionTurkish = questionTurkish,
            imageRes = imageRes,
            correctAnswer = correctAnswer.trim(), // Baştaki ve sondaki gereksiz boşlukları temizler
            options = options,
            questionType = QuestionType.FILL_IN_BLANK // 👈 Enum'ı değiştirmeden mevcut tipe bağladık
        )
    }
@Composable
fun LearningPathPaws(
    pawCount: Int = 4,
    solvedCount: Int,       // 0..pawCount arası
    isHorizontal: Boolean = false
) {
    // Her pati için animasyonlu alpha: yeşil mi siyah mı?
    if (isHorizontal) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            repeat(pawCount) { idx ->
                val isGreen = idx < solvedCount
                // Geçiş anında hafif scale animasyonu
                val scale by animateFloatAsState(
                    targetValue = if (isGreen) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "paw_scale_$idx"
                )
                Image(
                    painter = painterResource(
                        id = if (isGreen) R.drawable.green_paw else R.drawable.black_paw
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .scale(scale)
                        .padding(horizontal = 2.dp)
                )
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            repeat(pawCount) { idx ->
                val isGreen = idx < solvedCount
                val scale by animateFloatAsState(
                    targetValue = if (isGreen) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "paw_scale_v_$idx"
                )
                Image(
                    painter = painterResource(
                        id = if (isGreen) R.drawable.green_paw else R.drawable.black_paw
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .scale(scale)
                        .padding(vertical = 3.dp)
                )
            }
        }
    }
}
    @Composable
    fun QuizPathConnector(
        fromIndex: Int,
        toIndex: Int,
        lastSolvedIndex: Int,
        isHorizontal: Boolean = false,
        pawCount: Int = 4
    ) {
        // Çözülen pati sayısını hesapla
        val solvedCount = when {
            toIndex <= lastSolvedIndex -> pawCount
            fromIndex <= lastSolvedIndex -> pawCount / 2
            else -> 0
        }

        Box(
            modifier = if (isHorizontal)
                Modifier
                    .fillMaxWidth(0.55f)
                    .height(24.dp)
            else
                Modifier
                    .width(24.dp)
                    .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            LearningPathPaws(
                pawCount = pawCount,
                solvedCount = solvedCount,
                isHorizontal = isHorizontal
            )
        } // ✅ Box kapanışı
    }
}// ✅ QuizPathConnector kapanışı

@Composable
fun AnimatedResourceTag(
    composition: LottieComposition?, // Import hatası buradan düzelir
    value: String,
    iconColor: Color = Color.Unspecified // ✅ YAMA: Varsayılan değer ekleyerek hatayı çözüyoruz
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFF1F5F9), // MewEnglish temiz gri tonu
        modifier = Modifier.height(36.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
            )
            // Görseldeki şık artı butonu
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "+",
                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
            )
        }
    }
}