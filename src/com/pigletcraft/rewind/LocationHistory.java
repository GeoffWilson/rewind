package com.pigletcraft.rewind;

import java.sql.Timestamp;

/**
 * Created by Benshiro on 16/02/14.
 */
public class LocationHistory {

    private int locationId;
    private int typeId;
    private int data;
    private int playerId;
    private Timestamp timeStamp;
    private byte actionTypeId;

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
        return String.format("Action: %d, Type: %d, Data: %d, Modified: %s", actionTypeId, typeId, data, timeStamp);
    }
}
