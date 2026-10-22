package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import java.util.UUID

object UserProfileManager {
    private const val PREFS_NAME = "mew_user_prefs"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_FIRST_NAME = "first_name"
    private const val KEY_LAST_NAME = "last_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_AGE = "user_age"
    private const val KEY_IS_REGISTERED = "is_registered"
    private const val KEY_PREMIUM_PLAN = "premium_plan"

    // Canlı UI recomposition için state revision
    var stateRevision by mutableIntStateOf(0)
        private set

    fun notifyStateChanged() {
        stateRevision++
    }

    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var userId = prefs.getString(KEY_USER_ID, null)
        if (userId.isNullOrBlank()) {
            userId = "mew_" + UUID.randomUUID().toString().replace("-", "").take(12)
            prefs.edit().putString(KEY_USER_ID, userId).apply()
        }
        return userId
    }

    fun isRegistered(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_REGISTERED, false)
    }

    fun getUserFirstName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FIRST_NAME, "") ?: ""
    }

    fun getUserLastName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_NAME, "") ?: ""
    }

    fun getUserEmail(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_EMAIL, "") ?: ""
    }

    fun getUserFullName(context: Context): String {
        val fName = getUserFirstName(context).trim()
        val lName = getUserLastName(context).trim()
        val full = "$fName $lName".trim()
        return if (full.isNotBlank()) full else "Mew Öğrencisi"
    }

    fun getUserAge(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_AGE, 18)
    }

    private const val KEY_IS_DEMO_MODE = "is_demo_mode"

    fun isDemoMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_DEMO_MODE, false)
    }

    fun setDemoMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_DEMO_MODE, enabled).apply()
        notifyStateChanged()
    }

    fun getPremiumPlan(context: Context): String {
        val securePrefs = EncryptedPreferencesManager.getSecurePrefs(context, PREFS_NAME)
        val plainPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fallback = plainPrefs.getString(KEY_PREMIUM_PLAN, "freemium") ?: "freemium"
        return EncryptedPreferencesManager.getSecureString(securePrefs, KEY_PREMIUM_PLAN, fallback)
    }

    fun setPremiumPlan(context: Context, plan: String) {
        val securePrefs = EncryptedPreferencesManager.getSecurePrefs(context, PREFS_NAME)
        EncryptedPreferencesManager.saveSecureString(securePrefs, KEY_PREMIUM_PLAN, plan)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PREMIUM_PLAN, plan).apply()
        notifyStateChanged()
    }

    fun cyclePremiumPlan(context: Context): String {
        val current = getPremiumPlan(context).lowercase()
        val nextPlan = when (current) {
            "freemium", "free" -> "premium"
            "premium" -> "premium_plus"
            else -> "freemium"
        }
        setPremiumPlan(context, nextPlan)
        return nextPlan
    }

    fun isPremium(context: Context): Boolean {
        val plan = getPremiumPlan(context).lowercase()
        return plan != "freemium" && plan != "free" && plan.isNotBlank()
    }

    fun isPremiumPlus(context: Context): Boolean {
        val plan = getPremiumPlan(context).lowercase()
        return plan == "premium_plus" || plan == "mew_premium_plus_monthly"
    }

    fun isFamilyPlan(context: Context): Boolean {
        val plan = getPremiumPlan(context).lowercase()
        return plan == "family_plan" || plan == "mew_family_plan_monthly"
    }

    fun isAnonymous(context: Context): Boolean {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val email = getUserEmail(context).trim()
        val isAuthAnon = currentUser == null || currentUser.isAnonymous
        return isAuthAnon || email.isBlank() || email.startsWith("no_name_user")
    }

    fun setRegisteredFlag(context: Context, isRegistered: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_REGISTERED, isRegistered).apply()
        notifyStateChanged()
    }

    fun saveLocalProfile(
        context: Context,
        name: String,
        surname: String,
        email: String? = null,
        age: Int,
        premiumPlan: String? = null
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
            .putString(KEY_FIRST_NAME, name.trim())
            .putString(KEY_LAST_NAME, surname.trim())
            .putInt(KEY_AGE, age)
        if (!email.isNullOrBlank()) {
            editor.putString(KEY_EMAIL, email.trim())
        }
        if (premiumPlan != null) {
            editor.putString(KEY_PREMIUM_PLAN, premiumPlan)
            val sec = EncryptedPreferencesManager.getSecurePrefs(context, PREFS_NAME)
            EncryptedPreferencesManager.saveSecureString(sec, KEY_PREMIUM_PLAN, premiumPlan)
        }
        editor.apply()

        if (UserCosmeticsStore.getManualRank(context) == null) {
            UserCosmeticsStore.setManualRank(context, RankType.BRONZE)
            UserCosmeticsStore.setEquippedWingId(context, "bronze_kanat")
        }

        notifyStateChanged()
    }

    /**
     * Admin panelinden veya Firestore'dan geri yüklenen profili kaydeder.
     */
    fun saveRestoredProfile(
        context: Context,
        userId: String,
        name: String,
        surname: String,
        email: String,
        age: Int,
        premiumPlan: String? = "freemium"
    ) {
        val plan = premiumPlan ?: "freemium"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_FIRST_NAME, name.trim())
            .putString(KEY_LAST_NAME, surname.trim())
            .putString(KEY_EMAIL, email.trim())
            .putInt(KEY_AGE, age)
            .putBoolean(KEY_IS_REGISTERED, true)
            .putString(KEY_PREMIUM_PLAN, plan)
            .apply()

        val sec = EncryptedPreferencesManager.getSecurePrefs(context, PREFS_NAME)
        EncryptedPreferencesManager.saveSecureString(sec, KEY_PREMIUM_PLAN, plan)
        notifyStateChanged()
    }

    /**
     * Manuel form doldurarak kayıt olma metodu (E-posta zorunlu)
     */
    fun registerUser(
        context: Context,
        name: String,
        surname: String,
        email: String,
        age: Int,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = getUserId(context)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_FIRST_NAME, name.trim())
            .putString(KEY_LAST_NAME, surname.trim())
            .putString(KEY_EMAIL, email.trim())
            .putInt(KEY_AGE, age)
            .putBoolean(KEY_IS_REGISTERED, true)
            .putString(KEY_PREMIUM_PLAN, "freemium")
            .apply()

        // Yeni hesap direkt en düşük rank olan Bronz rankından ve 0 Coin/Elmas/XP ile başlar
        initializeZeroState(context)

        notifyStateChanged()

        FirestoreSyncManager.pushInitialProfile(
            context = context,
            userId = userId,
            name = name.trim(),
            surname = surname.trim(),
            email = email.trim(),
            age = age
        ) { success, errorMsg ->
            if (success) {
                FirestoreSyncManager.startRealtimeListener(context)
            }
            onComplete(success, errorMsg)
        }
    }

    /**
     * Yeni kullanıcılar için sıfır durumu (Zero-State Initialization):
     * Coin = 0, Elmas = 0, XP = 0, Rank = "Bronze" (Bronz).
     */
    fun initializeZeroState(context: Context) {
        QuizTreeProgressStore.setCoins(context, 0)
        QuizTreeProgressStore.setGems(context, 0)
        QuizTreeProgressStore.setXp(context, 0)
        UserCosmeticsStore.setManualRank(context, RankType.BRONZE)
        UserCosmeticsStore.setEquippedWingId(context, "bronze_kanat")
        notifyStateChanged()
    }

    /**
     * Cihazda kayıtlı Google hesabını veya aktif e-posta adresini güvenli bir şekilde algılar.
     * GoogleSignIn -> FirebaseAuth -> UserProfileManager -> AccountManager (safe try-catch).
     */
    fun getAutoDetectedEmail(context: Context): String {
        // 1. GoogleSignIn son oturum açılmış hesap
        try {
            val lastGAccount = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)?.email
            if (!lastGAccount.isNullOrBlank() && lastGAccount.contains("@")) {
                return lastGAccount.trim()
            }
        } catch (_: Exception) {}

        // 2. Firebase Auth mevcut e-postası
        try {
            val authEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
            if (!authEmail.isNullOrBlank() && authEmail.contains("@")) {
                return authEmail.trim()
            }
        } catch (_: Exception) {}

        // 3. UserProfileManager yerel kayıtlı e-posta
        val localEmail = getUserEmail(context).trim()
        if (localEmail.isNotBlank() && !localEmail.startsWith("no_name_user") && localEmail.contains("@")) {
            return localEmail
        }

        // 4. Android AccountManager (SecurityException güvenli yakalama)
        try {
            val accountManager = android.accounts.AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            if (accounts.isNotEmpty()) {
                val acc = accounts.firstOrNull { it.name.contains("@") }?.name
                if (!acc.isNullOrBlank()) {
                    return acc.trim()
                }
            }
        } catch (_: SecurityException) {
            // İzin verilmemişse sessizce devam et
        } catch (_: Exception) {}

        return ""
    }

    /**
     * Google One Tap veya Google ile Giriş Yap metodu
     */
    fun signInWithGoogleAccount(
        context: Context,
        googleId: String,
        name: String,
        surname: String,
        email: String,
        age: Int = 18,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val userId = "google_$googleId"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_FIRST_NAME, name.trim())
            .putString(KEY_LAST_NAME, surname.trim())
            .putString(KEY_EMAIL, email.trim())
            .putInt(KEY_AGE, age)
            .putBoolean(KEY_IS_REGISTERED, true)
            .putString(KEY_PREMIUM_PLAN, "freemium")
            .apply()

        notifyStateChanged()

        // Firestore'da bu Google kullanıcısı zaten var mı kontrol et, varsa mevcut verilerini kurtar
        FirestoreSyncManager.checkAndRestoreOrSaveProfile(
            context = context,
            userId = userId,
            name = name.trim(),
            surname = surname.trim(),
            email = email.trim(),
            age = age
        ) { success, errorMsg ->
            if (success) {
                FirestoreSyncManager.startRealtimeListener(context)
            }
            onComplete(success, errorMsg)
        }
    }

    /**
     * Kullanıcının yerel tüm verilerini ve önbelleğini temizler.
     */
    fun clearAllUserData(context: Context) {
        val userPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        userPrefs.edit().clear().apply()

        val knownPrefs = listOf(
            "quiz_tree_progress",
            "quiz_tree_progress_secure_vault",
            "mew_user_prefs",
            "mew_user_prefs_secure_vault",
            "quiz_tree_prefs",
            "user_cosmetics_prefs",
            "mistake_jar_prefs",
            "saved_words_prefs",
            "energy_prefs",
            "streak_prefs",
            "market_boost_prefs",
            "mew_security_prefs",
            "mew_chat_rate_limiter"
        )
        for (pref in knownPrefs) {
            try {
                context.getSharedPreferences(pref, Context.MODE_PRIVATE).edit().clear().apply()
            } catch (_: Exception) {}
            try {
                EncryptedPreferencesManager.getSecurePrefs(context, pref).edit().clear().apply()
            } catch (_: Exception) {}
        }
        notifyStateChanged()
    }
}
