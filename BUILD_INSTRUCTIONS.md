# Инструкция по сборке APK

> **Нет Android Studio?** Смотрите подробную инструкцию в файле `BUILD_WITHOUT_STUDIO.md`

## Быстрый старт

### Вариант 1: Через Android Studio (рекомендуется)

1. **Установите Android Studio** (если еще не установлен):
   - Скачайте с https://developer.android.com/studio
   - Установите Android SDK через SDK Manager

2. **Откройте проект**:
   - Запустите Android Studio
   - File → Open → выберите папку `d:\app`
   - Дождитесь завершения синхронизации Gradle

3. **Соберите APK**:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Или нажмите `Ctrl+Shift+A` и введите "Build APK"

4. **Найдите APK**:
   - После сборки появится уведомление "APK(s) generated successfully"
   - Нажмите "locate" или перейдите в: `app\build\outputs\apk\debug\app-debug.apk`

### Вариант 2: Через командную строку

**Требования:**
- Установлен JDK 8 или выше
- Установлен Android SDK
- Переменная окружения ANDROID_HOME указывает на Android SDK

**Команды:**

```bash
# Перейдите в папку проекта
cd d:\app

# Соберите APK (Windows)
gradlew.bat assembleDebug

# Или если Gradle установлен глобально
gradle assembleDebug
```

APK будет в: `app\build\outputs\apk\debug\app-debug.apk`

## Установка на телефон

1. **Включите установку из неизвестных источников:**
   - Настройки → Безопасность → Неизвестные источники (включить)
   - Или: Настройки → Приложения → Специальный доступ → Установка неизвестных приложений

2. **Скопируйте APK на телефон:**
   - Через USB кабель
   - Через облачное хранилище (Google Drive, Dropbox и т.д.)
   - Через Bluetooth

3. **Установите:**
   - Откройте файл APK на телефоне
   - Нажмите "Установить"
   - После установки можно запустить игру

## Релизная версия (для публикации)

Для создания подписанного релизного APK:

1. Создайте keystore (один раз):
   ```
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```

2. Настройте `app/build.gradle` для подписи (добавьте в `android` блок):
   ```gradle
   signingConfigs {
       release {
           storeFile file('my-release-key.jks')
           storePassword 'ваш_пароль'
           keyAlias 'my-key-alias'
           keyPassword 'ваш_пароль'
       }
   }
   buildTypes {
       release {
           signingConfig signingConfigs.release
       }
   }
   ```

3. Соберите релизный APK:
   ```
   gradlew.bat assembleRelease
   ```

## Решение проблем

**Ошибка "SDK location not found":**
- Создайте файл `local.properties` в корне проекта
- Добавьте строку: `sdk.dir=C\:\\Users\\ВашеИмя\\AppData\\Local\\Android\\Sdk`

**Ошибка "Gradle sync failed":**
- Проверьте подключение к интернету (Gradle загружает зависимости)
- Убедитесь, что версия Gradle совместима с Android Studio

**APK не устанавливается на телефон:**
- Убедитесь, что включена установка из неизвестных источников
- Проверьте, что APK не поврежден (попробуйте пересобрать)

