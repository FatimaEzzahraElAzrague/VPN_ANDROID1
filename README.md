# VPN Android Application

<div align="center">

![VPN App Logo](app/src/main/res/drawable/ic_logo.png)

**A comprehensive, production-ready VPN application for Android built with modern technologies**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-7.0+-green.svg)](https://developer.android.com/)
[![Compose](https://img.shields.io/badge/Compose-1.5.14-orange.svg)](https://developer.android.com/jetpack/compose)
[![Ktor](https://img.shields.io/badge/Ktor-2.3.12-purple.svg)](https://ktor.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

## 📋 Table of Contents

- [Project Description](#-project-description)
- [Features](#-features)
- [Architecture](#-architecture)
- [Installation Guide](#-installation-guide)
- [Configuration](#-configuration)
- [How to Run](#-how-to-run)
- [Usage Examples](#-usage-examples)
- [API Documentation](#-api-documentation)
- [Contributing Guidelines](#-contributing-guidelines)
- [License](#-license)
- [Support](#-support)

## 🎯 Project Description

The VPN Android Application is a production-ready, enterprise-grade VPN solution that provides secure, fast, and reliable VPN connections using the WireGuard protocol. Built with modern Android development practices and a robust backend infrastructure, this application offers comprehensive VPN functionality with advanced security features.

### Why This Project Exists

- **Security First**: Implements enterprise-grade security with WireGuard protocol, kill switch functionality, and comprehensive encryption
- **Modern Architecture**: Built with Jetpack Compose, Ktor backend, and modern Android development practices
- **Production Ready**: Includes comprehensive testing, monitoring, and deployment configurations
- **Scalable Design**: Modular architecture that supports multiple server locations and user tiers
- **User Experience**: Material Design 3 UI with intuitive controls and real-time feedback

## ✨ Features

### 🔒 Core VPN Features
- **WireGuard Protocol**: Modern, fast, and secure VPN implementation
- **Multi-Server Support**: Connect to servers in multiple geographic locations
- **Auto-Connect**: Automatic VPN connection based on network conditions
- **Kill Switch**: Network traffic blocking when VPN disconnects
- **Split Tunneling**: Selective app routing through VPN
- **Real-time Monitoring**: Live connection statistics and performance metrics

### 🛡️ Security Features
- **JWT Authentication**: Secure token-based authentication system
- **Google Sign-In**: OAuth 2.0 integration for seamless authentication
- **BouncyCastle Encryption**: Military-grade cryptography implementation
- **Permission Management**: Comprehensive Android VPN service permissions
- **Traffic Protection**: Advanced kill switch with automatic reconnection

### 📊 Performance Features
- **Real-time Speed Testing**: Download/upload speed measurement
- **Traffic Analytics**: Comprehensive network usage monitoring
- **Server Optimization**: Automatic best server selection
- **Connection Statistics**: Detailed connection metrics and history
- **Background Processing**: Efficient resource management

### 🎨 User Experience Features
- **Material Design 3**: Modern, accessible UI following Google's design guidelines
- **Dark/Light Theme**: Theme switching with system preference detection
- **Responsive Design**: Adaptive layouts for different screen sizes
- **Offline Support**: Local data persistence and offline functionality
- **Push Notifications**: Real-time connection status updates

## 🏗️ Architecture

### Frontend (Android App)
```
app/src/main/java/com/example/v/
├── MainActivity.kt              # Main entry point and coordinator
├── vpn/                         # VPN core functionality
│   ├── VPNManager.kt           # Central VPN connection management
│   ├── WireGuardVpnService.kt  # Android VPN service implementation
│   └── SplitTunnelingConfig.kt # Split tunneling configuration
├── screens/                     # UI screens
│   ├── HomeScreen.kt           # Main VPN connection interface
│   ├── ServerListScreen.kt     # Server selection interface
│   ├── SettingsScreen.kt       # Application configuration
│   └── AIAnalyzerScreen.kt     # AI-powered network analysis
├── components/                  # Reusable UI components
│   ├── AnimatedConnectButton.kt # Interactive VPN connection button
│   ├── ServerLocationCard.kt   # Server location display
│   └── SpeedTestGauge.kt       # Real-time speed test visualization
├── data/                        # Data management
│   ├── AppDatabase.kt          # Local Room database
│   ├── ApiClient.kt            # Backend API communication
│   └── ServersData.kt          # VPN server information
└── utils/                       # Utility functions
    ├── NetworkUtils.kt         # Network-related utilities
    ├── SpeedTestEngine.kt      # Speed testing implementation
    └── WireGuardKeyUtils.kt    # WireGuard key management
```

### Backend (Ktor Server)
```
backend/src/main/kotlin/com/myapp/backend/
├── Application.kt               # Main server entry point
├── config/                      # Configuration management
│   ├── Env.kt                  # Environment variables
│   └── Jwt.kt                  # JWT configuration
├── db/                          # Database layer
│   ├── DatabaseFactory.kt      # Database connection management
│   ├── Users.kt                # User data management
│   └── AutoConnectTable.kt     # Auto-connect configuration
├── routes/                      # API endpoints
│   ├── AuthRoutes.kt           # Authentication endpoints
│   ├── ProfileRoutes.kt        # User profile management
│   ├── VPNFeaturesRoutes.kt    # VPN feature endpoints
│   ├── KillSwitchRoutes.kt     # Kill switch functionality
│   └── AutoConnectRoutes.kt    # Auto-connect management
├── services/                    # Business logic
│   ├── EmailService.kt         # Email functionality
│   ├── GoogleAuthService.kt    # Google authentication
│   ├── SpeedTestService.kt     # Speed testing backend
│   └── KillSwitchService.kt    # Kill switch functionality
└── models/                      # Data models
    ├── Models.kt                # Core data models
    ├── SpeedTestModels.kt      # Speed test data models
    └── AutoConnectModels.kt    # Auto-connect data models
```

## 🚀 Installation Guide

### Prerequisites

#### System Requirements
- **Android Development**:
  - Android Studio Arctic Fox (2023.2.1) or later
  - Kotlin 1.9.24+
  - JDK 17
  - Android SDK 24+ (Android 7.0)
  - Target SDK 33+ (Android 13)

- **Backend Development**:
  - Kotlin 1.9.24+
  - JDK 17
  - PostgreSQL 12+
  - Redis 6+
  - Gradle 8.4+

#### Required Accounts
- Google Cloud Platform account (for OAuth 2.0)
- SMTP email service (Gmail, SendGrid, etc.)
- VPN server infrastructure (AWS, DigitalOcean, etc.)

### Step-by-Step Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/FatimaEzzahraElAzrague/VPN_ANDROID1.git
cd VPN_ANDROID1
```

#### 2. Android App Setup

##### Configure Android Studio
1. Open Android Studio
2. Open the `app/` folder as a project
3. Sync Gradle files
4. Configure your signing keys in `app/build.gradle.kts`

##### Update Configuration
1. Update server endpoints in `app/src/main/java/com/example/v/data/ApiClient.kt`
2. Configure Google Sign-In in `app/src/main/java/com/example/v/services/GoogleSignInService.kt`
3. Update VPN server configurations in `app/src/main/java/com/example/v/data/ServersData.kt`

##### Build and Run
```bash
# Build the project
./gradlew build

# Install on device/emulator
./gradlew installDebug

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

#### 3. Backend Server Setup

##### Install Dependencies
```bash
cd backend
./gradlew build
```

##### Database Setup
1. **PostgreSQL Installation**:
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install postgresql postgresql-contrib
   
   # macOS
   brew install postgresql
   
   # Windows
   # Download from https://www.postgresql.org/download/windows/
   ```

2. **Create Database**:
   ```sql
   CREATE DATABASE vpn_app;
   CREATE USER vpn_user WITH PASSWORD 'your_secure_password';
   GRANT ALL PRIVILEGES ON DATABASE vpn_app TO vpn_user;
   ```

3. **Redis Installation**:
   ```bash
   # Ubuntu/Debian
   sudo apt install redis-server
   
   # macOS
   brew install redis
   
   # Windows
   # Download from https://redis.io/download
   ```

##### Environment Configuration
Create a `.env` file in the `backend/` directory:

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/vpn_app
REDIS_URL=redis://localhost:6379

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key_here
JWT_ISSUER=vpn-app
JWT_AUDIENCE=vpn-users
JWT_EXP_SECONDS=86400

# Google OAuth
GOOGLE_CLIENT_ID_ANDROID=your_android_client_id
GOOGLE_CLIENT_ID_WEB=your_web_client_id

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_app_password
EMAIL_FROM=your_email@gmail.com

# Application Configuration
APP_BASE_URL=http://localhost:8080
PORT=8080

# OTP Configuration
OTP_TTL_SECONDS=600
OTP_LENGTH=6
OTP_COOLDOWN_SECONDS=60
```

#### 4. VPN Server Setup

##### WireGuard Server Configuration
1. **Copy server configs**:
   ```bash
   cp server-configs/*-wg0.conf /etc/wireguard/
   ```

2. **Run setup script**:
   ```bash
   sudo chmod +x server-configs/setup-server.sh
   sudo ./server-configs/setup-server.sh
   ```

3. **Generate keys**:
   ```bash
   cd server-configs
   ./derive-keys.sh
   ```

4. **Start WireGuard**:
   ```bash
   sudo systemctl start wg-quick@wg0
   sudo systemctl enable wg-quick@wg0
   ```

5. **Verify setup**:
   ```bash
   ./verify-setup.sh
   ```

## ⚙️ Configuration

### Environment Variables

#### Optional Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `JWT_EXP_SECONDS` | JWT expiration time | `86400` |
| `OTP_TTL_SECONDS` | OTP time-to-live | `600` |
| `OTP_LENGTH` | OTP code length | `6` |
| `OTP_COOLDOWN_SECONDS` | OTP cooldown period | `60` |

### Database Configuration

#### PostgreSQL Tables
The application automatically creates the following tables:
- `users` - User accounts and authentication
- `auto_connect_rules` - Auto-connect configuration
- `kill_switch_settings` - Kill switch preferences
- `speed_test_results` - Speed test history
- `vpn_events` - VPN connection events

#### Redis Keys
- `user:{userId}:session` - User session data
- `user:{userId}:killswitch` - Kill switch state
- `server:ping` - Server ping cache
- `speedtest:config:{userId}` - Speed test configuration

### VPN Server Configuration

#### WireGuard Interface
```bash
# /etc/wireguard/wg0.conf
[Interface]
PrivateKey = your_private_key
Address = 10.66.66.1/24
ListenPort = 51820
PostUp = iptables -A FORWARD -i wg0 -j ACCEPT; iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -j ACCEPT; iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE

[Peer]
PublicKey = client_public_key
AllowedIPs = 10.66.66.2/32
```

#### Firewall Rules
```bash
# Enable IP forwarding
echo 1 > /proc/sys/net/ipv4/ip_forward

# Configure iptables
iptables -A FORWARD -i wg0 -j ACCEPT
iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
```

## 🏃 How to Run

### Development Environment

#### Start Backend Server
```bash
cd backend

# Development mode with auto-reload
./gradlew run

# Production build
./gradlew build
java -jar build/libs/backend.jar
```

#### Start Android App
```bash
cd app

# Build and install
./gradlew installDebug

# Run specific build variant
./gradlew installRelease
```

### Production Deployment

#### Backend Deployment
```bash
# Build production JAR
./gradlew build

# Create systemd service
sudo nano /etc/systemd/system/vpn-backend.service
```

**Systemd Service Configuration**:
```ini
[Unit]
Description=VPN Backend Service
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=vpn
WorkingDirectory=/opt/vpn-backend
ExecStart=/usr/bin/java -jar vpn-backend.jar
Restart=always
RestartSec=10
Environment="DATABASE_URL=jdbc:postgresql://localhost:5432/vpn_app"
Environment="REDIS_URL=redis://localhost:6379"

[Install]
WantedBy=multi-user.target
```

**Enable and Start Service**:
```bash
sudo systemctl daemon-reload
sudo systemctl enable vpn-backend
sudo systemctl start vpn-backend
sudo systemctl status vpn-backend
```

#### Android App Distribution
```bash
# Build release APK
./gradlew assembleRelease

# Build AAB for Play Store
./gradlew bundleRelease

# Sign APK
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore your-keystore.jks app-release-unsigned.apk alias_name
```

### Docker Deployment (Optional)

#### Backend Dockerfile
```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app
COPY build/libs/backend.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

#### Docker Compose
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/vpn_app
      - REDIS_URL=redis://redis:6379
    depends_on:
      - postgres
      - redis

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: vpn_app
      POSTGRES_USER: vpn_user
      POSTGRES_PASSWORD: your_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

volumes:
  postgres_data:
  redis_data:
```

## 📱 Usage Examples

### Android App Usage

#### Basic VPN Connection
```kotlin
// Get VPN manager instance
val vpnManager = VPNManager.getInstance(context)

// Connect to optimal server
val optimalServer = ServersData.getOptimalServer()
vpnManager.connect(optimalServer)

// Check connection status
val isConnected = vpnManager.isConnected()

// Disconnect
vpnManager.disconnect()
```

#### Auto-Connect Configuration
```kotlin
// Enable auto-connect
val autoConnectManager = AutoConnectManager(context, repository) { server ->
    vpnManager.connect(server)
}

// Start monitoring
autoConnectManager.start()

// Add custom rule
val rule = AutoConnectRule(
    networkType = "WiFi",
    ssid = "HomeNetwork",
    autoConnect = true
)
repository.insertRule(rule)
```

#### Kill Switch Usage
```kotlin
// Enable kill switch
val killSwitchManager = KillSwitchManager.getInstance(context)
killSwitchManager.enableKillSwitch()

// Add listener
killSwitchManager.addKillSwitchListener(object : KillSwitchManager.KillSwitchListener {
    override fun onKillSwitchActivated(reason: String) {
        // Show notification
        showKillSwitchNotification(reason)
    }
    
    override fun onVpnReconnected() {
        // Hide notification
        hideKillSwitchNotification()
    }
})
```

### Backend API Usage

#### Authentication
```bash
# User Registration
curl -X POST "http://localhost:8080/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "secure_password",
    "name": "John Doe"
  }'

# User Login
curl -X POST "http://localhost:8080/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "secure_password"
  }'

# Google OAuth
curl -X POST "http://localhost:8080/auth/google" \
  -H "Content-Type: application/json" \
  -d '{
    "idToken": "google_id_token_here"
  }'
```

#### VPN Features
```bash
# Get speed test configuration
curl -X GET "http://localhost:8080/speedtest/config/user123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Update kill switch settings
curl -X PUT "http://localhost:8080/killswitch/user123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "isEnabled": true,
    "autoReconnectEnabled": true,
    "maxReconnectionAttempts": 5
  }'

# Get auto-connect rules
curl -X GET "http://localhost:8080/autoconnect/user123" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### Admin Operations
```bash
# Get global statistics
curl -X GET "http://localhost:8080/admin/statistics" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"

# Get user analytics
curl -X GET "http://localhost:8080/admin/users/user123/analytics" \
  -H "Authorization: Bearer ADMIN_JWT_TOKEN"
```

### WireGuard Configuration

#### Client Configuration
```bash
# Generate key pair
wg genkey | tee client_private.key | wg pubkey > client_public.key

# Create client config
cat > client.conf << EOF
[Interface]
PrivateKey = $(cat client_private.key)
Address = 10.66.66.2/32
DNS = 8.8.8.8, 8.8.4.4

[Peer]
PublicKey = server_public_key
Endpoint = your_server_ip:51820
AllowedIPs = 0.0.0.0/0
PersistentKeepalive = 25
EOF
```

#### Server Management
```bash
# Check WireGuard status
sudo wg show

# Monitor connections
sudo wg show wg0

# Add new client
sudo wg set wg0 peer CLIENT_PUBLIC_KEY allowed-ips 10.66.66.3/32

# Remove client
sudo wg set wg0 peer CLIENT_PUBLIC_KEY remove
```

## 📚 API Documentation

### Authentication Endpoints

#### POST `/auth/signup`
Create a new user account.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "secure_password",
  "name": "John Doe"
}
```

**Response**:
```json
{
  "success": true,
  "message": "User created successfully",
  "userId": "user123"
}
```

#### POST `/auth/signin`
Authenticate existing user.

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "secure_password"
}
```

**Response**:
```json
{
  "success": true,
  "accessToken": "jwt_token_here",
  "refreshToken": "refresh_token_here",
  "user": {
    "id": "user123",
    "email": "user@example.com",
    "name": "John Doe"
  }
}
```

#### POST `/auth/google`
Authenticate with Google OAuth.

**Request Body**:
```json
{
  "idToken": "google_id_token_here"
}
```

### User Management Endpoints

#### GET `/profile/{userId}`
Get user profile information.

**Headers**: `Authorization: Bearer {token}`

**Response**:
```json
{
  "id": "user123",
  "email": "user@example.com",
  "name": "John Doe",
  "createdAt": "2024-01-01T00:00:00Z",
  "lastLogin": "2024-01-15T12:00:00Z"
}
```

#### PUT `/profile/{userId}`
Update user profile.

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "name": "John Smith",
  "email": "john.smith@example.com"
}
```

### VPN Features Endpoints

#### GET `/speedtest/config/{userId}`
Get speed test configuration.

**Headers**: `Authorization: Bearer {token}`

**Response**:
```json
{
  "userId": "user123",
  "autoTestEnabled": true,
  "testIntervalMinutes": 30,
  "serverSelection": "auto",
  "customServers": []
}
```

#### POST `/speedtest/result/{userId}`
Save speed test result.

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "downloadSpeed": 50.5,
  "uploadSpeed": 25.3,
  "ping": 45,
  "server": "13.38.83.180",
  "timestamp": "2024-01-15T12:00:00Z"
}
```

### Kill Switch Endpoints

#### GET `/killswitch/{userId}`
Get kill switch configuration.

**Headers**: `Authorization: Bearer {token}`

**Response**:
```json
{
  "userId": "user123",
  "isEnabled": true,
  "autoReconnectEnabled": true,
  "maxReconnectionAttempts": 3,
  "connectionCheckIntervalMs": 5000,
  "connectionTimeoutMs": 10000
}
```

#### PUT `/killswitch/{userId}`
Update kill switch settings.

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "isEnabled": true,
  "autoReconnectEnabled": true,
  "maxReconnectionAttempts": 5
}
```

### Auto-Connect Endpoints

#### GET `/autoconnect/{userId}`
Get auto-connect rules.

**Headers**: `Authorization: Bearer {token}`

**Response**:
```json
{
  "rules": [
    {
      "id": "rule123",
      "userId": "user123",
      "networkType": "WiFi",
      "ssid": "HomeNetwork",
      "autoConnect": true,
      "serverPreference": "optimal"
    }
  ]
}
```

#### POST `/autoconnect/{userId}`
Create auto-connect rule.

**Headers**: `Authorization: Bearer {token}`

**Request Body**:
```json
{
  "networkType": "WiFi",
  "ssid": "WorkNetwork",
  "autoConnect": true,
  "serverPreference": "fastest"
}
```

## 🤝 Contributing Guidelines

We welcome contributions from the community! Please read these guidelines before submitting your contribution.

### How to Contribute

#### 1. Fork and Clone
```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/yourusername/vpn-android-app.git
cd vpn-android-app

# Add upstream remote
git remote add upstream https://github.com/originalusername/vpn-android-app.git
```

#### 2. Create Feature Branch
```bash
# Create and checkout feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b fix/your-bug-description
```

#### 3. Make Changes
- Follow the existing code style and conventions
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass

#### 4. Commit Changes
```bash
# Add your changes
git add .

# Commit with descriptive message
git commit -m "feat: add new VPN protocol support

- Implement OpenVPN protocol
- Add protocol selection UI
- Update documentation
- Add unit tests"

# Push to your fork
git push origin feature/your-feature-name
```

#### 5. Submit Pull Request
- Create a pull request on GitHub
- Provide a clear description of your changes
- Reference any related issues
- Request review from maintainers

### Development Setup

#### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.9.24+
- JDK 17
- PostgreSQL 12+
- Redis 6+

#### Local Development
```bash
# Backend development
cd backend
./gradlew run

# Android development
cd app
./gradlew assembleDebug
```

#### Testing
```bash
# Run all tests
./gradlew test

# Run specific test suite
./gradlew test --tests "com.example.v.vpn.VPNManagerTest"

# Run Android tests
./gradlew connectedAndroidTest
```

### Code Style Guidelines

#### Kotlin
- Use 4 spaces for indentation
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs

#### Android
- Follow Material Design guidelines
- Use Jetpack Compose for UI
- Implement proper error handling
- Follow Android lifecycle patterns

#### Backend
- Use Ktor framework conventions
- Implement proper error handling
- Add comprehensive logging
- Follow REST API best practices

### Pull Request Checklist

- [ ] Code follows project style guidelines
- [ ] Tests are added and passing
- [ ] Documentation is updated
- [ ] No breaking changes (or documented)
- [ ] Commit messages are clear and descriptive
- [ ] All CI checks pass

### Issue Reporting

When reporting issues, please include:

1. **Environment Details**:
   - Android version
   - Device model
   - App version
   - Backend version

2. **Steps to Reproduce**:
   - Clear step-by-step instructions
   - Expected vs actual behavior

3. **Logs and Screenshots**:
   - Relevant logcat output
   - Screenshots of the issue
   - Backend server logs

4. **Additional Context**:
   - Network conditions
   - VPN server location
   - Previous working state

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

### MIT License Summary
- ✅ **Commercial Use**: You can use this software for commercial purposes
- ✅ **Modification**: You can modify the software
- ✅ **Distribution**: You can distribute the software
- ✅ **Private Use**: You can use and modify the software privately
- ✅ **Sublicensing**: You can sublicense the software
- ❌ **Liability**: The software is provided without warranty
- ❌ **Warranty**: The software is provided "as is"

### License Text
```
MIT License

Copyright (c) 2024 VPN Android Application

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🆘 Support

### Getting Help

#### Documentation
- **Project Documentation**: This README and related docs
- **API Documentation**: Backend API endpoints and usage
- **Testing Guide**: See `TESTING.md` for testing instructions
- **Kill Switch Guide**: See `README_KILL_SWITCH.md` for detailed implementation

#### Community Support
- **GitHub Issues**: Report bugs and request features
- **GitHub Discussions**: Ask questions and share ideas
- **Pull Requests**: Contribute code and improvements

#### Contact Information
- **Project Maintainers**: [@maintainer1](https://github.com/maintainer1), [@maintainer2](https://github.com/maintainer2)
- **Email**: support@vpn-android-app.com
- **Discord**: [Join our community](https://discord.gg/vpn-android-app)

### Troubleshooting

#### Common Issues

1. **VPN Connection Fails**
   - Check server configuration
   - Verify network connectivity
   - Check firewall settings
   - Review server logs

2. **Kill Switch Not Working**
   - Verify kill switch is enabled
   - Check VPN service permissions
   - Review kill switch configuration
   - Check device compatibility

3. **Backend Connection Errors**
   - Verify server is running
   - Check database connectivity
   - Review environment variables
   - Check network configuration

4. **Build Errors**
   - Update Gradle version
   - Clean and rebuild project
   - Check dependency versions
   - Verify JDK installation

#### Debug Mode

Enable debug logging in the Android app:

```kotlin
// In your Application class or MainActivity
if (BuildConfig.DEBUG) {
    Log.d("VPN_DEBUG", "Debug mode enabled")
    // Enable verbose logging
}
```

Enable debug logging in the backend:

```kotlin
// In application.conf
ktor {
  deployment {
    port = ${?PORT}
  }
  application {
    modules = [ com.myapp.backend.ApplicationKt.module ]
  }
}

// Add logging configuration
logback {
  appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
      pattern = "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    }
  }
  root(INFO, ["STDOUT"])
}
```

### Performance Optimization

#### Android App
- Use lazy loading for large lists
- Implement efficient image caching
- Optimize database queries
- Use WorkManager for background tasks

#### Backend Server
- Implement connection pooling
- Use Redis for caching
- Optimize database queries
- Implement rate limiting

## 🔄 Version History

### Current Version: v1.3.0

#### v1.3.0 (Current)
- ✨ Added AI-powered network analyzer
- 🎨 Implemented Material Design 3 UI
- 🔒 Enhanced kill switch functionality
- 📊 Improved speed testing and analytics
- 🚀 Performance optimizations

#### v1.2.0
- 📊 Added speed testing and analytics
- 🔄 Implemented auto-connect features
- 🛡️ Enhanced security with kill switch
- 📱 Improved user interface

#### v1.1.0
- 🔐 Added Google Sign-In integration
- 🗄️ Implemented local database
- 📍 Added server location selection
- 🎯 Improved connection management

#### v1.0.0
- 🚀 Initial release
- 🔒 Basic WireGuard VPN functionality
- 📱 Material Design UI
- 🔐 JWT authentication

### Upcoming Features

#### v1.4.0 (Planned)
- 🌍 Geographic server optimization
- 📱 iOS companion app
- 🔐 Multi-factor authentication
- 📊 Advanced analytics dashboard

#### v1.5.0 (Planned)
- 🤖 Machine learning optimization
- ☁️ Cloud deployment automation
- 🔌 Plugin system
- 🌐 Web admin interface

---

<div align="center">

**Made with ❤️ by the VPN Android Application Team**

[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/yourusername/vpn-android-app)
[![Discord](https://img.shields.io/badge/Discord-7289DA?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/vpn-android-app)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>
