// com.example.v.components.AppIconPainter.kt
package com.example.v.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import android.graphics.drawable.Drawable

@Composable
fun rememberDrawablePainter(drawable: Drawable?): Painter {
    return remember(drawable) {
        try {
            BitmapPainter(drawable?.toBitmap()?.asImageBitmap() ?: ImageBitmap(1, 1))
        } catch (e: Exception) {
            BitmapPainter(ImageBitmap(1, 1))
        }
    }
}