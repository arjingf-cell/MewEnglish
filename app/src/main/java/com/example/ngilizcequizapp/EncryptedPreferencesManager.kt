package com.example.ngilizcequizapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Yerel Veri Şifreleme ve Bütünlük Denetimi (Anti-Tamper & Security Vault)
 * - AndroidX Security-Crypto (MasterKey + AES-256 GCM)
 * - HMAC-SHA256 Bütünlük ve Manipülasyon Kontrolü (Root / Hex Editor Koruması)
 */
object EncryptedPreferencesManager {
    private const val TAG = "EncryptedPreferences"
    private const val INTEGRITY_SEED = "MewEnglish_EncryptedVault_2026_AntiCheat_SigKey!"

    /**
     * Cihaz donanım Keystore destekliyorsa AES-256 GCM şifreli SharedPreferences örneği üretir.
     * Donanım arızası veya Keystore istisnalarında standart prefs'e güvenli geri dönüş yapar.
     */
    fun getSecurePrefs(context: Context, prefsName: String): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context.applicationContext,
                "${prefsName}_secure_vault",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        }
    }

    /**
     * Değer ve anahtar için HMAC-SHA256 dijital imzası üretir.
     */
    fun computeHmac(key: String, value: String): String {
        return try {
            val hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(INTEGRITY_SEED.toByteArray(Charsets.UTF_8), "HmacSHA256")
            hmac.init(secretKey)
            val signedBytes = hmac.doFinal("$key:$value".toByteArray(Charsets.UTF_8))
            Base64.encodeToString(signedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest("$key:$value:$INTEGRITY_SEED".toByteArray(Charsets.UTF_8))
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }

    /**
     * Sayısal değeri ve dijital imzasını güvenli bir şekilde saklar.
     */
    fun saveSecureInt(prefs: SharedPreferences, key: String, value: Int) {
        val sig = computeHmac(key, value.toString())
        prefs.edit()
            .putInt(key, value)
            .putString("_sig_$key", sig)
            .apply()
    }

    /**
     * Sayısal değeri doğrular; eğer dosya harici bir araçla manipüle edilmişse
     * imza uyuşmazlığı tespit edilir ve değer sıfırlanarak hile engellenir.
     */
    fun getSecureInt(prefs: SharedPreferences, key: String, defaultValue: Int): Int {
        if (!prefs.contains(key)) return defaultValue
        val value = prefs.getInt(key, defaultValue)
        val storedSig = prefs.getString("_sig_$key", null) ?: return value
        val expectedSig = computeHmac(key, value.toString())

        if (storedSig != expectedSig) {
            android.util.Log.w(TAG, "Güvenlik İhlali Tespit Edildi: $key manipüle edilmiş! Hile koruması devrede.")
            saveSecureInt(prefs, key, defaultValue)
            return defaultValue
        }
        return value
    }

    /**
     * Metin değerini ve dijital imzasını saklar.
     */
    fun saveSecureString(prefs: SharedPreferences, key: String, value: String) {
        val sig = computeHmac(key, value)
        prefs.edit()
            .putString(key, value)
            .putString("_sig_$key", sig)
            .apply()
    }

    /**
     * Metin değerini (örn: premium_plan) doğrular. Manipülasyon varsa varsayılan değere döndürür.
     */
    fun getSecureString(prefs: SharedPreferences, key: String, defaultValue: String): String {
        if (!prefs.contains(key)) return defaultValue
        val value = prefs.getString(key, defaultValue) ?: defaultValue
        val storedSig = prefs.getString("_sig_$key", null) ?: return value
        val expectedSig = computeHmac(key, value)

        if (storedSig != expectedSig) {
            android.util.Log.w(TAG, "Güvenlik İhlali Tespit Edildi: $key manipüle edilmiş! Varsayılana dönülüyor.")
            saveSecureString(prefs, key, defaultValue)
            return defaultValue
        }
        return value
    }
}
