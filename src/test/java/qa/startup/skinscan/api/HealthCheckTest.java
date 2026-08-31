package qa.startup.skinscan.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static qa.startup.skinscan.config.TestConfig.baseUrl;

@Tag("smoke")
class HealthCheckTest {

    @Test
    @DisplayName("GET /check-run: 200, статус 'Успешно', БД отвечает")
    void checkRunReturnsOkWithDbStatus() {
        Map<String, Object> body = given().baseUri(baseUrl())
                .when()
                .get("/skinScan/check-run")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");

        assertEquals("Успешно", body.get("статус"));
        assertEquals("PostgreSQL", body.get("БД"));
        assertEquals(1, ((Number) body.get("Результат работы БД")).intValue());
    }

    @Test
    @DisplayName("Swagger-документация доступна без авторизации")
    void swaggerDocsArePublic() {
        given().baseUri(baseUrl())
                .redirects().follow(false)
                .when()
                .get("/v3/api-docs")
                .then()
                .statusCode(200);
    }
}
