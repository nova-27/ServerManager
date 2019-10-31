package com.github.nova27.servermanager.Util;

import com.github.nova27.servermanager.config.Server_info;

public class Com_result_thread extends Thread {
	private static final int WAIT_TIME = 50;
	private static final int READ_LINE_CNT = 10;
	private static final int TRY_CNT = 5;

	private Server_info s_info;
	private String match;
	private Com_result_event com_result_event;

	/**
	 * コマンドの実行結果を取得後、com_result_event.got_result(実行結果[失敗時はnull])を実行します。
	 *
	 * @param s_info サーバーのオブジェクト
	 * @param match コマンド結果の形式 正規表現
	 * @param com_result_event 結果取得後のイベント
	 */
	public Com_result_thread(Server_info s_info, String match, Com_result_event com_result_event) {
		this.s_info = s_info;
		this.match = match;
		this.com_result_event = com_result_event;
	}

	/**
	 * 実行結果の取得
	 */
	@Override
	public void run() {
		for(int t_cnt=1; t_cnt <= TRY_CNT; t_cnt++) {
			//待機
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			for (int i=s_info.start_write-1,cnt=1; cnt <= READ_LINE_CNT; i--,cnt++) {
				if(i < 0) {
					i = s_info.BUF_LOG_CNT - 1;
				}

				//中身が空っぽの場合、中断
				if(s_info.logs[i] == null) {
					break;
				}

				//実行結果の形式にあっているか確認
				if(s_info.logs[i].matches(match)) {
					com_result_event.got_result(s_info.logs[i]);
					return;
				}
			}
		}
		com_result_event.got_result(null);
	}
}
