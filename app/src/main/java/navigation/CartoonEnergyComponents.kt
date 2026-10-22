package com.example.ngilizcequizapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ngilizcequizapp.EnergyManager
import com.example.ngilizcequizapp.LocalAppThemeColors
import com.example.ngilizcequizapp.LocalIsDarkMode
import com.example.ngilizcequizapp.R
import android.widget.Toast
import kotlinx.coroutines.delay

/**
 * Canlı saniye sayacı ile enerji durumunu reaktif takip eden Composable state
 */
data class EnergyState(
    val energy: Int,
    val remainingSeconds: Long,
    val countdownText: String,
    val progress: Float,
    val isFull: Boolean
)

@Composable
fun rememberEnergyState(): EnergyState {
    val context = LocalContext.current
    var currentEnergy by remember(EnergyManager.stateRevision) {
        mutableIntStateOf(EnergyManager.getEnergy(context))
    }
    var remainingSeconds by remember(EnergyManager.stateRevision) {
        mutableLongStateOf(EnergyManager.getRemainingSecondsToNextEnergy(context))
    }
    var progress by remember(EnergyManager.stateRevision) {
        mutableFloatStateOf(EnergyManager.getRegenProgress(context))
    }

    // Enerji 5'ten küçükse her saniye geri sayımı akıcı şekilde güncelle
    LaunchedEffect(currentEnergy, EnergyManager.stateRevision) {
        while (currentEnergy < EnergyManager.MAX_ENERGY) {
            delay(1000L)
            currentEnergy = EnergyManager.getEnergy(context)
            remainingSeconds = EnergyManager.getRemainingSecondsToNextEnergy(context)
            progress = EnergyManager.getRegenProgress(context)
        }
    }

    return EnergyState(
        energy = currentEnergy,
        remainingSeconds = remainingSeconds,
        countdownText = EnergyManager.formatCountdown(remainingSeconds),
        progress = progress,
        isFull = currentEnergy >= EnergyManager.MAX_ENERGY
    )
}

/**
 * Cartoon Tarzı Animasyonlu Şimşek / Enerji İkonu
 * - Zıplama & nefes alma (floating bounce & pulse)
 * - Hafif tatlı açı rotasyonu
 * - Çevresinde uçuşan minik parlak yıldızlar
 */
@Composable
fun CartoonEnergyIcon(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp,
    isFull: Boolean = false,
    isZero: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cartoon_energy_anim")

    // Yumuşak cartoon nefes alma / pulse
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Hafif dikey süzülme (bounce)
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    // Minik eğlenceli tilt
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Yıldız parıltısı opaklığı
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle"
    )

    Box(
        modifier = modifier.size(size + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dolu olduğunda arkadaki sıcak altın ışıltı
        if (isFull) {
            Box(
                modifier = Modifier
                    .size(size + 6.dp)
                    .scale(scaleAnim)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFD54F).copy(alpha = 0.45f), Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }

        // Ana Şimşek Görseli (Cartoon)
        Image(
            painter = painterResource(
                id = if (isZero) R.drawable.ic_cartoon_energy_empty else R.drawable.ic_cartoon_energy_bolt
            ),
            contentDescription = "Mew Enerji",
            modifier = Modifier
                .size(size)
                .graphicsLayer {
                    translationY = floatAnim
                    rotationZ = if (isZero) 0f else rotationAnim
                    scaleX = if (isZero) 0.95f else scaleAnim
                    scaleY = if (isZero) 0.95f else scaleAnim
                }
        )

        // Minik karikatürize parıltı yıldızları (sıfır değilse)
        if (!isZero) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFEB3B),
                modifier = Modifier
                    .size(size * 0.36f)
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-2).dp)
                    .graphicsLayer {
                        alpha = sparkleAlpha
                        scaleX = sparkleAlpha
                        scaleY = sparkleAlpha
                    }
            )
        }
    }
}

