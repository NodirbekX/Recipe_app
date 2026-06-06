package com.example.snaprecipe.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.snaprecipe.ui.state.LoadingPhase
import com.example.snaprecipe.ui.theme.Amber200
import com.example.snaprecipe.ui.theme.Amber400
import com.example.snaprecipe.ui.theme.Amber600
import com.example.snaprecipe.ui.theme.Cream

/**
 * Full-screen, non-dismissable loading state. Back navigation is intercepted so the
 * user can't bail out mid-request. The status line animates between the two phases.
 */
@Composable
fun LoadingScreen(
    phase: LoadingPhase,
    modifier: Modifier = Modifier
) {
    // Swallow back presses while work is in flight.
    BackHandler(enabled = true) { /* intentionally consumed */ }

    val phaseText = when (phase) {
        LoadingPhase.ANALYZING -> "🔍 Analyzing your photo..."
        LoadingPhase.SEARCHING -> "🍳 Searching recipes..."
    }

    // Gentle pulse on the progress indicator container.
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Amber600, Amber400, Amber200, Cream))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 5.dp,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
            )

            Spacer(Modifier.height(40.dp))

            AnimatedContent(
                targetState = phaseText,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn(tween(350))) togetherWith
                        (slideOutVertically { -it / 2 } + fadeOut(tween(250)))
                },
                label = "phase-text"
            ) { text ->
                Text(
                    text = text,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Hang tight — this only takes a moment.",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
