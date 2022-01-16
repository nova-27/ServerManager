package com.github.nova_27.mcplugin.servermanager.core.utils;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.UUID;

public class Requester {
    private final Type type;
    private final UUID uuid;
    private final String name;
    private final Object object;

    public Requester(Type type, UUID uuid, String name, Object object) {
        this.type = type;
        this.uuid = uuid;
        this.name = name;
        this.object = object;
    }

    public Type getType() {
        return type;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Object getObject() {
        return object;
    }

    public static Requester of(ProxiedPlayer player) {
        return new Requester(Type.PLAYER, player.getUniqueId(), player.getName(), player);
    }

    public static Requester of(CommandSender sender) {
        if (sender instanceof ProxiedPlayer)
            return of(((ProxiedPlayer) sender));
        return of(Type.CONSOLE, sender);
    }

    public static Requester of(Plugin plugin) {
        return new Requester(Type.PLUGIN, null, plugin.getDescription().getName(), plugin);
    }

    public static Requester of(Server server) {
        return new Requester(Type.PLUGIN_MESSAGE, null, server.getInfo().getName(), server);
    }

    public static Requester of(Type type) {
        return new Requester(type, null, null, null);
    }

    public static Requester of(Type type, Object object) {
        return new Requester(type, null, null, object);
    }


    public enum Type {
        PLAYER, CONSOLE, PLUGIN, PLUGIN_MESSAGE, UNKNOWN
    }

}
