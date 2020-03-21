package com.github.nova27.servermanager.config;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.utils.Bridge;
import com.github.nova27.servermanager.utils.DiscordSender;
import com.github.nova27.servermanager.utils.Messages;
import com.github.nova27.servermanager.utils.minecraft.GetCommandResultThread;
import com.github.nova27.servermanager.utils.minecraft.GetLogsThread;
import com.github.nova27.servermanager.utils.minecraft.StandardEventListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Minecraftサーバー管理クラス
 */
public class Server implements StandardEventListener {
    public final int BUF_LOG_CNT = 30;

    private ServerManager main;

    //Minecraftサーバー設定
    public String ID;
    public String Name;
    private String Dir;
    private String File;
    private String Args;

    //ログ取得
    private GetLogsThread Log_getter;
    public String[] Logs = new String[BUF_LOG_CNT];
    public int Start_write = 0;
    private DiscordSender ConsoleChannel_Sender;
    public long ConsoleChannelId;

    //サーバープロセス
    public Process Process = null;
    public boolean Started = false;
    public boolean Switching = false;
    public boolean Enabled = true;

    //タイマー
    private TimerTask task = null;
    private Timer timer = null;

    //正規表現
    private String ServerStartingDone;
    private String ServerStopped;
    private String LogDeleteRegex;
    private String LogListCmd;

    /**
     * コンストラクタ
     * @param main メインのオブジェクト
     * @param ID サーバーID（BungeeCordと同一）
     * @param Name サーバー名（表示用）
     * @param Dir サーバーのルートディレクトリ
     * @param File サーバー本体（jar）
     * @param Args 実行引数
     * @param ConsoleChannelId DiscordコンソールチャンネルのID（使わない場合0L）
     * @param ServerStartingDone サーバーが起動完了したときのログ（正規表現）
     * @param ServerStopped サーバーが停止した時のログ（正規表現）
     * @param LogDeleteRegex ログの最初につく無駄な削除する文字列（正規表現）
     * @param LogListCmd listコマンドを実行したときの正規表現
     */
    public Server(ServerManager main, String ID, String Name, String Dir, String File, String Args, long ConsoleChannelId, String ServerStartingDone, String ServerStopped, String LogDeleteRegex, String LogListCmd) {
        this.main = main;
        this.ID = ID;
        this.Name = Name;
        this.Dir = Dir;
        this.File = File;
        this.Args = Args;
        this.ConsoleChannelId = ConsoleChannelId;
        if(ConsoleChannelId != 0L) {
            ConsoleChannel_Sender = new DiscordSender(main, ConsoleChannelId);
            ConsoleChannel_Sender.start();
        }
        this.ServerStartingDone = ServerStartingDone;
        this.ServerStopped = ServerStopped;
        this.LogDeleteRegex = LogDeleteRegex;
        this.LogListCmd = LogListCmd;
    }

    /**
     * LogListCmdを返す
     */
    public String getLogListCmd() {
        return LogListCmd;
    }

    /**
     * サーバーをONにする
     */
    public void Server_On() {
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

                //ログ取得スレッドの開始
                Log_getter = new GetLogsThread(this, this, LogDeleteRegex);
                Log_getter.start();

                main.log(Bridge.Formatter(Messages.ServerStarting_Log.toString(), Name));
                main.bridge.sendToDiscord(Bridge.Formatter(Messages.ServerStarting_Discord.toString(), Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.ServerStarting_Minecraft.toString(), Name)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * コマンドを実行し、コマンドの実行結果を取得後、com_result_event.got_result(実行結果[失敗時はnull])を実行します。
     *
     * @param command コマンド
     * @param match コマンド実行結果の形式 正規表現
     * @param com_result_event 実行結果取得後のイベント (nullの場合、コマンド送信のみ)
     * @return コマンド送信できたかどうか
     */
    public boolean Exec_command(String command, String match, StandardEventListener com_result_event) {
        //起動していなかったら
        if(!Started) {
            return false;
        }
        //有効でなかったら
        if(!Enabled) {
            return false;
        }
        //ステータス切り替え中だったら
        if(Switching) {
            return false;
        }

        //コマンドの送信
        BufferedWriter streamInput = new BufferedWriter(new OutputStreamWriter(Process.getOutputStream()));
        try {
            streamInput.write(command+"\n");
            streamInput.flush();
        } catch (IOException e) {
            main.log(Messages.IOError.toString());
            e.printStackTrace();
            return false;
        }

        //コマンド取得後のイベントが未登録なら
        if(com_result_event == null) {
            return true;
        }

        //実行結果取得スレッドを実行
        GetCommandResultThread get_result = new GetCommandResultThread(this, match, com_result_event);
        get_result.start();

        return true;
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
                        main.log(Bridge.Formatter(Messages.ServerStopping_Log.toString(), Name));
                        main.bridge.sendToDiscord(Bridge.Formatter(Messages.ServerStopping_Discord.toString(), Name));
                        ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.ServerStopping_Minecraft.toString(), Name)));

                        Exec_command("stop", "", null);
                        Started = false;
                        Switching = true;
                    }else{
                        //処理中だったら見送り
                        main.log(Bridge.Formatter(Messages.TimerRestarted_Log.toString(), Name));
                        main.bridge.sendToDiscord(Bridge.Formatter(Messages.TimerRestarted_Discord.toString(), Name));
                        ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.TimerRestarted_Minecraft.toString(), Name)));

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
    public void StopTimer() {
        if(task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * ログを取得したあとの処理
     * @param line ログ
     */
    @Override
    public void exec(String line) {
        //コンソールチャンネルが有効なら送信
        if(ConsoleChannel_Sender != null) {
            ConsoleChannel_Sender.add_queue(line);
        }

        //起動が完了した場合
        if(line.matches(ServerStartingDone)) {
            Switching = false;
            main.log(Bridge.Formatter(Messages.ServerStarted_Log.toString(), Name));
            main.bridge.sendToDiscord(Bridge.Formatter(Messages.ServerStarted_Discord.toString(), Name));
            ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.ServerStarted_Minecraft.toString(), Name)));
        }
        //サーバーが停止した場合
        else if(line.matches(ServerStopped)) {
            Started = false;
            Switching = true;

            StopTimer();

            Thread after_process = new Thread() {
                public void run() {
                    //ログ取得スレッド終了待機
                    try {
                        Log_getter.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //ログ取得スレッド初期化
                    Log_getter = null;
                    Logs = new String[BUF_LOG_CNT];
                    Start_write = 0;

                    //ステータス切り替え
                    main.log(Bridge.Formatter(Messages.ServerStopped_Log.toString(), Name));
                    main.bridge.sendToDiscord(Bridge.Formatter(Messages.ServerStopped_Discord.toString(), Name));
                    ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.ServerStopped_Minecraft.toString(), Name)));
                    Switching = false;
                };
            };

            after_process.start();
            Log_getter.thread_stop();
        }
    }
}
