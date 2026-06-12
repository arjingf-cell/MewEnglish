package com.example.ngilizcequizapp


import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.BlendMode

import androidx.compose.ui.text.style.TextOverflow

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import com.example.ngilizcequizapp.SpeechAssistant
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip

import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// veya
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp


// ekranların importları
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment

import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

// Projene özel R ve Screen sınıflarını (kendi paket yoluna göre) eklemeyi unutma:
// import com.example.mewenglish.R
// import com.example.mewenglish.navigation.Screen
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1CB0F6),    // Mew Mavisi
    background = Color(0xFF121212), // Koyu mod arka planı
    surface = Color(0xFF1E1E1E),    // Kartların/Kutuların rengi
    onBackground = Color.White,     // Koyu modda yazı rengi
    onSurface = Color.White        // Koyu modda kart üstü yazı rengi
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1CB0F6),    // Mew Mavisi (Aynı kalabilir)
    background = Color.White,       // Açık mod arka planı
    surface = Color(0xFFF5F5F5),    // Açık modda hafif gri kartlar
    onBackground = Color.Black,     // Açık modda yazı rengi
    onSurface = Color.Black
)

// 1. DarkColorScheme ve LightColorScheme tanımlarının altına şu satırı ekle:
private val AppTypography = Typography()

@Composable
fun MewEnglishTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        // 2. Buradaki "Typography" (Sınıf adı) yerine "AppTypography" (Nesne adı) yazıyoruz:
        typography = AppTypography,
        content = content
    )
}
// ✅ İsmi değiştir
@Composable
fun AppRootScreen(speechAssistant: SpeechAssistant) {
    var isDarkModeManual by remember { mutableStateOf(false) }

    MewEnglishTheme(darkTheme = isDarkModeManual) {
        CompositionLocalProvider(LocalIsDarkMode provides isDarkModeManual) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                QuizAppNavigation(
                    speechAssistant = speechAssistant,
                    isDarkMode = isDarkModeManual,
                    onToggleDarkMode = { isDarkModeManual = !isDarkModeManual }
                )
            }
        }
    }
}

@Composable
fun DarkEliteHomeScreen(
    navController: NavHostController,
    showQuizPrompt: Boolean,
    onDismissQuizPrompt: () -> Unit,
    isDarkMode: Boolean,
    speechAssistant: SpeechAssistant
) {
    val bgColor = Color(0xFFF8FAFC)

    // Özellikler için veri modeli ve listesi
    data class FeatureItem(val title: String, val desc: String, val iconRes: Int)

    val features = listOf(
        FeatureItem("Mistake Jar", "Fix errors", R.drawable.mistake),
        FeatureItem("Reading", "Magic reading", R.drawable.reading),
        FeatureItem("My Words", "Vocabulary", R.drawable.word),
        FeatureItem("Grammar", "Master rules", R.drawable.grammar)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 🔹 HEADER
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .background(Color.White, CircleShape)
                            .border(3.dp, Color(0xFF1CB0F6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.master),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(6.dp).clip(CircleShape)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Good morning", fontSize = 13.sp, color = Color(0xFF94A3B8))
                        Text("Ready to learn?", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                        Text("Let's improve your English today!", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }

            // 🔹 WORD OF THE DAY
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight().align(Alignment.CenterEnd)) {
                            Image(
                                painter = painterResource(id = R.drawable.daily_word),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, Color(0xFFE0F2FE)),
                                                startX = 0f,
                                                endX = size.width * 0.7f
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    },
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterEnd
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.5f).padding(start = 24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Word of the Day ✨", color = Color(0xFF3B82F6), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(2.dp))
                            Text("Serendipity", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B), softWrap = false)
                            Spacer(Modifier.height(4.dp))
                            Text("The occurrence of events by chance in a happy way.", fontSize = 10.sp, color = Color(0xFF64748B), lineHeight = 14.sp, maxLines = 3)
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = {}, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)), modifier = Modifier.height(28.dp), contentPadding = PaddingValues(horizontal = 12.dp)) {
                                Text("Learn more", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 🔹 FEATURE CARDS (Görseller %100 Daha Büyütüldü ve Ortalandı)
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(end = 20.dp)
                ) {
                    items(features) { feature ->
                        Card(
                            modifier = Modifier
                                .size(width = 140.dp, height = 180.dp)
                                .clickable {
                                    if (feature.title == "My Words") navController.navigate("words_screen")
                                    else if (feature.title == "Mistake Jar") navController.navigate("mistake_jar")
                                },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // İkon Alanı: Tamamen devasa ve ortalanmış
                                Box(
                                    modifier = Modifier
                                        .weight(3.5f) // Metne yer açmak için alanı hafif daralttık
                                        .fillMaxWidth()
                                        .clipToBounds(), // Yazının üzerine taşmayı engeller
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = feature.iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize().scale(1.4f).offset(y = (-10).dp), // Resim biraz yukarı taşındı
                                        contentScale = ContentScale.Fit
                                    )
                                }

                                // Metin Alanı
                                Column(
                                    modifier = Modifier.weight(1.5f), // Yazılar için %50 daha fazla alan
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = feature.title,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = feature.desc,
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 🔹 MISSION CARD
            // 🔹 TODAY'S MISSION (Sağda Solan Resim ve %60 Progress Tasarımı)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE)) // Açık mavi arka plan
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {

                        // 1. Resim Alanı: Sağda durur ve sola doğru %40 mesafede solmaya başlar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f) // Sağdan %60'lık alanı kaplar
                                .fillMaxHeight()
                                .align(Alignment.CenterEnd)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.learn_word), // Yeni yüklediğin görsel
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent, // Sol taraf tamamen şeffaf
                                                    Color(0xFFE0F2FE)  // Sağ taraf net (kartın rengi)
                                                ),
                                                startX = 0f,
                                                endX = size.width * 0.4f // %40'a kadar solma efekti
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    },
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterEnd
                            )
                        }

                        // 2. Metin ve Progress Alanı
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth()
                                .padding(start = 24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Today's Mission",
                                color = Color(0xFF3B82F6), // Açık mavi (Primary Blue)
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Learn 15\nnew words", // \n ile "new words" alt satıra atıldı
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black, // Kalın siyah yazı
                                color = Color(0xFF000000), // Tam siyah
                                lineHeight = 26.sp
                            )

                            Spacer(Modifier.height(16.dp))

                            // Progress Bar: Kartın genişliğinin en fazla %60'ına gelebilir
                            Column(modifier = Modifier.fillMaxWidth(0.6f)) {
                                LinearProgressIndicator(
                                    progress = 7 / 15f,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFF3B82F6),
                                    trackColor = Color(0xFFE2E8F0).copy(alpha = 0.5f)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "7 / 15",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // 🔹 KEEP GOING
            item {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("⭐ Keep Going", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Text("See All", color = Color(0xFF3B82F6), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        repeat(5) {
                            Card(modifier = Modifier.size(62.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text("60%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                                }
                            }
                        }
                    }
                }
            }
        }



    }
}
data class FeatureItem(
    val title: String,
    val description: String,
    val iconRes: Int
)

