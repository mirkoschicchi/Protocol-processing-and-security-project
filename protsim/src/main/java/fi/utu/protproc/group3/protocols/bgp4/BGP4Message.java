package fi.utu.protproc.group3.protocols.bgp4;

import java.io.*;

public abstract class BGP4Message implements BGP4MessageImpl, Serializable {
    public byte[] marker;
    public short length;
    public short type;

    public BGP4Message(byte[] marker, short length, short type) {
        this.marker = marker;
        this.length = length;
        this.type = type;
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
    public byte[] serialize() {
        byte[] serialized = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);) {
            oos.writeObject(this);
            serialized = baos.toByteArray();
        } catch (IOException e) {
            // Error in serialization
            e.printStackTrace();
        }
        return serialized;
    }

}
