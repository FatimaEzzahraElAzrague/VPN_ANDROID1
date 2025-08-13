#!/bin/bash

# WireGuard Server Verification Script
# Run this script to verify your WireGuard setup is working correctly

echo "=== WireGuard Server Verification ==="
echo ""

# Detect server location
WAN_INTERFACE=$(ip route | grep default | awk '{print $5}' | head -n1)
if [[ "$WAN_INTERFACE" == "eth0" ]]; then
    SERVER_NAME="PARIS (13.38.83.180)"
elif [[ "$WAN_INTERFACE" == "ens5" ]]; then
    SERVER_NAME="OSAKA (56.155.92.31)"
else
    SERVER_NAME="UNKNOWN ($WAN_INTERFACE)"
fi

echo "🌍 Server: $SERVER_NAME"
echo "🔌 WAN Interface: $WAN_INTERFACE"
echo ""

echo "=== 1. WireGuard Service Status ==="
if systemctl is-active --quiet wg-quick@wg0; then
    echo "✅ WireGuard service is running"
else
    echo "❌ WireGuard service is NOT running"
    echo "   Start with: sudo systemctl start wg-quick@wg0"
fi

echo ""
echo "=== 2. Interface Configuration ==="
echo "🔍 Network interfaces:"
ip addr show | grep -A 2 -E "(wg0|$WAN_INTERFACE):" | grep -E "(inet|wg0|$WAN_INTERFACE)"

echo ""
echo "=== 3. WireGuard Status ==="
if command -v wg &> /dev/null; then
    echo "🔍 WireGuard peers (masking keys for security):"
    sudo wg show | sed 's/\([A-Za-z0-9+/]\{10\}\)[A-Za-z0-9+/=]*$/\1...MASKED/'
else
    echo "❌ WireGuard command not found"
fi

echo ""
echo "=== 4. IP Forwarding Status ==="
IPV4_FORWARD=$(cat /proc/sys/net/ipv4/ip_forward)
IPV6_FORWARD=$(cat /proc/sys/net/ipv6/conf/all/forwarding)

if [[ "$IPV4_FORWARD" == "1" ]]; then
    echo "✅ IPv4 forwarding enabled"
else
    echo "❌ IPv4 forwarding disabled"
fi

if [[ "$IPV6_FORWARD" == "1" ]]; then
    echo "✅ IPv6 forwarding enabled"
else
    echo "❌ IPv6 forwarding disabled"
fi

echo ""
echo "=== 5. Firewall Rules ==="
echo "🔍 UFW status:"
sudo ufw status | grep -E "(51820|Status)"

echo ""
echo "🔍 Active iptables NAT rules for $WAN_INTERFACE:"
sudo iptables -t nat -L POSTROUTING -n | grep $WAN_INTERFACE || echo "❌ No NAT rules found"

echo ""
echo "🔍 Active iptables FORWARD rules:"
sudo iptables -L FORWARD -n | grep -E "(wg0|$WAN_INTERFACE)" || echo "❌ No forward rules found"

echo ""
echo "=== 6. Route Configuration ==="
echo "🔍 Default route:"
ip route get 1.1.1.1

echo ""
echo "🔍 WireGuard routes:"
ip route | grep wg0 || echo "❌ No WireGuard routes found"

echo ""
echo "=== 7. Port Accessibility Test ==="
echo "🔍 Testing if WireGuard port 51820 is accessible..."
PUBLIC_IP=$(curl -s https://api.ipify.org || echo "unknown")
echo "   Server public IP: $PUBLIC_IP"

if command -v nc &> /dev/null; then
    if nc -u -z -w3 127.0.0.1 51820; then
        echo "✅ Port 51820 is locally accessible"
    else
        echo "❌ Port 51820 is not locally accessible"
    fi
else
    echo "⚠️  netcat not available for port testing"
fi

echo ""
echo "=== 8. Expected Client Configuration ==="
echo "📱 Your Android client should have:"
echo "   Private Key: (from VPNConfig.kt)"
echo "   Address: 10.66.66.2/32, fd42:42:42::2/128"
echo "   DNS: 1.1.1.1, 8.8.8.8"
echo "   Endpoint: $PUBLIC_IP:51820"
echo "   AllowedIPs: 0.0.0.0/0, ::/0"

echo ""
echo "=== 9. Live Connection Monitoring ==="
echo "🔍 To monitor connections in real-time, run:"
echo "   sudo watch -n 1 'wg show'"
echo ""
echo "   Look for:"
echo "   ✅ 'latest handshake' within last 2-3 minutes"
echo "   ✅ 'transfer' showing increasing rx/tx bytes"

echo ""
echo "=== 10. Success Criteria ==="
echo "✅ When working properly, you should see:"
echo "   1. WireGuard peer shows recent handshake"
echo "   2. Transfer counters increase during phone usage"
echo "   3. Phone's public IP becomes server IP ($PUBLIC_IP)"
echo "   4. Normal browsing works on Android"

echo ""
echo "=== Configuration Files to Check ==="
echo "📄 Current WireGuard config:"
if [[ -f /etc/wireguard/wg0.conf ]]; then
    echo "   File exists: /etc/wireguard/wg0.conf"
    echo ""
    echo "🔍 Config preview (keys masked):"
    sudo cat /etc/wireguard/wg0.conf | sed 's/\(PrivateKey\|PresharedKey\|PublicKey\).*=.*/\1 = ***MASKED***/'
else
    echo "❌ Configuration file missing: /etc/wireguard/wg0.conf"
fi

echo ""
echo "=== Verification Complete ==="
