package me.kirillirik.top.manager;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.util.Vector;

import java.util.List;

public final class TopPlace {

    private final Location location;
    private boolean generated;

    public TopPlace(Location location) {
        this.location = location;
        generated = false;
    }

    /**
     * Генерация постамента
     */
    public void generate() {
        final Location cursor = location.clone();

        for (int i = 0; i < 3; i++) {
            cursor.getBlock().setType(Material.YELLOW_CONCRETE_POWDER);
            cursor.add(0, 1, 0);
        }

        cursor.add(1, -3, 0);
        for (int i = 0; i < 2; i++) {
            cursor.getBlock().setType(Material.GREEN_CONCRETE_POWDER);
            cursor.add(0, 1, 0);
        }

        cursor.add(-2, -2, 0);
        cursor.getBlock().setType(Material.RED_CONCRETE_POWDER);
        generated = true;
    }

    /**
     * @return true если постамент сгенерирован
     */
    public boolean isGenerated() {
        return generated;
    }

    /**
     * Набор векторов смещения
     */
    private static final List<Vector> offsets = List.of(
            new Vector(0, 2, -1),
            new Vector(1, -1, 0),
            new Vector(-2, -1, 0));

    /**
     * Метод обновляют информацию на постаменте
     * @param players список лучших игроков
     */
    public void update(List<Pair<String, Integer>> players) {
        final Location cursor = location.clone();

        for (int i = 0; i < players.size(); i++) {
            final Pair<String, Integer> pair = players.get(i);
            cursor.add(offsets.get(i));
            updatePlayer(cursor.clone(), pair.first(), pair.second());
        }
    }

    /**
     * Метод обновляет информацию для конкретного места на постаменте
     * @param cursor локация таблички постамента
     * @param name имя игрока
     * @param point количество очков игрока
     */
    private void updatePlayer(Location cursor, String name, int point) {
        final Block block = cursor.getBlock();
        if (block.getType() != Material.OAK_WALL_SIGN) block.setType(Material.OAK_WALL_SIGN);

        final WallSign wallSign = (WallSign) block.getBlockData();
        wallSign.setFacing(BlockFace.NORTH);
        block.setBlockData(wallSign);

        final Sign sign = (Sign) block.getState();
        sign.line(0, Component.text(name));
        sign.line(1, Component.text(point));
        sign.update();

        cursor.add(0, 1, 1);
        final Block headBlock = cursor.getBlock();
        headBlock.setType(Material.PLAYER_HEAD);

        final Skull skull = (Skull) headBlock.getState();
        skull.setOwningPlayer(Bukkit.getOfflinePlayer(name));
        skull.update();
    }
}
