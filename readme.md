#Кубик решений

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
   - JDK 17 или новее
   - Android SDK (API 24+)
   - Эмулятор Android или физическое устройство

2. **Для backend**:
   - JDK 21 или новее (рекомендуется Java 21 или Java 24)
   - Docker и Docker Compose
   - Gradle (обычно устанавливается автоматически)

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

3. При необходимости отредактируйте `env` файл (пароли, порты). По умолчанию PostgreSQL запускается на порту 5433, чтобы избежать конфликтов с локальной установкой PostgreSQL.

4. Запустите PostgreSQL и pgAdmin:
   ```bash
   docker-compose up -d
   ```

5. Проверьте, что контейнеры запущены:
   ```bash
   docker-compose ps
   ```
   Оба контейнера должны быть в статусе "Up" или "healthy".

6. Доступ к сервисам:
   - **PostgreSQL**: `localhost:5433` (порт по умолчанию, если на вашем компьютере установлен PostgreSQL, он обычно занимает 5432)
   - **pgAdmin**: `http://localhost:5050` (порт по умолчанию)

7. Настройка pgAdmin (опционально):
   - Откройте `http://localhost:5050` в браузере
   - Войдите с учетными данными из файла `env` (по умолчанию admin@example.com / admin123)
   - Добавьте новый сервер:
     - Host: `dc_postgres` (имя контейнера)
     - Port: `5432` (порт внутри контейнера, не порт на хосте)
     - Database: `decision_cube`
     - Username: `dc_user`
     - Password: `dc_password`

### Шаг 2: Запуск Backend API

1. Перейдите в директорию `backend`:
   ```bash
   cd backend
   ```

2. Убедитесь, что база данных запущена (см. Шаг 1)

3. Запустите backend:
   ```bash
   ./gradlew run
   ```
   
   При первом запуске Gradle может скачивать зависимости - это нормально. Дождитесь завершения.

   Если у вас установлена Java 24, путь к ней указан в `gradle.properties`. Если используете другую версию Java, может потребоваться обновить путь в этом файле или в системных переменных окружения.

   Или запустите через IDE (IntelliJ IDEA / Android Studio):
   - Откройте `backend/src/main/kotlin/com/decisioncube/backend/Application.kt`
   - Нажмите Run рядом с функцией `main()`

4. Проверьте, что сервер запущен:
   - Когда в консоли появится сообщение "Responding at http://0.0.0.0:8080", значит backend запущен
   - Backend доступен на `http://localhost:8080`
   - Для проверки выполните: `curl http://localhost:8080/api/users/search`

   По умолчанию backend подключается к базе данных на порту 5433 (настройка в `DatabaseFactory.kt`). Если вы изменили порт PostgreSQL в docker-compose, обновите его и там.

   Если backend не запускается, проверьте:
   - База данных запущена: `cd docker && docker-compose ps`
   - Правильный порт базы данных в `DatabaseFactory.kt` (должен быть 5433)
   - Версия Java совместима (нужна Java 21 или выше)

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

**Проблема**: Ошибки компиляции в Android Studio
- Решение: Убедитесь, что используется JDK 17 или выше: `File → Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK`

**Проблема**: Backend не компилируется или не запускается
- Решение: Убедитесь, что используется Java 21 или выше. Проверьте путь к Java в `backend/gradle.properties` (файл `org.gradle.java.home`). Если используете Java 24, путь должен указывать на установку Java 24.

**Проблема**: Приложение не подключается к backend
- Решение: 
  - Проверьте, что backend запущен (`http://localhost:8080`)
  - Проверьте IP адрес в `ApiClient.kt`
  - Для физического устройства проверьте, что компьютер и устройство в одной сети
  - Проверьте файрвол (должен разрешать подключения на порт 8080)

**Проблема**: Ошибки базы данных в backend
- Решение:
  - Убедитесь, что PostgreSQL запущен: `cd docker && docker-compose ps`
  - Проверьте, что порт базы данных в `DatabaseFactory.kt` соответствует порту в docker-compose (по умолчанию 5433)
  - Убедитесь, что база данных полностью инициализировалась (подождите 10-15 секунд после запуска docker-compose)
  - Проверьте логи backend на наличие ошибок подключения
  - Проверьте, что на порту 5433 не запущена другая база данных: `lsof -i :5433`

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

- **Backend порт**: измените в `backend/src/main/kotlin/com/decisioncube/backend/Application.kt` (по умолчанию 8080). Также обновите `ApiClient.kt` в Android приложении, если меняете порт.

- **PostgreSQL порт на хосте**: измените `POSTGRES_PORT` в файле `docker/.env` (по умолчанию 5433). После изменения не забудьте обновить порт в `backend/src/main/kotlin/com/decisioncube/backend/database/DatabaseFactory.kt`.

- **API базовый URL в Android**: измените `BASE_URL` в `app/src/main/java/com/decisioncube/app/data/api/ApiClient.kt`. Для эмулятора используйте `10.0.2.2:8080`, для физического устройства - IP адрес вашего компьютера в локальной сети.

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
