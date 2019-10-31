package com.github.nova27.servermanager.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.nova27.servermanager.ServerManager;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Config_get {
	private ServerManager main;

	/** コンストラクタ
	 * @param main ServerManagerのオブジェクト
	 */
	public Config_get(ServerManager main){
		this.main = main;
	}

	/** configデータを格納する
	 * @param plugin_config プラグインのconfig
	 * @param bungee_configuration BungeeCordのconfig
	 */
	public void ConfigGet(File plugin_config, File bungee_config) {
		//プラグイン設定ファイルを読み込み
		Configuration plugin_configuration = null;
		try {
			plugin_configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(plugin_config);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Botの設定
		ConfigData.Token = plugin_configuration.getString("Token");
		ConfigData.ChannelId = plugin_configuration.getLong("ChannelId");
		ConfigData.PlayGame = plugin_configuration.getString("PlayGame");
		ConfigData.ServerName = plugin_configuration.getString("ServerName");
		ConfigData.FirstChar = plugin_configuration.getString("FirstChar");

		ConfigData.close_time = plugin_configuration.getInt("time");

		//サーバー登録情報
		List<String> Server_List = plugin_configuration.getStringList("Server_List");
		String[] Server_array = Server_List.toArray(new String[0]);
		ConfigData.s_info = new Server_info[Server_array.length];
		for(int i = 0; i < Server_array.length; i++) {
			ConfigData.s_info[i] = new Server_info(main);
		}

		for(int i =0;  i < Server_array.length; i++) {
			String name = Server_array[i];
			ConfigData.s_info[i].Name = name;
			ConfigData.s_info[i].Dir = plugin_configuration.getString("Server_Info." + name + ".Dir");
			ConfigData.s_info[i].File = plugin_configuration.getString("Server_Info." + name + ".File");
			ConfigData.s_info[i].Args = plugin_configuration.getString("Server_Info." + name +  ".Args");
			if (plugin_configuration.getBoolean("Server_Info." +name + ".Use_Remote_Console")) {
				ConfigData.s_info[i].Console_ChannelId = plugin_configuration.getLong("Server_Info." +name + ".Console_ChannelId");
			}else {
				ConfigData.s_info[i].Console_ChannelId = null;
			}
		}

		//管理者ユーザーID
		List<Long> UserID_List_Long = plugin_configuration.getLongList("Admin");
		List<String> UserID_List = new ArrayList<String>();
		for (Long i : UserID_List_Long) {
			UserID_List.add(String.valueOf(i));
		}
		ConfigData.Admin_UserID = UserID_List.toArray(new String[0]);

		//IP ポートの取得
		IP_port_get IPortGetter = new IP_port_get();
		ConfigData.IP = IPortGetter.getIP();
		ConfigData.Port = IPortGetter.getPort(bungee_config);
	}
}
