package com.supaham.npcs.utils;

import net.minecraft.server.v1_9_R1.MojangsonParseException;
import net.minecraft.server.v1_9_R1.MojangsonParser;
import net.minecraft.server.v1_9_R1.NBTTagCompound;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

/**
 * Created by Ali on 17/12/2014.
 */
public class NMSUtils {

  public static NBTTagCompound parse(String string) throws MojangsonParseException {
    return MojangsonParser.parse(string);
  }

  public static NBTTagCompound parseSafe(String string) {
    try {
      return parse(string);
    } catch (MojangsonParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void applyNBTStringSafe(Entity entity, String nbtString) {
    try {
      applyNBTString(entity, nbtString);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void applyNBTString(Entity entity, String nbtString) throws Exception {
    applyNBTString(entity, nbtString, true);
  }

  public static void applyNBTString(Entity entity, String nbtString,
                                    boolean removeUUID) throws MojangsonParseException {
    NBTTagCompound parsed;
    try {
      parsed = NMSUtils.parse(nbtString);
    } catch (MojangsonParseException e) {
      e.printStackTrace();
      return;
    }
    if (removeUUID) {
      // We don't want to change the spawning entity's uuid.
      parsed.remove("UUIDMost");
      parsed.remove("UUIDLeast");
    }

    NBTTagCompound nbt = new NBTTagCompound();
    ((CraftEntity) entity).getHandle().e(nbt); // Write current entity nbt
    nbt.a(parsed); // Append the parsed data to the nbt
    ((CraftEntity) entity).getHandle().f(nbt); // Write the updated nbt to the entity.
  }
}
