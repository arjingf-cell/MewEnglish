package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import java.text.SimpleDateFormat
import java.util.*

// -------------------------------------------------------------
// 1. DATA MODELS & REWARD LOGIC
// -------------------------------------------------------------

enum class MissionPeriod(val title: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"),
    MONTHLY("Aylık")
}

data class MissionReward(
    val coins: Int = 0,
    val xp: Int = 0,
    val gems: Int = 0,
    val badgeRes: Int? = null,
    val badgeName: String? = null,
    val badgeId: String? = null
)

data class MissionItem(
    val id: String,
    val title: String,
    val description: String,
    val period: MissionPeriod,
    val currentProgress: Int,
    val maxProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean,
    val reward: MissionReward,
    val iconEmoji: String = "🎯",
    val targetRoute: String = Screen.Home.route
)

object MissionConstants {
    const val SINGLE_QUIZ_XP = 25
    const val SINGLE_QUIZ_COINS = 20

    // Günlük tekil görev ödülü: 75 Coin + 25 XP, 3 günlük görev tamamlama bonusu: 3 Elmas
    val DAILY_REWARD = MissionReward(
        coins = 75,
        xp = 25,
        gems = 0,
        badgeRes = R.drawable.daily_badge,
        badgeName = "Günlük Zafer Nişanı",
        badgeId = "regular_daily_champion"
    )

    // Haftalık görev ödülü: 400 Coin + 150 XP + 10 Elmas
    val WEEKLY_REWARD = MissionReward(
        gems = 10,
        xp = 150,
        coins = 400,
        badgeRes = R.drawable.week_badge,
        badgeName = "Zamanında!",
        badgeId = "rare_weekly_champion"
    )

    // Aylık görev ödülü: 1.500 Coin + 500 XP + 25 Elmas
    val MONTHLY_REWARD = MissionReward(
        xp = 500,
        coins = 1500,
        gems = 25,
        badgeRes = R.drawable.month_badge,
        badgeName = "Ayın elemanı",
        badgeId = "legendary_monthly_ruler"
    )

    const val DAILY_BONUS_GEMS = 3
}

// -------------------------------------------------------------
// 2. MISSION MANAGER & STORAGE
// -------------------------------------------------------------

object MissionManager {
    private const val PREFS_NAME = "mew_mission_system"
    var stateRevision by mutableIntStateOf(0)
        private set

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun thisWeekKey(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"
    }

    private fun thisMonthKey(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    }

    fun syncAndResetIfNeeded(context: Context) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()

        val savedDay = prefs.getString("key_saved_day", "")
        val currentDay = todayKey()
        if (savedDay != currentDay) {
            editor.putString("key_saved_day", currentDay)
            editor.putInt("daily_quizzes", 0)
            editor.putInt("daily_reading", 0)
            editor.putInt("daily_words", 0)
            editor.putBoolean("claimed_daily_quizzes", false)
            editor.putBoolean("claimed_daily_reading", false)
            editor.putBoolean("claimed_daily_words", false)
            editor.putBoolean("claimed_daily_bonus", false)
        }

        val savedWeek = prefs.getString("key_saved_week", "")
        val currentWeek = thisWeekKey()
        if (savedWeek != currentWeek) {
            editor.putString("key_saved_week", currentWeek)
            editor.putInt("weekly_daily_done", 0)
            editor.putInt("weekly_quizzes", 0)
            editor.putInt("weekly_reading", 0)
            editor.putBoolean("claimed_weekly_daily_done", false)
            editor.putBoolean("claimed_weekly_quizzes", false)
            editor.putBoolean("claimed_weekly_reading", false)
        }

        val savedMonth = prefs.getString("key_saved_month", "")
        val currentMonth = thisMonthKey()
        if (savedMonth != currentMonth) {
            editor.putString("key_saved_month", currentMonth)
            editor.putInt("monthly_active_days", 0)
            editor.putInt("monthly_quizzes", 0)
            editor.putInt("monthly_reading", 0)
            editor.putBoolean("claimed_monthly_active_days", false)
            editor.putBoolean("claimed_monthly_quizzes", false)
            editor.putBoolean("claimed_monthly_reading", false)
        }

