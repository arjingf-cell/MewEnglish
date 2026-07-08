package com.example.ngilizcequizapp

import android.content.Context
import android.os.SystemClock
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.example.ngilizcequizapp.MewTopAppBar

// Lottie Animation Imports
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

// Streak Tracker Manager
object StreakManager {
    private const val PREFS_NAME = "streak_prefs"
    private const val KEY_STREAK_COUNT = "streak_count"
    private const val KEY_LAST_ENTRY_DATE = "last_entry_date"

    fun updateStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())

        val lastEntry = prefs.getString(KEY_LAST_ENTRY_DATE, "")
        var currentStreak = prefs.getInt(KEY_STREAK_COUNT, 0)

        if (lastEntry.isNullOrEmpty()) {
            currentStreak = 1
            prefs.edit()
                .putInt(KEY_STREAK_COUNT, currentStreak)
                .putString(KEY_LAST_ENTRY_DATE, todayStr)
                .apply()
        } else if (lastEntry == todayStr) {
            if (currentStreak == 0) {
                currentStreak = 1
                prefs.edit().putInt(KEY_STREAK_COUNT, currentStreak).apply()
            }
        } else {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(calendar.time)

            if (lastEntry == yesterdayStr) {
                currentStreak += 1
            } else {
                currentStreak = 1
            }
            prefs.edit()
                .putInt(KEY_STREAK_COUNT, currentStreak)
                .putString(KEY_LAST_ENTRY_DATE, todayStr)
                .apply()
        }
        return currentStreak
    }

    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val streak = prefs.getInt(KEY_STREAK_COUNT, 0)
        return if (streak == 0) 1 else streak
    }
}

// Activity Duration Tracker
object ActivityTracker {
    private const val PREFS_NAME = "activity_tracker_prefs"
    var sessionStartTime: Long = SystemClock.elapsedRealtime()

    fun recordSessionTime(context: Context) {
        val now = SystemClock.elapsedRealtime()
        val elapsedSec = (now - sessionStartTime) / 1000
        if (elapsedSec > 0) {
            val currentDayKey = getCurrentDayKey()
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentVal = prefs.getInt(currentDayKey, 0)
            prefs.edit().putInt(currentDayKey, currentVal + elapsedSec.toInt()).apply()
        }
        sessionStartTime = now
    }

    fun getWeeklyActivityMinutes(context: Context): List<DayActivity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        val keys = listOf("activity_sec_pzt", "activity_sec_sal", "activity_sec_çar", "activity_sec_per", "activity_sec_cum", "activity_sec_cmt", "activity_sec_paz")
        
        val currentDayIdx = getTodayIndex()
        val liveElapsedSec = (SystemClock.elapsedRealtime() - sessionStartTime) / 1000

        return days.mapIndexed { index, day ->
            val savedSec = prefs.getInt(keys[index], 0)
            val totalSec = if (index == currentDayIdx) savedSec + liveElapsedSec.toInt() else savedSec
            val totalMin = (totalSec / 60)
            DayActivity(day, totalMin)
        }
    }

    fun resetActivity(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keys = listOf("activity_sec_pzt", "activity_sec_sal", "activity_sec_çar", "activity_sec_per", "activity_sec_cum", "activity_sec_cmt", "activity_sec_paz")
        prefs.edit().apply {
            keys.forEach { putInt(it, 0) }
            apply()
        }
        sessionStartTime = SystemClock.elapsedRealtime()
    }

    private fun getCurrentDayKey(): String {
        val keys = listOf("activity_sec_paz", "activity_sec_pzt", "activity_sec_sal", "activity_sec_çar", "activity_sec_per", "activity_sec_cum", "activity_sec_cmt")
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return keys[dayOfWeek - 1]
    }

    private fun getTodayIndex(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }
    }
}

// Rank Hierarchy Definition
enum class RankType(
    val nameStr: String,
    val titleStr: String,
    val minXp: Int,
    val maxXp: Int,
    val color: Color
) {
    BRONZE("Bronz", "Çömez Dilci", 0, 299, Color(0xFFCD7F32)),
    SILVER("Gümüş", "Pratik Yapan", 300, 699, Color(0xFF9E9E9E)),
    GOLD("Altın", "Konuşkan", 700, 1499, Color(0xFFFFD700)),
    PLATINUM("Platin", "Akıcı Konuşan", 1500, 2999, Color(0xFF00E5FF)),
    DIAMOND("Elmas", "Dil Uzmanı", 3000, 4999, Color(0xFF2979FF)),
    MASTER("Usta", "Kelimelerin Efendisi", 5000, 9999, Color(0xFFFF5722)),
    GRANDMASTER("Üstad", "Edebiyatçı", 10000, 19999, Color(0xFF9C27B0)),
    LINGUARIUM("Linguarium", "Dil Üstadı", 20000, Int.MAX_VALUE, Color(0xFFFF3D00))
}

