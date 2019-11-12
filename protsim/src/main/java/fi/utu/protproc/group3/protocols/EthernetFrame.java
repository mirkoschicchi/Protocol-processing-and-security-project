package fi.utu.protproc.group3.protocols;


import java.util.Objects;

/**
 * Represents an 802.3 ethernet frame.
 */
public interface EthernetFrame {
    static EthernetFrame create(byte[] destination, byte[] source, short type, byte[] payload) {
        return new EthernetFrameImpl(destination, source, type, payload);
    }

    static EthernetFrame parse(byte[] pdu) {
        Objects.requireNonNull(pdu);
        return EthernetFrameImpl.parse(pdu);
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
