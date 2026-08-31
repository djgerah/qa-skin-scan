package qa.startup.skinscan.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static qa.startup.skinscan.config.TestConfig.baseUrl;

@Tag("regression")
class RateLimitApiTest {

    private static final int REGISTER_LIMIT = 5;
    private static final int LOGIN_LIMIT = 10;

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = baseUrl();
    }

    private static io.restassured.specification.RequestSpecification givenAsUniqueClient() {
        // Октеты 1..250, чтобы IP всегда был валидным (Math.abs(Integer.MIN_VALUE) отрицательный)
        String uniqueIp = "10."
                + (1 + Math.abs(UUID.randomUUID().hashCode()) % 250) + "."
                + (1 + Math.abs(UUID.randomUUID().hashCode()) % 250) + ".42";
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
                    .queryParam("auth", "cXpfZmFrZV91c2VyOnNvbWVfcGFzcw==") // qa_fake_user:some_pass
                    .get("/skinScan/login").statusCode();
            if (statusCode == 429) {
                throw new AssertionError("Лимит исчерпан раньше времени на запросе #" + (i + 1));
            }
        }

        client.when()
                .queryParam("auth", "cXpfZmFrZV91c2VyOnNvbWVfcGFzcw==")
                .get("/skinScan/login")
                .then()
                .statusCode(429)
                .header("Retry-After", notNullValue());
    }

    @Test
    @DisplayName("Эндпоинты, не подпадающие под rate limiting (check-run), не отдают 429")
    void nonLimitedEndpointsAreNotRateLimited() {
        var client = givenAsUniqueClient();

        for (int i = 0; i < REGISTER_LIMIT + 3; i++) {
            client.when()
                    .get("/skinScan/check-run")
                    .then()
                    .statusCode(200);
        }
    }
}
