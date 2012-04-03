package com.imjake9.server.warps.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WarpLocation {
    
    private Location loc;
    private String key;
    
    public WarpLocation(Location loc) {
        this.loc = loc;
    }
    
    public WarpLocation(Location loc, String key) {
        this.loc = loc;
        this.key = key;
    }
    
    public Location getLocation() {
        return this.loc;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public boolean hasKey(String key) {
        return this.key.equalsIgnoreCase(key);
    }
    
    public boolean isInWorld(World world) {
        return this.loc.getWorld().equals(world);
    }
    
    public String getLine() {
        StringBuilder builder = new StringBuilder();
        if(key != null) builder.append(key).append(":");
        builder.append(loc.getX()).append(":");
        builder.append(loc.getY()).append(":");
        builder.append(loc.getZ()).append(":");
        builder.append(loc.getYaw()).append(":");
        builder.append(loc.getWorld().getName());
        return builder.toString();
    }
    
    public void teleportTo(Player player) {
        loc.getBlock().getChunk().load();
        loc.setPitch(player.getLocation().getPitch());
        player.teleport(loc);
    }
    
}
