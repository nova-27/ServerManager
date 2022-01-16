package com.github.nova_27.mcplugin.servermanager.core.events;

import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.utils.Requester;
import net.md_5.bungee.api.plugin.Cancellable;
import net.md_5.bungee.api.plugin.Event;

/**
 * サーバーを起動する直前に呼ばれるイベント
 */
public class ServerPreStartEvent extends Event implements Cancellable {
    private final Server server;
    private final Requester requester;
    private boolean cancelled;

    public ServerPreStartEvent(Server server, Requester requester) {
        this.server = server;
        this.requester = requester;
    }

    public Server getServer() {
        return server;
    }

    public Requester getRequester() {
        return requester;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

}
