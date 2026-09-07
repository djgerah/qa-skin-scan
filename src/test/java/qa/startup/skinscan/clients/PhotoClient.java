package qa.startup.skinscan.clients;

import io.restassured.response.Response;
import qa.startup.skinscan.config.TestConfig;
import qa.startup.skinscan.models.User;

import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/photos.
 * Загрузка — multipart/form-data (файл), остальное — обычные GET-запросы.
 */
public final class PhotoClient {

    private PhotoClient() {
    }

    /** Минимальный валидный PNG 1x1 пиксель. */
    public static final byte[] PNG_1X1 = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==");

    public static String uniquePhotoName() {
        return "qa_photo_" + UUID.randomUUID() + ".png";
    }

    public static Response upload(User user, String fileName, byte[] content, String mimeType) {
        return given().baseUri(TestConfig.BASE_URL)
                .header("Authorization", user.basicAuth())
                .multiPart("file", fileName, content, mimeType)
                .multiPart("tags", "qa")
                .multiPart("description", "autotest")
                .multiPart("category", "skin")
                .when()
                .post("/skinScan/photos/upload");
    }

    public static Response getById(User user, String photoId) {
        return given().baseUri(TestConfig.BASE_URL)
                .header("Authorization", user.basicAuth())
                .when()
                .get("/skinScan/photos/" + photoId);
    }

    public static Response getByName(User user, String nameFile) {
        return given().baseUri(TestConfig.BASE_URL)
                .header("Authorization", user.basicAuth())
                .when()
                .get("/skinScan/photos/name/" + nameFile);
    }

    public static Response getAll(User user) {
        return given().baseUri(TestConfig.BASE_URL)
                .header("Authorization", user.basicAuth())
                .when()
                .get("/skinScan/photos");
    }
}
