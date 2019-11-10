package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageKeepalive extends BGP4Message {
    static BGP4MessageKeepalive create(byte[] marker, int length, short type) {
        throw new UnsupportedOperationException();
    }
}
