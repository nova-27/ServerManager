package com.github.nova_27.mcplugin.servermanager.core;

import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionThread;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketEventListener;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigGetter;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.listener.BungeeListener;
import com.github.nova_27.mcplugin.servermanager.core.listener.BungeeMinecraftCommand;
import com.github.nova_27.mcplugin.servermanager.core.socket.ClientConnection;
import com.github.nova_27.mcplugin.servermanager.core.socket.SocketServer;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import com.github.nova_27.mcplugin.servermanager.core.utils.Tools;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;

import static com.github.nova_27.mcplugin.servermanager.core.config.ConfigData.Lobby;

public final class Smfb_core extends Plugin implements PacketEventListener {

    private static Smfb_core instance;
    private Locale defaultLocale;

    private SocketServer socketServer;

    /**
     * インスタンスを返す
     */
    public static Smfb_core getInstance() {
        return instance;
    }

    /**
     * SocketServerを返す
     */
    public SocketServer getSocketServer() {
        return socketServer;
    }

    @Override
    public void onEnable() {
        instance = this;

        //イベント登録
        getProxy().getPluginManager().registerListener(this, new BungeeListener());

        //コマンド登録
        getProxy().getPluginManager().registerCommand(this, new BungeeMinecraftCommand());

        //言語設定
        defaultLocale = Locale.getDefault();
        if(defaultLocale != Locale.JAPAN) {
            //言語が日本語でもなかったら
            Locale.setDefault(Locale.JAPAN);
            log(Messages.ChangedLang.toString());
        }

        //OS判別
        String OS_NAME = System.getProperty("os.name").toLowerCase();
        if(!OS_NAME.startsWith("linux") && !OS_NAME.startsWith("windows")) {
            //Windows・Linux以外の場合
            log(Messages.UnsupportedOS.toString());
        }

        try {
            //configフォルダ
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }

            //configファイル
            File plugin_config = new File(getDataFolder(), "config.yml");
            if (!plugin_config.exists()) {
                //存在しなければコピー
                InputStream srcIs = getResourceAsStream("config.yml");
                Files.copy(srcIs, plugin_config.toPath());
                log(Messages.ConfigNotFound.toString());
            }

            //データを格納
            log(Messages.ConfigLoading.toString());
            ConfigGetter.ConfigGet(plugin_config);

            //ソケットサーバーの起動
            socketServer = new SocketServer();
            socketServer.start();

            // サーバーを起動
            Lobby.StartServer();
        } catch (IOException e) {
            log(Messages.IOError.toString());
        }
    }

    @Override
    public void onDisable() {
        for(Server server : ConfigData.Servers) {
            server.StopServer();
        }
        for(Server server : ConfigData.Servers) {
            while((server.Started || server.Switching) && server.Process.isAlive()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        socketServer.stopSocket();

        Locale.setDefault(defaultLocale);
    }

    /** ログを出力する
     * @param log 出力する文字
     */
    public void log(String log) {
        getLogger().info(log);
    }

    @Override
    public void IDRequest(byte[] gotData, ConnectionThread ct) {
    }

    @Override
    public void IDResponse(byte[] gotData, ConnectionThread ct) {
        byte[] byte_port = Arrays.copyOfRange(gotData, 0, 4);
        int port = (((short) byte_port[0]) & 0x00FF) * 16777216 +
                (((short) byte_port[1]) & 0x00FF) * 65536 +
                (((short) byte_port[2]) & 0x00FF) * 256 +
                (((short) byte_port[3]) & 0x00FF);

        Server srcServer = null;
        for (Server server : ConfigData.Servers) {
            if(server.Port == port) {
                srcServer = server;
                srcServer.Started = true;
                srcServer.Switching = false;
            }
        }
        if(srcServer == null) return;
        ClientConnection cc = (ClientConnection) ct;
        cc.setSrcServer(srcServer);

        log(Tools.Formatter(Messages.ServerStarted_log.toString(), srcServer.Name));
        ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.ServerStarted_minecraft.toString(), srcServer.Name)));
    }

    @Override
    public void PlayerCountResponse(byte[] gotData, ConnectionThread ct) {
        Server srcServer = ((ClientConnection)ct).getSrcServer();
        if(srcServer == Lobby) return;

        byte[] playerCount_byte = Arrays.copyOfRange(gotData, 0, 4);
        int playerCount = (((short) playerCount_byte[0]) & 0x00FF) * 16777216 +
                (((short) playerCount_byte[1]) & 0x00FF) * 65536 +
                (((short) playerCount_byte[2]) & 0x00FF) * 256 +
                (((short) playerCount_byte[3]) & 0x00FF);

        if(playerCount == 0) {
            if(srcServer.StartTimer()) {
                log(Tools.Formatter(Messages.TimerStarted_log.toString(), "" + ConfigData.CloseTime, srcServer.Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.TimerStarted_Minecraft.toString(), "" + ConfigData.CloseTime, srcServer.Name)));
            }
        }else{
            if(srcServer.StopTimer()) {
                Smfb_core.getInstance().log(Tools.Formatter(Messages.TimerStopped_log.toString(), srcServer.Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.TimerStopped_Minecraft.toString(), srcServer.Name)));
            }
        }
    }

    @Override
    public void ServerStopRequest(byte[] gotData, ConnectionThread ct) {

    }

    @Override
    public void ServerStopResponse(byte[] gotData, ConnectionThread ct) {
        ClientConnection cc = (ClientConnection) ct;
        cc.stopSocket();
    }
}
