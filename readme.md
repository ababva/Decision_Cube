# Fitness Decision Cube

Полнофункциональное Android‑приложение для фитнес‑тренировок с backend API на Ktor и базой данных PostgreSQL.

## Возможности

- **Кубик тренировок**: случайный выбор упражнения с анимацией броска кубика
- **Аутентификация**: регистрация и вход пользователей через backend API
- **Статистика**: отслеживание активности за последние 7 дней с визуализацией
- **Друзья**: поиск пользователей, отправка заявок в друзья
- **Рейтинг**: таблица лидеров с глобальной статистикой
- **Жест встряхивания**: бросок кубика при встряхивании устройства
- **Офлайн режим**: локальное хранение данных через Room Database

## Архитектура проекта

### Android приложение
- **Технологии**: Kotlin, Jetpack Compose, Room Database, Retrofit
- **Архитектура**: MVVM (Model-View-ViewModel)
- **Локальное хранилище**: Room Database для кеширования данных
- **Сетевое взаимодействие**: Retrofit для REST API

### Backend API
- **Технологии**: Ktor, Kotlin, Exposed ORM, PostgreSQL
- **База данных**: PostgreSQL через Docker Compose
- **API**: RESTful endpoints для всех операций

## Структура проекта

```
Decision_Cube/
├── app/                          # Android приложение
│   ├── src/main/java/com/decisioncube/app/
│   │   ├── data/
│   │   │   ├── api/              # Retrofit API клиент
│   │   │   ├── database/         # Room entities и DAO
│   │   │   ├── model/            # Модели данных
│   │   │   ├── repository/       # Репозитории
│   │   │   └── exercises/        # Данные упражнений
│   │   ├── ui/
│   │   │   └── theme/            # Тема приложения
│   │   ├── util/                 # Утилиты (ShakeDetector)
│   │   ├── viewmodel/            # ViewModels
│   │   └── MainActivity.kt       # Главная активность
│   └── build.gradle.kts
├── backend/                       # Ktor backend
│   ├── src/main/kotlin/com/decisioncube/backend/
│   │   ├── database/             # Exposed таблицы и подключение
│   │   ├── routes/               # API маршруты
│   │   └── Application.kt        # Точка входа
│   └── build.gradle.kts
├── docker/
│   ├── docker-compose.yml        # Postgres + pgAdmin
│   └── env.example               # Пример переменных окружения
├── build.gradle.kts              # Корневой Gradle файл
├── settings.gradle.kts
└── readme.md
```

## Развертывание и запуск

### Предварительные требования

1. **Для Android приложения**:
   - Android Studio Hedgehog или новее
   - JDK 17
   - Android SDK (API 24+)
   - Эмулятор Android или физическое устройство

2. **Для backend**:
   - JDK 17 или новее
   - Docker и Docker Compose

3. **Для базы данных**:
   - Docker и Docker Compose

### Шаг 1: Настройка базы данных PostgreSQL

1. Перейдите в директорию `docker`:
   ```bash
   cd docker
   ```

2. Скопируйте файл с переменными окружения:
   ```bash
   cp env.example env
   ```

3. При необходимости отредактируйте `env` файл (пароли, порты)

4. Запустите PostgreSQL и pgAdmin:
   ```bash
   docker compose -f docker-compose.yml up -d
   ```

5. Проверьте, что контейнеры запущены:
   ```bash
   docker compose -f docker-compose.yml ps
   ```

6. Доступ к сервисам:
   - **PostgreSQL**: `localhost:5432` (порт по умолчанию)
   - **pgAdmin**: `http://localhost:5050` (порт по умолчанию)

7. Настройка pgAdmin:
   - Откройте `http://localhost:5050` в браузере
   - Войдите с учетными данными из файла `env`
   - Добавьте новый сервер:
     - Host: `postgres`
     - Port: `5432`
     - Database: `fitness_db` (или из env)
     - Username/Password: из файла `env`

### Шаг 2: Запуск Backend API

1. Перейдите в директорию `backend`:
   ```bash
   cd backend
   ```

2. Убедитесь, что база данных запущена (см. Шаг 1)

3. Настройте переменные окружения (опционально):
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/fitness_db
   export DB_USER=fitness_user
   export DB_PASSWORD=fitness_password
   ```
   
   Или создайте файл `.env` в директории `backend`:
   ```
   DATABASE_URL=jdbc:postgresql://localhost:5432/fitness_db
   DB_USER=fitness_user
   DB_PASSWORD=fitness_password
   ```

4. Запустите backend:
   ```bash
   ./gradlew run
   ```
   
   Или через IDE (IntelliJ IDEA / Android Studio):
   - Откройте `backend/src/main/kotlin/com/decisioncube/backend/Application.kt`
   - Нажмите Run рядом с функцией `main()`

5. Проверьте, что сервер запущен:
   - Backend должен быть доступен на `http://localhost:8080`
   - Проверьте логи в консоли на наличие ошибок

### Шаг 3: Развертывание Android приложения в Android Studio

#### Первоначальная настройка

1. **Откройте проект**:
   - Запустите Android Studio
   - Выберите `File → Open`
   - Укажите путь к корневой директории проекта: `/Users/maksimanikeev/projects/Decision_Cube`
   - Нажмите `OK`

