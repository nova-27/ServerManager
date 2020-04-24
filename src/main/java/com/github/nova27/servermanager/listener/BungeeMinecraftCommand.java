package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.command.MinecraftCommandExecutor;
import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.config.Server;
import com.github.nova27.servermanager.request.RequestsManager;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static com.github.nova27.servermanager.listener.BungeeListener.Lobby;

/**
 * BungeeCordコマンド
 */
public class BungeeMinecraftCommand extends MinecraftCommandExecutor {
    private static final String NAME = "ServerManagerForBungeeCord";
    public static final String PERM = "servermanager.command";
    private static final String ALIASES = "smfb";

    private static final String START_PERM = "start";
    private static final String STOP_PERM = "stop";
    public static final String REQUEST_PERM = "request";

    /**
     * コンストラクタ
     */
    public BungeeMinecraftCommand() {
        super(NAME, PERM, ALIASES);
        addSubCommand(new MinecraftSubCommandBuilder("help", this::helpCmd).setDefault(true));
        addSubCommand(new MinecraftSubCommandBuilder("list", this::listCmd));
        addSubCommand(new MinecraftSubCommandBuilder("start", START_PERM, this::startCmd).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("stop", STOP_PERM, this::stopCmd).requireArgs(1));
        addSubCommand(new MinecraftSubCommandBuilder("request", REQUEST_PERM, this::requestCmd).requireArgs(1));
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
        sender.sendMessage(new TextComponent(Messages.BungeeCommand_help_requestcmd.toString()));
    }

    /**
     * リストコマンド
     */
    public void listCmd(CommandSender sender, String[] args) {
        String list = "";
        for (Server server : ConfigData.Server) {
            list += server.Name + "(" + server.ID;
            if(!server.AnotherID.equals("")) {
                list += " & " + server.AnotherID;
            }
            list += "), ";
        }
        sender.sendMessage(new TextComponent(ChatColor.GREEN + list));
    }

    /**
     * サーバースタートコマンド
     */
    public void startCmd(CommandSender sender, String[] args) {
        for (Server server : ConfigData.Server) {
            if (server.ID.equals(args[0]) || server.AnotherID.equals(args[0])) {
                //引数とサーバーがマッチしたら

                String ID = server.ID;
                if(!server.AnotherID.equals("")) {
                    ID += "(" + server.AnotherID + ")";
                }
                if (server.Started) {
                    if (server.Switching) {
                        //起動中だったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_starting.toString(), ID)));
                    } else {
                        //起動済みだったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_started.toString(), ID)));
                    }
                } else {
                    if (server.Switching) {
                        //停止中だったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_stopping.toString(), ID)));
                    } else {
                        //停止済みだったら
                        if (server.Enabled) {
                            //有効だったら
                            sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_start.toString(), ID)));
                            server.Server_On();
                        } else {
                            //無効だったら
                            sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_disabled.toString(), ID)));
                        }
                    }
                }

                return;
            }
        }

        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
    }

    /**
     * サーバーストップコマンド
     */
    public void stopCmd(CommandSender sender, String[] args) {
        for (Server server : ConfigData.Server) {
            if (server.ID.equals(args[0]) || server.AnotherID.equals(args[0])) {
                //引数とサーバーがマッチしたら

                if(Lobby.ID.equals(server.ID)) {
                    //ロビーだったら
                    sender.sendMessage(new TextComponent(Messages.BungeeCommand_lobby_error.toString()));
                    return;
                }

                String ID = server.ID;
                if(!server.AnotherID.equals("")) {
                    ID += "(" + server.AnotherID + ")";
                }
                if (!server.Started) {
                    if (server.Switching) {
                        //停止中だったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_stopping.toString(), ID)));
                    } else {
                        //停止済みだったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_stopped.toString(), ID)));
                    }
                }else{
                    if (server.Switching) {
                        //起動中だったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_starting.toString(), ID)));
                    } else {
                        //起動済みだったら
                        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.ServerStopping_Log.toString(), ID)));
                        server.Exec_command("stop", "", null);
                    }
                }

                return;
            }
        }

        sender.sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
    }

    /**
     * リクエストコマンド
     */
    public void requestCmd(CommandSender sender, String[] args) {
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
    }
}
