package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NyraBlue
import com.example.ui.theme.NyraCyan
import com.example.ui.theme.NyraEmerald
import com.example.ui.theme.NyraPurple
import kotlinx.coroutines.android.awaitFrame
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class NyraOrbState {
    IDLE,       // White/Cyan: slow float and breathe
    LISTENING,  // Blue: reacts to mic input and expands like sound waves
    THINKING,   // Purple: rotates around orb with purple glow
    SPEAKING    // Green/Cyan: pulses and changes colors according to voice output
}

private data class Particle(
    val baseAngle: Float,        // Radians around center circle
    val baseRadiusFactor: Float, // Multiplier around ring
    val speedFactor: Float,      // Individual velocity factor
    val size: Float,             // Base dot radius in px
    val phaseOffset: Float       // Sine phase shift
)

@Composable
fun FuturisticNyraOrbCanvas(
    state: NyraOrbState,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier,
    orbSize: Dp = 260.dp
) {
    // Continuous time ticker for 60fps frame rendering
    var frameTimeNanos by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            frameTimeNanos = awaitFrame()
        }
    }

    // Secondary pulse transition for ambient breathing
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientPulse"
    )

    // Generate 120 particles arranged around a circle
    val particleCount = 120
    val particles = remember {
        List(particleCount) { index ->
            val angle = (index.toFloat() / particleCount) * (2f * PI.toFloat())
            Particle(
                baseAngle = angle,
                baseRadiusFactor = 1.0f + ((index % 5) - 2) * 0.03f,
                speedFactor = 0.8f + (index % 7) * 0.1f,
                size = 2.5f + (index % 3) * 1.2f,
                phaseOffset = (index * 0.15f)
            )
        }
    }

    Box(
        modifier = modifier.size(orbSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.minDimension / 2f) * 0.82f
            val orbRadius = maxRadius * 0.45f * ambientPulse
            val particleRingRadius = maxRadius * 0.72f

            val timeSeconds = frameTimeNanos / 1_000_000_000f

            // Determine core colors based on State
            val (corePrimaryColor, coreSecondaryColor, particleBaseColor) = when (state) {
                NyraOrbState.IDLE -> Triple(
                    NyraCyan,
                    Color(0xFF80EEFF),
                    Color.White
                )
                NyraOrbState.LISTENING -> Triple(
                    NyraBlue,
                    Color(0xFF60A5FA),
                    Color(0xFF3B82F6)
                )
                NyraOrbState.THINKING -> Triple(
                    NyraPurple,
                    Color(0xFFC084FC),
                    Color(0xFFA855F7)
                )
                NyraOrbState.SPEAKING -> Triple(
                    NyraEmerald,
                    NyraCyan,
                    NyraCyan
                )
            }

            // --- 1. Draw Outer Glowing Atmosphere Halos ---
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        corePrimaryColor.copy(alpha = 0.35f),
                        coreSecondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 1.1f
                ),
                center = center,
                radius = maxRadius * 1.1f
            )

            // --- 2. Draw Central Core AI Orb ---
            // Glowing core background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        corePrimaryColor,
                        coreSecondaryColor.copy(alpha = 0.7f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = orbRadius * 1.2f
                ),
                center = center,
                radius = orbRadius
            )

            // Inner Glass Reflection Arc
            val arcPath = Path().apply {
                addArc(
                    oval = androidx.compose.ui.geometry.Rect(
                        left = center.x - orbRadius * 0.7f,
                        top = center.y - orbRadius * 0.7f,
                        right = center.x + orbRadius * 0.7f,
                        bottom = center.y + orbRadius * 0.7f
                    ),
                    startAngleDegrees = 200f,
                    sweepAngleDegrees = 100f
                )
            }
            drawPath(
                path = arcPath,
                color = Color.White.copy(alpha = 0.45f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Rotating Neon Tech Rings
            val rotationAngle = (timeSeconds * 30f) % 360f
            drawCircle(
                color = corePrimaryColor.copy(alpha = 0.4f),
                center = center,
                radius = orbRadius * 1.25f,
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), rotationAngle)
                )
            )

            val reverseRotation = (-timeSeconds * 45f) % 360f
            drawCircle(
                color = coreSecondaryColor.copy(alpha = 0.3f),
                center = center,
                radius = orbRadius * 1.42f,
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f, 8f, 15f), reverseRotation)
                )
            )

            // --- 3. Draw 100+ Animated Circle Particles ---
            val normalizedRms = (rmsLevel.coerceIn(0f, 12f) / 12f)

            particles.forEachIndexed { i, particle ->
                var currentAngle = particle.baseAngle
                var currentRadius = particleRingRadius * particle.baseRadiusFactor
                var currentParticleColor = particleBaseColor
                var pSize = particle.size

                when (state) {
                    NyraOrbState.IDLE -> {
                        // Slowly float & breathe
                        val wobble = sin(timeSeconds * 2f * particle.speedFactor + particle.phaseOffset) * 6f
                        currentRadius += wobble
                        // Interpolate white to cyan
                        val colorLerp = (sin(timeSeconds * 1.5f + particle.phaseOffset) + 1f) / 2f
                        currentParticleColor = if (colorLerp > 0.5f) NyraCyan else Color.White
                    }

                    NyraOrbState.LISTENING -> {
                        // React to mic volume and expand like sound waves
                        val wavePulse = sin(timeSeconds * 8f + currentAngle * 4f) * (12f + normalizedRms * 35f)
                        val rmsExpansion = normalizedRms * 40f
                        currentRadius += rmsExpansion + wavePulse
                        pSize += normalizedRms * 3f
                        currentParticleColor = Color(0xFF60A5FA)
                    }

                    NyraOrbState.THINKING -> {
                        // Rotate particles rapidly around the orb with purple glow
                        val rotationOffset = (timeSeconds * 2.2f * particle.speedFactor) % (2f * PI.toFloat())
                        currentAngle += rotationOffset
                        val swirl = cos(timeSeconds * 3f + particle.phaseOffset) * 10f
                        currentRadius += swirl
                        currentParticleColor = if (i % 2 == 0) NyraPurple else Color(0xFFE9D5FF)
                    }

                    NyraOrbState.SPEAKING -> {
                        // Pulse & change colors dynamically with simulated audio output frequency
                        val freqWave = sin(currentAngle * 8f + timeSeconds * 10f) * 18f
                        currentRadius += freqWave
                        pSize += (sin(timeSeconds * 12f + particle.phaseOffset) + 1f) * 1.5f
                        val colorToggle = (sin(timeSeconds * 4f + i) + 1f) / 2f
                        currentParticleColor = if (colorToggle > 0.5f) NyraEmerald else NyraCyan
                    }
                }

                // Compute Cartesian position (x, y)
                val px = center.x + currentRadius * cos(currentAngle)
                val py = center.y + currentRadius * sin(currentAngle)

                // Particle outer glow halo
                drawCircle(
                    color = currentParticleColor.copy(alpha = 0.3f),
                    center = Offset(px, py),
                    radius = pSize * 2.2f
                )

                // Solid Particle Core
                drawCircle(
                    color = currentParticleColor,
                    center = Offset(px, py),
                    radius = pSize
                )

                // Optional interconnecting line threads for high-tech network look
                if (state == NyraOrbState.THINKING && i % 6 == 0) {
                    val nextParticle = particles[(i + 1) % particles.size]
                    val nx = center.x + currentRadius * cos(nextParticle.baseAngle + (timeSeconds * 2.2f))
                    val ny = center.y + currentRadius * sin(nextParticle.baseAngle + (timeSeconds * 2.2f))
                    drawLine(
                        color = NyraPurple.copy(alpha = 0.25f),
                        start = Offset(px, py),
                        end = Offset(nx, ny),
                        strokeWidth = 1f
                    )
                }
            }
        }
    }
}
