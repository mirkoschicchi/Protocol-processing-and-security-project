package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageTrustRate extends BGP4Message {
    static BGP4MessageTrustRate create() {
        return new BGP4MessageTrustRateImpl((short) 29, BGP4Message.TYPE_TRUSTRATE);
    }
}
