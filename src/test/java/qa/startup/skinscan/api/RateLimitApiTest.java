package qa.startup.skinscan.api;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static qa.startup.skinscan.clients.AuthClient.randomForwardedFor;

@Tag("regression")
class RateLimitApiTest {

    private static final int REGISTER_LIMIT = 5;
    private static final int LOGIN_LIMIT = 10;

    @Test
    @DisplayName("Превышение лимита запросов с одного IP POST /skinScan/register возвращает 429")
    void registerRateLimitReturns429AfterLimit() {
        var client = given()
                .header("X-Forwarded-For", randomForwardedFor());

        Allure.step("POST /skinScan/register: " + REGISTER_LIMIT + " запросов в пределах лимита", (step) -> {
            for (int i = 0; i < REGISTER_LIMIT; i++) {
                int statusCode = client
                        .when()
                        .post("/skinScan/register").statusCode();

                if (statusCode == 429) {
                    throw new AssertionError("Лимит исчерпан на запросе #" + (i + 1));
                }
            }
        });

        Allure.step("POST /skinScan/register: запрос сверх лимита", (step) -> {
            client.when()
                    .post("/skinScan/register")
                    .then()
                    .statusCode(429);
        });
    }

    @Test
    @DisplayName("Превышение лимита запросов с одного IP GET /skinScan/login возвращает 429")
    void loginRateLimitReturns429AfterLimit() {
        var client = given()
                .header("X-Forwarded-For", randomForwardedFor());

        Allure.step("GET /skinScan/login: " + LOGIN_LIMIT + " запросов в пределах лимита", (step) -> {
            for (int i = 0; i < LOGIN_LIMIT; i++) {
                int statusCode = client
                        .when()
                        .get("/skinScan/login").statusCode();

                if (statusCode == 429) {
                    throw new AssertionError("Лимит исчерпан на запросе #" + (i + 1));
                }
            }
        });

        Allure.step("GET /skinScan/login: запрос сверх лимита", (step) -> {
            client.when()
                    .get("/skinScan/login")
                    .then()
                    .statusCode(429);
        });
    }
}
