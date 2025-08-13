# WireGuard VPN Fix Implementation Guide

## Problem Summary
Your Android VPN app shows "No Internet" because there's a mismatch between:
- **Android client expects**: `10.66.66.2/32` address
- **Server peer configured for**: `10.0.2.2/32` address

Plus missing IP forwarding and NAT rules on the servers.

## Solution Overview
1. **Standardize addressing** to `10.66.66.x/24` network
2. **Enable IP forwarding** on both servers
3. **Configure NAT/MASQUERADE** for internet access
4. **Verify firewall rules** allow WireGuard traffic

---

## Step 1: Get Android Client Public Keys

On either server, run the key derivation script:

```bash
# Make script executable
chmod +x derive-keys.sh

# Run to get public keys
./derive-keys.sh
```

**Expected output:**
```
Paris Android Client:
Private Key: kE3EMeOqSOPbf4I7rA/CqkCQA8DShPQQbxJsPg5xfFY=
Public Key:  [DERIVED_PUBLIC_KEY_PARIS]

Osaka Android Client:
Private Key: kMphb0Ww8aGc1kxHfbt2FL+q6YkPOvoP9xOxJuK3dFQ=
Public Key:  [DERIVED_PUBLIC_KEY_OSAKA]
```

**📝 Note down these public keys** - you'll need them for the server configs.

---

## Step 2: Configure Paris Server (13.38.83.180)

### 2.1 Setup Server Environment
```bash
# Copy and run the setup script
chmod +x setup-server.sh
sudo ./setup-server.sh
```

### 2.2 Install WireGuard Configuration
```bash
# Stop existing WireGuard (if running)
sudo systemctl stop wg-quick@wg0 2>/dev/null || true

# Copy configuration
sudo cp paris-wg0.conf /etc/wireguard/wg0.conf

# Edit the config to add your actual keys
sudo nano /etc/wireguard/wg0.conf
```

**Replace these placeholders:**
- `YOUR_PARIS_SERVER_PRIVATE_KEY_HERE` → Your Paris server's private key
- `YOUR_ANDROID_CLIENT_PUBLIC_KEY_HERE` → Public key from Step 1

### 2.3 Start WireGuard
```bash
# Enable and start service
sudo systemctl enable wg-quick@wg0
sudo systemctl start wg-quick@wg0

# Check status
sudo systemctl status wg-quick@wg0
```

---

## Step 3: Configure Osaka Server (56.155.92.31)

### 3.1 Setup Server Environment
```bash
# Copy and run the setup script
chmod +x setup-server.sh
sudo ./setup-server.sh
```

### 3.2 Install WireGuard Configuration
```bash
# Stop existing WireGuard (if running)
sudo systemctl stop wg-quick@wg0 2>/dev/null || true

# Copy configuration
sudo cp osaka-wg0.conf /etc/wireguard/wg0.conf

# Edit the config to add your actual keys
sudo nano /etc/wireguard/wg0.conf
```

**Replace these placeholders:**
- `YOUR_OSAKA_SERVER_PRIVATE_KEY_HERE` → Your Osaka server's private key
- `YOUR_ANDROID_CLIENT_PUBLIC_KEY_HERE` → Public key from Step 1

### 3.3 Start WireGuard
```bash
# Enable and start service
sudo systemctl enable wg-quick@wg0
sudo systemctl start wg-quick@wg0

# Check status
sudo systemctl status wg-quick@wg0
```

---

## Step 4: Verify Server Configurations

On **both servers**, run the verification script:

```bash
chmod +x verify-setup.sh
./verify-setup.sh
```

### Expected Output Checklist:
- ✅ WireGuard service is running
- ✅ IPv4 forwarding enabled
- ✅ IPv6 forwarding enabled
- ✅ UFW allows port 51820
- ✅ NAT rules present for WAN interface
- ✅ Forward rules present
- ✅ Port 51820 locally accessible

---

## Step 5: Test Android Connection

### 5.1 Test Paris Server
1. **Connect your Android** to Paris server
2. **Monitor server** with: `sudo watch -n 1 'wg show'`
3. **Look for**:
   - Recent handshake (within 2-3 minutes)
   - Increasing transfer bytes during browsing

### 5.2 Test Osaka Server
1. **Connect your Android** to Osaka server
2. **Monitor server** with: `sudo watch -n 1 'wg show'`
3. **Look for**:
   - Recent handshake (within 2-3 minutes)
   - Increasing transfer bytes during browsing

---

## Step 6: Verify Success

### On Android Phone:
1. **Check IP address**: Go to https://whatismyipaddress.com/
   - Should show **Paris IP**: `13.38.83.180`
   - Should show **Osaka IP**: `56.155.92.31`
2. **Browse normally** - internet should work
3. **No "No Internet" notification**

### On Servers:
```bash
# Real-time monitoring
sudo wg show

# Should show something like:
# peer: [CLIENT_PUBLIC_KEY]
#   latest handshake: 32 seconds ago
#   transfer: 1.23 MiB received, 8.45 MiB sent
```

---

## Troubleshooting

### If "No Internet" persists:

1. **Check server logs**:
   ```bash
   sudo journalctl -u wg-quick@wg0 -f
   ```

2. **Verify NAT is working**:
   ```bash
   # Test from server
   ping -c 3 8.8.8.8
   
   # Check if masquerade is working
   sudo iptables -t nat -L POSTROUTING -n -v
   ```

3. **Check firewall**:
   ```bash
   # Ensure port is open
   sudo ufw status verbose
   
   # Check if blocked by cloud security groups
   # (verify in AWS/cloud console)
   ```

4. **Restart everything**:
   ```bash
   sudo systemctl restart wg-quick@wg0
   sudo systemctl restart networking
   ```

### If handshake fails:

1. **Verify client/server key match**
2. **Check endpoint is reachable**: `ping 13.38.83.180`
3. **Verify PSK matches** on both sides
4. **Check AllowedIPs** matches client address

---

## Security Notes

- **Private keys** are shown as placeholders - never commit real keys to git
- **PresharedKeys** add extra security layer - keep them secret
- **Server configs** should have `600` permissions: `sudo chmod 600 /etc/wireguard/wg0.conf`

---

## Files Created

- `paris-wg0.conf` - Paris server configuration
- `osaka-wg0.conf` - Osaka server configuration  
- `derive-keys.sh` - Key derivation utility
- `setup-server.sh` - Server setup automation
- `verify-setup.sh` - Verification script
- `IMPLEMENTATION_GUIDE.md` - This guide

---

## Success Criteria ✅

When everything works correctly:

1. **Android connects** without "No Internet" notification
2. **Public IP changes** to server IP (13.38.83.180 or 56.155.92.31)
3. **Normal browsing works** on mobile
4. **Server shows** recent handshakes and increasing transfer
5. **No more connection timeouts** or auto-disconnects

The tunnel routing problem will be **completely fixed**! 🎉
