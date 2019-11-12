package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.simulator.EthernetInterface;
import fi.utu.protproc.group3.utils.NetworkAddress;

public interface RouterNode extends NetworkNode {
    public static RouterNode create(Iterable<EthernetInterface> interfaces, Thread backgroundThread, NetworkAddress networkAddress) {
        return new RouterNodeImpl(interfaces, backgroundThread, networkAddress);
    }

    void route();

    RoutingTable getRoutingTable();
}
