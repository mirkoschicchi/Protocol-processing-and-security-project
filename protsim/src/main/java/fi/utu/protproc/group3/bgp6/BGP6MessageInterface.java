package fi.utu.protproc.group3.bgp6;

public interface BGP6MessageInterface {
    short TYPE_OPEN = (short) 0x01;
    short TYPE_UPDATE = (short) 0x02;
    short TYPE_NOTIFICATION = (short) 0x03;
    short TYPE_KEEPALIVE = (short) 0x04;

    byte[] marker = null;
    int length = 19;
    short type = TYPE_OPEN;
    byte[] body = null;

    static BGP6MessageInterface parse(byte[] message) {
        throw new UnsupportedOperationException();
    }

    static BGP6MessageInterface createOpenMessage() {
        throw new UnsupportedOperationException();
    }

    static BGP6MessageInterface createUpdateMessage() {
        throw new UnsupportedOperationException();
    }

    static BGP6MessageInterface createNotificationMessage() {
        throw new UnsupportedOperationException();
    }

    static BGP6MessageInterface createKeepaliveMessage() {
        throw new UnsupportedOperationException();
    }

    /**
     * Serialize a BGP message into a byte array to send through the network
     * @return serialized byte array
     */
    static byte[] serialize() {throw new UnsupportedOperationException(); };
}


