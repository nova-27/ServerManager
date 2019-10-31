package com.github.nova27.servermanager.Util;

import java.awt.Color;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public class Bridge {
	ServerManager main;

	/**
	 * コンストラクタ
	 * @param main メインクラス
	 */
	public Bridge(ServerManager main) {
		this.main = main;
	}

	/**
	 * Minecraftへメッセージを送信
	 * @param message
	 */
	public void sendToMinecraft(Message message) {
		ProxyServer.getInstance().broadcast(new TextComponent(ChatColor.BLUE + "[Discord]" + ChatColor.WHITE + "<" + message.getAuthor().getName() + "> " + message.getContentRaw()));
	}

	/**
	 * Discordへメッセージを送信
	 * @param message 送信するメッセージ
	 */
	public void sendToDiscord(String message) {
		main.jda().getTextChannelById(ConfigData.ChannelId).sendMessage(message).queue();
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

		eb.setAuthor(author, null, "https://i.pinimg.com/originals/85/78/bf/8578bfd439ef6ee41e103ae82b561986.png");

		eb.setDescription(desc);

		for(String[] obj : list){
			eb.addField(obj[0], obj[1], false);
		}

		main.jda().getTextChannelById(ConfigData.ChannelId).sendMessage(eb.build()).queue();
	}

	/**
	 * プレイヤー数をカウント
	 * @param plus 増やす数
	 * @return プレイヤー数
	 */
	public int PlayerCount(int plus) {
		main.p_count += plus;
		return main.p_count;
	}
}
