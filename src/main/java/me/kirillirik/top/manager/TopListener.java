package me.kirillirik.top.manager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class TopListener implements Listener {

    private final TopManager topManager;

    public TopListener(TopManager topManager) {
        this.topManager = topManager;
    }

    /**
     * При убийстве добавляем игроку одно очко
     */
    @EventHandler(ignoreCancelled = true)
    private void onPlayerKillPlayer(PlayerDeathEvent event) {
        final Player killer = event.getPlayer().getKiller();
        if (killer == null) return;

        topManager.addPoints(killer.getName(), 1);
    }
}
