package com.supaham.npcs.npcs.database;

import static com.google.common.base.Preconditions.checkArgument;
import static com.supaham.commons.utils.StringUtils.normalizeString;

import com.supaham.npcs.NPCData;
import com.supaham.npcs.NPCManager;
import com.supaham.npcs.utils.ConfigUtils;
import com.supaham.npcs.utils.LocationUtils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Represents a YAML implementation of {@link Database}.
 */
public class YAMLDatabase implements Database {

  private final File file;
  private final NPCManager npcManager;
  private YamlConfiguration config;

  public YAMLDatabase(@Nonnull NPCManager npcManager, @Nonnull File file) {
    this.npcManager = npcManager;
    this.file = file;
    Logger logger = npcManager.getOwner().getLogger();
    if (!file.exists()) {
      try {
        if (file.createNewFile()) {
          logger.info("Created " + file.getName() + " for npcs.");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private YamlConfiguration load() {
    if (this.config != null) {
      return this.config;
    }
    return this.config = YamlConfiguration.loadConfiguration(this.file);
  }

  private void save() {
    try {
      this.config.save(this.file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public List<NPCData> findAll() {
    YamlConfiguration config = load();
    ConfigurationSection npcs = config.getConfigurationSection("npcs");
    if (npcs == null) {
      config.createSection("npcs");
      save();
      return Collections.emptyList();
    }
    List<NPCData> result = new ArrayList<>();
    for (String id : npcs.getKeys(false)) {
      ConfigurationSection cs = npcs.getConfigurationSection(id);

      Map<String, Object> vals = ConfigUtils.getValues(cs, true);

      // check the entity type
      // TODO not sure if i should remove this requirement to allow for just base NPCData
      // instances.
      checkArgument(vals.containsKey("type"), "npc type not specified.");
      EntityType type;
      try {
        type = EntityType.valueOf(vals.remove("type").toString());
      } catch (IllegalArgumentException e) {
        e.printStackTrace();
        continue;
      }
      NPCData npcData = new NPCData(id, type, LocationUtils
          .deserialize(vals.remove("location").toString()));

      // add npc data
      for (Entry<String, Object> entry : vals.entrySet()) {
        npcData.getData().put(entry.getKey(), entry.getValue());
      }
      // add to results
      result.add(npcData);
    }
    return result;
  }

  @Override
  public void saveAll(List<NPCData> datas) {
    YamlConfiguration config = load();
    ConfigurationSection npcs = config.getConfigurationSection("npcs");
    if (npcs == null) {
      npcs = config.createSection("npcs");
    }

    for (NPCData data : datas) {
      ConfigurationSection cs = npcs.createSection(normalizeString(data.getId()));
      cs.set("type", data.getType());
      cs.set("location", LocationUtils.serialize(data.getLocation()));
      for (Entry<String, Object> entry : data.getData().entrySet()) {
        cs.set(entry.getKey(), entry.getValue());
      }
    }

    save();
  }
}
