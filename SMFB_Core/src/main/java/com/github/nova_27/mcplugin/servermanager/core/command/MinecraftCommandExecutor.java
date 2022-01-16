package com.github.nova_27.mcplugin.servermanager.core.command;

import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Minecraftコマンドの呼び出し等を行うクラス
 */
public class MinecraftCommandExecutor extends Command implements TabExecutor {
    private ArrayList<MinecraftSubCommandBuilder> subCommands;
    private String permission;

    /**
     * コンストラクタ
     * @param name コマンド名
     * @param permission 権限
     * @param aliases エイリアス
     */
    public MinecraftCommandExecutor(String name, String permission, String... aliases) {
        super(name, permission, aliases);
        subCommands = new ArrayList<>();
        this.permission = permission;
    }

    /**
     * サブコマンドを追加する
     * @param builder サブコマンド
     */
    public void addSubCommand(MinecraftSubCommandBuilder builder) {
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
            commandSender.sendMessage(new TextComponent(Messages.BungeeCommand_denied.toString()));
            return;
        }
        //引数の確認
        if(args.length == 0) {
            for(MinecraftSubCommandBuilder subCommand : subCommands) {
                if(subCommand.isDefault) subCommand.action.execute(commandSender, new String[1]);
            }
            return;
        }

        //サブコマンドを選択
        MinecraftSubCommandBuilder execCmd = null;
        for(MinecraftSubCommandBuilder subCommand : subCommands) {
            if(subCommand.alias.equals(args[0])) {
                execCmd = subCommand;
                break;
            }
        }
        if(execCmd == null) {
            commandSender.sendMessage(new TextComponent(Messages.BungeeCommand_notfound.toString()));
            return;
        }

        //権限の確認
        if (execCmd.subPermission != null && !commandSender.hasPermission(execCmd.subPermission)) {
            commandSender.sendMessage(new TextComponent(Messages.BungeeCommand_denied.toString()));
            return;
        }

        String[] commandArgs = new String[args.length - 1];
        for(int i = 1; i <= commandArgs.length; i++) {
            commandArgs[i-1] = args[i];
        }

        //引数の確認
        if(commandArgs.length < execCmd.requireArgs) {
            commandSender.sendMessage(new TextComponent(Messages.BungeeCommand_syntaxerror.toString()));
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

        args[0] = args[0].toLowerCase();
        if(args.length == 1) {
            return subCommands.stream()
                    .map(b -> b.alias)
                    .filter(alias -> alias.startsWith(args[0]))
                    .collect(Collectors.toSet());

        } else {
            for (MinecraftSubCommandBuilder subCommand : subCommands) {
                if (subCommand.alias.equalsIgnoreCase(args[0])) {
                    ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
                    argsList.remove(0);

                    return (subCommand.tabExecutor != null) ?
                            subCommand.tabExecutor.onTabComplete(commandSender, argsList.toArray(new String[0])) : Collections.emptyList();
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * サブコマンドの設定等を保持するクラス
     */
    public class MinecraftSubCommandBuilder {
        private String alias;
        private String subPermission;
        private MinecraftCommandBase action;
        private boolean isDefault;
        private int requireArgs;
        private TabExecutor tabExecutor;

        /**
         * コンストラクタ
         * @param alias エイリアス
         * @param subPermission 権限
         * @param action 実行する処理
         * @param tabExecutor 引数の補完処理
         */
        public MinecraftSubCommandBuilder(String alias, String subPermission, MinecraftCommandBase action, TabExecutor tabExecutor) {
            this.alias = alias;
            this.subPermission = permission + "." + subPermission;
            this.action = action;
            isDefault = false;
            requireArgs = 0;
            this.tabExecutor = tabExecutor;
        }
        public MinecraftSubCommandBuilder(String alias, String subPermission, MinecraftCommandBase action) {
            this.alias = alias;
            this.subPermission = permission + "." + subPermission;
            this.action = action;
            isDefault = false;
            requireArgs = 0;
        }
        public MinecraftSubCommandBuilder(String alias, MinecraftCommandBase action) {
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
        public MinecraftSubCommandBuilder setDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        /**
         * 必要な引数の数を設定する
         * @param cnt 引数の数
         */
        public MinecraftSubCommandBuilder requireArgs(int cnt) {
            requireArgs = cnt;
            return this;
        }
    }
}
