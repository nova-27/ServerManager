package com.github.nova27.servermanager.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

import com.github.nova27.servermanager.config.Server_info;

public class Log_getter_thread extends Thread {
	private Server_info s_info;
	private BufferedReader br;
	private Log_getter_event event;
	private boolean isActive = true;

	/**
	 * サーバープロセスのログを取得します
	 *
	 * @param s_info サーバーのオブジェクト
	 * @param event ログ取得後のイベント
	 */
	public Log_getter_thread(Server_info s_info, Log_getter_event event) {
		this.s_info = s_info;
		br = new BufferedReader(new InputStreamReader(s_info.server.getInputStream()));
		this.event = event;
	}

	/**
	 * スレッドをストップする
	 */
	public void stopThread() {
		isActive  = false;
	}

	/**
	 * ログを処理する
	 */
	@Override
	public void run() {
		try {
			while(isActive) {
				if(br.ready()) {
					//ログを取得
					String line = br.readLine();
					if (line == null) 	break;
					if (Objects.equals(line, "")) {
						continue;
					}

					//不要な部分を削除
					line = line.replaceFirst("\\[[0-9]+:[0-9]+:[0-9]+ .+\\]:\\s+", "");

					if(s_info.start_write >= s_info.BUF_LOG_CNT) {
						s_info.start_write = 0;
					}
					//配列に書き込む
					s_info.logs[s_info.start_write] = line;
					s_info.start_write++;

					event.log_got(line);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
