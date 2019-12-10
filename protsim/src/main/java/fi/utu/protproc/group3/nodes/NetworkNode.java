package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.protocols.tcp.DatagramHandler;
import fi.utu.protproc.group3.routing.RoutingTable;
import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.util.Collection;

/**
 * Base interface for simulated network devices or routers.
 */
public interface NetworkNode extends fi.utu.protproc.group3.simulator.RuntimeControllable {
    static String getNodeTypeName(NetworkNode node) {
        if (node == null) {
            return "n/a";
        } else if (node instanceof ClientNode) {
            return "Client";
        } else if (node instanceof ServerNode) {
            return "Server";
        } else if (node instanceof RouterNode) {
            return "Router";
        } else if (node instanceof TunTapNodeImpl) {
            return "TUN/TAP";
        } else {
            return "Unknown node type: " + node.getClass().getName();
        }
    }

    String getHostname();
    Collection<EthernetInterface> getInterfaces();

    RoutingTable getRoutingTable();
    DatagramHandler getTcpHandler();
}
