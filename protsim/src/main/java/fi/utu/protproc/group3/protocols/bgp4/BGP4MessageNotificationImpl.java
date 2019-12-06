package fi.utu.protproc.group3.protocols.bgp4;

import java.nio.ByteBuffer;

public class BGP4MessageNotificationImpl extends BGP4MessageImpl implements BGP4MessageNotification {
    private final byte errorCode;
    private final byte errorSubCode;
    private final byte[] data;

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
        byte[] serialized;
        serialized = ByteBuffer.allocate(19 + 2 + getData().length)
                .put(super.serialize())
                .put(getErrorCode())
                .put(getErrorSubCode())
                .put(getData())
                .array();

        return serialized;
    }
}
