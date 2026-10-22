package com.example.ngilizcequizapp

import android.content.Context
import android.widget.Toast
import android.os.SystemClock
import androidx.compose.ui.draw.alpha
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavHostController

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
            val lastDate = try { sdf.parse(lastEntry) } catch (e: Exception) { null }
            val todayDate = try { sdf.parse(todayStr) } catch (e: Exception) { null }

            if (lastDate != null && todayDate != null) {
                val diffMillis = todayDate.time - lastDate.time
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                if (diffDays == 1) {
                    currentStreak += 1
                } else if (diffDays > 1) {
                    // Kullanıcı gün atlamış: Streak Kalkanı (Streak Kalkanı, Kalkan+, Mega Boost) devrede mi?
                    val shieldExpiry = navigation.MarketBoostStore.getShieldExpiryTimestamp(context)

                    if (shieldExpiry > lastDate.time) {
                        // Kalkan son girişten sonra aktifmiş
                        val daysCovered = if (shieldExpiry >= todayDate.time) {
                            diffDays
                        } else {
                            val coveredMillis = shieldExpiry - lastDate.time
                            maxOf(0, (coveredMillis / (1000L * 60L * 60L * 24L)).toInt())
                        }

                        if (daysCovered >= diffDays || shieldExpiry >= todayDate.time) {
                            // 🛡️ Kalkan devrede: 30 gün girilmese bile günler korunur ve seriye eklenir!
                            currentStreak += diffDays
                        } else if (daysCovered > 0 && (diffDays - daysCovered <= 1)) {
                            currentStreak += daysCovered
                        } else {
                            currentStreak = 1
                        }
                    } else {
                        currentStreak = 1
                    }
                } else {
                    if (currentStreak == 0) currentStreak = 1
                }
            } else {
                currentStreak = 1
            }

            prefs.edit()
                .putInt(KEY_STREAK_COUNT, currentStreak)
                .putString(KEY_LAST_ENTRY_DATE, todayStr)
                .apply()
        }

        // Seri rozetlerini otomatik tetikle
        try {
            if (currentStreak >= 3) {
                com.example.ngilizcequizapp.AchievementManager.unlockBadge(context, "regular_streak_3")
            }
            if (currentStreak >= 7) {
                com.example.ngilizcequizapp.AchievementManager.unlockBadge(context, "rare_7_day_streak")
            }
            if (currentStreak >= 30) {
                com.example.ngilizcequizapp.AchievementManager.unlockBadge(context, "epic_30_day_streak")
                com.example.ngilizcequizapp.AchievementManager.unlockBadge(context, "legendary_immortal_month")
            }
        } catch (e: Exception) {
            // Rozet hatası seriyi etkilemez
        }

        // Widget'ı anında güncelle
        updateStreakWidget(context)

        return currentStreak
    }

    fun getStreak(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val streak = prefs.getInt(KEY_STREAK_COUNT, 0)
        return if (streak == 0) 1 else streak
    }

    fun setStreak(context: Context, streak: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_STREAK_COUNT, Math.max(1, streak)).apply()
        updateStreakWidget(context)
    }

    fun updateStreakWidget(context: Context) {
        try {
            com.example.ngilizcequizapp.MewStreakWidgetProvider.updateStreakWidget(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

// Activity Duration Tracker (Yalnızca Ön Planda / Lifecycle ON_RESUME - ON_PAUSE Arası Çalışır)
object ActivityTracker {
    private const val PREFS_NAME = "activity_tracker_prefs"
    
    var isForeground: Boolean = false
        private set
    var foregroundStartTime: Long = 0L
        private set

    fun onAppResumed(context: Context) {
        if (!isForeground) {
            isForeground = true
            foregroundStartTime = SystemClock.elapsedRealtime()
        }
    }

    fun onAppPaused(context: Context) {
        if (isForeground) {
            val now = SystemClock.elapsedRealtime()
            val elapsedSec = (now - foregroundStartTime) / 1000
            if (elapsedSec > 0) {
                val currentDayKey = getCurrentDayKey()
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentVal = prefs.getInt(currentDayKey, 0)
                prefs.edit().putInt(currentDayKey, currentVal + elapsedSec.toInt()).apply()
            }
            isForeground = false
            foregroundStartTime = 0L
        }
    }

    fun recordSessionTime(context: Context) {
        if (isForeground && foregroundStartTime > 0L) {
            val now = SystemClock.elapsedRealtime()
            val elapsedSec = (now - foregroundStartTime) / 1000
            if (elapsedSec > 0) {
                val currentDayKey = getCurrentDayKey()
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val currentVal = prefs.getInt(currentDayKey, 0)
                prefs.edit().putInt(currentDayKey, currentVal + elapsedSec.toInt()).apply()
                foregroundStartTime = now
            }
        }
    }

    fun getWeeklyActivityMinutes(context: Context): List<DayActivity> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val days = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
        val keys = listOf("activity_sec_pzt", "activity_sec_sal", "activity_sec_çar", "activity_sec_per", "activity_sec_cum", "activity_sec_cmt", "activity_sec_paz")
        
        val currentDayIdx = getTodayIndex()
        val liveElapsedSec = if (isForeground && foregroundStartTime > 0L) {
            ((SystemClock.elapsedRealtime() - foregroundStartTime) / 1000).toInt()
        } else {
            0
        }

        return days.mapIndexed { index, day ->
            val savedSec = prefs.getInt(keys[index], 0)
            val totalSec = if (index == currentDayIdx) savedSec + liveElapsedSec else savedSec
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
        if (isForeground) {
            foregroundStartTime = SystemClock.elapsedRealtime()
        }
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

@kotlin.OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProgressScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Initialize/Reset mock activity values on first load
    LaunchedEffect(Unit) {
        val trackerPrefs = context.getSharedPreferences("activity_tracker_prefs", Context.MODE_PRIVATE)
        if (!trackerPrefs.getBoolean("initialized_v3", false)) {
            ActivityTracker.resetActivity(context)
            trackerPrefs.edit().putBoolean("initialized_v3", true).apply()
        }
    }

    // Save session time periodically in background on IO thread (does not trigger recomposition)
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                ActivityTracker.recordSessionTime(context)
            }
        }
    }

    // Fetch user statistics once
    val xp = remember { QuizTreeProgressStore.getXp(context) }
    val coins = remember { QuizTreeProgressStore.getCoins(context) }
    val gems = remember { QuizTreeProgressStore.getGems(context) }
    val progressMap = remember { QuizTreeProgressStore.loadAll(context) }
    val allSteps = remember { QuizDataHolder.quizSteps }
    val currentStreak = remember(context) { StreakManager.updateStreak(context) }

    val solvedCount = remember(progressMap, allSteps) {
        allSteps.indices.count { quizIndex ->
            val result = progressMap[quizIndex]
            val stars = QuizTreeProgressStore.starsFor(result)
            stars >= 1
        }
    }

    var learnedWordsCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(progressMap) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            var quizWords = 0
            for (quizIndex in allSteps.indices) {
                val result = progressMap[quizIndex]
                val stars = QuizTreeProgressStore.starsFor(result)
                if (stars >= 3) {
                    val qList = runCatching { QuizRepository.getQuizByNumber(quizIndex + 1) }.getOrNull()
                    quizWords += (qList as? QuizList)?.wordsLearned ?: 4
                }
            }

            val uniqueReadingWords = mutableSetOf<String>()
            for (storyTitle in allStories.keys) {
                if (ReadingProgressStore.isPerfect(context, storyTitle)) {
                    val savedFromThisStory = WordStorage.savedWords
                        .filter { it.sourceStory?.equals(storyTitle, ignoreCase = true) == true }
                        .map { it.word.lowercase().trim() }
                        .distinct()
                    uniqueReadingWords.addAll(savedFromThisStory.take(4))
                }
            }
            learnedWordsCount = quizWords + uniqueReadingWords.size
        }
    }

    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val rank = remember(xp, cosmeticsRevision) { UserCosmeticsStore.getUserEffectiveRank(context, getRankForXp(xp)) }
    val equippedAvatarRes = remember(cosmeticsRevision) { UserCosmeticsStore.getEquippedAvatarRes(context) }
    val equippedWingDrawable = remember(cosmeticsRevision, rank) {
        UserCosmeticsStore.getEquippedWingDrawable(context, rank)
    }
    var showCatSelector by remember { mutableStateOf(false) }
    var showWingSelector by remember { mutableStateOf(false) }
    val levelName = "A1"

    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

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

    val cardBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface
    val cardBorder = if (isKingdomTheme) BorderStroke(1.4.dp, kingdomGoldBrush) else BorderStroke(1.dp, appColors.border)

    val BrokenIceBlueText = if (isKingdomTheme) Color(0xFFFFF7D6) else appColors.text
    val VividIceBlue = if (isKingdomTheme) Color(0xFFFFD700) else Color(0xFF1CB0F6)
    val SoftBg = if (isKingdomTheme || isSpaceTheme) Color.Transparent else appColors.background

    val weeklyData = remember { ActivityTracker.getWeeklyActivityMinutes(context) }

    val unlockedBadges = remember(AchievementManager.unlockedIds) {
        AchievementManager.unlockedIds.mapNotNull { id ->
            AchievementManager.badges.find { it.id == id }
        }
    }

    var selectedBadge by remember { mutableStateOf<BadgeData?>(null) }
    var selectedTopicDialogData by remember { mutableStateOf<com.example.ngilizcequizapp.TopicMistakeSummary?>(null) }
    var showAllMistakesDialog by remember { mutableStateOf(false) }
    var showLearnedTopicsDialog by remember { mutableStateOf(false) }
    var selectedTopicAnalysisDetail by remember { mutableStateOf<com.example.ngilizcequizapp.TopicAnalysis?>(null) }

    var selectedProgressTab by remember { mutableIntStateOf(0) }

    val mistakeTopics = remember(com.example.ngilizcequizapp.QuizMistakeManager.stateRevision) {
        com.example.ngilizcequizapp.QuizMistakeManager.getMistakeTopics(context)
    }
    val learnedTopics = remember(com.example.ngilizcequizapp.QuizMistakeManager.stateRevision) {
        com.example.ngilizcequizapp.QuizMistakeManager.getLearnedTopics(context)
    }
    val nearlyPerfectTopics = remember(com.example.ngilizcequizapp.QuizMistakeManager.stateRevision) {
        com.example.ngilizcequizapp.QuizMistakeManager.getNearlyPerfectTopics(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = SoftBg,
            topBar = {
                Column {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (!navController.popBackStack()) {
                                    navController.navigate(Screen.Home.route)
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Geri",
                                    tint = if (isKingdomTheme) Color(0xFFFFD700) else BrokenIceBlueText
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = SoftBg
                        )
                    )
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = if (isKingdomTheme) Color(0xFFFFD700).copy(alpha = 0.4f) else appColors.border
                    )
                }
            },
            bottomBar = {
                AppBottomNavigationBar(navController = navController)
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
                // --- 👤 1. USER DETAILS AND LOTTIE STATS ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(x = (-10).dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selected Profile Cat Avatar & Wing Combination (Stuck to far left)
                    val isKingdomWing = equippedWingDrawable == R.drawable.kingdom_kanat
                    val wingDisplayHeight = if (isKingdomWing) 78.dp else 62.dp

                    ProfileAvatarWithWings(
                        avatarRes = equippedAvatarRes,
                        wingDrawableRes = equippedWingDrawable,
                        wingHeight = wingDisplayHeight,
                        onCatClick = { showCatSelector = true },
                        onWingClick = { showWingSelector = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // User info, Rank & XP immediately to the right
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val userFullName = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
                                com.example.ngilizcequizapp.UserProfileManager.getUserFullName(context)
                            }
                            val isDemoMode = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
                                com.example.ngilizcequizapp.UserProfileManager.isDemoMode(context)
                            }
                            val currentPlan = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
                                com.example.ngilizcequizapp.UserProfileManager.getPremiumPlan(context)
                            }

                            // 🚀 DEMO MODU: Kullanıcı adı yerine "Premium Plan Seç" butonu gelir
                            if (isDemoMode) {
                                val planDisplayName = when (currentPlan.lowercase()) {
                                    "premium_plus" -> "Premium Plus"
                                    "premium" -> "Premium"
                                    else -> "Free"
                                }
                                Surface(
                                    onClick = {
                                        val newPlan = com.example.ngilizcequizapp.UserProfileManager.cyclePremiumPlan(context)
                                        val newPlanName = when (newPlan.lowercase()) {
                                            "premium_plus" -> "Premium Plus"
                                            "premium" -> "Premium"
                                            else -> "Free"
                                        }
                                        Toast.makeText(context, "Plan Değiştirildi: $newPlanName", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF6366F1),
                                    border = BorderStroke(1.5.dp, Color(0xFFFFD700))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "⚡ Plan: $planDisplayName",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = userFullName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BrokenIceBlueText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Rank Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = rank.color.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = rank.nameStr,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = rank.color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Score Stats with responsive FlowRow for all themes (Kraliyet teması taşmalarını önler)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // XP
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFF9100).copy(alpha = 0.12f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    LottieIcon(resId = R.raw.badge, modifier = Modifier.size(20.dp), loop = false)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "$xp XP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF9100)
                                    )
                                }
                            }

                            // Coins
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LottieIcon(resId = R.raw.anim_coin, modifier = Modifier.size(20.dp), loop = true)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$coins",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFB300)
                                )
                            }

                            // Gems
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LottieIcon(resId = R.raw.anim_diamond, modifier = Modifier.size(20.dp).offset(y = (-1).dp), loop = true)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$gems",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF)
                                )
                            }
                        }
                    }
                }
            }

            // --- 🧭 TAB SEÇİCİ (GELİŞİM & ANALİZ vs AİLE LİDER TABLOSU) ---
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        Surface(
                            onClick = { selectedProgressTab = 0 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedProgressTab == 0) appColors.primary else Color.Transparent,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = null,
                                    tint = if (selectedProgressTab == 0) Color.White else appColors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Gelişim & Analiz",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedProgressTab == 0) Color.White else appColors.textMuted,
                                    softWrap = true,
                                    maxLines = 1
                                )
                            }
                        }

                        Surface(
                            onClick = { selectedProgressTab = 1 },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedProgressTab == 1) Color(0xFFE91E63) else Color.Transparent,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (selectedProgressTab == 1) Color.White else appColors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Aile Lider Tablosu",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedProgressTab == 1) Color.White else appColors.textMuted,
                                    softWrap = true,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            if (selectedProgressTab == 0) {
                // --- 🐾 1. ÜST ALAN - AKILLI MEW KOÇU ---
                item {
                    val unsolvedMistakes = remember {
                        MistakeJarStore.load(context)
                        MistakeJarStore.mistakes.count { !it.solved }
                    }
                    SmartMewCoachCard(
                        avatarRes = equippedAvatarRes,
                        unsolvedMistakesCount = unsolvedMistakes,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textColor = BrokenIceBlueText,
                        appColors = appColors,
                        isDark = isDark,
                        onPracticeClick = {
                            if (unsolvedMistakes > 0) {
                                navController.navigate("mistake_jar")
                            } else {
                                navController.navigate("word_quiz")
                            }
                        },
                        onChatClick = {
                            navController.navigate("mew_chat")
                        }
                    )
                }

                // --- ⭐ 1.1 YILDIZ SAYACI (SARI, MAVİ, KIRMIZI KÜMÜLATİF ÜST BAR) ---
                item {
                val totalQuizzesCount = 408
                val maxCapacity = 1224
                val totalYellow = remember { QuizTreeProgressStore.getTotalYellowStars(context, totalQuizzesCount) }
                val totalBlue = remember { QuizTreeProgressStore.getTotalBlueStars(context, totalQuizzesCount) }
                val totalRed = remember { QuizTreeProgressStore.getTotalRedStars(context, totalQuizzesCount) }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    shadowElevation = 2.dp,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Yıldız Koleksiyonu",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = BrokenIceBlueText
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = VividIceBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "Maks: $maxCapacity / renk",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VividIceBlue,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sarı Yıldız (İlk Çözüm)
                            StarCounterItem(
                                title = "Sarı Yıldız",
                                subtitle = "İlk Çözüm",
                                count = totalYellow,
                                max = maxCapacity,
                                starIcon = "⭐",
                                starColor = Color(0xFFFFD700),
                                bgColor = Color(0xFFFFD700).copy(alpha = 0.10f),
                                borderColor = Color(0xFFFFD700).copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            )

                            // Mavi Yıldız (1. Pekiştirme)
                            StarCounterItem(
                                title = "Mavi Yıldız",
                                subtitle = "1. Pekiştirme",
                                count = totalBlue,
                                max = maxCapacity,
                                starIcon = "🔷",
                                starColor = Color(0xFF38BDF8),
                                bgColor = Color(0xFF38BDF8).copy(alpha = 0.10f),
                                borderColor = Color(0xFF38BDF8).copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            )

                            // Kırmızı Yıldız (2. Pekiştirme)
                            StarCounterItem(
                                title = "Kırmızı Yıldız",
                                subtitle = "2. Pekiştirme",
                                count = totalRed,
                                max = maxCapacity,
                                starIcon = "🔴",
                                starColor = Color(0xFFEF4444),
                                bgColor = Color(0xFFEF4444).copy(alpha = 0.10f),
                                borderColor = Color(0xFFEF4444).copy(alpha = 0.35f),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- 👑 LİG DERECESİ & KANATLAR (Tıklayınca Ranka Göre Otomatik Kanat Değişir) ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = cardBg,
                    shadowElevation = 2.dp,
                    border = cardBorder
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MilitaryTech,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Lig Derecesi & Kanatlar",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = BrokenIceBlueText
                                    )
                                }
                                Text(
                                    text = "Tıkladığın ranka göre profil kanadın otomatik güncellenir",
                                    fontSize = 11.sp,
                                    color = appColors.textMuted
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = rank.color.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = rank.nameStr,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = rank.color,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Horizontal list of all 8 ranks with their wing previews
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(RankType.values().toList()) { r ->
                                val isSelected = r == rank
                                val wingDrawable = UserCosmeticsStore.getWingDrawableForRank(r)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) r.color.copy(alpha = 0.12f) else appColors.surfaceRaised,
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) r.color else appColors.border
                                    ),
                                    modifier = Modifier
                                        .width(105.dp)
                                        .clickable {
                                            UserCosmeticsStore.setManualRank(context, r)
                                            Toast.makeText(context, "${r.nameStr} Rankı & Kanadı Seçildi!", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Wing Preview
                                        Image(
                                            painter = painterResource(id = wingDrawable),
                                            contentDescription = r.nameStr,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = r.nameStr,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (isSelected) r.color else BrokenIceBlueText
                                        )

                                        Text(
                                            text = "${r.minXp} XP",
                                            fontSize = 10.sp,
                                            color = appColors.textMuted
                                        )

                                        if (isSelected) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = r.color,
                                                modifier = Modifier.size(6.dp)
                                            ) {}
                                        }
                                    }
                                }
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
                        color = cardBg,
                        shadowElevation = 2.dp,
                        border = cardBorder
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
                        color = cardBg,
                        shadowElevation = 2.dp,
                        border = cardBorder
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

            // --- 🧠 3.1 ORTA ALAN - UNUTMA EĞRİSİ (SPACED REPETITION) & 4 TEMEL BECERİ ---
            item {
                val unsolvedMistakesRep = remember {
                    MistakeJarStore.load(context)
                    MistakeJarStore.mistakes.count { !it.solved }
                }
                SpacedRepetitionAndSkillsCard(
                    learnedWordsCount = learnedWordsCount,
                    unsolvedMistakesCount = unsolvedMistakesRep,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textColor = BrokenIceBlueText,
                    appColors = appColors,
                    isDark = isDark,
                    onSaveFromForgettingClick = {
                        if (unsolvedMistakesRep > 0) {
                            navController.navigate("mistake_jar")
                        } else {
                            navController.navigate("word_quiz")
                        }
                    }
                )
            }

            // --- 🎯 4. SEVİYE HESAPLAMA & TAHMİN ALGORİTMASI KARTI ---
            item {
                val totalSolved = progressMap.count { it.value.redStars >= 1 }

                // Toplam quiz sayısı: 1500 (B1: 300, B2: 300, C1: 300)
                val (curLvlStr, nextLvlStr, totalInLevel, solvedInLevel) = when {
                    totalSolved < 200 -> QuadrupleData("A1", "A2", 200, totalSolved)
                    totalSolved < 300 -> QuadrupleData("A2", "B1", 100, totalSolved - 200)
                    totalSolved < 600 -> QuadrupleData("B1", "B2", 300, totalSolved - 300)
                    totalSolved < 900 -> QuadrupleData("B2", "C1", 300, totalSolved - 600)
                    totalSolved < 1200 -> QuadrupleData("C1", "C2", 300, totalSolved - 900)
                    else -> QuadrupleData("C2", "Master", 300, (totalSolved - 1200).coerceAtMost(300))
                }

                val remainingInLevel = (totalInLevel - solvedInLevel).coerceAtLeast(0)

                // Haftalık Çözülen Quiz Ortalaması = Son 7 günde çözülen quiz / 7.
                val solvedLast7Days = remember(QuizMistakeManager.stateRevision) {
                    QuizMistakeManager.getLast7DaysSolvedCount(context)
                }
                val weeklyDailyAvg = if (solvedLast7Days > 0) (solvedLast7Days.toFloat() / 7f) else 5f

                // Sonraki Seviye Kalan Gün = (Mevcut seviyede hiç çözülmemiş quiz sayısı) / (Haftalık ortalama çözülen quiz sayısı)
                val remainingDaysEst = if (remainingInLevel <= 0) 0 else {
                    Math.ceil(remainingInLevel.toDouble() / weeklyDailyAvg.toDouble()).toInt().coerceAtLeast(1)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = cardBg,
                    shadowElevation = 2.dp,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(Color(0xFF6366F1).copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Seviye Hedefi & Tahmin",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = BrokenIceBlueText
                                    )
                                    Text(
                                        text = "Önerilen günlük hedef: 5 quiz",
                                        fontSize = 11.sp,
                                        color = appColors.textMuted
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "$curLvlStr ➔ $nextLvlStr",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "İngilizceni bir sonraki seviyeye ($curLvlStr -> $nextLvlStr) taşımak için tahmini $remainingDaysEst gün kaldı.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrokenIceBlueText,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kalan: $remainingInLevel quiz",
                                fontSize = 11.sp,
                                color = appColors.textMuted,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Haftalık Günlük Hız: " + String.format(Locale.US, "%.1f", weeklyDailyAvg) + " quiz/gün",
                                fontSize = 11.sp,
                                color = VividIceBlue,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }



            // --- 🧭 5. EN ÇOK YOĞUNLAŞILMASI GEREKEN KONULAR (SADECE BAŞLIK SIRALAMASI) ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = cardBg,
                    shadowElevation = 2.dp,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "En Çok Yoğunlaşılması Gereken Konular",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = BrokenIceBlueText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quiz başarı durumuna göre çalışman önerilen konu başlıkları",
                            fontSize = 11.sp,
                            color = appColors.textMuted
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val focusTopics = remember(QuizMistakeManager.stateRevision) {
                            QuizMistakeManager.getTopFocusTopics(context).take(5)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            focusTopics.forEachIndexed { idx, topicName ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, appColors.border.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = when (idx) {
                                                0 -> Color(0xFFEF4444)
                                                1 -> Color(0xFFF59E0B)
                                                2 -> Color(0xFF6366F1)
                                                else -> appColors.primary
                                            }.copy(alpha = 0.15f),
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "${idx + 1}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = when (idx) {
                                                        0 -> Color(0xFFEF4444)
                                                        1 -> Color(0xFFF59E0B)
                                                        2 -> Color(0xFF6366F1)
                                                        else -> appColors.primary
                                                    }
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = topicName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BrokenIceBlueText,
                                            modifier = Modifier.weight(1f),
                                            softWrap = true,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 🔍 6. HATA YAPILAN KONULAR (BAŞARI <= %80) & ÖĞRENİLEN KONULAR (PREMIUM) ---
            item {
                val isPremiumUser = remember(UserProfileManager.stateRevision) {
                    UserProfileManager.isPremium(context) || UserProfileManager.isPremiumPlus(context)
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = cardBg,
                    shadowElevation = 2.dp,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hata Yapılan Konular",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = BrokenIceBlueText
                                )
                                Text(
                                    text = "Başarı oranı %80 ve altı olan konular (Hata oranına göre azalan)",
                                    fontSize = 11.sp,
                                    color = appColors.textMuted
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isPremiumUser) Color(0xFFFFD700).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isPremiumUser) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (isPremiumUser) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (isPremiumUser) "Premium Rapor" else "Kilitli",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPremiumUser) Color(0xFFFFD700) else Color(0xFF94A3B8),
                                        softWrap = true,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (!isPremiumUser) {
                            // Freemium kullanıcılar için kilitli kart
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Diamond,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Bu Rapor Premium ve Premium Plus Kullanıcılara Özeldir",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFD700),
                                            textAlign = TextAlign.Center,
                                            softWrap = true
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Quizlerde yaptığın hatalar konu bazlı filtrelenip başarı oranı %80 altı olan dersler listelenir. Tam analize erişmek için Premium'a geç!",
                                        fontSize = 11.sp,
                                        color = appColors.textMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (UserProfileManager.isDemoMode(context)) {
                                                UserProfileManager.cyclePremiumPlan(context)
                                            } else if (AppConfig.IS_MARKET_ENABLED) {
                                                navController.navigate("market")
                                            } else {
                                                Toast.makeText(context, "Premium analiz raporları çok yakında aktif olacaktır ✨", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (UserProfileManager.isDemoMode(context) || AppConfig.IS_MARKET_ENABLED) Color(0xFF6366F1) else Color(0xFF64748B)
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (UserProfileManager.isDemoMode(context)) {
                                                    "Demo: Premium'a Geç"
                                                } else if (AppConfig.IS_MARKET_ENABLED) {
                                                    "Premium'a Yükselt"
                                                } else {
                                                    "Premium Rapor (Çok Yakında ✨)"
                                                },
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color.White
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Premium & Premium Plus: Yalnızca çözülmüş ve başarı oranı <= %80 olan konular listelenir
                            if (mistakeTopics.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Tebrikler! Çözdüğün tüm konularda başarı oranın %80'in üzerinde!",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF10B981),
                                            textAlign = TextAlign.Center,
                                            softWrap = true
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // İlk 4 konuyu göster, altta Detaylara Bak ile aç
                                    mistakeTopics.take(4).forEach { item ->
                                        Surface(
                                            onClick = {
                                                selectedTopicAnalysisDetail = item
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            color = if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = item.topic,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BrokenIceBlueText,
                                                        maxLines = 2,
                                                        softWrap = true,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = "%${item.errorRate.toInt()} Hata Oranı (%${item.successRate.toInt()} Başarı)",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFEF4444),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFFEF4444).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = "İncele ➔",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFEF4444),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Detaylara Bak butonu
                                    OutlinedButton(
                                        onClick = { showAllMistakesDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, VividIceBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Detaylara Bak (${mistakeTopics.size} Konu) ➔",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = VividIceBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- ✨ 6.1 NEREDEYSE KUSURSUZ KONULAR (%90 - %99 BAŞARI) ---
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = cardBg,
                    shadowElevation = 2.dp,
                    border = cardBorder
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Neredeyse Kusursuz Konular",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = BrokenIceBlueText
                                    )
                                }
                                Text(
                                    text = "Tüm quizleri bitmiş ve %90 - %99 başarıyla öğrenilmiş konular",
                                    fontSize = 11.sp,
                                    color = appColors.textMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (nearlyPerfectTopics.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = appColors.surfaceRaised,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Henüz %90 - %99 aralığında bitirilmiş bir konu bulunmuyor. Bir konunun tüm quizlerini tamamladığında burada listelenir.",
                                    fontSize = 12.sp,
                                    color = appColors.textMuted,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                nearlyPerfectTopics.forEach { topic ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDark) appColors.surfaceRaised else Color(0xFFF0FDF4),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = topic.topic,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BrokenIceBlueText,
                                                    maxLines = 2,
                                                    softWrap = true,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Çok iyi derecede öğrenildi (%${topic.successRate.toInt()})",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF10B981),
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "${topic.totalQuizzes} Quiz",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF10B981),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Öğrenilen Konular (Tüm Liste) Sheet Açıcı Buton
                        OutlinedButton(
                            onClick = { showLearnedTopicsDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, appColors.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = appColors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Öğrenilen Konular (Tüm Seviyeler)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = appColors.primary
                                )
                            }
                        }
                    }
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
        } else {
            item {
                FamilyLeaderboardSection(
                    navController = navController,
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    textColor = BrokenIceBlueText,
                    userXp = xp,
                    userStreak = currentStreak,
                    isDark = isDark
                )
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
            containerColor = appColors.surface,
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
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = badge.rarity.color,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Badge Name (Colored by Rarity)
                    Text(
                        text = badge.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = badge.rarity.color,
                        textAlign = TextAlign.Center,
                        softWrap = true
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
                        color = appColors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        softWrap = true
                    )
                }
            }
        )
    }

    if (showCatSelector) {
        CatSelectorBottomSheet(
            onDismissRequest = { showCatSelector = false }
        )
    }

    if (showWingSelector) {
        WingSelectorBottomSheet(
            currentRank = rank,
            onDismissRequest = { showWingSelector = false }
        )
    }

    if (selectedTopicDialogData != null) {
        val summary = selectedTopicDialogData!!
        val quizFormatted = "${summary.quizIds.size}-${summary.quizIds.joinToString("-")}"
        AlertDialog(
            onDismissRequest = { selectedTopicDialogData = null },
            shape = RoundedCornerShape(24.dp),
            containerColor = appColors.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = appColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hata Odak Raporu",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = appColors.text,
                        softWrap = true
                    )
                }
            },
            text = {
                Text(
                    text = "\"${summary.topic}\" isimli konuda \"$quizFormatted\" isimli quizlerde küçük yanlışların var, buralara odaklanmaya ne dersin?",
                    fontSize = 14.sp,
                    color = appColors.textMuted,
                    lineHeight = 20.sp,
                    softWrap = true
                )
            },
            confirmButton = {
                Button(
                    onClick = { selectedTopicDialogData = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tamam, Odaklanacağım", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // 📋 "Hata Yapılan Konular" Detaylı Liste Pop-up
    if (showAllMistakesDialog) {
        AlertDialog(
            onDismissRequest = { showAllMistakesDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = appColors.surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Hata Yapılan Konular (%80 ve Altı)", fontWeight = FontWeight.Black, fontSize = 17.sp, color = appColors.text, softWrap = true)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (mistakeTopics.isEmpty()) {
                        item {
                            Text(
                                "Tebrikler! Hata oranı %20'nin üzerinde olan hiçbir konu bulunmuyor.",
                                fontSize = 13.sp,
                                color = appColors.textMuted,
                                softWrap = true
                            )
                        }
                    } else {
                        items(mistakeTopics) { topic ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDark) appColors.surfaceRaised else Color(0xFFFFF1F2),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = topic.topic,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = appColors.text,
                                        softWrap = true
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Hata Oranı: %${topic.errorRate.toInt()}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEF4444)
                                        )
                                        Text(
                                            text = "Başarı: %${topic.successRate.toInt()}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                    if (topic.erroneousQuizIds.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Hatalı Quizler: ${topic.erroneousQuizIds.joinToString(", ") { "Quiz $it" }}",
                                            fontSize = 11.sp,
                                            color = appColors.textMuted,
                                            softWrap = true
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllMistakesDialog = false }) {
                    Text("Kapat", fontWeight = FontWeight.Bold, color = appColors.primary)
                }
            }
        )
    }

    // 📜 "Öğrenilen Konular" (Kapatılabilir Dialog)
    if (showLearnedTopicsDialog) {
        AlertDialog(
            onDismissRequest = { showLearnedTopicsDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = appColors.surface,
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = appColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Öğrenilen Konular", fontWeight = FontWeight.Black, fontSize = 18.sp, color = appColors.text, softWrap = true)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "Konudaki tüm quizler bitmiş ve başarı seviyesine göre derecelendirilmiştir",
                        fontSize = 11.sp,
                        color = appColors.textMuted,
                        softWrap = true
                    )
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (learnedTopics.isEmpty()) {
                        item {
                            Text(
                                "Henüz tüm quizleri tamamlanmış bir konu bulunmuyor. Bir konudaki tüm quizleri en az %70 başarıyla tamamladığında burada listelenir.",
                                fontSize = 13.sp,
                                color = appColors.textMuted
                            )
                        }
                    } else {
                        items(learnedTopics) { topic ->
                            val (badgeColor, statusLabel) = when {
                                topic.successRate >= 99.9f -> Color(0xFF10B981) to "Kusursuz derecede bitirildi"
                                topic.successRate >= 90.0f -> Color(0xFF06B6D4) to "Çok iyi derecede öğrenildi"
                                topic.successRate >= 80.0f -> Color(0xFF6366F1) to "İyi derecede öğrenildi"
                                topic.successRate >= 70.0f -> Color(0xFFF59E0B) to "Tekrar edilmesi gerekir"
                                else -> Color(0xFF94A3B8) to "Yetersiz"
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC),
                                border = BorderStroke(1.2.dp, badgeColor.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = topic.topic,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = appColors.text,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = badgeColor.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "%${topic.successRate.toInt()}",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 12.sp,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = statusLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = badgeColor
                                        )
                                        Text(
                                            text = "${topic.totalQuizzes} Quiz",
                                            fontSize = 11.sp,
                                            color = appColors.textMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { (topic.successRate / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = badgeColor,
                                        trackColor = badgeColor.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLearnedTopicsDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Text("Kapat", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }
    }
}

data class QuadrupleData(
    val curLvl: String,
    val nextLvl: String,
    val totalInLvl: Int,
    val solvedInLvl: Int
)

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
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

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

    val context = LocalContext.current
    val isKingdomTheme = remember { UserCosmeticsStore.isKingdomThemeActive(context) }
    val cardBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface
    val cardBorder = if (isKingdomTheme) {
        BorderStroke(1.4.dp, Brush.linearGradient(listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))))
    } else BorderStroke(1.dp, appColors.border)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        border = cardBorder
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
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9),
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
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$remainingXp XP Kaldı",
                    fontSize = 11.sp,
                    color = Color(0xFF1CB0F6),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    text = "En Üst Ranktasın!",
                    fontSize = 11.sp,
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LevelProgressPieCard(solvedCount: Int, totalCount: Int, currentLevel: String, textColor: Color, modifier: Modifier = Modifier) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current

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

    val context = LocalContext.current
    val isKingdomTheme = remember { UserCosmeticsStore.isKingdomThemeActive(context) }
    val cardBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface
    val cardBorder = if (isKingdomTheme) {
        BorderStroke(1.4.dp, Brush.linearGradient(listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))))
    } else BorderStroke(1.dp, appColors.border)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        border = cardBorder
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
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9),
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
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$remainingQuizzes Konu Kaldı",
                fontSize = 11.sp,
                color = Color(0xFF22C55E),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun WeeklyActivityChart(weeklyData: List<DayActivity>, accentColor: Color, textColor: Color) {
    val context = LocalContext.current
    val isKingdomTheme = remember { UserCosmeticsStore.isKingdomThemeActive(context) }
    val appColors = LocalAppThemeColors.current
    val maxMinutes = weeklyData.maxOf { it.minutes }.coerceAtLeast(1)

    val cardBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface
    val cardBorder = if (isKingdomTheme) {
        BorderStroke(1.4.dp, Brush.linearGradient(listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))))
    } else BorderStroke(1.dp, appColors.border)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        border = cardBorder
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
            BadgeRarity.MYTHIC -> "Mistik"
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .border(
                    width = 2.dp,
                    color = badge.rarity.color,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(3.dp)
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
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = badge.rarity.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = shortRarity,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = badge.rarity.color,
            maxLines = 1,
            softWrap = true,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(68.dp)
        )
    }
}

