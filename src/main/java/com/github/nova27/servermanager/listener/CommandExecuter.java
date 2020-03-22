package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.config.Server;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BungeeCordコマンド管理クラス
 */
public class CommandExecuter extends Command implements TabExecutor {
    private static final String Name = "ServerManagerForBungeeCord";
    private static final String Perm = "servermanager.command";
    private static final String Aliases = "smfb";

    private static final String StartPerm = "servermanager.command.start";

    /**
     * コンストラクタ
     */
    public CommandExecuter() {
        super(Name, Perm, Aliases);
    }

    /**
     * コマンドが実行されたら
     * @param commandSender 送信者
     * @param args 引数
     */
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            //引数が足りなかったら構文エラー
            commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_syntaxerror.toString()));
            return;
        }

        args[0] = args[0].toLowerCase();
        if (args[0].equals("help")) {
            //helpコマンド
            commandSender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_1.toString()));
            commandSender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_helpcmd.toString()));
            commandSender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_listcmd.toString()));
            commandSender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_startcmd.toString()));
        }else if(args[0].equals("list")) {
            //listコマンド
            String list = "";
            for (Server server : ConfigData.Server) {
                list += server.Name+",";
            }
            commandSender.sendMessage(new TextComponent(ChatColor.GREEN + list));
        }else if (args[0].equals("start")) {
            //サーバー起動コマンドだったら

            if(!commandSender.hasPermission(StartPerm)) {
                //コマンド実行権限を持っていない
                commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_denied.toString()));
                return;
            }

            if (args.length < 2) {
                //引数が足りなかったら構文エラー
                commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_syntaxerror.toString()));
                return;
            }

            for (int i = 0; i < ConfigData.Server.length; i++) {
                if (ConfigData.Server[i].ID.equals(args[1])) {
                    //引数にマッチするサーバーがあったら
                    Server server = ConfigData.Server[i];

                    if (server.Started) {
                        if (server.Switching) {
                            //起動中だったら
                            commandSender.sendMessage(new TextComponent(ChatColor.YELLOW + Bridge.Formatter(Messages.BungeeCommand_starting.toString(), server.ID)));
                        } else {
                            //起動済みだったら
                            commandSender.sendMessage(new TextComponent(ChatColor.GREEN + Bridge.Formatter(Messages.BungeeCommand_started.toString(), server.ID)));
                        }
                    } else {
                        if (server.Switching) {
                            //停止中だったら
                            commandSender.sendMessage(new TextComponent(ChatColor.RED + Bridge.Formatter(Messages.BungeeCommand_stopping.toString(), server.ID)));
                        } else {
                            //停止済みだったら
                            if (server.Enabled) {
                                //有効だったら
                                commandSender.sendMessage(new TextComponent(ChatColor.GREEN + Bridge.Formatter(Messages.BungeeCommand_start.toString(), server.ID)));
                                server.Server_On();
                            } else {
                                //無効だったら
                                commandSender.sendMessage(new TextComponent(ChatColor.RED + Bridge.Formatter(Messages.BungeeCommand_disabled.toString(), server.ID)));
                            }
                        }
                    }

                    return;
                }
            }

            commandSender.sendMessage(new TextComponent(ChatColor.RED + Bridge.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[1])));
        } else {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_notfound.toString()));
        }
    }

    /**
     * タブ補完機能
     * @param commandSender 送信者
     * @param args 引数
     * @return 補完リスト
     */
    @Override
    public Iterable<String> onTabComplete(CommandSender commandSender, String[] args) {
        //引数がなかったら（「smfb」だけだったら無視）
        if (args.length == 0) {
            return Collections.emptyList();
        }

        Set<String> match = new HashSet();
        args[0] = args[0].toLowerCase();
        if(args.length == 1) {
            if ("help".startsWith(args[0])) match.add("help");
            if ("list".startsWith(args[0])) match.add("list");
            if ("start".startsWith(args[0])) match.add("start");
        }else if(args.length == 2) {
            if ("start".startsWith(args[0])) {
                for (Server server : ConfigData.Server) {
                    match.add(server.Name);
                }
            }
        }

        return match;
    }
}
