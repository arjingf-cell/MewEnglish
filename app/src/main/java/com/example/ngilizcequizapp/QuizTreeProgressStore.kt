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
        val isReinforcement: Boolean
    )

    fun starsFor(result: Result?): Int {
        if (result == null || result.totalQuestions <= 0) return 0
        val ratio = result.correctAnswers.toFloat() / result.totalQuestions.toFloat()
        return when {
            ratio >= 1.0f -> 3
            ratio >= 0.8f -> 2
            ratio >= 0.7f -> 1
            else -> 0
        }
    }

    fun getXp(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_XP, 0)
    }

    fun addXp(context: Context, xp: Int) {
        if (xp <= 0) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_XP, getXp(context) + xp).apply()
    }

    fun getCoins(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_COINS, 0)
    }

    fun addCoins(context: Context, coins: Int) {
        if (coins <= 0) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_COINS, getCoins(context) + coins).apply()
    }

    fun getGems(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_GEMS, 0)
    }

    fun addGems(context: Context, gems: Int) {
        if (gems <= 0) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_GEMS, getGems(context) + gems).apply()
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
        val stars = when {
            totalQuestions <= 0 -> 0
            correctAnswers.toFloat() / totalQuestions.toFloat() >= 1.0f -> 3
            correctAnswers.toFloat() / totalQuestions.toFloat() >= 0.8f -> 2
            correctAnswers.toFloat() / totalQuestions.toFloat() >= 0.7f -> 1
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
            val errors = totalQuestions - correctAnswers
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

        return AttemptOutcome(result, earnedCoins, earnedGems, earnedXp, isReinforcement || isBlueStarsReinforcement)
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

    fun lastSolvedIndex(results: Map<Int, Result>): Int {
        var idx = 0
        while (results.containsKey(idx)) {
            idx++
        }
        return idx - 1
    }

    fun getReinforcementStars(context: Context, quizIndex: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("reinforcement_stars_$quizIndex", 0)
    }

    fun saveReinforcementStars(context: Context, quizIndex: Int, stars: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt("reinforcement_stars_$quizIndex", 0)
        if (stars > current) {
            prefs.edit().putInt("reinforcement_stars_$quizIndex", stars).apply()
        }
    }
}
