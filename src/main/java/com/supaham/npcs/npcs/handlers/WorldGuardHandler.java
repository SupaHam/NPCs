package com.supaham.npcs.npcs.handlers;

import static com.supaham.npcs.npcs.NPCProperties.WG_SPAWN_IN;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.supaham.npcs.NPCManager;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.utils.WGUtils;

public class WorldGuardHandler extends NPCHandler {

  public static final String NAME = "WorldGuardHandler";

  public WorldGuardHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onNPCSpawn(NPCSpawnEvent event) {
    Entity npc = event.getNpc();
    Location loc = npc.getLocation();
    List<String> regions = (List<String>) getData(event.getData(), WG_SPAWN_IN, List.class);
    if (regions != null) {
      WorldGuardPlugin wg = WGUtils.get();
      // Check that WorldGuard is available.
      if (wg == null) {
        warn("%s has the %s but WorldGuard is not available, thus the property was ignored.",
             event.getData().getId(), WG_SPAWN_IN);
        return;
      }
      
      RegionManager mgr = wg.getRegionManager(npc.getWorld());
      if (mgr == null) {
        return;
      }

      boolean foundRegion = false;
      boolean canSpawn = false;
      for (String regionStr : regions) {
        ProtectedRegion region = mgr.getRegion(regionStr);
        if (region == null) {
          continue;
        }
        foundRegion = true;
        if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
          canSpawn = true;
        }
      }

      if (foundRegion && !canSpawn) {
        event.setCancelled(true);
      }
    }
  }
}
