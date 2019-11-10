package fi.utu.protproc.group3.protocols;


/**
 * Represents an 802.3 ethernet frame.
 */
public interface EthernetFrame {
    static EthernetFrame create(byte[] destination, byte[] source, int type, byte[] payload) {
        throw new UnsupportedOperationException();
    }

    static EthernetFrame parse(byte[] pdu) {
        throw new UnsupportedOperationException();
    }

    int TYPE_IPV4 = 0x8000;
    int TYPE_IPV6 = 0x86dd;

    byte[] getDestination();
    byte[] getSource();
    int getType();
    byte[] getPayload();

    /**
     * Serializes the packet to transmit over the wire.
     */
    byte[] serialize();
}
