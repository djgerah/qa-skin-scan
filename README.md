# Skin Scan — QA Automation Framework

## 1. О проекте

**Краткое описание:**  
Автотесты для серверной части приложения скрининга кожи (SkinScan API). Фреймворк работает по принципу **black-box**: тестируемое приложение запускается отдельно, фреймворк взаимодействует с ним только через REST API (REST Assured) и напрямую через БД (JDBC) — для проверки данных, записанных сервером.

**Покрытие на текущий момент:**
- ✅ Проверка состояния сервера и связности с БД (`/skinScan/check-run`).
- ✅ Регистрация пользователей: успешная, повторная (409), невалидный email и короткий пароль (400).
- ✅ Аутентификация: успешный вход, неверный пароль, несуществующий пользователь (401).
- ✅ Rate limiting: превышение лимита регистраций и логинов с одного IP (429).
- ✅ Обновление данных пользователя: с валидной авторизацией, без авторизации, с неверным паролем.
- ✅ Смена пароля: успешная смена, вход со старым/новым паролем, неверный старый пароль, короткий новый пароль.
- ✅ Загрузка фотографий: успешная загрузка (202), загрузка без авторизации (401).
- ✅ Получение фотографий: по id и по имени после завершения асинхронного анализа (200), несуществующие id/имя (404), доступ чужого пользователя (403), список без авторизации (401).
- ✅ Отчёты Allure с шагами (`@Step`) и человекочитаемыми названиями тестов (`@DisplayName`).

---

## 2. Стек технологий

**Описание выбранных решений с обоснованием:**

- **Язык:** Java 21 (toolchain задан в `build.gradle.kts`).
- **Тестовый фреймворк:** JUnit 5 (Jupiter). Стандарт де-факто для Java-тестов; поддерживает теги для фильтрации запусков.
- **HTTP-клиент:** REST Assured. Удобный DSL для проверки REST API: статус-коды, JSON-пути, multipart-загрузка файлов.
- **Отчёты:** Allure 2 (allure-junit5 + aspectjweaver). Формирует отчёт с шагами, вложениями и группировкой по фичам.
- **Ожидание асинхронных операций:** Awaitility. Асинхронный анализ фото на сервере завершается через несколько секунд — Awaitility позволяет опрашивать API до готовности (до 60 секунд, шаг опроса 1 секунда).
- **Работа с БД:** JDBC (PostgreSQL driver). Прямые запросы к БД для проверки данных, не видимых через API.
- **JSON:** Jackson Databind.
- **Сборка:** Gradle (Kotlin DSL).

---

## 3. Требования к окружению

Перед запуском убедитесь, что у вас установлены:

- **JDK 21** или новее.
- **Gradle 8+** (или используйте wrapper `./gradlew`).
- **Allure CLI** — для генерации и просмотра отчётов (`allure generate`, `allure serve`).
- **Запущенное приложение SkinScan** на `http://localhost:8080` (см. README backend-проекта).
- **PostgreSQL** — та же БД, которую использует тестируемое приложение (параметры подключения — в `src/test/resources/connectionBD.properties`).

---

## 4. Настройка и запуск

### 4.1 Клонирование репозитория
```bash
git clone <url-репозитория>
```

### 4.2 Настройка подключения к БД
Параметры подключения задаются в файле `src/test/resources/connectionBD.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/skinScan_db
spring.datasource.username=skinScan_user
spring.datasource.password=skinscan_dev_pass
```
Их можно переопределить системными свойствами, не меняя файл:
```bash
./gradlew test -Ddb.url=jdbc:postgresql://localhost:5432/skinScan_db -Ddb.user=skinScan_user -Ddb.password=your_password
```

### 4.3 Запуск всех тестов
```bash
./gradlew test
```

### 4.4 Запуск по тегам
Тесты размечены тегами `smoke` и `regression`. Фильтрация через системное свойство `tags` (через запятую):
```bash
./gradlew test -Dtags=smoke
./gradlew test -Dtags=regression
./gradlew test -Dtags=smoke,regression
```

