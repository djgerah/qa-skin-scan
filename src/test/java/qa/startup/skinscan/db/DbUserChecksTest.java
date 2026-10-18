package qa.startup.skinscan.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.AuthClient;
import qa.startup.skinscan.clients.UserClient;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Tag("db")
class DbUserChecksTest {
    /**
     * BCrypt — это алгоритм хеширования паролей.
     * Результат — строка фиксированной длины 60 символов с внутренней структурой:
     * Шаблон BCrypt-хеша:
     * $2a$12$RgtJlVaWeeP6KkTxPGJgIehncHWzSBZYiI91BW11YuJcaiDd55WMm
     * └┬─┘└┬┘└┬┘└───────────────────────┬──────────────────────┘
     *  │   │  │                        соль + хеш, 53 символа
     *  │   │  cost (2^12 = 4096 итераций)
     *  │   версия алгоритма: 2a / 2b / 2y
     *  маркер BCrypt
     */
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    @Test
    @DisplayName("После регистрации пароль хранится в БД как BCrypt-хеш, а не открытым текстом")
    void registeredPasswordIsStoredAsBcryptHash() {
        var user = AuthClient.getRegisteredUser();
        Map<String, Object> row = Db.jdbc().queryForMap(
                "SELECT password, email, phone FROM users WHERE login = ?", user.login());

        String password = (String) row.get("password");

        assertNotEquals(user.password(), password);
        assertTrue(BCRYPT.matcher(password).matches());
        assertEquals(user.email(), row.get("email"));
        assertEquals(user.phone(), row.get("phone"));
    }

    @Test
    @DisplayName("После смены пароля хеш пароля в БД обновляется")
    void passwordHashIsUpdatedAfterChange() {
        var user = AuthClient.getRegisteredUser();
        String rowBefore = Db.jdbc().queryForObject(
                "SELECT password FROM users WHERE login = ?", String.class, user.login());

        UserClient.updatePassword(user, user.login(), user.password(), "new_password_123")
                .then()
                .statusCode(200);

        String rowAfter = Db.jdbc().queryForObject(
                "SELECT password FROM users WHERE login = ?", String.class, user.login());

        assertNotNull(rowBefore);
        assertNotNull(rowAfter);
        assertNotEquals(rowBefore, rowAfter);
        assertTrue(BCRYPT.matcher(rowBefore).matches());
        assertTrue(BCRYPT.matcher(rowAfter).matches());
    }
}
