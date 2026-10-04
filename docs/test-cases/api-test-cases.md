# Тест-кейсы API SkinScan

Обозначения:
- `{{baseUrl}}` — `http://localhost:8080`
- `<base64>(login:password)` — значение для query-параметра `?auth=` и заголовка `Authorization: Basic`; `login` = `user_<uuid>`, `password` = `password_<uuid>`
- **Пользователь** — зарегистрированный пользователь с известными `login` = `user_<uuid>` и `password` = `password_<uuid>`
- **Фото** — фото, загруженное под Пользователем; известны `photoId` и `nameFile`

---

# AuthApiTest

## 1. Регистрация нового пользователя

**Описание:**
Регистрация нового пользователя с валидными данными `POST /skinScan/register` возвращает 201.

**Предусловия:**
Сервер доступен. `login` и `email` уникальны (сгенерировать UUID), чтобы не совпасть с существующими.

**Шаги:**

1. Зарегистрировать нового пользователя, отправив валидные данные:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
201 Created. Тело содержит id (UUID) созданного пользователя.

---

## 2. Повторная регистрация того же логина

**Описание:**
Повторная регистрация уже существующего логина `POST /skinScan/register` возвращает 409.

**Предусловия:**
Пользователь зарегистрирован (известны `login` и `password`).

**Шаги:**

1. Повторно зарегистрировать того же пользователя, отправив те же данные, что и при регистрации Пользователя:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
409 Conflict. Тело содержит сообщение, что пользователь уже существует.

---

## 3. Регистрация с невалидным email

**Описание:**
Регистрация с email неверного формата `POST /skinScan/register` возвращает 400.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться зарегистрироваться, указав email неверного формата:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>",
  "email": "not-an-email",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
400 Bad Request. Пользователь не создан.

---

## 4. Регистрация с коротким паролем

**Описание:**
Регистрация с паролем короче 6 символов `POST /skinScan/register` возвращает 400.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться зарегистрироваться, указав пароль короче 6 символов:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "123",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
400 Bad Request. Пользователь не создан.

---

## 5. Вход с валидными данными

**Описание:**
Вход существующего пользователя с верным паролем `GET /skinScan/login` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован (известны `login` и `password`).

**Шаги:**

1. Войти, передав в query-параметре `auth` закодированные логин и пароль:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
200 OK. Тело — UUID пользователя.

---

## 6. Вход с неверным паролем

**Описание:**
Вход существующего пользователя с неверным паролем `GET /skinScan/login` возвращает 401.

**Предусловия:**
Пользователь зарегистрирован (известны `login` и `password`).

**Шаги:**

1. Попытаться войти, передав верный логин и неверный пароль:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(user_<uuid>:wrong-password)
```

**Ожидаемый результат:**
401 Unauthorized. Тело — сообщение о неверном пароле.

---

## 7. Вход несуществующего пользователя

**Описание:**
Вход с логином, которого нет в системе, `GET /skinScan/login` возвращает 401.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться войти, передав случайные логин и пароль, которых нет в системе:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(<случайный>:<случайный>)
```

**Ожидаемый результат:**
401 Unauthorized. Тело — сообщение, что пользователь не найден.

---

## 8. Регистрация с пустым логином

**Описание:**
Регистрация с пустой строкой в поле login `POST /skinScan/register` возвращает 400.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться зарегистрироваться, указав пустой логин:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "",
  "password": "password_<uuid>",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
400 Bad Request. Пользователь не создан.

---

## 9. Регистрация с пустым паролем

**Описание:**
Регистрация с пустой строкой в поле password `POST /skinScan/register` возвращает 400.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться зарегистрироваться, указав пустой пароль:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
400 Bad Request. Пользователь не создан.

---

## 10. Регистрация только с логином и паролем

**Описание:**
Регистрация без необязательных полей email и phone `POST /skinScan/register` возвращает 201.

**Предусловия:**
Нет.

**Шаги:**

