package fi.utu.protproc.group3.routing;

import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.ASPath;
import fi.utu.protproc.group3.utils.IPAddress;
import fi.utu.protproc.group3.utils.NetworkAddress;

import java.util.List;


/**
 * This represents a row in the routing table
 */
public interface TableRow {
    static TableRow create(NetworkAddress prefix, IPAddress nextHop, int metric, EthernetInterface eInterface) {
        return new TableRowImpl(prefix, nextHop, metric, eInterface);
    }

    static TableRowImpl create(NetworkAddress prefix, IPAddress nextHop, int metric, int bgpPeer,
                               EthernetInterface eInterface, ASPath asPath, double neighborTrust) {
        return new TableRowImpl(prefix, nextHop, metric, bgpPeer, eInterface, asPath, neighborTrust);
    }

    NetworkAddress getPrefix();
    IPAddress getNextHop();
    double getCalculatedMetric();
    int getBgpPeer();

    EthernetInterface getInterface();
    ASPath getAsPath();

    void setTrust(double trust);
}
