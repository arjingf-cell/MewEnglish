package com.example.ngilizcequizapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Custom geometry per wing rank to ensure the inner cat avatar
 * fits perfectly into each rank's unique ornate center opening.
 */
data class WingHoleMetrics(
    val holeRatio: Float,    // Ratio of inner avatar circle diameter to wingHeight
    val yOffsetRatio: Float  // Vertical shift relative to wingHeight
)

fun getWingHoleMetrics(wingDrawableRes: Int): WingHoleMetrics {
    return when (wingDrawableRes) {
        R.drawable.bronze_kanat -> WingHoleMetrics(holeRatio = 0.580f, yOffsetRatio = -0.041f)
        R.drawable.silver_kanat -> WingHoleMetrics(holeRatio = 0.587f, yOffsetRatio = -0.039f)
        R.drawable.gold_kanat -> WingHoleMetrics(holeRatio = 0.589f, yOffsetRatio = -0.043f)
        R.drawable.plat_kanat -> WingHoleMetrics(holeRatio = 0.536f, yOffsetRatio = -0.027f)
        R.drawable.elmas_kanat -> WingHoleMetrics(holeRatio = 0.505f, yOffsetRatio = 0.011f)
        R.drawable.master_kanat -> WingHoleMetrics(holeRatio = 0.497f, yOffsetRatio = 0.004f)
        R.drawable.grandmaster_kanat -> WingHoleMetrics(holeRatio = 0.514f, yOffsetRatio = 0.004f)
        R.drawable.linguarium_kanat -> WingHoleMetrics(holeRatio = 0.475f, yOffsetRatio = 0.030f)
        R.drawable.kingdom_kanat -> WingHoleMetrics(holeRatio = 0.525f, yOffsetRatio = -0.015f)
        else -> WingHoleMetrics(holeRatio = 0.520f, yOffsetRatio = 0f)
    }
}

@Composable
fun ProfileWingDecoration(
    wingId: String,
    rank: RankType,
    modifier: Modifier = Modifier,
    avatarDiameter: Dp = 54.dp,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val wingRes = remember(wingId, rank, UserCosmeticsStore.stateRevision) {
        if (wingId.isNotBlank() && wingId != "rank_default") {
            UserCosmeticsStore.predefinedWings.find { it.id.equals(wingId, ignoreCase = true) }?.drawableRes
                ?: UserCosmeticsStore.getWingDrawableForRank(rank)
        } else {
            UserCosmeticsStore.getEquippedWingDrawable(context, rank)
        }
    }

    val wingHeight = avatarDiameter * 1.85f
    val wingWidth = wingHeight * 1.84f

    Box(
        modifier = modifier
            .size(width = wingWidth, height = wingHeight)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = wingRes),
            contentDescription = "Wing Asset",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Unified Component: Profile Cat Avatar surrounded and framed by the Symmetrical Wing asset.
 * - Sized statically without breathing animation for razor-sharp alignment.
 * - Perfectly tailored center hole & vertical offset per rank.
 * - Clicking the central cat circle calls [onCatClick].
 * - Clicking the wings frame calls [onWingClick].
 */
@Composable
fun ProfileAvatarWithWings(
    avatarRes: Int,
    wingDrawableRes: Int,
    modifier: Modifier = Modifier,
    wingHeight: Dp = 56.dp,
    avatarDiameter: Dp = 54.dp,
    onCatClick: (() -> Unit)? = null,
    onWingClick: (() -> Unit)? = null
) {
    val metrics = remember(wingDrawableRes) { getWingHoleMetrics(wingDrawableRes) }
    val wingWidth = wingHeight * 1.84f
    val avatarSize = wingHeight * metrics.holeRatio * 1.30f
    val avatarYOffset = wingHeight * metrics.yOffsetRatio

    Box(
        modifier = modifier
            .size(width = wingWidth, height = wingHeight)
            .then(
                if (onWingClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onWingClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Central Circular Cat Avatar Layer (Underneath the frame, perfectly fitted)
        Box(
            modifier = Modifier
                .offset(y = avatarYOffset)
                .size(avatarSize)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "Cat Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        // 2. Wing Frame Layer (Framing and overlapping smoothly)
        Image(
            painter = painterResource(id = wingDrawableRes),
            contentDescription = "Wing Frame",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Central Cat Click Target (Overlays center so clicking cat triggers onCatClick)
        if (onCatClick != null) {
            Box(
                modifier = Modifier
                    .offset(y = avatarYOffset)
                    .size(avatarSize)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCatClick
                    )
            )
        }
    }
}
