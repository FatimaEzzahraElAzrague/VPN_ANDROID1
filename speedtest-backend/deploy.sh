#!/bin/bash

# VPN Speed Test Backend Deployment Script
# Run this script on both Osaka (56.155.92.31) and Paris (52.47.190.220) EC2 servers

set -e

echo "🚀 Starting VPN Speed Test Backend deployment..."

# Update system packages
echo "📦 Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Python 3.11 and required packages
echo "🐍 Installing Python 3.11 and dependencies..."
sudo apt install -y python3.11 python3.11-venv python3.11-pip python3-pip nginx certbot python3-certbot-nginx

# Create application directory
echo "📁 Setting up application directory..."
mkdir -p /home/ubuntu/speedtest-backend
cd /home/ubuntu/speedtest-backend

# Create Python virtual environment
echo "🔧 Creating Python virtual environment..."
python3.11 -m venv venv
source venv/bin/activate

# Install Python dependencies
echo "📚 Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

# Set proper permissions
echo "🔐 Setting permissions..."
sudo chown -R ubuntu:ubuntu /home/ubuntu/speedtest-backend
chmod +x /home/ubuntu/speedtest-backend/deploy.sh

# Setup systemd service
echo "⚙️ Setting up systemd service..."
sudo cp speedtest.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable speedtest.service

# Start the service
echo "🚀 Starting the service..."
sudo systemctl start speedtest.service

# Check service status
echo "📊 Checking service status..."
sudo systemctl status speedtest.service --no-pager

# Configure firewall (if using ufw)
echo "🔥 Configuring firewall..."
sudo ufw allow 8000/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

echo "✅ Deployment completed successfully!"
echo "🌐 Service is running on http://0.0.0.0:8000"
echo "📱 Test endpoints:"
echo "   - GET /ping"
echo "   - GET /download"
echo "   - POST /upload"
echo ""
echo "🔧 To manage the service:"
echo "   - Check status: sudo systemctl status speedtest.service"
echo "   - Restart: sudo systemctl restart speedtest.service"
echo "   - View logs: sudo journalctl -u speedtest.service -f"
