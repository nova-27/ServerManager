package com.github.nova27.servermanager.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.nova27.servermanager.ServerManager;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class Config_get {
	ServerManager main;

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
		ConfigData.debug = plugin_configuration.getBoolean("debug");

		//サーバー登録情報
		List<String> Server_List = plugin_configuration.getStringList("Server_List");
		String[] Server_array = Server_List.toArray(new String[0]);
		ConfigData.Server_info = new String[Server_array.length][6];
		ConfigData.enabled = new boolean[Server_array.length];

		for(int i =0;  i < Server_array.length; i++) {
			String name = Server_array[i];
			ConfigData.Server_info[i][0] = name;
			ConfigData.Server_info[i][1] = plugin_configuration.getString("Server_Info." + name + ".Dir");
			ConfigData.Server_info[i][2] = plugin_configuration.getString("Server_Info." + name + ".File");
			ConfigData.Server_info[i][3] = String.valueOf(plugin_configuration.getInt("Server_Info." + name + ".Rcon"));
			ConfigData.Server_info[i][4] = plugin_configuration.getString("Server_Info." + name + ".Rcon_passwd");
			ConfigData.Server_info[i][5] = plugin_configuration.getString("Server_Info." + name +  ".Args");

			ConfigData.enabled[i] = true;

			// デバッグ情報を送る
			if(ConfigData.debug) {
				main.log(Arrays.deepToString(ConfigData.Server_info));
			}
		}

		//管理者ユーザーID
		List<Long> UserID_List_Long = plugin_configuration.getLongList("Admin");
		List<String> UserID_List = new ArrayList<String>();
		for (Long i : UserID_List_Long) {
		    UserID_List.add(String.valueOf(i));
		}
		ConfigData.Admin_UserID = UserID_List.toArray(new String[0]);

		//ポート番号の取得
		String host="0.0.0.0:0";
		try (BufferedReader in = new BufferedReader(new FileReader(bungee_config))){
            String line;
            while((line = in.readLine()) != null) {
            	if(line.indexOf("host: ") != -1) {
            		host = line;
            	}
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
		ConfigData.Port = host.split(":")[2];
		//IPの取得
		IP_get IPgetter = new IP_get();
		ConfigData.IP = IPgetter.getIP();
	}
}
