package com.supaham.npcs.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.supaham.commons.utils.NumberUtils.roundExact;
import static java.lang.Double.parseDouble;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Location utils.
 */
public class LocationUtils {

  public static Location deserialize(String str) {
    String[] split = str.split(" ");
    checkArgument(split.length >= 4 && split.length <= 6);
    World world = Bukkit.getWorld(split[0]);
    checkNotNull(world, "world '" + split[0] + " doesn't exist.");
    Location loc = new Location(world, parseDouble(split[1]), parseDouble(split[2]),
                                parseDouble(split[3]));
    if (split.length > 4) {
      loc.setYaw(Float.parseFloat(split[4]));
    }
    if (split.length > 5) {
      loc.setPitch(Float.parseFloat(split[5]));
    }
    return loc;
  }
  
  public static String serialize(Location location) {
    return serialize(location, true);
  }

  public static String serialize(Location location, boolean direction) {
    return serialize(location, direction, direction);
  }

  public static String serialize(Location location, boolean yaw, boolean pitch) {
    return location.getWorld().getName() + " "
           + roundExact(location.getX()) + " "
           + roundExact(location.getY()) + " "
           + roundExact(location.getZ()) + " "
           + (yaw ? roundExact(location.getYaw()) : pitch ? "0 " : "")
           + (pitch ? roundExact(location.getPitch()) : 0);
  }
}
