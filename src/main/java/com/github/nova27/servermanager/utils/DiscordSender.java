package com.github.nova27.servermanager.utils;

import com.github.nova27.servermanager.ServerManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Discordのチャンネルへメッセージを送信する(同期型)
 */
public class DiscordSender extends Thread {
    private final int QUEUE_BUF = 50;

    private ServerManager main;
    private long ChannelId;
    private int start_write = 0;
    private int start_read = 0;
    private String[] queue = new String[QUEUE_BUF];

    private boolean stopped = false;

    /**
     * コンストラクタ
     * @param main ServerManagerのオブジェクト
     * @param ChannelId メッセージを送信するチャンネルID
     */
    public DiscordSender(ServerManager main, long ChannelId) {
        this.main = main;
        this.ChannelId = ChannelId;
    }

    /**
     * キューに送信するメッセージを追加する
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
     * スレッドを停止する
     */
    public void thread_stop() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            String Messages = "";
            while(true) {
                if (queue[start_read] == null) {
                    //キューを読み込み終わったら
                    break;
                }

                Messages += queue[start_read] + "\n";
                queue[start_read] = null;

                start_read++;
                if (start_read >= QUEUE_BUF) {
                    start_read = 0;
                }
            }

            if(!Messages.equals("")) {
                //制限が2000文字なので1900文字で区切る
                Matcher m = Pattern.compile("[\\s\\S]{1,1900}").matcher(Messages);
                while (m.find()) {
                    main.jda().getTextChannelById(ChannelId).sendMessage(m.group()).complete();
                }
            }

            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
