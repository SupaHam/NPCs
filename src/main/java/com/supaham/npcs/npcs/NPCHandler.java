package com.supaham.npcs.npcs;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Preconditions;

import com.supaham.npcs.NPCData;
import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public abstract class NPCHandler implements Listener {

  protected final NPCManager npcManager;
  protected final Plugin plugin;
  private String name;

  protected String npcMetadataPrefix;

  public NPCHandler(NPCManager npcManager, String name) {
    Preconditions.checkNotNull(npcManager, "npc manager cannot be not null.");
    Preconditions.checkNotNull(name, "name cannot be not null.");
    this.npcManager = npcManager;
    this.plugin = npcManager.getOwner();
    this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    checkArgument(!name.isEmpty(), "name cannot be empty");
    this.name = name;
    this.npcMetadataPrefix = "npc-" + name;
  }
  
  public void unregister() {
    HandlerList.unregisterAll(this);
  }
  
  protected boolean hasData(NPCData data, String key) {
    return data.hasData(key);
  }
  
  protected String getDataName(Entity entity) {
    return this.npcManager.getDataNameFromEntity(entity);
  }

  protected <T> T getData(NPCData data, String key, Class<T> expected)
      throws IllegalArgumentException {
    return data.getData(key, expected);
  }
  
  protected void setMetadata(NPCEvent event, String metadata) {
    setMetadata(event, metadata, null);
  }
  
  protected void setMetadata(NPCEvent event, String metadata, Object obj) {
    event.getNpc().setMetadata(metadata, new FixedMetadataValue(this.plugin, obj));
  }

  protected void warn(String msg, Object... args) {
    plugin.getLogger().warning(String.format(msg, args));
  }

  public NPCManager getNpcManager() {
    return npcManager;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public String getName() {
    return name;
  }

  public String getNpcMetadataPrefix() {
    return npcMetadataPrefix;
  }
}