        editor.apply()
    }

    // Progress Handlers
    fun onQuizCompleted(context: Context) {
        syncAndResetIfNeeded(context)
        val prefs = getPrefs(context)
        prefs.edit()
            .putInt("daily_quizzes", prefs.getInt("daily_quizzes", 0) + 1)
            .putInt("weekly_quizzes", prefs.getInt("weekly_quizzes", 0) + 1)
            .putInt("monthly_quizzes", prefs.getInt("monthly_quizzes", 0) + 1)
            .apply()
        stateRevision++
    }

    fun onReadingCompleted(context: Context) {
        syncAndResetIfNeeded(context)
        val prefs = getPrefs(context)
        prefs.edit()
            .putInt("daily_reading", prefs.getInt("daily_reading", 0) + 1)
            .putInt("weekly_reading", prefs.getInt("weekly_reading", 0) + 1)
            .putInt("monthly_reading", prefs.getInt("monthly_reading", 0) + 1)
            .apply()
        stateRevision++
    }

    fun onWordSaved(context: Context) {
        syncAndResetIfNeeded(context)
        val prefs = getPrefs(context)
        prefs.edit()
            .putInt("daily_words", prefs.getInt("daily_words", 0) + 1)
            .apply()
        stateRevision++
    }

    fun getAllMissions(context: Context, period: MissionPeriod): List<MissionItem> {
        syncAndResetIfNeeded(context)
        val prefs = getPrefs(context)

        return when (period) {
            MissionPeriod.DAILY -> {
                val qProg = prefs.getInt("daily_quizzes", 0)
                val rProg = prefs.getInt("daily_reading", 0)
                val wProg = prefs.getInt("daily_words", 0)

                listOf(
                    MissionItem(
                        id = "daily_quizzes",
                        title = "3 Quiz Tamamla",
                        description = "Quiz ağacından 3 farklı quizi başarıyla bitir.",
                        period = MissionPeriod.DAILY,
                        currentProgress = qProg.coerceAtMost(3),
                        maxProgress = 3,
                        isCompleted = qProg >= 3,
                        isClaimed = prefs.getBoolean("claimed_daily_quizzes", false),
                        reward = MissionConstants.DAILY_REWARD,
                        iconEmoji = "🏹",
                        targetRoute = Screen.Home.route
                    ),
                    MissionItem(
                        id = "daily_reading",
                        title = "1 Okuma Parçası Oku",
                        description = "Kraliyet Kütüphanesi'nden bir okuma parçasını tamamla.",
                        period = MissionPeriod.DAILY,
                        currentProgress = rProg.coerceAtMost(1),
                        maxProgress = 1,
                        isCompleted = rProg >= 1,
                        isClaimed = prefs.getBoolean("claimed_daily_reading", false),
                        reward = MissionConstants.DAILY_REWARD,
                        iconEmoji = "📜",
                        targetRoute = "reading_screen"
                    ),
                    MissionItem(
                        id = "daily_words",
                        title = "Quizlerden 5 Kelime Kaydet",
                        description = "Quizler sırasında karşılaştığın kelimeleri sözlüğüne ekle.",
                        period = MissionPeriod.DAILY,
                        currentProgress = wProg.coerceAtMost(5),
                        maxProgress = 5,
                        isCompleted = wProg >= 5,
                        isClaimed = prefs.getBoolean("claimed_daily_words", false),
                        reward = MissionConstants.DAILY_REWARD,
                        iconEmoji = "📖",
                        targetRoute = "words_screen"
                    )
                )
            }
            MissionPeriod.WEEKLY -> {
                val dDone = prefs.getInt("weekly_daily_done", 0)
                val wQuizzes = prefs.getInt("weekly_quizzes", 0)
                val wReading = prefs.getInt("weekly_reading", 0)

                listOf(
                    MissionItem(
                        id = "weekly_daily_done",
                        title = "10 Günlük Görev Tamamla",
                        description = "Bu hafta boyunca toplam en az 10 günlük görevi bitir.",
                        period = MissionPeriod.WEEKLY,
                        currentProgress = dDone.coerceAtMost(10),
                        maxProgress = 10,
                        isCompleted = dDone >= 10,
                        isClaimed = prefs.getBoolean("claimed_weekly_daily_done", false),
                        reward = MissionConstants.WEEKLY_REWARD,
                        iconEmoji = "🛡️"
                    ),
                    MissionItem(
                        id = "weekly_quizzes",
                        title = "25 Quiz Tamamla",
                        description = "Haftalık serüvende 25 quiz tamamla, şövalye unvanını hak et.",
                        period = MissionPeriod.WEEKLY,
                        currentProgress = wQuizzes.coerceAtMost(25),
                        maxProgress = 25,
                        isCompleted = wQuizzes >= 25,
                        isClaimed = prefs.getBoolean("claimed_weekly_quizzes", false),
                        reward = MissionConstants.WEEKLY_REWARD,
                        iconEmoji = "⚔️"
                    ),
                    MissionItem(
                        id = "weekly_reading",
                        title = "7 Okuma Parçası Bitir",
                        description = "Hafta boyunca 7 kraliyet hikayesini derinlemesine oku.",
                        period = MissionPeriod.WEEKLY,
                        currentProgress = wReading.coerceAtMost(7),
                        maxProgress = 7,
                        isCompleted = wReading >= 7,
                        isClaimed = prefs.getBoolean("claimed_weekly_reading", false),
                        reward = MissionConstants.WEEKLY_REWARD,
                        iconEmoji = "📚",
                        targetRoute = "reading_screen"
                    )
                )
            }
            MissionPeriod.MONTHLY -> {
                val mDays = prefs.getInt("monthly_active_days", 0)
                val mQuizzes = prefs.getInt("monthly_quizzes", 0)
                val mReading = prefs.getInt("monthly_reading", 0)

                listOf(
                    MissionItem(
                        id = "monthly_active_days",
                        title = "15 Günlük Aktif Seri",
                        description = "Bu ay en az 15 farklı gün uygulamada ders çalış.",
                        period = MissionPeriod.MONTHLY,
                        currentProgress = mDays.coerceAtMost(15),
                        maxProgress = 15,
                        isCompleted = mDays >= 15,
                        isClaimed = prefs.getBoolean("claimed_monthly_active_days", false),
                        reward = MissionConstants.MONTHLY_REWARD,
                        iconEmoji = "👑"
                    ),
                    MissionItem(
                        id = "monthly_quizzes",
                        title = "75 Quiz Başarıyla Tamamla",
                        description = "Aylık büyük seferde 75 quizi tamamlayarak krallığı taçlandır.",
                        period = MissionPeriod.MONTHLY,
                        currentProgress = mQuizzes.coerceAtMost(75),
                        maxProgress = 75,
                        isCompleted = mQuizzes >= 75,
                        isClaimed = prefs.getBoolean("claimed_monthly_quizzes", false),
                        reward = MissionConstants.MONTHLY_REWARD,
                        iconEmoji = "🏆"
                    ),
                    MissionItem(
                        id = "monthly_reading",
                        title = "20 Okuma Parçası Tamamla",
                        description = "Ay boyunca 20 farklı kraliyet metnini tamamla.",
                        period = MissionPeriod.MONTHLY,
                        currentProgress = mReading.coerceAtMost(20),
                        maxProgress = 20,
                        isCompleted = mReading >= 20,
                        isClaimed = prefs.getBoolean("claimed_monthly_reading", false),
                        reward = MissionConstants.MONTHLY_REWARD,
                        iconEmoji = "🏰",
                        targetRoute = "reading_screen"
                    )
                )
            }
        }
    }

    fun getActiveDailyPreview(context: Context): MissionItem {
        val daily = getAllMissions(context, MissionPeriod.DAILY)
        return daily.firstOrNull { !it.isCompleted }
            ?: daily.firstOrNull { !it.isClaimed }
            ?: daily.first()
    }

    fun isDailyBonusAvailable(context: Context): Boolean {
        val daily = getAllMissions(context, MissionPeriod.DAILY)
        return daily.all { it.isCompleted }
    }

    fun isDailyBonusClaimed(context: Context): Boolean {
        return getPrefs(context).getBoolean("claimed_daily_bonus", false)
    }

    fun claimDailyBonus(context: Context): Boolean {
        if (!isDailyBonusAvailable(context) || isDailyBonusClaimed(context)) return false
        QuizTreeProgressStore.addGems(context, MissionConstants.DAILY_BONUS_GEMS)
        AchievementManager.unlockBadge(context, "regular_daily_champion")
        getPrefs(context).edit().putBoolean("claimed_daily_bonus", true).apply()
        stateRevision++
        return true
    }

    fun claimMissionReward(context: Context, mission: MissionItem): Boolean {
        if (!mission.isCompleted || mission.isClaimed) return false

        val prefs = getPrefs(context)
        val editor = prefs.edit()
        editor.putBoolean("claimed_${mission.id}", true)

        if (mission.period == MissionPeriod.DAILY) {
            val prevDone = prefs.getInt("weekly_daily_done", 0)
            editor.putInt("weekly_daily_done", prevDone + 1)
        }

        editor.apply()

        // Grant rewards
        if (mission.reward.coins > 0) QuizTreeProgressStore.addCoins(context, mission.reward.coins)
        if (mission.reward.xp > 0) QuizTreeProgressStore.addXp(context, mission.reward.xp)
        if (mission.reward.gems > 0) QuizTreeProgressStore.addGems(context, mission.reward.gems)
        if (!mission.reward.badgeId.isNullOrEmpty()) {
            AchievementManager.unlockBadge(context, mission.reward.badgeId)
        }

        stateRevision++
        return true
    }
}

