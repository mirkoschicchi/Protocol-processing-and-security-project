package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;

public interface BGP4MessageUpdate extends BGP4Message {
    byte ORIGIN_FROM_IGP = (short) 0x00;
    byte ORIGIN_FROM_ESP = (short) 0x01;
    byte ORIGIN_INCOMPLETE = (short) 0x02;

    short getLength();
    short getPathAttributesLength();

    static BGP4MessageUpdate create(List<NetworkAddress> withdrawnRoutes, byte origin,
                                    ASPath asPath, IPAddress nextHop,
                                    List<NetworkAddress> networkLayerReachabilityInformation) {
        short len = 21;
        for (NetworkAddress addr : withdrawnRoutes)
            len += (1 + addr.getAddress().toArray().length);
        len += 5; // origin
        len += 4 + (asPath.length() * 4);
        len += 1 + nextHop.toArray().length;
        for (NetworkAddress addr : networkLayerReachabilityInformation)
            len += (5 + addr.getAddress().toArray().length);
        return new BGP4MessageUpdateImpl(len, BGP4Message.TYPE_UPDATE,
                withdrawnRoutes, origin, asPath, nextHop, networkLayerReachabilityInformation);
    }

    List<NetworkAddress> getWithdrawnRoutes();
    byte getOrigin();
    ASPath getAsPath();

    IPAddress getNextHop();
    List<NetworkAddress> getNetworkLayerReachabilityInformation();
}
