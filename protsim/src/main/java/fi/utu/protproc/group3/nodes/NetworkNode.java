package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;

/**
 * Base interface for simulated network devices or routers.
 */
public interface NetworkNode {
    /**
     * Gets the hosts ethernet interfaces.
     */
    Iterable<EthernetInterface> getInterfaces();

    /**
     * Gets the background thread for the node (lifetime controlled by simulator).
     */
    Thread getBackgroundThread();
}
