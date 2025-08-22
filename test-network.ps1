# Test network connectivity to VPN servers
$servers = @(
    "vpn.richdalelab.com",     # Production VPN server
    "https://vpn.richdalelab.com"  # Production VPN server with HTTPS
)

Write-Host "`n🔍 Testing VPN Server Connectivity..." -ForegroundColor Green

Write-Host "`n📡 Testing VPN server endpoints:" -ForegroundColor Yellow
foreach ($server in $servers) {
    try {
        $response = Invoke-WebRequest -Uri "http://$server" -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $server - WORKING" -ForegroundColor Green
            Write-Host "   VPN server is responding" -ForegroundColor Cyan
        }
    } catch {
        Write-Host "❌ $server - NOT REACHABLE" -ForegroundColor Red
        Write-Host "   Error: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}

Write-Host "`n🌐 Testing DNS Resolution:" -ForegroundColor Yellow
try {
    $dnsResult = Resolve-DnsName -Name "vpn.richdalelab.com" -ErrorAction Stop
    Write-Host "✅ DNS Resolution: $($dnsResult.IPAddress)" -ForegroundColor Green
} catch {
    Write-Host "❌ DNS Resolution Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n📱 For Android app:" -ForegroundColor Yellow
Write-Host "If VPN server is not responding, check:" -ForegroundColor White
Write-Host "1. VPN server is running and accessible" -ForegroundColor White
Write-Host "2. Firewall allows connections on port 8080" -ForegroundColor White
Write-Host "3. DNS is properly configured" -ForegroundColor White
