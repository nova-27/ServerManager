package com.github.nova_27.mcplugin.servermanager.bukkit.listener;

import com.github.nova_27.mcplugin.servermanager.bukkit.Smfb_bukkitbridge;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.nio.ByteBuffer;
import java.util.Objects;

public class SpigotListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        byte[] count = ByteBuffer.allocate(4).putInt(Bukkit.getOnlinePlayers().size()).array();
        Smfb_bukkitbridge.getInstance().connectionThread.addQueue(PacketID.PlayerCountResponse, count);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        byte[] count = ByteBuffer.allocate(4).putInt(Bukkit.getOnlinePlayers().size() - 1).array();
        Smfb_bukkitbridge.getInstance().connectionThread.addQueue(PacketID.PlayerCountResponse, count);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();
        if (Objects.equals(command, "/reload")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "reload command has been disabled by SMFB.");
        }
    }

    @EventHandler
    public void onConsoleCommand(ServerCommandEvent event) {
        String command = event.getCommand().toLowerCase();
        if (Objects.equals(command, "reload")) {
            event.setCancelled(true);
            event.getSender().sendMessage(ChatColor.DARK_RED + "reload command has been disabled by SMFB.");
        }
    }
}
