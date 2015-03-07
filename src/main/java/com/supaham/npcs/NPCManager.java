package com.supaham.npcs;

import static com.google.common.base.Preconditions.checkState;
import static com.supaham.commons.utils.StringUtils.normalizeString;

import com.supaham.npcs.events.NPCDespawnEvent;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.npcs.database.Database;
import com.supaham.npcs.npcs.handlers.CommandsHandler;
import com.supaham.npcs.npcs.handlers.DefaultHandler;
import com.supaham.npcs.npcs.handlers.NBTHandler;
import com.supaham.npcs.npcs.handlers.NameHandler;
import com.supaham.npcs.npcs.handlers.PersistenceHandler;
import com.supaham.npcs.npcs.handlers.SocialHandler;
import com.supaham.npcs.npcs.handlers.VehicleHandler;
import com.supaham.npcs.npcs.handlers.WorldGuardHandler;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class NPCManager implements Listener {

  public static final String METADATA_KEY = "NPC";
  private final Plugin owner;

  @Getter(AccessLevel.NONE)
  private final Map<Class, NPCHandler> handlers = new HashMap<>();
  @Getter(AccessLevel.NONE)
  private final Map<String, NPCHandler> handlersByName = new HashMap<>();
  private final Map<String, NPCData> datas = new HashMap<>();
  private final Set<Entity> npcs = new HashSet<>();
  private boolean defaultsRegistered = false;
  @Getter(AccessLevel.NONE)
  private boolean spawning = false;

  public NPCManager(@NonNull Plugin owner) {
    this(owner, true);
  }

  public NPCManager(@NonNull Plugin owner, boolean defaultHandlers) {
    this.owner = owner;
    this.owner.getServer().getPluginManager().registerEvents(this, this.owner);
    if (defaultHandlers) {
      registerDefaultHandlers();
    }
  }

  public void load(Database database) {
    despawnAll();
    this.datas.clear();
    for (NPCData npcData : database.findAll()) {
      spawn(npcData);
      this.datas.put(normalizeString(npcData.getId()), npcData);
    }
  }

  public void reload(Database database) {
    load(database);
  }

  public void save(Database database) {
    database.saveAll(new ArrayList<>(this.datas.values()));
  }

  public void unregister() {
    unregister(null);
  }

  public void unregister(Database database) {
    despawnAll();
    if (database != null) {
      this.save(database);
    }
    for (NPCHandler npcHandler : handlers.values()) {
      npcHandler.unregister();
    }
  }

  @Nullable
  public Entity spawn(@NonNull NPCData npcData) {
    return spawn(npcData, npcData.getLocation());
  }

  @Nullable
  public Entity spawn(@NonNull NPCData npcData, @NonNull Location location) {
    World world = location.getWorld();
    // temporary fix for EntitySpawnEvent being cancelled...
    spawning = true;
    Entity entity = world.spawnEntity(location, npcData.getType());
    spawning = false;
    NPCSpawnEvent event = new NPCSpawnEvent(entity, npcData);
    owner.getServer().getPluginManager().callEvent(event);
    if (event.isCancelled()) {
      entity.remove();
      return null;
    }
    entity.setMetadata(METADATA_KEY, new FixedMetadataValue(this.owner, npcData.getId()));
    this.npcs.add(entity);
    return entity;
  }

  public int despawnAll() {
    int total = 0;
    Iterator<Entity> it = this.npcs.iterator();
    while (it.hasNext()) {
      Entity next = it.next();
      next.remove();
      switch (next.getType()) {
        case ARMOR_STAND:
          NPCDespawnEvent event = new NPCDespawnEvent(next);
          this.owner.getServer().getPluginManager().callEvent(event);
          break;
      }
      it.remove();
      total++;
    }
    return total;
  }

  public void registerDefaultHandlers() {
    checkState(!this.defaultsRegistered, "defaults already registered.");

    registerHandler(CommandsHandler.class);
    registerHandler(DefaultHandler.class);
    registerHandler(NameHandler.class);
    registerHandler(NBTHandler.class);
    registerHandler(PersistenceHandler.class);
    registerHandler(SocialHandler.class);
    registerHandler(VehicleHandler.class);
    registerHandler(WorldGuardHandler.class);

    this.defaultsRegistered = true;
  }

  public String getDataNameFromEntity(Entity entity) {
    try {
      return entity.getMetadata(METADATA_KEY).get(0).value().toString();
    } catch (NullPointerException e) {
      return null;
    }
  }

  @NonNull
  public Collection<NPCHandler> getAllNPCHandlers() {
    return this.handlers.values();
  }

  @Nullable
  public <T extends NPCHandler> T getNPCHandler(@NonNull Class<T> clazz) {
    return ((T) this.handlers.get(clazz));
  }

  @Nullable
  public NPCHandler getNPCHandler(@NonNull String name) {
    return this.handlersByName.get(normalizeString(name));
  }

  @NonNull
  public <T extends NPCHandler> T registerHandler(@NonNull Class<T> clazz) {
    T npcHandler;
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor(NPCManager.class);
      constructor.setAccessible(true);
      npcHandler = constructor.newInstance(this);
    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
        | IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }
    registerHandler(npcHandler);
    return npcHandler;
  }

  @Nullable
  public NPCHandler registerHandler(@NonNull NPCHandler npcHandler) {
    NPCHandler put = this.handlers.put(npcHandler.getClass(), npcHandler);
    this.handlersByName.put(npcHandler.getName(), npcHandler);
    return put;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntitySpawn(EntitySpawnEvent event) {
    if (spawning) {
      event.setCancelled(false);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onEntityRemove(EntityDeathEvent event) {
    if (event.getEntity().hasMetadata(METADATA_KEY)) {
      this.owner.getServer().getPluginManager().callEvent(new NPCDespawnEvent(event.getEntity()));
    }
  }
}
