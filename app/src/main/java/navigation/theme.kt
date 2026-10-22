package com.example.ngilizcequizapp


import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
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
// ✅ AppRootScreen with independent theme state
@Composable
fun AppRootScreen(speechAssistant: SpeechAssistant) {
    val context = LocalContext.current
    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val currentTheme = remember(cosmeticsRevision) { UserCosmeticsStore.getEquippedTheme(context) }
    val isSpaceTheme = currentTheme == "uzay"
    val isKingdomTheme = currentTheme == "kingdom"
    val activeDark = currentTheme != "klasik"

    val updateDarkMode: (Boolean) -> Unit = { newValue ->
        UserCosmeticsStore.setDarkMode(context, newValue)
    }

    MewEnglishTheme(darkTheme = activeDark) {
        CompositionLocalProvider(
            LocalIsDarkMode provides activeDark,
            LocalAppThemeColors provides MewThemeEngine.colors(activeDark)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (isSpaceTheme || isKingdomTheme) Color.Transparent else LocalAppThemeColors.current.background
            ) {
                QuizAppNavigation(
                    speechAssistant = speechAssistant,
                    isDarkMode = activeDark,
                    onThemeChange = updateDarkMode
                )
            }
        }
    }
}

@Composable
fun RoyalBookmarkRibbon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(26.dp)
            .height(34.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val ribbonPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(w, 0f)
                lineTo(w, h)
                lineTo(w / 2f, h - 7.dp.toPx()) // V-cut
                lineTo(0f, h)
                close()
            }
            drawPath(path = ribbonPath, color = Color(0xFF1B3A6B))
            drawPath(
                path = ribbonPath,
                color = Color(0xFFFFD700),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
            )
        }
        Text(
            text = "👑",
            fontSize = 10.sp,
            modifier = Modifier.padding(bottom = 5.dp)
        )
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
    val appColors = LocalAppThemeColors.current
    val bgColor = appColors.background
    val heroColor = appColors.primarySoft
    val cardColor = appColors.surface
    val textColor = appColors.text
    val mutedColor = appColors.textMuted
    val primaryColor = appColors.primary
    val borderColor = appColors.border

    val context = LocalContext.current
    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val userXp = remember { QuizTreeProgressStore.getXp(context) }
    val userRank = remember(userXp, cosmeticsRevision) { UserCosmeticsStore.getUserEffectiveRank(context) }
    val equippedAvatarRes = remember(cosmeticsRevision) { UserCosmeticsStore.getEquippedAvatarRes(context) }
    val equippedWingDrawable = remember(cosmeticsRevision, userRank) {
        UserCosmeticsStore.getEquippedWingDrawable(context, userRank)
    }
    var showInventorySheet by remember { mutableStateOf(false) }
    var showCatSelector by remember { mutableStateOf(false) }
    var showWingSelector by remember { mutableStateOf(false) }
    var showActiveBoostsDialog by remember { mutableStateOf(false) }
    var selectedKeepGoingBadge by remember { mutableStateOf<BadgeData?>(null) }
    val incompleteBadges = remember(cosmeticsRevision, userXp) {
        AchievementManager.getClosestIncompleteBadges(context, 5)
    }

    val isSpaceTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isSpaceThemeActive(context) }
    val isKingdomTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isKingdomThemeActive(context) }

    val kingdomGoldBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFDF7A),
                Color(0xFFD4AF37),
                Color(0xFFAA7C11),
                Color(0xFFFFDF7A)
            )
        )
    }

    // Özellikler için veri modeli ve listesi
    data class FeatureItem(val title: String, val desc: String, val iconRes: Int)

    val features = listOf(
        FeatureItem(
            title = "Mistake Jar",
            desc = when {
                isKingdomTheme -> "Kraliyet Hataları"
                isSpaceTheme -> "Kozmik Hatalar"
                else -> "Fix errors"
            },
            iconRes = when {
                isKingdomTheme -> R.drawable.kingdom_mistake_jar
                else -> R.drawable.mistake
            }
        ),
        FeatureItem(
            title = "Reading",
            desc = when {
                isKingdomTheme -> "Kraliyet Okuması"
                isSpaceTheme -> "Kozmik Okuma"
                else -> "Magic reading"
            },
            iconRes = when {
                isKingdomTheme -> R.drawable.kingdom_reading_thumb
                else -> R.drawable.reading
            }
        ),
        FeatureItem(
            title = "My Words",
            desc = when {
                isKingdomTheme -> "Kraliyet Sözlüğü"
                isSpaceTheme -> "Kozmik Sözlük"
                else -> "Vocabulary"
            },
            iconRes = when {
                isKingdomTheme -> R.drawable.kingdom_dict
                else -> R.drawable.word
            }
        ),
        FeatureItem(
            title = "Kelime Quizi",
            desc = when {
                isKingdomTheme -> "Asil Kelime Testi"
                isSpaceTheme -> "Kozmik Kelime Testi"
                else -> "Swipe & Test"
            },
            iconRes = when {
                isKingdomTheme -> R.drawable.kingdom_quiz
                else -> R.drawable.ic_feature_quiz
            }
        ),
        FeatureItem(
            title = "Rozetlerim",
            desc = when {
                isKingdomTheme -> "Kraliyet Nişanları"
                isSpaceTheme -> "Kozmik Rozetler"
                else -> "Kazandığın Rozetler"
            },
            iconRes = when {
                isKingdomTheme -> R.drawable.kingdom_badge
                else -> R.drawable.ic_feature_badge
            }
        )
    )

    val iceGradient = Brush.verticalGradient(
        colors = if (isDarkMode) {
            listOf(
                Color(0xFF103B5C).copy(alpha = 0.50f),
                Color(0xFF0C2742).copy(alpha = 0.22f),
                Color.Transparent
            )
        } else {
            listOf(
                Color(0xFFA5F3FC).copy(alpha = 0.55f),
                Color(0xFFCFFAFE).copy(alpha = 0.25f),
                Color.Transparent
            )
        },
        startY = 0f,
        endY = 850f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSpaceTheme || isKingdomTheme) Color.Transparent else appColors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            // 🔹 HEADER (Wing stuck to the far left, User Info & XP immediately to the right, Actions on the far right)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Cat Avatar & Symmetrical Wing Frame
                    val isKingdomWing = equippedWingDrawable == R.drawable.kingdom_kanat
                    val wingDisplayHeight = if (isKingdomWing) 78.dp else 68.dp
                    val wingDisplayWidth = wingDisplayHeight * 1.84f

                    // Controlled layout width so wings spread naturally without pushing User info / XP chips
                    Box(
                        modifier = Modifier
                            .width(if (isKingdomWing) 92.dp else 84.dp)
                            .height(wingDisplayHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileAvatarWithWings(
                            avatarRes = equippedAvatarRes,
                            wingDrawableRes = equippedWingDrawable,
                            wingHeight = wingDisplayHeight,
                            modifier = Modifier.requiredWidth(wingDisplayWidth),
                            onCatClick = { showCatSelector = true },
                            onWingClick = { showWingSelector = true }
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Person's info & XP count immediately to the right
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable { navController.navigate("settings") },
                        verticalArrangement = Arrangement.Center
                    ) {
                        val userFullName = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
                            com.example.ngilizcequizapp.UserProfileManager.getUserFullName(context)
                        }
                        Text(
                            text = userFullName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.wrapContentWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isKingdomTheme) Color(0xFF0F1E36) else primaryColor.copy(alpha = 0.15f),
                                border = if (isKingdomTheme) BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.8f)) else null
                            ) {
                                Text(
                                    text = userRank.nameStr,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isKingdomTheme) Color(0xFFFFD700) else primaryColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isKingdomTheme) Color(0xFF281C03) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                border = if (isKingdomTheme) BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.8f)) else null
                            ) {
                                Text(
                                    text = "$userXp XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF59E0B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Action buttons (Active Boosts, Blue Backpack & Market)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        ActiveBoostsTopBarButton(
                            compact = true,
                            onClick = { showActiveBoostsDialog = true }
                        )

                        // 🎒 Çanta (Inventory / Stats) Bileşeni
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isKingdomTheme) Color(0xFF0F1E36) else cardColor,
                            border = BorderStroke(1.dp, if (isKingdomTheme) Color(0xFFFFD700).copy(alpha = 0.6f) else borderColor),
                            modifier = Modifier
                                .height(36.dp)
                                .clickable { showInventorySheet = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_blue_bag),
                                    contentDescription = "Envanter Çantası",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Çantam",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isKingdomTheme) Color(0xFFFFD700) else primaryColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        // 🛒 Market Butonu (Geçici olarak pasif / Çok Yakında)
                        if (AppConfig.IS_MARKET_ENABLED) {
                            IconButton(
                                onClick = { navController.navigate(Screen.Market.route) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "Market",
                                    tint = primaryColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 🔹 WORD OF THE DAY / READING BIGBOX
            item {
                val readingImageRes = when {
                    isKingdomTheme -> R.drawable.kingdom_reading
                    isSpaceTheme -> R.drawable.bigboxspace1
                    else -> R.drawable.icecat
                }
                val readingBorder = when {
                    isKingdomTheme -> BorderStroke(1.6.dp, kingdomGoldBrush)
                    isSpaceTheme -> BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8))))
                    else -> null
                }
                val readingCardBg = when {
                    isKingdomTheme -> Color(0xFF070F26).copy(alpha = 0.92f)
                    isSpaceTheme -> Color(0xFF0C132C).copy(alpha = 0.90f)
                    else -> heroColor
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clickable { navController.navigate("reading_screen") },
                    shape = RoundedCornerShape(32.dp),
                    border = readingBorder,
                    colors = CardDefaults.cardColors(containerColor = readingCardBg)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxWidth(0.62f).fillMaxHeight().align(Alignment.CenterEnd)) {
                            Image(
                                painter = painterResource(id = readingImageRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(Color.Transparent, readingCardBg),
                                                startX = 0f,
                                                endX = size.width * 0.42f
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    },
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterEnd
                            )
                        }

                        Column(
                            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.55f).padding(start = 24.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                if (isKingdomTheme) "👑 Kraliyet Okuması" else if (isSpaceTheme) "🚀 Kozmik Okuma" else "Okuma Parçaları",
                                color = if (isKingdomTheme) Color(0xFFFFD700) else primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(if (isKingdomTheme) "Kraliyet Kütüphanesi" else "Reading", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor, softWrap = false)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (isKingdomTheme) "Kraliyet kütüphanesinden seviyeni seç, asil hikayelerle İngilizceni geliştir." else if (isSpaceTheme) "Kozmik kütüphaneden seviyeni seç, kısa parçalarla pratik yap." else "Seviyeni seç, kısa okuma parçalarıyla pratik yap.",
                                fontSize = 10.sp,
                                color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.85f) else mutedColor,
                                lineHeight = 14.sp,
                                maxLines = 3
                            )
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { navController.navigate("reading_screen") },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isKingdomTheme) Color(0xFF1B3A6B) else primaryColor
                                ),
                                border = if (isKingdomTheme) BorderStroke(1.dp, Color(0xFFFFD700)) else null,
                                modifier = Modifier.height(28.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text(
                                    if (isKingdomTheme) "Okumaya başla  ›" else "Okumaya başla",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (isKingdomTheme) {
                            RoyalBookmarkRibbon(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 22.dp)
                            )
                        }
                    }
                }
            }

            // 🔹 KELİME QUİZİ HERO CARD
            item {
                val quizCardBorder = when {
                    isKingdomTheme -> BorderStroke(1.6.dp, kingdomGoldBrush)
                    isSpaceTheme -> BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8))))
                    else -> null
                }
                val quizCardBg = when {
                    isKingdomTheme -> Color(0xFF070F26).copy(alpha = 0.92f)
                    isSpaceTheme -> Color(0xFF0C132C).copy(alpha = 0.90f)
                    else -> heroColor
                }
                val quizHeroImageRes = when {
                    isKingdomTheme -> R.drawable.kingdom_word
                    isSpaceTheme -> R.drawable.bigboxspace2
                    else -> R.drawable.ic_feature_quiz
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clickable { navController.navigate("word_quiz_screen") },
                    shape = RoundedCornerShape(28.dp),
                    border = quizCardBorder,
                    colors = CardDefaults.cardColors(containerColor = quizCardBg),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = quizHeroImageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(alpha = if (isKingdomTheme) 0.42f else if (isSpaceTheme) 0.48f else 0.38f),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            quizCardBg.copy(alpha = 0.96f),
                                            quizCardBg.copy(alpha = 0.75f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isKingdomTheme -> Color(0xFF1B3A6B).copy(alpha = 0.7f)
                                        isSpaceTheme -> Color(0xFF0284C7).copy(alpha = 0.25f)
                                        else -> primaryColor.copy(alpha = 0.15f)
                                    },
                                    border = when {
                                        isKingdomTheme -> BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f))
                                        isSpaceTheme -> BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
                                        else -> null
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            isKingdomTheme -> "👑 KELİME PRATİĞİ"
                                            isSpaceTheme -> "🚀 KOZMİK PRATİK"
                                            else -> "KELİME PRATİĞİ"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isKingdomTheme -> Color(0xFFFFD700)
                                            isSpaceTheme -> Color(0xFF38BDF8)
                                            else -> primaryColor
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Kelime Quizi",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        isKingdomTheme -> Color.White
                                        isSpaceTheme -> Color.White
                                        else -> textColor
                                    }
                                )
                                Text(
                                    text = when {
                                        isKingdomTheme -> "Kaydettiğin asil kelimeleri kaydırarak test et ve pekiştir!"
                                        isSpaceTheme -> "Kozmik sözlüğündeki kelimeleri test et ve pekiştir!"
                                        else -> "Kaydettiğin kelimeleri kaydırarak test et ve pekiştir!"
                                    },
                                    fontSize = 11.sp,
                                    color = when {
                                        isKingdomTheme -> Color(0xFFFFF7D6).copy(alpha = 0.75f)
                                        isSpaceTheme -> Color(0xFFE2E8F0).copy(alpha = 0.85f)
                                        else -> mutedColor
                                    },
                                    maxLines = 2
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = { navController.navigate("word_quiz_screen") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isKingdomTheme -> Color(0xFF1B3A6B)
                                        isSpaceTheme -> Color(0xFF0284C7)
                                        else -> primaryColor
                                    }
                                ),
                                border = when {
                                    isKingdomTheme -> BorderStroke(1.dp, Color(0xFFFFD700))
                                    isSpaceTheme -> BorderStroke(1.dp, Color(0xFF38BDF8))
                                    else -> null
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = when {
                                        isKingdomTheme -> Color(0xFFFFD700)
                                        else -> Color.White
                                    }
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Başlat",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (isKingdomTheme) {
                            RoyalBookmarkRibbon(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 18.dp)
                            )
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
                        val featureCardBorder = when {
                            isKingdomTheme -> BorderStroke(1.4.dp, kingdomGoldBrush)
                            isSpaceTheme -> BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8))))
                            else -> BorderStroke(1.dp, appColors.border.copy(alpha = 0.6f))
                        }
                        val featureCardBg = when {
                            isKingdomTheme -> Color(0xFF070F26).copy(alpha = 0.92f)
                            isSpaceTheme -> Color(0xFF0C132C).copy(alpha = 0.90f)
                            else -> cardColor
                        }

                        Card(
                            modifier = Modifier
                                .size(width = 140.dp, height = 180.dp)
                                .clickable {
                                    if (feature.title == "My Words" || feature.title == "My Word") navController.navigate("words_screen")
                                    else if (feature.title == "Kelime Quizi") navController.navigate("word_quiz_screen")
                                    else if (feature.title == "Mistake Jar") navController.navigate("mistake_jar")
                                    else if (feature.title == "Reading") navController.navigate("reading_screen")
                                    else if (feature.title == "Rozetlerim") navController.navigate("badges_screen")
                                    else Toast.makeText(context, "Bu özellik çok yakında aktif olacaktır ✨", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(24.dp),
                            border = featureCardBorder,
                            colors = CardDefaults.cardColors(containerColor = featureCardBg),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // İkon Alanı: Tamamen devasa ve ortalanmış
                                    Box(
                                        modifier = Modifier
                                            .weight(3.5f)
                                            .fillMaxWidth()
                                            .clipToBounds(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = feature.iconRes),
                                            contentDescription = null,
                                            modifier = if (isKingdomTheme) {
                                                Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp)
                                            } else {
                                                Modifier.fillMaxSize().scale(1.3f).offset(y = (-6).dp)
                                            },
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // Metin Alanı
                                    Column(
                                        modifier = Modifier.weight(1.5f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = feature.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = when {
                                                isKingdomTheme -> Color(0xFFFFF7D6)
                                                isSpaceTheme -> Color.White
                                                else -> textColor
                                            }
                                        )
                                        Text(
                                            text = feature.desc,
                                            fontSize = 10.sp,
                                            color = when {
                                                isKingdomTheme -> Color(0xFFFFF7D6).copy(alpha = 0.7f)
                                                isSpaceTheme -> Color(0xFF94A3B8)
                                                else -> mutedColor
                                            }
                                        )
                                    }
                                }

                                if (isKingdomTheme) {
                                    RoyalBookmarkRibbon(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(end = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 🔹 GÜNLÜK GÖREVLER (MISSION PREVIEW)
            item {
                MissionPreviewCard(navController = navController)
            }

            // 🔹 KEEP GOING (En Yakın Tamamlanmamış Rozetler & Çerçeve Progress)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isKingdomTheme) "👑 Devam Et" else "⭐ Keep Going",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = if (isSpaceTheme || isKingdomTheme) Color.White else textColor
                        )
                        Text(
                            text = "Tümünü Gör",
                            color = if (isKingdomTheme) Color(0xFFFFD700) else primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController.navigate("badges_screen")
                            }
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    if (incompleteBadges.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else cardColor,
                            border = if (isKingdomTheme) BorderStroke(1.2.dp, kingdomGoldBrush) else BorderStroke(1.dp, borderColor)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🏆 Tebrikler! Tüm rozetleri tamamladın!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isKingdomTheme) Color(0xFFFFD700) else primaryColor
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            incompleteBadges.forEach { (badge, progress) ->
                                KeepGoingBadgeCard(
                                    badge = badge,
                                    progress = progress,
                                    isKingdomTheme = isKingdomTheme,
                                    cardColor = cardColor,
                                    onClick = { selectedKeepGoingBadge = badge }
                                )
                            }
                            if (incompleteBadges.size < 5) {
                                repeat(5 - incompleteBadges.size) {
                                    Spacer(modifier = Modifier.size(62.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showInventorySheet) {
        InventoryBottomSheet(
            onDismissRequest = { showInventorySheet = false },
            navController = navController,
            userRank = userRank
        )
    }

    if (showCatSelector) {
        CatSelectorBottomSheet(
            onDismissRequest = { showCatSelector = false }
        )
    }

    if (showWingSelector) {
        WingSelectorBottomSheet(
            currentRank = userRank,
            onDismissRequest = { showWingSelector = false }
        )
    }

    if (showActiveBoostsDialog) {
        ActiveBoostsModalDialog(
            navController = navController,
            onDismiss = { showActiveBoostsDialog = false }
        )
    }

    selectedKeepGoingBadge?.let { badge ->
        BadgePopupDialog(
            badge = badge,
            isUnlocked = AchievementManager.isUnlocked(badge.id),
            onDismiss = { selectedKeepGoingBadge = null }
        )
    }
}

@Composable
private fun KeepGoingBadgeCard(
    badge: BadgeData,
    progress: Float,
    isKingdomTheme: Boolean,
    cardColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "keepGoingProgress"
    )

    val cleanName = remember(badge.assetName) { badge.assetName.substringBeforeLast(".") }
    val resId = remember(cleanName) {
        context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }

    val progressColor = if (isKingdomTheme) Color(0xFFFFD700) else Color(0xFF1CB0F6)
    val trackColor = if (isKingdomTheme) Color(0xFFFFD700).copy(alpha = 0.22f) else Color(0xFF1CB0F6).copy(alpha = 0.22f)

    Card(
        modifier = Modifier
            .size(62.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.94f) else cardColor
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Çerçeve Progress Loading (Mavi veya Krallık temasında Altın Sarısı)
            Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
                val strokeWidth = 2.8.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val radius = diameter / 2f
                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                // Track
                drawCircle(
                    color = trackColor,
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = strokeWidth)
                )

                // Progress Arc
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Rozet İkonu
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = badge.name,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(1.15f),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = badge.name.take(1).uppercase(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = progressColor
                )
            }

            // Yüzdelik Bilgisi (Pill Rozet)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp),
                shape = RoundedCornerShape(6.dp),
                color = if (isKingdomTheme) Color(0xFF070F26) else Color(0xFF0F172A).copy(alpha = 0.90f),
                border = BorderStroke(0.8.dp, progressColor)
            ) {
                Text(
                    text = "%${(progress * 100).toInt()}",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = progressColor,
                    modifier = Modifier.padding(horizontal = 3.5.dp, vertical = 0.5.dp)
                )
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
        FeatureItem("Rozetlerim", "Kazandığın Rozetler", R.drawable.ic_feature_badge)
    )

    val appColors = LocalAppThemeColors.current
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(features) { item ->
            Card(
                modifier = Modifier.size(width = 140.dp, height = 180.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
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
                            color = appColors.text
                        )
                        Text(
                            text = item.description,
                            fontSize = 10.sp,
                            color = appColors.textMuted
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

        val appColors = LocalAppThemeColors.current
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(appColors.surface, CircleShape)
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
fun WordOfDayCard(onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. Sağdaki Fotoğraf (Sağa doğru solarak arka planla birleşir)
            Image(
                painter = painterResource(id = R.drawable.icecat),
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
                        text = "Okuma Parçaları",
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
                        text = "Reading",
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
                    text = "Seviyeni seç, kısa okuma parçalarıyla pratik yap.",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp,
                    maxLines = 3 // Metin kutucuğu taşmasın diye
                )

                Spacer(Modifier.height(12.dp))

                // Learn More Butonu
                Button(
                    onClick = { onClick() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Okumaya başla", fontSize = 11.sp, color = Color.White)
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
        FeatureItem("Rozetlerim", "Rozetler", R.drawable.ic_feature_badge)
    )

    val appColors = LocalAppThemeColors.current
    val context = LocalContext.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items) { item ->
            Card(
                modifier = Modifier
                    .size(width = 140.dp, height = 180.dp)
                    .clickable {
                        if (item.title == "My Word" || item.title == "My Words") navController.navigate("words_screen")
                        else if (item.title == "Mistake Jar") navController.navigate("mistake_jar")
                        else if (item.title == "Reading") navController.navigate("reading_screen")
                        else if (item.title == "Rozetlerim") navController.navigate("badges_screen")
                        else if (item.title == "Kelime Quizi") navController.navigate("word_quiz_screen")
                        else Toast.makeText(context, "Bu özellik çok yakında aktif olacaktır ✨", Toast.LENGTH_SHORT).show()
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = appColors.surface),
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
                            color = appColors.text
                        )
                        Text(
                            text = item.description,
                            fontSize = 10.sp,
                            color = appColors.textMuted
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
