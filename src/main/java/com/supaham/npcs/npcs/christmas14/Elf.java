package com.supaham.npcs.npcs.christmas14;

import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public class Elf extends NPCHandler {

  private static final String NAME = "Elf14Handler";
  
  private final String ELF = "elf14";
  private final List<Entity> elfs = new ArrayList<>();

  public Elf(@NonNull NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(ignoreCancelled = true)
  public void onNPCSpawn(NPCSpawnEvent event) {
    Entity npc = event.getNpc();
    if (hasData(event.getData(), ELF)) {
      this.elfs.add(npc);
    }
  }
  
  @EventHandler
  public void onEntityInteract(PlayerInteractEntityEvent event) {
    boolean removed = this.elfs.remove(event.getRightClicked());
    if (removed) {
      event.setCancelled(true);
      ((LivingEntity) event.getRightClicked()).setHealth(0D);
    }
  }
  
  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    this.elfs.remove(event.getEntity());
  }
}
