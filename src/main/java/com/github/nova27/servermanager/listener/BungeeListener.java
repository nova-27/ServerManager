package com.github.nova27.servermanager.listener;

import com.github.nova27.servermanager.ServerManager;
import com.github.nova27.servermanager.config.ConfigData;
import com.github.nova27.servermanager.request.RequestsManager;
import com.gmail.necnionch.myapp.markdownconverter.MarkComponent;
import com.gmail.necnionch.myapp.markdownconverter.MarkdownConverter;
import com.gmail.necnionch.myplugin.n8chatcaster.bungee.N8ChatCasterAPI;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BungeeCordイベントリスナー
 */
public class BungeeListener implements Listener {
    private final ServerManager main;
    public static Server Lobby;

    private static String sendToDiscordFormat;

    /**
     * Discord宛て送信のフォーマット
     * @param format フォーマット
     */
    public static void setSendToDiscordFormat(String format) {
        sendToDiscordFormat = format;
    }

    /**
     * コンストラクタ
     * @param main ServerManagerのオブジェクト
     */
    public BungeeListener(ServerManager main) {
        this.main = main;
    }

    /**
     * チャットが送信されたら実行
     * @param event チャット情報
     */
    @EventHandler
    public void onChat(ChatEvent event) {
        //コマンドなら
        if(event.isCommand()){
            return;
        }

        //キャンセルされていたら
        if (event.isCancelled()) return;

        // プレイヤーの発言ではない場合は、そのまま無視する
        if ( !(event.getSender() instanceof ProxiedPlayer) ) {
            return;
        }

        N8ChatCasterAPI chatCasterApi = this.main.getChatCasterApi();
        if (chatCasterApi == null || !chatCasterApi.isEnabledChatCaster()) {
            // 連携プラグインが無効の場合
            ProxiedPlayer sender = (ProxiedPlayer)event.getSender();
            String senderServer = sender.getServer().getInfo().getName();
            String message = event.getMessage();

            MarkComponent[] components = MarkdownConverter.fromMinecraftMessage(message, '&');
            String output = MarkdownConverter.toDiscordMessage(components);
            main.bridge.sendToDiscord(Bridge.Formatter(sendToDiscordFormat, senderServer, sender.toString(), output));
        }
    }