// -------------------------------------------------------------
// 3. UI: HOME SCREEN MISSION PREVIEW CARD
// -------------------------------------------------------------

@Composable
fun MissionPreviewCard(navController: NavHostController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val missionRevision = MissionManager.stateRevision

    val isKingdomTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isKingdomThemeActive(context) }
    val isSpaceTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isSpaceThemeActive(context) }

    val activeMission = remember(missionRevision) {
        MissionManager.getActiveDailyPreview(context)
    }

    val cardBorder = when {
        isKingdomTheme -> BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))
            )
        )
        isSpaceTheme -> BorderStroke(
            1.4.dp,
            Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8)))
        )
        else -> BorderStroke(1.dp, appColors.border.copy(alpha = 0.6f))
    }

    val cardBg = when {
        isKingdomTheme -> Color(0xFF070F26).copy(alpha = 0.92f)
        isSpaceTheme -> Color(0xFF0C132C).copy(alpha = 0.90f)
        else -> appColors.surface
    }

    val progressAnimated by animateFloatAsState(
        targetValue = if (activeMission.maxProgress > 0) activeMission.currentProgress.toFloat() / activeMission.maxProgress else 0f,
        label = "missionProgress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Günlük görevlerin seni bekliyor",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text
            )
            Text(
                text = "Tümünü Gör ›",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.8f) else appColors.primary,
                modifier = Modifier.clickable { navController.navigate("missions_screen") }
            )
        }

        Spacer(Modifier.height(8.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { navController.navigate("missions_screen") },
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            border = cardBorder,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            if (isKingdomTheme) Color(0xFF1B3A6B).copy(alpha = 0.7f) else appColors.primarySoft,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MissionIconView(iconKey = activeMission.id, isKingdom = isKingdomTheme, modifier = Modifier.size(24.dp))
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeMission.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isKingdomTheme) Color.White else appColors.text,
                            maxLines = 1,
                            softWrap = true,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${activeMission.currentProgress} / ${activeMission.maxProgress}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressAnimated },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary,
                        trackColor = if (isKingdomTheme) Color(0xFF1B3A6B) else appColors.primarySoft
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RewardChip(icon = "🪙", label = "+${activeMission.reward.coins}", isKingdom = isKingdomTheme)
                        RewardChip(icon = "⚡", label = "+${activeMission.reward.xp} XP", isKingdom = isKingdomTheme)
                        if (activeMission.isCompleted && !activeMission.isClaimed) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(3.dp))
                                Text(
                                    text = "Hazır! Almak için tıkla",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Detay",
                    tint = if (isKingdomTheme) Color(0xFFFFD700) else appColors.textMuted
                )
            }
        }
    }
}

