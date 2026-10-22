package com.example.ngilizcequizapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Data representation for a single twinkling celestial star.
 */
private data class CosmicStar(
    val xRatio: Float,
    val yRatio: Float,
    val radiusDp: Float,
    val minAlpha: Float,
    val maxAlpha: Float,
    val phaseOffset: Float,
    val color: Color
)

/**
 * High-performance Space Theme animated background.
 * - Deep Cosmic Gradient Backdrop
 * - 80 Procedural Twinkling Stars (Sinusoidal pulsating alpha)
 * - Dynamic Occasional Shooting Stars (Meteors) with glowing trails every 4-7 seconds
 * - Alternating Drifting Earth & Moon (Left -> Right, delay, Right -> Left)
 * - Automatically hidden during quiz solving (showPlanets = false)
 */
@Composable
fun SpaceThemeBackground(
    modifier: Modifier = Modifier,
    showPlanets: Boolean = true,
    forceShow: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val isSpaceTheme = remember(UserCosmeticsStore.stateRevision) {
        UserCosmeticsStore.isSpaceThemeActive(context)
    }

    if (!isSpaceTheme && !forceShow) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    // 1. Generate deterministic starry night field once
    val stars = remember {
        val random = Random(42) // Stable seed so stars don't jump around
        val starColors = listOf(
            Color(0xFFFFFFFF), // Pure white
            Color(0xFFE0F2FE), // Soft Ice Blue
            Color(0xFFBAE6FD), // Sky Blue
            Color(0xFFFEF08A), // Warm Golden Tint
            Color(0xFFDDD6FE)  // Soft Violet
        )
        List(45) {
            CosmicStar(
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                radiusDp = 0.8f + random.nextFloat() * 1.8f,
                minAlpha = 0.15f + random.nextFloat() * 0.25f,
                maxAlpha = 0.70f + random.nextFloat() * 0.30f,
                phaseOffset = random.nextFloat() * 2f * PI.toFloat(),
                color = starColors[random.nextInt(starColors.size)]
            )
        }
    }

    // 2. Global infinite transition for smooth twinkling breathing
    val infiniteTransition = rememberInfiniteTransition(label = "SpaceTwinkle")
    val twinkleCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkleCycle"
    )

    // Orbital slow sine wave for Moon & vertical floating of planets
    val planetFloatCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "planetFloatCycle"
    )

    // 3. Occasional Shooting Star (Meteor) controller
    var meteorActive by remember { mutableStateOf(false) }
    var meteorStartX by remember { mutableFloatStateOf(0f) }
    var meteorStartY by remember { mutableFloatStateOf(0f) }
    val meteorProgress = remember { Animatable(0f) }

    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp.dp
    val screenWidthPx = remember(config) { config.screenWidthDp * 2.7f }

    LaunchedEffect(Unit) {
        val meteorRandom = Random(1337)
        while (true) {
            val delayMillis = 4500L + meteorRandom.nextLong(3000L)
            delay(delayMillis)

            meteorStartX = meteorRandom.nextFloat() * screenWidthPx * 0.9f
            meteorStartY = meteorRandom.nextFloat() * 400f
            meteorProgress.snapTo(0f)
            meteorActive = true

            meteorProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 850,
                    easing = FastOutSlowInEasing
                )
            )
            meteorActive = false
        }
    }

    // 4. Alternating Earth & Moon Drift Controller (Left -> Right, Pause, Right -> Left, Pause)
    var isMovingLeftToRight by remember { mutableStateOf(true) }
    val planetProgress = remember { Animatable(0f) }

    LaunchedEffect(showPlanets) {
        if (!showPlanets) return@LaunchedEffect
        while (true) {
            // Animate across screen in 20 seconds (graceful cosmic transit)
            planetProgress.snapTo(0f)
            planetProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 20000,
                    easing = LinearEasing
                )
            )
            // Wait 8 seconds off-screen before returning in the opposite direction
            delay(8000L)
            // Reverse direction for next transit
            isMovingLeftToRight = !isMovingLeftToRight
        }
    }

    // Background cosmic gradient colors
    val cosmicGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF030611), // Midnight Black-Navy
                Color(0xFF060D24), // Deep Cosmic Indigo
                Color(0xFF0B1437), // Nebula Navy
                Color(0xFF040816)  // Bottom Void
            )
        )
    }

    Box(modifier = modifier.fillMaxSize().background(cosmicGradient)) {
        // 🌟 Starfield & Shooting Star Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // A. Draw Twinkling Stars
            stars.forEach { star ->
                val x = star.xRatio * width
                val y = star.yRatio * height
                val sineVal = sin(twinkleCycle + star.phaseOffset)
                val normalizedSine = (sineVal + 1f) / 2f
                val alpha = star.minAlpha + (star.maxAlpha - star.minAlpha) * normalizedSine
                val radius = star.radiusDp.dp.toPx()

                drawCircle(
                    color = star.color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )

                if (star.radiusDp > 1.8f && alpha > 0.65f) {
                    drawCircle(
                        color = star.color.copy(alpha = alpha * 0.35f),
                        radius = radius * 2.2f,
                        center = Offset(x, y)
                    )
                }
            }

            // B. Draw Occasional Shooting Star (Meteor)
            if (meteorActive) {
                val progress = meteorProgress.value
                val streakLength = 260f
                val travelDistance = 500f
                val angleRad = 0.70f // ~40 degrees slope

                val currentHeadX = meteorStartX + progress * travelDistance * cos(angleRad)
                val currentHeadY = meteorStartY + progress * travelDistance * sin(angleRad)

                val tailX = currentHeadX - streakLength * cos(angleRad)
                val tailY = currentHeadY - streakLength * sin(angleRad)

                val meteorAlpha = when {
                    progress < 0.2f -> progress / 0.2f
                    progress > 0.7f -> (1f - progress) / 0.3f
                    else -> 1f
                }.coerceIn(0f, 1f)

                if (meteorAlpha > 0.01f) {
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0x00FFFFFF),
                                Color(0x8038BDF8),
                                Color(0xFFE0F2FE).copy(alpha = meteorAlpha)
                            ),
                            start = Offset(tailX, tailY),
                            end = Offset(currentHeadX, currentHeadY)
                        ),
                        start = Offset(tailX, tailY),
                        end = Offset(currentHeadX, currentHeadY),
                        strokeWidth = 2.8f,
                        cap = StrokeCap.Round
                    )

                    drawCircle(
                        color = Color.White.copy(alpha = meteorAlpha),
                        radius = 3.5f,
                        center = Offset(currentHeadX, currentHeadY)
                    )
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = meteorAlpha * 0.6f),
                        radius = 7f,
                        center = Offset(currentHeadX, currentHeadY)
                    )
                }
            }
        }

        // 🌍 🌙 C. Drifting Earth and Moon (Zero-recomposition hardware accelerated graphicsLayer)
        if (showPlanets) {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val startXPx = remember(density) { with(density) { (-180).dp.toPx() } }
            val endXPx = remember(density, screenWidthDp) { with(density) { (screenWidthDp + 180.dp).toPx() } }
            val baseYPx = remember(density) { with(density) { 120.dp.toPx() } }
            val driftAmpPx = remember(density) { with(density) { 14.dp.toPx() } }
            val moonBaseXPx = remember(density) { with(density) { 54.dp.toPx() } }
            val moonOffsetYPx = remember(density) { with(density) { (-22).dp.toPx() } }
            val moonAmpXPx = remember(density) { with(density) { 36.dp.toPx() } }
            val moonAmpYPx = remember(density) { with(density) { 20.dp.toPx() } }

            Box(modifier = Modifier.fillMaxSize()) {
                // Minimalist Earth Container
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            val p = planetProgress.value
                            val currentX = if (isMovingLeftToRight) {
                                startXPx + (endXPx - startXPx) * p
                            } else {
                                endXPx - (endXPx - startXPx) * p
                            }
                            val currentY = baseYPx + sin(planetFloatCycle) * driftAmpPx
                            translationX = currentX
                            translationY = currentY
                            alpha = 0.95f
                        }
                ) {
                    MinimalistEarth(modifier = Modifier.fillMaxSize())
                }

                // Minimalist Moon Container (Drifting alongside Earth)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer {
                            val p = planetProgress.value
                            val currentX = if (isMovingLeftToRight) {
                                startXPx + (endXPx - startXPx) * p
                            } else {
                                endXPx - (endXPx - startXPx) * p
                            }
                            val currentY = baseYPx + sin(planetFloatCycle) * driftAmpPx
                            val mox = cos(planetFloatCycle) * moonAmpXPx
                            val moy = sin(planetFloatCycle) * moonAmpYPx
                            translationX = currentX + moonBaseXPx + mox
                            translationY = currentY + moonOffsetYPx + moy
                            alpha = 0.92f
                        }
                ) {
                    MinimalistMoon(modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Foreground content on top of space background
        content()
    }
}

