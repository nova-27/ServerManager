package com.github.nova27.servermanager.utils.minecraft;

import com.github.nova27.servermanager.config.Server;

/**
 * 各サーバーにコマンドを送信する
 * コマンドの実行結果を取得後、StandardEventListsner.exec(実行結果[失敗時はnull])を実行する
 */
public class GetCommandResultThread extends Thread {
    private static final int WAIT_TIME = 50;
    private static final int READ_LINE_CNT = 10;
    private static final int TRY_CNT = 3;

    private Server server;
    private String match;
    private StandardEventListener event;

    /**
     *　コンストラクタ
     * @param server サーバーのオブジェクト
     * @param match コマンド結果の形式 正規表現
     * @param event 結果取得後のイベント
     */
    public GetCommandResultThread(Server server, String match, StandardEventListener event) {
        this.server = server;
        this.match = match;
        this.event = event;
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

            for (int i=server.Start_write-1,cnt=1; cnt <= READ_LINE_CNT; i--,cnt++) {
                if(i < 0) {
                    i = server.BUF_LOG_CNT - 1;
                }

                //中身が空っぽの場合、中断
                if(server.Logs[i] == null) {
                    break;
                }

                //実行結果の形式にあっているか確認
                if(server.Logs[i].matches(match)) {
                    event.exec(server.Logs[i]);
                    return;
                }
            }
        }
        event.exec(null);
    }
}
