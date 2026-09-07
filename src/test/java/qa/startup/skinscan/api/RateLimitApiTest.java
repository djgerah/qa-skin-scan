package qa.startup.skinscan.api;

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
    @DisplayName("Превышение лимита запросов sign up с одного IP")
    void registerRateLimitReturns429AfterLimit() {
        var client = given()
                .header("X-Forwarded-For", randomForwardedFor());

        for (int i = 0; i < REGISTER_LIMIT; i++) {
            int statusCode = client
                    .when()
                    .post("/skinScan/register").statusCode();

            if (statusCode == 429) {
                throw new AssertionError("Лимит исчерпан на запросе #" + (i + 1));
            }
        }

        client.when()
                .post("/skinScan/register")
                .then()
                .statusCode(429);
    }

    @Test
    @DisplayName("Превышение лимита запросов sign in с одного IP")
    void loginRateLimitReturns429AfterLimit() {
        var client = given()
                .header("X-Forwarded-For", randomForwardedFor());

        for (int i = 0; i < LOGIN_LIMIT; i++) {
            int statusCode = client
                    .when()
                    .get("/skinScan/login").statusCode();

            if (statusCode == 429) {
                throw new AssertionError("Лимит исчерпан на запросе #" + (i + 1));
            }
        }

        client.when()
                .get("/skinScan/login")
                .then()
                .statusCode(429);
    }
}
