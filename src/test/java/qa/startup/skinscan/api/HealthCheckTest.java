package qa.startup.skinscan.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("smoke")
class HealthCheckTest {

    @Test
    @DisplayName("Проверка доступности БД")
    void checkRunReturnsOkWithDbStatus() {
        Map<String, Object> body = given().baseUri("http://localhost:8080")
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
}
