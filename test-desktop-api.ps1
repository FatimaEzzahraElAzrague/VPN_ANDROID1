# Test Desktop VPN API Endpoint
# This tests the same endpoint that the mobile app now uses

Write-Host "Testing Desktop VPN API Endpoint..." -ForegroundColor Cyan
Write-Host ""

# Test the exact same endpoint and request that mobile app uses
$apiUrl = "https://vpn.richdalelab.com/vpn/connect"
$headers = @{
    "Authorization" = "Bearer vpn-agent-secret-token-2024"
    "Content-Type" = "application/json"
}
$body = @{
    location = "osaka"
    ad_block_enabled = $false
    anti_malware_enabled = $false
    family_safe_mode_enabled = $false
} | ConvertTo-Json

Write-Host "Testing API endpoint: $apiUrl" -ForegroundColor Yellow
Write-Host "Authorization: Bearer vpn-agent-secret-token-2024" -ForegroundColor Yellow
Write-Host "Request body: $body" -ForegroundColor Yellow
Write-Host ""

try {
    Write-Host "Making request..." -ForegroundColor Green
    
    $response = Invoke-RestMethod -Uri $apiUrl -Method POST -Headers $headers -Body $body -ContentType "application/json"
    
    Write-Host "SUCCESS! API is working!" -ForegroundColor Green
    Write-Host "Response received:" -ForegroundColor Green
    Write-Host ""
    
    # Display key response fields
    Write-Host "Private Key: $($response.private_key)" -ForegroundColor White
    Write-Host "Public Key: $($response.public_key)" -ForegroundColor White
    Write-Host "Server Endpoint: $($response.server_endpoint)" -ForegroundColor White
    Write-Host "Internal IP: $($response.internal_ip)" -ForegroundColor White
    Write-Host "DNS: $($response.dns)" -ForegroundColor White
    Write-Host "MTU: $($response.mtu)" -ForegroundColor White
    Write-Host "Preshared Key: $($response.preshared_key)" -ForegroundColor White
    Write-Host "Internal IPv6: $($response.internal_ipv6)" -ForegroundColor White
    Write-Host ""
    
    if ($response.client_config) {
        Write-Host "Client Config Available: YES" -ForegroundColor Green
        Write-Host "Config Length: $($response.client_config.Length) characters" -ForegroundColor Green
    } else {
        Write-Host "Client Config Available: NO" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "This means mobile VPN should now work!" -ForegroundColor Green
    
} catch {
    Write-Host "FAILED! API is not working:" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode
        Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
        
        if ($statusCode -eq 401) {
            Write-Host "Unauthorized - Check the authorization token" -ForegroundColor Red
        } elseif ($statusCode -eq 404) {
            Write-Host "Not Found - The endpoint doesn't exist" -ForegroundColor Red
        } elseif ($statusCode -eq 500) {
            Write-Host "Server Error - Backend issue" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    Write-Host "Mobile VPN will NOT work until this API is fixed!" -ForegroundColor Red
}

Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. If SUCCESS: Mobile app should now connect to VPN" -ForegroundColor White
Write-Host "2. If FAILED: Contact the desktop guy to fix the API" -ForegroundColor White
Write-Host "3. Test mobile app connection after fixing" -ForegroundColor White
