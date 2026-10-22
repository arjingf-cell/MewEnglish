@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.ngilizcequizapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.content.BroadcastReceiver
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.ngilizcequizapp.quiz.engine.*
import com.example.ngilizcequizapp.quiz.engine.QuestionType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MistakeEntry(
    val id: String = "",
    val quizIndex: Int = 0,
    val quizTitle: String? = "",
    val questionNumber: Int = 0,
    val questionId: Int = 0,
    val questionText: String? = "",
    val imageRes: Int = 0,
    val correctAnswer: String? = "",
    val options: List<String>? = emptyList(),
    val optionImages: List<Int>? = emptyList(),
    val questionType: String? = "",
    val selectedAnswer: String? = "",
    val createdAt: Long = 0L,
    val solved: Boolean = false,
    val patternTitle: String? = "",
    val patternExampleOne: String? = "",
    val patternHighlightOne: String? = "",
    val patternExampleTwo: String? = "",
    val patternHighlightTwo: String? = "",
    val speakingTitle: String? = "",
    val speakingSentence: String? = "",
    val speakingCharacterRes: Int? = 0,
    val correctWords: List<String>? = emptyList(),
    val extraWords: List<String>? = emptyList(),
    val fullAnswerTurkish: String? = ""
) {
    fun toQuestion(): VocabularyLevelDataQuiz {
        val realQuestion = runCatching {
            if (quizIndex > 0) {
                val list = QuizRepository.getQuizByNumber(quizIndex)
                list.find { it.id == questionId } ?: list.getOrNull(questionNumber - 1)
            } else null
        }.getOrNull()

        if (realQuestion != null) {
            return realQuestion
        }

        val safeCorrect = correctAnswer.orEmpty()
        val safeSpeaking = speakingSentence?.ifBlank { safeCorrect } ?: safeCorrect
        val safeFullAnswerTurkish = fullAnswerTurkish?.ifBlank { questionText.orEmpty() }
            ?: questionText.orEmpty().ifBlank { safeCorrect }
        val safeCorrectWords = if (!correctWords.isNullOrEmpty()) {
            correctWords
        } else if (safeCorrect.isNotBlank()) {
            safeCorrect.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else {
            emptyList()
        }
        val safeExtraWords = if (!extraWords.isNullOrEmpty()) {
            extraWords
        } else {
            listOf("the", "is", "a", "you")
        }

        return VocabularyLevelDataQuiz(
            id = questionId,
            questionTurkish = questionText.orEmpty(),
            imageRes = imageRes,
            correctAnswer = safeCorrect,
            options = options ?: emptyList(),
            optionImages = optionImages ?: emptyList(),
            questionType = runCatching { QuestionType.valueOf(questionType.orEmpty()) }.getOrDefault(QuestionType.VISUAL),
            patternTitle = patternTitle.orEmpty(),
            patternExampleOne = patternExampleOne.orEmpty(),
            patternHighlightOne = patternHighlightOne.orEmpty(),
            patternExampleTwo = patternExampleTwo.orEmpty(),
            patternHighlightTwo = patternHighlightTwo.orEmpty(),
            speakingTitle = speakingTitle.orEmpty(),
            speakingSentence = safeSpeaking,
            speakingCharacterRes = speakingCharacterRes ?: 0,
            correctWords = safeCorrectWords,
            extraWords = safeExtraWords,
            fullAnswerTurkish = safeFullAnswerTurkish
        )
    }
}

object MistakeJarStore {
    private const val PREFS = "mistake_jar_prefs"
    private const val KEY = "mistakes"
    val mistakes = mutableStateListOf<MistakeEntry>()
    private var loaded = false

    fun load(context: Context) {
        if (loaded) return
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null)
        if (!json.isNullOrBlank()) {
            val type = object : TypeToken<List<MistakeEntry>>() {}.type
            runCatching { Gson().fromJson<List<MistakeEntry>>(json, type) }
                .getOrNull()?.let { list ->
                    val sanitized = list.map { entry ->
                        val safeCorrect = entry.correctAnswer.orEmpty()
                        val safeCorrectWords = if (!entry.correctWords.isNullOrEmpty()) {
                            entry.correctWords
                        } else if (entry.questionType == QuestionType.TRANSLATION_BUILDER.name && safeCorrect.isNotBlank()) {
                            safeCorrect.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
                        } else {
                            entry.correctWords ?: emptyList()
                        }
                        MistakeEntry(
                            id = entry.id.ifBlank { "${entry.quizIndex}_${entry.questionNumber}_${entry.createdAt}" },
                            quizIndex = entry.quizIndex,
                            quizTitle = entry.quizTitle.orEmpty(),
                            questionNumber = entry.questionNumber,
                            questionId = entry.questionId,
                            questionText = entry.questionText.orEmpty(),
                            imageRes = entry.imageRes,
                            correctAnswer = safeCorrect,
                            options = entry.options ?: emptyList(),
                            optionImages = entry.optionImages ?: emptyList(),
                            questionType = entry.questionType.orEmpty(),
                            selectedAnswer = entry.selectedAnswer.orEmpty(),
                            createdAt = entry.createdAt,
                            solved = entry.solved,
                            patternTitle = entry.patternTitle.orEmpty(),
                            patternExampleOne = entry.patternExampleOne.orEmpty(),
                            patternHighlightOne = entry.patternHighlightOne.orEmpty(),
                            patternExampleTwo = entry.patternExampleTwo.orEmpty(),
                            patternHighlightTwo = entry.patternHighlightTwo.orEmpty(),
                            speakingTitle = entry.speakingTitle.orEmpty(),
                            speakingSentence = entry.speakingSentence.orEmpty(),
                            speakingCharacterRes = entry.speakingCharacterRes ?: 0,
                            correctWords = safeCorrectWords,
                            extraWords = entry.extraWords ?: emptyList(),
                            fullAnswerTurkish = entry.fullAnswerTurkish.orEmpty()
                        )
                    }
                    mistakes.clear()
                    mistakes.addAll(sanitized.sortedByDescending(MistakeEntry::createdAt))
                }
        }
        loaded = true
    }

    fun record(
        context: Context,
        quizIndex: Int,
        quizTitle: String,
        questionNumber: Int,
        question: VocabularyLevelDataQuiz,
        selectedAnswer: String
    ) {
        load(context)
        val stableId = "$quizIndex-${question.id}"
        mistakes.removeAll { it.id == stableId }
        val resolvedCorrectWords = if (question.correctWords.isNotEmpty()) {
            question.correctWords
        } else if (question.correctAnswer.isNotBlank()) {
            question.correctAnswer.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else {
            emptyList()
        }
        mistakes.add(
            0,
            MistakeEntry(
                id = stableId,
                quizIndex = quizIndex,
                quizTitle = quizTitle,
                questionNumber = questionNumber,
                questionId = question.id,
                questionText = question.questionTurkish,
                imageRes = question.imageRes,
                correctAnswer = question.correctAnswer,
                options = question.options,
                optionImages = question.optionImages,
                questionType = question.questionType.name,
                selectedAnswer = selectedAnswer,
                createdAt = System.currentTimeMillis(),
                patternTitle = question.patternTitle,
                patternExampleOne = question.patternExampleOne,
                patternHighlightOne = question.patternHighlightOne,
                patternExampleTwo = question.patternExampleTwo,
                patternHighlightTwo = question.patternHighlightTwo,
                speakingTitle = question.speakingTitle,
                speakingSentence = question.speakingSentence.ifBlank { question.correctAnswer },
                speakingCharacterRes = question.speakingCharacterRes,
                correctWords = resolvedCorrectWords,
                extraWords = question.extraWords,
                fullAnswerTurkish = question.fullAnswerTurkish.ifBlank { question.questionTurkish }
            )
        )
        persist(context)
        MistakeReminderNotifier.show(context)
    }

    fun markSolved(context: Context, id: String) = update(context, id) { it.copy(solved = true) }

    fun remove(context: Context, id: String) {
        load(context)
        mistakes.removeAll { it.id == id }
        persist(context)
    }

    private fun update(context: Context, id: String, transform: (MistakeEntry) -> MistakeEntry) {
        load(context)
        val index = mistakes.indexOfFirst { it.id == id }
        if (index >= 0) {
            runCatching {
                mistakes[index] = transform(mistakes[index])
                persist(context)
            }
        }
    }

    private fun persist(context: Context) {
        runCatching {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putString(KEY, Gson().toJson(mistakes.toList())).apply()
        }
    }
}

object AppNotificationRouter {
    const val EXTRA_ROUTE = "open_route"
    var pendingRoute by mutableStateOf<String?>(null)
}

object MistakeReminderNotifier {
    private const val CHANNEL_ID = "mistake_reminders"
    private const val REQUEST_CODE = 710
    private const val REMINDER_DELAY_MS = 150 * 60 * 1000L

    fun show(context: Context) {
        runCatching {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, MistakeReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + REMINDER_DELAY_MS,
                pendingIntent
            )
        }
    }

    fun showNow(context: Context) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Mistake Jar hatırlatmaları", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(AppNotificationRouter.EXTRA_ROUTE, "mistake_jar")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Kısa bir tekrar zamanı")
            .setContentText("Son quizlerde zorlandığın yerleri yeniden gözden geçirmeye ne dersin?")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Son çözdüğün quizlerde zorlandığın yerleri yeniden gözden geçirmeye ne dersin?"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        manager.notify(REQUEST_CODE, notification)
    }
}

class MistakeReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        MistakeJarStore.load(context)
        if (MistakeJarStore.mistakes.any { !it.solved }) MistakeReminderNotifier.showNow(context)
    }
}

private enum class MistakeFilter { PENDING, SOLVED, ALL }

fun selectMistakeBatch(pending: List<MistakeEntry>, maxCount: Int = 5): List<MistakeEntry> {
    if (pending.isEmpty()) return emptyList()
    if (pending.size <= maxCount) return pending

    // En çok çözülmemiş hatası olan quizIndex'i hedef al
    val targetQuizIndex = pending.groupBy { it.quizIndex }
        .maxByOrNull { it.value.size }?.key ?: pending.first().quizIndex

    // En yakın quizIndex'lere göre ve soru sırasına göre sırala
    return pending.sortedWith(
        compareBy<MistakeEntry> { kotlin.math.abs(it.quizIndex - targetQuizIndex) }
            .thenBy { it.questionNumber }
    ).take(maxCount)
}

@Composable
fun MistakeJarScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current

    MistakeJarStore.load(context)
    var filter by remember { mutableStateOf(MistakeFilter.PENDING) }
    var topic by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<MistakeEntry?>(null) }
    val all = MistakeJarStore.mistakes
    val topics: List<String> = all.map { it.quizTitle.orEmpty() }.filter { it.isNotBlank() }.distinct()
    val visible = all.filter {
        (topic == null || it.quizTitle.orEmpty() == topic) && when (filter) {
            MistakeFilter.PENDING -> !it.solved
            MistakeFilter.SOLVED -> it.solved
            MistakeFilter.ALL -> true
        }
    }

    val pendingMistakes = all.filter { !it.solved }
    val practiceBatch = remember(pendingMistakes) { selectMistakeBatch(pendingMistakes, 5) }

    val isKingdomTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }
    val kingdomGoldBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFDF7A),
                Color(0xFFD4AF37),
                Color(0xFFAA7C11),
                Color(0xFFFFDF7A)
            )
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isKingdomTheme) {
            Image(
                painter = painterResource(id = R.drawable.kingdom_arkaplan),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF040816).copy(alpha = 0.35f))
            )
        }

        Scaffold(
            containerColor = if (isKingdomTheme) Color.Transparent else appColors.background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (isKingdomTheme) "👑 Hata Torbası" else "Mistake Jar",
                            fontWeight = FontWeight.Black,
                            color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                "Geri",
                                tint = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isKingdomTheme) Color.Transparent else appColors.surface
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isKingdomTheme) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.4.dp, kingdomGoldBrush),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF070F26).copy(alpha = 0.92f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.kingdom_mistake_jar),
                                    contentDescription = "Kraliyet Hata Şişesi",
                                    modifier = Modifier.size(90.dp)
                                )
                                Spacer(Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Kraliyet Hata Şişesi",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFFD700)
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = "Zorlandığın kelime ve kurallar bu şişede toplanır. Pratik yaparak onları kalıcı bilgiye dönüştür!",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFFF7D6).copy(alpha = 0.85f),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MistakeStat("Kontrol et", all.count { !it.solved }, Color(0xFFFF8A65), filter == MistakeFilter.PENDING, Modifier.weight(1f)) { filter = MistakeFilter.PENDING }
                        MistakeStat("Çözüldü", all.count { it.solved }, Color(0xFF54B7A3), filter == MistakeFilter.SOLVED, Modifier.weight(1f)) { filter = MistakeFilter.SOLVED }
                        MistakeStat("Toplam", all.size, Color(0xFF4EA7E8), filter == MistakeFilter.ALL, Modifier.weight(1f)) { filter = MistakeFilter.ALL }
                    }
                }

                // ⚡ 5'li (veya eldeki kadar) Hata Quizi Başlatma Butonu
                if (practiceBatch.isNotEmpty()) {
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("mistake_practice_session")
                                },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isKingdomTheme) Color(0xFF1B3A6B) else appColors.primary,
                            border = if (isKingdomTheme) BorderStroke(1.2.dp, Color(0xFFFFD700)) else null,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = if (isKingdomTheme) Color(0xFFFFD700) else Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = if (isKingdomTheme) "👑 Hataları Pratik Yap" else "⚡ Hataları Pratik Yap",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 17.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${practiceBatch.size} soru hazır · En yakın quizlerden derlendi",
                                        fontSize = 13.sp,
                                        color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.9f) else Color.White.copy(alpha = 0.9f)
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = if (isKingdomTheme) Color(0xFFFFD700) else Color.White
                                )
                            }
                        }
                    }
                }

            if (topics.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = topic == null, onClick = { topic = null }, label = { Text("Tüm konular") }) }
                        items(topics) { item ->
                            FilterChip(selected = topic == item, onClick = { topic = item }, label = { Text(item, maxLines = 1) })
                        }
                    }
                }
            }
            if (visible.isEmpty()) {
                item { EmptyMistakeJar(filter == MistakeFilter.PENDING) }
            } else {
                visible.groupBy { formatMistakeDate(it.createdAt) }.forEach { (date, entries) ->
                    item { Text(date, fontWeight = FontWeight.Bold, color = appColors.textMuted, modifier = Modifier.padding(top = 6.dp)) }
                    items(entries, key = { it.id }) { entry ->
                        MistakeRow(
                            entry = entry,
                            onClick = { navController.navigate("mistake_review/${URLEncoder.encode(entry.id, "UTF-8")}") },
                            onDelete = { deleteTarget = entry }
                        )
                    }
                }
            }
        }
    }
    }

    deleteTarget?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            icon = { Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFE05A5A)) },
            title = { Text("Bu hatayı silelim mi?") },
            text = { Text("${entry.quizTitle.orEmpty()} içindeki ${entry.questionNumber}. soru Mistake Jar listesinden kaldırılacak.") },
            confirmButton = { TextButton(onClick = { MistakeJarStore.remove(context, entry.id); deleteTarget = null }) { Text("Sil", color = Color(0xFFD84343)) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Vazgeç") } },
            containerColor = appColors.surface
        )
    }
}

