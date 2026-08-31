package qa.startup.skinscan.models;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

/**
 * Учётные данные тестового пользователя.
 */
public record TestUser(String login, String password) {

    public static TestUser random() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new TestUser("qa_user_" + suffix, "Qa_" + suffix + "_Pass");
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
