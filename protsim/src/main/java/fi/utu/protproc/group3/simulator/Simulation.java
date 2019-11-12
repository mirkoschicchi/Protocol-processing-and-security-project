package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Represents a running simulation.
 */
public interface Simulation {
    static Simulation create() {
        return new SimulationImpl();
    }

    Network createNetwork();

    RouterNode createRouter(Network... networks);

    ClientNode createClient(Network network);

    ServerNode createServer(Network network);

    /**
     * Gets the root logger used for this simulation.
     */
    Logger getRootLogger();

    /**
     * Gets all the nodes in the current simulation.
     */
    Collection<NetworkNode> getNodes();

    /**
     * Gets all the networks in the current simulation.
     */
    Collection<Network> getNetworks();

    /**
     * Gets a random server from the whole simulation.
     */
    ServerNode getRandomServer();

    void start(String pcapFile);

    void stop();
}
