# Скрипт для настройки окружения сборки на Windows 11
# Запустите в PowerShell от имени администратора: .\setup-build-env.ps1

Write-Host "=== Настройка окружения для сборки Android APK ===" -ForegroundColor Green
Write-Host ""

# Проверка Java
Write-Host "Проверка Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "✓ Java найден: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Java не найден!" -ForegroundColor Red
    Write-Host "Установите Java JDK 11+ с https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "После установки добавьте Java в PATH и перезапустите скрипт" -ForegroundColor Yellow
    exit 1
}

# Проверка Android SDK
Write-Host ""
Write-Host "Проверка Android SDK..." -ForegroundColor Yellow

$sdkPath = $env:ANDROID_HOME
if (-not $sdkPath) {
    $sdkPath = "C:\Android\sdk"
    Write-Host "ANDROID_HOME не установлен, используем путь по умолчанию: $sdkPath" -ForegroundColor Yellow
}

if (Test-Path $sdkPath) {
    Write-Host "✓ Android SDK найден: $sdkPath" -ForegroundColor Green
} else {
    Write-Host "✗ Android SDK не найден!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Инструкция по установке:" -ForegroundColor Yellow
    Write-Host "1. Скачайте Android SDK Command Line Tools:" -ForegroundColor White
    Write-Host "   https://developer.android.com/studio#command-tools" -ForegroundColor Cyan
    Write-Host "2. Распакуйте в: $sdkPath\cmdline-tools\latest\" -ForegroundColor White
    Write-Host "3. Установите компоненты:" -ForegroundColor White
    Write-Host "   cd $sdkPath\cmdline-tools\latest\bin" -ForegroundColor Cyan
    Write-Host "   .\sdkmanager --licenses" -ForegroundColor Cyan
    Write-Host "   .\sdkmanager `"platform-tools`" `"platforms;android-34`" `"build-tools;34.0.0`"" -ForegroundColor Cyan
    exit 1
}

# Создание local.properties
Write-Host ""
Write-Host "Создание local.properties..." -ForegroundColor Yellow

$localPropsPath = "local.properties"
$sdkDir = $sdkPath -replace '\\', '\\'

if (Test-Path $localPropsPath) {
    Write-Host "✓ local.properties уже существует" -ForegroundColor Green
    $content = Get-Content $localPropsPath
    if ($content -match "sdk\.dir") {
        Write-Host "  Файл содержит настройки SDK" -ForegroundColor Green
    } else {
        Add-Content $localPropsPath "sdk.dir=$sdkDir"
        Write-Host "  Добавлен путь к SDK" -ForegroundColor Green
    }
} else {
    Set-Content $localPropsPath "sdk.dir=$sdkDir"
    Write-Host "✓ Создан файл local.properties" -ForegroundColor Green
}

# Проверка Gradle Wrapper
Write-Host ""
Write-Host "Проверка Gradle Wrapper..." -ForegroundColor Yellow

if (Test-Path "gradlew.bat") {
    Write-Host "✓ gradlew.bat найден" -ForegroundColor Green
} else {
    Write-Host "✗ gradlew.bat не найден!" -ForegroundColor Red
    Write-Host "Убедитесь, что вы находитесь в корне проекта" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== Готово к сборке! ===" -ForegroundColor Green
Write-Host ""
Write-Host "Для сборки APK выполните:" -ForegroundColor Yellow
Write-Host "  .\gradlew.bat assembleDebug" -ForegroundColor Cyan
Write-Host ""
Write-Host "APK будет в: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Yellow

