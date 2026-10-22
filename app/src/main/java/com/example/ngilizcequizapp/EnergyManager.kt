package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * MewEnglish Cartoon Enerji & Can Sistemi
 * - Maksimum 5 enerji
 * - Her 10 dakikada 1 enerji yenilenir
 * - Quiz ve Okuma parçalarına girişte 1 enerji harcanır
 */
object EnergyManager {
    const val MAX_ENERGY = 5
    const val REGEN_INTERVAL_MINUTES = 30
    const val REGEN_INTERVAL_MS = REGEN_INTERVAL_MINUTES * 60 * 1000L

    private const val PREFS_NAME = "mew_energy_prefs"
    private const val KEY_CURRENT_ENERGY = "current_energy"
    private const val KEY_LAST_REGEN_TIMESTAMP = "last_regen_timestamp"

    // Canlı Compose recomposition için state revision
    var stateRevision by mutableIntStateOf(0)
        private set

    fun notifyStateChanged() {
        stateRevision++
    }

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Zaman farkına göre enerjiyi günceller ve kaydeder.
     */
    fun syncEnergy(context: Context) {
        val prefs = getPrefs(context)
        val now = System.currentTimeMillis()
        var lastTimestamp = prefs.getLong(KEY_LAST_REGEN_TIMESTAMP, 0L)
        val currentEnergy = prefs.getInt(KEY_CURRENT_ENERGY, -1)

        // İlk kurulum
        if (currentEnergy == -1 || lastTimestamp == 0L) {
            prefs.edit()
                .putInt(KEY_CURRENT_ENERGY, MAX_ENERGY)
                .putLong(KEY_LAST_REGEN_TIMESTAMP, now)
                .apply()
            notifyStateChanged()
            return
        }

        if (currentEnergy >= MAX_ENERGY) {
            // Enerji zaten doluysa sayacı şimdiki zamana resetle
            if (lastTimestamp != now) {
                prefs.edit().putLong(KEY_LAST_REGEN_TIMESTAMP, now).apply()
            }
            return
        }

        val elapsed = now - lastTimestamp
        if (elapsed >= REGEN_INTERVAL_MS) {
            val pointsToAdd = (elapsed / REGEN_INTERVAL_MS).toInt()
            val newEnergy = minOf(MAX_ENERGY, currentEnergy + pointsToAdd)
            val newLastTimestamp = if (newEnergy >= MAX_ENERGY) {
                now
            } else {
                lastTimestamp + (pointsToAdd * REGEN_INTERVAL_MS)
            }

            prefs.edit()
                .putInt(KEY_CURRENT_ENERGY, newEnergy)
                .putLong(KEY_LAST_REGEN_TIMESTAMP, newLastTimestamp)
                .apply()

            notifyStateChanged()
        }
    }

    /**
     * Premium, Premium Plus, Aile Planı veya Demo Modundaki kullanıcılar için sınırsız enerji durumu.
     */
    fun isUnlimitedEnergy(context: Context): Boolean {
        return UserProfileManager.isPremium(context) ||
               UserProfileManager.isPremiumPlus(context) ||
               UserProfileManager.isFamilyPlan(context) ||
               UserProfileManager.isDemoMode(context)
    }

    /**
     * 2X XP çarpanı.
     * Premium Plus ve Aile Planı abonelikleri boyunca kalıcı 2X, Premium için 1 haftalık MarketBoostStore boost'u.
     */
    fun getXpMultiplier(context: Context): Int {
        if (UserProfileManager.isPremiumPlus(context) || UserProfileManager.isFamilyPlan(context)) {
            return 2
        }
        return navigation.MarketBoostStore.getXpMultiplier(context)
    }

    /**
     * 2X Coin çarpanı.
     * Premium Plus ve Aile Planı abonelikleri boyunca kalıcı 2X, Premium için 1 haftalık MarketBoostStore boost'u.
     */
    fun getCoinMultiplier(context: Context): Int {
        if (UserProfileManager.isPremiumPlus(context) || UserProfileManager.isFamilyPlan(context)) {
            return 2
        }
        return navigation.MarketBoostStore.getCoinMultiplier(context)
    }

