package qa.startup.skinscan.db;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.AuthClient;
import qa.startup.skinscan.clients.UserClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static qa.startup.skinscan.db.BcryptHash.isBcrypt;

@Tag("db")
class DbUserChecksTest {

    @Test
    @DisplayName("После регистрации пароль хранится в БД как BCrypt-хеш, а не открытым текстом")
    void registeredPasswordIsStoredAsBcryptHash() {
        var user = AuthClient.getRegisteredUser();
        Map<String, Object> row = Db.jdbc().queryForMap(
                "SELECT password, email, phone FROM users WHERE login = ?", user.login());

        String password = (String) row.get("password");

        assertNotEquals(user.password(), password);
        assertTrue(isBcrypt(password));
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
        assertTrue(isBcrypt(rowBefore));
        assertTrue(isBcrypt(rowAfter));
    }
}
