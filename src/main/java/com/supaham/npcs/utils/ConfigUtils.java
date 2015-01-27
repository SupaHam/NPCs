package com.supaham.npcs.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Ali on 25/12/2014.
 */
public class ConfigUtils {
  
  public static Map<String, Object> getValues(ConfigurationSection cs, boolean deep) {
    Map<String,Object> result = new LinkedHashMap<>();
    for (Entry<String, Object> entry : cs.getValues(deep).entrySet()) {
      Object value = entry.getValue();
      if (entry.getValue() instanceof ConfigurationSection) {
        value = getValues(((ConfigurationSection) value), false);
      }
      result.put(entry.getKey(), value);
    }
    return result;
  }
}
