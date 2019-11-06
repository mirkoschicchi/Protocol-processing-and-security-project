package fi.utu.protproc.group3.simulator;

/**
 * Base interface for simulated network devices or routers.
 */
public interface Host {
    /**
     * Gets the hosts ethernet interfaces.
     */
    Iterable<EthernetInterface> getInterfaces();
}
