package fi.utu.protproc.group3.nodes;

import fi.utu.protproc.group3.simulator.EthernetInterface;

import java.util.Collection;

/**
 * Base interface for simulated network devices or routers.
 */
public interface NetworkNode {
    /**
     * Gets the hosts ethernet interfaces.
     */
    Collection<EthernetInterface> getInterfaces();

    void start();

    void shutdown();
}
