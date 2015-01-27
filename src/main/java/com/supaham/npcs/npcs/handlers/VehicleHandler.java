package com.supaham.npcs.npcs.handlers;

import static com.supaham.npcs.npcs.NPCProperties.NOT_RIDEABLE;

import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.npcs.NPCProperties;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.spigotmc.event.entity.EntityMountEvent;

/**
 * Adds support for vehicle properties, such as being able to ride the entity. The 
 * {@link NPCProperties#NOT_RIDEABLE} works for all entities, not just {@link Vehicle}s.
 */
public class VehicleHandler extends NPCHandler {

  public static final String NAME = "VehicleHandler";

  private final String notRideable = this.npcMetadataPrefix + "-notRideable";

  public VehicleHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler
  public void onNPCSpawn(NPCSpawnEvent event) {
    if (hasData(event.getData(), NOT_RIDEABLE)) {
      setMetadata(event, this.notRideable, true);
    }
  }

  @EventHandler
  public void onMount(EntityMountEvent event) {
    if (event.getEntity().hasMetadata(this.notRideable)) {
      event.setCancelled(true);
    }
  }
}
