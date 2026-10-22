package navigation

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

enum class BoostType {
    XP,
    COIN,
    MEGA,
    STREAK_SHIELD,
    STREAK_SHIELD_PLUS,
    STREAK_SHIELD_PRIME
}

data class ActiveBoost(
    val id: String,
    val name: String,
    val type: BoostType,
    val expiryTimestamp: Long,
    val multiplier: Int = 2,
    val description: String = ""
)

data class QueuedBoost(
    val id: String,
    val name: String,
    val type: BoostType,
    val durationMinutes: Int,
    val multiplier: Int = 2
)

object MarketBoostStore {
    private const val PREFS_NAME = "market_boost_prefs"

    // Multi-boost expiry keys
    private const val KEY_XP_EXPIRY = "boost_xp_expiry"
    private const val KEY_COIN_EXPIRY = "boost_coin_expiry"
    private const val KEY_MEGA_EXPIRY = "boost_mega_expiry"
    private const val KEY_SHIELD_EXPIRY = "boost_shield_expiry"
    private const val KEY_SHIELD_IS_PLUS = "boost_shield_is_plus"
    private const val KEY_SHIELD_TIER = "boost_shield_tier"

    // Queue key
    private const val KEY_QUEUED_BOOSTS = "queued_boosts_json"

    // Legacy keys for fallback
    private const val KEY_LEGACY_NAME = "active_boost_name"
    private const val KEY_LEGACY_TYPE = "active_boost_type"
    private const val KEY_LEGACY_EXPIRY = "active_boost_expiry"

    var stateRevision by mutableStateOf(0)
        private set

    /**
     * Activates a boost. If the boost type is already active, extends the duration!
     */
    fun activateBoost(
        context: Context,
        name: String,
        type: BoostType,
        durationMinutes: Int,
        multiplier: Int = 2
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val durationMs = durationMinutes * 60 * 1000L
        val editor = prefs.edit()

        when (type) {
            BoostType.XP -> {
                val currentExp = prefs.getLong(KEY_XP_EXPIRY, 0L)
                val base = if (currentExp > now) currentExp else now
                editor.putLong(KEY_XP_EXPIRY, base + durationMs)
            }
            BoostType.COIN -> {
                val currentExp = prefs.getLong(KEY_COIN_EXPIRY, 0L)
                val base = if (currentExp > now) currentExp else now
                editor.putLong(KEY_COIN_EXPIRY, base + durationMs)
            }
            BoostType.MEGA -> {
                val currentExp = prefs.getLong(KEY_MEGA_EXPIRY, 0L)
                val base = if (currentExp > now) currentExp else now
                editor.putLong(KEY_MEGA_EXPIRY, base + durationMs)

                // Mega Boost Paketi: XP Boost + Coin Boost + Streak Kalkanı (en az 24 saat) birlikte devreye girer
                val currentShieldExp = prefs.getLong(KEY_SHIELD_EXPIRY, 0L)
                val shieldDurationMs = maxOf(durationMs, 1440 * 60 * 1000L)
                val shieldBase = if (currentShieldExp > now) currentShieldExp else now
                editor.putLong(KEY_SHIELD_EXPIRY, shieldBase + shieldDurationMs)
            }
            BoostType.STREAK_SHIELD -> {
                val currentExp = prefs.getLong(KEY_SHIELD_EXPIRY, 0L)
                val base = if (currentExp > now) currentExp else now
                editor.putLong(KEY_SHIELD_EXPIRY, base + durationMs)
                if (!prefs.getBoolean(KEY_SHIELD_IS_PLUS, false)) {
                    editor.putBoolean(KEY_SHIELD_IS_PLUS, false)
                }
                if (prefs.getString(KEY_SHIELD_TIER, "") != "prime" && prefs.getString(KEY_SHIELD_TIER, "") != "plus") {
                    editor.putString(KEY_SHIELD_TIER, "regular")
                }
            }
            BoostType.STREAK_SHIELD_PLUS -> {
                val currentExp = prefs.getLong(KEY_SHIELD_EXPIRY, 0L)
                val base = if (currentExp > now) currentExp else now
                editor.putLong(KEY_SHIELD_EXPIRY, base + durationMs)
                editor.putBoolean(KEY_SHIELD_IS_PLUS, true)
                if (prefs.getString(KEY_SHIELD_TIER, "") != "prime") {
                    editor.putString(KEY_SHIELD_TIER, "plus")
                }
            }
            BoostType.STREAK_SHIELD_PRIME -> {
                val currentExp = prefs.getLong(KEY_SHIELD_EXPIRY, 0L)
                val base = if (currentExp > now) currentExp else now
                editor.putLong(KEY_SHIELD_EXPIRY, base + durationMs)
                editor.putBoolean(KEY_SHIELD_IS_PLUS, true)
                editor.putString(KEY_SHIELD_TIER, "prime")
            }
        }

        // Also update legacy key for compatibility
        editor.putString(KEY_LEGACY_NAME, name)
        editor.putString(KEY_LEGACY_TYPE, type.name)
        editor.putLong(KEY_LEGACY_EXPIRY, now + durationMs)
        editor.apply()

        stateRevision++
        if (type == BoostType.STREAK_SHIELD || type == BoostType.STREAK_SHIELD_PLUS || type == BoostType.STREAK_SHIELD_PRIME || type == BoostType.MEGA) {
            com.example.ngilizcequizapp.MewStreakWidgetProvider.updateStreakWidget(context)
        }
    }

