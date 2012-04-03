package com.imjake9.server.warps;

import com.imjake9.server.lib.plugin.JSPlugin;
import com.imjake9.server.warps.utils.JSWConfigManager;
import com.imjake9.server.warps.utils.JSWarpsManager;

public class JSWarps extends JSPlugin {
    
    private JSWarpsCommandHandler commandHandler;
    
    private static JSWarps plugin;
    
    @Override
    public void onJSDisable() {
        // Save all warps upon disable
        JSWarpsManager.saveWarps();
    }

    @Override
    public void onJSEnable() {
        plugin = this;
        // Load config and warps
        JSWConfigManager.loadConfiguration();
        // Delegate commands
        commandHandler = new JSWarpsCommandHandler();
    }
    
    public static JSWarps getPlugin() {
        return plugin;
    }
    
}
