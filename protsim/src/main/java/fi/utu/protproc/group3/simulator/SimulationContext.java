package fi.utu.protproc.group3.simulator;

import java.util.logging.Logger;

/**
 * Holds information about the simulation itself.
 */
public interface SimulationContext {
    /**
     * Gets the currently running simulation.
     */
    Simulation getSimulation();

    /**
     * Gets the simulator running the simulation.
     */
    SimulationEngine getSimulator();

    /**
     * Gets the logger for the current component.
     */
    Logger getLogger();
}
