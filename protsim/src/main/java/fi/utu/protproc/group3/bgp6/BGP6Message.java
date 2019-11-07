package fi.utu.protproc.group3.bgp6;

import java.io.Serializable;

public class BGP6Message implements Serializable {
    public byte[] marker;
    public short length;
    public short type;
    public byte[] payload;

    public BGP6Message(byte[] marker, short length, short type, byte[] payload) {
        this.marker = marker;
        this.length = length;
        this.type = type;
        this.payload = payload;
    }

    static byte[] serialize() {

        return null;
    }
}
