package qa.startup.skinscan.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.models.User;

import static qa.startup.skinscan.clients.AuthClient.login;
import static qa.startup.skinscan.clients.AuthClient.register;
import static qa.startup.skinscan.clients.AuthClient.registeredUser;
import static qa.startup.skinscan.models.User.getRandomUser;

@Tag("smoke")
@Tag("regression")
class AuthApiTest {
    @Test
    @DisplayName("Регистрация нового пользователя POST /skinScan/register возвращает 201")
    void registerNewUserReturns201() {
        var user = getRandomUser();

        register(user)
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Повторная регистрация того же логина POST /skinScan/register возвращает 409")
    void registerDuplicateUserReturns409() {
        var user = getRandomUser();

        register(user).then().statusCode(201);

        register(user)
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Регистрация с невалидным email POST /skinScan/register возвращает 400")
    void registerWithInvalidEmailReturns400() {
        var user = getRandomUser();

        register(new User(user.login(), user.password(), "not-an-email", user.phone()))
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Регистрация с коротким паролем (< 6 символов) POST /skinScan/register возвращает 400")
    void registerWithShortPasswordReturns400() {
        var user = getRandomUser();

        register(new User(
                user.login(), "123", user.email(), user.phone()))
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Вход с валидными данными GET /skinScan/login возвращает 200")
    void loginWithValidCredentialsReturnsUserId() {
        var user = registeredUser();

        login(user)
                .then()
                .statusCode(200)
                .extract().asString();
    }

    @Test
    @DisplayName("Вход с неверным паролем GET /skinScan/login возвращает 401")
    void loginWithWrongPasswordReturns401() {
        var user = registeredUser();

        login(new User(user.login(), "wrong-password-123", user.email(), user.phone()))
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Вход несуществующего пользователя GET /skinScan/login возвращает 401")
    void loginUnknownUserReturns401() {
        login(getRandomUser())
                .then()
                .statusCode(401);
    }
}
