package qa.startup.skinscan.clients;

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

    private UserClient() {
    }

    public static Response update(User user, UserRequest request) {
        return given().baseUri("http://localhost:8080")
                .header("Authorization", user.basicAuth())
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .put("/skinScan/user/update");
    }

    public static Response updatePassword(User user, String oldPassword, String newPassword) {
        return given().baseUri("http://localhost:8080")
                .header("Authorization", user.basicAuth())
                .contentType(ContentType.JSON)
                .body(new PasswordUpdateRequest(newPassword, oldPassword))
                .when()
                .put("/skinScan/user/update/" + user.login());
    }
}
