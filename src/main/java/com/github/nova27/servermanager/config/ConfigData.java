package com.github.nova27.servermanager.config;

/**
 * プラグイン用データ群
 */
public class ConfigData {
    //Discordボットの設定
    public static String Token = "";
    public static long ChannelId = 0L;
    public static String PlayingGame = "";
    public static String ServerName = "";
    public static String FirstString = "";

    //Discord 管理者のユーザーID
    public static String[] Admin_UserID;

    //Minecraftサーバー設定
    public static int CloseTime = 0;
    public static String IP = "";
    public static String Port = "";
    public static Server[] Server;
}
