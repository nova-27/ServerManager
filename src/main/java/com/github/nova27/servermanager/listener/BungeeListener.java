package com.github.nova27.servermanager.listener;

import java.util.Objects;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;

import net.md_5.bungee.api.chat.TextComponent;
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

	/**
	 * チャットが送信されたら実行
	 * @param event チャット情報
	 */
	@EventHandler
	public void onChat(ChatEvent event) {
		// コマンド実行の場合
		if ( event.isCommand() ) {
			String cmd = event.getMessage();
			String senderServer = ((ProxiedPlayer) event.getSender()).getServer().getInfo().getName();
			if(cmd.equals("/stop")) {
				//stopコマンドが実行された場合
				for(int i = 0; i < ConfigData.s_info.length; i++) {
					if (senderServer.equals(ConfigData.s_info[i].Name)) {
						main.bridge.sendToDiscord(":information_source: " + senderServer + "サーバーがマインクラフトから停止されました");
						ConfigData.s_info[i].enabled = false;
						ConfigData.s_info[i].switching = true;
					}
				}
			}
			return;
		}

		//キャンセルされていたら
		if (event.isCancelled()) return;

		// プレイヤーの発言ではない場合は、そのまま無視する
		if ( !(event.getSender() instanceof ProxiedPlayer) ) {
			return;
		}

		ProxiedPlayer sender = (ProxiedPlayer)event.getSender();
		String senderServer = sender.getServer().getInfo().getName();
		String message = event.getMessage();

		main.bridge.sendToDiscord( "【" + senderServer + "】" + sender + " : " + message);
	}

	/**
	 * ログインされたら
	 * @param e ログイン情報
	 */
	@EventHandler
	public void onLogin(LoginEvent e) {
		main.bridge.PlayerCount(1);
		String name = e.getConnection().getName();
		main.bridge.sendToDiscord(":wave: " + name + "がサーバーに参加しました！");
		main.closetimer_stop();

		//一人目の場合
		if(main.bridge.PlayerCount(0) == 1) {
			if (!main.s_started) {
				//起動していなかったら、キック
				e.getConnection().disconnect(new TextComponent("サーバーを起動します。もうしばらくお待ち下さい。"));
			}

			//サーバー起動
			for(int i = 0; i < ConfigData.s_info.length;i++) {
				if(Objects.equals(ConfigData.s_info[i].Name,"lobby") && ConfigData.s_info[i].switching) {
					//ロビーサーバーが起動中だったら
					e.getConnection().disconnect(new TextComponent("サーバーを起動しています。もうしばらくお待ち下さい。"));
				}
				ConfigData.s_info[i].Server_On();
			}
			main.s_started = true;
		}
	}

	/**
	 * ログアウトされたら
	 * @param e ログアウト情報
	 */
	@EventHandler
	public void onLogout(PlayerDisconnectEvent e) {
		main.bridge.PlayerCount(-1);
		String name = e.getPlayer().getName();
		main.bridge.sendToDiscord(":wave: " + name + "がサーバーから退出しました！\n");

		if (main.bridge.PlayerCount(0) == 0) {
			//0人になったら
			main.closetimer();
			main.bridge.sendToDiscord(":alarm_clock: " + ConfigData.close_time + "分後に各サーバーが停止します");
		}
	}
}
