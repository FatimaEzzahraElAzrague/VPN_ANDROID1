# VPN Speed Test Backend

FastAPI backend for measuring VPN connection speeds on EC2 servers. This backend provides endpoints for download speed testing, upload speed testing, and ping/latency measurement.

## 🚀 Quick Start

### Prerequisites
- Ubuntu 20.04+ EC2 instance
- Python 3.11
- SSH access to the server

### Deployment

1. **Clone/Upload files to your EC2 server:**
```bash
# On your local machine, upload the files
scp -r speedtest-backend/ ubuntu@56.155.92.31:/home/ubuntu/
scp -r speedtest-backend/ ubuntu@52.47.190.220:/home/ubuntu/
```

2. **SSH into each server and run the deployment script:**
```bash
# Osaka Server (56.155.92.31)
ssh ubuntu@56.155.92.31
cd speedtest-backend
chmod +x deploy.sh
./deploy.sh

# Paris Server (52.47.190.220)
ssh ubuntu@52.47.190.220
cd speedtest-backend
chmod +x deploy.sh
./deploy.sh
```

## 🔧 Manual Setup (Alternative)

If you prefer manual setup:

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Python 3.11
sudo apt install -y python3.11 python3.11-venv python3.11-pip

# Create virtual environment
python3.11 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the application
uvicorn main:app --host 0.0.0.0 --port 8000
```

## 🌐 EC2 Security Group Configuration

Configure your EC2 security groups to allow inbound traffic:

1. **Go to EC2 Console → Security Groups**
2. **Select your server's security group**
3. **Add inbound rule:**
   - Type: Custom TCP
   - Port: 8000
   - Source: 0.0.0.0/0 (or restrict to your VPN IP ranges)

## 📱 API Endpoints

### 1. Ping Test
```http
GET /ping
```
**Response:**
```json
{
  "message": "pong",
  "timestamp": 1703123456.789
}
```

### 2. Download Speed Test
```http
GET /download?size=52428800
```
- `size`: File size in bytes (default: 100MB)
- Streams random data for speed measurement
- Use to test download speeds through VPN

### 3. Upload Speed Test
```http
POST /upload?expected_size=5242880
Content-Type: multipart/form-data

file: [binary file]
```
- `expected_size`: Expected file size in bytes (default: 10MB)
- Returns upload speed metrics
- Use to test upload speeds through VPN

## 🧪 Testing the API

### Using curl

**Ping Test:**
```bash
curl http://56.155.92.31:8000/ping
curl http://52.47.190.220:8000/ping
```

**Download Test (50MB):**
```bash
curl -o test_download.bin "http://56.155.92.31:8000/download?size=52428800"
```

**Upload Test (5MB):**
```bash
# Create a test file
dd if=/dev/urandom of=test_upload.bin bs=1M count=5

# Upload it
curl -X POST \
  -F "file=@test_upload.bin" \
  "http://56.155.92.31:8000/upload?expected_size=5242880"
```

### Using Postman

1. **Import the collection** (create endpoints manually)
2. **Test ping:** GET `http://56.155.92.31:8000/ping`
3. **Test download:** GET `http://56.155.92.31:8000/download?size=10485760`
4. **Test upload:** POST `http://56.155.92.31:8000/upload` with file in form-data

## 📱 Android Integration

### Download Speed Testing
```kotlin
class SpeedTestService {
    suspend fun testDownloadSpeed(serverUrl: String, sizeBytes: Long): Double {
        val startTime = System.currentTimeMillis()
        
        val response = client.get("$serverUrl/download?size=$sizeBytes")
        val inputStream = response.body?.byteStream()
        
        var totalBytes = 0L
        val buffer = ByteArray(8192)
        
        while (inputStream?.read(buffer) != -1) {
            totalBytes += buffer.size
        }
        
        val endTime = System.currentTimeMillis()
        val durationSeconds = (endTime - startTime) / 1000.0
        
        return totalBytes / durationSeconds // bytes per second
    }
}
```

### Upload Speed Testing
```kotlin
suspend fun testUploadSpeed(serverUrl: String, file: File, expectedSize: Long): UploadResult {
    val startTime = System.currentTimeMillis()
    
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("file", file.name, file.asRequestBody())
        .build()
    
    val response = client.post("$serverUrl/upload?expected_size=$expectedSize") {
        setBody(requestBody)
    }
    
    val endTime = System.currentTimeMillis()
    val durationSeconds = (endTime - startTime) / 1000.0
    
    return UploadResult(
        bytesPerSecond = file.length() / durationSeconds,
        durationSeconds = durationSeconds
    )
}
```

### Ping Testing
```kotlin
suspend fun testPing(serverUrl: String): Long {
    val startTime = System.currentTimeMillis()
    
    val response = client.get("$serverUrl/ping")
    
    val endTime = System.currentTimeMillis()
    return endTime - startTime // milliseconds
}
```

## 🔒 HTTPS Setup (Optional)

### 1. Install Nginx
```bash
sudo apt install -y nginx
```

### 2. Configure Nginx
```bash
sudo cp nginx.conf /etc/nginx/sites-available/speedtest
sudo ln -s /etc/nginx/sites-available/speedtest /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 3. Setup SSL with Let's Encrypt
```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get SSL certificate (replace with your domain)
sudo certbot --nginx -d yourdomain.com

# Auto-renewal
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

## 📊 Monitoring & Logs

### Service Status
```bash
sudo systemctl status speedtest.service
```

### View Logs
```bash
# Real-time logs
sudo journalctl -u speedtest.service -f

# Recent logs
sudo journalctl -u speedtest.service --since "1 hour ago"
```

### Restart Service
```bash
sudo systemctl restart speedtest.service
```

## 🚨 Troubleshooting

### Common Issues

1. **Port 8000 not accessible:**
   - Check EC2 security group
   - Verify firewall settings: `sudo ufw status`

2. **Service not starting:**
   - Check logs: `sudo journalctl -u speedtest.service -n 50`
   - Verify Python environment: `source venv/bin/activate && python --version`

3. **Permission denied:**
   - Fix ownership: `sudo chown -R ubuntu:ubuntu /home/ubuntu/speedtest-backend`

4. **Memory issues with large files:**
   - The backend streams files in chunks (64KB) to avoid memory problems
   - Monitor server resources during tests

### Performance Tuning

- **Increase chunk size** in `main.py` for better throughput
- **Adjust Nginx buffer sizes** in `nginx.conf`
- **Monitor server CPU/memory** during speed tests

## 📈 Performance Metrics

The backend provides:
- **Download speed** in bytes per second
- **Upload speed** in bytes per second  
- **Ping latency** in milliseconds
- **File transfer times** for accurate measurements

## 🔄 Updates & Maintenance

### Update the Backend
```bash
cd /home/ubuntu/speedtest-backend
git pull  # if using git
# or upload new files manually

# Restart service
sudo systemctl restart speedtest.service
```

### Backup Configuration
```bash
sudo cp /etc/systemd/system/speedtest.service /home/ubuntu/speedtest-backend/backup/
sudo cp /etc/nginx/sites-available/speedtest /home/ubuntu/speedtest-backend/backup/
```

## 📞 Support

For issues or questions:
1. Check service logs: `sudo journalctl -u speedtest.service`
2. Verify network connectivity: `curl localhost:8000/ping`
3. Test endpoints individually with curl/Postman

---

**Note:** This backend is designed to work efficiently with VPN connections and provides accurate speed measurements for your Android VPN app.
