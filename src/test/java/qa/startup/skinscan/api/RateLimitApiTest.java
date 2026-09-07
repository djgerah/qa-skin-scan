package qa.startup.skinscan.api;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static qa.startup.skinscan.clients.AuthClient.randomForwardedFor;

@Tag("regression")
class RateLimitApiTest {

    private static final int REGISTER_LIMIT = 5;
    private static final int LOGIN_LIMIT = 10;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "http://localhost:8080";
    }

    private static RequestSpecification givenAsUniqueClient() {
        String uniqueIp = randomForwardedFor();

        return RestAssured.given()
                .header("X-Forwarded-For", uniqueIp)
                .header("User-Agent", "qa-rate-limit-test/" + UUID.randomUUID());
    }

    @Test
    @DisplayName("Регистрация: (limit+1)-й запрос с одного IP возвращает 429 и заголовки X-RateLimit-*")
    void registerRateLimitReturns429AfterLimit() {
        var client = givenAsUniqueClient();

        for (int i = 0; i < REGISTER_LIMIT; i++) {
            int statusCode = client.when().post("/skinScan/register").statusCode();

            if (statusCode == 429) {
                throw new AssertionError("Лимит исчерпан раньше времени на запросе #" + (i + 1));
            }
        }

        client.when()
                .post("/skinScan/register")
                .then()
                .statusCode(429)
                .header("Retry-After", notNullValue())
                .header("X-RateLimit-Limit", equalTo(String.valueOf(REGISTER_LIMIT)));
    }

    @Test
    @DisplayName("Логин: (limit+1)-й запрос с одного IP возвращает 429")
    void loginRateLimitReturns429AfterLimit() {
        var client = givenAsUniqueClient();

        for (int i = 0; i < LOGIN_LIMIT; i++) {
            int statusCode = client.when()
                    .get("/skinScan/login").statusCode();

            if (statusCode == 429) {
                throw new AssertionError("Лимит исчерпан раньше времени на запросе #" + (i + 1));
            }
        }

        client.when()
                .get("/skinScan/login")
                .then()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }
}
