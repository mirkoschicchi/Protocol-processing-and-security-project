package fi.utu.protproc.group3.protocols.tcp;

import fi.utu.protproc.group3.utils.IPAddress;

import java.nio.ByteBuffer;
import java.util.Objects;

public class TCPDatagramImpl implements TCPDatagram {
    // RFC-793
    private final short sourcePort;
    private final short destinationPort;
    private final int seqN;
    private final int ackN;
    private final short flags;
    private final short window;
    private byte[] payload;
    private short checksum;

    TCPDatagramImpl(short sourcePort, short destinationPort, int seqN, int ackN,
                    short flags, short window, byte[] payload) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.seqN = seqN;
        this.ackN = ackN;
        this.flags = flags;
        this.window = window;
        this.payload = payload;

        if (this.payload == null) this.payload = new byte[0];
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
                flags, window, payload);
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

    public short getChecksum() {
        return this.checksum;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] serialize(IPAddress sourceIP, IPAddress destinationIP) {
        Objects.requireNonNull(sourceIP);
        Objects.requireNonNull(destinationIP);

        int length = 20;
        if (payload != null) {
            length += payload.length;
        }

        ByteBuffer bb = ByteBuffer.allocate(length);
        bb.putShort(this.sourcePort);
        bb.putShort(this.destinationPort);
        bb.putInt(this.seqN);
        bb.putInt(this.ackN);
        bb.putShort((short) (this.flags | (5 << 12)));
        bb.putShort(this.window);
        bb.putShort((short) 0); // checksum (placeholder)
        bb.putShort((short) 0); // urgent pointer
        bb.put(this.payload);

        var sourceIParray = sourceIP.toArray();
        var destinationIParray = destinationIP.toArray();

        var checksum = 6 + payload.length + 20; // protocol + TCP length
        for (var i = 0; i < sourceIParray.length; i += 2) {
            checksum += (((sourceIParray[i] & 0xff) << 8) | (sourceIParray[i + 1] & 0xff))
                    + (((destinationIParray[i] & 0xff) << 8) | (destinationIParray[i + 1] & 0xff));
        }

        bb.rewind();
        for (int i = 0; i < length / 2; ++i) {
            checksum += 0xffff & bb.getShort();
        }

        // pad to an even number of shorts
        if (length % 2 != 0) {
            checksum += (bb.get() & 0xff) << 8;
        }

        checksum = (((checksum >> 16) & 0xffff) + (checksum & 0xffff));

        // second time to carry over any overflows from first addition
        checksum = (((checksum >> 16) & 0xffff) + (checksum & 0xffff));

        this.checksum = (short) (~checksum & 0xffff);

        bb.putShort(16, (short) (this.checksum & 0xffff));

        return bb.array();
    }
}
