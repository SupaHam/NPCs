package com.supaham.npcs.npcs.handlers;

import static com.supaham.npcs.npcs.NPCProperties.MINECRAFT_NBT;

import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.utils.NMSUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

public class NBTHandler extends NPCHandler {

  public static final String NAME = "NBTHandler";

  public NBTHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
  public void onNPCSpawn(final NPCSpawnEvent event) {
    final String nbtString = getData(event.getData(), MINECRAFT_NBT, String.class);
    new BukkitRunnable() {
      @Override
      public void run() {
        if (nbtString != null) {
          try {
            NMSUtils.applyNBTString(event.getNpc(), nbtString);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    }.runTaskLater(plugin, 2);
  }
}
