package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageNotificationImpl extends BGP4MessageImpl {
    static BGP4MessageNotificationImpl create(short errorCode, short errorSubCode, byte[] data) {
        throw new UnsupportedOperationException();
    }

    short getErrorCode();
    short getErrorSubCode();
    byte[] getData();
}
