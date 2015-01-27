package com.supaham.npcs.npcs.handlers;

import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.npcs.NPCProperties;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.supaham.npcs.utils.NMSUtils;

public class PersistenceHandler extends NPCHandler {

  public static final String NAME = "PersistenceHandler";

  private final String invulnerable = this.npcMetadataPrefix + "-invulnerable";
  private final Set<Entity> entities = new HashSet<>();

  public PersistenceHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(ignoreCancelled = true)
  public void onNPCSpawn(NPCSpawnEvent event) {
    Entity npc = event.getNpc();
    if (hasData(event.getData(), NPCProperties.MAX_HEALTH)) {
      if (npc instanceof LivingEntity) {
        ((LivingEntity) npc).setMaxHealth(Double.MAX_VALUE);
        ((LivingEntity) npc).setHealth(Double.MAX_VALUE);
        entities.add(npc);
      } else if (npc instanceof Item) {
        setItemMax(((Item) npc));
        entities.add(npc);
      } else {
        warn("%s is not a %s, thus the %s property was ignored.", event.getData().getId(),
             "living entity or item", NPCProperties.MAX_HEALTH);
      }
    }

    if (hasData(event.getData(), NPCProperties.INVULNERABLE)) {
      if (npc instanceof LivingEntity) {
        setMetadata(event, this.invulnerable, true);
      } else {
        warn("%s is not a %s, thus the %s property was ignored.", event.getData().getId(),
             "living entity", NPCProperties.INVULNERABLE);
      }
    }
  }

  private void setItemMax(Item item) {
    try {
      NMSUtils.applyNBTString(item, "{Health:" + Short.MAX_VALUE + "}");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent event) {
    Entity entity = event.getEntity();
    if (entity.hasMetadata(this.invulnerable)) {
      event.setCancelled(true);
      entity.setFireTicks(0);
    } else if (entity instanceof LivingEntity && ((LivingEntity) entity).getHealth() <= 0
               && ((LivingEntity) entity).getMaxHealth() == Double.MAX_VALUE) {
      ((LivingEntity) entity).setHealth(Double.MAX_VALUE);
    }
  }

  @EventHandler
  public void onEntityDamage(ItemDespawnEvent event) {
    if (event.getEntity().hasMetadata(this.invulnerable)) {
      event.setCancelled(true);
    }
  }

  private final class MaxHealthTask extends BukkitRunnable {

    @Override
    public void run() {
      Iterator<Entity> it = entities.iterator();
      while (it.hasNext()) {
        Entity npc = it.next();
        if (!npc.isValid()) {
          it.remove();
          continue;
        }
        if (npc instanceof Item) {
          setItemMax(((Item) npc));
        } else if (npc instanceof LivingEntity) {
          ((LivingEntity) npc).setMaxHealth(Double.MAX_VALUE);
          ((LivingEntity) npc).setHealth(Double.MAX_VALUE);
        } else {
          warn("%s somehow got into the MaxHeathTask. Removing it from the List...",
               getDataName(npc));
          it.remove();
        }
      }
    }
  }
}
