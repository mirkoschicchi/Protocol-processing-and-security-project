package fi.utu.protproc.group3.protocols;

import fi.utu.protproc.group3.utils.IPAddress;

import java.util.Objects;

/**
 * Represents an IPv6 Packet
 */
public interface IPv6Packet {
    static IPv6Packet create(byte version, byte trafficClass, int flowLabel, short payloadLength,
                             byte nextHeader, byte hopLimit, IPAddress sourceIP, IPAddress destinationIP, byte[] payload) {
        return new IPv6PacketImpl(version, trafficClass, flowLabel, payloadLength,
                nextHeader, hopLimit, sourceIP, destinationIP, payload);
    }

    static IPv6Packet parse(byte[] pdu){
        Objects.requireNonNull(pdu);
        return IPv6PacketImpl.parse(pdu);
    }

    byte getVersion();
    byte getTrafficClass();
    int getFlowLabel();
    short getPayloadLength();
    byte getNextHeader();
    byte getHopLimit();
    IPAddress getSourceIP();
    IPAddress getDestinationIP();
    byte[] getPayload();

    /**
     * Serializes the packet to transmit over the wire.
     */
    byte[] serialize();
}