1. Зарегистрироваться, указав только обязательные поля login и password:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>"
}
```

**Ожидаемый результат:**
201 Created. Тело содержит id (UUID) созданного пользователя.

---

## 11. Вход без параметра auth

**Описание:**
Вход без обязательного query-параметра auth `GET /skinScan/login` возвращает 400.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться войти, не передав query-параметр auth:

```
GET {{baseUrl}}/skinScan/login
```

**Ожидаемый результат:**
400 Bad Request.

---

## 12. Вход с валидными данными возвращает id пользователя в виде UUID

**Описание:**
Вход с валидными данными `GET /skinScan/login` возвращает id пользователя в виде UUID.

**Предусловия:**
Пользователь зарегистрирован (известны `login` и `password`).

**Шаги:**

1. Войти с валидными данными:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(user_<uuid>:password_<uuid>)
```

2. Прочитать тело ответа и проверить, что оно является корректным UUID.

**Ожидаемый результат:**
200 OK. Тело — валидный UUID (успешно парсится как UUID).

---

# HealthCheckTest

## 1. Проверка доступности БД

**Описание:**
Проверка соединения приложения с базой данных `GET /skinScan/check-run` возвращает 200.

**Предусловия:**
Сервер запущен, база данных PostgreSQL доступна.

**Шаги:**

1. Запросить статус проверки соединения с БД:

```
GET {{baseUrl}}/skinScan/check-run
```

**Ожидаемый результат:**
200 OK. Тело содержит: `"статус": "Успешно"`, `"БД": "PostgreSQL"`, `"Результат работы БД": 1`.

---

# PhotoApiTest

## 1. Загрузка фото

**Описание:**
Загрузка изображения авторизованным пользователем `POST /skinScan/photos/upload` возвращает 202.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Загрузить изображение, приложив файл в multipart-части file:

```
POST {{baseUrl}}/skinScan/photos/upload
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: multipart/form-data

file = <png 1x1>
```

**Ожидаемый результат:**
202 Accepted. Тело — `photoId` (UUID) загруженного фото.

---

## 2. Загрузка без авторизации

**Описание:**
Загрузка изображения без заголовка Authorization `POST /skinScan/photos/upload` возвращает 401.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться загрузить изображение, не передавая заголовок Authorization:

```
POST {{baseUrl}}/skinScan/photos/upload
Content-Type: multipart/form-data

file = <png 1x1>
```

**Ожидаемый результат:**
401 Unauthorized. Фото не сохранено.

---

## 3. Получение фото по id после анализа

**Описание:**
Получение метаданных фото после завершения анализа `GET /skinScan/photos/{id}` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован; фото загружено (известен `photoId`); анализ завершён.

**Шаги:**

1. Дождаться завершения анализа фото: повторять запрос из шага 2, пока сервер не вернёт 200 вместо 502.

2. Запросить метаданные фото по его id:

```
GET {{baseUrl}}/skinScan/photos/{photoId}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
200 OK. Тело содержит: `id` = photoId, `size_file` > 0, `processing_time` заполнено, `status` — завершён.

---

## 4. Получение фото по id сразу после загрузки

**Описание:**
Получение фото, анализ которого ещё не завершён, `GET /skinScan/photos/{id}` возвращает 502.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Загрузить фото и запомнить полученный `photoId` (ответ 202).

2. Сразу же, не дожидаясь анализа, запросить фото по id:

```
GET {{baseUrl}}/skinScan/photos/{photoId}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
502 Bad Gateway. Тело — сообщение, что фото находится в обработке.

---

## 5. Получение фото по имени после анализа

**Описание:**
Получение метаданных фото по имени файла после анализа `GET /skinScan/photos/name/{nameFile}` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован; фото загружено (известно `nameFile`); анализ завершён.

**Шаги:**

1. Дождаться завершения анализа фото: повторять запрос из шага 2, пока сервер не вернёт 200 вместо 502.

2. Запросить метаданные фото по имени файла:

```
GET {{baseUrl}}/skinScan/photos/name/{nameFile}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
200 OK. Тело содержит: `size_file` > 0, `processing_time` заполнено, `status` — завершён.

---

## 6. Получение фото по имени сразу после загрузки

**Описание:**
Получение фото по имени, анализ которого ещё не завершён, `GET /skinScan/photos/name/{nameFile}` возвращает 502.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Загрузить фото и запомнить имя файла `nameFile` (ответ 202).

2. Сразу же, не дожидаясь анализа, запросить фото по имени:

```
GET {{baseUrl}}/skinScan/photos/name/{nameFile}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
502 Bad Gateway. Тело — сообщение, что фото находится в обработке.

