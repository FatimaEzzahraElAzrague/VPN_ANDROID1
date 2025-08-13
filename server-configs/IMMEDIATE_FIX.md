# IMMEDIATE FIX - Execute These Commands Now

## 🔥 Critical Issues Found:

### Paris Server:
- Client config has `10.0.2.2/32` but Android expects `10.66.66.2/32`

### Osaka Server: 
- Multiple `10.66.66.x` IPs are on `ens5` instead of `wg0` interface
- This causes traffic to route to wrong interface

---

## 🚀 PARIS SERVER FIXES (13.38.83.180)

```bash
# 1. Get current server config and derive Android public key
echo "kE3EMeOqSOPbf4I7rA/CqkCQA8DShPQQbxJsPg5xfFY=" | wg pubkey

# 2. Stop WireGuard
sudo systemctl stop wg-quick@wg0

# 3. Backup current config
sudo cp /etc/wireguard/wg0.conf /etc/wireguard/wg0.conf.backup

# 4. Get your server's private key
sudo cat /etc/wireguard/wg0.conf | grep PrivateKey

# 5. Create new fixed config
sudo tee /etc/wireguard/wg0.conf << 'EOF'
[Interface]
PrivateKey = YOUR_PARIS_SERVER_PRIVATE_KEY_HERE
Address = 10.66.66.1/24, fd42:42:42::1/64
ListenPort = 51820
MTU = 1420

# Enable IP forwarding and NAT
PostUp = echo 1 > /proc/sys/net/ipv4/ip_forward
PostUp = echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
PostUp = iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
PostUp = iptables -A FORWARD -i wg0 -o eth0 -j ACCEPT
PostUp = iptables -A FORWARD -i eth0 -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

PostDown = iptables -t nat -D POSTROUTING -o eth0 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -o eth0 -j ACCEPT
PostDown = iptables -D FORWARD -i eth0 -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

[Peer]
# Android Client - FIXED ADDRESS
PublicKey = ANDROID_PUBLIC_KEY_FROM_STEP_1
PresharedKey = EgWKN/5Nryhv3F1F7NZVdzF1qOqK4XjyDRV/nlGaWM0=
AllowedIPs = 10.66.66.2/32, fd42:42:42::2/128
PersistentKeepalive = 25
EOF

# 6. Enable IP forwarding permanently
echo 'net.ipv4.ip_forward=1' | sudo tee -a /etc/sysctl.conf
echo 'net.ipv6.conf.all.forwarding=1' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# 7. Start WireGuard
sudo systemctl start wg-quick@wg0
sudo systemctl enable wg-quick@wg0

# 8. Verify
sudo wg show
ip addr show wg0
```

---

## 🚀 OSAKA SERVER FIXES (56.155.92.31)

```bash
# 1. Get Android public key
echo "kMphb0Ww8aGc1kxHfbt2FL+q6YkPOvoP9xOxJuK3dFQ=" | wg pubkey

# 2. Stop WireGuard
sudo systemctl stop wg-quick@wg0

# 3. CRITICAL: Remove conflicting IPs from ens5
sudo ip addr del 10.66.66.2/32 dev ens5
sudo ip addr del 10.66.66.3/32 dev ens5
sudo ip addr del 10.66.66.4/32 dev ens5
sudo ip addr del 10.66.66.5/32 dev ens5

# 4. Backup current config
sudo cp /etc/wireguard/wg0.conf /etc/wireguard/wg0.conf.backup

# 5. Get your server's private key
sudo cat /etc/wireguard/wg0.conf | grep PrivateKey

# 6. Create new fixed config
sudo tee /etc/wireguard/wg0.conf << 'EOF'
[Interface]
PrivateKey = YOUR_OSAKA_SERVER_PRIVATE_KEY_HERE
Address = 10.66.66.1/24, fd42:42:42::1/64
ListenPort = 51820
MTU = 1420

# Enable IP forwarding and NAT
PostUp = echo 1 > /proc/sys/net/ipv4/ip_forward
PostUp = echo 1 > /proc/sys/net/ipv6/conf/all/forwarding
PostUp = iptables -t nat -A POSTROUTING -o ens5 -j MASQUERADE
PostUp = iptables -A FORWARD -i wg0 -o ens5 -j ACCEPT
PostUp = iptables -A FORWARD -i ens5 -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

PostDown = iptables -t nat -D POSTROUTING -o ens5 -j MASQUERADE
PostDown = iptables -D FORWARD -i wg0 -o ens5 -j ACCEPT
PostDown = iptables -D FORWARD -i ens5 -o wg0 -m state --state RELATED,ESTABLISHED -j ACCEPT

[Peer]
# Android Client - FIXED ADDRESS
PublicKey = ANDROID_PUBLIC_KEY_FROM_STEP_1
PresharedKey = OkW8zpjy57QcniepR66O0+awsoN+7/C3WVWnQxxhAK4=
AllowedIPs = 10.66.66.2/32, fd42:42:42::2/128
PersistentKeepalive = 25
EOF

# 7. Enable IP forwarding permanently
echo 'net.ipv4.ip_forward=1' | sudo tee -a /etc/sysctl.conf
echo 'net.ipv6.conf.all.forwarding=1' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p

# 8. Open firewall
sudo ufw allow 51820/udp

# 9. Start WireGuard
sudo systemctl start wg-quick@wg0
sudo systemctl enable wg-quick@wg0

# 10. Verify
sudo wg show
ip addr show wg0
ip addr show ens5
```

---

## 🔑 Key Replacement Steps:

### For Both Servers:

1. **Get Android public key** from step 1 output
2. **Get server private key** from step 5 output  
3. **Edit the config files** and replace:
   - `YOUR_PARIS_SERVER_PRIVATE_KEY_HERE` → Your actual server private key
   - `YOUR_OSAKA_SERVER_PRIVATE_KEY_HERE` → Your actual server private key
   - `ANDROID_PUBLIC_KEY_FROM_STEP_1` → The public key derived in step 1

---

## ✅ Expected Results:

### Paris Server:
- `wg0` interface gets `10.66.66.1/24`
- Android peer configured for `10.66.66.2/32`
- NAT rules route traffic through `eth0`

### Osaka Server:
- **NO MORE** `10.66.66.x` IPs on `ens5`
- `wg0` interface gets `10.66.66.1/24`  
- Android peer configured for `10.66.66.2/32`
- NAT rules route traffic through `ens5`

### Android App:
- ✅ Connects without "No Internet"
- ✅ Public IP becomes server IP
- ✅ Normal browsing works

---

## 🔧 Quick Verification:

After running the fixes, check:

```bash
# Should show recent handshake and traffic
sudo wg show

# Should show wg0 with 10.66.66.1/24
ip addr show wg0

# Should NOT show any 10.66.66.x (Osaka only)
ip addr show ens5
```

**This will completely fix your routing issues!** 🎉
