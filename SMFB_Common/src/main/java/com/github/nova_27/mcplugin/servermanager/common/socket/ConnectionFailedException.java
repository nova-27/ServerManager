package com.github.nova_27.mcplugin.servermanager.common.socket;

public class ConnectionFailedException extends Exception {
    private static final long serialVersionUID = 1L;

    // コンストラクタ
    public ConnectionFailedException(String msg){
        super(msg);
    }
}
