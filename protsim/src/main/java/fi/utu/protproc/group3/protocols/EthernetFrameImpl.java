package fi.utu.protproc.group3.protocols;

import java.nio.ByteBuffer;
import java.util.Objects;

public class EthernetFrameImpl implements EthernetFrame {
    private final byte[] destination;
    private final byte[] source;
    private final short type;
    private final byte[] payload;

    public EthernetFrameImpl(byte[] destination, byte[] source, short type, byte[] payload) {
        this.destination = destination;
        this.source = source;
        this.type = type;
        this.payload = payload;
    }

    public static EthernetFrameImpl parse(byte[] pdu) {
        Objects.requireNonNull(pdu);

        var dest = new byte[6];
        var src = new byte[6];
        byte[] payload = new byte[pdu.length - 14];
        var buf = ByteBuffer.allocate(pdu.length);
        buf.put(pdu).rewind();

        buf.get(dest);
        buf.get(src);
        short type = buf.getShort();
        buf.get(payload);

        return new EthernetFrameImpl(dest, src, type, payload);
    }

    @Override
    public byte[] getDestination() {
        return destination;
    }

    @Override
    public byte[] getSource() {
        return source;
    }

    @Override
    public short getType() {
        return type;
    }

    @Override
    public byte[] getPayload() {
        return payload;
    }

    @Override
    public byte[] serialize() {
        var length = 14 + Math.max(46, getPayload().length);
        var buf = ByteBuffer.allocate(length)
                .put(destination)
                .put(source)
                .putShort(type)
                .put(payload);

        return buf.array();
    }
}
