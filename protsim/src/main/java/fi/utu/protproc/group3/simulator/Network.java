package fi.utu.protproc.group3.simulator;

import fi.utu.protproc.group3.utils.NetworkAddress;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface Network extends RuntimeControllable {
    NetworkAddress getNetworkAddress();

    void addDevice(EthernetInterface intf);

    /**
     * Gets all devices attached to the network (simulated ARP scan)
     */
    Collection<EthernetInterface> getDevices();

    /**
     * Gets network name
     */
    String getNetworkName();

    /**
     * Gets the flux for the traffic on this network.
     */
    Flux<byte[]> getFlux();

    /**
     * Transmits a given frame via the network.
     */
    void transmit(byte[] pdu);

    int getAutonomousSystem();
}
