# Быстрая сборка APK на Windows 11 без Android Studio

> **💡 Автоматическая проверка:** Запустите `setup-build-env.ps1` для проверки готовности окружения!

## Самый простой способ (5 минут)

### 1. Установите Java JDK
- Скачайте: https://adoptium.net/ (выберите JDK 11 или выше для Windows)
- Установите, добавьте в PATH

### 2. Установите Android SDK Command Line Tools
- Скачайте: https://developer.android.com/studio#command-tools
- Распакуйте в `C:\Android\sdk\cmdline-tools\latest\`
- Установите компоненты:
  ```powershell
  cd C:\Android\sdk\cmdline-tools\latest\bin
  .\sdkmanager --licenses
  .\sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
  ```

### 3. Создайте файл local.properties
В папке `d:\app` создайте файл `local.properties`:
```properties
sdk.dir=C\:\\Android\\sdk
```

### 4. Соберите APK
```powershell
cd d:\app
.\gradlew.bat assembleDebug
```

### 5. Готово!
APK будет в: `d:\app\app\build\outputs\apk\debug\app-debug.apk`

---

## Еще проще: через GitHub Actions (без установки)

1. Зарегистрируйтесь на GitHub.com
2. Создайте новый репозиторий
3. Загрузите туда весь проект
4. Создайте файл `.github/workflows/build.yml` (см. `BUILD_WITHOUT_STUDIO.md`)
5. Запустите workflow в Actions
6. Скачайте готовый APK

---

Подробности в `BUILD_WITHOUT_STUDIO.md`

