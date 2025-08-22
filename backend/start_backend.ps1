# 🚀 VPN Backend Server Starter Script
# Run this script to start your VPN backend server

Write-Host "🚀 Starting VPN Backend Server..." -ForegroundColor Green
Write-Host ""

Write-Host "📁 Current directory: $PWD" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔧 Checking if Gradle wrapper exists..." -ForegroundColor Yellow
if (-not (Test-Path "gradlew.bat")) {
    Write-Host "❌ Gradle wrapper not found!" -ForegroundColor Red
    Write-Host "Please run this from the backend directory" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "✅ Gradle wrapper found" -ForegroundColor Green
Write-Host ""

Write-Host "🚀 Starting backend server..." -ForegroundColor Green
Write-Host ""
Write-Host "📡 Server will be available at:" -ForegroundColor Cyan
Write-Host "   - Local: http://localhost:8080" -ForegroundColor White
Write-Host "   - Network: http://0.0.0.0:8080" -ForegroundColor White
Write-Host ""
Write-Host "🔍 Debug endpoints:" -ForegroundColor Cyan
Write-Host "   - Users: http://localhost:8080/debug/users" -ForegroundColor White
Write-Host "   - Database: http://localhost:8080/debug/db-test" -ForegroundColor White
Write-Host ""
Write-Host "⏳ Starting server... (Press Ctrl+C to stop)" -ForegroundColor Yellow
Write-Host ""

try {
    # Start the backend server
    & .\gradlew.bat run
} catch {
    Write-Host ""
    Write-Host "❌ Error starting server: $($_.Exception.Message)" -ForegroundColor Red
} finally {
    Write-Host ""
    Write-Host "🛑 Backend server stopped" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
}
