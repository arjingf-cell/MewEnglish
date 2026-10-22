package com.example.ngilizcequizapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MewStreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        updateStreakWidget(context)
    }

    companion object {
        /**
         * Broadcast or direct call to update all active Mew Streak Widgets.
         * Call this whenever streak, theme, avatar, or streak shield changes.
         */
        fun updateStreakWidget(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, MewStreakWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                    for (widgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, widgetId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_mew_streak)

            // 1. Data Sources
            val streakPrefs = context.getSharedPreferences("streak_prefs", Context.MODE_PRIVATE)
            val streakCount = streakPrefs.getInt("streak_count", 0)
            val lastEntryDate = streakPrefs.getString("last_entry_date", "") ?: ""

            val boostPrefs = context.getSharedPreferences("market_boost_prefs", Context.MODE_PRIVATE)
            val boostShieldExpiry = boostPrefs.getLong("boost_shield_expiry", 0L)

            val cosmeticsPrefs = context.getSharedPreferences("user_cosmetics_prefs", Context.MODE_PRIVATE)
            val equippedThemeId = cosmeticsPrefs.getString("equipped_theme_id", "klasik") ?: "klasik"
            val equippedAvatarId = cosmeticsPrefs.getString("equipped_avatar_id", "akademisyen_mew") ?: "akademisyen_mew"

            // 2. Dates & Conditions
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayStr = sdf.format(Date())
            val isTodayCompleted = (lastEntryDate == todayStr)

            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val isWarningTime = (!isTodayCompleted && currentHour >= 20)
            val isShieldActive = (boostShieldExpiry > System.currentTimeMillis())

            val displayStreak = if (streakCount <= 0) 1 else streakCount

            // 3. Theme Styling
            val (bgRes, catBoxRes, titleColor, subtitleColor, dayTextColor, shieldTextColor) = when (equippedThemeId.lowercase(Locale.ROOT)) {
                "kingdom" -> ThemeWidgetConfig(
                    bgRes = R.drawable.widget_bg_kingdom,
                    catBoxRes = R.drawable.widget_cat_box_kingdom,
                    titleColor = Color.parseColor("#FFD700"),
                    subtitleColor = Color.parseColor("#E2E8F0"),
                    dayTextColor = Color.parseColor("#E5B842"),
                    shieldTextColor = Color.parseColor("#FFD700")
                )
                "uzay" -> ThemeWidgetConfig(
                    bgRes = R.drawable.widget_bg_uzay,
                    catBoxRes = R.drawable.widget_cat_box_uzay,
                    titleColor = Color.parseColor("#C4B5FD"),
                    subtitleColor = Color.parseColor("#94A3B8"),
                    dayTextColor = Color.parseColor("#A78BFA"),
                    shieldTextColor = Color.parseColor("#818CF8")
                )
                "sakura" -> ThemeWidgetConfig(
                    bgRes = R.drawable.widget_bg_sakura,
                    catBoxRes = R.drawable.widget_cat_box_sakura,
                    titleColor = Color.parseColor("#BE185D"),
                    subtitleColor = Color.parseColor("#831843"),
                    dayTextColor = Color.parseColor("#DB2777"),
                    shieldTextColor = Color.parseColor("#EC4899")
                )
                "okyanus" -> ThemeWidgetConfig(
                    bgRes = R.drawable.widget_bg_okyanus,
                    catBoxRes = R.drawable.widget_cat_box_okyanus,
                    titleColor = Color.parseColor("#38BDF8"),
                    subtitleColor = Color.parseColor("#BAE6FD"),
                    dayTextColor = Color.parseColor("#0284C7"),
                    shieldTextColor = Color.parseColor("#0EA5E9")
                )
                else -> ThemeWidgetConfig(
                    bgRes = R.drawable.widget_bg_klasik,
                    catBoxRes = R.drawable.widget_cat_box_klasik,
                    titleColor = Color.parseColor("#17324A"),
                    subtitleColor = Color.parseColor("#6C8297"),
                    dayTextColor = Color.parseColor("#64748B"),
                    shieldTextColor = Color.parseColor("#0284C7")
                )
            }

            // Apply Backgrounds and Colors
            views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)
            views.setInt(R.id.widget_cat_container, "setBackgroundResource", catBoxRes)
            views.setTextColor(R.id.widget_title, titleColor)
            views.setTextColor(R.id.widget_subtitle, subtitleColor)
            views.setTextColor(R.id.widget_shield_text, shieldTextColor)

            // 4. Status Title & Subtitle
            when {
                isTodayCompleted -> {
                    views.setTextViewText(R.id.widget_title, "Bugünkü serin güvende!")
                    views.setTextViewText(R.id.widget_subtitle, "$displayStreak Günlük Seri Devam Ediyor")
                }
                isWarningTime -> {
                    views.setTextViewText(R.id.widget_title, "Serin sönmek üzere! Hemen 1 quiz çöz")
                    views.setTextColor(R.id.widget_title, Color.parseColor("#DC2626")) // Critical Red Warning
                    views.setTextViewText(R.id.widget_subtitle, "$displayStreak Günlük Serini Koru")
                }
                else -> {
                    views.setTextViewText(R.id.widget_title, "Bugün henüz pratik yapmadın")
                    views.setTextViewText(R.id.widget_subtitle, "$displayStreak Günlük Serini Sürdür")
                }
            }

            // 5. Shield Badge
            if (isShieldActive) {
                views.setImageViewResource(R.id.widget_shield_icon, R.drawable.ic_widget_shield_active)
                views.setTextViewText(R.id.widget_shield_text, "Kalkan Aktif")
            } else {
                views.setImageViewResource(R.id.widget_shield_icon, R.drawable.ic_widget_shield_inactive)
                views.setTextViewText(R.id.widget_shield_text, "Kalkan Yok")
                views.setTextColor(R.id.widget_shield_text, Color.parseColor("#94A3B8"))
            }

            // 6. 7-Day Flame Indicator (Monday -> Sunday)
            val dayNames = arrayOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
            val todayDayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }

            for (i in 0..6) {
                val textId = context.resources.getIdentifier("widget_day_text_$i", "id", context.packageName)
                val flameId = context.resources.getIdentifier("widget_day_flame_$i", "id", context.packageName)
                val colId = context.resources.getIdentifier("widget_day_col_$i", "id", context.packageName)

                if (textId != 0 && flameId != 0) {
                    views.setTextViewText(textId, dayNames[i])

                    if (i == todayDayIndex) {
                        // Highlight Today
                        views.setTextColor(textId, Color.parseColor("#FF6D00"))
                        if (colId != 0) {
                            views.setInt(colId, "setBackgroundResource", R.drawable.widget_today_indicator)
                        }

                        if (isTodayCompleted) {
                            views.setImageViewResource(flameId, R.drawable.ic_widget_flame_active)
                        } else if (isWarningTime) {
                            views.setImageViewResource(flameId, R.drawable.ic_widget_flame_warning)
                        } else {
                            views.setImageViewResource(flameId, R.drawable.ic_widget_flame_inactive)
                        }
                    } else {
                        // Other days
                        views.setTextColor(textId, dayTextColor)
                        if (colId != 0) {
                            views.setInt(colId, "setBackgroundResource", 0)
                        }

                        if (i < todayDayIndex) {
                            // Completed if covered by current streak
                            val daysAgo = todayDayIndex - i
                            val wasCompleted = if (isTodayCompleted) (streakCount >= daysAgo + 1) else (streakCount >= daysAgo)
                            if (wasCompleted) {
                                views.setImageViewResource(flameId, R.drawable.ic_widget_flame_active)
                            } else {
                                views.setImageViewResource(flameId, R.drawable.ic_widget_flame_inactive)
                            }
                        } else {
                            // Future day
                            views.setImageViewResource(flameId, R.drawable.ic_widget_flame_inactive)
                        }
                    }
                }
            }

            // 7. Cat Holding Turkish Flag Composite Bitmap
            val catResId = when (equippedAvatarId) {
                "cool_mew" -> R.drawable.cool_mew
                "pirate_mew" -> R.drawable.pirate_mew
                "wizard_mew" -> R.drawable.wizard_mew
                "king_mew" -> R.drawable.king_mew
                "maestro_mew" -> R.drawable.maestro_mew
                "akademisyen_mew" -> R.drawable.akademisyen_mew
                else -> R.drawable.cat_logo
            }
            val catWithFlagBitmap = createCatWithFlagBitmap(context, catResId, R.drawable.turkey)
            if (catWithFlagBitmap != null) {
                views.setImageViewBitmap(R.id.widget_cat_image, catWithFlagBitmap)
            } else {
                views.setImageViewResource(R.id.widget_cat_image, catResId)
            }

            // 8. Click Action: Open MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

        private fun createCatWithFlagBitmap(context: Context, catResId: Int, flagResId: Int): Bitmap? {
            return try {
                val size = 200
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                // Draw Cat
                val catDrawable = ContextCompat.getDrawable(context, catResId) ?: ContextCompat.getDrawable(context, R.drawable.cat_logo)
                catDrawable?.let {
                    it.setBounds(10, 10, size - 10, size - 10)
                    it.draw(canvas)
                }

                // Draw Turkish Flag at bottom-right corner/paw
                val flagDrawable = ContextCompat.getDrawable(context, flagResId)
                flagDrawable?.let {
                    val flagW = 66
                    val flagH = 44
                    val flagLeft = size - flagW - 8
                    val flagTop = size - flagH - 8

                    // Background & Shadow border for flag
                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                        setShadowLayer(4f, 1f, 1f, Color.parseColor("#50000000"))
                    }
                    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = Color.WHITE
                        style = Paint.Style.STROKE
                        strokeWidth = 2.5f
                    }
                    val rectF = RectF(
                        flagLeft.toFloat() - 2f,
                        flagTop.toFloat() - 2f,
                        (flagLeft + flagW).toFloat() + 2f,
                        (flagTop + flagH).toFloat() + 2f
                    )
                    canvas.drawRoundRect(rectF, 4f, 4f, bgPaint)

                    it.setBounds(flagLeft, flagTop, flagLeft + flagW, flagTop + flagH)
                    it.draw(canvas)
                    canvas.drawRoundRect(rectF, 4f, 4f, borderPaint)
                }

                bitmap
            } catch (e: Exception) {
                null
            }
        }
    }

    private data class ThemeWidgetConfig(
        val bgRes: Int,
        val catBoxRes: Int,
        val titleColor: Int,
        val subtitleColor: Int,
        val dayTextColor: Int,
        val shieldTextColor: Int
    )
}
