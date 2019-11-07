package fi.utu.protproc.group3.protocols.bgp4;

import java.net.InetAddress;
import java.util.List;

public interface BGP4MessageUpdateImpl extends BGP4MessageImpl {
    // TODO: (see below) Missing path attributes
    static BGP4MessageUpdateImpl create(int withdrawnRoutesLength, List<InetAddress> withdrawnRoutes, int totalPathAttributeLength,
                                        List<InetAddress> networkLayerReachabilityInformation) {
        throw new UnsupportedOperationException();
    }

    int getWithdrawnRoutesLength();
    List<InetAddress> getWithdrawnRoutes();
    int getTotalPathAttributeLength();
    // TODO: Path attributes
    List<InetAddress> getNetworkLayerReachabilityInformation();
}
