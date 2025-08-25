# Production Deployment Script for VPN Backend
# This script deploys the Ktor backend to EC2 for VPN configuration serving

Write-Host "🚀 VPN Backend Production Deployment" -ForegroundColor Green
Write-Host "=====================================" -ForegroundColor Green

# Step 1: Build the backend
Write-Host "📦 Building backend..." -ForegroundColor Yellow
.\gradlew.bat clean build

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Backend built successfully" -ForegroundColor Green

# Step 2: Create deployment package
Write-Host "📋 Creating deployment package..." -ForegroundColor Yellow

$deployDir = "deploy"
if (Test-Path $deployDir) {
    Remove-Item $deployDir -Recurse -Force
}
New-Item -ItemType Directory -Path $deployDir

# Copy JAR file
Copy-Item "build/libs/backend-*.jar" "$deployDir/backend.jar"

# Copy environment file
Copy-Item "env.txt" "$deployDir/.env"

# Copy startup script
@"
#!/bin/bash
# Production startup script for VPN Backend

# Set environment variables
export JAVA_OPTS="-Xmx512m -Xms256m"

# Start the backend
java -jar backend.jar
"@ | Out-File -FilePath "$deployDir/start.sh" -Encoding UTF8

# Copy systemd service file
@"
[Unit]
Description=VPN Backend Service
After=network.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/vpn-backend
ExecStart=/usr/bin/java -jar backend.jar
Restart=always
RestartSec=10
Environment=JAVA_OPTS=-Xmx512m -Xms256m

[Install]
WantedBy=multi-user.target
"@ | Out-File -FilePath "$deployDir/vpn-backend.service" -Encoding UTF8

Write-Host "✅ Deployment package created in $deployDir" -ForegroundColor Green

# Step 3: Deployment instructions
Write-Host ""
Write-Host "🌐 DEPLOYMENT INSTRUCTIONS:" -ForegroundColor Cyan
Write-Host "==========================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Upload files to EC2:" -ForegroundColor White
Write-Host "   scp -r $deployDir/* ubuntu@YOUR_EC2_IP:/home/ubuntu/vpn-backend/"
Write-Host ""
Write-Host "2. SSH into EC2:" -ForegroundColor White
Write-Host "   ssh ubuntu@YOUR_EC2_IP"
Write-Host ""
Write-Host "3. Set up the service:" -ForegroundColor White
Write-Host "   cd /home/ubuntu/vpn-backend"
Write-Host "   sudo cp vpn-backend.service /etc/systemd/system/"
Write-Host "   sudo systemctl daemon-reload"
Write-Host "   sudo systemctl enable vpn-backend"
Write-Host "   sudo systemctl start vpn-backend"
Write-Host ""
Write-Host "4. Check service status:" -ForegroundColor White
Write-Host "   sudo systemctl status vpn-backend"
Write-Host ""
Write-Host "5. View logs:" -ForegroundColor White
Write-Host "   sudo journalctl -u vpn-backend -f"
Write-Host ""
Write-Host "6. Configure firewall:" -ForegroundColor White
Write-Host "   sudo ufw allow 8080/tcp  # Backend API"
Write-Host "   sudo ufw allow 51820/udp # WireGuard"
Write-Host ""
Write-Host "✅ Backend will be accessible at:" -ForegroundColor Green
Write-Host "   http://YOUR_EC2_IP:8080"
Write-Host "   VPN Config: http://YOUR_EC2_IP:8080/vpn/config/{userId}/minimal"
Write-Host ""

# Step 4: Environment configuration check
Write-Host "🔧 ENVIRONMENT CONFIGURATION:" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Make sure your .env file contains:" -ForegroundColor White
Write-Host "✅ DATABASE_URL (PostgreSQL connection)"
Write-Host "✅ JWT_SECRET (for authentication)"
Write-Host "✅ OSAKA_SERVER_IP, OSAKA_SERVER_PORT, OSAKA_SERVER_PUBLIC_KEY"
Write-Host "✅ PARIS_SERVER_IP, PARIS_SERVER_PORT, PARIS_SERVER_PUBLIC_KEY"
Write-Host ""

Write-Host "🎯 VPN ENDPOINTS:" -ForegroundColor Cyan
Write-Host "=================" -ForegroundColor Cyan
Write-Host ""
Write-Host "GET /vpn/config/{userId}/minimal" -ForegroundColor White
Write-Host "   Returns WireGuard config for both Osaka and Paris servers"
Write-Host "   Requires: Authorization: Bearer <JWT_TOKEN>"
Write-Host "   Response format:" -ForegroundColor Gray
Write-Host "   {"
Write-Host "     'success': true,"
Write-Host "     'configs': {"
Write-Host "       'osaka': {"
Write-Host "         'server_ip': '56.155.92.31',"
Write-Host "         'server_port': 51820,"
Write-Host "         'server_public_key': '...',"
Write-Host "         'client_private_key': '...',"
Write-Host "         'client_public_key': '...',"
Write-Host "         'allowed_ips': '0.0.0.0/0',"
Write-Host "         'dns': '8.8.8.8'"
Write-Host "       },"
Write-Host "       'paris': { ... }"
Write-Host "     }"
Write-Host "   }"
Write-Host ""

Write-Host "🚀 Ready for deployment!" -ForegroundColor Green
