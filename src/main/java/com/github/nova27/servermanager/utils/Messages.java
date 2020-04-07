package com.github.nova27.servermanager.utils;

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
    BungeeConfigNotFound,
    IOError,
    ConfigLoading,
    FailBotLogin,

    ConnectedToDiscord_Log,
    ConnectedToDiscord_Discord,

    JoinedTheGame,
    LeavedTheGame,
    TimerStarted_Log,
    TimerStarted_Discord,
    TimerStarted_Minecraft,
    TimerStopped_Log,
    TimerStopped_Discord,
    TimerStopped_Minecraft,

    BungeeNotStarted,
    BungeeSwitching,

    TimerRestarted_Log,
    TimerRestarted_Discord,
    TimerRestarted_Minecraft,
    ServerStarting_Log,
    ServerStarting_Discord,
    ServerStarting_Minecraft,
    ServerStarted_Log,
    ServerStarted_Discord,
    ServerStarted_Minecraft,
    ServerStopping_Log,
    ServerStopping_Discord,
    ServerStopping_Minecraft,
    ServerStopped_Log,
    ServerStopped_Discord,
    ServerStopped_Minecraft,

    AllServerStopping_Log,
    AllServerStopping_Discord,
    ProxyStopped,

    HelpCommand_desc,
    HelpCommand_help,
    HelpCommand_status,
    HelpCommand_info,
    HelpCommand_enabled,

    InfoCommand_desc,
    InfoCommand_IP,
    InfoCommand_Port,

    StatusCommand_desc,
    StatusCommand_bungee,
    StatusCommand_playercnt,
    StatusCommand_id,
    StatusCommand_serverStatus,

    EnabledCommand_permission,
    EnabledCommand_syntaxerror,
    EnabledCommand_sameflag,
    EnabledCommand_changedflag,
    EnabledCommand_notfound,
    EnabledCommand_tried_lobbyenabled_change,

    Command_notfound,
    Wait_Discord,

    BungeeCommand_starting,
    BungeeCommand_started,
    BungeeCommand_stopping,
    BungeeCommand_stopped,
    BungeeCommand_start,
    BungeeCommand_disabled,
    BungeeCommand_servernotfound,
    BungeeCommand_notfound,
    BungeeCommand_syntaxerror,
    BungeeCommand_help_1,
    BungeeCommand_help_helpcmd,
    BungeeCommand_help_listcmd,
    BungeeCommand_help_startcmd,
    BungeeCommand_denied,

    ServerStatus_disabled,
    ServerStatus_started,
    ServerStatus_starting,
    ServerStatus_stopped,
    ServerStatus_stopping;

    /**
     * propertiesファイルからメッセージを取ってくる
     * @return メッセージ
     */
    @Override public String toString() {
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
