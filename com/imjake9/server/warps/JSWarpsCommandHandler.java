package com.imjake9.server.warps;

import com.imjake9.server.lib.EconomyManager;
import com.imjake9.server.lib.Messaging;
import com.imjake9.server.lib.Messaging.JSMessage;
import com.imjake9.server.warps.utils.JSWConfigManager;
import com.imjake9.server.warps.utils.JSWMessage;
import com.imjake9.server.warps.utils.JSWarpsManager;
import com.imjake9.server.warps.utils.WarpLocation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;


public class JSWarpsCommandHandler implements CommandExecutor {
    
    public JSWarpsCommandHandler() {
        
        JSWarps plugin = JSWarps.getPlugin();
        
        for (JSWarpsCommand command : JSWarpsCommand.values()) {
            
            PluginCommand cmd = plugin.getCommand(command.name().toLowerCase());
            
            if (cmd == null)
                continue;
            
            cmd.setAliases(Arrays.asList(command.getAliases()));
            cmd.setPermission(command.getPermission());
            cmd.setPermissionMessage(command.getPermissionMessage());
            cmd.setExecutor(this);
            
        }
        
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String... args) {
        
        return JSWarpsCommand.handleCommand(command, sender, args);
        
    }
    
    public static enum JSWarpsCommand {
        
