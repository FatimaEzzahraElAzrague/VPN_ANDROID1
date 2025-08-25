package com.example.v.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import android.graphics.Bitmap
import android.graphics.Canvas

@Composable
fun AppIcon(
    drawable: Drawable?,
    modifier: Modifier = Modifier,
    size: Int = 48,
    tint: androidx.compose.ui.graphics.Color? = null
) {
    if (drawable != null) {
        val painter = drawable.toPainter()
        Image(
            painter = painter,
            contentDescription = "App Icon",
            modifier = modifier.size(size.dp),
            colorFilter = tint?.let { androidx.compose.ui.graphics.ColorFilter.tint(it) }
        )
    }
}

fun Drawable.toPainter(): Painter {
    return if (this is BitmapDrawable) {
        BitmapPainter(this.bitmap.asImageBitmap())
    } else {
        // Convert any drawable to bitmap
        val bitmap = Bitmap.createBitmap(
            this.intrinsicWidth,
            this.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        this.setBounds(0, 0, canvas.width, canvas.height)
        this.draw(canvas)
        BitmapPainter(bitmap.asImageBitmap())
    }
}
