package com.github.nova27.servermanager.command;

import net.dv8tion.jda.core.entities.User;

/**
 * Discordコマンドの原型
 */
public interface DiscordCommandBase {
    /**
     * 実行する処理
     * @param user 送信者
     * @param args 引数
     */
    void execute(User user, String[] args);
}
