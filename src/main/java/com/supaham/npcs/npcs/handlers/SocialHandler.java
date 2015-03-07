package com.supaham.npcs.npcs.handlers;

import static com.supaham.npcs.npcs.NPCProperties.SOCIAL;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import com.supaham.commons.utils.CollectionUtils;
import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCDespawnEvent;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.npcs.NPCProperties;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Adds support for vehicle properties, such as being able to ride the entity. The
 * {@link NPCProperties#NOT_RIDEABLE} works for all entities, not just {@link Vehicle}s.
 */
public class SocialHandler extends NPCHandler {

  public static final String NAME = "SocialHandler";

  private final String socialMetadata = this.npcMetadataPrefix + "-social";
  private final ListMultimap<Entity, Social> tasks = ArrayListMultimap.create();

  private final Map<Entity, ApproachSocial> approachSociables = new HashMap<>();

  public SocialHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(ignoreCancelled = true)
  public void onNPCSpawn(NPCSpawnEvent event) {
    Map<String, Object> map = getData(event.getData(), SOCIAL, Map.class);
    if (map == null) {
      return;
    }
    Entity npc = event.getNpc();
    for (Entry<String, Object> entry : map.entrySet()) {
      Social social = null;
      switch (entry.getKey().toLowerCase()) {
        case "nearby":
          social = new ApproachSocial(npc, ((Map<String, Object>) entry.getValue()));
          this.approachSociables.put(npc, ((ApproachSocial) social));
          break;
      }
      if (social != null) {
        this.tasks.put(npc, social);
      }
    }

    setMetadata(event, this.socialMetadata);
  }

  @EventHandler
  public void onNPCDespawn(NPCDespawnEvent event) {
    remove(event.getNpc());
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getFrom().getY() != event.getTo().getY()) {
      return;
    }
    if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
      return;
    }
    for (ApproachSocial approachSocial : approachSociables.values()) {
      approachSocial.handle(event);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    for (ApproachSocial approachSocial : approachSociables.values()) {
      approachSocial.handle(event);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onWorldChange(PlayerChangedWorldEvent event) {
    for (ApproachSocial approachSocial : approachSociables.values()) {
      approachSocial.handle(event);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    for (ApproachSocial approachSocial : approachSociables.values()) {
      approachSocial.handle(event);
    }
  }

  private void remove(Entity entity) {
    List<Social> socials = this.tasks.removeAll(entity);
    for (Social social : socials) {
      if (social instanceof ApproachSocial) {
        this.approachSociables.remove(entity);
      }
    }
  }

  @RequiredArgsConstructor
  @Getter
  private class Social {

    protected Entity npc;
  }

  public final class ApproachSocial extends Social {

    private int cooldown = 0;
    private double range = 1;
    private List<String> messages = new ArrayList<>();
    private Map<Player, Long> lastReceived = new HashMap<>();
    private List<Player> received = new ArrayList<>();

    private ApproachSocial(Entity npc, Map<String, Object> map) {
      this.npc = npc;
      if (map.containsKey("cooldown")) {
        this.cooldown = Integer.parseInt(map.get("cooldown").toString());
      }
      if (map.containsKey("range")) {
        this.range = Double.parseDouble(map.get("range").toString());
      }
      for (String s : (List<String>) map.get("messages")) {
        this.messages.add(ChatColor.translateAlternateColorCodes('&', s));
      }
    }

    public void handle(PlayerMoveEvent event) {
      Location npcLoc = this.npc.getLocation();
      if (!event.getTo().getWorld().equals(npcLoc.getWorld())) {
        return;
      }
      
      if (event.getTo().distance(npcLoc) <= this.range) {
        if (this.received.contains(event.getPlayer())) {
          return;
        }
        chatTo(event.getPlayer());
      } else {
        this.received.remove(event.getPlayer());
      }
    }
    
    public void handle(PlayerChangedWorldEvent event) {
      this.received.remove(event.getPlayer());
    }
    
    public void handle(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      this.lastReceived.remove(event.getPlayer());
      this.received.remove(player);
    }

    public void chatTo(Player player) {
      if (this.cooldown > 0 && this.lastReceived.containsKey(player)) {
        // Still on cooldown
        if (System.currentTimeMillis() - this.lastReceived.get(player) < 0) {
          return;
        } else {
          this.lastReceived.remove(player);
        }
      }

      String message = CollectionUtils.getRandomElement(messages)
          .replaceAll("\\$pname", player.getName())
          .replaceAll("\\$pdname", player.getDisplayName());

      player.sendMessage(message);
      received.add(player);
      if (this.cooldown > 0) {
        this.lastReceived.put(player, System.currentTimeMillis() + (cooldown * 1000));
      }
    }
  }
}
