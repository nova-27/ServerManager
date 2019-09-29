package com.github.nova27.servermanager.listener;

import java.util.Arrays;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;

import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;


public class DiscordListener extends ListenerAdapter {
	private final ServerManager main;

	public DiscordListener(ServerManager main) {
		this.main = main;
	}

	@Override
	public void onReady(ReadyEvent event) {
		main.setGame();
		String sendText = "";

		main.log("discordに接続しました！");
		sendText += ":white_check_mark: " + ConfigData.ServerName + "サーバーのプロキシが起動しました\n";


		main.ServerSwitch();
		main.closetimer();
		main.log("サーバー停止アラーム作動");
		sendText += ":alarm_clock: " + ConfigData.close_time + "分後に各サーバーが停止します";
		main.sendToDiscord(sendText);

		super.onReady(event);
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) return;
		if(event.getGuild() == null) return;
		if(event.getTextChannel().getIdLong() != ConfigData.ChannelId) return;

		// コマンドかどうか
		String FirstChar = ConfigData.FirstChar;

		if (event.getMessage().getContentRaw().startsWith(FirstChar)) {
			String command = event.getMessage().getContentRaw().replace(FirstChar, "").split(" ")[0];
			String[] args = event.getMessage().getContentRaw().replace(FirstChar + command + " ", "").split(" +");
			main.log(Arrays.deepToString(args));

			if (command.equalsIgnoreCase("help")) {
				//helpコマンド
				main.embed(event.getAuthor().getName(), "コマンド一覧", new String[][] {
					{
						FirstChar + "help",
						"(botの)コマンド一覧を出力します"
					},
					{
						FirstChar + "status",
						"各サーバーの情報を表示します"
					},
					{
						FirstChar + "info",
						"サーバーの情報を取得します"
					},
					{
						FirstChar + "enable [サーバー名] [true or false]",
						"サーバーの有効・無効状態を切り替えます"
					}
				});
			}else if (command.equalsIgnoreCase("info")) {
				//infoコマンド
				main.embed(event.getAuthor().getName(), "サーバー情報", new String[][] {
					{
						"IPアドレス",
						ConfigData.IP
					},
					{
						"Port番号",
						ConfigData.Port
					}
				});
			}else if (command.equalsIgnoreCase("status")) {
				//statusコマンド
				String [][] Server_info = new String[ConfigData.Server_info.length][2];
				for(int i = 0; i <= ConfigData.Server_info.length - 1; i++) {
					Server_info[i][0] = ConfigData.Server_info[i][0];
					String[] server = ConfigData.Server_info[i];
					String Text = "有効であるか : " + ConfigData.enabled[i] + "\n";
					if(ConfigData.enabled[i] && main.s_started) {
						Text += "プレイヤー : " + main.rcon_send(server, "list") + "\n";
					}
					Server_info [i][1] = Text;
				}
				String [][] Bungee_info = {
						{
							"BungeeCord統計情報",
							"プレイヤー数 : " + main.PlayerCount(0) + "人"
						}
				};
				String[][] embed_Text = new String[Server_info.length + Bungee_info.length][2];

				System.arraycopy(Bungee_info, 0, embed_Text, 0, Bungee_info.length);
				System.arraycopy(Server_info, 0, embed_Text, Bungee_info.length, Server_info.length);
				main.embed(event.getAuthor().getName(), "サーバーステータス", embed_Text);
			}else if(command.equalsIgnoreCase("enable")){
				//Adminかどうか
				if (!isAdmin(event.getGuild().getId())) {
					main.sendToDiscord(":exclamation: このコマンドは管理者専用です！");
					main.log("管理コマンドを実行したユーザーのID : " + event.getGuild().getId());
					main.log("config : " + ConfigData.Admin_UserID[0]);
					return;
				}

				if(args.length < 2) {
					main.sendToDiscord("コマンドの構文が間違っています！");
					return;
				}

				boolean flag = Boolean.valueOf(args[1]);

				for(int i = 0; i <= ConfigData.Server_info.length - 1; i++) {
					if(args[0].equals(ConfigData.Server_info[i][0])) {
						if(ConfigData.enabled[i]== flag) {
							return;
						}
						if(flag) {
							main.ServerSwitch(ConfigData.Server_info[i]);
							ConfigData.enabled[i] = flag;
							main.sendToDiscord(":information_source: " + ConfigData.Server_info[i][0] + "サーバーを" + flag + "にしました");
						}else {
							if(main.s_started) {
								main.rcon_send(ConfigData.Server_info[i] ,"stop");
							}
							ConfigData.enabled[i] = flag;
							main.sendToDiscord(":information_source: " + ConfigData.Server_info[i][0] + "サーバーを" + flag + "にしました");
						}

						return;
					}
				}

				main.sendToDiscord("サーバーが存在しません！");
			}else {
				//無効なコマンドなら
				main.sendToDiscord("無効なコマンドです");
			}
		}else {
			// コマンドでなければMinecraftへ送信
			main.sendToMinecraft(event.getMessage());
		}
	}

	public boolean isAdmin(String ID) {
		for(int i = 0; i <= ConfigData.Admin_UserID.length - 1; i++) {
			if(ID.equals(ConfigData.Admin_UserID[i])) {
				return true;
			}
		}
		return false;
	}
}
