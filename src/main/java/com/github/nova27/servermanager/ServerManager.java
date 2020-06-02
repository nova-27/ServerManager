package com.github.nova27.servermanager;

import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.listener.BungeeListener;
import com.github.nova27.servermanager.listener.ChatCasterListener;
import com.github.nova27.servermanager.listener.DiscordListener;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.Messages;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterAPI;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterPlugin;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.minecrell.serverlistplus.bungee.BungeePlugin;
import net.minecrell.serverlistplus.core.ServerListPlusCore;
import net.minecrell.serverlistplus.core.replacement.LiteralPlaceholder;
import net.minecrell.serverlistplus.core.replacement.ReplacementManager;
import net.minecrell.serverlistplus.core.status.StatusResponse;

import javax.security.auth.login.LoginException;
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
	 * プラグインが有効になったとき
	 */
	@Override
	public void onEnable() {



		//プラグイン連携 N8ChatListener
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
		//jda設定
		try {
			jda = new JDABuilder(ConfigData.Token).build();
			jda.addEventListener(new DiscordListener(this));
			bridge = new Bridge(this);
		} catch (LoginException e) {
			log(Messages.FailBotLogin.toString());
			e.printStackTrace();
			pl_enabled = false;
			onDisable();
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
					if(BungeeListener.Lobby != null) {
						return this.replace(s, BungeeListener.Lobby.Status());
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