    /**
     * Güncel enerjiyi döndürür (otomatik sync yapar).
     */
    fun getEnergy(context: Context): Int {
        if (isUnlimitedEnergy(context)) return MAX_ENERGY
        syncEnergy(context)
        return getPrefs(context).getInt(KEY_CURRENT_ENERGY, MAX_ENERGY)
    }

    /**
     * Kullanıcının en az 1 enerjisi olup olmadığını kontrol eder.
     */
    fun hasEnergy(context: Context): Boolean {
        if (isUnlimitedEnergy(context)) return true
        return getEnergy(context) > 0
    }

    /**
     * Quiz veya Okuma girişi için 1 enerji tüketir.
     * Başarılıysa true, enerji yoksa false döner.
     */
    fun useEnergy(context: Context, amount: Int = 1): Boolean {
        if (isUnlimitedEnergy(context)) return true
        syncEnergy(context)
        val prefs = getPrefs(context)
        val current = prefs.getInt(KEY_CURRENT_ENERGY, MAX_ENERGY)

        if (current < amount) {
            return false
        }

        val now = System.currentTimeMillis()
        val newEnergy = current - amount
        val editor = prefs.edit().putInt(KEY_CURRENT_ENERGY, newEnergy)

        // Eğer enerji tam doluyken ilk kez eksildiyse, sayacı tam şu andan başlatıyoruz
        if (current >= MAX_ENERGY) {
            editor.putLong(KEY_LAST_REGEN_TIMESTAMP, now)
        }

        editor.apply()
        notifyStateChanged()
        return true
    }

    /**
     * Ödül veya bonus enerji ekler.
     */
    fun addEnergy(context: Context, amount: Int = 1) {
        syncEnergy(context)
        val prefs = getPrefs(context)
        val current = prefs.getInt(KEY_CURRENT_ENERGY, MAX_ENERGY)
        val newEnergy = minOf(MAX_ENERGY, current + amount)
        val editor = prefs.edit().putInt(KEY_CURRENT_ENERGY, newEnergy)

        if (newEnergy >= MAX_ENERGY) {
            editor.putLong(KEY_LAST_REGEN_TIMESTAMP, System.currentTimeMillis())
        }

        editor.apply()
        notifyStateChanged()
    }

    /**
     * Enerjiyi tamamen 5'e doldurur (Test/Demo/Bonus).
     */
    fun refillFull(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putInt(KEY_CURRENT_ENERGY, MAX_ENERGY)
            .putLong(KEY_LAST_REGEN_TIMESTAMP, System.currentTimeMillis())
            .apply()
        notifyStateChanged()
    }

    /**
     * Bir sonraki enerjinin dolmasına kalan süreyi saniye cinsinden döner.
     */
    fun getRemainingSecondsToNextEnergy(context: Context): Long {
        syncEnergy(context)
        val prefs = getPrefs(context)
        val current = prefs.getInt(KEY_CURRENT_ENERGY, MAX_ENERGY)
        if (current >= MAX_ENERGY) return 0L

        val lastTimestamp = prefs.getLong(KEY_LAST_REGEN_TIMESTAMP, System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val elapsed = now - lastTimestamp
        val remainingMs = REGEN_INTERVAL_MS - (elapsed % REGEN_INTERVAL_MS)
        return (remainingMs / 1000L).coerceAtLeast(0L)
    }

    /**
     * Yenilenme periyodunun ilerleme oranını 0f..1f arasında döner (Progress bar için).
     */
    fun getRegenProgress(context: Context): Float {
        syncEnergy(context)
        val current = getEnergy(context)
        if (current >= MAX_ENERGY) return 1f

        val prefs = getPrefs(context)
        val lastTimestamp = prefs.getLong(KEY_LAST_REGEN_TIMESTAMP, System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val elapsed = (now - lastTimestamp).coerceAtLeast(0L)
        val cycleElapsed = elapsed % REGEN_INTERVAL_MS
        return (cycleElapsed.toFloat() / REGEN_INTERVAL_MS.toFloat()).coerceIn(0f, 1f)
    }

    /**
     * Saniyeyi "MM:SS" biçiminde formatlar.
     */
    fun formatCountdown(seconds: Long): String {
        if (seconds <= 0L) return "00:00"
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }
}
