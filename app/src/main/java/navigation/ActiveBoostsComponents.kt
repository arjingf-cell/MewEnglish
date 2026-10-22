package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import navigation.ActiveBoost
import navigation.BoostType
import navigation.MarketBoostStore
import navigation.QueuedBoost

/**
 * Top bar button with lightning icon, "Aktif Eklentiler" text, and badge count.
 */
@Composable
fun ActiveBoostsTopBarButton(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    val activeBoosts = remember(MarketBoostStore.stateRevision) {
        MarketBoostStore.getActiveBoosts(context)
    }
    val hasActive = activeBoosts.isNotEmpty()

    if (compact) {
        Surface(
            shape = CircleShape,
            color = if (hasActive) {
                Color(0xFF8B5CF6).copy(alpha = 0.18f)
            } else {
                if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9)
            },
            border = BorderStroke(
                1.dp,
                if (hasActive) Color(0xFF8B5CF6) else appColors.border
            ),
            modifier = modifier
                .size(36.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Aktif Eklentiler",
                    tint = if (hasActive) Color(0xFFF59E0B) else appColors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
                if (hasActive) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(7.dp)
                            .background(Color(0xFF8B5CF6), CircleShape)
                    )
                }
            }
        }
    } else {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (hasActive) {
                Color(0xFF8B5CF6).copy(alpha = 0.15f)
            } else {
                if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9)
            },
            border = BorderStroke(
                1.dp,
                if (hasActive) Color(0xFF8B5CF6) else appColors.border
            ),
            modifier = modifier
                .height(34.dp)
                .clickable { onClick() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Aktif Eklentiler",
                    tint = if (hasActive) Color(0xFFF59E0B) else appColors.textMuted,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (hasActive) "Aktif (${activeBoosts.size})" else "Eklentiler",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasActive) Color(0xFF8B5CF6) else appColors.textMuted
                )
            }
        }
    }
}

/**
 * Dialog displaying all active boosts, their live countdowns, and queued boosts.
 */
@Composable
fun ActiveBoostsModalDialog(
    navController: NavController? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    // Ticker that updates every second
    var tick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            tick++
        }
    }

    val activeBoosts = remember(tick, MarketBoostStore.stateRevision) {
        MarketBoostStore.getActiveBoosts(context)
    }
    val queuedBoosts = remember(tick, MarketBoostStore.stateRevision) {
        MarketBoostStore.getQueuedBoosts(context)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface),
            border = BorderStroke(1.5.dp, appColors.border),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFF59E0B).copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aktif Eklentiler",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = appColors.text
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = appColors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeBoosts.isEmpty() && queuedBoosts.isEmpty()) {
                    // Empty State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = appColors.textMuted.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Şu anda aktif bir eklentiniz yok",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Markete gidip XP Boost, Coin Boost veya Mega Boost alarak çifte kazanç sağlayabilirsin.",
                            fontSize = 11.sp,
                            color = appColors.textMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (navController != null && AppConfig.IS_MARKET_ENABLED) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    navController.navigate(Screen.Market.route)
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Markete Git", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else if (!AppConfig.IS_MARKET_ENABLED) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = appColors.surfaceRaised,
                                border = BorderStroke(1.dp, appColors.border)
                            ) {
                                Text(
                                    text = "✨ Market Çok Yakında Açılacak!",
                                    color = appColors.textMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        // 1. ACTIVE BOOSTS
                        items(activeBoosts) { boost ->
                            val now = System.currentTimeMillis()
                            val remSec = if (boost.expiryTimestamp > now) (boost.expiryTimestamp - now) / 1000L else 0L
                            val remStr = MarketBoostStore.formatRemainingTime(remSec)

                            val iconDrawable = when (boost.type) {
                                BoostType.MEGA -> R.drawable.mega_boost
                                BoostType.XP -> R.drawable.xpboost
                                BoostType.COIN -> R.drawable.coinboost
                                BoostType.STREAK_SHIELD, BoostType.STREAK_SHIELD_PLUS, BoostType.STREAK_SHIELD_PRIME -> R.drawable.streakkalkan
                            }

                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(1.dp, appColors.border)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = iconDrawable),
                                        contentDescription = boost.name,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = boost.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors.text
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "AKTİF",
                                                    color = Color(0xFF10B981),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = boost.description,
                                            fontSize = 11.sp,
                                            color = appColors.textMuted
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Countdown Badge
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF8B5CF6).copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f))
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "⏱️ Kalan: ",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = appColors.textMuted
                                                )
                                                Text(
                                                    text = remStr,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF8B5CF6)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 2. QUEUED BOOSTS
                        if (queuedBoosts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⏳ Sırada Bekleyen Eklentiler",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textMuted,
                                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                                )
                            }

                            items(queuedBoosts) { qBoost ->
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) appColors.surfaceRaised.copy(alpha = 0.5f) else Color(0xFFF1F5F9)
                                    ),
                                    border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⏳",
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "${qBoost.name} (${qBoost.durationMinutes} Dk)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = appColors.text
                                            )
                                            Text(
                                                text = "Mega Boost bittiğinde otomatik başlayacak.",
                                                fontSize = 10.sp,
                                                color = appColors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("Tamam", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
