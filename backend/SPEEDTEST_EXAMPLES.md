# Speed Test Backend - Examples and Testing

This document provides examples for testing the speed test backend endpoints.

## Base URL
```
https://your-backend-domain.com
```

## Authentication
All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## 1. Get Speed Test Configuration

### Endpoint
```
GET /speedtest/config/{userId}
```

### cURL Example
```bash
curl -X GET "https://your-backend-domain.com/speedtest/config/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

## 2. Ping Test (Public Endpoint)

### Endpoint
```
GET /speedtest/ping
```

### cURL Example
```bash
curl -X GET "https://your-backend-domain.com/speedtest/ping" \
  -H "Content-Type: application/json"
```

### Response Example
```json
{
  "success": true,
  "message": "pong",
  "timestamp": 1704067200000
}
```

### Response Example
```json
{
  "config": {
    "userId": "123",
    "preferredServer": null,
    "autoTestEnabled": false,
    "testIntervalMinutes": 60,
    "saveResults": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  },
  "availableServers": [
    {
      "id": "osaka",
      "name": "Osaka VPN Server",
      "host": "osaka.myvpn.com",
      "port": 443,
      "location": "Osaka",
      "country": "Japan",
      "ip": "15.168.240.118",
      "isActive": true,
      "priority": 1
    },
    {
      "id": "paris",
      "name": "Paris VPN Server",
      "host": "paris.myvpn.com",
      "port": 443,
      "location": "Paris",
      "country": "France",
      "ip": "52.47.190.220",
      "isActive": true,
      "priority": 2
    }
  ]
}
```

## 3. Process Speed Test Result

### Endpoint
```
POST /speedtest/result/{userId}
```

### Request Body
```json
{
  "serverId": "osaka",
  "pingMs": 45,
  "downloadMbps": 85.2,
  "uploadMbps": 23.7
}
```

### cURL Example
```bash
curl -X POST "https://your-backend-domain.com/speedtest/result/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "serverId": "osaka",
    "pingMs": 45,
    "downloadMbps": 85.2,
    "uploadMbps": 23.7
  }'
```

### Response Example
```json
{
  "success": true,
  "message": "Speed test completed successfully"
}
```

## 4. Get Available Servers

### Endpoint
```
GET /speedtest/servers
```

### cURL Example
```bash
curl -X GET "https://your-backend-domain.com/speedtest/servers" \
  -H "Content-Type: application/json"
```

### Response Example
```json
{
  "success": true,
        "servers": [
        {
          "id": "osaka",
          "name": "Osaka VPN Server",
          "host": "osaka.myvpn.com",
          "port": 443,
          "location": "Osaka",
          "country": "Japan",
          "ip": "15.168.240.118",
          "isActive": true,
          "priority": 1
        },
        {
          "id": "paris",
          "name": "Paris VPN Server",
          "host": "paris.myvpn.com",
          "port": 443,
          "location": "Paris",
          "country": "France",
          "ip": "52.47.190.220",
          "isActive": true,
          "priority": 2
        }
      ]
}
```

## 5. Download Speed Test

### Endpoint
```
GET /speedtest/download?size=50000000
```

### Parameters
- `size`: File size in bytes (default: 50MB, max: 100MB)

### cURL Example
```bash
curl -X GET "https://your-backend-domain.com/speedtest/download?size=50000000" \
  -H "Content-Type: application/octet-stream" \
  --output speedtest.bin
```

### Response
- Binary data stream
- Content-Type: application/octet-stream
- Rate limited to 10 requests per minute

## 6. Upload Speed Test

### Endpoint
```
POST /speedtest/upload
```

### cURL Example
```bash
# Create a test file
dd if=/dev/urandom of=testfile.bin bs=1M count=10

# Upload the file
curl -X POST "https://your-backend-domain.com/speedtest/upload" \
  -H "Content-Type: application/octet-stream" \
  --data-binary @testfile.bin
```

### Response Example
```json
{
  "success": true,
  "message": "Upload processed successfully",
  "sizeBytes": 10485760,
  "uploadTimeMs": 1250,
  "uploadSpeedMbps": 67.1
}
```

## 7. Get Statistics (Admin)

### Endpoint
```
GET /speedtest/statistics
```

### cURL Example
```bash
curl -X GET "https://your-backend-domain.com/speedtest/statistics" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

### Response Example
```json
{
  "success": true,
  "statistics": {
    "totalServers": 2,
    "lastCleanup": "2024-01-01T00:00:00",
    "rateLimitEnabled": true,
    "maxRequestsPerMinute": 10
  }
}
```

## Error Responses

### Unauthorized (401)
```json
{
  "error": "Missing authorization token"
}
```

### Forbidden (403)
```json
{
  "error": "Access denied"
}
```

### Rate Limited (429)
```json
{
  "error": "Rate limit exceeded. Try again later."
}
```

### Bad Request (400)
```json
{
  "error": "User ID is required"
}
```

### Internal Server Error (500)
```json
{
  "error": "Failed to process speed test result"
}
```

## Testing with Different File Sizes

### Small Test (1MB)
```bash
curl -X GET "https://your-backend-domain.com/speedtest/download?size=1048576" \
  --output small_test.bin
```

### Medium Test (25MB)
```bash
curl -X GET "https://your-backend-domain.com/speedtest/download?size=26214400" \
  --output medium_test.bin
```

### Large Test (100MB)
```bash
curl -X GET "https://your-backend-domain.com/speedtest/download?size=104857600" \
  --output large_test.bin
```

## Performance Testing

### Test Download Speed
```bash
time curl -X GET "https://your-backend-domain.com/speedtest/download?size=50000000" \
  --output /dev/null
```

### Test Upload Speed
```bash
# Create a 10MB test file
dd if=/dev/urandom of=upload_test.bin bs=1M count=10

# Measure upload time
time curl -X POST "https://your-backend-domain.com/speedtest/upload" \
  --data-binary @upload_test.bin
```

## Notes

1. **No Data Storage**: Speed test results are processed in real-time but not stored in the database
2. **Rate Limiting**: Download and upload endpoints are limited to 10 requests per minute
3. **Authentication**: Most endpoints require valid JWT tokens
4. **File Size Limits**: Download is capped at 100MB, upload at 100MB
5. **Server Selection**: Use the `/servers` endpoint to get available test servers
