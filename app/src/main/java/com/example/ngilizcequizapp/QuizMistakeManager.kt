package com.example.ngilizcequizapp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

data class TopicMistakeSummary(
    val topic: String,
    val quizIds: List<Int>
)

data class TopicAnalysis(
    val topic: String,
    val totalQuizzes: Int,
    val solvedQuizzes: Int,
    val successRate: Float,
    val errorRate: Float,
    val erroneousQuizIds: List<Int>,
    val statusText: String,
    val isFullyCompleted: Boolean
)

object QuizMistakeManager {
    private const val PREFS_NAME = "mew_mistake_tracker"
    private const val KEY_MISTAKES_JSON = "topic_mistakes_map"
    private const val KEY_SOLVE_TIMESTAMPS = "solve_timestamps"

    var stateRevision by mutableIntStateOf(0)
        private set

    private fun notifyChanged() {
        stateRevision++
    }

    // Mevcut oturumda çözülen quiz sayısı (Lazy auth email bağlama tetikleyicisi için)
    private var sessionQuizzesSolved = 0

    fun getSessionQuizzesSolved(): Int = sessionQuizzesSolved

    fun incrementSessionQuizzesSolved(): Int {
        sessionQuizzesSolved++
        return sessionQuizzesSolved
    }

    /**
     * Quiz ID aralıklarına göre konu eşleştirmesi (1..1500)
     */
    fun getTopicForQuiz(quizId: Int): String {
        return when (quizId) {
            in 1..25 -> "Tanışma, Renkler & Sayılar"
            in 26..50 -> "Aile, Ev & Günlük Yaşam"
            in 51..80 -> "Saatler & Günlük Rutinler"
            in 81..120 -> "Geçmiş Zaman (Past Simple)"
            in 121..160 -> "Gelecek Zaman (Future Tense)"
            in 161..200 -> "Modal Fiiller & Bağlaçlar"
            in 201..230 -> "Dersler, Okul & Bilim (A2)"
            in 231..255 -> "Hayvanlar & Doğa (A2)"
            in 256..280 -> "Can, Should & Yetenek Kipleri"
            in 281..300 -> "Vücut, Sağlık & Hobiler"
            in 301..325 -> "Miktar Belirteçleri (Quantifiers)"
            in 326..350 -> "Kıyaslama Sıfatları (Comparatives)"
            in 351..400 -> "Üstünlük Sıfatları (Superlatives)"
            in 401..450 -> "Present Perfect & Zaman İfadeleri"
            in 451..500 -> "Edilgen Çatı (Passive Voice)"
            in 501..600 -> "B1 Orta Düzey Dilbilgisi"
            in 601..750 -> "B1+ İleri Konuşma Kalıpları"
            in 751..900 -> "B2 Deyimler & Phrasal Verbs"
            in 901..1200 -> "B2+ Akademik & İş İngilizcesi"
            in 1201..1500 -> "C1 İleri Seviye Akıcılık & Makale"
            else -> "Genel Dilbilgisi & Kelime"
        }
    }

    /**
     * Tüm konuların listesi
     */
    val allTopics = listOf(
        "Tanışma, Renkler & Sayılar",
        "Aile, Ev & Günlük Yaşam",
        "Saatler & Günlük Rutinler",
        "Geçmiş Zaman (Past Simple)",
        "Gelecek Zaman (Future Tense)",
        "Modal Fiiller & Bağlaçlar",
        "Dersler, Okul & Bilim (A2)",
        "Hayvanlar & Doğa (A2)",
        "Can, Should & Yetenek Kipleri",
        "Vücut, Sağlık & Hobiler",
        "Miktar Belirteçleri (Quantifiers)",
        "Kıyaslama Sıfatları (Comparatives)",
        "Üstünlük Sıfatları (Superlatives)",
        "Present Perfect & Zaman İfadeleri",
        "Edilgen Çatı (Passive Voice)",
        "B1 Orta Düzey Dilbilgisi",
        "B1+ İleri Konuşma Kalıpları",
        "B2 Deyimler & Phrasal Verbs",
        "B2+ Akademik & İş İngilizcesi",
        "C1 İleri Seviye Akıcılık & Makale"
    )

