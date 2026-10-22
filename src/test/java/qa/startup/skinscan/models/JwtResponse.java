package qa.startup.skinscan.models;

/**
 * Ответ логина и обновления токенов.
 *
 * @param type         схема авторизации, всегда "Bearer"
 * @param accessToken  токен доступа, передаётся в заголовке Authorization
 * @param refreshToken токен обновления
 */
public record JwtResponse(String type, String accessToken, String refreshToken) {
}
