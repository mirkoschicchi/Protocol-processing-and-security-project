package fi.utu.protproc.group3.protocols.tcp;

import java.nio.ByteBuffer;
import java.util.Objects;

public class TCPDatagramImpl implements TCPDatagram {
    // RFC-793
    private short sourcePort;
    private short destinationPort;
    private int seqN;
    private int ackN;
    private short flags;
    private short window;
    private short checksum;
    private byte[] payload;

    TCPDatagramImpl(short sourcePort, short destinationPort, int seqN, int ackN,
                    short flags, short window, short checksum, byte[] payload) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.seqN = seqN;
        this.ackN = ackN;
        this.flags = flags;
        this.window = window;
        this.checksum = checksum;
        this.payload = payload;
    }

    public static TCPDatagram parse(byte[] pdu) {
        Objects.requireNonNull(pdu);

        ByteBuffer bb = ByteBuffer.wrap(pdu);
        short sourcePort = (short) (bb.getShort() & 0xffff);
        short destinationPort = (short) (bb.getShort() & 0xffff);
        int seqN = bb.getInt();
        int ackN = bb.getInt();
        short flags = bb.getShort(); // here we get also data off-set and reserved fields. Total 16 bits.
        byte dataOffset = (byte) ((flags >> 12) & 0xf);
        if (dataOffset < 5) {
            throw new IllegalArgumentException("Invalid tcp header length < 20");
        }
        flags = (short) (flags & 0x1ff);
        short window = bb.getShort();
        short checksum = bb.getShort();

        bb.getShort(); // Skip urgent pointer

        bb.position(bb.position() + (dataOffset - 5) * 4); // Skip options

        byte[] payload = new byte[bb.remaining()];
        bb.get(payload, 0, bb.remaining());

        return new TCPDatagramImpl(sourcePort, destinationPort, seqN, ackN,
                flags, window, checksum, payload);
    }

    public short getSourcePort() {
        return this.sourcePort;
    }

    public short getDestinationPort() {
        return this.destinationPort;
    }

    public int getSeqN() {
        return this.seqN;
    }

    public int getAckN() {
        return this.ackN;
    }

    public short getFlags() {
        return this.flags;
    }

    public short getWindow() {
        return this.window;
    }

    public int getChecksum() {
        return this.checksum;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] serialize() {
        int length = 20;
        if (payload != null) {
            length += payload.length;
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.putShort(this.sourcePort);
        bb.putShort(this.destinationPort);
        bb.putInt(this.seqN);
        bb.putInt(this.ackN);
        bb.putShort((short) (this.flags | (5 << 12)));
        bb.putShort(this.window);
        bb.putShort(this.checksum);
        bb.putShort((short) 0); // urgent pointer

        if (this.payload != null) {
            bb.put(this.payload);
        }

        // TODO : Fix this (includes IP)
        // compute checksum if needed
        if (this.checksum == 0) {
            bb.rewind();
            int accumulation = 0;
            for (int i = 0; i < length / 2; ++i) {
                accumulation += 0xffff & bb.getShort();
            }
            // pad to an even number of shorts
            if (length % 2 > 0) {
                accumulation += (bb.get() & 0xff) << 8;
            }

            accumulation = ((accumulation >> 16) & 0xffff)
                    + (accumulation & 0xffff);
            this.checksum = (short) (~accumulation & 0xffff);
        }

        // TODO: Add pseudo header to checksum
        /*
            The checksum also covers a 96 bit pseudo header conceptually
            prefixed to the TCP header.  This pseudo header contains the Source
            Address, the Destination Address, the Protocol, and TCP length.
            This gives the TCP protection against misrouted segments.  This
            information is carried in the Internet Protocol and is transferred
            across the TCP/Network interface in the arguments or results of
            calls by the TCP on the IP.

                             +--------+--------+--------+--------+
                             |           Source Address          |
                             +--------+--------+--------+--------+
                             |         Destination Address       |
                             +--------+--------+--------+--------+
                             |  zero  |  PTCL  |    TCP Length   |
                             +--------+--------+--------+--------+
         */

        bb.putShort(16, this.checksum);

        return data;
    }
}
