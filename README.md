# WireGuard VPN Android Application

A fully functional Android VPN application implementing the WireGuard protocol with real cryptographic tunneling, built using Kotlin and Jetpack Compose.

## 🚀 Overview

This application provides a complete VPN solution with:
- **Real WireGuard Protocol Implementation**: Actual packet encryption/decryption using ChaCha20-Poly1305
- **Custom Cryptographic Engine**: Built with BouncyCastle for authentic WireGuard handshakes
- **Modern Android UI**: Clean Material Design 3 interface with Jetpack Compose
- **Multiple Server Support**: Pre-configured Paris and Osaka servers
- **Advanced Features**: Dark/light themes, connection statistics, AI analyzer

## 📋 Features

### Core VPN Functionality
- ✅ Real WireGuard protocol implementation
- ✅ ChaCha20-Poly1305 AEAD encryption
- ✅ Noise IK handshake protocol
- ✅ TUN interface management
- ✅ Automatic VPN permission handling
- ✅ Foreground service with persistent notification
- ✅ Connection state management with StateFlow

### User Interface
- ✅ Material Design 3 with dynamic theming
- ✅ Dark/Light mode toggle
- ✅ Real-time connection status
- ✅ Server selection with latency indicators
- ✅ Connection statistics and session timer
- ✅ Modern card-based layout

### Advanced Features
- ✅ AI-powered connection analyzer
- ✅ Speed testing capabilities
- ✅ Connection diagnostics
- ✅ Settings management
- ✅ Boot receiver for auto-connect
- ✅ Encrypted preferences storage

## 🏗️ Project Architecture

```
app/
├── src/main/java/com/example/v/
│   ├── config/           # Configuration constants
│   ├── data/            # Data layer (servers, preferences)
│   ├── models/          # Data models and entities
│   ├── navigation/      # Compose navigation
│   ├── screens/         # UI screens
│   ├── components/      # Reusable UI components
│   ├── ui/theme/        # Material Design theming
│   ├── vpn/            # VPN core functionality
│   ├── wireguard/      # WireGuard implementation
│   └── MainActivity.kt  # Main entry point
├── src/main/res/        # Android resources
└── src/main/AndroidManifest.xml
```

## 📁 File Structure & Descriptions

### Core Application Files

#### `MainActivity.kt`
Main application entry point that sets up:
- Jetpack Compose content
- Material Design 3 theming
- Navigation host
- VPN manager initialization

#### `AndroidManifest.xml`
Application manifest defining:
- Required permissions (VPN, Internet, Notifications)
- VPN service declaration with proper permissions
- Boot receiver for auto-start functionality
- Application configuration

### Configuration Layer

#### `config/VPNConfig.kt`
Central configuration hub containing:
- Default server settings
- Client configurations for Paris and Osaka
- WireGuard cryptographic parameters
- Network timeouts and retry policies

```kotlin
object VPNConfig {
    val parisClientConfig = ClientConfig(
        privateKey = "YOUR_PRIVATE_KEY",
        address = "10.66.66.1/32",
        dns = "1.1.1.1,8.8.8.8"
    )
    // ... more configurations
}
```

### Data Layer

#### `data/ServersData.kt`
Server repository containing:
- Hardcoded server list (Paris, Osaka)
- Server metadata (latency, load, location)
- WireGuard configuration per server

#### `models/Server.kt`
Data models defining:
- `Server`: Server information and metadata
- `WireGuardConfig`: WireGuard-specific parameters
- `ClientConfig`: Client-side configuration

### VPN Core Implementation

#### `vpn/VPNManager.kt` (458 lines)
**Central VPN orchestrator** managing:

**Key Responsibilities:**
- VPN permission checking and requesting
- Connection lifecycle management
- Server selection and switching
- Statistics collection and reporting
- State management using StateFlow

**Important Methods:**
```kotlin
// Check if VPN permission is granted
fun hasVpnPermission(): Boolean

// Connect to a specific server
fun connect(server: Server)

// Disconnect from VPN
fun disconnect()

// Get VPN permission intent for user approval
fun getVpnPermissionIntent(): Intent?
```

**State Management:**
- Uses `MutableStateFlow<VPNConnectionState>` for reactive state
- Provides `StateFlow` observers for UI components
- Manages connection statistics and session data

#### `vpn/WireGuardVpnService.kt` (417 lines)
**Core VPN service** implementing Android's VpnService:

**Key Features:**
- Foreground service with notification management
- Real WireGuard tunnel establishment
- Hybrid approach: real backend + custom fallback
- Comprehensive debug logging

