package com.supaham.npcs.events;

import com.google.common.base.Preconditions;

import com.supaham.npcs.NPCData;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Represents an event for when an NPC spawns.
 */
public class NPCSpawnEvent extends NPCEvent implements Cancellable {

  private static final HandlerList handlers = new HandlerList();
  private final NPCData data;
  
  private boolean cancelled;

  public NPCSpawnEvent(Entity npc, NPCData data) {
    super(npc);
    Preconditions.checkNotNull(data, "data cannot be null.");
    this.data = data;
  }

  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public NPCData getData() {
    return data;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}