/**
 * Üst Barlarda yer alan şık, interaktif Cartoon Enerji Rozeti (Badge)
 * Tıklandığında detaylı enerji modalını açar.
 */
@Composable
fun CartoonEnergyBadge(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val energyState = rememberEnergyState()
    val context = LocalContext.current
    val isDark = LocalIsDarkMode.current
    var showDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDark) Color(0xFF1E1705) else Color(0xFFFFF9E6)
    val borderColor = if (isDark) Color(0xFFB45309) else Color(0xFFFFD54F)
    val textColor = if (isDark) Color(0xFFFFE082) else Color(0xFFB45309)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable {
                if (onClick != null) onClick() else showDialog = true
            },
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CartoonEnergyIcon(
                size = 22.dp,
                isFull = energyState.isFull,
                isZero = energyState.energy <= 0
            )

            val isUnlimited = remember(EnergyManager.stateRevision, UserProfileManager.stateRevision) {
                EnergyManager.isUnlimitedEnergy(context)
            }

            Spacer(Modifier.width(4.dp))

            Text(
                text = if (isUnlimited) "Sınırsız ⚡" else "${energyState.energy}/${EnergyManager.MAX_ENERGY}",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = if (isUnlimited) Color(0xFFFFD700) else textColor
            )

            // Enerji tam dolu değilse ve sınırsız değilse yanına tatlı bir mini sayaç ekle
            if (!energyState.isFull && !isUnlimited) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDark) Color(0xFF2E2207) else Color(0xFFFFECB3),
                    modifier = Modifier.padding(vertical = 1.dp)
                ) {
                    Text(
                        text = energyState.countdownText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        CartoonEnergyDialog(onDismiss = { showDialog = false })
    }
}

/**
 * Detaylı Cartoon Enerji Bilgilendirme ve Durum Modalı
 */
