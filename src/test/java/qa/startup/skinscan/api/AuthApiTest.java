package qa.startup.skinscan.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.models.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static qa.startup.skinscan.clients.AuthClient.login;
import static qa.startup.skinscan.clients.AuthClient.register;
import static qa.startup.skinscan.clients.AuthClient.registeredUser;
import static qa.startup.skinscan.models.User.random;

@Tag("smoke")
@Tag("regression")
class AuthApiTest {
    @Test
    @DisplayName("Регистрация нового пользователя")
    void registerNewUserReturns201() {
        var user = random();

        register(user)
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Повторная регистрация того же логина")
    void registerDuplicateUserReturns409() {
        var user = random();

        register(user).then().statusCode(201);

        register(user)
                .then()
                .statusCode(409);
    }

    @Test
    @DisplayName("Регистрация с невалидным email: 400")
    void registerWithInvalidEmailReturns400() {
        var user = random();

        register(new User(user.login(), user.password(), "not-an-email", null))
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Регистрация с коротким паролем (<6 символов): 400")
    void registerWithShortPasswordReturns400() {
        register(new User(
                "qa_user_" + UUID.randomUUID().toString().substring(0, 8), "123", null, null))
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Логин с валидными данными: 200 и UUID в теле")
    void loginWithValidCredentialsReturnsUserId() {
        var user = registeredUser();

        var body = login(user)
                .then()
                .statusCode(200)
                .extract().asString();

        var uuid = body.replace("\"", "").trim();
        assertDoesNotThrow(() -> UUID.fromString(uuid),
                "Тело ответа логина должно быть UUID, получено: " + body);
    }

    @Test
    @DisplayName("Логин с неверным паролем: 401")
    void loginWithWrongPasswordReturns401() {
        var user = registeredUser();
        var wrong = new User(user.login(), "wrong-password-123", user.email(), user.phone());

        login(wrong)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Логин несуществующего пользователя: 401")
    void loginUnknownUserReturns401() {
        login(random())
                .then()
                .statusCode(401);
    }
}
