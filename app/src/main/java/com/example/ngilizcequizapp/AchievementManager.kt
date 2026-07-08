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
    LEGENDARY("Efsanevi (Legendary)", Color(0xFFF59E0B))
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

        // REGULAR
        BadgeData("regular_streak_3", "Üçleme", "badge_regular_streak_3.png", "Üst üste 3 gün boyunca ders bitirmeyi başardın!", BadgeRarity.REGULAR),
        BadgeData("regular_jar_fill", "Kavanoz Desteği", "badge_regular_jar_fill.png", "Hata kavanozuna ilk kez 5 hata biriktirdin!", BadgeRarity.REGULAR),
        BadgeData("regular_half_way", "Yarı Yol", "badge_regular_half_way.png", "Bir ünitenin veya konunun yarısına ulaştın!", BadgeRarity.REGULAR),
        BadgeData("regular_fast_responder", "Hızlı Cevap", "badge_regular_fast_responder.png", "Bir soruyu 3 saniyenin altında doğru cevapladın!", BadgeRarity.REGULAR),
        BadgeData("regular_word_chaser", "Kelime Avcısı", "badge_regular_word_chaser.png", "Toplamda 20 farklı kelime öğrenmeyi başardın!", BadgeRarity.REGULAR),
        BadgeData("regular_weekend_warrior", "Hafta Sonu Savaşçısı", "badge_regular_weekend_warrior.png", "Cumartesi ve Pazar günleri uygulamayı aktif kullandın!", BadgeRarity.REGULAR),

        // RARE
        BadgeData("rare_jar_emptier", "Kavanoz Boşaltıcı", "badge_rare_jar_emptier.png", "Hata kavanozundaki tüm hataları tek seferde sıfırladın!", BadgeRarity.RARE),
        BadgeData("rare_flawless", "Kusursuz Seri", "badge_rare_flawless.png", "Tek bir ders içinde hiç hata yapmadan bitirdin!", BadgeRarity.RARE),
        BadgeData("rare_7_day_streak", "Haftalık İstikrar", "badge_rare_7_day_streak.png", "Üst üste 7 gün boyunca kesintisiz giriş yapıp etkinlik tamamladın!", BadgeRarity.RARE),
        BadgeData("rare_quiz_ready", "Sınav Hazırlığı", "badge_rare_quiz_ready.png", "Üst üste 3 bilgi yarışması veya testi başarıyla geçtin!", BadgeRarity.RARE),
        BadgeData("rare_word_collector", "Kelime Koleksiyoneri", "badge_rare_word_collector.png", "Kelime listene 100 aktif kelime ekleyip çalıştın!", BadgeRarity.RARE),

        // EPIC
        BadgeData("epic_speedrun", "Hız Elementi", "badge_epic_speedrun.png", "Bir testi normal sürenin yarısından kısa sürede %100 başarıyla tamamladın!", BadgeRarity.EPIC),
        BadgeData("epic_30_day_streak", "Aylık Maraton", "badge_epic_30_day_streak.png", "Tam 30 gün boyunca her gün uygulamayı aktif kullandın!", BadgeRarity.EPIC),
        BadgeData("epic_master_fixer", "Hata Profesörü", "badge_epic_master_fixer.png", "Hata kavanozundan toplamda 100 hatayı başarıyla temizledin!", BadgeRarity.EPIC),
        BadgeData("epic_lexicon_master", "Sözlük Ustası", "badge_epic_lexicon_master.png", "250 kelimeyi tamamen ezberleyip testlerde kusursuz eşleştirdin!", BadgeRarity.EPIC),

        // LEGENDARY
        BadgeData("legendary_immortal_month", "Kusursuz Ay", "badge_legendary_immortal_month.png", "Koskoca bir ay boyunca tek bir gün bile sektirmeden her gün en az bir ders bitirdin!", BadgeRarity.LEGENDARY),
        BadgeData("legendary_cat_reflex", "Kedi Refleksi", "badge_legendary_cat_reflex.png", "Süreye karşı yarışılan en zorlu çeviri veya dinleme testinde hiç hata yapmadan rekor kırdın!", BadgeRarity.LEGENDARY),
        BadgeData("legendary_grandmaster", "Piramidin Hakimi", "badge_legendary_grandmaster.png", "Tüm nadirlik seviyelerindeki kupa sentezlerini tamamlayıp en üst lige ulaştın!", BadgeRarity.LEGENDARY)
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
}
