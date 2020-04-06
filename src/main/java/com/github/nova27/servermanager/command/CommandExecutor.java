package com.github.nova27.servermanager.command;

import com.github.nova27.servermanager.utils.Messages;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;

/**
 * コマンドの呼び出し等を行うクラス
 */
public class CommandExecutor extends Command implements TabExecutor {
    private ArrayList<SubCommandBuilder> subCommands;
    private String permission;

    /**
     * コンストラクタ
     * @param name コマンド名
     * @param permission 権限
     * @param aliases エイリアス
     */
    public CommandExecutor(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        subCommands = new ArrayList<>();
        this.permission = permission;
    }

    /**
     * サブコマンドを追加する
     * @param builder サブコマンド
     */
    public void addSubCommand(SubCommandBuilder builder) {
        subCommands.add(builder);
    }

    /**
     * コマンド実行時に呼び出される
     * @param commandSender コマンド送信者
     * @param args 引数
     */
    @Override
    public void execute(CommandSender commandSender, String[] args) {
        //権限の確認
        if(!commandSender.hasPermission(permission)) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_denied.toString()));
            return;
        }
        //引数の確認
        if(args.length == 0) {
            for(SubCommandBuilder subCommand : subCommands) {
                if(subCommand.isDefault) subCommand.action.execute(commandSender, new String[1]);
            }
            return;
        }

        //サブコマンドを選択
        SubCommandBuilder execCmd = null;
        for(SubCommandBuilder subCommand : subCommands) {
            if(subCommand.alias.equals(args[0])) {
                execCmd = subCommand;
                break;
            }
        }
        if(execCmd == null) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_notfound.toString()));
            return;
        }

        //権限の確認
        if (execCmd.subPermission != null && !commandSender.hasPermission(execCmd.subPermission)) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_denied.toString()));
            return;
        }

        String[] commandArgs = null;
        if(args.length >= 2) {
            Arrays.copyOfRange(args, 1, args.length - 1);
        }else{
            commandArgs = new String[1];
        }
        //引数の確認
        if(commandArgs.length < execCmd.requireArgs) {
            commandSender.sendMessage(new TextComponent(ChatColor.RED + Messages.BungeeCommand_syntaxerror.toString()));
            return;
        }

        execCmd.action.execute(commandSender, commandArgs);
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
            for(SubCommandBuilder subCommand : subCommands) {
                if(subCommand.alias.startsWith(args[0])) match.add(subCommand.alias);
            }
        }

        return match;
    }

    /**
     * サブコマンドの設定等を保持するクラス
     */
    public class SubCommandBuilder {
        private String alias;
        private String subPermission;
        private CommandBase action;
        private boolean isDefault;
        private int requireArgs;

        /**
         * コンストラクタ
         * @param alias エイリアス
         * @param subPermission 権限
         * @param action 実行する処理
         */
        public SubCommandBuilder(String alias, String subPermission, CommandBase action) {
            this.alias = alias;
            this.subPermission = permission + "." + subPermission;
            this.action = action;
            isDefault = false;
            requireArgs = 0;
        }
        public SubCommandBuilder(String alias, CommandBase action) {
            this.alias = alias;
            this.subPermission = null;
            this.action = action;
            isDefault = false;
            requireArgs = 0;
        }

        /**
         * デフォルトコマンドを設定する
         * @param isDefault デフォルトか
         */
        public SubCommandBuilder setDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        /**
         * 必要な引数の数を設定する
         * @param cnt 引数の数
         */
        public SubCommandBuilder requireArgs(int cnt) {
            requireArgs = cnt;
            return this;
        }
    }
}
