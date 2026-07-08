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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.ngilizcequizapp.quiz.engine.QuestionType
import com.example.ngilizcequizapp.quiz.engine.VocabularyLevelDataQuiz
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MistakeEntry(
    val id: String,
    val quizIndex: Int,
    val quizTitle: String,
    val questionNumber: Int,
    val questionId: Int,
    val questionText: String,
    val imageRes: Int,
    val correctAnswer: String,
    val options: List<String>,
    val optionImages: List<Int>,
    val questionType: String,
    val selectedAnswer: String,
    val createdAt: Long,
    val solved: Boolean = false
) {
    fun toQuestion() = VocabularyLevelDataQuiz(
        id = questionId,
        questionTurkish = questionText,
        imageRes = imageRes,
        correctAnswer = correctAnswer,
        options = options,
        optionImages = optionImages,
        questionType = runCatching { QuestionType.valueOf(questionType) }.getOrDefault(QuestionType.VISUAL)
    )
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
                .getOrNull()?.let { mistakes.addAll(it.sortedByDescending(MistakeEntry::createdAt)) }
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
                createdAt = System.currentTimeMillis()
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
        if (index >= 0) mistakes[index] = transform(mistakes[index])
        persist(context)
    }

    private fun persist(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY, Gson().toJson(mistakes.toList())).apply()
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
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MistakeReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + REMINDER_DELAY_MS,
            pendingIntent
        )
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

@Composable
fun MistakeJarScreen(navController: NavHostController) {
    val context = LocalContext.current
    MistakeJarStore.load(context)
    var filter by remember { mutableStateOf(MistakeFilter.PENDING) }
    var topic by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<MistakeEntry?>(null) }
    val all = MistakeJarStore.mistakes
    val topics = all.map { it.quizTitle }.distinct()
    val visible = all.filter {
        (topic == null || it.quizTitle == topic) && when (filter) {
            MistakeFilter.PENDING -> !it.solved
            MistakeFilter.SOLVED -> it.solved
            MistakeFilter.ALL -> true
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5FAFE),
        topBar = {
            TopAppBar(
                title = { Text("Mistake Jar", fontWeight = FontWeight.Black, color = Color(0xFF203247)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE5F5FD))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MistakeStat("Kontrol et", all.count { !it.solved }, Color(0xFFFF8A65), filter == MistakeFilter.PENDING, Modifier.weight(1f)) { filter = MistakeFilter.PENDING }
                    MistakeStat("Çözüldü", all.count { it.solved }, Color(0xFF54B7A3), filter == MistakeFilter.SOLVED, Modifier.weight(1f)) { filter = MistakeFilter.SOLVED }
                    MistakeStat("Toplam", all.size, Color(0xFF4EA7E8), filter == MistakeFilter.ALL, Modifier.weight(1f)) { filter = MistakeFilter.ALL }
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
                    item { Text(date, fontWeight = FontWeight.Bold, color = Color(0xFF607D8B), modifier = Modifier.padding(top = 6.dp)) }
                    items(entries, key = { it.id }) { entry ->
                        MistakeRow(
                            entry = entry,
                            onClick = { navController.navigate("mistake_review/${entry.id}") },
                            onDelete = { deleteTarget = entry }
                        )
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
            text = { Text("${entry.quizTitle} içindeki ${entry.questionNumber}. soru Mistake Jar listesinden kaldırılacak.") },
            confirmButton = { TextButton(onClick = { MistakeJarStore.remove(context, entry.id); deleteTarget = null }) { Text("Sil", color = Color(0xFFD84343)) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Vazgeç") } }
        )
    }
}

@Composable
private fun MistakeStat(label: String, count: Int, accent: Color, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(92.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) accent.copy(alpha = .18f) else Color.White,
        border = BorderStroke(if (selected) 2.dp else 1.dp, accent.copy(alpha = if (selected) .85f else .25f))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(count.toString(), fontSize = 24.sp, fontWeight = FontWeight.Black, color = accent)
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34495E), maxLines = 1)
        }
    }
}

