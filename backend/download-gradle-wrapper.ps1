# Download Gradle Wrapper JAR
$gradleWrapperUrl = "https://github.com/gradle/gradle/raw/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
$gradleWrapperPath = "gradle\wrapper\gradle-wrapper.jar"

# Create directory if it doesn't exist
if (!(Test-Path "gradle\wrapper")) {
    New-Item -ItemType Directory -Path "gradle\wrapper" -Force
}

# Download the file
Write-Host "Downloading Gradle Wrapper JAR..."
Invoke-WebRequest -Uri $gradleWrapperUrl -OutFile $gradleWrapperPath

Write-Host "Gradle Wrapper downloaded successfully!"
Write-Host "You can now run: .\gradlew.bat run"