    /**
     * Queues a boost to begin automatically after Mega Boost ends.
     */
    fun queueBoost(
        context: Context,
        name: String,
        type: BoostType,
        durationMinutes: Int,
        multiplier: Int = 2
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentJson = prefs.getString(KEY_QUEUED_BOOSTS, "[]") ?: "[]"
        val array = try { JSONArray(currentJson) } catch (e: Exception) { JSONArray() }

        val obj = JSONObject().apply {
            put("id", System.currentTimeMillis().toString())
            put("name", name)
            put("type", type.name)
            put("durationMinutes", durationMinutes)
            put("multiplier", multiplier)
        }
        array.put(obj)

        prefs.edit().putString(KEY_QUEUED_BOOSTS, array.toString()).apply()
        stateRevision++
    }

    fun getQueuedBoosts(context: Context): List<QueuedBoost> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_QUEUED_BOOSTS, "[]") ?: "[]"
        val list = mutableListOf<QueuedBoost>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QueuedBoost(
                        id = obj.optString("id", i.toString()),
                        name = obj.optString("name", "Güçlendirici"),
                        type = BoostType.valueOf(obj.optString("type", BoostType.XP.name)),
                        durationMinutes = obj.optInt("durationMinutes", 30),
                        multiplier = obj.optInt("multiplier", 2)
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    /**
     * Checks if Mega Boost has expired. If so, pops and activates queued boosts!
     */
    private fun checkAndProcessQueue(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val megaExp = prefs.getLong(KEY_MEGA_EXPIRY, 0L)

        if (megaExp <= now) {
            val queued = getQueuedBoosts(context)
            if (queued.isNotEmpty()) {
                // Clear queue
                prefs.edit().putString(KEY_QUEUED_BOOSTS, "[]").apply()
                // Activate each queued boost now
                for (b in queued) {
                    activateBoost(context, b.name, b.type, b.durationMinutes, b.multiplier)
                }
            }
        }
    }

    /**
     * Returns a list of all currently active boosts.
     */
    fun getActiveBoosts(context: Context): List<ActiveBoost> {
        checkAndProcessQueue(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val list = mutableListOf<ActiveBoost>()

        // 1. Mega Boost
        val megaExp = prefs.getLong(KEY_MEGA_EXPIRY, 0L)
        if (megaExp > now) {
            list.add(
                ActiveBoost(
                    id = "mega",
                    name = "Mega Boost Paketi",
                    type = BoostType.MEGA,
                    expiryTimestamp = megaExp,
                    multiplier = 2,
                    description = "🔥 2x XP + 💰 2x Coin + 💎 2x Elmas Aktif!"
                )
            )
        }

        // 2. XP Boost
        val xpExp = prefs.getLong(KEY_XP_EXPIRY, 0L)
        if (xpExp > now) {
            list.add(
                ActiveBoost(
                    id = "xp",
                    name = "XP Boost",
                    type = BoostType.XP,
                    expiryTimestamp = xpExp,
                    multiplier = 2,
                    description = "⚡ Tüm Quiz ve Readinglerden 2 Kat XP"
                )
            )
        }

        // 3. Coin Boost
        val coinExp = prefs.getLong(KEY_COIN_EXPIRY, 0L)
        if (coinExp > now) {
            list.add(
                ActiveBoost(
                    id = "coin",
                    name = "Coin Boost",
                    type = BoostType.COIN,
                    expiryTimestamp = coinExp,
                    multiplier = 2,
                    description = "🪙 Tüm İçeriklerden 2 Kat Altın (Coin)"
                )
            )
        }

        // 4. Streak Shield
        val shieldExp = prefs.getLong(KEY_SHIELD_EXPIRY, 0L)
        if (shieldExp > now) {
            val tier = prefs.getString(KEY_SHIELD_TIER, if (prefs.getBoolean(KEY_SHIELD_IS_PLUS, false)) "plus" else "regular") ?: "regular"
            val (shieldName, shieldType, shieldDesc) = when (tier) {
                "prime" -> Triple("Streak Kalkanı Prime", BoostType.STREAK_SHIELD_PRIME, "🛡️ 30 Günlük Seri Kalkanı devrede")
                "plus" -> Triple("Streak Kalkanı Plus", BoostType.STREAK_SHIELD_PLUS, "🛡️ 7 Günlük Seri Kalkanı devrede")
                else -> Triple("Streak Kalkanı", BoostType.STREAK_SHIELD, "🛡️ 24 Saatlik Seri Kalkanı devrede")
            }
            list.add(
                ActiveBoost(
                    id = "shield_$tier",
                    name = shieldName,
                    type = shieldType,
                    expiryTimestamp = shieldExp,
                    multiplier = 1,
                    description = shieldDesc
                )
            )
        }

        // Legacy fallback if newer keys aren't set
        if (list.isEmpty()) {
            val legacyExp = prefs.getLong(KEY_LEGACY_EXPIRY, 0L)
            if (legacyExp > now) {
                val name = prefs.getString(KEY_LEGACY_NAME, "Boost") ?: "Boost"
                val typeStr = prefs.getString(KEY_LEGACY_TYPE, BoostType.XP.name) ?: BoostType.XP.name
                val type = try { BoostType.valueOf(typeStr) } catch (e: Exception) { BoostType.XP }
                list.add(ActiveBoost("legacy", name, type, legacyExp, 2, "Aktif güçlendirici"))
            }
        }

        return list
    }

    /**
     * Primary active boost for compatibility with single-boost callers.
     */
    fun getActiveBoost(context: Context): ActiveBoost? {
        val list = getActiveBoosts(context)
        return list.firstOrNull()
    }

    fun isMegaBoostActive(context: Context): Boolean {
        val now = System.currentTimeMillis()
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_MEGA_EXPIRY, 0L) > now
    }

    fun isXpBoostActive(context: Context): Boolean {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_MEGA_EXPIRY, 0L) > now || prefs.getLong(KEY_XP_EXPIRY, 0L) > now
    }

    fun isCoinBoostActive(context: Context): Boolean {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_MEGA_EXPIRY, 0L) > now || prefs.getLong(KEY_COIN_EXPIRY, 0L) > now
    }

    fun isStreakShieldActive(context: Context): Boolean {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_MEGA_EXPIRY, 0L) > now || prefs.getLong(KEY_SHIELD_EXPIRY, 0L) > now
    }

    fun isStreakShieldPlusActive(context: Context): Boolean {
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_SHIELD_EXPIRY, 0L) > now && prefs.getBoolean(KEY_SHIELD_IS_PLUS, false)
    }

    fun getShieldExpiryTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return maxOf(
            prefs.getLong(KEY_SHIELD_EXPIRY, 0L),
            prefs.getLong(KEY_MEGA_EXPIRY, 0L)
        )
    }

    fun getRemainingSeconds(context: Context): Long {
        val boost = getActiveBoost(context) ?: return 0L
        val diff = boost.expiryTimestamp - System.currentTimeMillis()
        return if (diff > 0) diff / 1000L else 0L
    }

    fun formatRemainingTime(seconds: Long): String {
        if (seconds <= 0) return "00:00"
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return when {
            days > 0 -> "${days}g ${hours}s ${mins}dk"
            hours > 0 -> String.format("%02d:%02d:%02d", hours, mins, secs)
            else -> String.format("%02d:%02d", mins, secs)
        }
    }

    fun getXpMultiplier(context: Context): Int {
        checkAndProcessQueue(context)
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getLong(KEY_MEGA_EXPIRY, 0L) > now || prefs.getLong(KEY_XP_EXPIRY, 0L) > now) 2 else 1
    }

    fun getCoinMultiplier(context: Context): Int {
        checkAndProcessQueue(context)
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getLong(KEY_MEGA_EXPIRY, 0L) > now || prefs.getLong(KEY_COIN_EXPIRY, 0L) > now) 2 else 1
    }

    fun getGemMultiplier(context: Context): Int {
        checkAndProcessQueue(context)
        val now = System.currentTimeMillis()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.getLong(KEY_MEGA_EXPIRY, 0L) > now) 2 else 1
    }

    fun clearAllBoosts(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        stateRevision++
    }
}
