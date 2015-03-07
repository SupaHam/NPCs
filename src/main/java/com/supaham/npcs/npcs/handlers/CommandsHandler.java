package com.supaham.npcs.npcs.handlers;

import com.supaham.npcs.NPCManager;
import com.supaham.npcs.events.NPCSpawnEvent;
import com.supaham.npcs.npcs.NPCHandler;
import com.supaham.npcs.npcs.NPCProperties;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class CommandsHandler extends NPCHandler {

  public static final String NAME = "CommandsHandler";

  private final String cmdsMetadata = this.npcMetadataPrefix + "-cmds";

  public CommandsHandler(NPCManager npcManager) {
    super(npcManager, NAME);
  }

  @EventHandler(ignoreCancelled = true)
  public void onNPCSpawn(NPCSpawnEvent event) {
    String left = getData(event.getData(), NPCProperties.COMMANDS_LEFT_CLICK, String.class);
    String leftPerm = null;
    String right = getData(event.getData(), NPCProperties.COMMANDS_RIGHT_CLICK, String.class);
    String rightPerm = null;
    if (left != null && !left.isEmpty()) {
      leftPerm = getData(event.getData(), NPCProperties.COMMANDS_LEFT_CLICK_PERM, String.class);
    }
    if (right != null && !right.isEmpty()) {
      rightPerm = getData(event.getData(), NPCProperties.COMMANDS_RIGHT_CLICK_PERM, String.class);
    }
    setMetadata(event, this.cmdsMetadata, new Commands(left, leftPerm, right, rightPerm));
  }

  @EventHandler(priority = EventPriority.LOW)
  public void leftClick(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player)) {
      return;
    }
    List<MetadataValue> list = event.getEntity().getMetadata(cmdsMetadata);
    if (!list.isEmpty()) {
      if (list.get(0).value() instanceof Commands) {
        event.setCancelled(true);
        ((Commands) list.get(0).value()).click((Player) event.getDamager(), true);
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void rightClick(PlayerInteractEntityEvent event) {
    List<MetadataValue> list = event.getRightClicked().getMetadata(cmdsMetadata);
    if (!list.isEmpty()) {
      if (list.get(0).value() instanceof Commands) {
        event.setCancelled(true);
        ((Commands) list.get(0).value()).click(event.getPlayer(), false);
      }
    }
  }

  private final class Commands {

    private final String left;
    private final String leftPerm;
    private final String right;
    private final String rightPerm;

    public Commands(String left, String leftPerm, String right, String rightPerm) {
      this.left = left;
      this.leftPerm = leftPerm;
      this.right = right;
      this.rightPerm = rightPerm;
    }

    public boolean click(Player player, boolean left) {
      if (!canInteract(left, player)) {
        player.sendMessage(ChatColor.RED + "You don't have permission to "
                           + (left ? "left" : "right") + " click this npc!");
        return false;
      }

      return execute(player, left ? this.left : this.right);
    }

    public boolean canInteract(boolean left, Player player) {
      if (left) {
        return this.leftPerm == null || player.hasPermission(this.leftPerm);
      } else {
        return this.rightPerm == null || player.hasPermission(this.rightPerm);
      }
    }

    public boolean execute(Player player, String cmd) {
      if (cmd != null) {
        if (player != null) {
          cmd = cmd
              .replaceAll("\\$pname", player.getName())
              .replaceAll("\\$dname", player.getDisplayName());
        }
        if (cmd.startsWith("~") || player == null) {
          Server server = npcManager.getOwner().getServer();
          server.dispatchCommand(server.getConsoleSender(), cmd.substring(1));
        } else {
          player.chat("/" + cmd);
        }
        return true;
      }
      return false;
    }
  }
}
