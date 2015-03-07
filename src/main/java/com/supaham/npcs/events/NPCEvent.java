package com.supaham.npcs.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

/**
 * Represents a base {@link Event} class for NPCs. 
 */
public abstract class NPCEvent extends Event {
  
  private final Entity npc;

  public NPCEvent(Entity npc) {
    this.npc = npc;
  }

  public Entity getNpc() {
    return npc;
  }
}
