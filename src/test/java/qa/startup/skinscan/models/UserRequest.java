package qa.startup.skinscan.models;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Тело запроса регистрации / обновления данных пользователя.
 * Соответствует ru.startup.skinscan.web.model.UserRequest на сервере.
 * null-поля не сериализуются (email и phone опциональны).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserRequest(String login, String password, String email, String phone) {

    public static UserRequest of(TestUser user) {
        return new UserRequest(user.login(), user.password(), null, null);
    }
}