/**
 * Clean, modern, minimalist vector-drawn Earth with atmosphere, continents, and subtle clouds.
 */
@Composable
fun MinimalistEarth(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Soft atmospheric outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x4038BDF8), Color(0x1538BDF8), Color.Transparent),
                center = center,
                radius = radius * 1.35f
            ),
            radius = radius * 1.35f,
            center = center
        )

        // 2. Earth ocean sphere with gradient
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF38BDF8), // Cyan blue
                    Color(0xFF0284C7), // Vibrant azure
                    Color(0xFF075985)  // Deep navy
                ),
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius)
            ),
            radius = radius,
            center = center
        )

        // 3. Minimalist landmass / continents
        // Continent 1: Upper-left continent (mint green)
        drawCircle(
            color = Color(0xFF34D399).copy(alpha = 0.90f),
            radius = radius * 0.42f,
            center = Offset(center.x - radius * 0.35f, center.y - radius * 0.28f)
        )
        drawCircle(
            color = Color(0xFF34D399).copy(alpha = 0.90f),
            radius = radius * 0.30f,
            center = Offset(center.x - radius * 0.15f, center.y - radius * 0.05f)
        )

        // Continent 2: Lower-right continent
        drawCircle(
            color = Color(0xFF4ADE80).copy(alpha = 0.85f),
            radius = radius * 0.38f,
            center = Offset(center.x + radius * 0.32f, center.y + radius * 0.25f)
        )

        // Island dot
        drawCircle(
            color = Color(0xFF34D399).copy(alpha = 0.85f),
            radius = radius * 0.12f,
            center = Offset(center.x + radius * 0.40f, center.y - radius * 0.30f)
        )

        // 4. Polar ice cap (subtle soft white arc at top)
        drawCircle(
            color = Color.White.copy(alpha = 0.30f),
            radius = radius * 0.28f,
            center = Offset(center.x + radius * 0.05f, center.y - radius * 0.85f)
        )

        // 5. Stylized minimalist cloud sweeps
        drawLine(
            color = Color.White.copy(alpha = 0.45f),
            start = Offset(center.x - radius * 0.65f, center.y - radius * 0.15f),
            end = Offset(center.x - radius * 0.10f, center.y - radius * 0.12f),
            strokeWidth = radius * 0.08f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.40f),
            start = Offset(center.x + radius * 0.05f, center.y + radius * 0.10f),
            end = Offset(center.x + radius * 0.60f, center.y + radius * 0.12f),
            strokeWidth = radius * 0.08f,
            cap = StrokeCap.Round
        )

        // 6. Smooth planetary crescent shadow (depth)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0x66020617)),
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                radius = radius * 1.3f
            ),
            radius = radius,
            center = center
        )

        // 7. Delicate atmosphere outer rim stroke
        drawCircle(
            color = Color(0x9938BDF8),
            radius = radius - 0.5f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
    }
}

