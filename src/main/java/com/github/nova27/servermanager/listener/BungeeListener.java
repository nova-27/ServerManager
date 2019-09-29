package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {
	private final ServerManager main;

	/**
	 * コンストラクタ
	 * @param main ServerManagerのオブジェクト
	 */
	public BungeeListener(ServerManager main) {
		this.main = main;
	}

	@EventHandler
    public void onChat(ChatEvent event) {
		// コマンド実行の場合
        if ( event.isCommand() ) {
        	String cmd = event.getMessage();
        	String senderServer = ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName();
        	if(cmd.equals("/stop")) {
        		for(int i = 0; i <= ConfigData.Server_info.length - 1; i++) {
        			if (senderServer.equals(ConfigData.Server_info[i][0])) {
        				main.sendToDiscord(":information_source: " + senderServer + "サーバーがマインクラフトから停止されました");
        				ConfigData.enabled[i] = false;
        			}
        		}
        	}
            return;
        }

        // プレイヤーの発言ではない場合は、そのまま無視する
        if ( !(event.getSender() instanceof ProxiedPlayer) ) {
            return;
        }

        ProxiedPlayer sender = (ProxiedPlayer)event.getSender();
        String senderServer = sender.getServer().getInfo().getName();
        String message = event.getMessage();

        main.sendToDiscord( "[" + senderServer + "]" + sender + " : " + message);
    }

    @EventHandler
    public void onLogin(LoginEvent e) {
    	main.PlayerCount(1);
    	String name = e.getConnection().getName();
    	main.sendToDiscord(":wave: " + name + "がサーバーに参加しました！");
    	main.closetimer_stop();
    	main.ServerSwitch();
    }

    @EventHandler
    public void onLogout(PlayerDisconnectEvent e) {
    	main.PlayerCount(-1);
    	String sendText = "";
    	String name = e.getPlayer().getName();
    	sendText += ":wave: " + name + "がサーバーから退出しました！\n";

    	if (main.PlayerCount(0) == 0) {
    		main.closetimer();
    		sendText += ":alarm_clock: " + ConfigData.close_time + "分後に各サーバーが停止します";
    	}
    	main.sendToDiscord(sendText);
    }
}
