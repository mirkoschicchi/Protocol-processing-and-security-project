package fi.utu.protproc.group3.protocols;

import fi.utu.protproc.group3.utils.IPAddress;

import java.util.Objects;

/**
 * Represents an IPv6 Packet
 */
public interface IPv6Packet {
    static IPv6Packet create(byte nextHeader, IPAddress sourceIP, IPAddress destinationIP, byte[] payload) {
        return create(
                VERSION_IPV6,
                TRAFFIC_CLASS_NONE,
                FLOW_LABEL_DEFAULT,
                nextHeader,
                HOP_LIMIT_DEFAULT,
                sourceIP,
                destinationIP,
                payload
        );
    }

    static IPv6Packet create(byte version, byte trafficClass, int flowLabel,
                             byte nextHeader, byte hopLimit, IPAddress sourceIP, IPAddress destinationIP, byte[] payload) {
        return new IPv6PacketImpl(version, trafficClass, flowLabel,
                nextHeader, hopLimit, sourceIP, destinationIP, payload);
    }

    static IPv6Packet parse(byte[] pdu){
        Objects.requireNonNull(pdu);
        return IPv6PacketImpl.parse(pdu);
    }

    byte VERSION_IPV6 = 0x06;
    byte TRAFFIC_CLASS_NONE = 0x00;
    int FLOW_LABEL_DEFAULT = 0x00;
    byte NEXT_HEADER_TCP = 0x06;
    byte HOP_LIMIT_DEFAULT = (byte) 0x7f;
    byte HOP_LIMIT_MAX = (byte) 0xff;

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
