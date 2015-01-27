package com.supaham.npcs.events;

import com.supaham.npcs.NPCData;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Represents an event for when an NPC spawns.
 */
@Getter
@Setter
public class NPCSpawnEvent extends NPCEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  @NonNull
  private final NPCData data;
  
  private boolean cancelled;

  public NPCSpawnEvent(Entity npc, @NonNull NPCData data) {
    super(npc);
    this.data = data;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
