package me.kirillirik.top.manager;

import it.unimi.dsi.fastutil.Pair;
import me.kirillirik.top.database.TopDatabase;
import me.kirillirik.top.utils.LoggingUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TopManager implements Manager {

    private final Plugin plugin;
    private final FileConfiguration configuration;
    private final Map<String, Integer> players;
    private final List<String> places;

    private TopDatabase topDatabase;
    private long lastSave;
    private long lastUpdate;

    public TopManager(Plugin plugin, FileConfiguration configuration) {
        this.plugin = plugin;
        this.configuration = configuration;
        topDatabase = null;

        players = new HashMap<>();
        places = new ArrayList<>();

        Bukkit.getPluginManager().registerEvents(new TopListener(this), plugin);
    }

    /**
     * Метод кеширует количество очков игрока
     * если игрок уже имеет очки то асинхронно получает информацию об этом
     * из базы данных и суммирует очки
     * @param name имя игрока
     * @param amount количество добавляемых очков
     */
    public void addPoints(String name, int amount) {
        if (!players.containsKey(name)) {
            topDatabase.asyncGetPlayerPoints(name).whenComplete((pair, exception) -> {
                if (exception != null) {
                    LoggingUtils.error(exception.getMessage());
                    for (final StackTraceElement element : exception.getStackTrace()) {
                        LoggingUtils.error(element.toString());
                    }
                    return;
                }

                if (pair == null) {
                    pair = Pair.of(name, 0);
                }

                players.put(name, pair.second() + amount);
            });
            return;
        }
        players.put(name, players.get(name) + amount);
    }

    /**
     * Метод обновления вызывается раз в 20 тиков
     * в зависимости от прошедшего времени либо отправляет кешированную
     * информацию в базу данных, либо обновляет информацию на постаментах
     */
    public void update() {
        final int saveSeconds = (int) ((System.currentTimeMillis() - lastSave) / 1000);
        final int updateSeconds = (int) ((System.currentTimeMillis() - lastUpdate) / 1000);
        if (saveSeconds >= 30) {
            savePoints();
            LoggingUtils.info("Save points");
        }
        if (updateSeconds <= 60) return;

        updatePlaces();
        LoggingUtils.info("Update places");
    }

    /**
     * Метод который асинхронно обновляет информацию на всех постаментах
     */
    private void updatePlaces() {
        lastUpdate = System.currentTimeMillis();
        topDatabase.asyncGetTop(3).whenComplete((list, exception) -> {
            if (exception != null) {
                LoggingUtils.error(exception.getMessage());
                for (final StackTraceElement element : exception.getStackTrace()) {
                    LoggingUtils.error(element.toString());
                }
                return;
            }

            if (list == null) {
                LoggingUtils.error("Не загружен набор игроков ");
                return;
            }

            for (final String string : places) {
                final String[] split = string.split(":");

                final Location location = new Location(Bukkit.getWorld(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2]),
                        Double.parseDouble(split[3]));

                final TopPlace topPlace = new TopPlace(location);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!topPlace.isGenerated()) topPlace.generate();
                    topPlace.update(list);
                });
            }
        });
    }

    /**
     * Метод, который отправляет кешированную информацию об игроках
     * в базу данных
     */
    private void savePoints() {
        lastSave = System.currentTimeMillis();
        for (final Map.Entry<String, Integer> pair : players.entrySet()) {
            topDatabase.savePlayerPoints(pair.getKey(), pair.getValue());
        }
    }

    @Override
    public void onLoad() {
        topDatabase = new TopDatabase(configuration);
        topDatabase.create();

        places.addAll(configuration.getStringList("places"));
        if (places.isEmpty()) {
            LoggingUtils.error("Не найдены локации постаментов в config.yml");
            return;
        }

        updatePlaces();

        Bukkit.getScheduler().runTaskTimer(plugin, this::update, 20L, 20L);
    }

    @Override
    public void onDisable() {
        savePoints();
    }
}
