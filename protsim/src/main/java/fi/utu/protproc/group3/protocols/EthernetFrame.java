package fi.utu.protproc.group3.protocols;


/**
 * Represents an 802.3 ethernet frame.
 */
public interface EthernetFrame {
    static EthernetFrame create(byte[] destination, byte[] source, short type, byte[] payload) {
        throw new UnsupportedOperationException();
    }

    static EthernetFrame parse(byte[] pdu) {
        throw new UnsupportedOperationException();
    }

    short TYPE_IPV4 = (short) 0x8000;
    short TYPE_IPV6 = (short) 0x86dd;

    byte[] getDestination();
    byte[] getSource();
    short getType();
    byte[] getPayload();

    /**
     * Serializes the packet to transmit over the wire.
     */
    byte[] serialize();
}
