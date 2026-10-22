package com.example.ngilizcequizapp

import java.util.Locale

enum class WordPOS(val displayName: String) {
    FIIL("Fiil"),
    ISIM("İsim"),
    SIFAT("Sıfat"),
    ZARF("Zarf")
}

object WordPOSHelper {

    private val verbKeywords = setOf(
        "etmek", "olmak", "yapmak", "vermek", "almak", "tutmak", "gelmek", "gitmek",
        "demek", "bilmek", "görmek", "geçmek", "durmak", "açmak", "kapatmak", "çıkmak",
        "girmek", "seçmek", "istemek", "sevmek", "çalışmak", "yazmak", "okumak", "konuşmak",
        "dinlemek", "bakmak", "anlamak", "koşmak", "yüzmek", "yürümek", "başlamak", "bitirmek",
        "düşünmek", "hissetmek", "beklemek", "aramak", "bırakmak", "bulmak", "göstermek",
        "anlatmak", "izlemek", "oynamak", "söylemek", "sormak", "cevaplamak", "uyumak",
        "kalkmak", "oturmak", "pişirmek", "yemek", "içmek", "ödemek", "öğrenmek", "öğretmek",
        "gülmek", "ağlamak", "uçmak", "atlamak", "sürmek", "yıkamak", "temizlemek", "açıklamak"
    )

    private val adverbKeywords = setOf(
        "şekilde", "biçimde", "olarak", "tamamen", "kesinlikle", "birlikte", "hemen",
        "sonra", "önce", "şimdi", "ayrıca", "aslında", "zaten", "belki", "daima", "asla",
        "hızlıca", "yavaşça", "kolayca", "sessizce", "çoğunlukla", "genellikle"
    )

    private val adjKeywords = setOf(
        "büyük", "küçük", "yeni", "eski", "iyi", "kötü", "güzel", "çirkin", "hızlı",
        "yavaş", "sıcak", "soğuk", "uzun", "kısa", "genç", "yaşlı", "zengin", "fakir",
        "mutlu", "üzgün", "zor", "kolay", "temiz", "kirli", "kırmızı", "mavi", "yeşil",
        "sarı", "siyah", "beyaz", "harika", "mükemmel", "özel", "önemli", "farklı", "yüksek", "düşük"
    )

    fun detectPOS(englishWord: String, turkishMeaning: String): WordPOS {
        val eng = englishWord.lowercase(Locale.ROOT).trim()
        val tr = turkishMeaning.lowercase(Locale.ROOT).trim()

        if (eng.startsWith("to ") || tr.endsWith("mek") || tr.endsWith("mak")) {
            return WordPOS.FIIL
        }

        val trWords = tr.split(",", "/", ";", " ").map { it.trim() }.filter { it.isNotEmpty() }
        for (w in trWords) {
            if (verbKeywords.any { w.endsWith(it) || w == it }) {
                return WordPOS.FIIL
            }
        }

        if ((eng.endsWith("ly") && eng.length > 4 && !isAdjectiveEndingWithLy(eng)) ||
            adverbKeywords.any { tr.contains(it) }) {
            return WordPOS.ZARF
        }

        if (adjKeywords.any { tr.contains(it) } ||
            eng.endsWith("ful") || eng.endsWith("less") || eng.endsWith("ous") || eng.endsWith("ive") || eng.endsWith("able")) {
            return WordPOS.SIFAT
        }

        return WordPOS.ISIM
    }

    private fun isAdjectiveEndingWithLy(word: String): Boolean {
        val lyAdjectives = setOf("friendly", "lovely", "ugly", "lonely", "silly", "early", "daily", "weekly", "monthly")
        return lyAdjectives.contains(word)
    }

    fun getDistractorForPOS(targetWord: String, targetMeaning: String, targetPOS: WordPOS, allSavedWords: List<SavedWord>): String {
        val samePOSFromSaved = allSavedWords.filter {
            !it.word.equals(targetWord, ignoreCase = true) &&
            !it.meaning.equals(targetMeaning, ignoreCase = true) &&
            detectPOS(it.word, it.meaning) == targetPOS
        }

        if (samePOSFromSaved.isNotEmpty()) {
            return samePOSFromSaved.shuffled().first().meaning
        }

        val dictionaryEntries = AppDictionary.words.entries.filter {
            !it.key.equals(targetWord, ignoreCase = true) &&
            !it.value.equals(targetMeaning, ignoreCase = true) &&
            detectPOS(it.key, it.value) == targetPOS
        }

        if (dictionaryEntries.isNotEmpty()) {
            return dictionaryEntries.shuffled().first().value
        }

        val fallbackSaved = allSavedWords.filter {
            !it.word.equals(targetWord, ignoreCase = true) &&
            !it.meaning.equals(targetMeaning, ignoreCase = true)
        }
        if (fallbackSaved.isNotEmpty()) {
            return fallbackSaved.shuffled().first().meaning
        }

        return "Farklı Kelime"
    }
}
