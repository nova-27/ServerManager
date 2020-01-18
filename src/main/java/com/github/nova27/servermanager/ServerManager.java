package com.github.nova27.servermanager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Locale;

import javax.security.auth.login.LoginException;

import com.github.nova27.servermanager.config.ConfigGetter;
import com.github.nova27.servermanager.listener.CommandExecuter;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.Messages;
import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.listener.BungeeListener;
import com.github.nova27.servermanager.listener.ChatCasterListener;
import com.github.nova27.servermanager.listener.DiscordListener;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterAPI;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterPlugin;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;

public class ServerManager extends Plugin {
	public Bridge bridge;

	//プラグイン有効フラグ
	public boolean pl_enabled = true;

	//言語
	private Locale defaultLocale;

	//連携用
	private N8ChatCasterAPI chatCasterApi = null;

	//config
	private File plugin_config;
	private File bungee_config;

	//jda
	private JDA jda;

	//その他
	public int p_count = 0;

	/**
	 * プラグインが有効になったとき
	 */
	@Override
	public void onEnable() {
		//言語設定
		defaultLocale = Locale.getDefault();
		if(defaultLocale != Locale.JAPAN && defaultLocale != Locale.US) {
			//言語が日本語でも英語でもなかったら
			Locale.setDefault(Locale.ENGLISH);
			log(Messages.ChangedLang.toString());
		}

		//OS判別
		String OS_NAME = System.getProperty("os.name").toLowerCase();
		if(!OS_NAME.startsWith("linux") && !OS_NAME.startsWith("windows")) {
			//Windows・Linux以外の場合
			error_log(Messages.UnsupportedOS.toString());
			pl_enabled = false;
			onDisable();
		}

		//イベント登録
		getProxy().getPluginManager().registerListener(this, new BungeeListener(this));

		//コマンド登録
		getProxy().getPluginManager().registerCommand(this, new CommandExecuter());

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
			if (!getDataFolder().exists()) {
				getDataFolder().mkdir();
			}

			//configファイル
			plugin_config = new File(getDataFolder(), "config.yml");
			if (!plugin_config.exists()) {
				//存在しなければコピー
				InputStream srcIs = getResourceAsStream("config.yml");
				Files.copy(srcIs, plugin_config.toPath());
				log(Messages.ConfigNotFound.toString());
				pl_enabled = false;
				onDisable();
			}

			//bungee_configファイル
			bungee_config = new File(
					"config.yml");
			if (!bungee_config.exists()) {
				error_log(Messages.BungeeConfigNotFound.toString());
				pl_enabled = false;
				onDisable();
			}
		} catch (IOException e) {
			error_log(Messages.IOError.toString());
			e.printStackTrace();
			pl_enabled = false;
			onDisable();
		}

		//データを格納
		log(Messages.ConfigLoading.toString());
		ConfigGetter.ConfigGet(this, plugin_config, bungee_config);

		//jda設定
		try {
			jda = new JDABuilder(ConfigData.Token).build();
			jda.addEventListener(new DiscordListener(this));
			bridge = new Bridge(this);
		} catch (LoginException e) {
			error_log(Messages.FailBotLogin.toString());
			e.printStackTrace();
			pl_enabled = false;
			onDisable();
		}

		super.onLoad();
	}

	/**
	 * プラグインが無効になったとき
	 */
	@Override
	public void onDisable() {
		if(!pl_enabled){
			//プラグインが無効だったら
			super.onDisable();
			return;
		}

		//サーバーを停止
		getLogger().info(Messages.AllServerStopping_Log.toString());
		bridge.sendToDiscord(Messages.AllServerStopping_Discord.toString());
		for(int i = 0; i < ConfigData.Server.length; i++) {
			ConfigData.Server[i].StopTimer();

			while(ConfigData.Server[i].Switching) {
				//切り替えが終わるまで待機
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if(ConfigData.Server[i].Exec_command("stop", "", null)) {
				//コマンドの実行に成功したら(起動していたら)
				try {
					ConfigData.Server[i].Process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		//ボットの停止
		jda.getTextChannelById(ConfigData.ChannelId).sendMessage(Bridge.Formatter(Messages.ProxyStopped.toString(), ConfigData.ServerName)).complete();
		bridge.SendToDiscord_stop();
		jda.shutdown();

		Locale.setDefault(defaultLocale);
		super.onDisable();
	}

	/** ログを出力する
	 * @param log 出力する文字
	 */
	public void log(String log) {
		getLogger().info(log);
	}

	/**
	 * エラーログを出力する
	 * @param log 出力する文字
	 */
	public void error_log(String log) {
		getLogger().info(ChatColor.RED+log);
	}

	/**
	 * JDAを返す
	 * @return jda
	 */
	public JDA jda() {
		return jda;
	}


	/**
	 * 連携プラグインAPIのゲッター
	 * @return API
	 */
	public N8ChatCasterAPI getChatCasterApi() {
		return chatCasterApi;
	}
}
