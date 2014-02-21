package com.pigletcraft.rewind;

import org.bukkit.Location;
import org.bukkit.Material;

import java.sql.Timestamp;

public class LocationHistory {

    private int locationId;
    private int typeId;
    private int data;
    private int playerId;
    private Timestamp timeStamp;
    private byte actionTypeId;

    private String playerName;
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }

    public byte getActionTypeId() {
        return actionTypeId;
    }

    public void setActionTypeId(byte actionTypeId) {
        this.actionTypeId = actionTypeId;
    }

    @Override
    public String toString() {

        StringBuilder output = new StringBuilder();

        output.append(playerName);

        switch (actionTypeId) {
            case 1:
                output.append(" placed ");
                break;
            case 2:
                output.append(" broke  ");
                break;
        }

        output.append(Material.getMaterial(typeId).toString()).append(" at ").append(timeStamp);

        return output.toString();
    }
}
