package com.github.nova_27.mcplugin.servermanager.discord;

import com.github.nova_27.mcplugin.servermanager.core.Smfb_core;
import net.md_5.bungee.api.plugin.Plugin;

public final class Smfb_discord extends Plugin {
    Smfb_core core;

    @Override
    public void onEnable() {
        Plugin temp = getProxy().getPluginManager().getPlugin("SMFBCore");
        if (temp instanceof Smfb_core) {
            core = ((Smfb_core) temp).getInstance();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
