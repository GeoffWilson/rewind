package com.pigletcraft.rewind;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;

public class RewindListener implements Listener {

    private ConcurrentLinkedDeque<LocationHistory> historyQueue;

    private HashMap<String, Integer> playerIdCache;
    private HashMap<String, Integer> locationIdCache;

    private StorageRunnable thread;

    public RewindListener() {
        historyQueue = new ConcurrentLinkedDeque<>();

        playerIdCache = new HashMap<>();
        locationIdCache = new HashMap<>();

        thread = new StorageRunnable(this);
        thread.start();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {

        Player p = event.getPlayer();
        if (!p.isOp()) return;

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (p.getItemInHand().getType() == Material.CARROT_STICK) {

                event.setCancelled(true);

                Database db = new Database();
                Block b = event.getClickedBlock();
                int worldId = 1;
                switch (b.getWorld().getEnvironment()) {
                    case NETHER:
                        worldId = 2;
                        break;
                    case THE_END:
                        worldId = 3;
                        break;
                }
                int locationId = db.getLocationId(b.getX(), b.getY(), b.getZ(), worldId);

                if (locationId == 0) {
                    p.sendMessage(ChatColor.RED + "No History for this block");
                    return;
                }

                ArrayList<LocationHistory> history =  db.getHistory(locationId);

                if (history.size() == 0) {
                    p.sendMessage(ChatColor.RED + "No History for this block");
                    return;
                }

                boolean alt = false;

                p.sendMessage("Block History for X: " + b.getX() + " Y: " + b.getY() + " Z:" + b.getZ());

                for (LocationHistory h : history) {
                    p.sendMessage((alt ? ChatColor.GOLD : ChatColor.LIGHT_PURPLE) + h.toString());
                    alt = !alt;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityExplode(EntityExplodeEvent event) {

        Database db = new Database();

        for (Block b : event.blockList()) {
            int locationId = db.getLocationId(b.getX(), b.getY(), b.getZ(), 1);

            LocationHistory locationHistory = new LocationHistory();

            locationHistory.setLocationId(locationId);
            locationHistory.setPlayerId(5);
            locationHistory.setActionTypeId(ActionTypes.BREAK.getValue());
            locationHistory.setTypeId(b.getTypeId());
            locationHistory.setData(b.getData());

            historyQueue.push(locationHistory);
            if (thread.getState() == Thread.State.WAITING) {
                LockSupport.unpark(thread);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {

        Database db = new Database();

        // Get the block
        Block b = event.getBlock();

        Player p = event.getPlayer();
        int playerId;

        World w = b.getWorld();
        int worldId = 1;

        switch (w.getEnvironment()) {
            case NETHER:
                worldId = 2;
                break;
            case THE_END:
                worldId = 3;
                break;
        }

        if (playerIdCache.containsKey(p.getName())) {
            playerId = playerIdCache.get(p.getName());
        } else {
            playerId = db.getPlayerId(p.getName());
            playerIdCache.put(p.getName(), playerId);
        }

        int locationId = db.getLocationId(b.getX(), b.getY(), b.getZ(), worldId);

        LocationHistory locationHistory = new LocationHistory();

        locationHistory.setLocationId(locationId);
        locationHistory.setPlayerId(playerId);
        locationHistory.setActionTypeId(ActionTypes.BREAK.getValue());
        locationHistory.setTypeId(b.getTypeId());
        locationHistory.setData(b.getData());

        historyQueue.push(locationHistory);
        if (thread.getState() == Thread.State.WAITING) {
            LockSupport.unpark(thread);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {

        Database db = new Database();

        // Get the block
        Block b = event.getBlock();

        Player p = event.getPlayer();
        int playerId;

        World w = b.getWorld();
        int worldId = 1;

        switch (w.getEnvironment()) {
            case NETHER:
                worldId = 2;
                break;
            case THE_END:
                worldId = 3;
                break;
        }

        if (playerIdCache.containsKey(p.getName())) {
            playerId = playerIdCache.get(p.getName());
        } else {
            playerId = db.getPlayerId(p.getName());
            playerIdCache.put(p.getName(), playerId);
        }

        int locationId = db.getLocationId(b.getX(), b.getY(), b.getZ(), worldId);

        LocationHistory locationHistory = new LocationHistory();

        locationHistory.setLocationId(locationId);
        locationHistory.setPlayerId(playerId);
        locationHistory.setActionTypeId(ActionTypes.PLACE.getValue());
        locationHistory.setTypeId(b.getTypeId());
        locationHistory.setData(b.getData());

        historyQueue.push(locationHistory);
        if (thread.getState() == Thread.State.WAITING) {
            LockSupport.unpark(thread);
        }
    }

    public synchronized void shutdown() {
        thread.shutdown();
    }

    protected LocationHistory getNextHistory() {
        return historyQueue.poll();
    }

    private synchronized int getQueueCount() {
        return historyQueue.size();
    }

    private class StorageRunnable extends Thread {

        private RewindListener parent;
        private boolean running = true;

        public StorageRunnable(RewindListener parent) {
            this.parent = parent;
        }

        public synchronized void shutdown() {
            this.running = false;
        }

        @Override
        public void run() {

            Database db = new Database();

            while (running || parent.getQueueCount() > 0) {
                LocationHistory history = parent.getNextHistory();
                if (history == null) {
                    LockSupport.park();
                } else {
                    db.insertHistory(history);
                }
            }
        }
    }

}