---

## 7. Получение фото по несуществующему имени

**Описание:**
Поиск фото по имени, которого нет у пользователя, `GET /skinScan/photos/name/{nameFile}` возвращает 404.

**Предусловия:**
Пользователь зарегистрирован; фото с таким именем не загружалось.

**Шаги:**

1. Запросить фото по имени, которого у пользователя нет:

```
GET {{baseUrl}}/skinScan/photos/name/qa_photo_<uuid>.png
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
404 Not Found. Тело — сообщение, что фото с таким именем не найдено.

---

## 8. Получение фото по несуществующему id

**Описание:**
Поиск фото по id, которого нет в системе, `GET /skinScan/photos/{id}` возвращает 404.

**Предусловия:**
Пользователь зарегистрирован; фото с таким id не существует.

**Шаги:**

1. Запросить фото по случайному UUID, которого нет в системе:

```
GET {{baseUrl}}/skinScan/photos/<случайный-uuid>
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
404 Not Found. Тело — сообщение, что фото с таким id не найдено.

---

## 9. Посторонний пользователь не имеет доступа к фото

**Описание:**
Доступ к чужому фото другим авторизованным пользователем `GET /skinScan/photos/{id}` возвращает 403.

**Предусловия:**
Зарегистрированы Пользователь A и Пользователь B; A загрузил фото (известен `photoId`).

**Шаги:**

1. От имени пользователя B запросить фото, принадлежащее пользователю A:

```
GET {{baseUrl}}/skinScan/photos/{photoId}
Authorization: Basic <base64>(user_<uuid_B>:password_<uuid_B>)
```

**Ожидаемый результат:**
403 Forbidden. Тело — сообщение об отсутствии доступа к фото.

---

## 10. Список фото без авторизации

**Описание:**
Получение списка фото без заголовка Authorization `GET /skinScan/photos` возвращает 401.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться получить список фото, не передавая заголовок Authorization:

```
GET {{baseUrl}}/skinScan/photos
```

**Ожидаемый результат:**
401 Unauthorized.

---

## 11. Список фото у нового пользователя

**Описание:**
Список фото пользователя без загруженных фото `GET /skinScan/photos` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован; фото не загружал.

**Шаги:**

1. Запросить список фото нового пользователя, у которого нет загруженных фото:

```
GET {{baseUrl}}/skinScan/photos
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
200 OK. Тело — пустой массив `[]`.

---

## 12. Загрузка фото с уже существующим именем

**Описание:**
Повторная загрузка фото с именем, которое уже есть у пользователя, `POST /skinScan/photos/upload` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован; фото с именем `nameFile` уже загружено (ответ был 202).

**Шаги:**

1. Повторно загрузить фото с уже использованным именем `nameFile`:

```
POST {{baseUrl}}/skinScan/photos/upload
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: multipart/form-data

file = <png 1x1>
```

**Ожидаемый результат:**
200 OK (не 202). Тело — сообщение, что фото с таким именем уже существует. Новое фото не создаётся.

---

## 13. Загрузка файла неверного формата

**Описание:**
Загрузка файла, не являющегося изображением, `POST /skinScan/photos/upload` возвращает 400.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Попытаться загрузить текстовый файл вместо изображения:

```
POST {{baseUrl}}/skinScan/photos/upload
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: multipart/form-data