@Composable
private fun MistakeRow(entry: MistakeEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, if (entry.solved) Color(0xFF90D6C8) else Color(0xFFFFCCBC))
    ) {
        Row(Modifier.padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(42.dp).background(if (entry.solved) Color(0xFFE0F4EF) else Color(0xFFFFEEE8), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (entry.solved) Icons.Default.Check else Icons.Default.ErrorOutline, null, tint = if (entry.solved) Color(0xFF278B76) else Color(0xFFE56F51))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.quizTitle, fontWeight = FontWeight.ExtraBold, color = Color(0xFF24394E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${entry.questionNumber}. soru · ${questionTypeLabel(entry.questionType)}", fontSize = 13.sp, color = Color(0xFF607D8B))
                Text(entry.questionText, fontSize = 13.sp, color = Color(0xFF455A64), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close, "Listeden sil", tint = Color(0xFF9AA7B2)) }
        }
    }
}

@Composable
private fun EmptyMistakeJar(pending: Boolean) {
    Column(Modifier.fillMaxWidth().padding(vertical = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.TaskAlt, null, modifier = Modifier.size(56.dp), tint = Color(0xFF54B7A3))
        Spacer(Modifier.height(12.dp))
        Text(if (pending) "Kontrol edilecek hata kalmadı" else "Bu bölüm henüz boş", fontWeight = FontWeight.Bold, color = Color(0xFF40566D))
        Text("Yeni öğrendiklerin burada düzenli biçimde görünecek.", color = Color(0xFF78909C), fontSize = 14.sp)
    }
}

@Composable
fun MistakeReviewScreen(navController: NavHostController, mistakeId: String) {
    val context = LocalContext.current
    MistakeJarStore.load(context)
    WordStorage.loadFromDisk(context)
    val entry = MistakeJarStore.mistakes.firstOrNull { it.id == mistakeId }
    if (entry == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }
    val question = entry.toQuestion()
    var selected by remember { mutableStateOf<String?>(null) }
    var checked by remember { mutableStateOf(false) }
    var showWords by remember { mutableStateOf(false) }
    val isCorrect = selected?.trim()?.equals(question.correctAnswer.trim(), ignoreCase = true) == true

    Scaffold(
        containerColor = Color(0xFFF7FBFE),
        topBar = {
            TopAppBar(
                title = { Column { Text(entry.quizTitle, fontWeight = FontWeight.Bold); Text("${entry.questionNumber}. soruyu tekrar et", fontSize = 12.sp, color = Color(0xFF607D8B)) } },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, "Kapat") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE5F5FD))
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = if (!checked) Color.White else if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)) {
                Column(Modifier.padding(16.dp).navigationBarsPadding()) {
                    if (checked) {
                        Text(if (isCorrect) "Doğru cevap, harika!" else "Doğru cevap: ${question.correctAnswer}", fontWeight = FontWeight.Black, color = if (isCorrect) Color(0xFF257A55) else Color(0xFFC84747))
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (!checked) {
                                checked = true
                                if (isCorrect) {
                                    MistakeJarStore.markSolved(context, entry.id)
                                    AchievementManager.unlockBadge(context, "common_first_fix")
                                    val unsolvedExist = MistakeJarStore.mistakes.any { !it.solved }
                                    if (!unsolvedExist) {
                                        AchievementManager.unlockBadge(context, "rare_jar_emptier")
                                    }
                                }
                            } else showWords = true
                        },
                        enabled = selected != null,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) { Text(if (checked) "KELİME SEÇ" else "KONTROL ET", fontWeight = FontWeight.Bold) }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 24.dp, 20.dp, 130.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(question.questionTurkish, fontSize = 23.sp, fontWeight = FontWeight.Black, color = Color(0xFF24394E)) }
            if (question.imageRes != 0) {
                item { Image(painterResource(question.imageRes), null, Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Fit) }
            }
            items(question.options) { option ->
                val optionSelected = selected == option
                val optionColor = when {
                    checked && option.equals(question.correctAnswer, true) -> Color(0xFFDDF3E9)
                    checked && optionSelected -> Color(0xFFFFE1E1)
                    optionSelected -> Color(0xFFE1F3FD)
                    else -> Color.White
                }
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp).clickable(enabled = !checked) { selected = option },
                    shape = RoundedCornerShape(8.dp), color = optionColor,
                    border = BorderStroke(if (optionSelected) 2.dp else 1.dp, if (optionSelected) Color(0xFF2196F3) else Color(0xFFD7E4EC))
                ) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Text(option, Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = Color(0xFF30475B)); if (optionSelected) Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2196F3)) } }
            }
        }
    }

    if (showWords) {
        SaveQuizWordsDialog(
            entry = entry,
            onDismiss = { showWords = false },
            onFinish = { showWords = false; navController.popBackStack() }
        )
    }
}

