package com.supaham.npcs.npcs.handlers;

import com.google.common.base.Preconditions;

import com.supaham.npcs.NPCData;
import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCDespawnEvent;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.npcs.NPCProperties;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NameHandler extends NPCHandler {

  public static final String NAME = "NameHandler";
  public final String nametagMetadata = this.npcMetadataPrefix + "-nametag";

  private final NPCData blankNametag;
  private final Map<Entity, NameData> datas = new HashMap<>();
  private ArmorStandTask task = null;
  private WeakReference<EntityDamageByEntityEvent> lastDamageEvent = new WeakReference<>(null);
  private WeakReference<PlayerInteractEntityEvent> lastInteractEvent = new WeakReference<>(null);

  {
    this.blankNametag = new NPCData("blankNametag", EntityType.ARMOR_STAND, null);
    this.blankNametag.getData()
        .put(NPCProperties.MINECRAFT_NBT,
             "{Invisible:1,Invulnerable:1,NoAI:1,NoGravity:1,NoBasePlate:1,CustomNameVisible:1"
             + ",DisabledSlots:2039552}");
  }

  public NameHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(ignoreCancelled = true)
  public void onNPCSpawn(NPCSpawnEvent event) {
    String name = getData(event.getData(), NPCProperties.NAME, String.class);
    if (name == null) {
      return;
    }
    Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty.");
    Entity npc = event.getNpc();
    ArmorStand armorStand = (ArmorStand) npcManager.spawn(this.blankNametag, npc.getLocation());
    if (armorStand == null) {
      warn("ArmorStand for %s could not be spawned.", event.getData().getId());
      return;
    }
    armorStand.setCustomName(name);
    armorStand.setMetadata(nametagMetadata,
                           new FixedMetadataValue(this.npcManager.getOwner(), npc.getEntityId()));

    this.datas.put(npc, new NameData(npc, armorStand));
    if (this.task == null) {
      this.task = new ArmorStandTask();
    }
  }

  @EventHandler
  public void onNPCDespawn(NPCDespawnEvent event) {
    if (event.getNpc().hasMetadata(nametagMetadata)) {
      boolean removed = this.datas.remove(event.getNpc()) != null;
      if (removed && this.datas.size() == 0) {
        this.task.cancel();
        this.task = null;
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (this.lastDamageEvent.get() == event) { // We're already in the midst of the original event.
      return;
    }
    Entity damagee = getVehicleEntity(event.getEntity());
    if (damagee != null) {
      event.setCancelled(true);
      this.lastDamageEvent = new WeakReference<>(
          new EntityDamageByEntityEvent(event.getDamager(), damagee, event.getCause(),
                                        event.getDamage()));
      this.npcManager.getOwner().getServer().getPluginManager().callEvent(this.lastDamageEvent.get());
    }
  }
  /*
  Doesn't look like I actually need to listen for the interact event for some reason.
  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onInteract(PlayerInteractEntityEvent event) {
    if (this.lastInteractEvent.get() == event) { // We're already in the midst of the original event.
      return;
    }
    Entity rightClicked = getVehicleEntity(event.getRightClicked());
    if (rightClicked != null) {
      event.setCancelled(true);
      this.lastInteractEvent = new WeakReference<>(
          new PlayerInteractEntityEvent(event.getPlayer(), event.getRightClicked()));
      this.npcManager.getOwner().getServer().getPluginManager().callEvent(this.lastInteractEvent.get());
    }
  }
  */

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDamage(EntityDamageEvent event) {
    if (event.getEntity().hasMetadata(this.nametagMetadata)) {
      event.setCancelled(true);
    }
  }

  public Entity getVehicleEntity(Entity entity) {
    Preconditions.checkNotNull(entity, "entity cannot be null.");
    List<MetadataValue> metadata = entity.getMetadata(this.nametagMetadata);
    if (!metadata.isEmpty()) {
      int targetId = (int) metadata.get(0).value();
      for (Entity e : entity.getWorld().getEntities()) {
        if (e.getEntityId() == targetId) {
          return e;
        }
      }
    }
    return null;
  }

  private final class NameData {

    private final Entity entity;
    private final ArmorStand armorStand;
    private Location lastLocation;
    private double yMod = 0.0D;

    NameData(Entity entity, ArmorStand armorStand) {
      this.entity = entity;
      this.armorStand = armorStand;
      if (entity instanceof LivingEntity) {
        yMod += ((LivingEntity) entity).getEyeHeight(true);
      }
      yMod -= this.armorStand.getEyeHeight();
    }
  }

  private final class ArmorStandTask extends BukkitRunnable {

    public ArmorStandTask() {
      runTaskTimer(npcManager.getOwner(), 0L, 1L);
    }

    @Override
    public void run() {
      Iterator<NameData> it = datas.values().iterator();
      while (it.hasNext()) {
        NameData data = it.next();
        if (!data.entity.isValid() || !data.armorStand.isValid()) {
          data.armorStand.remove();
          it.remove();
          continue;
        }
        // Don't teleport the armor stand if the entity hasn't moved.
        if (!data.entity.getLocation().equals(data.lastLocation)) {
          data.armorStand.teleport(data.entity.getLocation().add(0, data.yMod, 0));
        }
      }
    }
  }
}
