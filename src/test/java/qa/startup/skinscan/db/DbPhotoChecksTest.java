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
import java.util.UUID;

@Tag("db")
class DbPhotoChecksTest {

    private static final String ANALYZED = "Анализ завершен";

    @Test
    @DisplayName("После загрузки фото в БД ему присваивается статус «Анализ завершен»")
    void photoStatusReachesAnalyzed() {
        var user = AuthClient.getRegisteredUser();
        var pic = new Picture();

        String photoId = Allure.step("Загрузка фото [" + pic.name + "]", () ->
                PhotoClient.upload(user, pic)
                        .then()
                        .statusCode(202)
                        .extract()
                        .jsonPath().getString(""));

        Allure.step("Ожидание завершения анализа", (step) -> {
            Awaitility.await()
                    .alias("Статус фото " + photoId + " в БД не дошёл до «" + ANALYZED + "»")
                    .atMost(Duration.ofSeconds(60))
                    .pollInterval(Duration.ofSeconds(1))
                    .until(() -> ANALYZED.equals(currentStatus(photoId)));
        });
    }

    /**
     * Читает текущий статус фото из БД.
     * id — первичный ключ, поэтому запрос возвращает не более одной строки: результат — строка, а не список.
     * null означает, что фото ещё не появилось в БД, — ожидание продолжается.
     */
    private static String currentStatus(String photoId) {
        return Db.jdbc().query(
                "SELECT status FROM photo WHERE id = ?",
                resultSet -> resultSet.next() ? resultSet.getString("status") : null,
                UUID.fromString(photoId));
    }
}
