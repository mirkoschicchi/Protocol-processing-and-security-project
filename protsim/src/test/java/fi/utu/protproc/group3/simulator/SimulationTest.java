package fi.utu.protproc.group3.simulator;

import org.junit.jupiter.api.Test;

import java.util.logging.Level;

public class SimulationTest {
    @Test
    public void loadAndStartSimulation() {
        SimulationEngine engine = SimulationEngine.getInstance();
        Simulation sim = engine.setupSimulation("scenarios/simple.yml");

        // Disable logging during test case
        sim.getRootLogger().setLevel(Level.OFF);

        // Start simulation
        engine.startSimulation(null);

        // Shut down first network and first node defined in the scenario
        var testNet = sim.getNetworks().iterator().next();
        var testNode = sim.getNodes().iterator().next();
        sim.setNetworkState(testNet, false);
        sim.setNodeState(testNode, false);

        // Stop simulation
        engine.stopSimulation();
    }
}