enum class RewardType { COIN, XP, GEM }

@Composable
fun MissionIconView(iconKey: String, isKingdom: Boolean, modifier: Modifier = Modifier) {
    val appColors = LocalAppThemeColors.current
    val iconVector = when {
        iconKey.contains("quizzes") || iconKey.contains("quiz") || iconKey == "🏹" -> Icons.Default.TrackChanges
        iconKey.contains("reading") || iconKey == "📜" -> Icons.Default.AutoStories
        iconKey.contains("words") || iconKey == "📖" -> Icons.Default.MenuBook
        iconKey.contains("shield") || iconKey.contains("daily_done") || iconKey == "🛡️" -> Icons.Default.Shield
        iconKey.contains("battle") || iconKey == "⚔️" -> Icons.Default.SportsScore
        iconKey.contains("library") || iconKey == "📚" -> Icons.Default.LocalLibrary
        iconKey.contains("crown") || iconKey.contains("active_days") || iconKey == "👑" -> Icons.Default.MilitaryTech
        iconKey.contains("trophy") || iconKey == "🏆" -> Icons.Default.EmojiEvents
        iconKey.contains("castle") || iconKey == "🏰" -> Icons.Default.AccountBalance
        else -> Icons.Default.Stars
    }
    Icon(
        imageVector = iconVector,
        contentDescription = null,
        tint = if (isKingdom) Color(0xFFFFD700) else appColors.primary,
        modifier = modifier
    )
}

