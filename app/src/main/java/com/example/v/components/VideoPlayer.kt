package com.example.v.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri

@Composable
fun VideoPlayer(
    videoResId: Int? = null,
    uri: String? = null,
    modifier: Modifier = Modifier,
    onVideoEnd: () -> Unit = {}
) {
    val context = LocalContext.current
    
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                when {
                    videoResId != null -> {
                        setVideoURI(Uri.parse("android.resource://${context.packageName}/$videoResId"))
                    }
                    uri != null -> {
                        setVideoURI(Uri.parse(uri))
                    }
                }
                setOnCompletionListener {
                    onVideoEnd()
                }
                start()
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
