package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageNotification extends BGP4Message {
    // Error codes
    byte ERR_CODE_MESSAGE_HEADER_ERROR = (short) 0x1;
    byte ERR_CODE_OPEN_MESSAGE_ERROR = (short) 0x2;
    byte ERR_CODE_UPDATE_MESSAGE_ERROR = (short) 0x3;
    byte ERR_CODE_HOLD_TIMER_EXPIRED = (short) 0x4;
    byte ERR_CODE_FINITE_STATE_MACHINE_ERROR = (short) 0x5;
    byte ERR_CODE_CAESE = (short) 0x6;

    // Message Header Error subcodes
    byte ERR_SUBCODE_CONNECTION_NOT_SYNC = (short) 0x1;
    byte ERR_SUBCODE_BAD_MESSAGE_LENGTH = (short) 0x2;
    byte ERR_SUBCODE_BAD_MESSAGE_TYPE = (short) 0x3;

    static BGP4MessageNotification create(byte errorCode, byte errorSubCode, byte[] data) {
        return new BGP4MessageNotificationImpl((short) (21 + data.length), (byte) BGP4Message.TYPE_NOTIFICATION, errorCode, errorSubCode, data);
    }

    byte getErrorCode();
    byte getErrorSubCode();
    byte[] getData();
}
