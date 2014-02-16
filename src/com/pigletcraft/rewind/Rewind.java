package com.pigletcraft.rewind;

import org.bukkit.block.Block;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;

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
}
