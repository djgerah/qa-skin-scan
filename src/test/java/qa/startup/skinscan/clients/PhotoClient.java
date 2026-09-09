package qa.startup.skinscan.clients;

import io.restassured.response.Response;
import qa.startup.skinscan.models.User;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/photos.
 * Загрузка — multipart/form-data (файл), остальное — обычные GET-запросы.
 * Тестовые данные (содержимое и имена файлов) — см. qa.startup.skinscan.testdata.Picture.
 */
public final class PhotoClient {

    /**
     * Загружает фото: POST /skinScan/photos/upload.
     * Успех — 202, тело ответа — id фото (UUID).
     * Обработка асинхронная: GET по id вернёт 200 только после анализа.
     */
    public static Response upload(User user, String fileName, byte[] content, String mimeType) {
        return given().baseUri("http://localhost:8080")
                .header("Authorization", user.basicAuth())
                .multiPart("file", fileName, content, mimeType)
                .when()
                .post("/skinScan/photos/upload");
    }

    /** Получает фото по id: GET /skinScan/photos/{id}. 404 — нет такого, 403 — чужое. */
    public static Response getPhotoById(User user, String photoId) {
        return given().baseUri("http://localhost:8080")
                .header("Authorization", user.basicAuth())
                .when()
                .get("/skinScan/photos/" + photoId);
    }

    /** Ищет фото по имени файла: GET /skinScan/photos/name/{nameFile}. 404 — не найдено. */
    public static Response getPhotoByName(User user, String nameFile) {
        return given().baseUri("http://localhost:8080")
                .header("Authorization", user.basicAuth())
                .when()
                .get("/skinScan/photos/name/" + nameFile);
    }

    /** Список всех фото пользователя: GET /skinScan/photos. */
    public static Response getAllPhotos(User user) {
        return given().baseUri("http://localhost:8080")
                .header("Authorization", user.basicAuth())
                .when()
                .get("/skinScan/photos");
    }
}
