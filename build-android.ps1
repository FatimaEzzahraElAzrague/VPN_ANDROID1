# Android VPN App Build Script
# This script builds the Android project and checks for compilation issues

Write-Host "🔨 Building Android VPN App..." -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Check if we're in the right directory
if (-not (Test-Path "app/build.gradle.kts")) {
    Write-Host "❌ Error: Not in the correct directory. Please run this from VPN_ANDROID1 folder." -ForegroundColor Red
    exit 1
}

# Clean previous builds
Write-Host "🧹 Cleaning previous builds..." -ForegroundColor Yellow
try {
    ./gradlew clean
    Write-Host "✅ Clean completed" -ForegroundColor Green
} catch {
    Write-Host "⚠️ Clean failed, continuing..." -ForegroundColor Yellow
}

# Build debug version
Write-Host "🔨 Building debug version..." -ForegroundColor Yellow
try {
    ./gradlew assembleDebug
    Write-Host "✅ Build completed successfully!" -ForegroundColor Green
    Write-Host "📱 APK location: app/build/outputs/apk/debug/app-debug.apk" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Build failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above and fix any compilation issues." -ForegroundColor Red
    exit 1
}

# Check if APK was created
if (Test-Path "app/build/outputs/apk/debug/app-debug.apk") {
    $apkSize = (Get-Item "app/build/outputs/apk/debug/app-debug.apk").Length / 1MB
    Write-Host "📦 APK size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "⚠️ APK not found in expected location" -ForegroundColor Yellow
}

Write-Host "🎉 Build process completed!" -ForegroundColor Green
Write-Host "You can now install the APK on your Android device." -ForegroundColor Cyan