file = document_<uuid>.txt
```

**Ожидаемый результат:**
400 Bad Request. Тело — сообщение о неверном формате файла. Файл не загружен.

---

# RateLimitApiTest

## 1. Превышение лимита регистрации

**Описание:**
Ограничение частоты запросов регистрации с одного IP `POST /skinScan/register` возвращает 429.

**Предусловия:**
Используется уникальный IP в заголовке `X-Forwarded-For`, ранее не применявшийся (чтобы лимит был не исчерпан).

**Шаги:**

1. Выполнить 5 запросов регистрации подряд с одного IP (в пределах лимита):

```
POST {{baseUrl}}/skinScan/register
X-Forwarded-For: 10.<случайный>.<случайный>.<случайный>
```

2. Выполнить 6-й запрос регистрации с того же IP (сверх лимита):

```
POST {{baseUrl}}/skinScan/register
X-Forwarded-For: 10.<случайный>.<случайный>.<случайный>
```

**Ожидаемый результат:**
Первые 5 запросов — не 429. 6-й — 429 Too Many Requests, заголовок `Retry-After` присутствует.

---

## 2. Превышение лимита входа

**Описание:**
Ограничение частоты запросов входа с одного IP `GET /skinScan/login` возвращает 429.

**Предусловия:**
Используется уникальный IP в заголовке `X-Forwarded-For`, ранее не применявшийся.

**Шаги:**

1. Выполнить 10 запросов входа подряд с одного IP (в пределах лимита):

```
GET {{baseUrl}}/skinScan/login
X-Forwarded-For: 10.<случайный>.<случайный>.<случайный>
```

2. Выполнить 11-й запрос входа с того же IP (сверх лимита):

```
GET {{baseUrl}}/skinScan/login
X-Forwarded-For: 10.<случайный>.<случайный>.<случайный>
```

**Ожидаемый результат:**
Первые 10 запросов — не 429. 11-й — 429 Too Many Requests, заголовок `Retry-After` присутствует.

---

# UserApiTest

## 1. Обновление данных с валидной авторизацией

**Описание:**
Обновление email и телефона авторизованным пользователем `PUT /skinScan/user/update` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Обновить email и телефон, передав текущий пароль и новые данные:

```
PUT {{baseUrl}}/skinScan/user/update
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
200 OK. Тело — UUID пользователя. Email и телефон обновлены.

---

## 2. Обновление данных без авторизации

**Описание:**
Обновление данных без заголовка Authorization `PUT /skinScan/user/update` возвращает 401.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться обновить данные, не передавая заголовок Authorization:

```
PUT {{baseUrl}}/skinScan/user/update
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
401 Unauthorized. Данные не изменены.

---

## 3. Обновление данных с неверным паролем

**Описание:**
Обновление данных с неверным паролем `PUT /skinScan/user/update` возвращает 401.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Попытаться обновить данные, передав неверный пароль и в Basic-авторизации, и в теле:

```
PUT {{baseUrl}}/skinScan/user/update
Authorization: Basic <base64>(user_<uuid>:wrong-password)
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "wrong-password",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
401 Unauthorized. Данные не изменены.

---

## 4. Успешный вход после смены пароля

**Описание:**
Вход с новым паролем после его успешной смены `GET /skinScan/login` возвращает 200.

**Предусловия:**
Пользователь зарегистрирован (известны `login` и `password`).

**Шаги:**

1. Сменить пароль, передав текущий и новый пароль:

```
PUT {{baseUrl}}/skinScan/user/update/{login}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "oldPassword": "password_<uuid>",
  "newPassword": "new_<uuid>_password"
}
```

2. Войти, используя новый пароль:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(user_<uuid>:new_<uuid>_password)
```

**Ожидаемый результат:**
Смена — 200 OK (тело — UUID). Вход с новым паролем — 200 OK (тело — UUID).

---

## 5. Безуспешный вход со старым паролем после смены

**Описание:**
Вход со старым паролем после его смены `GET /skinScan/login` возвращает 401.

**Предусловия:**
Пользователь зарегистрирован (известны `login` и `password`).

**Шаги:**

1. Сменить пароль, передав текущий и новый пароль:

```
PUT {{baseUrl}}/skinScan/user/update/{login}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "oldPassword": "password_<uuid>",
  "newPassword": "new_<uuid>_password"
}
```

2. Попытаться войти со старым паролем:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(user_<uuid>:password_<uuid>)
```

**Ожидаемый результат:**
Смена — 200 OK. Вход со старым паролем — 401 Unauthorized.

---

## 6. Смена пароля с неверным старым паролем

**Описание:**
Смена пароля при неверно указанном старом пароле `PUT /skinScan/user/update/{login}` возвращает 401.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Попытаться сменить пароль, указав неверный старый пароль:

