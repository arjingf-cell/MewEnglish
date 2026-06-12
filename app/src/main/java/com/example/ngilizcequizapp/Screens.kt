package com.example.ngilizcequizapp.navigation // ✅ KRİTİK: navigation.kt ile aynı paket adı

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.ngilizcequizapp.R // Kendi paket adını kontrol et!
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val iconRes: Int? = null,              // 1. Kapı: PNG (Sayı)
    val vectorIcon: ImageVector? = null     // 2. Kapı: Vector (Obje)
) {
    // ✅ PNG olanlar: iconRes kısmına yazıyoruz
    object Home : Screen("home", "Ana Sayfa", iconRes = R.drawable.home)
    object Progress : Screen("progress", "İlerleme", iconRes = R.drawable.kupa)
    object Quizzes : Screen("quizzes", "Quiz", iconRes = R.drawable.quiz_icon)
    object Words : Screen("words", "Kelime", iconRes = R.drawable.dicti)

    // ✅ Vector olanlar: iconRes'i boş geçip (null), vectorIcon kısmına yazıyoruz
    object Profile : Screen("profile", "Profil", vectorIcon = Icons.Default.Person)

    // Diğerleri için hata almamak adına varsayılan bir ikon atayalım
    object ReadingLevels : Screen("reading_levels", "Reading", vectorIcon = Icons.Default.LibraryBooks)
    object Guide : Screen("guide_screen", "Guide", vectorIcon = Icons.Default.Info)

    // ✅ "vectorIcon =" diyerek bunun 4. sıradaki parametre olduğunu belirttik
    object ListeningLevels : Screen("listening_levels", "Dinleme", vectorIcon = Icons.Default.Hearing)

    object ListeningSelection : Screen("listening_selection/{level}", "Quiz", vectorIcon = Icons.Default.Audiotrack) {
        fun createRoute(level: String) = "listening_selection/$level"
    }

    object QuizTree : Screen("quiz_tree/{lvl}", "Quiz", vectorIcon = Icons.Default.AccountTree) {
        fun createRoute(level: String) = "quiz_tree/$level"
    }
}