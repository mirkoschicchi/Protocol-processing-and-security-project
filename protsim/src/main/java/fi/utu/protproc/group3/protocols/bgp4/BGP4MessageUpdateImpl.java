package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.NetworkAddress;

import java.nio.ByteBuffer;
import java.util.List;

public class BGP4MessageUpdateImpl extends BGP4MessageImpl implements BGP4MessageUpdate {
    // Attribute Length is represented by two octets for all types
    // each Attribute type is composed by flags + one type code = 2 byte
    byte TYPE_FLAGS = (short) 0x50;
    byte TYPE_ORIGIN = (short) 0x01;
    byte TYPE_ASPATH = (short) 0x02;
    byte TYPE_NEXTHOP = (short) 0x03;
    byte AS_SET = (short) 0x01;
    byte AS_SEQUENCE = (short) 0x02;

    private List<NetworkAddress> withdrawnRoutes;
    private byte origin;
    private List<List<Short>> asPath;
    private NetworkAddress nextHop;
    private List<NetworkAddress> networkLayerReachabilityInformation;

    public BGP4MessageUpdateImpl(short length, byte type,
                                 List<NetworkAddress> withdrawnRoutes,
                                 byte origin, List<List<Short>> asPath, NetworkAddress nextHop,
                                 List<NetworkAddress> networkLayerReachabilityInformation) {
        super(length, type);
        this.withdrawnRoutes = withdrawnRoutes;
        this.origin = origin;
        this.asPath = asPath;
        this.nextHop = nextHop;
        this.networkLayerReachabilityInformation = networkLayerReachabilityInformation;
    }

    public short getLength() {
        short len = 21;
        for (NetworkAddress addr : getWithdrawnRoutes())
            len += (1 + addr.getAddress().toArray().length);
        len += 2; // Total Path Attribute Length
        len += getPathAttributesLength();
        for (NetworkAddress addr : getNetworkLayerReachabilityInformation())
            len += (1 + addr.getAddress().toArray().length);

        return len;
    }

    public short getPathAttributesLength() {
        short len = 5; // origin

        len += 4;
        for (List<Short> asSet : getAsPath()) {
            len += 2;
            len += asSet.size() * 2;
        }

        len += 5 + getNextHop().getAddress().toArray().length;
        return len;
    }

    @Override
    public List<NetworkAddress> getWithdrawnRoutes() {
        return withdrawnRoutes;
    }

    @Override
    public byte getOrigin() {
        return origin;
    }

    @Override
    public List<List<Short>> getAsPath() {
        return asPath;
    }

    @Override
    public NetworkAddress getNextHop() {
        return nextHop;
    }

    @Override
    public List<NetworkAddress> getNetworkLayerReachabilityInformation() {
        return networkLayerReachabilityInformation;
    }

    @Override
    public byte[] serialize() {
        ByteBuffer serialized = ByteBuffer.allocate(getLength());
        serialized.put(super.serialize());

        serialized.putShort((short) (getWithdrawnRoutes().size() + getWithdrawnRoutes().size() * 16));
        for(NetworkAddress addr : getWithdrawnRoutes()) {
            serialized.put((byte)addr.getPrefixLength())
                    .put(addr.getAddress().toArray());
        }

        //  Total Path Attribute Length
        serialized.putShort(getPathAttributesLength());

        // ORIGIN
        serialized.put(TYPE_FLAGS)
                .put(TYPE_ORIGIN)
                .putShort((short)0x01)
                .put(getOrigin());

        // ASPATH
        short asPathLen = 0;
        for (List<Short> aslist : getAsPath()) {
            asPathLen += 2;
            for(Short as : aslist)
                asPathLen += 2;
        }
        serialized.put(TYPE_FLAGS)
                .put(TYPE_ASPATH)
                .putShort((short)(asPathLen));

        for (List<Short> asSet : getAsPath()) {
            serialized.put(AS_SET)
                    .put((byte)asSet.size());
            for(Short as : asSet) {
                serialized.putShort(as);
            }
        }

        // NEXTHOP
        serialized.put(TYPE_FLAGS)
                .put(TYPE_NEXTHOP)
                .putShort((short)(1 + getNextHop().getAddress().toArray().length))
                .put((byte)getNextHop().getPrefixLength())
                .put(getNextHop().getAddress().toArray());

        for(NetworkAddress addr : getNetworkLayerReachabilityInformation()) {
            serialized.put((byte)addr.getPrefixLength())
                    .put(addr.getAddress().toArray());
        }

        return serialized.array();
    }
}