@Composable
private fun MistakeStat(label: String, count: Int, accent: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember { UserCosmeticsStore.isKingdomThemeActive(context) }

    val statBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else if (selected) accent.copy(alpha = .18f) else appColors.surface
    val statBorder = if (isKingdomTheme) {
        BorderStroke(if (selected) 2.dp else 1.2.dp, if (selected) Color(0xFFFFD700) else Color(0xFFD4AF37).copy(alpha = 0.4f))
    } else BorderStroke(if (selected) 2.dp else 1.dp, accent.copy(alpha = if (selected) .85f else .25f))

    Surface(
        modifier = modifier.height(92.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = statBg,
        border = statBorder
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = if (isKingdomTheme && selected) Color(0xFFFFD700) else accent
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isKingdomTheme) Color(0xFFFFF7D6) else appColors.text,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MistakeRow(entry: MistakeEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isKingdomTheme = remember { UserCosmeticsStore.isKingdomThemeActive(context) }

    val rowBg = if (isKingdomTheme) Color(0xFF070F26).copy(alpha = 0.92f) else appColors.surface
    val rowBorder = if (isKingdomTheme) {
        BorderStroke(1.2.dp, if (entry.solved) Color(0xFF10B981) else Color(0xFFFFD700))
    } else BorderStroke(1.dp, if (entry.solved) Color(0xFF90D6C8) else Color(0xFFFFCCBC))

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = rowBg,
        shadowElevation = 2.dp,
        border = rowBorder
    ) {
        Row(Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(if (entry.solved) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFFF9100).copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (entry.solved) Icons.Default.Check else Icons.Default.ErrorOutline,
                    null,
                    tint = if (entry.solved) Color(0xFF10B981) else (if (isKingdomTheme) Color(0xFFFFD700) else Color(0xFFE56F51))
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.quizTitle.orEmpty(),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isKingdomTheme) Color(0xFFFFD700) else appColors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${entry.questionNumber}. soru · ${questionTypeLabel(entry.questionType.orEmpty())}",
                    fontSize = 12.sp,
                    color = if (isKingdomTheme) Color(0xFFFFF7D6).copy(alpha = 0.7f) else appColors.textMuted
                )
                Text(
                    text = entry.questionText.orEmpty(),
                    fontSize = 13.sp,
                    color = if (isKingdomTheme) Color.White else appColors.text,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Close,
                    "Listeden sil",
                    tint = if (isKingdomTheme) Color(0xFFFFD700).copy(alpha = 0.7f) else appColors.textMuted
                )
            }
        }
    }
}

