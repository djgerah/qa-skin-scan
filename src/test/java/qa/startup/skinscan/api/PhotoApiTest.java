package qa.startup.skinscan.api;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.PhotoClient;
import qa.startup.skinscan.models.TestUser;

import java.time.Duration;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static qa.startup.skinscan.config.TestConfig.baseUrl;
import static qa.startup.skinscan.clients.AuthClient.createRandomUser;
import static qa.startup.skinscan.clients.PhotoClient.PNG_1X1;
import static qa.startup.skinscan.clients.PhotoClient.getById;
import static qa.startup.skinscan.clients.PhotoClient.getByName;
import static qa.startup.skinscan.clients.PhotoClient.uniquePhotoName;
import static qa.startup.skinscan.clients.PhotoClient.upload;

@Tag("regression")
class PhotoApiTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = baseUrl();
    }

    private static void awaitAnalyzed(TestUser user, String photoId) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> getById(user, photoId).statusCode() == 200);
    }

    @Test
    @DisplayName("Загрузка фото: 202 и photoId в ответе")
    void uploadPhotoReturns202WithId() {
        var user = createRandomUser();

        upload(user, uniquePhotoName(), PNG_1X1, "image/png")
                .then()
                .statusCode(202);
    }

    @Test
    @DisplayName("Загрузка без авторизации: 401")
    void uploadPhotoWithoutAuthReturns401() {
        RestAssured.given()
                .multiPart("file", uniquePhotoName(), PNG_1X1, "image/png")
                .when()
                .post("/skinScan/photos/upload")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("После обработки фото по id: 200, size_file и processing_time присутствуют")
    void getPhotoByIdAfterAnalysisReturns200WithMetadata() {
        var user = createRandomUser();
        String fileName = uniquePhotoName();

        Response upload = upload(user, fileName, PNG_1X1, "image/png");
        upload.then().statusCode(202);
        String photoId = upload.asString().replace("\"", "").trim();

        awaitAnalyzed(user, photoId);

        getById(user, photoId)
                .then()
                .statusCode(200)
                .body("size_file", greaterThan(0))
                .body("processing_time", notNullValue());
    }

    @Test
    @DisplayName("Получение фото по имени: 200")
    void getPhotoByNameAfterAnalysisReturns200() {
        var user = createRandomUser();
        String fileName = uniquePhotoName();

        Response upload = upload(user, fileName, PNG_1X1, "image/png");
        upload.then().statusCode(202);
        awaitAnalyzed(user, upload.asString().replace("\"", "").trim());

        getByName(user, fileName)
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Получение фото по несуществующему имени: 404")
    void getPhotoByUnknownNameReturns404() {
        var user = createRandomUser();

        getByName(user, "no_such_file_" + UUID.randomUUID() + ".png")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Получение фото по несуществующему id: 404")
    void getPhotoByUnknownIdReturns404() {
        var user = createRandomUser();

        getById(user, UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("Чужой пользователь не имеет доступа к фото: 403")
    void otherUserCannotAccessPhotoReturns403() {
        var owner = createRandomUser();
        var stranger = createRandomUser();

        Response upload = upload(owner, uniquePhotoName(), PNG_1X1, "image/png");
        upload.then().statusCode(202);
        String photoId = upload.asString().replace("\"", "").trim();

        getById(stranger, photoId)
                .then()
                .statusCode(403);
    }

    @Test
    @DisplayName("Список фото без авторизации: 401")
    void getAllPhotosWithoutAuthReturns401() {
        RestAssured.given()
                .when()
                .get("/skinScan/photos")
                .then()
                .statusCode(401);
    }
}
