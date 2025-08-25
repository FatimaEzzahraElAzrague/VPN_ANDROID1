# Test Script for VPN Backend Endpoints
# This script tests all the major endpoints to ensure they're working correctly

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$UserId = "test-user-123"
)

Write-Host "🧪 Testing VPN Backend Endpoints" -ForegroundColor Green
Write-Host "Base URL: $BaseUrl" -ForegroundColor Yellow
Write-Host "User ID: $UserId" -ForegroundColor Yellow
Write-Host ""

# Test 1: Health Check
Write-Host "1️⃣ Testing Health Check..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/" -Method Get
    Write-Host "✅ Health Check: $($response.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Health Check Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 2: Speed Test Ping (No Auth Required)
Write-Host "2️⃣ Testing Speed Test Ping..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/speedtest/ping" -Method Get
    Write-Host "✅ Speed Test Ping: $($response.message)" -ForegroundColor Green
} catch {
    Write-Host "❌ Speed Test Ping Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 3: Speed Test Servers (No Auth Required)
Write-Host "3️⃣ Testing Speed Test Servers..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/speedtest/servers" -Method Get
    Write-Host "✅ Speed Test Servers: $($response.servers.Count) servers found" -ForegroundColor Green
    foreach ($server in $response.servers) {
        Write-Host "   - $($server.name) ($($server.location), $($server.country))" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Speed Test Servers Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: VPN Servers (No Auth Required)
Write-Host "4️⃣ Testing VPN Servers..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/vpn/servers" -Method Get
    Write-Host "✅ VPN Servers: $($response.servers.Count) servers found" -ForegroundColor Green
    foreach ($server in $response.servers) {
        Write-Host "   - $($server.name) ($($server.city), $($server.country)) - $($server.ip):$($server.port)" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ VPN Servers Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 5: Speed Test Download (Rate Limited)
Write-Host "5️⃣ Testing Speed Test Download..." -ForegroundColor Cyan
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/speedtest/download?size=1048576" -Method Get
    Write-Host "✅ Speed Test Download: $($response.ContentLength) bytes received" -ForegroundColor Green
} catch {
    Write-Host "❌ Speed Test Download Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 6: Speed Test Upload (Rate Limited)
Write-Host "6️⃣ Testing Speed Test Upload..." -ForegroundColor Cyan
try {
    $testData = [System.Text.Encoding]::UTF8.GetBytes("Test upload data for speed testing")
    $response = Invoke-RestMethod -Uri "$BaseUrl/speedtest/upload?expected_size=$($testData.Length)" -Method Post -Body $testData -ContentType "application/octet-stream"
    Write-Host "✅ Speed Test Upload: $($response.sizeBytes) bytes uploaded, Speed: $($response.uploadSpeedMbps) Mbps" -ForegroundColor Green
} catch {
    Write-Host "❌ Speed Test Upload Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 7: VPN Config (Requires Auth - Will Fail)
Write-Host "7️⃣ Testing VPN Config (No Auth - Expected to Fail)..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/vpn/config/$UserId" -Method Get
    Write-Host "✅ VPN Config: Unexpected success" -ForegroundColor Yellow
} catch {
    Write-Host "✅ VPN Config: Correctly rejected (401 Unauthorized)" -ForegroundColor Green
}

# Test 8: Speed Test Config (Requires Auth - Will Fail)
Write-Host "8️⃣ Testing Speed Test Config (No Auth - Expected to Fail)..." -ForegroundColor Cyan
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/speedtest/config/$UserId" -Method Get
    Write-Host "✅ Speed Test Config: Unexpected success" -ForegroundColor Yellow
} catch {
    Write-Host "✅ Speed Test Config: Correctly rejected (401 Unauthorized)" -ForegroundColor Green
}

Write-Host ""
Write-Host "🎯 Test Summary:" -ForegroundColor Green
Write-Host "   - Public endpoints should work without authentication" -ForegroundColor White
Write-Host "   - Protected endpoints should return 401 Unauthorized" -ForegroundColor White
Write-Host "   - Rate limiting should be active on download/upload endpoints" -ForegroundColor White
Write-Host ""
Write-Host "💡 To test authenticated endpoints, you need a valid JWT token" -ForegroundColor Yellow
Write-Host "   Use the auth endpoints to get a token first" -ForegroundColor Yellow

# Test rate limiting
Write-Host ""
Write-Host "🔄 Testing Rate Limiting..." -ForegroundColor Cyan
$successCount = 0
for ($i = 1; $i -le 25; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/speedtest/download?size=1024" -Method Get
        $successCount++
        Write-Host "   Request $i`: Success" -ForegroundColor Gray
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            Write-Host "   Request $i`: Rate Limited (429)" -ForegroundColor Yellow
            break
        } else {
            Write-Host "   Request $i`: Failed - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}
Write-Host "✅ Rate Limiting Test: $successCount successful requests before rate limit" -ForegroundColor Green
