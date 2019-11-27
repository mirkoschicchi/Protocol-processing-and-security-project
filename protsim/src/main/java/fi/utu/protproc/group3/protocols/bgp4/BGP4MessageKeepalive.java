package fi.utu.protproc.group3.protocols.bgp4;

public interface BGP4MessageKeepalive extends BGP4Message {
    static BGP4MessageKeepalive create() {
        return new BGP4MessageKeepaliveImpl((short) 19, BGP4Message.TYPE_KEEPALIVE);
    }
}