2. **Синхронизация Gradle**:
   - Android Studio автоматически начнет синхронизацию Gradle
   - Дождитесь завершения (индикатор внизу справа)
   - Если возникнут ошибки, нажмите `Sync Project with Gradle Files` (иконка слона)

3. **Настройка SDK**:
   - Убедитесь, что установлен Android SDK Platform 34
   - `File → Settings → Appearance & Behavior → System Settings → Android SDK`
   - Вкладка `SDK Platforms`: установите `Android 14.0 (API 34)`
   - Вкладка `SDK Tools`: установите `Android SDK Build-Tools`, `Android Emulator`, `Android SDK Platform-Tools`

#### Настройка эмулятора или устройства

**Вариант A: Эмулятор Android**

1. Создайте виртуальное устройство:
   - `Tools → Device Manager`
   - Нажмите `Create Device`
   - Выберите устройство (например, Pixel 5)
   - Выберите системный образ (рекомендуется API 34)
   - Завершите создание

2. Запустите эмулятор:
   - В `Device Manager` нажмите ▶️ рядом с устройством
   - Дождитесь загрузки

**Вариант B: Физическое устройство**

1. Включите режим разработчика:
   - `Настройки → О телефоне → Номер сборки` (нажмите 7 раз)

2. Включите отладку по USB:
   - `Настройки → Для разработчиков → Отладка по USB`

3. Подключите устройство через USB

4. Разрешите отладку на устройстве (появится диалог)

#### Настройка подключения к backend

**Важно**: Android эмулятор использует специальный IP для доступа к localhost хоста.

1. Откройте файл `app/src/main/java/com/decisioncube/app/data/api/ApiClient.kt`

2. Для эмулятора (по умолчанию):
   ```kotlin
   private const val BASE_URL = "http://10.0.2.2:8080/api/"
   ```
   - `10.0.2.2` — специальный IP эмулятора для доступа к localhost хоста

3. Для физического устройства:
   - Узнайте IP вашего компьютера в локальной сети:
     - macOS/Linux: `ifconfig | grep "inet "`
     - Windows: `ipconfig`
   - Замените в `ApiClient.kt`:
     ```kotlin
     private const val BASE_URL = "http://192.168.x.x:8080/api/"
     ```
   - Убедитесь, что устройство и компьютер в одной Wi‑Fi сети

#### Сборка и запуск

1. Выберите конфигурацию запуска:
   - Вверху панели инструментов выберите `app` в выпадающем списке
   - Выберите устройство/эмулятор

2. Запустите приложение:
   - Нажмите кнопку `Run` (▶️) или `Shift + F10`
   - Дождитесь сборки и установки на устройство

3. Проверка работы:
   - Приложение должно запуститься
   - Попробуйте зарегистрировать нового пользователя
   - Выполните бросок кубика
   - Проверьте статистику

#### Решение проблем

**Проблема**: Gradle синхронизация не завершается
- Решение: Проверьте интернет‑соединение, очистите кеш: `File → Invalidate Caches → Invalidate and Restart`

**Проблема**: Ошибки компиляции
- Решение: Убедитесь, что используется JDK 17: `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK`

**Проблема**: Приложение не подключается к backend
- Решение: 
  - Проверьте, что backend запущен (`http://localhost:8080`)
  - Проверьте IP адрес в `ApiClient.kt`
  - Для физического устройства проверьте, что компьютер и устройство в одной сети
  - Проверьте файрвол (должен разрешать подключения на порт 8080)

**Проблема**: Ошибки базы данных в backend
- Решение:
  - Убедитесь, что PostgreSQL запущен: `docker compose -f docker/docker-compose.yml ps`
  - Проверьте переменные окружения (DATABASE_URL, DB_USER, DB_PASSWORD)
  - Проверьте логи backend на наличие ошибок подключения

## API Endpoints

Backend предоставляет следующие endpoints:

- `POST /api/auth/register` — регистрация пользователя
- `POST /api/auth/login` — вход пользователя
- `GET /api/users/{id}` — получение пользователя по ID
- `GET /api/users/search?query={query}` — поиск пользователей
- `GET /api/users/leaderboard` — рейтинг пользователей
- `POST /api/exercises` — сохранение выполненного упражнения
- `GET /api/statistics/daily?days={days}` — статистика за последние N дней

## Разработка

### Добавление новых упражнений

Отредактируйте файл `app/src/main/java/com/decisioncube/app/data/exercises/ExerciseData.kt`

### Изменение портов и настроек

- **Backend порт**: измените в `backend/src/main/kotlin/com/decisioncube/backend/Application.kt` (по умолчанию 8080)
- **PostgreSQL порт**: измените в `docker/docker-compose.yml`
- **API базовый URL**: измените в `app/src/main/java/com/decisioncube/app/data/api/ApiClient.kt`

## Сборка релизной версии

### Android APK

1. В Android Studio: `Build → Generate Signed Bundle / APK`
2. Выберите `APK`
3. Создайте или выберите ключ подписи
4. Выберите `release` build variant
5. Нажмите `Finish`

### Backend JAR

```bash
cd backend
./gradlew build
```

JAR файл будет в `backend/build/libs/`

## Тестирование

### Android приложение

Запустите unit тесты:
```bash
./gradlew test
```

Запустите instrumented тесты:
```bash
./gradlew connectedAndroidTest
```

### Backend API

```bash
cd backend
./gradlew test
```

## Лицензия

Проект создан в образовательных целях.
