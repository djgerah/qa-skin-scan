package qa.startup.skinscan.clients;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import qa.startup.skinscan.config.TestConfig;
import qa.startup.skinscan.models.User;
import qa.startup.skinscan.models.UserRequest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/register и /skinScan/login.
 */
public final class AuthClient {

    public static Response register(User request) {
        return given().baseUri("http://localhost:8080")
                .header("X-Forwarded-For", randomForwardedFor())
                .header("User-Agent", "qa-test/" + UUID.randomUUID())
                .contentType(ContentType.JSON)
                .body(UserRequest.of(request))
                .when()
                .post("/skinScan/register");
    }

    public static Response login(User user) {
        return given().baseUri(TestConfig.BASE_URL)
                .header("X-Forwarded-For", randomForwardedFor())
                .header("User-Agent", "qa-test/" + UUID.randomUUID())
                .queryParam("auth", user.authParam())
                .when()
                .get("/skinScan/login");
    }

    /**
     * Фикстура: генерирует случайные данные (User.random()) и регистрирует
     * пользователя через API. Возвращает пользователя, который гарантированно
     * существует на сервере.
     */
    public static User registeredUser() {
        User user = User.random();
        Response response = register(user);
        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Не удалось зарегистрировать тестового пользователя, код: " + response.statusCode()
                            + ", тело: " + response.asString());
        }
        return user;
    }

    public static String loginAndGetUserId(User user) {
        Response response = login(user);
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Логин не удался, код: " + response.statusCode() + ", тело: " + response.asString());
        }
        return response.asString().replace("\"", "").trim();
    }

    private static String randomForwardedFor() {
        return "10."
                + randomOctet() + "."
                + randomOctet() + "."
                + randomOctet();
    }

    private static int randomOctet() {
        return ThreadLocalRandom.current().nextInt(1, 256);
    }
}
