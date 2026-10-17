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
 */
public final class Db {

    private Db() {
    }

    /**
     * Выполняет SELECT и возвращает значение первой колонки первой строки (или Optional.empty()).
     */
    public static Optional<String> selectQuery(String query, Object... args) {
        try (Connection connection = open();
             // Скомпилированный SQL запрос с placeholder-ами ?, компилируется драйвером один раз
             PreparedStatement statement = connection.prepareStatement(query)) {

            bind(statement, args);

            // Курсор по строкам результата: next() сдвигает его на следующую строку
            try (ResultSet cursor = statement.executeQuery()) {
                if (cursor.next()) {
                    return Optional.ofNullable(cursor.getString(1));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка запроса к БД: " + query, e);
        }
    }

    /** Открывает соединение с БД по параметрам из {@link Config}. */
    private static Connection open() throws SQLException {
        // DriverManager сам находит драйвер PostgreSQL в classpath
        return DriverManager.getConnection(Config.dbUrl(), Config.dbUser(), Config.dbPassword());
    }

    /** Подставляет значения в placeholder-ы ? (нумерация JDBC начинается с 1). */
    private static void bind(PreparedStatement statement, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1, args[i]);
        }
    }
}
