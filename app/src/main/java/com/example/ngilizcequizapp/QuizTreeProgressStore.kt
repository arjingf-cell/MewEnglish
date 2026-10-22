package com.example.ngilizcequizapp

import android.content.Context

object QuizTreeProgressStore {
    private const val PREFS_NAME = "quiz_tree_progress"
    private const val KEY_PREFIX = "quiz_attempt_"
    private const val KEY_COINS = "coins"
    private const val KEY_GEMS = "gems"
    private const val KEY_XP = "xp"
    private const val KEY_UNLOCKED_THROUGH = "unlocked_through"
    private const val KEY_REINFORCEMENT_XP_PREFIX = "reinforcement_xp_"
    private const val DEFAULT_COINS = 0
    private const val DEFAULT_GEMS = 0

    data class Result(
        val correctAnswers: Int,
        val totalQuestions: Int,
        val redStars: Int
    )

    data class AttemptOutcome(
        val result: Result,
        val earnedCoins: Int,
        val earnedGems: Int,
        val earnedXp: Int,
        val isReinforcement: Boolean,
        val baseCoins: Int = earnedCoins,
        val baseXp: Int = earnedXp,
        val baseGems: Int = earnedGems,
        val xpMultiplier: Int = 1,
        val coinMultiplier: Int = 1,
        val gemMultiplier: Int = 1
    )

    fun starsFor(result: Result?): Int {
        if (result == null) return 0
        // Atlanan veya '0/0/0' formatında kaydedilmiş quizler çözülmüş/atlandı sayılır (en az 1 yıldız)
        if (result.totalQuestions <= 0) return if (result.redStars > 0) result.redStars else 1
        val ratio = result.correctAnswers.toFloat() / result.totalQuestions.toFloat()
        return when {
            ratio >= 1.0f -> 3
            ratio >= 0.8f -> 2
            ratio >= 0.7f -> 1
            else -> 0
        }
    }

    private var isMigrated = false
    private fun getPrefs(context: Context): android.content.SharedPreferences {
        val securePrefs = EncryptedPreferencesManager.getSecurePrefs(context, PREFS_NAME)
        if (!isMigrated) {
            isMigrated = true
            try {
                val oldPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                if (oldPrefs != securePrefs && oldPrefs.contains(KEY_COINS) && !securePrefs.contains(KEY_COINS)) {
                    val oldCoins = oldPrefs.getInt(KEY_COINS, 0)
                    val oldGems = oldPrefs.getInt(KEY_GEMS, 0)
                    val oldXp = oldPrefs.getInt(KEY_XP, 0)
                    EncryptedPreferencesManager.saveSecureInt(securePrefs, KEY_COINS, oldCoins)
                    EncryptedPreferencesManager.saveSecureInt(securePrefs, KEY_GEMS, oldGems)
                    EncryptedPreferencesManager.saveSecureInt(securePrefs, KEY_XP, oldXp)
                }
            } catch (_: Exception) {}
        }
        return securePrefs
    }

    fun getXp(context: Context): Int {
        return EncryptedPreferencesManager.getSecureInt(getPrefs(context), KEY_XP, 0)
    }

    fun setXp(context: Context, xp: Int) {
        val safeXp = Math.max(0, xp)
        EncryptedPreferencesManager.saveSecureInt(getPrefs(context), KEY_XP, safeXp)
    }

    fun addXp(context: Context, xp: Int) {
        if (xp <= 0) return
        setXp(context, getXp(context) + xp)
        FirestoreSyncManager.syncProgress(context)
    }

    fun getCoins(context: Context): Int {
        return EncryptedPreferencesManager.getSecureInt(getPrefs(context), KEY_COINS, 0)
    }

    fun setCoins(context: Context, coins: Int) {
        val safeCoins = Math.max(0, coins)
        EncryptedPreferencesManager.saveSecureInt(getPrefs(context), KEY_COINS, safeCoins)
    }

    fun addCoins(context: Context, coins: Int) {
        val current = getCoins(context)
        val newCoins = Math.max(0, current + coins)
        setCoins(context, newCoins)
        FirestoreSyncManager.syncProgress(context)
    }

