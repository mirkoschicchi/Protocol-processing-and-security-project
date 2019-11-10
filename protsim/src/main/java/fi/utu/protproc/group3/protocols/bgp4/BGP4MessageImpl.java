package fi.utu.protproc.group3.protocols.bgp4;

import java.io.*;

public abstract class BGP4MessageImpl implements BGP4Message {
    // Marker is set all to 1 following RFC-4271
    private byte[] marker = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    private short length;
    private byte type;

    public BGP4MessageImpl(short length, byte type) {
        this.length = length;
        this.type = type;
    }

    @Override
    public byte[] getMarker() {
        return marker;
    }

    @Override
    public short getLength() {
        return length;
    }

    @Override
    public byte getType() {
        return type;
    }
}
