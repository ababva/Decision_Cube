# Быстрый старт

## 1. Запуск базы данных

```bash
cd docker
cp env.example env
docker compose -f docker-compose.yml up -d
```

## 2. Запуск backend

```bash
cd backend
./gradlew run
```

Backend будет доступен на `http://localhost:8080`

## 3. Запуск Android приложения

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Нажмите Run ▶️

**Важно**: Для эмулятора IP backend: `10.0.2.2:8080` (уже настроено в ApiClient.kt)

## Проверка работы

1. Зарегистрируйте нового пользователя в приложении
2. Выполните бросок кубика
3. Проверьте статистику
4. Проверьте рейтинг

## Решение проблем

### Backend не запускается
- Убедитесь, что PostgreSQL запущен: `docker compose -f docker/docker-compose.yml ps`
- Проверьте переменные окружения (DATABASE_URL, DB_USER, DB_PASSWORD)

### Android не подключается к backend
- Для эмулятора: используйте `10.0.2.2:8080`
- Для физического устройства: замените IP в `ApiClient.kt` на IP вашего компьютера в локальной сети

