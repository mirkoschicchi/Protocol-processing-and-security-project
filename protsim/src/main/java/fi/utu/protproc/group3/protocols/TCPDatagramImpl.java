package fi.utu.protproc.group3.protocols;

import java.nio.ByteBuffer;

public class TCPDatagramImpl implements TCPDatagram {
    // RFC-793
    private int sourcePort;
    private int destinationPort;
    private long seqN = 0;
    private long ackN = 0;
    private byte flags;
    private int checksum;
    private byte[] payload;

    public TCPDatagramImpl(int destinationPort, int sourcePort, byte flags, long seqN, long ackN, byte[] payload) {
        if (destinationPort != 179) {
            throw new IllegalArgumentException("Destination port must be 179!"); // BGP router only receives BGP messages to port 179
        }

        if (sourcePort < 0 || sourcePort > 65535) { // maybe 1024 - 49151?
            throw new IllegalArgumentException("Invalid port! It must be 0 <= port <= 65535");
        }

        if (flags == TCPDatagram.SYN) { // new connection
            if (this.seqN != 0) {
                throw new IllegalArgumentException("Connection already established!");
            }
            long leftLimit = 1L;
            long rightLimit = 4294967295L;
            this.seqN = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        }

        if (flags == TCPDatagram.SYNACK) { // connection already established
            if (this.seqN != (ackN - 1)){
                throw new IllegalArgumentException("Invalid (SYN) ACK number!");
            }

            this.seqN++;
            this.ackN = seqN + 1;
        }

        if (flags == TCPDatagram.FIN) { // closing connection
            long leftLimit = 1L;
            long rightLimit = 4294967295L;
            this.seqN = leftLimit + (long) (Math.random() * (rightLimit - leftLimit));
        }

        if (flags == TCPDatagram.FINACK) { // closing connection
            if (this.ackN != (seqN - 1)){
                throw new IllegalArgumentException("Invalid (FIN) ACK number!");
            }

            this.ackN = seqN + 1;
        }

        if (flags == ACK) { // closed connection
            if (ackN != (this.seqN - 1)){
                throw new IllegalArgumentException("Invalid last ACK number!");
            }
        }

        this.destinationPort = destinationPort;
        this.sourcePort = sourcePort;
        this.flags = flags;
        this.payload = payload;
    }

    public int getSourcePort() {
        return sourcePort;
    }
    public TCPDatagramImpl setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
        return this;
    }

    public int getDestinationPort() {
        return destinationPort;
    }
    public TCPDatagramImpl setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
        return this;
    }

    public long getSeqN() {
        return this.seqN;
    }
    public TCPDatagramImpl setSeqN(long seq) {
        this.seqN = seq;
        return this;
    }

    public long getAckN() {
        return this.ackN;
    }
    public TCPDatagramImpl setAckN(long ack) {
        this.ackN = ack;
        return this;
    }

    public byte getFlags() {
        return this.flags;
    }
    public TCPDatagramImpl setFlags(byte flags) {
        this.flags = flags;
        return this;
    }

    public int getChecksum() {
        return this.checksum;
    }
    public TCPDatagramImpl setChecksum(int checksum) {
        this.checksum = checksum;
        return this;
    }
    public void resetChecksum() {
        this.checksum = 0;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] serialize() {
        int length = 4+4+8+8+2+4;
        if (payload != null) {
            length += payload.length;
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.putInt(this.sourcePort); //TCP ports are defined to be 16 bits but in Java we do not have unsigned
        bb.putInt(this.destinationPort);
        bb.putLong(this.seqN);
        bb.putLong(this.ackN);
        bb.put(this.flags);
        bb.putInt(this.checksum); // it should be calculated
        if (payload != null){
            bb.put(payload);
        }

        return data;
    }

    public TCPDatagram parse(byte[] pdu) throws UnsupportedOperationException {
        ByteBuffer bb = ByteBuffer.wrap(pdu);
        this.sourcePort = bb.getInt(); // short will be signed, pos or neg
        this.destinationPort = bb.getInt(); // convert range 0 to 65534, not -32768 to 32767
        this.seqN = bb.getLong();
        this.ackN = bb.getLong();
        this.flags = bb.get();
        this.checksum = bb.getInt();
        this.payload = new byte[bb.remaining()];

        return this;
    }
}
