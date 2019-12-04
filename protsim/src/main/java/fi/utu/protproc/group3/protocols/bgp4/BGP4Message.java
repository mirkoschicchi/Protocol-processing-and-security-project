package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4Message {
    byte TYPE_OPEN = (short) 0x1;
    byte TYPE_UPDATE = (short) 0x2;
    byte TYPE_NOTIFICATION = (short) 0x3;
    byte TYPE_KEEPALIVE = (short) 0x4;

    byte[] getMarker();
    short getLength();
    byte getType();

    /**
     * Parse  a byte array into a BGP message
     *
     * @return BGP4Message object
     */
    static BGP4Message parse(byte[] message) {
        return BGP4MessageImpl.parse(message);
    }

    /**
     * Serialize a BGP message into a byte array to send through the network
     * @return serialized byte array
    */
    byte[] serialize();
}


