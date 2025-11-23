## О приложении

Avito Books — приложение для чтения электронных книг. Приложение позволяет:

- Читать книги в форматах TXT, EPUB и PDF
- Хранить книги в облаке (Яндекс.Облако S3)
- Скачивать книги для офлайн-чтения
- Искать и фильтровать книги в библиотеке
- Управлять профилем пользователя
- Безопасная аутентификация через Firebase (Email/Password и Google Sign-In)
- Настраиваемые темы чтения (светлая/темная, размер шрифта, цвет фона)


### Структура проекта

Проект разделен на модули:

- `app/` - главный модуль приложения
- `core/` - базовые модули:
  - `common/` - общие утилиты и расширения
  - `database/` - Room база данных
  - `di/` - Dependency Injection (Dagger)
  - `firebase/` - Firebase интеграция
  - `navigation/` - навигация и TopAppBar
  - `ui/` - общие UI компоненты и тема
- `feature/` - функциональные модули:
  - `auth/` - аутентификация
  - `booksList/` - список книг
  - `bookReader/` - чтение книг
  - `bookUpload/` - загрузка книг
  - `profile/` - профиль пользователя

### Архитектурные слои

Каждый feature-модуль следует принципам Clean Architecture:

- `data/` - слой данных (DataSource, RepositoryImpl)
- `domain/` - бизнес-логика (модели, интерфейсы репозиториев, Use Cases)
- `presentation/` - UI слой (Screen, ViewModel, State, composable компоненты)
- `di/` - Dependency Injection модули

### Поток данных

UI (Compose) → ViewModel (обрабатывает Intent) → UseCase (бизнес-логика) → Repository (абстракция над источниками данных) → DataSource (Remote S3 / Local Room)

### Dependency Injection

Используется Dagger 2 для управления зависимостями:

- AppComponent — корневой компонент в app модуле
- Feature Components — компоненты для каждого feature-модуля
- Scopes — управление жизненным циклом зависимостей

## Стек технологий

Приложение разработано на Kotlin с использованием Jetpack Compose для UI. 
Архитектура построена на принципах MVI и Clean Architecture. Для управления зависимостями используется Dagger 2. 
Локальное хранение данных реализовано через Room, асинхронные операции выполняются с помощью Kotlin Coroutines. 
Навигация между экранами организована через Navigation Compose.

### Библиотеки

**UI & Compose:**
- Material 3 — современный Material Design
- Lottie — анимации
- Coil — загрузка изображений
- Accompanist SwipeRefresh — pull-to-refresh

**Backend & Storage:**
- Firebase Auth — аутентификация (Email/Password, Google Sign-In)
- AWS S3 SDK — интеграция с Яндекс.Облаком S3
- Room — локальная база данных

**Парсинг файлов:**
- PDFBox Android — парсинг PDF файлов
- Custom EPUB Parser — парсинг EPUB (ZIP-based)
- Kotlinx Serialization — сериализация данных


### Клонирование и сборка

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd AvitoTest
```

2. Откройте проект в Android Studio:
   - File → Open → выберите папку `AvitoTest`

3. Дождитесь синхронизации Gradle (Android Studio автоматически загрузит зависимости)

4. Соберите проект:
```bash
./gradlew build
```

### Настройка Firebase

1. Создайте проект в Firebase Console:
   - Перейдите на [Firebase Console](https://console.firebase.google.com/)
   - Создайте новый проект или используйте существующий

2. Добавьте Android приложение:
   - Package name: `com.avito.avitotest`
   - Скачайте `google-services.json`

3. Поместите `google-services.json` в `app/` директорию

4. Включите Authentication в Firebase Console:
   - Authentication → Sign-in method
   - Включите "Email/Password"
   - Включите "Google" (укажите SHA-1 сертификата)

### Настройка Яндекс.Облака S3

Для работы с облачным хранилищем книг необходимо настроить Яндекс.Облако S3. Если не хотите настраивать, можно просто установить APK файл ниже.

#### Шаг 1: Создание бакета в Яндекс.Облаке

1. Войдите в Яндекс.Облако:
   - Перейдите на [console.cloud.yandex.ru](https://console.cloud.yandex.ru/)

2. Создайте бакет:
   - Перейдите в раздел Object Storage
   - Нажмите "Создать бакет"
   - Укажите имя бакета (например, `avito-books`)
   - Выберите регион: `ru-central1`
   - Настройте публичный доступ (если нужно)

#### Шаг 2: Создание статического ключа доступа

1. Создайте сервисный аккаунт:
   - Перейдите в раздел IAM → Сервисные аккаунты
   - Нажмите "Создать сервисный аккаунт"
   - Укажите имя и назначьте роль: `editor` или `storage.editor`

2. Создайте статический ключ доступа:
   - Откройте созданный сервисный аккаунт
   - Перейдите на вкладку "Ключи"
   - Нажмите "Создать новый ключ" → "Статический ключ доступа"
   - Сохраните Access Key ID и Secret Access Key

#### Шаг 3: Настройка проекта

1. Создайте файл `gradle.properties` в корне проекта (если его нет):
```properties
# S3 Configuration для Яндекс.Облака
S3_ENDPOINT=https://storage.yandexcloud.net
S3_REGION=ru-central1
S3_BUCKET=your-bucket-name
S3_ACCESS_KEY=YOUR_S3_ACCESS_KEY_HERE
S3_SECRET_KEY=YOUR_S3_SECRET_KEY_HERE
S3_PUBLIC_BASE_URL=https://storage.yandexcloud.net/your-bucket-name
```

2. Замените значения:
   - `your-bucket-name` — имя вашего бакета
   - `YOUR_S3_ACCESS_KEY_HERE` — Access Key ID из шага 2
   - `YOUR_S3_SECRET_KEY_HERE` — Secret Access Key из шага 2

#### Шаг 4: Проверка настройки

После настройки пересоберите проект:
```bash
./gradlew clean build
```

Приложение автоматически подхватит настройки из `gradle.properties` и будет использовать их для подключения к Яндекс.Облаку.

### Запуск приложения

1. Подключите устройство или запустите эмулятор

2. Запустите приложение:
   - Нажмите `Run` в Android Studio, или
   - Выполните: `./gradlew installDebug`

## Скриншоты

### Экран входа
![Экран входа](screenshots/auth_screen.png)

### Список книг
![Список книг](screenshots/books_list.png)

### Чтение книги
![Чтение книги](screenshots/book_reader.png)

### Профиль
![Профиль](screenshots/profile.png)

## Видео демонстрация

[![Демонстрация приложения](https://img.youtube.com/vi/VIDEO_ID/0.jpg)](https://www.youtube.com/watch?v=VIDEO_ID)

## Скачать APK

Готовый APK файл для установки на устройство:

[Скачать APK (Release)](https://github.com/your-repo/releases/download/v1.0/app-release.apk)

### Установка APK

1. Разрешите установку из неизвестных источников:
   - Настройки → Безопасность → Неизвестные источники (включить)

2. Скачайте APK по ссылке выше

3. Откройте файл и следуйте инструкциям установки
