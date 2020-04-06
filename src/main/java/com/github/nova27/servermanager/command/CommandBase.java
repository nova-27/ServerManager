package com.github.nova27.servermanager.command;

import net.md_5.bungee.api.CommandSender;

/**
 * コマンドの原型
 */
public interface CommandBase {
    /**
     * 実行する処理
     * @param sender 送信者
     * @param args 引数
     */
    void execute(CommandSender sender, String[] args);
}
