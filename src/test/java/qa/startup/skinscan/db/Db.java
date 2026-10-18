package qa.startup.skinscan.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import qa.startup.skinscan.config.Config;

/**
 * Точка доступа к БД тестируемого приложения.
 * JDBC (Java Database Connectivity) — стандартный API Java для работы с реляционными БД
 * (пакет java.sql).
 * Это набор интерфейсов:
 * (Connection) — соединение,
 * (PreparedStatement) — запрос с параметрами,
 * (ResultSet) — курсор по строкам результата).
 */
public final class Db {
    /**
     * JdbcTemplate потокобезопасен и рассчитан на переиспользование, поэтому создаётся
     * один раз, а не на каждый вызов.
     */
    private static final JdbcTemplate JDBC = new JdbcTemplate(dataSource());

    private Db() {
    }

    /**
     * Возвращает JdbcTemplate, настроенный на БД тестируемого приложения.
     */
    public static JdbcTemplate jdbc() {
        return JDBC;
    }

    /**
     * Создаёт источник соединений (DataSource) по параметрам из Config.java.
     * DriverManagerDataSource открывает новое физическое соединение на каждый запрос.
     */
    private static DriverManagerDataSource dataSource() {
        var dataSource = new DriverManagerDataSource();
        dataSource.setUrl(Config.dbUrl());
        dataSource.setUsername(Config.dbUser());
        dataSource.setPassword(Config.dbPassword());
        return dataSource;
    }
}
