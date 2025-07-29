// app/src/main/java/com/example/v/components/VideoPlayer.kt
package com.example.v.components

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@UnstableApi
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    uri: Uri,
    isPlaying: Boolean = true,
    isLooping: Boolean = true
) {
    val context = LocalContext.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(uri))
            repeatMode = if (isLooping) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
            playWhenReady = isPlaying
            prepare()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
                // Alternative approach:
                this.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT // 0=FIT, 1=STRETCH, 2=ZOOM
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}