package com.github.nova27.servermanager.command;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.utils.Messages;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;

/**
 * Discordコマンドの呼び出し等を行うクラス
 */
public class DiscordCommandExecutor {
    private ArrayList<DiscordSubCommandBuilder> subCommands = new ArrayList<>();
    private ServerManager main;

    /**
     * コンストラクタ
     * @param main ServerManagerオブジェクト
     */
    public DiscordCommandExecutor(ServerManager main) {
        this.main = main;
    }

    /**
     * サブコマンドを追加する
     * @param builder サブコマンド
     */
    public void addSubCommand(DiscordSubCommandBuilder builder) {
        subCommands.add(builder);
    }

    /**
     * コマンドの実行
     */
    public void executeCmd(User user, String command, String[] args) {
        //サブコマンドの検索
        DiscordSubCommandBuilder exec = null;
        for(DiscordSubCommandBuilder subCommand : subCommands) {
            if(subCommand.alias.equals(command.toLowerCase())) exec = subCommand;
        }
        if(exec == null) {
            //見つからなかったら
            main.bridge.sendToDiscord(Messages.Command_notfound.toString());
            return;
        }

        //Adminか確認する
        if (exec.onlyFromAdmin && !isAdmin(user.getId())) {
            main.bridge.sendToDiscord(Messages.EnabledCommand_permission.toString());
            return;
        }
        //引数の確認
        if(args.length < exec.requireArgs) {
            main.bridge.sendToDiscord(Messages.EnabledCommand_syntaxerror.toString());
            return;
        }

        exec.action.execute(user, args);
    }

    /**
     * Adminかどうか確認する
     * @param ID 確認するユーザーID
     * @return Adminならtrue
     */
    public boolean isAdmin(String ID) {
        for (String adminID : ConfigData.Admin_UserID) {
            if(ID.equals(adminID)) return true;
        }

        return false;
    }

    /**
     * サブコマンドの設定等を保持するクラス
     */
    public static class DiscordSubCommandBuilder {
        private String alias;
        private DiscordCommandBase action;
        private int requireArgs;
        private boolean onlyFromAdmin;

        /**
         * コンストラクタ
         * @param alias エイリアス
         * @param action 実行する処理
         */
        public DiscordSubCommandBuilder(String alias, DiscordCommandBase action) {
            this.alias = alias;
            this.action = action;
            requireArgs = 0;
            onlyFromAdmin = false;
        }

        /**
         * 必要な引数の数を設定する
         * @param cnt 引数の数
         */
        public DiscordSubCommandBuilder requireArgs(int cnt) {
            requireArgs = cnt;
            return this;
        }

        /**
         * Adminから実行可能かを設定する
         * @param onlyFromAdmin Adminからのみか
         */
        public DiscordSubCommandBuilder setOnlyFromAdmin(boolean onlyFromAdmin) {
            this.onlyFromAdmin = onlyFromAdmin;
            return this;
        }
    }
}
