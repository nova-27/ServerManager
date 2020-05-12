package com.github.nova_27.mcplugin.servermanager.core.utils;

/**
 * MinecraftとDiscordのリンクに関するユーティリティ
 */
public class Tools {
    /**
     * プレイヤー数をカウント
     * @param plus 増やす数
     * @return プレイヤー数
     */
    /*public int PlayerCount(int plus) {
        main.p_count += plus;
        return main.p_count;
    }*/

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
