package fr.panncake.pjavaorm.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConnectionManager {
    private static ConnectionManager instance;
    private Connection connection;

    private final String url;
    private final Properties props;

    private ConnectionManager(
            String url,
            String user,
            String password
    ) {
        this.url = url;
        this.props = new Properties();

        if (user != null) this.props.setProperty("user", user);
        if (password != null) this.props.setProperty("password", password);

        this.props.setProperty("useLegacyDatetimeCode", "false");
        this.props.setProperty("serverTimezone", "UTC");
    }

    public static void initialize(
            String url,
            String user,
            String password
    ) {
        if (instance != null) {
            throw new IllegalStateException("Already initialized");
        }

        instance = new ConnectionManager(url, user, password);
    }

    public static Connection getConnection() throws SQLException {
        if (instance == null) {
            throw new IllegalStateException("Not initialized. Call initialize() first.");
        }

        if (instance.connection == null || instance.connection.isClosed()) {
            instance.connection = DriverManager.getConnection(instance.url, instance.props);
        }

        return instance.connection;
    }
}
