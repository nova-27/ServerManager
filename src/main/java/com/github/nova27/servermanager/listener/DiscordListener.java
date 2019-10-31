package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.Util.Com_result_event;
import com.github.nova27.servermanager.config.ConfigData;

import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
	private ServerManager main;

	/**
	 * コンストラクタ
	 * @param main
	 */
	public DiscordListener(ServerManager main) {
		this.main = main;
	}

	/**
	 * Botが起動完了したら
	 */
	@Override
	public void onReady(ReadyEvent event) {
		main.jda().getPresence().setGame(Game.playing(ConfigData.PlayGame));

		main.log("discordに接続しました！");
		main.bridge.sendToDiscord(":white_check_mark: " + ConfigData.ServerName + "サーバーのプロキシが起動しました\n");

		for(int i = 0; i < ConfigData.s_info.length;i++) {
			ConfigData.s_info[i].Server_On();
		}
		main.s_started = true;
		main.closetimer();
		main.log("サーバー停止アラーム作動");
		main.bridge.sendToDiscord(":alarm_clock: " + ConfigData.close_time + "分後に各サーバーが停止します");

		super.onReady(event);
	}

	/**
	 * メッセージが送信されたら
	 */
	private int result_get_count;
	private String[] results;
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if(event.getAuthor().isBot()) return;
		if(event.getGuild() == null) return;
		if(event.getTextChannel().getIdLong() != ConfigData.ChannelId) return;

		// コマンドかどうか
		String FirstChar = ConfigData.FirstChar;
		if (event.getMessage().getContentRaw().startsWith(FirstChar)) {
			String command = event.getMessage().getContentRaw().replace(FirstChar, "").split(" ")[0];
			String[] args = event.getMessage().getContentRaw().replace(FirstChar + command + "\\s+", "").split("\\s+");

			if (command.equalsIgnoreCase("help")) {
				//helpコマンド
				main.bridge.embed(event.getAuthor().getName(), "コマンド一覧", new String[][] {
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
				main.bridge.embed(event.getAuthor().getName(), "サーバー情報", new String[][] {
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
				String[][] Embed_text = new String[ConfigData.s_info.length + 1][2];

				//初期化
				result_get_count = 0;
				int enabled_server = 0;
				for(int i = 0; i < ConfigData.s_info.length; i++) {
					if(ConfigData.s_info[i].enabled) {
						enabled_server++;
					}
				}
				results = new String[enabled_server];

				if(!main.s_started || enabled_server == 0) {
					//サーバーが停止中だったら
					Embed_text[0][0] = "BungeeCord統計情報";
					Embed_text[0][1] = "プレイヤー数 : 0人";
					for(int i = 0; i < ConfigData.s_info.length; i++) {
						Embed_text[i+1][0] = ConfigData.s_info[i].Name;
						Embed_text[i+1][1] = "有効であるか : " + ConfigData.s_info[i].enabled;
					}

					main.bridge.embed(event.getAuthor().getName(), "サーバーステータス", Embed_text);
				}else {
					//コマンドの実行
					for(int i = 0; i < ConfigData.s_info.length; i++) {
						if(ConfigData.s_info[i].enabled) {
							boolean result = ConfigData.s_info[i].Exec_command("list", "There are.+of a max.+players online:.+", new Com_result_event() {
								@Override
								public void got_result(String result) {
									results[result_get_count] = result;

									result_get_count++;
									String[][] Embed_text;
									if(result_get_count == results.length) {
										Embed_text = new String[ConfigData.s_info.length + 1][2];
										Embed_text[0][0] = "BungeeCord統計情報";
										Embed_text[0][1] = "プレイヤー数 : 0人";

										int j = 0;
										for(int i = 0; i < ConfigData.s_info.length; i++) {
											Embed_text[i+1][0] = ConfigData.s_info[i].Name;
											Embed_text[i+1][1] = "有効であるか : " + ConfigData.s_info[i].enabled;

											if(ConfigData.s_info[i].enabled) {
												Embed_text[i+1][1] += "\nプレイヤー数 : " + results[j];
												j++;
											}
										}

										main.bridge.embed(event.getAuthor().getName(), "サーバーステータス", Embed_text);
									}
								}
							});

							if(!result) {
								main.bridge.sendToDiscord(":exclamation: サーバーは現在処理中です！");
								return;
							}
						}
					}
				}
			}else if(command.equalsIgnoreCase("enable")){
				//Adminかどうか
				if (!isAdmin(event.getGuild().getId())) {
					main.bridge.sendToDiscord(":exclamation: このコマンドは管理者専用です！");
					return;
				}

				if(args.length < 2) {
					main.bridge.sendToDiscord("コマンドの構文が間違っています！");
					return;
				}

				boolean flag = Boolean.valueOf(args[1]);

				for(int i = 0; i <= ConfigData.s_info.length - 1; i++) {
					if(args[0].equals(ConfigData.s_info[i].Name)) {
						if(ConfigData.s_info[i].enabled == flag) {
							main.bridge.sendToDiscord(":information_source: " + ConfigData.s_info[i].Name + "サーバーは既に" + flag + "です！");
							return;
						}
						if(flag) {
							ConfigData.s_info[i].enabled = flag;
							if(main.s_started) {
								main.s_started = false;
								ConfigData.s_info[i].Server_On();
								main.s_started = true;
							}
							main.bridge.sendToDiscord(":information_source: " + ConfigData.s_info[i].Name + "サーバーを" + flag + "にしました");
						}else {
							if(main.s_started) {
								ConfigData.s_info[i].Exec_command("stop", "", null);
							}
							ConfigData.s_info[i].enabled = flag;
							main.bridge.sendToDiscord(":information_source: " + ConfigData.s_info[i].Name + "サーバーを" + flag + "にしました");
						}

						return;
					}
				}

				main.bridge.sendToDiscord("サーバーが存在しません！");
			}else {
				//無効なコマンドなら
				main.bridge.sendToDiscord("コマンドが見つかりません");
			}
		}else {
			// コマンドでなければMinecraftへ送信
			main.bridge.sendToMinecraft(event.getMessage());
		}
	}

	/**
	 * Adminかどうか確認する
	 * @param ID 確認するユーザーID
	 * @return Adminならtrue
	 */
	public boolean isAdmin(String ID) {
		for(int i = 0; i < ConfigData.Admin_UserID.length; i++) {
			if(ID.equals(ConfigData.Admin_UserID[i])) {
				return true;
			}
		}
		return false;
	}
}
