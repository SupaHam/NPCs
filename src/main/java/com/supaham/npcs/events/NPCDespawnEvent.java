package com.supaham.npcs.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents an event for when an NPC despawns.
 */
@Getter
@Setter
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
