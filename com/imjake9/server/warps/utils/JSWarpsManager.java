package com.imjake9.server.warps.utils;

import com.imjake9.server.warps.JSWarps;
import java.io.*;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class JSWarpsManager {
    
    private static Map<String, File> warpFiles;
    private static Map<String, List<WarpLocation>> warps;
    
    /**
     * Loads warps from a map of names and files.
     * 
     * @param files 
     */
    public static void loadWarps(Map<String, File> files) {
        
        warpFiles = files;
        
        // Create map for warps
        warps = new HashMap<String, List<WarpLocation>>();
        for (String key : warpFiles.keySet()) {
            try {
                
                // Create warp list for player
                List<WarpLocation> warpList = new ArrayList<WarpLocation>();
                
                // Create reader for warp file
                Scanner s = new Scanner(new FileReader(warpFiles.get(key)));
                
                // Read each line into a WarpLocation
                while (s.hasNextLine()) {
                    String[] elements = s.nextLine().split(":");
                    Location loc = new Location(
                            Bukkit.getServer().getWorld(elements[5]),
                            Double.parseDouble(elements[1]),
                            Double.parseDouble(elements[2]),
                            Double.parseDouble(elements[3]));
                    loc.setYaw(Float.parseFloat(elements[4]));
                    warpList.add(new WarpLocation(loc, elements[0]));
                }
                
                warps.put(key, warpList);
                
            } catch (FileNotFoundException ex) {
                JSWarps.getPlugin().getMessager().warning("Could not load existing warp file for " + key + ".");
            }
        }
        
    }
    
    /**
     * Gets a list of warps for the given key.
     * 
     * @param key
     * @return 
     */
    public static List<WarpLocation> getWarpList(String key) {
        return warps.get(key);
    }
    
    /**
     * Sets a list of warps to the given key.
     * 
     * @param key
     * @param value 
     */
    public static void setWarpList(String key, List<WarpLocation> value) {
        warps.put(key, value);
    }
    
    /**
     * Gets a list of warps from the given key, limited to a world.
     * 
     * @param key
     * @param world
     * @return 
     */
    public static List<WarpLocation> getWarpListForWorld(String key, World world) {
        List<WarpLocation> warpList;
        
        if (!warps.containsKey(key.toLowerCase())) {
            warpList = new ArrayList<WarpLocation>();
        } else {
            warpList = warps.get(key.toLowerCase());
        }
        
        List<WarpLocation> retList = new ArrayList<WarpLocation>();
        
        for (WarpLocation loc : warpList) {
            if (loc.getLocation().getWorld().equals(world))
                retList.add(loc);
        }
        
        return retList;
    }
    
    /**
     * Saves all warp files.
     */
    public static void saveWarps() {
        
        // Save all warp files
        for (String key : warps.keySet()) {
            saveWarpFile(key);
        }
        
    }
    
    /**
     * Saves a single warp file.
     * 
     * @param name 
     */
    public static void saveWarpFile(String name) {
        
         try {
            // Get the file for the player
            File f = new File(JSWarps.getPlugin().getDataFolder(), "warps" + File.separator + name + ".txt");
            if (!f.exists()) f.createNewFile();
            
            // Create a writer
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            
            // Loop through the player's warps and write them out
            for (WarpLocation loc : warps.get(name)) {
                bw.append(loc.getLine());
                bw.newLine();
            }

            bw.close();
        } catch (IOException ex) {
            JSWarps.getPlugin().getMessager().warning("Error in saving warp file.");
            ex.printStackTrace();
        }
        
    }

}
