package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.NetworkAddress;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

public class BGP4MessageUpdateImpl extends BGP4MessageImpl implements BGP4MessageUpdate {
    private short withdrawnRoutesLength;
    private List<NetworkAddress> withdrawnRoutes;
    private short totalPathAttributeLength;
    private List<NetworkAddress> networkLayerReachabilityInformation;

    public BGP4MessageUpdateImpl(short length, byte type) {
        super(length, type);
    }

    @Override
    public short getWithdrawnRoutesLength() {
        return withdrawnRoutesLength;
    }

    @Override
    public List<NetworkAddress> getWithdrawnRoutes() {
        return withdrawnRoutes;
    }

    @Override
    public short getTotalPathAttributeLength() {
        return totalPathAttributeLength;
    }

    @Override
    public List<NetworkAddress> getNetworkLayerReachabilityInformation() {
        return networkLayerReachabilityInformation;
    }

    @Override
    public byte[] serialize() {
        byte[] serialized;
        serialized = ByteBuffer.allocate(21)
                .put(getMarker())
                .putShort(getLength())
                .put(getType())
                .putShort(getWithdrawnRoutesLength())
                .array();
        for(NetworkAddress addr : getWithdrawnRoutes()) {
            serialized = ByteBuffer.allocate(1 + addr.getAddress().getAddress().length)
                    .putInt(addr.getPrefixLength())
                    .put(addr.getAddress().getAddress())
                    .array();
        }
        serialized = ByteBuffer.allocate(2)
                .putShort(getTotalPathAttributeLength())
                .array();

        // TODO: Missing path attributes
        for(NetworkAddress addr : getNetworkLayerReachabilityInformation()) {
            serialized = ByteBuffer.allocate(1 + addr.getAddress().getAddress().length)
                    .putInt(addr.getPrefixLength())
                    .put(addr.getAddress().getAddress())
                    .array();
        }


        return serialized;
    }
}
