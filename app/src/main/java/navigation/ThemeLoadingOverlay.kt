package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun UniversalThemeLoadingOverlay(
    themeId: String,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val normalizedTheme = themeId.lowercase().trim()
    val isKingdom = normalizedTheme in listOf("kingdom", "krallik")
    val isSpace = normalizedTheme in listOf("uzay", "space")

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔹 ARKA PLAN
            when {
                isKingdom -> {
                    Image(
                        painter = painterResource(id = R.drawable.kingdom_arkaplan),
                        contentDescription = "Krallık Arka Planı",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Yumuşak karartma / atmosfer katmanı
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF040816).copy(alpha = 0.45f))
                    )
                }
                isSpace -> {
                    SpaceThemeBackground(
                        modifier = Modifier.fillMaxSize(),
                        showPlanets = false,
                        forceShow = true
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0B132B), Color(0xFF1C2541))
                                )
                            )
                    )
                }
            }

            // 🔹 ORTA YÜKLEME KARTI / AVATAR + KANATLAR
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val avatarRes = UserCosmeticsStore.getEquippedAvatarRes(context)

                val infiniteTransition = rememberInfiniteTransition(label = "themePulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 0.94f,
                    targetValue = 1.09f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                val glowColor = when {
                    isKingdom -> Color(0xFFFFD700) // Kraliyet Altını
                    isSpace -> Color(0xFF38BDF8)   // Kozmik Neon Mavi
                    else -> Color(0xFF3B82F6)
                }

                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Nabız Işıltısı
                    Box(
                        modifier = Modifier
                            .size(165.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    listOf(glowColor.copy(alpha = 0.55f), Color.Transparent)
                                ),
                                CircleShape
                            )
                    )

                    if (isKingdom) {
                        // Krallık kanadı ve avatar birleşimi
                        ProfileAvatarWithWings(
                            avatarRes = avatarRes,
                            wingDrawableRes = R.drawable.kingdom_kanat,
                            wingHeight = 90.dp,
                            avatarDiameter = 68.dp
                        )
                    } else {
                        // Profil fotoğrafı çerçevesi
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .border(3.5.dp, glowColor, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFF0C132C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = avatarRes),
                                contentDescription = "Profil Resmi",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                val titleText = when {
                    isKingdom -> "Krallık Teması Yükleniyor..."
                    isSpace -> "Uzay Teması Yükleniyor..."
                    else -> "Tema Yükleniyor..."
                }

                val subtitleText = when {
                    isKingdom -> "👑 Kraliyet sarayı ve altın işlemeler hazırlanıyor..."
                    isSpace -> "🚀 Kozmik galaksiye geçiş yapılıyor..."
                    else -> "Lütfen bekleyin..."
                }

                Text(
                    text = titleText,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = subtitleText,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(1800L)
        onFinished()
    }
}
