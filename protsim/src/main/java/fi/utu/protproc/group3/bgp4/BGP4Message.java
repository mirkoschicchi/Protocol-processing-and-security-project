package fi.utu.protproc.group3.bgp4;

import java.io.Serializable;

public class BGP4Message implements BGP4MessageInterface {
    public byte[] marker;
    public short length;
    public short type;
    public byte[] body;

    public BGP4Message(byte[] marker, short length, short type, byte[] body) {
        this.marker = marker;
        this.length = length;
        this.type = type;
        this.body = body;
    }

    @Override
    public byte[] getMarker() {
        return marker;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public short getType() {
        return type;
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

}
