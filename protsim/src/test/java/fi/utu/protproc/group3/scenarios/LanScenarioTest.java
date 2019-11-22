package fi.utu.protproc.group3.scenarios;

import fi.utu.protproc.group3.ScenarioBasedTest;
import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.simulator.Network;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class LanScenarioTest extends ScenarioBasedTest {
    protected Network lan;
    protected ClientNode client;
    protected ServerNode server;

    @Override
    protected String getSimulation() {
        return "lan";
    }
}
