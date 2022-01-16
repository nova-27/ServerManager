package com.github.nova_27.mcplugin.servermanager.core.listener;

import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.command.MinecraftCommandExecutor;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import com.github.nova_27.mcplugin.servermanager.core.events.ServerEvent;
import com.github.nova_27.mcplugin.servermanager.core.socket.ClientConnection;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import com.github.nova_27.mcplugin.servermanager.core.utils.Requester;
import com.github.nova_27.mcplugin.servermanager.core.utils.Tools;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * BungeeCordコマンド
 */
public class BungeeMinecraftCommand extends MinecraftCommandExecutor {
    private static final String NAME = "ServerManagerForBungeeCord";
    public static final String PERM = "servermanager.command";
    private static final String ALIASES = "smfb";

    private static final String START_PERM = "start";
    private static final String STOP_PERM = "stop";
    private static final String STATUS_PERM = "status";
    private static final String ENABLE_PERM = "enable";
    private static final String DISABLE_PERM = "disable";
    private static final String SEND_CMD_PERM = "send-cmd";

    /**
     * コンストラクタ
     */
    public BungeeMinecraftCommand() {
        super(NAME, PERM, ALIASES);
        addSubCommand(new MinecraftSubCommandBuilder("help", this::helpCmd).setDefault(true));
        addSubCommand(new MinecraftSubCommandBuilder("list", this::listCmd));
        addSubCommand(new MinecraftSubCommandBuilder("start", START_PERM, this::startCmd, this::completeServers).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("stop", STOP_PERM, this::stopCmd, this::completeServers).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("status", STATUS_PERM, this::statusCmd, this::completeServers).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("enable", ENABLE_PERM, this::enableCmd, this::completeServers).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("disable", DISABLE_PERM, this::disableCmd, this::completeServers).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("send-cmd", SEND_CMD_PERM, this::sendcmd_Cmd, this::completeServers).requireArgs(2));
    }

