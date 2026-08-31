package qa.startup.skinscan.models;

/**
 * Тело запроса смены пароля.
 * Соответствует ru.startup.skinscan.web.model.PasswordUpdateRequest на сервере.
 */
public record PasswordUpdateRequest(String newPassword, String oldPassword) {
}
