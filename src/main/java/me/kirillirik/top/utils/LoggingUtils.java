package me.kirillirik.top.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

public final class LoggingUtils {

    private static final ConsoleCommandSender sender = Bukkit.getConsoleSender();

    public static void info(String message) {
        sender.sendMessage(ChatColor.GREEN + message);
    }

    public static void error(String message) {
        sender.sendMessage(ChatColor.RED + message);
    }
}
