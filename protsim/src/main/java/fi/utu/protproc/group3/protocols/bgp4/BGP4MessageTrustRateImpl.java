package fi.utu.protproc.group3.protocols.bgp4;

public class BGP4MessageTrustRateImpl extends BGP4MessageImpl implements BGP4MessageTrustRate {
    public BGP4MessageTrustRateImpl(short length, byte type) {
         super(length, type);
    }
}