@Composable
fun StarCounterItem(
    title: String,
    subtitle: String,
    count: Int,
    max: Int,
    starIcon: String,
    starColor: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (max > 0) (count.toFloat() / max.toFloat()).coerceIn(0f, 1f) else 0f
    val appColors = LocalAppThemeColors.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = starColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "$count",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = starColor
                )
                Text(
                    text = "/$max",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = starColor.copy(alpha = 0.75f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = appColors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = starColor,
                trackColor = starColor.copy(alpha = 0.18f)
            )
        }
    }
}

// -------------------------------------------------------------
// 👨‍👩‍👧‍👦 AİLE LİDER TABLOSU (FAMILY LEADERBOARD)
// -------------------------------------------------------------

data class FamilyMemberItem(
    val id: String,
    val name: String,
    val role: String,
    val avatarRes: Int,
    val rank: RankType,
    val xp: Int,
    val streak: Int,
    val isCurrentUser: Boolean = false,
    val isEmptySlot: Boolean = false
)

@Composable
fun FamilyLeaderboardSection(
    navController: NavHostController,
    cardBg: Color,
    cardBorder: BorderStroke,
    textColor: Color,
    userXp: Int,
    userStreak: Int,
    isDark: Boolean
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isFamilyPlan = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
        com.example.ngilizcequizapp.UserProfileManager.isFamilyPlan(context) ||
        com.example.ngilizcequizapp.UserProfileManager.isDemoMode(context)
    }

    val userFullName = remember(com.example.ngilizcequizapp.UserProfileManager.stateRevision) {
        com.example.ngilizcequizapp.UserProfileManager.getUserFullName(context)
    }

    val userAvatarRes = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.getEquippedAvatarRes(context)
    }

    val userRank = remember(userXp) { getRankForXp(userXp) }

    var showFamilyCodeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isFamilyPlan) {
            // 🌟 AİLE PLANI AKTİF KULLANICILAR İÇİN CANLI LİDER TABLOSU
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.5.dp, Color(0xFFE91E63).copy(alpha = 0.6f)),
                shadowElevation = 3.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Groups,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Mew Aile Ligi",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textColor
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "5 Cihaza Kadar Aile Üyeleri Sıralaması",
                                fontSize = 11.sp,
                                color = appColors.textMuted
                            )
                        }

                        Surface(
                            onClick = { showFamilyCodeDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFE91E63))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Aile Kodu",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE91E63)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Haftalık Aile Ortak Görevi
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFFDF2F8),
                        border = BorderStroke(1.dp, Color(0xFFF472B6).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.TrackChanges,
                                        contentDescription = null,
                                        tint = Color(0xFFBE185D),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Haftalık Ortak Aile Hedefi",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBE185D)
                                    )
                                }
                                Text(
                                    text = "${userXp + 2480} / 5.000 XP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFE91E63)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            val targetProgress = ((userXp + 2480).toFloat() / 5000f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { targetProgress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFE91E63),
                                trackColor = Color(0xFFE91E63).copy(alpha = 0.2f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Hedefe ulaşınca tüm aile üyelerine 500 Coin + 50 Elmas ödülü tanımlanır!",
                                fontSize = 10.sp,
                                color = appColors.textMuted
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Canlı Sıralama",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
                    )
                    Spacer(Modifier.height(10.dp))

                    // 5 Aile Üyesi Sıralaması
                    val familyMembers = listOf(
                        FamilyMemberItem("1", userFullName + " (Sen)", "Aile Lideri", userAvatarRes, userRank, userXp, userStreak, isCurrentUser = true),
                        FamilyMemberItem("2", "Mew Anne", "2. Cihaz", R.drawable.cool_mew, RankType.GOLD, 1420, 7),
                        FamilyMemberItem("3", "Mini Mew (Kardeş)", "3. Cihaz", R.drawable.wizard_mew, RankType.SILVER, 620, 4),
                        FamilyMemberItem("4", "Can (Mew Kaşif)", "4. Cihaz", R.drawable.pirate_mew, RankType.BRONZE, 440, 2),
                        FamilyMemberItem("5", "Yeni Cihaz Bekleniyor", "5. Slot Boş", R.drawable.readingcat, RankType.BRONZE, 0, 0, isEmptySlot = true)
                    ).sortedByDescending { it.xp }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        familyMembers.forEachIndexed { index, member ->
                            FamilyMemberCard(
                                rankIndex = index + 1,
                                member = member,
                                isDark = isDark,
                                appColors = appColors,
                                onInviteClick = { showFamilyCodeDialog = true }
                            )
                        }
                    }
                }
            }
        } else {
            // 🔒 AİLE PLANI OLMAYAN KULLANICILAR İÇİN LÜKS TANITIM & ÖNİZLEME
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF3B82F6)))),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(Brush.linearGradient(listOf(Color(0xFFE91E63), Color(0xFF9C27B0))), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mew Aile Lider Tablosu",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "Ailenle Birlikte Öğren & Yarış!",
                                fontSize = 12.sp,
                                color = Color(0xFFE91E63),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    Text(
                        text = "Tek abonelikle 5 farklı cihazda sınırsız İngilizce öğrenimi, sınırsız enerji ve ailenize özel canlı liderlik tablosu açılır. Herkes kendi cihazından çözer, ortak haftalık hedeflerde birlikte yarışır!",
                        fontSize = 12.5.sp,
                        color = appColors.textMuted,
                        lineHeight = 18.sp
                    )

                    Spacer(Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FamilyFeatureRow(Icons.Default.Devices, "5 Farklı Cihaz / Aile Profili", appColors)
                        FamilyFeatureRow(Icons.Default.Bolt, "Sınırsız Enerji (Can bekleme yok)", appColors)
                        FamilyFeatureRow(Icons.Default.Leaderboard, "Özel Canlı Aile İçi Sıralama & Haftalık Hedefler", appColors)
                        FamilyFeatureRow(Icons.Default.AutoAwesome, "Abonelik Boyunca Sınırsız / Kalıcı 2X XP & Coin Çarpanı", appColors)
                        FamilyFeatureRow(Icons.Default.Diamond, "Hoş Geldin Hediyesi: 300 Elmas + 2.000 Coin", appColors)
                    }

                    Spacer(Modifier.height(18.dp))

                    if (AppConfig.IS_MARKET_ENABLED) {
                        Button(
                            onClick = { navController.navigate("premium_screen") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                        ) {
                            Text(
                                text = "Aile Planına Geç (₺399,00 / ay)",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Aile Planı aboneliği çok yakında aktif olacaktır ✨", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                        ) {
                            Text(
                                text = "Aile Planı (Çok Yakında ✨)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFamilyCodeDialog) {
        AlertDialog(
            onDismissRequest = { showFamilyCodeDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Aile Kodu", "MEW-FAM-8492")
                        clipboard?.setPrimaryClip(clip)
                        Toast.makeText(context, "Aile kodu kopyalandı: MEW-FAM-8492", Toast.LENGTH_SHORT).show()
                        showFamilyCodeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                ) {
                    Text("Kodu Kopyala", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFamilyCodeDialog = false }) {
                    Text("Kapat")
                }
            },
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Aile Cihaz Eşleme Kodu", fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Diğer aile üyelerinizin cihazlarında bu kodu girerek aynı aile paketine ve liderlik tablosuna bağlanmalarını sağlayabilirsiniz (En fazla 5 cihaz).",
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDark) appColors.surfaceRaised else Color(0xFFFDF2F8),
                        border = BorderStroke(1.dp, Color(0xFFE91E63)),
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "MEW-FAM-8492",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE91E63),
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FamilyMemberCard(
    rankIndex: Int,
    member: FamilyMemberItem,
    isDark: Boolean,
    appColors: AppThemeColors,
    onInviteClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (member.isCurrentUser) {
            Color(0xFFE91E63).copy(alpha = 0.12f)
        } else if (isDark) appColors.surfaceRaised else Color(0xFFF8FAFC),
        border = BorderStroke(
            1.dp,
            if (member.isCurrentUser) Color(0xFFE91E63) else appColors.border.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (rankIndex <= 3) {
                    val medalColor = when (rankIndex) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        else -> Color(0xFFCD7F32)
                    }
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Rank $rankIndex",
                        tint = medalColor,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "$rankIndex.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = appColors.textMuted
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = member.avatarRes),
                    contentDescription = member.name,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    fontSize = 13.sp,
                    fontWeight = if (member.isCurrentUser) FontWeight.Black else FontWeight.Bold,
                    color = if (member.isCurrentUser) Color(0xFFE91E63) else appColors.text,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = member.role,
                    fontSize = 10.sp,
                    color = appColors.textMuted,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (member.isEmptySlot) {
                Surface(
                    onClick = onInviteClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE91E63).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "+ Eşle",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${member.xp} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF9100)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${member.streak} gün",
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
fun FamilyFeatureRow(icon: ImageVector, text: String, appColors: AppThemeColors) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFE91E63),
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = appColors.text,
            softWrap = true
        )
    }
}

/**
 * 1. ÜST ALAN - AKILLI MEW KOÇU:
 * Seçili kedi avatarı ve yanında sevimli konuşma balonu.
 * Hata şişesindeki kelime sayısını okur, tıklandığında hata pratiği başlatır.
 */
@Composable
fun SmartMewCoachCard(
    avatarRes: Int,
    unsolvedMistakesCount: Int,
    cardBg: Color,
    cardBorder: BorderStroke,
    textColor: Color,
    appColors: AppThemeColors,
    isDark: Boolean,
    onPracticeClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                            .background(Color(0xFF6366F1).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pets,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Akıllı Kedi Mew",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = textColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (unsolvedMistakesCount > 0) Color(0xFFEF4444).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (unsolvedMistakesCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (unsolvedMistakesCount > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (unsolvedMistakesCount > 0) "$unsolvedMistakesCount Hata Bekliyor" else "Şişe Temiz",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unsolvedMistakesCount > 0) Color(0xFFEF4444) else Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Avatar + Speech Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cat Avatar in cute circle container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFEDE9FE))
                        .border(2.dp, Color(0xFF6366F1).copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = avatarRes),
                        contentDescription = "Mew Coach Avatar",
                        modifier = Modifier.size(54.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Speech Bubble
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                    color = if (isDark) appColors.surfaceRaised else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (unsolvedMistakesCount > 0) {
                            "Miyav! Hata şişende $unsolvedMistakesCount soru birikmiş. Gel onları unutmadan pratikle kalıcı bilgiye dönüştürelim!"
                        } else {
                            "Harikasın! Hata şişen tertemiz. Yeni quizlerle kelime hazineni genişletmeye devam et!"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textColor,
                        lineHeight = 18.sp,
                        softWrap = true,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CTA Buttons Row (Practice & Chat)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPracticeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (unsolvedMistakesCount > 0) Color(0xFF6366F1) else Color(0xFF10B981)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (unsolvedMistakesCount > 0) Icons.Default.TrackChanges else Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (unsolvedMistakesCount > 0) "Pratik Yap" else "Pratiğe Başla",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                OutlinedButton(
                    onClick = onChatClick,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF8B5CF6)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF8B5CF6).copy(alpha = 0.10f)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = Color(0xFF8B5CF6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sohbet Et",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B5CF6)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. ORTA ALAN - UNUTMA EĞRİSİ (SPACED REPETITION) & 4 TEMEL DİL BECERİSİ
 */
@Composable
fun SpacedRepetitionAndSkillsCard(
    learnedWordsCount: Int,
    unsolvedMistakesCount: Int,
    cardBg: Color,
    cardBorder: BorderStroke,
    textColor: Color,
    appColors: AppThemeColors,
    isDark: Boolean,
    onSaveFromForgettingClick: () -> Unit
) {
    val totalWords = maxOf(learnedWordsCount, 10)
    val freshCount = (totalWords * 0.70).toInt().coerceAtLeast(1)
    val reviewCount = (totalWords * 0.20).toInt().coerceAtLeast(1)
    val criticalCount = maxOf((totalWords * 0.10).toInt(), unsolvedMistakesCount)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = 2.dp,
        border = cardBorder
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Section 1: Unutma Eğrisi (Spaced Repetition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Kelime Hafıza Durumu",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = textColor,
                            softWrap = true
                        )
                        Text(
                            text = "Aralıklı Tekrar (Spaced Repetition) Algoritması",
                            fontSize = 10.5.sp,
                            color = appColors.textMuted,
                            softWrap = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Level Counter Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 🟢 Zihinde Taze
                SpacedTierItem(
                    title = "Taze",
                    count = freshCount,
                    subtitle = "Son 3 gün",
                    badgeColor = Color(0xFF10B981),
                    bgColor = Color(0xFF10B981).copy(alpha = 0.10f),
                    borderColor = Color(0xFF10B981).copy(alpha = 0.35f),
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )

                // 🟡 Tekrar Vakti
                SpacedTierItem(
                    title = "Tekrar",
                    count = reviewCount,
                    subtitle = "5-7 gün",
                    badgeColor = Color(0xFFF59E0B),
                    bgColor = Color(0xFFF59E0B).copy(alpha = 0.10f),
                    borderColor = Color(0xFFF59E0B).copy(alpha = 0.35f),
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )

                // 🔴 Kritik
                SpacedTierItem(
                    title = "Kritik",
                    count = criticalCount,
                    subtitle = "Unutma riski",
                    badgeColor = Color(0xFFEF4444),
                    bgColor = Color(0xFFEF4444).copy(alpha = 0.10f),
                    borderColor = Color(0xFFEF4444).copy(alpha = 0.35f),
                    textColor = textColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CTA: Unutulmadan Kurtar
            OutlinedButton(
                onClick = onSaveFromForgettingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFF59E0B).copy(alpha = 0.08f)
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Unutulmadan Kurtar (5 Soru Pratik)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706),
                        softWrap = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(thickness = 0.6.dp, color = appColors.border.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: 4 Temel Dil Becerisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "4 Temel Dil Becerisi",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = textColor,
                            softWrap = true
                        )
                        Text(
                            text = "Quiz ve pratik türlerine göre yetkinlik dağılımı",
                            fontSize = 10.5.sp,
                            color = appColors.textMuted,
                            softWrap = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SkillProgressRow(Icons.Default.Headphones, "Listening (Dinleme)", 0.88f, "%88", Color(0xFF8B5CF6), isDark, textColor, appColors)
                SkillProgressRow(Icons.Default.Visibility, "Visual (Görsel Algı)", 0.94f, "%94", Color(0xFF10B981), isDark, textColor, appColors)
                SkillProgressRow(Icons.Default.MenuBook, "Vocabulary (Kelime)", 0.85f, "%85", Color(0xFFF59E0B), isDark, textColor, appColors)
                SkillProgressRow(Icons.Default.AutoStories, "Reading (Okuma/Anlama)", 0.78f, "%78", Color(0xFF3B82F6), isDark, textColor, appColors)
            }
        }
    }
}

@Composable
private fun SpacedTierItem(
    title: String,
    count: Int,
    subtitle: String,
    badgeColor: Color,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = badgeColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = badgeColor,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.65f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = true,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SkillProgressRow(
    icon: ImageVector,
    title: String,
    progress: Float,
    percentageText: String,
    accentColor: Color,
    isDark: Boolean,
    textColor: Color,
    appColors: AppThemeColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = percentageText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = accentColor,
            trackColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
        )
    }
}


