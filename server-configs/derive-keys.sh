#!/bin/bash

# Script to derive WireGuard public keys from private keys
# Run this on your servers to get the public keys needed for peer configuration

echo "=== WireGuard Key Derivation Script ==="
echo ""

# Android Client Private Keys from VPNConfig.kt
PARIS_ANDROID_PRIVATE="kE3EMeOqSOPbf4I7rA/CqkCQA8DShPQQbxJsPg5xfFY="
OSAKA_ANDROID_PRIVATE="kMphb0Ww8aGc1kxHfbt2FL+q6YkPOvoP9xOxJuK3dFQ="

echo "Deriving Android client public keys..."
echo ""

echo "Paris Android Client:"
echo "Private Key: $PARIS_ANDROID_PRIVATE"
echo -n "Public Key:  "
echo "$PARIS_ANDROID_PRIVATE" | wg pubkey
echo ""

echo "Osaka Android Client:"
echo "Private Key: $OSAKA_ANDROID_PRIVATE"
echo -n "Public Key:  "
echo "$OSAKA_ANDROID_PRIVATE" | wg pubkey
echo ""

echo "=== Instructions ==="
echo "1. Copy the public keys above"
echo "2. Replace 'YOUR_ANDROID_CLIENT_PUBLIC_KEY_HERE' in your wg0.conf files"
echo "3. Make sure your server private keys are also set correctly"
echo "4. Restart WireGuard service: sudo systemctl restart wg-quick@wg0"
echo ""
echo "=== Server Public Key Derivation ==="
echo "To get your server public keys (if needed):"
echo "  Paris: sudo wg show wg0 public-key"
echo "  Osaka: sudo wg show wg0 public-key"
echo ""
echo "Or derive from private key:"
echo "  echo 'YOUR_SERVER_PRIVATE_KEY' | wg pubkey"
