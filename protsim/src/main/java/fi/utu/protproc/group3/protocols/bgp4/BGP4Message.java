package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4Message {
    short TYPE_OPEN = (short) 0x1;
    short TYPE_UPDATE = (short) 0x2;
    short TYPE_NOTIFICATION = (short) 0x3;
    short TYPE_KEEPALIVE = (short) 0x4;

    byte[] getMarker();
    int getLength();
    short getType();

    static BGP4Message create(byte[] marker, int length, short type) {
        throw new UnsupportedOperationException();
    }

    static BGP4Message parse(byte[] message) {
        throw new UnsupportedOperationException();
    }

    /**
     * Serialize a BGP message into a byte array to send through the network
     * @return serialized byte array
     */
    byte[] serialize();
}


