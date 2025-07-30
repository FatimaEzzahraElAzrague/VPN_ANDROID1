package com.example.v.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.v.ui.theme.getSecondaryTextColor

@Composable
fun AppIcon(
    drawable: android.graphics.drawable.Drawable?,
    modifier: Modifier = Modifier,
    tint: Color = getSecondaryTextColor()
) {
    if (drawable != null) {
        val bitmap = drawable.toBitmap()
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
    } else {
        // Fallback to default app icon
        Icon(
            imageVector = Icons.Default.Apps,
            contentDescription = null,
            modifier = modifier.size(24.dp),
            tint = tint
        )
    }
} 