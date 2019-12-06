package fi.utu.protproc.group3.scenarios;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.nodes.NetworkNode;
import fi.utu.protproc.group3.simulator.Network;
import fi.utu.protproc.group3.simulator.Simulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

abstract class ScenarioBasedTest {
    private Simulation simulation;

    private Simulation load(String scenario) throws IOException {
        SimulationConfiguration config;
        try (var res = ScenarioBasedTest.class.getClassLoader().getResourceAsStream("scenarios/" + scenario + ".yaml")) {
            config = SimulationConfiguration.parse(res);
        }

        return Simulation.create(config);
    }

    protected abstract String getSimulation();

    private void simulationLoaded() {
        Class<?> clazz = getClass();
        while (clazz != null) {
            for (var field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    if (Network.class.isAssignableFrom(field.getType())) {
                        var obj = simulation.getNetwork(field.getName());

                        assertNotNull(obj, "Could not load network " + field.getName() + " from scenario");

                        field.set(this, obj);
                    } else if (NetworkNode.class.isAssignableFrom(field.getType())) {
                        var obj = simulation.getNode(field.getName());

                        assertNotNull(obj, "Could not load node " + field.getName() + " from scenario");

                        field.set(this, obj);
                    }
                } catch (Exception e) {
                    fail(e);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    @BeforeEach
    public void setupSimulation() throws IOException {
        var simulationName = getSimulation();
        simulation = load(simulationName);

        assertNotNull(simulation, "Could not load simulation " + simulationName);

        simulationLoaded();
        simulation.start();
    }

    @AfterEach
    public void tearDownSimulation() {
        simulation.stop();
        simulation = null;
    }
}
