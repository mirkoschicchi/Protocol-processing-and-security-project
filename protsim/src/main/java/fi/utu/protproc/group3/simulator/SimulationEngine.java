package fi.utu.protproc.group3.simulator;

import java.util.logging.Logger;

/**
 * Represents the simulator responsible to manage all components and their connections.
 */
public interface SimulationEngine {
    static SimulationEngine getInstance() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new simulation with the given scenario file.
     */
    Simulation setupSimulation(String scenarioFile);

    /**
     * Starts the current simulation exporting the traffic into the given file.
     * @param pcapFilename The pcap file name to write to. Can be used with FIFO for live updates in wireshark
     *                     (-k option).
     */
    void startSimulation(String pcapFilename);

    /**
     * Stops the current simulation (shuts down all nodes) and finishes the pcap export.
     */
    void stopSimulation();
}
