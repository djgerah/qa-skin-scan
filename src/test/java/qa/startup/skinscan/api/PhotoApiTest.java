package qa.startup.skinscan.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.AuthClient;
import qa.startup.skinscan.clients.PhotoClient;
import qa.startup.skinscan.models.User;
import qa.startup.skinscan.testdata.Picture;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("regression")
class PhotoApiTest {

    /**
     * Ждёт завершения асинхронного анализа фото (сервер возвращает 502, пока фото в обработке).
     */
    private static void awaitAnalyzed(User user, String photoId) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> PhotoClient.getPhotoById(user, photoId).statusCode() == 200);
    }

    @Test
    @DisplayName("Загрузка фото POST /skinScan/photos/upload возвращает 202")
    void uploadPhotoReturns202WithId() {
        var user = AuthClient.getRegisteredUser();

        PhotoClient.upload(user, Picture.uniqueName(), Picture.PNG_1X1, Picture.mimeType)
                .then()
                .statusCode(202);
    }

    @Test
    @DisplayName("Загрузка без авторизации POST /skinScan/photos/upload возвращает 401")
    void uploadPhotoWithoutAuthReturns401() {
        RestAssured.given()
                .multiPart("file", Picture.uniqueName(), Picture.PNG_1X1, Picture.mimeType)
                .when()
                .post("/skinScan/photos/upload")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Получение фото по id после анализа GET /skinScan/photos/{id} возвращает 200")
    void getPhotoByIdAfterAnalysisReturns200WithMetadata() {
        var user = AuthClient.getRegisteredUser();

        Response response = PhotoClient.upload(user, Picture.uniqueName(), Picture.PNG_1X1, Picture.mimeType);

        response.then()
                .statusCode(202);

        String photoId = response.asString().replace("\"", "").trim();

        awaitAnalyzed(user, photoId);

        var json = PhotoClient.getPhotoById(user, photoId)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertTrue(json.getInt("size_file") > 0);
        assertNotNull(json.getString("processing_time"));
    }

    @Test
    @DisplayName("Получение фото по имени GET /skinScan/photos/name/{nameFile} возвращает 200")
    void getPhotoByNameAfterAnalysisReturns200() {
        var user = AuthClient.getRegisteredUser();
        String pictureName = Picture.uniqueName();

        Response response = PhotoClient.upload(user, pictureName, Picture.PNG_1X1, Picture.mimeType);

        String photoId = response.then()
                .statusCode(202)
                .extract()
                .jsonPath().getString("");

        awaitAnalyzed(user, photoId);

        var json = PhotoClient.getPhotoByName(user, pictureName)
                .then()
                .statusCode(200)
                .extract()
                .jsonPath();

        assertTrue(json.getInt("size_file") > 0);
        assertNotNull(json.getString("processing_time"));
    }

    @Test
    @DisplayName("Получение фото по несуществующему имени GET /skinScan/photos/name/{nameFile} возвращает 404")
    void getPhotoByUnknownNameReturns404() {
        var user = AuthClient.getRegisteredUser();
        String noSuchPicture = Picture.uniqueName();

        PhotoClient.getPhotoByName(user, noSuchPicture)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Получение фото по несуществующему id GET /skinScan/photos/{id} возвращает 404")
    void getPhotoByUnknownIdReturns404() {
        var user = AuthClient.getRegisteredUser();
        String noSuchPicture = Picture.uniqueId();

        PhotoClient.getPhotoById(user, noSuchPicture)
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Посторонний пользователь не имеет доступа к фото GET /skinScan/photos/{id} возвращает 403")
    void otherUserCannotAccessPhotoReturns403() {
        var owner = AuthClient.getRegisteredUser();
        var stranger = AuthClient.getRegisteredUser();

        Response response = PhotoClient.upload(owner, Picture.uniqueName(), Picture.PNG_1X1, Picture.mimeType);

        String photoId = response.then()
                .statusCode(202)
                .extract()
                .jsonPath().getString("");

        PhotoClient.getPhotoById(stranger, photoId)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Список фото без авторизации GET /skinScan/photos возвращает 401")
    void getAllPhotosWithoutAuthReturns401() {
        RestAssured.given()
                .when()
                .get("/skinScan/photos")
                .then()
                .statusCode(401);
    }
}
