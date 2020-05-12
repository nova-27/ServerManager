package com.github.nova_27.mcplugin.servermanager.core.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.PropertyResourceBundle;

/**
 * 多言語対応メッセージ
 */
public enum Messages {
    ChangedLang,
    UnsupportedOS,

    ConfigNotFound,
    IOError,
    ConfigLoading,

    ServerSocketStarted,

    TimerRestarted_log,
    TimerRestarted_Minecraft,
    TimerStarted_log,
    TimerStarted_Minecraft,
    TimerStopped_log,
    TimerStopped_Minecraft,

    LobbyNotStarted,
    LobbySwitching,

    ServerStarting_log,
    ServerStarting_minecraft,
    ServerStarted_log,
    ServerStarted_minecraft,
    ServerStopping_log,
    ServerStopping_Minecraft,
    ServerStopped_log,
    ServerStopped_minecraft;

    /**
     * propertiesファイルからメッセージを取ってくる
     * @return メッセージ
     */
    @Override
    public String toString() {
        try {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(Locale.getDefault().toString()+".properties");
                 InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                 BufferedReader reader = new BufferedReader(isr)) {
                return new PropertyResourceBundle(reader).getString(name());
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}