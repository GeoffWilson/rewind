package com.pigletcraft.rewind;

import com.google.gson.Gson;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class Rewind extends JavaPlugin implements Listener {

    public static final JedisPool redisPool = new JedisPool("127.0.0.1");

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
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Jedis redis = redisPool.getResource();
        redis.select(1);
        Block b = event.getBlock();
        int blockX = b.getX();
        int blockY = b.getY();
        int blockZ = b.getZ();
        String key = String.format("%d,%d,%d", blockX,blockY,blockZ);
        String date = String.format("%d", System.currentTimeMillis());
        Gson g = new Gson();

        BlockStorage block = new BlockStorage(b);

        redis.hset(key, date, g.toJson(block));
        redisPool.returnResource(redis);
    }
}
