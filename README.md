# LoginApp
Офлайн Android-приложение для регистрации и авторизации на Kotlin, Jetpack Compose, Room.

## Сборка
Откройте проект в Android Studio и запустите `./gradlew assembleDebug`. Требуется Android SDK 36.

Пароль сохраняется как PBKDF2-HMAC-SHA256 хеш с индивидуальной случайной солью. Состояние входа хранится локально в SharedPreferences.
