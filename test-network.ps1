# Network Test Script for Backend
Write-Host "🔍 Testing Backend Network Connectivity..." -ForegroundColor Green

# Get all IP addresses
$ips = @(
    "192.168.100.190",
    "192.168.56.1", 
    "172.22.96.1",
    "172.19.16.1"
)

Write-Host "`n📡 Testing IP addresses:" -ForegroundColor Yellow
foreach ($ip in $ips) {
    try {
        $response = Invoke-WebRequest -Uri "http://$ip`:8080/" -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $ip`:8080 - WORKING" -ForegroundColor Green
            Write-Host "   Use this IP in your Android app: $ip" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "❌ $ip`:8080 - NOT REACHABLE" -ForegroundColor Red
    }
}

Write-Host "`n🔧 To fix network issues:" -ForegroundColor Yellow
Write-Host "1. Open Windows Defender Firewall" -ForegroundColor White
Write-Host "2. Click 'Allow an app through firewall'" -ForegroundColor White
Write-Host "3. Click 'Change settings'" -ForegroundColor White
Write-Host "4. Click 'Allow another app'" -ForegroundColor White
Write-Host "5. Browse to your Java installation" -ForegroundColor White
Write-Host "6. Add it and check both Private and Public" -ForegroundColor White

Write-Host "`n📱 For Android app:" -ForegroundColor Yellow
Write-Host "Update ApiClient.kt with the working IP address" -ForegroundColor White
