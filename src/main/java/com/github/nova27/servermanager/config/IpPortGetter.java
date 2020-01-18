package com.github.nova27.servermanager.config;

import java.io.*;
import java.net.URL;

/**
 * IPアドレスとポートを取得する
 */
public class IpPortGetter {

    /**
     * IPアドレスを取得する
     * @return IPアドレス
     */
    protected String getIP() {
        URL whatismyip = null;
        BufferedReader in = null;
        String ip = "Error";
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            ip = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return ip;
    }

    /**
     * BungeeCordサーバーのポート番号を取得する
     * @param bungee_config bungeecordのconfig
     * @return ポート番号
     */
    protected String getPort(File bungee_config) {
        String host="0.0.0.0:0";
        try (BufferedReader in = new BufferedReader(new FileReader(bungee_config))){
            String line;
            while((line = in.readLine()) != null) {
                if(line.indexOf("host: ") != -1) {
                    host = line;
                    break;
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        return host.split(":")[2];
    }
}
