package me.kirillirik.top.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class HikariDatabase implements Database {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(5);

    protected static final String MYSQL_URL = "jdbc:mysql://%s:%s/%s";

    protected final HikariConfig config;
    protected final HikariDataSource database;

    protected final String name;

    public HikariDatabase(FileConfiguration configuration) {
        final ConfigurationSection section = configuration.getConfigurationSection("database");
        if (section == null) {
            throw new RuntimeException("Необходима информация о базе данных в config.yml");
        }

        name = section.getString("name");

        final String URL = String.format(MYSQL_URL, section.getString("host"), section.getString("port"), name);
        config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(section.getString("username"));
        config.setPassword(section.getString("password"));
        config.addDataSourceProperty("databaseName", name);
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("useUnicode", "true");

        database = new HikariDataSource(config);
    }

    @Override
    public abstract void create();

    @Override
    public void execute(String sql) {
        try (final Connection connection = database.getConnection();
             final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось выполнить запрос для базы данных " + name + " " + sql + " " + exception);
        }
    }
}
