package com.example.ngilizcequizapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ngilizcequizapp.quiz.engine.VocabularyLevelDataQuiz

@Composable
fun VisualQuizUI(
    data: VocabularyLevelDataQuiz,
    tts: SpeechAssistant,
    onAnswer: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Başlık: Soru Türkçe olarak buradan geliyor
        Text(
            text = data.questionTurkish, // "Hangisi 'Su'?" gibi
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4B4B4B)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Ana Soru Görseli (Büyük Kart)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            border = BorderStroke(2.dp, Color(0xFFE5E5E5))
        ) {
            Image(
                painter = painterResource(id = data.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Şıklar Listesi
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            data.options.forEachIndexed { index, option ->

                // Eğer modelinde her şık için resim varsa (optionImages) onu al,
                // yoksa ana resmi kullanma (çünkü hepsi aynı olur), şimdilik ikon gösterelim.
                val optionIcon = if (data.optionImages.isNotEmpty()) data.optionImages[index] else null

                Button(
                    onClick = {
                        tts.speak(option)
                        onAnswer(data.isCorrect(option))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFE5E5E5)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Şıkka özel resim varsa göster
                            if (optionIcon != null) {
                                Image(
                                    painter = painterResource(id = optionIcon),
                                    contentDescription = null,
                                    modifier = Modifier.size(45.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                            }

                            Text(
                                text = option,
                                style = TextStyle(
                                    color = Color(0xFF4B4B4B),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF1CB0F6),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}