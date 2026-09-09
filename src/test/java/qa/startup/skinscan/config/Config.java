package qa.startup.skinscan.config;

/**
 * Конфигурация окружения тестов.
 * Значения читаются из системных свойств, затем из переменных окружения,
 * при отсутствии обоих используются значения по умолчанию (локальный запуск).
 */
public final class Config {

    private Config() {
    }

    /** Базовый URL тестируемого приложения. Свойство base.url / переменная BASE_URL. */
    public static String baseUrl() {
        return System.getProperty("base.url",
                envOrDefault());
    }

    private static String envOrDefault() {
        var value = System.getenv("BASE_URL");
        return (value == null || value.isBlank()) ? "http://localhost:8080" : value;
    }
}
