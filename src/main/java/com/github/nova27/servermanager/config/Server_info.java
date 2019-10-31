package com.github.nova27.servermanager.config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.Util.Com_result_event;
import com.github.nova27.servermanager.Util.Com_result_thread;
import com.github.nova27.servermanager.Util.Log_getter_event;
import com.github.nova27.servermanager.Util.Log_getter_thread;

public class Server_info implements Log_getter_event {
	public final int BUF_LOG_CNT = 30;

	private ServerManager main;

	public String Name;
	public String Dir;
	public String File;
	public String Args;
	public Long Console_ChannelId;

	private Log_getter_thread log_getter;
	public String[] logs = new String[BUF_LOG_CNT];
	public int start_write = 0;

	public Process server = null;
	public boolean switching = false;
	public boolean enabled = true;

	/**
	 * サーバーの情報・管理用クラス
	 * @param main メインのオブジェクト
	 */
	public Server_info(ServerManager main) {
		this.main = main;
	}

	/**
	 * 特定サーバーをONにする
	 */
	public void Server_On() {
		if(!main.s_started && enabled) {
			try {
				String OS_NAME = System.getProperty("os.name").toLowerCase();
				if(OS_NAME.startsWith("linux")) {
					//Linuxの場合
					server = new ProcessBuilder("/bin/bash","-c","cd  " + Dir + " ; java -jar " + Args + " " + File).start();
				}else if(OS_NAME.startsWith("windows")) {
					//Windowsの場合
					Runtime r = Runtime.getRuntime();
					server = r.exec("cmd /c cd " + Dir + " && java -jar " + Args + " " + File);
				}

				//ログ取得スレッドの開始
				log_getter = new Log_getter_thread(this, this);
				log_getter.start();

				switching = true;
				main.log(Name + "サーバーを起動しています");
				main.bridge.sendToDiscord(":information_source: " + Name +  "サーバーを起動しています...\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * コマンドを実行し、コマンドの実行結果を取得後、com_result_event.got_result(実行結果[失敗時はnull])を実行します。
	 *
	 * @param command コマンド
	 * @param match コマンド実行結果の形式 正規表現
	 * @param com_result_event 実行結果取得後のイベント (nullの場合、コマンド送信のみ)
	 * @return コマンド送信できたかどうか
	 */
	public boolean Exec_command(String command, String match, Com_result_event com_result_event) {
		//起動していなかったら
		if(!main.s_started) {
			return false;
		}
		//有効じゃなかったら
		if(!enabled) {
			return false;
		}
		//ステータス切り替え中だったら
		if(switching) {
			return false;
		}

		//コマンドの送信
		BufferedWriter streamInput = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
		try {
			streamInput.write(command+"\n");
			streamInput.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if(com_result_event == null) {
			return true;
		}

		//実行結果取得スレッドを実行
		//TODO
		Com_result_thread get_result = new Com_result_thread(this, match, com_result_event);
		get_result.start();

		return true;
	}

	/**
	 * ログを取得したあとの処理
	 * @param line ログ
	 */
	@Override
	public void log_got(String line) {
		//コンソールチャンネルIDが存在するなら、送信
		if(Console_ChannelId != null) {
			main.jda().getTextChannelById(Console_ChannelId).sendMessage(line).queue();
		}

		//起動が完了した場合
		if(line.matches("Done \\(.+\\)! For help, type \"help\"")) {
			switching = false;
			main.log(Name + "サーバーの起動が完了しました");
			main.bridge.sendToDiscord(":ballot_box_with_check: " + Name +  "サーバーの起動が完了しました！");
		}
		//サーバーが停止した場合
		if(line.matches("Saving worlds")) {
			log_getter.stopThread();
			try {
				log_getter.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			main.log(Name + "サーバーが停止しました");
			main.bridge.sendToDiscord(":ballot_box_with_check: " + Name +  "サーバーが停止しました！");

			switching = false;
			enabled = false;
		}
	}
}
