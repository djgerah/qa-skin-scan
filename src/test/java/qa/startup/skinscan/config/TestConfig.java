package qa.startup.skinscan.config;

public final class TestConfig {
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    public static String baseUrl() {
        return System.getProperty("baseUrl", DEFAULT_BASE_URL);
    }
}
