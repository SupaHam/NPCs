package com.supaham.npcs.npcs.handlers;

import com.supaham.npcs.NPCManager;
import com.supaham.npcs.npcs.NPCHandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.utils.NMSUtils;

public class DefaultHandler extends NPCHandler {

  public static final String NAME = "DefaultHandler";

  public DefaultHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onNPCSpawn(NPCSpawnEvent event) {
    Entity npc = event.getNpc();
    if (npc instanceof LivingEntity) {
      LivingEntity living = (LivingEntity) npc;
      living.setRemoveWhenFarAway(false);
      living.setCanPickupItems(false);
    } else if (npc instanceof Item) {
      NMSUtils.applyNBTStringSafe(npc, "{Age:-32768,PickupDelay:32767}");
    }
  }
}
