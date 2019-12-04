package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageTrustRate extends BGP4Message {
    static BGP4MessageTrustRate create(int inheritTrust) {
        return new BGP4MessageTrustRateImpl((short) 1, BGP4Message.TYPE_TRUSTRATE, inheritTrust);
    }

    int getInheritTrust();
}
