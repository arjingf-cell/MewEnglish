package com.example.ngilizcequizapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import navigation.MarketBoostStore
import navigation.BoostType
import navigation.ActiveBoost
import com.airbnb.lottie.compose.rememberLottieComposition

// Global Data Models
data class BoostWarningDialogData(
    val title: String,
    val message: String,
    val confirmText: String = "Evet, Onayla",
    val cancelText: String = "Hayır, Vazgeç",
    val onConfirm: () -> Unit
)

data class MarketItemDialogData(
    val title: String,
    val category: String,
    val description: String,
    val priceGems: Int = 0,
    val priceCoins: Int = 0,
    val isRealMoney: Boolean = false,
    val realMoneyPrice: String = "",
    val productId: String = "",
    val iconName: String = "",
    val lottieRes: Int? = null,
    val isCat: Boolean = false,
    val isWing: Boolean = false,
    val wingData: WingItemData? = null,
    val isFrame: Boolean = false,
    val isBoost: Boolean = false,
    val isTheme: Boolean = false,
    val isCurrencyPack: Boolean = false,
    val isOwned: Boolean = false,
    val isEquipped: Boolean = false,
    val onConfirmCoins: (() -> Unit)? = null,
    val onConfirmGems: (() -> Unit)? = null,
    val onConfirmRealMoney: (() -> Unit)? = null,
    val onConfirm: (() -> Unit)? = null
)
data class CurrencyPack(
    val amount: String,
    val typeName: String,
    val price: String,
    val lottieRes: Int,
    val tag: String? = null,
    val productId: String = ""
)

data class BoostItem(
    val name: String,
    val desc: String,
    val priceCoins: Int,
    val priceGems: Int,
    val iconName: String
)

data class ThemeItem(
    val name: String,
    val desc: String,
    val priceCoins: Int = 0,
    val priceGems: Int = 0,
    val isRealMoneyOnly: Boolean = false,
    val realMoneyPrice: String = "",
    val productId: String = "",
    val iconName: String,
    val isActive: Boolean = false
)

data class CatItem(
    val name: String,
    val priceCoins: Int,
    val priceGems: Int,
    val iconName: String,
    val isActive: Boolean = false
)

data class FrameItem(
    val name: String,
    val priceCoins: Int,
    val priceGems: Int,
    val iconName: String
)

// Helper function to resolve drawables with fallback to readingcat
fun getMarketDrawableRes(context: Context, name: String): Int {
    val cleanName = name.removeSuffix(".png").removeSuffix(".xml").removeSuffix(".jpg").lowercase().trim()
    when (cleanName) {
        "streakk", "streakkalkan" -> return R.drawable.streakkalkan
        "akademisyen", "akademisyen_mew" -> return R.drawable.akademisyen_mew
        "havali", "cool_mew", "cool" -> return R.drawable.cool_mew
        "kaptan", "pirate_mew", "pirate", "korsan" -> return R.drawable.pirate_mew
        "buyucu", "wizard_mew", "wizard" -> return R.drawable.wizard_mew
        "master", "maestro_mew", "maestro", "usta" -> return R.drawable.maestro_mew
        "king", "king_mew", "kral", "kral_mew" -> return R.drawable.king_mew
        "megaboost", "mega_boost" -> return R.drawable.mega_boost
        "xpboost", "xp_boost" -> return R.drawable.xpboost
        "coinboost", "coin_boost" -> return R.drawable.coinboost
        "kingdom", "krallik", "kingdom_theme" -> return R.drawable.kingdom_reading
        "kingdom_kanat" -> return R.drawable.kingdom_kanat
        "kingdom_ana_ekran" -> return R.drawable.kingdom_ana_ekran
        "kingdom_arkaplan" -> return R.drawable.kingdom_arkaplan
    }
    val id = context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    return if (id != 0) id else R.drawable.readingcat
}

