package com.github.nova27.servermanager.config;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.listener.BungeeListener;
import com.github.nova27.servermanager.listener.DiscordListener;
import com.github.nova27.servermanager.utils.Messages;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * config取得クラス
 */
public class ConfigGetter {
    /**
     * configデータを取得する
     * @param main ServerManagerのオブジェクト
     * @param plugin_config プラグインのconfig
     * @param bungee_config BungeeCordのconfig
     */
    public static void ConfigGet(ServerManager main, File plugin_config, File bungee_config){
        //プラグイン設定ファイルを読み込み
        Configuration plugin_configuration = null;
        try {
            plugin_configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(plugin_config);
        } catch (IOException e) {
            main.log(Messages.IOError.toString());
            e.printStackTrace();
            main.pl_enabled = false;
            main.onDisable();
        }

        //Botの設定
        ConfigData.Token = plugin_configuration.getString("Token");
        ConfigData.ChannelId = plugin_configuration.getLong("ChannelId");
        ConfigData.PlayingGame = plugin_configuration.getString("PlayingGame");
        ConfigData.ServerName = plugin_configuration.getString("ServerName");
        ConfigData.FirstString = plugin_configuration.getString("FirstString");

        ConfigData.CloseTime = plugin_configuration.getInt("CloseTime");

        //相互送信形式の設定
        BungeeListener.setSendToDiscordFormat(plugin_configuration.getString("ToDiscord"));
        DiscordListener.setSendToMinecraft_format(plugin_configuration.getString("ToMinecraft"));

        //サーバー登録情報
        List<String> Server_List = plugin_configuration.getStringList("Server_List");
        ConfigData.Server = new Server[Server_List.size()];

        for(int i =0;  i < Server_List.size(); i++) {
            String Id = Server_List.get(i);
            String AnotherID = plugin_configuration.getString("Server." + Id + ".AnotherID");
            String Name = plugin_configuration.getString("Server." + Id + ".Name");
            String Dir = plugin_configuration.getString("Server." + Id + ".Dir");
            String File = plugin_configuration.getString("Server." + Id + ".File");
            String Args = plugin_configuration.getString("Server." + Id +  ".Args");
            long Console_ChannelId;
            if (plugin_configuration.getBoolean("Server." + Id + ".Use_Remote_Console")) {
                Console_ChannelId = plugin_configuration.getLong("Server." + Id + ".Console_ChannelId");
            }else {
                Console_ChannelId = 0L;
            }
            String ServerStartingDone = plugin_configuration.getString("Server." + Id + ".advanced.LogServerStartingDone");
            String ServerStopped = plugin_configuration.getString("Server." + Id + ".advanced.LogServerStopped");
            String LogDeleteRegex = plugin_configuration.getString("Server." + Id + ".advanced.LogDeleteRegex");
            String LogListCmd = plugin_configuration.getString("Server." + Id + ".advanced.LogListCmd");

            ConfigData.Server[i] = new Server(main, Id, AnotherID, Name, Dir, File, Args, Console_ChannelId, ServerStartingDone, ServerStopped, LogDeleteRegex, LogListCmd);
        }

        //管理者ユーザーID
        List<Long> UserID_List_Long = plugin_configuration.getLongList("Admin");
        List<String> UserID_List = new ArrayList<>();
        for (Long i : UserID_List_Long) {
            UserID_List.add(String.valueOf(i));
        }
        ConfigData.Admin_UserID = UserID_List.toArray(new String[0]);

        //IP ポートの取得
        IpPortGetter IPortGetter = new IpPortGetter();
        ConfigData.IP = IPortGetter.getIP();
        ConfigData.Port = IPortGetter.getPort(bungee_config);

        //ロビーサーバー名を取得
        String lobby_name = getLobbyName(bungee_config);
        for(Server server : ConfigData.Server) {
            if (lobby_name.equals(server.ID)) {
                BungeeListener.Lobby = server;
                break;
            }
        }

        //リクエスト設定を取得
        ConfigData.requestRequired = plugin_configuration.getInt("NumberOf_RequestsRequired");
        int requestWaitMinutes = plugin_configuration.getInt("RequestWaitTime");
        ConfigData.requestWait = requestWaitMinutes * 60 * 1000;
        int requestDelayMinutes = plugin_configuration.getInt("RequestDelay");
        ConfigData.requestDelay = requestDelayMinutes * 60 * 1000;
    }

    /**
     * ロビーサーバーを特定する
     */
    private static String getLobbyName(File bungee_config) {
        String name="  - lobby";
        try (BufferedReader in = new BufferedReader(new FileReader(bungee_config))){
            String line;
            while((line = in.readLine()) != null) {
                if(line.contains("priorities:")) {
                    name = in.readLine();
                    break;
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return name.replaceAll("\\s*\\-\\s*", "");
    }
}
