package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class BGP4MessageUpdateImpl extends BGP4MessageImpl implements BGP4MessageUpdate {
    // Attribute Length is represented by two octets for all types
    // each Attribute type is composed by flags + one type code = 2 byte
    byte TYPE_FLAGS_WELL_KNOWN_TRANSITIVE = (byte) 0x50;
    byte TYPE_FLAGS_OPTIONAL_NON_TRANSITIVE = (byte) 0x90;
    byte TYPE_ORIGIN = (byte) 0x01;
    byte TYPE_ASPATH = (byte) 0x02;
    byte TYPE_NEXTHOP = (byte) 0x03;
    byte TYPE_MP_REACH_NLRI = (byte) 0x0E;   // 14
    byte TYPE_MP_UNREACH_NLRI = (byte) 0x0F;   // 15
    byte AS_SET = (byte) 0x01;
    byte AS_SEQUENCE = (byte) 0x02;
    byte SAFI_UNICAST = (byte) 0x01;
    byte AFI_IPV6 = (byte) 0x02;

    private List<NetworkAddress> withdrawnRoutes;
    private byte origin;
    private List<List<Short>> asPath;
    private IPAddress nextHop;
    private List<NetworkAddress> networkLayerReachabilityInformation;

    public BGP4MessageUpdateImpl(short length, byte type,
                                 List<NetworkAddress> withdrawnRoutes,
                                 byte origin, List<List<Short>> asPath, IPAddress nextHop,
                                 List<NetworkAddress> networkLayerReachabilityInformation) {
        super(length, type);
        this.withdrawnRoutes = withdrawnRoutes;
        this.origin = origin;
        this.asPath = asPath;
        this.nextHop = nextHop;
        this.networkLayerReachabilityInformation = networkLayerReachabilityInformation;
    }

    public short getLength() {
        short len = 19 + 2;
        len += 2; // Total Path Attribute Length
        len += getPathAttributesLength();

        return len;
    }

    public short getPathAttributesLength() {
        short len = 5; // origin

        len += 4;
        for (List<Short> asSet : getAsPath()) {
            len += 2;
            len += asSet.size() * 2;
        }

        // MP_REACH_NLRI
        len += 4 + 5 + getNextHop().toArray().length;
        for (NetworkAddress addr : getNetworkLayerReachabilityInformation())
            len += (1 + addr.getRequiredBytesForPrefix());

        // MP_UNREACH_NLRI
        len += 4 + 3;
        for (NetworkAddress addr : getWithdrawnRoutes())
            len += (1 + addr.getRequiredBytesForPrefix());

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
    public IPAddress getNextHop() {
        return nextHop;
    }

    @Override
    public List<NetworkAddress> getNetworkLayerReachabilityInformation() {
        return networkLayerReachabilityInformation;
    }

    @Override
    public byte[] serialize() {
        ByteBuffer serialized = ByteBuffer.allocate(getLength());
        serialized.put(super.serialize())
            .putShort((short)0);    // withdrawnRoutesLength

        //  Total Path Attribute Length
        serialized.putShort(getPathAttributesLength());

        // ORIGIN
        serialized.put(TYPE_FLAGS_WELL_KNOWN_TRANSITIVE)
                .put(TYPE_ORIGIN)
                .putShort((short)0x01)
                .put(getOrigin());

        // ASPATH
        short asPathLen = 0;
        for (List<Short> aslist : getAsPath()) {
            asPathLen += 2 + aslist.size() * 2;
        }
        serialized.put(TYPE_FLAGS_WELL_KNOWN_TRANSITIVE)
                .put(TYPE_ASPATH)
                .putShort(asPathLen);

        for (List<Short> asSet : getAsPath()) {
            serialized.put(AS_SET)
                    .put((byte)asSet.size());
            for(Short as : asSet) {
                serialized.putShort(as);
            }
        }

        // MP_REACH_NLRI
        int nlriByteLen = 0;
        for (NetworkAddress addr : getNetworkLayerReachabilityInformation())
            nlriByteLen += 1 + addr.getRequiredBytesForPrefix();
        serialized.put(TYPE_FLAGS_OPTIONAL_NON_TRANSITIVE)
                .put(TYPE_MP_REACH_NLRI)
                .putShort((short)(5 + getNextHop().toArray().length + nlriByteLen))
                .putShort(AFI_IPV6)
                .put(SAFI_UNICAST)
                .put((byte)getNextHop().toArray().length)
                .put(getNextHop().toArray())
                .put((byte)0);
        for (NetworkAddress addr : getNetworkLayerReachabilityInformation()) {
            serialized.put((byte)addr.getPrefixLength())
                    .put(Arrays.copyOfRange(addr.getAddress().toArray(), 0, addr.getRequiredBytesForPrefix()));
        }

        // MP_UNREACH_NLRI
        nlriByteLen = 0;
        for (NetworkAddress addr : getWithdrawnRoutes())
            nlriByteLen += 1 + addr.getRequiredBytesForPrefix();
        serialized.put(TYPE_FLAGS_OPTIONAL_NON_TRANSITIVE)
                .put(TYPE_MP_UNREACH_NLRI)
                .putShort((short)(3 + nlriByteLen))
                .putShort(AFI_IPV6)
                .put(SAFI_UNICAST);
        for (NetworkAddress addr : getWithdrawnRoutes()) {
            serialized.put((byte)addr.getPrefixLength())
                    .put(Arrays.copyOfRange(addr.getAddress().toArray(), 0, addr.getRequiredBytesForPrefix()));
        }

        return serialized.array();
    }
}
