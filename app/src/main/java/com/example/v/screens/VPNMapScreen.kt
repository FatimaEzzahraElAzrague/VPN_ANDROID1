// screens/VPNMapScreen.kt
package com.example.v.screens

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import com.example.v.models.Server

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VPNMapScreen(
    userLat: Double = 34.02,
    userLng: Double = -6.84,
    server: Server? = null,
    modifier: Modifier = Modifier
) {
    val serverLat = server?.latitude ?: 48.85
    val serverLng = server?.longitude ?: 2.35
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    
    Box(
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                android.util.Log.d("VPNMap", "Creating WebView for VPNMapScreen...")
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                    }
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            android.util.Log.d("VPNMap", "Page loaded: $url")
                            // Inject JS to update markers and arc
                            val js = "updateMap($userLat, $userLng, $serverLat, $serverLng);"
                            view?.evaluateJavascript(js) { result ->
                                android.util.Log.d("VPNMap", "JS result: $result")
                            }
                        }
                        
                        override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            android.util.Log.e("VPNMap", "WebView error: $errorCode - $description for URL: $failingUrl")
                        }
                        
                        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            android.util.Log.d("VPNMap", "Page started loading: $url")
                        }
                    }
                    
                    android.util.Log.d("VPNMap", "Loading vpn_map.html...")
                    loadUrl("file:///android_asset/vpn_map.html")
                    webViewState.value = this
                }
            }
        )
    }
}

@Composable
fun VPNMapBackground(
    userLat: Double = 34.02,
    userLng: Double = -6.84,
    serverLat: Double = 48.85,
    serverLng: Double = 2.35,
    modifier: Modifier = Modifier
) {
    VPNMapScreen(
        userLat = userLat,
        userLng = userLng,
        server = Server(
            id = "test",
            name = "Test Server",
            country = "Test",
            countryCode = "TT",
            city = "Test",
            flag = "",
            ping = 0,
            load = 0,
            isOptimal = false,
            isPremium = false,
            isFavorite = false,
            latitude = serverLat,
            longitude = serverLng
        ),
        modifier = modifier
    )
}