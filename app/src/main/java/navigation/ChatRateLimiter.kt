package com.example.ngilizcequizapp

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Yapay Zeka (Gemini) İstek Sınırlandırma (Rate Limiter & Anti-Spam Kalkanı)
 * 1. Cooldown / Debounce: Art arda hızlı spam mesaj gönderilmesini engeller (Minimum 3.5 saniye bekleme).
 * 2. Günlük Makul Kullanım Kotası (Fair-Usage Limit): Ücretsiz kullanıcılar için 60, Premium için 300 mesaj/gün.
 */
object ChatRateLimiter {
    private const val PREFS_NAME = "mew_chat_rate_limiter"
    private const val KEY_LAST_SENT_TIMESTAMP = "last_sent_timestamp"
    private const val KEY_QUOTA_DATE = "quota_date"
    private const val KEY_QUOTA_COUNT = "quota_count"

    const val COOLDOWN_SECONDS = 3.5f
    const val COOLDOWN_MILLIS = 3500L
    const val DAILY_LIMIT_FREE = 60
    const val DAILY_LIMIT_PREMIUM = 300

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    /**
     * Kullanıcının yeni bir mesaj gönderip gönderemeyeceğini kontrol eder.
     * @return Pair(İzin verildi mi?, Hata / Uyarı Mesajı)
     */
    fun checkCanSend(context: Context, isPremium: Boolean): Pair<Boolean, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        // 1. Cooldown / Debounce Kontrolü
        val lastSent = prefs.getLong(KEY_LAST_SENT_TIMESTAMP, 0L)
        val elapsed = now - lastSent
        if (elapsed < COOLDOWN_MILLIS) {
            val remainingSec = String.format(Locale.US, "%.1f", (COOLDOWN_MILLIS - elapsed) / 1000.0)
            return Pair(
                false,
                "Lütfen biraz yavaşlayın! Robot Mew'e yeni mesaj göndermeden önce ${remainingSec} sn bekleyin 🐾"
            )
        }

        // 2. Günlük Kota (Fair-Use) Kontrolü
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_QUOTA_DATE, "")
        val currentCount = if (savedDate == today) prefs.getInt(KEY_QUOTA_COUNT, 0) else 0

        val maxQuota = if (isPremium) DAILY_LIMIT_PREMIUM else DAILY_LIMIT_FREE
        if (currentCount >= maxQuota) {
            return Pair(
                false,
                "Günlük yapay zeka mesaj kotanıza ($maxQuota mesaj) ulaştınız. Kotanız yarın sıfırlanacaktır 🌙"
            )
        }

        return Pair(true, "")
    }

    /**
     * Mesaj başarıyla gönderildiğinde zaman damgasını ve günlük sayacı günceller.
     */
    fun onMessageSent(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_QUOTA_DATE, "")
        val currentCount = if (savedDate == today) prefs.getInt(KEY_QUOTA_COUNT, 0) else 0

        prefs.edit()
            .putLong(KEY_LAST_SENT_TIMESTAMP, now)
            .putString(KEY_QUOTA_DATE, today)
            .putInt(KEY_QUOTA_COUNT, currentCount + 1)
            .apply()
    }

    /**
     * Günlük kalan mesaj hakkını döndürür.
     */
    fun getRemainingQuota(context: Context, isPremium: Boolean): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getTodayDateString()
        val savedDate = prefs.getString(KEY_QUOTA_DATE, "")
        val currentCount = if (savedDate == today) prefs.getInt(KEY_QUOTA_COUNT, 0) else 0
        val maxQuota = if (isPremium) DAILY_LIMIT_PREMIUM else DAILY_LIMIT_FREE
        return (maxQuota - currentCount).coerceAtLeast(0)
    }
}
