package com.github.nova_27.mcplugin.servermanager.common.socket;

import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.InvalidIDException;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketEventListener;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.PacketID;
import com.github.nova_27.mcplugin.servermanager.common.socket.protocol.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static com.github.nova_27.mcplugin.servermanager.common.socket.protocol.ProtocolUtils.DATASIZE;
import static com.github.nova_27.mcplugin.servermanager.common.socket.protocol.ProtocolUtils.TMP_SIZE;

public class ConnectionThread extends Thread {
    private Socket connection;
    private PacketEventListener pel;
    private byte[] tmp;
    private boolean isRunning;

    private boolean isQueueEmpty;
    private ArrayList<byte[]> queue;

    /**
     * コンストラクタ
     * @param connection 接続先socket
     * @param pel パケットイベントリスナ―
     */
    public ConnectionThread(Socket connection, PacketEventListener pel) {
        this.connection = connection;
        this.pel = pel;
        tmp = new byte[TMP_SIZE];
        isRunning = true;
        isQueueEmpty = true;
        queue = new ArrayList<>();
    }

    /**
     * キューを追加する
     * @param id パケットID
     * @param data 送信データ
     */
    public void addQueue(PacketID id, byte[] data) {
        queue.addAll(ProtocolUtils.createSendQueue(id, data));
        isQueueEmpty = false;
    }

    /**
     * 通信を停止する
     */
    public void stopSocket() {
        isRunning = false;
    }

    @Override
    public void run() {
        try {
            OutputStream os = connection.getOutputStream();
            InputStream is = connection.getInputStream();

            while (isRunning && !connection.isClosed()) {
                if (!isQueueEmpty) {
                    for (byte[] data : queue) {
                        os.write(data);
                    }
                    os.flush();
                    queue = new ArrayList<>();
                    isQueueEmpty = true;
                }

                if (is.available() >= DATASIZE) {
                    byte[] data = new byte[DATASIZE];
                    is.read(data, 0, DATASIZE);

                    tmp = ProtocolUtils.decoder(data, tmp, this, pel);
                }

                Thread.sleep(100);
            }

            if (!isQueueEmpty) {
                for (byte[] data : queue) {
                    os.write(data);
                }
                os.flush();
                queue = new ArrayList<>();
                isQueueEmpty = true;
            }
        } catch (InvalidIDException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("The connection to the managed server was forcibly broken.");
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                finalProgress();
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    public void finalProgress() { }
}