    fun getGems(context: Context): Int {
        return EncryptedPreferencesManager.getSecureInt(getPrefs(context), KEY_GEMS, 0)
    }

    fun setGems(context: Context, gems: Int) {
        val safeGems = Math.max(0, gems)
        EncryptedPreferencesManager.saveSecureInt(getPrefs(context), KEY_GEMS, safeGems)
    }

    fun addGems(context: Context, gems: Int) {
        val current = getGems(context)
        val newGems = Math.max(0, current + gems)
        setGems(context, newGems)
        FirestoreSyncManager.syncProgress(context)
    }

    fun getUnlockedThrough(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_UNLOCKED_THROUGH, 0)
    }

    fun unlockThrough(context: Context, quizIndex: Int) {
        val current = getUnlockedThrough(context)
        val target = Math.max(current, quizIndex)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_UNLOCKED_THROUGH, target)
            .apply()
    }

    fun hasReinforcementXp(context: Context, quizIndex: Int): Boolean {
        if (quizIndex < 0) return false
        val key = "$KEY_REINFORCEMENT_XP_PREFIX$quizIndex"
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(key, false)
    }

    fun calculateOutcome(
        context: Context,
        previousResult: Result?,
        correctAnswers: Int,
        totalQuestions: Int,
        quizIndex: Int,
        isReinforcement: Boolean
    ): AttemptOutcome {
        val errors = maxOf(0, totalQuestions - correctAnswers)
        val stars = when {
            totalQuestions <= 0 -> 0
            errors == 0 -> 3
            errors == 1 -> 2
            errors == 2 -> 1
            else -> 0
        }
        val result = Result(correctAnswers, totalQuestions, stars)

        val prevStars = starsFor(previousResult)
        val starDiff = Math.max(0, stars - prevStars)
        
        // Mavi yıldız pekiştirme durumu: Eğer önceden 3 sarı yıldıza (redStars) ulaşılmışsa
        val isBlueStarsReinforcement = prevStars == 3
        
        val earnedXp: Int
        val earnedCoins: Int
        val earnedGems: Int
        val isMultipleOfTen = (quizIndex + 1) % 10 == 0

        if (isBlueStarsReinforcement) {
            val blueStars = when (errors) {
                0 -> 3
                1 -> 2
                2 -> 1
                else -> 0
            }
            val prevBlueStars = getReinforcementStars(context, quizIndex)
            val newBlueStars = Math.max(0, blueStars - prevBlueStars)
            
            earnedXp = 10 // Pekiştirme quizi sabit 10 XP verir
            earnedCoins = newBlueStars * 5 // Her yeni mavi yıldız için 5 coin
            
            // 10 ve katı olan quizlerde 3 mavi yıldıza ilk kez ulaşıldığında 5 elmas verilir
            earnedGems = if (isMultipleOfTen && blueStars == 3 && prevBlueStars < 3) 5 else 0
        } else {
            earnedXp = if (isReinforcement) 10 else starDiff * 15
            earnedCoins = if (isReinforcement) 5 else starDiff * 10
            
            val reachedThreeStarsNow = (stars == 3 && prevStars < 3)
            earnedGems = if (reachedThreeStarsNow) {
                if (isMultipleOfTen) 5 else 1
            } else {
                0
            }
        }

        val xpMultiplier = navigation.MarketBoostStore.getXpMultiplier(context)
        val coinMultiplier = navigation.MarketBoostStore.getCoinMultiplier(context)
        val gemMultiplier = navigation.MarketBoostStore.getGemMultiplier(context)

        val boostedXp = earnedXp * xpMultiplier
        val boostedCoins = earnedCoins * coinMultiplier
        val boostedGems = earnedGems * gemMultiplier

        return AttemptOutcome(
            result = result,
            earnedCoins = boostedCoins,
            earnedGems = boostedGems,
            earnedXp = boostedXp,
            isReinforcement = isReinforcement || isBlueStarsReinforcement,
            baseCoins = earnedCoins,
            baseXp = earnedXp,
            baseGems = earnedGems,
            xpMultiplier = xpMultiplier,
            coinMultiplier = coinMultiplier,
            gemMultiplier = gemMultiplier
        )
    }

    fun recordAttempt(
        context: Context,
        quizIndex: Int,
        correctAnswers: Int,
        totalQuestions: Int
    ): AttemptOutcome {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val all = loadAll(context)
        val previousResult = all[quizIndex]
        
        val isReinforcement = hasReinforcementXp(context, quizIndex)
        val outcome = calculateOutcome(context, previousResult, correctAnswers, totalQuestions, quizIndex, isReinforcement)
        
        val editor = prefs.edit()
        
        val key = "$KEY_PREFIX$quizIndex"
        val value = "${outcome.result.correctAnswers}/${outcome.result.totalQuestions}/${outcome.result.redStars}"
        editor.putString(key, value)
        
        if (outcome.earnedXp > 0) {
            editor.putInt(KEY_XP, getXp(context) + outcome.earnedXp)
        }
        if (outcome.earnedCoins > 0) {
            editor.putInt(KEY_COINS, getCoins(context) + outcome.earnedCoins)
        }
        if (outcome.earnedGems > 0) {
            editor.putInt(KEY_GEMS, getGems(context) + outcome.earnedGems)
        }
        
        if (isReinforcement && outcome.earnedXp > 0) {
            editor.putBoolean("$KEY_REINFORCEMENT_XP_PREFIX$quizIndex", true)
        }
        
        editor.apply()

        // 🌟 Kümülatif Yıldız Mantığı:
        // İlk Çözüm: 0 yanlış -> 3 Sarı; 1 yanlış -> 2 Sarı; 2 yanlış -> 1 Sarı
        // 1. Pekiştirme: Mavi Yıldız kazandırır (aynı baremler)
        // 2. Pekiştirme: Kırmızı Yıldız kazandırır (aynı baremler)
        val errors = maxOf(0, totalQuestions - correctAnswers)
        val earnedStars = when (errors) {
            0 -> 3
            1 -> 2
            2 -> 1
            else -> 0
        }

        val currentYellow = getYellowStars(context, quizIndex)
        val currentBlue = getBlueStars(context, quizIndex)
        val currentRed = getRedStars(context, quizIndex)

        if (currentYellow == 0) {
            saveYellowStars(context, quizIndex, earnedStars)
        } else if (currentBlue == 0) {
            saveBlueStars(context, quizIndex, earnedStars)
            if (earnedStars > currentYellow) {
                saveYellowStars(context, quizIndex, earnedStars)
            }
        } else {
            saveRedStars(context, quizIndex, maxOf(currentRed, earnedStars))
            if (earnedStars > currentBlue) {
                saveBlueStars(context, quizIndex, earnedStars)
            }
            if (earnedStars > currentYellow) {
                saveYellowStars(context, quizIndex, earnedStars)
            }
        }

        AchievementManager.checkStarBadges(context)
        MissionManager.onQuizCompleted(context)
        return outcome
    }

    fun loadAll(context: Context): Map<Int, Result> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allEntries = prefs.all
        val results = mutableMapOf<Int, Result>()
        
        for ((key, value) in allEntries) {
            if (key.startsWith(KEY_PREFIX)) {
                val indexStr = key.substring(KEY_PREFIX.length)
                val quizIndex = indexStr.toIntOrNull()
                if (quizIndex != null && value is String) {
                    val parts = value.split("/")
                    if (parts.size >= 3) {
                        val correct = parts[0].toIntOrNull() ?: 0
                        val total = parts[1].toIntOrNull() ?: 0
                        val stars = parts[2].toIntOrNull() ?: 0
                        results[quizIndex] = Result(correct, total, stars)
                    }
                }
            }
        }
        return results
    }

    fun getSolvedQuizzesCount(context: Context): Int {
        return loadAll(context).count { it.value.redStars > 0 || it.value.correctAnswers > 0 }
    }

    fun lastSolvedIndex(results: Map<Int, Result>): Int {
        var idx = 0
        while (results.containsKey(idx)) {
            idx++
        }
        return idx - 1
    }

    fun getYellowStars(context: Context, quizIndex: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains("yellow_stars_$quizIndex")) {
            return prefs.getInt("yellow_stars_$quizIndex", 0)
        }
        val res = loadAll(context)[quizIndex]
        return starsFor(res)
    }

    fun saveYellowStars(context: Context, quizIndex: Int, stars: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cur = getYellowStars(context, quizIndex)
        if (stars > cur) {
            prefs.edit().putInt("yellow_stars_$quizIndex", stars.coerceIn(0, 3)).apply()
        }
    }

    fun getBlueStars(context: Context, quizIndex: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains("blue_stars_$quizIndex")) {
            return prefs.getInt("blue_stars_$quizIndex", 0)
        }
        return prefs.getInt("reinforcement_stars_$quizIndex", 0)
    }

    fun saveBlueStars(context: Context, quizIndex: Int, stars: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cur = getBlueStars(context, quizIndex)
        if (stars > cur) {
            prefs.edit()
                .putInt("blue_stars_$quizIndex", stars.coerceIn(0, 3))
                .putInt("reinforcement_stars_$quizIndex", stars.coerceIn(0, 3))
                .apply()
        }
    }

    fun getRedStars(context: Context, quizIndex: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("red_stars_$quizIndex", 0)
    }

    fun saveRedStars(context: Context, quizIndex: Int, stars: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cur = getRedStars(context, quizIndex)
        if (stars > cur) {
            prefs.edit().putInt("red_stars_$quizIndex", stars.coerceIn(0, 3)).apply()
            FirestoreSyncManager.syncProgress(context)
        }
    }

    fun getTotalYellowStars(context: Context, totalQuizzes: Int = 408): Int {
        val limit = maxOf(totalQuizzes, 408)
        return (0 until limit).sumOf { getYellowStars(context, it) }
    }

    fun getTotalBlueStars(context: Context, totalQuizzes: Int = 408): Int {
        val limit = maxOf(totalQuizzes, 408)
        return (0 until limit).sumOf { getBlueStars(context, it) }
    }

    fun getTotalRedStars(context: Context, totalQuizzes: Int = 408): Int {
        val limit = maxOf(totalQuizzes, 408)
        return (0 until limit).sumOf { getRedStars(context, it) }
    }

    // Geriye dönük uyumluluk (Legacy compatibility)
    fun getReinforcementStars(context: Context, quizIndex: Int): Int = getBlueStars(context, quizIndex)

    fun saveReinforcementStars(context: Context, quizIndex: Int, stars: Int) = saveBlueStars(context, quizIndex, stars)

    // Sandık (Chest) Yönetimi
    fun isChestOpened(context: Context, chestNumber: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("chest_opened_$chestNumber", false)
    }

    fun isChestUnlockable(chestNumber: Int, progress: Map<Int, Result>): Boolean {
        // Kendisinden önceki 5 quizin tamamı (örneğin chestNumber 5 ise 0,1,2,3,4. quizler) en az 1 yıldızla tamamlanmış olmalı
        val startQuizIndex = maxOf(0, chestNumber - 5)
        val endQuizIndex = chestNumber - 1
        for (idx in startQuizIndex..endQuizIndex) {
            val res = progress[idx]
            if (starsFor(res) < 1) {
                return false
            }
        }
        return true
    }

    fun openChest(context: Context, chestNumber: Int, coins: Int, xp: Int, gems: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean("chest_opened_$chestNumber", false)) return false
        prefs.edit().putBoolean("chest_opened_$chestNumber", true).apply()
        if (coins > 0) addCoins(context, coins)
        if (xp > 0) addXp(context, xp)
        if (gems > 0) addGems(context, gems)
        return true
    }

    fun claimChest(context: Context, chestNumber: Int, coins: Int, xp: Int, gems: Int): Boolean =
        openChest(context, chestNumber, coins, xp, gems)
}
