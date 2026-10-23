package com.example.clutch_ai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.clutch_ai.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CinematicIntroScreen(
    onComplete: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val rawStatusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val statusBarTop = if (rawStatusBarTop > 0.dp) rawStatusBarTop else 24.dp

    // Stages: 0 = 3 parts flying in, 1 = snapped at center, 2 = gliding to top-left
    var stage by remember { mutableStateOf(0) }

    // Trajectory Progress: 0.0f -> 1.0f (0.0s to 1.4s)
    val flyInProgress = remember { Animatable(0f) }

    // Shockwave expansion at snap
    val shockwaveScale = remember { Animatable(0.3f) }
    val shockwaveAlpha = remember { Animatable(0f) }

    // Stage 2 Smooth Glide Easing (1.4s to 2.95s)
    val shiftProgress = remember { Animatable(0f) }

    // Crossfade between 3 individual parts and the unified reactor logo
    val logoCrossfade by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(durationMillis = 180, easing = LinearEasing),
        label = "logoCrossfade"
    )

    LaunchedEffect(Unit) {
        // Stage 1: Fly in the 3 mechanical parts (950ms)
        flyInProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 950,
                easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
            )
        )

        // Mechanical Snap at Center
        stage = 1

        // Trigger Radiant Shockwave Burst
        launch {
            shockwaveAlpha.snapTo(1f)
            launch { shockwaveScale.animateTo(2.8f, tween(750, easing = FastOutSlowInEasing)) }
            launch { shockwaveAlpha.animateTo(0f, tween(750, easing = LinearEasing)) }
        }

        // Brief crisp pause before gliding
        delay(50)

        // Stage 2: Direct Smooth Glide to Top-Left AppBar Logo position (850ms)
        stage = 2
        shiftProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 850,
                easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
            )
        )

        // Handover to main app
        onComplete()
    }

    val p = flyInProgress.value

    // Exact Netflix-scale Fly-In Trajectories:
    // Part 1 (Outer Cyan Ring): from (-190dp, -140dp) to (0, 0), rotate -280° -> 0°
    val g1X = (-190f * (1f - p)).dp
    val g1Y = (-140f * (1f - p)).dp
    val g1Rot = -280f * (1f - p)
    val g1Scale = 0.75f + 0.25f * p
    val g1Alpha = (p * 5f).coerceIn(0f, 1f)

    // Part 2 (Middle Dark Teal Cog Ring): from (190dp, -140dp) to (0, 0), rotate 280° -> 0°
    val g2X = (190f * (1f - p)).dp
    val g2Y = (-140f * (1f - p)).dp
    val g2Rot = 280f * (1f - p)
    val g2Scale = 0.70f + 0.30f * p

    // Part 3 (Inner Titanium Core Hub): from (0, 190dp) to (0, 0), rotate -340° -> 0°
    val g3Y = (190f * (1f - p)).dp
    val g3Rot = -340f * (1f - p)
    val g3Scale = 0.65f + 0.35f * p

    // NETFLIX-PROPORTIONED MOBILE SPLASH SIZE (120dp center icon -> 38dp navbar)
    val s = shiftProgress.value
    val baseSize = 120.dp
    val targetSize = 38.dp
    val targetScale = (targetSize / baseSize) // ~0.3166f

    // Calculate exact center offsets in pixels
    val screenCenterX = with(density) { (screenWidth / 2).toPx() }
    val screenCenterY = with(density) { (screenHeight / 2).toPx() }

    val destCenterX = with(density) { (16.dp + targetSize / 2).toPx() }
    val destCenterY = with(density) { (statusBarTop + 10.dp + targetSize / 2).toPx() }

    val totalDeltaX = destCenterX - screenCenterX
    val totalDeltaY = destCenterY - screenCenterY

    // Current transformation values
    val currentTransX = totalDeltaX * s
    val currentTransY = totalDeltaY * s
    val currentScale = 1.0f + (targetScale - 1.0f) * s

    // Backdrop fades out as logo glides, but logo remains 100% solid
    val backdropAlpha = (1f - s).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onComplete()
            }
    ) {
        // LAYER 1: PURE BLACK BACKDROP (Fades out smoothly to reveal dashboard underneath)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(backdropAlpha)
                .background(Color(0xFF000000))
        ) {
            // Central Cyan Spotlight Glow (Scaled to 120dp logo proportion)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(280.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF00F0FF).copy(alpha = 0.28f), Color.Transparent),
                                radius = 140.dp.toPx()
                            )
                        )
                    }
            )

            // Shockwave Burst on Assembly Snap
            if (shockwaveAlpha.value > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(120.dp)
                        .scale(shockwaveScale.value)
                        .alpha(shockwaveAlpha.value)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF00F0FF).copy(alpha = 0.85f), Color.Transparent),
                                    radius = 60.dp.toPx()
                                )
                            )
                        }
                )
            }


        }

        // LAYER 2: THE KINETIC CLUTCH LOGO (Netflix mobile size: 120dp -> glides to 38dp)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(baseSize)
                .graphicsLayer {
                    translationX = currentTransX
                    translationY = currentTransY
                    scaleX = currentScale
                    scaleY = currentScale
                },
            contentAlignment = Alignment.Center
        ) {
            // A. The 3 Assembling Component Parts (Crossfades out at snap)
            if (logoCrossfade < 1f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(1f - logoCrossfade),
                    contentAlignment = Alignment.Center
                ) {
                    // Part 1: Outer Cyan Chassis Ring
                    ClutchOuterChassisRing(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = g1X, y = g1Y)
                            .rotate(g1Rot)
                            .scale(g1Scale)
                            .alpha(g1Alpha)
                    )

                    // Part 2: Middle Intermediate Cog Ring
                    ClutchMiddleCogRing(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = g2X, y = g2Y)
                            .rotate(g2Rot)
                            .scale(g2Scale)
                            .alpha(g1Alpha)
                    )

                    // Part 3: Inner Titanium Core Hub
                    ClutchInnerCoreHub(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(x = 0.dp, y = g3Y)
                            .rotate(g3Rot)
                            .scale(g3Scale)
                            .alpha(g1Alpha)
                    )
                }
            }

            // B. The Assembled Finished Reactor Logo (Crossfades in at snap, glides into top bar)
            if (logoCrossfade > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(logoCrossfade),
                    contentAlignment = Alignment.Center
                ) {
                    // Pure Official Assembled Clutch Reactor (No circular background)
                    Image(
                        painter = painterResource(id = R.drawable.clutch_reactor_logo),
                        contentDescription = "Assembled Clutch Reactor",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * PART 1 OF 3: Outer Cyan Chassis Ring with 12 Mechanical Teeth (Proportioned to 120dp)
 */
@Composable
fun ClutchOuterChassisRing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.width * 0.41f

        val cyanGradient = Brush.linearGradient(
            colors = listOf(Color(0xFF00F0FF), Color(0xFF0891B2), Color(0xFF0284C7))
        )

        // 12 Outer Gear Teeth
        for (i in 0 until 12) {
            val angle = i * 30.0
            rotate(degrees = angle.toFloat(), pivot = center) {
                drawRoundRect(
                    brush = cyanGradient,
                    topLeft = Offset(center.x - 4.5.dp.toPx(), center.y - outerRadius - 6.5.dp.toPx()),
                    size = Size(9.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF00F0FF),
                    topLeft = Offset(center.x - 4.5.dp.toPx(), center.y - outerRadius - 6.5.dp.toPx()),
                    size = Size(9.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(center.x - 3.5.dp.toPx(), center.y - outerRadius - 4.5.dp.toPx()),
                    size = Size(7.dp.toPx(), 2.5.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }

        // Heavy Outer Rim (Hollow inside)
        drawCircle(
            brush = cyanGradient,
            radius = outerRadius,
            center = center,
            style = Stroke(width = 6.5.dp.toPx())
        )
        // Outer neon sheen line
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = outerRadius + 3.5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        // Inner rim lip
        drawCircle(
            color = Color(0xFF06B6D4),
            radius = outerRadius - 3.5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/**
 * PART 2 OF 3: Middle Dark Teal Intermediate Cog Ring with 16 Teeth (Proportioned to 120dp)
 */
@Composable
fun ClutchMiddleCogRing(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val midRadius = size.width * 0.29f

        val tealGradient = Brush.linearGradient(
            colors = listOf(Color(0xFF00F0FF), Color(0xFF0E7490))
        )

        // 16 Cog Teeth
        for (i in 0 until 16) {
            val angle = i * 22.5
            rotate(degrees = angle.toFloat(), pivot = center) {
                drawRoundRect(
                    color = Color(0xFF051726),
                    topLeft = Offset(center.x - 4.dp.toPx(), center.y - midRadius - 4.5.dp.toPx()),
                    size = Size(8.dp.toPx(), 9.dp.toPx()),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF00F0FF),
                    topLeft = Offset(center.x - 4.dp.toPx(), center.y - midRadius - 4.5.dp.toPx()),
                    size = Size(8.dp.toPx(), 9.dp.toPx()),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        drawCircle(
            brush = tealGradient,
            radius = midRadius,
            center = center,
            style = Stroke(width = 3.5.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = midRadius - 2.5.dp.toPx(),
            center = center,
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )
    }
}

/**
 * PART 3 OF 3: Inner Titanium Core Hub with 10 Teeth and 6 Rotor Vents (Proportioned to 120dp)
 */
@Composable
fun ClutchInnerCoreHub(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val hubRadius = size.width * 0.19f

        val titaniumGradient = Brush.linearGradient(
            colors = listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8), Color(0xFF475569))
        )

        // 10 Central Hub Teeth
        for (i in 0 until 10) {
            val angle = i * 36.0
            rotate(degrees = angle.toFloat(), pivot = center) {
                drawRoundRect(
                    brush = titaniumGradient,
                    topLeft = Offset(center.x - 3.5.dp.toPx(), center.y - hubRadius - 3.dp.toPx()),
                    size = Size(7.dp.toPx(), 6.5.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFF00F0FF),
                    topLeft = Offset(center.x - 3.5.dp.toPx(), center.y - hubRadius - 3.dp.toPx()),
                    size = Size(7.dp.toPx(), 6.5.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }
        }

        drawCircle(
            brush = titaniumGradient,
            radius = hubRadius,
            center = center
        )
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = hubRadius,
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )

        // 6 Circular Rotor Vents
        for (i in 0 until 6) {
            val rad = Math.toRadians(i * 60.0)
            val ventDistance = hubRadius * 0.62f
            val ventCenter = Offset(
                center.x + (ventDistance * cos(rad)).toFloat(),
                center.y + (ventDistance * sin(rad)).toFloat()
            )
            drawCircle(
                color = Color(0xFF051726),
                radius = 2.5.dp.toPx(),
                center = ventCenter
            )
            drawCircle(
                color = Color(0xFF00F0FF),
                radius = 2.5.dp.toPx(),
                center = ventCenter,
                style = Stroke(width = 0.8.dp.toPx())
            )
        }

        // Center Quantum Axle Core
        drawCircle(
            color = Color(0xFF030A14),
            radius = 4.5.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = 4.5.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
