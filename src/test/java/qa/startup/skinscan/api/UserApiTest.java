package qa.startup.skinscan.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.UserClient;
import qa.startup.skinscan.models.User;
import qa.startup.skinscan.models.UserRequest;

import static io.restassured.RestAssured.given;
import static qa.startup.skinscan.models.User.getRandomPhone;
import static qa.startup.skinscan.models.User.getRandomUser;

import java.util.UUID;

import static qa.startup.skinscan.clients.AuthClient.login;
import static qa.startup.skinscan.clients.AuthClient.registeredUser;

@Tag("regression")
class UserApiTest {

    @Test
    @DisplayName("Обновление данных пользователя с валидной авторизацией PUT /skinScan/user/update возвращает 200")
    void updateUserDataWithValidAuthReturns200() {
        var user = registeredUser();

        UserClient.update(user, new UserRequest(
                        user.login(),
                        user.password(),
                        UUID.randomUUID().toString().substring(0, 8) + "@skin-scan.ru",
                        getRandomPhone()))
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Обновление данных без авторизации PUT /skinScan/user/update возвращает 401")
    void updateUserDataWithoutAuthReturns401() {
        var user = getRandomUser();

        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(user)
                .when()
                .put("/skinScan/user/update")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Обновление данных с неверным паролем PUT /skinScan/user/update возвращает 401")
    void updateUserDataWithWrongPasswordReturns401() {
        var user = getRandomUser();

        given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(new UserRequest(user.login(), "wrong-password", UUID.randomUUID().toString().substring(0, 8) + "@skin-scan.ru", getRandomPhone()))
                .when()
                .put("/skinScan/user/update")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Успешный вход после смены пароля GET /skinScan/login возвращает 200")
    void loginWithNewPasswordReturns200() {
        var user = registeredUser();
        var newPassword = UUID.randomUUID().toString().substring(0, 8) + "_password";

        UserClient.updatePassword(user, user.password(), newPassword)
                .then()
                .statusCode(200);

        login(new User(user.login(), newPassword, user.email(), user.phone()))
                .then()
                .statusCode(200);
   }

    @Test
    @DisplayName("Безуспешный вход со старым паролем после его смены GET /skinScan/login возвращает 401")
    void loginWithOldPasswordReturns401() {
        var user = registeredUser();
        var newPassword = UUID.randomUUID().toString().substring(0, 8) + "_password";

        UserClient.updatePassword(user, user.password(), newPassword)
                .then()
                .statusCode(200);

        login(user)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Смена пароля с неверным старым паролем PUT /skinScan/user/update/{login} возвращает 401")
    void changePasswordWithWrongOldPasswordReturns401() {
        var user = registeredUser();

        UserClient.updatePassword(user, "wrong-old-password", "new_password_123")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Смена пароля на короткий (< 6 символов) PUT /skinScan/user/update/{login} возвращает 400")
    void changePasswordToShortPasswordReturns400() {
        var user = registeredUser();

        UserClient.updatePassword(user, user.password(), "123")
                .then()
                .statusCode(400);
    }
}
