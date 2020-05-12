package com.github.nova_27.mcplugin.servermanager.bukkit.listener;

import com.github.nova_27.mcplugin.servermanager.bukkit.SFMB_BukkitBridge;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.nio.ByteBuffer;

public class SpigotListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        byte[] count = ByteBuffer.allocate(4).putInt(Bukkit.getOnlinePlayers().size()).array();
        SFMB_BukkitBridge.getInstance().connectionThread.addQueue(PacketID.PlayerCountResponse, count);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        byte[] count = ByteBuffer.allocate(4).putInt(Bukkit.getOnlinePlayers().size()).array();
        SFMB_BukkitBridge.getInstance().connectionThread.addQueue(PacketID.PlayerCountResponse, count);
    }
}
