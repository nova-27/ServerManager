package com.github.nova27.servermanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import com.github.nova27.servermanager.Util.Bridge;
import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.config.Config_get;
import com.github.nova27.servermanager.listener.BungeeListener;
import com.github.nova27.servermanager.listener.ChatCasterListener;
import com.github.nova27.servermanager.listener.DiscordListener;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterAPI;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterPlugin;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class ServerManager extends Plugin {
	public Bridge bridge = new Bridge(this);

	private JDA jda;
	private File plugin_config;
	private File bungee_config;

	public int p_count = 0;
	private TimerTask task = null;
	public boolean s_started = false;
	private Timer timer = null;

	//連携用
	private N8ChatCasterAPI chatCasterApi = null;

	/**
	 * プラグインが有効になったとき
	 */
	@Override
	public void onEnable() {
		//イベント登録
		getProxy().getPluginManager().registerListener(this, new BungeeListener(this));

		//プラグイン連携
		Plugin temp = getProxy().getPluginManager().getPlugin("N8ChatCaster");
		if (temp instanceof N8ChatCasterPlugin) {
			chatCasterApi = (((N8ChatCasterPlugin) temp).getChatCasterApi());
			getProxy().getPluginManager().registerListener(this, new ChatCasterListener(this));
		}

		super.onEnable();
	}

	/**
	 * プラグインが読み込まれたとき
	 */
	@Override
	public void onLoad() {
		try {
			//configフォルダ
			File folder = new File(
					ProxyServer.getInstance().getPluginsFolder(),
					"ServerManagerForBungeeCord");
			if (!folder.exists()) {
				//存在しなければ作成
				folder.mkdirs();
			}

			//configファイル
			plugin_config = new File(folder, "config.yml");
			if (!plugin_config.exists()) {
				//存在しなければコピー
				log("設定ファイルが存在しないため、テンプレートが作成されました。");
				InputStream srcIs = getClass().getResourceAsStream("/config.yml");
				Files.copy(srcIs, plugin_config.toPath());
			}

			//bungee_configファイル
			bungee_config = new File(
					"config.yml");
			if (!bungee_config.exists()) {
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

	/**
	 * プラグインが無効になったとき
	 */
	@Override
	public void onDisable() {
		//サーバーが起動していたら停止
		if (s_started == true) {
			getLogger().info("各サーバーの停止");
			bridge.sendToDiscord(":exclamation: 各サーバーを停止しています...");
			for (int i = 0; i < ConfigData.s_info.length; i++) {
				ConfigData.s_info[i].Exec_command("stop", "", null);

				ConfigData.s_info[i].enabled = false;
				ConfigData.s_info[i].switching = true;
			}
		}

		//停止まで待機
		for (int i = 0; i < ConfigData.s_info.length; i++) {
			if (ConfigData.s_info[i].switching || ConfigData.s_info[i].enabled) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}
		}

		//ボットの停止
		jda.getTextChannelById(ConfigData.ChannelId)
		.sendMessage(":octagonal_sign: " + ConfigData.ServerName + "サーバーのプロキシが停止します").complete();
		bridge.SendToDiscord_stop();
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
	 * JDAを返す
	 * @return jda
	 */
	public JDA jda() {
		return jda;
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
					bridge.sendToDiscord(":exclamation: 各サーバーが停止します");
					for (int i = 0; i < ConfigData.s_info.length; i++) {
						ConfigData.s_info[i].Exec_command("stop", "", null);
					}
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
		if(task != null) {
			task.cancel();
			task = null;
		}
	}

	/**
	 * 連携プラグインAPIのゲッター
	 * @return API
	 */
	public N8ChatCasterAPI getChatCasterApi() {
		return chatCasterApi;
	}
}
