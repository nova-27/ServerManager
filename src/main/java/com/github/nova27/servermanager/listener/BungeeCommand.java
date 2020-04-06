package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.command.CommandExecutor;
import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.config.Server;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * BungeeCordコマンド
 */
public class BungeeCommand extends CommandExecutor {
    private static final String NAME = "ServerManagerForBungeeCord";
    private static final String PERM = "servermanager.command";
    private static final String ALIASES = "smfb";

    private static final String STARTPERM = "start";

    /**
     * コンストラクタ
     */
    public BungeeCommand() {
        super(NAME, PERM, ALIASES);
        addSubCommand(new CommandExecutor.SubCommandBuilder("help", this::helpCmd));
        addSubCommand(new CommandExecutor.SubCommandBuilder("list", this::listCmd));
        addSubCommand(new CommandExecutor.SubCommandBuilder("start", STARTPERM, this::startCmd));
    }

    /**
     * ヘルプコマンド
     */
    public void helpCmd(CommandSender sender, String[] args) {
        sender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_1.toString()));
        sender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_helpcmd.toString()));
        sender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_listcmd.toString()));
        sender.sendMessage(new TextComponent(ChatColor.WHITE + Messages.BungeeCommand_help_startcmd.toString()));
    }

    /**
     * リストコマンド
     */
    public void listCmd(CommandSender sender, String[] args) {
        String list = "";
        for (Server server : ConfigData.Server) {
            list += server.Name + "(" + server.ID + "), ";
        }
        sender.sendMessage(new TextComponent(ChatColor.GREEN + list));
    }

    /**
     * サーバースタートコマンド
     */
    public void startCmd(CommandSender sender, String[] args) {
        for (Server server : ConfigData.Server) {
            if (server.ID.equals(args[0])) {
                //引数とサーバーがマッチしたら

                if (server.Started) {
                    if (server.Switching) {
                        //起動中だったら
                        sender.sendMessage(new TextComponent(ChatColor.YELLOW + Bridge.Formatter(Messages.BungeeCommand_starting.toString(), server.ID)));
                    } else {
                        //起動済みだったら
                        sender.sendMessage(new TextComponent(ChatColor.GREEN + Bridge.Formatter(Messages.BungeeCommand_started.toString(), server.ID)));
                    }
                } else {
                    if (server.Switching) {
                        //停止中だったら
                        sender.sendMessage(new TextComponent(ChatColor.RED + Bridge.Formatter(Messages.BungeeCommand_stopping.toString(), server.ID)));
                    } else {
                        //停止済みだったら
                        if (server.Enabled) {
                            //有効だったら
                            sender.sendMessage(new TextComponent(ChatColor.GREEN + Bridge.Formatter(Messages.BungeeCommand_start.toString(), server.ID)));
                            server.Server_On();
                        } else {
                            //無効だったら
                            sender.sendMessage(new TextComponent(ChatColor.RED + Bridge.Formatter(Messages.BungeeCommand_disabled.toString(), server.ID)));
                        }
                    }
                }

                return;
            }
        }

        sender.sendMessage(new TextComponent(ChatColor.RED + Bridge.Formatter(Messages.BungeeCommand_servernotfound.toString(), args[0])));
    }
}