### 4.5 Отчёты Allure
```bash
# сгенерировать отчёт в build/allure-report
./gradlew allureReport

# сгенерировать и сразу открыть в браузере
./gradlew allureServe
```
Результаты тестов пишутся в `build/allure-results/` (настроено в `src/test/resources/allure.properties`).

---

## 5. Структура проекта

```
src/test/java/qa/startup/skinscan/
├── api/            # Тестовые классы (по одному на группу эндпоинтов)
│   ├── HealthCheckTest.java    # /skinScan/check-run
│   ├── AuthApiTest.java        # /skinScan/register, /skinScan/login
│   ├── RateLimitApiTest.java   # лимиты запросов с одного IP
│   ├── UserApiTest.java        # /skinScan/user/update*
│   └── PhotoApiTest.java       # /skinScan/photos*
├── clients/        # HTTP-клиенты (обёртки над REST Assured)
│   ├── AuthClient.java         # register, login, getRegisteredUser, X-Forwarded-For
│   ├── UserClient.java         # update, updatePassword
│   └── PhotoClient.java        # upload, getPhotoById, getPhotoByName, getAllPhotos
├── models/         # Модели данных (records)
│   ├── User.java               # учётные данные + basicAuth() / authParam()
│   ├── UserRequest.java        # тело запроса регистрации/обновления
│   └── PasswordUpdateRequest.java  # тело запроса смены пароля
├── testdata/
│   └── Picture.java            # тестовые изображения (PNG 1x1 в Base64), уникальные имена
└── config/
    └── Db.java                 # JDBC-доступ к БД приложения
```

**Ключевые принципы:**
- **Клиенты (`clients/`)** занимаются только HTTP-вызовами и помечены `@Step` — каждый вызов виден в отчёте Allure как шаг.
- **Тестовые данные (`testdata/`, `models/`) отделены от клиентов** — генерация случайных пользователей, телефонов, имён файлов и изображений вынесена отдельно.
- **`AuthClient.getRegisteredUser()`** — хелпер: создаёт пользователя со случайными данными и регистрирует его на сервере. Гарантирует, что тесты не конфликтуют между собой (суффикс из 8 символов UUID в логине/email).
- **`AuthClient.randomForwardedFor()`** — случайный IP в заголовке `X-Forwarded-For`, чтобы rate limit сервера не блокировал повторные запуски тестов.

---

## 6. Покрытие API (текущие тесты)

### 6.1 Health Check
- `GET /skinScan/check-run` — 200, статус `Успешно`, БД `PostgreSQL`, результат работы БД `1`.

### 6.2 Регистрация и вход
- `POST /skinScan/register` — 201 для нового пользователя.
- `POST /skinScan/register` — 409 при повторной регистрации того же логина.
- `POST /skinScan/register` — 400 при невалидном email / коротком пароле (< 6 символов).
- `GET /skinScan/login` — 200 с валидными учётными данными.
- `GET /skinScan/login` — 401 при неверном пароле или несуществующем пользователе.

### 6.3 Rate limiting
- `POST /skinScan/register` — 429 после 5 запросов с одного IP.
- `GET /skinScan/login` — 429 после 10 запросов с одного IP.

### 6.4 Обновление пользователя
- `PUT /skinScan/user/update` — 200 с валидной авторизацией.
- `PUT /skinScan/user/update` — 401 без заголовка Authorization / с неверным паролем.
- `PUT /skinScan/user/update/{login}` — смена пароля: вход с новым паролем 200, со старым — 401.
- `PUT /skinScan/user/update/{login}` — 401 с неверным старым паролем; 400 при новом пароле < 6 символов.

### 6.5 Фотографии
- `POST /skinScan/photos/upload` — 202 при успешной загрузке; 401 без авторизации.
- `GET /skinScan/photos/{id}` — 200 с метаданными (`size_file`, `processing_time`) после завершения анализа; 404 для несуществующего id; 403 для чужого фото.
- `GET /skinScan/photos/name/{nameFile}` — 200 после анализа; 404 для несуществующего имени.
- `GET /skinScan/photos` — 401 без авторизации.
- **Асинхронность:** после загрузки фото тесты через Awaitility ждут завершения анализа (до 60 секунд), т.к. сервер возвращает 502, пока фото в обработке.
