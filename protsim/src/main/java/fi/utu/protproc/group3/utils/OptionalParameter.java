package fi.utu.protproc.group3.utils;

/**
 * Class to represent Optional Parameter field of BGP4MessageOpenImpl
 */
public final class OptionalParameter {
    private short parmType;
    private short parmLength;
    private byte[] parmValue;

    public OptionalParameter(short parmType, short parmLength, byte[] parmValue) {
        this.parmType = parmType;
        this.parmLength = parmLength;
        this.parmValue = parmValue;
    }

    public short getParmType() {
        return parmType;
    }

    public short getParmLength() {
        return parmLength;
    }

    public byte[] getParmValue() {
        return parmValue;
    }
}
