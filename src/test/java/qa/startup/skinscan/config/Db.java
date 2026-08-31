package qa.startup.skinscan.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;

/**
 * JDBC-доступ к той же БД, которую использует тестируемое приложение.
 * <p>
 * Параметры подключения по умолчанию берутся из classpath-ресурса connectionBD.properties
 * (тот же файл, что читает приложение), их можно переопределить системными свойствами:
 * -Ddb.url, -Ddb.user, -Ddb.password.
 */
public final class Db {

    private Db() {
    }

    public static String url() {
        return System.getProperty("db.url", config().getProperty("spring.datasource.url"));
    }

    public static String user() {
        return System.getProperty("db.user", config().getProperty("spring.datasource.username"));
    }

    public static String password() {
        return System.getProperty("db.password", config().getProperty("spring.datasource.password"));
    }

    public static Connection connect() {
        try {
            return DriverManager.getConnection(url(), user(), password());
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Не удалось подключиться к БД (" + url() + "). "
                            + "Проверьте, что PostgreSQL запущен и параметры совпадают с connectionBD.properties",
                    e);
        }
    }

    /**
     * Выполняет запрос и возвращает первую колонку первой строки, если строка найдена.
     */
    public static Optional<Object> queryValue(String sql, Object... params) {
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.ofNullable(rs.getObject(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка выполнения запроса: " + sql, e);
        }
    }

    private static Properties config() {
        Properties props = new Properties();
        try (InputStream in = Db.class.getClassLoader().getResourceAsStream("connectionBD.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать connectionBD.properties", e);
        }
        return props;
    }
}
