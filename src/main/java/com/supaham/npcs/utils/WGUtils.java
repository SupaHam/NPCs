package com.supaham.npcs.utils;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

/**
 * Created by Ali on 26/12/2014.
 */
public class WGUtils {

  public static WorldGuardPlugin get() {
    try {
      Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
      return WorldGuardPlugin.inst();
    } catch (ClassNotFoundException e) {
      return null;
    }
  }
}
