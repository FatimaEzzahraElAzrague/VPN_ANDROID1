// components/LeafletWebView.kt
package com.example.v.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun LeafletWebView(
    userLat: Double,
    userLng: Double,
    serverLat: Double,
    serverLng: Double,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = WebViewClient()

                // Load the HTML file from assets
                loadUrl("file:///android_asset/vpn_map.html")
            }
        },
        modifier = modifier,
        update = { webView ->
            // Call JavaScript function to update the map when coordinates change
            webView.loadUrl("javascript:createConnection($userLat, $userLng, $serverLat, $serverLng)")
        }
    )
}