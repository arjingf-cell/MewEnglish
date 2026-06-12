package navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.shape.CircleShape // ✅ Doğrusu bu




// --- 1. VERİ MODELLERİ (Bunlar en üstte durmalı ki motor bunları tanısın) ---
data class VisualQuizOption(
    val id: Int,
    val text: String,
    val imageRes: Int
)

data class VisualQuizStep(
    val question: String,
    val characterSpeech: String,
    val options: List<VisualQuizOption>,
    val correctAnswerId: Int
)

// --- 2. ANA MOTOR (ŞABLON) ---
@Composable
fun VisualQuizScreen(
    quizStep: VisualQuizStep,
    onAnswerSelected: (Boolean) -> Unit
) {
    var selectedId by remember { mutableStateOf<Int?>(null) }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFEBF5FF), Color(0xFFFFFFFF))
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Karakter ve Balon
            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                // Burada R.drawable.prof_kedi kısmında hala hata alırsan
                // şimdilik res/drawable içindeki kesin olan bir resim adını yaz.
                Box(modifier = Modifier.size(80.dp)) {
                    // Image(painter = painterResource(id = R.drawable.prof_kedi), contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 2.dp) {
                    Text(text = quizStep.characterSpeech, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Soru Alanı
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = quizStep.question, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color(0xFF1CB0F6), thickness = 4.dp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cevap Grid'i
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(quizStep.options) { option ->
                    AnswerCard(
                        option = option,
                        isSelected = selectedId == option.id,
                        onSelect = { selectedId = option.id }
                    )
                }
            }

            // Kontrol Butonu
            Button(
                onClick = { onAnswerSelected(selectedId == quizStep.correctAnswerId) },
                enabled = selectedId != null,
                modifier = Modifier.fillMaxWidth().height(60.dp).padding(bottom = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedId != null) Color(0xFF1CB0F6) else Color.LightGray
                )
            ) {
                Text("KONTROL ET", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}
@Composable
fun AnswerCard(option: VisualQuizOption, isSelected: Boolean, onSelect: () -> Unit) {
    val borderColor = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onSelect() }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFE3F2FD) else Color.White
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFF1CB0F6).copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Buradaki imageRes referansı data class'tan geliyor
                // Image(painter = painterResource(id = option.imageRes), contentDescription = null, modifier = Modifier.size(50.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = option.text, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable
fun AnswerOptionCard(isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected) Color(0xFF1CB0F6) else Color(0xFFE5E5E5)
    val bgColor = if (isSelected) Color(0xFFE3F2FD) else Color.White

    Surface(
        modifier = Modifier
            .aspectRatio(0.9f)
            .clickable { onClick() }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            // PNG arkasındaki mavi yuvarlak
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFF1CB0F6).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Cevap PNG'si
                Box(modifier = Modifier.size(50.dp).background(Color.Transparent))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cevap Metni",
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF1CB0F6) else Color.Gray
            )
        }
    }
}