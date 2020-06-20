package com.github.nova_27.mcplugin.servermanager.core.events;


import com.github.nova_27.mcplugin.servermanager.core.config.Server;
import net.md_5.bungee.api.plugin.Event;

/**
 * タイマーに関するイベント
 */
public class TimerEvent extends Event {
    private Server server;
    private EventType eventType;

    public TimerEvent(Server server, EventType eventType) {
        this.server = server;
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Server getServer() {
        return server;
    }

    public enum EventType {
        TimerStarted,
        TimerStopped,
        TimerRestarted
    }
}
