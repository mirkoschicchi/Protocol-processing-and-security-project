package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;

public interface BGP4MessageUpdate extends BGP4Message {
    // TODO: (see below) Missing path attributes
    static BGP4MessageUpdate create(int withdrawnRoutesLength, List<NetworkAddress> withdrawnRoutes, int totalPathAttributeLength,
                                    List<NetworkAddress> networkLayerReachabilityInformation) {
        throw new UnsupportedOperationException();
    }

    short getWithdrawnRoutesLength();
    List<NetworkAddress> getWithdrawnRoutes();
    short getTotalPathAttributeLength();
    // TODO: Path attributes
    List<NetworkAddress> getNetworkLayerReachabilityInformation();
}
