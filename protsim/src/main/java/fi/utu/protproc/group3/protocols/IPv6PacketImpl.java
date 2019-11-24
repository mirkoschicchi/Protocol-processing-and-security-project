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
    private byte nextHeader;
    private byte hopLimit;
    private IPAddress sourceIP;
    private IPAddress destinationIP;
    private byte[] payload;

    IPv6PacketImpl(byte version, byte trafficClass, int flowLabel,
                   byte nextHeader, byte hopLimit, IPAddress sourceIP, IPAddress destinationIP, byte[] payload) {
        this.version = version;
        this.trafficClass = trafficClass;
        this.flowLabel = flowLabel;
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

        return new IPv6PacketImpl(version, trafficClass, flowLabel,
                nextHeader, hopLimit, new IPAddress(sourceIP), new IPAddress(destinationIP), payload);
    }

    public byte getVersion() {
        return this.version;
    }

    public byte getTrafficClass() {
        return this.trafficClass;
    }

    public int getFlowLabel() {
        return this.flowLabel;
    }

    public short getPayloadLength() {
        if (this.payload == null) return 0;

        return (short) this.payload.length;
    }

    public byte getNextHeader() {
        return this.nextHeader;
    }

    public byte getHopLimit() {
        return this.hopLimit;
    }

    public IPAddress getSourceIP() {
        return this.sourceIP;
    }

    public IPAddress getDestinationIP() {
        return this.destinationIP;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] serialize() {
        int length = 40; // IPv6 header length in bytes
        if (payload != null) {
            length += payload.length;
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);

        byte firstByte = (byte) (((this.version & 0x0F) << 4) | ((this.trafficClass & 0xF0) >>> 4));
        byte secondByte = (byte) (((this.trafficClass & 0x0F) << 4) | ((this.flowLabel & 0x000F0000) >>> 16));
        short partialFlowLabel = (short) (this.flowLabel & 0x0000FFFF);

        bb.put(firstByte);
        bb.put(secondByte);
        bb.putShort(partialFlowLabel);
        bb.putShort(getPayloadLength());
        bb.put(nextHeader);
        bb.put(hopLimit);
        bb.put(sourceIP.toArray());
        bb.put(destinationIP.toArray());

        if (payload != null) {
            bb.put(payload);
        }

        return data;
    }
}