```
PUT {{baseUrl}}/skinScan/user/update/{login}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "oldPassword": "wrong-old-password",
  "newPassword": "new_password_123"
}
```

**Ожидаемый результат:**
401 Unauthorized. Пароль не изменён.

---

## 7. Смена пароля на короткий

**Описание:**
Смена пароля на пароль короче 6 символов `PUT /skinScan/user/update/{login}` возвращает 400.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Попытаться сменить пароль на пароль короче 6 символов:

```
PUT {{baseUrl}}/skinScan/user/update/{login}
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "oldPassword": "password_<uuid>",
  "newPassword": "123"
}
```

**Ожидаемый результат:**
400 Bad Request. Пароль не изменён.

---

## 8. Обновление данных с невалидным email

**Описание:**
Обновление данных с email неверного формата `PUT /skinScan/user/update` возвращает 400.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Попытаться обновить данные, указав email неверного формата:

```
PUT {{baseUrl}}/skinScan/user/update
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "login": "user_<uuid>",
  "password": "password_<uuid>",
  "email": "not-an-email",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
400 Bad Request. Данные не изменены.

---

## 9. Смена пароля несуществующего логина

**Описание:**
Смена пароля пользователя, которого нет в системе, `PUT /skinScan/user/update/{login}` возвращает 401.

**Предусловия:**
Нет.

**Шаги:**

1. Попытаться сменить пароль пользователя, которого нет в системе:

```
PUT {{baseUrl}}/skinScan/user/update/<случайный-login>
Authorization: Basic <base64>(<случайный>:<случайный>)
Content-Type: application/json

{
  "oldPassword": "password_<uuid>",
  "newPassword": "new_password_123"
}
```

**Ожидаемый результат:**
401 Unauthorized.

---

## 10. Обновление данных с пустым логином

**Описание:**
Обновление данных с пустой строкой в поле login `PUT /skinScan/user/update` возвращает 400.

**Предусловия:**
Пользователь зарегистрирован.

**Шаги:**

1. Попытаться обновить данные, указав пустой логин:

```
PUT {{baseUrl}}/skinScan/user/update
Authorization: Basic <base64>(user_<uuid>:password_<uuid>)
Content-Type: application/json

{
  "login": "",
  "password": "password_<uuid>",
  "email": "user_<uuid>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Ожидаемый результат:**
400 Bad Request. Данные не изменены.

---

## 11. Пользователь A не может сменить пароль пользователя B

**Описание:**
Пользователь «A» может сменить пароль пользователя «B» `PUT /skinScan/user/update/{login}` возвращает 200.

**Предусловия:**
Зарегистрировать пользователя A:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid_A>",
  "password": "password_<uuid_A>",
  "email": "user_<uuid_A>@skin-scan.ru",
  "phone": "+78005553535"
}
```

Зарегистрировать пользователя B:

```
POST {{baseUrl}}/skinScan/register
Content-Type: application/json

{
  "login": "user_<uuid_B>",
  "password": "password_<uuid_B>",
  "email": "user_<uuid_B>@skin-scan.ru",
  "phone": "+78005553535"
}
```

**Шаги:**

1. От имени A попытаться сменить пароль пользователя B: в заголовке Authorization — данные A, в URL — логин B:

```
PUT {{baseUrl}}/skinScan/user/update/{user_<uuid_B>}
Authorization: Basic <base64>(user_<uuid_A>:password_<uuid_A>)
Content-Type: application/json

{
  "oldPassword": "password_<uuid_B>",
  "newPassword": "new_<uuid>_password"
}
```

2. Проверить, что пароль B не изменился — попробовать войти от имени B с новым паролем:

```
GET {{baseUrl}}/skinScan/login?auth=<base64>(user_<uuid_B>:new_<uuid>_password)
```

**Ожидаемый результат:**
403 Forbidden (или 401). Смена пароля допустима только для самого себя; пароль B остаётся неизменным.

**Фактический результат:**
200 OK, тело — UUID пользователя B. Пароль пользователя B изменён — вход B с новым паролем успешен, со старым — 401.
