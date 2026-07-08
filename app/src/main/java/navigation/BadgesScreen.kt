@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ngilizcequizapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.ui.layout.ContentScale

@Composable
fun ProceduralBadge(
    rarity: BadgeRarity,
    isUnlocked: Boolean,
    name: String,
    modifier: Modifier = Modifier
) {
    val color = rarity.color
    
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = Math.min(w, h) * 0.4f
        
        // Çokgen kalkan şekli çizimi
        val path = Path()
        val numPoints = 8
        for (i in 0 until numPoints) {
            val angle = i * (2 * Math.PI / numPoints) - Math.PI / 2
            val radius = if (i % 2 == 0) r else r * 0.8f
            val x = cx + radius * Math.cos(angle).toFloat()
            val y = cy + radius * Math.sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        
        // Kalkanı doldur
        drawPath(
            path = path,
            brush = Brush.radialGradient(
                colors = if (isUnlocked) listOf(color.copy(alpha = 0.7f), color) else listOf(Color(0xFF94A3B8), Color(0xFF64748B)),
                center = Offset(cx, cy),
                radius = r
            )
        )
        
        // İç halka çizgisi
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = r * 0.65f,
            center = Offset(cx, cy)
        )
    }
    
    // Harf
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp
        )
    }
}

@Composable
fun BadgeImage(
    badge: BadgeData,
    context: Context,
    modifier: Modifier = Modifier,
    isUnlocked: Boolean = true
) {
    val resId = remember(badge.assetName) {
        val cleanName = badge.assetName.substringBeforeLast(".")
        context.resources.getIdentifier(cleanName, "drawable", context.packageName)
    }

    val alpha = if (isUnlocked) 1f else 0.3f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (isUnlocked) Color(0xFF1CB0F6) else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        if (resId != 0) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = badge.name,
                modifier = Modifier.fillMaxSize().scale(1.1f).offset(y = (-3).dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                colorFilter = if (isUnlocked) null else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
                alpha = 1f
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = if (isUnlocked) Color(0xFF1CB0F6) else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                ProceduralBadge(rarity = badge.rarity, isUnlocked = isUnlocked, name = badge.name)
            }
        }

        // Padlock overlay if locked
        if (!isUnlocked) {
            val padlockId = remember {
                context.resources.getIdentifier("padlock", "drawable", context.packageName)
            }
            if (padlockId != 0) {
                Image(
                    painter = painterResource(id = padlockId),
                    contentDescription = "Kilitli",
                    modifier = Modifier.size(24.dp),
                    alpha = 0.9f
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Kilitli",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

    }
}

@Composable
fun BadgePopupDialog(
    badge: BadgeData,
    isUnlocked: Boolean,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeImage(
                    badge = badge,
                    context = LocalContext.current,
                    modifier = Modifier.size(72.dp),
                    isUnlocked = isUnlocked
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge.name,
                        color = Color(0xFF00E5FF), // açık mavi
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.description,
                        color = Color(0xFFCBD5E1), // açık gri
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isUnlocked) "Rozet Kazanıldı!" else "Kilitli (Simülasyon panelinden açabilirsiniz)",
                        color = if (isUnlocked) Color(0xFF22C55E) else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun BadgesScreen(navController: NavController) {
    val context = LocalContext.current
    var selectedBadge by remember { mutableStateOf<BadgeData?>(null) }
    var showSimulator by remember { mutableStateOf(false) }

    val window = remember(context) {
        (context as? android.app.Activity)?.window
    }

    // Initialize badges state
    LaunchedEffect(Unit) {
        AchievementManager.init(context)
        AchievementManager.unlockBadge(context, "common_explorer")
        window?.let {
            it.statusBarColor = android.graphics.Color.parseColor("#F8FAFC")
            it.navigationBarColor = android.graphics.Color.parseColor("#F8FAFC")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rozetlerim",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 🛠 Simülasyon Paneli Butonu
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Rozet Test Simülatörü",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF),
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { showSimulator = !showSimulator },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    if (showSimulator) "Gizle" else "Göster",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        AnimatedVisibility(visible = showSimulator) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Aşağıdaki butonlara tıklayarak istediğiniz rozeti anında açabilir, toast bildirimini ve kilit açılma animasyonlarını test edebilirsiniz:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Category wise badge trigger buttons
                                val categories = BadgeRarity.values()
                                categories.forEach { rarity ->
                                    Text(
                                        rarity.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = rarity.color,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val catBadges = AchievementManager.badges.filter { it.rarity == rarity }
                                        catBadges.forEach { badge ->
                                            val isUnlocked = AchievementManager.isUnlocked(badge.id)
                                            Button(
                                                onClick = {
                                                    if (isUnlocked) {
                                                        AchievementManager.lockBadge(context, badge.id)
                                                    } else {
                                                        AchievementManager.unlockBadge(context, badge.id)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isUnlocked) Color(0xFF10B981) else Color(0xFF94A3B8)
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(24.dp).padding(bottom = 4.dp)
                                            ) {
                                                Text(
                                                    badge.name + (if (isUnlocked) " 🔓" else " 🔒"),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Rozet Kategorileri
            val rarities = BadgeRarity.values()
            rarities.forEach { rarity ->
                val rarityBadges = AchievementManager.badges.filter { it.rarity == rarity }
                if (rarityBadges.isNotEmpty()) {
                    item {
                        Text(
                            text = rarity.displayName.uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = rarity.color,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    // Grid representation inside LazyColumn using chunks of 3 items
                    val chunks = rarityBadges.chunked(3)
                    items(chunks) { rowBadges ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowBadges.forEach { badge ->
                                val isUnlocked = AchievementManager.isUnlocked(badge.id)
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedBadge = badge
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        BadgeImage(
                                            badge = badge,
                                            context = context,
                                            modifier = Modifier.size(60.dp),
                                            isUnlocked = isUnlocked
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = badge.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) Color(0xFF1E293B) else Color.Gray,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                            // Add empty space fillers for row alignment
                            if (rowBadges.size < 3) {
                                for (i in 0 until (3 - rowBadges.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedBadge?.let { badge ->
        BadgePopupDialog(
            badge = badge,
            isUnlocked = AchievementManager.isUnlocked(badge.id),
            onDismiss = { selectedBadge = null }
        )
    }
}

// FlowRow implementation since standard flow layout is in compose.foundation in newer versions but sometimes requires experimental or extra imports.
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val layoutWidth = constraints.maxWidth
        
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        
        placeables.forEach { placeable ->
            if (currentRowWidth + placeable.width > layoutWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + 12 // spacing
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        var height = 0
        rows.forEach { row ->
            val rowHeight = row.maxOf { it.height }
            height += rowHeight + 8 // row spacing
        }
        
        layout(layoutWidth, height.coerceAtLeast(0)) {
            var y = 0
            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOf { it.height }
                row.forEach { placeable ->
                    placeable.placeRelative(x, y + (rowHeight - placeable.height) / 2)
                    x += placeable.width + 12
                }
                y += rowHeight + 8
            }
        }
    }
}

@Composable
fun AchievementToast(
    badge: BadgeData,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(badge) {
        isVisible = true
        delay(6500) // Persists for exactly 6.5 seconds
        isVisible = false
        delay(500)
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeImage(
                    badge = badge,
                    context = LocalContext.current,
                    modifier = Modifier.size(56.dp),
                    isUnlocked = true
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge.name,
                        color = Color(0xFF1CB0F6),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.description,
                        color = Color(0xFF475569),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