    /**
     * ログインされたら
     * @param e ログイン情報
     */
    @EventHandler
    public void onLogin(LoginEvent e) {
        main.bridge.PlayerCount(1);
        String name = e.getConnection().getName();
        main.bridge.sendToDiscord(Bridge.Formatter(Messages.JoinedTheGame.toString(), name));
        ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.TimerStopped_Minecraft.toString(), Lobby.Name)));

        //一人目の場合
        if(main.bridge.PlayerCount(0) == 1) {
            //サーバーを起動
            if (!Lobby.Started) {
                //起動していなかったら、キック
                e.getConnection().disconnect(new TextComponent(Messages.BungeeNotStarted.toString()));
                main.bridge.PlayerCount(-1);

                Lobby.Server_On();
            }
            else if (Lobby.Switching){
                //処理中だったら、キック
                e.getConnection().disconnect(new TextComponent(Messages.BungeeSwitching.toString()));
                main.bridge.PlayerCount(-1);
            }else{
                //起動済みだったらタイマーストップ
                Lobby.StopTimer();
                main.log(Bridge.Formatter(Messages.TimerStopped_Log.toString(), Lobby.Name));
                main.bridge.sendToDiscord(Bridge.Formatter(Messages.TimerStopped_Discord.toString(), Lobby.Name));
            }
        }
    }

    /**
     * サーバー間を移動できたら
     */
    @EventHandler
    public void onSwitch(ServerSwitchEvent e) {
        for(int i = 0; i < ConfigData.Server.length; i++) {
            //ロビーサーバーは停止しない
            if(ConfigData.Server[i] == Lobby) {
                continue;
            }

            //タイマーのストップ
            if(ConfigData.Server[i].ID.equals(e.getPlayer().getServer().getInfo().getName()) || ConfigData.Server[i].AnotherID.equals(e.getPlayer().getServer().getInfo().getName())) {
                ConfigData.Server[i].StopTimer();
                main.log(Bridge.Formatter(Messages.TimerStopped_Log.toString(), ConfigData.Server[i].Name));
                main.bridge.sendToDiscord(Bridge.Formatter(Messages.TimerStopped_Discord.toString(), ConfigData.Server[i].Name));
                ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.TimerStopped_Minecraft.toString(), ConfigData.Server[i].Name)));
                continue;
            }

            if(ConfigData.Server[i].Started && !ConfigData.Server[i].Switching) {
                final int f_i = i;

                Timer timer = new Timer(false);
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        final int[] result = new int[]{0};
                        ConfigData.Server[f_i].Exec_command("list", ConfigData.Server[f_i].getLogListCmd(), new StandardEventListener() {
                            @Override
                            public void exec(String result_tmp) {
                                Matcher m = Pattern.compile("[0-9]+").matcher(result_tmp);

                                if(m.find()) {
                                    if (Integer.parseInt(m.group()) == 0) {
                                        result[0] = 1;
                                    }else{
                                        result[0] = 2;
                                    }
                                }
                            }
                        });

                        while(result[0] == 0){
                            //処理が終わるまで待機
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if(result[0] == 1) {
                            //0人だったらタイマー起動
                            if(ConfigData.Server[f_i].StartTimer()){
                                //タイマーが起動していなかったら
                                main.bridge.sendToDiscord(Bridge.Formatter(Messages.TimerStarted_Discord.toString(), ""+ConfigData.CloseTime, ConfigData.Server[f_i].Name));
                                main.log(Bridge.Formatter(Messages.TimerStarted_Log.toString(), ""+ConfigData.CloseTime, ConfigData.Server[f_i].Name));
                                ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.TimerStarted_Minecraft.toString(), ""+ConfigData.CloseTime, ConfigData.Server[f_i].Name)));
                            }
                        }
                    }
                };
                timer.schedule(task, 3000);
            }
        }
    }

    /**
     * サーバーに接続しようとしたら
     */
    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        if (e.getReason().name().equals("JOIN_PROXY")) {
            //プロキシに入りたてだったら
            return;
        }

        Server targetServer = null;
        for(Server server : ConfigData.Server) {
            if(server.ID.equals(e.getTarget().getName()) || server.AnotherID.equals(e.getTarget().getName())) {
                targetServer = server;
                break;
            }
        }

        if(targetServer == null) {
            //プラグインに登録されていないサーバーだったら
            return;
        }

        //起動していたらreturn
        if(targetServer.Started) return;

        if(ConfigData.requestRequired == 0) {
            //リクエスト機能が無効だったら
            e.getPlayer().sendMessage(new TextComponent(Messages.BungeeCommand_request_disabled.toString()));
            return;
        }

        if(!e.getPlayer().hasPermission(BungeeMinecraftCommand.PERM + "." + BungeeMinecraftCommand.REQUEST_PERM)) {
            //権限を持っていなかったら
            e.getPlayer().sendMessage(new TextComponent(Messages.BungeeCommand_denied.toString()));
            return;
        }

        String ID = targetServer.ID;
        if(!targetServer.AnotherID.equals("")) {
            ID += "(" + targetServer.AnotherID + ")";
        }
        if(!targetServer.Enabled) {
            //サーバーが無効だったら
            e.getPlayer().sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_disabled.toString(), ID)));
            return;
        }

        RequestsManager.RequestsStats stats = RequestsManager.getRequestsStats(e.getPlayer().getName());

        if(stats.getRequestServers().contains(targetServer)) {
            //リクエスト済みだったら
            e.getPlayer().sendMessage(new TextComponent(Messages.BungeeCommand_already_requested.toString()));
            return;
        }

        if(stats.getLastRequestTime() + ConfigData.requestDelay > System.currentTimeMillis()) {
            //再リクエスト待機時間が経過していなかったら
            e.getPlayer().sendMessage(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_request_wait.toString(), (ConfigData.requestDelay / 1000 / 60) + "")));
            return;
        }

        RequestsManager.addRequest(e.getPlayer().getName(), targetServer);
        e.getPlayer().sendMessage(new TextComponent(Messages.BungeeCommand_request_successful.toString()));

        if(!targetServer.Started) {
            //リクエストが承認されていないときだけアナウンス
            Timestamp expiration_date = new Timestamp(targetServer.lastRequest + ConfigData.requestWait);
            SimpleDateFormat sdf = new SimpleDateFormat("MM.dd HH:mm:ss");
            String expiration_String = sdf.format(expiration_date);
            ProxyServer.getInstance().broadcast(new TextComponent(Bridge.Formatter(Messages.BungeeCommand_new_request.toString(), targetServer.Name, targetServer.requests.size() + "", expiration_String)));
        }
    }
}
