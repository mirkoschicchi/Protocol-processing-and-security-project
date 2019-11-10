package fi.utu.protproc.group3.protocols;

import java.net.Inet6Address;

/**
 * Represents an IPv6 Packet
 */
public interface IPv6Packet {
    static IPv6Packet create(Inet6Address destinationIP, Inet6Address sourceIP, byte hopLimit, byte[] payload) {
        throw new UnsupportedOperationException();
    }

    static IPv6Packet parse(byte[] pdu) {
        throw new UnsupportedOperationException();
    }

    Inet6Address getDestinationIP();
    Inet6Address getSourceIP();
    byte getHopLimit();
    int getPayloadLength();
    byte[] getPayload();

    /**
     * Serializes the packet to transmit over the wire.
     */
    byte[] serialize();
}
