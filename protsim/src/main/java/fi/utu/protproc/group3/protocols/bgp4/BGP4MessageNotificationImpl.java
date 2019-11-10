package fi.utu.protproc.group3.protocols.bgp4;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class BGP4MessageNotificationImpl extends BGP4MessageImpl implements BGP4MessageNotification {
    private byte errorCode;
    private byte errorSubCode;
    private byte[] data;

    public BGP4MessageNotificationImpl(short length, byte type, byte errorCode, byte errorSubCode, byte[] data) {
        super(length, type);
        this.errorCode = errorCode;
        this.errorSubCode = errorSubCode;
        this.data = data;
    }

    @Override
    public byte getErrorCode() {
        return errorCode;
    }

    @Override
    public byte getErrorSubCode() {
        return errorSubCode;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(getMarker(), 0, getMarker().length);

        byte[] length_array;
        length_array = ByteBuffer.allocate(2).putShort(getLength()).array();
        baos.write(length_array, 0, length_array.length);

        baos.write(getType());

        baos.write(getErrorCode());

        baos.write(getErrorSubCode());

        baos.write(getData(), 0, getData().length);

        return baos.toByteArray();
    }
}
