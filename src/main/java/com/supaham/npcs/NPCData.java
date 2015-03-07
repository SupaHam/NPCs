package com.supaham.npcs;

import static com.google.common.base.Preconditions.checkArgument;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NPCData {

  private final String id;
  private final EntityType type;
  private final Location location;
  private final Map<String, Object> data = new HashMap<>();

  public NPCData(String id, EntityType type, Location location) {
    this.id = id;
    this.type = type;
    this.location = location;
  }

  public boolean hasData(String key) {
    return this.data.containsKey(key);
  }
  
  public <T> T getData(String key, Class<T> expected) throws IllegalArgumentException {
    Object result = this.getData().get(key); 
    if (result == null) {
      return null;
    }
    
    if (expected.isAssignableFrom(List.class) && !(result instanceof List)) {
      result = new ArrayList<>(Arrays.asList(result));
    } else {
      checkArgument(expected.isAssignableFrom(result.getClass()),
                    this.id + "'s '" + key + "' key is not of type " +
                    expected.getSimpleName());
    }
    return (T) result;
  }

  public String getId() {
    return id;
  }

  public EntityType getType() {
    return type;
  }

  public Location getLocation() {
    return location;
  }

  public Map<String, Object> getData() {
    return data;
  }
}
