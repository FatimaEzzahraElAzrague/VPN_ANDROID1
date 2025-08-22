@echo off
echo 🚀 Starting VPN Backend Server...
echo.

echo 📁 Current directory: %CD%
echo.

echo 🔧 Checking if Gradle wrapper exists...
if not exist "gradlew.bat" (
    echo ❌ Gradle wrapper not found!
    echo Please run this from the backend directory
    pause
    exit /b 1
)

echo ✅ Gradle wrapper found
echo.

echo 🚀 Starting backend server...
echo.
echo 📡 Server will be available at:
echo    - Local: http://localhost:8080
echo    - Network: http://0.0.0.0:8080
echo.
echo 🔍 Debug endpoints:
echo    - Users: http://localhost:8080/debug/users
echo    - Database: http://localhost:8080/debug/db-test
echo.
echo ⏳ Starting server... (Press Ctrl+C to stop)
echo.

gradlew.bat run

echo.
echo 🛑 Backend server stopped
pause
