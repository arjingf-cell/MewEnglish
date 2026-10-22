package com.example.ngilizcequizapp

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition

val ReadingPageBlue = Color(0xFF1F8EF1)
val ReadingPaleBlue = Color(0xFFE8F6FE)
val ReadingNavy = Color(0xFF18324A)
val ReadingMuted = Color(0xFF6F8499)
val ReadingPageBackground = Color(0xFFF7FBFE)

private data class ReadingTopicUi(val title: String, val imageRes: Int)
private data class ReadingLevelUi(val title: String, val range: String, val imageName: String, val color: Color)

@Composable
fun ReadingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val topicImages = listOf(R.drawable.sing, R.drawable.notebook, R.drawable.family, R.drawable.house, R.drawable.bread, R.drawable.read, R.drawable.kitap1, R.drawable.education)
    val a1Topics = remember {
        ReadingDataRepository.a1Stories.mapIndexed { index, story ->
            ReadingTopicUi(story.title, topicImages[index % topicImages.size])
        }
    }
    val a2Topics = remember {
        ReadingDataRepository.a2Stories.mapIndexed { index, story ->
            ReadingTopicUi(story.title, topicImages[index % topicImages.size])
        }
    }
    val allTopics = remember(a1Topics, a2Topics) { a1Topics + a2Topics }
    val completed = allTopics.count { ReadingProgressStore.isCompleted(context, it.title) }
    val isSpaceTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }

    var selectedLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levels = listOf(
        ReadingLevelUi("Beginner", "A1", if (isKingdomTheme) "kingdom_plant1" else "plant1", Color(0xFF43B95C)),
        ReadingLevelUi("Elementary", "A2", if (isKingdomTheme) "kingdom_plant2" else "plant2", Color(0xFF2E8EF5)),
        ReadingLevelUi("Intermediate", "B1", if (isKingdomTheme) "kingdom_plant3" else "plant3", Color(0xFF8A62DB)),
        ReadingLevelUi("Advanced", "B2-C1", if (isKingdomTheme) "kingdom_plant4" else "plant4", Color(0xFFF07335))
    )
    val unfinished = allTopics.filterNot { ReadingProgressStore.isCompleted(context, it.title) }
    val nextTopic = unfinished.maxByOrNull { ReadingProgressStore.lastStartedAt(context, it.title) }
        ?: allTopics.firstOrNull() ?: ReadingTopicUi("My Pet Dog", topicImages[0])
    val nextProgress = if (ReadingProgressStore.lastStartedAt(context, nextTopic.title) > 0L) .6f else 0f

    var showLockDialog by remember { mutableStateOf<String?>(null) }
    var showOutOfEnergyDialog by remember { mutableStateOf(false) }

    if (showOutOfEnergyDialog) {
        OutOfEnergyDialog(onDismiss = { showOutOfEnergyDialog = false })
    }

    if (showLockDialog != null) {
        AlertDialog(
            onDismissRequest = { showLockDialog = null },
            icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = appColors.primary, modifier = Modifier.size(32.dp)) },
            title = { Text("Kilitli Seviye", fontWeight = FontWeight.Black, fontSize = 20.sp, color = appColors.text) },
            text = {
                Text(
                    "Bu seviyenin kilidini açmak için Beginner (A1) ve Elementary (A2) seviyelerindeki okuma parçalarını tamamlamalısınız.",
                    fontSize = 15.sp,
                    color = appColors.textMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = { showLockDialog = null },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Text("Anladım", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = appColors.surface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        containerColor = if (isSpaceTheme || isKingdomTheme) Color.Transparent else appColors.background,
        bottomBar = { ReadingBottomNav(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ReadingWalletBar() }
                item {
                    ReadingHeader(
                        navController = navController,
                        completed = completed,
                        total = allTopics.size,
                        onBack = if (selectedLevel >= 0) ({ selectedLevel = -1 }) else null
                    )
                }
                if (selectedLevel < 0) {
                    item { ReadingHeroCard { selectedLevel = 0 } }
                    item { Text(if (isKingdomTheme) "👑 Seviyenizi Seçin" else "Seviyenizi Seçin", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text) }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                            levels.forEachIndexed { index, level ->
                                item {
                                    val isLocked = index > 1
                                    ReadingLevelCard(
                                        level = level,
                                        lessonCount = when (index) {
                                            0 -> a1Topics.size
                                            1 -> a2Topics.size
                                            else -> 0
                                        },
                                        isLocked = isLocked,
                                        modifier = Modifier.width(150.dp)
                                    ) {
                                        if (isLocked) {
                                            showLockDialog = level.title
                                        } else {
                                            selectedLevel = index
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Text(if (isKingdomTheme) "👑 Okumaya Devam Et" else "Continue Reading", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text, modifier = Modifier.padding(top = 8.dp))
                    }
                    item {
                        ContinueReadingCard(
                            title = nextTopic.title,
                            progress = nextProgress,
                            onClick = {
                                if (com.example.ngilizcequizapp.EnergyManager.hasEnergy(context)) {
                                    com.example.ngilizcequizapp.EnergyManager.useEnergy(context)
                                    navController.navigate("ReadingDetail/${android.net.Uri.encode(nextTopic.title)}")
                                } else {
                                    showOutOfEnergyDialog = true
                                }
                            }
                        )
                    }
                } else {
                    item {
                        Text(levels[selectedLevel].title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = appColors.text)
                        Text("${levels[selectedLevel].range} reading parçaları", color = appColors.textMuted)
                    }
                    val levelTopics = when (selectedLevel) {
                        0 -> a1Topics
                        1 -> a2Topics
                        else -> emptyList()
                    }
                    items(levelTopics) { topic ->
                        ReadingTopicCard(
                            title = topic.title,
                            imageRes = topic.imageRes,
                            completed = ReadingProgressStore.isCompleted(context, topic.title),
                            earnedXp = ReadingProgressStore.earnedXp(context, topic.title),
                            earnedCoins = ReadingProgressStore.earnedCoins(context, topic.title),
                            onClick = {
                                if (com.example.ngilizcequizapp.EnergyManager.hasEnergy(context)) {
                                    com.example.ngilizcequizapp.EnergyManager.useEnergy(context)
                                    navController.navigate("ReadingDetail/${android.net.Uri.encode(topic.title)}")
                                } else {
                                    showOutOfEnergyDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ReadingHeader(navController: NavHostController, completed: Int, total: Int, onBack: (() -> Unit)? = null) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val bookIcon = if (isKingdomTheme) R.drawable.kingdom_book_icon else R.drawable.kitap1
    val progressIcon = if (isKingdomTheme) R.drawable.kingdom_progress_icon else R.drawable.kitap1

    Column(Modifier.fillMaxWidth().statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onBack?.invoke() ?: navController.popBackStack() }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text)
            }
            CartoonEnergyBadge()
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(56.dp)
                    .background(
                        if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface,
                        CircleShape
                    )
                    .then(
                        if (isKingdomTheme) Modifier.border(
                            1.2.dp,
                            Brush.linearGradient(listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11))),
                            CircleShape
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(bookIcon), null, Modifier.size(if (isKingdomTheme) 40.dp else 46.dp), contentScale = ContentScale.Fit)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isKingdomTheme) "Kraliyet Okuması" else "Reading", fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (isKingdomTheme) Color.White else appColors.text)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.Eco, null, tint = if (isKingdomTheme) Color(0xFFFFD700) else Color(0xFF62AEEB), modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(2.dp))
                Text(if (isKingdomTheme) "Kraliyet hikayelerini oku, anla ve kelime hazineni genişlet!" else "Read, understand and grow your English!", color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.8f) else appColors.textMuted, fontSize = 11.5.sp, lineHeight = 15.sp)
            }
            Spacer(Modifier.width(10.dp))
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (total > 0) completed.toFloat() / total else 0f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary,
                    trackColor = if (isKingdomTheme) Color(0xFF1B3A6B) else appColors.primarySoft,
                    strokeWidth = 6.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(progressIcon), null, Modifier.size(22.dp), contentScale = ContentScale.Fit)
                    Text("$completed/$total", fontWeight = FontWeight.Black, color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text, fontSize = 10.5.sp)
                }
            }
        }
    }
}