@Composable
private fun SaveQuizWordsDialog(entry: MistakeEntry, onDismiss: () -> Unit, onFinish: () -> Unit) {
    val context = LocalContext.current
    val words = remember(entry.id) { extractEnglishWords(listOf(entry.questionText, entry.correctAnswer) + entry.options) }
    val selected = remember { mutableStateListOf<String>() }
    val translations = remember { mutableStateMapOf<String, String>() }
    var translating by remember { mutableStateOf(false) }
    val translator = remember {
        Translation.getClient(
            TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.TURKISH).build()
        )
    }
    DisposableEffect(Unit) { onDispose { translator.close() } }

    fun saveAndFinish() {
        WordStorage.loadFromDisk(context)
        selected.forEach { word ->
            if (WordStorage.savedWords.none { it.word.equals(word, true) }) {
                WordStorage.savedWords.add(SavedWord(word, translations[word] ?: AppDictionary.words[word.lowercase()] ?: ""))
            }
        }
        WordStorage.saveToDisk(context)
        onFinish()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kaydetmek istediğin kelime var mı?", fontWeight = FontWeight.Black) },
        text = {
            if (words.isEmpty()) Text("Bu soruda kaydedilebilecek İngilizce bir kelime bulunamadı.")
            else LazyColumn(Modifier.heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(words) { word ->
                    val checked = word in selected
                    Surface(
                        Modifier.fillMaxWidth().clickable {
                            if (checked) selected.remove(word) else {
                                selected.add(word)
                                if (translations[word] == null) {
                                    translating = true
                                    translator.downloadModelIfNeeded(DownloadConditions.Builder().build())
                                        .continueWithTask { translator.translate(word) }
                                        .addOnSuccessListener { translations[word] = it; translating = false }
                                        .addOnFailureListener { translations[word] = AppDictionary.words[word.lowercase()] ?: "Çeviri alınamadı"; translating = false }
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (checked) Color(0xFFE3F2FD) else Color(0xFFF8FAFC)
                    ) {
                        Row(Modifier.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked, onCheckedChange = null)
                            Column { Text(word, fontWeight = FontWeight.Bold); if (checked) Text(translations[word] ?: "Çevriliyor...", fontSize = 13.sp, color = Color(0xFF607D8B)) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = ::saveAndFinish, enabled = !translating) { Text(if (selected.isEmpty()) "Atla" else "Kelimelerime kaydet") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Geri dön") } }
    )
}

private fun extractEnglishWords(texts: List<String>): List<String> =
    texts.flatMap { Regex("[A-Za-z]+(?:'[A-Za-z]+)?").findAll(it).map { match -> match.value.lowercase() }.toList() }
        .filter { it.length > 1 }.distinct().sorted()

private fun formatMistakeDate(timestamp: Long): String =
    SimpleDateFormat("dd MMMM yyyy, EEEE", Locale("tr", "TR")).format(Date(timestamp))

private fun questionTypeLabel(type: String) = when (type) {
    QuestionType.LISTENING.name -> "Dinleme"
    QuestionType.MATCHING.name -> "Eşleştirme"
    QuestionType.FILL_IN_BLANK.name -> "Boşluk doldurma"
    else -> "Görsel seçim"
}
