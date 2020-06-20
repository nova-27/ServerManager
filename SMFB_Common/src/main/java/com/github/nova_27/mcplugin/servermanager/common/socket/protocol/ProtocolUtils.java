package com.github.nova_27.mcplugin.servermanager.common.socket.protocol;

import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionThread;

import java.util.ArrayList;
import java.util.Arrays;

public class ProtocolUtils {
    public static final int DATASIZE = 100;
    public static int TMP_SIZE = DATASIZE*10;

    /**
     * 送信データ配列を作成する
     * @param id パケットID
     * @param data データ
     * @return 送信データ配列
     */
    public static ArrayList<byte[]> createSendQueue(PacketID id, byte[] data) {
        ArrayList<byte[]> sendData = new ArrayList<>();
        addSendQueue(id, data, sendData);
        return sendData;
    }

    /**
     * キューに送信データを追加する
     * @param id パケットID
     * @param data データ
     * @param queue キュー
     */
    public static void addSendQueue(PacketID id, byte[] data, ArrayList<byte[]> queue) {
        if(data.length == DATASIZE-1) {
            byte[] sendData = new byte[DATASIZE];
            sendData[0] = id.getID();

            System.arraycopy (
                    data, 0,
                    sendData, 1,
                    data.length
            );

            queue.add(sendData);
        } else if(data.length < DATASIZE-1) {
            byte[] sendData = new byte[DATASIZE-1];
            Arrays.fill(sendData, (byte) 0x00);

            System.arraycopy (
                    data, 0,
                    sendData, 0,
                    data.length
            );

            addSendQueue(id, sendData, queue);
        } else {
            int from;
            for(from = 0; from <= data.length-100; from+=99) {
                byte[] sendData = new byte[DATASIZE-1];
                System.arraycopy (
                        data, from,
                        sendData, 0,
                        99
                );
                addSendQueue(PacketID.MORE, sendData, queue);
            }

            byte[] LastData = new byte[DATASIZE-1];
            System.arraycopy (
                    data, from,
                    LastData, 0,
                    data.length - from
            );
            addSendQueue(id, LastData, queue);
        }
    }

    public static byte[] decoder(byte[] packet, byte[] tmp, ConnectionThread ct, PacketEventListener pel) throws InvalidIDException {
        PacketID id = PacketID.getById(packet[0]);
        switch (id) {
            case MORE:
                int tmpDataLength;
                for(tmpDataLength = 0; tmp[tmpDataLength] != 0x00; tmpDataLength++);
                System.arraycopy(packet, 1, tmp, tmpDataLength, DATASIZE-1);
                return tmp;
            case UNKNOWN:
                throw new InvalidIDException("不正なID プログラムのバグです");
        }

        byte[] gotData = new byte[TMP_SIZE];
        boolean tmp_init = false;

        //一時データがあったら
        int tmpDataLength;
        for(tmpDataLength = 0; tmp[tmpDataLength] != 0x00; tmpDataLength++);
        if(tmpDataLength > 0) {
            System.arraycopy(tmp, 0, gotData, 0, tmp.length);
            tmp_init = true;
        }

        int gotDataLength;
        for(gotDataLength = 0; gotData[gotDataLength] != 0x00; gotDataLength++);
        System.arraycopy(packet, 1, gotData, gotDataLength, packet.length-1);
        switch (id) {
            case IDRequest:
                pel.IDRequest(gotData, ct);
                break;
            case IDResponse:
                pel.IDResponse(gotData, ct);
                break;
            case PlayerCountResponse:
                pel.PlayerCountResponse(gotData, ct);
                break;
            case ServerStopRequest:
                pel.ServerStopRequest(gotData, ct);
                break;
            case ServerStopResponse:
                pel.ServerStopResponse(gotData, ct);
                break;
            case SendCommand:
                pel.SendCommand(gotData, ct);
                break;
        }

        if(tmp_init) return new byte[TMP_SIZE];
        else return tmp;
    }
}
