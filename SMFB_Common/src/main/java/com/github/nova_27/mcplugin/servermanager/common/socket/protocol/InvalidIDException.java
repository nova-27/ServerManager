package com.github.nova_27.mcplugin.servermanager.common.socket.protocol;

public class InvalidIDException extends Exception {
    private static final long serialVersionUID = 1L;

    // コンストラクタ
    public InvalidIDException(String msg){
        super(msg);
    }
}
