package qa.startup.skinscan.models;

/**
 * Тело запроса логина.
 * Соответствует ru.startup.skinscan.web.model.jwt.JwtRequest на сервере.
 *
 * @param login логин пользователя
 * @param pass  пароль в открытом виде
 */
public record JwtRequest(String login, String pass) {

    public static JwtRequest of(User user) {
        return new JwtRequest(user.login(), user.password());
    }
}