    /**
     * ヘルプコマンド
     */
    public void helpCmd(CommandSender sender, String[] args) {
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_1.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_helpcmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_listcmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_startcmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_stopcmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_statuscmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_enablecmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_disablecmd.toString()));
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_sendcmdCmd.toString()));
    }

    /**
     * リストコマンド
     */
    public void listCmd(CommandSender sender, String[] args) {
        String list = "";
        for (Server server : ConfigData.Servers) {
            if(!server.Enabled) {
                //無効だったら
                list += "§c" + server.Name + "(" + server.ID + "), ";
            }else if(!server.Started) {
                //停止していたら
                list += "§e" + server.Name + "(" + server.ID + "), ";
            }else {
                list += "§9" + server.Name + "(" + server.ID + "), ";
            }
        }
        sender.sendMessage(new TextComponent(list));
    }

    /**
     * サーバースタートコマンド
     */
    public void startCmd(CommandSender sender, String[] args) {
        Server server = Server.getServerByID(args[0]);
        //サーバーが見つからなかったら
        if(server == null) {
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        String ID = server.ID;
        if (server.Started) {
            if (server.Switching) {
                //起動中だったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_starting.toString(), ID)));
            } else {
                //起動済みだったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_started.toString(), ID)));
            }
        } else {
            if (server.Switching) {
                //停止中だったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_stopping.toString(), ID)));
            } else {
                //停止済みだったら
                if (server.Enabled) {
                    //有効だったら
                    if (server.StartServer(Requester.of(sender))) {
                        //起動リクエストに成功したら
                        sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_start.toString(), ID)));
                    }
                } else {
                    //無効だったら
                    sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_disabled.toString(), ID)));
                }
            }
        }
    }

    /**
     * サーバーストップコマンド
     */
    public void stopCmd(CommandSender sender, String[] args) {
        Server server = Server.getServerByID(args[0]);
        //サーバーが見つからなかったら
        if(server == null) {
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        if(ConfigData.Lobby.ID.equals(server.ID)) {
            //ロビーだったら
            sender.sendMessage(new TextComponent(Messages.BungeeCommand_lobby_error.toString()));
            return;
        }

        String ID = server.ID;
        if (!server.Started) {
            if (server.Switching) {
                //停止中だったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_stopping.toString(), ID)));
            } else {
                //停止済みだったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_stopped.toString(), ID)));
            }
        }else{
            if (server.Switching) {
                //起動中だったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_starting.toString(), ID)));
            } else {
                //起動済みだったら
                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.ServerStopping_log.toString(), ID)));
                Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new ServerEvent(server, ServerEvent.EventType.ServerStopping));
                server.StopServer();
            }
        }
    }

    /**
     * ステータスコマンド
     */
    public void statusCmd(CommandSender sender, String[] args) {
        Server server = Server.getServerByID(args[0]);
        //サーバーが見つからなかったら
        if(server == null) {
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        //状態の修正
        server.AliveCheck();

        //メッセージ
        sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_status.toString(), args[0])));

        if(server.Process == null || server.Process.isAlive()) {
            //プロセスが生きていたら
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_status_process.toString(), server.Status())));
        }else{
            //死んでいたら
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_status_process.toString(), server.Status() + " exitCode=" + server.Process.exitValue())));
        }
    }

    /**
     * サーバー有効化コマンド
     */
    public void enableCmd(CommandSender sender, String[] args) {
        Server server = Server.getServerByID(args[0]);
        //サーバーが見つからなかったら
        if(server == null) {
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        if(ConfigData.Lobby.ID.equals(server.ID)) {
            //ロビーだったら
            sender.sendMessage(new TextComponent(Messages.BungeeCommand_lobby_error.toString()));
            return;
        }

        if(server.Enabled) {
            //変更がなければ
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.EnableDisableCommand_sameflag.toString(), server.Name, "true")));
            return;
        }

        //フラグを変更する
        server.Enabled = true;
        sender.sendMessage(new TextComponent(Tools.Formatter(Messages.EnableDisableCommand_changedflag.toString(), server.Name, "true")));
        Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new ServerEvent(server, ServerEvent.EventType.ServerEnabled));
    }

    /**
     * サーバー無効化コマンド
     */
    public void disableCmd(CommandSender sender, String[] args) {
        Server server = Server.getServerByID(args[0]);
        //サーバーが見つからなかったら
        if(server == null) {
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        if(ConfigData.Lobby.ID.equals(server.ID)) {
            //ロビーだったら
            sender.sendMessage(new TextComponent(Messages.BungeeCommand_lobby_error.toString()));
            return;
        }

        if(!server.Enabled) {
            //変更がなければ
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.EnableDisableCommand_sameflag.toString(), server.Name, "true")));
            return;
        }

        server.StopServer();
        server.Enabled = false;
        sender.sendMessage(new TextComponent(Tools.Formatter(Messages.EnableDisableCommand_changedflag.toString(), server.Name, "true")));
        Smfb_core.getInstance().getProxy().getPluginManager().callEvent(new ServerEvent(server, ServerEvent.EventType.ServerDisabled));
    }

    /**
     * コマンド送信コマンド
     */
    public void sendcmd_Cmd(CommandSender sender, String[] args) {
        Server server = Server.getServerByID(args[0]);
        //サーバーが見つからなかったら
        if(server == null) {
            sender.sendMessage(new TextComponent(Tools.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        //コマンドを構成
        String command = "";
        for(int i = 1; i < args.length; i++) {
            command += args[i] + " ";
        }

        //コマンドを送信
        for(ClientConnection cc : Smfb_core.getInstance().getSocketServer().getClientConnections()) {
            if(Objects.equals(cc.getSrcServer(), server)) { ;
                cc.addQueue(PacketID.SendCommand, command.getBytes(StandardCharsets.UTF_8));

                sender.sendMessage(new TextComponent(Tools.Formatter(Messages.SentCommand.toString(), server.Name)));
                sender.sendMessage(new TextComponent(Messages.ShowLogs.toString()));
                Smfb_core.getInstance().getProxy().getScheduler().schedule(Smfb_core.getInstance(), ()-> sender.sendMessage(new TextComponent(server.getLatestLog(5))), 2L, TimeUnit.SECONDS);

                return;
            }
        }

        sender.sendMessage(new TextComponent(Tools.Formatter(Messages.ServerIsNotOnline.toString(), server.Name)));
    }

    /**
     * リクエストコマンド
     */
    /*public void requestCmd(CommandSender sender, String[] args) {
        if(ConfigData.requestRequired == 0) {
            //リクエスト機能が無効だったら
            sender.sendMessage(new TextComponent(Messages.BungeeCommand_request_disabled.toString()));
            return;
        }

        Server requestServer = null;
        for(Server server : ConfigData.Server) {
            if(server.ID.equals(args[0]) || server.AnotherID.equals(args[0])) {
                requestServer = server;
                break;
            }
        }

        if(requestServer == null) {
            //サーバーが見つからなかったら
            sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
            return;
        }

        String ID = requestServer.ID;
        if(!requestServer.AnotherID.equals("")) {
            ID += "(" + requestServer.AnotherID + ")";
        }
        if(!requestServer.Enabled) {
            //サーバーが無効だったら
            sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_disabled.toString(), ID)));
            return;
        }

        if(requestServer.Started) {
            //起動していたら
            sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_started.toString(), ID)));
            return;
        }

        RequestsManager.RequestsStats stats = RequestsManager.getRequestsStats(sender.getName());

        if(stats.getRequestServers().contains(requestServer)) {
            //リクエスト済みだったら
            sender.sendMessage(new TextComponent(Messages.BungeeCommand_already_requested.toString()));
            return;
        }

        if(stats.getLastRequestTime() + ConfigData.requestDelay > System.currentTimeMillis()) {
            //再リクエスト待機時間が経過していなかったら
            sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_request_wait.toString(), (ConfigData.requestDelay / 1000 / 60) + "")));
            return;
        }

        RequestsManager.addRequest(sender.getName(), requestServer);
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_request_successful.toString()));

        if(!requestServer.Started) {
            //リクエストが承認されていないときだけアナウンス
            Timestamp expiration_date = new Timestamp(requestServer.lastRequest + ConfigData.requestWait);
            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd HH:mm:ss");
            String expiration_String = sdf.format(expiration_date);
            ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_new_request.toString(), requestServer.Name, requestServer.requests.size() + "", expiration_String)));
        }
    }*/


    private Iterable<String> completeServers(CommandSender sender, String[] args) {
        if (args.length == 1) {
            String argument = args[0].toLowerCase(Locale.ROOT);
            return Stream.of(ConfigData.Servers)
                    .map(s -> s.ID)
                    .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(argument))
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

}
