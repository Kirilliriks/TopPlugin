package me.kirillirik.top.database;

import it.unimi.dsi.fastutil.Pair;
import me.kirillirik.top.utils.LoggingUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TopDatabase extends HikariDatabase {

    private final String tableName;
    private final String playerColumnName;
    private final String pointColumnName;

    public TopDatabase(FileConfiguration configuration) {
        super(configuration);

        this.tableName = configuration.getString("table_name");
        this.playerColumnName = configuration.getString("player_column_name");
        this.pointColumnName = configuration.getString("point_column_name");
    }

    @Override
    public void create() {
        execute("CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                "  `" + playerColumnName + "` text(16) NOT NULL," +
                "  `" + pointColumnName + "` int NOT NULL," +
                "  PRIMARY KEY (`" + playerColumnName + "`(16))" +
                ");");
    }

    /**
     * Метод для сохранения информации об игроке
     * @param name имя игрока
     * @param points количество очков игрока
     */
    public void savePlayerPoints(String name, int points) {
        execute("INSERT INTO `" + tableName + "`(`" + playerColumnName + "`, `" + pointColumnName + "`)" +
                " VALUES (\"" + name + "\", " + points + ") " +
                "ON DUPLICATE KEY UPDATE `" + pointColumnName + "`=" + points);
    }

    /**
     * Асинхронное получение списка лучших игроков
     * @param limit необходимое количество игроков
     * @return экземпляр CompletableFuture
     */
    public CompletableFuture<List<Pair<String, Integer>>> asyncGetTop(int limit) {
        return CompletableFuture.supplyAsync(()-> {
            try (final Connection connection = database.getConnection();
                 final PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM `" + tableName +
                         "` ORDER BY `" + pointColumnName +
                         "` DESC LIMIT " + limit)) {

                final List<Pair<String, Integer>> list = new ArrayList<>();
                final ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    list.add(Pair.of(resultSet.getString(1), resultSet.getInt(2)));
                }

                return list;
            } catch (SQLException e) {
                LoggingUtils.error("Ошибка при загрузке информации об лучших игроках");
                throw new RuntimeException(e);
            }
        }, EXECUTOR_SERVICE);
    }

    /**
     * Асинхронное получение информации об игроке
     * @param name имя игрока
     * @return если информация есть, то экземпляр CompletableFuture, иначе Null
     */
    public CompletableFuture<Pair<String, Integer>> asyncGetPlayerPoints(String name) {
        return CompletableFuture.supplyAsync(()-> {
            try (final Connection connection = database.getConnection();
                 final PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM `" + tableName +
                         "` WHERE `" + pointColumnName + "`=\"" + name + "\"")) {

                final ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) return null;

                return Pair.of(resultSet.getString(1), resultSet.getInt(2));
            } catch (SQLException e) {
                LoggingUtils.error("Ошибка при загрузке информации об игроке");
                throw new RuntimeException(e);
            }
        }, EXECUTOR_SERVICE);
    }
}
