package qa.startup.skinscan.config;

/**
 * Конфигурация окружения тестов.
 * Значения читаются из системных свойств, затем из переменных окружения,
 * при отсутствии обоих используются значения по умолчанию.
 */
public final class Config {

    private Config() {
    }

    /** Базовый URL тестируемого приложения. Свойство base.url / переменная BASE_URL. */
    public static String baseUrl() {
        return System.getProperty("base.url",
                envOrDefault("BASE_URL", "http://localhost:8080"));
    }

    /** JDBC URL тестовой БД. Свойство db.url / переменная DB_URL. */
    public static String dbUrl() {
        return System.getProperty("db.url",
                envOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/skinScan_db"));
    }

    /** Пользователь БД. Свойство db.user / переменная DB_USER. */
    public static String dbUser() {
        return System.getProperty("db.user",
                envOrDefault("DB_USER", "skinScan_user"));
    }

    /** Пароль БД. Свойство db.password / переменная DB_PASSWORD. */
    public static String dbPassword() {
        return System.getProperty("db.password",
                envOrDefault("DB_PASSWORD", "password"));
    }

    /** Значение переменной окружения по имени, а если она не задана — значение по умолчанию. */
    private static String envOrDefault(String name, String defaultValue) {
        var environmentValue = System.getenv(name);
        return (environmentValue == null || environmentValue.isBlank()) ? defaultValue : environmentValue;
    }
}
