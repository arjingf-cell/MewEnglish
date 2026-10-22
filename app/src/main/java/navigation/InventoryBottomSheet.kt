package com.example.ngilizcequizapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryBottomSheet(
    onDismissRequest: () -> Unit,
    navController: NavHostController,
    userRank: RankType
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Current state from cosmetics store
    val currentWingId = UserCosmeticsStore.getEquippedWingId(context)
    val currentAvatar = UserCosmeticsStore.getEquippedAvatar(context)
    val currentFrame = UserCosmeticsStore.getEquippedFrame(context)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showThemeLoadingOverlay by remember { mutableStateOf(false) }
    var loadingThemeId by remember { mutableStateOf("uzay") }
    val tabs = listOf("Kanat / Rozet", "Karakterler", "Temalar", "Çerçeveler")

    val appColors = LocalAppThemeColors.current
    val cardColor = appColors.surface
    val textColor = appColors.text
    val mutedColor = appColors.textMuted
    val primaryColor = appColors.primary
    val borderColor = appColors.border

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = appColors.background,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .background(mutedColor.copy(alpha = 0.3f), CircleShape)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header Row: Title and Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(primaryColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.school_bag),
                            contentDescription = "Envanter Çantası",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Envanterim & Çantam",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Text(
                            text = "Sahip olduğun eşyaları dilediğin gibi kuşan",
                            fontSize = 11.sp,
                            color = mutedColor
                        )
                    }
                }

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = mutedColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tabs Row
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Content per tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> WingsInventoryTab(
                        context = context,
                        currentWingId = currentWingId,
                        userRank = userRank,
                        cardColor = cardColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        primaryColor = primaryColor,
                        borderColor = borderColor
                    )
                    1 -> AvatarsInventoryTab(
                        context = context,
                        currentAvatar = currentAvatar,
                        cardColor = cardColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        primaryColor = primaryColor,
                        borderColor = borderColor
                    )
                    2 -> ThemesInventoryTab(
                        context = context,
                        cardColor = cardColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        primaryColor = primaryColor,
                        borderColor = borderColor,
                        onEquipTheme = { themeId ->
                            UserCosmeticsStore.setEquippedTheme(context, themeId)
                            loadingThemeId = themeId
                            showThemeLoadingOverlay = true
                        }
                    )
                    3 -> FramesInventoryTab(
                        context = context,
                        currentFrame = currentFrame,
                        cardColor = cardColor,
                        textColor = textColor,
                        mutedColor = mutedColor,
                        primaryColor = primaryColor,
                        borderColor = borderColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Footer shortcut to Market
            if (AppConfig.IS_MARKET_ENABLED) {
                Button(
                    onClick = {
                        onDismissRequest()
                        navController.navigate(Screen.Market.route)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Daha Fazla Eşya Keşfet (Markete Git)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "✨ Market Mağazası Çok Yakında Açılıyor!",
                            fontSize = 12.sp,
                            color = mutedColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showThemeLoadingOverlay) {
        UniversalThemeLoadingOverlay(
            themeId = loadingThemeId,
            onFinished = {
                showThemeLoadingOverlay = false
                onDismissRequest()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )
    }
}

@Composable
private fun WingsInventoryTab(
    context: Context,
    currentWingId: String,
    userRank: RankType,
    cardColor: Color,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color,
    borderColor: Color
) {
    val ownedWings = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.predefinedWings.filter {
            UserCosmeticsStore.isWingOwned(context, it.id)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(ownedWings) { wing ->
            val isEquipped = currentWingId == wing.id

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor,
                border = BorderStroke(
                    width = if (isEquipped) 2.dp else 1.dp,
                    color = if (isEquipped) primaryColor else borderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Mini Wing Preview
                    Box(
                        modifier = Modifier
                            .size(width = 118.dp, height = 58.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ProfileWingDecoration(
                            wingId = wing.id,
                            rank = userRank,
                            avatarDiameter = 42.dp
                        )
                        val equippedAvatarRes = remember(UserCosmeticsStore.stateRevision) {
                            UserCosmeticsStore.getEquippedAvatarRes(context)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(cardColor, CircleShape)
                                .border(1.5.dp, if (isEquipped) primaryColor else borderColor.copy(alpha = 0.8f), CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = equippedAvatarRes),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = wing.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = wing.description,
                        fontSize = 10.sp,
                        color = mutedColor,
                        maxLines = 2,
                        minLines = 2,
                        lineHeight = 12.sp,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(8.dp))

                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Kuşanıldı",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                UserCosmeticsStore.setEquippedWingId(context, wing.id)
                                Toast.makeText(context, "${wing.name} Kuşanıldı!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Text("Kuşan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarsInventoryTab(
    context: Context,
    currentAvatar: String,
    cardColor: Color,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color,
    borderColor: Color
) {
    val allCats = listOf(
        Pair("Akademisyen Mew", "akademisyen_mew"),
        Pair("Havalı Mew", "cool_mew"),
        Pair("Kaptan Mew", "pirate_mew"),
        Pair("Büyücü Mew", "wizard_mew"),
        Pair("Maestro Mew", "maestro_mew"),
        Pair("Kral Mew", "king_mew")
    )

    val ownedCats = remember(UserCosmeticsStore.stateRevision) {
        allCats.filter { UserCosmeticsStore.isAvatarOwned(context, it.second) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(ownedCats) { (name, iconName) ->
            val isEquipped = UserCosmeticsStore.isAvatarEquipped(context, iconName)
            val drawableRes = getMarketDrawableRes(context, iconName)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor,
                border = BorderStroke(
                    width = if (isEquipped) 2.dp else 1.dp,
                    color = if (isEquipped) primaryColor else borderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(primaryColor.copy(alpha = 0.08f), CircleShape)
                            .border(2.dp, if (isEquipped) primaryColor else borderColor, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(8.dp))

                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Kuşanıldı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                UserCosmeticsStore.setEquippedAvatar(context, iconName)
                                Toast.makeText(context, "$name Profil Resmi Yapıldı!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Text("Kuşan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemesInventoryTab(
    context: Context,
    cardColor: Color,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color,
    borderColor: Color,
    onEquipTheme: (String) -> Unit
) {
    val currentTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.getEquippedTheme(context)
    }
    val isSpaceActive = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }
    val isKingdomActive = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isKingdomThemeActive(context)
    }

    val allThemes = listOf(
        Triple("Krallık", "kingdom", "👑"),
        Triple("Space", "uzay", "🪐"),
        Triple("Klasik", "klasik", "☀️"),
        Triple("Dark", "dark", "🌙")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(allThemes) { (name, id, icon) ->
            val isEquipped = when (id) {
                "uzay" -> isSpaceActive
                "kingdom" -> isKingdomActive
                else -> (!isSpaceActive && !isKingdomActive && currentTheme == id)
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor,
                border = BorderStroke(
                    width = if (isEquipped) 2.dp else 1.dp,
                    color = if (isEquipped) Color(0xFF38BDF8) else borderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(primaryColor.copy(alpha = 0.1f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = icon,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(8.dp))

                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Kuşanıldı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                onEquipTheme(id)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Text("Kuşan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FramesInventoryTab(
    context: Context,
    currentFrame: String,
    cardColor: Color,
    textColor: Color,
    mutedColor: Color,
    primaryColor: Color,
    borderColor: Color
) {
    val allFrames = listOf(
        Triple("Mavi Çerçeve", "blue", Color(0xFF1CB0F6)),
        Triple("Altın Çerçeve", "altin", Color(0xFFFFD700)),
        Triple("Çiçek Çerçeve", "cicek", Color(0xFFF43F5E)),
        Triple("Neon Çerçeve", "neon", Color(0xFF00E5FF)),
        Triple("Kraliyet Çerçevesi", "kraliyet", Color(0xFFFFB300))
    )

    val ownedFrames = remember(UserCosmeticsStore.stateRevision) {
        allFrames.filter { UserCosmeticsStore.isFrameOwned(context, it.second) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(ownedFrames) { (name, frameId, frameColor) ->
            val isEquipped = currentFrame == frameId

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardColor,
                border = BorderStroke(
                    width = if (isEquipped) 2.dp else 1.dp,
                    color = if (isEquipped) primaryColor else borderColor
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val equippedAvatarRes = remember(UserCosmeticsStore.stateRevision) {
                        UserCosmeticsStore.getEquippedAvatarRes(context)
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(cardColor, CircleShape)
                            .border(3.dp, frameColor, CircleShape)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = equippedAvatarRes),
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)

                    Spacer(Modifier.height(8.dp))

                    if (isEquipped) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Kuşanıldı", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                UserCosmeticsStore.setEquippedFrame(context, frameId)
                                Toast.makeText(context, "$name Kuşanıldı!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                        ) {
                            Text("Kuşan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
