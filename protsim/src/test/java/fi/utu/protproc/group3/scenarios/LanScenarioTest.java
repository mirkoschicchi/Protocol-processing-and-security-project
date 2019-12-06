package fi.utu.protproc.group3.scenarios;

import fi.utu.protproc.group3.nodes.ClientNode;
import fi.utu.protproc.group3.nodes.ServerNode;
import fi.utu.protproc.group3.simulator.Network;

public abstract class LanScenarioTest extends ScenarioBasedTest {
    protected Network lan;
    protected ClientNode client;
    protected ServerNode server;

    @Override
    protected String getSimulation() {
        return "lan";
    }
}
