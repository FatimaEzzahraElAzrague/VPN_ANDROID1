# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# VPN Service Keep Rules
-keep class com.example.v.vpn.RealWireGuardVPNService { *; }

# VpnService Keep Rules
-keep class android.net.VpnService { *; }
-keep class android.net.VpnService$Builder { *; }

# Keep VPN-related classes
-keep class * extends android.net.VpnService { *; }

# Keep notification-related classes
-keep class android.app.Notification { *; }
-keep class android.app.NotificationChannel { *; }
-keep class android.app.NotificationManager { *; }

# Keep Intent and PendingIntent classes
-keep class android.content.Intent { *; }
-keep class android.app.PendingIntent { *; }

# Keep ParcelFileDescriptor
-keep class android.os.ParcelFileDescriptor { *; }

# Keep network-related classes
-keep class java.net.DatagramSocket { *; }
-keep class java.net.InetSocketAddress { *; }

# Keep coroutines
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.** {
    volatile <fields>;
}

# Keep Compose UI components
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** {
    *;
}

# Keep Material3 components
-keep class androidx.compose.material3.** { *; }

# Keep StateFlow and MutableStateFlow
-keep class kotlinx.coroutines.flow.** { *; }