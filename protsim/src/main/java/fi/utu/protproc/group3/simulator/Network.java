package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.utils.NetworkAddress;

public interface Network {
    static Network create() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets all devices attached to the network (simulated ARP scan)
     */
    Iterable<EthernetInterface> getDevices();

    /**
     * Gets the network address for this network.
     */
    NetworkAddress getNetworkAddress();
}
