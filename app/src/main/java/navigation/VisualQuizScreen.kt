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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import com.example.ngilizcequizapp.LocalAppThemeColors
import com.example.ngilizcequizapp.LocalIsDarkMode

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
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    var selectedId by remember { mutableStateOf<Int?>(null) }

    val backgroundGradient = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(appColors.background, appColors.surfaceRaised)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFEBF5FF), Color(0xFFFFFFFF))
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Karakter ve Balon
            Row(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = appColors.surface,
                    border = BorderStroke(1.dp, appColors.border),
                    shadowElevation = if (isDark) 0.dp else 2.dp
                ) {
                    Text(
                        text = quizStep.characterSpeech,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp,
                        color = appColors.text
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Soru Alanı
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = appColors.surface,
                border = BorderStroke(1.dp, appColors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = quizStep.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.text
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = appColors.primary, thickness = 4.dp)
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
                    containerColor = if (selectedId != null) appColors.primary else if (isDark) appColors.surfaceRaised else Color.LightGray
                )
            ) {
                Text("KONTROL ET", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
    }
}

@Composable
fun AnswerCard(option: VisualQuizOption, isSelected: Boolean, onSelect: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val borderColor = if (isSelected) appColors.primary else appColors.border

    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable { onSelect() }
            .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) (if (isDark) appColors.surfaceRaised else Color(0xFFE3F2FD)) else appColors.surface
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(appColors.primary.copy(alpha = 0.1f), CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = option.text,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) appColors.primary else appColors.text
            )
        }
    }
}

@Composable
fun AnswerOptionCard(isSelected: Boolean, onClick: () -> Unit) {
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val borderColor = if (isSelected) appColors.primary else appColors.border
    val bgColor = if (isSelected) (if (isDark) appColors.surfaceRaised else Color(0xFFE3F2FD)) else appColors.surface

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
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(appColors.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(50.dp).background(Color.Transparent))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cevap Metni",
                fontWeight = FontWeight.Bold,
                color = if (isSelected) appColors.primary else appColors.textMuted
            )
        }
    }
}