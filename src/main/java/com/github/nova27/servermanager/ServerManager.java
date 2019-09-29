package com.github.nova27.servermanager;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.security.auth.login.LoginException;

import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.config.Config_get;
import com.github.nova27.servermanager.listener.BungeeListener;
import com.github.nova27.servermanager.listener.DiscordListener;

import io.graversen.minecraft.rcon.RconClient;
import io.graversen.minecraft.rcon.RconResponse;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;



public class ServerManager extends Plugin {
	private JDA jda;
	private File plugin_config;
	private File bungee_config;

	private int p_count = 0;
	private TimerTask task = null;
	public boolean s_started = false;
	private Timer timer = null;

	@Override
	public void onEnable() {
		//イベント登録
		getProxy().getPluginManager().registerListener(this, new BungeeListener(this));

		super.onEnable();
	}

	@Override
	public void onLoad() {
		try {
			//configフォルダ
			File folder = new File(
					ProxyServer.getInstance().getPluginsFolder(),
					"ServerManagerForBungeeCord");
			if ( !folder.exists() ) {
				//存在しなければ作成
				folder.mkdirs();
			}

			//configファイル
			plugin_config = new File(folder, "config.yml");
			if ( !plugin_config.exists() ) {
				//存在しなければコピー
				log("設定ファイルが存在しないため、テンプレートが作成されました。");
				InputStream srcIs = getClass().getResourceAsStream("/config.yml");
				Files.copy(srcIs, plugin_config.toPath());
			}

			//bungee_configファイル
			bungee_config = new File(
					"config.yml");
			if ( !bungee_config.exists() ) {
				log("bungeecordのconfigファイルが見つかりません");
			}
		} catch (IOException e) {
			log("ファイル入出力エラー");
			e.printStackTrace();
		}

		//データを格納
		log("設定ファイルを読み込んでいます...");
		Config_get c_getter = new Config_get(this);
		c_getter.ConfigGet(plugin_config, bungee_config);

		//jda設定
		try {
			jda = new JDABuilder(ConfigData.Token).build();
			jda.addEventListener(new DiscordListener(this));
		} catch (LoginException e) {
			log("Botのログインに失敗しました");
			e.printStackTrace();
		}

		super.onLoad();
	}

	@Override
	public void onDisable() {
    	//サーバーが起動していたら停止
    	if (s_started == true) {
    		getLogger().info("各サーバーの停止");
			sendToDiscord(":exclamation: 各サーバーが停止します");
			sendToDiscord("実行結果 :");
			sendToDiscord("```" + rcon_send("stop") + "```");
    	}
		jda.getTextChannelById(ConfigData.ChannelId).sendMessage(":octagonal_sign: " + ConfigData.ServerName + "サーバーのプロキシが停止しました").queue();
		jda.shutdown();

		super.onDisable();
	}

	/** ログを出力する
	 * @param log 出力する文字
	 */
	public void log(String log) {
		getLogger().info(log);
	}

	/**
	 * Minecraftへメッセージを送信
	 * @param message
	 */
	@SuppressWarnings("deprecation")
	public void sendToMinecraft(Message message) {
		//ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.BLUE + "[Discord]" + ChatColor.WHITE + "<" + message.getAuthor().getName() + "> " + message.getContentRaw()));
		ProxyServer.getInstance().broadcast(ChatColor.BLUE + "[Discord]" + ChatColor.WHITE + "<" + message.getAuthor().getName() + "> " + message.getContentRaw());
	}


	/**
	 * Discordへメッセージを送信
	 * @param message 送信するメッセージ
	 */
	public void sendToDiscord(String message) {
		jda.getTextChannelById(ConfigData.ChannelId).sendMessage(message).queue();
	}

	/**
	 * Discord Embed機能
	 * @param author 宛て名
	 * @param desc タイトル
	 * @param list 内容
	 */
	public void embed(String author, String desc, String[][] list) {
		EmbedBuilder eb = new EmbedBuilder();

		eb.setColor(Color.blue);

		eb.setAuthor(author, null, "https://github.com/zekroTJA/DiscordBot/blob/master/.websrc/zekroBot_Logo_-_round_small.png");

		eb.setDescription(desc);

		for(String[] obj : list){
			eb.addField(obj[0], obj[1], false);
		}

		jda.getTextChannelById(ConfigData.ChannelId).sendMessage(eb.build()).queue();
	}

	/**
	 * プレイヤー数をカウント
	 * @param plus 増やす数
	 * @return プレイヤー数
	 */
	public int PlayerCount(int plus) {
		p_count += plus;
		return p_count;
	}

	/**
	 * ゲームの設定
	 * @param event
	 */
	public void setGame() {
		jda.getPresence().setGame(Game.playing(ConfigData.PlayGame));
	}