    private fun loadMistakesMap(context: Context): MutableMap<String, MutableSet<Int>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_MISTAKES_JSON, null)
        val result = mutableMapOf<String, MutableSet<Int>>()

        if (!jsonStr.isNullOrBlank()) {
            try {
                val json = JSONObject(jsonStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val arr = json.getJSONArray(key)
                    val set = mutableSetOf<Int>()
                    for (i in 0 until arr.length()) {
                        set.add(arr.getInt(i))
                    }
                    if (set.isNotEmpty()) {
                        result[key] = set
                    }
                }
                return result
            } catch (e: Exception) {
                // Parse error, fallback to reconstruction
            }
        }

        // Eğer kayıt yoksa QuizTreeProgressStore'daki mevcut 2 yıldız ve altı çözülmüş quizlerden derle
        val allSolved = QuizTreeProgressStore.loadAll(context)
        for ((idx, res) in allSolved) {
            val stars = QuizTreeProgressStore.starsFor(res)
            val quizId = idx + 1
            if (stars in 1..2 || (res.totalQuestions > 0 && res.correctAnswers < res.totalQuestions)) {
                val topic = getTopicForQuiz(quizId)
                val set = result.getOrPut(topic) { mutableSetOf() }
                set.add(quizId)
            }
        }
        return result
    }

    private fun saveMistakesMap(context: Context, map: Map<String, Set<Int>>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = JSONObject()
        for ((k, v) in map) {
            if (v.isNotEmpty()) {
                val arr = JSONArray()
                v.forEach { arr.put(it) }
                json.put(k, arr)
            }
        }
        prefs.edit().putString(KEY_MISTAKES_JSON, json.toString()).apply()
        notifyChanged()
    }

    /**
     * Hatalı Quiz Listesi Temizleme Kuralı:
     * - Kullanıcı listedeki hatalı quizi tekrar çözüp 3 yıldız alana kadar o quiz listeden SİLİNMEZ.
     * - 2 yıldız veya altı listede tutmaya devam eder.
     * - 3 yıldız alındığında quiz hatası listeden temizlenir.
     */
    fun onQuizCompleted(
        context: Context,
        quizId: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        stars: Int
    ) {
        val topic = getTopicForQuiz(quizId)
        val map = loadMistakesMap(context)
        val set = map.getOrPut(topic) { mutableSetOf() }

        if (stars >= 3 && correctAnswers >= totalQuestions) {
            // 3 yıldız alındı -> Listeden sil!
            set.remove(quizId)
            if (set.isEmpty()) {
                map.remove(topic)
            }
        } else {
            // Hata var veya 2 yıldız / altı -> Listede tut / ekle!
            set.add(quizId)
        }

        saveMistakesMap(context, map)
        recordQuizSolveTimestamp(context)
    }

    /**
     * Hata yapılan konuları hata sayısına göre çoktan aza sıralı döndürür.
     */
    fun getTopicsWithMistakes(context: Context): List<TopicMistakeSummary> {
        val map = loadMistakesMap(context)
        return map.entries
            .filter { it.value.isNotEmpty() }
            .map { TopicMistakeSummary(it.key, it.value.toList().sorted()) }
            .sortedByDescending { it.quizIds.size }
    }

    /**
     * "En Çok Yoğunlaşılması Gereken Konular" adıyla yalnızca konu başlıklarını
     * hata sayısına veya pedagojik önceliğe göre sıralı listeler.
     */
    fun getTopFocusTopics(context: Context): List<String> {
        val mistakes = getTopicsWithMistakes(context).map { it.topic }
        val remaining = allTopics.filter { !mistakes.contains(it) }
        return mistakes + remaining
    }

    /**
     * Son 7 günde çözülen quiz sayısını hesaplamak için timestamp kaydeder.
     */
    private fun recordQuizSolveTimestamp(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val oldStr = prefs.getString(KEY_SOLVE_TIMESTAMPS, "") ?: ""
        val list = oldStr.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { now - it < 7L * 24 * 60 * 60 * 1000 }
            .toMutableList()
        list.add(now)
        prefs.edit().putString(KEY_SOLVE_TIMESTAMPS, list.joinToString(",")).apply()
    }

    fun getLast7DaysSolvedCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val oldStr = prefs.getString(KEY_SOLVE_TIMESTAMPS, "") ?: ""
        val validList = oldStr.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { now - it < 7L * 24 * 60 * 60 * 1000 }
        
        // Eğer timestamp henüz az ise mevcut çözülen toplam quiz sayısından makul bir oran hesapla
        if (validList.isEmpty()) {
            val totalSolved = QuizTreeProgressStore.loadAll(context).size
            return minOf(totalSolved, 14)
        }
        return validList.size
    }

    /**
     * Konuların başarı, hata ve tamamlanma oranlarını hesaplar.
     */
    fun getTopicAnalysisList(context: Context): List<TopicAnalysis> {
        val allSteps = QuizDataHolder.quizSteps + QuizDataHolder.quizStepsA2
        val progressMap = QuizTreeProgressStore.loadAll(context)
        val mistakesMap = loadMistakesMap(context)

        // Konulara göre quizleri grupla
        val grouped = allSteps.groupBy { getTopicForQuiz(it.id) }

        return grouped.map { (topic, steps) ->
            val totalQuizzes = steps.size
            val solvedSteps = steps.filter { progressMap.containsKey(it.id - 1) }
            val solvedCount = solvedSteps.size
            val isFullyCompleted = totalQuizzes > 0 && solvedCount == totalQuizzes

            var totalCorrect = 0
            var totalQuestions = 0
            solvedSteps.forEach { step ->
                val res = progressMap[step.id - 1]
                if (res != null) {
                    totalCorrect += res.correctAnswers
                    totalQuestions += res.totalQuestions
                }
            }

            val successRate = if (totalQuestions > 0) {
                (totalCorrect.toFloat() / totalQuestions.toFloat()) * 100f
            } else if (solvedCount > 0) {
                100f
            } else {
                0f
            }
            val errorRate = (100f - successRate).coerceIn(0f, 100f)

            val erroneousQuizIds = mistakesMap[topic]?.toList()?.sorted()
                ?: solvedSteps.filter { step ->
                    val res = progressMap[step.id - 1]
                    res != null && (res.correctAnswers < res.totalQuestions || QuizTreeProgressStore.starsFor(res) < 3)
                }.map { it.id }

            val statusText = when {
                !isFullyCompleted -> "Devam Ediyor ($solvedCount/$totalQuizzes)"
                successRate >= 99.9f -> "Kusursuz derecede bitirildi"
                successRate >= 90.0f -> "Çok iyi derecede öğrenildi"
                successRate >= 80.0f -> "İyi derecede öğrenildi"
                successRate >= 70.0f -> "Tekrar edilmesi gerekir"
                else -> "Yetersiz (< %70)"
            }

            TopicAnalysis(
                topic = topic,
                totalQuizzes = totalQuizzes,
                solvedQuizzes = solvedCount,
                successRate = successRate,
                errorRate = errorRate,
                erroneousQuizIds = erroneousQuizIds,
                statusText = statusText,
                isFullyCompleted = isFullyCompleted
            )
        }
    }

    /**
     * "Hata Yapılan Konular": Yalnızca çözülmüş ve başarı oranı <= %80 olan konular listelenir.
     * Hata oranına göre azalan sıralanır.
     */
    fun getMistakeTopics(context: Context): List<TopicAnalysis> {
        return getTopicAnalysisList(context)
            .filter { it.solvedQuizzes > 0 && it.successRate <= 80f }
            .sortedByDescending { it.errorRate }
    }

    /**
     * "Öğrenilen Konular": Konudaki tüm quizler bitmiş olmalı ve başarı oranı >= %70 olmalı.
     * < %70 olanlar çözülmemiş sayılır, listede yer almaz.
     */
    fun getLearnedTopics(context: Context): List<TopicAnalysis> {
        return getTopicAnalysisList(context)
            .filter { it.isFullyCompleted && it.successRate >= 70f }
            .sortedByDescending { it.successRate }
    }

    /**
     * "Neredeyse Kusursuz" Listesi: %90-%99 arası bitirilmiş konular.
     */
    fun getNearlyPerfectTopics(context: Context): List<TopicAnalysis> {
        return getTopicAnalysisList(context)
            .filter { it.isFullyCompleted && it.successRate >= 90f && it.successRate < 99.9f }
            .sortedByDescending { it.successRate }
    }
}
