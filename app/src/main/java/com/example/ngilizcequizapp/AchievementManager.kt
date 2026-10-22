package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class BadgeRarity(val displayName: String, val color: Color) {
    COMMON("Yaygın (Common)", Color(0xFF94A3B8)),
    REGULAR("Normal (Regular)", Color(0xFF22C55E)),
    RARE("Nadir (Rare)", Color(0xFF3B82F6)),
    EPIC("Epik (Epic)", Color(0xFF8B5CF6)),
    LEGENDARY("Efsanevi (Legendary)", Color(0xFFF59E0B)),
    MYTHIC("Mistik / Efsanevi+ (Mythic)", Color(0xFFF43F5E))
}

data class BadgeData(
    val id: String,
    val name: String,
    val assetName: String,
    val description: String,
    val rarity: BadgeRarity
)

object AchievementManager {
    private const val PREFS_NAME = "badge_achievements"
    private const val KEY_UNLOCKED_PREFIX = "badge_unlocked_"

    var activeToast by mutableStateOf<BadgeData?>(null)

    // Unlocked states
    val unlockedIds = mutableStateListOf<String>()

    val badges = listOf(
        // COMMON
        BadgeData("common_first_step", "İlk Adım", "badge_common_first_step.png", "İlk dersini başarıyla tamamladın!", BadgeRarity.COMMON),
        BadgeData("common_first_mistake", "Hata Dostu", "badge_common_first_mistake.png", "İlk hatanı yaptın, öğrenme sürecine hoş geldin!", BadgeRarity.COMMON),
        BadgeData("common_first_fix", "İlk Tamir", "badge_common_first_fix.png", "Hata Kavanozu içindeki ilk hatanı başarıyla düzelttin!", BadgeRarity.COMMON),
        BadgeData("common_listener", "Kulak Misafiri", "badge_common_listener.png", "İlk dinleme aktivitesini başarıyla tamamladın!", BadgeRarity.COMMON),
        BadgeData("common_explorer", "Meraklı", "badge_common_explorer.png", "Uygulama sekmeleri arasında ilk keşfini yaptın!", BadgeRarity.COMMON),
        BadgeData("common_morning_run", "Güneş Doğdu", "badge_common_morning_run.png", "Sabah 06:00 - 10:00 arasında bir aktivite tamamladın!", BadgeRarity.COMMON),
        BadgeData("common_night_owl", "Gece Kuşu", "badge_common_night_owl.png", "Gece 00:00'dan sonra bir ders tamamladın!", BadgeRarity.COMMON),
        BadgeData("common_word_intro", "Kelime Tanışıklığı", "badge_common_word_intro.png", "Sözlüğüne ilk kelimeyi başarıyla ekledin!", BadgeRarity.COMMON),
        BadgeData("regular_daily_champion", "Günlük Zafer Nişanı", "daily_badge.png", "Günlük krallık görevlerini başarıyla tamamladın!", BadgeRarity.COMMON),
        BadgeData("star_100_yellow", "Sarı Yıldız Ustası", "sari_yildiz.png", "Toplamda 100 Sarı Yıldız kazandın!", BadgeRarity.COMMON),

        // REGULAR
        BadgeData("regular_streak_3", "Üçleme", "badge_regular_streak_3.png", "Üst üste 3 gün boyunca ders bitirmeyi başardın!", BadgeRarity.REGULAR),
        BadgeData("regular_jar_fill", "Kavanoz Desteği", "badge_regular_jar_fill.png", "Hata kavanozuna ilk kez 5 hata biriktirdin!", BadgeRarity.REGULAR),
        BadgeData("regular_half_way", "Yarı Yol", "badge_regular_half_way.png", "Bir ünitenin veya konunun yarısına ulaştın!", BadgeRarity.REGULAR),
        BadgeData("regular_fast_responder", "Hızlı Cevap", "badge_regular_fast_responder.png", "Bir soruyu 3 saniyenin altında doğru cevapladın!", BadgeRarity.REGULAR),
        BadgeData("regular_word_chaser", "Kelime Avcısı", "badge_regular_word_chaser.png", "Toplamda 20 farklı kelime öğrenmeyi başardın!", BadgeRarity.REGULAR),
        BadgeData("regular_weekend_warrior", "Hafta Sonu Savaşçısı", "badge_regular_weekend_warrior.png", "Cumartesi ve Pazar günleri uygulamayı aktif kullandın!", BadgeRarity.REGULAR),
        BadgeData("rare_weekly_champion", "Zamanında!", "week_badge.png", "Haftalık krallık görevini başarıyla tamamladın!", BadgeRarity.REGULAR),

        // RARE
        BadgeData("rare_jar_emptier", "Kavanoz Boşaltıcı", "badge_rare_jar_emptier.png", "Hata kavanozundaki tüm hataları tek seferde sıfırladın!", BadgeRarity.RARE),
        BadgeData("rare_flawless", "Kusursuz Seri", "badge_rare_flawless.png", "Tek bir ders içinde hiç hata yapmadan bitirdin!", BadgeRarity.RARE),
        BadgeData("rare_7_day_streak", "Haftalık İstikrar", "badge_rare_7_day_streak.png", "Üst üste 7 gün boyunca kesintisiz giriş yapıp etkinlik tamamladın!", BadgeRarity.RARE),
        BadgeData("rare_quiz_ready", "Sınav Hazırlığı", "badge_rare_quiz_ready.png", "Üst üste 3 bilgi yarışması veya testi başarıyla geçtin!", BadgeRarity.RARE),
        BadgeData("rare_word_collector", "Kelime Koleksiyoneri", "badge_rare_word_collector.png", "Kelime listene 100 aktif kelime ekleyip çalıştın!", BadgeRarity.RARE),
        BadgeData("star_200_blue", "Mavi Yıldız Muhafızı", "mavi_yildiz.png", "Toplamda 200 Mavi Yıldız kazandın!", BadgeRarity.RARE),

        // EPIC
        BadgeData("epic_speedrun", "Hız Elementi", "badge_epic_speedrun.png", "Bir testi normal sürenin yarısından kısa sürede %100 başarıyla tamamladın!", BadgeRarity.EPIC),
        BadgeData("epic_30_day_streak", "Aylık Maraton", "badge_epic_30_day_streak.png", "Tam 30 gün boyunca her gün uygulamayı aktif kullandın!", BadgeRarity.EPIC),
        BadgeData("epic_master_fixer", "Hata Profesörü", "badge_epic_master_fixer.png", "Hata kavanozundan toplamda 100 hatayı başarıyla temizledin!", BadgeRarity.EPIC),
        BadgeData("epic_lexicon_master", "Sözlük Ustası", "badge_epic_lexicon_master.png", "250 kelimeyi tamamen ezberleyip testlerde kusursuz eşleştirdin!", BadgeRarity.EPIC),
        BadgeData("legendary_monthly_ruler", "Ayın elemanı", "month_badge.png", "Aylık efsanevi krallık görevini başarıyla tamamladın!", BadgeRarity.EPIC),
        BadgeData("star_300_red", "Kırmızı Yıldız Şampiyonu", "kirmizi_yildiz.png", "Toplamda 300 Kırmızı Yıldız kazandın!", BadgeRarity.EPIC),
        BadgeData("star_500_yellow", "Güneş Hükümdarı", "sari_yildiz2.png", "Tam 500 Sarı Yıldız ile eşsiz bir taht kurdun!", BadgeRarity.EPIC),

        // LEGENDARY
        BadgeData("legendary_immortal_month", "Kusursuz Ay", "badge_legendary_immortal_month.png", "Koskoca bir ay boyunca tek bir gün bile sektirmeden her gün en az bir ders bitirdin!", BadgeRarity.LEGENDARY),
        BadgeData("legendary_cat_reflex", "Kedi Refleksi", "badge_legendary_cat_reflex.png", "Süreye karşı yarışılan en zorlu çeviri veya dinleme testinde hiç hata yapmadan rekor kırdın!", BadgeRarity.LEGENDARY),
        BadgeData("legendary_grandmaster", "Piramidin Hakimi", "badge_legendary_grandmaster.png", "Tüm nadirlik seviyelerindeki kupa sentezlerini tamamlayıp en üst lige ulaştın!", BadgeRarity.LEGENDARY),
        BadgeData("star_500_blue", "Safir Lordu", "mavi_yildiz2.png", "Tam 500 Mavi Yıldız biriktirerek efsanevi mertebeye eriştin!", BadgeRarity.LEGENDARY),

        // MYTHIC
        BadgeData("star_500_red", "Mistik Kızıl Lord", "kirmizi_yildiz2.png", "Tam 500 Kırmızı Yıldız toplayarak zirveye adını kazıdın!", BadgeRarity.MYTHIC)
    )

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        unlockedIds.clear()
        badges.forEach { badge ->
            if (prefs.getBoolean("$KEY_UNLOCKED_PREFIX${badge.id}", false)) {
                unlockedIds.add(badge.id)
            }
        }
    }

    fun isUnlocked(badgeId: String): Boolean {
        return unlockedIds.contains(badgeId)
    }

    fun unlockBadge(context: Context, badgeId: String) {
        val badge = badges.find { it.id == badgeId } ?: return
        if (isUnlocked(badgeId)) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("$KEY_UNLOCKED_PREFIX$badgeId", true).apply()
        
        unlockedIds.add(badgeId)
        activeToast = badge
        
        // Check if Grandmaster (Piramidin Hakimi) can be unlocked now!
        if (badgeId != "legendary_grandmaster") {
            val allOthersUnlocked = badges.filter { it.id != "legendary_grandmaster" }.all { isUnlocked(it.id) }
            if (allOthersUnlocked) {
                unlockBadge(context, "legendary_grandmaster")
            }
        }
    }

    fun lockBadge(context: Context, badgeId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("$KEY_UNLOCKED_PREFIX$badgeId", false).apply()
        unlockedIds.remove(badgeId)
    }

    fun getBadgeProgress(context: Context, badgeId: String): Float {
        if (isUnlocked(badgeId)) return 1.0f

        val totalQuizzes = maxOf(1, QuizDataHolder.quizSteps.size + QuizDataHolder.quizStepsA2.size)

        return when (badgeId) {
            "star_100_yellow" -> {
                val totalYellow = QuizTreeProgressStore.getTotalYellowStars(context, totalQuizzes)
                (totalYellow.toFloat() / 100f).coerceIn(0f, 1f)
            }
            "star_200_blue" -> {
                val totalBlue = QuizTreeProgressStore.getTotalBlueStars(context, totalQuizzes)
                (totalBlue.toFloat() / 200f).coerceIn(0f, 1f)
            }
            "star_300_red" -> {
                val totalRed = QuizTreeProgressStore.getTotalRedStars(context, totalQuizzes)
                (totalRed.toFloat() / 300f).coerceIn(0f, 1f)
            }
            "star_500_yellow" -> {
                val totalYellow = QuizTreeProgressStore.getTotalYellowStars(context, totalQuizzes)
                (totalYellow.toFloat() / 500f).coerceIn(0f, 1f)
            }
            "star_500_blue" -> {
                val totalBlue = QuizTreeProgressStore.getTotalBlueStars(context, totalQuizzes)
                (totalBlue.toFloat() / 500f).coerceIn(0f, 1f)
            }
            "star_500_red" -> {
                val totalRed = QuizTreeProgressStore.getTotalRedStars(context, totalQuizzes)
                (totalRed.toFloat() / 500f).coerceIn(0f, 1f)
            }
            "common_first_step" -> {
                val solved = QuizTreeProgressStore.loadAll(context).values.count {
                    QuizTreeProgressStore.starsFor(it) >= 1
                }
                (solved.toFloat() / 1f).coerceIn(0f, 1f)
            }
            "common_first_mistake" -> {
                MistakeJarStore.load(context)
                val mistakes = MistakeJarStore.mistakes.size
                (mistakes.toFloat() / 1f).coerceIn(0f, 1f)
            }
            "common_first_fix" -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (prefs.getBoolean("$KEY_UNLOCKED_PREFIX$badgeId", false)) 1f else 0.5f
            }
            "common_listener" -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (prefs.getBoolean("$KEY_UNLOCKED_PREFIX$badgeId", false)) 1f else 0.5f
            }
            "common_explorer" -> {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (prefs.getBoolean("$KEY_UNLOCKED_PREFIX$badgeId", false)) 1f else 0.5f
            }
            "common_morning_run" -> {
                val cal = java.util.Calendar.getInstance()
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                if (hour in 6..10) 0.8f else 0.3f
            }
            "common_night_owl" -> {
                val cal = java.util.Calendar.getInstance()
                val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
                if (hour in 0..4) 0.8f else 0.3f
            }
            "common_word_intro" -> {
                val words = WordStorage.savedWords.size
                (words.toFloat() / 1f).coerceIn(0f, 1f)
            }
            "regular_daily_champion" -> {
                val dailyMissions = MissionManager.getAllMissions(context, MissionPeriod.DAILY)
                val completed = dailyMissions.count { it.isCompleted }
                (completed.toFloat() / 3f).coerceIn(0f, 1f)
            }
            "regular_streak_3" -> {
                val streak = StreakManager.getStreak(context)
                (streak.toFloat() / 3f).coerceIn(0f, 1f)
            }
            "regular_jar_fill" -> {
                MistakeJarStore.load(context)
                val mistakes = MistakeJarStore.mistakes.size
                (mistakes.toFloat() / 5f).coerceIn(0f, 1f)
            }
            "regular_half_way" -> {
                val solved = QuizTreeProgressStore.loadAll(context).values.count {
                    QuizTreeProgressStore.starsFor(it) >= 1
                }
                val total = maxOf(1, QuizDataHolder.quizSteps.size)
                (solved.toFloat() / (total / 2f)).coerceIn(0f, 1f)
            }
            "regular_fast_responder" -> 0.6f
            "regular_word_chaser" -> {
                val words = WordStorage.savedWords.size
                (words.toFloat() / 20f).coerceIn(0f, 1f)
            }
            "regular_weekend_warrior" -> {
                val cal = java.util.Calendar.getInstance()
                val day = cal.get(java.util.Calendar.DAY_OF_WEEK)
                if (day == java.util.Calendar.SATURDAY || day == java.util.Calendar.SUNDAY) 0.75f else 0.25f
            }
            "rare_weekly_champion" -> {
                val weeklyMissions = MissionManager.getAllMissions(context, MissionPeriod.WEEKLY)
                val completed = weeklyMissions.count { it.isCompleted }
                (completed.toFloat() / 3f).coerceIn(0f, 1f)
            }
            "rare_jar_emptier" -> {
                MistakeJarStore.load(context)
                val mistakes = MistakeJarStore.mistakes.size
                if (mistakes == 0) 0.8f else (1f / (mistakes + 1)).coerceIn(0.1f, 0.9f)
            }
            "rare_flawless" -> 0.7f
            "rare_7_day_streak" -> {
                val streak = StreakManager.getStreak(context)
                (streak.toFloat() / 7f).coerceIn(0f, 1f)
            }
            "rare_quiz_ready" -> 0.66f
            "rare_word_collector" -> {
                val words = WordStorage.savedWords.size
                (words.toFloat() / 100f).coerceIn(0f, 1f)
            }
            "epic_speedrun" -> 0.5f
            "epic_30_day_streak" -> {
                val streak = StreakManager.getStreak(context)
                (streak.toFloat() / 30f).coerceIn(0f, 1f)
            }
            "epic_master_fixer" -> {
                val words = WordStorage.savedWords.size
                (words.toFloat() / 100f).coerceIn(0f, 1f)
            }
            "epic_lexicon_master" -> {
                val words = WordStorage.savedWords.size
                (words.toFloat() / 250f).coerceIn(0f, 1f)
            }
            "legendary_monthly_ruler" -> {
                val monthlyMissions = MissionManager.getAllMissions(context, MissionPeriod.MONTHLY)
                val completed = monthlyMissions.count { it.isCompleted }
                (completed.toFloat() / 3f).coerceIn(0f, 1f)
            }
            "legendary_immortal_month" -> {
                val streak = StreakManager.getStreak(context)
                (streak.toFloat() / 30f).coerceIn(0f, 1f)
            }
            "legendary_cat_reflex" -> 0.4f
            "legendary_grandmaster" -> {
                val unlockedCount = badges.count { isUnlocked(it.id) }
                val totalOther = maxOf(1, badges.size - 1)
                (unlockedCount.toFloat() / totalOther).coerceIn(0f, 1f)
            }
            else -> 0.25f
        }
    }

    fun checkStarBadges(context: Context) {
        val starBadgeIds = listOf(
            "star_100_yellow",
            "star_200_blue",
            "star_300_red",
            "star_500_yellow",
            "star_500_blue",
            "star_500_red"
        )
        starBadgeIds.forEach { id ->
            if (!isUnlocked(id) && getBadgeProgress(context, id) >= 1.0f) {
                unlockBadge(context, id)
            }
        }
    }

    fun getClosestIncompleteBadges(context: Context, limit: Int = 5): List<Pair<BadgeData, Float>> {
        init(context)
        val list = mutableListOf<Pair<BadgeData, Float>>()
        badges.forEach { badge ->
            if (!isUnlocked(badge.id)) {
                val progress = getBadgeProgress(context, badge.id)
                if (progress >= 1.0f) {
                    unlockBadge(context, badge.id)
                } else {
                    list.add(badge to progress)
                }
            }
        }
        return list.sortedByDescending { it.second }.take(limit)
    }
}
