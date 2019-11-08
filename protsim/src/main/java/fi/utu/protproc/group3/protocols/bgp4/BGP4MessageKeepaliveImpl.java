package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageKeepaliveImpl extends BGP4MessageImpl {
    static BGP4MessageKeepaliveImpl create(byte[] marker, int length, short type) {
        throw new UnsupportedOperationException();
    }
}