@Composable
fun CartoonEnergyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val energyState = rememberEnergyState()
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val isDemo = remember { UserProfileManager.isDemoMode(context) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = appColors.surface,
            border = BorderStroke(2.dp, Color(0xFFFFD54F)),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık & Kapat Butonu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Mew Enerjisi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = appColors.text
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = appColors.textMuted
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Büyük Animasyonlu Cartoon Şimşek Pod'u
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF3C4), Color(0xFFFFE082).copy(alpha = 0.35f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CartoonEnergyIcon(
                        size = 64.dp,
                        isFull = energyState.isFull,
                        isZero = energyState.energy <= 0
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Enerji Miktarı
                Text(
                    text = "${energyState.energy} / ${EnergyManager.MAX_ENERGY} Enerji",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD97706)
                )

                Spacer(Modifier.height(14.dp))

                // 5'li Cartoon Enerji Yuvaları
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(EnergyManager.MAX_ENERGY) { index ->
                        val isSlotFilled = index < energyState.energy
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSlotFilled) {
                                if (isDark) Color(0xFF3B2A06) else Color(0xFFFFF8E1)
                            } else {
                                if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)
                            },
                            border = BorderStroke(
                                1.5.dp,
                                if (isSlotFilled) Color(0xFFFFCA28) else Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(
                                        id = if (isSlotFilled) R.drawable.ic_cartoon_energy_bolt else R.drawable.ic_cartoon_energy_empty
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(if (isSlotFilled) 28.dp else 22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Sayaç & Durum Kutusu
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF182234) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, appColors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (energyState.isFull) {
                            Text(
                                text = "✨ Enerjin Tamamen Dolu!",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF16A34A)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Quiz çözmeye veya okuma yapmaya hemen başlayabilirsin.",
                                fontSize = 12.sp,
                                color = appColors.textMuted,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sonraki Enerji:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textMuted
                                )
                                Text(
                                    text = energyState.countdownText,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFD97706)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Yenilenme Progress Bar
                            LinearProgressIndicator(
                                progress = { energyState.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(9.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                color = Color(0xFFF59E0B),
                                trackColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = "⏳ Her 30 dakikada 1 enerji yenilenir.",
                                fontSize = 11.sp,
                                color = appColors.textMuted
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Bilgilendirme Maddeleri
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎯", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Her yeni quiz ve okuma parçasına başlarken 1 enerji harcanır.",
                            fontSize = 12.sp,
                            color = appColors.text,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛡️", fontSize = 14.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Soru içindeki yanlışlarında enerjin eksilmez!",
                            fontSize = 12.sp,
                            color = appColors.text,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 🎬 Ödüllü Reklam ile +1 Enerji Kazan Butonu
                if (energyState.energy < EnergyManager.MAX_ENERGY) {
                    val activity = context.findActivity()
                    val isAdLoading = RewardedAdManager.isLoadingAd
                    Button(
                        onClick = {
                            if (activity != null) {
                                RewardedAdManager.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = { amount ->
                                        EnergyManager.addEnergy(context, amount)
                                        Toast.makeText(
                                            context,
                                            "⚡ Tebrikler! +$amount Enerji Kazandın!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    },
                                    onAdFailedToShow = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        if (isAdLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Reklam Yükleniyor...", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        } else {
                            Text("🎬 Reklam İzle (+1 Enerji Kazan) ⚡", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }

                // Test / Demo Modu için Doldurma Butonu
                if (isDemo || energyState.energy < EnergyManager.MAX_ENERGY) {
                    OutlinedButton(
                        onClick = {
                            EnergyManager.refillFull(context)
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Enerjiyi Doldur (Test)", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Harika! ⚡",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Enerji Kalmadığında (0 Enerji) Açılan Cartoon Hata / Bekleme Modalı
 */
@Composable
fun OutOfEnergyDialog(
    onDismiss: () -> Unit,
    onRefillDemo: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val energyState = rememberEnergyState()
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = appColors.surface,
            border = BorderStroke(2.dp, Color(0xFFEF4444)),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cartoon Boş Enerji Görseli
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFEE2E2), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CartoonEnergyIcon(
                        size = 54.dp,
                        isFull = false,
                        isZero = true
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "⚡ Enerjin Tükendi!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFDC2626)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Yeni bir teste veya okuma parçasına başlayabilmek için en az 1 enerjiye ihtiyacın var.",
                    fontSize = 13.sp,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(16.dp))

                // Canlı Geri Sayım Kutusu
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isDark) Color(0xFF2B1616) else Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sonraki Enerjinin Dolmasına:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.textMuted
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = energyState.countdownText,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFDC2626)
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { energyState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            color = Color(0xFFEF4444),
                            trackColor = if (isDark) Color(0xFF451A1A) else Color(0xFFFEE2E2)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Her 30 dakikada 1 enerji otomatik yenilenir.",
                            fontSize = 11.sp,
                            color = appColors.textMuted
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 🎬 Ödüllü Reklam ile +1 Enerji Kazan Butonu
                val activity = context.findActivity()
                val isAdLoading = RewardedAdManager.isLoadingAd
                Button(
                    onClick = {
                        if (activity != null) {
                            RewardedAdManager.showRewardedAd(
                                activity = activity,
                                onRewardEarned = { amount ->
                                    EnergyManager.addEnergy(context, amount)
                                    Toast.makeText(
                                        context,
                                        "⚡ Harika! +$amount Enerji Kazandın!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onDismiss()
                                },
                                onAdFailedToShow = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(context, "Hata: Aktivite bulunamadı.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981) // Canlı zümrüt yeşili
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 10.dp)
                ) {
                    if (isAdLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Reklam Yükleniyor...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "🎬 Reklam İzle (+1 Enerji Kazan) ⚡",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }

                // Test / Hızlı Doldurma Butonu (Demo modu)
                OutlinedButton(
                    onClick = {
                        EnergyManager.refillFull(context)
                        onRefillDemo?.invoke()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Enerjiyi Doldur (Test)", color = Color(0xFFD97706), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Tamam, Bekleyeceğim",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
