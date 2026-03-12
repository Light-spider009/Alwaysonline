package com.alwaysonline;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AlwaysOnlineCommand implements CommandExecutor {

    private final AlwaysOnlinePlugin plugin;

    public AlwaysOnlineCommand(AlwaysOnlinePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("alwaysonline.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sendStatus(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status":
                sendStatus(sender);
                break;

            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "[AlwaysOnline] Configuration reloaded!");
                break;

            case "meminfo":
                sendMemoryInfo(sender);
                break;

            case "tps":
                sendTpsInfo(sender);
                break;

            case "help":
                sendHelp(sender);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /alwaysonline help");
        }

        return true;
    }

    private void sendStatus(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        long maxMemory = runtime.maxMemory() / 1024 / 1024;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDate = sdf.format(new Date(plugin.getStartTime()));

        sender.sendMessage(ChatColor.GOLD + "===== AlwaysOnline Status =====");
        sender.sendMessage(ChatColor.YELLOW + "Status: " + ChatColor.GREEN + "✔ ONLINE (24/7 Active)");
        sender.sendMessage(ChatColor.YELLOW + "Uptime: " + ChatColor.WHITE + plugin.getUptimeString());
        sender.sendMessage(ChatColor.YELLOW + "Started: " + ChatColor.WHITE + startDate);
        sender.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size()
                + "/" + Bukkit.getMaxPlayers());
        sender.sendMessage(ChatColor.YELLOW + "Memory: " + ChatColor.WHITE + usedMemory + "MB / " + maxMemory + "MB");
        sender.sendMessage(ChatColor.GOLD + "===============================");
    }

    private void sendMemoryInfo(CommandSender sender) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        double usagePercent = (double) usedMemory / maxMemory * 100;

        sender.sendMessage(ChatColor.GOLD + "===== Memory Info =====");
        sender.sendMessage(ChatColor.YELLOW + "Used:  " + ChatColor.WHITE + usedMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Free:  " + ChatColor.WHITE + freeMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Total: " + ChatColor.WHITE + totalMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Max:   " + ChatColor.WHITE + maxMemory + " MB");
        sender.sendMessage(ChatColor.YELLOW + "Usage: "
                + (usagePercent > 85 ? ChatColor.RED : ChatColor.GREEN)
                + String.format("%.1f", usagePercent) + "%");
        sender.sendMessage(ChatColor.GOLD + "=======================");
    }

    private void sendTpsInfo(CommandSender sender) {
        try {
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);

            double tps1m = Math.min(20.0, recentTps[0]);
            double tps5m = Math.min(20.0, recentTps[1]);
            double tps15m = Math.min(20.0, recentTps[2]);

            sender.sendMessage(ChatColor.GOLD + "===== TPS Info =====");
            sender.sendMessage(ChatColor.YELLOW + "1m:  " + getTpsColor(tps1m) + String.format("%.2f", tps1m));
            sender.sendMessage(ChatColor.YELLOW + "5m:  " + getTpsColor(tps5m) + String.format("%.2f", tps5m));
            sender.sendMessage(ChatColor.YELLOW + "15m: " + getTpsColor(tps15m) + String.format("%.2f", tps15m));
            sender.sendMessage(ChatColor.GOLD + "====================");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Unable to retrieve TPS data.");
        }
    }

    private ChatColor getTpsColor(double tps) {
        if (tps >= 18) return ChatColor.GREEN;
        if (tps >= 15) return ChatColor.YELLOW;
        return ChatColor.RED;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "===== AlwaysOnline Commands =====");
        sender.sendMessage(ChatColor.YELLOW + "/alwaysonline status " + ChatColor.WHITE + "- Show server status");
        sender.sendMessage(ChatColor.YELLOW + "/alwaysonline meminfo " + ChatColor.WHITE + "- Show memory usage");
        sender.sendMessage(ChatColor.YELLOW + "/alwaysonline tps " + ChatColor.WHITE + "- Show TPS info");
        sender.sendMessage(ChatColor.YELLOW + "/alwaysonline reload " + ChatColor.WHITE + "- Reload config");
        sender.sendMessage(ChatColor.YELLOW + "/alwaysonline help " + ChatColor.WHITE + "- Show this help");
        sender.sendMessage(ChatColor.GOLD + "=================================");
    }
}
