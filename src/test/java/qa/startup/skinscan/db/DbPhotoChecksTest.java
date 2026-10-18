package qa.startup.skinscan.db;

import io.qameta.allure.Allure;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import qa.startup.skinscan.clients.AuthClient;
import qa.startup.skinscan.clients.PhotoClient;
import qa.startup.skinscan.testdata.Picture;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверки таблицы photo напрямую через БД (Spring JdbcTemplate).
 * Статусы в БД хранятся в виде русских подписей (StatusPhoto).
 */
@Tag("db")
class DbPhotoChecksTest {

    private static final String ANALYZED = "Анализ завершен";

    @Test
    @DisplayName("После загрузки фото статус в БД доходит до «Анализ завершен»")
    void photoStatusReachesAnalyzed() {
        var user = AuthClient.getRegisteredUser();

        String photoId = PhotoClient.upload(user, Picture.uniqueName(), Picture.PNG_1X1, Picture.mimeType)
                .then()
                .statusCode(202)
                .extract()
                .jsonPath().getString("");

        Set<String> observed = new LinkedHashSet<>();

        Allure.step("Ожидание статуса «" + ANALYZED + "» в БД (до 60 секунд)", (step) -> {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(60))
                    .pollInterval(Duration.ofSeconds(1))
                    .until(() -> {
                        var statuses = Db.jdbc().queryForList(
                                "SELECT status FROM photo WHERE id = ?",
                                String.class,
                                UUID.fromString(photoId));
                        observed.addAll(statuses);
                        return statuses.contains(ANALYZED);
                    });
        });

        assertTrue(observed.contains(ANALYZED),
                "Финальный статус не «" + ANALYZED + "». Наблюдаемые статусы: " + observed);
    }
}
