# VPN API Setup Guide

## Overview
Your Android app now integrates with the same centralized VPN management system used by your desktop version. This system automatically generates WireGuard keys and manages connections.

## What Changed
1. **Automatic Key Generation**: No more manual key management
2. **Dynamic IP Assignment**: Server handles IP conflicts automatically
3. **Centralized Management**: Same system as desktop version
4. **Fresh Keys**: New keys generated for each connection

## Configuration Required

### Step 1: Update VPN API Server URL
In `app/src/main/java/com/example/v/config/VPNConfig.kt`, update:

```kotlin
object VPNApiConfig {
    // TODO: Update this URL to your actual VPN API server
    const val BASE_URL = "http://YOUR_SERVER_IP:8000" // Replace with your actual server IP
    const val AGENT_TOKEN = "vpn-agent-secret-token-2024" // Same as desktop version
}
```

**Replace `YOUR_SERVER_IP` with the IP address of the server running your desktop VPN API.**

### Step 2: Verify Server Locations
The app now uses these server IDs that match your API:
- `paris` → Europe (Paris)
- `osaka` → Asia Pacific (Osaka)
- `virginia` → US East (Virginia)
- `oregon` → US West (Oregon)
- `london` → Europe (London)
- `frankfurt` → Europe (Frankfurt)
- `mumbai` → Asia Pacific (Mumbai)
- `seoul` → Asia Pacific (Seoul)
- `saopaulo` → South America (São Paulo)

## How It Works

1. **User selects server** (e.g., Osaka)
2. **App calls `/vpn/connect`** with location "osaka"
3. **Server generates new keys** automatically
4. **Server assigns available IP** from subnet
5. **Server adds peer** to WireGuard via agent API
6. **App receives complete config** with all keys
7. **App connects** using generated configuration

## Benefits

✅ **No more key conflicts** - Fresh keys each time  
✅ **Automatic IP management** - Server handles subnet conflicts  
✅ **Centralized control** - Same system as desktop  
✅ **Scalable** - Easy to add new servers  
✅ **Secure** - Keys generated per connection  

## Troubleshooting

### Connection Fails
1. Check VPN API server is running
2. Verify server IP in `VPNApiConfig.BASE_URL`
3. Check server logs for errors
4. Ensure `AGENT_TOKEN` matches server configuration

### Server Not Found
1. Verify server location names match API expectations
2. Check server is online and accessible
3. Verify firewall allows connections on port 8000

## Testing

1. **Update the BASE_URL** with your actual server IP
2. **Build and run** the app
3. **Select a server** (e.g., Osaka)
4. **Tap Connect** - should automatically generate keys and connect

## Next Steps

After configuring:
1. Test connection to Osaka server
2. Test connection to Paris server
3. Add more servers as needed
4. Monitor server logs for any issues

## Support

If you encounter issues:
1. Check server logs for error messages
2. Verify network connectivity to VPN API server
3. Ensure server has all required dependencies installed
4. Check that WireGuard agent is running on target servers
