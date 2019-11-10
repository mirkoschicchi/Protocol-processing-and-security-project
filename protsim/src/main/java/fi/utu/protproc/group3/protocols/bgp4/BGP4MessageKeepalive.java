package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageKeepalive extends BGP4Message {
    static BGP4MessageKeepalive create(short length, byte type) {
        return new BGP4MessageKeepaliveImpl(length, type);
    }
}
