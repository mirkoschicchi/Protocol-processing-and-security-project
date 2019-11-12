package fi.utu.protproc.group3;

import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for a simple two-network simulation.
 */
public abstract class SimpleSimulationTestBase {
    protected Simulation simulation;
    protected Network clientNet;
    protected ClientNode client;
    protected Network serverNet;
    protected ServerNode server;
    protected Network ispLink;
    protected RouterNode clientRouter, serverRouter;

    @BeforeEach
    public void setupSimulation() {
        simulation = Simulation.create();
        clientNet = simulation.createNetwork();
        serverNet = simulation.createNetwork();
        ispLink = simulation.createNetwork();
        client = simulation.createClient(clientNet);
        server = simulation.createServer(serverNet);
        clientRouter = simulation.createRouter(clientNet, ispLink);
        serverRouter = simulation.createRouter(serverNet, ispLink);

        simulation.start(null);
    }

    @AfterEach
    public void tearDownSimulation() {
        simulation.stop();
    }
}
