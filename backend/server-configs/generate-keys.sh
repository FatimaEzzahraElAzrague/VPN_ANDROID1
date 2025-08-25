#!/bin/bash

# WireGuard Key Generation Script for VPN Servers
# This script generates private/public key pairs for Osaka and Paris servers

echo "🔐 Generating WireGuard keys for VPN servers..."

# Create keys directory
mkdir -p keys

# Generate Osaka server keys
echo "🇯🇵 Generating Osaka server keys..."
wg genkey | tee keys/osaka_private.key | wg pubkey > keys/osaka_public.key
echo "✅ Osaka private key: keys/osaka_private.key"
echo "✅ Osaka public key: keys/osaka_public.key"

# Generate Paris server keys
echo "🇫🇷 Generating Paris server keys..."
wg genkey | tee keys/paris_private.key | wg pubkey > keys/paris_public.key
echo "✅ Paris private key: keys/paris_private.key"
echo "✅ Paris public key: keys/paris_public.key"

# Display public keys for easy copying
echo ""
echo "📋 PUBLIC KEYS (copy these to your server configs):"
echo "=================================================="
echo "🇯🇵 Osaka Server Public Key:"
cat keys/osaka_public.key
echo ""
echo "🇫🇷 Paris Server Public Key:"
cat keys/paris_public.key
echo ""
echo "=================================================="

# Generate client key pairs for testing
echo "📱 Generating client test keys..."
wg genkey | tee keys/client1_private.key | wg pubkey > keys/client1_public.key
wg genkey | tee keys/client2_private.key | wg pubkey > keys/client2_public.key

echo "✅ Client 1 private key: keys/client1_private.key"
echo "✅ Client 1 public key: keys/client1_public.key"
echo "✅ Client 2 private key: keys/client2_private.key"
echo "✅ Client 2 public key: keys/client2_public.key"

# Create a summary file
cat > keys/keys_summary.txt << EOF
WireGuard Keys Summary
======================

OSAKA SERVER (15.168.240.118:51820)
====================================
Private Key: $(cat keys/osaka_private.key)
Public Key: $(cat keys/osaka_public.key)

PARIS SERVER (52.47.190.220:51820)
===================================
Private Key: $(cat keys/paris_private.key)
Public Key: $(cat keys/paris_public.key)

CLIENT TEST KEYS
================
Client 1 Private: $(cat keys/client1_private.key)
Client 1 Public: $(cat keys/client1_public.key)

Client 2 Private: $(cat keys/client2_private.key)
Client 2 Public: $(cat keys/client2_public.key)

USAGE INSTRUCTIONS
==================
1. Copy the private keys to your server WireGuard configs
2. Use the public keys in your backend database
3. Generate unique client keys for each user
4. Keep private keys secure and never share them
EOF

echo ""
echo "📄 Keys summary saved to: keys/keys_summary.txt"
echo ""
echo "🎉 Key generation completed successfully!"
echo "💡 Next steps:"
echo "   1. Copy private keys to your server configs"
echo "   2. Update the backend database with public keys"
echo "   3. Test the VPN connections"
