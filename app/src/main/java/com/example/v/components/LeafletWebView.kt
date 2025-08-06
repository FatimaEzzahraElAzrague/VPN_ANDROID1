package com.example.v.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.v.models.Server

@Composable
fun LeafletWebView(
    modifier: Modifier = Modifier,
    selectedServer: Server,
    isConnected: Boolean
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true

                loadUrl("file:///android_asset/leaflet_map.html")
            }
        },
        update = { webView ->
            // Update map with server location and connection status
            val jsCode = """
                if (typeof updateServerLocation === 'function') {
                    updateServerLocation(
                        ${selectedServer.latitude}, 
                        ${selectedServer.longitude}, 
                        '${selectedServer.name}',
                        $isConnected
                    );
                }
            """.trimIndent()

            webView.evaluateJavascript(jsCode, null)
        }
    )
}