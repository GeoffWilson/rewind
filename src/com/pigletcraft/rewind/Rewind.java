package com.pigletcraft.rewind;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;

public class Rewind extends JavaPlugin implements Listener {

    public static final JedisPool redisPool = new JedisPool("127.0.0.1");

    private RewindListener listener;

    private class BlockStorage {
        public byte data;
        public int type;

        public BlockStorage(Block b) {
            data = b.getData();
            type = b.getType().ordinal();
        }
    }

    @Override
    public void onEnable() {

        // Register as an event handler
        listener = new RewindListener();
        getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void onDisable() {

        listener.shutdown();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        switch (cmd.getName()) {
            case "rewind":

                Bukkit.getLogger().info("In /rewind");

                if (!(sender instanceof Player)) return false;

                Player p = (Player)sender;
                if (!p.isOp()) {
                    return false;
                }

                WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
                if (worldEdit == null) {
                    p.sendMessage(ChatColor.RED + "Could not obtain WorldEdit plugin object");
                    return false;
                }

                Selection selection = worldEdit.getSelection(p);
                if (selection == null) {
                    p.sendMessage(ChatColor.RED + "Please select a region first!");
                    return false;
                }

                if (args.length == 0) {
                    p.sendMessage(ChatColor.RED + "Format is /rewind DateTime [Player]");
                    return false;
                }

                String date = "";
                if (args.length == 2) {
                    date = args[0] + " " + args[1];
                }

                Database db = new Database();
                ArrayList<LocationHistory> history =db.getOldestChanges(date, 1, selection.getMaximumPoint(), selection.getMinimumPoint());

                for (LocationHistory h : history) {
                    switch (h.getActionTypeId()) {
                        case 1: // Place
                            Block b = selection.getWorld().getBlockAt(h.getLocation());
                            b.setType(Material.AIR);
                            break;
                        case 2: // Break
                            b = selection.getWorld().getBlockAt(h.getLocation());
                            b.setType(Material.getMaterial(h.getTypeId()));
                            b.setData((byte) h.getData());
                            break;
                    }
                }

                break;

        }

        return true;
    }
}
