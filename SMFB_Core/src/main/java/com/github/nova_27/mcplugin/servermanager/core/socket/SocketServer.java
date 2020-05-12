package com.github.nova_27.mcplugin.servermanager.core.socket;

import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import com.github.nova_27.mcplugin.servermanager.core.config.ConfigData;
import com.github.nova_27.mcplugin.servermanager.core.utils.Messages;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class SocketServer extends Thread {
    private boolean isRunning;
    private ArrayList<ClientConnection> clientConnections;

    /**
     * コンストラクタ
     */
    public SocketServer() {
        isRunning = true;
        clientConnections = new ArrayList<>();
    }

    /**
     * clientConnectionを返す
     */
    public ArrayList<ClientConnection> getClientConnections() {
        return clientConnections;
    }

    /**
     * 通信を停止する
     */
    public void stopSocket() {
        isRunning = false;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(ConfigData.socketPort);
            serverSocket.setSoTimeout(1000);
            Smfb_core.getInstance().log(Messages.ServerSocketStarted.toString());

            while(isRunning) {
                try {
                    ClientConnection clientConnection = new ClientConnection(serverSocket.accept(), Smfb_core.getInstance());
                    clientConnections.add(clientConnection);
                    clientConnection.start();
                    clientConnection.addQueue(PacketID.IDRequest, new byte[1]);
                } catch (SocketTimeoutException ignored) {
                }
            }

            for(ClientConnection cc : clientConnections) {
                cc.stopSocket();
            }
            for(ClientConnection cc : clientConnections) {
                cc.join();
            }
        }
        catch (IOException | InterruptedException e) {
            Smfb_core.getInstance().log(Messages.IOError.toString());
        }
        finally{
            try{
                if (serverSocket != null){
                    serverSocket.close();
                }
            }
            catch (IOException ioex) {
                Smfb_core.getInstance().log(Messages.IOError.toString());
            }
        }
    }
}