@Composable
private fun RewardChip(type: RewardType, label: String, isKingdom: Boolean) {
    val iconVector = when (type) {
        RewardType.COIN -> Icons.Default.MonetizationOn
        RewardType.XP -> Icons.Default.Bolt
        RewardType.GEM -> Icons.Default.Diamond
    }
    val iconColor = when (type) {
        RewardType.COIN -> Color(0xFFFFB300)
        RewardType.XP -> Color(0xFFFF9800)
        RewardType.GEM -> Color(0xFF00E5FF)
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isKingdom) Color(0xFF122244) else Color(0xFFF1F5F9)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isKingdom) Color(0xFFFFF7D6) else Color(0xFF334155)
            )
        }
    }
}

@Composable
private fun RewardChip(icon: String, label: String, isKingdom: Boolean) {
    val type = when (icon) {
        "🪙", "coin" -> RewardType.COIN
        "⚡", "xp" -> RewardType.XP
        "💎", "gem" -> RewardType.GEM
        else -> RewardType.COIN
    }
    RewardChip(type = type, label = label, isKingdom = isKingdom)
}

// -------------------------------------------------------------
// 4. UI: MISSIONS SCREEN (GENERIC & THEME COMPATIBLE)
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val cosmeticsRevision = UserCosmeticsStore.stateRevision
    val missionRevision = MissionManager.stateRevision

    val isKingdomTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isKingdomThemeActive(context) }
    val isSpaceTheme = remember(cosmeticsRevision) { UserCosmeticsStore.isSpaceThemeActive(context) }

    var selectedTab by remember { mutableStateOf(MissionPeriod.DAILY) }

    val missions = remember(selectedTab, missionRevision) {
        MissionManager.getAllMissions(context, selectedTab)
    }

    val isBonusAvailable = remember(missionRevision) { MissionManager.isDailyBonusAvailable(context) }
    val isBonusClaimed = remember(missionRevision) { MissionManager.isDailyBonusClaimed(context) }

    Scaffold(
        containerColor = if (isSpaceTheme || isKingdomTheme) Color.Transparent else appColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isKingdomTheme) "👑 Kraliyet Görevleri" else if (isSpaceTheme) "🚀 Kozmik Görevler" else "Görevler & Seferler",
                        fontWeight = FontWeight.Black,
                        color = if (isKingdomTheme) Color(0xFFFFD700) else if (isSpaceTheme) Color.White else appColors.text
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text
                        )
                    }
                },
                actions = {
                    MissionWalletSummary(isKingdom = isKingdomTheme, isSpace = isSpaceTheme)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Tab Seçici
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.90f) else appColors.surface,
                    contentColor = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.primary
                        )
                    }
                ) {
                    MissionPeriod.values().forEach { period ->
                        Tab(
                            selected = selectedTab == period,
                            onClick = { selectedTab = period },
                            text = {
                                Text(
                                    period.title,
                                    fontWeight = if (selectedTab == period) FontWeight.Black else FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Günlük Bonus Banner (Sadece Günlük sekmesinde)
                if (selectedTab == MissionPeriod.DAILY) {
                    DailyBonusBanner(
                        isAvailable = isBonusAvailable,
                        isClaimed = isBonusClaimed,
                        isKingdom = isKingdomTheme,
                        onClaim = { MissionManager.claimDailyBonus(context) }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Görev Listesi
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(missions, key = { it.id }) { mission ->
                        MissionDetailCard(
                            mission = mission,
                            isKingdom = isKingdomTheme,
                            isSpace = isSpaceTheme,
                            onClaim = { MissionManager.claimMissionReward(context, mission) },
                            onGo = {
                                if (mission.targetRoute.isNotEmpty() && mission.targetRoute != Screen.Home.route) {
                                    navController.navigate(mission.targetRoute)
                                } else {
                                    navController.popBackStack()
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
private fun DailyBonusBanner(
    isAvailable: Boolean,
    isClaimed: Boolean,
    isKingdom: Boolean,
    onClaim: () -> Unit
) {
    val appColors = LocalAppThemeColors.current
    val bgBrush = if (isKingdom) {
        Brush.horizontalGradient(listOf(Color(0xFF1B3A6B), Color(0xFF0F2347)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFF38BDF8).copy(alpha = 0.25f), Color(0xFF818CF8).copy(alpha = 0.25f)))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.2.dp, if (isKingdom) Color(0xFFFFD700) else Color(0xFF38BDF8)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .background(bgBrush)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.daily_badge),
                contentDescription = "Günlük Rozet",
                modifier = Modifier.size(34.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "3/3 Günlük Görev Bonusu",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = if (isKingdom) Color.White else appColors.text
                )
                Text(
                    text = "Bugünkü tüm görevleri bitir, ekstra ${MissionConstants.DAILY_BONUS_GEMS} Elmas & Günlük Rozet kazan!",
                    fontSize = 11.sp,
                    color = if (isKingdom) Color(0xFFFFF7D6).copy(alpha = 0.8f) else appColors.textMuted
                )
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onClaim,
                enabled = isAvailable && !isClaimed,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isKingdom) Color(0xFFFFD700) else Color(0xFF38BDF8),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isClaimed) "Alındı ✓" else "+${MissionConstants.DAILY_BONUS_GEMS} Elmas & Rozet",
                    color = if (isAvailable && !isClaimed) Color.Black else Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun MissionDetailCard(
    mission: MissionItem,
    isKingdom: Boolean,
    isSpace: Boolean,
    onClaim: () -> Unit,
    onGo: () -> Unit
) {
    val appColors = LocalAppThemeColors.current

    val cardBorder = when {
        isKingdom -> BorderStroke(
            1.2.dp,
            if (mission.isCompleted && !mission.isClaimed) Brush.linearGradient(
                listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFFAA7C11), Color(0xFFFFDF7A))
            ) else Brush.linearGradient(listOf(Color(0xFF2E4870), Color(0xFF1B3A6B)))
        )
        isSpace -> BorderStroke(
            1.dp,
            if (mission.isCompleted && !mission.isClaimed) Color(0xFF38BDF8) else Color(0xFF334155)
        )
        else -> BorderStroke(
            1.dp,
            if (mission.isCompleted && !mission.isClaimed) appColors.primary else appColors.border
        )
    }

    val cardBg = when {
        isKingdom -> Color(0xFF070F26).copy(alpha = 0.92f)
        isSpace -> Color(0xFF0C132C).copy(alpha = 0.90f)
        else -> appColors.surface
    }

    val progressAnimated by animateFloatAsState(
        targetValue = if (mission.maxProgress > 0) mission.currentProgress.toFloat() / mission.maxProgress else 0f,
        label = "detailProgress"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = cardBorder,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (isKingdom) Color(0xFF1B3A6B).copy(alpha = 0.65f) else appColors.primarySoft,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    MissionIconView(iconKey = mission.id, isKingdom = isKingdom, modifier = Modifier.size(22.dp))
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mission.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isKingdom) Color.White else appColors.text
                    )
                    Text(
                        text = mission.description,
                        fontSize = 11.sp,
                        color = if (isKingdom) Color(0xFFFFF7D6).copy(alpha = 0.75f) else appColors.textMuted
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (isKingdom) Color(0xFFFFD700) else appColors.primary,
                    trackColor = if (isKingdom) Color(0xFF1B3A6B) else appColors.primarySoft
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "${mission.currentProgress} / ${mission.maxProgress}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isKingdom) Color(0xFFFFD700) else appColors.primary
                )
            }

            Spacer(Modifier.height(14.dp))

            // Ödüller ve Buton Alanı (Sığmazsa alt satıra taşınır, harf harf alt alta kırılmaz)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isCompact = maxWidth < 330.dp
                if (isCompact) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Ödül Çipleri
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (mission.reward.coins > 0) {
                                RewardChip("🪙", "+${mission.reward.coins}", isKingdom)
                            }
                            if (mission.reward.xp > 0) {
                                RewardChip("⚡", "+${mission.reward.xp} XP", isKingdom)
                            }
                            if (mission.reward.gems > 0) {
                                RewardChip("💎", "+${mission.reward.gems}", isKingdom)
                            }
                            if (mission.reward.badgeRes != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isKingdom) Color(0xFF122244) else Color(0xFFF1F5F9)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(mission.reward.badgeRes),
                                            contentDescription = mission.reward.badgeName,
                                            modifier = Modifier.size(16.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = when (mission.period) {
                                                MissionPeriod.DAILY -> "Günlük Rozet"
                                                MissionPeriod.WEEKLY -> "Haftalık Rozet"
                                                MissionPeriod.MONTHLY -> "Aylık Rozet"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when (mission.period) {
                                                MissionPeriod.DAILY -> Color(0xFF22C55E)
                                                MissionPeriod.WEEKLY -> Color(0xFF38BDF8)
                                                MissionPeriod.MONTHLY -> Color(0xFFEF4444)
                                            },
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        // Sığmadığında Başla butonu bir alt satıra taşınır
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            when {
                                mission.isClaimed -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Alındı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), maxLines = 1, softWrap = false)
                                        }
                                    }
                                }
                                mission.isCompleted -> {
                                    Button(
                                        onClick = onClaim,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isKingdom) Color(0xFFFFD700) else Color(0xFF10B981)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "Ödülü Al",
                                            color = if (isKingdom) Color.Black else Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                else -> {
                                    OutlinedButton(
                                        onClick = onGo,
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, if (isKingdom) Color(0xFFFFD700).copy(alpha = 0.6f) else appColors.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "Başla ›",
                                            color = if (isKingdom) Color(0xFFFFD700) else appColors.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ödül Çipleri
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (mission.reward.coins > 0) {
                                RewardChip("🪙", "+${mission.reward.coins}", isKingdom)
                            }
                            if (mission.reward.xp > 0) {
                                RewardChip("⚡", "+${mission.reward.xp} XP", isKingdom)
                            }
                            if (mission.reward.gems > 0) {
                                RewardChip("💎", "+${mission.reward.gems}", isKingdom)
                            }
                            if (mission.reward.badgeRes != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isKingdom) Color(0xFF122244) else Color(0xFFF1F5F9)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Image(
                                            painter = painterResource(mission.reward.badgeRes),
                                            contentDescription = mission.reward.badgeName,
                                            modifier = Modifier.size(16.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = when (mission.period) {
                                                MissionPeriod.DAILY -> "Günlük Rozet"
                                                MissionPeriod.WEEKLY -> "Haftalık Rozet"
                                                MissionPeriod.MONTHLY -> "Aylık Rozet"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when (mission.period) {
                                                MissionPeriod.DAILY -> Color(0xFF22C55E)
                                                MissionPeriod.WEEKLY -> Color(0xFF38BDF8)
                                                MissionPeriod.MONTHLY -> Color(0xFFEF4444)
                                            },
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Aksiyon Butonu
                        when {
                            mission.isClaimed -> {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Alındı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), maxLines = 1, softWrap = false)
                                    }
                                }
                            }
                            mission.isCompleted -> {
                                Button(
                                    onClick = onClaim,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isKingdom) Color(0xFFFFD700) else Color(0xFF10B981)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        "Ödülü Al",
                                        color = if (isKingdom) Color.Black else Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            else -> {
                                OutlinedButton(
                                    onClick = onGo,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, if (isKingdom) Color(0xFFFFD700).copy(alpha = 0.6f) else appColors.primary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        "Başla ›",
                                        color = if (isKingdom) Color(0xFFFFD700) else appColors.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionWalletSummary(isKingdom: Boolean, isSpace: Boolean) {
    val context = LocalContext.current
    Row(
        modifier = Modifier.padding(end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WalletChip(icon = "🪙", value = QuizTreeProgressStore.getCoins(context).toString(), isKingdom = isKingdom)
        WalletChip(icon = "💎", value = QuizTreeProgressStore.getGems(context).toString(), isKingdom = isKingdom)
    }
}

@Composable
private fun WalletChip(type: RewardType, value: String, isKingdom: Boolean) {
    val iconVector = when (type) {
        RewardType.COIN -> Icons.Default.MonetizationOn
        RewardType.XP -> Icons.Default.Bolt
        RewardType.GEM -> Icons.Default.Diamond
    }
    val iconColor = when (type) {
        RewardType.COIN -> Color(0xFFFFB300)
        RewardType.XP -> Color(0xFFFF9800)
        RewardType.GEM -> Color(0xFF00E5FF)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isKingdom) Color(0xFF070F26).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, if (isKingdom) Color(0xFFFFD700).copy(alpha = 0.6f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = if (isKingdom) Color(0xFFFFD700) else Color.White
            )
        }
    }
}

@Composable
private fun WalletChip(icon: String, value: String, isKingdom: Boolean) {
    val type = when (icon) {
        "🪙" -> RewardType.COIN
        "💎" -> RewardType.GEM
        else -> RewardType.XP
    }
    WalletChip(type = type, value = value, isKingdom = isKingdom)
}
