package com.github.nova_27.mcplugin.servermanager.core.socket;

import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionThread;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketEventListener;
import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.events.ServerEvent;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import com.github.nova_27.mcplugin.servermanager.core.utils.Tools;
import net.md_5.bungee.api.chat.TextComponent;

import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ClientConnection extends ConnectionThread {
    Server srcServer;

    /**
     * コンストラクタ
     *
     * @param connection 接続先socket
     * @param pel        パケットイベントリスナ―
     */
    public ClientConnection(Socket connection, PacketEventListener pel) {
        super(connection, pel);
    }

    public void setSrcServer(Server ClientID) {
        this.srcServer = ClientID;
    }

    public Server getSrcServer() {
        return srcServer;
    }

    @Override
    public void finalProgress() {
        Smfb_core.getInstance().getProxy().getScheduler().schedule(Smfb_core.getInstance(), ()->{
            try {
                srcServer.Process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            srcServer.Process.destroy();

            getSrcServer().Started = false;
            getSrcServer().Switching = false;

            Smfb_core.getInstance().log(Tools.Formatter(Messages.ServerStopped_log.toString(), srcServer.Name));
            Smfb_core.getInstance().getProxy().broadcast(new TextComponent(Tools.Formatter(Messages.ServerStopped_minecraft.toString(), srcServer.Name)));
            Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new ServerEvent(srcServer, ServerEvent.EventType.ServerStopped));
        }, 0L, TimeUnit.SECONDS);
    }
}
