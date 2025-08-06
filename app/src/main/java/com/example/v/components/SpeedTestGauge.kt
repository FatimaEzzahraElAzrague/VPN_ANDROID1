package com.example.v.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import com.example.v.ui.theme.*

@Composable
fun SpeedTestGauge(
    downloadSpeed: Float,
    uploadSpeed: Float,
    ping: Float,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    isRealTime: Boolean = false
) {
    // Animation values with real-time support
    val downloadSpeedAnimated by animateFloatAsState(
        targetValue = downloadSpeed,
        animationSpec = if (isRealTime) {
            tween(durationMillis = 500, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        },
        label = "download_speed"
    )

    val uploadSpeedAnimated by animateFloatAsState(
        targetValue = uploadSpeed,
        animationSpec = if (isRealTime) {
            tween(durationMillis = 500, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 2000, easing = FastOutSlowInEasing)
        },
        label = "upload_speed"
    )

    val pingAnimated by animateFloatAsState(
        targetValue = ping,
        animationSpec = if (isRealTime) {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        } else {
            tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        },
        label = "ping"
    )

    // Use app theme colors
    val backgroundColor = getGradientBackground(isDarkTheme)
    val gaugeBackgroundColor = if (isDarkTheme) Color(0xFF2D2D30) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val secondaryTextColor = Color.Gray
    val orangeColor = Color(0xFFFF6B35)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main speedometer gauge
        Box(
            modifier = Modifier.size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawCarSpeedometer(
                    speed = downloadSpeedAnimated,
                    maxSpeed = 200f,
                    isDarkTheme = isDarkTheme
                )
            }

            // Center digital display
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${downloadSpeedAnimated.toInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = orangeColor
                    )
                    Text(
                        text = "Mbps",
                        fontSize = 10.sp,
                        color = secondaryTextColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "DOWNLOAD SPEED",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Secondary gauges row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Upload gauge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawMiniGauge(
                            speed = uploadSpeedAnimated,
                            maxSpeed = 100f,
                            gaugeColor = Color(0xFF4CAF50),
                            isDarkTheme = isDarkTheme,
                            label = "UPLOAD"
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uploadSpeedAnimated.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Text(
                            text = "Mbps",
                            fontSize = 8.sp,
                            color = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "UPLOAD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.8.sp
                )
            }

            // Ping gauge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawMiniGauge(
                            speed = pingAnimated,
                            maxSpeed = 100f,
                            gaugeColor = Color(0xFF2196F3),
                            isDarkTheme = isDarkTheme,
                            label = "PING",
                            isReversed = true // Lower ping is better
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${pingAnimated.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        Text(
                            text = "ms",
                            fontSize = 8.sp,
                            color = secondaryTextColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "PING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

private fun DrawScope.drawCarSpeedometer(
    speed: Float,
    maxSpeed: Float,
    isDarkTheme: Boolean
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f * 0.8f

    // Use app theme colors
    val gaugeBackgroundColor = if (isDarkTheme) Color(0xFF2D2D30) else Color.White
    val tickColor = Color.Gray.copy(alpha = 0.6f)
    val majorTickColor = Color.Gray.copy(alpha = 0.8f)

    // Draw outer ring with gradient
    val outerGradient = Brush.sweepGradient(
        colors = listOf(
            Color(0xFF333333),
            Color(0xFF666666),
            Color(0xFF333333)
        ),
        center = center
    )

    drawCircle(
        brush = outerGradient,
        radius = radius + 20f,
        center = center,
        style = Stroke(width = 8.dp.toPx())
    )

    // Draw main gauge background
    drawCircle(
        color = gaugeBackgroundColor,
        radius = radius,
        center = center,
        style = Stroke(width = 40.dp.toPx())
    )

    // Draw speed range colors
    val speedRanges = listOf(
        0f to 50f to Color(0xFF4CAF50),    // Green
        50f to 100f to Color(0xFFFFEB3B),  // Yellow
        100f to 150f to Color(0xFFFF9800), // Orange
        150f to 200f to Color(0xFFF44336)  // Red
    )

    val startAngle = 140f
    val sweepAngle = 260f

    speedRanges.forEach { (range, color) ->
        val (start, end) = range
        val rangeStartAngle = startAngle + (start / maxSpeed) * sweepAngle
        val rangeSweepAngle = ((end - start) / maxSpeed) * sweepAngle

        drawArc(
            color = color.copy(alpha = 0.3f),
            startAngle = rangeStartAngle,
            sweepAngle = rangeSweepAngle,
            useCenter = false,
            style = Stroke(width = 35.dp.toPx()),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }

    // Draw tick marks
    for (i in 0..20) {
        val angle = startAngle + (i / 20f) * sweepAngle
        val isMajorTick = i % 5 == 0
        val tickLength = if (isMajorTick) 30f else 15f
        val tickWidth = if (isMajorTick) 3f else 1.5f
        val tickColorToUse = if (isMajorTick) majorTickColor else tickColor

        val startRadius = radius - 20f
        val endRadius = startRadius - tickLength

        val angleRad = Math.toRadians(angle.toDouble())
        val startPos = Offset(
            center.x + startRadius * cos(angleRad).toFloat(),
            center.y + startRadius * sin(angleRad).toFloat()
        )
        val endPos = Offset(
            center.x + endRadius * cos(angleRad).toFloat(),
            center.y + endRadius * sin(angleRad).toFloat()
        )

        drawLine(
            color = tickColorToUse,
            start = startPos,
            end = endPos,
            strokeWidth = tickWidth
        )

        // Draw speed numbers for major ticks
        if (isMajorTick) {
            val speedValue = (i / 20f * maxSpeed).toInt()
            val textRadius = endRadius - 25f
            val textPos = Offset(
                center.x + textRadius * cos(angleRad).toFloat(),
                center.y + textRadius * sin(angleRad).toFloat()
            )

            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = if (isDarkTheme) android.graphics.Color.WHITE else android.graphics.Color.BLACK
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                }

                canvas.nativeCanvas.drawText(
                    speedValue.toString(),
                    textPos.x,
                    textPos.y + 8f,
                    paint
                )
            }
        }
    }

    // Draw speed needle
    val needleAngle = startAngle + (speed / maxSpeed) * sweepAngle
    val needleLength = radius - 40f

    rotate(needleAngle, center) {
        // Needle shadow
        drawLine(
            color = Color.Black.copy(alpha = 0.3f),
            start = center + Offset(2f, 2f),
            end = center + Offset(needleLength + 2f, 2f),
            strokeWidth = 6f,
            cap = StrokeCap.Round
        )

        // Main needle
        drawLine(
            color = Color(0xFFFF6B35),
            start = center,
            end = center + Offset(needleLength, 0f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // Needle base circle
        drawCircle(
            color = Color(0xFFFF6B35),
            radius = 8f,
            center = center
        )

        drawCircle(
            color = gaugeBackgroundColor,
            radius = 5f,
            center = center
        )
    }
}

private fun DrawScope.drawMiniGauge(
    speed: Float,
    maxSpeed: Float,
    gaugeColor: Color,
    isDarkTheme: Boolean,
    label: String,
    isReversed: Boolean = false
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f * 0.7f

    val gaugeBackgroundColor = if (isDarkTheme) Color(0xFF2D2D30) else Color.White

    // Draw background arc
    drawArc(
        color = gaugeBackgroundColor,
        startAngle = 135f,
        sweepAngle = 270f,
        useCenter = false,
        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2)
    )

    // Draw active arc
    val normalizedSpeed = if (isReversed) {
        1f - (speed / maxSpeed)
    } else {
        speed / maxSpeed
    }

    val activeSweep = normalizedSpeed * 270f

    drawArc(
        brush = Brush.sweepGradient(
            colors = listOf(
                gaugeColor.copy(alpha = 0.7f),
                gaugeColor,
                gaugeColor.copy(alpha = 0.9f)
            ),
            center = center
        ),
        startAngle = 135f,
        sweepAngle = activeSweep,
        useCenter = false,
        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2)
    )

    // Draw indicator dot
    val indicatorAngle = 135f + activeSweep
    val angleRad = Math.toRadians(indicatorAngle.toDouble())
    val indicatorPos = Offset(
        center.x + radius * cos(angleRad).toFloat(),
        center.y + radius * sin(angleRad).toFloat()
    )

    drawCircle(
        color = gaugeColor,
        radius = 6f,
        center = indicatorPos
    )

    drawCircle(
        color = Color.White,
        radius = 3f,
        center = indicatorPos
    )
}