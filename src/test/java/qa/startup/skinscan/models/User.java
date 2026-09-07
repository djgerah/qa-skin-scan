package qa.startup.skinscan.models;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Учётные данные тестового пользователя.
 */
public record User(String login, String password, String email, String phone) {

    public static User getRandomUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new User("user_" + suffix, suffix + "_password", suffix + "@skin-scan.ru", getRandomPhone());
    }

    public static String getRandomPhone() {
        long number = ThreadLocalRandom.current().nextLong(900_000_0000L, 1_000_000_0000L);
        return "+7" + number;
    }

    public String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (login + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    public String authParam() {
        return Base64.getEncoder().encodeToString(
                (login + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
