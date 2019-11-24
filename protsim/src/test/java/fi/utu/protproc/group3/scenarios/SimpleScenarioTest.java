package fi.utu.protproc.group3.scenarios;

import fi.utu.protproc.group3.ScenarioBasedTest;
import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.RouterNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.simulator.Network;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Base class for a simple two-network simulation.
 */
public abstract class SimpleScenarioTest extends ScenarioBasedTest {
    protected Network clientNet;
    protected ClientNode client;
    protected Network serverNet;
    protected ServerNode server;
    protected Network ispLink;
    protected RouterNode clientRouter, serverRouter;

    @Override
    protected String getSimulation() {
        return "simple";
    }
}
