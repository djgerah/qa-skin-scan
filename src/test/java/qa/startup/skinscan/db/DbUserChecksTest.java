package qa.startup.skinscan.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.AuthClient;
import qa.startup.skinscan.clients.UserClient;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверки таблицы users напрямую через БД (JDBC).
 */
@Tag("db")
class DbUserChecksTest {

    /** BCrypt-хеш: $2a$12$ + 53 символа (всего 60). */
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    @Test
    @DisplayName("После регистрации пароль хранится в БД как BCrypt-хеш, а не открытым текстом")
    void registeredPasswordIsStoredAsBcryptHash() {
        var user = AuthClient.getRegisteredUser();

        String password = Db.selectQuery("SELECT password FROM users WHERE login = ?", user.login())
                .orElseThrow(() -> new AssertionError("Пользователь " + user.login() + " не найден в БД"));
        String email = Db.selectQuery("SELECT email FROM users WHERE login = ?", user.login())
                .orElseThrow(() -> new AssertionError("Пользователь " + user.login() + " не найден в БД"));
        String phone = Db.selectQuery("SELECT phone FROM users WHERE login = ?", user.login())
                .orElseThrow(() -> new AssertionError("Пользователь " + user.login() + " не найден в БД"));

        assertNotEquals(user.password(), password, "Пароль хранится открытым текстом");
        assertTrue(BCRYPT.matcher(password).matches(), "Пароль не является BCrypt-хешем: " + password);
        assertEquals(user.email(), email);
        assertEquals(user.phone(), phone);
    }

    @Test
    @DisplayName("После смены пароля хеш пароля в БД обновляется")
    void passwordHashIsUpdatedAfterChange() {
        var user = AuthClient.getRegisteredUser();

        String before = Db.selectQuery("SELECT password FROM users WHERE login = ?", user.login())
                .orElseThrow(() -> new AssertionError("Пользователь не найден в БД"));

        UserClient.updatePassword(user, user.login(), user.password(), "new_password_123")
                .then()
                .statusCode(200);

        String after = Db.selectQuery("SELECT password FROM users WHERE login = ?", user.login())
                .orElseThrow(() -> new AssertionError("Пользователь не найден в БД"));

        assertNotEquals(before, after, "Хеш пароля в БД не изменился");
        assertTrue(BCRYPT.matcher(before).matches(), "Старый пароль не является BCrypt-хешем");
        assertTrue(BCRYPT.matcher(after).matches(), "Новый пароль не является BCrypt-хешем");
    }
}
