package com.github.nova27.servermanager.utils.minecraft;

import com.github.nova27.servermanager.config.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * サーバープロセスのログを取得する
 */
public class GetLogsThread extends Thread {
    private String DeleteRegex;

    private Server server;
    private BufferedReader br;
    private StandardEventListener event;

    private boolean stopped = false;

    /**
     * コンストラクタ
     * @param server サーバーのオブジェクト
     * @param event ログ取得後のイベント
     * @param DeleteRegex ログの最初につく無駄な削除する文字列
     */
    public GetLogsThread(Server server, StandardEventListener event, String DeleteRegex) {
        this.server = server;
        br = new BufferedReader(new InputStreamReader(server.Process.getInputStream()));
        this.event = event;
        this.DeleteRegex = DeleteRegex;
    }

    /**
     * スレッドを停止する
     */
    public void thread_stop() {
        stopped = true;
    }

    /**
     * ログを処理する
     */
    public void run() {
        try {
            while (!stopped) {
                if (br.ready()) {
                    //ログを取得
                    String line = br.readLine();
                    if (line == null)
                        break;
                    if (Objects.equals(line, "")) {
                        continue;
                    }

                    //不要な部分を削除
                    line = line.replaceFirst(DeleteRegex, "");

                    if (server.Start_write >= server.BUF_LOG_CNT) {
                        server.Start_write = 0;
                    }
                    //配列に書き込む
                    server.Logs[server.Start_write] = line;
                    server.Start_write++;

                    event.exec(line);
                }

                sleep(100);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
