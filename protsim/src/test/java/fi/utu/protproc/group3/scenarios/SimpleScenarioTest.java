package fi.utu.protproc.group3.scenarios;

import fi.utu.protproc.group3.ScenarioBasedTest;
import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base class for a simple two-network simulation.
 */
public abstract class SimpleScenarioTest extends ScenarioBasedTest {
    protected Simulation simulation;
    protected Network clientNet;
    protected ClientNode client;
    protected Network serverNet;
    protected ServerNode server;
    protected Network ispLink;
    protected RouterNode clientRouter, serverRouter;

    @BeforeEach
    public void setupSimulation() throws IOException {
        simulation = load("simple");
        clientNet = simulation.getNetwork("clientNet");
        serverNet = simulation.getNetwork("serverNet");
        ispLink = simulation.getNetwork("ispLink");
        client = simulation.getNode("client");
        server = simulation.getNode("server");
        clientRouter = simulation.getNode("clientRouter");
        serverRouter = simulation.getNode("serverRouter");

        assertNotNull(simulation);
        assertNotNull(clientNet);
        assertNotNull(serverNet);
        assertNotNull(ispLink);
        assertNotNull(client);
        assertNotNull(server);
        assertNotNull(clientRouter);
        assertNotNull(serverRouter);

        simulation.start(null);
    }

    @AfterEach
    public void tearDownSimulation() {
        simulation.stop();
    }
}