fun getRankForXp(xp: Int): RankType {
    return RankType.values().firstOrNull { xp in it.minXp..it.maxXp } ?: RankType.BRONZE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavHostController) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Initialize/Reset mock activity values on first load
    LaunchedEffect(Unit) {
        val trackerPrefs = context.getSharedPreferences("activity_tracker_prefs", Context.MODE_PRIVATE)
        if (!trackerPrefs.getBoolean("initialized_v3", false)) {
            ActivityTracker.resetActivity(context)
            trackerPrefs.edit().putBoolean("initialized_v3", true).apply()
        }
    }

    // Refresh loop to calculate live time spent and update graphs dynamically
    var tick by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            tick++
        }
    }

    // Save elapsed time dynamically on tick
    LaunchedEffect(tick) {
        ActivityTracker.recordSessionTime(context)
    }

    // Fetch user statistics
    val xp = remember(tick) { QuizTreeProgressStore.getXp(context) }
    val coins = remember(tick) { QuizTreeProgressStore.getCoins(context) }
    val gems = remember(tick) { QuizTreeProgressStore.getGems(context) }
    val progressMap = remember(tick) { QuizTreeProgressStore.loadAll(context) }
    val allSteps = remember { QuizDataHolder.quizSteps }
    val currentStreak = remember(context) { StreakManager.updateStreak(context) }

    val solvedCount = remember(progressMap, allSteps) {
        allSteps.indices.count { quizIndex ->
            val result = progressMap[quizIndex]
            val stars = QuizTreeProgressStore.starsFor(result)
            stars >= 1
        }
    }

    val learnedWordsCount by remember(progressMap, allSteps) {
        derivedStateOf {
            val quizWords = allSteps.indices.sumOf { quizIndex ->
                val result = progressMap[quizIndex]
                val stars = QuizTreeProgressStore.starsFor(result)
                if (stars >= 3) {
                    val quizList = QuizRepository.getQuizByNumber(quizIndex + 1)
                    (quizList as? QuizList)?.wordsLearned ?: 0
                } else {
                    0
                }
            }

            val uniqueReadingWords = mutableSetOf<String>()
            for (storyTitle in allStories.keys) {
                if (ReadingProgressStore.isPerfect(context, storyTitle)) {
                    val savedFromThisStory = WordStorage.savedWords
                        .filter { it.sourceStory?.equals(storyTitle, ignoreCase = true) == true }
                        .map { it.word.lowercase().trim() }
                        .distinct()
                    
                    val wordsToCount = savedFromThisStory.take(4)
                    uniqueReadingWords.addAll(wordsToCount)
                }
            }
            quizWords + uniqueReadingWords.size
        }
    }

    val rank = getRankForXp(xp)
    val levelName = "A1"

    val BrokenIceBlueText = Color(0xFF2C3E50)
    val VividIceBlue = Color(0xFF1CB0F6)
    val SoftBg = Color(0xFFF8FBFF)

    val weeklyData = remember(tick) { ActivityTracker.getWeeklyActivityMinutes(context) }

    val unlockedBadges = remember(AchievementManager.unlockedIds) {
        AchievementManager.unlockedIds.mapNotNull { id ->
            AchievementManager.badges.find { it.id == id }
        }
    }

    var selectedBadge by remember { mutableStateOf<BadgeData?>(null) }

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
            // --- 👤 1. USER DETAILS AND LOTTIE STATS ROW ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Looping Rank Animation (Left)
                    Box(
                        modifier = Modifier
                            .size(105.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(rank.color.copy(alpha = 0.1f))
                            .border(2.dp, rank.color, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        RankMediaView(rank = rank, modifier = Modifier.fillMaxSize())
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Username & Lottie Stats Summary
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Arjin Mew",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BrokenIceBlueText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = rank.color.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${rank.nameStr} (${rank.titleStr})",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = rank.color
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Score Stats Block with Lottie Icons
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: XP and Coins side-by-side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // XP (Plays once)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    LottieIcon(resId = R.raw.badge, modifier = Modifier.size(34.dp), loop = false)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$xp XP",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9100)
                                    )
                                }
                                // Coins (Looping)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    LottieIcon(resId = R.raw.anim_coin, modifier = Modifier.size(34.dp), loop = true)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$coins",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFB300)
                                    )
                                }
                            }

                            // Row 2: Centered Diamonds
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LottieIcon(resId = R.raw.anim_diamond, modifier = Modifier.size(34.dp).offset(y = (-3).dp), loop = true)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$gems",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }
            }

            // --- 📚 2. WORDS LEARNED AND DAILY STREAK BOXES WITH LOTTIE ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Learned Words Box with Book.lottie
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LottieIcon(resId = R.raw.book_anim, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$learnedWordsCount",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = BrokenIceBlueText
                            )
                            Text(
                                text = "Öğrenilen Kelimeler",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrokenIceBlueText.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Streak Day Count Box with Fire animation.lottie
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LottieIcon(resId = R.raw.fire_anim, modifier = Modifier.size(50.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$currentStreak Gün",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF6B6B)
                            )
                            Text(
                                text = "Günlük Seri (Streak)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrokenIceBlueText.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // --- 📊 3. RANK AND LEVEL PROGRESS PIE CHARTS ROW ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RankProgressPieCard(
                        xp = xp,
                        rank = rank,
                        textColor = BrokenIceBlueText,
                        modifier = Modifier.weight(1f)
                    )
                    LevelProgressPieCard(
                        solvedCount = solvedCount,
                        totalCount = allSteps.size,
                        currentLevel = levelName,
                        textColor = BrokenIceBlueText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- 📈 4. WEEKLY ACTIVITY DURATION BAR CHART ---
            item {
                WeeklyActivityChart(
                    weeklyData = weeklyData,
                    accentColor = VividIceBlue,
                    textColor = BrokenIceBlueText
                )
            }

            // --- 🏅 5. LATEST BADGES ---
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Son Rozetler", fontWeight = FontWeight.Bold, color = BrokenIceBlueText)
                        Text(
                            text = "Hepsini Gör",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VividIceBlue,
                            modifier = Modifier.clickable {
                                navController.navigate("badges_screen")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (unlockedBadges.isEmpty()) {
                        Text(
                            text = "Henüz kazanılmış rozetiniz bulunmuyor. Tüm rozetleri görmek için tıklayın.",
                            fontSize = 13.sp,
                            color = BrokenIceBlueText.copy(alpha = 0.5f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.clickable {
                                navController.navigate("badges_screen")
                            }
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(unlockedBadges.reversed().size) { index ->
                                val badge = unlockedBadges.reversed()[index]
                                BadgeItem(
                                    badge = badge,
                                    textColor = BrokenIceBlueText,
                                    onClick = { selectedBadge = badge }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Pop-up Box
    if (selectedBadge != null) {
        val badge = selectedBadge!!
        val cleanName = badge.assetName.substringBeforeLast(".")
        val resId = remember(badge.assetName) {
            context.resources.getIdentifier(cleanName, "drawable", context.packageName)
        }

        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("Kapat", color = VividIceBlue, fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Badge Image
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(
                                width = 3.dp,
                                color = badge.rarity.color,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(6.dp)
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (resId != 0) {
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = badge.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.1f)
                                    .offset(y = (-4).dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("🏅", fontSize = 48.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Badge Name (Colored by Rarity)
                    Text(
                        text = badge.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = badge.rarity.color,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Rarity Title
                    Text(
                        text = badge.rarity.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = badge.rarity.color.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Badge Description (Gray)
                    Text(
                        text = badge.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        )
    }
}

@Composable
fun LottieIcon(resId: Int, modifier: Modifier = Modifier, loop: Boolean = true) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    LottieAnimation(
        composition = composition,
        iterations = if (loop) LottieConstants.IterateForever else 1,
        modifier = modifier
    )
}

@Composable
fun RankMediaView(rank: RankType, modifier: Modifier = Modifier) {
    if (rank == RankType.LINGUARIUM) {
        VideoLogoPlayer(modifier = modifier)
    } else {
        val lottieRes = when (rank) {
            RankType.BRONZE -> R.raw.anim_coin
            RankType.SILVER -> R.raw.anim_coin
            RankType.GOLD -> R.raw.badge
            RankType.PLATINUM -> R.raw.star_badge
            RankType.DIAMOND -> R.raw.anim_diamond
            RankType.MASTER -> R.raw.premium
            RankType.GRANDMASTER -> R.raw.fire
            else -> R.raw.badge
        }
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))
        LottieAnimation(
            composition = composition,
            iterations = 1, // Plays once and stops
            modifier = modifier
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoLogoPlayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.linguarium}")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        modifier = modifier
    )
}

@Composable
fun RankProgressPieCard(xp: Int, rank: RankType, textColor: Color, modifier: Modifier = Modifier) {
    val nextRank = when (rank) {
        RankType.BRONZE -> RankType.SILVER
        RankType.SILVER -> RankType.GOLD
        RankType.GOLD -> RankType.PLATINUM
        RankType.PLATINUM -> RankType.DIAMOND
        RankType.DIAMOND -> RankType.MASTER
        RankType.MASTER -> RankType.GRANDMASTER
        RankType.GRANDMASTER -> RankType.LINGUARIUM
        RankType.LINGUARIUM -> null
    }

    val percentage: Float
    val remainingXp: Int
    if (nextRank == null) {
        percentage = 1.0f
        remainingXp = 0
    } else {
        val range = nextRank.minXp - rank.minXp
        val currentProgress = (xp - rank.minXp).coerceAtLeast(0)
        percentage = (currentProgress.toFloat() / range.toFloat()).coerceIn(0f, 1f)
        remainingXp = (nextRank.minXp - xp).coerceAtLeast(0)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Rank İlerlemesi",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(70.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFF1F5F9),
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                    )
                    drawArc(
                        color = rank.color,
                        startAngle = -90f,
                        sweepAngle = percentage * 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = rank.color
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (nextRank != null) {
                Text(
                    text = "Sıradaki: ${nextRank.nameStr}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$remainingXp XP Kaldı",
                    fontSize = 11.sp,
                    color = Color(0xFF1CB0F6),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "En Üst Ranktasın!",
                    fontSize = 11.sp,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun LevelProgressPieCard(solvedCount: Int, totalCount: Int, currentLevel: String, textColor: Color, modifier: Modifier = Modifier) {
    val level = currentLevel.uppercase()
    val nextLevel = when (level) {
        "A1" -> "A2"
        "A2" -> "B1"
        "B1" -> "B2"
        "B2" -> "C1"
        "C1" -> "C2"
        else -> "B1"
    }

    val percentage = if (totalCount > 0) (solvedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
    val remainingQuizzes = (totalCount - solvedCount).coerceAtLeast(0)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$level İlerlemesi",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(70.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFF1F5F9),
                        radius = size.minDimension / 2,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx())
                    )
                    drawArc(
                        color = Color(0xFF1CB0F6),
                        startAngle = -90f,
                        sweepAngle = percentage * 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 6.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }
                Text(
                    text = "${(percentage * 100).toInt()}%",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFF1CB0F6)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sıradaki: $nextLevel",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$remainingQuizzes Konu Kaldı",
                fontSize = 11.sp,
                color = Color(0xFF22C55E),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WeeklyActivityChart(weeklyData: List<DayActivity>, accentColor: Color, textColor: Color) {
    val maxMinutes = weeklyData.maxOf { it.minutes }.coerceAtLeast(1)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Haftalık Aktivite",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = textColor
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { data ->
                    val heightFactor = data.minutes.toFloat() / maxMinutes.toFloat()
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${data.minutes} dk",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(heightFactor * 0.8f)
                                .width(18.dp)
                                .background(
                                    color = if (data.minutes == maxMinutes) accentColor else accentColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = data.day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

data class DayActivity(val day: String, val minutes: Int)

@Composable
fun BadgeItem(
    badge: BadgeData,
    textColor: Color,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val resId = remember(badge.assetName) {
        val cleanName = badge.assetName.substringBeforeLast(".")
        context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }

    val shortRarity = remember(badge.rarity) {
        when (badge.rarity) {
            BadgeRarity.COMMON -> "Yaygın"
            BadgeRarity.REGULAR -> "Normal"
            BadgeRarity.RARE -> "Nadir"
            BadgeRarity.EPIC -> "Epik"
            BadgeRarity.LEGENDARY -> "Efsanevi"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .border(
                    width = 2.dp,
                    color = badge.rarity.color,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
                .clipToBounds(),
            contentAlignment = Alignment.Center
        ) {
            if (resId != 0) {
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = badge.name,
                    modifier = Modifier.fillMaxSize().scale(1.1f).offset(y = (-3).dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("🏅", fontSize = 28.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = shortRarity,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = badge.rarity.color,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(68.dp)
        )
    }
}
