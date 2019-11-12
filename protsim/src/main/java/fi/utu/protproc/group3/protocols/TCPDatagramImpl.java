package fi.utu.protproc.group3.protocols;

import java.nio.ByteBuffer;
import java.util.Objects;

public class TCPDatagramImpl implements TCPDatagram {
    // RFC-793
    private short sourcePort;
    private short destinationPort;
    private int seqN;
    private int ackN;
    private byte dataOffset;
    private short flags;
    private short window;
    private short checksum;
    private short urgentPointer;
    private byte[] optionsAndPadding;
    private byte[] payload;

    TCPDatagramImpl(short sourcePort, short destinationPort, int seqN, int ackN,
                    byte dataOffset, short flags, short window, short checksum, short urgentPointer,
                    byte[] optionsAndPadding, byte[] payload) {
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.seqN = seqN;
        this.ackN = ackN;
        this.dataOffset = dataOffset;
        this.flags = flags;
        this.window = window;
        this.checksum = checksum;
        this.urgentPointer = urgentPointer;
        this.optionsAndPadding = optionsAndPadding;
        this.payload = payload;
    }

    public static TCPDatagram parse(byte[] pdu) {
        Objects.requireNonNull(pdu);

        ByteBuffer bb = ByteBuffer.wrap(pdu);
        short sourcePort = bb.getShort();
        short destinationPort = bb.getShort();
        int seqN = bb.getInt();
        int ackN = bb.getInt();
        short flags = bb.getShort(); // here we get also data off-set and reserved fields. Total 16 bits.
        byte dataOffset = (byte) ((flags >> 12) & 0xf);
        if (dataOffset < 5) {
            throw new IllegalArgumentException("Invalid tcp header length < 20");
        }
        flags &= 0x1ff;
        short window = bb.getShort();
        short checksum = bb.getShort(); // if it is 0 should be calculated
        short urgentPointer = bb.getShort();
        byte[] optionsAndPadding = new byte[0];
        if (dataOffset > 5) {
            int optLength = (dataOffset << 2) - 20;
            if (bb.limit() < bb.position()+optLength) {
                optLength = bb.limit() - bb.position();
            }
            try {
                optionsAndPadding = new byte[optLength];
                bb.get(optionsAndPadding, 0, optLength);
            } catch (IndexOutOfBoundsException e) {
                optionsAndPadding = new byte[0];
            }
        }

        byte[] payload = new byte[bb.remaining()];
        bb.get(payload, 0, bb.remaining());

        return new TCPDatagramImpl(sourcePort, destinationPort, seqN, ackN,
                dataOffset, flags, window, checksum, urgentPointer, optionsAndPadding, payload);
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

    public byte getDataOffset() {
        return this.dataOffset;
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

    public int getUrgentPointer() {
        return this.urgentPointer;
    }

    public byte[] getOptionsAndPadding() {
        return this.optionsAndPadding;
    }

    public byte[] getPayload() {
        return this.payload;
    }

    public byte[] serialize() {
        int length = 20;
        if (optionsAndPadding != null) {
            length += optionsAndPadding.length;
        }
        if (payload != null) {
            length += payload.length;
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);

        bb.putShort(sourcePort);
        bb.putShort(destinationPort);
        bb.putInt(seqN);
        bb.putInt(ackN);
        bb.putShort((short) (flags | (dataOffset << 12)));
        bb.putShort(window);
        bb.putShort(checksum);
        bb.putShort(urgentPointer);
        bb.put(optionsAndPadding);
        if (payload != null){
            bb.put(payload);
        }

        return data;
    }
}
