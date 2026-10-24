package qa.startup.skinscan.models;

/**
 * Тело запроса логина.
 *
 * @param login логин пользователя
 * @param pass  пароль в открытом виде
 */
public record JwtRequest(String login, String pass) {

    public static JwtRequest of(User user) {
        return new JwtRequest(user.login(), user.password());
    }
}
