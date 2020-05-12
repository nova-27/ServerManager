package com.github.nova_27.mcplugin.servermanager.common.socket.protocol;

import com.github.nova_27.mcplugin.servermanager.common.socket.ConnectionThread;

public interface PacketEventListener {
    void IDRequest(byte[] gotData, ConnectionThread ct);

    void IDResponse(byte[] gotData, ConnectionThread ct);

    void PlayerCountResponse(byte[] gotData, ConnectionThread ct);

    void ServerStopRequest(byte[] gotData, ConnectionThread ct);

    void ServerStopResponse(byte[] gotData, ConnectionThread ct);
}
