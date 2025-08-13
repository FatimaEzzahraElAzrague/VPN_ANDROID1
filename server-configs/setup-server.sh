#!/bin/bash

# WireGuard Server Setup Script
# Run this script on both Paris and Osaka servers to configure proper routing

set -e

echo "=== WireGuard Server Setup Script ==="
echo ""

# Detect the WAN interface (the one with the default route)
WAN_INTERFACE=$(ip route | grep default | awk '{print $5}' | head -n1)
echo "🔍 Detected WAN interface: $WAN_INTERFACE"

# Check if we're on Paris (eth0) or Osaka (ens5) based on interface
if [[ "$WAN_INTERFACE" == "eth0" ]]; then
    SERVER_NAME="PARIS"
    echo "📍 Detected Paris server (WAN: eth0)"
elif [[ "$WAN_INTERFACE" == "ens5" ]]; then
    SERVER_NAME="OSAKA"
    echo "📍 Detected Osaka server (WAN: ens5)"
else
    echo "⚠️  Unknown WAN interface: $WAN_INTERFACE"
    echo "Please verify this is correct before proceeding."
    SERVER_NAME="UNKNOWN"
fi

echo ""
echo "=== Step 1: Enable IP Forwarding ==="
echo "Enabling IPv4 and IPv6 forwarding..."

# Enable IP forwarding persistently
echo 'net.ipv4.ip_forward=1' | sudo tee -a /etc/sysctl.conf
echo 'net.ipv6.conf.all.forwarding=1' | sudo tee -a /etc/sysctl.conf

# Apply immediately
sudo sysctl -p

echo "✅ IP forwarding enabled"
echo ""

echo "=== Step 2: Configure Firewall Rules ==="
echo "Setting up iptables rules for WireGuard..."

# Allow WireGuard port
sudo ufw allow 51820/udp
echo "✅ UFW: Allowed UDP/51820"

# Configure iptables for NAT and forwarding
sudo iptables -t nat -A POSTROUTING -o $WAN_INTERFACE -j MASQUERADE
sudo iptables -A FORWARD -i wg0 -o $WAN_INTERFACE -j ACCEPT
sudo iptables -A FORWARD -i $WAN_INTERFACE -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

echo "✅ IPv4 NAT and forwarding rules added"

# Configure ip6tables for IPv6 (if available)
if command -v ip6tables &> /dev/null; then
    sudo ip6tables -t nat -A POSTROUTING -o $WAN_INTERFACE -j MASQUERADE 2>/dev/null || echo "⚠️  IPv6 NAT not supported (normal on many systems)"
    sudo ip6tables -A FORWARD -i wg0 -o $WAN_INTERFACE -j ACCEPT
    sudo ip6tables -A FORWARD -i $WAN_INTERFACE -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT
    echo "✅ IPv6 forwarding rules added"
fi

echo ""
echo "=== Step 3: Clean Up Conflicting IP Assignments ==="
echo "Removing any 10.66.66.x addresses from non-wg interfaces..."

# Remove any 10.66.66.x addresses from non-wg interfaces
for iface in $(ip link show | grep -E '^[0-9]+:' | cut -d: -f2 | tr -d ' '); do
    if [[ "$iface" != "wg0" && "$iface" != "lo" ]]; then
        # Check if this interface has 10.66.66.x addresses
        if ip addr show $iface | grep -q "10\.66\.66\."; then
            echo "⚠️  Found 10.66.66.x on interface $iface, removing..."
            sudo ip addr flush dev $iface scope global || true
        fi
    fi
done

echo "✅ Cleaned up conflicting IP assignments"
echo ""

echo "=== Step 4: Install/Update WireGuard ==="
if ! command -v wg &> /dev/null; then
    echo "Installing WireGuard..."
    sudo apt update
    sudo apt install -y wireguard
else
    echo "✅ WireGuard already installed"
fi

echo ""
echo "=== Step 5: Backup Existing Configuration ==="
if [[ -f /etc/wireguard/wg0.conf ]]; then
    echo "Backing up existing wg0.conf..."
    sudo cp /etc/wireguard/wg0.conf /etc/wireguard/wg0.conf.backup.$(date +%Y%m%d_%H%M%S)
    echo "✅ Backup created"
fi

echo ""
echo "=== Setup Complete! ==="
echo ""
echo "📋 Next Steps:"
echo "1. Copy the appropriate wg0.conf file to /etc/wireguard/"
echo "   - For Paris: Use paris-wg0.conf"
echo "   - For Osaka: Use osaka-wg0.conf"
echo ""
echo "2. Replace placeholder keys in the config:"
echo "   - YOUR_${SERVER_NAME}_SERVER_PRIVATE_KEY_HERE"
echo "   - YOUR_ANDROID_CLIENT_PUBLIC_KEY_HERE"
echo ""
echo "3. Start WireGuard:"
echo "   sudo systemctl enable wg-quick@wg0"
echo "   sudo systemctl start wg-quick@wg0"
echo ""
echo "4. Verify setup with verification script"
echo ""
echo "🔧 Current network configuration:"
ip addr show | grep -E "(inet|wg0|$WAN_INTERFACE)"
