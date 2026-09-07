package qa.startup.skinscan.api;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.UserClient;
import qa.startup.skinscan.config.Db;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static qa.startup.skinscan.clients.AuthClient.loginAndGetUserId;
import static qa.startup.skinscan.clients.AuthClient.register;
import static qa.startup.skinscan.clients.AuthClient.registeredUser;
import static qa.startup.skinscan.clients.PhotoClient.PNG_1X1;
import static qa.startup.skinscan.clients.PhotoClient.uniquePhotoName;
import static qa.startup.skinscan.clients.PhotoClient.upload;
import static qa.startup.skinscan.models.User.random;

/**
 * Верификация на уровне БД: проверяем не только HTTP-ответы API,
 * но и реальные записи в PostgreSQL (users, photo, analysis).
 * Схема БД — см. src/main/resources/db/migration/.
 */
@Tag("regression")
class DbVerificationTest {

    // BCrypt-хэши всегда начинаются с одного из этих префиксов ($2a/$2b/$2y — варианты алгоритма)
    private static final String BCRYPT_PREFIX_REGEX = "^\\$2[aby]\\$\\d{2}\\$.{53}$";

    @Test
    @DisplayName("Регистрация через API создаёт запись в users с BCrypt-хэшем пароля")
    void registerCreatesUserRowWithBcryptHash() {
        var user = random();
//        String email = "qa_" + UUID.randomUUID() + "@test.local";
//        String phone = "+70000000001";

        register(user)
                .then()
                .statusCode(201);

        try (var c = Db.connect();
             var ps = c.prepareStatement(
                     "SELECT password, email, phone, created_at FROM users WHERE login = ?")) {
            ps.setString(1, user.login());
            try (var rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Пользователь " + user.login() + " должен существовать в таблице users");

                String storedHash = rs.getString("password");
                assertTrue(storedHash.matches(BCRYPT_PREFIX_REGEX),
                        "Пароль должен храниться как BCrypt-хэш, получено: " + storedHash);
                assertNotEquals(user.password(), storedHash,
                        "Пароль не должен храниться в открытом виде");

                assertEquals(user.email(), rs.getString("email"),
                        "email, переданный при регистрации, должен быть сохранён");
                assertEquals(user.phone(), rs.getString("phone"));
                assertNotNull(rs.getTimestamp("created_at"), "created_at должен быть заполнен");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Смена пароля через API обновляет BCrypt-хэш в users")
    void passwordChangeUpdatesHashInDb() {
        var user = registeredUser();

        String hashBefore = (String) Db.queryValue(
                "SELECT password FROM users WHERE login = ?", user.login()).orElseThrow();

        var newPassword = "New_" + UUID.randomUUID().toString().substring(0, 8) + "_Pass";
        UserClient.updatePassword(user, user.password(), newPassword).then().statusCode(200);

        String hashAfter = (String) Db.queryValue(
                "SELECT password FROM users WHERE login = ?", user.login()).orElseThrow();

        assertNotEquals(hashBefore, hashAfter, "Хэш пароля в БД должен измениться после смены пароля");
        assertTrue(hashAfter.matches(BCRYPT_PREFIX_REGEX),
                "Новый пароль должен храниться как BCrypt-хэш, получено: " + hashAfter);
    }

    @Test
    @DisplayName("Загрузка фото создаёт запись в photo, а после анализа — запись в analysis со статусом 'Анализ завершен'")
    void photoUploadAndAnalysisPersistedInDb() {
        var user = registeredUser();
        String userId = loginAndGetUserId(user);
        String fileName = uniquePhotoName();

        var upload = upload(user, fileName, PNG_1X1, "image/png");
        upload.then().statusCode(202);
        String photoId = upload.asString().replace("\"", "").trim();

        // Запись фото появилась сразу после загрузки
        try (var c = Db.connect();
             var ps = c.prepareStatement(
                     "SELECT user_id, file_name, mime_type, file_size_bytes, status FROM photo WHERE id = ?::uuid")) {
            ps.setObject(1, UUID.fromString(photoId));
            try (var rs = ps.executeQuery()) {
                assertTrue(rs.next(), "После загрузки в таблице photo должна появиться запись id=" + photoId);
                assertEquals(UUID.fromString(userId), rs.getObject("user_id", UUID.class),
                        "Фото должно быть привязано к пользователю, загрузившему его");
                assertEquals(fileName, rs.getString("file_name"));
                assertEquals("image/png", rs.getString("mime_type"));
                assertTrue(rs.getInt("file_size_bytes") > 0, "Размер файла должен быть больше 0");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Ждём завершения асинхронного анализа (ML-заглушка работает 3-5 секунд)
        Awaitility.await()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> "Анализ завершен".equals(Db.queryValue(
                        "SELECT status FROM photo WHERE id = ?::uuid", photoId).orElse("")));

        // После анализа: статус фото и запись в analysis
        assertEquals("Анализ завершен", Db.queryValue(
                        "SELECT status FROM photo WHERE id = ?::uuid", photoId).orElse(null),
                "Статус фото в БД должен стать 'Анализ завершен'");

        try (var c = Db.connect();
             var ps = c.prepareStatement(
                     "SELECT status, result, completed_at FROM analysis WHERE photo_id = ?::uuid")) {
            ps.setObject(1, UUID.fromString(photoId));
            try (var rs = ps.executeQuery()) {
                assertTrue(rs.next(), "В таблице analysis должна появиться запись по фото id=" + photoId);
                assertEquals("Анализ завершен", rs.getString("status"),
                        "Статус анализа в БД должен быть 'Анализ завершен'");
                assertNotNull(rs.getString("result"), "Результат анализа (result JSONB) должен быть заполнен");
                assertNotNull(rs.getTimestamp("completed_at"), "completed_at должен быть заполнен");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
