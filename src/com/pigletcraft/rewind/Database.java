package com.pigletcraft.rewind;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Connection to the MySQL database
 *
 * @author Ben Carvell
 * @author Geoff Wilson
 */
public class Database {

    private String username;
    private String password;

    private static Connection connection;

    public Database() {

        Properties prop = new Properties();

        try {
            FileInputStream fis = new FileInputStream("permissions.properties");
            prop.load(fis);
            username = prop.getProperty("username");
            password = prop.getProperty("password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws SQLException {
        if (connection == null || !connection.isValid(1000)) {
            String connectionString = "jdbc:mysql://localhost/piglet";
            connection = DriverManager.getConnection(connectionString, username, password);
        }
    }

    public int getPlayerId(String playerName) {
        int playerId = 0;
        try {
            connect();
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM minecraft_user WHERE name = ?");
            statement.setString(1, playerName);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                playerId = result.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return playerId;
    }

    public void insertHistory(LocationHistory history) {
        try {
            connect();

            PreparedStatement statement = connection.prepareStatement("INSERT INTO location_history (player_id, action_type_id, location_id, type_id, data) VALUES (?, ?, ?, ?, ?)");
            statement.setInt(1, history.getPlayerId());
            statement.setInt(2, history.getActionTypeId());
            statement.setInt(3, history.getLocationId());
            statement.setInt(4, history.getTypeId());
            statement.setInt(5, history.getData());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getLocationId(int x, int y, int z, int worldId) {
        int locationId = 0;
        try {
            connect();
            PreparedStatement statement = connection.prepareStatement("SELECT id FROM location WHERE x = ? AND y = ? AND z = ? AND world_id = ?");
            statement.setInt(1, x);
            statement.setInt(2, y);
            statement.setInt(3, z);
            statement.setInt(4, worldId);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                locationId = result.getInt("id");
            } else {
                statement.close();
                statement = connection.prepareStatement("INSERT INTO location (x, y, z, world_id) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
                statement.setInt(1, x);
                statement.setInt(2, y);
                statement.setInt(3, z);
                statement.setInt(4, worldId);
                statement.executeUpdate();
                result = statement.getGeneratedKeys();
                if (result.next()) {
                    locationId = result.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return locationId;
    }

    public ArrayList<LocationHistory> getOldestChanges(String timestamp, int playerId, Location max, Location min) {
        try {
            connect();
            PreparedStatement statement = connection.prepareStatement("SELECT l.x, l.y, l.z, type_id, data, action_type_id, MIN(timestamp) FROM location_history lh JOIN location l ON lh.location_id = l.id WHERE timestamp >= ? AND player_id >= ? AND location_id IN (SELECT id FROM location WHERE x BETWEEN ? AND ? AND y BETWEEN ? AND ? AND z BETWEEN ? AND ?) GROUP BY location_id");

            statement.setString(1, timestamp);
            statement.setInt(2, playerId);
            statement.setInt(3, min.getBlockX());
            statement.setInt(4, max.getBlockX());
            statement.setInt(5, min.getBlockY());
            statement.setInt(6, max.getBlockY());
            statement.setInt(7, min.getBlockZ());
            statement.setInt(8, max.getBlockZ());

            long start = System.currentTimeMillis();
            ResultSet r = statement.executeQuery();
            long end = System.currentTimeMillis();

            Bukkit.getLogger().info(String.valueOf(end-start));

            ArrayList<LocationHistory> result = new ArrayList<>();

            while (r.next()) {
                LocationHistory history = new LocationHistory();
                history.setTypeId(r.getInt("type_id"));
                history.setData(r.getInt("data"));
                history.setActionTypeId(r.getByte("action_type_id"));
                int x = r.getInt("x");
                int y = r.getInt("y");
                int z = r.getInt("z");
                history.setLocation(new Location(max.getWorld(), x, y, z));
                result.add(history);
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public ArrayList<LocationHistory> getHistory(int locationId) {

        try {
            connect();
            PreparedStatement statement = connection.prepareStatement("SELECT l.*, m.name FROM location_history l JOIN minecraft_user m ON l.player_id = m.id WHERE location_id = ?");
            statement.setInt(1, locationId);
            ResultSet r = statement.executeQuery();

            ArrayList<LocationHistory> result = new ArrayList<>();
            while(r.next()) {
                LocationHistory history = new LocationHistory();
                history.setTypeId(r.getInt("type_id"));
                history.setData(r.getInt("data"));
                history.setTimeStamp(r.getTimestamp("timestamp"));
                history.setActionTypeId((byte) r.getInt("action_type_id"));
                history.setPlayerName(r.getString("name"));
                result.add(history);
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();

    }
}
