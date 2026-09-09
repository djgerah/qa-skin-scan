package qa.startup.skinscan.clients;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import qa.startup.skinscan.models.User;
import qa.startup.skinscan.models.UserRequest;

import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/register и /skinScan/login.
 */
public final class AuthClient {

    /**
     * Регистрирует пользователя: POST /skinScan/register. 201 — успех, 400 — данные не прошли валидацию.
     */
    @Step("POST /skinScan/register — регистрация пользователя [{user.login}]")
    public static Response register(User user) {
        return given().baseUri("http://localhost:8080")
                .header("X-Forwarded-For", randomForwardedFor())
                .contentType(ContentType.JSON)
                .body(UserRequest.of(user))
                .when()
                .post("/skinScan/register");
    }

    /**
     * Логин: GET /skinScan/login?auth=Base64("логин:пароль"). 200 — успех, 401 — неверные учётные данные.
     */
    @Step("GET /skinScan/login — вход пользователя [{user.login}]")
    public static Response login(User user) {
        return given().baseUri("http://localhost:8080")
                .header("X-Forwarded-For", randomForwardedFor())
                .queryParam("auth", user.authParam())
                .when()
                .get("/skinScan/login");
    }

    /**
     * Создаёт пользователя со случайными данными и регистрирует его.
     * Возвращает пользователя, который гарантированно существует на сервере.
     * Бросает IllegalStateException, если регистрация не удалась.
     */
    @Step("Подготовка тестового пользователя: генерация и регистрация")
    public static User getRegisteredUser() {
        var user = User.getRandomUser();
        var response = register(user);

        if (response.statusCode() != 201) {
            throw new IllegalStateException(
                    "getRegisteredUser: не удалось зарегистрировать пользователя, код: " + response.statusCode()
                            + ", тело: " + response.asString());
        }

        return user;
    }

    /**
     * Случайный IP для X-Forwarded-For: используется сервером для лимита запросов с одного IP.
     */
    public static String randomForwardedFor() {
        return "10."
                + randomOctet() + "."
                + randomOctet() + "."
                + randomOctet();
    }

    /** Случайный октет IP-адреса: число 1–255 (0 исключён, чтобы не получились адреса вида 10.0.0.0). */
    private static int randomOctet() {
        return ThreadLocalRandom.current().nextInt(1, 256);
    }
}
