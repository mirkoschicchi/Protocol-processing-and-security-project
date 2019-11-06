package fi.utu.protproc.group3.simulator;

public interface Network {
    static Network create() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets all devices attached to the network (simulated ARP scan)
     */
    Iterable<EthernetInterface> getDevices();
}
