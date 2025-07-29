package com.example.v.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedFadeIn(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + expandVertically(
            animationSpec = tween(500)
        ),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
            animationSpec = tween(300)
        ),
        content = content
    )
}

@Composable
fun AnimatedSlideIn(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(500),
            initialOffsetX = { it }
        ) + fadeIn(animationSpec = tween(500)),
        exit = slideOutHorizontally(
            animationSpec = tween(300),
            targetOffsetX = { -it }
        ) + fadeOut(animationSpec = tween(300)),
        content = content
    )
}

@Composable
fun PulseAnimation(
    isActive: Boolean,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun RotatingAnimation(
    isActive: Boolean,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isActive) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Box(
        modifier = Modifier.graphicsLayer(rotationZ = rotation),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun BounceAnimation(
    isActive: Boolean,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isActive) -10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )
    
    Box(
        modifier = Modifier.graphicsLayer(translationY = offsetY),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun LoadingSpinner(
    color: Color = Color(0xFFFF6C36),
    size: Int = 48
) {
    CircularProgressIndicator(
        color = color,
        modifier = Modifier.size(size.dp)
    )
}

@Composable
fun AnimatedCounter(
    targetValue: Int,
    duration: Int = 1000,
    content: @Composable (Int) -> Unit
) {
    var animatedValue by remember { mutableStateOf(0) }
    
    LaunchedEffect(targetValue) {
        animate(
            initialValue = animatedValue.toFloat(),
            targetValue = targetValue.toFloat(),
            animationSpec = tween(duration, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatedValue = value.toInt()
        }
    }
    
    content(animatedValue)
}