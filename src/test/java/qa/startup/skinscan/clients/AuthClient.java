package qa.startup.skinscan.clients;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import qa.startup.skinscan.models.User;
import qa.startup.skinscan.models.UserRequest;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/register и /skinScan/login.
 */
public final class AuthClient {

    public static Response register(User user) {
        return given().baseUri("http://localhost:8080")
                .header("X-Forwarded-For", randomForwardedFor())
                .contentType(ContentType.JSON)
                .body(UserRequest.of(user))
                .when()
                .post("/skinScan/register");
    }

    public static Response login(User user) {
        return given().baseUri("http://localhost:8080")
                .header("X-Forwarded-For", randomForwardedFor())
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
        User user = User.getRandomUser();
        Response response = register(user);

        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                    "Не удалось зарегистрировать пользователя, код: " + response.statusCode()
                            + ", тело: " + response.asString());
        }

        return user;
    }

//    public static String loginAndGetUserId(User user) {
//        Response response = login(user);
//        if (response.statusCode() != 200) {
//            throw new IllegalStateException(
//                    "Логин не удался, код: " + response.statusCode() + ", тело: " + response.asString());
//        }
//        return response.asString().replace("\"", "").trim();
//    }

    public static String randomForwardedFor() {
        return "10."
                + randomOctet() + "."
                + randomOctet() + "."
                + randomOctet();
    }

    private static int randomOctet() {
        return ThreadLocalRandom.current().nextInt(1, 256);
    }
}
