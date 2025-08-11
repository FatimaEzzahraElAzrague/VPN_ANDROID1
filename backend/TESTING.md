# 🧪 Testing VPN Features Backend

This guide explains how to test if your new VPN features backend is working correctly.

## 🚀 **Prerequisites**

1. **Start your Ktor backend server**:
   ```bash
   cd backend
   # If using Gradle wrapper
   ./gradlew run
   
   # If using system Gradle
   gradle run
   
   # Or run from your IDE by running Application.kt
   ```

2. **Verify server is running**:
   - Check console output for "🚀 Backend started successfully!"
   - Server should be accessible at `http://localhost:8080`

## 🧪 **Testing Methods**

### **Method 1: Manual cURL Testing**

#### **Speed Test Endpoints**
```bash
# Get available speed test servers
curl -X GET "http://localhost:8080/speedtest/servers"

# Get user's speed test configuration
curl -X GET "http://localhost:8080/speedtest/config/test123"

# Update speed test settings
curl -X PUT "http://localhost:8080/speedtest/config/test123" \
  -H "Content-Type: application/json" \
  -d '{
    "preferredServer": "https://speed.cloudflare.com/__down",
    "uploadSizeBytes": 10000000,
    "autoTestEnabled": true
  }'

# Save speed test result
curl -X POST "http://localhost:8080/speedtest/result/test123" \
  -H "Content-Type: application/json" \
  -d '{
    "pingMs": 25,
    "downloadMbps": 150.5,
    "uploadMbps": 45.2,
    "testServer": "https://speed.cloudflare.com/__down",
    "networkType": "WiFi"
  }'

# Get recent results
curl -X GET "http://localhost:8080/speedtest/results/test123?limit=5"

# Get analytics
curl -X GET "http://localhost:8080/speedtest/analytics/test123?period=DAY"

# Get admin statistics
curl -X GET "http://localhost:8080/speedtest/statistics"
```

#### **Split Tunneling Endpoints**
```bash
# Get split tunneling configuration
curl -X GET "http://localhost:8080/splittunneling/config/test123"

# Update split tunneling settings
curl -X PUT "http://localhost:8080/splittunneling/config/test123" \
  -H "Content-Type: application/json" \
  -d '{
    "isEnabled": true,
    "mode": "EXCLUDE",
    "appPackages": ["com.netflix.mediaclient", "com.spotify.music"]
  }'

# Get available presets
curl -X GET "http://localhost:8080/splittunneling/presets"
```

#### **Kill Switch Endpoints**
```bash
# Get kill switch configuration
curl -X GET "http://localhost:8080/killswitch/config/test123"

# Update kill switch settings
curl -X PUT "http://localhost:8080/killswitch/config/test123" \
  -H "Content-Type: application/json" \
  -d '{
    "isEnabled": true,
    "autoReconnectEnabled": true,
    "maxReconnectionAttempts": 5
  }'

# Get admin statistics
curl -X GET "http://localhost:8080/killswitch/statistics"
```

### **Method 2: Kotlin Test Script**

1. **Run the test script**:
   ```bash
   cd backend
   kotlin test_api.kt
   ```

2. **Expected output**:
   ```
   🧪 Testing VPN Features Backend API
   =====================================
   
   🚀 Testing Speed Test Endpoints:
   ---------------------------------
   ✅ GET http://localhost:8080/speedtest/servers
   Status: 200
   Response: {"success":true,"servers":[...]}
   
   ✅ GET http://localhost:8080/speedtest/config/test123
   Status: 200
   Response: {"config":{...},"availableServers":[...],"analytics":{...}}
   ```

### **Method 3: Web Browser Testing**

1. **Open the HTML test page**:
   ```bash
   # Open in your browser
   open backend/test_api.html
   # Or double-click the file
   ```

2. **Click "Test" buttons** for each endpoint
3. **View responses** below each endpoint

## ✅ **Expected Results**

### **Speed Test Endpoints**
- **GET /speedtest/servers**: Should return list of available test servers
- **GET /speedtest/config/{userId}**: Should return user's configuration with default values
- **PUT /speedtest/config/{userId}**: Should update and return new configuration
- **POST /speedtest/result/{userId}**: Should save result and return success
- **GET /speedtest/results/{userId}**: Should return saved results
- **GET /speedtest/analytics/{userId}**: Should return analytics data
- **GET /speedtest/statistics**: Should return admin statistics

### **Split Tunneling Endpoints**
- **GET /splittunneling/config/{userId}**: Should return default configuration
- **PUT /splittunneling/config/{userId}**: Should update and return new configuration
- **GET /splittunneling/presets**: Should return available presets

### **Kill Switch Endpoints**
- **GET /killswitch/config/{userId}**: Should return user's kill switch settings
- **PUT /killswitch/config/{userId}**: Should update and return new settings
- **GET /killswitch/statistics**: Should return admin statistics

## 🚨 **Troubleshooting**

### **Common Issues**

1. **Server not starting**:
   - Check if port 8080 is available
   - Verify all dependencies are installed
   - Check console for error messages

2. **404 errors**:
   - Ensure routes are properly registered in `Application.kt`
   - Check if `vpnFeaturesRoutes()` is called in routing block

3. **500 errors**:
   - Check server logs for detailed error messages
   - Verify all required models and services are properly imported

4. **CORS issues** (when testing from browser):
   - Add CORS plugin to your Ktor application if needed
   - Or use cURL/Postman for testing

### **Debug Steps**

1. **Check server logs** for detailed error messages
2. **Verify imports** in `Application.kt`:
   ```kotlin
   import com.myapp.backend.routes.vpnFeaturesRoutes
   ```
3. **Confirm routes are registered**:
   ```kotlin
   routing {
       // ... other routes
       vpnFeaturesRoutes()
   }
   ```
4. **Test basic connectivity**:
   ```bash
   curl -X GET "http://localhost:8080/"
   # Should return: {"status":"ok"}
   ```

## 🎯 **Success Criteria**

Your backend is working correctly if:

✅ **All GET endpoints return 200 status with proper JSON responses**  
✅ **All PUT/POST endpoints accept requests and return 200 status**  
✅ **Data is properly stored and retrieved**  
✅ **Error handling works for invalid requests**  
✅ **Server logs show successful request processing**  

## 🔄 **Next Steps**

Once testing is successful:

1. **Integrate with Android app** - Update your frontend to call these endpoints
2. **Add authentication** - Protect endpoints with JWT tokens
3. **Database integration** - Replace in-memory storage with persistent database
4. **Production deployment** - Deploy to your production environment

## 📚 **Additional Resources**

- [Ktor Documentation](https://ktor.io/docs/)
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [HTTP Status Codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
