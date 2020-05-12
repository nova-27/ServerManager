package com.github.nova_27.mcplugin.servermanager.core.listener;

import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import com.github.nova_27.mcplugin.servermanager.core.utils.Tools;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import static com.github.nova_27.mcplugin.servermanager.core.config.ConfigData.Lobby;

/**
 * BungeeCordイベントリスナー
 */
public class BungeeListener implements Listener {
    /**
     * ログインされたら
     * @param e ログイン情報
     */
    @EventHandler
    public void onLogin(LoginEvent e) {
        //一人目の場合
        if(ProxyServer.getInstance().getPlayers().size() == 0) {
            //サーバーを起動
            if (!Lobby.Started) {
                //起動していなかったら、キック
                e.getConnection().disconnect(new TextComponent(Messages.LobbyNotStarted.toString()));
                Lobby.StartServer();
            }
            else if (Lobby.Switching){
                //処理中だったら、キック
                e.getConnection().disconnect(new TextComponent(Messages.LobbySwitching.toString()));
            }else{
                //起動済みだったらタイマーストップ
                Lobby.StopTimer();
                Smfb_core.getInstance().log(Tools.Formatter(Messages.TimerStopped_log.toString(), Lobby.Name));
            }
        }
    }

    /**
     * ログアウトされたら
     * @param e ログアウト情報
     */
    @EventHandler
    public void onLogout(PlayerDisconnectEvent e) {
        if (ProxyServer.getInstance().getPlayers().size() == 1) {
            //0人になったら
            Lobby.StartTimer();
            Smfb_core.getInstance().log(Tools.Formatter(Messages.TimerStarted_log.toString(), ""+ ConfigData.CloseTime, Lobby.Name));
        }
    }

    /**
     * サーバー間を移動できたら
     */
    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        for (Server server : ConfigData.Servers) {
            //ロビーサーバーは除外
            if(server == Lobby) continue;

            //タイマーのストップ
            if(server.equals(e.getPlayer().getServer().getInfo().getName())) {
                server.StopTimer();
                Smfb_core.getInstance().log(Tools.Formatter(Messages.TimerStopped_log.toString(), server.Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.TimerStopped_Minecraft.toString(), server.Name)));
            }
        }
    }
}
