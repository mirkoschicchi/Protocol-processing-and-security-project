package fi.utu.protproc.group3.protocols.bgp4;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BGP4MessageKeepaliveImpl extends BGP4MessageImpl implements BGP4MessageKeepalive {
    public BGP4MessageKeepaliveImpl(short length, byte type) {
        super(length, type);
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(getMarker(), 0, getMarker().length);

        byte[] length_array;
        length_array = ByteBuffer.allocate(2).putShort(getLength()).array();
        baos.write(length_array, 0, length_array.length);

        baos.write(getType());

        return baos.toByteArray();
    }
}
