package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageNotification extends BGP4Message {
    // Error codes
    short ERR_CODE_MESSAGE_HEADER_ERROR = (short) 0x1;
    short ERR_CODE_OPEN_MESSAGE_ERROR = (short) 0x2;
    short ERR_CODE_UPDATE_MESSAGE_ERROR = (short) 0x3;
    short ERR_CODE_HOLD_TIMER_EXPIRED = (short) 0x4;
    short ERR_CODE_FINITE_STATE_MACHINE_ERROR = (short) 0x5;
    short ERR_CODE_CAESE = (short) 0x6;

    // Message Header Error subcodes
    short ERR_SUBCODE_CONNECTION_NOT_SYNC = (short) 0x1;
    short ERR_SUBCODE_BAD_MESSAGE_LENGTH = (short) 0x2;
    short ERR_SUBCODE_BAD_MESSAGE_TYPE = (short) 0x3;

    static BGP4MessageNotification create(short errorCode, short errorSubCode, byte[] data) {
        throw new UnsupportedOperationException();
    }

    short getErrorCode();
    short getErrorSubCode();
    byte[] getData();
}
