package com.github.nova_27.mcplugin.servermanager.core.utils;

/**
 * ツール
 */
public class Tools {
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
