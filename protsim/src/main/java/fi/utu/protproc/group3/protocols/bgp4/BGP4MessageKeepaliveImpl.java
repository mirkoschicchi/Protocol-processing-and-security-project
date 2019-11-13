package fi.utu.protproc.group3.protocols.bgp4;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BGP4MessageKeepaliveImpl extends BGP4MessageImpl implements BGP4MessageKeepalive {
    public BGP4MessageKeepaliveImpl(short length, byte type) {
        super(length, type);
    }

    @Override
    public byte[] serialize() {
        return super.serialize();
    }
}
