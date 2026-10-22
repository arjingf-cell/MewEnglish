package com.example.ngilizcequizapp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiService {
    // Proje API Anahtarı (Google Cloud / AI Studio)
    var apiKey: String = "AIzaSyCvFguqIF_lp-Qtw6rAr341ZpEi86MrVLk"

    // Kullanılan model (gerekirse güncel bir modelle değiştirilebilir)
    var model: String = "gemini-1.5-flash"

    private const val SYSTEM_PROMPT = """Sen MewEnglish uygulamasının akıllı, esnek ve doğal bir yapay zeka dil asistanısın. Asla sabit, ezberlenmiş veya tekrarlayan kalıp cümle kullanma.
1. DOĞAL DİYALOG: Kullanıcının yazdığı cümle veya soruyu dikkatle incele ve ona mantıklı, akıcı, bağlama uygun bir cevap ver. Asla her mesaja aynı şablonla karşılık verme.
2. DİL UYUMU VE DEĞİŞİM: Kullanıcı hangi dilde (Türkçe, İngilizce, Fransızca, İspanyolca, Almanca vb.) konuşursa, anında o dilde akıcı bir şekilde yanıt ver. Kullanıcı farklı bir dilde konuşmak istediğini belirttiğinde doğrudan o dile geç.
3. EMOJI YASAĞI: Yanıtlarında kesinlikle emoji veya emoji açıklaması kullanma. Sadece düz metin üret.
4. YARDIMCI VE SAMİMİ OL: Kullanıcının dil öğrenme sürecini destekle, hata durumunu doğal akış içinde düzelt ve arkadaş canlısı bir tavır sergile.
Konu sınırı yoktur: bilim, teknoloji, günlük yaşam, sanat, felsefe, yazılım veya kullanıcı hangi konuyu açarsa onun üzerine konuş."""

    // Yalnızca gerçek emoji kod noktalarını temizler; diğer metne dokunmaz.
    private val emojiRegex = Regex(
        "[\\x{1F000}-\\x{1FAFF}\\x{2600}-\\x{27BF}\\x{FE0F}\\x{200D}\\x{1F1E6}-\\x{1F1FF}]"
    )

    private fun removeEmojis(text: String): String = text.replace(emojiRegex, "")

    /**
     * Canlı Gemini çağrısı. Başarılıysa modelin ham metnini döndürür.
     * Başarısızsa sahte bir yanıt üretmez; hatanın nedenini açıkça bildirir.
     *
     * @param history Ekranda görünen tüm mesajlar (güncel kullanıcı mesajı dahil).
     */
    suspend fun getGeminiResponse(history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true

            val root = JSONObject()
            root.put(
                "systemInstruction",
                JSONObject().put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
            )

            // Geçmiş: son 20 mesaj; Gemini ilk turun "user" olmasını beklediği için
            // baştaki model mesajları (karşılama mesajı gibi) atlanır.
            val recent = history.takeLast(20).dropWhile { !it.isFromUser }
            val contents = JSONArray()
            for (msg in recent) {
                contents.put(
                    JSONObject()
                        .put("role", if (msg.isFromUser) "user" else "model")
                        .put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                )
            }
            root.put("contents", contents)

            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(root.toString()) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
            val body = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }

            if (code in 200..299) {
                val parts = JSONObject(body)
                    .optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                val text = buildString {
                    if (parts != null) for (i in 0 until parts.length()) {
                        append(parts.getJSONObject(i).optString("text", ""))
                    }
                }
                if (text.isNotBlank()) return@withContext removeEmojis(text).trim()
                return@withContext "[Hata] Model boş yanıt döndürdü."
            }

            val apiMessage = runCatching {
                JSONObject(body).optJSONObject("error")?.optString("message")
            }.getOrNull().orEmpty()
            android.util.Log.e("GeminiService", "HTTP $code: $body")
            "[Hata $code] Yapay zeka servisine ulaşılamadı. $apiMessage".trim()
        } catch (e: Exception) {
            android.util.Log.e("GeminiService", "Connection error", e)
            "[Hata] Bağlantı kurulamadı: ${e.message ?: e.javaClass.simpleName}"
        }
    }
}
