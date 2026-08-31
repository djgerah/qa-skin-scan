package qa.startup.skinscan.clients;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import qa.startup.skinscan.config.TestConfig;
import qa.startup.skinscan.models.TestUser;
import qa.startup.skinscan.models.UserRequest;

import java.util.UUID;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/register и /skinScan/login.
 */
public final class AuthClient {

    private AuthClient() {
    }

    public static Response register(UserRequest request) {
        return given().baseUri(TestConfig.baseUrl())
                // уникальный IP и User-Agent, чтобы не упираться в rate limiting между тестами
                .header("X-Forwarded-For", uniqueForwardedFor())
                .header("User-Agent", "qa-test/" + UUID.randomUUID())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/skinScan/register");
    }

    public static Response login(TestUser user) {
        return given().baseUri(TestConfig.baseUrl())
                .header("X-Forwarded-For", uniqueForwardedFor())
                .header("User-Agent", "qa-test/" + UUID.randomUUID())
                .queryParam("auth", user.authParam())
                .when()
                .get("/skinScan/login");
    }

    public static TestUser createRandomUser() {
        TestUser user = TestUser.random();
        Response response = register(UserRequest.of(user));
        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Не удалось зарегистрировать тестового пользователя, код: " + response.statusCode()
                            + ", тело: " + response.asString());
        }
        return user;
    }

    public static String loginAndGetUserId(TestUser user) {
        Response response = login(user);
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Логин не удался, код: " + response.statusCode() + ", тело: " + response.asString());
        }
        return response.asString().replace("\"", "").trim();
    }

    private static String uniqueForwardedFor() {
        return "10." + (1 + Math.abs(UUID.randomUUID().hashCode()) % 250) + "."
                + (1 + Math.abs(UUID.randomUUID().hashCode()) % 250) + "."
                + (1 + Math.abs(UUID.randomUUID().hashCode()) % 250);
    }
}
