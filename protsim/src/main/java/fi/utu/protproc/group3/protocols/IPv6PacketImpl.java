package fi.utu.protproc.group3.protocols;

import java.net.Inet6Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Represents an IPv6 Packet
 */
public class IPv6PacketImpl implements IPv6Packet {
    // RFC 2460
    private Inet6Address destinationIP;
    private Inet6Address sourceIP;
    private byte hopLimit;
    private byte[] payload;

    IPv6PacketImpl(Inet6Address destinationIP, Inet6Address sourceIP, byte hopLimit, byte[] payload) {
        this.destinationIP = destinationIP;
        this.sourceIP = sourceIP;
        this.hopLimit = hopLimit;
        this.payload = payload;
    }

    public Inet6Address getDestinationIP() {
        return destinationIP;
    }
    public IPv6PacketImpl setDestinationIP(Inet6Address addr) {
        this.destinationIP = addr;
        return this;
    }

    public Inet6Address getSourceIP(){
        return sourceIP;
    }
    public IPv6PacketImpl setSourceIP(Inet6Address addr) {
        this.sourceIP = addr;
        return this;
    }

    public byte getHopLimit(){
        return hopLimit;
    }
    public IPv6PacketImpl setHopLimit(byte hopLimit){
        this.hopLimit = hopLimit;
        return this;
    }

    public int getPayloadLength(){
        return payload.length;
    }

    public byte[] getPayload(){
        return payload;
    }
    public IPv6PacketImpl setPayload(byte[] payload){
        this.payload = payload;
        return this;
    }

    public byte[] serialize() {
        int length = 16+16+1;
        if (payload != null) {
            length += payload.length;
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.put(this.sourceIP.getAddress());
        bb.put(this.destinationIP.getAddress());
        bb.put(this.hopLimit);
        if (payload != null){
            bb.put(payload);
        }

        return data;
    }

    public IPv6Packet parse(byte[] pdu) throws UnknownHostException {
        ByteBuffer bb = ByteBuffer.wrap(pdu);
        byte[] ipPacket = new byte[bb.remaining()];
        byte[] sourceIP = Arrays.copyOfRange(ipPacket, 0, 16);
        byte[] destinationIP = Arrays.copyOfRange(ipPacket, 17, 33);

        this.sourceIP = (Inet6Address) Inet6Address.getByAddress(sourceIP);
        this.destinationIP = (Inet6Address) Inet6Address.getByAddress(destinationIP);
        bb.position(32);
        this.hopLimit = bb.get();
        this.payload = new byte[bb.remaining()];

        return this;
    }
}
