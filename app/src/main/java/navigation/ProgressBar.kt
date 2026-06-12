package navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Button, Text ve ButtonDefaults için
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.animateContentSize




import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color




import androidx.compose.animation.core.*

import androidx.compose.material3.Text



import androidx.compose.ui.draw.scale



@Composable
fun SegmentedProgressBar(
    totalSteps: Int,
    currentStep: Int,
    consecutiveCorrect: Int
) {
    val isComboActive = consecutiveCorrect >= 3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(totalSteps) { i ->
            // Renk geçişini yumuşak yapmak için animasyon
            val color by animateColorAsState(
                targetValue = when {
                    isComboActive && i <= currentStep -> Color(0xFFFFD700) // Combo Sarısı
                    i == currentStep -> Color(0xFFFFD700) // Şu anki Sarı
                    i < currentStep -> Color(0xFF1CB0F6)  // Çözülen Mavi
                    else -> Color(0xFFE5E5E5)             // Bekleyen Gri
                },
                animationSpec = tween(500)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun ComboStarsAnimation(isComboActive: Boolean) {
    if (isComboActive) {
        val infiniteTransition = rememberInfiniteTransition(label = "stars")

        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2.0f, // Patlamayı biraz daha büyüttüm
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ), label = "scale"
        )

        val opacity by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart
            ), label = "alpha"
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨🌟✨",
                fontSize = 40.sp,
                modifier = Modifier
                    .scale(scale) // Büyüme burada
                    .graphicsLayer(alpha = opacity) // ✅ Şeffaflık BURADA (Hata çözümü)
                    .padding(bottom = 150.dp)
            )
        }
    }
}
data class ProgressState(
    val totalQuestions: Int,
    val currentQuestionIndex: Int, // 0'dan başlıyor
    val consecutiveCorrect: Int = 0 // Üst üste doğru sayısı
)


@Composable
fun DuoProgressBar(
    state: ProgressState,
    isComboActive: Boolean // 3 doğru üst üste gelince true olacak
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp) // Parçalar arası boşluk
    ) {
        repeat(state.totalQuestions) { index ->
            // Renk Mantığı
            val color = when {
                isComboActive && index <= state.currentQuestionIndex -> Color(0xFFFFD700) // 🔥 Combo: Her yer SARI
                index < state.currentQuestionIndex -> Color(0xFF1CB0F6) // ✅ Çözülen: MAVİ
                index == state.currentQuestionIndex -> Color(0xFFFFD700) // 📍 Şu anki: SARI
                else -> Color(0xFFE5E5E5) // ⏳ Bekleyen: GRİ
            }

            // Her bir parça
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .animateContentSize() // Renk değişimi yumuşak olsun
            )
        }
    }
}

@Composable
fun QuizScreenContent() {
    var currentIndex by remember { mutableIntStateOf(0) }
    var consecutiveCorrect by remember { mutableIntStateOf(0) }
    val totalQuestions = 10
    val isComboActive = consecutiveCorrect >= 3

    val onAnswerCorrect = {
        consecutiveCorrect++
        if (currentIndex < totalQuestions - 1) currentIndex++
    }

    val onAnswerWrong = {
        consecutiveCorrect = 0
        if (currentIndex < totalQuestions - 1) currentIndex++
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Bar
            DuoProgressBar(
                state = ProgressState(totalQuestions, currentIndex, consecutiveCorrect),
                isComboActive = isComboActive
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Test İçin Veriler
            Text(text = "Soru: ${currentIndex + 1}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(text = "Combo: $consecutiveCorrect", color = if(isComboActive) Color(0xFFFFD700) else Color.Gray)

            Spacer(modifier = Modifier.height(20.dp))

            // DOĞRU BUTONU
            Button(
                onClick = onAnswerCorrect,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF58CC02)),
                modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
            ) {
                Text("DOĞRU")
            }

            Spacer(modifier = Modifier.height(10.dp))

            // YANLIŞ BUTONU
            Button(
                onClick = onAnswerWrong,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth(0.7f).height(50.dp)
            ) {
                Text("YANLIŞ")
            }
        }
    }
}