@Composable
fun FeatureSection() {
    val features = listOf(
        FeatureItem("Mistake Jar", "Fix errors", R.drawable.mistake),
        FeatureItem("Reading", "Magic reading", R.drawable.reading),
        FeatureItem("My Words", "Vocabulary", R.drawable.word),
        FeatureItem("Daily Idiom", "Special phrases", R.drawable.daily_idiom),
        FeatureItem("Grammar", "Master rules", R.drawable.grammar)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(features) { item ->
            Card(
                modifier = Modifier.size(width = 140.dp, height = 180.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(3.5f)
                            .fillMaxWidth()
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize().scale(1.4f).offset(y = (-10).dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1.5f).padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = item.description,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color.White, CircleShape)
                .border(3.dp, Color(0xFF1CB0F6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.master),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().padding(6.dp).clip(CircleShape)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text("Good morning", fontSize = 13.sp, color = Color.Gray)
            Text(
                "Ready to learn?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Let's improve your English today!", fontSize = 12.sp)
        }

        Text("🔥 12", fontWeight = FontWeight.Bold)
    }
}
@Composable
fun WordOfDayCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp) // Kart yüksekliğini sabitledik
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Sağdaki Fotoğraf (Sağa doğru solarak arka planla birleşir)
            Image(
                painter = painterResource(id = R.drawable.daily_word), // daily_word olarak yüklediğin resim
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd) // Sağ alta sabitle
                    .fillMaxHeight(0.9f) // Kartın %90'ını kaplasın
                    .padding(end = 8.dp),
                contentScale = ContentScale.Fit
            )

            // 2. Metin İçeriği (Soldan sağa %40 sınırlaması ile)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f) // En fazla %50 kaplasın (Senin isteğin olan %40 civarında kalır)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Word of the Day",
                        color = Color(0xFF3B82F6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star, // Örnek yıldız ikonu
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF3B82F6)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Serendipity",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E293B)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Ses ikonu
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = Color(0xFF3B82F6)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "The occurrence and development of events by chance in a happy or beneficial way.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp,
                    maxLines = 3 // Metin kutucuğu taşmasın diye
                )

                Spacer(Modifier.height(12.dp))

                // Learn More Butonu
                Button(
                    onClick = { /* Git */ },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Learn more →", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}
@Composable
fun FeatureCardsRow(navController: NavHostController) {
    val items = listOf(
        FeatureItem("Mistake Jar", "Fix errors", R.drawable.mistake),
        FeatureItem("Reading", "Stories", R.drawable.reading),
        FeatureItem("My Word", "Vocabulary", R.drawable.word),
        FeatureItem("Grammar", "Rules", R.drawable.grammar)
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .size(width = 140.dp, height = 180.dp)
                    .clickable {
                        if (item.title == "My Word") navController.navigate("words_screen")
                        else if (item.title == "Mistake Jar") navController.navigate("mistake_jar")
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .weight(3.5f)
                            .fillMaxWidth()
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize().scale(1.4f).offset(y = (-10).dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1.5f).padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = item.description,
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun MissionCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE))
    ) {
        Column(Modifier.padding(20.dp)) {

            Text("Today's Mission", color = Color.Blue)

            Text("Learn 15 new words", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(progress = 7 / 15f)
        }
    }
}

@Composable
fun KeepGoingSection() {

    Column {

        Text("Keep Going", fontWeight = FontWeight.Bold)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.SpaceBetween) {

            repeat(4) {
                Card(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("60%")
                    }
                }
            }
        }
    }
}


data class IdiomEntry(
    val title: String,
    val transcript: String
)
