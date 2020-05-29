package com.github.nova_27.mcplugin.servermanager.core.config;

import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.socket.ClientConnection;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;
import com.github.nova_27.mcplugin.servermanager.core.utils.Tools;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Server {
    //Minecraftサーバー設定
    public String ID;
    public String Name;
    public int Port;
    private String Dir;
    private String File;
    private String Args;

    //サーバープロセス
    public Process Process = null;
    public boolean Started = false;
    public boolean Switching = false;
    public boolean Enabled = true;

    //タイマー
    private TimerTask task = null;
    private Timer timer = null;

    /**
     * コンストラクタ
     * @param ID サーバーID（BungeeCordと同一）
     * @param Name サーバー名（表示用）
     * @param Port サーバーポート番号
     * @param Dir サーバーのルートディレクトリ
     * @param File サーバー本体（jar）
     * @param Args 実行引数
     */
    public Server(String ID, String Name, int Port, String Dir, String File, String Args) {
        this.ID = ID;
        this.Name = Name;
        this.Port = Port;
        this.Dir = Dir;
        this.File = File;
        this.Args = Args;
    }

    /**
     * サーバーをスタートする
     */
    public void StartServer() {
        //有効かつ未処理で開始されていないときは開始
        if(!Started && !Switching && Enabled) {
            try {
                Started = true;
                Switching = true;

                String OS_NAME = System.getProperty("os.name").toLowerCase();
                if(OS_NAME.startsWith("linux")) {
                    //Linuxの場合
                    Process = new ProcessBuilder("/bin/bash","-c","cd  " + Dir + " ; java -jar " + Args + " " + File).start();
                }else if(OS_NAME.startsWith("windows")) {
                    //Windowsの場合
                    Runtime r = Runtime.getRuntime();
                    Process = r.exec("cmd /c cd " + Dir + " && java -jar " + Args + " " + File);
                }

                Smfb_core.getInstance().log(Tools.Formatter(Messages.ServerStarting_log.toString(), Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.ServerStarting_minecraft.toString(), Name)));
            } catch (IOException e) {
                Smfb_core.getInstance().log(Messages.IOError.toString());
            }
        }
    }

    public void StopServer() {
        for(ClientConnection cc : Smfb_core.getInstance().getSocketServer().getClientConnections()) {
            if(Objects.equals(cc.getSrcServer(), this)) cc.addQueue(PacketID.ServerStopRequest, new byte[1]);
        }
    }

    /**
     * タイマーの起動
     * @return true タイマーが起動していなかった
     */
    public boolean StartTimer() {
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    if(!Switching) {
                        //処理中でなかったら
                        Smfb_core.getInstance().log(Tools.Formatter(Messages.ServerStopping_log.toString(), Name));
                        ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.ServerStopping_Minecraft.toString(), Name)));

                        StopServer();
                    }else{
                        //処理中だったら見送り
                        Smfb_core.getInstance().log(Tools.Formatter(Messages.TimerRestarted_log.toString(), Name));
                        ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.TimerRestarted_Minecraft.toString(), Name)));

                        TimerTask task = this;
                        timer = new Timer();
                        timer.schedule(task, ConfigData.CloseTime * 60000);
                    }
                }
            };
            timer = new Timer();
            timer.schedule(task, ConfigData.CloseTime * 60000);

            return true;
        }else{
            return false;
        }
    }

    /**
     * タイマーのストップ
     */
    public boolean StopTimer() {
        if(task == null) return false;

        task.cancel();
        task = null;
        return true;
    }

    /**
     * サーバーが動いているかチェックする
     */
    public void AliveCheck() {
        if((Started || Switching) && !Process.isAlive()) {
            ProxyServer.getInstance().broadcast(new TextComponent(Tools.Formatter(Messages.ProcessDied.toString(), Name)));
            Switching = false;
            Started = false;
        }
    }

    /**
     * サーバーIDからサーバーを取得する
     * @param ID サーバーID
     * @return サーバー
     */
    public static Server getServerByID(String ID) {
        for(Server server : ConfigData.Servers) {
            if(server.ID.equals(ID)) {
                return server;
            }
        }

        return null;
    }

    /**
     * サーバーのステータスを文字で返す
     * @return ステータス
     */
    public String Status() {
        if(!Enabled) return Messages.ServerStatus_disabled.toString();

        if(Started) {
            if(!Switching) {
                return Messages.ServerStatus_started.toString();
            }else{
                return Messages.ServerStatus_starting.toString();
            }
        }else{
            if(!Switching) {
                return Messages.ServerStatus_stopped.toString();
            }else{
                return Messages.ServerStatus_stopping.toString();
            }
        }
    }
}
