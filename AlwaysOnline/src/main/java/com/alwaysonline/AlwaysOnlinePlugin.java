package com.alwaysonline;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Logger;

public class AlwaysOnlinePlugin extends JavaPlugin {

    private static final Logger log = Bukkit.getLogger();
    private BukkitTask keepAliveTask;
    private BukkitTask watchdogTask;
    private long startTime;

    @Override
    public void onEnable() {
        startTime = System.currentTimeMillis();

        // Save default config
        saveDefaultConfig();

        // Register commands
        getCommand("alwaysonline").setExecutor(new AlwaysOnlineCommand(this));
        getCommand("alwaysonline").setTabCompleter(new AlwaysOnlineTabCompleter());

        // Register events
        getServer().getPluginManager().registerEvents(new ServerEventListener(this), this);

        // Start keep-alive task (runs every 5 minutes)
        startKeepAliveTask();

        // Start watchdog task (runs every 30 seconds)
        startWatchdogTask();

        log.info("[AlwaysOnline] Plugin enabled! Your server will stay online 24/7.");
        log.info("[AlwaysOnline] Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        if (keepAliveTask != null) keepAliveTask.cancel();
        if (watchdogTask != null) watchdogTask.cancel();
        log.info("[AlwaysOnline] Plugin disabled.");
    }

    private void startKeepAliveTask() {
        long intervalTicks = getConfig().getLong("keep-alive-interval-minutes", 5) * 60 * 20L;

        keepAliveTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (getConfig().getBoolean("log-keep-alive", false)) {
                log.info("[AlwaysOnline] Keep-alive ping. Server uptime: " + getUptimeString());
            }
            // Perform lightweight server health checks
            performHealthCheck();
        }, intervalTicks, intervalTicks);
    }

    private void startWatchdogTask() {
        // Watchdog runs every 30 seconds to monitor server health
        watchdogTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
            monitorServerHealth();
        }, 600L, 600L); // 600 ticks = 30 seconds
    }

    private void performHealthCheck() {
        // Check available memory
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
        double memUsagePercent = (double) usedMemory / maxMemory * 100;

        if (getConfig().getBoolean("warn-high-memory", true) && memUsagePercent > 85) {
            log.warning("[AlwaysOnline] High memory usage: " + String.format("%.1f", memUsagePercent)
                    + "% (" + usedMemory + "MB / " + maxMemory + "MB)");

            // Suggest GC if memory is critically high
            if (memUsagePercent > 95) {
                log.warning("[AlwaysOnline] Critical memory! Requesting garbage collection...");
                System.gc();
            }
        }
    }

    private void monitorServerHealth() {
        // Check TPS (Ticks Per Second) via server
        double tps = getTPS();
        if (tps < 15.0 && getConfig().getBoolean("warn-low-tps", true)) {
            log.warning("[AlwaysOnline] Low TPS detected: " + String.format("%.2f", tps)
                    + ". Server may be lagging.");
        }
    }

    private double getTPS() {
        try {
            // Use reflection to get TPS from CraftServer
            Object server = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
            double[] recentTps = (double[]) server.getClass().getField("recentTps").get(server);
            return Math.min(20.0, recentTps[0]);
        } catch (Exception e) {
            return 20.0; // Default to 20 TPS if unable to retrieve
        }
    }

    public String getUptimeString() {
        long uptimeMs = System.currentTimeMillis() - startTime;
        long seconds = (uptimeMs / 1000) % 60;
        long minutes = (uptimeMs / (1000 * 60)) % 60;
        long hours = (uptimeMs / (1000 * 60 * 60)) % 24;
        long days = uptimeMs / (1000 * 60 * 60 * 24);

        if (days > 0) return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
        if (hours > 0) return hours + "h " + minutes + "m " + seconds + "s";
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }

    public long getStartTime() {
        return startTime;
    }
}
