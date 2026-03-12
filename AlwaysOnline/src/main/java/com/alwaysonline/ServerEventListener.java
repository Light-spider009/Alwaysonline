package com.alwaysonline;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.logging.Logger;

public class ServerEventListener implements Listener {

    private final AlwaysOnlinePlugin plugin;
    private static final Logger log = Bukkit.getLogger();

    public ServerEventListener(AlwaysOnlinePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Intercept /stop and /restart commands to optionally warn or block them.
     * Useful for preventing accidental shutdowns.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onServerCommand(ServerCommandEvent event) {
        String command = event.getCommand().trim().toLowerCase();

        // Check if the command is a shutdown command
        if (command.equals("stop") || command.equals("shutdown")) {
            if (plugin.getConfig().getBoolean("block-stop-command", false)) {
                event.setCancelled(true);
                log.warning("[AlwaysOnline] /stop command was BLOCKED by AlwaysOnline plugin!");
                log.warning("[AlwaysOnline] Set block-stop-command: false in config to allow shutdown.");
            } else if (plugin.getConfig().getBoolean("warn-on-stop", true)) {
                log.warning("[AlwaysOnline] WARNING: Server is shutting down! Uptime was: "
                        + plugin.getUptimeString());
                log.warning("[AlwaysOnline] If this was unintentional, restart the server process.");
            }
        }
    }

    /**
     * Notify admins when a player joins, showing server uptime.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getConfig().getBoolean("show-uptime-on-join", false)) {
            if (event.getPlayer().hasPermission("alwaysonline.admin")) {
                event.getPlayer().sendMessage(
                        ChatColor.GOLD + "[AlwaysOnline] " + ChatColor.YELLOW
                                + "Server Uptime: " + ChatColor.WHITE + plugin.getUptimeString()
                );
            }
        }
    }

    /**
     * Log player quit for server monitoring.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Log when server becomes empty (useful for monitoring)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                if (plugin.getConfig().getBoolean("log-empty-server", false)) {
                    log.info("[AlwaysOnline] Server is now empty. Staying online 24/7 as configured.");
                }
            }
        }, 1L);
    }
}
