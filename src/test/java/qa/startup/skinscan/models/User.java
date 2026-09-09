package qa.startup.skinscan.models;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Учётные данные тестового пользователя: логин, пароль, email, телефон.
 *
 * @param login    логин (уникален, генерируется случайно)
 * @param password пароль в открытом виде
 * @param email    email пользователя
 * @param phone    телефон в формате +7XXXXXXXXXX
 */
public record User(String login, String password, String email, String phone) {

    /**
     * Создаёт пользователя со случайными данными.
     * Логин, пароль и email получают суффикс из 8 символов UUID,
     * чтобы повторные вызовы гарантированно не конфликтовали
     * с уже существующими пользователями на сервере.
     */
    public static User getRandomUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return new User("user_" + suffix, suffix + "_password", suffix + "@skin-scan.ru", getRandomPhone());
    }

    /**
     * Генерирует случайный российский мобильный номер:
     * +7 и 10 цифр, первая — из диапазона 9xx.
     */
    public static String getRandomPhone() {
        long number = ThreadLocalRandom.current().nextLong(900_000_0000L, 1_000_000_0000L);
        return "+7" + number;
    }

    /**
     * Значение заголовка Authorization для HTTP Basic-аутентификации (RFC 7617).
     * Формат: "Basic " + Base64("логин:пароль").
     * Используется в UserClient и PhotoClient для доступа к защищённым эндпоинтам.
     */
    public String basicAuth() {
        return "Basic " + authParam();
    }

    /**
     * Учётные данные "логин:пароль", закодированные в Base64, — без префикса "Basic ".
     * Используется как значение query-параметра "auth" в AuthClient.login()
     * (эндпоинт /skinScan/login ожидает чистый Base64, а не заголовок).
     */
    public String authParam() {
        return Base64.getEncoder().encodeToString(
                (login + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
