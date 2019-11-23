package fi.utu.protproc.group3;

import fi.utu.protproc.group3.configuration.SimulationConfiguration;
import fi.utu.protproc.group3.simulator.Simulation;

import java.io.IOException;

public abstract class ScenarioBasedTest {
    protected Simulation load(String scenario) throws IOException {
        SimulationConfiguration config = null;
        var res = ScenarioBasedTest.class.getClassLoader().getResourceAsStream("scenarios/" + scenario + ".yaml");
        try {
            config = SimulationConfiguration.parse(res);
        } finally {
            if (res != null) res.close();
        }

        var result = Simulation.create(config);

        return result;
    }
}
