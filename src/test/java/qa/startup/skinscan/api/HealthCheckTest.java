package qa.startup.skinscan.api;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("smoke")
class HealthCheckTest {

    @Test
    @DisplayName("Проверка доступности БД GET /skinScan/check-run возвращает 200")
    void checkRunReturnsOkWithDbStatus() {
        final Map<String, Object> body = checkRun();

        assertEquals("Успешно", body.get("статус"));
        assertEquals("PostgreSQL", body.get("БД"));
        assertEquals(1, ((Number) body.get("Результат работы БД")).intValue());
    }

    @Step("GET /skinScan/check-run — проверка доступности БД")
    private Map<String, Object> checkRun() {
        return given().baseUri("http://localhost:8080")
                .when()
                .get("/skinScan/check-run")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getMap("$");
    }
}
