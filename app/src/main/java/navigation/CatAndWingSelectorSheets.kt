package com.example.ngilizcequizapp

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatSelectorBottomSheet(
    isForTopBar: Boolean = false,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val currentAvatar = if (isForTopBar) {
        UserCosmeticsStore.getEquippedTopBarAvatar(context)
    } else {
        UserCosmeticsStore.getEquippedAvatar(context)
    }
    val appColors = LocalAppThemeColors.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = appColors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isForTopBar) "🐱 Üst Bar Simgesini Değiştir" else "🐱 Kedi Profilini Değiştir",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = appColors.text
                    )
                    Text(
                        text = if (isForTopBar) "Üst barda görünmesini istediğin Mew'i seç" else "Kanatlarının içinde kullanmak istediğin Mew'i seç",
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = appColors.textMuted)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Mew Cats Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(UserCosmeticsStore.allMewCats) { cat ->
                    val isSelected = currentAvatar.equals(cat.id, ignoreCase = true)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) appColors.primary.copy(alpha = 0.08f) else appColors.background,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) appColors.primary else appColors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isForTopBar) {
                                    UserCosmeticsStore.setEquippedTopBarAvatar(context, cat.id)
                                    Toast.makeText(context, "${cat.name} üst bar simgesi yapıldı!", Toast.LENGTH_SHORT).show()
                                } else {
                                    UserCosmeticsStore.setEquippedAvatar(context, cat.id)
                                    Toast.makeText(context, "${cat.name} profil resmi yapıldı!", Toast.LENGTH_SHORT).show()
                                }
                                onDismissRequest()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color.White, CircleShape)
                                    .border(2.5.dp, if (isSelected) appColors.primary else appColors.border, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = cat.drawableRes),
                                    contentDescription = cat.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(3.dp)
                                        .clip(CircleShape)
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = cat.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.text,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = cat.title,
                                fontSize = 10.sp,
                                color = appColors.textMuted,
                                textAlign = TextAlign.Center
                            )

                            Spacer(Modifier.height(6.dp))

                            if (isSelected) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Seçili",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WingSelectorBottomSheet(
    currentRank: RankType,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val currentWingId = UserCosmeticsStore.getEquippedWingId(context)
    val appColors = LocalAppThemeColors.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = appColors.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "🪽 Kanat / Rozeti Değiştir",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = appColors.text
                    )
                    Text(
                        text = "Avatarını çevreleyecek kanat ve rozeti seç",
                        fontSize = 12.sp,
                        color = appColors.textMuted
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat", tint = appColors.textMuted)
                }
            }

            Spacer(Modifier.height(18.dp))

            // Wings Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(UserCosmeticsStore.predefinedWings) { wing ->
                    val isSelected = currentWingId == wing.id

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) appColors.primary.copy(alpha = 0.08f) else appColors.background,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) appColors.primary else appColors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                UserCosmeticsStore.setEquippedWingId(context, wing.id)
                                Toast.makeText(context, "${wing.name} kuşanıldı!", Toast.LENGTH_SHORT).show()
                                onDismissRequest()
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Wing Preview
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = wing.drawableRes),
                                    contentDescription = wing.name,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = wing.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.text,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = wing.description,
                                fontSize = 9.sp,
                                color = appColors.textMuted,
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )

                            Spacer(Modifier.height(4.dp))

                            if (isSelected) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = "Kuşanıldı",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
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
