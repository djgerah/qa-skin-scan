package qa.startup.skinscan.db;

import qa.startup.skinscan.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Тонкий JDBC-хелпер для проверок состояния БД из тестов.
 * Подключение — к той же БД, что использует тестируемое приложение (см. {@link Config}).
 */
public final class Db {

    private Db() {
    }

    /** Выполняет SELECT и возвращает значение первой колонки первой строки (или Optional.empty()). */
    public static Optional<String> selectQuery(String query, Object... args) {
        try (Connection connection = open();
             PreparedStatement statement = connection.prepareStatement(query)) {

            bind(statement, args);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString(1));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка запроса к БД: " + query, e);
        }
    }

    private static Connection open() throws SQLException {
        return DriverManager.getConnection(Config.dbUrl(), Config.dbUser(), Config.dbPassword());
    }

    private static void bind(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
