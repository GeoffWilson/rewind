package com.pigletcraft.rewind;

public enum ActionTypes {

    BREAK((byte)2),
    PLACE((byte)1);

    private final byte value;
    private ActionTypes(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return this.value;
    }
}
