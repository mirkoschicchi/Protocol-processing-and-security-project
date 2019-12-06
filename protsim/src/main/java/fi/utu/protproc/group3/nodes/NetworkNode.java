package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.util.Collection;

/**
 * Base interface for simulated network devices or routers.
 */
public interface NetworkNode {
    String getHostname();
    Collection<EthernetInterface> getInterfaces();

    RoutingTable getRoutingTable();

    boolean nodeIsRunning();
    void start();
    void shutdown();

    DatagramHandler getTcpHandler();
}
