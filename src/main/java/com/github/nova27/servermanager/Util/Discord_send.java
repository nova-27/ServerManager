package com.github.nova27.servermanager.Util;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;

public class Discord_send extends Thread {
	private final int QUEUE_BUF = 50;

	private ServerManager main;
	private int start_write = 0;
	private int start_read = 0;
	private String[] queue = new String[QUEUE_BUF];

	private boolean stopped = false;

	/**
	 * Discordにメッセージを送信します（同期型）
	 * @param main ServerManagerのオブジェクト
	 */
	public Discord_send(ServerManager main) {
		this.main = main;
	}

	/**
	 * キューに送信するメッセージを追加します
	 * @param text 送信するメッセージ
	 */
	public void add_queue(String text) {
		queue[start_write] = text;

		start_write++;
		if (start_write >= QUEUE_BUF) {
			start_write = 0;
		}
	}

	/**
	 * スレッドを停止します
	 */
	public void thread_stop() {
		stopped = true;
	}

	@Override
	public void run() {
		while (!stopped) {
			for (; queue[start_read] != null; start_read++) {
				if (start_read >= QUEUE_BUF) {
					start_read = 0;
				}

				main.jda().getTextChannelById(ConfigData.ChannelId).sendMessage(queue[start_read]).complete();
				queue[start_read] = null;
			}

			try {
				sleep(100);
			} catch (InterruptedException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		}
	}
}
