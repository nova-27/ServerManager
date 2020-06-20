package com.github.nova_27.mcplugin.servermanager.core.utils;

import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
    ServerStopped_minecraft,

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
    BungeeCommand_help_stopcmd,
    BungeeCommand_help_statuscmd,
    BungeeCommand_help_enablecmd,
    BungeeCommand_help_disablecmd,
    BungeeCommand_help_sendcmdCmd,
    BungeeCommand_lobby_error,
    BungeeCommand_denied,

    BungeeCommand_status,
    BungeeCommand_status_process,

    ServerStatus_disabled,
    ServerStatus_started,
    ServerStatus_starting,
    ServerStatus_stopped,
    ServerStatus_stopping,

    EnableDisableCommand_sameflag,
    EnableDisableCommand_changedflag,

    ServerIsNotOnline,
    SentCommand,
    ShowLogs,

    ProcessDied;

    /**
     * propertiesファイルからメッセージを取ってくる
     * @return メッセージ
     */
    @Override
    public String toString() {
        try {
            File message_file = new File(Smfb_core.getInstance().getDataFolder(), "message.yml");
            InputStreamReader fileReader = new InputStreamReader(new FileInputStream(message_file), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(fileReader);

            return new PropertyResourceBundle(reader).getString(name());
        }catch (IOException e) {
            return "";
        }
    }
}