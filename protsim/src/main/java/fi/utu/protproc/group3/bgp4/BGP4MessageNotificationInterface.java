package fi.utu.protproc.group3.bgp4;

public interface BGP4MessageNotificationInterface extends BGP4MessageInterface {
    static BGP4MessageNotificationInterface create(short errorCode, short errorSubCode, byte[] data) {
        throw new UnsupportedOperationException();
    }

    short getErrorCode();
    short getErrorSubCode();
    byte[] getData();
}
