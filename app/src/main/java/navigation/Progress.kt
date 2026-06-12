package com.example.ngilizcequizapp

import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ngilizcequizapp.MewTopAppBar
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val BrokenIceBlueText = Color(0xFF2C3E50)
    val VividIceBlue = Color(0xFF1CB0F6)
    val SoftBg = Color(0xFFF8FBFF)

    Scaffold(
        containerColor = SoftBg,
        topBar = {
            MewTopAppBar(scope = scope, drawerState = drawerState)
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp)
        ) {
            // --- ✨ 1. DEV LINGUARIUM ANİMASYONU VE KULLANICI BİLGİLERİ ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically // Video ve metni dikeyde ortalar
                ) {
                    // 🎬 Devasa Animasyon
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(24.dp))
                    ) {
                        VideoLogoPlayer(modifier = Modifier.fillMaxSize())
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Video ile yazı arasındaki boşluk

                    // 📝 İsim ve Rank Bilgileri
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Arjin", // Buraya Firebase'den gelen isim değişkenini bağlayabilirsin
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrokenIceBlueText
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1CB0F6).copy(alpha = 0.1f) // Rank için hafif mavi arka plan
                        ) {
                            Text(
                                text = "Linguarium",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1CB0F6) // VividIceBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Dil Üstadı",
                            fontSize = 12.sp,
                            color = BrokenIceBlueText.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }

            // --- Diğer itemlar (StatCards, LevelProgressCard vb.) aynen devam eder ---

            // --- 📊 2. İSTATİSTİKLER ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatSmallCard("XP", "1.250", "⚡", Color(0xFFFFD93D), Modifier.weight(1f), BrokenIceBlueText)
                    StatSmallCard("Gün", "12", "🔥", Color(0xFFFF6B6B), Modifier.weight(1f), BrokenIceBlueText)
                }
            }

            // --- 🆙 3. SEVİYE KARTI ---
            item {
                LevelProgressCard(VividIceBlue, BrokenIceBlueText)
            }

            // --- 📈 4. HAFTALIK GRAFİK ---
            item {
                WeeklyActivityChart(VividIceBlue, BrokenIceBlueText)
            }

            // --- 🏅 5. ROZETLER ---
            item {
                BadgesList(BrokenIceBlueText)
            }
        }
    }
}

@Composable
fun StatSmallCard(title: String, value: String, emoji: String, accentColor: Color, modifier: Modifier, textColor: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(text = title, fontSize = 12.sp, color = textColor.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun LevelProgressCard(accentColor: Color, textColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE0F2F1))
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = 0.65f,
                    modifier = Modifier.fillMaxSize(),
                    color = accentColor,
                    strokeWidth = 8.dp,
                    trackColor = accentColor.copy(alpha = 0.1f)
                )
                Text("65%", fontWeight = FontWeight.ExtraBold, color = accentColor)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text("A1 Seviyesi", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = textColor)
                Text("Gelişimin harika!", fontSize = 13.sp, color = textColor.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
fun BadgesList(textColor: Color) {
    Column {
        Text("Son Rozetler", fontWeight = FontWeight.Bold, color = textColor)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // ✨ Buradaki her BadgeItem'a 'textColor = textColor' ekledik
            BadgeItem("İlk Ders", "🎉", Color(0xFFFFD93D), textColor = textColor)
            BadgeItem("10 Kelime", "📚", Color(0xFF4FC3F7), textColor = textColor)
            BadgeItem("3 Gün 🔥", "🔥", Color(0xFFFF6B6B), textColor = textColor)
        }
    }
}

@Composable
fun BadgeItem(
    title: String,
    emoji: String,
    accentColor: Color,
    textColor: Color // 👈 Hatayı çözmek için bunu ekledik
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(accentColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.8f) // 👈 Artık 'BrokenIceBlueText' yerine 'textColor' kullanıyoruz
        )
    }
}

@Composable
fun WeeklyActivityChart(accentColor: Color, textColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Haftalık Aktivite", fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(24.dp))
            Canvas(modifier = Modifier.fillMaxSize()) {
                val bars = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.3f, 0.6f, 0.8f)
                val barWidth = 25.dp.toPx()
                val spacing = (size.width - (bars.size * barWidth)) / (bars.size - 1)
                bars.forEachIndexed { index, heightFactor ->
                    val x = index * (barWidth + spacing)
                    val barHeight = size.height * heightFactor
                    drawRoundRect(
                        color = accentColor.copy(alpha = if(index == 3) 1f else 0.4f),
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }
            }
        }
    }
}

//rank animasyonları
@OptIn(UnstableApi::class)
@Composable
fun VideoLogoPlayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // Videonun dosya ismini (örneğin rank_anim) buraya yazmalısın
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.linguarium}")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL // Sonsuz döngü
            volume = 0f // Sesini kapat
            prepare()
            playWhenReady = true // Otomatik başlat
        }
    }

    // Ekrandan çıkınca player'ı durdurmak için
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false // Kontrol butonlarını gizle
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setBackgroundColor(android.graphics.Color.TRANSPARENT) // Arka plan şeffaf
            }
        },
        modifier = modifier
    )
}

