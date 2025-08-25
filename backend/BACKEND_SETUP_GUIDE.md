# VPN Backend Setup Guide

This guide provides comprehensive instructions for setting up the VPN backend with Osaka and Paris servers, including VPN configuration and speed test functionality.

## 🏗️ Architecture Overview

The backend provides two main services:
1. **VPN Service**: WireGuard configuration management for Osaka and Paris servers
2. **Speed Test Service**: Real-time speed testing with rate limiting and security

## 🚀 Quick Start

### 1. Environment Setup

Copy the environment template and configure your settings:

```bash
cp env.txt .env
```

Update the following critical variables:
```bash
# VPN Server Configuration
OSAKA_SERVER_IP=15.168.240.118
OSAKA_SERVER_PORT=51820
PARIS_SERVER_IP=52.47.190.220
PARIS_SERVER_PORT=51820

# JWT Configuration (keep existing)
JWT_SECRET=your-secure-jwt-secret
JWT_ISSUER=myapp.backend
JWT_AUDIENCE=myapp.client
JWT_EXP_SECONDS=3600

# Database (already configured)
DATABASE_URL=postgresql://neondb_owner:npg_ZAqFk4UE8arp@ep-withered-wind-adhutu0i-pooler.c-2.us-east-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
```

### 2. Generate WireGuard Keys

Run the key generation script:

```bash
cd server-configs
chmod +x generate-keys.sh
./generate-keys.sh
```

This will create:
- `keys/osaka_private.key` - Osaka server private key
- `keys/osaka_public.key` - Osaka server public key
- `keys/paris_private.key` - Paris server private key
- `keys/paris_public.key` - Paris server public key

### 3. Update Server Configurations

Copy the generated keys to your server configuration files:

```bash
# On Osaka server (15.168.240.118)
sudo cp keys/osaka_private.key /etc/wireguard/wg0.conf
sudo cp keys/osaka_public.key /etc/wireguard/wg0.pub

# On Paris server (52.47.190.220)
sudo cp keys/paris_private.key /etc/wireguard/wg0.conf
sudo cp keys/paris_public.key /etc/wireguard/wg0.pub
```

### 4. Start the Backend

```bash
# Using Gradle
./gradlew run

# Or using the provided scripts
./start_backend.ps1  # Windows PowerShell
./start_backend.bat  # Windows Command Prompt
```

## 🔌 API Endpoints

### VPN Configuration

