package com.supaham.npcs;

import static com.google.common.base.Preconditions.checkState;

import com.supaham.npcs.npcs.christmas14.Elf;
import com.supaham.npcs.npcs.database.YAMLDatabase;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * NPCs plugin main class
 */
public class NPCPlugin extends JavaPlugin {

  private static NPCPlugin instance;

  private NPCManager npcManager;

  public NPCPlugin() {
    checkState(instance == null, "NPCPlugin already initialized.");
    NPCPlugin.instance = this;
  }

  @Override
  public void onEnable() {
    this.npcManager = new NPCManager(this);
    this.npcManager.load(new YAMLDatabase(this.npcManager, new File(getDataFolder(), "npcs.yml")));
    this.npcManager.registerHandler(Elf.class);
  }

  @Override
  public void onDisable() {
    this.npcManager.unregister();
    NPCPlugin.instance = null;
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("npcs.reload")) {
      sender.sendMessage(ChatColor.RED + "You don't have permission.");
      return true;
    }
    this.npcManager
        .reload(new YAMLDatabase(this.npcManager, new File(getDataFolder(), "npcs.yml")));
    return true;
  }
}
