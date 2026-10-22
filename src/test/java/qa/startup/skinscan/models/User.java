package qa.startup.skinscan.models;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Учётные данные тестового пользователя: логин, пароль, email, телефон, access-токен.
 *
 * @param login       логин (уникален, генерируется случайно)
 * @param password    пароль в открытом виде
 * @param email       email пользователя
 * @param phone       телефон в формате +7XXXXXXXXXX
 * @param accessToken JWT для заголовка Authorization; null, пока пользователь не залогинен
 */
public record User(String login, String password, String email, String phone, String accessToken) {

    /**
     * Учётные данные без токена — для регистрации и логина.
     */
    public User(String login, String password, String email, String phone) {
        this(login, password, email, phone, null);
    }

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
     * Копия пользователя с access-токеном, полученным при логине.
     */
    public User withAccessToken(String token) {
        return new User(login, password, email, phone, token);
    }

    /**
     * Значение заголовка Authorization для JWT-аутентификации.
     * Формат: "Bearer " + accessToken.
     * Используется в UserClient и PhotoClient для доступа к защищённым эндпоинтам.
     */
    public String authorization() {
        if (accessToken == null) {
            throw new IllegalStateException(
                    "У пользователя " + login + " нет access-токена: сначала выполните логин");
        }
        return "Bearer " + accessToken;
    }
}
