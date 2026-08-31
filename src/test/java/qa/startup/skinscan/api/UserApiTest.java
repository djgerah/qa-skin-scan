package qa.startup.skinscan.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.UserClient;
import qa.startup.skinscan.models.TestUser;
import qa.startup.skinscan.models.UserRequest;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static qa.startup.skinscan.config.TestConfig.baseUrl;
import static qa.startup.skinscan.clients.AuthClient.createRandomUser;
import static qa.startup.skinscan.clients.AuthClient.login;

@Tag("regression")
class UserApiTest {

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = baseUrl();
    }

    @Test
    @DisplayName("Обновление данных пользователя: 200")
    void updateUserDataWithValidAuthReturns200() {
        var user = createRandomUser();

        UserClient.update(user, new UserRequest(
                        user.login(),
                        user.password(),
                        "qa_" + UUID.randomUUID() + "@test.local",
                        "+79990001122"))
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("Обновление данных без авторизации: 401")
    void updateUserDataWithoutAuthReturns401() {
        var user = createRandomUser();

        RestAssured.given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(UserRequest.of(user))
                .when()
                .put("/skinScan/user/update")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Обновление данных с неверным паролем: 401")
    void updateUserDataWithWrongPasswordReturns401() {
        var user = createRandomUser();

        RestAssured.given()
                .header("Authorization", "Basic " + java.util.Base64.getEncoder()
                        .encodeToString((user.login() + ":wrong-pass").getBytes()))
                .contentType(io.restassured.http.ContentType.JSON)
                .body(new UserRequest(user.login(), "wrong-pass", null, null))
                .when()
                .put("/skinScan/user/update")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Смена пароля: 200, вход по новому паролю 200, по старому 401")
    void changePasswordFullFlow() {
        var user = createRandomUser();
        var newPassword = "New_" + UUID.randomUUID().toString().substring(0, 8) + "_Pass";

        UserClient.updatePassword(user, user.password(), newPassword)
                .then()
                .statusCode(200);

        login(new TestUser(user.login(), newPassword))
                .then()
                .statusCode(200);

        login(user)
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Смена пароля с неверным старым паролем: 401")
    void changePasswordWithWrongOldPasswordReturns401() {
        var user = createRandomUser();

        UserClient.updatePassword(user, "wrong-old-pass", "Some_New_Pass_123")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("Смена пароля на короткий (<6 символов): 400")
    void changePasswordToShortPasswordReturns400() {
        var user = createRandomUser();

        UserClient.updatePassword(user, user.password(), "123")
                .then()
                .statusCode(400)
                .body(containsString("Пароль"));
    }
}