#### GET `/vpn/config/{userId}`
Get VPN configurations for both Osaka and Paris servers.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "VPN configurations generated successfully",
  "configs": {
    "osaka": {
      "server": {
        "id": "osaka",
        "name": "Osaka VPN Server",
        "city": "Osaka",
        "country": "Japan",
        "ip": "15.168.240.118",
        "port": 51820,
        "wireguardEndpoint": "15.168.240.118:51820"
      },
      "clientConfig": {
        "privateKey": "client_private_key",
        "publicKey": "client_public_key",
        "address": "10.0.1.2/32"
      },
      "wireguardConfig": "[Interface]\nPrivateKey = ...\n..."
    },
    "paris": {
      "server": {
        "id": "paris",
        "name": "Paris VPN Server",
        "city": "Paris",
        "country": "France",
        "ip": "52.47.190.220",
        "port": 51820,
        "wireguardEndpoint": "52.47.190.220:51820"
      },
      "clientConfig": {
        "privateKey": "client_private_key",
        "publicKey": "client_public_key",
        "address": "10.0.2.2/32"
      },
      "wireguardConfig": "[Interface]\nPrivateKey = ...\n..."
    }
  }
}
```

### Speed Test Service

#### GET `/speedtest/config/{userId}`
Get speed test configuration and available servers.

**Headers:**
```
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "config": {
    "userId": "user123",
    "preferredServer": null,
    "autoTestEnabled": false,
    "testIntervalMinutes": 60
  },
  "servers": [
    {
      "id": "osaka",
      "name": "Osaka VPN Server",
      "host": "osaka.myvpn.com",
      "port": 443,
      "location": "Osaka",
      "country": "Japan",
      "ip": "15.168.240.118"
    },
    {
      "id": "paris",
      "name": "Paris VPN Server",
      "host": "paris.myvpn.com",
      "port": 443,
      "location": "Paris",
      "country": "France",
      "ip": "52.47.190.220"
    }
  ]
}
```

#### GET `/speedtest/ping`
Lightweight ping endpoint for latency measurement.

**Response:**
```json
{
  "success": true,
  "message": "pong",
  "timestamp": 1703123456789,
  "server": "VPN Backend"
}
```

#### GET `/speedtest/download`
Stream random bytes for download speed testing.

**Query Parameters:**
- `size`: Size in bytes (default: 10MB, max: 100MB)

**Headers:**
```
Content-Type: application/octet-stream
Content-Length: <size>
Cache-Control: no-cache, no-store, must-revalidate
```

#### POST `/speedtest/upload`
Accept data for upload speed testing.

**Query Parameters:**
- `expected_size`: Expected size in bytes

**Response:**
```json
{
  "success": true,
  "message": "Upload processed successfully",
  "sizeBytes": 5242880,
  "uploadTimeMs": 1250,
  "uploadSpeedMbps": "33.55"
}
```

## 🔐 Security Features

### Authentication
- JWT-based authentication for all sensitive endpoints
- User ID validation to prevent unauthorized access
- Token expiration and refresh mechanisms

### Rate Limiting
- Download endpoint: 20 requests per minute
- Upload endpoint: 20 requests per minute
- Automatic rate limit reset every minute

### Input Validation
- File size limits (max 100MB for download/upload)
- Content type validation
- Request parameter sanitization

## 🗄️ Database Schema

The backend uses the following tables:

### VPNServers
- `id`: Server identifier (osaka, paris)
- `name`: Human-readable server name
- `city`, `country`: Geographic location
- `ip`, `port`: Server endpoint
- `wireguardPublicKey`: Server's public key
- `wireguardEndpoint`: Full endpoint string
- `allowedIPs`: Allowed IP ranges
- `mtu`: Maximum transmission unit

### VPNClientConfigs
- `userId`: User identifier
- `serverId`: Server identifier
- `privateKey`, `publicKey`: Client keys
- `address`: Client IP address
- `dns`: DNS servers
- `mtu`: Client MTU

### SpeedTestServers
- `id`: Server identifier
- `name`: Server name
- `host`: Hostname
- `port`: Port number
- `location`: Geographic location
- `ip`: IP address

## 🚀 Production Deployment

### 1. HTTPS Configuration

Enable HTTPS using a reverse proxy (Nginx/Apache) or Ktor's built-in SSL:

```kotlin
// In Application.kt
embeddedServer(Netty, host = "0.0.0.0", port = 443) {
    module()
}.start(wait = true)
```

### 2. Environment Variables

Set production environment variables:

```bash
export NODE_ENV=production
export DATABASE_URL=your_production_db_url
export JWT_SECRET=your_very_secure_jwt_secret
export PORT=443
```

### 3. Firewall Configuration

Configure your servers to allow:
- UDP 51820 (WireGuard)
- TCP 443 (HTTPS)
- TCP 8080 (HTTP, if needed)

### 4. Monitoring and Logging

The backend includes comprehensive logging:
- Request/response logging
- Error tracking
- Performance metrics
- Security event logging

## 🔧 Troubleshooting

### Common Issues

1. **VPN Connection Fails**
   - Verify WireGuard keys are correctly configured
   - Check server firewall settings
   - Ensure IP forwarding is enabled on servers

2. **Speed Test Timeouts**
   - Check network connectivity
   - Verify rate limiting isn't blocking requests
   - Check server resource usage

3. **Authentication Errors**
   - Verify JWT secret is consistent
   - Check token expiration
   - Ensure proper Authorization header format

### Debug Endpoints

- `GET /` - Health check
- `GET /debug/users` - User session information
- `GET /vpn/servers` - Available VPN servers
- `GET /speedtest/servers` - Available speed test servers

## 📱 Android App Integration

The Android app expects these data formats:

### VPN Configuration
```kotlin
data class VPNConnectionResponse(
    val serverEndpoint: String,
    val internalIP: String,
    val dns: String,
    val mtu: Int,
    val allowedIPs: String,
    val serverPublicKey: String
)
```

### Speed Test Results
```kotlin
data class SpeedTestResults(
    val downloadSpeed: Float,  // Mbps
    val uploadSpeed: Float,    // Mbps
    val ping: Int,            // milliseconds
    val jitter: Int           // milliseconds
)
```

## 🎯 Next Steps

1. **Generate WireGuard keys** using the provided script
2. **Configure your EC2 servers** with the generated keys
3. **Update the backend database** with the public keys
4. **Test the VPN connections** from the Android app
5. **Verify speed test functionality** works through the VPN tunnel
6. **Deploy to production** with proper SSL certificates

## 📞 Support

For issues or questions:
1. Check the logs in the backend console
2. Verify server configurations
3. Test endpoints individually
4. Check database connectivity
5. Review firewall and security group settings

---

**Note**: This backend is designed to work seamlessly with your existing Android frontend. No changes to the Android app should be required beyond ensuring the API endpoints match.
