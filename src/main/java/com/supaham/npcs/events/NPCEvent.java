package com.supaham.npcs.events;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a base {@link Event} class for NPCs. 
 */
@RequiredArgsConstructor
@Getter
public abstract class NPCEvent extends Event {
  
  @NonNull
  private final Entity npc;
}
