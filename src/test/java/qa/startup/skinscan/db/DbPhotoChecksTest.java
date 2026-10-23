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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("db")
class DbPhotoChecksTest {

    private static final String ANALYZED = "Анализ завершен";

    @Test
    @DisplayName("После загрузки фото в БД ему присваивается статус «Анализ завершен»")
    void photoStatusReachesAnalyzed() {
        var user = AuthClient.getRegisteredUser();
        var pic = new Picture();

        String photoId = PhotoClient.upload(user, pic)
                .then()
                .statusCode(202)
                .extract()
                .jsonPath().getString("");

        Set<String> observed = new LinkedHashSet<>();

        Allure.step("Ожидание статуса «" + ANALYZED + "»", (step) -> {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(60))
                    .pollInterval(Duration.ofSeconds(1))
                    .until(() -> {
                        List<String> statuses = Db.jdbc().queryForList(
                                "SELECT status FROM photo WHERE id = ?",
                                String.class,
                                UUID.fromString(photoId));

                        observed.addAll(statuses);

                        return statuses.contains(ANALYZED);
                    });
        });

        assertTrue(observed.contains(ANALYZED));
    }
}
