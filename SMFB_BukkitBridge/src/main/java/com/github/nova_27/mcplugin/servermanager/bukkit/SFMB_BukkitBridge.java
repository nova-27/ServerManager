package com.github.nova_27.mcplugin.servermanager.bukkit;

import com.github.nova_27.mcplugin.servermanager.bukkit.listener.SpigotListener;
import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionFailedException;
import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionThread;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketEventListener;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public final class SFMB_BukkitBridge extends JavaPlugin implements PacketEventListener {
    private static final int TRY = 3;

    private static SFMB_BukkitBridge instance;
    public ConnectionThread connectionThread;

    public static SFMB_BukkitBridge getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        //configロード
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        //configデータ取得
        String ip = config.getString("Socket.IP");
        int port = config.getInt("Socket.Port");

        //サーバーに接続する
        Socket socket = null;

        try {
            for (int i = 1; i <= TRY; i++) {
                InetSocketAddress socketAddress =
                        new InetSocketAddress(ip, port);

                socket = new Socket();
                socket.connect(socketAddress, 5000);

                if ((socket.getInetAddress()) != null) break;
            }

            if (socket.getInetAddress() == null) throw new ConnectionFailedException("Socket connection failed!");
        }catch(IOException | ConnectionFailedException e){
            e.printStackTrace();
            return;
        }

        //スレッドを開始する
        connectionThread = new ConnectionThread(socket, this);
        connectionThread.start();

        //リスナーの登録
        getServer().getPluginManager().registerEvents(new SpigotListener(), this);
    }

    @Override
    public void onDisable() {
        connectionThread.addQueue(PacketID.ServerStopResponse, new byte[1]);

        if(connectionThread != null) {
            connectionThread.stopSocket();
            try {
                connectionThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void IDRequest(byte[] gotData, ConnectionThread ct) {
        byte[] port = ByteBuffer.allocate(4).putInt(Bukkit.getPort()).array();
        ct.addQueue(PacketID.IDResponse, port);
    }

    @Override
    public void IDResponse(byte[] gotData, ConnectionThread ct) {

    }

    @Override
    public void PlayerCountResponse(byte[] gotData, ConnectionThread ct) {

    }

    @Override
    public void ServerStopRequest(byte[] gotData, ConnectionThread ct) {
        try {
            Bukkit.getScheduler().callSyncMethod( this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop") ).get();
        }catch (InterruptedException | ExecutionException ignored) {

        }
    }

    @Override
    public void ServerStopResponse(byte[] gotData, ConnectionThread ct) {

    }
}
