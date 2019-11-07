package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.nodes.NetworkNode;

import java.util.logging.Logger;

/**
 * Represents a running simulation.
 */
public interface Simulation {
    /**
     * Gets the root logger used for this simulation.
     */
    Logger getRootLogger();

    /**
     * Gets all the nodes in the current simulation.
     */
    Iterable<NetworkNode> getNodes();

    /**
     * Gets all the networks in the current simulation.
     */
    Iterable<Network> getNetworks();

    /**
     * Gets the online state of the given node.
     */
    boolean getNodeState(NetworkNode node);

    /**
     * Sets the online state of the given node to the given value.
     */
    void setNodeState(NetworkNode node, boolean online);

    /**
     * Gets the online state of the given network.
     */
    boolean getNetworkStae(Network network);

    /**
     * Sets the online state of the given network to the given value.
     */
    void setNetworkState(Network network, boolean online);
}