@Composable
fun MarketScreen(navController: NavHostController, isDarkMode: Boolean) {
    val context = LocalContext.current
    
    // Fetch user coins and gems dynamically
    var coins by remember { mutableIntStateOf(QuizTreeProgressStore.getCoins(context)) }
    var gems by remember { mutableIntStateOf(QuizTreeProgressStore.getGems(context)) }

    var remainingSeconds by remember { mutableLongStateOf(MarketBoostStore.getRemainingSeconds(context)) }
    LaunchedEffect(MarketBoostStore.stateRevision) {
        while (true) {
            remainingSeconds = MarketBoostStore.getRemainingSeconds(context)
            delay(1000L)
        }
    }

    var selectedDialogItem by remember { mutableStateOf<MarketItemDialogData?>(null) }
    var loadingThemeId by remember { mutableStateOf<String?>(null) }
    var showActiveBoostsDialog by remember { mutableStateOf(false) }
    var boostWarningDialog by remember { mutableStateOf<BoostWarningDialogData?>(null) }
    var showCatSelector by remember { mutableStateOf(false) }
    var showMarketLinkDialog by remember { mutableStateOf(false) }
    var pendingPurchaseAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val isRestoring by BillingManager.isRestoring.collectAsState()

    LaunchedEffect(Unit) {
        BillingManager.events.collect { event ->
            when (event) {
                is BillingEvent.PurchaseSuccess -> {
                    coins = QuizTreeProgressStore.getCoins(context)
                    gems = QuizTreeProgressStore.getGems(context)
                    if (event.productId == BillingProducts.INAPP_THEME_KINGDOM) {
                        loadingThemeId = "kingdom"
                    } else if (event.productId == BillingProducts.INAPP_THEME_SPACE) {
                        loadingThemeId = "uzay"
                    }
                    val name = BillingProducts.getProductName(event.productId)
                    Toast.makeText(context, "🎉 Tebrikler! $name başarıyla hesabınıza tanımlandı!", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseCancelled -> {
                    Toast.makeText(context, "Satın alma işlemi iptal edildi.", Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.PurchasePending -> {
                    Toast.makeText(context, "⏳ Ödemeniz onay bekliyor. Onaylandığında ürününüz otomatik olarak yüklenecektir.", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseError -> {
                    Toast.makeText(context, "⚠️ Ödeme Hatası: ${event.message}", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseFailed -> {
                    Toast.makeText(context, "Ödeme tamamlanamadı: ${event.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.ItemAlreadyOwned -> {
                    coins = QuizTreeProgressStore.getCoins(context)
                    gems = QuizTreeProgressStore.getGems(context)
                    Toast.makeText(context, "Bu ürüne zaten sahipsiniz. Haklarınız yenilendi.", Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.PurchasesRestored -> {
                    coins = QuizTreeProgressStore.getCoins(context)
                    gems = QuizTreeProgressStore.getGems(context)
                    val msg = if (event.message.isNotBlank()) {
                        event.message
                    } else if (event.restoredCount > 0) {
                        "Tebrikler! ${event.restoredCount} adet satın alımınız geri yüklendi."
                    } else {
                        "Aktif bir satın alım veya abonelik kaydı bulunamadı."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val safeExecutePurchase: (() -> Unit) -> Unit = { action ->
        if (UserProfileManager.isAnonymous(context)) {
            pendingPurchaseAction = action
            showMarketLinkDialog = true
        } else {
            action()
        }
    }

    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val equippedAvatarRes = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.getEquippedAvatarRes(context)
    }
    val equippedTopBarAvatarRes = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.getEquippedTopBarAvatarRes(context)
    }
    var showInventorySheet by remember { mutableStateOf(false) }
    val userXp = remember { QuizTreeProgressStore.getXp(context) }
    val userRank = remember(userXp) { getRankForXp(userXp) }

    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    // Colors
    val backgroundColor = appColors.background
    val cardColor = appColors.surface
    val textColor = appColors.text
    val subtitleColor = appColors.textMuted
    val primaryColor = appColors.primary
    val borderColor = appColors.border

    val isSpaceTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSpaceTheme) Color.Transparent else backgroundColor),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 🔹 1. CUSTOM TOP STATUS BAR / HEADER
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (!navController.popBackStack()) {
                                navController.navigate(Screen.Home.route)
                            }
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = textColor
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Circular Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(cardColor, CircleShape)
                            .border(2.dp, primaryColor, CircleShape)
                            .clip(CircleShape)
                            .clickable { showCatSelector = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = equippedTopBarAvatarRes),
                            contentDescription = "Profil Resmi",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title and Subtitle
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Market",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor,
                            maxLines = 1
                        )
                        Text(
                            text = "Öğren, kazan, geliştir!",
                            fontSize = 10.sp,
                            color = subtitleColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Active Boosts Button
                    ActiveBoostsTopBarButton(
                        onClick = { showActiveBoostsDialog = true }
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    // Backpack / Inventory Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .height(36.dp)
                            .clickable { showInventorySheet = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_blue_bag),
                                contentDescription = "Çantam",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Çantam",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Restore Purchases Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .height(36.dp)
                            .clickable(enabled = !isRestoring) {
                                BillingManager.restorePurchases(context) { _, count, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4CAF50)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Satın Alımları Geri Yükle",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isRestoring) "..." else "Geri Yükle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                }

                // Balance Info (Gems and Coins) - Moved down below!
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Gems Container
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", gems),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B0FF)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                                    .clickable {
                                        QuizTreeProgressStore.addGems(context, 100)
                                        gems = QuizTreeProgressStore.getGems(context)
                                        Toast.makeText(context, "100 Elmas Eklendi!", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Gems",
                                    tint = primaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Coins Container
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            LottieIcon(rawRes = R.raw.anim_coin, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%,d", coins),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                                    .clickable {
                                        QuizTreeProgressStore.addCoins(context, 1000)
                                        coins = QuizTreeProgressStore.getCoins(context)
                                        Toast.makeText(context, "1,000 Altın Eklendi!", Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Coins",
                                    tint = primaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                // ⚡ Active Boost Countdown Banner
                val activeBoost = remember(MarketBoostStore.stateRevision, remainingSeconds) {
                    MarketBoostStore.getActiveBoost(context)
                }
                if (activeBoost != null && remainingSeconds > 0) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF8B5CF6).copy(alpha = 0.14f),
                        border = BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showActiveBoostsDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF8B5CF6),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = "⚡", fontSize = 16.sp)
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${activeBoost.name} Aktif!",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF8B5CF6)
                                    )
                                    Text(
                                        text = if (activeBoost.multiplier > 1) "${activeBoost.multiplier}X Ödül Çarpanı devrede" else "Seri koruması devrede",
                                        fontSize = 10.sp,
                                        color = subtitleColor
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF8B5CF6),
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "⏳ " + MarketBoostStore.formatRemainingTime(remainingSeconds),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 🔹 2. TOP BANNER (Daha fazlasını keşfet)
        item {
            val bannerBorder = if (isSpaceTheme) {
                BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF818CF8), Color(0xFF38BDF8))))
            } else null
            val bannerCardBg = if (isSpaceTheme) Color(0xFF0C132C).copy(alpha = 0.90f)
            else if (isDark) Color(0xFF262C54)
            else Color(0xFF6B78E8)

            val bannerBg = if (isSpaceTheme) Modifier.background(Color(0xFF0C132C).copy(alpha = 0.90f))
            else if (isDark) Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF384078), Color(0xFF262C54))))
            else Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF808CF0), Color(0xFF6B78E8))))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(180.dp),
                shape = RoundedCornerShape(32.dp),
                border = bannerBorder,
                colors = CardDefaults.cardColors(containerColor = bannerCardBg)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().then(bannerBg)
                ) {
                    // Banner Content
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, top = 20.dp, bottom = 20.dp, end = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isSpaceTheme) "⭐ Galaktik Fırsatlar" else "Daha fazlasını keşfet",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = if (isSpaceTheme) "Kozmik Mew\nMarketi Seni\nBekliyor!" else "Öğrenmeni\nhızlandıracak\nürünler seni bekliyor!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                lineHeight = 24.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF4A7CF5),
                                modifier = Modifier
                                    .clickable {
                                        navController.navigate("premium_screen")
                                    }
                            ) {
                                Text(
                                    text = "Hemen premium satın al!",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Cat Image (Identical layout and placement for Space, Dark, Light, and Kingdom themes)
                        val bannerImgRes = if (isSpaceTheme) R.drawable.bigboxspace5 else getMarketDrawableRes(context, "marketcar")
                        Box(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Image(
                                painter = painterResource(id = bannerImgRes),
                                contentDescription = if (isSpaceTheme) "Market Space Cat" else "Market Cat",
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.CenterEnd,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    bannerCardBg
                                                ),
                                                startX = 0f,
                                                endX = size.width * 0.42f
                                            ),
                                            blendMode = BlendMode.DstIn
                                        )
                                    }
                            )
                        }
                    }
                }
            }
        }

        // 🔹 3. ELMAS & ALTIN SECTION
        item {
            Column {
                SectionHeader(title = "Elmas & Altın")
                Spacer(modifier = Modifier.height(12.dp))

                val currencyPacks = listOf(
                    CurrencyPack("60", "Elmas", BillingManager.getFormattedPrice(BillingProducts.INAPP_GEMS_60, "₺29,99"), R.raw.anim_diamond, productId = BillingProducts.INAPP_GEMS_60),
                    CurrencyPack("300", "Elmas", BillingManager.getFormattedPrice(BillingProducts.INAPP_GEMS_300, "₺129,99"), R.raw.anim_diamond, "Popüler", productId = BillingProducts.INAPP_GEMS_300),
                    CurrencyPack("900", "Elmas", BillingManager.getFormattedPrice(BillingProducts.INAPP_GEMS_900, "₺349,99"), R.raw.anim_diamond, "En iyi fırsat", productId = BillingProducts.INAPP_GEMS_900),
                    CurrencyPack("5.000", "Altın", BillingManager.getFormattedPrice(BillingProducts.INAPP_COINS_5000, "₺59,99"), R.raw.anim_coin, productId = BillingProducts.INAPP_COINS_5000),
                    CurrencyPack("25.000", "Altın", BillingManager.getFormattedPrice(BillingProducts.INAPP_COINS_25000, "₺239,99"), R.raw.anim_coin, productId = BillingProducts.INAPP_COINS_25000)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currencyPacks) { pack ->
                        CurrencyCard(pack = pack, cardColor = cardColor, textColor = textColor, isDarkMode = isDarkMode) {
                            selectedDialogItem = MarketItemDialogData(
                                title = "${pack.amount} ${pack.typeName}",
                                category = "Bakiye Paketi",
                                description = "${pack.amount} ${pack.typeName} anında hesabına tanımlanır. Marketteki tüm karakterleri, kanatları ve 2x boostları açmak için kullanabilirsin.",
                                isRealMoney = true,
                                realMoneyPrice = pack.price,
                                productId = pack.productId,
                                lottieRes = pack.lottieRes,
                                isCurrencyPack = true,
                                onConfirmRealMoney = {
                                    val activity = context as? Activity
                                    if (activity != null) {
                                        BillingManager.launchBillingFlow(activity, pack.productId)
                                    } else {
                                        Toast.makeText(context, "Ödeme ekranı açılamadı", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // 🔹 4. BOOSTLAR & KORUMALAR SECTION
        item {
            Column {
                SectionHeader(title = "Boostlar & Korumalar")
                Spacer(modifier = Modifier.height(12.dp))

                val boosts = listOf(
                    BoostItem("Streak Kalkanı", "24 saat serini korur.", priceCoins = 250, priceGems = 10, iconName = "streakkalkan"),
                    BoostItem("Streak Kalkanı Plus", "7 gün boyunca serini korur.", priceCoins = 1250, priceGems = 50, iconName = "streakkalkan"),
                    BoostItem("Streak Kalkanı Prime", "30 gün asil seri koruması.", priceCoins = 5000, priceGems = 200, iconName = "streakkalkan"),
                    BoostItem("XP Boost", "Deneyim kazanımını 2 katına çıkarır.", priceCoins = 3000, priceGems = 75, iconName = "xpboost"),
                    BoostItem("Coin Boost", "Altın kazanımını 2 katına çıkarır.", priceCoins = 0, priceGems = 25, iconName = "coinboost"),
                    BoostItem("Mega Boost Paketi", "Tüm boostları aktif eder!", priceCoins = 6000, priceGems = 120, iconName = "mega_boost")
                )

                fun handleBoostPurchase(boost: BoostItem, useCoins: Boolean) {
                    val costCoins = boost.priceCoins
                    val costGems = boost.priceGems

                    if (useCoins && costCoins <= 0) {
                        Toast.makeText(context, "Bu ürün sadece Elmas ile satın alınabilir!", Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (useCoins && boost.name.contains("Streak Kalkanı")) {
                        val solvedCount = QuizTreeProgressStore.getSolvedQuizzesCount(context)
                        if (solvedCount < 30) {
                            Toast.makeText(
                                context,
                                "Coin ile Streak Kalkanı alabilmek için en az 30 başarılı quiz çözmüş olmalısın! (Mevcut: $solvedCount/30)",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }
                    }

                    if (useCoins && coins < costCoins) {
                        Toast.makeText(context, "Yetersiz Altın (Coin)!", Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (!useCoins && gems < costGems) {
                        Toast.makeText(context, "Yetersiz Elmas!", Toast.LENGTH_SHORT).show()
                        return
                    }

                    val boostType = when (boost.name) {
                        "XP Boost" -> BoostType.XP
                        "Coin Boost" -> BoostType.COIN
                        "Mega Boost Paketi" -> BoostType.MEGA
                        "Streak Kalkanı Prime" -> BoostType.STREAK_SHIELD_PRIME
                        "Streak Kalkanı Plus" -> BoostType.STREAK_SHIELD_PLUS
                        else -> BoostType.STREAK_SHIELD
                    }
                    val durationMinutes = when (boost.name) {
                        "Streak Kalkanı Prime" -> 43200 // 30 Gün
                        "Streak Kalkanı Plus" -> 10080  // 7 Gün
                        "Streak Kalkanı" -> 1440        // 24 Saat
                        else -> 30
                    }

                    val executeDirect = {
                        if (useCoins) {
                            QuizTreeProgressStore.addCoins(context, -costCoins)
                            coins = QuizTreeProgressStore.getCoins(context)
                            FirestoreSyncManager.recordPurchase(context, packageName = boost.name, price = "$costCoins Coin")
                        } else {
                            QuizTreeProgressStore.addGems(context, -costGems)
                            gems = QuizTreeProgressStore.getGems(context)
                            FirestoreSyncManager.recordPurchase(context, packageName = boost.name, price = "$costGems Elmas")
                        }
                        FirestoreSyncManager.syncProgress(context)
                        MarketBoostStore.activateBoost(context, boost.name, boostType, durationMinutes)
                        remainingSeconds = MarketBoostStore.getRemainingSeconds(context)
                        Toast.makeText(context, "${boost.name} Etkinleştirildi! Kalan süre sayaçta görünüyor.", Toast.LENGTH_LONG).show()
                    }

                    val executeQueue = {
                        if (useCoins) {
                            QuizTreeProgressStore.addCoins(context, -costCoins)
                            coins = QuizTreeProgressStore.getCoins(context)
                            FirestoreSyncManager.recordPurchase(context, packageName = boost.name, price = "$costCoins Coin")
                        } else {
                            QuizTreeProgressStore.addGems(context, -costGems)
                            gems = QuizTreeProgressStore.getGems(context)
                            FirestoreSyncManager.recordPurchase(context, packageName = boost.name, price = "$costGems Elmas")
                        }
                        FirestoreSyncManager.syncProgress(context)
                        MarketBoostStore.queueBoost(context, boost.name, boostType, durationMinutes)
                        Toast.makeText(context, "${boost.name} sıraya eklendi! Mega Boost bitince devreye girecek.", Toast.LENGTH_LONG).show()
                    }

                    val performPurchaseIntent = {
                        // Rule 1: Mega Boost active and buying non-mega boost
                        if (MarketBoostStore.isMegaBoostActive(context) && boostType != BoostType.MEGA) {
                            boostWarningDialog = BoostWarningDialogData(
                                title = "Aktif Mega Boost Uyarısı",
                                message = "Şu anda aktif Mega Boost eklentiniz bulunmaktadır. İsterseniz yeni aldığınız ${boost.name} eklentisi Mega Boost biter bitmez otomatik başlasın mı?",
                                confirmText = "Evet, Sıraya Ekle",
                                cancelText = "Hayır, Vazgeç",
                                onConfirm = executeQueue
                            )
                        } else if (boostType == BoostType.MEGA && (MarketBoostStore.isXpBoostActive(context) || MarketBoostStore.isCoinBoostActive(context))) {
                            val activeTypeNames = when {
                                MarketBoostStore.isXpBoostActive(context) && MarketBoostStore.isCoinBoostActive(context) -> "XP ve Coin"
                                MarketBoostStore.isCoinBoostActive(context) -> "Coin"
                                else -> "XP"
                            }
                            boostWarningDialog = BoostWarningDialogData(
                                title = "Mega Boost Satın Alma",
                                message = "Şu anda aktif bir $activeTypeNames Boost paketiniz var. Mega Boost ile tüm ödüller (XP, Coin, Elmas) 2 katına çıkacaktır. Mevcut boostunuz bittiğinde Mega Boost içindeki ek süre devreye girecektir. Devam etmek istiyor musunuz?",
                                confirmText = "Evet, Satın Al",
                                cancelText = "Vazgeç",
                                onConfirm = executeDirect
                            )
                        } else if ((boostType == BoostType.STREAK_SHIELD_PLUS || boostType == BoostType.STREAK_SHIELD_PRIME) && MarketBoostStore.isStreakShieldActive(context)) {
                            boostWarningDialog = BoostWarningDialogData(
                                title = "Seri Kalkanı Yükseltme",
                                message = "Zaten aktif bir Seri Kalkanınız bulunmaktadır. Serinizi ${if (boostType == BoostType.STREAK_SHIELD_PRIME) "30 günlük Prime" else "7 günlük Plus"} korumaya yükseltmek ister misiniz?",
                                confirmText = "Evet, Yükselt",
                                cancelText = "Vazgeç",
                                onConfirm = executeDirect
                            )
                        } else {
                            executeDirect()
                        }
                    }

                    if (UserProfileManager.isAnonymous(context)) {
                        pendingPurchaseAction = performPurchaseIntent
                        showMarketLinkDialog = true
                    } else {
                        performPurchaseIntent()
                    }
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(boosts) { boost ->
                        val durationText = when (boost.name) {
                            "Streak Kalkanı Prime" -> "30 Gün"
                            "Streak Kalkanı Plus" -> "7 Gün"
                            "Streak Kalkanı" -> "24 Saat"
                            else -> "30 Dakika"
                        }
                        val detailExplanation = when (boost.name) {
                            "XP Boost" -> "🔥 30 Dakika boyunca tamamlayacağın tüm Quiz ve Reading içeriklerinden tam 2 Kat (2X) XP kazanırsın! Normalde 45 XP veren bir quiz bu paketle 90 XP kazandırır."
                            "Coin Boost" -> "💰 30 Dakika boyunca tüm Quiz, Reading ve Kelime Pratiklerinden tam 2 Kat (2X) Altın kazanırsın! (Yalnızca Elmas ile satılır)."
                            "Mega Boost Paketi" -> "⭐ 30 Dakika boyunca 2x XP + 2x Altın + 2x Elmas ikiye katlanır ve 🛡️ 24 Saatlik Seri Kalkanı serini güvence altına alır!"
                            "Streak Kalkanı" -> "🛡️ 24 Saat boyunca gün atlasan bile serin bozulmaz. (Coin ile satın almak için en az 30 başarılı quiz çözmüş olma şartı aranır)."
                            "Streak Kalkanı Plus" -> "🛡️ 7 Gün boyunca serini kesintisiz korur. Parlayan mavi kalkan çerçevesiyle serin güvendedir."
                            "Streak Kalkanı Prime" -> "👑 30 Gün boyunca serini asil altın kalkanla korur, uzun süre rahatça öğrenmene odaklanabilirsin."
                            else -> boost.desc
                        }

                        ProductCard(
                            name = boost.name,
                            desc = boost.desc,
                            priceCoins = boost.priceCoins,
                            priceGems = boost.priceGems,
                            iconName = boost.iconName,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            borderColor = borderColor,
                            context = context,
                            onClick = {
                                selectedDialogItem = MarketItemDialogData(
                                    title = boost.name,
                                    category = "⚡ $durationText Boost",
                                    description = detailExplanation,
                                    priceCoins = boost.priceCoins,
                                    priceGems = boost.priceGems,
                                    iconName = boost.iconName,
                                    isBoost = true,
                                    onConfirmCoins = if (boost.priceCoins > 0) { { handleBoostPurchase(boost, useCoins = true) } } else null,
                                    onConfirmGems = if (boost.priceGems > 0) { { handleBoostPurchase(boost, useCoins = false) } } else null
                                )
                            },
                            onBuyCoin = if (boost.priceCoins > 0) { { handleBoostPurchase(boost, useCoins = true) } } else null,
                            onBuyGem = if (boost.priceGems > 0) { { handleBoostPurchase(boost, useCoins = false) } } else null
                        )
                    }
                }
            }
        }

        // 🔹 5. TEMALAR SECTION
        item {
            Column {
                SectionHeader(title = "Temalar")
                Spacer(modifier = Modifier.height(12.dp))

                val themes = listOf(
                    ThemeItem(
                        name = "Krallık Mew",
                        desc = "Altın işlemeli kraliyet sarayı ve Krallık Kanatları hediyeli",
                        priceCoins = 0,
                        priceGems = 0,
                        isRealMoneyOnly = true,
                        realMoneyPrice = BillingManager.getFormattedPrice(BillingProducts.INAPP_THEME_KINGDOM, "₺50,00"),
                        productId = BillingProducts.INAPP_THEME_KINGDOM,
                        iconName = "kingdom"
                    ),
                    ThemeItem(
                        name = "Uzay",
                        desc = "Kozmik galaksi ve yıldız teması",
                        priceCoins = 0,
                        priceGems = 200,
                        isRealMoneyOnly = false,
                        realMoneyPrice = BillingManager.getFormattedPrice(BillingProducts.INAPP_THEME_SPACE, "₺20,00"),
                        productId = BillingProducts.INAPP_THEME_SPACE,
                        iconName = "uzay"
                    ),
                    ThemeItem(
                        name = "Klasik Mew",
                        desc = "Varsayılan mavi tema",
                        priceCoins = 0,
                        priceGems = 0,
                        iconName = "klasik",
                        isActive = true
                    ),
                    ThemeItem(
                        name = "Sakura",
                        desc = "Pembe çiçekli bahar teması",
                        priceCoins = 2500,
                        priceGems = 90,
                        iconName = "sakura"
                    ),
                    ThemeItem(
                        name = "Okyanus",
                        desc = "Mavi okyanus teması",
                        priceCoins = 2500,
                        priceGems = 90,
                        iconName = "okyanus"
                    )
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(themes) { thm ->
                        val currentEquippedTheme = UserCosmeticsStore.getEquippedTheme(context)
                        val isEquipped = when (thm.iconName) {
                            "uzay" -> UserCosmeticsStore.isSpaceThemeActive(context)
                            "kingdom" -> UserCosmeticsStore.isKingdomThemeActive(context)
                            else -> currentEquippedTheme.equals(thm.iconName, ignoreCase = true)
                        }
                        val isOwned = thm.iconName == "klasik" || isEquipped || UserCosmeticsStore.isThemeOwned(context, thm.iconName)

                        val equipThemeAction = {
                            UserCosmeticsStore.setEquippedTheme(context, thm.iconName)
                            if (thm.iconName in listOf("kingdom", "uzay")) {
                                loadingThemeId = thm.iconName
                            } else {
                                Toast.makeText(context, "${thm.name} Tema Seçildi!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val buyThemeWithCoins = {
                            if (coins < thm.priceCoins) {
                                Toast.makeText(context, "Yetersiz Altın (Coin)!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addCoins(context, -thm.priceCoins)
                                coins = QuizTreeProgressStore.getCoins(context)
                                UserCosmeticsStore.unlockTheme(context, thm.iconName)
                                UserCosmeticsStore.setEquippedTheme(context, thm.iconName)
                                FirestoreSyncManager.recordPurchase(context, packageName = thm.name, price = "${thm.priceCoins} Coin")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${thm.name} Teması Satın Alındı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val buyThemeWithGems = {
                            if (gems < thm.priceGems) {
                                Toast.makeText(context, "Yetersiz Elmas!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addGems(context, -thm.priceGems)
                                gems = QuizTreeProgressStore.getGems(context)
                                UserCosmeticsStore.unlockTheme(context, thm.iconName)
                                UserCosmeticsStore.setEquippedTheme(context, thm.iconName)
                                if (thm.iconName == "uzay") {
                                    loadingThemeId = "uzay"
                                }
                                FirestoreSyncManager.recordPurchase(context, packageName = thm.name, price = "${thm.priceGems} Elmas")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${thm.name} Teması Satın Alındı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val buyThemeWithRealMoney = {
                            val activity = context as? Activity
                            if (activity != null && thm.productId.isNotEmpty()) {
                                BillingManager.launchBillingFlow(activity, thm.productId)
                            } else {
                                Toast.makeText(context, "Ödeme ekranı açılamadı", Toast.LENGTH_SHORT).show()
                            }
                        }

                        ProductCard(
                            name = thm.name,
                            desc = thm.desc,
                            priceCoins = thm.priceCoins,
                            priceGems = thm.priceGems,
                            isRealMoneyOnly = thm.isRealMoneyOnly,
                            realMoneyPrice = thm.realMoneyPrice,
                            iconName = thm.iconName,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            borderColor = borderColor,
                            context = context,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            onClick = {
                                selectedDialogItem = MarketItemDialogData(
                                    title = thm.name,
                                    category = "🎨 Uygulama Teması",
                                    description = if (thm.iconName == "kingdom") {
                                        "👑 Kraliyet sarayı, altın işlemeli kartlar, özel kraliyet navigasyonu ve yanında KRALLIK KANATLARI hediyeli!"
                                    } else {
                                        "${thm.name} teması uygulama renklerini ve atmosferini bu stile dönüştürür."
                                    },
                                    priceCoins = thm.priceCoins,
                                    priceGems = thm.priceGems,
                                    isRealMoney = thm.isRealMoneyOnly || thm.realMoneyPrice.isNotEmpty(),
                                    realMoneyPrice = thm.realMoneyPrice,
                                    productId = thm.productId,
                                    iconName = thm.iconName,
                                    isTheme = true,
                                    isOwned = isOwned,
                                    isEquipped = isEquipped,
                                    onConfirm = equipThemeAction,
                                    onConfirmCoins = buyThemeWithCoins,
                                    onConfirmGems = buyThemeWithGems,
                                    onConfirmRealMoney = buyThemeWithRealMoney
                                )
                            },
                            onBuyCoin = if (thm.priceCoins > 0 && !isOwned) { { safeExecutePurchase(buyThemeWithCoins) } } else null,
                            onBuyGem = if (thm.priceGems > 0 && !isOwned) { { safeExecutePurchase(buyThemeWithGems) } } else null
                        )
                    }
                }
            }
        }

        // 🔹 6. KEDİ KARAKTERLERİ SECTION
        item {
            Column {
                SectionHeader(title = "Kedi Karakterleri")
                Spacer(modifier = Modifier.height(12.dp))

                val cats = listOf(
                    CatItem("Akademisyen Mew", priceCoins = 0, priceGems = 0, iconName = "akademisyen_mew", isActive = true),
                    CatItem("Havalı Mew", priceCoins = 2000, priceGems = 60, iconName = "cool_mew"),
                    CatItem("Kaptan Mew", priceCoins = 3000, priceGems = 80, iconName = "pirate_mew"),
                    CatItem("Büyücü Mew", priceCoins = 3000, priceGems = 80, iconName = "wizard_mew"),
                    CatItem("Kral Mew", priceCoins = 5000, priceGems = 120, iconName = "king_mew")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(cats) { cat ->
                        val isOwned = cat.priceCoins == 0 || UserCosmeticsStore.isAvatarOwned(context, cat.iconName)
                        val isEquipped = UserCosmeticsStore.isAvatarEquipped(context, cat.iconName)

                        val equipCatAction = {
                            UserCosmeticsStore.setEquippedAvatar(context, cat.iconName)
                            Toast.makeText(context, "${cat.name} Karakteri Seçildi!", Toast.LENGTH_SHORT).show()
                        }

                        val buyCatWithCoins = {
                            if (coins < cat.priceCoins) {
                                Toast.makeText(context, "Yetersiz Altın (Coin)!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addCoins(context, -cat.priceCoins)
                                coins = QuizTreeProgressStore.getCoins(context)
                                UserCosmeticsStore.unlockAvatar(context, cat.iconName)
                                UserCosmeticsStore.setEquippedAvatar(context, cat.iconName)
                                FirestoreSyncManager.recordPurchase(context, packageName = cat.name, price = "${cat.priceCoins} Coin")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${cat.name} Satın Alındı ve Kuşanıldı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val buyCatWithGems = {
                            if (gems < cat.priceGems) {
                                Toast.makeText(context, "Yetersiz Elmas!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addGems(context, -cat.priceGems)
                                gems = QuizTreeProgressStore.getGems(context)
                                UserCosmeticsStore.unlockAvatar(context, cat.iconName)
                                UserCosmeticsStore.setEquippedAvatar(context, cat.iconName)
                                FirestoreSyncManager.recordPurchase(context, packageName = cat.name, price = "${cat.priceGems} Elmas")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${cat.name} Satın Alındı ve Kuşanıldı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        ProductCard(
                            name = cat.name,
                            desc = if (isEquipped) "Aktif karakter" else if (isOwned) "Envanterinde mevcut" else "Profil resmi olarak kullan",
                            priceCoins = cat.priceCoins,
                            priceGems = cat.priceGems,
                            iconName = cat.iconName,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            borderColor = borderColor,
                            context = context,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            onClick = {
                                selectedDialogItem = MarketItemDialogData(
                                    title = cat.name,
                                    category = "🐱 Kedi Karakteri",
                                    description = "${cat.name} karakterini profil resmi olarak kuşanabilirsin. Quizlerde, profilinde ve sıralamada bu avatar görünür.",
                                    priceCoins = cat.priceCoins,
                                    priceGems = cat.priceGems,
                                    iconName = cat.iconName,
                                    isCat = true,
                                    isOwned = isOwned,
                                    isEquipped = isEquipped,
                                    onConfirm = equipCatAction,
                                    onConfirmCoins = buyCatWithCoins,
                                    onConfirmGems = buyCatWithGems
                                )
                            },
                            onBuyCoin = if (cat.priceCoins > 0 && !isOwned) { { safeExecutePurchase(buyCatWithCoins) } } else null,
                            onBuyGem = if (cat.priceGems > 0 && !isOwned) { { safeExecutePurchase(buyCatWithGems) } } else null
                        )
                    }
                }
            }
        }

        // 🔹 7. ÇERÇEVELER SECTION
        item {
            Column {
                SectionHeader(title = "Çerçeveler")
                Spacer(modifier = Modifier.height(12.dp))

                val frames = listOf(
                    FrameItem("Mavi Çerçeve", priceCoins = 800, priceGems = 25, iconName = "blue"),
                    FrameItem("Altın Çerçeve", priceCoins = 2500, priceGems = 50, iconName = "altin"),
                    FrameItem("Çiçek Çerçeve", priceCoins = 2000, priceGems = 40, iconName = "cicek"),
                    FrameItem("Neon Çerçeve", priceCoins = 3500, priceGems = 70, iconName = "neon"),
                    FrameItem("Kraliyet Çerçevesi", priceCoins = 5000, priceGems = 100, iconName = "kraliyet")
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(frames) { frame ->
                        val isOwned = UserCosmeticsStore.isFrameOwned(context, frame.iconName)
                        val isEquipped = UserCosmeticsStore.getEquippedFrame(context) == frame.iconName

                        val equipFrameAction = {
                            UserCosmeticsStore.setEquippedFrame(context, frame.iconName)
                            Toast.makeText(context, "${frame.name} Kuşanıldı!", Toast.LENGTH_SHORT).show()
                        }

                        val buyFrameWithCoins = {
                            if (coins < frame.priceCoins) {
                                Toast.makeText(context, "Yetersiz Altın (Coin)!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addCoins(context, -frame.priceCoins)
                                coins = QuizTreeProgressStore.getCoins(context)
                                UserCosmeticsStore.unlockFrame(context, frame.iconName)
                                UserCosmeticsStore.setEquippedFrame(context, frame.iconName)
                                FirestoreSyncManager.recordPurchase(context, packageName = frame.name, price = "${frame.priceCoins} Coin")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${frame.name} Çerçevesi Satın Alındı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val buyFrameWithGems = {
                            if (gems < frame.priceGems) {
                                Toast.makeText(context, "Yetersiz Elmas!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addGems(context, -frame.priceGems)
                                gems = QuizTreeProgressStore.getGems(context)
                                UserCosmeticsStore.unlockFrame(context, frame.iconName)
                                UserCosmeticsStore.setEquippedFrame(context, frame.iconName)
                                FirestoreSyncManager.recordPurchase(context, packageName = frame.name, price = "${frame.priceGems} Elmas")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${frame.name} Çerçevesi Satın Alındı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        ProductCard(
                            name = frame.name,
                            desc = "Profil çerçevesi",
                            priceCoins = frame.priceCoins,
                            priceGems = frame.priceGems,
                            iconName = frame.iconName,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            borderColor = borderColor,
                            context = context,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            onClick = {
                                selectedDialogItem = MarketItemDialogData(
                                    title = frame.name,
                                    category = "🖼️ Profil Çerçevesi",
                                    description = "Avatarının etrafını saran özel tasarım ${frame.name}. Profilini daha seçkin ve dikkat çekici kılar.",
                                    priceCoins = frame.priceCoins,
                                    priceGems = frame.priceGems,
                                    iconName = frame.iconName,
                                    isFrame = true,
                                    isOwned = isOwned,
                                    isEquipped = isEquipped,
                                    onConfirm = equipFrameAction,
                                    onConfirmCoins = buyFrameWithCoins,
                                    onConfirmGems = buyFrameWithGems
                                )
                            },
                            onBuyCoin = if (frame.priceCoins > 0 && !isOwned) buyFrameWithCoins else null,
                            onBuyGem = if (frame.priceGems > 0 && !isOwned) buyFrameWithGems else null
                        )
                    }
                }
            }
        }

        // 🔹 8. KANATLAR & ROZETLER (Wings & Banners) SECTION
        item {
            Column {
                SectionHeader(title = "Kanatlar & Rozetler")
                Spacer(modifier = Modifier.height(12.dp))

                val wings = UserCosmeticsStore.predefinedWings.filter { !it.isRankDefault }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(wings) { wing ->
                        val isOwned = UserCosmeticsStore.isWingOwned(context, wing.id)
                        val isEquipped = UserCosmeticsStore.getEquippedWingId(context) == wing.id

                        val equipWingAction = {
                            UserCosmeticsStore.setEquippedWingId(context, wing.id)
                            Toast.makeText(context, "${wing.name} Kuşanıldı!", Toast.LENGTH_SHORT).show()
                        }

                        val buyWingWithCoins = {
                            if (wing.priceCoins <= 0) {
                                Toast.makeText(context, "Bu kanat sadece Elmas veya özel paketle alınabilir!", Toast.LENGTH_SHORT).show()
                            } else if (coins < wing.priceCoins) {
                                Toast.makeText(context, "Yetersiz Altın (Coin)!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addCoins(context, -wing.priceCoins)
                                coins = QuizTreeProgressStore.getCoins(context)
                                UserCosmeticsStore.unlockWing(context, wing.id)
                                UserCosmeticsStore.setEquippedWingId(context, wing.id)
                                FirestoreSyncManager.recordPurchase(context, packageName = wing.name, price = "${wing.priceCoins} Coin")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${wing.name} Satın Alındı ve Kuşanıldı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        val buyWingWithGems = {
                            if (gems < wing.priceGems) {
                                Toast.makeText(context, "Yetersiz Elmas!", Toast.LENGTH_SHORT).show()
                            } else {
                                QuizTreeProgressStore.addGems(context, -wing.priceGems)
                                gems = QuizTreeProgressStore.getGems(context)
                                UserCosmeticsStore.unlockWing(context, wing.id)
                                UserCosmeticsStore.setEquippedWingId(context, wing.id)
                                FirestoreSyncManager.recordPurchase(context, packageName = wing.name, price = "${wing.priceGems} Elmas")
                                FirestoreSyncManager.syncProgress(context)
                                Toast.makeText(context, "${wing.name} Satın Alındı ve Kuşanıldı!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        WingProductCard(
                            wing = wing,
                            isOwned = isOwned,
                            isEquipped = isEquipped,
                            cardColor = cardColor,
                            textColor = textColor,
                            subtitleColor = subtitleColor,
                            borderColor = borderColor,
                            primaryColor = primaryColor,
                            userRank = userRank,
                            onClick = {
                                selectedDialogItem = MarketItemDialogData(
                                    title = wing.name,
                                    category = "🪽 Özel Kanat",
                                    description = wing.description + " Profil avatarının iki yanını süsleyen görkemli simetrik kanatlar.",
                                    priceCoins = wing.priceCoins,
                                    priceGems = wing.priceGems,
                                    isWing = true,
                                    wingData = wing,
                                    isOwned = isOwned,
                                    isEquipped = isEquipped,
                                    onConfirm = equipWingAction,
                                    onConfirmCoins = if (wing.priceCoins > 0) buyWingWithCoins else null,
                                    onConfirmGems = buyWingWithGems
                                )
                            },
                            onBuyCoin = if (wing.priceCoins > 0 && !isOwned) { { safeExecutePurchase(buyWingWithCoins) } } else null,
                            onBuyGem = if (wing.priceGems > 0 && !isOwned) { { safeExecutePurchase(buyWingWithGems) } } else null
                        )
                    }
                }
            }
        }
    }


    if (boostWarningDialog != null) {
        val dlg = boostWarningDialog!!
        AlertDialog(
            onDismissRequest = { boostWarningDialog = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = appColors.surface,
            title = {
                Text(
                    text = dlg.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.text
                )
            },
            text = {
                Text(
                    text = dlg.message,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = appColors.textMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val action = dlg.onConfirm
                        boostWarningDialog = null
                        action.invoke()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Text(dlg.confirmText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { boostWarningDialog = null },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(dlg.cancelText, color = appColors.textMuted)
                }
            }
        )
    }

    if (selectedDialogItem != null) {
        val currentItem = selectedDialogItem!!
        MarketItemDetailDialog(
            item = currentItem,
            userGems = gems,
            userCoins = coins,
            userRank = userRank,
            onDismiss = { selectedDialogItem = null },
            onExecutePurchase = safeExecutePurchase,
            onConfirm = {
                val action = currentItem.onConfirm
                selectedDialogItem = null
                
                // Anonim kullanıcı kontrolü: Ürün henüz sahip olunmamışsa önce Mail Bağla penceresi gösterilir
                if (!currentItem.isOwned && UserProfileManager.isAnonymous(context)) {
                    pendingPurchaseAction = action
                    showMarketLinkDialog = true
                } else {
                    action?.invoke()
                }
            }
        )
    }

    if (showMarketLinkDialog) {
        AccountLinkingDialog(
            title = "Satın Alımı Korumak İçin Mailini Bağla 🐾",
            description = "Satın aldığın eşyaların kaybolmaması ve güvende kalması için lütfen hesabını bağla.",
            onDismiss = {
                // Kullanıcı "Hayır/Kapat" derse satın alım yapılmaz ama markette gezinmeye devam eder
                showMarketLinkDialog = false
                pendingPurchaseAction = null
            },
            onLinkedSuccess = {
                showMarketLinkDialog = false
                pendingPurchaseAction?.invoke()
                pendingPurchaseAction = null
            }
        )
    }

    if (showActiveBoostsDialog) {
        ActiveBoostsModalDialog(
            navController = navController,
            onDismiss = { showActiveBoostsDialog = false }
        )
    }

    if (showInventorySheet) {
        InventoryBottomSheet(
            onDismissRequest = { showInventorySheet = false },
            navController = navController,
            userRank = userRank
        )
    }

    if (loadingThemeId != null) {
        UniversalThemeLoadingOverlay(
            themeId = loadingThemeId!!,
            onFinished = {
                loadingThemeId = null
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }

    if (showCatSelector) {
        CatSelectorBottomSheet(
            isForTopBar = true,
            onDismissRequest = { showCatSelector = false }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    val appColors = LocalAppThemeColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.text
        )
    }
}

@Composable
fun CurrencyCard(pack: CurrencyPack, cardColor: Color, textColor: Color, isDarkMode: Boolean, onClick: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(1.dp, if (isDark) appColors.border else Color(0xFFF1F5F9)),
        modifier = Modifier
            .width(115.dp)
            .height(180.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Tag Banner (e.g. Popüler)
            if (pack.tag != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .background(
                            brush = Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pack.tag,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Lottie Icon
                LottieIcon(rawRes = pack.lottieRes, modifier = Modifier.size(48.dp))

                // Amount Text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pack.amount,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = pack.typeName,
                        fontSize = 10.sp,
                        color = appColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }

                // Buy button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                    border = BorderStroke(1.dp, if (isDark) appColors.primary.copy(alpha = 0.5f) else Color(0xFF3897F0).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() }
                ) {
                    Text(
                        text = pack.price,
                        color = if (isDark) appColors.primary else Color(0xFF0284C7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    name: String,
    desc: String,
    priceCoins: Int = 0,
    priceGems: Int = 0,
    isRealMoneyOnly: Boolean = false,
    realMoneyPrice: String = "",
    iconName: String,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    borderColor: Color,
    context: Context,
    isActive: Boolean = false,
    isOwned: Boolean = false,
    onClick: () -> Unit,
    onBuyCoin: (() -> Unit)? = null,
    onBuyGem: (() -> Unit)? = null
) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    val isPlusShield = name.contains("Plus") || name.contains("Kalkanı+")
    val isPrimeShield = name.contains("Prime")

    val cardBorder = when {
        isPrimeShield -> BorderStroke(2.5.dp, Color(0xFFFFD700))
        isPlusShield -> BorderStroke(2.dp, Color(0xFF00E5FF))
        isActive -> BorderStroke(2.dp, appColors.primary)
        else -> BorderStroke(1.dp, borderColor)
    }

    val cardContainerColor = when {
        isPrimeShield && isDark -> Color(0xFF261D0C)
        isPrimeShield -> Color(0xFFFFFDF2)
        isPlusShield && isDark -> Color(0xFF0C243C)
        isPlusShield -> Color(0xFFF0F9FF)
        else -> cardColor
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        border = cardBorder,
        modifier = Modifier
            .width(142.dp)
            .height(215.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon
            val isFrame = iconName in listOf("blue", "altin", "cicek", "neon", "kraliyet")
            val frameBorderColor = when (iconName) {
                "altin" -> Color(0xFFFFD700)
                "cicek" -> Color(0xFFF43F5E)
                "neon" -> Color(0xFF00E5FF)
                "kraliyet" -> Color(0xFFFFB300)
                "blue" -> Color(0xFF3897F0)
                else -> appColors.primary
            }

            if (isFrame) {
                val equippedAvatarRes = remember(UserCosmeticsStore.stateRevision) {
                    UserCosmeticsStore.getEquippedAvatarRes(context)
                }
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC), CircleShape)
                        .border(3.dp, frameBorderColor, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = equippedAvatarRes),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                val iconBoxBorder = when {
                    isPrimeShield -> BorderStroke(2.5.dp, Color(0xFFFFD700))
                    isPlusShield -> BorderStroke(2.dp, Color(0xFF00E5FF))
                    isActive -> BorderStroke(1.5.dp, appColors.primary)
                    else -> BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
                }
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC), CircleShape)
                        .border(iconBoxBorder, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = getMarketDrawableRes(context, iconName)),
                        contentDescription = name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Text info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = name,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 9.sp,
                    color = subtitleColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 11.sp
                )
            }

            // Price / Buy button
            when {
                isActive -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Kuşanıldı",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                isOwned -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = appColors.primary.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, appColors.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Kuşan",
                                color = appColors.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                isRealMoneyOnly -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = realMoneyPrice,
                                color = Color(0xFF2563EB),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                realMoneyPrice.isNotEmpty() && priceGems > 0 -> {
                    // Both Real Money and Gems (e.g. Uzay theme)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable { onClick() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = realMoneyPrice,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable { onBuyGem?.invoke() ?: onClick() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$priceGems",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0284C7)
                                )
                                Spacer(Modifier.width(1.dp))
                                LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
                priceCoins > 0 && priceGems > 0 -> {
                    // Dual currency buttons (Coin & Elmas)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Coin button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onBuyCoin?.invoke() ?: onClick() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (priceCoins >= 1000) String.format("%,d", priceCoins) else "$priceCoins",
                                    fontSize = if (priceCoins >= 10000) 8.5.sp else 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB45309),
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(1.dp))
                                LottieIcon(rawRes = R.raw.anim_coin, modifier = Modifier.size(11.dp))
                            }
                        }

                        // Gem button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onBuyGem?.invoke() ?: onClick() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$priceGems",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0284C7),
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(1.dp))
                                LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
                priceGems > 0 -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onBuyGem?.invoke() ?: onClick() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$priceGems",
                                color = Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(Modifier.width(3.dp))
                            LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(13.dp))
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, appColors.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Seç",
                                color = if (isDark) appColors.primary else Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LottieIcon(rawRes: Int, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(navController: NavHostController, isDarkMode: Boolean = false) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    
    // Colors
    val backgroundColor = appColors.background
    val cardColor = appColors.surface
    val textColor = appColors.text
    val subtitleColor = appColors.textMuted
    val borderColor = appColors.border

    val isRestoring by BillingManager.isRestoring.collectAsState()

    LaunchedEffect(Unit) {
        BillingManager.events.collect { event ->
            when (event) {
                is BillingEvent.PurchaseSuccess -> {
                    val name = BillingProducts.getProductName(event.productId)
                    Toast.makeText(context, "🎉 Tebrikler! $name başarıyla aktif edildi!", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseCancelled -> {
                    Toast.makeText(context, "Satın alma işlemi iptal edildi.", Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.PurchasePending -> {
                    Toast.makeText(context, "⏳ Ödemeniz onay bekliyor. Onaylandığında aboneliğiniz aktifleşecektir.", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseError -> {
                    Toast.makeText(context, "⚠️ Ödeme Hatası: ${event.message}", Toast.LENGTH_LONG).show()
                }
                is BillingEvent.PurchaseFailed -> {
                    Toast.makeText(context, "Ödeme tamamlanamadı: ${event.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.ItemAlreadyOwned -> {
                    Toast.makeText(context, "Zaten aktif bir aboneliğiniz var! Haklarınız kontrol edildi.", Toast.LENGTH_SHORT).show()
                }
                is BillingEvent.PurchasesRestored -> {
                    val msg = if (event.message.isNotBlank()) {
                        event.message
                    } else if (event.restoredCount > 0) {
                        "Tebrikler! ${event.restoredCount} adet abonelik/satın alımınız geri yüklendi."
                    } else {
                        "Aktif bir abonelik kaydı bulunamadı."
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Premium Paketler", fontWeight = FontWeight.Bold, color = appColors.text) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = appColors.text)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            BillingManager.restorePurchases(context) { _, count, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isRestoring
                    ) {
                        if (isRestoring) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = appColors.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Satın Alımları Geri Yükle",
                                tint = appColors.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF5D9CEC), Color(0xFF9C27B0), Color(0xFFEC407A))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MewEnglish Premium",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dil öğrenme yolculuğunu hızlandır, sınırsız özellikleri aç!",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3 packages (SUBS)
            val plans = listOf(
                PremiumPlan(
                    title = "Premium",
                    price = BillingManager.getFormattedPrice(BillingProducts.SUBS_PREMIUM_MONTHLY, "₺149,00 / ay"),
                    features = listOf(
                        "Sınırsız Can / Enerji",
                        "1 Hafta Boyunca 2X XP ve Coin Çarpanı",
                        "Tüm Seviyelerde Kesintisiz Pratik",
                        "Detaylı öğrenilen içerik analizleri",
                        "Özel kelime kaydırma özellikleri",
                        "Hoş Geldin Hediyesi: 200 Elmas",
                        "Hoş Geldin Hediyesi: 1000 Coin"
                    ),
                    gemsReward = 200,
                    coinsReward = 1000,
                    badge = null,
                    accentColor = Color(0xFF2196F3),
                    productId = BillingProducts.SUBS_PREMIUM_MONTHLY
                ),
                PremiumPlan(
                    title = "Premium Plus",
                    price = BillingManager.getFormattedPrice(BillingProducts.SUBS_PREMIUM_PLUS_MONTHLY, "₺199,00 / ay"),
                    features = listOf(
                        "Tüm Premium özellikleri",
                        "Sınırsız Can / Enerji",
                        "Abonelik Boyunca Sınırsız / Kalıcı 2X XP & Coin Çarpanı",
                        "Daha detaylı öğrenilen içerik raporlama",
                        "Hoş Geldin Hediyesi: 300 Elmas",
                        "Hoş Geldin Hediyesi: 2000 Coin"
                    ),
                    gemsReward = 300,
                    coinsReward = 2000,
                    badge = "En Popüler / Tavsiye Edilen",
                    accentColor = Color(0xFF9C27B0),
                    productId = BillingProducts.SUBS_PREMIUM_PLUS_MONTHLY
                ),
                PremiumPlan(
                    title = "Aile Planı",
                    price = BillingManager.getFormattedPrice(BillingProducts.SUBS_FAMILY_PLAN_MONTHLY, "₺399,00 / ay"),
                    features = listOf(
                        "Tüm Premium özellikleri",
                        "En fazla 5 cihazdan giriş",
                        "Sınırsız Can / Enerji (Tüm Cihazlar)",
                        "Abonelik Boyunca Sınırsız / Kalıcı 2X XP & Coin Çarpanı",
                        "Aile üyeleriyle Leaderboard",
                        "Hoş Geldin Hediyesi: 300 Elmas",
                        "Hoş Geldin Hediyesi: 2000 Coin"
                    ),
                    gemsReward = 300,
                    coinsReward = 2000,
                    badge = "Aileye Özel",
                    accentColor = Color(0xFFE91E63),
                    productId = BillingProducts.SUBS_FAMILY_PLAN_MONTHLY
                )
            )

            items(plans) { plan ->
                PremiumPlanCard(
                    plan = plan,
                    cardColor = cardColor,
                    textColor = textColor,
                    subtitleColor = subtitleColor,
                    borderColor = borderColor
                ) {
                    val activity = context as? Activity
                    if (activity != null) {
                        BillingManager.launchBillingFlow(activity, plan.productId)
                    } else {
                        Toast.makeText(context, "Ödeme ekranı açılamadı", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Google Play Abonelik Yönetimi ve İptal Kartı
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Abonelik Yönetimi ve İptal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Google Play üzerinden aboneliğinizi dilediğiniz zaman yönetebilir veya iptal edebilirsiniz.",
                            fontSize = 12.sp,
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { openSubscriptionManagement(context) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF2196F3))
                        ) {
                            Text("Aboneliği Google Play'de Yönet", color = Color(0xFF2196F3), fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                BillingManager.restorePurchases(context) { _, count, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isRestoring,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50))
                        ) {
                            if (isRestoring) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Geri Yükleniyor...", color = Color(0xFF4CAF50), fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Satın Alımları Geri Yükle", color = Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

fun openSubscriptionManagement(context: Context) {
    try {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
        )
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Google Play Store açılamadı", Toast.LENGTH_SHORT).show()
    }
}

data class PremiumPlan(
    val title: String,
    val price: String,
    val features: List<String>,
    val gemsReward: Int,
    val coinsReward: Int,
    val badge: String? = null,
    val accentColor: Color,
    val productId: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPlanCard(
    plan: PremiumPlan,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    borderColor: Color,
    onBuyClick: () -> Unit
) {
    val context = LocalContext.current
    val isDark = cardColor != Color.White
    val isHighlighted = plan.badge?.contains("Tavsiye") == true
    val cardBackground = if (isDark) {
        if (isHighlighted) Color(0xFF2A1B3D) else cardColor
    } else {
        if (isHighlighted) plan.accentColor.copy(alpha = 0.08f) else plan.accentColor.copy(alpha = 0.04f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = if (isHighlighted) BorderStroke(2.dp, Color(0xFFFF9800)) else BorderStroke(1.5.dp, plan.accentColor.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = plan.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                if (plan.badge != null) {
                    val isFav = plan.badge.contains("Tavsiye")
                    Surface(
                        color = if (isFav) Color(0xFFFF9800) else plan.accentColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isFav) "⭐ ${plan.badge}" else plan.badge,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plan.price,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = plan.accentColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Features list
            plan.features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 3.dp)) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = plan.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feature,
                        fontSize = 13.sp,
                        color = subtitleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBuyClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = plan.accentColor)
            ) {
                Text("Satın Al", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun WingProductCard(
    wing: WingItemData,
    isOwned: Boolean,
    isEquipped: Boolean,
    cardColor: Color,
    textColor: Color,
    subtitleColor: Color,
    borderColor: Color,
    primaryColor: Color,
    userRank: RankType,
    onClick: () -> Unit,
    onBuyCoin: (() -> Unit)? = null,
    onBuyGem: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val equippedAvatarRes = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.getEquippedAvatarRes(context)
    }
    val isDark = LocalIsDarkMode.current
    val appColors = LocalAppThemeColors.current

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = BorderStroke(if (isEquipped) 2.dp else 1.dp, if (isEquipped) primaryColor else borderColor),
        modifier = Modifier
            .width(145.dp)
            .height(215.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Preview of the Wing
            Box(
                modifier = Modifier
                    .size(width = 118.dp, height = 66.dp),
                contentAlignment = Alignment.Center
            ) {
                ProfileWingDecoration(
                    wingId = wing.id,
                    rank = userRank,
                    avatarDiameter = 42.dp
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(cardColor, CircleShape)
                        .border(1.5.dp, if (isEquipped) primaryColor else borderColor.copy(alpha = 0.8f), CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = equippedAvatarRes),
                        contentDescription = "Profil Resmi",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = wing.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = wing.description,
                    fontSize = 9.sp,
                    color = subtitleColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 11.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action Button
            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth().height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Kuşanıldı",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                isOwned -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onClick() }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Kuşan",
                                color = primaryColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                wing.id == "kingdom_kanat" -> {
                    // Kingdom kanat: Sadece 400 Elmas veya Krallık Teması hediyesi
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onBuyGem?.invoke() ?: onClick() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${wing.priceGems}",
                                color = Color(0xFF0284C7),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(13.dp))
                        }
                    }
                }
                wing.priceCoins > 0 && wing.priceGems > 0 -> {
                    // Dual currency buttons (Coin & Elmas)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Coin button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onBuyCoin?.invoke() ?: onClick() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (wing.priceCoins >= 1000) String.format("%,d", wing.priceCoins) else "${wing.priceCoins}",
                                    fontSize = if (wing.priceCoins >= 10000) 8.sp else 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFB45309),
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(1.dp))
                                LottieIcon(rawRes = R.raw.anim_coin, modifier = Modifier.size(11.dp))
                            }
                        }

                        // Gem button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onBuyGem?.invoke() ?: onClick() }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${wing.priceGems}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0284C7),
                                    maxLines = 1
                                )
                                Spacer(Modifier.width(1.dp))
                                LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, appColors.primary.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clickable { onClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "Seç",
                                color = if (isDark) appColors.primary else Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketItemDetailDialog(
    item: MarketItemDialogData,
    userGems: Int,
    userCoins: Int,
    userRank: RankType,
    onDismiss: () -> Unit,
    onConfirm: (() -> Unit)? = null,
    onExecutePurchase: ((() -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

    val canAffordCoins = item.priceCoins > 0 && userCoins >= item.priceCoins
    val canAffordGems = item.priceGems > 0 && userGems >= item.priceGems

    val handlePurchaseClick: ((() -> Unit)?) -> Unit = { action ->
        if (action != null) {
            onDismiss()
            if (onExecutePurchase != null) {
                onExecutePurchase(action)
            } else {
                action.invoke()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surface),
            border = BorderStroke(1.5.dp, appColors.border),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Category Tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = appColors.primary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, appColors.primary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = item.category.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = appColors.primary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enlarged Circular Frame
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    appColors.primary.copy(alpha = 0.15f),
                                    if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9)
                                )
                            ),
                            shape = CircleShape
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    appColors.primary,
                                    Color(0xFFFFD700),
                                    Color(0xFF38BDF8),
                                    appColors.primary
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        item.isWing && item.wingData != null -> {
                            Box(
                                modifier = Modifier.size(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileWingDecoration(
                                    wingId = item.wingData.id,
                                    rank = userRank,
                                    modifier = Modifier.size(124.dp)
                                )
                                val equippedAvatarRes = UserCosmeticsStore.getEquippedAvatarRes(context)
                                Image(
                                    painter = painterResource(id = equippedAvatarRes),
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        }
                        item.isFrame -> {
                            val frameBorderColor = when (item.iconName) {
                                "altin" -> Color(0xFFFFD700)
                                "cicek" -> Color(0xFFF43F5E)
                                "neon" -> Color(0xFF00E5FF)
                                "kraliyet" -> Color(0xFFFFB300)
                                "blue" -> Color(0xFF3897F0)
                                else -> appColors.primary
                            }
                            val equippedAvatarRes = UserCosmeticsStore.getEquippedAvatarRes(context)
                            Box(
                                modifier = Modifier
                                    .size(105.dp)
                                    .background(appColors.surfaceRaised, CircleShape)
                                    .border(4.dp, frameBorderColor, CircleShape)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = equippedAvatarRes),
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        item.lottieRes != null -> {
                            LottieIcon(
                                rawRes = item.lottieRes,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                        item.iconName.isNotBlank() -> {
                            Image(
                                painter = painterResource(id = getMarketDrawableRes(context, item.iconName)),
                                contentDescription = item.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = item.title,
                                tint = appColors.primary,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item Title
                Text(
                    text = item.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.text,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Item Description
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = appColors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Price / Balance Check Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, appColors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (item.isOwned) "Mevcut Durum:" else "Fiyat Seçenekleri:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textMuted
                            )

                            when {
                                item.isEquipped -> {
                                    Text(
                                        text = "Şu Anda Kuşanılı",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                                item.isOwned -> {
                                    Text(
                                        text = "Envanterde Mevcut",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.primary
                                    )
                                }
                                item.isRealMoney && item.priceGems <= 0 -> {
                                    Text(
                                        text = item.realMoneyPrice,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2563EB)
                                    )
                                }
                                else -> {}
                            }
                        }

                        if (!item.isOwned && !item.isEquipped) {
                            if (item.realMoneyPrice.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Gerçek Para:", fontSize = 11.sp, color = appColors.textMuted)
                                    Text(item.realMoneyPrice, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2563EB))
                                }
                            }
                            if (item.priceCoins > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LottieIcon(rawRes = R.raw.anim_coin, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${String.format("%,d", item.priceCoins)} Coin", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    }
                                    Text("Bakiye: ${String.format("%,d", userCoins)}", fontSize = 11.sp, color = if (canAffordCoins) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            if (item.priceGems > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("${item.priceGems} Elmas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    }
                                    Text("Bakiye: $userGems", fontSize = 11.sp, color = if (canAffordGems) Color(0xFF10B981) else Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                when {
                    item.isEquipped -> {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Text("Kuşanıldı (Kapat)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        }
                    }
                    item.isOwned -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Kapat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    item.onConfirm?.invoke() ?: onConfirm?.invoke()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                                modifier = Modifier.weight(1.3f).height(46.dp)
                            ) {
                                Text("Kuşan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                    item.isRealMoney && item.priceGems > 0 -> {
                        // Uzay theme: Real money ₺20 or 200 Gems
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { handlePurchaseClick(item.onConfirmRealMoney) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Text("${item.realMoneyPrice} ile Al", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                                }
                                Button(
                                    onClick = { handlePurchaseClick(item.onConfirmGems) },
                                    enabled = canAffordGems,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("${item.priceGems} Elmas", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Vazgeç", color = appColors.textMuted, fontSize = 12.sp)
                            }
                        }
                    }
                    item.isRealMoney -> {
                        // Real Money only (e.g. Krallık Mew or CurrencyPack)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Vazgeç", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { handlePurchaseClick(item.onConfirmRealMoney ?: item.onConfirm ?: onConfirm) },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier.weight(1.5f).height(46.dp)
                            ) {
                                Text("${item.realMoneyPrice} Satın Al", fontWeight = FontWeight.Black, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                    item.priceCoins > 0 && item.priceGems > 0 -> {
                        // Dual Currency (Coin and Gem)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { handlePurchaseClick(item.onConfirmCoins) },
                                    enabled = canAffordCoins,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706), disabledContainerColor = Color(0xFFD97706).copy(alpha = 0.35f)),
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LottieIcon(rawRes = R.raw.anim_coin, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("${String.format("%,d", item.priceCoins)} Coin", fontWeight = FontWeight.Black, fontSize = if (item.priceCoins >= 10000) 10.sp else 11.5.sp, color = Color.White)
                                    }
                                }
                                Button(
                                    onClick = { handlePurchaseClick(item.onConfirmGems) },
                                    enabled = canAffordGems,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7), disabledContainerColor = Color(0xFF0284C7).copy(alpha = 0.35f)),
                                    modifier = Modifier.weight(1f).height(46.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("${item.priceGems} Elmas", fontWeight = FontWeight.Black, fontSize = 11.5.sp, color = Color.White)
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(42.dp)
                            ) {
                                Text("Vazgeç", color = appColors.textMuted, fontSize = 12.sp)
                            }
                        }
                    }
                    item.priceGems > 0 -> {
                        // Gem only (e.g. kingdom_kanat 400 Elmas)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Vazgeç", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = { handlePurchaseClick(item.onConfirmGems ?: item.onConfirm ?: onConfirm) },
                                enabled = canAffordGems,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1.4f).height(46.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LottieIcon(rawRes = R.raw.anim_diamond, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("${item.priceGems} Elmas ile Al", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    else -> {
                        // Free / Default
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Text("Kapat", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    item.onConfirm?.invoke() ?: onConfirm?.invoke()
                                    onDismiss()
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                                modifier = Modifier.weight(1.3f).height(46.dp)
                            ) {
                                Text("Seç", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}


