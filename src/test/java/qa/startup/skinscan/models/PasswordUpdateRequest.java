package qa.startup.skinscan.models;

/**
 * Тело запроса смены пароля.
 */
public record PasswordUpdateRequest(String newPassword, String oldPassword) {
}
