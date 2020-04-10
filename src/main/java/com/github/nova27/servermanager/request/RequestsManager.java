package com.github.nova27.servermanager.request;

import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.config.Server;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.Messages;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * サーバーオープンリクエストを管理する
 */
public class RequestsManager {
    /**
     * リクエストを追加する
     * @param name 名前
     * @param server サーバークラス
     */
    public static void addRequest(String name, Server server) {
        long now = System.currentTimeMillis();

        if(server.lastRequest != 0 && server.lastRequest + ConfigData.requestWait < now) {
            //最終リクエストから制限時間が経っていたら
            ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_request_expired.toString(), server.Name)));
            server.requests = new ArrayList<>();
        }

        Request request = new Request(name);
        server.requests.add(request);
        server.lastRequest = now;

        if(server.requests.size() >= ConfigData.requestRequired) {
            //リクエスト承認人数を越していたら
            ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_request_approval.toString(), server.Name)));
            server.Server_On();
        }
    }

    /**
     * リクエストに関する情報を返す
     * @return リクエスト統計クラス
     */
    public static @Nullable RequestsStats getRequestsStats(String name) {
        ArrayList<Server> requestServers = new ArrayList<>();
        long lastRequestTime = 0L;

        for(Server server : ConfigData.Server) {
            for(Request request : server.requests) {
                if(request.name.equals(name)) {
                    requestServers.add(server);
                    if(request.requestTime > lastRequestTime) lastRequestTime = request.requestTime;
                }
            }
        }

        RequestsStats result = new RequestsStats(requestServers, lastRequestTime);
        return result;
    }

    /**
     * リクエスト情報
     */
    public static class RequestsStats {
        private ArrayList<Server> requestServers;
        private long lastRequestTime;

        private RequestsStats(ArrayList<Server> requestServers, long lastRequestTime) {
            this.requestServers = requestServers;
            this.lastRequestTime = lastRequestTime;
        }

        public ArrayList<Server> getRequestServers() {
            return requestServers;
        }

        public long getLastRequestTime() {
            return lastRequestTime;
        }
    }
}
