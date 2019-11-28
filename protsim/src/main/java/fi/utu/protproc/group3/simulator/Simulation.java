package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.configuration.*;
import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.nodes.ServerNode;

import java.util.Collection;
import java.util.logging.Logger;

/**
 * Represents a running simulation.
 */
public interface Simulation {
    static Simulation create(SimulationConfiguration configuration) {
        var result = new SimulationImpl();

        return result.load(configuration);
    }

    /**
     * Gets the root logger used for this simulation.
     */
    Logger getRootLogger();

    <T extends NetworkNode> T getNode(String name);

    /**
     * Gets all the nodes in the current simulation.
     * @return
     */
    Collection<NetworkNode> getNodes();

    Network getNetwork(String name);

    /**
     * Gets all the networks in the current simulation.
     * @return
     */
    Collection<Network> getNetworks();

    /**
     * Gets a random server from the whole simulation.
     */
    ServerNode getRandomServer();

    void start(String pcapFile, String network);
    void start();
    void show();
    void close();
    void stop();
}