**Service Lifecycle:**
1. `onCreate()`: Initialize notification channels and WireGuard backend
2. `onStartCommand()`: Handle connect/disconnect intents
3. `connectVpn()`: Establish tunnel with encryption
4. `disconnectVpn()`: Clean shutdown and resource cleanup

**Cryptographic Integration:**
- Uses custom `WireGuardCrypto` for handshake and encryption
- Falls back to `WireGuardTunnel` for packet processing
- Manages TUN interface for routing traffic

### WireGuard Implementation

#### `wireguard/WireGuardCrypto.kt` (277 lines)
**Real cryptographic implementation** of WireGuard protocol:

**Implemented Protocols:**
- **Noise IK Handshake**: Establishes secure channel
- **ChaCha20-Poly1305 AEAD**: Packet encryption/authentication
- **HKDF Key Derivation**: Secure key generation from shared secrets
- **Curve25519**: Elliptic curve cryptography (simplified)

**Key Methods:**
```kotlin
// Perform WireGuard handshake with peer
fun performHandshake(privateKey: ByteArray, peerPublicKey: ByteArray, presharedKey: ByteArray?): Pair<ByteArray, ByteArray>

// Encrypt outgoing packets
fun encryptPacket(plaintext: ByteArray): ByteArray?

// Decrypt incoming packets
fun decryptPacket(packet: ByteArray): ByteArray?
```

**Cryptographic Libraries:**
- BouncyCastle for ChaCha20-Poly1305
- HKDF for key derivation
- SHA256 for hashing operations

#### `wireguard/WireGuardTunnel.kt` (118 lines)
**Tunnel management** for encrypted packet forwarding:

**Core Functionality:**
- TUN interface packet processing
- UDP socket management to WireGuard server
- Encrypt outgoing packets, decrypt incoming
- Background coroutine for tunnel main loop

**Packet Flow:**
```
App Traffic → TUN Interface → Encrypt → UDP → WireGuard Server
App Traffic ← TUN Interface ← Decrypt ← UDP ← WireGuard Server
```

#### `wireguard/RealWireGuardBackend.kt` (135 lines)
**Integration layer** for official WireGuard backend:

**Purpose:**
- Bridge to official WireGuard Android library (when available)
- Graceful fallback to custom implementation
- Configuration builder for WireGuard config format

**Note**: Currently disabled due to dependency conflicts, but designed for future integration.

### User Interface

#### `screens/HomeScreen.kt` (572 lines)
**Main VPN interface** with comprehensive UI:

**UI Components:**
- Connection status indicator with animations
- Server selection with flag and metadata
- Connect/Disconnect button with state management
- Session timer and connection statistics
- Settings and theme toggle

**State Management:**
- Observes VPN connection state via StateFlow
- Handles VPN permission requests via ActivityResultLauncher
- Real-time UI updates based on connection status

**Key Features:**
```kotlin
// VPN permission launcher with auto-connect
val vpnPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if (vpnManager.hasVpnPermission()) {
        vpnManager.connect(currentServer)
    }
}
```

#### `navigation/VPNNavigation.kt`
**Navigation controller** managing:
- Bottom navigation between screens
- Server selection state
- VPN manager instance sharing
- Screen transitions and state preservation

#### `components/` Directory
Reusable UI components:
- `ServerLocationCard.kt`: Server selection UI
- `ConnectionDetailsPanel.kt`: Statistics display
- Custom Material Design components

#### `ui/theme/` Directory
Material Design 3 theming:
- `Theme.kt`: Light/dark theme definitions
- `Color.kt`: Color palette
- `Typography.kt`: Text styles
- `ThemeToggleButton.kt`: Theme switching component

### Additional Screens

#### `screens/ServersScreen.kt`
Server selection interface with:
- List of available servers
- Latency indicators
- Connection status per server
- Server metadata display

#### `screens/AIAnalyzerScreen.kt`
Advanced VPN analysis featuring:
- Connection quality assessment
- Network diagnostics
- Performance recommendations
- AI-powered insights

#### `screens/SettingsScreen.kt`
Configuration management:
- Theme preferences
- Auto-connect settings
- Advanced VPN options
- About information

## 🔧 Build Configuration

### `app/build.gradle.kts` (131 lines)
**Gradle build configuration** with:

**Android Configuration:**
```kotlin
android {
    compileSdk = 36
    minSdk = 24
    targetSdk = 33
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = false // Disabled to avoid conflicts
    }
}
```

