package me.kirillirik.top;

import me.kirillirik.top.manager.TopManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TopPlugin extends JavaPlugin {

    private TopManager topManager;

    @Override
    public void onEnable() {
        topManager = new TopManager(this, getConfig());
        topManager.onLoad();
    }

    @Override
    public void onDisable() {
        topManager.onDisable();
    }
}