        HOME () {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                if (!(sender instanceof Player)) {
                    Messaging.send(JSMessage.ONLY_PLAYERS, sender);
                    return true;
                }
                
                // Cast sender to player
                Player player = (Player) sender;
                
                // Get home and warp
                boolean found = false;
                for (WarpLocation loc : JSWarpsManager.getWarpList("homes")) {
                    if (loc.getKey() != null && loc.getKey().equalsIgnoreCase(player.getName()) && loc.getLocation().getWorld().equals(player.getWorld())) {
                        loc.teleportTo(player);
                        found = true;
                        break;
                    }
                }
                
                // Display message
                if (!found)
                    Messaging.send(JSWMessage.HOME_POINT_NOT_SET, sender);
                else
                    Messaging.send(JSWMessage.WARPED_TO, sender, "home");
                
                return true;
            }
            
        },
        SETHOME () {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                if (!(sender instanceof Player)) {
                    Messaging.send(JSMessage.ONLY_PLAYERS, sender);
                    return true;
                }
                
                // Cast sender to player
                Player player = (Player) sender;
                
                // Get warp to remove
                WarpLocation remove = null;
                for (WarpLocation loc : JSWarpsManager.getWarpList("homes")) {
                    if (loc.getKey() != null && loc.hasKey(player.getName()) && loc.isInWorld(player.getWorld())) {
                        remove = loc;
                        break;
                    }
                }
                
                // Remove old warp
                if (remove != null)
                    JSWarpsManager.getWarpList("homes").remove(remove);
                
                // Add new warp
                WarpLocation newWarp = new WarpLocation(player.getLocation(), player.getName());
                JSWarpsManager.getWarpList("homes").add(newWarp);
                
                // Save warps
                JSWarpsManager.saveWarpFile("homes");
                
                // Display message
                Messaging.send(JSWMessage.SET_HOME_POINT, sender);
                
                return true;
            }
            
        },
        PRIVATEWARP("pw", "bw", "jw") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                if (!(sender instanceof Player)) {
                    Messaging.send(JSMessage.ONLY_PLAYERS, sender);
                    return true;
                }
                
                // Cast sender to player
                Player player = (Player) sender;
                
                // Initial handling:

                List<WarpLocation> worldWarps = JSWarpsManager.getWarpListForWorld(player.getName().toLowerCase(), player.getWorld());
                List<WarpLocation> playerWarps = JSWarpsManager.getWarpList(player.getName().toLowerCase());
                if (playerWarps == null) {
                    playerWarps = new ArrayList<WarpLocation>();
                }
                
                // Display help menu
                if (args.length == 0) {
                    player.sendMessage(ChatColor.AQUA + "- - - - JSWarps - - - -");
                    if (!EconomyManager.isUsingEconomy()) {
                        Messaging.send(JSWMessage.WARPS_RATIO, sender, String.valueOf(worldWarps.size()), String.valueOf(JSWConfigManager.getUsableWarps(player)));
                    } else {
                        Messaging.send(JSWMessage.WARPS_RATIO, sender, String.valueOf(worldWarps.size()), String.valueOf(JSWConfigManager.getUsableWarps(player)), String.valueOf(JSWConfigManager.getPotentialWarps(player)));
                        Messaging.send(JSWMessage.PRICE_TO_UNLOCK, sender, String.valueOf(JSWConfigManager.getSlotPrice(player)));
                    }
                    player.sendMessage("  Use /pw list to view your warps.");
                    player.sendMessage("  Use /pw set <name> to create a new warp.");
                    player.sendMessage("  Use /pw remove <name> to remove a warp.");
                    player.sendMessage("  Use /pw goto <name> to warp.");
                    if (EconomyManager.isUsingEconomy()) {
                        player.sendMessage("  Use /pw unlock to purchase a new warp slot.");
                    }
                    player.sendMessage(ChatColor.AQUA + " - - - - - - - - - - - - ");
                    return true;
                }

                String subcommand = args[0];
                
                // LIST
                if (subcommand.equalsIgnoreCase("list")) {
                    if (!EconomyManager.isUsingEconomy()) {
                        Messaging.send(JSWMessage.WARPS_RATIO, sender, String.valueOf(worldWarps.size()), String.valueOf(JSWConfigManager.getUsableWarps(player)));
                    } else {
                        Messaging.send(JSWMessage.WARPS_RATIO, sender, String.valueOf(worldWarps.size()), String.valueOf(JSWConfigManager.getUsableWarps(player)), String.valueOf(JSWConfigManager.getPotentialWarps(player)));
                    }
                    for (WarpLocation loc : worldWarps) {
                        player.sendMessage("  - " + loc.getKey());
                    }
                    return true;
                }
                
                // SET
                if (subcommand.equalsIgnoreCase("set")) {
                    if (args.length < 2) {
                        Messaging.send(JSMessage.MISSING_PARAMETER, sender, "name");
                        return false;
                    }
                    
                    // Get warp to overwrite
                    WarpLocation overwrite = null;
                    for (WarpLocation loc : worldWarps) {
                        if (loc.getKey().equalsIgnoreCase(args[1])) {
                            overwrite = loc;
                            break;
                        }
                    }
                    
                    // No more room for warps?
                    if (overwrite == null && worldWarps.size() >= JSWConfigManager.getUsableWarps(player)) {
                        Messaging.send(JSWMessage.OUT_OF_WARPS, sender);
                        return true;
                    }
                    
                    // Remove old warp
                    if (overwrite != null)
                        playerWarps.remove(overwrite);
                    
                    // Get new warp
                    WarpLocation newWarp = new WarpLocation(player.getLocation(), args[1]);
                    playerWarps.add(newWarp);
                    
                    // Save warps
                    JSWarpsManager.saveWarpFile(player.getName().toLowerCase());

                    Messaging.send((overwrite == null) ? JSWMessage.WARP_CREATED : JSWMessage.WARP_MOVED, sender, args[1]);
                    return true;
                }
                
                // REMOVE
                if (subcommand.equalsIgnoreCase("remove")) {
                    if (args.length < 2) {
                        Messaging.send(JSMessage.MISSING_PARAMETER, sender, "name");
                        return false;
                    }
                    
                    // Get warp and remove it
                    for (WarpLocation loc : worldWarps) {
                        if (loc.getKey().equalsIgnoreCase(args[1])) {
                            playerWarps.remove(loc);
                            JSWarpsManager.saveWarpFile(player.getName().toLowerCase());
                            Messaging.send(JSWMessage.WARP_REMOVED, sender);
                            return true;
                        }
                    }
                    
                    // No warp was found
                    Messaging.send(JSWMessage.NO_WARP_NAMED, sender, args[1]);
                    return true;
                }
                
                // GOTO
                if (subcommand.equalsIgnoreCase("goto")) {
                    if (args.length < 2) {
                        Messaging.send(JSMessage.MISSING_PARAMETER, sender, "name");
                        return false;
                    }
                    
                    // Find warp and teleport
                    for (WarpLocation loc : worldWarps) {
                        if (loc.getKey().equalsIgnoreCase(args[1])) {
                            loc.teleportTo(player);
                            Messaging.send(JSWMessage.WARPED_TO, sender, args[1]);
                            return true;
                        }
                    }
                    
                    // No warp was found
                    Messaging.send(JSWMessage.NO_WARP_NAMED, sender, args[1]);
                    return true;
                }
                
                // UNLOCK
                if (EconomyManager.isUsingEconomy() && subcommand.equalsIgnoreCase("unlock")) {
                    if (JSWConfigManager.getUnlockedWarps(player) >= JSWConfigManager.getUnlockableWarps(player)) {
                        Messaging.send(JSWMessage.CANNOT_UNLOCK, sender);
                        return true;
                    }
                    
                    double price = JSWConfigManager.getSlotPrice(player);
                    Economy econ = EconomyManager.getEconomy();
                    
                    if (econ.getBalance(player.getName()) < price) {
                        Messaging.send(JSWMessage.NOT_ENOUGH_MONEY, sender);
                        return true;
                    }

                    econ.withdrawPlayer(player.getName(), price);

                    FileConfiguration config = JSWConfigManager.getPlayerData(player.getName());
                    config.set("slots", config.getInt("slots", 0) + 1);
                    JSWConfigManager.savePlayerData();

                    Messaging.send(JSWMessage.PURCHASED_SLOT, sender);
                    return true;
                }
                
                return false;
   
            }
            
        },
        PUBLICWARP("warp", "w") {
            
            @Override
            public boolean handle(CommandSender sender, String... args) {
                if (!(sender instanceof Player)) {
                    Messaging.send(JSMessage.ONLY_PLAYERS, sender);
                    return true;
                }
                
                // Cast sender to player
                Player player = (Player) sender;
                
                // Initial handling:

                List<WarpLocation> worldWarps = JSWarpsManager.getWarpListForWorld("public", player.getWorld());
                List<WarpLocation> playerWarps = JSWarpsManager.getWarpList("public");
                if (playerWarps == null) {
                    playerWarps = new ArrayList<WarpLocation>();
                }
                
                // Display help menu
                if (args.length == 0) {
                    player.sendMessage(ChatColor.AQUA + "- - - - JSWarps - - - -");
                    player.sendMessage("  Use /w list to view your warps.");
                    if (JSWarps.getPlugin().getPermissionsManager().hasPermission(sender, "admin")) {
                        player.sendMessage("  Use /w set <name> to create a new warp.");
                        player.sendMessage("  Use /w remove <name> to remove a warp.");
                    }
                    player.sendMessage("  Use /w goto <name> to warp.");
                    player.sendMessage(ChatColor.AQUA + " - - - - - - - - - - - - ");
                    return true;
                }

                String subcommand = args[0];
                
                // LIST
                if (subcommand.equalsIgnoreCase("list")) {
                    if (!EconomyManager.isUsingEconomy()) {
                        Messaging.send(JSWMessage.WARPS_RATIO, sender, String.valueOf(worldWarps.size()), String.valueOf(JSWConfigManager.getUsableWarps(player)));
                    } else {
                        Messaging.send(JSWMessage.WARPS_RATIO, sender, String.valueOf(worldWarps.size()), String.valueOf(JSWConfigManager.getUsableWarps(player)), String.valueOf(JSWConfigManager.getPotentialWarps(player)));
                    }
                    for (WarpLocation loc : worldWarps) {
                        player.sendMessage("  - " + loc.getKey());
                    }
                    return true;
                }
                
                // SET
                if (subcommand.equalsIgnoreCase("set")) {
                    if (!JSWarps.getPlugin().getPermissionsManager().hasPermission(sender, "admin")) {
                        Messaging.send(JSMessage.LACKS_PERMISSION, sender, "jswarps.admin");
                    }
                    
                    if (args.length < 2) {
                        Messaging.send(JSMessage.MISSING_PARAMETER, sender, "name");
                        return false;
                    }
                    
                    // Get warp to overwrite
                    WarpLocation overwrite = null;
                    for (WarpLocation loc : worldWarps) {
                        if (loc.getKey().equalsIgnoreCase(args[1])) {
                            overwrite = loc;
                            break;
                        }
                    }
                    
                    // No more room for warps?
                    if (overwrite == null && worldWarps.size() >= JSWConfigManager.getUsableWarps(player)) {
                        Messaging.send(JSWMessage.OUT_OF_WARPS, sender);
                        return true;
                    }
                    
                    // Remove old warp
                    if (overwrite != null)
                        playerWarps.remove(overwrite);
                    
                    // Get new warp
                    WarpLocation newWarp = new WarpLocation(player.getLocation(), args[1]);
                    playerWarps.add(newWarp);
                    
                    // Save warps
                    JSWarpsManager.saveWarpFile("public");
                    
                    Messaging.send((overwrite == null) ? JSWMessage.WARP_CREATED : JSWMessage.WARP_MOVED, sender, args[1]);
                    return true;
                }
                
                // REMOVE
                if (subcommand.equalsIgnoreCase("remove")) {
                    if (!JSWarps.getPlugin().getPermissionsManager().hasPermission(sender, "admin")) {
                        Messaging.send(JSMessage.LACKS_PERMISSION, sender, "jswarps.admin");
                    }
                    
                    if (args.length < 2) {
                        Messaging.send(JSMessage.MISSING_PARAMETER, sender, "name");
                        return false;
                    }
                    
                    // Get warp and remove it
                    for (WarpLocation loc : worldWarps) {
                        if (loc.getKey().equalsIgnoreCase(args[1])) {
                            playerWarps.remove(loc);
                            JSWarpsManager.saveWarpFile("public");
                            Messaging.send(JSWMessage.WARP_REMOVED, sender);
                            return true;
                        }
                    }
                    
                    // No warp was found
                    Messaging.send(JSWMessage.NO_WARP_NAMED, sender, args[1]);
                    return true;
                }
                
                // GOTO
                if (subcommand.equalsIgnoreCase("goto")) {
                    if (args.length < 2) {
                        Messaging.send(JSMessage.MISSING_PARAMETER, sender, "name");
                        return false;
                    }
                    
                    // Find warp and teleport
                    for (WarpLocation loc : worldWarps) {
                        if (loc.getKey().equalsIgnoreCase(args[1])) {
                            loc.teleportTo(player);
                            Messaging.send(JSWMessage.WARPED_TO, sender, args[1]);
                            return true;
                        }
                    }
                    
                    // No warp was found
                    Messaging.send(JSWMessage.NO_WARP_NAMED, sender, args[1]);
                    return true;
                }
                
                return false;
   
            }
            
        };
        
        private String[] aliases;
        
        public static boolean handleCommand(Command command, CommandSender sender, String... args) {
            JSWarpsCommand handler = valueOf(command.getName().toUpperCase());
            if (!handler.hasPermission(sender)) {
                Messaging.send(JSMessage.LACKS_PERMISSION, sender, handler.getPermission());
                return true;
            }
            return handler.handle(sender, args);
        }
        
        JSWarpsCommand (String... aliases) {
            this.aliases = aliases;
        }
        
        public String[] getAliases() {
            return aliases;
        }
        
        public String getPermission() {
            return JSWarps.getPlugin().getPermissionsManager().getPermission(name().toLowerCase());
        }
        
        public boolean hasPermission(CommandSender sender) {
            return JSWarps.getPlugin().getPermissionsManager().hasPermission(sender, name().toLowerCase());
        }
        
        public String getPermissionMessage() {
            return Messaging.fillArgs(JSMessage.LACKS_PERMISSION, getPermission());
        }
        
        public String getSubPermission(String node) {
            return getPermission() + "." + node;
        }
        
        public boolean hasSubPermission(CommandSender sender, String node) {
            return JSWarps.getPlugin().getPermissionsManager().hasPermission(sender, name().toLowerCase() + "." + node);
        }
        
        public abstract boolean handle(CommandSender sender, String... args);
        
    }

}
