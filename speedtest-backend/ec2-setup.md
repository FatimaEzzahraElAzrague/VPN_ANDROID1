# EC2 Server Setup Guide

This guide provides step-by-step instructions for setting up the VPN Speed Test Backend on your EC2 servers.

## 🖥️ Server Information

- **Osaka Server**: 56.155.92.31
- **Paris Server**: 52.47.190.220
- **Operating System**: Ubuntu (assumed)
- **Python Version**: 3.11

## 🔐 Step 1: EC2 Security Group Configuration

### Configure Security Group for Port 8000

1. **Open AWS Console**
   - Go to [EC2 Console](https://console.aws.amazon.com/ec2/)
   - Select your region

2. **Navigate to Security Groups**
   - Click on "Security Groups" in the left sidebar
   - Find the security group attached to your EC2 instances

3. **Add Inbound Rule for Port 8000**
   - Select your security group
   - Click "Edit inbound rules"
   - Click "Add rule"
   - Configure the rule:
     - **Type**: Custom TCP
     - **Port range**: 8000
     - **Source**: 0.0.0.0/0 (or restrict to specific IP ranges)
     - **Description**: VPN Speed Test Backend
   - Click "Save rules"

4. **Verify Rules**
   - Ensure you have these inbound rules:
     - SSH (Port 22) - for server access
     - HTTP (Port 80) - optional for Nginx
     - HTTPS (Port 443) - optional for Nginx
     - Custom TCP (Port 8000) - for FastAPI backend

## 🚀 Step 2: Deploy to Osaka Server (56.155.92.31)

### Upload Files
```bash
# From your local machine
scp -r speedtest-backend/ ubuntu@56.155.92.31:/home/ubuntu/
```

### SSH into Server
```bash
ssh ubuntu@56.155.92.31
```

### Run Deployment
```bash
cd speedtest-backend
chmod +x deploy.sh
./deploy.sh
```

### Verify Deployment
```bash
# Check service status
sudo systemctl status speedtest.service

# Test local endpoint
curl http://localhost:8000/ping

# Check logs
sudo journalctl -u speedtest.service -f
```

## 🚀 Step 3: Deploy to Paris Server (52.47.190.220)

### Upload Files
```bash
# From your local machine
scp -r speedtest-backend/ ubuntu@52.47.190.220:/home/ubuntu/
```

### SSH into Server
```bash
ssh ubuntu@52.47.190.220
```

### Run Deployment
```bash
cd speedtest-backend
chmod +x deploy.sh
./deploy.sh
```

### Verify Deployment
```bash
# Check service status
sudo systemctl status speedtest.service

# Test local endpoint
curl http://localhost:8000/ping

# Check logs
sudo journalctl -u speedtest.service -f
```

## 🧪 Step 4: Test Both Servers

### Run Test Script
```bash
# Install requests if needed
pip install requests

# Run the test script
python3 test_endpoints.py
```

### Manual Testing with curl

**Test Osaka Server:**
```bash
# Ping test
curl http://56.155.92.31:8000/ping

# Download test (10MB)
curl -o test_osaka.bin "http://56.155.92.31:8000/download?size=10485760"

# Upload test (5MB)
dd if=/dev/urandom of=test_upload.bin bs=1M count=5
curl -X POST -F "file=@test_upload.bin" "http://56.155.92.31:8000/upload?expected_size=5242880"
```

**Test Paris Server:**
```bash
# Ping test
curl http://52.47.190.220:8000/ping

# Download test (10MB)
curl -o test_paris.bin "http://52.47.190.220:8000/download?size=10485760"

# Upload test (5MB)
curl -X POST -F "file=@test_upload.bin" "http://52.47.190.220:8000/upload?expected_size=5242880"
```

## 🔒 Step 5: Optional HTTPS Setup

### Install Nginx
```bash
sudo apt install -y nginx
```

### Configure Nginx
```bash
# Copy configuration
sudo cp nginx.conf /etc/nginx/sites-available/speedtest

# Enable site
sudo ln -s /etc/nginx/sites-available/speedtest /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Restart Nginx
sudo systemctl restart nginx
```

### Setup SSL with Let's Encrypt
```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get SSL certificate (replace with your domain)
sudo certbot --nginx -d yourdomain.com

# Test auto-renewal
sudo certbot renew --dry-run
```

## 📊 Step 6: Monitoring & Maintenance

### Service Management
```bash
# Start service
sudo systemctl start speedtest.service

# Stop service
sudo systemctl stop speedtest.service

# Restart service
sudo systemctl restart speedtest.service

# Enable auto-start
sudo systemctl enable speedtest.service

# Check status
sudo systemctl status speedtest.service
```

### Log Management
```bash
# View real-time logs
sudo journalctl -u speedtest.service -f

# View recent logs
sudo journalctl -u speedtest.service --since "1 hour ago"

# View logs for specific time
sudo journalctl -u speedtest.service --since "2024-01-01 00:00:00"
```

### Performance Monitoring
```bash
# Check system resources
htop

# Check disk usage
df -h

# Check memory usage
free -h

# Check network connections
netstat -tulpn | grep :8000
```

## 🚨 Troubleshooting

### Common Issues

1. **Port 8000 not accessible from outside:**
   ```bash
   # Check if service is running
   sudo systemctl status speedtest.service
   
   # Check if port is listening
   sudo netstat -tulpn | grep :8000
   
   # Check firewall
   sudo ufw status
   ```

2. **Service won't start:**
   ```bash
   # Check logs
   sudo journalctl -u speedtest.service -n 50
   
   # Check Python environment
   source venv/bin/activate
   python --version
   pip list
   ```

3. **Permission denied:**
   ```bash
   # Fix ownership
   sudo chown -R ubuntu:ubuntu /home/ubuntu/speedtest-backend
   
   # Fix permissions
   chmod +x /home/ubuntu/speedtest-backend/deploy.sh
   ```

4. **Memory issues:**
   ```bash
   # Check available memory
   free -h
   
   # Check swap
   swapon --show
   
   # Monitor during tests
   htop
   ```

### Performance Tuning

1. **Increase chunk size** in `main.py`:
   ```python
   CHUNK_SIZE = 128 * 1024  # Increase from 64KB to 128KB
   ```

2. **Adjust Nginx buffers** in `nginx.conf`:
   ```nginx
   proxy_buffer_size 8k;
   proxy_buffers 16 8k;
   ```

3. **Optimize Python settings**:
   ```bash
   # Add to systemd service file
   Environment=PYTHONUNBUFFERED=1
   Environment=PYTHONDONTWRITEBYTECODE=1
   ```

## 🔄 Updates & Maintenance

### Update Backend
```bash
cd /home/ubuntu/speedtest-backend

# Upload new files or git pull
# Then restart service
sudo systemctl restart speedtest.service
```

### Backup Configuration
```bash
# Create backup directory
mkdir -p /home/ubuntu/speedtest-backend/backup

# Backup service file
sudo cp /etc/systemd/system/speedtest.service /home/ubuntu/speedtest-backend/backup/

# Backup Nginx config
sudo cp /etc/nginx/sites-available/speedtest /home/ubuntu/speedtest-backend/backup/
```

### Health Checks
```bash
# Create health check script
cat > /home/ubuntu/speedtest-backend/health_check.sh << 'EOF'
#!/bin/bash
if curl -s http://localhost:8000/health | grep -q "healthy"; then
    echo "Service is healthy"
    exit 0
else
    echo "Service is unhealthy"
    exit 1
fi
EOF

chmod +x /home/ubuntu/speedtest-backend/health_check.sh

# Add to crontab for monitoring
crontab -e
# Add: */5 * * * * /home/ubuntu/speedtest-backend/health_check.sh
```

## ✅ Verification Checklist

- [ ] EC2 security group allows port 8000
- [ ] Files uploaded to both servers
- [ ] Deployment script executed successfully
- [ ] Service running on both servers
- [ ] Endpoints accessible from outside
- [ ] Download test working
- [ ] Upload test working
- [ ] Ping test working
- [ ] Optional: Nginx configured
- [ ] Optional: SSL certificate installed
- [ ] Android app can connect to both servers

## 📞 Support Commands

```bash
# Quick health check
curl -s http://56.155.92.31:8000/health
curl -s http://52.47.190.220:8000/health

# Service status
sudo systemctl status speedtest.service

# Recent logs
sudo journalctl -u speedtest.service --since "10 minutes ago"

# Network connectivity
telnet 56.155.92.31 8000
telnet 52.47.190.220 8000
```

---

**Note**: Keep your EC2 security groups restrictive and only open necessary ports. Consider using VPC and private subnets for additional security if needed.
