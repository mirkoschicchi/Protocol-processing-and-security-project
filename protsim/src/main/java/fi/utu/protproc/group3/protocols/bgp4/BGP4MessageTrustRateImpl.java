package fi.utu.protproc.group3.protocols.bgp4;

import java.nio.ByteBuffer;

public class BGP4MessageTrustRateImpl extends BGP4MessageImpl implements BGP4MessageTrustRate {
    private int inheritTrust;

    public BGP4MessageTrustRateImpl(short length, byte type, int inheritTrust) {
         super(length, type);
         this.inheritTrust = inheritTrust;
    }

    @Override
    public int getInheritTrust() {
        return inheritTrust;
    }

    @Override
    public byte[] serialize() {
        byte[] serialized;
        serialized = ByteBuffer.allocate(19 + 4)
                .put(super.serialize())
                .putInt(getInheritTrust())
                .array();

        return serialized;
    }
}