	/**
	 * タイマーの起動
	 */
	public void closetimer() {
		if (task == null) {
            task = new TimerTask() {
        		@Override
        		public void run() {
        			getLogger().info("各サーバーの停止");
        			String sendText = "";
        			sendText += ":exclamation: 各サーバーが停止します\n";
        			sendText += "実行結果 :\n";
        			sendText += "```" + Arrays.deepToString(rcon_send("stop")) + "```";
        			sendToDiscord(sendText);
        			s_started = false;
        		}
            };
        }
		timer = new Timer();
        timer.schedule(task, ConfigData.close_time * 60000);
	}

	/**
	 * タイマーのストップ
	 */
	public void closetimer_stop() {
		task.cancel();
		task = null;
	}

	/**
	 * サーバーON,OFF切り替え
	 */
	public void ServerSwitch() {
		if (s_started == false) {
			try {
				String sendText = "";
				for(int i = 0; i <= ConfigData.Server_info.length - 1; i++) {
					if(ConfigData.enabled[i]) {
						//サーバーが有効なら
						String dir = ConfigData.Server_info[i][1];
						String file = ConfigData.Server_info[i][2];
						String args = ConfigData.Server_info[i][5];

						String OS_NAME = System.getProperty("os.name").toLowerCase();
						if(OS_NAME.startsWith("linux")) {
							//Linuxの場合
							new ProcessBuilder("/bin/bash","-c","cd  " + dir + " ; java -jar " + args + " " + file).start();
						}else if(OS_NAME.startsWith("windows")) {
							//Windowsの場合
							Runtime r = Runtime.getRuntime();
							r.exec("cmd /c cd " + dir + " && java -jar " + args + " " + file);
						}

						getLogger().info(ConfigData.Server_info[i][0] + "サーバーの起動");
						sendText += ":exclamation: " + ConfigData.Server_info[i][0] +  "サーバーが起動しました\n";
					}
				}
				if(sendText != "") sendToDiscord(sendText);
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}

			s_started = true;
		}
	}

	/**
	 * 特定サーバーをONにする
	 */
	public void ServerSwitch(String[] server) {
		if (s_started == true) {
			try {
				String dir = server[1];
				String file = server[2];
				String args = server[5];

				String OS_NAME = System.getProperty("os.name").toLowerCase();
				if(OS_NAME.startsWith("linux")) {
					//Linuxの場合
					new ProcessBuilder("/bin/bash","-c","cd  " + dir + " ; java -jar " + args + " " + file).start();
				}else if(OS_NAME.startsWith("windows")) {
					//Windowsの場合
					Runtime r = Runtime.getRuntime();
					r.exec("cmd /c cd " + dir + " && java -jar " + args + " " + file);
				}

				getLogger().info(server[0] + "サーバーの起動");
				sendToDiscord(":exclamation: " + server[0] +  "サーバーが起動しました\n");
			} catch (IOException e1) {
				// TODO 自動生成された catch ブロック
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 全部のサーバーへコマンドを送信
	 * @param command コマンド
	 * @return 結果
	 */
	public String[] rcon_send(String command) {
		int enable_count = 0;
		for(int i = 0; i <= ConfigData.Server_info.length - 1; i++) {
			if(ConfigData.enabled[i]) {
				enable_count++;
			}
		}
		String[] result = new String[enable_count];
		try {
			for(int i = 0, a = 0; i <= ConfigData.Server_info.length - 1; i++) {
				if(ConfigData.enabled[i]) {
					String port = ConfigData.Server_info[i][3];
					String passwd = ConfigData.Server_info[i][4];

					RconClient rcon = RconClient.connect("127.0.0.1", passwd, Integer.parseInt(port));
					Future<RconResponse> responseFuture = rcon.sendRaw(command);
					try {
						result[a] = responseFuture.get(30, TimeUnit.SECONDS).getResponseString();
					} catch (TimeoutException e) {
						result[a] = "タイムアウト";
					}
					rcon.close();

					a++;
				}
			}
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 特定のサーバーへコマンドを送る
	 * @param server コマンドを送るサーバーの情報配列
	 * @param command 送るコマンド
	 * @return 実行結果
	 */
	public String rcon_send(String[] server, String command) {
		String port = server[3];
		String passwd = server[4];

		String result = "";
		RconClient rcon;
		try {
			rcon = RconClient.connect("127.0.0.1", passwd, Integer.parseInt(port));
			Future<RconResponse> responseFuture = rcon.sendRaw(command);
			try {
				result = responseFuture.get(30, TimeUnit.SECONDS).getResponseString();
			} catch(TimeoutException e) {
				result = "タイムアウト";
			}
			rcon.close();
		} catch (InterruptedException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return result;
	}
}
