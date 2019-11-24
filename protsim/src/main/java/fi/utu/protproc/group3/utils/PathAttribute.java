package fi.utu.protproc.group3.utils;

public final class PathAttribute {
    public static byte ORIGIN = 0x1;
    public static byte AS_PATH = 0x2;
    public static byte NEXT_HOP = 0x3;
    public static byte MULTI_EXIT_DISC = 0x4;
    public static byte LOCAL_PREF = 0x5;
    public static byte ATOMIC_AGGREGATE = 0x6;
    public static byte AGGREGATOR = 0x7;

    private byte optionalBit = (byte) (1 << 7);
    private byte transitiveBit = (byte) (1 << 6);
    private byte partialBit = (byte) (1 << 5);
    private byte extendedLengthBit = (byte) (1 << 4);

    private byte attributeFlags;
    private byte attributeTypeCode;
    private short attributeDataLength;

    public static byte[] create(byte attributeFlags, byte attributeTypeCode, short attributeDataLength) {
        byte[] pathAttributeArray = null;

        pathAttributeArray[0] = attributeFlags;
        pathAttributeArray[1] = attributeTypeCode;

        if((attributeFlags & (byte) 0x10) == 1) {
            pathAttributeArray[3] = (byte) (attributeDataLength >> 8);
            pathAttributeArray[4] = (byte) attributeDataLength;
        } else {
            pathAttributeArray[3] = (byte) attributeDataLength;
        }



        return pathAttributeArray;
    }


    public PathAttribute (byte attributeFlags, byte attributeTypeCode, short attributeDataLength) {
        this.attributeFlags = attributeFlags;
        this.attributeTypeCode = attributeTypeCode;
        this.attributeDataLength = attributeDataLength;

        if((attributeFlags & (byte) 0x10) == 1) {

        } else {

        }

    }



    public byte getAttributeFlags() {
        return attributeFlags;
    }

    public byte getAttributeTypeCode() {
        return attributeTypeCode;
    }

    public short getAttributeDataLength() {
        return attributeDataLength;
    }
}
