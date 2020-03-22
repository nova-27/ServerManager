package com.github.nova27.servermanager.utils;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.awt.*;

/**
 * MinecraftとDiscordのリンクに関するユーティリティ
 */
public class Bridge {
    private ServerManager main;
    private DiscordSender main_channel;
    private static String sendToMinecraft_format;
    /**
     * マイクラ宛てメッセージの形式を設定
     * @param format 形式
     */
    public static void setSendToMinecraft_format(String format) {
        sendToMinecraft_format = format;
    }

    /**
     * コンストラクタ
     * @param main ServerManagerのオブジェクト
     */
    public Bridge(ServerManager main) {
        this.main = main;
        main_channel = new DiscordSender(main, ConfigData.ChannelId);
        main_channel.start();
    }

    /**
     * Minecraftへメッセージを送信
     * @param message 送信するメッセージ
     */
    public void sendToMinecraft(Message message) {
        ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(sendToMinecraft_format, message.getAuthor().getName(), message.getContentRaw())));
    }

    /**
     * Discordメインチャンネルへメッセージを送信
     * @param message 送信するメッセージ
     */
    public void sendToDiscord(String message) {
        main_channel.add_queue(message);
    }

    /**
     * Discord Embed機能
     * @param to 宛て名
     * @param desc タイトル
     * @param list 内容 TODO
     */
    public void embed(String to, String desc, String[][] list) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(Color.blue);

        eb.setAuthor(to, null, "https://i.pinimg.com/originals/85/78/bf/8578bfd439ef6ee41e103ae82b561986.png");

        eb.setDescription(desc);

        for(String[] obj : list){
            eb.addField(obj[0], obj[1], false);
        }

        main.jda().getTextChannelById(ConfigData.ChannelId).sendMessage(eb.build()).complete();
    }

    /**
     * Discordメインチャンネルへのメッセージ送信を停止
     */
    public void SendToDiscord_stop() {
        main_channel.thread_stop();
        try {
            main_channel.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    /**
     * 文字列の{0},{1},{2},...をargsで置き換える
     * @param original もとの文字
     * @param args 置き換えする文字列
     * @return 置き換えた文字列
     */
    public static String Formatter(String original, String... args) {
        int i = 0;
        for(String arg : args) {
            original = original.replace("{"+i+"}", arg);
            i++;
        }
        return original;
    }
}
