package com.github.nova27.servermanager.request;

/**
 * リクエストの情報を保持するクラス
 */
public class Request {
    protected String name;
    protected long requestTime;

    /**
     * コンストラクタ
     * @param name プレイヤー名
     */
    protected Request(String name) {
        this.name = name;
        this.requestTime = System.currentTimeMillis();
    }
}