@Composable
private fun ReadingWalletBar() {
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ReadingWalletItem(
            rawRes = R.raw.anim_coin,
            value = QuizTreeProgressStore.getCoins(context).toString(),
            showPlus = true,
            iterations = 1,
            modifier = Modifier.weight(1f)
        )
        ReadingWalletItem(
            rawRes = R.raw.badge,
            value = "${QuizTreeProgressStore.getXp(context)} XP",
            showPlus = false,
            iterations = 1,
            modifier = Modifier.weight(1f)
        )
        ReadingWalletItem(
            rawRes = R.raw.anim_diamond,
            value = QuizTreeProgressStore.getGems(context).toString(),
            showPlus = true,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReadingWalletItem(
    rawRes: Int,
    value: String,
    showPlus: Boolean,
    iterations: Int,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppThemeColors.current
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    Surface(modifier.height(58.dp), shape = RoundedCornerShape(28.dp), color = appColors.surfaceRaised) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = iterations,
                modifier = Modifier.size(34.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(value, fontWeight = FontWeight.Black, color = appColors.text, fontSize = 16.sp)
            if (showPlus) {
                Spacer(Modifier.width(3.dp))
                Text("+", fontWeight = FontWeight.Black, color = appColors.textMuted, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ReadingLevelCard(
    level: ReadingLevelUi,
    lessonCount: Int,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }

    val cardBorder = if (isKingdomTheme) {
        BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFFFFDF7A),
                    Color(0xFFD4AF37),
                    Color(0xFFAA7C11),
                    Color(0xFFFFDF7A)
                )
            )
        )
    } else null

    val cardBg = if (isKingdomTheme) {
        Color(0xFF070F26).copy(alpha = 0.92f)
    } else appColors.surface

    Surface(
        modifier = modifier.height(190.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = cardBg,
        border = cardBorder,
        shadowElevation = 4.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            Text(
                level.title,
                color = if (isKingdomTheme) Color(0xFFFFD700) else level.color,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                maxLines = 1
            )
            Text(
                level.range,
                color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.75f) else appColors.textMuted,
                fontSize = 12.sp
            )
            Image(
                painterResource(resolveDrawable(level.imageName, R.drawable.education)),
                contentDescription = null,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(10.dp),
                contentScale = ContentScale.Fit
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        if (isKingdomTheme) Color(0xFF1B3A6B).copy(alpha = 0.65f) else level.color.copy(alpha = .12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isKingdomTheme) Color(0xFFFFD700) else level.color,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Kilitli",
                            color = if (isKingdomTheme) Color(0xFFFFD700) else level.color,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        "$lessonCount Lessons",
                        color = if (isKingdomTheme) Color(0xFFFFD700) else level.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingHeroCard(onStart: () -> Unit) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isSpaceTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val readingImageRes = when {
        isKingdomTheme -> R.drawable.kingdom_reading
        isSpaceTheme -> R.drawable.bigboxspace4
        else -> R.drawable.icecat
    }
    val cardBorder = when {
        isKingdomTheme -> BorderStroke(1.6.dp, Brush.linearGradient(listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))))
        isSpaceTheme -> BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8))))
        else -> null
    }
    val cardBg = when {
        isKingdomTheme -> Color(0xFF070F26).copy(alpha = 0.92f)
        isSpaceTheme -> Color(0xFF0C132C).copy(alpha = 0.90f)
        else -> appColors.primarySoft
    }

    Surface(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(28.dp),
        color = cardBg,
        border = cardBorder,
        shadowElevation = 4.dp
    ) {
        val contentBgModifier = if (isSpaceTheme || isKingdomTheme) Modifier.background(Color.Transparent) else Modifier.background(Brush.horizontalGradient(listOf(appColors.primarySoft, appColors.surfaceRaised)))
        Box(Modifier.then(contentBgModifier)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0f to Color.Transparent,
                                    0.42f to Color.Transparent,
                                    0.78f to Color.Black,
                                    1f to Color.Black
                                )
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                Image(
                    painterResource(readingImageRes), null,
                    Modifier.fillMaxHeight().fillMaxWidth(0.60f).align(Alignment.CenterEnd),
                    contentScale = ContentScale.Crop
                )
            }
            Column(Modifier.fillMaxWidth(.52f).padding(20.dp), verticalArrangement = Arrangement.Center) {
                Text(
                    if (isKingdomTheme) "👑 Kraliyet Kütüphanesi" else if (isSpaceTheme) "🚀 Kozmik Okuma" else "Okuma Parçaları",
                    color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    if (isKingdomTheme) "Asil Okuma Serüveni" else if (isSpaceTheme) "Galaktik Okuma İstasyonu" else "Level up your reading skills",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isKingdomTheme) Color.White else appColors.text,
                    lineHeight = 25.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (isKingdomTheme) "Seviyeni seç, asil hikayeler ve efsanelerle kelime dağarcığını zenginleştir." else if (isSpaceTheme) "Seviyeni seç, kozmik kısa parçalarla kelimeleri keşfet." else "Choose a level, read short passages and learn new words.",
                    color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.85f) else appColors.textMuted,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isKingdomTheme) Color(0xFF1B3A6B) else appColors.primary),
                    border = if (isKingdomTheme) BorderStroke(1.dp, Color(0xFFFFD700)) else null
                ) {
                    Text(if (isKingdomTheme) "Okumaya başla  ›" else "Okumaya başla", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun resolveDrawable(name: String, fallback: Int): Int {
    val context = LocalContext.current
    return context.resources.getIdentifier(name, "drawable", context.packageName).takeIf { it != 0 } ?: fallback
}

@Composable
fun ReadingTopicCard(title: String, imageRes: Int, completed: Boolean, earnedXp: Int, earnedCoins: Int, onClick: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    val coin by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.anim_coin))
    val xp by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.badge))
    Surface(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp), color = appColors.surface, shadowElevation = 3.dp
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(imageRes), null, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, color = appColors.text, fontSize = 16.sp)
                Text(if (completed) "1 / 1 article" else "0 / 1 article", color = appColors.textMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (completed) 1f else 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = appColors.primary, trackColor = appColors.primarySoft
                )
                if (completed) Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E9D78), modifier = Modifier.size(18.dp))
                    LottieAnimation(coin, iterations = 1, modifier = Modifier.size(28.dp))
                    Text("$earnedCoins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = appColors.text)
                    LottieAnimation(xp, iterations = 1, modifier = Modifier.size(28.dp))
                    Text("$earnedXp XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = appColors.text)
                }
            }
            Box(Modifier.size(38.dp).background(appColors.primarySoft, CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = appColors.primary)
            }
        }
    }
}

