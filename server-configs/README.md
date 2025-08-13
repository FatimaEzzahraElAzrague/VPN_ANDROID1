# WireGuard Server Configuration Files

This directory contains the configuration files and scripts to fix your Android VPN "No Internet" issue.

## 🚨 The Problem
Your Android app expects `10.66.66.2/32` but your servers are configured for `10.0.2.2/32`. This address mismatch prevents proper routing.

## 📁 Files Included

| File | Purpose |
|------|---------|
| `paris-wg0.conf` | WireGuard config for Paris server (13.38.83.180) |
| `osaka-wg0.conf` | WireGuard config for Osaka server (56.155.92.31) |
| `derive-keys.sh` | Generate Android client public keys from private keys |
| `setup-server.sh` | Automated server setup (IP forwarding, firewall, NAT) |
| `verify-setup.sh` | Verify server configuration is working |
| `IMPLEMENTATION_GUIDE.md` | Complete step-by-step implementation instructions |

## 🚀 Quick Start

1. **Copy files to both servers**
2. **Run setup script**: `sudo ./setup-server.sh`
3. **Get client public keys**: `./derive-keys.sh`
4. **Install config**: Copy appropriate `*-wg0.conf` to `/etc/wireguard/wg0.conf`
5. **Edit keys**: Replace placeholders with actual keys
6. **Start WireGuard**: `sudo systemctl start wg-quick@wg0`
7. **Verify**: `./verify-setup.sh`

## ✅ Expected Result

After implementing these configs:
- ✅ Android connects without "No Internet" 
- ✅ Phone's public IP becomes server IP
- ✅ Normal browsing works
- ✅ Server shows active handshakes and traffic

## 📖 Detailed Instructions

See `IMPLEMENTATION_GUIDE.md` for complete step-by-step instructions.

## 🔧 Key Changes Made

1. **Standardized addressing** to `10.66.66.x/24` network
2. **Added IP forwarding** and NAT rules  
3. **Fixed AllowedIPs** to match Android client
4. **Configured proper PostUp/PostDown** rules
5. **Automated server setup** process

Your WireGuard tunnel will work properly after applying these configurations! 🎉
