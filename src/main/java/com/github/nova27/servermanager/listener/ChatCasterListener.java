package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.ServerManager;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.events.GlobalChatEvent;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatCasterListener implements Listener {
	ServerManager main;

	/**
	 * 連携プラグイン用リスナー
	 * @param main ServerManagerのオブジェクト
	 */
	public ChatCasterListener(ServerManager main) {
		this.main = main;
	}

	/**
	 * グローバルチャットに送信されたら実行（連携プラグイン有効時のみ実行される）
	 * @param event チャット情報
	 */
	@EventHandler
	public void onGlobalChat(GlobalChatEvent event) {
	    if (event.isCancelled()) return;

	    String message = this.main.getChatCasterApi().formatMessageForDiscord(event);
	    this.main.bridge.sendToDiscord(message);
	}
}