@Composable
fun ContinueReadingCard(title: String, progress: Float, onClick: () -> Unit) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val continueImageRes = if (isKingdomTheme) R.drawable.kingdom_continue_reading else R.drawable.reading

    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface,
        border = if (isKingdomTheme) BorderStroke(1.4.dp, Color(0xFFFFD700).copy(alpha = 0.8f)) else null,
        shadowElevation = 4.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(continueImageRes),
                null,
                Modifier.size(82.dp).clip(RoundedCornerShape(18.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (isKingdomTheme) "👑 Okumaya Devam Et" else "Continue",
                    color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(title, color = if (isKingdomTheme) Color.White else appColors.text, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Spacer(Modifier.height(7.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                    color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary,
                    trackColor = if (isKingdomTheme) Color(0xFF1B3A6B) else appColors.primarySoft
                )
                Text("İlerleme: %${(progress * 100).toInt()}", color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.75f) else appColors.textMuted, fontSize = 11.sp)
            }
            Spacer(Modifier.width(10.dp))
            FilledIconButton(
                onClick = onClick,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = if (isKingdomTheme) Color(0xFF1B3A6B) else appColors.primary)
            ) {
                Icon(Icons.Default.PlayArrow, "Devam et", tint = if (isKingdomTheme) Color(0xFFFFD700) else Color.White)
            }
        }
    }
}

@Composable
fun ReadingBottomNav(navController: NavHostController) {
    val appColors = LocalAppThemeColors.current
    NavigationBar(containerColor = appColors.surface, tonalElevation = 8.dp) {
        val items = listOf(
            Triple("Ana Sayfa", R.drawable.home, Screen.Home.route),
            Triple("İlerleme", R.drawable.kupa, Screen.Progress.route),
            Triple("Reading", R.drawable.reading, "reading_screen"),
            Triple("Sözlük", R.drawable.dicti, Screen.Word.route)
        )
        items.forEach { (label, icon, route) ->
            NavigationBarItem(
                selected = route == "reading_screen",
                onClick = { navController.navigate(route) { launchSingleTop = true } },
                icon = { Image(painterResource(icon), label, Modifier.size(if (route == "reading_screen") 34.dp else 29.dp)) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(indicatorColor = appColors.primarySoft, selectedTextColor = appColors.primary, unselectedTextColor = appColors.textMuted)
            )
        }
    }
}
