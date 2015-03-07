package com.supaham.npcs.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

/**
 * Represents an event for when an NPC despawns.
 */
public class NPCDespawnEvent extends NPCEvent {

  private static final HandlerList handlers = new HandlerList();

  public NPCDespawnEvent(Entity npc) {
    super(npc);
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
