package com.github.nova27.servermanager;

import com.github.nova27.servermanager.config.ConfigData;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterAPI;
import net.dv8tion.jda.core.JDA;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.Locale;

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
	 * プラグインが読み込まれたとき
	 */
	@Override
	public void onLoad() {



		super.onLoad();
	}

	/**
	 * プラグインが無効になったとき
	 */
	@Override
	public void onDisable() {



		//ボットの停止
		jda.getTextChannelById(ConfigData.ChannelId).sendMessage(Bridge.Formatter(Messages.ProxyStopped.toString(), ConfigData.ServerName)).complete();
		bridge.SendToDiscord_stop();
		jda.shutdown();

		Locale.setDefault(defaultLocale);
		super.onDisable();
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
