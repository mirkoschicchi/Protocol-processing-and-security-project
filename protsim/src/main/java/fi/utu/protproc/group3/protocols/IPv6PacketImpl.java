package fi.utu.protproc.group3.protocols;

import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;

/**
 * Represents an IPv6 Packet
 */
public class IPv6PacketImpl implements IPv6Packet {
    // RFC 2460
    private byte version;
    private byte trafficClass;
    private int flowLabel;
    private short payloadLength;
    byte nextHeader;
    private byte hopLimit;
    private IPAddress sourceIP;
    private IPAddress destinationIP;
    private byte[] payload;

    IPv6PacketImpl(byte version, byte trafficClass, int flowLabel, short payloadLength,
                   byte nextHeader, byte hopLimit, IPAddress sourceIP, IPAddress destinationIP, byte[] payload) {
        this.version = version;
        this.trafficClass = trafficClass;
        this.flowLabel = flowLabel;
        this.payloadLength = payloadLength;
        this.nextHeader = nextHeader;
        this.hopLimit = hopLimit;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.payload = payload;
    }

    public static IPv6PacketImpl parse(byte[] pdu) {
        ByteBuffer bb = ByteBuffer.wrap(pdu);
        // Retrieve values from IPv6 header.
        byte firstByte = bb.get();
        byte secondByte = bb.get();
        byte version = (byte) ((firstByte & 0xF0) >>> 4); // 4 bit
        if (version != 6) {
            throw new IllegalArgumentException("Invalid version for IPv6 packet: " + version);
        }
        byte trafficClass = (byte) (((firstByte & 0xF) << 4) | ((secondByte & 0xF0) >>> 4)); // 8 bit
        int flowLabel = ((secondByte & 0xF) << 16) | (bb.getShort() & 0xFFFF); // 20 bit
        short payloadLength = bb.getShort(); // 16 bit
        byte nextHeader = bb.get(); // 8 bit
        byte hopLimit = bb.get(); // 8 bit
        byte[] sourceIP = new byte[16];
        bb.get(sourceIP, 0, 16);
        byte[] destinationIP = new byte[16];
        bb.get(destinationIP, 0, 16);

        // Retrieve the payload, if possible.
        short payloadBytes = (short)Math.min(payloadLength, bb.remaining());
        byte[] payload = new byte[payloadBytes];
        bb.get(payload, 0, payloadBytes);

        return new IPv6PacketImpl(version, trafficClass, flowLabel, payloadLength,
                nextHeader, hopLimit, new IPAddress(sourceIP), new IPAddress(destinationIP), payload);
    }

    public byte getVersion() {
        return version;
    }

    public byte getTrafficClass() {
        return trafficClass;
    }

    public int getFlowLabel() {
        return flowLabel;
    }

    public short getPayloadLength() {
        return payloadLength;
    }

    public byte getNextHeader() {
        return nextHeader;
    }

    public byte getHopLimit() {
        return hopLimit;
    }

    public IPAddress getSourceIP() {
        return sourceIP;
    }

    public IPAddress getDestinationIP() {
        return destinationIP;
    }

    public byte[] getPayload() {
        return payload;
    }

    public byte[] serialize() {
        int length = 16+16+1;
        if (payload != null) {
            length += payload.length;
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.put(sourceIP.toArray());
        bb.put(destinationIP.toArray());
        bb.put(hopLimit);
        if (payload != null){
            bb.put(payload);
        }

        return data;
    }
}
