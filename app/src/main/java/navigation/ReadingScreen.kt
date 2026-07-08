package com.example.ngilizcequizapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
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
    val topics = allStories.keys.mapIndexed { index, title -> ReadingTopicUi(title, topicImages[index % topicImages.size]) }
    val completed = topics.count { ReadingProgressStore.isCompleted(context, it.title) }
    var selectedLevel by rememberSaveable { mutableIntStateOf(-1) }
    val levels = listOf(
        ReadingLevelUi("Beginner", "A1-A2", "plant1", Color(0xFF43B95C)),
        ReadingLevelUi("Elementary", "A2-B1", "plant2", Color(0xFF2E8EF5)),
        ReadingLevelUi("Intermediate", "B1-B2", "plant3", Color(0xFF8A62DB)),
        ReadingLevelUi("Advanced", "B2-C1", "plant4", Color(0xFFF07335))
    )
    val unfinished = topics.filterNot { ReadingProgressStore.isCompleted(context, it.title) }
    val nextTopic = unfinished.maxByOrNull { ReadingProgressStore.lastStartedAt(context, it.title) }
        ?: topics.last()
    val nextProgress = if (ReadingProgressStore.lastStartedAt(context, nextTopic.title) > 0L) .6f else 0f

    Scaffold(
        containerColor = appColors.background,
        bottomBar = { ReadingBottomNav(navController) }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).mewScreenBackground(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { ReadingWalletBar() }
            item {
                ReadingHeader(
                    navController = navController,
                    completed = completed,
                    total = topics.size,
                    onBack = if (selectedLevel >= 0) ({ selectedLevel = -1 }) else null
                )
            }
            if (selectedLevel < 0) {
                item { ReadingHeroCard { selectedLevel = 0 } }
                item { Text("Seviyenizi Seçin", fontSize = 20.sp, fontWeight = FontWeight.Black, color = appColors.text) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(end = 8.dp)) {
                        levels.forEachIndexed { index, level ->
                            item {
                                ReadingLevelCard(level, if (index == 0) topics.size else 0, Modifier.width(150.dp)) {
                                    selectedLevel = index
                                }
                            }
                        }
                    }
                }
                item {
                    Text("Continue Reading", fontSize = 20.sp, fontWeight = FontWeight.Black, color = appColors.text, modifier = Modifier.padding(top = 8.dp))
                }
                item {
                    ContinueReadingCard(
                        title = nextTopic.title,
                        progress = nextProgress,
                        onClick = { navController.navigate("ReadingDetail/${android.net.Uri.encode(nextTopic.title)}") }
                    )
                }
            } else {
                item {
                    Text(levels[selectedLevel].title, fontSize = 24.sp, fontWeight = FontWeight.Black, color = appColors.text)
                    Text("${levels[selectedLevel].range} reading parçaları", color = appColors.textMuted)
                }
                val levelTopics = if (selectedLevel == 0) topics else emptyList()
                if (levelTopics.isEmpty()) item {
                    Text("Bu seviye için yeni reading parçaları hazırlanıyor.", color = appColors.textMuted, modifier = Modifier.padding(vertical = 36.dp))
                }
                items(levelTopics) { topic ->
                    ReadingTopicCard(
                        title = topic.title,
                        imageRes = topic.imageRes,
                        completed = ReadingProgressStore.isCompleted(context, topic.title),
                        earnedXp = ReadingProgressStore.earnedXp(context, topic.title),
                        earnedCoins = ReadingProgressStore.earnedCoins(context, topic.title),
                        onClick = { navController.navigate("ReadingDetail/${android.net.Uri.encode(topic.title)}") }
                    )
                }
            }
        }
    }
}

@Composable
fun ReadingHeader(navController: NavHostController, completed: Int, total: Int, onBack: (() -> Unit)? = null) {
    val appColors = LocalAppThemeColors.current
    Row(Modifier.fillMaxWidth().statusBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onBack?.invoke() ?: navController.popBackStack() }, modifier = Modifier.size(40.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = appColors.text)
        }
        Box(Modifier.size(58.dp).background(appColors.surface, CircleShape), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.kitap1), null, Modifier.size(50.dp), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Reading", fontSize = 26.sp, fontWeight = FontWeight.Black, color = appColors.text)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.Eco, null, tint = Color(0xFF62AEEB), modifier = Modifier.size(23.dp))
            }
            Text("Read, understand and grow your English!", color = appColors.textMuted, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { if (total > 0) completed.toFloat() / total else 0f },
                modifier = Modifier.fillMaxSize(),
                color = appColors.primary,
                trackColor = appColors.primarySoft,
                strokeWidth = 7.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.kitap1), null, Modifier.size(26.dp))
                Text("$completed/$total", fontWeight = FontWeight.Black, color = appColors.text, fontSize = 11.sp)
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
private fun ReadingLevelCard(level: ReadingLevelUi, lessonCount: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = modifier.height(190.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = appColors.surface,
        shadowElevation = 4.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(16.dp))
            Text(level.title, color = level.color, fontWeight = FontWeight.Black, fontSize = 13.sp, maxLines = 1)
            Text(level.range, color = appColors.textMuted, fontSize = 12.sp)
            Image(
                painterResource(resolveDrawable(level.imageName, R.drawable.education)),
                contentDescription = null,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(10.dp),
                contentScale = ContentScale.Fit
            )
            Box(
                Modifier.fillMaxWidth().height(36.dp).background(level.color.copy(alpha = .12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("$lessonCount Lessons", color = level.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ReadingHeroCard(onStart: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    Surface(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(28.dp),
        color = appColors.primarySoft,
        shadowElevation = 4.dp
    ) {
        Box(Modifier.background(Brush.horizontalGradient(listOf(appColors.primarySoft, appColors.surfaceRaised)))) {
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
                                    0.60f to Color.Transparent,
                                    0.82f to appColors.surface,
                                    1f to appColors.surface
                                )
                            ),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                Image(
                    painterResource(R.drawable.icecat), null,
                    Modifier.fillMaxHeight().fillMaxWidth(.48f).align(Alignment.CenterEnd).padding(8.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Column(Modifier.fillMaxWidth(.58f).padding(20.dp), verticalArrangement = Arrangement.Center) {
                Text("Okuma Parçaları", color = appColors.primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(5.dp))
                Text("Level up your reading skills", fontSize = 23.sp, fontWeight = FontWeight.Black, color = appColors.text, lineHeight = 27.sp)
                Spacer(Modifier.height(8.dp))
                Text("Choose a level, read short passages and learn new words.", color = appColors.textMuted, fontSize = 14.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onStart, shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)) {
                    Text("Okumaya başla", fontWeight = FontWeight.Bold)
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
    val appColors = LocalAppThemeColors.current
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = appColors.surface, shadowElevation = 4.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.reading), null, Modifier.size(82.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Continue", color = appColors.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(title, color = appColors.text, fontWeight = FontWeight.Black, fontSize = 17.sp)
                Spacer(Modifier.height(7.dp))
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape), color = appColors.primary, trackColor = appColors.primarySoft)
                Text("You were ${(progress * 100).toInt()}% complete", color = appColors.textMuted, fontSize = 11.sp)
            }
            Spacer(Modifier.width(10.dp))
            FilledIconButton(onClick = onClick, colors = IconButtonDefaults.filledIconButtonColors(containerColor = appColors.primary)) {
                Icon(Icons.Default.PlayArrow, "Devam et")
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
