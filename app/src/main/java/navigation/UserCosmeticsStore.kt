package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

data class MewCatData(
    val id: String,
    val name: String,
    val title: String,
    val drawableRes: Int
)

data class WingItemData(
    val id: String,
    val name: String,
    val description: String,
    val priceGems: Int,
    val priceCoins: Int = 0,
    val drawableRes: Int,
    val rankType: RankType? = null,
    val isRankDefault: Boolean = false
)

object UserCosmeticsStore {
    private const val PREFS_NAME = "user_cosmetics_prefs"
    private const val KEY_EQUIPPED_WING = "equipped_wing_id"
    private const val KEY_EQUIPPED_AVATAR = "equipped_avatar_id"
    private const val KEY_TOP_BAR_AVATAR = "equipped_top_bar_avatar_id"
    private const val KEY_SPACE_THEME = "is_space_theme_enabled"
    private const val KEY_EQUIPPED_THEME = "equipped_theme_id"
    private const val KEY_EQUIPPED_FRAME = "equipped_frame_id"
    private const val KEY_MANUAL_RANK = "manual_rank_name"
    private const val KEY_OWNED_WINGS = "owned_wings_set"
    private const val KEY_OWNED_AVATARS = "owned_avatars_set"
    private const val KEY_OWNED_THEMES = "owned_themes_set"
    private const val KEY_OWNED_FRAMES = "owned_frames_set"

    // Recomposition triggers
    var stateRevision by mutableStateOf(0)
        private set

    fun notifyStateChanged() {
        stateRevision++
    }

    // The 5 Mew Cats ending with "mew"
    val allMewCats = listOf(
        MewCatData("akademisyen_mew", "Akademisyen Mew", "Bilge ve Araştırmacı", R.drawable.akademisyen_mew),
        MewCatData("cool_mew", "Havalı Mew", "Gözlüklü ve Karizmatik", R.drawable.cool_mew),
        MewCatData("pirate_mew", "Kaptan Mew", "Cesur Dil Kaşifi", R.drawable.pirate_mew),
        MewCatData("wizard_mew", "Büyücü Mew", "Gizemli ve Bilge", R.drawable.wizard_mew),
        MewCatData("maestro_mew", "Maestro Mew", "Kelimelerin Ustası", R.drawable.maestro_mew),
        MewCatData("king_mew", "Kral Mew", "Kraliyetin Hükümdarı", R.drawable.king_mew)
    )

    // The 8 Wings corresponding to the Ranks
    val predefinedWings = listOf(
        WingItemData(
            id = "rank_default",
            name = "Otomatik Rank Kanadı",
            description = "Seçili rankına göre otomatik dönüşen kanat.",
            priceGems = 0,
            priceCoins = 0,
            drawableRes = R.drawable.bronze_kanat,
            isRankDefault = true
        ),
        WingItemData(
            id = "bronze_kanat",
            name = "Bronz Kanat",
            description = "Bronz rankı için özel tasarım kanat.",
            priceGems = 0,
            priceCoins = 0,
            drawableRes = R.drawable.bronze_kanat,
            rankType = RankType.BRONZE
        ),
        WingItemData(
            id = "silver_kanat",
            name = "Gümüş Kanat",
            description = "Gümüş rankı için parlak metalik kanat.",
            priceGems = 30,
            priceCoins = 1000,
            drawableRes = R.drawable.silver_kanat,
            rankType = RankType.SILVER
        ),
        WingItemData(
            id = "gold_kanat",
            name = "Altın Kanat",
            description = "Altın rankı için ışıltılı altın kanat.",
            priceGems = 50,
            priceCoins = 2000,
            drawableRes = R.drawable.gold_kanat,
            rankType = RankType.GOLD
        ),
        WingItemData(
            id = "plat_kanat",
            name = "Platin Kanat",
            description = "Platin rankı için saf kristal platin kanat.",
            priceGems = 80,
            priceCoins = 3500,
            drawableRes = R.drawable.plat_kanat,
            rankType = RankType.PLATINUM
        ),
        WingItemData(
            id = "elmas_kanat",
            name = "Elmas Kanat",
            description = "Elmas rankı için göz alıcı mavi elmas kanat.",
            priceGems = 110,
            priceCoins = 5000,
            drawableRes = R.drawable.elmas_kanat,
            rankType = RankType.DIAMOND
        ),
        WingItemData(
            id = "master_kanat",
            name = "Usta Kanat",
            description = "Usta (Master) rankı için mor enerjili elit kanat.",
            priceGems = 150,
            priceCoins = 7500,
            drawableRes = R.drawable.master_kanat,
            rankType = RankType.MASTER
        ),
        WingItemData(
            id = "grandmaster_kanat",
            name = "Grandmaster Kanat",
            description = "Üstad (Grandmaster) rankı için alevli kızıl kanat.",
            priceGems = 200,
            priceCoins = 10000,
            drawableRes = R.drawable.grandmaster_kanat,
            rankType = RankType.GRANDMASTER
        ),
        WingItemData(
            id = "linguarium_kanat",
            name = "Linguarium Kanat",
            description = "En üst seviye Linguarium için efsanevi altın kanat.",
            priceGems = 350,
            priceCoins = 20000,
            drawableRes = R.drawable.linguarium_kanat,
            rankType = RankType.LINGUARIUM
        ),
        WingItemData(
            id = "kingdom_kanat",
            name = "Krallık Kanadı",
            description = "Kraliyet hanedanına özel altın taçlı ve kanatlı asil arma.",
            priceGems = 400,
            priceCoins = 0,
            drawableRes = R.drawable.kingdom_kanat
        )
    )