/**
 * Clean, modern, minimalist vector-drawn Moon with craters and soft lunar glow.
 */
@Composable
fun MinimalistMoon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 1. Soft lunar glow aura
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x30FFFFFF), Color(0x0CFFFFFF), Color.Transparent),
                center = center,
                radius = radius * 1.4f
            ),
            radius = radius * 1.4f,
            center = center
        )

        // 2. Moon body with clean pearl / slate gradient
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFF8FAFC), // Pure pearl white
                    Color(0xFFE2E8F0), // Soft silver
                    Color(0xFFCBD5E1)  // Slate shadow
                ),
                start = Offset(center.x - radius, center.y - radius),
                end = Offset(center.x + radius, center.y + radius)
            ),
            radius = radius,
            center = center
        )

        // 3. Minimalist craters
        // Crater 1 (Major)
        val c1 = Offset(center.x - radius * 0.28f, center.y - radius * 0.22f)
        drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = 0.50f),
            radius = radius * 0.22f,
            center = c1
        )
        drawCircle(
            color = Color(0xFF64748B).copy(alpha = 0.35f),
            radius = radius * 0.16f,
            center = c1
        )

        // Crater 2 (Medium)
        val c2 = Offset(center.x + radius * 0.25f, center.y + radius * 0.20f)
        drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = 0.45f),
            radius = radius * 0.17f,
            center = c2
        )
        drawCircle(
            color = Color(0xFF64748B).copy(alpha = 0.30f),
            radius = radius * 0.11f,
            center = c2
        )

        // Crater 3 (Small)
        val c3 = Offset(center.x - radius * 0.12f, center.y + radius * 0.42f)
        drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = 0.40f),
            radius = radius * 0.12f,
            center = c3
        )

        // 4. Subtle crescent shadow (giving 3D moon curve)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0x40334155)),
                center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                radius = radius * 1.25f
            ),
            radius = radius,
            center = center
        )

        // 5. Fine outer rim
        drawCircle(
            color = Color(0x80FFFFFF),
            radius = radius - 0.5f,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
        )
    }
}
