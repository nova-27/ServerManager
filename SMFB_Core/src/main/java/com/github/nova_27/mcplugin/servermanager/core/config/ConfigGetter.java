package com.github.nova_27.mcplugin.servermanager.core.config;


import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * config取得クラス
 */
public class ConfigGetter {
    /**
     * configデータを取得する
     * @param plugin_config プラグインのconfig
     */
    public static void ConfigGet(File plugin_config){
        //プラグイン設定ファイルを読み込み
        Configuration plugin_configuration = null;
        try {
            plugin_configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(plugin_config);
        } catch (IOException e) {
            Smfb_core.getInstance().log(Messages.IOError.toString());
        }

        ConfigData.socketPort = plugin_configuration.getInt("SocketPort");
        ConfigData.CloseTime = plugin_configuration.getInt("CloseTime");

        //サーバー登録情報
        List<String> Server_List = plugin_configuration.getStringList("Server_List");
        ConfigData.Servers = new Server[Server_List.size()];

        for(int i =0;  i < Server_List.size(); i++) {
            String Id = Server_List.get(i);
            String Name = plugin_configuration.getString("Server." + Id + ".Name");
            String Dir = plugin_configuration.getString("Server." + Id + ".Dir");
            String File = plugin_configuration.getString("Server." + Id + ".File");
            String Args = plugin_configuration.getString("Server." + Id +  ".Args");

            int Port = 0;
            Map<String, ServerInfo> servers_map = Smfb_core.getInstance().getProxy().getConfig().getServers();
            for(ServerInfo serverInfo : servers_map.values()) {
                if (serverInfo.getName().equals(Id)) {
                    Port = serverInfo.getAddress().getPort();
                    break;
                }
            }

            ConfigData.Servers[i] = new Server(Id, Name, Port, Dir, File, Args);
        }

        //ロビーサーバーを取得
        String lobbyName = null;
        for (ListenerInfo li : Smfb_core.getInstance().getProxy().getConfig().getListeners()) {
            lobbyName = li.getServerPriority().get(0);
        }
        ConfigData.Lobby = Server.getServerByID(lobbyName);
    }
}
