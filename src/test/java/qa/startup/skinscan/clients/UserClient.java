package qa.startup.skinscan.clients;

import io.qameta.allure.Step;
import qa.startup.skinscan.config.Config;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import qa.startup.skinscan.models.PasswordUpdateRequest;
import qa.startup.skinscan.models.User;
import qa.startup.skinscan.models.UserRequest;

import static io.restassured.RestAssured.given;

/**
 * Клиент эндпоинтов /skinScan/user/update и /skinScan/user/update/{login}.
 */
public final class UserClient {

    /** Обновляет данные пользователя: PUT /skinScan/user/update. 200 — успех, 401 — без авторизации. */
    @Step("PUT /skinScan/user/update — обновление данных пользователя [{user.login}]")
    public static Response update(User user, UserRequest request) {
        return given().baseUri(Config.baseUrl())
                .header("Authorization", user.basicAuth())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/skinScan/user/update");
    }

    /** Меняет пароль: PUT /skinScan/user/update/{login}. 200 — успех, 401/403 — неверный старый пароль или чужой login. */
    @Step("PUT /skinScan/user/update/{login} — смена пароля пользователя [{user.login}]")
    public static Response updatePassword(User user, String oldPassword, String newPassword) {
        return given().baseUri(Config.baseUrl())
                .header("Authorization", user.basicAuth())
                .contentType(ContentType.JSON)
                .body(new PasswordUpdateRequest(newPassword, oldPassword))
                .when()
                .put("/skinScan/user/update/" + user.login());
    }
}
