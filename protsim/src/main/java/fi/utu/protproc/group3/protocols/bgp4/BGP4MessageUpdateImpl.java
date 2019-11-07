package fi.utu.protproc.group3.protocols.bgp4;

import fi.utu.protproc.group3.utils.NetworkAddress;
import java.util.List;

public interface BGP4MessageUpdateImpl extends BGP4MessageImpl {
    // TODO: (see below) Missing path attributes
    static BGP4MessageUpdateImpl create(int withdrawnRoutesLength, List<NetworkAddress> withdrawnRoutes, int totalPathAttributeLength,
                                        List<NetworkAddress> networkLayerReachabilityInformation) {
        throw new UnsupportedOperationException();
    }

    int getWithdrawnRoutesLength();
    List<NetworkAddress> getWithdrawnRoutes();
    int getTotalPathAttributeLength();
    // TODO: Path attributes
    List<NetworkAddress> getNetworkLayerReachabilityInformation();
}