    fun getEquippedWingId(context: Context): String {
        val id = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EQUIPPED_WING, "rank_default") ?: "rank_default"
        // Migrate old names if saved
        return when (id) {
            "bronz_kanat" -> "bronze_kanat"
            "gumus_kanat" -> "silver_kanat"
            "altin_kanat" -> "gold_kanat"
            "platin_kanat" -> "plat_kanat"
            "usta_kanat" -> "master_kanat"
            else -> id
        }
    }

    fun setEquippedWingId(context: Context, wingId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_EQUIPPED_WING, wingId)
            .apply()
        stateRevision++
    }

    fun getWingDrawableForRank(rank: RankType): Int {
        return when (rank) {
            RankType.BRONZE -> R.drawable.bronze_kanat
            RankType.SILVER -> R.drawable.silver_kanat
            RankType.GOLD -> R.drawable.gold_kanat
            RankType.PLATINUM -> R.drawable.plat_kanat
            RankType.DIAMOND -> R.drawable.elmas_kanat
            RankType.MASTER -> R.drawable.master_kanat
            RankType.GRANDMASTER -> R.drawable.grandmaster_kanat
            RankType.LINGUARIUM -> R.drawable.linguarium_kanat
        }
    }

    fun getWingIdForRank(rank: RankType): String {
        return when (rank) {
            RankType.BRONZE -> "bronze_kanat"
            RankType.SILVER -> "silver_kanat"
            RankType.GOLD -> "gold_kanat"
            RankType.PLATINUM -> "plat_kanat"
            RankType.DIAMOND -> "elmas_kanat"
            RankType.MASTER -> "master_kanat"
            RankType.GRANDMASTER -> "grandmaster_kanat"
            RankType.LINGUARIUM -> "linguarium_kanat"
        }
    }

    fun getEquippedWingDrawable(context: Context, fallbackRank: RankType): Int {
        val wingId = getEquippedWingId(context)
        if (wingId == "rank_default") {
            return getWingDrawableForRank(getUserEffectiveRank(context, fallbackRank))
        }
        val found = predefinedWings.find { it.id == wingId }
        return found?.drawableRes ?: getWingDrawableForRank(fallbackRank)
    }

    // Manual rank selection
    fun getManualRank(context: Context): RankType? {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MANUAL_RANK, null) ?: return null
        return try {
            RankType.valueOf(name)
        } catch (e: Exception) {
            null
        }
    }

    fun setManualRank(context: Context, rank: RankType) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MANUAL_RANK, rank.name)
            .apply()
        // Automatically equip the corresponding wing
        setEquippedWingId(context, getWingIdForRank(rank))
    }

    fun getUserEffectiveRank(context: Context, defaultRank: RankType? = null): RankType {
        val manual = getManualRank(context)
        if (manual != null) return manual
        if (defaultRank != null) return defaultRank
        val xp = QuizTreeProgressStore.getXp(context)
        return getRankForXp(xp)
    }

    // Avatar Management
    fun normalizeAvatarId(id: String): String = when (id.lowercase().trim()) {
        "akademisyen", "akademisyen_mew" -> "akademisyen_mew"
        "havali", "cool_mew", "cool" -> "cool_mew"
        "kaptan", "pirate_mew", "pirate", "korsan" -> "pirate_mew"
        "buyucu", "wizard_mew", "wizard" -> "wizard_mew"
        "master", "maestro_mew", "maestro", "usta" -> "maestro_mew"
        "king", "king_mew", "kral", "kral_mew" -> "king_mew"
        else -> id.lowercase().trim()
    }

    fun isAvatarEquipped(context: Context, avatarId: String): Boolean {
        val current = getEquippedAvatar(context)
        return normalizeAvatarId(current) == normalizeAvatarId(avatarId)
    }

    fun getEquippedAvatar(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EQUIPPED_AVATAR, "akademisyen_mew") ?: "akademisyen_mew"
    }

    fun setEquippedAvatar(context: Context, avatarId: String) {
        val normalized = normalizeAvatarId(avatarId)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_EQUIPPED_AVATAR, normalized)
            .apply()
        stateRevision++
        com.example.ngilizcequizapp.MewStreakWidgetProvider.updateStreakWidget(context)
    }

    fun getEquippedAvatarRes(context: Context): Int {
        val avatarId = getEquippedAvatar(context).lowercase().trim()
        val normalized = normalizeAvatarId(avatarId)
        val mewCat = allMewCats.find { it.id.equals(normalized, ignoreCase = true) }
        if (mewCat != null) return mewCat.drawableRes

        val resId = context.resources.getIdentifier(normalized, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.akademisyen_mew
    }

    // Top Bar Avatar Management (Decoupled from Wing/Main Avatar)
    fun getEquippedTopBarAvatar(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TOP_BAR_AVATAR, "akademisyen_mew") ?: "akademisyen_mew"
    }

    fun setEquippedTopBarAvatar(context: Context, avatarId: String) {
        val normalized = normalizeAvatarId(avatarId)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TOP_BAR_AVATAR, normalized)
            .apply()
        stateRevision++
    }

    fun getEquippedTopBarAvatarRes(context: Context): Int {
        val avatarId = getEquippedTopBarAvatar(context).lowercase().trim()
        val normalized = normalizeAvatarId(avatarId)
        val mewCat = allMewCats.find { it.id.equals(normalized, ignoreCase = true) }
        if (mewCat != null) return mewCat.drawableRes

        val resId = context.resources.getIdentifier(normalized, "drawable", context.packageName)
        return if (resId != 0) resId else R.drawable.akademisyen_mew
    }

    // Single Source of Truth Theme Management
    fun getEquippedTheme(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTheme = prefs.getString(KEY_EQUIPPED_THEME, null)
        if (!savedTheme.isNullOrBlank()) {
            return when (savedTheme.lowercase().trim()) {
                "kingdom", "krallik" -> "kingdom"
                "uzay", "space" -> "uzay"
                "dark" -> "dark"
                else -> "klasik"
            }
        }
        val themePrefs = context.getSharedPreferences("mew_theme_prefs", Context.MODE_PRIVATE)
        return if (themePrefs.getBoolean("dark_mode", false)) "dark" else "klasik"
    }

    fun isSpaceThemeActive(context: Context): Boolean {
        return getEquippedTheme(context) == "uzay"
    }

    fun isKingdomThemeActive(context: Context): Boolean {
        return getEquippedTheme(context) == "kingdom"
    }

    fun isDarkThemeActive(context: Context): Boolean {
        return getEquippedTheme(context) == "dark"
    }

    fun isKlasikThemeActive(context: Context): Boolean {
        return getEquippedTheme(context) == "klasik"
    }

    fun isDarkModeActive(context: Context): Boolean {
        return getEquippedTheme(context) != "klasik"
    }

    fun setSpaceThemeActive(context: Context, active: Boolean) {
        if (active) {
            setEquippedTheme(context, "uzay")
        } else {
            val current = getEquippedTheme(context)
            if (current == "uzay") {
                setEquippedTheme(context, "klasik")
            }
        }
    }

    fun setDarkMode(context: Context, enabled: Boolean) {
        if (enabled) {
            val current = getEquippedTheme(context)
            if (current == "klasik") {
                setEquippedTheme(context, "dark")
            }
        } else {
            setEquippedTheme(context, "klasik")
        }
    }

    fun setEquippedTheme(context: Context, themeId: String) {
        val cleanTheme = when (themeId.lowercase().trim()) {
            "kingdom", "krallik" -> "kingdom"
            "uzay", "space" -> "uzay"
            "dark" -> "dark"
            else -> "klasik"
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_EQUIPPED_THEME, cleanTheme)
            .putBoolean(KEY_SPACE_THEME, cleanTheme == "uzay")
            .apply()

        val isDark = cleanTheme != "klasik"
        context.getSharedPreferences("mew_theme_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("dark_mode", isDark)
            .apply()

        stateRevision++
        com.example.ngilizcequizapp.MewStreakWidgetProvider.updateStreakWidget(context)
    }

    // Frames
    fun getEquippedFrame(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_EQUIPPED_FRAME, "blue") ?: "blue"
    }

    fun setEquippedFrame(context: Context, frameId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_EQUIPPED_FRAME, frameId)
            .apply()
        stateRevision++
    }

    fun getEquippedFrameColor(context: Context, fallbackPrimary: Color): Color {
        return when (getEquippedFrame(context)) {
            "altin" -> Color(0xFFFFD700)
            "cicek" -> Color(0xFFF43F5E)
            "neon" -> Color(0xFF00E5FF)
            "kraliyet" -> Color(0xFFFFB300)
            "blue" -> Color(0xFF3897F0)
            else -> fallbackPrimary
        }
    }

    // Cosmetic Price Constants: (priceCoins, priceGems)
    val avatarPrices = mapOf(
        "akademisyen_mew" to Pair(0, 0),
        "cool_mew" to Pair(2000, 60),
        "pirate_mew" to Pair(3000, 80),
        "wizard_mew" to Pair(3000, 80),
        "king_mew" to Pair(5000, 120)
    )

    val framePrices = mapOf(
        "blue" to Pair(800, 25),
        "altin" to Pair(2500, 50),
        "cicek" to Pair(2000, 40),
        "neon" to Pair(3500, 70),
        "kraliyet" to Pair(5000, 100)
    )

    // Owned Items
    fun getOwnedWings(context: Context): Set<String> {
        val set = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_OWNED_WINGS, null)
        val defaults = setOf("rank_default", "bronze_kanat")
        return (set ?: emptySet()) + defaults
    }

    fun unlockWing(context: Context, wingId: String) {
        val current = getOwnedWings(context).toMutableSet()
        current.add(wingId.lowercase().trim())
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_OWNED_WINGS, current)
            .apply()
        stateRevision++
    }

    fun isWingOwned(context: Context, wingId: String): Boolean {
        val normalized = wingId.lowercase().trim()
        if (normalized in listOf("rank_default", "bronze_kanat")) return true
        return getOwnedWings(context).contains(normalized)
    }

    fun getOwnedAvatars(context: Context): Set<String> {
        val set = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_OWNED_AVATARS, null)
        val defaultCats = setOf("akademisyen_mew")
        return (set ?: emptySet()) + defaultCats
    }

    fun unlockAvatar(context: Context, avatarId: String) {
        val current = getOwnedAvatars(context).toMutableSet()
        current.add(normalizeAvatarId(avatarId))
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_OWNED_AVATARS, current)
            .apply()
        stateRevision++
    }

    fun isAvatarOwned(context: Context, avatarId: String): Boolean {
        val normalized = normalizeAvatarId(avatarId)
        if (normalized == "akademisyen_mew") return true
        return getOwnedAvatars(context).contains(normalized)
    }

    fun getOwnedThemes(context: Context): Set<String> {
        val set = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_OWNED_THEMES, null)
        val defaults = setOf("klasik", "dark")
        return (set ?: emptySet()) + defaults
    }

    fun unlockTheme(context: Context, themeId: String) {
        val current = getOwnedThemes(context).toMutableSet()
        val normalized = themeId.lowercase().trim()
        current.add(normalized)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_OWNED_THEMES, current)
            .apply()
        stateRevision++
    }

    fun isThemeOwned(context: Context, themeId: String): Boolean {
        val normalized = themeId.lowercase().trim()
        if (normalized in listOf("klasik", "dark")) return true
        return getOwnedThemes(context).contains(normalized)
    }

    fun getOwnedFrames(context: Context): Set<String> {
        val set = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_OWNED_FRAMES, null)
        val defaults = setOf("blue")
        return (set ?: emptySet()) + defaults
    }

    fun unlockFrame(context: Context, frameId: String) {
        val current = getOwnedFrames(context).toMutableSet()
        current.add(frameId.lowercase().trim())
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_OWNED_FRAMES, current)
            .apply()
        stateRevision++
    }

    fun isFrameOwned(context: Context, frameId: String): Boolean {
        val normalized = frameId.lowercase().trim()
        if (normalized == "blue") return true
        return getOwnedFrames(context).contains(normalized)
    }

    // --- GOOGLE PLAY BILLING PRODUCT IDS & HELPERS ---
    const val PRODUCT_ID_THEME_KINGDOM = "theme_kingdom"
    const val PRODUCT_ID_THEME_SPACE = "theme_space"

    fun unlockKingdomThemeWithWing(context: Context) {
        unlockTheme(context, "kingdom")
        setEquippedTheme(context, "kingdom")
        unlockWing(context, "kingdom_kanat")
        setEquippedWingId(context, "kingdom_kanat")
        stateRevision++
    }

    fun unlockSpaceTheme(context: Context) {
        unlockTheme(context, "uzay")
        setEquippedTheme(context, "uzay")
        stateRevision++
    }
}