@Composable
private fun EmptyMistakeJar(pending: Boolean) {
    val appColors = LocalAppThemeColors.current
    Column(Modifier.fillMaxWidth().padding(vertical = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.TaskAlt, null, modifier = Modifier.size(56.dp), tint = Color(0xFF54B7A3))
        Spacer(Modifier.height(12.dp))
        Text(if (pending) "Kontrol edilecek hata kalmadı" else "Bu bölüm henüz boş", fontWeight = FontWeight.Bold, color = appColors.text)
        Text("Yeni öğrendiklerin burada düzenli biçimde görünecek.", color = appColors.textMuted, fontSize = 14.sp)
    }
}

@Composable
fun MistakePracticeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val ttsHelper = remember { SpeechAssistant(context) }

    MistakeJarStore.load(context)
    val pendingMistakes = remember { MistakeJarStore.mistakes.filter { !it.solved } }
    val batch = remember { selectMistakeBatch(pendingMistakes, 5) }

    if (batch.isEmpty()) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var isAnswerCorrect by rememberSaveable { mutableStateOf(false) }
    var correctCount by rememberSaveable { mutableIntStateOf(0) }
    var finished by rememberSaveable { mutableStateOf(false) }
    var showWordsDialog by rememberSaveable { mutableStateOf(false) }

    val currentEntry = batch.getOrNull(currentIndex)

    if (finished || currentEntry == null) {
        Scaffold(
            containerColor = appColors.background,
            topBar = {
                TopAppBar(
                    title = { Text("Pratik Tamamlandı", fontWeight = FontWeight.Black, color = appColors.text) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.Close, "Kapat", tint = appColors.text)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surface)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (correctCount > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (correctCount > 0) Icons.Default.EmojiEvents else Icons.Default.Replay,
                            contentDescription = null,
                            tint = if (correctCount > 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = if (correctCount == batch.size) "Mükemmel Tekrar!" else "Pratik Bitti!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = appColors.text
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "${batch.size} sorudan $correctCount tanesini doğru çözdün.",
                    fontSize = 16.sp,
                    color = appColors.textMuted
                )

                if (correctCount > 0) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "🎉 Doğru çözülen $correctCount soru 'Çözüldü' sekmesine aktarıldı.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32)
                    )
                }

                if (batch.size - correctCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Yanlış yapılan ${batch.size - correctCount} soru torbanda tekrar çözülmek üzere bekliyor.",
                        fontSize = 13.sp,
                        color = appColors.textMuted
                    )
                }

                Spacer(Modifier.height(36.dp))

                Button(
                    onClick = { showWordsDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
                ) {
                    Icon(Icons.Default.BookmarkAdd, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Kelimeleri Kaydet", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.White)
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, appColors.border)
                ) {
                    Text("Hata Torbasına Dön", fontWeight = FontWeight.Bold, color = appColors.text)
                }
            }
        }

        if (showWordsDialog) {
            SaveQuizWordsDialog(
                entries = batch,
                onDismiss = { showWordsDialog = false },
                onFinish = {
                    showWordsDialog = false
                    navController.popBackStack()
                }
            )
        }
        return
    }

    val question = remember(currentEntry.id) { currentEntry.toQuestion() }

    val onAnswerResult: (Boolean, String) -> Unit = { isCorrect: Boolean, selectedText: String ->
        if (!showSheet) {
            isAnswerCorrect = isCorrect
            if (isCorrect) {
                correctCount++
                MistakeJarStore.markSolved(context, currentEntry.id)
                AchievementManager.unlockBadge(context, "common_first_fix")
                if (MistakeJarStore.mistakes.none { !it.solved }) {
                    AchievementManager.unlockBadge(context, "rare_jar_emptier")
                }
            }
            showSheet = true
        }
    }

    Scaffold(
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentEntry.quizTitle.orEmpty(), fontWeight = FontWeight.Black, fontSize = 16.sp, color = appColors.text, maxLines = 1)
                        Text("Soru ${currentIndex + 1} / ${batch.size}", fontSize = 12.sp, color = appColors.textMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, "Kapat", tint = appColors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                key(currentEntry.id) {
                    when (question.questionType) {
                        QuestionType.VISUAL -> {
                            VisualQuizContent(
                                data = question,
                                tts = ttsHelper,
                                onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                            )
                        }

                        QuestionType.LISTENING -> {
                            ListeningQuizContent(
                                data = question,
                                tts = ttsHelper,
                                onAnswer = { text: String ->
                                    val isCorr = text.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                    onAnswerResult(isCorr, text)
                                }
                            )
                        }

                        QuestionType.SPEAKING -> {
                            SpeakingQuizContent(
                                data = question,
                                tts = ttsHelper,
                                onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) },
                                onCannotSpeak = { onAnswerResult(false, "") }
                            )
                        }

                        QuestionType.PATTERN -> {
                            PatternQuizContent(
                                data = question,
                                onAnswer = { text ->
                                    val isCorr = text.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                    onAnswerResult(isCorr, text)
                                }
                            )
                        }

                        QuestionType.FILL_IN_BLANK -> {
                            boslukdoldurma(
                                data = question,
                                onAnswerResult = { isCorr, text -> onAnswerResult(isCorr, text) },
                                health = 5,
                                progress = (currentIndex.toFloat() / batch.size),
                                ttsHelper = ttsHelper
                            )
                        }

                        QuestionType.MATCHING -> {
                            eslestirme(
                                data = question,
                                onAnswerResult = { isCorr -> onAnswerResult(isCorr, "") },
                                health = 5,
                                progress = (currentIndex.toFloat() / batch.size)
                            )
                        }

                        QuestionType.TRANSLATION_BUILDER -> {
                            TranslationBuilderScreen(
                                question = TranslationQuestion(
                                    id = question.id,
                                    qEnglish = question.questionTurkish,
                                    correctWords = question.correctWords,
                                    extraWords = question.extraWords,
                                    fullAnswerTurkish = question.fullAnswerTurkish
                                ),
                                comboCount = 0,
                                progress = (currentIndex.toFloat() / batch.size).coerceIn(0f, 1f),
                                energy = 5,
                                onClose = { navController.popBackStack() },
                                onSpeak = { ttsHelper.speak(question.questionTurkish) },
                                onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                            )
                        }

                        else -> {
                            if (question.optionImages.isNotEmpty() && question.optionImages.any { it != 0 }) {
                                VisualQuizContent(
                                    data = question,
                                    tts = ttsHelper,
                                    onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                                )
                            } else {
                                ListeningQuizContent(
                                    data = question,
                                    tts = ttsHelper,
                                    onAnswer = { text: String ->
                                        val isCorr = text.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                        onAnswerResult(isCorr, text)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSheet) {
                QuizResultSheet(
                    isCorrect = isAnswerCorrect,
                    correctAnswer = question.correctAnswer,
                    onContinue = {
                        showSheet = false
                        if (currentIndex < batch.size - 1) {
                            currentIndex++
                        } else {
                            finished = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MistakeReviewScreen(navController: NavHostController, mistakeId: String) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current
    val isDark = LocalIsDarkMode.current
    val ttsHelper = remember { SpeechAssistant(context) }

    MistakeJarStore.load(context)
    WordStorage.loadFromDisk(context)
    val decodedMistakeId = remember(mistakeId) {
        runCatching { URLDecoder.decode(mistakeId, "UTF-8") }.getOrDefault(mistakeId)
    }
    val entry = MistakeJarStore.mistakes.firstOrNull { it.id == decodedMistakeId || it.id == mistakeId }
    if (entry == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }
    val question = remember(entry.id) { entry.toQuestion() }
    var showSheet by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var showWords by remember { mutableStateOf(false) }

    val onAnswerResult: (Boolean, String) -> Unit = { isCorrect: Boolean, selectedText: String ->
        if (!showSheet) {
            isAnswerCorrect = isCorrect
            if (isCorrect) {
                MistakeJarStore.markSolved(context, entry.id)
                AchievementManager.unlockBadge(context, "common_first_fix")
                val unsolvedExist = MistakeJarStore.mistakes.any { !it.solved }
                if (!unsolvedExist) {
                    AchievementManager.unlockBadge(context, "rare_jar_emptier")
                }
            }
            showSheet = true
        }
    }

    Scaffold(
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(entry.quizTitle.orEmpty(), fontWeight = FontWeight.Bold, color = appColors.text)
                        Text("${entry.questionNumber}. soruyu tekrar et", fontSize = 12.sp, color = appColors.textMuted)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, "Kapat", tint = appColors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = appColors.surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                key(entry.id) {
                    when (question.questionType) {
                        QuestionType.VISUAL -> {
                            VisualQuizContent(
                                data = question,
                                tts = ttsHelper,
                                onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                            )
                        }

                        QuestionType.LISTENING -> {
                            ListeningQuizContent(
                                data = question,
                                tts = ttsHelper,
                                onAnswer = { text: String ->
                                    val isCorr = text.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                    onAnswerResult(isCorr, text)
                                }
                            )
                        }

                        QuestionType.SPEAKING -> {
                            SpeakingQuizContent(
                                data = question,
                                tts = ttsHelper,
                                onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) },
                                onCannotSpeak = { onAnswerResult(false, "") }
                            )
                        }

                        QuestionType.PATTERN -> {
                            PatternQuizContent(
                                data = question,
                                onAnswer = { text ->
                                    val isCorr = text.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                    onAnswerResult(isCorr, text)
                                }
                            )
                        }

                        QuestionType.FILL_IN_BLANK -> {
                            boslukdoldurma(
                                data = question,
                                onAnswerResult = { isCorr, text -> onAnswerResult(isCorr, text) },
                                health = 5,
                                progress = 1f,
                                ttsHelper = ttsHelper
                            )
                        }

                        QuestionType.MATCHING -> {
                            eslestirme(
                                data = question,
                                onAnswerResult = { isCorr -> onAnswerResult(isCorr, "") },
                                health = 5,
                                progress = 1f
                            )
                        }

                        QuestionType.TRANSLATION_BUILDER -> {
                            TranslationBuilderScreen(
                                question = TranslationQuestion(
                                    id = question.id,
                                    qEnglish = question.questionTurkish,
                                    correctWords = question.correctWords,
                                    extraWords = question.extraWords,
                                    fullAnswerTurkish = question.fullAnswerTurkish
                                ),
                                comboCount = 0,
                                progress = 1f,
                                energy = 5,
                                onClose = { navController.popBackStack() },
                                onSpeak = { ttsHelper.speak(question.questionTurkish) },
                                onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                            )
                        }

                        else -> {
                            if (question.optionImages.isNotEmpty() && question.optionImages.any { it != 0 }) {
                                VisualQuizContent(
                                    data = question,
                                    tts = ttsHelper,
                                    onAnswer = { isCorr, text -> onAnswerResult(isCorr, text) }
                                )
                            } else {
                                ListeningQuizContent(
                                    data = question,
                                    tts = ttsHelper,
                                    onAnswer = { text: String ->
                                        val isCorr = text.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
                                        onAnswerResult(isCorr, text)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (showSheet) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = if (isAnswerCorrect) (if (isDark) Color(0xFF103326) else Color(0xFFE8F5E9)) else (if (isDark) Color(0xFF38151D) else Color(0xFFFFEBEE)),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).navigationBarsPadding(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isAnswerCorrect) "Harika! Doğru Çözüldü ✨" else "Doğru Cevap:",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAnswerCorrect) (if (isDark) Color(0xFF55D6A7) else Color(0xFF2E7D32)) else (if (isDark) Color(0xFFFF7A90) else Color(0xFFC62828))
                            )
                        )
                        if (!isAnswerCorrect) {
                            Text(
                                text = question.correctAnswer,
                                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = appColors.text)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showWords = true },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, appColors.border)
                            ) {
                                Text("Kelimeleri Kaydet", fontWeight = FontWeight.Bold, color = appColors.text, fontSize = 14.sp)
                            }
                            Button(
                                onClick = {
                                    showSheet = false
                                    navController.popBackStack()
                                },
                                modifier = Modifier.weight(1f).height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isAnswerCorrect) appColors.success else appColors.primary)
                            ) {
                                Text("TAMAMLA", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showWords) {
        SaveQuizWordsDialog(
            entries = listOf(entry),
            onDismiss = { showWords = false },
            onFinish = { showWords = false; navController.popBackStack() }
        )
    }
}

@Composable
fun SaveQuizWordsDialog(
    entries: List<MistakeEntry>,
    onDismiss: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val appColors = LocalAppThemeColors.current

    LaunchedEffect(Unit) {
        AppDictionary.initialize(context)
    }

    val words = remember(entries) {
        val allTexts = entries.flatMap {
            listOf(it.questionText.orEmpty(), it.correctAnswer.orEmpty()) +
                (it.options ?: emptyList()) +
                (it.correctWords ?: emptyList()) +
                (it.extraWords ?: emptyList())
        }
        extractEnglishWords(allTexts)
    }
    val selected = remember { mutableStateListOf<String>().apply { addAll(words) } }

    fun saveAndFinish() {
        if (selected.isNotEmpty()) {
            WordStorage.loadFromDisk(context)
            var addedCount = 0
            selected.forEach { word ->
                val trMeaning = AppDictionary.words[word] ?: AppDictionary.words[word.removeSuffix("s")] ?: ""
                if (WordStorage.savedWords.none { it.word.equals(word, ignoreCase = true) }) {
                    WordStorage.savedWords.add(SavedWord(word = word, meaning = trMeaning))
                    addedCount++
                }
            }
            WordStorage.saveToDisk(context)
            Toast.makeText(
                context,
                if (addedCount > 0) "$addedCount kelime Kelimelerim'e kaydedildi!" else "Seçilen kelimeler zaten kayıtlıydı.",
                Toast.LENGTH_SHORT
            ).show()
        }
        onFinish()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = appColors.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text("Kelimeleri Kaydet", fontWeight = FontWeight.Black, color = appColors.text, fontSize = 20.sp)
        },
        text = {
            if (words.isEmpty()) {
                Text("Bu sorularda kaydedilebilecek yeni bir İngilizce kelime bulunamadı.", color = appColors.textMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Öğrenmek istediğin İngilizce kelimeleri seç:", fontSize = 13.sp, color = appColors.textMuted)
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(words) { word ->
                            val isChecked = word in selected
                            val trMeaning = AppDictionary.words[word] ?: AppDictionary.words[word.removeSuffix("s")] ?: ""
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (isChecked) selected.remove(word) else selected.add(word)
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChecked) appColors.primarySoft else appColors.surfaceRaised,
                                border = BorderStroke(1.5.dp, if (isChecked) appColors.primary else appColors.border)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = {
                                            if (isChecked) selected.remove(word) else selected.add(word)
                                        }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(word, fontWeight = FontWeight.Black, fontSize = 16.sp, color = appColors.text)
                                        if (trMeaning.isNotBlank()) {
                                            Text(trMeaning, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = appColors.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = ::saveAndFinish,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = appColors.primary)
            ) {
                Text(
                    if (selected.isEmpty()) "Atla" else "Kelimelerime Kaydet (${selected.size})",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = appColors.textMuted)
            }
        }
    )
}

private fun extractEnglishWords(texts: List<String>): List<String> {
    val stopWords = setOf(
        "the", "a", "an", "is", "are", "am", "was", "were", "in", "on", "at",
        "to", "for", "of", "with", "and", "or", "but", "so", "it", "this",
        "that", "these", "those", "he", "she", "they", "we", "you", "i", "me",
        "my", "your", "his", "her", "its", "our", "their", "not", "be", "do",
        "does", "did", "have", "has", "had", "can", "will", "would", "what",
        "which", "who", "whom", "whose", "where", "when", "why", "how", "all",
        "any", "some", "no", "yes"
    )
    return texts.flatMap {
        Regex("[A-Za-z]+(?:'[A-Za-z]+)?").findAll(it).map { m -> m.value.lowercase().trim() }.toList()
    }
    .filter { it.length >= 3 && it !in stopWords }
    .filter { word ->
        // Yalnızca AppDictionary'de karşılığı olan gerçek İngilizce kelimeler kabul edilir (Türkçe kelimeler elenir)
        AppDictionary.words.containsKey(word) || AppDictionary.words.containsKey(word.removeSuffix("s"))
    }
    .distinct()
    .sorted()
}

private fun formatMistakeDate(timestamp: Long): String =
    SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr", "TR")).format(Date(timestamp))

private fun questionTypeLabel(type: String) = when (type) {
    QuestionType.LISTENING.name -> "Dinleme"
    QuestionType.MATCHING.name -> "Eşleştirme"
    QuestionType.FILL_IN_BLANK.name -> "Boşluk doldurma"
    QuestionType.PATTERN.name -> "Kalıp"
    QuestionType.SPEAKING.name -> "Konuşma"
    QuestionType.TRANSLATION_BUILDER.name -> "Cümle kurma"
    else -> "Görsel seçim"
}
