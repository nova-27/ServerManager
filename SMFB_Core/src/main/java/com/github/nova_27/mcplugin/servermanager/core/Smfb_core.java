package com.github.nova_27.mcplugin.servermanager.core;

import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionThread;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketEventListener;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigGetter;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.events.ServerEvent;
import com.github.nova_27.mcplugin.servermanager.core.events.TimerEvent;
import com.github.nova_27.mcplugin.servermanager.core.listener.BungeeListener;
import com.github.nova_27.mcplugin.servermanager.core.listener.BungeeMinecraftCommand;
import com.github.nova_27.mcplugin.servermanager.core.socket.ClientConnection;
import com.github.nova_27.mcplugin.servermanager.core.socket.SocketServer;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import com.github.nova_27.mcplugin.servermanager.core.utils.Tools;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.minecrell.serverlistplus.bungee.BungeePlugin;
import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.replacement.LiteralPlaceholder;
import net.minecrell.serverlistplus.core.replacement.ReplacementManager;
import net.minecrell.serverlistplus.core.status.StatusResponse;
import org.bstats.bungeecord.Metrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;

import static com.github.nova_27.mcplugin.servermanager.core.config.ConfigData.Lobby;
import static com.github.nova_27.mcplugin.servermanager.core.events.TimerEvent.EventType.TimerStopped;

public final class Smfb_core extends Plugin implements PacketEventListener {

    private static Smfb_core instance;

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

        // 統計
        new Metrics(this, 7908);

        //イベント登録
        getProxy().getPluginManager().registerListener(this, new BungeeListener());

        //コマンド登録
        getProxy().getPluginManager().registerCommand(this, new BungeeMinecraftCommand());

        //言語ファイル
        File language_file = new File(getDataFolder(), "message.yml");
        if (!language_file.exists()) {
            //存在しなければコピー
            InputStream src = getResourceAsStream(Locale.getDefault().toString() + ".properties");
            if(src == null) src = getResourceAsStream("ja_JP.properties");

            try {
                Files.copy(src, language_file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        //プラグイン連携 ServerListPlus
        Plugin temp2 = getProxy().getPluginManager().getPlugin("ServerListPlus");
        if (temp2 instanceof BungeePlugin) {
            ReplacementManager.getDynamic().add(new LiteralPlaceholder("%lobby_status%") {
                /**
                 * プレイヤーがpingを送信したとき
                 */
                @Override
                public String replace(StatusResponse response, String s) {
                    if(Lobby != null) {
                        return this.replace(s, Lobby.Status());
                    }else{
                        return "--";
                    }
                }

                /**
                 * Unknown Player
                 */
                @Override
                public String replace(ServerListPlusCore core, String s) {
                    return "";
                }
            });
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

        // 起動完了かつプレイヤーが未参加であれば停止タイマーを開始する
        ServerInfo bungeeServerInfo = getProxy().getServerInfo(srcServer.ID);
        if (bungeeServerInfo != null && bungeeServerInfo.getPlayers().isEmpty())
            srcServer.StartTimer();

        Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new ServerEvent(srcServer, ServerEvent.EventType.ServerStarted));
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
                Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new TimerEvent(srcServer, TimerEvent.EventType.TimerStarted));
            }
        }else{
            if(srcServer.StopTimer()) {
                Smfb_core.getInstance().log(Tools.Formatter(Messages.TimerStopped_log.toString(), srcServer.Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.TimerStopped_Minecraft.toString(), srcServer.Name)));
                Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new TimerEvent(srcServer, TimerStopped));
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

    @Override
    public void SendCommand(byte[] gotData, ConnectionThread ct) {

    }
}
