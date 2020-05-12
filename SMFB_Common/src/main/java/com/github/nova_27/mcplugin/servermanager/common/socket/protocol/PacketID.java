package com.github.nova_27.mcplugin.servermanager.common.socket.protocol;

import java.util.Objects;

public enum PacketID {
    MORE(0x01),
    IDRequest(0x02),
    IDResponse(0x03),
    PlayerCountResponse(0x04),
    ServerStopRequest(0x05),
    ServerStopResponse(0x06),
    UNKNOWN(0xFF);

    private byte id;

    PacketID(int id) {
        this.id = (byte)id;
    }

    public byte getID() {
        return id;
    }

    public static PacketID getById(byte id) {
        for(PacketID val : values()) {
            if(Objects.equals(val.getID(), id)) return val;
        }
        return UNKNOWN;
    }
}