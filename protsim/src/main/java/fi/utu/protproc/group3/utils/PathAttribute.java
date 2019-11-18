package fi.utu.protproc.group3.utils;

public final class PathAttribute {
    public static byte ORIGIN = 0x1;
    public static byte AS_PATH = 0x2;
    public static byte NEXT_HOP = 0x3;
    public static byte MULTI_EXIT_DISC = 0x4;
    public static byte LOCAL_PREF = 0x5;
    public static byte ATOMIC_AGGREGATE = 0x6;
    public static byte AGGREGATOR = 0x7;

    private boolean optionalBit;
    private boolean transitiveBit;
    private boolean partialBit;

    /*
        If set to 0 the third octet contains the length of the attribute data in octets
        If set to 1 the third and fourth octets of the path attribute contain
         the length of the attribute data in octets.
     */
    private boolean extendedLengthBit;

    private short attributeType;
    private byte attributeFlags;
    private byte attributeTypeCode;


    public short getAttributeType() {
        return attributeType;
    }

}