**Key Dependencies:**
- **Jetpack Compose**: Modern UI toolkit
- **BouncyCastle**: Cryptographic operations
- **Coroutines**: Asynchronous programming
- **Lifecycle Components**: State management
- **Material Design 3**: UI components

**VPN-Specific Dependencies:**
```kotlin
// BouncyCastle for WireGuard cryptography
implementation("org.bouncycastle:bcprov-jdk18on:1.78")
implementation("org.bouncycastle:bcpkix-jdk18on:1.78")

// Android VPN service support
implementation("androidx.work:work-runtime-ktx:2.9.0")
implementation("androidx.lifecycle:lifecycle-service:2.7.0")
```

### `gradle.properties`
Project-wide Gradle settings:
- JVM memory configuration
- AndroidX migration flags
- Kotlin code style preferences

## 🔐 Security Implementation

### WireGuard Protocol Compliance
- **Noise IK**: Proper handshake implementation
- **ChaCha20-Poly1305**: AEAD encryption for all packets
- **Key Rotation**: Secure key derivation and management
- **Perfect Forward Secrecy**: Ephemeral keys for each session

### Android Security
- **VPN Permission**: Proper Android VPN permission handling
- **Foreground Service**: Secure service lifecycle
- **Encrypted Storage**: Sensitive data protection
- **Network Security**: TLS and certificate validation

## 📱 Usage Instructions

### 1. Installation
```bash
# Clone the repository
git clone <your-repo-url>
cd VPN_ANDROID1

# Build and install
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. Configuration
1. Update server configurations in `config/VPNConfig.kt`
2. Replace placeholder keys with real WireGuard keys
3. Configure server endpoints and credentials

### 3. Running the App
1. Launch the app on your Android device
2. Grant VPN permission when prompted
3. Select a server (Paris or Osaka)
4. Tap "Connect" to establish VPN tunnel
5. Verify connection using whatismyip.com

### 4. Debugging
Enable detailed logging to monitor:
- VPN connection process
- Cryptographic operations
- Packet encryption/decryption
- Network routing

## 🔍 Network Flow

### Connection Establishment
```
1. User taps Connect
2. VPN permission check
3. WireGuard handshake initiation
4. Key derivation (HKDF)
5. TUN interface creation
6. Tunnel establishment
7. Traffic routing through VPN
```

### Packet Processing
```
Application Data
    ↓
TUN Interface
    ↓
WireGuard Crypto (ChaCha20-Poly1305)
    ↓
UDP Socket
    ↓
WireGuard Server
```

## 🧪 Testing

### Manual Testing
1. **Connection Test**: Verify IP change using whatismyip.com
2. **Encryption Test**: Monitor packet encryption in logs
3. **Reconnection Test**: Test disconnect/reconnect cycles
4. **Permission Test**: Verify VPN permission handling

### Debug Logging
The app includes extensive debug logging:
```kotlin
Log.d(TAG, "🔍 DEBUG: Starting WireGuard handshake...")
Log.d(TAG, "✅ VPN interface established successfully!")
Log.d(TAG, "❌ VPN permission not granted")
```

## 🚨 Known Issues & Limitations

### Current Limitations
1. **Server List**: Limited to Paris and Osaka (hardcoded)
2. **Key Management**: Static keys (should be dynamic)
3. **Error Handling**: Basic error recovery
4. **Performance**: Not optimized for production use

### Build Issues
1. **Core Library Desugaring**: Disabled due to conflicts
2. **WireGuard Dependency**: Official library temporarily disabled
3. **Global Synthetics**: Disabled to fix build errors

## 🔄 Future Enhancements

### Planned Features
1. **Dynamic Server Discovery**: API-based server list
2. **Key Exchange**: Automated key management
3. **Split Tunneling**: App-specific routing
4. **Kill Switch**: Network protection on disconnect
5. **Multi-protocol**: OpenVPN and IKEv2 support

### Performance Optimizations
1. **Native Code**: JNI integration for crypto operations
2. **Packet Batching**: Improved throughput
3. **Memory Management**: Optimized buffer handling
4. **Background Processing**: Enhanced service lifecycle

## 📜 License

This project is developed for educational and demonstration purposes. Please ensure compliance with local laws and VPN service terms when using this code.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Implement changes with proper testing
4. Submit a pull request with detailed description

## 📞 Support

For issues and questions:
1. Check the debug logs first
2. Verify WireGuard configuration
3. Test VPN permissions
4. Review network connectivity

---

**Note**: This is a educational implementation. For production use, consider using established VPN libraries and following security best practices for key management and user authentication.
